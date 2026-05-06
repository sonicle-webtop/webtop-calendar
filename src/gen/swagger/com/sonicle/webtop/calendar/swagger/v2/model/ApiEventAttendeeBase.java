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
 * Represent an Attendee object with base updateable fields.
 **/
@ApiModel(description = "Represent an Attendee object with base updateable fields.")
@JsonTypeName("EventAttendeeBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventAttendeeBase   {
  private @Valid ApiRecipient address;
  private @Valid String userId;
  public enum TypeEnum {

    IND(String.valueOf("IND")), RES(String.valueOf("RES"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static TypeEnum fromString(String s) {
        for (TypeEnum b : TypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid TypeEnum type;
  public enum RoleEnum {

    CHA(String.valueOf("CHA")), OPT(String.valueOf("OPT")), REQ(String.valueOf("REQ"));


    private String value;

    RoleEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static RoleEnum fromString(String s) {
        for (RoleEnum b : RoleEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static RoleEnum fromValue(String value) {
        for (RoleEnum b : RoleEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid RoleEnum role;

  /**
   **/
  public ApiEventAttendeeBase address(ApiRecipient address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("address")
  @NotNull
  public ApiRecipient getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(ApiRecipient address) {
    this.address = address;
  }

  /**
   * The Recipient&#39;s user ID, to bound this role to a specific User. Optional.
   **/
  public ApiEventAttendeeBase userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "The Recipient's user ID, to bound this role to a specific User. Optional.")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * The type of attendee: IND&#x3D;individual, RES&#x3D;resource. Defaults to IND.
   **/
  public ApiEventAttendeeBase type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "The type of attendee: IND=individual, RES=resource. Defaults to IND.")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * The role of attendee: CHA&#x3D;chair, OPT&#x3D;optional, REQ&#x3D;required. Defaults to REQ.
   **/
  public ApiEventAttendeeBase role(RoleEnum role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(value = "The role of attendee: CHA=chair, OPT=optional, REQ=required. Defaults to REQ.")
  @JsonProperty("role")
  public RoleEnum getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(RoleEnum role) {
    this.role = role;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventAttendeeBase eventAttendeeBase = (ApiEventAttendeeBase) o;
    return Objects.equals(this.address, eventAttendeeBase.address) &&
        Objects.equals(this.userId, eventAttendeeBase.userId) &&
        Objects.equals(this.type, eventAttendeeBase.type) &&
        Objects.equals(this.role, eventAttendeeBase.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, userId, type, role);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventAttendeeBase {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
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
