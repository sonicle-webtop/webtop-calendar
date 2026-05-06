package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
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



@JsonTypeName("EventEx_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventExAllOf   {
  private @Valid ApiEventRecurrence recurrence;

  /**
   **/
  public ApiEventExAllOf recurrence(ApiEventRecurrence recurrence) {
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
    ApiEventExAllOf eventExAllOf = (ApiEventExAllOf) o;
    return Objects.equals(this.recurrence, eventExAllOf.recurrence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recurrence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventExAllOf {\n");
    
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
