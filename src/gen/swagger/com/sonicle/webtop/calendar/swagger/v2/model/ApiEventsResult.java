package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEvent;
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
@JsonTypeName("EventsResult")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiEventsResult   {
  private @Valid Integer totalCount;
  private @Valid List<ApiEvent> items;

  /**
   * Items total count, usually used for pagination.
   **/
  public ApiEventsResult totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  
  @ApiModelProperty(value = "Items total count, usually used for pagination.")
  @JsonProperty("totalCount")
  public Integer getTotalCount() {
    return totalCount;
  }

  @JsonProperty("totalCount")
  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  /**
   **/
  public ApiEventsResult items(List<ApiEvent> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("items")
  public List<ApiEvent> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<ApiEvent> items) {
    this.items = items;
  }

  public ApiEventsResult addItemsItem(ApiEvent itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public ApiEventsResult removeItemsItem(ApiEvent itemsItem) {
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
    ApiEventsResult eventsResult = (ApiEventsResult) o;
    return Objects.equals(this.totalCount, eventsResult.totalCount) &&
        Objects.equals(this.items, eventsResult.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalCount, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventsResult {\n");
    
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
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
