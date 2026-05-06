package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventRecurrence;
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
 * Represent an Event object with extended data.
 **/
@ApiModel(description = "Represent an Event object with extended data.")
@JsonTypeName("EventEx")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventEx extends ApiEventBase  {
  private @Valid ApiEventRecurrence recurrence;

  /**
   **/
  public ApiEventEx recurrence(ApiEventRecurrence recurrence) {
    this.recurrence = recurrence;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("recurrence")
  public ApiEventRecurrence getRecurrence() {
    return recurrence;
  }

  @JsonProperty("recurrence")
  public void setRecurrence(ApiEventRecurrence recurrence) {
    this.recurrence = recurrence;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventEx eventEx = (ApiEventEx) o;
    return Objects.equals(this.recurrence, eventEx.recurrence) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recurrence, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventEx {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    recurrence: ").append(toIndentedString(recurrence)).append("\n");
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
