package com.sonicle.webtop.calendar.swagger.v1.api;

import com.sonicle.webtop.calendar.swagger.v1.model.CalObject;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectsChanges;
import com.sonicle.webtop.calendar.swagger.v1.model.Calendar;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarUpdate;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/caldav")
@Api(description = "the caldav API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-11-21T12:38:18.813+01:00")
public abstract class CaldavApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/calendars/{calendarUid}/objects")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Adds a new calendar object", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = Void.class) })
    public Response addCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@Valid CalObjectNew body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/calendars")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a new calendar", notes = "", response = Calendar.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-calendars",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = Calendar.class) })
    public Response addCalendar(@Valid CalendarNew body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/calendars/{calendarUid}/objects/{href}")
    @ApiOperation(value = "Deletes a calendar object", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class) })
    public Response deleteCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/calendars/{calendarUid}")
    @ApiOperation(value = "Deletes a calendar", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-calendars",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class),
        @ApiResponse(code = 405, message = "Delete operation is not allowed", response = Void.class) })
    public Response deleteCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendarUid}/objects/{href}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a single calendar object", notes = "", response = CalObject.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = CalObject.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class) })
    public Response getCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendarUid}/objects")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all calendar objects belonging to a specific calendar", notes = "", response = CalObject.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = CalObject.class, responseContainer = "List") })
    public Response getCalObjects(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@QueryParam("hrefs")    List<String> hrefs) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendarUid}/objects/changes")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get calendar object changes", notes = "Returns changed calendar objects (added/modified/deleted) since the specified syncToken. If token is not provided, the initial sync configuration will be returned.", response = CalObjectsChanges.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = CalObjectsChanges.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class) })
    public Response getCalObjectsChanges(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@QueryParam("syncToken")   @ApiParam("Marks changes starting point")  String syncToken,@QueryParam("limit")   @ApiParam("Limits the number of returned results")  Integer limit) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars/{calendarUid}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a single calendar", notes = "", response = Calendar.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-calendars",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Calendar.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class) })
    public Response getCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/calendars")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all calendars", notes = "", response = Calendar.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-calendars",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Calendar.class, responseContainer = "List") })
    public Response getCalendars() {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/calendars/{calendarUid}/objects/{href}")
    @Consumes({ "text/calendar" })
    @ApiOperation(value = "Updates a calendar object", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-cal-objects",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class) })
    public Response updateCalObject(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@PathParam("href") @ApiParam("CalObject reference URI") String href,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/calendars/{calendarUid}")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Updates a calendar", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav-calendars" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Calendar not found", response = Void.class) })
    public Response updateCalendar(@PathParam("calendarUid") @ApiParam("Calendar UID") String calendarUid,@Valid CalendarUpdate body) {
        return Response.ok().entity("magic!").build();
    }
}
