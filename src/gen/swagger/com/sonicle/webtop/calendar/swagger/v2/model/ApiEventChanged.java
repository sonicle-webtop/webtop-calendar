package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEvent;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventRecurrence;
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
 * Represent an intermediate object for better organizing changes.
 **/
@ApiModel(description = "Represent an intermediate object for better organizing changes.")
@JsonTypeName("EventChanged")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public class ApiEventChanged extends ApiEvent  {
  private @Valid Boolean $added;
  private @Valid Boolean $updated;
  private @Valid Boolean $deleted;

  /**
   **/
  public ApiEventChanged $added(Boolean $added) {
    this.$added = $added;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("$added")
  public Boolean get$Added() {
    return $added;
  }

  @JsonProperty("$added")
  public void set$Added(Boolean $added) {
    this.$added = $added;
  }

  /**
   **/
  public ApiEventChanged $updated(Boolean $updated) {
    this.$updated = $updated;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("$updated")
  public Boolean get$Updated() {
    return $updated;
  }

  @JsonProperty("$updated")
  public void set$Updated(Boolean $updated) {
    this.$updated = $updated;
  }

  /**
   **/
  public ApiEventChanged $deleted(Boolean $deleted) {
    this.$deleted = $deleted;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("$deleted")
  public Boolean get$Deleted() {
    return $deleted;
  }

  @JsonProperty("$deleted")
  public void set$Deleted(Boolean $deleted) {
    this.$deleted = $deleted;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventChanged eventChanged = (ApiEventChanged) o;
    return Objects.equals(this.$added, eventChanged.$added) &&
        Objects.equals(this.$updated, eventChanged.$updated) &&
        Objects.equals(this.$deleted, eventChanged.$deleted) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash($added, $updated, $deleted, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventChanged {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    $added: ").append(toIndentedString($added)).append("\n");
    sb.append("    $updated: ").append(toIndentedString($updated)).append("\n");
    sb.append("    $deleted: ").append(toIndentedString($deleted)).append("\n");
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
