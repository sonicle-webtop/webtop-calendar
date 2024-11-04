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
 * Bean for carry calendar&#39;s fields
 **/
@ApiModel(description = "Bean for carry calendar's fields")
@JsonTypeName("DavCalendar")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public class ApiDavCalendar   {
  private @Valid String id;
  private @Valid String uid;
  private @Valid String displayName;
  private @Valid String description;
  private @Valid String color;
  private @Valid String syncToken;
  private @Valid String aclFol;
  private @Valid String aclEle;
  private @Valid String ownerUsername;

  /**
   * Calendar ID (internal)
   **/
  public ApiDavCalendar id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Calendar ID (internal)")
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
   * Calendar UID (public)
   **/
  public ApiDavCalendar uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Calendar UID (public)")
  @JsonProperty("uid")
  @NotNull
  public String getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Display name
   **/
  public ApiDavCalendar displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description
   **/
  public ApiDavCalendar description(String description) {
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
   * Assigned color
   **/
  public ApiDavCalendar color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "Assigned color")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }

  @JsonProperty("color")
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Current sync token
   **/
  public ApiDavCalendar syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Current sync token")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }

  @JsonProperty("syncToken")
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   * ACL info for folder itself
   **/
  public ApiDavCalendar aclFol(String aclFol) {
    this.aclFol = aclFol;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder itself")
  @JsonProperty("aclFol")
  @NotNull
  public String getAclFol() {
    return aclFol;
  }

  @JsonProperty("aclFol")
  public void setAclFol(String aclFol) {
    this.aclFol = aclFol;
  }

  /**
   * ACL info for folder elements
   **/
  public ApiDavCalendar aclEle(String aclEle) {
    this.aclEle = aclEle;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder elements")
  @JsonProperty("aclEle")
  @NotNull
  public String getAclEle() {
    return aclEle;
  }

  @JsonProperty("aclEle")
  public void setAclEle(String aclEle) {
    this.aclEle = aclEle;
  }

  /**
   * The owner profile&#39;s username
   **/
  public ApiDavCalendar ownerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner profile's username")
  @JsonProperty("ownerUsername")
  @NotNull
  public String getOwnerUsername() {
    return ownerUsername;
  }

  @JsonProperty("ownerUsername")
  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavCalendar davCalendar = (ApiDavCalendar) o;
    return Objects.equals(this.id, davCalendar.id) &&
        Objects.equals(this.uid, davCalendar.uid) &&
        Objects.equals(this.displayName, davCalendar.displayName) &&
        Objects.equals(this.description, davCalendar.description) &&
        Objects.equals(this.color, davCalendar.color) &&
        Objects.equals(this.syncToken, davCalendar.syncToken) &&
        Objects.equals(this.aclFol, davCalendar.aclFol) &&
        Objects.equals(this.aclEle, davCalendar.aclEle) &&
        Objects.equals(this.ownerUsername, davCalendar.ownerUsername);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, displayName, description, color, syncToken, aclFol, aclEle, ownerUsername);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavCalendar {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    syncToken: ").append(toIndentedString(syncToken)).append("\n");
    sb.append("    aclFol: ").append(toIndentedString(aclFol)).append("\n");
    sb.append("    aclEle: ").append(toIndentedString(aclEle)).append("\n");
    sb.append("    ownerUsername: ").append(toIndentedString(ownerUsername)).append("\n");
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

