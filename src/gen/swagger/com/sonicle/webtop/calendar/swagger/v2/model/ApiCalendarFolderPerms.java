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
 * Represent a set of permissions for a Calendar folder.
 **/
@ApiModel(description = "Represent a set of permissions for a Calendar folder.")
@JsonTypeName("CalendarFolderPerms")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-09-18T14:19:18.243+02:00[Europe/Berlin]")
public class ApiCalendarFolderPerms   {
  private @Valid String folder;
  private @Valid String items;

  /**
   * Effective permissions granted to the current user on the folder itself. Possible permissions are: READ (r), UPDATE (u), DELETE (d), and MANAGE (m).
   **/
  public ApiCalendarFolderPerms folder(String folder) {
    this.folder = folder;
    return this;
  }

  
  @ApiModelProperty(value = "Effective permissions granted to the current user on the folder itself. Possible permissions are: READ (r), UPDATE (u), DELETE (d), and MANAGE (m).")
  @JsonProperty("folder")
  public String getFolder() {
    return folder;
  }

  @JsonProperty("folder")
  public void setFolder(String folder) {
    this.folder = folder;
  }

  /**
   * Effective permissions granted to the current user on the items contained in the folder. Possible permissions are: CREATE (c), UPDATE (u), and DELETE (d).
   **/
  public ApiCalendarFolderPerms items(String items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "Effective permissions granted to the current user on the items contained in the folder. Possible permissions are: CREATE (c), UPDATE (u), and DELETE (d).")
  @JsonProperty("items")
  public String getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(String items) {
    this.items = items;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiCalendarFolderPerms calendarFolderPerms = (ApiCalendarFolderPerms) o;
    return Objects.equals(this.folder, calendarFolderPerms.folder) &&
        Objects.equals(this.items, calendarFolderPerms.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(folder, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendarFolderPerms {\n");
    
    sb.append("    folder: ").append(toIndentedString(folder)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
