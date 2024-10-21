package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalObjectChanged;
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
@JsonTypeName("DavCalObjectsChanges")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-21T12:09:53.556+02:00[Europe/Berlin]")
public class ApiDavCalObjectsChanges   {
  private @Valid String syncToken;
  private @Valid List<ApiDavCalObjectChanged> inserted = new ArrayList<>();
  private @Valid List<ApiDavCalObjectChanged> updated = new ArrayList<>();
  private @Valid List<ApiDavCalObjectChanged> deleted = new ArrayList<>();

  /**
   * Current sync token
   **/
  public ApiDavCalObjectsChanges syncToken(String syncToken) {
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
  public ApiDavCalObjectsChanges inserted(List<ApiDavCalObjectChanged> inserted) {
    this.inserted = inserted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been inserted")
  @JsonProperty("inserted")
  @NotNull
  public List<ApiDavCalObjectChanged> getInserted() {
    return inserted;
  }

  @JsonProperty("inserted")
  public void setInserted(List<ApiDavCalObjectChanged> inserted) {
    this.inserted = inserted;
  }

  public ApiDavCalObjectsChanges addInsertedItem(ApiDavCalObjectChanged insertedItem) {
    if (this.inserted == null) {
      this.inserted = new ArrayList<>();
    }

    this.inserted.add(insertedItem);
    return this;
  }

  public ApiDavCalObjectsChanges removeInsertedItem(ApiDavCalObjectChanged insertedItem) {
    if (insertedItem != null && this.inserted != null) {
      this.inserted.remove(insertedItem);
    }

    return this;
  }
  /**
   * Items that have been updated
   **/
  public ApiDavCalObjectsChanges updated(List<ApiDavCalObjectChanged> updated) {
    this.updated = updated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been updated")
  @JsonProperty("updated")
  @NotNull
  public List<ApiDavCalObjectChanged> getUpdated() {
    return updated;
  }

  @JsonProperty("updated")
  public void setUpdated(List<ApiDavCalObjectChanged> updated) {
    this.updated = updated;
  }

  public ApiDavCalObjectsChanges addUpdatedItem(ApiDavCalObjectChanged updatedItem) {
    if (this.updated == null) {
      this.updated = new ArrayList<>();
    }

    this.updated.add(updatedItem);
    return this;
  }

  public ApiDavCalObjectsChanges removeUpdatedItem(ApiDavCalObjectChanged updatedItem) {
    if (updatedItem != null && this.updated != null) {
      this.updated.remove(updatedItem);
    }

    return this;
  }
  /**
   * Items that have been deleted
   **/
  public ApiDavCalObjectsChanges deleted(List<ApiDavCalObjectChanged> deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Items that have been deleted")
  @JsonProperty("deleted")
  @NotNull
  public List<ApiDavCalObjectChanged> getDeleted() {
    return deleted;
  }

  @JsonProperty("deleted")
  public void setDeleted(List<ApiDavCalObjectChanged> deleted) {
    this.deleted = deleted;
  }

  public ApiDavCalObjectsChanges addDeletedItem(ApiDavCalObjectChanged deletedItem) {
    if (this.deleted == null) {
      this.deleted = new ArrayList<>();
    }

    this.deleted.add(deletedItem);
    return this;
  }

  public ApiDavCalObjectsChanges removeDeletedItem(ApiDavCalObjectChanged deletedItem) {
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
    ApiDavCalObjectsChanges davCalObjectsChanges = (ApiDavCalObjectsChanges) o;
    return Objects.equals(this.syncToken, davCalObjectsChanges.syncToken) &&
        Objects.equals(this.inserted, davCalObjectsChanges.inserted) &&
        Objects.equals(this.updated, davCalObjectsChanges.updated) &&
        Objects.equals(this.deleted, davCalObjectsChanges.deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(syncToken, inserted, updated, deleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavCalObjectsChanges {\n");
    
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

