package com.sonicle.webtop.calendar.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry calendar&#39;s updateable fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry calendar's updateable fields")

public class CalendarUpdate   {
  
  private @Valid String displayName = null;
  private @Valid String description = null;
  private @Valid String color = null;
  private @Valid List<String> updatedFields = new ArrayList<String>();

  /**
   * New value for displayName
   **/
  public CalendarUpdate displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "New value for displayName")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * New value for description
   **/
  public CalendarUpdate description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "New value for description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * New value for color
   **/
  public CalendarUpdate color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "New value for color")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Specifies which of above fields have been updated
   **/
  public CalendarUpdate updatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies which of above fields have been updated")
  @JsonProperty("updatedFields")
  public List<String> getUpdatedFields() {
    return updatedFields;
  }
  public void setUpdatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CalendarUpdate calendarUpdate = (CalendarUpdate) o;
    return Objects.equals(displayName, calendarUpdate.displayName) &&
        Objects.equals(description, calendarUpdate.description) &&
        Objects.equals(color, calendarUpdate.color) &&
        Objects.equals(updatedFields, calendarUpdate.updatedFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, description, color, updatedFields);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CalendarUpdate {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

