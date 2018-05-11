package com.sonicle.webtop.calendar.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry calendar object&#39;s fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry calendar object's fields")

public class CalObject   {
  
  private @Valid Integer id = null;
  private @Valid String uid = null;
  private @Valid String href = null;
  private @Valid Long lastModified = null;
  private @Valid String etag = null;
  private @Valid Integer size = null;
  private @Valid String icalendar = null;

  /**
   * Unique ID
   **/
  public CalObject id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "Unique ID")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Unique public ID
   **/
  public CalObject uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(value = "Unique public ID")
  @JsonProperty("uid")
  public String getUid() {
    return uid;
  }
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Reference URI
   **/
  public CalObject href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(value = "Reference URI")
  @JsonProperty("href")
  public String getHref() {
    return href;
  }
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Last modification time (unix timestamp)
   **/
  public CalObject lastModified(Long lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  
  @ApiModelProperty(value = "Last modification time (unix timestamp)")
  @JsonProperty("lastModified")
  public Long getLastModified() {
    return lastModified;
  }
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Revision tag
   **/
  public CalObject etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(value = "Revision tag")
  @JsonProperty("etag")
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Size (in bytes) of calendar data
   **/
  public CalObject size(Integer size) {
    this.size = size;
    return this;
  }

  
  @ApiModelProperty(value = "Size (in bytes) of calendar data")
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Calendar data (iCalendar format)
   **/
  public CalObject icalendar(String icalendar) {
    this.icalendar = icalendar;
    return this;
  }

  
  @ApiModelProperty(value = "Calendar data (iCalendar format)")
  @JsonProperty("icalendar")
  public String getIcalendar() {
    return icalendar;
  }
  public void setIcalendar(String icalendar) {
    this.icalendar = icalendar;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CalObject calObject = (CalObject) o;
    return Objects.equals(id, calObject.id) &&
        Objects.equals(uid, calObject.uid) &&
        Objects.equals(href, calObject.href) &&
        Objects.equals(lastModified, calObject.lastModified) &&
        Objects.equals(etag, calObject.etag) &&
        Objects.equals(size, calObject.size) &&
        Objects.equals(icalendar, calObject.icalendar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, href, lastModified, etag, size, icalendar);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CalObject {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

