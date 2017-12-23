package io.confluent.connect.contentful.source;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.Map;

public class ContentfulSourceConnectorConfig  extends AbstractConfig {

    public static final String SPACE_CONFIG = "contentful.space";

    public static final String ACCESSTOKEN_CONFIG = "contentful.token";

    public static final String TOPIC_PREFIX_CONFIG = "topic.prefix";

    public static final ConfigDef CONFIG_DEF = Create();

    public ContentfulSourceConnectorConfig(Map<String, String> props) {
        super(CONFIG_DEF, props);
    }

    public static ConfigDef Create() {
        ConfigDef config = new ConfigDef();

        return config;
    }
}
