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
 * The name and email address of a contact or message recipient.
 **/
@ApiModel(description = "The name and email address of a contact or message recipient.")
@JsonTypeName("Recipient")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRecipient   {
  private @Valid String address;
  private @Valid String name;

  /**
   * The email address of the person or entity.
   **/
  public ApiRecipient address(String address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(value = "The email address of the person or entity.")
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * The display name of the person or entity.
   **/
  public ApiRecipient name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "The display name of the person or entity.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiRecipient recipient = (ApiRecipient) o;
    return Objects.equals(this.address, recipient.address) &&
        Objects.equals(this.name, recipient.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRecipient {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
