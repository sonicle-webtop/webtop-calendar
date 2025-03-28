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
 * Bean for carry event&#39;s fields
 **/
@ApiModel(description = "Bean for carry event's fields")
@JsonTypeName("EasSyncEvent")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public class ApiEasSyncEvent   {
  private @Valid String id;
  private @Valid String etag;
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
   * Event ID (internal)
   **/
  public ApiEasSyncEvent id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Event ID (internal)")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Revision tag
   **/
  public ApiEasSyncEvent etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }

  @JsonProperty("etag")
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Start date/time (ISO format)
   **/
  public ApiEasSyncEvent start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Start date/time (ISO format)")
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
   * End date/time (ISO format)
   **/
  public ApiEasSyncEvent end(String end) {
    this.end = end;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "End date/time (ISO format)")
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
  public ApiEasSyncEvent tz(String tz) {
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
  public ApiEasSyncEvent allDay(Boolean allDay) {
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
  public ApiEasSyncEvent organizer(String organizer) {
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
  public ApiEasSyncEvent title(String title) {
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
  public ApiEasSyncEvent description(String description) {
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
  public ApiEasSyncEvent location(String location) {
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
  public ApiEasSyncEvent prvt(Boolean prvt) {
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
  public ApiEasSyncEvent busy(Boolean busy) {
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
  public ApiEasSyncEvent reminder(Integer reminder) {
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
  public ApiEasSyncEvent recRule(String recRule) {
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
  public ApiEasSyncEvent recStart(String recStart) {
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
  public ApiEasSyncEvent exDates(List<String> exDates) {
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

  public ApiEasSyncEvent addExDatesItem(String exDatesItem) {
    if (this.exDates == null) {
      this.exDates = new ArrayList<>();
    }

    this.exDates.add(exDatesItem);
    return this;
  }

  public ApiEasSyncEvent removeExDatesItem(String exDatesItem) {
    if (exDatesItem != null && this.exDates != null) {
      this.exDates.remove(exDatesItem);
    }

    return this;
  }
  /**
   * Appointment attendees
   **/
  public ApiEasSyncEvent attendees(List<ApiEasSyncEventDataAttendee> attendees) {
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

  public ApiEasSyncEvent addAttendeesItem(ApiEasSyncEventDataAttendee attendeesItem) {
    if (this.attendees == null) {
      this.attendees = new ArrayList<>();
    }

    this.attendees.add(attendeesItem);
    return this;
  }

  public ApiEasSyncEvent removeAttendeesItem(ApiEasSyncEventDataAttendee attendeesItem) {
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
    ApiEasSyncEvent easSyncEvent = (ApiEasSyncEvent) o;
    return Objects.equals(this.id, easSyncEvent.id) &&
        Objects.equals(this.etag, easSyncEvent.etag) &&
        Objects.equals(this.start, easSyncEvent.start) &&
        Objects.equals(this.end, easSyncEvent.end) &&
        Objects.equals(this.tz, easSyncEvent.tz) &&
        Objects.equals(this.allDay, easSyncEvent.allDay) &&
        Objects.equals(this.organizer, easSyncEvent.organizer) &&
        Objects.equals(this.title, easSyncEvent.title) &&
        Objects.equals(this.description, easSyncEvent.description) &&
        Objects.equals(this.location, easSyncEvent.location) &&
        Objects.equals(this.prvt, easSyncEvent.prvt) &&
        Objects.equals(this.busy, easSyncEvent.busy) &&
        Objects.equals(this.reminder, easSyncEvent.reminder) &&
        Objects.equals(this.recRule, easSyncEvent.recRule) &&
        Objects.equals(this.recStart, easSyncEvent.recStart) &&
        Objects.equals(this.exDates, easSyncEvent.exDates) &&
        Objects.equals(this.attendees, easSyncEvent.attendees);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, etag, start, end, tz, allDay, organizer, title, description, location, prvt, busy, reminder, recRule, recStart, exDates, attendees);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEasSyncEvent {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
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

