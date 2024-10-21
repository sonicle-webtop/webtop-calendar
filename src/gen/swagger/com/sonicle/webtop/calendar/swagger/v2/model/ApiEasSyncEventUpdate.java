package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncEventData;
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
 * Bean for carry event&#39;s updateable fields
 **/
@ApiModel(description = "Bean for carry event's updateable fields")
@JsonTypeName("EasSyncEventUpdate")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-21T12:09:53.556+02:00[Europe/Berlin]")
public class ApiEasSyncEventUpdate   {
  private @Valid ApiEasSyncEventData data;
  private @Valid List<ApiEasSyncEventData> exceptions = null;

  /**
   **/
  public ApiEasSyncEventUpdate data(ApiEasSyncEventData data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("data")
  @NotNull
  public ApiEasSyncEventData getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(ApiEasSyncEventData data) {
    this.data = data;
  }

  /**
   **/
  public ApiEasSyncEventUpdate exceptions(List<ApiEasSyncEventData> exceptions) {
    this.exceptions = exceptions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("exceptions")
  public List<ApiEasSyncEventData> getExceptions() {
    return exceptions;
  }

  @JsonProperty("exceptions")
  public void setExceptions(List<ApiEasSyncEventData> exceptions) {
    this.exceptions = exceptions;
  }

  public ApiEasSyncEventUpdate addExceptionsItem(ApiEasSyncEventData exceptionsItem) {
    if (this.exceptions == null) {
      this.exceptions = new ArrayList<>();
    }

    this.exceptions.add(exceptionsItem);
    return this;
  }

  public ApiEasSyncEventUpdate removeExceptionsItem(ApiEasSyncEventData exceptionsItem) {
    if (exceptionsItem != null && this.exceptions != null) {
      this.exceptions.remove(exceptionsItem);
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
    ApiEasSyncEventUpdate easSyncEventUpdate = (ApiEasSyncEventUpdate) o;
    return Objects.equals(this.data, easSyncEventUpdate.data) &&
        Objects.equals(this.exceptions, easSyncEventUpdate.exceptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, exceptions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEasSyncEventUpdate {\n");
    
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    exceptions: ").append(toIndentedString(exceptions)).append("\n");
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

