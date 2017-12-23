package io.confluent.connect.contentful.utils;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class ContentfulSchemas {

    public static final Schema Key = Schema.STRING_SCHEMA;

    public static final Schema Entry =
            SchemaBuilder.struct().name("com.contentful.java.cda.CDAEntry")
                    .field("id", Schema.STRING_SCHEMA)
                    .field("type", Schema.INT16_SCHEMA)
                    .field("contentType", Schema.BYTES_SCHEMA)
                    .field("attrs", Schema.BYTES_SCHEMA)
                    .field("locale", Schema.STRING_SCHEMA)
                    .field("defaultLocale", Schema.STRING_SCHEMA)
                    .field("rawFields", Schema.BYTES_SCHEMA)
                    .build();

    public static final Schema Asset =
            SchemaBuilder.struct().name("com.contentful.java.cda.CDAAsset")
                    .field("id", Schema.STRING_SCHEMA)
                    .field("type", Schema.STRING_SCHEMA)
                    .field("title", Schema.STRING_SCHEMA)
                    .field("url", Schema.STRING_SCHEMA)
                    .field("attrs", Schema.BYTES_SCHEMA)
                    .field("mimeType", Schema.STRING_SCHEMA)
                    .field("locale", Schema.STRING_SCHEMA)
                    .field("defaultLocale", Schema.STRING_SCHEMA)
                    .field("rawFields", Schema.BYTES_SCHEMA)
                    .build();
}
