package com.sonicle.webtop.calendar.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry attendee&#39;s fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry attendee's fields")

public class SyncEventDataAttendee   {
  
  private @Valid String address = null;
  private @Valid String type = null;
  private @Valid String role = null;
  private @Valid String status = null;

  /**
   * Address (as RFC822)
   **/
  public SyncEventDataAttendee address(String address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Address (as RFC822)")
  @JsonProperty("address")
  @NotNull
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Type
   **/
  public SyncEventDataAttendee type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Role
   **/
  public SyncEventDataAttendee role(String role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Role")
  @JsonProperty("role")
  @NotNull
  public String getRole() {
    return role;
  }
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Response status
   **/
  public SyncEventDataAttendee status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Response status")
  @JsonProperty("status")
  @NotNull
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncEventDataAttendee syncEventDataAttendee = (SyncEventDataAttendee) o;
    return Objects.equals(address, syncEventDataAttendee.address) &&
        Objects.equals(type, syncEventDataAttendee.type) &&
        Objects.equals(role, syncEventDataAttendee.role) &&
        Objects.equals(status, syncEventDataAttendee.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, type, role, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SyncEventDataAttendee {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

