package com.events2insights;

import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonProperty;

public class InputMessage implements Serializable {

    public String timestamp;
    public String message;
    @JsonProperty("user_agent") public String userAgent;

}
