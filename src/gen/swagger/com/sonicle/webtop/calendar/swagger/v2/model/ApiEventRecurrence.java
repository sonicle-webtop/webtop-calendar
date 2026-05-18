package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
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
 * Defines for a recurring Event the repetition pattern and optionally the exceptions.
 **/
@ApiModel(description = "Defines for a recurring Event the repetition pattern and optionally the exceptions.")
@JsonTypeName("EventRecurrence")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
public class ApiEventRecurrence   {
  private @Valid String start;
  private @Valid String rrule;
  private @Valid List<String> exDates;

  /**
   * The (inclusive) start time of the recurrence.
   **/
  public ApiEventRecurrence start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The (inclusive) start time of the recurrence.")
  @JsonProperty("start")
  @NotNull
  public String getStart() {
    return start;
  }

  @JsonProperty("start")
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * The rule that define repeating pattern for recurring event instances.
   **/
  public ApiEventRecurrence rrule(String rrule) {
    this.rrule = rrule;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The rule that define repeating pattern for recurring event instances.")
  @JsonProperty("rrule")
  @NotNull
  public String getRrule() {
    return rrule;
  }

  @JsonProperty("rrule")
  public void setRrule(String rrule) {
    this.rrule = rrule;
  }

  /**
   * Set of date/time exceptions of the recurrence.
   **/
  public ApiEventRecurrence exDates(List<String> exDates) {
    this.exDates = exDates;
    return this;
  }

  
  @ApiModelProperty(value = "Set of date/time exceptions of the recurrence.")
  @JsonProperty("exDates")
  public List<String> getExDates() {
    return exDates;
  }

  @JsonProperty("exDates")
  public void setExDates(List<String> exDates) {
    this.exDates = exDates;
  }

  public ApiEventRecurrence addExDatesItem(String exDatesItem) {
    if (this.exDates == null) {
      this.exDates = new ArrayList<>();
    }

    this.exDates.add(exDatesItem);
    return this;
  }

  public ApiEventRecurrence removeExDatesItem(String exDatesItem) {
    if (exDatesItem != null && this.exDates != null) {
      this.exDates.remove(exDatesItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventRecurrence eventRecurrence = (ApiEventRecurrence) o;
    return Objects.equals(this.start, eventRecurrence.start) &&
        Objects.equals(this.rrule, eventRecurrence.rrule) &&
        Objects.equals(this.exDates, eventRecurrence.exDates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, rrule, exDates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventRecurrence {\n");
    
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    rrule: ").append(toIndentedString(rrule)).append("\n");
    sb.append("    exDates: ").append(toIndentedString(exDates)).append("\n");
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
