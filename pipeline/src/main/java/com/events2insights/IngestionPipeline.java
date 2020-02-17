package com.events2insights;

import com.google.api.services.bigquery.model.TableRow;
import java.io.IOException;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.codehaus.jackson.map.ObjectMapper;
import ua_parser.Client;
import ua_parser.Parser;


public class IngestionPipeline {

  public interface Config extends PipelineOptions {

    @Required String getInputSubscription();
    void setInputSubscription(String value);

    @Required String getTargetTable();
    void setTargetTable(String value);
  }

  private static Parser getParser(){
    try {
      return new Parser();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static class ParseToBQRowDoFn extends DoFn<String, TableRow> {

    static ObjectMapper mapper = new ObjectMapper();
    static Parser parser = getParser();

    @ProcessElement
    public void processElement(@Element String element, OutputReceiver<TableRow> receiver) throws IOException {
      InputMessage record = mapper.readValue(element, InputMessage.class);
      Client client = parser.parse(record.userAgent);

      TableRow row = new TableRow();
      row.set("timestamp", record.timestamp);
      row.set("message", record.message);
      row.set("deviceType", client.device.family);
      row.set("browserType", client.userAgent.family);
      row.set("osType", client.os.family);

      receiver.output(row);
    }
  }

  static void runPipeline(Config options) {
    Pipeline p = Pipeline.create(options);

    p
        .apply("ReadMessages", PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
        .apply("ParseMessages", ParDo.of(new ParseToBQRowDoFn())).setCoder(TableRowJsonCoder.of())
        .apply("WriteToBQ", BigQueryIO.writeTableRows()
                                      .to(options.getTargetTable())
                                      .withSchema(com.events2insights.Config.SCHEMA)
                                      .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                                      .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

    p.run();
  }

  public static void main(String[] args) {
      Config config = PipelineOptionsFactory.fromArgs(args).withValidation().as(Config.class);
      runPipeline(config);
  }
}
