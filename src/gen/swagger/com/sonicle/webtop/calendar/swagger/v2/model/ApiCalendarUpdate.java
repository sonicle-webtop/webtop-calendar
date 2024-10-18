package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Bean for carry calendar&#39;s updateable fields
 **/
@ApiModel(description = "Bean for carry calendar's updateable fields")
@JsonTypeName("CalendarUpdate")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-18T16:52:35.694+02:00[Europe/Berlin]")
public class ApiCalendarUpdate   {
  private @Valid String displayName;
  private @Valid String description;
  private @Valid String color;
  private @Valid List<String> updatedFields = null;

  /**
   * New value for displayName
   **/
  public ApiCalendarUpdate displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "New value for displayName")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * New value for description
   **/
  public ApiCalendarUpdate description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "New value for description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * New value for color
   **/
  public ApiCalendarUpdate color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "New value for color")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }

  @JsonProperty("color")
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Specifies which of above fields have been updated
   **/
  public ApiCalendarUpdate updatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies which of above fields have been updated")
  @JsonProperty("updatedFields")
  public List<String> getUpdatedFields() {
    return updatedFields;
  }

  @JsonProperty("updatedFields")
  public void setUpdatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
  }

  public ApiCalendarUpdate addUpdatedFieldsItem(String updatedFieldsItem) {
    if (this.updatedFields == null) {
      this.updatedFields = new ArrayList<>();
    }

    this.updatedFields.add(updatedFieldsItem);
    return this;
  }

  public ApiCalendarUpdate removeUpdatedFieldsItem(String updatedFieldsItem) {
    if (updatedFieldsItem != null && this.updatedFields != null) {
      this.updatedFields.remove(updatedFieldsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiCalendarUpdate calendarUpdate = (ApiCalendarUpdate) o;
    return Objects.equals(this.displayName, calendarUpdate.displayName) &&
        Objects.equals(this.description, calendarUpdate.description) &&
        Objects.equals(this.color, calendarUpdate.color) &&
        Objects.equals(this.updatedFields, calendarUpdate.updatedFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, description, color, updatedFields);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendarUpdate {\n");
    
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    updatedFields: ").append(toIndentedString(updatedFields)).append("\n");
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

