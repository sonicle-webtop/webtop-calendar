package com.sonicle.webtop.calendar.swagger.v1.model;

import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventData;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry event&#39;s updateable fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry event's updateable fields")

public class SyncEventUpdate   {
  
  private @Valid SyncEventData data = null;
  private @Valid List<SyncEventData> exceptions = new ArrayList<SyncEventData>();

  /**
   **/
  public SyncEventUpdate data(SyncEventData data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("data")
  @NotNull
  public SyncEventData getData() {
    return data;
  }
  public void setData(SyncEventData data) {
    this.data = data;
  }

  /**
   **/
  public SyncEventUpdate exceptions(List<SyncEventData> exceptions) {
    this.exceptions = exceptions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("exceptions")
  public List<SyncEventData> getExceptions() {
    return exceptions;
  }
  public void setExceptions(List<SyncEventData> exceptions) {
    this.exceptions = exceptions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncEventUpdate syncEventUpdate = (SyncEventUpdate) o;
    return Objects.equals(data, syncEventUpdate.data) &&
        Objects.equals(exceptions, syncEventUpdate.exceptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, exceptions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SyncEventUpdate {\n");
    
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    exceptions: ").append(toIndentedString(exceptions)).append("\n");
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

