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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Bean for carry calendar object&#39;s fields
 **/
@ApiModel(description = "Bean for carry calendar object's fields")
@JsonTypeName("DavCalObject")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public class ApiDavCalObject   {
  private @Valid String id;
  private @Valid String uid;
  private @Valid String href;
  private @Valid Long lastModified;
  private @Valid String etag;
  private @Valid Integer size;
  private @Valid String icalendar;

  /**
   * CalObject ID (internal)
   **/
  public ApiDavCalObject id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "CalObject ID (internal)")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * CalObject UID (public)
   **/
  public ApiDavCalObject uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(value = "CalObject UID (public)")
  @JsonProperty("uid")
  public String getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Reference URI
   **/
  public ApiDavCalObject href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(value = "Reference URI")
  @JsonProperty("href")
  public String getHref() {
    return href;
  }

  @JsonProperty("href")
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Last modification time (unix timestamp)
   **/
  public ApiDavCalObject lastModified(Long lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  
  @ApiModelProperty(value = "Last modification time (unix timestamp)")
  @JsonProperty("lastModified")
  public Long getLastModified() {
    return lastModified;
  }

  @JsonProperty("lastModified")
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Revision tag
   **/
  public ApiDavCalObject etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(value = "Revision tag")
  @JsonProperty("etag")
  public String getEtag() {
    return etag;
  }

  @JsonProperty("etag")
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Size (in bytes) of calendar data
   **/
  public ApiDavCalObject size(Integer size) {
    this.size = size;
    return this;
  }

  
  @ApiModelProperty(value = "Size (in bytes) of calendar data")
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }

  @JsonProperty("size")
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Calendar data (iCalendar format)
   **/
  public ApiDavCalObject icalendar(String icalendar) {
    this.icalendar = icalendar;
    return this;
  }

  
  @ApiModelProperty(value = "Calendar data (iCalendar format)")
  @JsonProperty("icalendar")
  public String getIcalendar() {
    return icalendar;
  }

  @JsonProperty("icalendar")
  public void setIcalendar(String icalendar) {
    this.icalendar = icalendar;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavCalObject davCalObject = (ApiDavCalObject) o;
    return Objects.equals(this.id, davCalObject.id) &&
        Objects.equals(this.uid, davCalObject.uid) &&
        Objects.equals(this.href, davCalObject.href) &&
        Objects.equals(this.lastModified, davCalObject.lastModified) &&
        Objects.equals(this.etag, davCalObject.etag) &&
        Objects.equals(this.size, davCalObject.size) &&
        Objects.equals(this.icalendar, davCalObject.icalendar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, href, lastModified, etag, size, icalendar);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavCalObject {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
    sb.append("    lastModified: ").append(toIndentedString(lastModified)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    icalendar: ").append(toIndentedString(icalendar)).append("\n");
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

