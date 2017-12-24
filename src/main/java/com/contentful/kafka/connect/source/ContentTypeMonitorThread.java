package com.contentful.kafka.connect.source;

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAContentType;
import org.apache.kafka.connect.connector.ConnectorContext;
import org.apache.kafka.connect.errors.ConnectException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContentTypeMonitorThread extends Thread {

    private final ConnectorContext context;

    private final String space;

    private final String accessToken;

    private final long pollingInterval;

    private final long startUpTimeout;

    private final CMAClient client;


    List<String> contentTypes;

    private final CountDownLatch stop;

    public ContentTypeMonitorThread(ConnectorContext context, String space, String accessToken, long pollingInterval, long startUpTimeout)
    {
        this.context = context;
        this.space = space;
        this.accessToken = accessToken;
        this.pollingInterval = pollingInterval;
        this.startUpTimeout = startUpTimeout;

        this.client = new CMAClient
                .Builder()
                .setAccessToken(accessToken)
                .build();

        this.stop = new CountDownLatch(1);
    }

    @Override
    public void run() {
        while(this.stop.getCount() > 0) {
            try {
                if (updateContentTypes()) {
                    this.context.requestTaskReconfiguration();
                }
            } catch (Exception e) {
                //this.context.raiseError(e);
                throw e;
            }

            try {
                boolean shuttingDown = this.stop.await(this.pollingInterval, TimeUnit.MILLISECONDS);
                if (shuttingDown) {
                    return;
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public synchronized List<String> getContentTypes() {

        long started = System.currentTimeMillis();
        long now = started;
        while (this.contentTypes == null && now - started < this.startUpTimeout) {
            try {
                wait(this.startUpTimeout - (now - started));
            } catch (InterruptedException e) {

            }
            now = System.currentTimeMillis();
        }
        if (this.contentTypes == null) {
            throw new ConnectException("ContentTypes could not be updated quickly enough.");
        }

        return this.contentTypes;

    }

    private synchronized boolean updateContentTypes(){

        CMAArray<CMAContentType> CMAcontentTypes = this.client.contentTypes().fetchAll(this.space);

        final List<String> contentTypes = new ArrayList<>(CMAcontentTypes.getTotal()+1);
        for(CMAContentType contentType : CMAcontentTypes.getItems()){
            contentTypes.add(contentType.getName());
        }

        contentTypes.add(ContentfulSourceTaskConfig.ASSET_NAME_KEY);

        if(!contentTypes.equals(this.contentTypes)) {
            List<String> previous = this.contentTypes;
            this.contentTypes = contentTypes;
            notifyAll();

            return previous != null;
        }

        return false;
    }

    public void shutdown() {
        this.stop.countDown();
    }
}
