package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiRecipient;
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
 * The organizer of the event.
 **/
@ApiModel(description = "The organizer of the event.")
@JsonTypeName("EventOrganizer")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T16:34:35.908+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiEventOrganizer   {
  private @Valid String userId;
  private @Valid ApiRecipient emailAddress;

  /**
   * The Organizer&#39;s user ID, to bound this role to a specific User. Optional.
   **/
  public ApiEventOrganizer userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "The Organizer's user ID, to bound this role to a specific User. Optional.")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   **/
  public ApiEventOrganizer emailAddress(ApiRecipient emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("emailAddress")
  public ApiRecipient getEmailAddress() {
    return emailAddress;
  }

  @JsonProperty("emailAddress")
  public void setEmailAddress(ApiRecipient emailAddress) {
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
    ApiEventOrganizer eventOrganizer = (ApiEventOrganizer) o;
    return Objects.equals(this.userId, eventOrganizer.userId) &&
        Objects.equals(this.emailAddress, eventOrganizer.emailAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, emailAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventOrganizer {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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
