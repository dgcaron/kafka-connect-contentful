package io.confluent.connect.contentful.source;

import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class ContentfulSourceTaskConfig  extends ContentfulSourceConnectorConfig {

    public static final String CONTENTTYPES_CONFIG = "contenttypes";
    private static final String CONTENTTYPES_DOC = "List of contenttypes for this task to watch for changes.";

    public static final String CONTENTTYPE_NAME_KEY = "contenttype";

    public static final String ASSET_NAME_KEY = "asset";

    static ConfigDef config = Create()
            .define(CONTENTTYPES_CONFIG, ConfigDef.Type.LIST, ConfigDef.Importance.HIGH,CONTENTTYPES_DOC);

    public ContentfulSourceTaskConfig(Map<String, String> props) {
        super(config, props);
    }
}
