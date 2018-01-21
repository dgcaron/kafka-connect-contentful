package com.contentful.kafka.connect.utils;

import com.contentful.java.cda.*;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.SchemaBuilderException;

import java.util.Map;

public class ContentfulSchemas {

    public static Struct convert(CDAEntry entry, Schema schema)
    {
        final Struct record = new Struct(schema);

        final Struct sys = new Struct(schema.field("sys").schema());
        for(Map.Entry<String, Object> attr : entry.attrs().entrySet()) {
            String key = attr.getKey();
            Object obj = attr.getValue();

            if (obj instanceof Map) {
                Schema fieldSchema = schema.field("sys").schema().field(key).schema();
                sys.put(key, convert((Map<String,Object>) obj,fieldSchema));
            } else {
                sys.put(key, obj);

            }
        }

        record.put("sys", sys);
        record.put("fields", convert(entry.rawFields(),schema.field("fields").schema(),"en-US"));

        return  record;
    }

    private static Struct convert(Map<String,Object> map, Schema schema){
        Struct struct = new Struct(schema);
        for (Map.Entry<String,Object> entry :  map.entrySet()){
            String key = entry.getKey();
            Object obj = entry.getValue();

            if (obj instanceof Map) {
                struct.put(key, convert((Map<String,Object>)obj,schema.field(key).schema()));
            }
            else {
                struct.put(key, obj);
            }
        }

        return struct;
    }

    private static Struct convert(Map<String,Object> map, Schema schema, String locale){
        Struct struct = new Struct(schema);
        for (Map.Entry<String,Object> entry :  map.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();

            Schema fieldSchema = schema.field(key).schema();

            if (fieldSchema != null) {
                if (fieldSchema.field(locale) != null && fieldSchema.field(locale).schema() == Schema.STRING_SCHEMA) {
                    Struct field = new Struct(SchemaBuilder.struct().field(locale, Schema.STRING_SCHEMA).build());
                    field.put(locale, ((Map<String, String>) obj).get(locale));

                    struct.put(key, field);

                    //struct.put(key, convert((Map<String,Object>)obj,schema.field(key).schema()));
                } else {
                    struct.put(key, obj);
                }
            }
            else{
                // dropping field
                String field = key;
            }
        }

        return struct;
    }

    public Struct convert(CDAAsset asset)
    {
        final Struct record = new Struct(Asset);
        put(asset, record);
        record.put("locale", asset.locale());

        return  record;
    }

    private void put(CDAResource resource, Struct record)
    {
        record.put("id", resource.id());
        record.put("type", resource.type());
    }

    private void put(LocalizedResource resource, Struct record)
    {


    }

    public static Schema Sys(CDAContentType contentType)
    {
        SchemaBuilder sysSchema = SchemaBuilder.struct()
                //.name("com.google.gson.internal.LinkedTreeMap")
                .field("id",Schema.STRING_SCHEMA)
                .field("type",Schema.STRING_SCHEMA)
                .field("createdAt",Schema.STRING_SCHEMA)
                .field("updatedAt",Schema.STRING_SCHEMA)
                .field("space",SchemaBuilder.struct()
                        .field("sys",SchemaBuilder.struct()
                                .field("id",Schema.STRING_SCHEMA)
                                .field("type",Schema.STRING_SCHEMA)
                                .field("linkType",Schema.STRING_SCHEMA).build()).build())
                .field("contentType",SchemaBuilder.struct()
                        .field("sys",SchemaBuilder.struct()
                                .field("id",Schema.STRING_SCHEMA)
                                .field("type",Schema.STRING_SCHEMA)
                                .field("linkType",Schema.STRING_SCHEMA).build()).build())
                .field("revision",Schema.FLOAT64_SCHEMA);

        SchemaBuilder fieldsSchema = SchemaBuilder.struct();

        for(CDAField field: contentType.fields()){
            if(!field.isDisabled()) {
                fieldsSchema = fieldsSchema.field(field.id(), convert(field));
            }
        }

        return SchemaBuilder.struct()
                .name("com.google.gson.internal.LinkedTreeMap")
                .field("sys",sysSchema.build())
                .field("fields",fieldsSchema.build())
                .build();
    }

    public static Schema convert(CDAContentType contentType)
    {
        SchemaBuilder fieldsSchema = SchemaBuilder.struct();

        for(CDAField field: contentType.fields()){
            if(!field.isDisabled()) {
                fieldsSchema = fieldsSchema.field(field.id(), convert(field));
            }
        }

        return SchemaBuilder.struct()
                .field("sys", ContentfulSchemas.Sys)
                .field("fields",fieldsSchema.build())
                .build();
    }

    private static Schema convert(CDAField field)
    {
        SchemaBuilder schema = SchemaBuilder.struct();

        switch (field.type())
        {
            case "Text":{
                schema = schema.field("en-US", Schema.STRING_SCHEMA);
                break;
            }
            case "Integer":{
                schema = schema.field("en-US", Schema.INT32_SCHEMA);
                break;
            }
            case "Location":{
                schema = schema
                        .field("en-US", SchemaBuilder.struct()
                                .field("lat",Schema.FLOAT32_SCHEMA)
                                .field("lon",Schema.FLOAT32_SCHEMA));
            }
            case "Symbol":{
                schema = schema.field("en-US", Schema.STRING_SCHEMA);
                break;
            }
            case "Link":{
                schema = schema.field("en-US", SchemaBuilder.struct()
                                 .field("sys",SchemaBuilder.struct()
                                   .field("id",Schema.STRING_SCHEMA)
                                   .field("type",Schema.STRING_SCHEMA)
                                   .field("linkType",Schema.STRING_SCHEMA)));
                break;
            }
            case "Array":{
                break;
            }
            default: {
                throw new SchemaBuilderException("unknown type "+field.type());
            }
        }

        if(!field.isRequired()) {
            return schema.required().build();
        } else {
           return schema.optional().build();
        }
    }

    public static final Schema Key = Schema.STRING_SCHEMA;

    public static final Schema Entry =
            SchemaBuilder.struct()
                    .field("sys", ContentfulSchemas.Sys)
                    //.field("type", Schema.INT16_SCHEMA)
                    .field("fields", ContentfulSchemas.Sys)
                    //.field("defaultLocale", Schema.STRING_SCHEMA)
                    //.field("contentType", ContentfulSchemas.ContentType)
                    .build();

    /*
    public static final Schema Entry =
            SchemaBuilder.struct()
                    .field("id", Schema.STRING_SCHEMA)
                    //.field("sys", ContentfulSchemas.Sys)
                    //.field("type", Schema.INT16_SCHEMA)
                    .field("locale", Schema.STRING_SCHEMA)
                    //.field("defaultLocale", Schema.STRING_SCHEMA)
                    //.field("contentType", ContentfulSchemas.ContentType)
                    .build();
    */

    public static final Schema Sys = SchemaBuilder.struct()
                    //.name("com.google.gson.internal.LinkedTreeMap")
                    .field("id",Schema.STRING_SCHEMA)
                    .field("type",Schema.STRING_SCHEMA)
                    .field("createdAt",Schema.STRING_SCHEMA)
                    .field("updatedAt",Schema.STRING_SCHEMA)
                    .field("space",SchemaBuilder.struct()
                            .field("sys",SchemaBuilder.struct()
                                    .field("id",Schema.STRING_SCHEMA)
                                    .field("type",Schema.STRING_SCHEMA)
                                    .field("linkType",Schema.STRING_SCHEMA).build()).build())
                    .field("contentType",SchemaBuilder.struct()
                            .field("sys",SchemaBuilder.struct()
                                    .field("id",Schema.STRING_SCHEMA)
                                    .field("type",Schema.STRING_SCHEMA)
                                    .field("linkType",Schema.STRING_SCHEMA).build()).build())
                    .field("revision",Schema.FLOAT64_SCHEMA).build();

    private static  final Schema Link  = SchemaBuilder.struct()
            .field("sys",SchemaBuilder.struct()
                    .field("id",Schema.STRING_SCHEMA)
                    .field("type",Schema.STRING_SCHEMA)
                    .field("linkType",Schema.STRING_SCHEMA).build()).build();



    private static final Schema ContentType =
            SchemaBuilder.struct().name("com.contentful.java.cda.CDAContentType")
                    .field("name", Schema.STRING_SCHEMA)
                    .field("displayField", Schema.STRING_SCHEMA)
                    .field("description", Schema.STRING_SCHEMA)
                    .field("fields", ContentfulSchemas.Fields)
                    .build();



    private static final Schema Fields =
            SchemaBuilder.array(ContentfulSchemas.Field)
                    .build();

    private static final Schema Field =
            SchemaBuilder.struct().name("com.contentful.java.cda.CDAField")
                    .field("id", Schema.STRING_SCHEMA)
                    .field("name", Schema.STRING_SCHEMA)
                    .field("type", Schema.INT16_SCHEMA)
                    .field("linkType", Schema.INT16_SCHEMA)
                    .build();

    public static final Schema Asset =
            SchemaBuilder.struct().name("com.contentful.java.cda.CDAAsset")
                    .field("sys",ContentfulSchemas.Sys)
                    /*.field("type", Schema.STRING_SCHEMA)
                    .field("title", Schema.STRING_SCHEMA)
                    .field("url", Schema.STRING_SCHEMA)
                    .field("mimeType", Schema.STRING_SCHEMA)
                    .field("locale", Schema.STRING_SCHEMA)
                    .field("defaultLocale", Schema.STRING_SCHEMA)*/
                    .build();

}
