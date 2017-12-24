package com.contentful.kafka.connect.source;

import java.lang.Object;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.SynchronizedSpace;
import com.contentful.kafka.connect.utils.ContentfulSchemas;
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

    private String topicAssets;

    private String topicContentTypes;

    private String syncToken;

    private Map offset;

    public void start(Map<String, String> props) {
        config = new ContentfulSourceConnectorConfig(props);

        space = config.getString(ContentfulSourceConnectorConfig.SPACE_CONFIG);
        topicContentTypes = config.getString(ContentfulSourceConnectorConfig.TOPIC_CONTENTTYPES_NAME_CONFIG);
        topicAssets = config.getString(ContentfulSourceConnectorConfig.TOPIC_ASSETS_NAME_CONFIG);
        String token = config.getString(ContentfulSourceConnectorConfig.ACCESSTOKEN_CONFIG);

        if(space.isEmpty() || token.isEmpty())
        {
            throw new ConnectException("space and token are required");
        }

        client = CDAClient.builder()
                .setSpace(space)
                .setToken(token)
                .build();

        List<Map<String, String>> partitions = getPartitions();
        if(partitions != null && partitions.size() > 0) {
            Map<Map<String, String>, Map<String, Object>> offsets = context.offsetStorageReader().offsets(partitions);
            Map<String, Object> offset = offsets == null ? null : offsets.get(partitions.get(0));

            if (offset != null) {
                syncToken = (String) offset.get("position");
            }
        }
        stop = new AtomicBoolean(false);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException
    {
        while(!stop.get()) {
            final List<SourceRecord> results = new LinkedList<>();
            SynchronizedSpace deltas;
            if(syncToken.isEmpty()){
               deltas =  client.sync().fetch();
            }
            else {
                deltas = client.sync(syncToken).fetch();
            }

            if(deltas != null) {
                // hold it locally
                syncToken = deltas.nextSyncUrl();
                offset = Collections.singletonMap("position", syncToken);

                if(deltas.entries() != null)
                {
                    for(Map.Entry<String, CDAEntry> entry : deltas.entries().entrySet())
                    {
                        String key = entry.getKey();
                        CDAEntry content = entry.getValue();
                        Map<String, String> partition =  Collections.singletonMap(ContentfulSourceTaskConfig.CONTENTTYPE_NAME_KEY, content.contentType().name());

                        results.add(new SourceRecord(partition, offset, topicContentTypes, ContentfulSchemas.Key, key, ContentfulSchemas.Entry, content));
                    }
                }

                if(deltas.assets() != null)
                {
                    for(Map.Entry<String, CDAAsset> asset : deltas.assets().entrySet())
                    {
                        String key = asset.getKey();
                        Map<String, String> partition =  Collections.singletonMap(ContentfulSourceTaskConfig.ASSET_NAME_KEY, ContentfulSourceTaskConfig.ASSET_NAME_KEY);
                        results.add(new SourceRecord(partition, offset, topicAssets, ContentfulSchemas.Key, key, ContentfulSchemas.Asset, asset));
                    }
                }

                return results;
            }
        }

        return null;
    }

    private List<Map<String, String>> getPartitions(){
        List<String> contentTypes = config.getList(ContentfulSourceTaskConfig.CONTENTTYPES_CONFIG);

        List<Map<String, String>> partitions = new ArrayList<>(contentTypes.size());
        for (String contentType : contentTypes) {
            Map<String, String> partition =
                    Collections.singletonMap(ContentfulSourceTaskConfig.CONTENTTYPE_NAME_KEY, contentType);
            partitions.add(partition);
        }

        partitions.add(Collections.singletonMap(ContentfulSourceTaskConfig.ASSET_NAME_KEY, ContentfulSourceTaskConfig.ASSET_NAME_KEY));
        return partitions;
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
