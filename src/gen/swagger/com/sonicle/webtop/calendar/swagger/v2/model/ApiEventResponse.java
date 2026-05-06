package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
 * Represent a base response object to an Event invitation.
 **/
@ApiModel(description = "Represent a base response object to an Event invitation.")
@JsonTypeName("EventResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventResponse   {
  public enum StatusEnum {

    NA(String.valueOf("NA")), DE(String.valueOf("DE")), TE(String.valueOf("TE")), AC(String.valueOf("AC"));


    private String value;

    StatusEnum (String v) {
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
	public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid StatusEnum status;
  private @Valid String comment;
  private @Valid Boolean notify = true;
  private @Valid String proposedNewTime;

  /**
   **/
  public ApiEventResponse status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("status")
  @NotNull
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Text included in the response. Optional. (for future use)
   **/
  public ApiEventResponse comment(String comment) {
    this.comment = comment;
    return this;
  }

  
  @ApiModelProperty(value = "Text included in the response. Optional. (for future use)")
  @JsonProperty("comment")
  public String getComment() {
    return comment;
  }

  @JsonProperty("comment")
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Specifies if a response is to be sent to the organizer. Default is true. For declined response the notification is always sent.
   **/
  public ApiEventResponse notify(Boolean notify) {
    this.notify = notify;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies if a response is to be sent to the organizer. Default is true. For declined response the notification is always sent.")
  @JsonProperty("notify")
  public Boolean getNotify() {
    return notify;
  }

  @JsonProperty("notify")
  public void setNotify(Boolean notify) {
    this.notify = notify;
  }

  /**
   * Only for tentative response. (for future use)
   **/
  public ApiEventResponse proposedNewTime(String proposedNewTime) {
    this.proposedNewTime = proposedNewTime;
    return this;
  }

  
  @ApiModelProperty(value = "Only for tentative response. (for future use)")
  @JsonProperty("proposedNewTime")
  public String getProposedNewTime() {
    return proposedNewTime;
  }

  @JsonProperty("proposedNewTime")
  public void setProposedNewTime(String proposedNewTime) {
    this.proposedNewTime = proposedNewTime;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventResponse eventResponse = (ApiEventResponse) o;
    return Objects.equals(this.status, eventResponse.status) &&
        Objects.equals(this.comment, eventResponse.comment) &&
        Objects.equals(this.notify, eventResponse.notify) &&
        Objects.equals(this.proposedNewTime, eventResponse.proposedNewTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, comment, notify, proposedNewTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventResponse {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    notify: ").append(toIndentedString(notify)).append("\n");
    sb.append("    proposedNewTime: ").append(toIndentedString(proposedNewTime)).append("\n");
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
