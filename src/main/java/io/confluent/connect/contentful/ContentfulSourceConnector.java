package io.confluent.connect.contentful;

import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAContentType;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import io.confluent.connect.contentful.source.ContentfulSourceTask;
import io.confluent.connect.contentful.source.ContentfulSourceConnectorConfig;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

public class ContentfulSourceConnector  extends SourceConnector {

    private ContentfulSourceConnectorConfig config;

    private CMAClient client;

    @Override
    public Class<? extends Task> taskClass() {
        return ContentfulSourceTask.class;
    }

    public ConfigDef config() {
        return ContentfulSourceConnectorConfig.CONFIG_DEF;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        return null;
    }

    @Override
    public void start(Map<String, String> props) {
        config = new ContentfulSourceConnectorConfig(props);

        String space = config.getString(ContentfulSourceConnectorConfig.SPACE_CONFIG);
        String token = config.getString(ContentfulSourceConnectorConfig.ACCESSTOKEN_CONFIG);

        client = new CMAClient
                .Builder()
                .setAccessToken(token)
                .build();

       CMAArray<CMAContentType> result;
        result = client.contentTypes().fetchAll(space);
    }

    @Override
    public void stop() {
    }

    public String version() {
        return null;
    }
}
