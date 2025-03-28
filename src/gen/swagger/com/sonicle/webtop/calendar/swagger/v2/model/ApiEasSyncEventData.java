package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncEventDataAttendee;
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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Bean for carry event&#39;s updateable fields
 **/
@ApiModel(description = "Bean for carry event's updateable fields")
@JsonTypeName("EasSyncEventData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public class ApiEasSyncEventData   {
  private @Valid String start;
  private @Valid String end;
  private @Valid String tz;
  private @Valid Boolean allDay;
  private @Valid String organizer;
  private @Valid String title;
  private @Valid String description;
  private @Valid String location;
  private @Valid Boolean prvt;
  private @Valid Boolean busy;
  private @Valid Integer reminder;
  private @Valid String recRule;
  private @Valid String recStart;
  private @Valid List<String> exDates = null;
  private @Valid List<ApiEasSyncEventDataAttendee> attendees = new ArrayList<>();

  /**
   * Start date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public ApiEasSyncEventData start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Start date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
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
   * End date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public ApiEasSyncEventData end(String end) {
    this.end = end;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "End date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("end")
  @NotNull
  public String getEnd() {
    return end;
  }

  @JsonProperty("end")
  public void setEnd(String end) {
    this.end = end;
  }

  /**
   * Timezone ID
   **/
  public ApiEasSyncEventData tz(String tz) {
    this.tz = tz;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Timezone ID")
  @JsonProperty("tz")
  @NotNull
  public String getTz() {
    return tz;
  }

  @JsonProperty("tz")
  public void setTz(String tz) {
    this.tz = tz;
  }

  /**
   * All day flag
   **/
  public ApiEasSyncEventData allDay(Boolean allDay) {
    this.allDay = allDay;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "All day flag")
  @JsonProperty("allDay")
  @NotNull
  public Boolean getAllDay() {
    return allDay;
  }

  @JsonProperty("allDay")
  public void setAllDay(Boolean allDay) {
    this.allDay = allDay;
  }

  /**
   * Organizer address (as RFC822)
   **/
  public ApiEasSyncEventData organizer(String organizer) {
    this.organizer = organizer;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Organizer address (as RFC822)")
  @JsonProperty("organizer")
  @NotNull
  public String getOrganizer() {
    return organizer;
  }

  @JsonProperty("organizer")
  public void setOrganizer(String organizer) {
    this.organizer = organizer;
  }

  /**
   * Title
   **/
  public ApiEasSyncEventData title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(value = "Title")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Description
   **/
  public ApiEasSyncEventData description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Location
   **/
  public ApiEasSyncEventData location(String location) {
    this.location = location;
    return this;
  }

  
  @ApiModelProperty(value = "Location")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }

  @JsonProperty("location")
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Private flag
   **/
  public ApiEasSyncEventData prvt(Boolean prvt) {
    this.prvt = prvt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Private flag")
  @JsonProperty("prvt")
  @NotNull
  public Boolean getPrvt() {
    return prvt;
  }

  @JsonProperty("prvt")
  public void setPrvt(Boolean prvt) {
    this.prvt = prvt;
  }

  /**
   * Busy flag
   **/
  public ApiEasSyncEventData busy(Boolean busy) {
    this.busy = busy;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Busy flag")
  @JsonProperty("busy")
  @NotNull
  public Boolean getBusy() {
    return busy;
  }

  @JsonProperty("busy")
  public void setBusy(Boolean busy) {
    this.busy = busy;
  }

  /**
   * Reminder
   **/
  public ApiEasSyncEventData reminder(Integer reminder) {
    this.reminder = reminder;
    return this;
  }

  
  @ApiModelProperty(value = "Reminder")
  @JsonProperty("reminder")
  public Integer getReminder() {
    return reminder;
  }

  @JsonProperty("reminder")
  public void setReminder(Integer reminder) {
    this.reminder = reminder;
  }

  /**
   * Recurrence RULE string
   **/
  public ApiEasSyncEventData recRule(String recRule) {
    this.recRule = recRule;
    return this;
  }

  
  @ApiModelProperty(value = "Recurrence RULE string")
  @JsonProperty("recRule")
  public String getRecRule() {
    return recRule;
  }

  @JsonProperty("recRule")
  public void setRecRule(String recRule) {
    this.recRule = recRule;
  }

  /**
   * Recurrence start date (ISO date YYYYMMDD)
   **/
  public ApiEasSyncEventData recStart(String recStart) {
    this.recStart = recStart;
    return this;
  }

  
  @ApiModelProperty(value = "Recurrence start date (ISO date YYYYMMDD)")
  @JsonProperty("recStart")
  public String getRecStart() {
    return recStart;
  }

  @JsonProperty("recStart")
  public void setRecStart(String recStart) {
    this.recStart = recStart;
  }

  /**
   * Excluded dates (ISO date YYYYMMDD)
   **/
  public ApiEasSyncEventData exDates(List<String> exDates) {
    this.exDates = exDates;
    return this;
  }

  
  @ApiModelProperty(value = "Excluded dates (ISO date YYYYMMDD)")
  @JsonProperty("exDates")
  public List<String> getExDates() {
    return exDates;
  }

  @JsonProperty("exDates")
  public void setExDates(List<String> exDates) {
    this.exDates = exDates;
  }

  public ApiEasSyncEventData addExDatesItem(String exDatesItem) {
    if (this.exDates == null) {
      this.exDates = new ArrayList<>();
    }

    this.exDates.add(exDatesItem);
    return this;
  }

  public ApiEasSyncEventData removeExDatesItem(String exDatesItem) {
    if (exDatesItem != null && this.exDates != null) {
      this.exDates.remove(exDatesItem);
    }

    return this;
  }
  /**
   * Appointment attendees
   **/
  public ApiEasSyncEventData attendees(List<ApiEasSyncEventDataAttendee> attendees) {
    this.attendees = attendees;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Appointment attendees")
  @JsonProperty("attendees")
  @NotNull
  public List<ApiEasSyncEventDataAttendee> getAttendees() {
    return attendees;
  }

  @JsonProperty("attendees")
  public void setAttendees(List<ApiEasSyncEventDataAttendee> attendees) {
    this.attendees = attendees;
  }

  public ApiEasSyncEventData addAttendeesItem(ApiEasSyncEventDataAttendee attendeesItem) {
    if (this.attendees == null) {
      this.attendees = new ArrayList<>();
    }

    this.attendees.add(attendeesItem);
    return this;
  }

  public ApiEasSyncEventData removeAttendeesItem(ApiEasSyncEventDataAttendee attendeesItem) {
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
    ApiEasSyncEventData easSyncEventData = (ApiEasSyncEventData) o;
    return Objects.equals(this.start, easSyncEventData.start) &&
        Objects.equals(this.end, easSyncEventData.end) &&
        Objects.equals(this.tz, easSyncEventData.tz) &&
        Objects.equals(this.allDay, easSyncEventData.allDay) &&
        Objects.equals(this.organizer, easSyncEventData.organizer) &&
        Objects.equals(this.title, easSyncEventData.title) &&
        Objects.equals(this.description, easSyncEventData.description) &&
        Objects.equals(this.location, easSyncEventData.location) &&
        Objects.equals(this.prvt, easSyncEventData.prvt) &&
        Objects.equals(this.busy, easSyncEventData.busy) &&
        Objects.equals(this.reminder, easSyncEventData.reminder) &&
        Objects.equals(this.recRule, easSyncEventData.recRule) &&
        Objects.equals(this.recStart, easSyncEventData.recStart) &&
        Objects.equals(this.exDates, easSyncEventData.exDates) &&
        Objects.equals(this.attendees, easSyncEventData.attendees);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end, tz, allDay, organizer, title, description, location, prvt, busy, reminder, recRule, recStart, exDates, attendees);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEasSyncEventData {\n");
    
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    tz: ").append(toIndentedString(tz)).append("\n");
    sb.append("    allDay: ").append(toIndentedString(allDay)).append("\n");
    sb.append("    organizer: ").append(toIndentedString(organizer)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    prvt: ").append(toIndentedString(prvt)).append("\n");
    sb.append("    busy: ").append(toIndentedString(busy)).append("\n");
    sb.append("    reminder: ").append(toIndentedString(reminder)).append("\n");
    sb.append("    recRule: ").append(toIndentedString(recRule)).append("\n");
    sb.append("    recStart: ").append(toIndentedString(recStart)).append("\n");
    sb.append("    exDates: ").append(toIndentedString(exDates)).append("\n");
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

