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

/**
 * Represent a Calendar object with base updateable fields.
 **/
@ApiModel(description = "Represent a Calendar object with base updateable fields.")
@JsonTypeName("CalendarBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T16:34:35.908+02:00[Europe/Berlin]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiCalendarBase   {
  public enum ProviderEnum {

    LOCAL(String.valueOf("local")), WEBCAL(String.valueOf("webcal")), CALDAV(String.valueOf("caldav"));


    private String value;

    ProviderEnum (String v) {
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
	public static ProviderEnum fromString(String s) {
        for (ProviderEnum b : ProviderEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static ProviderEnum fromValue(String value) {
        for (ProviderEnum b : ProviderEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid ProviderEnum provider = ProviderEnum.LOCAL;
  private @Valid Boolean builtIn = false;
  private @Valid String name;
  private @Valid String description;
  private @Valid String color = "#F3F4F6";
  public enum EasSyncEnum {

    OFF(String.valueOf("off")), READ(String.valueOf("read")), READ_WRITE(String.valueOf("read-write"));


    private String value;

    EasSyncEnum (String v) {
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
	public static EasSyncEnum fromString(String s) {
        for (EasSyncEnum b : EasSyncEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static EasSyncEnum fromValue(String value) {
        for (EasSyncEnum b : EasSyncEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid EasSyncEnum easSync;
  public enum DefVisibilityEnum {

    PU(String.valueOf("PU")), PV(String.valueOf("PV"));


    private String value;

    DefVisibilityEnum (String v) {
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
	public static DefVisibilityEnum fromString(String s) {
        for (DefVisibilityEnum b : DefVisibilityEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static DefVisibilityEnum fromValue(String value) {
        for (DefVisibilityEnum b : DefVisibilityEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid DefVisibilityEnum defVisibility;
  public enum DefTransparencyEnum {

    TP(String.valueOf("TP")), OP(String.valueOf("OP"));


    private String value;

    DefTransparencyEnum (String v) {
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
	public static DefTransparencyEnum fromString(String s) {
        for (DefTransparencyEnum b : DefTransparencyEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static DefTransparencyEnum fromValue(String value) {
        for (DefTransparencyEnum b : DefTransparencyEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid DefTransparencyEnum defTransparency;
  private @Valid Integer defReminder;
  private @Valid Integer remoteSyncFrequency;
  private @Valid String remoteSyncTimestamp;
  private @Valid String remoteSyncToken;
  private @Valid String providerParams;

  /**
   * Specifies the data provider. Defaults to &#x60;local&#x60;.
   **/
  public ApiCalendarBase provider(ProviderEnum provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies the data provider. Defaults to `local`.")
  @JsonProperty("provider")
  public ProviderEnum getProvider() {
    return provider;
  }

  @JsonProperty("provider")
  public void setProvider(ProviderEnum provider) {
    this.provider = provider;
  }

  /**
   * Specifies whether the calendar is built-in.
   **/
  public ApiCalendarBase builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether the calendar is built-in.")
  @JsonProperty("builtIn")
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }

  /**
   * The name of the calendar.
   **/
  public ApiCalendarBase name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The name of the calendar.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Optional description of the calendar.
   **/
  public ApiCalendarBase description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Optional description of the calendar.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The color associated to the calendar.
   **/
  public ApiCalendarBase color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "The color associated to the calendar.")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }

  @JsonProperty("color")
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Sets the EAS synchronization status.
   **/
  public ApiCalendarBase easSync(EasSyncEnum easSync) {
    this.easSync = easSync;
    return this;
  }

  
  @ApiModelProperty(value = "Sets the EAS synchronization status.")
  @JsonProperty("easSync")
  public EasSyncEnum getEasSync() {
    return easSync;
  }

  @JsonProperty("easSync")
  public void setEasSync(EasSyncEnum easSync) {
    this.easSync = easSync;
  }

  /**
   * Default visibility value for new events.
   **/
  public ApiCalendarBase defVisibility(DefVisibilityEnum defVisibility) {
    this.defVisibility = defVisibility;
    return this;
  }

  
  @ApiModelProperty(value = "Default visibility value for new events.")
  @JsonProperty("defVisibility")
  public DefVisibilityEnum getDefVisibility() {
    return defVisibility;
  }

  @JsonProperty("defVisibility")
  public void setDefVisibility(DefVisibilityEnum defVisibility) {
    this.defVisibility = defVisibility;
  }

  /**
   * Default transparency value for new events.
   **/
  public ApiCalendarBase defTransparency(DefTransparencyEnum defTransparency) {
    this.defTransparency = defTransparency;
    return this;
  }

  
  @ApiModelProperty(value = "Default transparency value for new events.")
  @JsonProperty("defTransparency")
  public DefTransparencyEnum getDefTransparency() {
    return defTransparency;
  }

  @JsonProperty("defTransparency")
  public void setDefTransparency(DefTransparencyEnum defTransparency) {
    this.defTransparency = defTransparency;
  }

  /**
   * Default reminder value for new events.
   **/
  public ApiCalendarBase defReminder(Integer defReminder) {
    this.defReminder = defReminder;
    return this;
  }

  
  @ApiModelProperty(value = "Default reminder value for new events.")
  @JsonProperty("defReminder")
  public Integer getDefReminder() {
    return defReminder;
  }

  @JsonProperty("defReminder")
  public void setDefReminder(Integer defReminder) {
    this.defReminder = defReminder;
  }

  /**
   * Sync frequency (in minutes) for remote calendars (webcal, caldav).
   * minimum: 0
   **/
  public ApiCalendarBase remoteSyncFrequency(Integer remoteSyncFrequency) {
    this.remoteSyncFrequency = remoteSyncFrequency;
    return this;
  }

  
  @ApiModelProperty(value = "Sync frequency (in minutes) for remote calendars (webcal, caldav).")
  @JsonProperty("remoteSyncFrequency")
 @Min(0)  public Integer getRemoteSyncFrequency() {
    return remoteSyncFrequency;
  }

  @JsonProperty("remoteSyncFrequency")
  public void setRemoteSyncFrequency(Integer remoteSyncFrequency) {
    this.remoteSyncFrequency = remoteSyncFrequency;
  }

  /**
   * The last remote sync execution timestamp, in ISO 8601 format and UTC time.
   **/
  public ApiCalendarBase remoteSyncTimestamp(String remoteSyncTimestamp) {
    this.remoteSyncTimestamp = remoteSyncTimestamp;
    return this;
  }

  
  @ApiModelProperty(value = "The last remote sync execution timestamp, in ISO 8601 format and UTC time.")
  @JsonProperty("remoteSyncTimestamp")
  public String getRemoteSyncTimestamp() {
    return remoteSyncTimestamp;
  }

  @JsonProperty("remoteSyncTimestamp")
  public void setRemoteSyncTimestamp(String remoteSyncTimestamp) {
    this.remoteSyncTimestamp = remoteSyncTimestamp;
  }

  /**
   * The sync-token returned by the last remote sync.
   **/
  public ApiCalendarBase remoteSyncToken(String remoteSyncToken) {
    this.remoteSyncToken = remoteSyncToken;
    return this;
  }

  
  @ApiModelProperty(value = "The sync-token returned by the last remote sync.")
  @JsonProperty("remoteSyncToken")
  public String getRemoteSyncToken() {
    return remoteSyncToken;
  }

  @JsonProperty("remoteSyncToken")
  public void setRemoteSyncToken(String remoteSyncToken) {
    this.remoteSyncToken = remoteSyncToken;
  }

  /**
   * Configuration params for non-local calendars in JSON form.
   **/
  public ApiCalendarBase providerParams(String providerParams) {
    this.providerParams = providerParams;
    return this;
  }

  
  @ApiModelProperty(value = "Configuration params for non-local calendars in JSON form.")
  @JsonProperty("providerParams")
  public String getProviderParams() {
    return providerParams;
  }

  @JsonProperty("providerParams")
  public void setProviderParams(String providerParams) {
    this.providerParams = providerParams;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiCalendarBase calendarBase = (ApiCalendarBase) o;
    return Objects.equals(this.provider, calendarBase.provider) &&
        Objects.equals(this.builtIn, calendarBase.builtIn) &&
        Objects.equals(this.name, calendarBase.name) &&
        Objects.equals(this.description, calendarBase.description) &&
        Objects.equals(this.color, calendarBase.color) &&
        Objects.equals(this.easSync, calendarBase.easSync) &&
        Objects.equals(this.defVisibility, calendarBase.defVisibility) &&
        Objects.equals(this.defTransparency, calendarBase.defTransparency) &&
        Objects.equals(this.defReminder, calendarBase.defReminder) &&
        Objects.equals(this.remoteSyncFrequency, calendarBase.remoteSyncFrequency) &&
        Objects.equals(this.remoteSyncTimestamp, calendarBase.remoteSyncTimestamp) &&
        Objects.equals(this.remoteSyncToken, calendarBase.remoteSyncToken) &&
        Objects.equals(this.providerParams, calendarBase.providerParams);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, builtIn, name, description, color, easSync, defVisibility, defTransparency, defReminder, remoteSyncFrequency, remoteSyncTimestamp, remoteSyncToken, providerParams);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendarBase {\n");
    
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    easSync: ").append(toIndentedString(easSync)).append("\n");
    sb.append("    defVisibility: ").append(toIndentedString(defVisibility)).append("\n");
    sb.append("    defTransparency: ").append(toIndentedString(defTransparency)).append("\n");
    sb.append("    defReminder: ").append(toIndentedString(defReminder)).append("\n");
    sb.append("    remoteSyncFrequency: ").append(toIndentedString(remoteSyncFrequency)).append("\n");
    sb.append("    remoteSyncTimestamp: ").append(toIndentedString(remoteSyncTimestamp)).append("\n");
    sb.append("    remoteSyncToken: ").append(toIndentedString(remoteSyncToken)).append("\n");
    sb.append("    providerParams: ").append(toIndentedString(providerParams)).append("\n");
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
