package com.sonicle.webtop.calendar.swagger.v1.model;

import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventDataAttendee;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry event&#39;s fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry event's fields")

public class SyncEvent   {
  
  private @Valid Integer id = null;
  private @Valid String etag = null;
  private @Valid String start = null;
  private @Valid String end = null;
  private @Valid String tz = null;
  private @Valid Boolean allDay = null;
  private @Valid String organizer = null;
  private @Valid String title = null;
  private @Valid String description = null;
  private @Valid String location = null;
  private @Valid Boolean prvt = null;
  private @Valid Boolean busy = null;
  private @Valid Integer reminder = null;
  private @Valid String recRule = null;
  private @Valid String recStart = null;
  private @Valid List<String> exDates = new ArrayList<String>();
  private @Valid List<SyncEventDataAttendee> attendees = new ArrayList<SyncEventDataAttendee>();

  /**
   * Event ID (internal)
   **/
  public SyncEvent id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Event ID (internal)")
  @JsonProperty("id")
  @NotNull
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Revision tag
   **/
  public SyncEvent etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Start date/time (ISO format)
   **/
  public SyncEvent start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Start date/time (ISO format)")
  @JsonProperty("start")
  @NotNull
  public String getStart() {
    return start;
  }
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * End date/time (ISO format)
   **/
  public SyncEvent end(String end) {
    this.end = end;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "End date/time (ISO format)")
  @JsonProperty("end")
  @NotNull
  public String getEnd() {
    return end;
  }
  public void setEnd(String end) {
    this.end = end;
  }

  /**
   * Timezone ID
   **/
  public SyncEvent tz(String tz) {
    this.tz = tz;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Timezone ID")
  @JsonProperty("tz")
  @NotNull
  public String getTz() {
    return tz;
  }
  public void setTz(String tz) {
    this.tz = tz;
  }

  /**
   * All day flag
   **/
  public SyncEvent allDay(Boolean allDay) {
    this.allDay = allDay;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "All day flag")
  @JsonProperty("allDay")
  @NotNull
  public Boolean isAllDay() {
    return allDay;
  }
  public void setAllDay(Boolean allDay) {
    this.allDay = allDay;
  }

  /**
   * Organizer address (as RFC822)
   **/
  public SyncEvent organizer(String organizer) {
    this.organizer = organizer;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Organizer address (as RFC822)")
  @JsonProperty("organizer")
  @NotNull
  public String getOrganizer() {
    return organizer;
  }
  public void setOrganizer(String organizer) {
    this.organizer = organizer;
  }

  /**
   * Title
   **/
  public SyncEvent title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(value = "Title")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Description
   **/
  public SyncEvent description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Location
   **/
  public SyncEvent location(String location) {
    this.location = location;
    return this;
  }

  
  @ApiModelProperty(value = "Location")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Private flag
   **/
  public SyncEvent prvt(Boolean prvt) {
    this.prvt = prvt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Private flag")
  @JsonProperty("prvt")
  @NotNull
  public Boolean isPrvt() {
    return prvt;
  }
  public void setPrvt(Boolean prvt) {
    this.prvt = prvt;
  }

  /**
   * Busy flag
   **/
  public SyncEvent busy(Boolean busy) {
    this.busy = busy;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Busy flag")
  @JsonProperty("busy")
  @NotNull
  public Boolean isBusy() {
    return busy;
  }
  public void setBusy(Boolean busy) {
    this.busy = busy;
  }

  /**
   * Reminder
   **/
  public SyncEvent reminder(Integer reminder) {
    this.reminder = reminder;
    return this;
  }

  
  @ApiModelProperty(value = "Reminder")
  @JsonProperty("reminder")
  public Integer getReminder() {
    return reminder;
  }
  public void setReminder(Integer reminder) {
    this.reminder = reminder;
  }

  /**
   * Recurrence RULE string
   **/
  public SyncEvent recRule(String recRule) {
    this.recRule = recRule;
    return this;
  }

  
  @ApiModelProperty(value = "Recurrence RULE string")
  @JsonProperty("recRule")
  public String getRecRule() {
    return recRule;
  }
  public void setRecRule(String recRule) {
    this.recRule = recRule;
  }

  /**
   * Recurrence start date (ISO date YYYYMMDD)
   **/
  public SyncEvent recStart(String recStart) {
    this.recStart = recStart;
    return this;
  }

  
  @ApiModelProperty(value = "Recurrence start date (ISO date YYYYMMDD)")
  @JsonProperty("recStart")
  public String getRecStart() {
    return recStart;
  }
  public void setRecStart(String recStart) {
    this.recStart = recStart;
  }

  /**
   * Excluded dates (ISO date YYYYMMDD)
   **/
  public SyncEvent exDates(List<String> exDates) {
    this.exDates = exDates;
    return this;
  }

  
  @ApiModelProperty(value = "Excluded dates (ISO date YYYYMMDD)")
  @JsonProperty("exDates")
  public List<String> getExDates() {
    return exDates;
  }
  public void setExDates(List<String> exDates) {
    this.exDates = exDates;
  }

  /**
   * Appointment attendees
   **/
  public SyncEvent attendees(List<SyncEventDataAttendee> attendees) {
    this.attendees = attendees;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Appointment attendees")
  @JsonProperty("attendees")
  @NotNull
  public List<SyncEventDataAttendee> getAttendees() {
    return attendees;
  }
  public void setAttendees(List<SyncEventDataAttendee> attendees) {
    this.attendees = attendees;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncEvent syncEvent = (SyncEvent) o;
    return Objects.equals(id, syncEvent.id) &&
        Objects.equals(etag, syncEvent.etag) &&
        Objects.equals(start, syncEvent.start) &&
        Objects.equals(end, syncEvent.end) &&
        Objects.equals(tz, syncEvent.tz) &&
        Objects.equals(allDay, syncEvent.allDay) &&
        Objects.equals(organizer, syncEvent.organizer) &&
        Objects.equals(title, syncEvent.title) &&
        Objects.equals(description, syncEvent.description) &&
        Objects.equals(location, syncEvent.location) &&
        Objects.equals(prvt, syncEvent.prvt) &&
        Objects.equals(busy, syncEvent.busy) &&
        Objects.equals(reminder, syncEvent.reminder) &&
        Objects.equals(recRule, syncEvent.recRule) &&
        Objects.equals(recStart, syncEvent.recStart) &&
        Objects.equals(exDates, syncEvent.exDates) &&
        Objects.equals(attendees, syncEvent.attendees);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, etag, start, end, tz, allDay, organizer, title, description, location, prvt, busy, reminder, recRule, recStart, exDates, attendees);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SyncEvent {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

