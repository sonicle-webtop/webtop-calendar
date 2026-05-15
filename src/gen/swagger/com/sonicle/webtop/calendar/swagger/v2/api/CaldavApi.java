package com.sonicle.webtop.calendar.swagger.v2.api;

import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalObject;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalObjectNew;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalObjectsChanges;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalendar;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalendarNew;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiDavCalendarUpdate;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/caldav/calendars")
@Api(description = "the caldav API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-15T14:19:21.068+02:00[Europe/Berlin]")
public abstract class CaldavApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/{calendarUid}/objects")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Adds a new calendar object", notes = "Creates new CalendarObject into specified Calendar.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = Void.class)
    })
    public Response addDavCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@Valid @NotNull ApiDavCalObjectNew body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a new calendar", notes = "Creates new Calendar.", response = ApiDavCalendar.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = ApiDavCalendar.class)
    })
    public Response addDavCalendar(@Valid @NotNull ApiDavCalendarNew body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/{calendarUid}/objects/{href}")
    @ApiOperation(value = "Deletes a calendar object", notes = "Deletes specified CalendarObject.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class)
    })
    public Response deleteDavCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/{calendarUid}")
    @ApiOperation(value = "Deletes a calendar", notes = "Deletes specified Calendar.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Delete operation is not allowed", response = Void.class)
    })
    public Response deleteDavCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{calendarUid}/objects/{href}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a single calendar object", notes = "Gets specified CalendarObject.", response = ApiDavCalObject.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavCalObject.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class)
    })
    public Response getDavCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{calendarUid}/objects")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all calendar objects belonging to a specific calendar", notes = "List all CalendarObjects of specified Calendar.", response = ApiDavCalObject.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavCalObject.class, responseContainer = "List")
    })
    public Response getDavCalObjects(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@QueryParam("hrefs")   List<String> hrefs) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{calendarUid}/objects/changes")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get calendar object changes", notes = "Returns changed CalendarObjects (added/modified/deleted) since the specified syncToken. If token is not provided, the initial sync configuration will be returned.", response = ApiDavCalObjectsChanges.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavCalObjectsChanges.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class)
    })
    public Response getDavCalObjectsChanges(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@QueryParam("syncToken")  @ApiParam("Marks changes starting point")  String syncToken,@QueryParam("limit")  @ApiParam("Limits the number of returned results")  Integer limit) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{calendarUid}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a single calendar", notes = "Gets specified Calendar.", response = ApiDavCalendar.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavCalendar.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class)
    })
    public Response getDavCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "List all calendars", notes = "List available Calendars.", response = ApiDavCalendar.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavCalendar.class, responseContainer = "List")
    })
    public Response getDavCalendars() {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{calendarUid}/objects/{href}")
    @Consumes({ "text/calendar" })
    @ApiOperation(value = "Updates a calendar object", notes = "Updates specified CalendarObject.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class)
    })
    public Response updateDavCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href,@Valid @NotNull String body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{calendarUid}")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Updates a calendar", notes = "Updates specified Calendar.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-apikey-username"),
        
        @Authorization(value = "auth-apikey-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class)
    })
    public Response updateDavCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@Valid @NotNull ApiDavCalendarUpdate body) {
        return Response.ok().entity("magic!").build();
    }
}
