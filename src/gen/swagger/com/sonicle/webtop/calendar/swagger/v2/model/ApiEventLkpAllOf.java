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



@JsonTypeName("EventLkp_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventLkpAllOf   {
  private @Valid String tags;
  private @Valid Boolean hasRecurrence;
  private @Valid Integer attendeesCount;
  private @Valid Integer notifyableAttendeesCount;
  private @Valid String calendarName;
  private @Valid String calendarDomainId;
  private @Valid String calendarUserId;

  /**
   **/
  public ApiEventLkpAllOf tags(String tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public String getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(String tags) {
    this.tags = tags;
  }

  /**
   **/
  public ApiEventLkpAllOf hasRecurrence(Boolean hasRecurrence) {
    this.hasRecurrence = hasRecurrence;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("hasRecurrence")
  public Boolean getHasRecurrence() {
    return hasRecurrence;
  }

  @JsonProperty("hasRecurrence")
  public void setHasRecurrence(Boolean hasRecurrence) {
    this.hasRecurrence = hasRecurrence;
  }

  /**
   **/
  public ApiEventLkpAllOf attendeesCount(Integer attendeesCount) {
    this.attendeesCount = attendeesCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("attendeesCount")
  public Integer getAttendeesCount() {
    return attendeesCount;
  }

  @JsonProperty("attendeesCount")
  public void setAttendeesCount(Integer attendeesCount) {
    this.attendeesCount = attendeesCount;
  }

  /**
   **/
  public ApiEventLkpAllOf notifyableAttendeesCount(Integer notifyableAttendeesCount) {
    this.notifyableAttendeesCount = notifyableAttendeesCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("notifyableAttendeesCount")
  public Integer getNotifyableAttendeesCount() {
    return notifyableAttendeesCount;
  }

  @JsonProperty("notifyableAttendeesCount")
  public void setNotifyableAttendeesCount(Integer notifyableAttendeesCount) {
    this.notifyableAttendeesCount = notifyableAttendeesCount;
  }

  /**
   **/
  public ApiEventLkpAllOf calendarName(String calendarName) {
    this.calendarName = calendarName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("calendarName")
  public String getCalendarName() {
    return calendarName;
  }

  @JsonProperty("calendarName")
  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  /**
   **/
  public ApiEventLkpAllOf calendarDomainId(String calendarDomainId) {
    this.calendarDomainId = calendarDomainId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("calendarDomainId")
  public String getCalendarDomainId() {
    return calendarDomainId;
  }

  @JsonProperty("calendarDomainId")
  public void setCalendarDomainId(String calendarDomainId) {
    this.calendarDomainId = calendarDomainId;
  }

  /**
   **/
  public ApiEventLkpAllOf calendarUserId(String calendarUserId) {
    this.calendarUserId = calendarUserId;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
    ApiEventLkpAllOf eventLkpAllOf = (ApiEventLkpAllOf) o;
    return Objects.equals(this.tags, eventLkpAllOf.tags) &&
        Objects.equals(this.hasRecurrence, eventLkpAllOf.hasRecurrence) &&
        Objects.equals(this.attendeesCount, eventLkpAllOf.attendeesCount) &&
        Objects.equals(this.notifyableAttendeesCount, eventLkpAllOf.notifyableAttendeesCount) &&
        Objects.equals(this.calendarName, eventLkpAllOf.calendarName) &&
        Objects.equals(this.calendarDomainId, eventLkpAllOf.calendarDomainId) &&
        Objects.equals(this.calendarUserId, eventLkpAllOf.calendarUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tags, hasRecurrence, attendeesCount, notifyableAttendeesCount, calendarName, calendarDomainId, calendarUserId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventLkpAllOf {\n");
    
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    hasRecurrence: ").append(toIndentedString(hasRecurrence)).append("\n");
    sb.append("    attendeesCount: ").append(toIndentedString(attendeesCount)).append("\n");
    sb.append("    notifyableAttendeesCount: ").append(toIndentedString(notifyableAttendeesCount)).append("\n");
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
