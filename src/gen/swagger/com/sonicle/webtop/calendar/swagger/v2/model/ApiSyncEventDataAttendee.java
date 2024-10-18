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
 * Bean for carry attendee&#39;s fields
 **/
@ApiModel(description = "Bean for carry attendee's fields")
@JsonTypeName("SyncEventDataAttendee")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-18T16:52:35.694+02:00[Europe/Berlin]")
public class ApiSyncEventDataAttendee   {
  private @Valid String address;
  private @Valid String type;
  private @Valid String role;
  private @Valid String status;

  /**
   * Address (as RFC822)
   **/
  public ApiSyncEventDataAttendee address(String address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Address (as RFC822)")
  @JsonProperty("address")
  @NotNull
  public String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Type
   **/
  public ApiSyncEventDataAttendee type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Role
   **/
  public ApiSyncEventDataAttendee role(String role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Role")
  @JsonProperty("role")
  @NotNull
  public String getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Response status
   **/
  public ApiSyncEventDataAttendee status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Response status")
  @JsonProperty("status")
  @NotNull
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiSyncEventDataAttendee syncEventDataAttendee = (ApiSyncEventDataAttendee) o;
    return Objects.equals(this.address, syncEventDataAttendee.address) &&
        Objects.equals(this.type, syncEventDataAttendee.type) &&
        Objects.equals(this.role, syncEventDataAttendee.role) &&
        Objects.equals(this.status, syncEventDataAttendee.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, type, role, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiSyncEventDataAttendee {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

