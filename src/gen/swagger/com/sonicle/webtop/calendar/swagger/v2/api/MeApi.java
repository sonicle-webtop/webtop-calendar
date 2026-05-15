package com.sonicle.webtop.calendar.swagger.v2.api;

import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendar;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEvent;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventEx;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventLkpInstance;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventQuick;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventResponse;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResultDelta;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/me")
@Api(description = "the me API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public abstract class MeApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/calendars")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Calendar", notes = "Adds new category specifying the owning user ID. If no user ID is provided, the owner will be the current user.", response = ApiCalendar.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Calendar created", response = ApiCalendar.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response addCalendar(@QueryParam("user_id")  @ApiParam("The ID of a user")  String userId,@Valid ApiCalendarBase body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/calendars/{calendar_id}/events")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Event into Calendar", notes = "Adds new Event into given Calendar.", response = ApiEvent.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Event created", response = ApiEvent.class),
        @ApiResponse(code = 404, message = "Calendar not Found", response = Void.class)
    })
    public Response addCalendarEvent(@PathParam("calendar_id") String calendarId,@Valid ApiEventEx body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/events")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Event", notes = "Adds new Event into specified Calendar.", response = ApiEvent.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Event created", response = ApiEvent.class)
    })
    public Response addEvent(@QueryParam("calendar_id") @NotNull  @ApiParam("The Calendar ID where the event will be added into")  String calendarId,@Valid ApiEventEx body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/calendars/{calendar_id}")
    @ApiOperation(value = "Delete a Calendar", notes = "Delete a Calendar given its ID.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Calendar deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response deleteCalendar(@PathParam("calendar_id") String calendarId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/events/instances/{event_instance_id}")
    @ApiOperation(value = "Delete an Event instance", notes = "Delete a specific Event instance identified by its instance ID.  The event_instance_id uses the format {eventId}.{date} and determines the scope of the deletion: - {eventId}.00000000 — targets a single event or the master event of a recurring series. For single events, the event is permanently deleted. For master events, the entire series is deleted including all its instances and exceptions. - {eventId}.{YYYYMMDD} — targets a single occurrence; the occurrence is removed from the series and added to the exclusion list of the master event, leaving the rest of the series unaffected.  When targeting a single occurrence, the optional modify_since boolean parameter extends the scope of the deletion: - modify_since=false (default) — deletes only the specified instance - modify_since=true — splits the series at the specified date; all occurrences from that date onward are deleted, while earlier occurrences remain unchanged. This operation truncates the master event's recurrence rule accordingly.  Note: modify_since is ignored for single events and master events.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Event deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Event not found", response = Void.class)
    })
    public Response deleteEventInstance(@PathParam("event_instance_id") String eventInstanceId,@QueryParam("modify_since") @DefaultValue("false")  @ApiParam("For recurring events specifies that the change affects instances from the current onward.")  Boolean modifySince,@QueryParam("notify") @DefaultValue("all")  @ApiParam("Attendees who should receive notifications about the creation/modification of the new event.")  String notify) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendar_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Calendar", notes = "Gets the specified Calendar given its ID.", response = ApiCalendar.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiCalendar.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response getCalendar(@PathParam("calendar_id") String calendarId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/events/instances/{event_instance_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an Event instance", notes = "Retrieve a specific event instance identified by its instance ID.  The event_instance_id uses the format {eventId}.{date} and determines what is returned:  - {eventId}.00000000 — targets a single event or the master event of a recurring series. Returns the event object with its recurrence rules, but does not expand the individual occurrences of the series. - {eventId}.{YYYYMMDD} — targets a single occurrence or an exception. Returns the resolved instance for that date, merging the master event data with any exception overrides applicable to that occurrence.  Param *_select* supports the following fields: displayName, title, firstName, lastName, nickname, mobile, pager1, pager2, email1, email2, email3, im1, im2, im3, workAddress, workPostalCode, workCity, workState, workCountry, workTelephone1, workTelephone2, workFax, homeAddress, homePostalCode, homeCity, homeState, homeCountry, homeTelephone1, homeTelephone2, homeFax, otherAddress, otherPostalCode, otherCity, otherState, otherCountry, taxCode, vatNumber, eInvoicingCode", response = ApiEvent.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEvent.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Event not found", response = Void.class)
    })
    public Response getEventInstance(@PathParam("event_instance_id") String eventInstanceId,@QueryParam("get_options") @Min(0) @DefaultValue("1")  @ApiParam("Bitmask that specifies which parts of the event must be updated. Multiple options can be combined by summing their values.  Flags: 1&#x3D;Attendees")  Integer getOptions,@QueryParam("_select")  @ApiParam("List (comma-separated) of field names to include in resulting items. Optional, if omitted all available field will be taken into account.")  String select) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendar_id}/events")
    @Produces({ "application/json" })
    @ApiOperation(value = "List Events of a Calendar", notes = "Returns a list of Events from specified Calendar.  Param *_filter* supports the following fields in RSQL query conditions: id, createdAt, updatedAt, rowStatus, status, organizer, organizerId, title, location, description, timezone, allDay, start, end, visibility, transparency, tagId  Param *_select* supports the following fields: publicUid, location, description, visibility, transparency, href, reminder", response = ApiEventsResult.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEventsResult.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response listCalendarEvents(@PathParam("calendar_id") String calendarId,@QueryParam("_filter")  @ApiParam("A RSQL filter query to filter out resulting items. Optional.")  String filter,@QueryParam("_select")  @ApiParam("List (comma-separated) of field names to include in resulting items. Optional, if omitted all available field will be taken into account.")  String select,@QueryParam("_order_by")  @ApiParam("List (comma-separated) of field names and direction (ASC or DESC) to sort resulting items. Optional.")  String orderBy,@QueryParam("_page_no") @Min(1)  @ApiParam("The page number to return, providing a value actually activates pagination. Optional.")  Integer pageNo,@QueryParam("_page_size") @Min(1)  @ApiParam("How many items to return when paginating. Defaults to 50.")  Integer pageSize,@QueryParam("_return_count")  @ApiParam("Specifies whether to compute and return the full count of a list of items. Useful when dealing with paginated data. Optional.")  Boolean returnCount) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendar_id}/events/delta")
    @Produces({ "application/json" })
    @ApiOperation(value = "List changes on Events collection", notes = "Get a set of Events that have been added, deleted, or updated in a specified Calendar, starting from a precise instant identified by a given syncToken.  Param *_select* supports the following fields: location, description, visibility, transparency, href, reminder", response = ApiEventsResultDelta.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEventsResultDelta.class)
    })
    public Response listCalendarEventsDelta(@PathParam("calendar_id") String calendarId,@QueryParam("_sync_token")  @ApiParam("Token exchanged between client and server that tracks changes from a precise state.")  String syncToken,@QueryParam("_select")  @ApiParam("List (comma-separated) of field names to include in resulting items. Optional, if omitted all available field will be taken into account.")  String select) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars")
    @Produces({ "application/json" })
    @ApiOperation(value = "List Calendars", notes = "Returns a list of Calendars readable by the current user: this includes both personal and incoming shared categories.  Param *_filter* supports the following fields in RSQL condition: id, createdAt, updatedAt, userId, builtIn, provider, name, description, color, easSync  Param *_select* supports the following fields: provider, builtIn, name, description, color, easSync, defSensitivity, defShowAs, defReminder, remoteSyncFrequency, remoteSyncTimestamp, remoteSyncToken", response = ApiCalendarsResult.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiCalendarsResult.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response listCalendars(@QueryParam("_filter")  @ApiParam("A RSQL filter query to filter out resulting items. Optional.")  String filter,@QueryParam("_select")  @ApiParam("List (comma-separated) of field names to include in resulting items. Optional, if omitted all available field will be taken into account.")  String select,@QueryParam("_order_by")  @ApiParam("List (comma-separated) of field names and direction (ASC or DESC) to sort resulting items. Optional.")  String orderBy,@QueryParam("_page_no") @Min(1)  @ApiParam("The page number to return, providing a value actually activates pagination. Optional.")  Integer pageNo,@QueryParam("_page_size") @Min(1)  @ApiParam("How many items to return when paginating. Defaults to 50.")  Integer pageSize,@QueryParam("_return_count")  @ApiParam("Specifies whether to compute and return the full count of a list of items. Useful when dealing with paginated data. Optional.")  Boolean returnCount) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/events/instances")
    @Produces({ "application/json" })
    @ApiOperation(value = "List Event instances in a specific time-range.", notes = "Returns a list of Event instances from specified calendars.  Param *_filter* supports the following fields in RSQL query conditions: id, createdAt, updatedAt, rowStatus, status, organizer, organizerId, title, location, description, timezone, allDay, start, end, visibility, transparency, tagId  Param *_select* supports the following fields: publicUid, location, description, visibility, transparency, href, reminder", response = ApiEventLkpInstance.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEventLkpInstance.class, responseContainer = "List"),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response listEventInstances(@QueryParam("calendar_ids") @NotNull  @ApiParam("List (comma-separated) of Calendar IDs whose contacts should be included in the results.")  String calendarIds,@QueryParam("rangeStart") @NotNull  @ApiParam("The start date and time (inclusive) of the time range, represented in ISO 8601 format and UTC time.")  String rangeStart,@QueryParam("rangeEnd") @NotNull  @ApiParam("The end date and time (exclusive) of the time range, represented in ISO 8601 format and UTC time.")  String rangeEnd,@QueryParam("sort") @DefaultValue("false")  @ApiParam("Specified wheather to sort results by date")  Boolean sort,@QueryParam("_filter")  @ApiParam("A RSQL filter query to filter out resulting items. Optional.")  String filter,@QueryParam("_select")  @ApiParam("List (comma-separated) of field names to include in resulting items. Optional, if omitted all available field will be taken into account.")  String select) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/event/instances/dates")
    @Produces({ "application/json" })
    @ApiOperation(value = "List dates in a specific time-range within Event instances.", notes = "Returns a list of dates that contains any Event instances according to specified calendars.  Param *_filter* supports the following fields in RSQL query conditions: id, createdAt, updatedAt, rowStatus, status, organizer, organizerId, title, location, description, timezone, allDay, start, end, visibility, transparency, tagId", response = String.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response listEventInstancesDates(@QueryParam("calendar_ids") @NotNull  @ApiParam("List (comma-separated) of Calendar IDs whose contacts should be included in the results.")  String calendarIds,@QueryParam("rangeStart") @NotNull  @ApiParam("The start date and time (inclusive) of the time range, represented in ISO 8601 format and UTC time.")  String rangeStart,@QueryParam("rangeEnd") @NotNull  @ApiParam("The end date and time (exclusive) of the time range, represented in ISO 8601 format and UTC time.")  String rangeEnd,@QueryParam("_filter")  @ApiParam("A RSQL filter query to filter out resulting items. Optional.")  String filter) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/calendars/{calendar_id}")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Update a Calendar", notes = "Update the specified Calendar given its ID. You can choose to update the entire object or only a subset of data.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Calendar updated", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Not allowed", response = Void.class)
    })
    public Response updateCalendar(@PathParam("calendar_id") String calendarId,@Valid ApiCalendarBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/events/instances/{event_instance_id}")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Update an Event instance", notes = "Update a specific Event instance identified by its instance ID.  The event_instance_id uses the format {eventId}.{date} and determines the scope of the update: - {eventId}.00000000 — targets a single event or the master event of a recurring series. For single events, the update applies to the event itself. For master events, the update applies to all instances in the series, preserving any existing exceptions. - {eventId}.{YYYYMMDD} — targets a single occurrence; the update applies only to that instance, which is promoted to an exception within the series.  When targeting a single occurrence, the optional modify_since boolean parameter extends the scope of the update: - modify_since=false (default) — updates only the specified instance - modify_since=true — splits the series at the specified date; all occurrences from that date onward inherit the update, while earlier occurrences remain unchanged  Note: modify_since is ignored for single events and master events. You can update the entire event object or a subset of its fields.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event updated", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Event not found", response = Void.class)
    })
    public Response updateEventInstance(@PathParam("event_instance_id") String eventInstanceId,@QueryParam("modify_since") @DefaultValue("false")  @ApiParam("For recurring events specifies that the change affects instances from the current onward.")  Boolean modifySince,@QueryParam("update_options") @Min(0) @DefaultValue("1")  @ApiParam("Bitmask that specifies which parts of the event must be updated. Multiple options can be combined by summing their values.  Flags: 1&#x3D;Attendees")  Integer updateOptions,@QueryParam("notify") @DefaultValue("all")  @ApiParam("Attendees who should receive notifications about the creation/modification of the new event.")  String notify,@QueryParam("_update")  @ApiParam("List (comma-separated) of field names to update. Optional, if omitted all available field will be taken into account.")  String update,@Valid ApiEventEx body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/events/instances/{event_instance_id}/quick")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Update an Event instance quickly", notes = "Update the specified Event instance given its instance ID. This is the quick version of the update operation where you can specify fresh values for some main fields.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event updated", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Event not found", response = Void.class)
    })
    public Response updateEventInstanceQuick(@PathParam("event_instance_id") String eventInstanceId,@QueryParam("modify_since") @DefaultValue("false")  @ApiParam("For recurring events specifies that the change affects instances from the current onward.")  Boolean modifySince,@QueryParam("notify") @DefaultValue("all")  @ApiParam("Attendees who should receive notifications about the creation/modification of the new event.")  String notify,@Valid ApiEventQuick body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/events/instances/{event_instance_id}/response")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Update the response to an Event invitation", notes = "Update the invitation response for the specified Event instance.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 202, message = "Accepted", response = Void.class),
        @ApiResponse(code = 404, message = "Event or invitation not found", response = Void.class)
    })
    public Response updateEventInstanceResponse(@PathParam("event_instance_id") String eventInstanceId,@Valid ApiEventResponse apiEventResponse) {
        return Response.ok().entity("magic!").build();
    }
}
