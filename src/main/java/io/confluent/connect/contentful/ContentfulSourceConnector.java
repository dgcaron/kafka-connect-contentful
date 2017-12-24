package io.confluent.connect.contentful;

import io.confluent.connect.contentful.source.ContentTypeMonitorThread;
import io.confluent.connect.contentful.source.ContentfulSourceTaskConfig;
import io.confluent.connect.contentful.utils.StringUtils;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import io.confluent.connect.contentful.source.ContentfulSourceTask;
import io.confluent.connect.contentful.source.ContentfulSourceConnectorConfig;
import org.apache.kafka.connect.util.ConnectorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentfulSourceConnector  extends SourceConnector {

    private final long MAX_TIMEOUT = 10000L;

    private Map<String, String> configProperties;

    private ContentfulSourceConnectorConfig config;

    private ContentTypeMonitorThread monitor;

    @Override
    public Class<? extends Task> taskClass() {
        return ContentfulSourceTask.class;
    }

    public ConfigDef config() {
        return ContentfulSourceConnectorConfig.CONFIG_DEF;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<String> contentTypes = monitor.getContentTypes();

        List<List<String>> contentTypesGrouped = ConnectorUtils.groupPartitions(contentTypes, Math.min(contentTypes.size(), maxTasks));

        List<Map<String, String>> taskConfigs = new ArrayList<>(contentTypesGrouped.size());

        for (List<String> taskContentTypes : contentTypesGrouped) {
            Map<String, String> taskProps = new HashMap<>(this.configProperties);
            taskProps.put(ContentfulSourceTaskConfig.CONTENTTYPES_CONFIG, StringUtils.join(taskContentTypes, ","));
            taskConfigs.add(taskProps);
        }

        return taskConfigs;
    }

    @Override
    public void start(Map<String, String> props) {
        configProperties = props;
        config = new ContentfulSourceConnectorConfig(props);

        String space = config.getString(ContentfulSourceConnectorConfig.SPACE_CONFIG);
        String token = config.getString(ContentfulSourceConnectorConfig.ACCESSTOKEN_CONFIG);

        long pollingInterval = config.getLong(ContentfulSourceConnectorConfig.POLL_INTERVAL_CONTENTTYPES_MS_CONFIG);

        monitor = new ContentTypeMonitorThread(this.context,space,token, pollingInterval,MAX_TIMEOUT);
    }

    @Override
    public void stop() {
        monitor.shutdown();
        try {
            monitor.join(MAX_TIMEOUT);
        } catch (InterruptedException e) {
            // Ignore, shouldn't be interrupted
        }
    }

    public String version() {
        return null;
    }
}
