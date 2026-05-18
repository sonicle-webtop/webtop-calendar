package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventAttendee;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventRecurrence;
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
 * Represent an Event object with extended data.
 **/
@ApiModel(description = "Represent an Event object with extended data.")
@JsonTypeName("EventEx")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
public class ApiEventEx extends ApiEventBase  {
  private @Valid ApiEventRecurrence recurrence;
  private @Valid List<ApiEventAttendee> attendees;

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

  /**
   * The collection of attendees for the event.
   **/
  public ApiEventEx attendees(List<ApiEventAttendee> attendees) {
    this.attendees = attendees;
    return this;
  }

  
  @ApiModelProperty(value = "The collection of attendees for the event.")
  @JsonProperty("attendees")
  public List<ApiEventAttendee> getAttendees() {
    return attendees;
  }

  @JsonProperty("attendees")
  public void setAttendees(List<ApiEventAttendee> attendees) {
    this.attendees = attendees;
  }

  public ApiEventEx addAttendeesItem(ApiEventAttendee attendeesItem) {
    if (this.attendees == null) {
      this.attendees = new ArrayList<>();
    }

    this.attendees.add(attendeesItem);
    return this;
  }

  public ApiEventEx removeAttendeesItem(ApiEventAttendee attendeesItem) {
    if (attendeesItem != null && this.attendees != null) {
      this.attendees.remove(attendeesItem);
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
    ApiEventEx eventEx = (ApiEventEx) o;
    return Objects.equals(this.recurrence, eventEx.recurrence) &&
        Objects.equals(this.attendees, eventEx.attendees) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recurrence, attendees, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventEx {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    recurrence: ").append(toIndentedString(recurrence)).append("\n");
    sb.append("    attendees: ").append(toIndentedString(attendees)).append("\n");
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
