package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiOwnerInfo;
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
 * Represent a Calendar that contains Events.
 **/
@ApiModel(description = "Represent a Calendar that contains Events.")
@JsonTypeName("Calendar")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
public class ApiCalendar extends ApiCalendarBase  {
  private @Valid String id;
  private @Valid String etag;
  private @Valid String itemsETag;
  private @Valid String createdAt;
  private @Valid String updatedAt;
  private @Valid ApiOwnerInfo owner;
  private @Valid Boolean isDefault;

  /**
   * The calendar ID.
   **/
  public ApiCalendar id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "The calendar ID.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The revision identifier that refers to the last modification.
   **/
  public ApiCalendar etag(String etag) {
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
   * The revision identifier that refers to the last modification of the items.
   **/
  public ApiCalendar itemsETag(String itemsETag) {
    this.itemsETag = itemsETag;
    return this;
  }

  
  @ApiModelProperty(value = "The revision identifier that refers to the last modification of the items.")
  @JsonProperty("itemsETag")
  public String getItemsETag() {
    return itemsETag;
  }

  @JsonProperty("itemsETag")
  public void setItemsETag(String itemsETag) {
    this.itemsETag = itemsETag;
  }

  /**
   * Creation timestamp in ISO 8601 format and UTC time.
   **/
  public ApiCalendar createdAt(String createdAt) {
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
  public ApiCalendar updatedAt(String updatedAt) {
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

  /**
   **/
  public ApiCalendar owner(ApiOwnerInfo owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("owner")
  public ApiOwnerInfo getOwner() {
    return owner;
  }

  @JsonProperty("owner")
  public void setOwner(ApiOwnerInfo owner) {
    this.owner = owner;
  }

  /**
   * Specifies if the calendar is markes as &#x60;default&#x60; one.
   **/
  public ApiCalendar isDefault(Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies if the calendar is markes as `default` one.")
  @JsonProperty("isDefault")
  public Boolean getIsDefault() {
    return isDefault;
  }

  @JsonProperty("isDefault")
  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiCalendar calendar = (ApiCalendar) o;
    return Objects.equals(this.id, calendar.id) &&
        Objects.equals(this.etag, calendar.etag) &&
        Objects.equals(this.itemsETag, calendar.itemsETag) &&
        Objects.equals(this.createdAt, calendar.createdAt) &&
        Objects.equals(this.updatedAt, calendar.updatedAt) &&
        Objects.equals(this.owner, calendar.owner) &&
        Objects.equals(this.isDefault, calendar.isDefault) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, etag, itemsETag, createdAt, updatedAt, owner, isDefault, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendar {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    itemsETag: ").append(toIndentedString(itemsETag)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
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
