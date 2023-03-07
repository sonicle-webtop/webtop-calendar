/* 
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sonicle.commons.BitFlag;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.cache.AbstractPassiveExpiringBulkMap;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeRange;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.time.TimeRange;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.DispositionType;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.ServletUtils.StringArray;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.bean.IntegerSet;
import com.sonicle.commons.web.json.bean.QueryObj;
import com.sonicle.commons.web.json.bean.StringSet;
import com.sonicle.commons.web.json.extjs.FieldMeta;
import com.sonicle.commons.web.json.extjs.GridColumnMeta;
import com.sonicle.commons.web.json.extjs.GridMetadata;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.bol.js.JsCalendar;
import com.sonicle.webtop.calendar.bol.js.JsCalendarLinks;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsCalendarLkp;
import com.sonicle.webtop.calendar.bol.js.JsCalendarSharing;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.calendar.bol.js.JsPletEvents;
import com.sonicle.webtop.calendar.bol.js.JsSharing;
import com.sonicle.webtop.calendar.bol.model.MyCalendarFSFolder;
import com.sonicle.webtop.calendar.bol.model.MyCalendarFSOrigin;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.bol.model.RBEventDetail;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.MyShareFolderCalendar;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.bol.model.SetupDataCalendarRemote;
import com.sonicle.webtop.calendar.io.EventICalFileReader;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarFSFolder;
import com.sonicle.webtop.calendar.model.CalendarFSOrigin;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventAttachmentWithStream;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.model.UpdateTagsOperation;
import com.sonicle.webtop.calendar.msg.RemoteSyncResult;
import com.sonicle.webtop.calendar.rpt.AbstractAgenda;
import com.sonicle.webtop.calendar.rpt.RptAgendaSummary;
import com.sonicle.webtop.calendar.rpt.RptEventsDetail;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek5;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek7;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.app.sdk.AbstractFolderTreeCache;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.app.sdk.WTUnsupportedOperationException;
import com.sonicle.webtop.core.app.sdk.bol.js.JsFolderSharing;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsCustomFieldDefsData;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsWizardData;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.bol.js.ObjSearchableCustomField;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldEx;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.model.FolderShareOriginFolders;
import com.sonicle.webtop.core.app.model.FolderShareOrigin;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.bol.js.JsSubjectLkp;
import com.sonicle.webtop.core.sdk.AsyncActionCollection;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BaseServiceAsyncAction;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import com.sonicle.webtop.core.util.RRuleStringify;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jakarta.mail.internet.InternetAddress;
import java.util.Iterator;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	public static final Logger logger = WT.getLogger(Service.class);
	
	private CalendarManager manager;
	private CalendarServiceSettings ss;
	private CalendarUserSettings us;
	
	public static final String ERP_EXPORT_FILENAME = "events_{0}-{1}-{2}.{3}";
	
	private final KeyedReentrantLocks<String> locks = new KeyedReentrantLocks<>();
	private final SearchableCustomFieldTypeCache cacheSearchableCustomFieldType = new SearchableCustomFieldTypeCache(5, TimeUnit.SECONDS);
	private final FoldersTreeCache foldersTreeCache = new FoldersTreeCache();
	private final LoadingCache<Integer, Optional<CalendarPropSet>> foldersPropsCache = Caffeine.newBuilder().build(new FoldersPropsCacheLoader());
	private StringSet inactiveOrigins = null;
	private IntegerSet inactiveFolders = null;	
	private final AsyncActionCollection<Integer, SyncRemoteCalendarAA> syncRemoteCalendarAAs = new AsyncActionCollection<>();
	
	private ErpExportWizard erpWizard = null;
	private Pattern MEETING_PROVIDERS_URLS_PATTERN = null;

	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (CalendarManager)WT.getServiceManager(SERVICE_ID, false, up.getId());
		ss = new CalendarServiceSettings(SERVICE_ID, up.getDomainId());
		us = new CalendarUserSettings(SERVICE_ID, up.getId());
		MEETING_PROVIDERS_URLS_PATTERN = getEnv().getCoreServiceSettings().getMeetingProvidersURLsPattern();
		initFolders();
	}
	
	@Override
	public void cleanup() throws Exception {
		synchronized(syncRemoteCalendarAAs) {
			syncRemoteCalendarAAs.clear();
		}
		if (inactiveFolders != null) inactiveFolders.clear();
		if (foldersTreeCache != null) foldersTreeCache.clear();
		us = null;
		ss = null;
		manager = null;
	}
	
	@Override
	public ServiceVars returnServiceVars() {
		DateTimeFormatter hmf = DateTimeUtils.createHmFormatter();
		ServiceVars co = new ServiceVars();
		co.put("calendarRemoteSyncEnabled", ss.getCalendarRemoteAutoSyncEnabled());
		co.put("eventStatFieldsVisible", ss.getEventStatisticFieldsVisible());
		co.put("defaultCalendarSync", EnumUtils.toSerializedName(ss.getDefaultCalendarSync()));
		co.put("view", us.getView());
		co.put("timeResolution", us.getSchedulerTimeResolution());
		co.put("workdayStart", hmf.print(us.getWorkdayStart()));
		co.put("workdayEnd", hmf.print(us.getWorkdayEnd()));
		co.put("cfieldsSearchable", LangUtils.serialize(getSearchableCustomFieldDefs(), ObjCustomFieldDefs.FieldsList.class));
		co.put("hasAudit", manager.isAuditEnabled() && (RunContext.isImpersonated() || RunContext.isPermitted(true, CoreManifest.ID, "AUDIT")));
		return co;
	}
	
	private ObjCustomFieldDefs.FieldsList getSearchableCustomFieldDefs() {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ObjCustomFieldDefs.FieldsList scfields = new ObjCustomFieldDefs.FieldsList();
			for (CustomFieldEx cfield : coreMgr.listCustomFields(SERVICE_ID, BitFlag.of(CoreManager.CustomFieldListOptions.SEARCHABLE)).values()) {
				scfields.add(new ObjCustomFieldDefs.Field(cfield, up.getLanguageTag()));
			}
			return scfields;
			
		} catch(Exception ex) {
			return null;
		}
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	private void runSyncRemoteCalendar(int calendarId, String calendarName, boolean full) throws WTException {
		synchronized(syncRemoteCalendarAAs) {
			if (syncRemoteCalendarAAs.containsKey(calendarId)) {
				logger.debug("SyncRemoteCalendar run skipped [{}]", calendarId);
				return;
			}
			final SyncRemoteCalendarAA aa = new SyncRemoteCalendarAA(calendarId, calendarName, full);
			syncRemoteCalendarAAs.put(calendarId, aa);
			aa.start(RunContext.getSubject(), RunContext.getRunProfileId());
		}
	}
	
	private void initFolders() throws Exception {
		foldersTreeCache.init();
		// Retrieves inactive origins set
		inactiveOrigins = us.getInactiveCalendarOrigins();
		if (inactiveOrigins.removeIf(originPid -> !foldersTreeCache.existsOrigin(UserProfileId.parseQuielty(originPid)))) { // Clean-up orphans
			us.setInactiveCalendarOrigins(inactiveOrigins);
		}
		// Retrieves inactive folders set
		inactiveFolders = us.getInactiveCalendarFolders();
		if (inactiveFolders.removeIf(calendarId -> !foldersTreeCache.existsFolder(calendarId))) { // Clean-up orphans
			us.setInactiveCalendarFolders(inactiveFolders);
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
			
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				boolean chooser = ServletUtils.getBooleanParameter(request, "chooser", false);
				
				if (node.equals("root")) { // Tree ROOT node -> list folder origins (resources origins will be grouped)
					boolean hasResources = false;
					// Classic root nodes
					for (CalendarFSOrigin origin : foldersTreeCache.getOrigins()) {
						if (origin.isResource()) {
							hasResources = true;
							continue;
						}
						final ExtTreeNode xnode = createFolderNodeLevel0(chooser, origin);
						if (xnode != null) children.add(xnode);
					}
					// Resources root node
					if (!chooser && hasResources) {
						final ExtTreeNode xnode = createFolderNodeLevel0Resources(chooser, true);
						if (xnode != null) children.add(xnode);
					}
					
				} else {
					boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", false);
					CalendarNodeId nodeId = new CalendarNodeId(node);
					if (nodeId.isGrouperResource() && !chooser) { // Tree node (resources grouper) -> list all resources folder only
						CoreManager coreMgr = WT.getCoreManager();
						for (CalendarFSOrigin origin : foldersTreeCache.getOrigins()) {
							if (!origin.isResource()) continue;
							
							for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
								final ExtTreeNode xnode = createFolderNodeLevel1(chooser, origin, folder, false);
								if (xnode != null) {
									xnode.set("_resourceAvail", coreMgr.getResourceEnabled(origin.getProfileId().getUserId()));
									children.add(xnode);
								}
							}
						}
						
					} else if (CalendarNodeId.Type.ORIGIN.equals(nodeId.getType())) { // Tree node -> list folder of specified origin
						final Integer defaultCalendarId = manager.getDefaultCalendarId();
						final CalendarFSOrigin origin = foldersTreeCache.getOriginByProfile(nodeId.getOriginAsProfileId());
						for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
							if (writableOnly && !folder.getPermissions().getItemsPermissions().has(FolderShare.ItemsRight.CREATE)) continue;
							
							final boolean isDefault = folder.getFolderId().equals(defaultCalendarId);
							final ExtTreeNode xnode = createFolderNodeLevel1(chooser, origin, folder, isDefault);
							if (xnode != null) children.add(xnode);
						}	
						
					} else {
						throw new WTParseException("Unable to parse '{}' as node ID", node);
					}
				}
				new JsonResult("children", children).printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					CalendarNodeId nodeId = new CalendarNodeId(node.id);
					if (CalendarNodeId.Type.ORIGIN.equals(nodeId.getType()) || CalendarNodeId.Type.GROUPER.equals(nodeId.getType())) {
						toggleActiveOrigin(nodeId.getOrigin(), node._active);
					} else if (CalendarNodeId.Type.FOLDER.equals(nodeId.getType()) || CalendarNodeId.Type.FOLDER_RESOURCE.equals(nodeId.getType())) {
						toggleActiveFolder(nodeId.getFolderId(), node._active);
					}
				}
				new JsonResult().printTo(out);
				
			} else if (Crud.DELETE.equals(crud)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					CalendarNodeId nodeId = new CalendarNodeId(node.id);
					if (CalendarNodeId.Type.FOLDER.equals(nodeId.getType()) || CalendarNodeId.Type.FOLDER_RESOURCE.equals(nodeId.getType())) {
						manager.deleteCalendar(nodeId.getFolderId());
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageFoldersTree", ex);
		}
	}
	
	private ExtTreeNode createFolderNodeLevel0Resources(boolean chooser, boolean isActive) {
		CalendarNodeId nodeId = CalendarNodeId.build(CalendarNodeId.Type.GROUPER, CalendarNodeId.GROUPER_RESOURCES_ORIGIN);
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), "{trfolders.origin.resources}", false);
		node.put("_active", isActive);
		if (!chooser) node.setChecked(isActive);
		node.put("expandable", false);
		node.setIconClass("wtcal-icon-calendarResources");
		node.setExpanded(true);
		return node;
	}
	
	private ExtTreeNode createFolderNodeLevel0(boolean chooser, CalendarFSOrigin origin) {
		CalendarNodeId nodeId = CalendarNodeId.build(CalendarNodeId.Type.ORIGIN, origin.getProfileId());
		boolean checked = !inactiveOrigins.contains(origin.getProfileId().toString());
		if (origin instanceof MyCalendarFSOrigin) {
			return createFolderNodeLevel0(chooser, nodeId, "{trfolders.origin.my}", "wtcal-icon-calendarMy", origin.getWildcardPermissions(), checked);
		} else {
			return createFolderNodeLevel0(chooser, nodeId, origin.getDisplayName(), "wtcal-icon-calendarIncoming", origin.getWildcardPermissions(), checked);
		}
	}
	
	private ExtTreeNode createFolderNodeLevel0(boolean chooser, CalendarNodeId nodeId, String text, String iconClass, FolderShare.Permissions originPermissions, boolean isActive) {
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), text, false);
		node.put("_orights", originPermissions.getFolderPermissions().toString(true));
		node.put("_active", isActive);
		node.setIconClass(iconClass);
		if (!chooser) node.setChecked(isActive);
		node.put("expandable", false);
		node.setExpanded(true);
		return node;
	}
	
	private ExtTreeNode createFolderNodeLevel1(boolean chooser, CalendarFSOrigin origin, CalendarFSFolder folder, boolean isDefault) {
		CalendarNodeId.Type type = origin.isResource() ? CalendarNodeId.Type.FOLDER_RESOURCE : CalendarNodeId.Type.FOLDER;
		final CalendarNodeId nodeId = CalendarNodeId.build(type, origin.getProfileId(), folder.getFolderId());
		final String name = origin.isResource() ? origin.getDisplayName() : folder.getDisplayName();
		final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
		final boolean active = !inactiveFolders.contains(folder.getFolderId());
		return createFolderNodeLevel1(chooser, nodeId, name, folder.getPermissions(), folder.getCalendar(), props, isDefault, active);
	}
	
	private ExtTreeNode createFolderNodeLevel1(boolean chooser, CalendarNodeId nodeId, String name, FolderShare.Permissions folderPermissions, Calendar calendar, CalendarPropSet folderProps, boolean isDefault, boolean isActive) {
		String color = calendar.getColor();
		Calendar.Sync sync = Calendar.Sync.OFF;
		
		if (folderProps != null) { // Props are not null only for incoming folders
			if (folderProps.getHiddenOrDefault(false)) return null;
			color = folderProps.getColorOrDefault(color);
			sync = folderProps.getSyncOrDefault(sync);
		} else {
			sync = calendar.getSync();
		}
		
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), name, true);
		//node.put("_ownerId", nodeId.getOriginAsProfileId().toString());
		node.put("_frights", folderPermissions.getFolderPermissions().toString());
		node.put("_irights", folderPermissions.getItemsPermissions().toString());
		//node.put("_calId", nodeId.getFolderId());
		node.put("_builtIn", calendar.getBuiltIn());
		node.put("_provider", EnumUtils.toSerializedName(calendar.getProvider()));
		node.put("_color", Calendar.getHexColor(color));
		node.put("_sync", EnumUtils.toSerializedName(sync));
		node.put("_default", isDefault);
		node.put("_active", isActive);
		node.put("_isPrivate", calendar.getIsPrivate());
		node.put("_defBusy", calendar.getDefaultBusy());
		node.put("_defReminder", calendar.getDefaultReminder());
		if (!chooser) node.setChecked(isActive);
		return node;
	}
	
	public void processLookupCalendarFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCalendarLkp> items = new ArrayList<>();
		
		try {
			Integer defltCalendarId = manager.getDefaultCalendarId();
			for (CalendarFSOrigin origin : foldersTreeCache.getOrigins()) {
				for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
					final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					final boolean isDefault = folder.getFolderId().equals(defltCalendarId);
					items.add(new JsCalendarLkp(origin, folder, props, isDefault, items.size()));
				}
			}
			new JsonResult("folders", items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCalendarFolders", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupResources(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		
		try {
			Set<UserProfileId> originProfiles = manager.listIncomingCalendarOrigins().keySet();
			List<JsSubjectLkp> items = new ArrayList<>();
			for (GenericSubject subject : coreMgr.listSubjects(false, true, false, false, true).values()) {
				final UserProfileId profileId = new UserProfileId(subject.getDomainId(), subject.getName());
				if (originProfiles.contains(profileId)) {
					InternetAddress personalAddress = WT.getProfilePersonalAddress(profileId);
					items.add(new JsSubjectLkp(subject.getName(), subject.getName(), subject.getDisplayName(), (personalAddress != null) ? personalAddress.getAddress() : null, subject.getType()));
				}
			}
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupResources", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "id", true);
				
				CalendarNodeId nodeId = new CalendarNodeId(node);
				FolderSharing.Scope scope = JsCalendarSharing.toFolderSharingScope(nodeId);
				List<FolderSharing.SubjectRights> rights = manager.getFolderShareConfiguration(nodeId.getOriginAsProfileId(), scope);
				String[] sdn = buildSharingDisplayNames(nodeId);
				new JsonResult(new JsCalendarSharing(nodeId, sdn[0], sdn[1], rights)).printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsCalendarSharing> pl = ServletUtils.getPayload(request, JsCalendarSharing.class);
				
				CalendarNodeId nodeId = new CalendarNodeId(pl.data.id);
				FolderSharing.Scope scope = JsCalendarSharing.toFolderSharingScope(nodeId);
				manager.updateFolderShareConfiguration(nodeId.getOriginAsProfileId(), scope, pl.data.toSubjectRights());
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageSharing", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private String[] buildSharingDisplayNames(CalendarNodeId nodeId) throws WTException {
		String originDn = null, folderDn = null;
	
		CalendarFSOrigin origin = foldersTreeCache.getOrigin(nodeId.getOriginAsProfileId());
		if (origin instanceof MyCalendarFSOrigin) {
			originDn = lookupResource(CalendarLocale.CALENDARS_MY);
		} else if (origin instanceof CalendarFSOrigin) {
			originDn = origin.getDisplayName();
		}
		
		if (CalendarNodeId.Type.FOLDER.equals(nodeId.getType())) {
			CalendarFSFolder folder = foldersTreeCache.getFolder(nodeId.getFolderId());
			folderDn = (folder != null) ? folder.getCalendar().getName() : String.valueOf(nodeId.getFolderId());
		}
		
		return new String[]{originDn, folderDn};
	}
	
	public void processManageCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Calendar item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCalendar(id);
				new JsonResult(new JsCalendar(item, getEnv().getProfile().getTimeZone())).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				item = manager.addCalendar(JsCalendar.createCalendar(pl.data));
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				manager.updateCalendar(JsCalendar.createCalendar(pl.data));
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				manager.deleteCalendar(pl.data.calendarId);
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				toggleActiveFolder(pl.data.calendarId, true); // forgets it by simply activating it
				new JsonResult().printTo(out);
				
			} else if (crud.equals("readLinks")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				final CalendarFSFolder folder = foldersTreeCache.getFolder(id);
				if (folder == null) throw new WTException("Calendar not found [{}]", id);
				
				final Map<String, String> links = manager.getCalendarLinks(id);
				new JsonResult(new JsCalendarLinks(id, folder.getCalendar().getName(), links)).printTo(out);
				
			} else if (crud.equals("sync")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				boolean full = ServletUtils.getBooleanParameter(request, "full", false);
				
				item = manager.getCalendar(id);
				if (item == null) throw new WTException("Calendar not found [{0}]", id);
				runSyncRemoteCalendar(id, item.getName(), full);
				
				new JsonResult().printTo(out);
				
			} else if (crud.equals("updateTag")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				UpdateTagsOperation op = ServletUtils.getEnumParameter(request, "op", true, UpdateTagsOperation.class);
				ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
				
				manager.updateEventCalendarTags(op, id, new HashSet<>(tags));
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageCalendar", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetupCalendarRemote(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = getEnv().getWebTopSession();
		final String PROPERTY_PREFIX = "setupremotecalendar-";
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals("s1")) {
				String tag = ServletUtils.getStringParameter(request, "tag", true);
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				String provider = ServletUtils.getStringParameter(request, "provider", true);
				String url = ServletUtils.getStringParameter(request, "url", true);
				String username = ServletUtils.getStringParameter(request, "username", null);
				String password = ServletUtils.getStringParameter(request, "password", null);
				
				Calendar.Provider remoteProvider = EnumUtils.forSerializedName(provider, Calendar.Provider.class);
				URI uri = URIUtils.createURI(url);
				
				CalendarManager.ProbeCalendarRemoteUrlResult result = manager.probeCalendarRemoteUrl(remoteProvider, uri, username, password);
				if (result == null) throw new CalendarProbeException(this.lookupResource("setupCalendarRemote.error.probe"));
				
				SetupDataCalendarRemote setup = new SetupDataCalendarRemote();
				setup.setProfileId(profileId);
				setup.setProvider(remoteProvider);
				setup.setUrl(uri);
				setup.setUsername(username);
				setup.setPassword(password);
				setup.setName(result.displayName);
				wts.setProperty(SERVICE_ID, PROPERTY_PREFIX+tag, setup);
				
				new JsonResult(setup).printTo(out);
				
			} else if(crud.equals("s2")) {
				String tag = ServletUtils.getStringParameter(request, "tag", true);
				String name = ServletUtils.getStringParameter(request, "name", true);
				String color = ServletUtils.getStringParameter(request, "color", true);
				Short syncFrequency = ServletUtils.getShortParameter(request, "syncFrequency", null);
				
				wts.hasPropertyOrThrow(SERVICE_ID, PROPERTY_PREFIX+tag);
				SetupDataCalendarRemote setup = (SetupDataCalendarRemote) wts.getProperty(SERVICE_ID, PROPERTY_PREFIX+tag);
				setup.setName(name);
				setup.setColor(color);
				setup.setSyncFrequency(syncFrequency);
				
				Calendar cal = manager.addCalendar(setup.toCalendar());
				wts.clearProperty(SERVICE_ID, PROPERTY_PREFIX+tag);
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				runSyncRemoteCalendar(cal.getCalendarId(), cal.getName(), true); // Starts a full-sync
				
				new JsonResult().printTo(out);
			}
		
		} catch (CalendarProbeException ex) {
			new JsonResult(ex).printTo(out);
		} catch (Exception ex) {
			logger.error("Error in SetupCalendarRemote", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageHiddenCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				
				CalendarNodeId nodeId = new CalendarNodeId(node);
				if (CalendarNodeId.Type.ORIGIN.equals(nodeId.getType())) {
					final CalendarFSOrigin origin = foldersTreeCache.getOrigin(nodeId.getOriginAsProfileId());
					if (origin instanceof MyCalendarFSOrigin) throw new WTException("Unsupported for personal origin");
					
					ArrayList<JsSimple> items = new ArrayList<>();
					for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
						final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
						if ((props != null) && props.getHiddenOrDefault(false)) {
							items.add(new JsSimple(folder.getFolderId(), folder.getCalendar().getName()));
						}
					}
					new JsonResult(items).printTo(out);
					
				} else if (nodeId.isGrouperResource()) {
					ArrayList<JsSimple> items = new ArrayList<>();
					for (CalendarFSOrigin origin : foldersTreeCache.getOrigins()) {
						if (!origin.isResource()) continue;
						
						for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
							final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
							if ((props != null) && props.getHiddenOrDefault(false)) {
								items.add(new JsSimple(folder.getFolderId(), folder.getCalendar().getName()));
							}
						}
					}
					new JsonResult(items).printTo(out);
					
				} else {
					throw new WTException("Invalid node [{}]", node);
				}
				
				/*
				if (!(CalendarNodeId.Type.ORIGIN.equals(nodeId.getType()) || CalendarNodeId.Type.GROUPER.equals(nodeId.getType()))) throw new WTException("Invalid node [{}]", node);
				final CalendarFSOrigin origin = foldersTreeCache.getOrigin(nodeId.getOriginAsProfileId());
				if (origin instanceof MyCalendarFSOrigin) throw new WTException("Unsupported for personal origin");
				
				ArrayList<JsSimple> items = new ArrayList<>();
				for (CalendarFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
					final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					if ((props != null) && props.getHiddenOrDefault(false)) {
						items.add(new JsSimple(folder.getFolderId(), folder.getCalendar().getName()));
					}
				}
				new JsonResult(items).printTo(out);
				*/
				
			} else if (Crud.UPDATE.equals(crud)) {
				Integer calendarId = ServletUtils.getIntParameter(request, "calendarId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCalendarFolderVisibility(calendarId, hidden);
				new JsonResult().printTo(out);
				
			} else if (Crud.DELETE.equals(crud)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> originProfileIds = new HashSet<>();
				for (String folderId : ids) {
					int calendarId = Integer.valueOf(folderId);
					CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(calendarId);
					if (origin == null) continue;
					originProfileIds.add(origin.getProfileId().toString());
					updateCalendarFolderVisibility(calendarId, null);
				}
				new JsonResult(originProfileIds).printTo(out);
			}
			
		} catch (Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetDefaultCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			
			us.setDefaultCalendarFolder(id);
			Integer defltCalendarId = manager.getDefaultCalendarId();
			new JsonResult(String.valueOf(defltCalendarId)).printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetDefaultCalendar", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetCalendarColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);
			
			updateCalendarFolderColor(id, color);
			new JsonResult().printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetCalendarColor", ex);
			new JsonResult(ex).printTo(out);
		}
	}
				
	public void processSetCalendarSync(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String sync = ServletUtils.getStringParameter(request, "sync", null);
			
			updateCalendarFolderSync(id, EnumUtils.forSerializedName(sync, Calendar.Sync.class));
			new JsonResult().printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetCalendarSync", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processGetSchedulerDates(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEventDate> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			//DateTimeFormatter ymdZoneFmt = DateTimeUtils.createYmdFormatter(utz);
			
			// Defines boundaries
			String start = ServletUtils.getStringParameter(request, "startDate", true);
			String end = ServletUtils.getStringParameter(request, "endDate", true);
			DateTime fromDate = DateTimeUtils.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
			DateTime toDate = DateTimeUtils.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
			
			Set<Integer> activeCalIds = getActiveFolderIds();
			List<LocalDate> dates = manager.listEventDates(activeCalIds, fromDate, toDate, utz);
			for (LocalDate date : dates) {
				items.add(new JsSchedulerEventDate(date.toString("yyyy-MM-dd")));
			}
			
			new JsonResult("dates", items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetSchedulerDates", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageEventsScheduler(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String from = ServletUtils.getStringParameter(request, "startDate", true);
				String to = ServletUtils.getStringParameter(request, "endDate", true);
				
				// Defines view boundary
				DateTime fromDate = DateTimeUtils.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
				DateTime toDate = DateTimeUtils.parseYmdHmsWithZone(to, "23:59:59", up.getTimeZone());
				
				Set<Integer> activeCalIds = getActiveFolderIds();
				List<SchedEventInstance> instances = manager.listEventInstances(activeCalIds, new DateTimeRange(fromDate, toDate), utz, false);
				for (SchedEventInstance instance : instances) {
					final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCalendarId());
					if (origin == null) continue;
					final CalendarFSFolder folder = foldersTreeCache.getFolder(instance.getCalendarId());
					if (folder == null) continue;
					
					final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					items.add(new JsSchedulerEvent(origin, folder, props, instance, up.getId(), utz, MEETING_PROVIDERS_URLS_PATTERN));
				}
				
				new JsonResult("events", items).printTo(out);
				
			}/* else if (crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsSchedulerEvent> pl = ServletUtils.getPayload(request, JsSchedulerEvent.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = DateTimeUtils.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = DateTimeUtils.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.cloneEventInstance(EventKey.buildKey(pl.data.eventId, pl.data.originalEventId), newStart, newEnd);
				
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsSchedulerEvent.Update> pl = ServletUtils.getPayload(request, JsSchedulerEvent.Update.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = DateTimeUtils.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = DateTimeUtils.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.updateEventInstance(pl.data.id, newStart, newEnd, pl.data.title, false);
				
				new JsonResult().printTo(out);
				
			}*/ else if (crud.equals(Crud.UPDATE)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				String newStartString = ServletUtils.getStringParameter(request, "newStart", null);
				String newEndString = ServletUtils.getStringParameter(request, "newEnd", null);
				String newTitle = ServletUtils.getStringParameter(request, "newTitle", null);
				UpdateEventTarget target = ServletUtils.getEnumParameter(request, "target", UpdateEventTarget.THIS_INSTANCE, UpdateEventTarget.class);
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				
				DateTime newStart = null, newEnd = null;
				if ((newStartString != null) || (newEndString != null)) {
					newStart = DateTimeUtils.parseYmdHmsWithZone(newStartString, utz);
					newEnd = DateTimeUtils.parseYmdHmsWithZone(newEndString, utz);
				}
				
				manager.updateEventInstance(target, new EventKey(eventKey), newStart, newEnd, newTitle, notify);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				UpdateEventTarget target = ServletUtils.getEnumParameter(request, "target", UpdateEventTarget.THIS_INSTANCE, UpdateEventTarget.class);
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				
				manager.deleteEventInstance(target, new EventKey(eventKey), notify);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.COPY)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				String newStartString = ServletUtils.getStringParameter(request, "newStart", null);
				//UpdateEventTarget target = ServletUtils.getEnumParameter(request, "target", UpdateEventTarget.THIS_INSTANCE, UpdateEventTarget.class);
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				
				DateTime newStart = null;
				if (newStartString != null) {
					newStart = DateTimeUtils.parseYmdHmsWithZone(newStartString, utz);
				}
				
				manager.cloneEventInstance(new EventKey(eventKey), null, newStart, null, notify);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.MOVE)) {
				boolean copy = ServletUtils.getBooleanParameter(request, "copy", false);
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				Integer calendarId = ServletUtils.getIntParameter(request, "targetCalendarId", true);
				
				if (copy) {
					boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
					manager.cloneEventInstance(new EventKey(eventKey), calendarId, null, null, notify);
				} else {
					manager.moveEventInstance(new EventKey(eventKey), calendarId);
				}
				new JsonResult().printTo(out);
				
			} else if(crud.equals("restore")) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				
				manager.restoreEventInstance(new EventKey(eventKey));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageEventsScheduler", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		JsEvent item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				
				EventInstance evt = manager.getEventInstance(eventKey);
				UserProfileId ownerId = manager.getCalendarOwner(evt.getCalendarId());
				
				Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, evt.getTags());
				Map<String, CustomField> cfields = new HashMap<>();
				for (CustomPanel cpanel : cpanels.values()) {
					for (String fieldId : cpanel.getFields()) {
						CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
						if (cfield != null) cfields.put(fieldId, cfield);
					}
				}
				
				item = new JsEvent(ownerId, evt, cpanels.values(), cfields, up.getLanguageTag(), up.getTimeZone());
				new JsonResult(item).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				EventInstance event = pl.data.toEventInstance(up.getTimeZone());
				CoreManager core = WT.getCoreManager();
				event.setOrganizer(core.getUserData().getFullEmailAddress());
				
				for (JsEvent.Attachment jsa : pl.data.attachments) {
					UploadedFile upFile = getUploadedFileOrThrow(jsa._uplId);
					EventAttachmentWithStream att = new EventAttachmentWithStream(upFile.getFile());
					att.setAttachmentId(jsa.id);
					att.setFilename(upFile.getFilename());
					att.setSize(upFile.getSize());
					att.setMediaType(upFile.getMediaType());
					event.getAttachments().add(att);
				}
				manager.addEvent(event, notify);
				
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				UpdateEventTarget target = ServletUtils.getEnumParameter(request, "target", UpdateEventTarget.THIS_INSTANCE, UpdateEventTarget.class);
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				EventInstance event = pl.data.toEventInstance(up.getTimeZone());
				
				for (JsEvent.Attachment jsa : pl.data.attachments) {
					if (!StringUtils.isBlank(jsa._uplId)) {
						UploadedFile upFile = getUploadedFileOrThrow(jsa._uplId);
						EventAttachmentWithStream att = new EventAttachmentWithStream(upFile.getFile());
						att.setAttachmentId(jsa.id);
						att.setFilename(upFile.getFilename());
						att.setSize(upFile.getSize());
						att.setMediaType(upFile.getMediaType());
						event.getAttachments().add(att);
					} else {
						EventAttachment att = new EventAttachment();
						att.setAttachmentId(jsa.id);
						att.setFilename(jsa.name);
						att.setSize(jsa.size);
						event.getAttachments().add(att);
					}
				}
				manager.updateEventInstance(target, event, true, true, true, notify);
				
				new JsonResult().printTo(out);
				
			}/* else if(crud.equals(Crud.MOVE)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				Integer calendarId = ServletUtils.getIntParameter(request, "targetCalendarId", true);
				boolean copy = ServletUtils.getBooleanParameter(request, "copy", false);
				
				manager.moveEventInstance(copy, id, calendarId);
				new JsonResult().printTo(out);
			}*/
			
		} catch(Exception ex) {
			logger.error("Error in ManageEvents", ex);
			new JsonResult(ex).printTo(out);	
		}
	}
	
	public void processDownloadEventAttachment(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			boolean inline = ServletUtils.getBooleanParameter(request, "inline", false);
			String attachmentId = ServletUtils.getStringParameter(request, "attachmentId", null);
			
			if (!StringUtils.isBlank(attachmentId)) {
				Integer eventId = ServletUtils.getIntParameter(request, "eventId", true);
				
				EventAttachmentWithBytes attch = manager.getEventAttachment(eventId, attachmentId);
				InputStream is = null;
				try {
					is = new ByteArrayInputStream(attch.getBytes());
					ServletUtils.writeFileResponse(response, inline, attch.getFilename(), null, attch.getSize(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			} else {
				String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
				
				UploadedFile uplFile = getUploadedFileOrThrow(uploadId);
				InputStream is = null;
				try {
					is = new FileInputStream(uplFile.getFile());
					ServletUtils.writeFileResponse(response, inline, uplFile.getFilename(), null, uplFile.getSize(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in DownloadEventAttachment", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		}
	}
	
	public void processGetCustomFieldsDefsData(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
			Integer eventId = ServletUtils.getIntParameter(request, "id", false);
			
			Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, tags);
			Map<String, CustomFieldValue> cvalues = (eventId != null) ? manager.getEventCustomValues(eventId) : null;
			Map<String, CustomField> cfields = new HashMap<>();
			for (CustomPanel cpanel : cpanels.values()) {
				for (String fieldId : cpanel.getFields()) {
					CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
					if (cfield != null) cfields.put(fieldId, cfield);
				}
			}
			new JsonResult(new JsCustomFieldDefsData(cpanels.values(), cfields, cvalues, up.getLanguageTag(), up.getTimeZone())).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in GetCustomFieldsDefsData", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
				
				Map<String, CustomField.Type> map = cacheSearchableCustomFieldType.shallowCopy();
				Set<Integer> activeCalIds = getActiveFolderIds();
				List<SchedEventInstance> instances = manager.listEventInstances(activeCalIds, EventQuery.createCondition(queryObj, map, utz), utz);
				for (SchedEventInstance instance : instances) {
					final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCalendarId());
					if (origin == null) continue;
					final CalendarFSFolder folder = foldersTreeCache.getFolder(instance.getCalendarId());
					if (folder == null) continue;
					
					final CalendarPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					items.add(new JsSchedulerEvent(origin, folder, props, instance, up.getId(), utz, MEETING_PROVIDERS_URLS_PATTERN));
				}
				
				new JsonResult("events", items).printTo(out);
				
			} else if (crud.equals("updateTag")) {
				StringArray keys = ServletUtils.getObjectParameter(request, "keys", StringArray.class, true);
				UpdateTagsOperation op = ServletUtils.getEnumParameter(request, "op", true, UpdateTagsOperation.class);
				StringArray tags = ServletUtils.getObjectParameter(request, "tags", StringArray.class, true);
				
				LinkedHashSet<Integer> ids = new LinkedHashSet<>();
				for (String key : keys) {
					ids.add(new EventKey(key).eventId);
				}
				manager.updateEventTags(op, ids, new HashSet<>(tags));
				
				new JsonResult().printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in ManageGridEvents", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processExportForErp(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String op = ServletUtils.getStringParameter(request, "op", true);
			if (op.equals("do")) {
				String fromDate = ServletUtils.getStringParameter(request, "fromDate", true);
				String toDate = ServletUtils.getStringParameter(request, "toDate", true);
				
				DateTimeFormatter ymd = DateTimeUtils.createYmdFormatter(up.getTimeZone());
				erpWizard = new ErpExportWizard();
				erpWizard.fromDate = ymd.parseDateTime(fromDate).withTimeAtStartOfDay();
				erpWizard.toDate = DateTimeUtils.withTimeAtEndOfDay(ymd.parseDateTime(toDate));
				
				LogEntries log = new LogEntries();
				File file = WT.createTempFile();
				
				try {
					DateTimeFormatter ymd2 = DateTimeUtils.createFormatter("yyyyMMdd", up.getTimeZone());
					DateTimeFormatter ymdhms = DateTimeUtils.createFormatter("yyyy-MM-dd HH:mm:ss", up.getTimeZone());
					
					try (FileOutputStream fos = new FileOutputStream(file)) {
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Started on {0}", ymdhms.print(new DateTime())));
						manager.exportEvents(log, erpWizard.fromDate, erpWizard.toDate, fos);
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Ended on {0}", ymdhms.print(new DateTime())));
						erpWizard.file = file;
						erpWizard.filename = MessageFormat.format(ERP_EXPORT_FILENAME, up.getDomainId(), ymd2.print(erpWizard.fromDate), ymd2.print(erpWizard.fromDate), "csv");
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "File ready: {0}", erpWizard.filename));
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Operation completed succesfully"));
						new JsonResult(new JsWizardData(log.print())).printTo(out);
					}
				} catch(Throwable t) {
					logger.error("Error generating export", t);
					file.delete();
					new JsonResult(new JsWizardData(log.print())).setSuccess(false).printTo(out);
				}	
			}
			
		} catch(Exception ex) {
			logger.error("Error in ExportForErp", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processExportForErp(HttpServletRequest request, HttpServletResponse response) {
		try {
			try(FileInputStream fis = new FileInputStream(erpWizard.file)) {
				ServletUtils.setFileStreamHeaders(response, "application/octet-stream", DispositionType.ATTACHMENT, erpWizard.filename);
				ServletUtils.setContentLengthHeader(response, erpWizard.file.length());
				IOUtils.copy(fis, response.getOutputStream());
			}
			
		} catch(Exception ex) {
			logger.error("Error in ExportForErp", ex);
		} finally {
			erpWizard = null;
		}
	}
	
	public void processGeneratePlanningView(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String eventStartDate = ServletUtils.getStringParameter(request, "startDate", true);
			String eventEndDate = ServletUtils.getStringParameter(request, "endDate", true);
			Boolean eventAllDay = ServletUtils.getBooleanParameter(request, "allDay", false);
			String timezone = ServletUtils.getStringParameter(request, "timezone", true);
			StringArray attendeesAddresses = ServletUtils.getObjectParameter(request, "attendees", StringArray.class, true);
			String organizer = ServletUtils.getStringParameter(request, "organizer", false);
			int resolution = ServletUtils.getIntParameter(request, "resolution", 30);
			boolean boundToWorkingHours = ServletUtils.getBooleanParameter(request, "boundToWorkingHours", true);
			
			// Parses string parameters
			DateTimeZone eventTz = DateTimeZone.forID(timezone);
			DateTimeFormatter ymdHmFmt = DateTimeUtils.createFormatter("yyyy-MM-dd HH:mm", eventTz);
			DateTime eventStartDt = DateTimeUtils.parseDateTime(ymdHmFmt, eventStartDate);
			DateTime eventEndDt = DateTimeUtils.parseDateTime(ymdHmFmt, eventEndDate);
			
			CalendarUtils.EventBoundary bounds = CalendarUtils.toEventBoundaryForWrite(eventAllDay, eventStartDt, eventEndDt, eventTz);
			DateTime viewFrom = bounds.start.withTimeAtStartOfDay();
			DateTime viewEnd = bounds.end.withTimeAtStartOfDay().plusDays(7-1); // Display 7days in total
			//DateTime viewEnd = viewFrom.plusDays(7-1); // Display 7days in total
			TimeRange workdayTimeRange = null;
			if (boundToWorkingHours) workdayTimeRange = TimeRange.builder().withStart(us.getWorkdayStart()).withEnd(us.getWorkdayEnd()).build();
			
			Map<String, DateTime> spans = manager.generateTimeSpans(viewFrom.toLocalDate(), viewEnd.toLocalDate(), workdayTimeRange, true, eventTz, resolution);
			
			ArrayList<MapItem> items = new ArrayList<>();
			ArrayList<GridColumnMeta> colsInfo = new ArrayList<>();
			colsInfo.add(new GridColumnMeta("ATTENDEE"));
			
			int spanIndex = -1;
			for (Map.Entry<String, DateTime> entry : spans.entrySet()) {
				spanIndex++;
				//final LocalDateTime local = entry.getValue().toLocalDateTime();
				final GridColumnMeta col = new GridColumnMeta();
				col.put("date", entry.getKey());
				col.put("sidx", spanIndex);
				//col.put("ov", (local.compareTo(eventStartDt.toLocalDateTime()) >= 0) && (local.compareTo(eventEndDt.toLocalDateTime()) < 0) ? 1 : 0);
				colsInfo.add(col);
			}
			
			// Prepares guessed attendees
			String organizerAddress = null;
			Map<String, UserProfileId> recipientProfiles = new LinkedHashMap<>();
			if (!StringUtils.isBlank(organizer)) { // Add organizer, if specified!
				final UserProfileId pid = UserProfileId.parseQuielty(organizer);
				if (pid != null) {
					final InternetAddress ia = WT.getProfilePersonalAddress(pid);
					if (ia != null) {
						organizerAddress = ia.getAddress();
						recipientProfiles.put(ia.getAddress(), pid);
					}
				}
			}
			for (String attendeeAddress : attendeesAddresses) { // Add recipient from attendees
				final InternetAddress ia = InternetAddressUtils.toInternetAddress(attendeeAddress);
				if (ia != null) {
					final UserProfileId pid = WT.guessProfileIdByPersonalAddress(ia.getAddress());
					if (!recipientProfiles.containsKey(ia.getAddress())) recipientProfiles.put(ia.getAddress(), pid);
				}
			}
			
			final String UNKNOWN = "0";
			final String FREE = "1";
			final String BUSY = "2";
			final String OFFSITE = "3";
			final String ABSENT = "4";
			DateTimeFormatter hmf = DateTimeUtils.createHmFormatter();
			for (Map.Entry<String, UserProfileId> entry : recipientProfiles.entrySet()) {
				final String address = entry.getKey();
				final UserProfileId pid = entry.getValue();
				
				String name = null;
				LocalTime workdayStart = null;
				LocalTime workdayEnd = null;
				if (pid != null) {
					name = WT.getProfileData(pid).getDisplayName();
					CalendarUserSettings rcus = new CalendarUserSettings(SERVICE_ID, entry.getValue());
					workdayStart = rcus.getWorkdayStart();
					workdayEnd = rcus.getWorkdayEnd();
				}
				
				final MapItem item = new MapItem();
				if (organizerAddress != null && organizerAddress.equals(address)) {
					item.put("attendee", name);
					item.put("attendeeAddress", address);
					item.put("organizer", true);
				} else {
					item.put("attendee", name);
					item.put("attendeeAddress", address);
				}
				item.put("workdayStart", DateTimeUtils.print(hmf, workdayStart));
				item.put("workdayEnd", DateTimeUtils.print(hmf, workdayEnd));
				
				StringBuilder availability = new StringBuilder();
				if (pid != null) {
					final Map<String, DateTime> busySpans = manager.calculateTransparencyTimeSpans(pid, true, viewFrom.toLocalDate(), viewEnd.toLocalDate(), workdayTimeRange, eventTz, resolution);
					Iterator it = spans.keySet().iterator();
					while (it.hasNext()) {
						final String key = (String)it.next();
						final String spanStatus = busySpans.containsKey(key) ? BUSY : FREE;
						availability.append(spanStatus);
						if (it.hasNext()) availability.append("|");
					}
				} else {
					availability.append(StringUtils.repeat(UNKNOWN, "|", spans.size()));
				}
				item.put("spanAvailability", availability.toString());
				items.add(item);
			}
			
			GridMetadata meta = new GridMetadata(true);
			meta.setColumnsInfo(colsInfo);
			new JsonResult(items, meta, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GeneratePlanningView", ex);
			new JsonResult(ex).printTo(out);	
		}
	}
	
	public void processImportEventsFromICal(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
			String op = ServletUtils.getStringParameter(request, "op", true);
			
			UploadedFile upl = getUploadedFile(uploadId);
			if(upl == null) throw new WTException("Uploaded file not found [{0}]", uploadId);
			File file = new File(WT.getTempFolder(), upl.getUploadId());
			
			EventICalFileReader rea = new EventICalFileReader(up.getTimeZone());
			
			if(op.equals("do")) {
				Integer calendarId = ServletUtils.getIntParameter(request, "calendarId", true);
				String mode = ServletUtils.getStringParameter(request, "importMode", true);
				
				LogEntries log = manager.importEvents(calendarId, rea, file, mode);
				removeUploadedFile(uploadId);
				new JsonResult(new JsWizardData(log.print())).printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ImportContactsFromICal", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processPrintScheduler(HttpServletRequest request, HttpServletResponse response) {
		ByteArrayOutputStream baos = null;
		UserProfile up = getEnv().getProfile();
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", "print");
			String view = ServletUtils.getStringParameter(request, "view", "w5");
			String from = ServletUtils.getStringParameter(request, "startDate", true);
			
			DateTime startDate = DateTimeUtils.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
			
			ReportConfig.Builder builder = reportConfigBuilder();
			DateTime fromDate = null, toDate = null;
			AbstractAgenda rpt = null;
			if(view.equals("d")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(1).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), 1);
				
			} else if(view.equals("w5")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(5).withTimeAtStartOfDay();
				rpt = new RptAgendaWeek5(builder.build());
				
			} else if(view.equals("w")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(7).withTimeAtStartOfDay();
				rpt = new RptAgendaWeek7(builder.build());
				
			} else if(view.equals("dw")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(14).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), 14);
				
			} else if(view.equals("m")) {
				if(startDate.getDayOfMonth() == 1) {
					fromDate = startDate.withTimeAtStartOfDay();
				} else {
					fromDate = startDate.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();
				}
				int days = fromDate.dayOfMonth().getMaximumValue();
				toDate = fromDate.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), days);
				
			} else {
				throw new WTException("View not supported [{0}]", view);
			}
			
			Set<Integer> activeCalIds = getActiveFolderIds();
			Map<Integer, Calendar> calendars = foldersTreeCache.getFolders().stream()
				.filter(folder -> activeCalIds.contains(folder.getFolderId()))
				.collect(Collectors.toMap(folder -> folder.getFolderId(), folder -> folder.getCalendar()));
			
			List<SchedEventInstance> instances = manager.listEventInstances(activeCalIds, new DateTimeRange(fromDate, toDate), up.getTimeZone(), true);
			rpt.setDataSource(manager, fromDate, toDate, up.getTimeZone(), calendars, instances);
			
			baos = new ByteArrayOutputStream();
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, baos);
			ServletUtils.setContentDispositionHeader(response, "inline", filename + ".pdf");
			ServletUtils.writeContent(response, baos, "application/pdf");
			
		} catch(Exception ex) {
			logger.error("Error in PrintScheduler", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	public void processPrintEventsDetail(HttpServletRequest request, HttpServletResponse response) {
		ArrayList<RBEventDetail> items = new ArrayList<>();
		ByteArrayOutputStream baos = null;
		CoreManager core = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", "print");
			StringArray keys = ServletUtils.getObjectParameter(request, "keys", StringArray.class, true);
			
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(up.getLocale());
			RRuleStringify rrs = new RRuleStringify(strings, up.getTimeZone());
			
			EventInstance event = null;
			Calendar calendar = null;
			for(String key : keys) {
				event = manager.getEventInstance(key);
				calendar = manager.getCalendar(event.getCalendarId());
				items.add(new RBEventDetail(core, rrs, calendar, event));
			}
			
			ReportConfig.Builder builder = reportConfigBuilder();
			RptEventsDetail rpt = new RptEventsDetail(builder.build());
			rpt.setDataSource(items);
			
			baos = new ByteArrayOutputStream();
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, baos);
			ServletUtils.setContentDispositionHeader(response, "inline", filename + ".pdf");
			ServletUtils.writeContent(response, baos, "application/pdf");
			
		} catch(Exception ex) {
			logger.error("Error in PrintContacts", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	private DateTimeRange buildSearchRange(DateTime now, DateTimeZone timezone) {
		return new DateTimeRange(
			now.withZone(timezone).minusMonths(6).withTimeAtStartOfDay(),
			now.withZone(timezone).plusMonths(6).withTimeAtStartOfDay());
	}
	
	public void processPortletEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsPletEvents> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", null);
			if (query == null) {
				final CalendarFSOrigin origin = foldersTreeCache.getOrigin(up.getId());
				final Set<Integer> ids = manager.listCalendarIds();
				for (SchedEventInstance instance : manager.listUpcomingEventInstances(ids, DateTimeUtils.now(), utz)) {
					final CalendarFSFolder folder = foldersTreeCache.getFolder(instance.getCalendarId());
					if (folder == null) continue;
					
					items.add(new JsPletEvents(origin, folder, instance, utz, MEETING_PROVIDERS_URLS_PATTERN));
				}
				
			} else {
				final Set<Integer> ids = foldersTreeCache.getFolderIDs();
				final String pattern = LangUtils.patternizeWords(query);
				List<SchedEventInstance> instances = manager.listEventInstances(ids, buildSearchRange(DateTimeUtils.now(), utz), EventQuery.createCondition(pattern), utz, true);
				for (SchedEventInstance instance : instances) {
					final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCalendarId());
					if (origin == null) continue;
					final CalendarFSFolder folder = foldersTreeCache.getFolder(instance.getCalendarId());
					if (folder == null) continue;
					
					items.add(new JsPletEvents(origin, folder, instance, utz, MEETING_PROVIDERS_URLS_PATTERN));
				}
			}
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in PortletEvents", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	private ReportConfig.Builder reportConfigBuilder() {
		UserProfile.Data ud = getEnv().getProfile().getData();
		CoreUserSettings cus = getEnv().getCoreUserSettings();
		return new ReportConfig.Builder()
			.useLocale(ud.getLocale())
			.useTimeZone(ud.getTimeZone().toTimeZone())
			.dateFormatShort(cus.getShortDateFormat())
			.dateFormatLong(cus.getLongDateFormat())
			.timeFormatShort(cus.getShortTimeFormat())
			.timeFormatLong(cus.getLongTimeFormat())
			.generatedBy(WT.getPlatformName() + " " + lookupResource(CalendarLocale.SERVICE_NAME))
			.printedBy(ud.getDisplayName());
	}
	
	private OUser guessUserByAttendee(CoreManager core, String recipient) {
		Connection con = null;
		
		try {
			InternetAddress ia = InternetAddressUtils.toInternetAddress(recipient);
			if (ia == null) return null;
			List<UserProfileId> pids = core.listUserIdsByEmail(ia.getAddress());
			if (pids.isEmpty()) return null;
			UserProfileId pid = pids.get(0);
			
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			return udao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
		
		} catch(WTRuntimeException ex) {
			return null;
		} catch(Exception ex) {
			logger.error("Error guessing user from attendee", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private LinkedHashSet<Integer> getActiveFolderIds() {
		LinkedHashSet<Integer> ids = new LinkedHashSet<>();
		for (CalendarFSOrigin origin : getActiveOrigins()) {
			for (CalendarFSFolder folder: foldersTreeCache.getFoldersByOrigin(origin)) {
				if (inactiveFolders.contains(folder.getFolderId())) continue;
				ids.add(folder.getFolderId());
			}
		}
		return ids;
	}
	
	private List<CalendarFSOrigin> getActiveOrigins() {
		return foldersTreeCache.getOrigins().stream()
			.filter(origin -> !inactiveOrigins.contains(toInactiveOriginKey(origin)))
			.collect(Collectors.toList());
	}
	
	private String toInactiveOriginKey(CalendarFSOrigin origin) {
		return origin.isResource() ? CalendarNodeId.GROUPER_RESOURCES_ORIGIN : origin.getProfileId().toString();
	}
	
	private void toggleActiveOrigin(String originKey, boolean active) {
		toggleActiveOrigins(new String[]{originKey}, active);
	}
	
	private void toggleActiveOrigins(String[] originKeys, boolean active) {	
		try {
			locks.tryLock("inactiveOrigins", 60, TimeUnit.SECONDS);
			for (String originId : originKeys) {
				if (active) {
					inactiveOrigins.remove(originId);
				} else {
					inactiveOrigins.add(originId);
				}
			}
			us.setInactiveCalendarOrigins(inactiveOrigins);
			
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("inactiveOrigins");
		}
	}
	
	private void toggleActiveFolder(Integer folderId, boolean active) {
		toggleActiveFolders(new Integer[]{folderId}, active);
	}
	
	private void toggleActiveFolders(Integer[] folderIds, boolean active) {
		try {
			locks.tryLock("inactiveFolders", 60, TimeUnit.SECONDS);
			for (int folderId : folderIds) {
				if (active) {
					inactiveFolders.remove(folderId);
				} else {
					inactiveFolders.add(folderId);
				}
			}
			us.setInactiveCalendarFolders(inactiveFolders);
			
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("inactiveFolders");
		}
	}
	
	private void updateCalendarFolderVisibility(int calendarId, Boolean hidden) {
		try {
			locks.tryLock("folderVisibility-"+calendarId, 60, TimeUnit.SECONDS);
			CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
			pset.setHidden(hidden);
			manager.updateCalendarCustomProps(calendarId, pset);
			
			final CalendarFSFolder folder = foldersTreeCache.getFolder(calendarId);
			if (!(folder instanceof MyCalendarFSFolder)) {
				foldersPropsCache.put(calendarId, Optional.of(pset));
			}
		
		} catch (WTException ex) {
			logger.error("Error saving custom calendar props", ex);
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("folderVisibility-"+calendarId);
		}
	}
	
	private void updateCalendarFolderColor(int calendarId, String color) throws WTException {
		final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(calendarId);
		if (origin instanceof MyCalendarFSOrigin) {
			Calendar cal = manager.getCalendar(calendarId);
			cal.setColor(color);
			manager.updateCalendar(cal);
			foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
			
		} else if (origin instanceof CalendarFSOrigin) {
			CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
			pset.setColor(color);
			manager.updateCalendarCustomProps(calendarId, pset);
			foldersPropsCache.put(calendarId, Optional.of(pset));
		}
	}
	
	private void updateCalendarFolderSync(int calendarId, Calendar.Sync sync) throws WTException {
		final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(calendarId);
		if (origin instanceof MyCalendarFSOrigin) {
			Calendar cal = manager.getCalendar(calendarId);
			cal.setSync(sync);
			manager.updateCalendar(cal);
			foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
			
		} else if (origin instanceof CalendarFSOrigin) {
			CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
			pset.setSync(sync);
			manager.updateCalendarCustomProps(calendarId, pset);
			foldersPropsCache.put(calendarId, Optional.of(pset));
		}
	}
	
	private class SearchableCustomFieldTypeCache extends AbstractPassiveExpiringBulkMap<String, CustomField.Type> {
		
		public SearchableCustomFieldTypeCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Map<String, CustomField.Type> internalGetMap() {
			try {
				CoreManager coreMgr = WT.getCoreManager();
				return coreMgr.listCustomFieldTypesById(SERVICE_ID, BitFlag.of(CoreManager.CustomFieldListOptions.SEARCHABLE));
				
			} catch(Exception ex) {
				logger.error("[SearchableCustomFieldTypeCache] Unable to build cache", ex);
				throw new UnsupportedOperationException();
			}
		}
	}
	
	private class SyncRemoteCalendarAA extends BaseServiceAsyncAction {
		private final int calendarId;
		private final String calendarName;
		private final boolean full;
		
		public SyncRemoteCalendarAA(int calendarId, String calendarName, boolean full) {
			super();
			setName(this.getClass().getSimpleName());
			this.calendarId = calendarId;
			this.calendarName = calendarName;
			this.full = full;
		}

		@Override
		public void executeAction() {
			getWts().notify(new RemoteSyncResult(true)
				.setCalendarId(calendarId)
				.setCalendarName(calendarName)
				.setSuccess(true)
			);
			try {
				manager.syncRemoteCalendar(calendarId, full);
				this.completed();
				getWts().notify(new RemoteSyncResult(false)
					.setCalendarId(calendarId)
					.setCalendarName(calendarName)
					.setSuccess(true)
				);
				
			} catch(WTException ex) {
				logger.error("Remote sync failed", ex);
				getWts().notify(new RemoteSyncResult(false)
					.setCalendarId(calendarId)
					.setCalendarName(calendarName)
					.setThrowable(ex, true)
				);
			} finally {
				synchronized(syncRemoteCalendarAAs) {
					syncRemoteCalendarAAs.remove(calendarId);
				}
			}
		}
	}
	
	private static class CalendarProbeException extends WTException {
		public CalendarProbeException(String message, Object... arguments) {
			super(message, arguments);
		}
	}
	
	private static class ErpExportWizard {
		public DateTime fromDate;
		public DateTime toDate;
		public File file;
		public String filename;
	}
	
	private class FoldersPropsCacheLoader implements CacheLoader<Integer, Optional<CalendarPropSet>> {
		@Override
		public Optional<CalendarPropSet> load(Integer k) throws Exception {
			try {
				logger.trace("[FoldersPropsCache] Loading... [{}]", k);
				final CalendarFSOrigin origin = foldersTreeCache.getOriginByFolder(k);
				if (origin == null) return Optional.empty(); // Disable lookup for unknown folder IDs
				if (origin instanceof MyCalendarFSOrigin) return Optional.empty(); // Disable lookup for personal folder IDs
				return Optional.ofNullable(manager.getCalendarCustomProps(k));
				
			} catch (Exception ex) {
				logger.error("[FoldersPropsCache] Unable to load [[{}]", k);
				return null;
			}
		}
	}
	
	private class FoldersTreeCache extends AbstractFolderTreeCache<Integer, CalendarFSOrigin, CalendarFSFolder, Object> {
		
		@Override
		protected void internalBuildCache(Target options) {
			UserProfileId pid = getEnv().getProfile().getId();
				
			if (Target.ALL.equals(options) || Target.ORIGINS.equals(options)) {
				try {
					this.internalClear(Target.ORIGINS);
					this.origins.put(pid, new MyCalendarFSOrigin(pid));
					for (CalendarFSOrigin origin : manager.listIncomingCalendarOrigins().values()) {
						this.origins.put(origin.getProfileId(), origin);
					}
					
				} catch (WTException ex) {
					logger.error("[FoldersTreeCache] Error updating Origins", ex);
				}
			}	
			if (Target.ALL.equals(options) || Target.FOLDERS.equals(options)) {
				try {
					this.internalClear(Target.FOLDERS);
					for (CalendarFSOrigin origin : this.origins.values()) {
						if (origin instanceof MyCalendarFSOrigin) {
							for (Calendar calendar : manager.listCalendars().values()) {
								final MyCalendarFSFolder folder = new MyCalendarFSFolder(calendar.getCalendarId(), calendar);
								this.folders.put(folder.getFolderId(), folder);
								this.foldersByOrigin.put(origin.getProfileId(), folder);
								this.originsByFolder.put(folder.getFolderId(), origin);
							}
						} else if (origin instanceof CalendarFSOrigin) {
							for (CalendarFSFolder folder : manager.listIncomingCalendarFolders(origin.getProfileId()).values()) {
								// Make sure to track only folders with at least READ premission: 
								// it is ugly having in UI empty folder nodes for just manage update/delete/sharing operations.
								if (!folder.getPermissions().getFolderPermissions().has(FolderShare.FolderRight.READ)) continue;
								
								this.folders.put(folder.getFolderId(), folder);
								this.foldersByOrigin.put(origin.getProfileId(), folder);
								this.originsByFolder.put(folder.getFolderId(), origin);
							}
						}
					}
					
				} catch (WTException ex) {
					logger.error("[FoldersTreeCache] Error updating Folders", ex);
				}
			}
		}
	}
}
