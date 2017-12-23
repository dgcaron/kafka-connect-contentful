package io.confluent.connect.contentful;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.List;
import java.util.Map;

public class ContentfulSinkConnector  extends SinkConnector {


    @Override
    public Class<? extends Task> taskClass() {
        return null;
    }

    public ConfigDef config() {
        return null;
    }

    public List<Map<String, String>> taskConfigs(int i) {
        return null;
    }

    @Override
    public void start(Map<String, String> props) {
    }

    @Override
    public void stop() {
    }

    public String version() {
        return null;
    }
}
