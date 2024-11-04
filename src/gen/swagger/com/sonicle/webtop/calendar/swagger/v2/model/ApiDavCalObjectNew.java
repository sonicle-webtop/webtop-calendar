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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Bean for carry new calendar object&#39;s fields
 **/
@ApiModel(description = "Bean for carry new calendar object's fields")
@JsonTypeName("DavCalObjectNew")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public class ApiDavCalObjectNew   {
  private @Valid String href;
  private @Valid String icalendar;

  /**
   * Reference URI
   **/
  public ApiDavCalObjectNew href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Reference URI")
  @JsonProperty("href")
  @NotNull
  public String getHref() {
    return href;
  }

  @JsonProperty("href")
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Calendar data (iCalendar format)
   **/
  public ApiDavCalObjectNew icalendar(String icalendar) {
    this.icalendar = icalendar;
    return this;
  }

  
  @ApiModelProperty(value = "Calendar data (iCalendar format)")
  @JsonProperty("icalendar")
  public String getIcalendar() {
    return icalendar;
  }

  @JsonProperty("icalendar")
  public void setIcalendar(String icalendar) {
    this.icalendar = icalendar;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavCalObjectNew davCalObjectNew = (ApiDavCalObjectNew) o;
    return Objects.equals(this.href, davCalObjectNew.href) &&
        Objects.equals(this.icalendar, davCalObjectNew.icalendar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(href, icalendar);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavCalObjectNew {\n");
    
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
    sb.append("    icalendar: ").append(toIndentedString(icalendar)).append("\n");
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

