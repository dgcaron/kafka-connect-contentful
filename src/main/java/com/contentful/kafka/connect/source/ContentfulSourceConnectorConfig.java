package com.contentful.kafka.connect.source;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

import java.util.Map;

public class ContentfulSourceConnectorConfig  extends AbstractConfig {

    public static final String SPACE_CONFIG = "contentful.space";

    public static final String CMA_ACCESSTOKEN_CONFIG = "contentful.cma.token";

    public static final String CDA_ACCESSTOKEN_CONFIG = "contentful.cda.token";

    public static final String CONNECTOR_GROUP = "Connector";

    public static final String POLL_INTERVAL_MS_CONFIG = "poll.interval.ms";

    private static final String POLL_INTERVAL_MS_DOC = "Frequency in ms to poll for new data";

    public static final int POLL_INTERVAL_MS_DEFAULT = 5000;

    private static final String POLL_INTERVAL_MS_DISPLAY = "Poll Interval (ms)";


    public static final String POLL_INTERVAL_CONTENTTYPES_MS_CONFIG = "poll.interval.contenttypes.ms";

    private static final String POLL_INTERVAL_CONTENTTYPES_MS_DOC = "Frequency in ms to poll for new or removed content types";

    public static final int POLL_INTERVAL_CONTENTTYPES_MS_DEFAULT = 60 * 1000;

    private static final String POLL_INTERVAL_CONTENTTYPES_MS_DISPLAY = "Metadata Change Monitoring Interval (ms)";



    public static final String TOPIC_ENTRIES_NAME_CONFIG = "topic.contenttypes.name";

    private static final String TOPIC_ENTRIES_NAME_DOC = "Name of the Kafka topic to publish contenttypes to.";

    private static final String TOPIC_ENTRIES_NAME_DISPLAY = "Topic Name";

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

        config.define(SPACE_CONFIG, Type.STRING, Importance.HIGH,"");

        config.define(CMA_ACCESSTOKEN_CONFIG, Type.STRING, Importance.HIGH,"");
        config.define(CDA_ACCESSTOKEN_CONFIG, Type.STRING, Importance.HIGH,"");

        config.define(
                POLL_INTERVAL_MS_CONFIG,
                Type.INT,
                POLL_INTERVAL_MS_DEFAULT,
                Importance.HIGH,
                POLL_INTERVAL_MS_DOC,
                CONNECTOR_GROUP,
                1,
                Width.SHORT,
                POLL_INTERVAL_MS_DISPLAY
        );

        config.define(
                POLL_INTERVAL_CONTENTTYPES_MS_CONFIG,
                Type.INT,
                POLL_INTERVAL_CONTENTTYPES_MS_DEFAULT,
                Importance.HIGH,
                POLL_INTERVAL_CONTENTTYPES_MS_DOC,
                CONNECTOR_GROUP,
                1,
                Width.SHORT,
                POLL_INTERVAL_CONTENTTYPES_MS_DISPLAY
        );

        config.define(
                TOPIC_ENTRIES_NAME_CONFIG,
                Type.STRING,
                Importance.HIGH,
                TOPIC_ENTRIES_NAME_DOC,
                CONNECTOR_GROUP,
                4,
                Width.MEDIUM,
                TOPIC_ENTRIES_NAME_DISPLAY
        );

        config.define(
                TOPIC_ASSETS_NAME_CONFIG,
                Type.STRING,
                Importance.HIGH,
                TOPIC_ASSETS_NAME_DOC,
                CONNECTOR_GROUP,
                4,
                Width.MEDIUM,
                TOPIC_ASSETS_NAME_DISPLAY
        );

        return config;
    }
}
