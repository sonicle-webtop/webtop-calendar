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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
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
import com.sonicle.webtop.calendar.bol.js.JsFolderNode;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.calendar.bol.js.JsPletEvents;
import com.sonicle.webtop.calendar.bol.js.JsSharing;
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
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventAttachmentWithStream;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.msg.RemoteSyncResult;
import com.sonicle.webtop.calendar.rpt.AbstractAgenda;
import com.sonicle.webtop.calendar.rpt.RptAgendaSummary;
import com.sonicle.webtop.calendar.rpt.RptEventsDetail;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek5;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek7;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsWizardData;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
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
import java.util.stream.Collectors;
import javax.mail.internet.InternetAddress;
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
	
	private final LinkedHashMap<String, ShareRootCalendar> roots = new LinkedHashMap<>();
	private final LinkedHashMap<Integer, ShareFolderCalendar> folders = new LinkedHashMap<>();
	private final HashMap<Integer, CalendarPropSet> folderProps = new HashMap<>();
	private final HashMap<String, ArrayList<ShareFolderCalendar>> foldersByRoot = new HashMap<>();
	private final HashMap<Integer, ShareRootCalendar> rootByFolder = new HashMap<>();
	private final AsyncActionCollection<Integer, SyncRemoteCalendarAA> syncRemoteCalendarAAs = new AsyncActionCollection<>();
	private StringSet inactiveRoots = null;
	private IntegerSet inactiveFolders = null;
	private ErpExportWizard erpWizard = null;

	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (CalendarManager)WT.getServiceManager(SERVICE_ID, false, up.getId());
		ss = new CalendarServiceSettings(SERVICE_ID, up.getDomainId());
		us = new CalendarUserSettings(SERVICE_ID, up.getId());
		initFolders();
	}
	
	@Override
	public void cleanup() throws Exception {
		synchronized(syncRemoteCalendarAAs) {
			syncRemoteCalendarAAs.clear();
		}
		inactiveFolders.clear();
		inactiveFolders = null;
		inactiveRoots.clear();
		inactiveRoots = null;
		rootByFolder.clear();
		foldersByRoot.clear();
		folderProps.clear();
		folders.clear();
		roots.clear();
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
		co.put("workdayStart", hmf.print(us.getWorkdayStart()));
		co.put("workdayEnd", hmf.print(us.getWorkdayEnd()));
		return co;
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
		synchronized(roots) {
			updateRootFoldersCache();
			updateFoldersCache();
			
			// HANDLE TRANSITION: cleanup code when process is completed!
			StringSet checkedRoots = us.getCheckedCalendarRoots();
			if (checkedRoots != null) { // Migration code... (remove after migration)
				List<String> toInactive = roots.keySet().stream()
						.filter(shareId -> !checkedRoots.contains(shareId))
						.collect(Collectors.toList());
				inactiveRoots = new StringSet(toInactive);
				us.setInactiveCalendarRoots(inactiveRoots);
				us.clearCheckedCalendarRoots();
				
			} else { // New code... (keep after migrarion)
				inactiveRoots = us.getInactiveCalendarRoots();
				// Clean-up orphans
				if (inactiveRoots.removeIf(shareId -> !roots.containsKey(shareId))) {
					us.setInactiveCalendarRoots(inactiveRoots);
				}
			}
			
			IntegerSet checkedFolders = us.getCheckedCalendarFolders();
			if (checkedFolders != null) { // Migration code... (remove after migration)
				List<Integer> toInactive = folders.keySet().stream()
						.filter(calendarId -> !checkedFolders.contains(calendarId))
						.collect(Collectors.toList());
				inactiveFolders = new IntegerSet(toInactive);
				us.setInactiveCalendarFolders(inactiveFolders);
				us.clearCheckedCalendarFolders();
				
			} else { // New code... (keep after migrarion)
				inactiveFolders = us.getInactiveCalendarFolders();
				// Clean-up orphans
				if (inactiveFolders.removeIf(calendarId -> !folders.containsKey(calendarId))) {
					us.setInactiveCalendarFolders(inactiveFolders);
				}
			}
		}
	}
	
	private void updateRootFoldersCache() throws WTException {
		UserProfileId pid = getEnv().getProfile().getId();
		synchronized(roots) {
			roots.clear();
			roots.put(MyShareRootCalendar.SHARE_ID, new MyShareRootCalendar(pid));
			for (ShareRootCalendar root : manager.listIncomingCalendarRoots()) {
				roots.put(root.getShareId(), root);
			}
		}
	}
	
	private void updateFoldersCache() throws WTException {
		synchronized(roots) {
			foldersByRoot.clear();
			folders.clear();
			rootByFolder.clear();
			for (ShareRootCalendar root : roots.values()) {
				foldersByRoot.put(root.getShareId(), new ArrayList<ShareFolderCalendar>());
				if (root instanceof MyShareRootCalendar) {
					for (Calendar cal : manager.listCalendars().values()) {
						final MyShareFolderCalendar fold = new MyShareFolderCalendar(root.getShareId(), cal);
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(cal.getCalendarId(), fold);
						rootByFolder.put(cal.getCalendarId(), root);
					}
				} else {
					for (ShareFolderCalendar fold : manager.listIncomingCalendarFolders(root.getShareId()).values()) {
						final int calId = fold.getCalendar().getCalendarId();
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(calId, fold);
						folderProps.put(calId, manager.getCalendarCustomProps(calId));
						rootByFolder.put(calId, root);
					}
				}
			}
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
				
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				boolean chooser = ServletUtils.getBooleanParameter(request, "chooser", false);
				
				if (node.equals("root")) { // Node: root -> list roots
					for (ShareRootCalendar root : roots.values()) {
						children.add(createRootNode(chooser, root));
					}
				} else { // Node: folder -> list folders (calendars)
					boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", false);
					ShareRootCalendar root = roots.get(node);
					
					if (root instanceof MyShareRootCalendar) {
						for (Calendar cal : manager.listCalendars().values()) {
							MyShareFolderCalendar folder = new MyShareFolderCalendar(node, cal);
							if (writableOnly && !folder.getElementsPerms().implies("CREATE")) continue;
							
							children.add(createFolderNode(chooser, folder, null, root.getPerms()));
						}
					} else {
						if (foldersByRoot.containsKey(root.getShareId())) {
							for (ShareFolderCalendar folder : foldersByRoot.get(root.getShareId())) {
								if (writableOnly && !folder.getElementsPerms().implies("CREATE")) continue;
								
								final ExtTreeNode etn = createFolderNode(chooser, folder, folderProps.get(folder.getCalendar().getCalendarId()), root.getPerms());
								if (etn != null) children.add(etn);
							}
						}
					}
				}
				new JsonResult("children", children).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					if (node._type.equals(JsFolderNode.TYPE_ROOT)) {
						toggleActiveRoot(node.id, node._active);
						
					} else if (node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						toggleActiveFolder(Integer.valueOf(cid.getToken(1)), node._active);
					}
				}
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					if (node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						manager.deleteCalendar(Integer.valueOf(cid.getToken(1)));
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCalendarsTree", ex);
		}
	}
	
	public void processLookupCalendarRoots(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", true);
			
			synchronized(roots) {
				for(ShareRootCalendar root : roots.values()) {
					if(root instanceof MyShareRootCalendar) {
						UserProfile up = getEnv().getProfile();
						items.add(new JsSimple(up.getStringId(), up.getDisplayName()));
					} else {
						//TODO: se writableOnly verificare che il gruppo condiviso sia scrivibile
						items.add(new JsSimple(root.getOwnerProfileId().toString(), root.getDescription()));
					}
				}
			}
			
			new JsonResult("roots", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCalendarRoots", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCalendarFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCalendarLkp> items = new ArrayList<>();
		
		try {
			synchronized(roots) {
				for (ShareRootCalendar root : roots.values()) {
					if (foldersByRoot.containsKey(root.getShareId())) {
						for (ShareFolderCalendar fold : foldersByRoot.get(root.getShareId())) {
							items.add(new JsCalendarLkp(fold, folderProps.get(fold.getCalendar().getCalendarId())));
						}
					}
				}
			}
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCalendarFolders", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Sharing sharing = manager.getSharing(id);
				String description = buildSharingPath(sharing);
				new JsonResult(new JsSharing(sharing, description)).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Sharing> pl = ServletUtils.getPayload(request, Sharing.class);
				
				manager.updateSharing(pl.data);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
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
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				manager.updateCalendar(JsCalendar.createCalendar(pl.data));
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				manager.deleteCalendar(pl.data.calendarId);
				updateFoldersCache();
				toggleActiveFolder(pl.data.calendarId, true); // forgets it by simply activating it
				new JsonResult().printTo(out);
				
			} else if (crud.equals("readLinks")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				
				ShareFolderCalendar fold = folders.get(id);
				if (fold == null) throw new WTException("Calendar not found [{}]", id);
				Map<String, String> links = manager.getCalendarLinks(id);
				new JsonResult(new JsCalendarLinks(id, fold.getCalendar().getName(), links)).printTo(out);
				
			} else if (crud.equals("sync")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				boolean full = ServletUtils.getBooleanParameter(request, "full", false);
				
				item = manager.getCalendar(id);
				if (item == null) throw new WTException("Calendar not found [{0}]", id);
				runSyncRemoteCalendar(id, item.getName(), full);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageCalendars", t);
			new JsonResult(false, "Error").printTo(out);
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
				updateFoldersCache();
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
			if(crud.equals(Crud.READ)) {
				String rootId = ServletUtils.getStringParameter(request, "rootId", true);
				if (rootId.equals(MyShareRootCalendar.SHARE_ID)) throw new WTException();
				
				ArrayList<JsSimple> items = new ArrayList<>();
				synchronized(roots) {
					for(ShareFolderCalendar folder : foldersByRoot.get(rootId)) {
						CalendarPropSet pset = folderProps.get(folder.getCalendar().getCalendarId());
						if ((pset != null) && pset.getHiddenOrDefault(false)) {
							items.add(new JsSimple(folder.getCalendar().getCalendarId(), folder.getCalendar().getName()));
						}
					}
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Integer categoryId = ServletUtils.getIntParameter(request, "calendarId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCalendarFolderVisibility(categoryId, hidden);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> pids = new HashSet<>();
				synchronized(roots) {
					for(String id : ids) {
						int categoryId = Integer.valueOf(id);
						ShareFolderCalendar fold = folders.get(categoryId);
						if (fold != null) {
							updateCalendarFolderVisibility(categoryId, null);
							pids.add(fold.getCalendar().getProfileId().toString());
						}
					}
				}
				new JsonResult(pids).printTo(out);
			}
			
		} catch(Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetCalendarColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);

			updateCalendarFolderColor(id, color);

			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetCalendarSync(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String sync = ServletUtils.getStringParameter(request, "sync", null);
			
			updateCalendarFolderSync(id, EnumUtils.forSerializedName(sync, Calendar.Sync.class));
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in SetCategorySync", ex);
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
			
			List<Integer> activeCalIds = getActiveFolderIds();
			List<LocalDate> dates = manager.listEventDates(activeCalIds, fromDate, toDate, utz);
			for (LocalDate date : dates) {
				items.add(new JsSchedulerEventDate(date.toString("yyyy-MM-dd")));
			}
			/*
			List<DateTime> dates = manager.listEventDates(activeCalIds, fromDate, toDate, utz);
			for (DateTime date : dates) {
				items.add(new JsSchedulerEventDate(ymdZoneFmt.print(date)));
			}
			*/
			
			new JsonResult("dates", items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in GetSchedulerDates", ex);
			new JsonResult(false, "Error").printTo(out);
			
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
				
				List<Integer> activeCalIds = getActiveFolderIds();
				List<SchedEventInstance> instances = manager.listEventInstances(activeCalIds, fromDate, toDate, utz);
				for (SchedEventInstance instance : instances) {
					final ShareRootCalendar root = rootByFolder.get(instance.getCalendarId());
					if (root == null) continue;
					final ShareFolderCalendar fold = folders.get(instance.getCalendarId());
					if (fold == null) continue;
					CalendarPropSet pset = folderProps.get(instance.getCalendarId());
					
					items.add(new JsSchedulerEvent(root, fold, pset, instance, up.getId(), utz));
				}
				
				
				/* way1
				List<Integer> visibleCategoryIds = getVisibleFolderIds(true);
				List<FolderEvents> foEventsObjs = manager.listFolderEvents(visibleCategoryIds, fromDate, toDate);
				for (FolderEvents foEventsObj : foEventsObjs) {
					final CalendarRoot root = rootByFolder.get(foEventsObj.folder.getCalendarId());
					if (root == null) continue;
					final CalendarFolder fold = folders.get(foEventsObj.folder.getCalendarId());
					if (fold == null) continue;
					
					for (SchedEvent se : foEventsObj.events) {
						for (SchedEventInstance sei : manager.toInstances(se, fromDate, toDate, utz)) {
							items.add(new JsSchedulerEvent(root, fold, sei, up.getId(), utz));
						}
					}
				}
				*/
				
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
		JsEvent item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				
				EventInstance evt = manager.getEventInstance(eventKey);
				UserProfileId ownerId = manager.getCalendarOwner(evt.getCalendarId());
				item = new JsEvent(evt, ownerId.toString());
				new JsonResult(item).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				boolean notify = ServletUtils.getBooleanParameter(request, "notify", false);
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				EventInstance event = JsEvent.buildEventInstance(pl.data);
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
				
				EventInstance event = JsEvent.buildEventInstance(pl.data);
				
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
				manager.updateEventInstance(target, event, notify);
				
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
			new JsonResult(false, "Error").printTo(out);	
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
	
	public void processManageGridEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
				
				List<Integer> activeCalIds = getActiveFolderIds();
				List<SchedEventInstance> instances = manager.listEventInstances(activeCalIds, EventQuery.toCondition(queryObj, utz), utz);
				for (SchedEventInstance instance : instances) {
					final ShareRootCalendar root = rootByFolder.get(instance.getCalendarId());
					if (root == null) continue;
					final ShareFolderCalendar fold = folders.get(instance.getCalendarId());
					if (fold == null) continue;
					CalendarPropSet pset = folderProps.get(instance.getCalendarId());
					
					items.add(new JsSchedulerEvent(root, fold, pset, instance, up.getId(), utz));
				}
				
				/*
				String query = ServletUtils.getStringParameter(request, "query", true);
				
				final List<Integer> activeCalIds = getActiveFolderIds();
				final List<FolderEventInstances> foInstancesObjs = manager.listFolderEventInstances(activeCalIds, "%"+query+"%", utz);
				for (FolderEventInstances foInstancesObj : foInstancesObjs) {
					final ShareRootCalendar root = rootByFolder.get(foInstancesObj.folder.getCalendarId());
					if (root == null) continue;
					final ShareFolderCalendar fold = folders.get(foInstancesObj.folder.getCalendarId());
					if (fold == null) continue;
					CalendarPropSet pset = folderProps.get(foInstancesObj.folder.getCalendarId());
					
					for (SchedEventInstance sei : foInstancesObj.instances) {
						items.add(new JsSchedulerEvent(root, fold, pset, sei, up.getId(), utz));
					}
				}
				*/
				
				/*
				List<CalendarManager.CalendarEvents> calEvts = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					calEvts = manager.searchSchedulerEvents(root.getOwnerProfileId(), checked, "%"+query+"%");
					// Iterates over calendar->events
					for(CalendarManager.CalendarEvents ce : calEvts) {
						CalendarFolder fold = folders.get(ce.calendar.getCalendarId());
                        if (fold == null) continue;
						
						for(SchedulerEventInstance evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(root, fold, evt, up.getId(), utz));
							}
						}
					}
				}
				*/
				
				new JsonResult("events", items).printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in ManageGridEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
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
	
	public void processGetPlanning(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreUserSettings cus = getEnv().getCoreUserSettings();
		CoreManager core = WT.getCoreManager();
		ArrayList<MapItem> items = new ArrayList<>();
		Connection con = null;
		
		try {
			String eventStartDate = ServletUtils.getStringParameter(request, "startDate", true);
			String eventEndDate = ServletUtils.getStringParameter(request, "endDate", true);
			String timezone = ServletUtils.getStringParameter(request, "timezone", true);
			JsEvent.Attendee.List attendees = ServletUtils.getObjectParameter(request, "attendees", new JsEvent.Attendee.List(), JsEvent.Attendee.List.class);
			//JsAttendeeList attendees = ServletUtils.getObjectParameter(request, "attendees", new JsAttendeeList(), JsAttendeeList.class);
			
			// Parses string parameters
			DateTimeZone eventTz = DateTimeZone.forID(timezone);
			DateTime eventStartDt = DateTimeUtils.parseYmdHmsWithZone(eventStartDate, eventTz);
			DateTime eventEndDt = DateTimeUtils.parseYmdHmsWithZone(eventEndDate, eventTz);
			
			UserProfile up = getEnv().getProfile();
			DateTimeZone profileTz = up.getTimeZone();
			
			LocalTime localStartTime = eventStartDt.toLocalTime();
			LocalTime localEndTime = eventEndDt.toLocalTime();
			LocalTime fromTime = DateTimeUtils.min(localStartTime, us.getWorkdayStart());
			LocalTime toTime = DateTimeUtils.max(localEndTime, us.getWorkdayEnd());
			
			// Defines useful date/time formatters
			DateTimeFormatter ymdhmFmt = DateTimeUtils.createYmdHmFormatter();
			DateTimeFormatter tFmt = DateTimeUtils.createFormatter(cus.getShortTimeFormat());
			DateTimeFormatter dFmt = DateTimeUtils.createFormatter(cus.getShortDateFormat());
			
			ArrayList<String> hours = manager.generateTimeSpans(60, eventStartDt.toLocalDate(), eventEndDt.toLocalDate(), us.getWorkdayStart(), us.getWorkdayEnd(), profileTz);
			
			// Generates fields and columnsInfo dynamically
			ArrayList<FieldMeta> fields = new ArrayList<>();
			ArrayList<GridColumnMeta> colsInfo = new ArrayList<>();
			
			GridColumnMeta col = null;
			fields.add(new FieldMeta("recipient"));
			colsInfo.add(new GridColumnMeta("recipient"));
			for(String hourKey : hours) {
				LocalDateTime ldt = ymdhmFmt.parseLocalDateTime(hourKey);
				fields.add(new FieldMeta(hourKey));
				col = new GridColumnMeta(hourKey, tFmt.print(ldt));
				col.put("date", dFmt.print(ldt));
				col.put("overlaps", DateTimeUtils.between(ldt, eventStartDt.toLocalDateTime(), eventEndDt.toLocalDateTime()));
				colsInfo.add(col);
			}
			
			// Collects attendees availability...
			OUser user = null;
			UserProfileId profileId = null;
			LinkedHashSet<String> busyHours = null;
			MapItem item = null;
			for (JsEvent.Attendee attendee : attendees) {
				item = new MapItem();
				item.put("recipient", attendee.recipient);
				
				user = guessUserByAttendee(core, attendee.recipient);
				if (user != null) {
					profileId = new UserProfileId(user.getDomainId(), user.getUserId());
					busyHours = manager.calculateAvailabilitySpans(60, profileId, eventStartDt.withTime(fromTime), eventEndDt.withTime(toTime), eventTz, true);
					for(String hourKey : hours) {
						item.put(hourKey, busyHours.contains(hourKey) ? "busy" : "free");
					}
				} else {
					for(String hourKey : hours) {
						item.put(hourKey, "unknown");
					}
				}
				items.add(item);
			}
			
			GridMetadata meta = new GridMetadata(true);
			meta.setFields(fields);
			meta.setColumnsInfo(colsInfo);
			new JsonResult(items, meta, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in GetPlanning", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
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
			
			List<Integer> activeCalIds = getActiveFolderIds();
			List<FolderEventInstances> foInstancesObjs = manager.listFolderEventInstances(activeCalIds, fromDate, toDate, up.getTimeZone());
			rpt.setDataSource(manager, fromDate, toDate, up.getTimeZone(), foInstancesObjs);
			
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
	
	public void processPortletEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsPletEvents> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", null);
			
			if (query == null) {
				final ShareRootCalendar root = roots.get(MyShareRootCalendar.SHARE_ID);
				final List<Integer> ids = manager.listCalendarIds();
				for (SchedEventInstance instance : manager.listUpcomingEventInstances(ids, DateTimeUtils.now().withSecondOfMinute(0))) {
					final ShareFolderCalendar folder = folders.get(instance.getCalendarId());
					if (folder == null) continue;
					
					items.add(new JsPletEvents(root, folder, instance, utz));
				}
			} else {
				final Set<Integer> ids = folders.keySet();
				final String pattern = LangUtils.patternizeWords(query);
				for (FolderEventInstances foInstancesObj : manager.listFolderEventInstances(ids, pattern, utz)) {
					final ShareRootCalendar root = rootByFolder.get(foInstancesObj.folder.getCalendarId());
					if (root == null) continue;
					final ShareFolderCalendar fold = folders.get(foInstancesObj.folder.getCalendarId());
					if (fold == null) continue;
					
					for (SchedEventInstance sei : foInstancesObj.instances) {
						items.add(new JsPletEvents(root, fold, sei, utz));
					}
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
			List<UserProfileId> pids = core.listUserIdsByEmail(recipient);
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
	
	private String buildSharingPath(Sharing sharing) throws WTException {
		StringBuilder sb = new StringBuilder();
		
		// Root description part
		CompositeId cid = new CompositeId().parse(sharing.getId());
		if(roots.containsKey(cid.getToken(0))) {
			ShareRootCalendar root = roots.get(cid.getToken(0));
			if(root instanceof MyShareRootCalendar) {
				sb.append(lookupResource(CalendarLocale.CALENDARS_MY));
			} else {
				sb.append(root.getDescription());
			}
		}
		
		// Folder description part
		if(sharing.getLevel() == 1) {
			int calId = Integer.valueOf(cid.getToken(1));
			Calendar calendar = manager.getCalendar(calId);
			sb.append("/");
			sb.append((calendar != null) ? calendar.getName() : cid.getToken(1));
		}
		
		return sb.toString();
	}
	
	private ArrayList<Integer> getActiveFolderIds() {
		ArrayList<Integer> ids = new ArrayList<>();
		synchronized(roots) {
			for (ShareRootCalendar root : getActiveRoots()) {
				for (ShareFolderCalendar folder : foldersByRoot.get(root.getShareId())) {
					if (inactiveFolders.contains(folder.getCalendar().getCalendarId())) continue;
					ids.add(folder.getCalendar().getCalendarId());
				}
			}
		}
		return ids;
	}
	
	private List<ShareRootCalendar> getActiveRoots() {
		return roots.values().stream()
				.filter(root -> !inactiveRoots.contains(root.getShareId()))
				.collect(Collectors.toList());
	}
	
	private void toggleActiveRoot(String shareId, boolean active) {
		toggleActiveRoots(new String[]{shareId}, active);
	}
	
	private void toggleActiveRoots(String[] shareIds, boolean active) {
		synchronized(roots) {
			for (String shareId : shareIds) {
				if (active) {
					inactiveRoots.remove(shareId);
				} else {
					inactiveRoots.add(shareId);
				}
			}	
			us.setInactiveCalendarRoots(inactiveRoots);
		}
	}
	
	private void toggleActiveFolder(Integer folderId, boolean active) {
		toggleActiveFolders(new Integer[]{folderId}, active);
	}
	
	private void toggleActiveFolders(Integer[] folderIds, boolean active) {
		synchronized(roots) {
			for (int folderId : folderIds) {
				if (active) {
					inactiveFolders.remove(folderId);
				} else {
					inactiveFolders.add(folderId);
				}
			}
			us.setInactiveCalendarFolders(inactiveFolders);
		}
	}
	
	private void updateCalendarFolderVisibility(int calendarId, Boolean hidden) {
		synchronized(roots) {
			try {
				CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
				pset.setHidden(hidden);
				manager.updateCalendarCustomProps(calendarId, pset);
				
				// Update internal cache
				ShareFolderCalendar folder = folders.get(calendarId);
				if (!(folder instanceof MyShareFolderCalendar)) {
					folderProps.put(calendarId, pset);
				}
			} catch(WTException ex) {
				logger.error("Error saving custom calendar props", ex);
			}
		}
	}
	
	private void updateCalendarFolderColor(int calendarId, String color) throws WTException {
		synchronized(roots) {
			if (folders.get(calendarId) instanceof MyShareFolderCalendar) {
				Calendar cal = manager.getCalendar(calendarId);
				cal.setColor(color);
				manager.updateCalendar(cal);
				updateFoldersCache();
			} else {
				CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
				pset.setColor(color);
				manager.updateCalendarCustomProps(calendarId, pset);
				folderProps.put(calendarId, pset);
			}
		}
	}
	
	private void updateCalendarFolderSync(int calendarId, Calendar.Sync sync) throws WTException {
		synchronized(roots) {
			if (folders.get(calendarId) instanceof MyShareFolderCalendar) {
				Calendar cat = manager.getCalendar(calendarId);
				cat.setSync(sync);
				manager.updateCalendar(cat);
				updateFoldersCache();
			} else {
				CalendarPropSet pset = manager.getCalendarCustomProps(calendarId);
				pset.setSync(sync);
				manager.updateCalendarCustomProps(calendarId, pset);
				folderProps.put(calendarId, pset);
			}
		}
	}
	
	private ExtTreeNode createRootNode(boolean chooser, ShareRootCalendar root) {
		if (root instanceof MyShareRootCalendar) {
			return createRootNode(chooser, root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), lookupResource(CalendarLocale.CALENDARS_MY), false, "wtcal-icon-root-my-xs")
					.setExpanded(true);
		} else {
			return createRootNode(chooser, root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), root.getDescription(), false, "wtcal-icon-root-incoming-xs")
					.setExpanded(true);
		}
	}
	
	private ExtTreeNode createRootNode(boolean chooser, String id, String pid, String rights, String text, boolean leaf, String iconClass) {
		boolean active = !inactiveRoots.contains(id);
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", JsFolderNode.TYPE_ROOT);
		node.put("_pid", pid);
		node.put("_rrights", rights);
		node.put("_active", active);
		node.setIconClass(iconClass);
		if (!chooser) node.setChecked(active);
		node.put("expandable", false);
		return node;
	}
	
	private ExtTreeNode createFolderNode(boolean chooser, ShareFolderCalendar folder, CalendarPropSet folderProps, SharePermsRoot rootPerms) {
		Calendar cal = folder.getCalendar();
		String id = new CompositeId().setTokens(folder.getShareId(), cal.getCalendarId()).toString();
		String color = cal.getColor();
		Calendar.Sync sync = Calendar.Sync.OFF;
		boolean active = !inactiveFolders.contains(cal.getCalendarId());
		
		if (folderProps != null) { // Props are not null only for incoming folders
			if (folderProps.getHiddenOrDefault(false)) return null;
			color = folderProps.getColorOrDefault(color);
			sync = folderProps.getSyncOrDefault(sync);
		} else {
			sync = cal.getSync();
		}
		
		ExtTreeNode node = new ExtTreeNode(id, cal.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cal.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_calId", cal.getCalendarId());
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_provider", EnumUtils.toSerializedName(cal.getProvider()));
		node.put("_color", Calendar.getHexColor(color));
		node.put("_sync", EnumUtils.toSerializedName(sync));
		node.put("_default", cal.getIsDefault());
		node.put("_active", active);
		node.put("_isPrivate", cal.getIsPrivate());
		node.put("_defBusy", cal.getDefaultBusy());
		node.put("_defReminder", cal.getDefaultReminder());
		if (!chooser) node.setChecked(active);
		return node;
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
}
