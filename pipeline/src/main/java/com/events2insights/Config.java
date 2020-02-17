package com.events2insights;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import java.util.Arrays;

public class Config {

    public static final TableSchema SCHEMA = new TableSchema().setFields(Arrays.asList(
        new TableFieldSchema().setName("timestamp").setType("TIMESTAMP").setMode("NULLABLE"),
        new TableFieldSchema().setName("message").setType("STRING").setMode("NULLABLE"),
        new TableFieldSchema().setName("deviceType").setType("STRING").setMode("NULLABLE"),
        new TableFieldSchema().setName("browserType").setType("STRING").setMode("NULLABLE"),
        new TableFieldSchema().setName("osType").setType("STRING").setMode("NULLABLE")
    ));
}
