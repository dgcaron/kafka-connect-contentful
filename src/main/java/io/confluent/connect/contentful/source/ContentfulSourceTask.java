package io.confluent.connect.contentful.source;

import java.lang.Object;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.SynchronizedSpace;
import io.confluent.connect.contentful.utils.ContentfulSchemas;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContentfulSourceTask  extends SourceTask {

    private ContentfulSourceConnectorConfig config;

    private CDAClient client;

    private AtomicBoolean stop;

    private String space;

    private String topicPrefix;

    private String syncToken;

    private Map offset;

    public void start(Map<String, String> props) {
        config = new ContentfulSourceConnectorConfig(props);

        space = config.getString(ContentfulSourceConnectorConfig.SPACE_CONFIG);
        topicPrefix = config.getString(ContentfulSourceConnectorConfig.TOPIC_PREFIX_CONFIG);
        String token = config.getString(ContentfulSourceConnectorConfig.ACCESSTOKEN_CONFIG);

        if(space.isEmpty() || token.isEmpty())
        {
            throw new ConnectException("space and token are required");
        }

        client = CDAClient.builder()
                .setSpace(space)
                .setToken(token)
                .build();


        Map partition = Collections.singletonMap("space", space);
        List<Map<String, String>> partitions = new ArrayList<>(1);

        Map<Map<String, String>, Map<String, Object>> offsets  = context.offsetStorageReader().offsets(partitions);

        Map sourceOffset = Collections.singletonMap("position", syncToken);

        stop = new AtomicBoolean(false);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException
    {
        Map partition = Collections.singletonMap("space", space);

    while(!stop.get())
        {
            final List<SourceRecord> results = new LinkedList<>();
            SynchronizedSpace deltas;
            if(syncToken.isEmpty()){
               deltas =  client.sync().fetch();
            }
            else {
                deltas = client.sync(syncToken).fetch();
            }

            if(deltas != null)
            {

                if(deltas.entries() != null)
                {
                    for(Map.Entry<String, CDAEntry> entry : deltas.entries().entrySet())
                    {
                        String key = entry.getKey();
                        CDAEntry content = entry.getValue();
                        //results.add(new SourceRecord(partition,offset,topicPrefix+".updates", ContentfulSchemas.Key, ((Object)key), ContentfulSchemas.Entry,((Object)content));
                    }
                }

                if(deltas.assets() != null)
                {
                    for(Map.Entry<String, CDAAsset> asset : deltas.assets().entrySet())
                    {
                        String key = asset.getKey();
                        CDAAsset content = asset.getValue();
                        //results.add(new SourceRecord(partition,offset,topicPrefix+".updates", ContentfulSchemas.Key, ((Object)key), ContentfulSchemas.Asset, content);
                    }
                }


                // process the entries

                // hold it locally
                syncToken = deltas.nextSyncUrl();
                offset = Collections.singletonMap("position", syncToken);

                return results;
            }
        }

        return null;
    }

    @Override
    public synchronized void stop() {
        if (stop != null) {
            stop.set(true);
        }

        client = null;
    }

    public String version() {
        return null;
    }
}
