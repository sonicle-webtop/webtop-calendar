package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventChanged;
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

/**
 * Represent a response object that returns a collection of Events.
 **/
@ApiModel(description = "Represent a response object that returns a collection of Events.")
@JsonTypeName("EventsResultDelta")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiEventsResultDelta   {
  private @Valid String nextSyncToken;
  private @Valid List<ApiEventChanged> items = new ArrayList<>();

  /**
   * The syncToken that identifies the instant in past since to get changes.
   **/
  public ApiEventsResultDelta nextSyncToken(String nextSyncToken) {
    this.nextSyncToken = nextSyncToken;
    return this;
  }

  
  @ApiModelProperty(value = "The syncToken that identifies the instant in past since to get changes.")
  @JsonProperty("nextSyncToken")
  public String getNextSyncToken() {
    return nextSyncToken;
  }

  @JsonProperty("nextSyncToken")
  public void setNextSyncToken(String nextSyncToken) {
    this.nextSyncToken = nextSyncToken;
  }

  /**
   **/
  public ApiEventsResultDelta items(List<ApiEventChanged> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("items")
  @NotNull
  public List<ApiEventChanged> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<ApiEventChanged> items) {
    this.items = items;
  }

  public ApiEventsResultDelta addItemsItem(ApiEventChanged itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public ApiEventsResultDelta removeItemsItem(ApiEventChanged itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
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
    ApiEventsResultDelta eventsResultDelta = (ApiEventsResultDelta) o;
    return Objects.equals(this.nextSyncToken, eventsResultDelta.nextSyncToken) &&
        Objects.equals(this.items, eventsResultDelta.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nextSyncToken, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventsResultDelta {\n");
    
    sb.append("    nextSyncToken: ").append(toIndentedString(nextSyncToken)).append("\n");
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
