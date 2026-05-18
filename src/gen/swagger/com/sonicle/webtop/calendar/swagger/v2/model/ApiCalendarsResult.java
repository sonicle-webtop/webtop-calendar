package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendar;
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
 * Represents a response object for a collection of Calendars.
 **/
@ApiModel(description = "Represents a response object for a collection of Calendars.")
@JsonTypeName("CalendarsResult")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T14:26:01.620+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiCalendarsResult   {
  private @Valid Integer totalCount;
  private @Valid List<ApiCalendar> items;

  /**
   * Items total count, usually used for pagination.
   **/
  public ApiCalendarsResult totalCount(Integer totalCount) {
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
  public ApiCalendarsResult items(List<ApiCalendar> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("items")
  public List<ApiCalendar> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<ApiCalendar> items) {
    this.items = items;
  }

  public ApiCalendarsResult addItemsItem(ApiCalendar itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public ApiCalendarsResult removeItemsItem(ApiCalendar itemsItem) {
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
    ApiCalendarsResult calendarsResult = (ApiCalendarsResult) o;
    return Objects.equals(this.totalCount, calendarsResult.totalCount) &&
        Objects.equals(this.items, calendarsResult.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalCount, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendarsResult {\n");
    
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
