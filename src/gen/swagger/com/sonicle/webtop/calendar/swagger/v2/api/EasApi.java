package com.sonicle.webtop.calendar.swagger.v2.api;

import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncEvent;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncEventStat;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncEventUpdate;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEasSyncFolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/eas/folders")
@Api(description = "the eas API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:39:39.410+01:00[Europe/Berlin]")
public abstract class EasApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/{folderId}/messages")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a message", notes = "Add new Event into specified Calendar.", response = ApiEasSyncEventStat.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = ApiEasSyncEventStat.class)
    })
    public Response addMessage(@PathParam("folderId") @ApiParam("Folder ID") String folderId,@Valid @NotNull ApiEasSyncEventUpdate body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/{folderId}/messages/{id}")
    @ApiOperation(value = "Deletes a message", notes = "Deletes specified Event.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class)
    })
    public Response deleteMessage(@PathParam("folderId") @ApiParam("Folder ID") String folderId,@PathParam("id") @ApiParam("Message ID") String id) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "List all folders", notes = "Returns a list of available Calendars with enabled synchronization.", response = ApiEasSyncFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEasSyncFolder.class, responseContainer = "List")
    })
    public Response getFolders() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderId}/messages/{id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a single message", notes = "Gets specified Event.", response = ApiEasSyncEvent.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEasSyncEvent.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class)
    })
    public Response getMessage(@PathParam("folderId") @ApiParam("Folder ID") String folderId,@PathParam("id") @ApiParam("Message ID") String id) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderId}/messages-stats")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all messages for a specific folder", notes = "Returns sync informations for the specified Calendar.", response = ApiEasSyncEventStat.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEasSyncEventStat.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class)
    })
    public Response getMessagesStats(@PathParam("folderId") @ApiParam("Folder ID") String folderId,@QueryParam("cutoffDate")  @ApiParam("Cut-off date (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)")  String cutoffDate) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{folderId}/messages/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a message", notes = "Updates specified Event.", response = ApiEasSyncEventStat.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "eas" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiEasSyncEventStat.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class)
    })
    public Response updateMessage(@PathParam("folderId") @ApiParam("Folder ID") String folderId,@PathParam("id") @ApiParam("Message ID") String id,@Valid @NotNull ApiEasSyncEventUpdate body) {
        return Response.ok().entity("magic!").build();
    }
}
