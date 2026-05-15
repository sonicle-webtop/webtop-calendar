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



@JsonTypeName("EventQuick")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public class ApiEventQuick   {
  private @Valid String start;
  private @Valid String end;
  private @Valid String title;

  /**
   * The (inclusive) start time of the event. For a recurring event, this is the start time of the first instance.
   **/
  public ApiEventQuick start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(value = "The (inclusive) start time of the event. For a recurring event, this is the start time of the first instance.")
  @JsonProperty("start")
  public String getStart() {
    return start;
  }

  @JsonProperty("start")
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * The (exclusive) end time of the event. For a recurring event, this is the end time of the first instance.
   **/
  public ApiEventQuick end(String end) {
    this.end = end;
    return this;
  }

  
  @ApiModelProperty(value = "The (exclusive) end time of the event. For a recurring event, this is the end time of the first instance.")
  @JsonProperty("end")
  public String getEnd() {
    return end;
  }

  @JsonProperty("end")
  public void setEnd(String end) {
    this.end = end;
  }

  /**
   * Title of the event.
   **/
  public ApiEventQuick title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(value = "Title of the event.")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventQuick eventQuick = (ApiEventQuick) o;
    return Objects.equals(this.start, eventQuick.start) &&
        Objects.equals(this.end, eventQuick.end) &&
        Objects.equals(this.title, eventQuick.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end, title);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventQuick {\n");
    
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
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
