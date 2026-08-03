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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("UserSettings")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-08-03T14:05:12.119+02:00[Europe/Berlin]")
public class ApiUserSettings   {
  private @Valid Integer schedulerTimeResolution;
  private @Valid String workdayStart;
  private @Valid String workdayEnd;
  private @Valid List<String> inactiveCalendarFolders;
  private @Valid String defaultCalendarFolder;

  /**
   * The time resolution in minutes for time slots.
   **/
  public ApiUserSettings schedulerTimeResolution(Integer schedulerTimeResolution) {
    this.schedulerTimeResolution = schedulerTimeResolution;
    return this;
  }

  
  @ApiModelProperty(value = "The time resolution in minutes for time slots.")
  @JsonProperty("schedulerTimeResolution")
  public Integer getSchedulerTimeResolution() {
    return schedulerTimeResolution;
  }

  @JsonProperty("schedulerTimeResolution")
  public void setSchedulerTimeResolution(Integer schedulerTimeResolution) {
    this.schedulerTimeResolution = schedulerTimeResolution;
  }

  /**
   * The workday start time (in HH:mm format).
   **/
  public ApiUserSettings workdayStart(String workdayStart) {
    this.workdayStart = workdayStart;
    return this;
  }

  
  @ApiModelProperty(value = "The workday start time (in HH:mm format).")
  @JsonProperty("workdayStart")
  public String getWorkdayStart() {
    return workdayStart;
  }

  @JsonProperty("workdayStart")
  public void setWorkdayStart(String workdayStart) {
    this.workdayStart = workdayStart;
  }

  /**
   * The workday end time (in HH:mm format).
   **/
  public ApiUserSettings workdayEnd(String workdayEnd) {
    this.workdayEnd = workdayEnd;
    return this;
  }

  
  @ApiModelProperty(value = "The workday end time (in HH:mm format).")
  @JsonProperty("workdayEnd")
  public String getWorkdayEnd() {
    return workdayEnd;
  }

  @JsonProperty("workdayEnd")
  public void setWorkdayEnd(String workdayEnd) {
    this.workdayEnd = workdayEnd;
  }

  /**
   * List of inactive calendar IDs.
   **/
  public ApiUserSettings inactiveCalendarFolders(List<String> inactiveCalendarFolders) {
    this.inactiveCalendarFolders = inactiveCalendarFolders;
    return this;
  }

  
  @ApiModelProperty(value = "List of inactive calendar IDs.")
  @JsonProperty("inactiveCalendarFolders")
  public List<String> getInactiveCalendarFolders() {
    return inactiveCalendarFolders;
  }

  @JsonProperty("inactiveCalendarFolders")
  public void setInactiveCalendarFolders(List<String> inactiveCalendarFolders) {
    this.inactiveCalendarFolders = inactiveCalendarFolders;
  }

  public ApiUserSettings addInactiveCalendarFoldersItem(String inactiveCalendarFoldersItem) {
    if (this.inactiveCalendarFolders == null) {
      this.inactiveCalendarFolders = new ArrayList<>();
    }

    this.inactiveCalendarFolders.add(inactiveCalendarFoldersItem);
    return this;
  }

  public ApiUserSettings removeInactiveCalendarFoldersItem(String inactiveCalendarFoldersItem) {
    if (inactiveCalendarFoldersItem != null && this.inactiveCalendarFolders != null) {
      this.inactiveCalendarFolders.remove(inactiveCalendarFoldersItem);
    }

    return this;
  }
  /**
   * The calendar ID marked as default folder.
   **/
  public ApiUserSettings defaultCalendarFolder(String defaultCalendarFolder) {
    this.defaultCalendarFolder = defaultCalendarFolder;
    return this;
  }

  
  @ApiModelProperty(value = "The calendar ID marked as default folder.")
  @JsonProperty("defaultCalendarFolder")
  public String getDefaultCalendarFolder() {
    return defaultCalendarFolder;
  }

  @JsonProperty("defaultCalendarFolder")
  public void setDefaultCalendarFolder(String defaultCalendarFolder) {
    this.defaultCalendarFolder = defaultCalendarFolder;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiUserSettings userSettings = (ApiUserSettings) o;
    return Objects.equals(this.schedulerTimeResolution, userSettings.schedulerTimeResolution) &&
        Objects.equals(this.workdayStart, userSettings.workdayStart) &&
        Objects.equals(this.workdayEnd, userSettings.workdayEnd) &&
        Objects.equals(this.inactiveCalendarFolders, userSettings.inactiveCalendarFolders) &&
        Objects.equals(this.defaultCalendarFolder, userSettings.defaultCalendarFolder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schedulerTimeResolution, workdayStart, workdayEnd, inactiveCalendarFolders, defaultCalendarFolder);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiUserSettings {\n");
    
    sb.append("    schedulerTimeResolution: ").append(toIndentedString(schedulerTimeResolution)).append("\n");
    sb.append("    workdayStart: ").append(toIndentedString(workdayStart)).append("\n");
    sb.append("    workdayEnd: ").append(toIndentedString(workdayEnd)).append("\n");
    sb.append("    inactiveCalendarFolders: ").append(toIndentedString(inactiveCalendarFolders)).append("\n");
    sb.append("    defaultCalendarFolder: ").append(toIndentedString(defaultCalendarFolder)).append("\n");
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
