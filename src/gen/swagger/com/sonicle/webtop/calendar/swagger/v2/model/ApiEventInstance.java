package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventEx;
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
 * Represent an Event instance, a scheduled occurrence on a calendar, such as a meeting, holiday, or time block.
 **/
@ApiModel(description = "Represent an Event instance, a scheduled occurrence on a calendar, such as a meeting, holiday, or time block.")
@JsonTypeName("EventInstance")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public class ApiEventInstance extends ApiEventEx  {
  private @Valid String iid;
  private @Valid String instanceType;
  private @Valid String originalEventId;
  private @Valid String seriesEventId;
  private @Valid String etag;
  private @Valid String createdAt;
  private @Valid String updatedAt;

  /**
   * The event&#39;s unique ID that identifies an instance.
   **/
  public ApiEventInstance iid(String iid) {
    this.iid = iid;
    return this;
  }

  
  @ApiModelProperty(value = "The event's unique ID that identifies an instance.")
  @JsonProperty("iid")
  public String getIid() {
    return iid;
  }

  @JsonProperty("iid")
  public void setIid(String iid) {
    this.iid = iid;
  }

  /**
   * The event instance type. The possible values are: SI&#x3D;single, MA&#x3D;master, OC&#x3D;occurrence, EX&#x3D;exception.
   **/
  public ApiEventInstance instanceType(String instanceType) {
    this.instanceType = instanceType;
    return this;
  }

  
  @ApiModelProperty(value = "The event instance type. The possible values are: SI=single, MA=master, OC=occurrence, EX=exception.")
  @JsonProperty("instanceType")
  public String getInstanceType() {
    return instanceType;
  }

  @JsonProperty("instanceType")
  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  /**
   * The ID of the event.
   **/
  public ApiEventInstance originalEventId(String originalEventId) {
    this.originalEventId = originalEventId;
    return this;
  }

  
  @ApiModelProperty(value = "The ID of the event.")
  @JsonProperty("originalEventId")
  public String getOriginalEventId() {
    return originalEventId;
  }

  @JsonProperty("originalEventId")
  public void setOriginalEventId(String originalEventId) {
    this.originalEventId = originalEventId;
  }

  /**
   * The ID for the recurring series master item, if this event is part of a recurring series (master too).
   **/
  public ApiEventInstance seriesEventId(String seriesEventId) {
    this.seriesEventId = seriesEventId;
    return this;
  }

  
  @ApiModelProperty(value = "The ID for the recurring series master item, if this event is part of a recurring series (master too).")
  @JsonProperty("seriesEventId")
  public String getSeriesEventId() {
    return seriesEventId;
  }

  @JsonProperty("seriesEventId")
  public void setSeriesEventId(String seriesEventId) {
    this.seriesEventId = seriesEventId;
  }

  /**
   * The revision identifier that refers to the last modification.
   **/
  public ApiEventInstance etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(value = "The revision identifier that refers to the last modification.")
  @JsonProperty("etag")
  public String getEtag() {
    return etag;
  }

  @JsonProperty("etag")
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Creation timestamp in ISO 8601 format and UTC time.
   **/
  public ApiEventInstance createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "Creation timestamp in ISO 8601 format and UTC time.")
  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Modification timestamp in ISO 8601 format and UTC time.
   **/
  public ApiEventInstance updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "Modification timestamp in ISO 8601 format and UTC time.")
  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventInstance eventInstance = (ApiEventInstance) o;
    return Objects.equals(this.iid, eventInstance.iid) &&
        Objects.equals(this.instanceType, eventInstance.instanceType) &&
        Objects.equals(this.originalEventId, eventInstance.originalEventId) &&
        Objects.equals(this.seriesEventId, eventInstance.seriesEventId) &&
        Objects.equals(this.etag, eventInstance.etag) &&
        Objects.equals(this.createdAt, eventInstance.createdAt) &&
        Objects.equals(this.updatedAt, eventInstance.updatedAt) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iid, instanceType, originalEventId, seriesEventId, etag, createdAt, updatedAt, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventInstance {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    iid: ").append(toIndentedString(iid)).append("\n");
    sb.append("    instanceType: ").append(toIndentedString(instanceType)).append("\n");
    sb.append("    originalEventId: ").append(toIndentedString(originalEventId)).append("\n");
    sb.append("    seriesEventId: ").append(toIndentedString(seriesEventId)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
