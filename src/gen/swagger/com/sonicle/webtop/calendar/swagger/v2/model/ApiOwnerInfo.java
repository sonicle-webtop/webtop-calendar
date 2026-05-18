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

/**
 * Represent a user owning an item.
 **/
@ApiModel(description = "Represent a user owning an item.")
@JsonTypeName("OwnerInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiOwnerInfo   {
  private @Valid String userId;
  private @Valid String displayName;
  private @Valid String emailAddress;

  /**
   * User profile ID.
   **/
  public ApiOwnerInfo userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "User profile ID.")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * User profile display-name.
   **/
  public ApiOwnerInfo displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "User profile display-name.")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * User profile personal email address.
   **/
  public ApiOwnerInfo emailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  
  @ApiModelProperty(value = "User profile personal email address.")
  @JsonProperty("emailAddress")
  public String getEmailAddress() {
    return emailAddress;
  }

  @JsonProperty("emailAddress")
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiOwnerInfo ownerInfo = (ApiOwnerInfo) o;
    return Objects.equals(this.userId, ownerInfo.userId) &&
        Objects.equals(this.displayName, ownerInfo.displayName) &&
        Objects.equals(this.emailAddress, ownerInfo.emailAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, displayName, emailAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiOwnerInfo {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    emailAddress: ").append(toIndentedString(emailAddress)).append("\n");
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
