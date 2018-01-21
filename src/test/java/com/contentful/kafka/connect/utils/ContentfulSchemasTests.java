package com.contentful.kafka.connect.utils;

import com.contentful.java.cda.*;
import com.contentful.java.cma.model.CMAUiExtension;
import io.confluent.connect.avro.AvroConverter;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.connect.data.Schema;

import org.apache.kafka.connect.data.Struct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ContentfulSchemas.class })
@PowerMockIgnore("com.contentful.java.cda.*")
public class ContentfulSchemasTests {

    private final SchemaRegistryClient schemaRegistry;
    private final AvroConverter converter;
    private final ContentfulSchemas schemas;


    private final CDAField text_required = CDAField("text-required","text-required","Text", null,false,true);

    private final CDAField text_optional = CDAField("text-optional","text-optional","Text", null,false,false);

    private final CDAField text_disabled = CDAField("text-disabled","text-disabled","Text", null,true,true);



    public ContentfulSchemasTests() {
        schemaRegistry = new MockSchemaRegistryClient();
        converter = new AvroConverter(schemaRegistry);
       schemas = new ContentfulSchemas();


    }

    @Before
    public void setUp() {
        converter.configure(Collections.singletonMap("schema.registry.url", "http://fake-url"), false);
    }

    private final CDAField CDAField (String id, String name, String type, String linkType, boolean disabled, boolean required) {
        CDAField field = new CDAField();
        Whitebox.setInternalState(field, "id",id, CDAField.class);
        Whitebox.setInternalState(field, "name",name, CDAField.class);
        Whitebox.setInternalState(field, "type",type, CDAField.class);
        Whitebox.setInternalState(field, "linkType",linkType, CDAField.class);
        Whitebox.setInternalState(field, "disabled",disabled, CDAField.class);
        Whitebox.setInternalState(field, "required",required, CDAField.class);
        return field;
    };



    @Test
    public void can_build_schena_from_contenttype_with_textfield() {
        CDAContentType contentType = mock(CDAContentType.class);

        List<CDAField> fields = new ArrayList<>();
        fields.add(text_required);
        fields.add(text_optional);
        fields.add(text_disabled);

        expect(contentType.name()).andReturn("Test");
        expect(contentType.fields()).andReturn(fields);

        replay(contentType);

        Schema schema = ContentfulSchemas.convert(contentType);

        verifyAll();

        assertNotNull(schema);
        assertNotNull(schema.field("fields"));

        Schema fieldsSchema = schema.field("fields").schema();

        assertNotNull(fieldsSchema.field(text_required.name()));
        assertEquals(fieldsSchema.field(text_required.name()).schema().field("en-US").schema(), Schema.STRING_SCHEMA);
        assertNotNull(fieldsSchema.field(text_optional.name()));
        assertTrue(fieldsSchema.field(text_required.name()).schema().isOptional());
        assertNull(fieldsSchema.field(text_disabled.name()));
    }

        @Test
    public void CDAEntry_schema_valid() {
        CDAEntry entry = mock(CDAEntry.class);
        expect(entry.id()).andReturn("id");
        expect(entry.locale()).andReturn("en-US");




        replay(entry);

        /*
        Struct record = schemas.convert(entry);

        verify(entry);

        byte[] converted = converter.fromConnectData("topic", schemas.Entry, record);
        assertNotNull(converted);
        */
    }

    @Test
    public void CDAAsset_schema_valid() {
        CDAAsset asset = mock(CDAAsset.class);
        expect(asset.id()).andReturn("id");
        expect(asset.locale()).andReturn("en-US");

        replay(asset);

        Struct record = schemas.convert(asset);

        verify(asset);

        byte[] converted = converter.fromConnectData("topic", schemas.Asset, record);
        assertNotNull(converted);
    }
}
