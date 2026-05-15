package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
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



@JsonTypeName("EventLkp")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T16:34:35.908+02:00[Europe/Berlin]")
public class ApiEventLkp extends ApiEventBase  {
  private @Valid String tags;
  private @Valid Boolean hasRecurrence;
  private @Valid Integer attendeesCount;
  private @Valid Integer notifyableAttendeesCount;
  private @Valid String calendarId;
  private @Valid String calendarName;
  private @Valid String calendarDomainId;
  private @Valid String calendarUserId;

  /**
   * Pipe separated list of associated tag IDs.
   **/
  public ApiEventLkp tags(String tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(value = "Pipe separated list of associated tag IDs.")
  @JsonProperty("tags")
  public String getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(String tags) {
    this.tags = tags;
  }

  /**
   * Specified if 
   **/
  public ApiEventLkp hasRecurrence(Boolean hasRecurrence) {
    this.hasRecurrence = hasRecurrence;
    return this;
  }

  
  @ApiModelProperty(value = "Specified if ")
  @JsonProperty("hasRecurrence")
  public Boolean getHasRecurrence() {
    return hasRecurrence;
  }

  @JsonProperty("hasRecurrence")
  public void setHasRecurrence(Boolean hasRecurrence) {
    this.hasRecurrence = hasRecurrence;
  }

  /**
   * The total attendees count in the event.
   **/
  public ApiEventLkp attendeesCount(Integer attendeesCount) {
    this.attendeesCount = attendeesCount;
    return this;
  }

  
  @ApiModelProperty(value = "The total attendees count in the event.")
  @JsonProperty("attendeesCount")
  public Integer getAttendeesCount() {
    return attendeesCount;
  }

  @JsonProperty("attendeesCount")
  public void setAttendeesCount(Integer attendeesCount) {
    this.attendeesCount = attendeesCount;
  }

  /**
   * The count of attendees for which notification is required.
   **/
  public ApiEventLkp notifyableAttendeesCount(Integer notifyableAttendeesCount) {
    this.notifyableAttendeesCount = notifyableAttendeesCount;
    return this;
  }

  
  @ApiModelProperty(value = "The count of attendees for which notification is required.")
  @JsonProperty("notifyableAttendeesCount")
  public Integer getNotifyableAttendeesCount() {
    return notifyableAttendeesCount;
  }

  @JsonProperty("notifyableAttendeesCount")
  public void setNotifyableAttendeesCount(Integer notifyableAttendeesCount) {
    this.notifyableAttendeesCount = notifyableAttendeesCount;
  }

  /**
   * The Calendar ID.
   **/
  public ApiEventLkp calendarId(String calendarId) {
    this.calendarId = calendarId;
    return this;
  }

  
  @ApiModelProperty(value = "The Calendar ID.")
  @JsonProperty("calendarId")
  public String getCalendarId() {
    return calendarId;
  }

  @JsonProperty("calendarId")
  public void setCalendarId(String calendarId) {
    this.calendarId = calendarId;
  }

  /**
   * The Calendar name.
   **/
  public ApiEventLkp calendarName(String calendarName) {
    this.calendarName = calendarName;
    return this;
  }

  
  @ApiModelProperty(value = "The Calendar name.")
  @JsonProperty("calendarName")
  public String getCalendarName() {
    return calendarName;
  }

  @JsonProperty("calendarName")
  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  /**
   * The Calendar&#39;s owner domain ID.
   **/
  public ApiEventLkp calendarDomainId(String calendarDomainId) {
    this.calendarDomainId = calendarDomainId;
    return this;
  }

  
  @ApiModelProperty(value = "The Calendar's owner domain ID.")
  @JsonProperty("calendarDomainId")
  public String getCalendarDomainId() {
    return calendarDomainId;
  }

  @JsonProperty("calendarDomainId")
  public void setCalendarDomainId(String calendarDomainId) {
    this.calendarDomainId = calendarDomainId;
  }

  /**
   * The Calendar&#39;s owner user ID.
   **/
  public ApiEventLkp calendarUserId(String calendarUserId) {
    this.calendarUserId = calendarUserId;
    return this;
  }

  
  @ApiModelProperty(value = "The Calendar's owner user ID.")
  @JsonProperty("calendarUserId")
  public String getCalendarUserId() {
    return calendarUserId;
  }

  @JsonProperty("calendarUserId")
  public void setCalendarUserId(String calendarUserId) {
    this.calendarUserId = calendarUserId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventLkp eventLkp = (ApiEventLkp) o;
    return Objects.equals(this.tags, eventLkp.tags) &&
        Objects.equals(this.hasRecurrence, eventLkp.hasRecurrence) &&
        Objects.equals(this.attendeesCount, eventLkp.attendeesCount) &&
        Objects.equals(this.notifyableAttendeesCount, eventLkp.notifyableAttendeesCount) &&
        Objects.equals(this.calendarId, eventLkp.calendarId) &&
        Objects.equals(this.calendarName, eventLkp.calendarName) &&
        Objects.equals(this.calendarDomainId, eventLkp.calendarDomainId) &&
        Objects.equals(this.calendarUserId, eventLkp.calendarUserId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tags, hasRecurrence, attendeesCount, notifyableAttendeesCount, calendarId, calendarName, calendarDomainId, calendarUserId, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventLkp {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    hasRecurrence: ").append(toIndentedString(hasRecurrence)).append("\n");
    sb.append("    attendeesCount: ").append(toIndentedString(attendeesCount)).append("\n");
    sb.append("    notifyableAttendeesCount: ").append(toIndentedString(notifyableAttendeesCount)).append("\n");
    sb.append("    calendarId: ").append(toIndentedString(calendarId)).append("\n");
    sb.append("    calendarName: ").append(toIndentedString(calendarName)).append("\n");
    sb.append("    calendarDomainId: ").append(toIndentedString(calendarDomainId)).append("\n");
    sb.append("    calendarUserId: ").append(toIndentedString(calendarUserId)).append("\n");
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
