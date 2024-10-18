package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalObjectChanged;
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
 * Bean for carry calendar object collection changes
 **/
@ApiModel(description = "Bean for carry calendar object collection changes")
@JsonTypeName("CalObjectsChanges")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-18T16:52:35.694+02:00[Europe/Berlin]")
public class ApiCalObjectsChanges   {
  private @Valid String syncToken;
  private @Valid List<ApiCalObjectChanged> inserted = new ArrayList<>();
  private @Valid List<ApiCalObjectChanged> updated = new ArrayList<>();
  private @Valid List<ApiCalObjectChanged> deleted = new ArrayList<>();

  /**
   * Current sync token
   **/
  public ApiCalObjectsChanges syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Current sync token")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }

  @JsonProperty("syncToken")
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   * Items that have been inserted
   **/
  public ApiCalObjectsChanges inserted(List<ApiCalObjectChanged> inserted) {
    this.inserted = inserted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been inserted")
  @JsonProperty("inserted")
  @NotNull
  public List<ApiCalObjectChanged> getInserted() {
    return inserted;
  }

  @JsonProperty("inserted")
  public void setInserted(List<ApiCalObjectChanged> inserted) {
    this.inserted = inserted;
  }

  public ApiCalObjectsChanges addInsertedItem(ApiCalObjectChanged insertedItem) {
    if (this.inserted == null) {
      this.inserted = new ArrayList<>();
    }

    this.inserted.add(insertedItem);
    return this;
  }

  public ApiCalObjectsChanges removeInsertedItem(ApiCalObjectChanged insertedItem) {
    if (insertedItem != null && this.inserted != null) {
      this.inserted.remove(insertedItem);
    }

    return this;
  }
  /**
   * Items that have been updated
   **/
  public ApiCalObjectsChanges updated(List<ApiCalObjectChanged> updated) {
    this.updated = updated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been updated")
  @JsonProperty("updated")
  @NotNull
  public List<ApiCalObjectChanged> getUpdated() {
    return updated;
  }

  @JsonProperty("updated")
  public void setUpdated(List<ApiCalObjectChanged> updated) {
    this.updated = updated;
  }

  public ApiCalObjectsChanges addUpdatedItem(ApiCalObjectChanged updatedItem) {
    if (this.updated == null) {
      this.updated = new ArrayList<>();
    }

    this.updated.add(updatedItem);
    return this;
  }

  public ApiCalObjectsChanges removeUpdatedItem(ApiCalObjectChanged updatedItem) {
    if (updatedItem != null && this.updated != null) {
      this.updated.remove(updatedItem);
    }

    return this;
  }
  /**
   * Items that have been deleted
   **/
  public ApiCalObjectsChanges deleted(List<ApiCalObjectChanged> deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been deleted")
  @JsonProperty("deleted")
  @NotNull
  public List<ApiCalObjectChanged> getDeleted() {
    return deleted;
  }

  @JsonProperty("deleted")
  public void setDeleted(List<ApiCalObjectChanged> deleted) {
    this.deleted = deleted;
  }

  public ApiCalObjectsChanges addDeletedItem(ApiCalObjectChanged deletedItem) {
    if (this.deleted == null) {
      this.deleted = new ArrayList<>();
    }

    this.deleted.add(deletedItem);
    return this;
  }

  public ApiCalObjectsChanges removeDeletedItem(ApiCalObjectChanged deletedItem) {
    if (deletedItem != null && this.deleted != null) {
      this.deleted.remove(deletedItem);
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
    ApiCalObjectsChanges calObjectsChanges = (ApiCalObjectsChanges) o;
    return Objects.equals(this.syncToken, calObjectsChanges.syncToken) &&
        Objects.equals(this.inserted, calObjectsChanges.inserted) &&
        Objects.equals(this.updated, calObjectsChanges.updated) &&
        Objects.equals(this.deleted, calObjectsChanges.deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(syncToken, inserted, updated, deleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalObjectsChanges {\n");
    
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

