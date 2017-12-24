package com.contentful.kafka.connect.source;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.Map;

public class ContentfulSourceConnectorConfig  extends AbstractConfig {

    public static final String SPACE_CONFIG = "contentful.space";

    public static final String ACCESSTOKEN_CONFIG = "contentful.token";

    public static final String POLL_INTERVAL_MS_CONFIG = "poll.interval.ms";

    public static final String POLL_INTERVAL_CONTENTTYPES_MS_CONFIG = "poll.interval.contenttypes.ms";

    public static final String TOPIC_CONTENTTYPES_NAME_CONFIG = "topic.contenttypes.name";

    private static final String TOPIC_CONTENTTYPES_NAME_DOC = "Name of the Kafka topic to publish contenttypes to.";

    private static final String TOPIC_CONTENTTYPES_NAME_DISPLAY = "Topic Name";

    public static final String TOPIC_ASSETS_NAME_CONFIG = "topic.assets.name";

    private static final String TOPIC_ASSETS_NAME_DOC = "Name of the Kafka topic to publish assets to.";

    private static final String TOPIC_ASSETS_NAME_DISPLAY = "Topic Name";

    public static final ConfigDef CONFIG_DEF = Create();

    protected ContentfulSourceConnectorConfig(ConfigDef subclassConfigDef, Map<String, String> props) {
        super(subclassConfigDef, props);
    }

    public ContentfulSourceConnectorConfig(Map<String, String> props) {
        super(CONFIG_DEF, props);
    }

    public static ConfigDef Create() {
        ConfigDef config = new ConfigDef();

        return config;
    }
}
