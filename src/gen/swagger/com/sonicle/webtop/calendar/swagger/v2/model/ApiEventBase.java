package com.sonicle.webtop.calendar.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
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
 * Represent an Event object with base updateable fields.
 **/
@ApiModel(description = "Represent an Event object with base updateable fields.")
@JsonTypeName("EventBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-06T10:50:03.275+02:00[Europe/Berlin]")
public class ApiEventBase   {
  private @Valid String publicUid;
  public enum RowStatusEnum {

    DF(String.valueOf("DF")), RO(String.valueOf("RO"));


    private String value;

    RowStatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static RowStatusEnum fromString(String s) {
        for (RowStatusEnum b : RowStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static RowStatusEnum fromValue(String value) {
        for (RowStatusEnum b : RowStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid RowStatusEnum rowStatus;
  public enum StatusEnum {

    CO(String.valueOf("CO"));


    private String value;

    StatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid StatusEnum status;
  private @Valid ApiEventOrganizer organizer;
  private @Valid String timezone;
  private @Valid Boolean allDay;
  private @Valid String start;
  private @Valid String end;
  private @Valid String title;
  private @Valid String location;
  public enum DescriptionTypeEnum {

    TEXT(String.valueOf("text"));


    private String value;

    DescriptionTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static DescriptionTypeEnum fromString(String s) {
        for (DescriptionTypeEnum b : DescriptionTypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static DescriptionTypeEnum fromValue(String value) {
        for (DescriptionTypeEnum b : DescriptionTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid DescriptionTypeEnum descriptionType;
  private @Valid String description;
  public enum VisibilityEnum {

    PU(String.valueOf("PU")), PV(String.valueOf("PV"));


    private String value;

    VisibilityEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static VisibilityEnum fromString(String s) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static VisibilityEnum fromValue(String value) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid VisibilityEnum visibility;
  public enum TransparencyEnum {

    TP(String.valueOf("TP")), OP(String.valueOf("OP"));


    private String value;

    TransparencyEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static TransparencyEnum fromString(String s) {
        for (TransparencyEnum b : TransparencyEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static TransparencyEnum fromValue(String value) {
        for (TransparencyEnum b : TransparencyEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid TransparencyEnum transparency;
  private @Valid Integer reminder;
  private @Valid String href;

  /**
   **/
  public ApiEventBase publicUid(String publicUid) {
    this.publicUid = publicUid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("publicUid")
  public String getPublicUid() {
    return publicUid;
  }

  @JsonProperty("publicUid")
  public void setPublicUid(String publicUid) {
    this.publicUid = publicUid;
  }

  /**
   * Status of the event row: DF&#x3D;default, RO&#x3D;read-only.
   **/
  public ApiEventBase rowStatus(RowStatusEnum rowStatus) {
    this.rowStatus = rowStatus;
    return this;
  }

  
  @ApiModelProperty(value = "Status of the event row: DF=default, RO=read-only.")
  @JsonProperty("rowStatus")
  public RowStatusEnum getRowStatus() {
    return rowStatus;
  }

  @JsonProperty("rowStatus")
  public void setRowStatus(RowStatusEnum rowStatus) {
    this.rowStatus = rowStatus;
  }

  /**
   * Status of the event: CF&#x3D;confirmed.
   **/
  public ApiEventBase status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Status of the event: CF=confirmed.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public ApiEventBase organizer(ApiEventOrganizer organizer) {
    this.organizer = organizer;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("organizer")
  public ApiEventOrganizer getOrganizer() {
    return organizer;
  }

  @JsonProperty("organizer")
  public void setOrganizer(ApiEventOrganizer organizer) {
    this.organizer = organizer;
  }

  /**
   * The timezone ID. (Formatted as an IANA Time Zone Database name, e.g. \&quot;Europe/Zurich\&quot;.)
   **/
  public ApiEventBase timezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The timezone ID. (Formatted as an IANA Time Zone Database name, e.g. \"Europe/Zurich\".)")
  @JsonProperty("timezone")
  @NotNull
  public String getTimezone() {
    return timezone;
  }

  @JsonProperty("timezone")
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  /**
   * Set to true if the event lasts all day.
   **/
  public ApiEventBase allDay(Boolean allDay) {
    this.allDay = allDay;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Set to true if the event lasts all day.")
  @JsonProperty("allDay")
  @NotNull
  public Boolean getAllDay() {
    return allDay;
  }

  @JsonProperty("allDay")
  public void setAllDay(Boolean allDay) {
    this.allDay = allDay;
  }

  /**
   * The (inclusive) start time of the event. For a recurring event, this is the start time of the first instance.
   **/
  public ApiEventBase start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The (inclusive) start time of the event. For a recurring event, this is the start time of the first instance.")
  @JsonProperty("start")
  @NotNull
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
  public ApiEventBase end(String end) {
    this.end = end;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The (exclusive) end time of the event. For a recurring event, this is the end time of the first instance.")
  @JsonProperty("end")
  @NotNull
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
  public ApiEventBase title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Title of the event.")
  @JsonProperty("title")
  @NotNull
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * The location of the event. Optional.
   **/
  public ApiEventBase location(String location) {
    this.location = location;
    return this;
  }

  
  @ApiModelProperty(value = "The location of the event. Optional.")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }

  @JsonProperty("location")
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Specifies the data format of the description.
   **/
  public ApiEventBase descriptionType(DescriptionTypeEnum descriptionType) {
    this.descriptionType = descriptionType;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies the data format of the description.")
  @JsonProperty("descriptionType")
  public DescriptionTypeEnum getDescriptionType() {
    return descriptionType;
  }

  @JsonProperty("descriptionType")
  public void setDescriptionType(DescriptionTypeEnum descriptionType) {
    this.descriptionType = descriptionType;
  }

  /**
   * Description of the event. Optional.
   **/
  public ApiEventBase description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description of the event. Optional.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Visibility of the event: PU&#x3D;public, PV&#x3D;private
   **/
  public ApiEventBase visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

  
  @ApiModelProperty(value = "Visibility of the event: PU=public, PV=private")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }

  @JsonProperty("visibility")
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  /**
   * Whether the event blocks time on the calendar: TP&#x3D;transparent, OP&#x3D;opaque.
   **/
  public ApiEventBase transparency(TransparencyEnum transparency) {
    this.transparency = transparency;
    return this;
  }

  
  @ApiModelProperty(value = "Whether the event blocks time on the calendar: TP=transparent, OP=opaque.")
  @JsonProperty("transparency")
  public TransparencyEnum getTransparency() {
    return transparency;
  }

  @JsonProperty("transparency")
  public void setTransparency(TransparencyEnum transparency) {
    this.transparency = transparency;
  }

  /**
   * minimum: 0
   **/
  public ApiEventBase reminder(Integer reminder) {
    this.reminder = reminder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("reminder")
 @Min(0)  public Integer getReminder() {
    return reminder;
  }

  @JsonProperty("reminder")
  public void setReminder(Integer reminder) {
    this.reminder = reminder;
  }

  /**
   **/
  public ApiEventBase href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("href")
  public String getHref() {
    return href;
  }

  @JsonProperty("href")
  public void setHref(String href) {
    this.href = href;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEventBase eventBase = (ApiEventBase) o;
    return Objects.equals(this.publicUid, eventBase.publicUid) &&
        Objects.equals(this.rowStatus, eventBase.rowStatus) &&
        Objects.equals(this.status, eventBase.status) &&
        Objects.equals(this.organizer, eventBase.organizer) &&
        Objects.equals(this.timezone, eventBase.timezone) &&
        Objects.equals(this.allDay, eventBase.allDay) &&
        Objects.equals(this.start, eventBase.start) &&
        Objects.equals(this.end, eventBase.end) &&
        Objects.equals(this.title, eventBase.title) &&
        Objects.equals(this.location, eventBase.location) &&
        Objects.equals(this.descriptionType, eventBase.descriptionType) &&
        Objects.equals(this.description, eventBase.description) &&
        Objects.equals(this.visibility, eventBase.visibility) &&
        Objects.equals(this.transparency, eventBase.transparency) &&
        Objects.equals(this.reminder, eventBase.reminder) &&
        Objects.equals(this.href, eventBase.href);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicUid, rowStatus, status, organizer, timezone, allDay, start, end, title, location, descriptionType, description, visibility, transparency, reminder, href);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEventBase {\n");
    
    sb.append("    publicUid: ").append(toIndentedString(publicUid)).append("\n");
    sb.append("    rowStatus: ").append(toIndentedString(rowStatus)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    organizer: ").append(toIndentedString(organizer)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    allDay: ").append(toIndentedString(allDay)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    descriptionType: ").append(toIndentedString(descriptionType)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    transparency: ").append(toIndentedString(transparency)).append("\n");
    sb.append("    reminder: ").append(toIndentedString(reminder)).append("\n");
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
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
