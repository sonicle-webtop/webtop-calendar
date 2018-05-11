package com.sonicle.webtop.calendar.swagger.v1.model;

import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectChanged;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry calendar object collection changes
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry calendar object collection changes")

public class CalObjectsChanges   {
  
  private @Valid String syncToken = null;
  private @Valid List<CalObjectChanged> inserted = new ArrayList<CalObjectChanged>();
  private @Valid List<CalObjectChanged> updated = new ArrayList<CalObjectChanged>();
  private @Valid List<CalObjectChanged> deleted = new ArrayList<CalObjectChanged>();

  /**
   * Current sync token
   **/
  public CalObjectsChanges syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Current sync token")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   * Items that have been inserted
   **/
  public CalObjectsChanges inserted(List<CalObjectChanged> inserted) {
    this.inserted = inserted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been inserted")
  @JsonProperty("inserted")
  @NotNull
  public List<CalObjectChanged> getInserted() {
    return inserted;
  }
  public void setInserted(List<CalObjectChanged> inserted) {
    this.inserted = inserted;
  }

  /**
   * Items that have been updated
   **/
  public CalObjectsChanges updated(List<CalObjectChanged> updated) {
    this.updated = updated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been updated")
  @JsonProperty("updated")
  @NotNull
  public List<CalObjectChanged> getUpdated() {
    return updated;
  }
  public void setUpdated(List<CalObjectChanged> updated) {
    this.updated = updated;
  }

  /**
   * Items that have been deleted
   **/
  public CalObjectsChanges deleted(List<CalObjectChanged> deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been deleted")
  @JsonProperty("deleted")
  @NotNull
  public List<CalObjectChanged> getDeleted() {
    return deleted;
  }
  public void setDeleted(List<CalObjectChanged> deleted) {
    this.deleted = deleted;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CalObjectsChanges calObjectsChanges = (CalObjectsChanges) o;
    return Objects.equals(syncToken, calObjectsChanges.syncToken) &&
        Objects.equals(inserted, calObjectsChanges.inserted) &&
        Objects.equals(updated, calObjectsChanges.updated) &&
        Objects.equals(deleted, calObjectsChanges.deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(syncToken, inserted, updated, deleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CalObjectsChanges {\n");
    
    sb.append("    syncToken: ").append(toIndentedString(syncToken)).append("\n");
    sb.append("    inserted: ").append(toIndentedString(inserted)).append("\n");
    sb.append("    updated: ").append(toIndentedString(updated)).append("\n");
    sb.append("    deleted: ").append(toIndentedString(deleted)).append("\n");
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

