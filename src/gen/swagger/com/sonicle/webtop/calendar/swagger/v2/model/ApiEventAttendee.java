package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventAttendeeBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiRecipient;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An event attendee that can be a person or resource such as a meeting room or equipment, that has been set up as a resource on the server configuration.
 **/
@ApiModel(description = "An event attendee that can be a person or resource such as a meeting room or equipment, that has been set up as a resource on the server configuration.")
@JsonTypeName("EventAttendee")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public class ApiEventAttendee extends ApiEventAttendeeBase  {
  private @Valid String id;
  public enum ResponseStatusEnum {

    NA(String.valueOf("NA")), DE(String.valueOf("DE")), TE(String.valueOf("TE")), AC(String.valueOf("AC"));


    private String value;

    ResponseStatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static ResponseStatusEnum fromString(String s) {
        for (ResponseStatusEnum b : ResponseStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static ResponseStatusEnum fromValue(String value) {
        for (ResponseStatusEnum b : ResponseStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid ResponseStatusEnum responseStatus;
  private @Valid String responseTimestamp;

  /**
   **/
  public ApiEventAttendee id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The response type: NA&#x3D;needsAction, DE&#x3D;declined, TE&#x3D;tentative, AC&#x3D;accepted.
   **/
  public ApiEventAttendee responseStatus(ResponseStatusEnum responseStatus) {
    this.responseStatus = responseStatus;
    return this;
  }

  
  @ApiModelProperty(value = "The response type: NA=needsAction, DE=declined, TE=tentative, AC=accepted.")
  @JsonProperty("responseStatus")
  public ResponseStatusEnum getResponseStatus() {
    return responseStatus;
  }

  @JsonProperty("responseStatus")
  public void setResponseStatus(ResponseStatusEnum responseStatus) {
    this.responseStatus = responseStatus;
  }

  /**
   * The date and time when the response was returned. It uses ISO 8601 format and UTC time.
   **/
  public ApiEventAttendee responseTimestamp(String responseTimestamp) {
    this.responseTimestamp = responseTimestamp;
    return this;
  }

  
  @ApiModelProperty(value = "The date and time when the response was returned. It uses ISO 8601 format and UTC time.")
  @JsonProperty("responseTimestamp")
  public String getResponseTimestamp() {
    return responseTimestamp;
  }

  @JsonProperty("responseTimestamp")
  public void setResponseTimestamp(String responseTimestamp) {
    this.responseTimestamp = responseTimestamp;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventAttendee eventAttendee = (ApiEventAttendee) o;
    return Objects.equals(this.id, eventAttendee.id) &&
        Objects.equals(this.responseStatus, eventAttendee.responseStatus) &&
        Objects.equals(this.responseTimestamp, eventAttendee.responseTimestamp) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, responseStatus, responseTimestamp, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventAttendee {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    responseStatus: ").append(toIndentedString(responseStatus)).append("\n");
    sb.append("    responseTimestamp: ").append(toIndentedString(responseTimestamp)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}
