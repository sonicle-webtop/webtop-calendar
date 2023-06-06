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

import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.calendar.model.GetEventScope;
import com.rits.cloning.Cloner;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.time.DateRange;
import com.sonicle.commons.time.DateTimeRange;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.dav.CalDav;
import com.sonicle.dav.CalDavFactory;
import com.sonicle.dav.DavSyncStatus;
import com.sonicle.dav.DavUtil;
import com.sonicle.dav.caldav.DavCalendar;
import com.sonicle.dav.caldav.DavCalendarEvent;
import com.sonicle.dav.impl.DavException;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.bol.VVEvent;
import com.sonicle.webtop.calendar.bol.VVEventInstance;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OCalendarOwnerInfo;
import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttachment;
import com.sonicle.webtop.calendar.bol.OEventAttachmentData;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OEventCustomValue;
import com.sonicle.webtop.calendar.bol.OEventICalendar;
import com.sonicle.webtop.calendar.bol.OEventInfo;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VEventHrefSync;
import com.sonicle.webtop.calendar.bol.VEventFootprint;
import com.sonicle.webtop.calendar.bol.VExpEvent;
import com.sonicle.webtop.calendar.bol.VExpEventInstance;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.CalendarPropsDAO;
import com.sonicle.webtop.calendar.dal.EventAttachmentDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventCustomValueDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.EventICalendarDAO;
import com.sonicle.webtop.calendar.dal.EventPredicateVisitor;
import com.sonicle.webtop.calendar.dal.EventTagDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.calendar.io.EventInput;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import com.sonicle.webtop.core.util.NotificationHelper;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtDateTime;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import com.sonicle.webtop.calendar.io.EventFileReader;
import com.sonicle.webtop.calendar.io.ICalendarOutput;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.EventObjectChanged;
import com.sonicle.webtop.calendar.model.EventFootprint;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.io.ICalendarInput;
import com.sonicle.webtop.calendar.model.CalendarFSFolder;
import com.sonicle.webtop.calendar.model.CalendarFSOrigin;
import com.sonicle.webtop.calendar.model.ComparableEventBounds;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventAttachmentWithStream;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.calendar.model.EventObjectWithBean;
import com.sonicle.webtop.calendar.model.EventObjectWithICalendar;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.calendar.model.UpdateTagsOperation;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.AuditLogManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.sdk.AbstractFolderShareCache;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.app.sdk.WTConstraintException;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.app.sdk.WTUnsupportedOperationException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.model.FolderShareOriginFolders;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.sdk.AbstractMapCache;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.mail.IMailManager;
import freemarker.template.TemplateException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMultipart;
import java.util.concurrent.TimeUnit;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.sf.qualitycheck.Check;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.subject.Subject;
import org.joda.time.Duration;
import com.sonicle.commons.flags.BitFlagsEnum;
import com.sonicle.commons.time.DateTimeRange2;
import com.sonicle.commons.time.TimeRange;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.webtop.calendar.bol.VCalendarDefaults;
import com.sonicle.webtop.calendar.bol.VEventObjectChange;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.msg.ResourceReservationReplySM;

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseManager implements ICalendarManager {
	public static final Logger logger = WT.getLogger(CalendarManager.class);
	private static final String GROUPNAME_CALENDAR = "CALENDAR";
	public static final String TARGET_THIS = "this";
	public static final String TARGET_SINCE = "since";
	public static final String TARGET_ALL = "all";
	public static final String SUGGESTION_EVENT_TITLE = "eventtitle";
	public static final String SUGGESTION_EVENT_LOCATION = "eventlocation";
	
	private final OwnerCache ownerCache = new OwnerCache();
	private final ShareCache shareCache = new ShareCache();
	private final KeyedReentrantLocks<String> locks = new KeyedReentrantLocks<>();
	
	private static final ConcurrentHashMap<String, UserProfileId> pendingRemoteCalendarSyncs = new ConcurrentHashMap<>();
	
	public CalendarManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		if (!fastInit) {
			shareCache.init();
		}
	}
	
	/**
	 * @deprecated Use listMyCalendarIds() instead.
	 */
	@Deprecated
	@Override
	public Set<Integer> listCalendarIds() throws WTException {
		return listMyCalendarIds();
	}
	
	/**
	 * @deprecated Use listMyCalendar() instead.
	 */
	@Deprecated
	@Override
	public Map<Integer, Calendar> listCalendars() throws WTException {
		return listCalendars(getTargetProfileId(), true);
	}
	
	private CoreManager getCoreManager() {
		return WT.getCoreManager(getTargetProfileId());
	}
	
	private CalendarServiceSettings getServiceSettings() {
		return new CalendarServiceSettings(SERVICE_ID, getTargetProfileId().getDomainId());
	}
	
	private CalDav getCalDav(String username, String password) {
		if (!StringUtils.isBlank(username) && !StringUtils.isBlank(username)) {
			return CalDavFactory.begin(username, password);
		} else {
			return CalDavFactory.begin();
		}
	}
	
	@Override
	public Set<FolderSharing.SubjectConfiguration> getFolderShareConfigurations(final UserProfileId originProfileId, final FolderSharing.Scope scope) throws WTException {
		CoreManager coreMgr = getCoreManager();
		return coreMgr.getFolderShareConfigurations(SERVICE_ID, GROUPNAME_CALENDAR, originProfileId, scope);
	}
	
	@Override
	public void updateFolderShareConfigurations(final UserProfileId originProfileId, final FolderSharing.Scope scope, final Set<FolderSharing.SubjectConfiguration> configurations) throws WTException {
		CoreManager coreMgr = getCoreManager();
		coreMgr.updateFolderShareConfigurations(SERVICE_ID, GROUPNAME_CALENDAR, originProfileId, scope, configurations);
	}
	
	@Override
	public Map<UserProfileId, CalendarFSOrigin> listIncomingCalendarOrigins() throws WTException {
		return shareCache.getOriginsMap();
	}
	
	@Override
	public CalendarFSOrigin getIncomingCalendarOriginByFolderId(final int calendarId) throws WTException {
		return shareCache.getOriginByFolderId(calendarId);
	}
	
	@Override
	public Map<Integer, CalendarFSFolder> listIncomingCalendarFolders(final CalendarFSOrigin origin) throws WTException {
		Check.notNull(origin, "origin");
		return listIncomingCalendarFolders(origin.getProfileId());
	}
	
	@Override
	public Map<Integer, CalendarFSFolder> listIncomingCalendarFolders(final UserProfileId originProfileId) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		CoreManager coreMgr = getCoreManager();
		LinkedHashMap<Integer, CalendarFSFolder> folders = new LinkedHashMap<>();
		
		CalendarFSOrigin origin = shareCache.getOrigin(originProfileId);
		if (origin != null) {
			for (Integer folderId : shareCache.getFolderIdsByOrigin(originProfileId)) {
				if (origin.isResource()) {
					final Calendar calendar = getCalendar(folderId, false);
					if (calendar == null) continue;
					
					folders.put(folderId, new CalendarFSFolder(folderId, origin.getWildcardPermissions(), calendar));
					
				} else {
					final Calendar calendar = getCalendar(folderId, false);
					if (calendar == null) continue;
					
					FolderShare.Permissions permissions = coreMgr.evaluateFolderSharePermissions(SERVICE_ID, GROUPNAME_CALENDAR, originProfileId, FolderSharing.Scope.folder(String.valueOf(folderId)), false);
					if (permissions == null) {
						// If permissions are not defined at requested folder scope,
						// generates an empty permission object that will be filled below
						// with wildcard rights
						permissions = FolderShare.Permissions.none();
					}
					permissions.getFolderPermissions().set(origin.getWildcardPermissions().getFolderPermissions());
					permissions.getItemsPermissions().set(origin.getWildcardPermissions().getItemsPermissions());
					
					// Here we can have folders with no READ permission: these folders
					// will be included in cache for now, Manager's clients may filter
					// out them in downstream processing.
					// if (!permissions.getFolderPermissions().has(FolderShare.FolderRight.READ)) continue;
					folders.put(folderId, new CalendarFSFolder(folderId, permissions, calendar));
				}	
			}
		}
		return folders;
	}
	
	@Override
	public Set<Integer> listMyCalendarIds() throws WTException {
		return listCalendarIds(getTargetProfileId());
	}
	
	@Override
	public Set<Integer> listIncomingCalendarIds() throws WTException {
		return shareCache.getFolderIds();
	}
	
	@Override
	public Set<Integer> listAllCalendarIds() throws WTException {
		return Stream.concat(listMyCalendarIds().stream(), listIncomingCalendarIds().stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	private Set<Integer> listCalendarIds(UserProfileId pid) throws WTException {
		return listCalendarIdsIn(pid, null);
	}
	
	private Set<Integer> listCalendarIdsIn(UserProfileId pid, Collection<Integer> calendarIds) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doListCalendarIdsIn(con, pid, calendarIds);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private Set<Integer> doListCalendarIdsIn(Connection con, UserProfileId profileId, Collection<Integer> calendarIds) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		
		if (calendarIds == null) {
			return calDao.selectIdsByProfile(con, profileId.getDomainId(), profileId.getUserId());
		} else {
			return calDao.selectIdsByProfileIn(con, profileId.getDomainId(), profileId.getUserId(), calendarIds);
		}
	}
	
	@Override
	public Map<Integer, Calendar> listMyCalendars() throws WTException {
		return listCalendars(getTargetProfileId(), true);
	}
	
	private Map<Integer, Calendar> listCalendars(UserProfileId ownerPid, boolean evalRights) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		LinkedHashMap<Integer, Calendar> items = new LinkedHashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCalendar ocal : calDao.selectByProfile(con, ownerPid.getDomainId(), ownerPid.getUserId())) {
				if (evalRights && !quietlyCheckRightsOnCalendar(ocal.getCalendarId(), FolderShare.FolderRight.READ)) continue;
				items.put(ocal.getCalendarId(), ManagerUtils.createCalendar(ocal));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<Calendar> listRemoteCalendarsToBeSynchronized() throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		ArrayList<Calendar> items = new ArrayList<>();
		Connection con = null;
		
		try {
			ensureSysAdmin();
			con = WT.getConnection(SERVICE_ID);
			for (OCalendar ocal : calDao.selectByProvider(con, Arrays.asList(Calendar.Provider.WEBCAL, Calendar.Provider.CALDAV))) {
				items.add(ManagerUtils.createCalendar(ocal));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<Integer, DateTime> getCalendarsLastRevision(Collection<Integer> calendarIds) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(categoryId -> quietlyCheckRightsOnCalendar(categoryId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			return evtDao.selectMaxRevTimestampByCalendars(con, okCalendarIds);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public UserProfileId getCalendarOwner(int calendarId) throws WTException {
		return ownerCache.get(calendarId);
	}
	
	@Override
	public Integer getDefaultCalendarId() throws WTException {
		CalendarUserSettings us = new CalendarUserSettings(SERVICE_ID, getTargetProfileId());
		
		Integer calendarId = null;
		try {
			locks.tryLock("getDefaultCalendarId", 60, TimeUnit.SECONDS);
			calendarId = us.getDefaultCalendarFolder();
			if (calendarId == null || !quietlyCheckRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE)) {
				try {
					calendarId = getBuiltInCalendarId();
					if (calendarId == null) throw new WTException("Built-in calendar is null");
					us.setDefaultCalendarFolder(calendarId);
				} catch (Exception ex) {
					logger.error("Unable to get built-in calendar", ex);
				}
			}
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("getDefaultCalendarId");
		}
		return calendarId;
	}
	
	@Override
	public Integer getBuiltInCalendarId() throws WTException {
		CalendarDAO catDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer calendarId = catDao.selectBuiltInIdByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (calendarId == null) return null;
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			return calendarId;
			
		} catch(SQLException | DAOException | WTException ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	@Override
	public Integer getDefaultCalendarId() throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			Integer calendarId = calDao.selectDefaultByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (calendarId == null) return null;
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			return calendarId;
			
		} catch(SQLException | DAOException | WTException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public boolean existCalendar(int calendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			return calDao.existsById(con, calendarId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar getCalendar(int calendarId) throws WTException {
		return getCalendar(calendarId, true);
	}
	
	private Calendar getCalendar(int calendarId, boolean evalRights) throws WTException {
		Connection con = null;
		
		try {
			if (evalRights) checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGet(con, calendarId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar getBuiltInCalendar() throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OCalendar ocal = calDao.selectBuiltInByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (ocal == null) return null;
			checkRightsOnCalendar(ocal.getCalendarId(), FolderShare.FolderRight.READ);
			
			return ManagerUtils.createCalendar(ocal);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, String> getCalendarLinks(int calendarId) throws WTException {
		checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
		
		UserProfile.Data ud = WT.getUserData(getTargetProfileId());
		String davServerBaseUrl = WT.getDavServerBaseUrl(getTargetProfileId().getDomainId());
		String calendarUid = ManagerUtils.encodeAsCalendarUid(calendarId);
		String calendarUrl = MessageFormat.format(ManagerUtils.CALDAV_CALENDAR_URL, ud.getProfileEmailAddress(), calendarUid);
		
		LinkedHashMap<String, String> links = new LinkedHashMap<>();
		links.put(ManagerUtils.CALENDAR_LINK_CALDAV, PathUtils.concatPathParts(davServerBaseUrl, calendarUrl));
		return links;
	}
	
	@Override
	public Calendar addCalendar(Calendar calendar) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarOrigin(calendar.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar.setBuiltIn(false);
			calendar = doCalendarInsert(con, calendar);
			
			DbUtils.commitQuietly(con);
			onAfterCalendarAction(calendar.getCalendarId(), calendar.getProfileId());
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CALENDAR, AuditAction.CREATE, calendar.getCalendarId(), null);
			}
			
			return calendar;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar addBuiltInCalendar() throws WTException {
		return addBuiltInCalendar(null);
	}
	
	@Override
	public Calendar addBuiltInCalendar(final String color) throws WTException {
		CalendarDAO caldao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarOrigin(getTargetProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			OCalendar ocal = caldao.selectBuiltInByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (ocal != null) {
				logger.debug("Built-in calendar already present");
				return null;
			}
			
			Calendar cal = new Calendar();
			cal.setBuiltIn(true);
			cal.setName(WT.getPlatformName());
			cal.setDescription("");
			cal.setColor(color);
			cal.setDefaultBusy(true);
			cal = doCalendarInsert(con, cal);
			
			DbUtils.commitQuietly(con);
			onAfterCalendarAction(cal.getCalendarId(), cal.getProfileId());
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CALENDAR, AuditAction.CREATE, cal.getCalendarId(), null);
			}
			
			// Sets calendar as default
			CalendarUserSettings us = new CalendarUserSettings(SERVICE_ID, cal.getProfileId());
			us.setDefaultCalendarFolder(cal.getCalendarId());
			
			return cal;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateCalendar(Calendar calendar) throws WTNotFoundException, WTException {
		Connection con = null;
		
		try {
			int calendarId = calendar.getCalendarId();
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.UPDATE);
			
			con = WT.getConnection(SERVICE_ID, false);
			boolean ret = doCalendarUpdate(con, calendar);
			if (!ret) throw new WTNotFoundException("Calendar not found [{}]", calendarId);
			
			DbUtils.commitQuietly(con);
			onAfterCalendarAction(calendarId, calendar.getProfileId());
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CALENDAR, AuditAction.UPDATE, calendarId, null);
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteCalendar(int calendarId) throws WTNotFoundException, WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.DELETE);
			
			// Retrieve sharing configuration (for later)
			final UserProfileId sharingOwnerPid = getCalendarOwner(calendarId);
			final FolderSharing.Scope sharingScope = FolderSharing.Scope.folder(String.valueOf(calendarId));
			Set<FolderSharing.SubjectConfiguration> configurations = getFolderShareConfigurations(sharingOwnerPid, sharingScope);
			
			con = WT.getConnection(SERVICE_ID, false);
			Calendar cal = ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) throw new WTNotFoundException("Calendar not found [{}]", calendarId);
			
			int ret = calDao.deleteById(con, calendarId);
			psetDao.deleteByCalendar(con, calendarId);
			doEventsDeleteByCalendar(con, calendarId, !cal.isProviderRemote());
			
			// Cleanup sharing, if necessary
			if ((configurations != null) && !configurations.isEmpty()) {
				logger.debug("Removing {} active sharing [{}]", configurations.size(), sharingOwnerPid);
				configurations.clear();
				updateFolderShareConfigurations(sharingOwnerPid, sharingScope, configurations);
			}
			
			DbUtils.commitQuietly(con);
			onAfterCalendarAction(calendarId, cal.getProfileId());
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CALENDAR, AuditAction.DELETE, calendarId, null);
				// removed due to new audit implementation
				// auditLogWrite(AuditContext.CALENDAR, AuditAction.DELETE, "*", calendarId);
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CalendarPropSet getCalendarCustomProps(int calendarId) throws WTException {
		return getCalendarCustomProps(getTargetProfileId(), calendarId);
	}
	
	private CalendarPropSet getCalendarCustomProps(UserProfileId profileId, int calendarId) throws WTException {
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OCalendarPropSet opset = psetDao.selectByProfileCalendar(con, profileId.getDomainId(), profileId.getUserId(), calendarId);
			return (opset == null) ? new CalendarPropSet() : ManagerUtils.createCalendarPropSet(opset);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<Integer, CalendarPropSet> getCalendarCustomProps(Collection<Integer> calendarIds) throws WTException {
		return getCalendarCustomProps(getTargetProfileId(), calendarIds);
	}
	
	public Map<Integer, CalendarPropSet> getCalendarCustomProps(UserProfileId profileId, Collection<Integer> calendarIds) throws WTException {
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			LinkedHashMap<Integer, CalendarPropSet> psets = new LinkedHashMap<>(calendarIds.size());
			Map<Integer, OCalendarPropSet> map = psetDao.selectByProfileCalendarIn(con, profileId.getDomainId(), profileId.getUserId(), calendarIds);
			for (Integer categoryId : calendarIds) {
				OCalendarPropSet opset = map.get(categoryId);
				psets.put(categoryId, (opset == null) ? new CalendarPropSet() : ManagerUtils.createCalendarPropSet(opset));
			}
			return psets;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CalendarPropSet updateCalendarCustomProps(int calendarId, CalendarPropSet propertySet) throws WTException {
		ensureUser();
		return updateCalendarCustomProps(getTargetProfileId(), calendarId, propertySet);
	}
	
	private CalendarPropSet updateCalendarCustomProps(UserProfileId profileId, int calendarId, CalendarPropSet propertySet) throws WTException {
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			OCalendarPropSet opset = ManagerUtils.createOCalendarPropSet(propertySet);
			opset.setDomainId(profileId.getDomainId());
			opset.setUserId(profileId.getUserId());
			opset.setCalendarId(calendarId);
			
			con = WT.getConnection(SERVICE_ID);
			try {
				psetDao.insert(con, opset);
			} catch(DAOIntegrityViolationException ex1) {
				psetDao.update(con, opset);
			}
			return propertySet;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<LocalDate> listEventDates(Collection<Integer> calendarIds, DateTime from, DateTime to, DateTimeZone refTimezone) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> okCalendarIds = calendarIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
					.collect(Collectors.toList());
			
			HashSet<LocalDate> dates = new HashSet<>();
			for (VVEvent vevt : evtDao.viewByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				dates.addAll(CalendarUtils.getDatesSpan(vevt.getAllDay(), vevt.getStartDate(), vevt.getEndDate(), DateTimeZone.forID(vevt.getTimezone())));
			}
			int noOfRecurringInst = Days.daysBetween(from, to).getDays() + 2;
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				final List<SchedEventInstance> instances = calculateRecurringInstances_OLD(con, new SchedEventInstanceMapper(vevt, false), from, to, refTimezone, noOfRecurringInst);
				for (SchedEventInstance instance : instances) {
					dates.addAll(CalendarUtils.getDatesSpan(instance.getAllDay(), instance.getStartDate(), instance.getEndDate(), instance.getDateTimeZone()));
				}
			}
			return new ArrayList<>(dates);
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<ComparableEventBounds> listEventsBounds(final Collection<Integer> calendarIds, final DateTimeRange2 viewRange, final DateTimeZone targetTimezone) throws WTException {
		Check.notNull(calendarIds, "calendarIds");
		Check.notNull(viewRange, "viewRange");
		Check.notNull(viewRange.getStart(), "viewRange.getStart()");
		Check.notNull(viewRange.getEnd(), "viewRange.getEnd()");
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			return doListEventsBounds(con, okCalendarIds, viewRange.getStart(), viewRange.getEnd(), targetTimezone);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<EventObject> listEventObjects(int calendarId, EventObjectOutputType outputType) throws WTException {
		return CalendarManager.this.listEventObjects(calendarId, null, outputType);
	}
	
	@Override
	public List<EventObject> listEventObjects(int calendarId, DateTime since, EventObjectOutputType outputType) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			ArrayList<EventObject> items = new ArrayList<>();
			Map<String, List<VEventObject>> vobjMap = null;
			if (since == null) {
				vobjMap = evtDao.viewCalObjectsByCalendar(con, calendarId);
			} else {
				vobjMap = evtDao.viewCalObjectsByCalendarSince(con, calendarId, since);
			}
			for (List<VEventObject> vobjs : vobjMap.values()) {
				if (vobjs.isEmpty()) continue;
				VEventObject vobj = vobjs.get(vobjs.size()-1);
				if (vobjs.size() > 1) {
					logger.trace("Many events ({}) found for same href [{} -> {}]", vobjs.size(), vobj.getHref(), vobj.getEventId());
				}
				
				items.add(doEventObjectPrepare(con, vobj, outputType));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CollectionChangeSet<EventObjectChanged> listEventObjectsChanges(int calendarId, DateTime since, Integer limit) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			con = WT.getConnection(SERVICE_ID);
			ArrayList<EventObjectChanged> inserted = new ArrayList<>();
			ArrayList<EventObjectChanged> updated = new ArrayList<>();
			ArrayList<EventObjectChanged> deleted = new ArrayList<>();
			List<VEventObjectChange> changes = evtDao.viewChangedObjectsByCalendarSince(con, calendarId, since, limit == null ? -1 : limit);
			for (VEventObjectChange change : changes) {
				if (change.isInserted()) {
					inserted.add(new EventObjectChanged(change.getEventId(), change.getTimestamp(), change.getHref()));
				} else if (change.isUpdated()) {
					updated.add(new EventObjectChanged(change.getEventId(), change.getTimestamp(), change.getHref()));
				} else if (change.isDeleted()) {
					deleted.add(new EventObjectChanged(change.getEventId(), change.getTimestamp(), change.getHref()));
				}
			}
			return new CollectionChangeSet<>(inserted, updated, deleted);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventObjectWithICalendar getEventObjectWithICalendar(int calendarId, String href) throws WTException {
		List<EventObjectWithICalendar> ccs = getEventObjectsWithICalendar(calendarId, Arrays.asList(href));
		return ccs.isEmpty() ? null : ccs.get(0);
	}
	
	@Override
	public List<EventObjectWithICalendar> getEventObjectsWithICalendar(int calendarId, Collection<String> hrefs) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			ArrayList<EventObjectWithICalendar> items = new ArrayList<>();
			Map<String, List<VEventObject>> map = evtDao.viewCalObjectsByCalendarHrefs(con, calendarId, hrefs);
			for (String href : hrefs) {
				List<VEventObject> vevts = map.get(href);
				if (vevts == null) continue;
				if (vevts.isEmpty()) continue;
				VEventObject vevt = vevts.get(vevts.size()-1);
				if (vevts.size() > 1) {
					logger.trace("Many events ({}) found for same href [{} -> {}]", vevts.size(), vevt.getHref(), vevt.getEventId());
				}
				
				items.add((EventObjectWithICalendar)doEventObjectPrepare(con, vevt, EventObjectOutputType.ICALENDAR));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventObject getEventObject(int calendarId, int eventId, EventObjectOutputType outputType) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			VEventObject vevt = evtDao.viewCalObjectById(con, calendarId, eventId);
			return (vevt == null) ? null : doEventObjectPrepare(con, vevt, outputType);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void addEventObject(int calendarId, String href, net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		boolean notifyAttendees = false;
		ICalendarInput in = new ICalendarInput(udata.getTimeZone())
				.withDefaultAttendeeNotify(notifyAttendees);
		
		ArrayList<EventInput> eis = in.fromICalendarFile(iCalendar, null);
		if (eis.isEmpty()) throw new WTException("iCalendar object does not contain any events");
		if (eis.size() > 1) throw new WTException("iCalendar object should contain one event");
		EventInput ei = eis.get(0);
		ei.event.setCalendarId(calendarId);
		ei.event.setHref(href);
		
		String rawData = null;
		if (iCalendar != null) {
			String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
			try {
				rawData = new ICalendarOutput(prodId).write(iCalendar);
			} catch(IOException ex) {
				throw new WTException(ex, "Error serializing iCalendar");
			}
		}
		
		// Invitation messages should be sent only in two cases:
		// 1) Organizer addr. matches current-user's personal address
		// 2) Organizer addr. matches current-user's profile address
		String orgAddress = ei.event.getOrganizerAddress();
		if (StringUtils.equals(orgAddress, udata.getPersonalEmailAddress()) || StringUtils.equals(orgAddress, udata.getProfileEmailAddress())) {
			notifyAttendees = true;
			ei.event.getAttendees().stream().forEach(att -> att.setNotify(true));
		}
		addEvent(ei.event, rawData, notifyAttendees);
	}
	
	@Override
	public void updateEventObject(int calendarId, String href, net.fortuna.ical4j.model.Calendar iCalendar) throws WTNotFoundException, WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		int eventId = getEventIdByCategoryHref(calendarId, href, true);
		
		ICalendarInput in = new ICalendarInput(udata.getTimeZone())
				.withDefaultAttendeeNotify(true)
				.withIncludeVEventSourceInOutput(true);
		ArrayList<EventInput> eis = in.fromICalendarFile(iCalendar, null);
		if (eis.isEmpty()) throw new WTException("iCalendar object does not contain any events");
		
		EventInput refInput = eis.remove(0);
		if (refInput.exRefersToPublicUid != null) throw new WTException("First iCalendar event should not have a RECURRENCE-ID set");
		
		// Collect dates exceptions and prepare broken event to be inserted
		ArrayList<EventInput> eiExs = new ArrayList<>();
		LinkedHashSet<LocalDate> exDates = new LinkedHashSet<>();
		for (EventInput ei : eis) {
			if (!StringUtils.equals(ei.exRefersToPublicUid, refInput.event.getPublicUid())) continue;
			if (exDates.contains(ei.addsExOnMaster)) continue;
			
			exDates.add(ei.addsExOnMaster);
			if (!ei.isSourceEventCancelled()) eiExs.add(ei);
		}
		
		// Adds new collected exceptions and then updates master event
		if (!exDates.isEmpty()) {
			if (refInput.event.hasExcludedDates()) {
				refInput.event.getExcludedDates().addAll(exDates);
			} else {
				refInput.event.setExcludedDates(exDates);
			}
		}
		refInput.event.setEventId(eventId);
		refInput.event.setCalendarId(calendarId);
		refInput.event.setExcludedDates(exDates);
		updateEvent(refInput.event, false, false, false, true);
		
		// Here we do not support creating broken events related to the
		// main event series (re-attach unavailable). Instance exceptions 
		// should have been created above as date exception into the main
		// event; so simply create exceptions as new events.
		
		if (!eiExs.isEmpty()) {
			for (EventInput eiEx : eiExs) {
				eiEx.event.setCalendarId(calendarId);
				eiEx.event.setPublicUid(null); // reset uid
				//TODO: handle raw value to persist custom properties
				try {
					addEvent(eiEx.event, null, true);
				} catch (Exception ex) {
					logger.error("Unable to insert exception on {} as new event", eiEx.addsExOnMaster, ex);
				}
			}
		}
	}
	
	@Override
	public void deleteEventObject(int calendarId, String href) throws WTNotFoundException, WTException {
		int eventId = getEventIdByCategoryHref(calendarId, href, true);
		deleteEvent(eventId, true);
	}
	
	private int getEventIdByCategoryHref(int calendarId, String href, boolean throwExIfManyMatchesFound) throws WTNotFoundException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> ids = evtDao.selectAliveIdsByCalendarHrefs(con, calendarId, href);
			if (ids.isEmpty()) throw new WTNotFoundException("Event not found [{}, {}]", calendarId, href);
			if (throwExIfManyMatchesFound && (ids.size() > 1)) throw new WTException("Many matches for href [{}]", href);
			return ids.get(ids.size()-1);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public boolean existEventInstance(Collection<Integer> calendarIds, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
					.collect(Collectors.toList());
			
			EventPredicateVisitor epv = new EventPredicateVisitor(EventPredicateVisitor.Target.NORMAL)
					.withIgnoreCase(true)
					.withForceStringLikeComparison(true);
			org.jooq.Condition norCondition = null;
			org.jooq.Condition recCondition = null;
			if (conditionPredicate != null) {
				norCondition = BaseDAO.createCondition(conditionPredicate, epv);
				recCondition = BaseDAO.createCondition(conditionPredicate, new EventPredicateVisitor(EventPredicateVisitor.Target.RECURRING)
						.withIgnoreCase(true)
						.withForceStringLikeComparison(true)
				);
			}
			
			DateTime from = null;
			DateTime to = null;
			DateTime instFrom = epv.hasFromRange() ? epv.getFromRange() : from;
			DateTime instTo = epv.hasToRange() ? epv.getToRange() : to;
			int noOfRecurringInst = 368;
			
			con = WT.getConnection(SERVICE_ID);
			if (evtDao.existByCalendarTypeCondition(con, okCalendarIds, from, to, norCondition)) {
				return true;
			}
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, okCalendarIds, from, to, recCondition)) {
				List<SchedEventInstance> instances = calculateRecurringInstances_OLD(con, new SchedEventInstanceMapper(vevt, false), instFrom, instTo, targetTimezone, noOfRecurringInst);
				if (!instances.isEmpty()) return true;
			}
			return false;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarIds, DateTime now, DateTimeZone targetTimezone) throws WTException {
		return listUpcomingEventInstances(calendarIds, now, null, targetTimezone);
	}
	
	@Override
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarIds, DateTime now, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone) throws WTException {
		return listUpcomingEventInstances(calendarIds, now, 3, conditionPredicate, targetTimezone);
	}
	
	@Override
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarIds, DateTime now, int days, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone) throws WTException {
		if (days > 15) days = 15;
		DateTimeRange range = new DateTimeRange(
				now.withSecondOfMinute(0).withMillisOfSecond(0).withZone(targetTimezone), 
				now.withTimeAtStartOfDay().plusDays(days));
		return listEventInstances(calendarIds, range, conditionPredicate, targetTimezone, true);
	}
	
	@Override
	public List<SchedEventInstance> listEventInstances(Collection<Integer> calendarIds, DateRange range, DateTimeZone targetTimezone, boolean sort) throws WTException {
		return listEventInstances(calendarIds, range, null, targetTimezone, sort);
	}
	
	@Override
	public List<SchedEventInstance> listEventInstances(Collection<Integer> calendarIds, DateRange range, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone, boolean sort) throws WTException {
		DateTimeRange newRange = (range == null) ? null : new DateTimeRange(
				range.from.toDateTimeAtStartOfDay(targetTimezone), 
				range.to.plusDays(1).toDateTimeAtStartOfDay(targetTimezone));
		return listEventInstances(calendarIds, newRange, conditionPredicate, targetTimezone, sort);
	}
	
	@Override
	public List<SchedEventInstance> listEventInstances(Collection<Integer> calendarIds, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone) throws WTException {
		return listEventInstances(calendarIds, (DateTimeRange)null, conditionPredicate, targetTimezone, true);
	}
	
	@Override
	public List<SchedEventInstance> listEventInstances(Collection<Integer> calendarIds, DateTimeRange range, DateTimeZone targetTimezone, boolean sort) throws WTException {
		return listEventInstances(calendarIds, range, null, targetTimezone, sort);
	}
	
	@Override
	public List<SchedEventInstance> listEventInstances(Collection<Integer> calendarIds, DateTimeRange range, Condition<EventQuery> conditionPredicate, DateTimeZone targetTimezone, boolean sort) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			UserProfileId runProfile = RunContext.getRunProfileId();
			List<Integer> okCalendarIds = calendarIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
					.collect(Collectors.toList());
			
			// Prepares condition Visitor
			EventPredicateVisitor epv = new EventPredicateVisitor(EventPredicateVisitor.Target.NORMAL)
					.withIgnoreCase(true)
					.withForceStringLikeComparison(true);
			org.jooq.Condition norCondition = null;
			org.jooq.Condition recCondition = null;
			if (conditionPredicate != null) {
				norCondition = BaseDAO.createCondition(conditionPredicate, epv);
				recCondition = BaseDAO.createCondition(conditionPredicate, new EventPredicateVisitor(EventPredicateVisitor.Target.RECURRING)
						.withIgnoreCase(true)
						.withForceStringLikeComparison(true)
				);
			}
			
			boolean hasRange = (range != null);
			DateTime from = hasRange ? range.from : null;
			DateTime to = hasRange ? range.to : null;
			DateTime instFrom = epv.hasFromRange() ? epv.getFromRange() : from;
			DateTime instTo = epv.hasToRange() ? epv.getToRange() : to;
			int noOfRecurringInst = hasRange ? Days.daysBetween(from, to).getDays() + 2 : 368;
			
			con = WT.getConnection(SERVICE_ID);
			ArrayList<SchedEventInstance> instances = new ArrayList<>();
			for (VVEvent vevt : evtDao.viewByCalendarRangeCondition(con, okCalendarIds, from, to, norCondition)) {
				boolean keepPrivate = needsTreatAsPrivate(runProfile, vevt.getCalendarProfileId(), vevt.getIsPrivate());
				SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), vevt);
				item.setKey(EventKey.buildKey(vevt.getEventId(), vevt.getSeriesEventId()));
				if (keepPrivate) item.censorize();
				instances.add(item);
			}
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, okCalendarIds, from, to, recCondition)) {
				boolean keepPrivate = needsTreatAsPrivate(runProfile, vevt.getCalendarProfileId(), vevt.getIsPrivate());
				instances.addAll(calculateRecurringInstances_OLD(con, new SchedEventInstanceMapper(vevt, keepPrivate), instFrom, instTo, targetTimezone, noOfRecurringInst));
			}
			
			//TODO: transform to an ordered insert
			if (sort) {
				Collections.sort(instances, new Comparator<SchedEventInstance>() {
					@Override
					public int compare(final SchedEventInstance se1, final SchedEventInstance se2) {
						return se1.getStartDate().compareTo(se2.getStartDate());
					}
				});
			}
			
			return instances;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	public List<SchedEventInstance> toInstances(SchedEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		if (event.isRecurring()) {
			Connection con = null;
			try {
				con = WT.getConnection(SERVICE_ID);
				return calculateRecurringInstances(con, event, fromDate, toDate, userTimezone, 200);

			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}
		} else {
			return Arrays.asList(new SchedEventInstance(event));
		}
	}
	*/
	
	private VVEventInstance getSchedulerEventByUid(Connection con, String eventPublicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VVEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		return new VVEventInstance(ve);
	}
	
	/*
	public Integer getEventId(GetEventScope scope, boolean forceOriginal, String publicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			final String timeBasedPart = StringUtils.split(publicUid, ".", 2)[0];
			final String internetName = WT.getDomainInternetName(getTargetProfileId().getDomainId());
			
			con = WT.getConnection(SERVICE_ID);
			List<Integer> ids = null;
			
			if (scope.equals(GetEventScope.ALL)) {
				ids = edao.selectAliveIdsByPublicUid(con, publicUid);
				for(Integer id : ids) {
					if (!forceOriginal || publicUid.equals(ManagerUtils.buildEventUid(timeBasedPart, id, internetName))) {
						return id;
					}
				}
				
			} else {
				if (scope.equals(GetEventScope.PERSONAL) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
					ids = edao.selectAliveIdsByCalendarsPublicUid(con, listCalendarIds(), publicUid);
					for(Integer id : ids) {
						if (!forceOriginal || publicUid.equals(ManagerUtils.buildEventUid(timeBasedPart, id, internetName))) {
							return id;
						}
					}
				}
				if (scope.equals(GetEventScope.INCOMING) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
					ids = edao.selectAliveIdsByCalendarsPublicUid(con, listIncomingCalendarIds(), publicUid);
					for(Integer id : ids) {
						if (!forceOriginal || publicUid.equals(ManagerUtils.buildEventUid(timeBasedPart, id, internetName))) {
							return id;
						}
					}
				}
			}	
			
			return null;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	public String eventKeyByPublicUid(String eventPublicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			VVEventInstance ve = getSchedulerEventByUid(con, eventPublicUid);
			return (ve == null) ? null : EventKey.buildKey(ve.getEventId(), ve.getSeriesEventId());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Integer getEventId(String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doEventGetId(con, publicUid, null);
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event updateEvent(int eventId, String attendeeUid, EventAttendee.ResponseStatus responseStatus) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			Integer calendarId = evtDao.selectCalendarId(con, eventId);
			if (calendarId == null) throw new WTException("Calendar ID not found [{}]", eventId);
			ensureSysAdmin();
			
			int ret = attDao.updateAttendeeResponseByIdEvent(con, EnumUtils.toSerializedName(responseStatus), attendeeUid, eventId);
			if (ret == 1) {
				Event event = doEventGet(con, eventId, false, false);
				if (event == null) throw new WTException("Event not found [{}]", eventId);
				// Computes senderProfile: if available the calendarOnwer,
				// otherwise the targetProfile (in this case admin@domain)
				UserProfileId senderProfile = getCalendarOwner(event.getCalendarId());
				if (senderProfile == null) senderProfile = getTargetProfileId();
				
				notifyOrganizer(senderProfile, event, attendeeUid);
				return event;
				
			} else {
				return null;
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event updateEventFromSite(String publicUid, String attendeeUid, EventAttendee.ResponseStatus responseStatus) throws WTException {
		EventAttendeeDAO evtDao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		// TODO: permission check
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			Integer eventId = doEventGetId(con, publicUid, null);
			if (eventId == null) throw new WTException("Event ID lookup failed [{0}]", publicUid);
			
			int ret = evtDao.updateAttendeeResponseByIdEvent(con, EnumUtils.toSerializedName(responseStatus), attendeeUid, eventId);
			if (ret == 1) {
				Event event = doEventGet(con, eventId, false, false);
				if (event == null) throw new WTException("Event not found [{0}]", eventId);
				// Computes senderProfile: if available the calendarOnwer,
				// otherwise the targetProfile (in this case admin@domain)
				UserProfileId senderProfile = getCalendarOwner(event.getCalendarId());
				if (senderProfile == null) senderProfile = getTargetProfileId();
				
				notifyOrganizer(senderProfile, event, attendeeUid);
				return event;
			} else {
				return null;
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(int eventId) throws WTException {
		return getEvent(eventId, true, true);
	}
	
	public Event getEvent(int eventId, boolean processAttachments, boolean processTags) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Event event = doEventGet(con, eventId, processAttachments, processTags);
			if (event == null) return null;
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.FolderRight.READ);
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.getIsPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			return event;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(GetEventScope scope, String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Event event = doEventGet(con, scope, publicUid);
			if (event == null) return null;
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.getIsPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			return event;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventAttachmentWithBytes getEventAttachment(int eventId, String attachmentId) throws WTNotFoundException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttachmentDAO attDao = EventAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Map<Integer, Integer> map = evtDao.selectCalendarsByIds(con, Arrays.asList(eventId));
			if (!map.containsKey(eventId)) throw new WTNotFoundException("Event not found [{}]", eventId);
			checkRightsOnCalendar(map.get(eventId), FolderShare.FolderRight.READ);
			
			OEventAttachment oatt = attDao.selectByIdEvent(con, attachmentId, eventId);
			if (oatt == null) return null;
			
			OEventAttachmentData oattData = attDao.selectBytes(con, attachmentId);
			return ManagerUtils.fillEventAttachment(new EventAttachmentWithBytes(oattData.getBytes()), oatt);
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<String, CustomFieldValue> getEventCustomValues(int eventId) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer calId = evtDao.selectCalendarId(con, eventId);
			if (calId == null) return null;
			checkRightsOnCalendar(calId, FolderShare.FolderRight.READ);
			
			List<OEventCustomValue> ovals = cvalDao.selectByEvent(con, eventId);
			return ManagerUtils.createCustomValuesMap(ovals);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event addEvent(Event event) throws WTException {
		return addEvent(event, true);
	}
	
	@Override
	public Event addEvent(Event event, boolean notifyAttendees) throws WTException {
		return addEvent(event, null, notifyAttendees);
	}
	
	public Event addEvent(Event event, String iCalendarRawData, boolean notifyAttendees) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.ItemsRight.CREATE);
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			Set<String> validTags = coreMgr.listTagIds();
			EventInsertResult result = doEventInsert(con, event, iCalendarRawData, true, true, true, true, true, true, ProcessReminder.YES, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, result.oevent.getEventId(), null);
			}
			
			storeAsSuggestion(coreMgr, SUGGESTION_EVENT_TITLE, event.getTitle());
			if (!StringUtils.startsWithIgnoreCase(event.getLocation(), "http")) {
				storeAsSuggestion(coreMgr, SUGGESTION_EVENT_LOCATION, event.getLocation());
			}
			
			Event eventDump = getEvent(result.oevent.getEventId());
			
			// Notify last modification
			List<RecipientTuple> nmRcpts = getModificationRecipients(result.oevent.getCalendarId(), Crud.CREATE);
			if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.CREATE);
			
			// Notify attendees
			if (notifyAttendees) {
				List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.CREATE);
				if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.CREATE);
			}
			
			//TODO: IS THIS STILL NECESSARY????????????????????????????????????????????????????????????
			
			Event evt = ManagerUtils.createEvent(result.oevent);
			if (result.orecurrence != null) {
				evt.setRecurrence(result.orecurrence.getRule(), result.orecurrence.getLocalStartDate(evt.getDateTimeZone()), null);
			} else {
				evt.setRecurrence(null, null, null);
			}
			evt.setAttendees(ManagerUtils.createEventAttendeeList(result.oattendees));
			evt.setAttachments(ManagerUtils.createEventAttachmentList(result.oattachments));
			
			
			return evt;
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEvent(Event event, boolean processAttachments, boolean processTags, boolean processCustomValues, boolean notifyAttendees) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.ItemsRight.UPDATE);
			Set<String> validTags = processTags ? coreMgr.listTagIds() : null;
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			OEventInfo einfo = evtDao.selectOEventInfoById(con, event.getEventId());
			if (einfo == null) throw new WTException("Unable to retrieve event-info [{}]", event.getEventId());
			if (needsTreatAsPrivate(RunContext.getRunProfileId(), einfo)) {
				throw new AuthException("Event is private and therefore it cannot be updated [{}]", event.getEventId());
			}
			
			doEventMasterUpdateAndCommit(con, event, processAttachments, processTags, processCustomValues, notifyAttendees, validTags);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event handleInvitationFromICal(final net.fortuna.ical4j.model.Calendar ical, final Integer calendarId, final BitFlags<HandleICalInviationOption> options) throws WTParseException, WTNotFoundException, WTConstraintException, WTException {
		Check.notNull(ical, "ical");
		Check.notNull(options, "options");
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		final VEvent ve = ICalendarUtils.getVEvent(ical);
		if (ve == null) throw new WTParseException("Calendar object does not contain any events");
		final String uid = ICalendarUtils.getUidValue(ve);
		if (StringUtils.isBlank(uid)) throw new WTParseException("Event object does not provide a valid Uid");
		final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ve);
		if (iaOrganizer == null) throw new WTParseException("Event object does not provide a valid Organizer");
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			// By default the search scope is wide (personal + incoming) to 
			// allow for eg. to reply to an invitation even if a user is 
			// acting on behalf another: tipically mailbox and calendars are shared.
			final GetEventScope scope = options.has(HandleICalInviationOption.EVENT_LOOKUP_SCOPE_STRICT) ? GetEventScope.PERSONAL : GetEventScope.PERSONAL_AND_INCOMING;
			
			if (Method.REQUEST.equals(ical.getMethod())) { // A request has been received
				// Organizer -(invite)-> Attendee
				// The organizer sends a REQUEST to an attendee when creates a new 
				// appointment and also when makes some modifications to a placed one
				
				boolean defaultIsPrivate = false;
				boolean defaultBusy = true;
				if (calendarId != null) {
					VCalendarDefaults defaults = doCalendarGetDefaults(calendarId);
					if (defaults != null) {
						defaultBusy = defaults.getDefaultsBusy();
						defaultIsPrivate = defaults.getDefaultsPrivate();
					}
				}
				
				EventInput eventInput = new ICalendarInput(udata.getTimeZone())
					.withIgnoreClassification(options.has(HandleICalInviationOption.IGNORE_ICAL_CLASSIFICATION))
					.withIgnoreTransparency(options.has(HandleICalInviationOption.IGNORE_ICAL_TRASPARENCY))
					.withIgnoreAlarms(options.has(HandleICalInviationOption.IGNORE_ICAL_ALARMS))
					.withDefaultIsPrivate(defaultIsPrivate)
					.withDefaultBusy(defaultBusy)
					.fromVEvent(ve, null);
				Event event = doEventGet(con, scope, eventInput.event.getPublicUid());
				
				if (options.has(HandleICalInviationOption.CONSTRAIN_AVAILABILITY)) {
					// For now, availability is checked only against built-in calendar
					Integer builtinCalendarId = getBuiltInCalendarId();
					if (builtinCalendarId == null) throw new WTNotFoundException("Built-in calendar not found [{}]", getTargetProfileId());
					
					// Extract event bounds from parsed event
					final List<ComparableEventBounds> inputBounds;
					if (eventInput.event.hasRecurrence()) {
						inputBounds = calculateRecurringInstances(new EventBoundsRecurringContext(eventInput.event), null, null, 365);
					} else {
						inputBounds = Arrays.asList(new ComparableEventBounds(eventInput.event.getStartDate(), eventInput.event.getEndDate(), EventInstanceId.build(String.valueOf(0), null)));
					}
					
					// Add actual bound (if applicable) to exclusion list
					Set<DateTimeRange2> exclusions = null;
					if (event != null) {
						exclusions = new LinkedHashSet<>();
						if (event.hasRecurrence()) {
							exclusions.addAll(calculateRecurringInstances(new EventBoundsRecurringContext(event), null, null, 365));
						} else {
							exclusions.add(new ComparableEventBounds(event.getStartDate(), event.getEndDate(), EventInstanceId.build(String.valueOf(0), null)));
						}
					}
					
					DateTimeRange2 boundingRange = findBoundingRange(inputBounds);
					List<ComparableEventBounds> otherBounds = doListEventsBounds(con, Arrays.asList(builtinCalendarId), boundingRange.getStart(), boundingRange.getEnd(), udata.getTimeZone());
					if (areMyBoundsOverlappingWith(inputBounds, otherBounds, exclusions)) throw new WTConstraintException("Availability constraint cannot be satisfied");
				}
				
				if (event == null) { // New invitation
					Check.notNull(calendarId, "calendarId");
					if (!doListCalendarIdsIn(con, getTargetProfileId(), null).contains(calendarId)) {
						throw new WTException("Invitations must be inserted into personal calendar");
					}
					checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
					eventInput.event.setCalendarId(calendarId);
					
					return addEvent(eventInput.event, false);
					
				} else { // Invitation update
					checkRightsOnCalendar(event.getCalendarId(), FolderShare.ItemsRight.UPDATE);
					eventInput.event.setCalendarId(event.getCalendarId());
					
					//TODO: handle this as an option into doEventUpdate in order to instruct internal code to not update these field
					OEvent original = evtDao.selectById(con, event.getEventId());
					// Set into parsed all fields that can't be changed by the iCal
					// update otherwise data can be lost inside doEventUpdate
					eventInput.event.setEventId(original.getEventId());
					eventInput.event.setCalendarId(original.getCalendarId());
					eventInput.event.setReadOnly(original.getReadOnly());
					eventInput.event.setReminder(Event.Reminder.valueOf(original.getReminder()));
					eventInput.event.setEtag(original.getEtag());
					eventInput.event.setActivityId(original.getActivityId());
					eventInput.event.setMasterDataId(original.getMasterDataId());
					eventInput.event.setStatMasterDataId(original.getStatMasterDataId());
					eventInput.event.setCausalId(original.getCausalId());
					///////////////////////////////////////////////////////////

					doEventUpdate(con, original, eventInput.event, false, false, true, false, false, false, null);

					DbUtils.commitQuietly(con);
					if (isAuditEnabled()) {
						auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, original.getEventId(), null);
					}
					return null;
				}
				
			} else if (Method.REPLY.equals(ical.getMethod())) { // An invitation reply has been received
				// Attendee -(reply)-> Organizer
				// The attendee replied to an invitation, the organizer received 
				// a message with an attached iCalendar object, properly filled.
				// When preparing the reply, the iCalendar should be kept untouched 
				// except for the attendee list: it should contains only the 
				// references of the attendee that have actually replied to the invitation.
				
				// We expect to find only one attendee in the Event object
				Attendee att = ICalendarUtils.getAttendee(ve);
				if (att == null) throw new WTParseException("Event object does not provide any attendees");
				
				// Look for the event in personal and incoming calendars.
				// (i can be the organizer of a meeting created for my boss that 
				// share his calendar of me; all received replies must be bringed
				// back to the event in the shared calendar)
				Event evt = doEventGet(con, scope, uid);
				if (evt == null) throw new WTNotFoundException("Event not found [{}]", uid);
				
				// Extract response info...
				PartStat partStat = (PartStat)att.getParameter(Parameter.PARTSTAT);
				ICalendarInput.partStatToResponseStatus(partStat);
				
				List<String> updatedAttIds = doEventAttendeeUpdateResponseByRecipient(con, evt, att.getCalAddress().getSchemeSpecificPart(), ICalendarInput.partStatToResponseStatus(partStat));
				
				DbUtils.commitQuietly(con);
				
				// Commented to not send notification email in this case: 
				// the organizer already knows this info, he updated 'manually' the 
				// event by clicking the "Update event" button on the preview!
				/*
				if (!updatedAttIds.isEmpty()) {
					evt = getEvent(evt.getEventId());
					for(String attId : updatedAttIds) notifyOrganizer(getLocale(), evt, attId);
				}
				*/
				return null;
				
			} else if (Method.CANCEL.equals(ical.getMethod())) { // Cancellation has been received
				// Organizer -(cancelled invite)-> Attendee
				// The organizer after cancelling the event send a mail message
				// to all attendees telling to update their saved information.
				
				EventInput eventInput = new ICalendarInput(udata.getTimeZone())
					.withIncludeVEventSourceInOutput(true)
					.fromVEvent(ve, null);
				
				if (eventInput.isSourceEventCancelled()) {
					//if (eventInput.exRefersToPublicUid != null) {
						//TODO: add support to single event instance (of recurrence) cancellation
					//} else {
						Event evt = doEventGet(con, scope, uid);
						if (evt == null) throw new WTNotFoundException("Event not found [{}]", uid);
						
						doEventDelete(con, evt.getEventId(), true);
						
						DbUtils.commitQuietly(con);
						if (isAuditEnabled()) {
							auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, evt.getEventId(), null);
						}
					//}
				} else {
					// Nothing done
				}		
				return null;
				
			} else {
				throw new WTUnsupportedOperationException("Unsupported Calendar's method [{}]", ical.getMethod() != null ? ical.getMethod().toString() : null);
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private DateTimeRange2 findBoundingRange(Collection<? extends DateTimeRange2> ranges) {
		DateTime min = null, max = null;
		for (DateTimeRange2 range : ranges) {
			if (range.getStart() != null) {
				if (min == null || range.getStart().compareTo(min) < 0) {
					min = range.getStart();
				}
			}
			if (range.getEnd() != null) {
				if (max == null || range.getEnd().compareTo(max) > 0) {
					max = range.getEnd();
				}
			}
		}
		return DateTimeRange2.builder().withStart(min).withEnd(max).build();
	}
	
	private Set<ComparableEventBounds> findOverlappingBounds(Collection<ComparableEventBounds> bounds) {
		ArrayList<ComparableEventBounds> list = new ArrayList<>(bounds);
		LangUtils.quickSort(list);
		//https://massivealgorithms.blogspot.com/2015/06/given-n-appointments-find-all.html
		Set<ComparableEventBounds> overlapping = new HashSet<>();
		ComparableEventBounds latest = null;
        Iterator<ComparableEventBounds> iter = list.iterator();
        while (iter.hasNext()) {
            ComparableEventBounds next = iter.next();
            if ((latest != null) && (next.getStart().compareTo(latest.getEnd()) < 0)) {
				overlapping.add(next);
				overlapping.add(latest);
            }
            if ((latest == null) || (next.getEnd().compareTo(latest.getEnd()) > 0)) {
                latest = next;
            }
        }
		return overlapping;
	}
	
	private boolean areMyBoundsOverlappingWith(List<ComparableEventBounds> myBounds, List<ComparableEventBounds> withBounds, Set<? extends DateTimeRange2> matchExclusions) {
		List<ComparableEventBounds> list = new ArrayList<>(myBounds.size() + withBounds.size());
		list.addAll(myBounds);
		list.addAll(withBounds);
		list = LangUtils.quickSort(list);
		
		if (logger.isTraceEnabled()) {
			StringBuilder sb = null;
			
			sb = new StringBuilder();
			for (ComparableEventBounds ceb : myBounds) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    myBounds:{}", sb.toString());
			
			sb = new StringBuilder();
			for (ComparableEventBounds ceb : withBounds) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    withBounds:{}", sb.toString());
			
			if (matchExclusions != null) {
				sb = new StringBuilder();
				for (DateTimeRange2 ceb : matchExclusions) sb.append("\n").append("    (").append(ceb.toString()).append(")");
				logger.trace("[areMyBoundsOverlappingWith]\n    exclusions:{}", sb.toString());
			}	
			
			sb = new StringBuilder();
			for (ComparableEventBounds ceb : list) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    list (sorted):{}", sb.toString());
		}
		
		ComparableEventBounds latest = null;
        Iterator<ComparableEventBounds> iter = list.iterator();
        while (iter.hasNext()) {
            ComparableEventBounds next = iter.next();
			if (matchExclusions != null && matchExclusions.contains(next)) {
				if (logger.isTraceEnabled()) logger.trace("[areMyBoundsOverlappingWith]\n    '({})' IGNORED", next.toString());
				continue;
			}
            if ((latest != null) && (next.getStart().compareTo(latest.getEnd()) < 0)) {
				if (logger.isTraceEnabled()) logger.trace("[areMyBoundsOverlappingWith]\n    '({})' HAS COLLISION", next.toString());
				return true;
				/*
				if (myBounds.contains(next)) {
					logger.trace("[areMyBoundsOverlappingWith]\n    collision  '({})'", next.toString());
					return true;
				}
				if (myBounds.contains(latest)) {
					logger.trace("[areMyBoundsOverlappingWith] '{}' (latest) in myBounds", latest.toString());
					return true;
				}
				*/
				//if (myBounds.contains(next) || myBounds.contains(latest)) return true;
            }
            if ((latest == null) || (next.getEnd().compareTo(latest.getEnd()) > 0)) {
                latest = next;
				if (logger.isTraceEnabled()) logger.trace("[areMyBoundsOverlappingWith]\n    '({})' HAS LATEST END", next.toString());
            }
        }
		return false;
	}
	
	/**
	 * @deprecated use handleInvitationFromICal instead
	 */
	@Deprecated
	@Override
	public Event addEventFromICal(int calendarId, net.fortuna.ical4j.model.Calendar ical) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		ArrayList<EventInput> parsed = new ICalendarInput(udata.getTimeZone()).fromICalendarFile(ical, null);
		if (parsed.isEmpty()) throw new WTException("iCal must contain at least one event");
		
		Event event = parsed.get(0).event;
		event.setCalendarId(calendarId);
		return addEvent(event, false);
	}
	
	@Override
	public void updateEventFromICal(net.fortuna.ical4j.model.Calendar ical) throws WTException {
		Connection con = null;
		
		VEvent ve = ICalendarUtils.getVEvent(ical);
		if (ve == null) throw new WTException("iCalendar object does not contain any events");
		String uid = ICalendarUtils.getUidValue(ve);
		if (StringUtils.isBlank(uid)) throw new WTException("Event does not provide a valid Uid");
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			if (Method.REQUEST.equals(ical.getMethod())) {
				// Organizer -> Attendee
				// The organizer after updating the event details send a mail message
				// to all attendees telling to update their saved information

				// Gets the event...
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{}]", uid);

				EventDAO edao = EventDAO.getInstance();
				final UserProfile.Data udata = WT.getUserData(getTargetProfileId());

				// Parse the ical using the code used in import
				ArrayList<EventInput> parsed = new ICalendarInput(udata.getTimeZone()).fromICalendarFile(ical, null);
				if (parsed.isEmpty()) throw new WTException("iCal must contain at least one event");
				Event parsedEvent = parsed.get(0).event;
				parsedEvent.setCalendarId(evt.getCalendarId());

				checkRightsOnCalendar(evt.getCalendarId(), FolderShare.ItemsRight.UPDATE);

				OEvent original = edao.selectById(con, evt.getEventId());

				// Set into parsed all fields that can't be changed by the iCal
				// update otherwise data can be lost inside doEventUpdate
				parsedEvent.setEventId(original.getEventId());
				parsedEvent.setCalendarId(original.getCalendarId());
				parsedEvent.setReadOnly(original.getReadOnly());
				parsedEvent.setReminder(Event.Reminder.valueOf(original.getReminder()));
				parsedEvent.setEtag(original.getEtag());
				parsedEvent.setActivityId(original.getActivityId());
				parsedEvent.setMasterDataId(original.getMasterDataId());
				parsedEvent.setStatMasterDataId(original.getStatMasterDataId());
				parsedEvent.setCausalId(original.getCausalId());

				doEventUpdate(con, original, parsedEvent, false, false, true, false, false, false, null);
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, original.getEventId(), null);
				}
				
			} else if (Method.REPLY.equals(ical.getMethod())) {
				// Attendee -> Organizer
				// The attendee replies to an event sending to the organizer a mail 
				// message of an attached ical (the above param), properly configured.
				// The ical should be kept untouched in the reply except for the 
				// attendee list: it should contains only the references of the 
				// attendee that replies to the invitation.

				Attendee att = ICalendarUtils.getAttendee(ve);
				if (att == null) throw new WTException("Event does not provide any attendees");

				// Gets the event looking also into incoming calendars...
				// (i can be the organizer of a meeting created for my boss that 
				// share his calendar of me; all received replies must be bringed
				// back to the event in the shared calendar)
				//Event evt = getEvent(uid);
				// Previous impl. forced (forceOriginal == true)
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{}]", uid);
				
				// Extract response info...
				PartStat partStat = (PartStat)att.getParameter(Parameter.PARTSTAT);
				ICalendarInput inWithDummyTz = new ICalendarInput(DateTimeZone.UTC);
				
				List<String> updatedAttIds = doEventAttendeeUpdateResponseByRecipient(con, evt, att.getCalAddress().getSchemeSpecificPart(), inWithDummyTz.partStatToResponseStatus(partStat));
				//List<String> updatedAttIds = updateEventAttendeeResponseByRecipient(evt, att.getCalAddress().getSchemeSpecificPart(), responseStatus);
				
				DbUtils.commitQuietly(con);
				
				// Commented to not send notification email in this case: 
				// the organizer already knows this info, he updated 'manually' the 
				// event by clicking the "Update event" button on the preview!
				/*
				if (!updatedAttIds.isEmpty()) {
					evt = getEvent(evt.getEventId());
					for(String attId : updatedAttIds) notifyOrganizer(getLocale(), evt, attId);
				}
				*/
				
			} else if (Method.CANCEL.equals(ical.getMethod())) {
				// Organizer -> Attendee
				// The organizer after cancelling the event send a mail message
				// to all attendees telling to update their saved information

				// Gets the event...
				//Event evt = getEventForICalUpdate(uid);
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{}]", uid);
				
				doEventDelete(con, evt.getEventId(), true);
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, evt.getEventId(), null);
				}
				
			} else {
				throw new WTException("Unsupported Calendar's method [{}]", ical.getMethod() != null ? ical.getMethod().toString() : null);
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(int eventId, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer calendarId = evtDao.selectCalendarId(con, eventId);
			if (calendarId == null) throw new WTException("Unable to retrieve event [{}]", eventId);
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceDeleteAndCommit(con, UpdateEventTarget.ALL_SERIES, new EventKey(eventId), notifyAttendees, false);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEvent(String publicUid, Integer calendarId, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer eventId = doEventGetId(con, publicUid, Arrays.asList(calendarId));
			if (eventId == null) throw new WTException("Event ID lookup failed [{0}]", publicUid);
			
			Integer targetCalendarId = null;
			if (calendarId == null) {
				targetCalendarId = evtDao.selectCalendarId(con, eventId);
				if (targetCalendarId == null) throw new WTException("Unable to retrieve event [{}]", eventId);
			} else {
				targetCalendarId = calendarId;
			}
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			
			String provider = calDao.selectProviderById(con, targetCalendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", targetCalendarId);
			
			doEventInstanceDeleteAndCommit(con, UpdateEventTarget.ALL_SERIES, new EventKey(eventId), notifyAttendees, false);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<String> doEventAttendeeUpdateResponseByRecipient(Connection con, Event event, String recipient, EventAttendee.ResponseStatus responseStatus) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();

		// Find matching attendees
		ArrayList<String> matchingIds = new ArrayList<>();
		List<OEventAttendee> atts = attDao.selectByEvent(con, event.getEventId());
		for (OEventAttendee att : atts) {
			final InternetAddress ia = InternetAddressUtils.toInternetAddress(att.getRecipient());
			if (ia == null) continue;
			if (StringUtils.equalsIgnoreCase(ia.getAddress(), recipient)) matchingIds.add(att.getAttendeeId());
		}
		
		// Update responses
		int ret = attDao.updateAttendeeResponseByIds(con, EnumUtils.toSerializedName(responseStatus), matchingIds);
		evtDao.updateRevision(con, event.getEventId(), BaseDAO.createRevisionTimestamp());
		if (matchingIds.size() == ret) {
			return matchingIds;
		} else {
			throw new WTException("# of attendees to update don't match the uptated ones");
		}
	}
	
	@Override
	public String getEventInstanceKey(int eventId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OEvent oevt = edao.selectById(con, eventId);
			if(oevt == null) return null;
			
			//TODO: completare implementazione differenziando eventi singoli, broken ed istanze ricorrenti...
			return EventKey.buildKey(oevt.getEventId(), oevt.getEventId());
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventInstance getEventInstance(String eventKey) throws WTException {
		return getEventInstance(new EventKey(eventKey), true, true, true);
	}
	
	public EventInstance getEventInstance(EventKey key, boolean processAttachments, boolean processTags, boolean processCustomValues) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			EventInstance instance = doEventInstanceGet(con, key.eventId, key.instanceDate, processAttachments, processTags, processCustomValues);
			if (instance == null) return null;
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), instance.getIsPrivate(), instance.getCalendarId());
			if (keepPrivate) instance.censorize();
			
			return instance;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventInstance event, boolean processAttachments, boolean notifyAttendees) throws WTException {
		updateEventInstance(target, event, processAttachments, notifyAttendees, false, false);
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventInstance event, boolean processAttachments, boolean processTags, boolean processCustomValues, boolean notifyAttendees) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer calendarId = evtDao.selectCalendarId(con, event.getEventId());
			if (calendarId == null) throw new WTException("Unable to retrieve event [{}]", event.getEventId());
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			OEventInfo einfo = evtDao.selectOEventInfoById(con, event.getEventId());
			if (einfo == null) throw new WTException("Unable to retrieve event-info [{}]", event.getEventId());
			if (needsTreatAsPrivate(RunContext.getRunProfileId(), einfo)) {
				throw new AuthException("Event is private and therefore it cannot be updated [{}]", event.getEventId());
			}
			
			Set<String> validTags = processTags ? coreMgr.listTagIds() : null;
			
			// TODO: avoid this!!!
			EventKey eventKey = new EventKey(event.getKey());
			doEventInstanceUpdateAndCommit(con, einfo, target, eventKey, event, processAttachments, processTags, processCustomValues, notifyAttendees, validTags);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventKey key, DateTime newStart, DateTime newEnd, String newTitle, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			OEventInfo einfo = evtDao.selectOEventInfoById(con, key.eventId);
			if (einfo == null) throw new WTException("Unable to retrieve event-info [{}]", key.eventId);
			
			checkRightsOnCalendar(einfo.getCalendarId(), FolderShare.ItemsRight.UPDATE);
			String provider = calDao.selectProviderById(con, einfo.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", einfo.getCalendarId());
			
			if (needsTreatAsPrivate(RunContext.getRunProfileId(), einfo)) {
				throw new AuthException("Event is private and therefore it cannot be updated [{}]", einfo.getEventId());
			}
			
			EventInstance ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true, true, true);
			if ((newStart != null) && (newEnd != null)) {
				ei.setStartDate(newStart);
				ei.setEndDate(newEnd);
			} else if (newStart != null) {
				Duration length = new Duration(ei.getStartDate(), ei.getEndDate());
				ei.setStartDate(newStart);
				ei.setEndDate(newStart.plus(length));
			} else if (newEnd != null) {
				Duration length = new Duration(ei.getStartDate(), ei.getEndDate());
				ei.setStartDate(newEnd.minus(length));
				ei.setEndDate(newEnd);
			}
			if (newTitle != null) {
				ei.setTitle(newTitle);
			}
			
			ei.ensureCoherence();
			doEventInstanceUpdateAndCommit(con, einfo, target, key, ei, true, true, true, notifyAttendees, null);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	public void updateEventInstance(String eventKey, DateTime startDate, DateTime endDate, String title, boolean notifyAttendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "UPDATE");
			
			if (vevt.isEventRecurring()) {
				//TODO: add support to recurring events
				throw new WTException("Not supported on recurring events [{}]", ekey.eventId);
			} else {
				// 1 - Updates event's dates/times (+revision)
				OEvent oevt = evtDao.selectById(con, ekey.eventId);
				if (oevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
				
				oevt.setStartDate(startDate);
				oevt.setEndDate(endDate);
				oevt.setTitle(title);
				oevt.ensureCoherence();
				evtDao.update(con, oevt, BaseDAO.createRevisionTimestamp(), oevt.getStartDate().isAfterNow());
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(oevt.getEventId()));
				
				EventInstance eventDump = getEventInstance(eventKey);
				
				// Notify last modification
				List<RecipientTuple> nmRcpts = getModificationRecipients(oevt.getCalendarId(), Crud.UPDATE);
				if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.UPDATE);
				
				// Notify attendees
				if (notifyAttendees) {
					List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.UPDATE);
					if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.UPDATE);
				}
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public void deleteEventInstance(UpdateEventTarget target, String eventKey, boolean notifyAttendees) throws WTException {
		deleteEventInstance(target, new EventKey(eventKey), notifyAttendees);
	}
	
	@Override
	public void deleteEventInstance(UpdateEventTarget target, EventKey key, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer calendarId = evtDao.selectCalendarId(con, key.eventId);
			if (calendarId == null) throw new WTException("Unable to retrieve event [{}]", key.eventId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceDeleteAndCommit(con, target, key, notifyAttendees, true);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void restoreEventInstance(EventKey key) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer calendarId = evtDao.selectCalendarId(con, key.eventId);
			if (calendarId == null) throw new WTException("Unable to retrieve event [{}]", key.eventId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceRestoreAndCommit(con, key, true);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void afterEventOperation(Event event, String crud, final boolean notifyAttendees) throws WTException {
		afterEventOperation(Collections.singletonMap(event.getEventId(), crud), notifyAttendees, Collections.singletonMap(event.getEventId(), event));
	}
	
	private void afterEventOperation(int eventId, String crud, final boolean notifyAttendees) throws WTException {
		afterEventOperation(Collections.singletonMap(eventId, crud), notifyAttendees, null);
	}
	
	private void afterEventOperation(final Map<Integer, String> operations, final boolean notifyAttendees, final Map<Integer, Event> eventCache) throws WTException {
		Map<Integer, Integer> eventCalendarMap = mapCalendarByEvent(operations.keySet());
		
		for (Map.Entry<Integer, String> entry : operations.entrySet()) {
			int eventId = entry.getKey();
			String crud = entry.getValue();
			
			int calendarId = eventCalendarMap.get(eventId);
			Event eventDump = (eventCache != null) ? eventCache.get(eventId) : null;
			
			// Notify last modification
			List<RecipientTuple> nmRcpts = getModificationRecipients(calendarId, crud);
			if (!nmRcpts.isEmpty()) {
				if (eventDump == null) eventDump = getEvent(eventId);
				notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), crud);
			}
			
			// Notify attendees
			if (notifyAttendees) {
				List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, crud);
				if (!attRcpts.isEmpty()) {
					if (eventDump == null) eventDump = getEvent(eventId);
					notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, crud);
				}
			}
		}
	}
	
	public void moveEvent(boolean copy, Collection<Integer> eventIds, int targetCalendarId, boolean notifyAttendees) throws WTNotFoundException, WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(targetCalendarId, FolderShare.ItemsRight.CREATE);
			
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, targetCalendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", targetCalendarId);
			
			Set<Integer> readOkCache = new HashSet<>();
			Set<Integer> deleteOkCache = new HashSet<>();
			Map<Integer, Integer> map = mapCalendarByEvent(eventIds);
			Map<Integer, String> operations = new LinkedHashMap<>();
			ArrayList<AuditReferenceDataEntry> copied = new ArrayList<>();
			ArrayList<AuditReferenceDataEntry> moved = new ArrayList<>();
			for (Integer eventId : eventIds) {
				if (eventId == null) continue;
				if (!map.containsKey(eventId)) throw new WTNotFoundException("Event not found [{}]", eventId);
				int calendarId = map.get(eventId);
				checkRightsOnCalendar(readOkCache, calendarId, FolderShare.FolderRight.READ);
				
				if (copy || (targetCalendarId != calendarId)) {
					if (copy) {
						Event origEvent = doEventGet(con, eventId, false, true);
						if (origEvent == null) throw new WTNotFoundException("Event not found [{}]", eventId);
						
						boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), origEvent.getIsPrivate(), origEvent.getCalendarId());
						if (keepPrivate) origEvent.censorize();
						EventInsertResult result = doEventCopy(con, origEvent, targetCalendarId);
						
						copied.add(new AuditEventCopy(result.oevent.getEventId(), origEvent.getEventId()));
						operations.put(result.oevent.getEventId(), Crud.CREATE);
						
					} else {
						checkRightsOnCalendar(deleteOkCache, calendarId, FolderShare.ItemsRight.DELETE);
						boolean ret = doEventMove(con, eventId, targetCalendarId);
						if (!ret) throw new WTNotFoundException("Event not found [{}]", eventId);
						
						moved.add(new AuditEventMove(eventId, calendarId));
						operations.put(eventId, Crud.CREATE);
						// There is no need to notify attendees, the event is simply moved from a calendar to another!
					}	
				}
			}
			DbUtils.commitQuietly(con);
			afterEventOperation(operations, notifyAttendees ? copy : false, null);
			if (isAuditEnabled()) {
				if (copy) {
					auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, copied);
				} else {
					auditLogWrite(AuditContext.EVENT, AuditAction.MOVE, moved);
				}
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void moveEventInstance(EventKey key, int targetCalendarId) throws WTNotFoundException, WTException {
		moveEvent(false, Arrays.asList(key.eventId), targetCalendarId, false);
	}
	
	/*
	@Override
	public void moveEventInstance(EventKey key, int targetCalendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		EventInstance ei = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true, true);
			checkRightsOnCalendar(ei.getCalendarId(), FolderShare.FolderRight.READ);
			
			if (targetCalendarId != ei.getCalendarId()) {
				checkRightsOnCalendar(ei.getCalendarId(), FolderShare.ItemsRight.DELETE);
				String provider = calDao.selectProviderById(con, targetCalendarId);
				if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", targetCalendarId);
				
				doEventMove(con, false, ei, targetCalendarId);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(ei.getEventId()));
				
				// Notify last modification
				List<RecipientTuple> nmRcpts = getModificationRecipients(ei.getCalendarId(), Crud.DELETE);
				if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, ei.getFootprint(), Crud.DELETE);
				
				// There is no need to notify attendees, the event is simply moved from a calendar to another!
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public Event cloneEventInstance(EventKey key, Integer newCalendarId, DateTime newStart, DateTime newEnd, boolean notifyAttendees) throws WTException {
		Connection con = null;
		EventInstance ei = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true, true, true);
			checkRightsOnCalendar(ei.getCalendarId(), FolderShare.FolderRight.READ);
			int calendarId = (newCalendarId != null) ? newCalendarId : ei.getCalendarId();
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), ei.getIsPrivate(), ei.getCalendarId());
			if (keepPrivate) ei.censorize();
			
			ei.setCalendarId(calendarId);
			ei.setPublicUid(null); // Reset value in order to make inner function generate new one!
			ei.setHref(null); // Reset value in order to make inner function generate new one!
			if ((newStart != null) && (newEnd != null)) {
				ei.setStartDate(newStart);
				ei.setEndDate(newEnd);
			} else if (newStart != null) {
				Duration length = new Duration(ei.getStartDate(), ei.getEndDate());
				ei.setStartDate(newStart);
				ei.setEndDate(newStart.plus(length));
			} else if (newEnd != null) {
				Duration length = new Duration(ei.getStartDate(), ei.getEndDate());
				ei.setStartDate(newEnd.minus(length));
				ei.setEndDate(newEnd);
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return addEvent(ei, notifyAttendees);
	}
	
	@Override
	public void updateEventCalendarTags(UpdateTagsOperation operation, int calendarId, Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		EventTagDAO etagDao = EventTagDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			List<String> auditTags = new ArrayList<>();
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				Set<String> validTags = coreMgr.listTagIds();
				List<String> okTagIds = tagIds.stream()
					.filter(tagId -> validTags.contains(tagId))
					.collect(Collectors.toList());
				
				con = WT.getConnection(SERVICE_ID, false);
				if (UpdateTagsOperation.RESET.equals(operation)) etagDao.deleteByCalendar(con, calendarId);
				for (String tagId : okTagIds) {
					etagDao.insertByCalendar(con, calendarId, tagId);
				}
				if (UpdateTagsOperation.SET.equals(operation)) auditTags.addAll(okTagIds);
				
			} else if (UpdateTagsOperation.UNSET.equals(operation)) {
				con = WT.getConnection(SERVICE_ID, false);
				etagDao.deleteByCalendarTags(con, calendarId, tagIds);
				auditTags.addAll(tagIds);
			}
			
			DbUtils.commitQuietly(con);
			
			if (isAuditEnabled() && !auditTags.isEmpty()) {
				String tagAction = UpdateTagsOperation.SET.equals(operation) ? "set" : "unset";
				HashMap<String, List<String>> audit = new HashMap<>();
				audit.put(tagAction, auditTags);
				
				auditLogWrite(
					AuditContext.CALENDAR,
					AuditAction.TAG,
					calendarId,
					JsonResult.gson().toJson(audit)
				);
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventTags(UpdateTagsOperation operation, Collection<Integer> eventIds, Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		EventTagDAO etagDao = EventTagDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = listAllCalendarIds().stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE))
				.collect(Collectors.toList());
			Set<String> validTags = coreMgr.listTagIds();
			List<String> okTagIds = tagIds.stream()
				.filter(tagId -> validTags.contains(tagId))
				.collect(Collectors.toList());
			
			List<String> auditTags = new ArrayList<>();
			Map<Integer, List<String>> auditOldTags = null;
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				con = WT.getConnection(SERVICE_ID, false);
				if (UpdateTagsOperation.RESET.equals(operation)) {
					if (isAuditEnabled()) {
						auditOldTags = etagDao.selectTagsByEvent(con, eventIds);
					}
					etagDao.deleteByCalendarsEvents(con, okCalendarIds, eventIds);
				} else {
					if (isAuditEnabled()) auditTags.addAll(okTagIds);
				}
				for (String tagId : okTagIds) {
					etagDao.insertByCalendarsEvents(con, okCalendarIds, eventIds, tagId);
				}
				
			} else if (UpdateTagsOperation.UNSET.equals(operation)) {
				con = WT.getConnection(SERVICE_ID, false);
				etagDao.deleteByCalendarsEventsTags(con, okCalendarIds, eventIds, okTagIds);
				if (isAuditEnabled()) auditTags.addAll(okTagIds);
			}
			
			DbUtils.commitQuietly(con);
			
			if (isAuditEnabled()) {
				String tagAction = UpdateTagsOperation.SET.equals(operation) ? "set" : "unset";
				AuditLogManager.Batch auditBatch = auditLogGetBatch(AuditContext.EVENT, AuditAction.TAG);
				if (auditBatch != null) {
					if (UpdateTagsOperation.RESET.equals(operation)) {
						for (int eventId : eventIds) {
							HashMap<String, List<String>> data = coreMgr.compareTags(new ArrayList<>(auditOldTags.get(eventId)), new ArrayList<>(okTagIds));
							auditBatch.write(
								eventId,
								JsonResult.gson().toJson(data)
							);
						}
						auditBatch.flush();

					} else {
						if (!auditTags.isEmpty()) {
							for (int eventId : eventIds) {
								HashMap<String, List<String>> data = new HashMap<>();
								data.put(tagAction, auditTags);
								auditBatch.write(
									eventId,
									JsonResult.gson().toJson(data)
								);
							}
							auditBatch.flush();
						}
					}
				}
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, DateTime> generateTimeSpans(final LocalDate fromDate, final LocalDate toDate, final TimeRange timeRange, final boolean applyTimeRangeForEachDay, final DateTimeZone timezone, final int minuteResolution) {
		LinkedHashMap<String, DateTime> spans = new LinkedHashMap<>();
		DateTimeFormatter ymdhmZoneFmt = DateTimeUtils.createYmdHmFormatter(timezone);
		
		DateTime boundaryInstant = new DateTime(timezone).withDate(toDate.plusDays(1)).withTimeAtStartOfDay();
		DateTime instant = new DateTime(timezone).withDate(fromDate).withTimeAtStartOfDay();
		while (instant.compareTo(boundaryInstant) < 0) {
			boolean add = false;
			if (timeRange != null) {
				final LocalTime time = instant.toLocalTime();
				if (applyTimeRangeForEachDay) {
					add = DateTimeUtils.between(time, timeRange.getStart(), timeRange.getEnd());
				} else {
					final LocalDate date = instant.toLocalDate();
					if (date.isEqual(fromDate) && date.isEqual(toDate)) {
						add = DateTimeUtils.between(time, timeRange.getStart(), timeRange.getEnd());
					} else if (date.isEqual(fromDate)) {
						add = (time.compareTo(timeRange.getStart()) >= 0);
					} else if (date.isEqual(toDate)) {
						add = (time.compareTo(timeRange.getEnd()) < 0);
					} else {
						add = true;
					}
				}	
			} else {
				add = true;
			}
			
			if (add) spans.put(ymdhmZoneFmt.print(instant), instant);
			instant = instant.plusMinutes(minuteResolution);
		}
		return spans;
	}
	
	public Map<String, DateTime> calculateTransparencyTimeSpans(final UserProfileId profileId, final boolean busy, final LocalDate fromDate, final LocalDate toDate, final TimeRange timeRange, final DateTimeZone timezone, final int minuteResolution) throws WTException {
		Check.stateIsTrue(!(minuteResolution < 1 || 60 % minuteResolution != 0), "minuteResolution must be a divider of 60");
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		LinkedHashMap<String, DateTime> spans = new LinkedHashMap<>();
		try {
			DateTime from = new DateTime(timezone).withDate(fromDate).withTimeAtStartOfDay();
			DateTime to = new DateTime(timezone).withDate(toDate).withTimeAtStartOfDay();
			
			// We do NOT have to define two EPVs here: we are interested to a 
			// condition (busy or not) that not involved dates fields' changes 
			// between recurring and simple events.
			EventPredicateVisitor epv = new EventPredicateVisitor(EventPredicateVisitor.Target.NORMAL)
				.withIgnoreCase(true)
				.withForceStringLikeComparison(true);
			Condition<EventQuery> conditionPredicate = busy ? new EventQuery().isBusy().isTrue() : new EventQuery().isBusy().isFalse();
			
			con = WT.getConnection(SERVICE_ID);
			int noOfRecurringInst = 365+2;
			final Set<Integer> calendarIds = doListCalendarIdsIn(con, profileId, null);
			for (VVEvent vevt : evtDao.viewByCalendarRangeCondition(con, calendarIds, from, to, BaseDAO.createCondition(conditionPredicate, epv))) {
				SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), vevt);
				
				final DateTime rouStart = DateTimeUtils.roundToNearestMinute(item.getStartDate().withZone(timezone), minuteResolution);
				final DateTime rouEnd = DateTimeUtils.roundToNearestMinute(item.getEndDate().withZone(timezone), minuteResolution);
				final TimeRange rouTimeRange = TimeRange.builder().with(rouStart.toLocalTime(), rouEnd.toLocalTime()).build();
				spans.putAll(generateTimeSpans(rouStart.toLocalDate(), rouEnd.toLocalDate(), rouTimeRange, false, timezone, minuteResolution));
			}
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, doListCalendarIdsIn(con, profileId, null), from, to, BaseDAO.createCondition(conditionPredicate, epv))) {
				for (SchedEventInstance item : calculateRecurringInstances_OLD(con, new SchedEventInstanceMapper(vevt, false), from, to, timezone, noOfRecurringInst)) {
					final DateTime rouStart = DateTimeUtils.roundToNearestMinute(item.getStartDate().withZone(timezone), minuteResolution);
					final DateTime rouEnd = DateTimeUtils.roundToNearestMinute(item.getEndDate().withZone(timezone), minuteResolution);
					final TimeRange rouTimeRange = TimeRange.builder().with(rouStart.toLocalTime(), rouEnd.toLocalTime()).build();
					spans.putAll(generateTimeSpans(rouStart.toLocalDate(), rouEnd.toLocalDate(), rouTimeRange, false, timezone, minuteResolution));
				}
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return spans;
	}
	
	public List<EventAttendee> listEventAttendees(int eventId, boolean notifiedOnly) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return getEventAttendees(con, eventId, notifiedOnly);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<EventAttendee> getEventAttendees(Connection con, int eventId, boolean notifiedOnly) throws WTException {
		List<OEventAttendee> attendees = null;
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		if(notifiedOnly) {
			attendees = eadao.selectByEventNotify(con, eventId, true);
		} else {
			attendees = eadao.selectByEvent(con, eventId);
		}
		return ManagerUtils.createEventAttendeeList(attendees);
	}
	
	public LogEntries importEvents(int calendarId, EventFileReader rea, File file, String mode) throws WTException {
		LogEntries log = new LogEntries();
		HashMap<String, OEvent> uidMap = new HashMap<>();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
			if (mode.equals("copy")) checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Started at {0}", new DateTime()));
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Reading source file..."));
			
			ArrayList<EventInput> input = null;
			try {
				input = rea.listEvents(log, file);
			} catch(IOException | UnsupportedOperationException ex) {
				log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to complete reading. Reason: {0}", ex.getMessage()));
				throw new WTException(ex);
			}
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s found!", input.size()));
			
			con = WT.getConnection(SERVICE_ID, false);
			
			if(mode.equals("copy")) {
				log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Cleaning previous events..."));
				int del = doEventsDeleteByCalendar(con, calendarId, false);
				log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s deleted!", del));
				DbUtils.commitQuietly(con);
			}
			
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Importing..."));
			int count = 0;
			for(EventInput ei : input) {
				ei.event.setCalendarId(calendarId);
				try {
					doEventInputInsert(con, uidMap, ei, ProcessReminder.DISARM_PAST);
					DbUtils.commitQuietly(con);
					count++;
				} catch(Exception ex) {
					logger.trace("Error inserting event", ex);
					DbUtils.rollbackQuietly(con);
					log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", ei.event.getTitle(), ei.event.getPublicUid(), ex.getMessage()));
				}
			}
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s imported!", count));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Ended at {0}", new DateTime()));
		}
		return log;
	}
	
	public void exportEvents(LogEntries log, DateTime fromDate, DateTime toDate, OutputStream os) throws Exception {
		Connection con = null, ccon = null;
		ICsvMapWriter mapw = null;
		EventDAO edao = EventDAO.getInstance();
		
		try {
			//TODO: Gestire campi visit_id e action_id provenienti dal servizio DRM
			final String dateFmt = "yyyy-MM-dd";
			final String timeFmt = "HH:mm:ss";
			final String[] headers = new String[]{
				"eventId", 
				"userId", "userDescription", 
				"startDate", "startTime", "endDate", "endTime", 
				"timezone", "duration", 
				"title", "description", 
				"activityId", "activityDescription", "activityExternalId", 
				"causalId", "causalDescription", "causalExternalId", 
				"masterDataId", "masterDataDescription"
			};
			final CellProcessor[] processors = new CellProcessor[]{
				new NotNull(), 
				new NotNull(), null, 
				new FmtDateTime(dateFmt), new FmtDateTime(timeFmt), new FmtDateTime(dateFmt), new FmtDateTime(timeFmt), 
				new NotNull(), new NotNull(),
				new NotNull(), null, 
				null, null, null, 
				null, null, null, 
				null, null
			};
			
			CsvPreference pref = new CsvPreference.Builder('"', ';', "\n").build();
			mapw = new CsvMapWriter(new OutputStreamWriter(os), pref);
			mapw.writeHeader(headers);
			
			CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
			con = WT.getConnection(SERVICE_ID);
			ccon = WT.getCoreConnection();
			
			final UserProfileId pid = getTargetProfileId();
			final UserProfile.Data udata = WT.getUserData(pid);
			HashMap<String, Object> map = null;
			for (Integer calendarId : listMyCalendarIds()) {
				for (VVEvent ve : edao.viewByCalendarRangeCondition(con, calendarId, fromDate, toDate, null)) {
					final VVEventInstance vei = new VVEventInstance(ve);
					try {
						map = new HashMap<>();
						map.put("userId", pid.getUserId());
						map.put("descriptionId", udata.getDisplayName());
						fillExportMapBasic(map, coreMgr, con, vei);
						fillExportMapDates(map, udata.getTimeZone(), vei);
						mapw.write(map, headers, processors);
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Event skipped [{0}]. Reason: {1}", ve.getEventId(), ex.getMessage()));
					}
				}
				for(VVEvent ve : edao.viewRecurringByCalendarRangeCondition(con, calendarId, fromDate, toDate, null)) {
					final List<VVEventInstance> instances = calculateRecurringInstances_OLD(con, new VVEventInstanceMapper(ve), fromDate, toDate, udata.getTimeZone());
					
					try {
						map = new HashMap<>();
						map.put("userId", pid.getUserId());
						map.put("descriptionId", udata.getDisplayName());
						fillExportMapBasic(map, coreMgr, con, ve);
						for (VVEventInstance vei : instances) {
							fillExportMapDates(map, udata.getTimeZone(), vei);
							mapw.write(map, headers, processors);
						}	
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Event skipped [{0}]. Reason: {1}", ve.getEventId(), ex.getMessage()));
					}
				}
			}
			mapw.flush();
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
			DbUtils.closeQuietly(ccon);
			try { if(mapw != null) mapw.close(); } catch(Exception ex) { /* Do nothing... */ }
		}
	}
	
	public void eraseData(boolean deep) throws WTException {
		CalendarDAO caldao = CalendarDAO.getInstance();
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		EventDAO evtdao = EventDAO.getInstance();
		EventAttendeeDAO attdao = EventAttendeeDAO.getInstance();
		RecurrenceDAO recdao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO recbrkdao = RecurrenceBrokenDAO.getInstance();
		Connection con = null;
		
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			UserProfileId pid = getTargetProfileId();
			
			// Erase events and related tables
			if (deep) {
				for (OCalendar ocal : caldao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					attdao.deleteByCalendar(con, ocal.getCalendarId());
					recdao.deleteByCalendar(con, ocal.getCalendarId());
					recbrkdao.deleteByCalendar(con, ocal.getCalendarId());
					evtdao.deleteByCalendar(con, ocal.getCalendarId());
				}
			} else {
				DateTime revTs = BaseDAO.createRevisionTimestamp();
				for (OCalendar ocal : caldao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					evtdao.logicDeleteByCalendar(con, ocal.getCalendarId(), revTs);
				}
			}
			
			// Erase calendars
			psetDao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			caldao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		ensureSysAdmin();
		
		try {
			final boolean shouldLog = logger.isDebugEnabled();
			final DateTime from = now.withTimeAtStartOfDay();
			final DateTime to = from.plusDays(7*2+1);
			con = WT.getConnection(SERVICE_ID, false);
			if (shouldLog) logger.debug("Retrieving expired event instances... [{} -> {}]", from, to);
			
			List<VExpEventInstance> instances = doEventGetExpiredForUpdate(con, from, to);
			HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
			
			int i = 0;
			if (shouldLog) logger.debug("Found {} expired event instances", instances.size());
			for (VExpEventInstance instance : instances) {
				i++;
				try {
					if (shouldLog) logger.debug("[{}] Working on event instance... [{}]", i, instance.getKey());
					
					final DateTime remindOn = instance.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(instance.getReminder());
					if (now.compareTo(remindOn) < 0) continue;
					
					// If instance should have been reminded in past...
					if (instance.getRemindedOn() != null) {
						// Only recurring event instances should pass here, classic events are already excluded by the db query
						if (instance.getRecurrenceId() == null) throw new WTException("This should never happen (famous last words)");
						final DateTime lastRemindedOn = instance.getRemindedOn().withZone(DateTimeZone.UTC);
						if (remindOn.compareTo(lastRemindedOn) <= 0) continue;
						// If instance should have been reminded after last remind...
					}
					
					if (!byEmailCache.containsKey(instance.getCalendarProfileId())) {
						CalendarUserSettings cus = new CalendarUserSettings(SERVICE_ID, instance.getCalendarProfileId());
						boolean bool = cus.getEventReminderDelivery().equals(CalendarSettings.EVENT_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(instance.getCalendarProfileId(), bool);
					}
					
					if (shouldLog) logger.debug("[{}] Creating alert... [{}]", i, instance.getKey());
					BaseReminder alert = null;
					if (byEmailCache.get(instance.getCalendarProfileId())) {
						UserProfile.Data ud = WT.getUserData(instance.getCalendarProfileId());
						if (ud == null) throw new WTException("UserData is null [{}]", instance.getCalendarProfileId());
						CoreUserSettings cus = new CoreUserSettings(instance.getCalendarProfileId());
						EventInstance eventInstance = getEventInstance(instance.getKey());
						alert = createEventReminderAlertEmail(ud.getLocale(), cus.getShortDateFormat(), cus.getShortTimeFormat(), ud.getPersonalEmailAddress(), instance.getCalendarProfileId(), eventInstance);
						
					} else {
						alert = createEventReminderAlertWeb(instance);
					}
					
					if (shouldLog) logger.debug("[{}] Updating event record... [{}]", i, instance.getEventId());
					int ret = evtDao.updateRemindedOn(con, instance.getEventId(), now);
					if (ret != 1) continue;
					
					alerts.add(alert);
					if (shouldLog) logger.debug("[{}] Alert collected [{}]", i, instance.getKey());
					
				} catch (Exception ex1) {
					logger.warn("[{}] Unable to manage reminder. Event instance skipped! [{}]", ex1, i, instance.getKey());
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error handling event instances' reminder alerts", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
	}

	/*
	private void sendInvitationForZPushEvents() {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			ArrayList<Integer> processed = new ArrayList<>();
			for (OEvent oevt : evtDao.selectHandleInvitationByRevision(con)) {
				Event.RevisionStatus revStatus = EnumUtils.forSerializedName(oevt.getRevisionStatus(), Event.RevisionStatus.class);
				String crud = null;
				if (Event.RevisionStatus.NEW.equals(revStatus)) {
					crud = Crud.CREATE;
				} else if (Event.RevisionStatus.MODIFIED.equals(revStatus)) {
					crud = Crud.UPDATE;
				} else if (Event.RevisionStatus.DELETED.equals(revStatus)) {
					crud = Crud.DELETE;
				}
				if (crud == null) {
					logger.warn("Invalid revision status [{}]", oevt.getEventId());
					continue;
				}
				Event event = getEvent(oevt.getEventId(), true);
				if (event == null) {
					logger.warn("Event not found [{}]", oevt.getEventId());
					continue;
				}
				Calendar calendar = getCalendar(oevt.getCalendarId());
				if (calendar == null) {
					logger.warn("Calendar not found [{}]", oevt.getCalendarId());
				} else {
					//notifyAttendees(calendar.getProfileId(), crud, event);
					List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), event, crud);
					if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, event, crud);
				}
				processed.add(oevt.getEventId());
			}
			
			evtDao.updateHandleInvitationIn(con, processed, false);
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error collecting reminder alerts", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	public ProbeCalendarRemoteUrlResult probeCalendarRemoteUrl(Calendar.Provider provider, URI url, String username, String password) throws WTException {
		if (!Calendar.isProviderRemote(provider)) {
			throw new WTException("Provider is not remote (webcal or CalDAV) [{}]", EnumUtils.toSerializedName(provider));
		}
		
		try {
			if (Calendar.Provider.WEBCAL.equals(provider)) {
				URIBuilder builder = new URIBuilder(url);
				if (StringUtils.equalsIgnoreCase(builder.getScheme(), "webcal")) {
					builder.setScheme("http"); // Force http scheme
				}
				if (!StringUtils.isBlank(username) && !StringUtils.isBlank(username)) {
					builder.setUserInfo(username, password);
				}
				URI newUrl = URIUtils.buildQuietly(builder);
				
				HttpClient httpCli = null;
				try {
					logger.debug("Checking remote calendar URL [{}]", newUrl.toString());
					httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), newUrl);
					return HttpClientUtils.exists(httpCli, newUrl) ? new ProbeCalendarRemoteUrlResult(FilenameUtils.getBaseName(newUrl.getPath())) : null;
				} finally {
					HttpClientUtils.closeQuietly(httpCli);
				}
				
			} else if (Calendar.Provider.CALDAV.equals(provider)) {
				CalDav dav = getCalDav(username, password);
				
				try {
					DavCalendar dcal = dav.getCalendar(url.toString());
					return (dcal != null) ? new ProbeCalendarRemoteUrlResult(dcal.getDisplayName()) : null;
					
				} catch(DavException ex) {
					logger.error("DAV error", ex);
					return null;
				}
			} else {
				throw new WTException("Unsupported provider");
			}
			
		} catch(IOException ex) {
			throw new WTException(ex, "Unable to check URL [{0}]", url.toString());
		}
	}
	
	public void syncRemoteCalendar(int calendarId, boolean full) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		final ICalendarInput icalInput = new ICalendarInput(udata.getTimeZone());
		final String PENDING_KEY = String.valueOf(calendarId);
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		if (pendingRemoteCalendarSyncs.putIfAbsent(PENDING_KEY, RunContext.getRunProfileId()) != null) {
			throw new ConcurrentSyncException("Sync activity is already running [{}, {}]", calendarId, RunContext.getRunProfileId());
		}
		
		try {
			//checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID, false);
			Calendar cal = ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) throw new WTException("Calendar not found [{0}]", calendarId);
			if (!Calendar.Provider.WEBCAL.equals(cal.getProvider()) && !Calendar.Provider.CALDAV.equals(cal.getProvider())) {
				throw new WTException("Specified calendar is not remote (webcal or CalDAV) [{0}]", calendarId);
			}
			
			// Force a full update if last-sync date is null
			if (cal.getRemoteSyncTimestamp() == null) full = true;
			
			CalendarRemoteParameters params = LangUtils.deserialize(cal.getParameters(), CalendarRemoteParameters.class);
			if (params == null) throw new WTException("Unable to deserialize remote parameters");
			if (params.url == null) throw new WTException("Remote URL is undefined");
			
			if (Calendar.Provider.WEBCAL.equals(cal.getProvider())) {
				final String PREFIX = "webcal-";
				File tempFile = null;
				
				URIBuilder builder = new URIBuilder(params.url);
				if (StringUtils.equalsIgnoreCase(builder.getScheme(), "webcal")) {
					builder.setScheme("http"); // Force http scheme
				}
				if (!StringUtils.isBlank(params.username) && !StringUtils.isBlank(params.username)) {
					builder.setUserInfo(params.username, params.password);
				}
				URI newUrl = URIUtils.buildQuietly(builder);
				
				try {
					final DateTime newLastSync = DateTimeUtils.now();
					tempFile = WT.createTempFile(PREFIX, null);
					
					// Retrieve webcal content (iCalendar) from the specified URL 
					// and save it locally
					logger.debug("Downloading iCalendar file from URL [{}]", newUrl);
					HttpClient httpCli = null;
					FileOutputStream os = null;
					try {
						httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), newUrl);
						os = new FileOutputStream(tempFile);
						HttpClientUtils.writeContent(httpCli, newUrl, os);
						
					} catch(IOException ex) {
						throw new WTException(ex, "Unable to retrieve webcal [{0}]", newUrl);
					} finally {
						IOUtils.closeQuietly(os);
						HttpClientUtils.closeQuietly(httpCli);
					}
					logger.debug("Saved to temp file [{}]", tempFile.getName());

					// Parse downloaded iCalendar
					logger.debug("Parsing downloaded iCalendar file");
					net.fortuna.ical4j.model.Calendar ical = null;
					FileInputStream is = null;
					try {
						is = new FileInputStream(tempFile);
						ical = ICalendarUtils.parse(is);
						//TODO: add support to FILENAME property (Google https://github.com/ical4j/ical4j/issues/69)
					} catch(IOException | ParserException ex) {
						throw new WTException(ex, "Unable to read webcal");
					} finally {
						IOUtils.closeQuietly(os);
					}
					
					icalInput.withIncludeVEventSourceInOutput(true);
					ArrayList<EventInput> input = icalInput.fromICalendarFile(ical, null);
					logger.debug("Found {} events", input.size());
					
					Map<String, VEventHrefSync> syncByHref = null;
							
					if (full) {
						logger.debug("Cleaning up calendar [{}]", calendarId);
						doEventsDeleteByCalendar(con, calendarId, false);
					} else {
						EventDAO evtDao = EventDAO.getInstance();
						syncByHref = evtDao.viewHrefSyncDataByCalendar(con, calendarId);
					}
					
					// Inserts/Updates data...
					logger.debug("Inserting/Updating events...");
					try {
						String autoUidPrefix = DigestUtils.md5Hex(newUrl.toString()); // auto-gen base prefix in case of missing UID
						HashSet<String> hrefs = new HashSet<>();
						HashMap<String, OEvent> cache = new HashMap<>();
						int i = 0;
						for (EventInput ei : input) {
							if (StringUtils.isBlank(ei.event.getPublicUid())) {
								String autoUid = autoUidPrefix + "-" + i;
								ei.event.setPublicUid(autoUid);
								logger.trace("Missing UID: using auto-gen value. [{}]", autoUid);
							}
							String href = ManagerUtils.buildHref(ei.event.getPublicUid());
							
							//if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
							if (hrefs.contains(href)) {
								logger.trace("Event duplicated. Skipped! [{}]", href);
								continue;
							}
							
							boolean skip = false;
							Integer matchingEventId = null;
							String eiHash = DigestUtils.md5Hex(ei.sourceEvent.toString());
							
							if (syncByHref != null) { // Only if... (!full) see above!
								VEventHrefSync hrefSync = syncByHref.remove(href);
								if (hrefSync != null) { // Href found -> maybe updated item
									if (!StringUtils.equals(hrefSync.getEtag(), eiHash)) {
										matchingEventId = hrefSync.getEventId();
										logger.trace("Event updated [{}, {}]", href, eiHash);
									} else {
										skip = true;
										logger.trace("Event not modified [{}, {}]", href, eiHash);
									}
								} else { // Href not found -> added item
									logger.trace("Event newly added [{}, {}]", href, eiHash);
								}
							}
							
							if (!skip) {
								ei.event.setCalendarId(calendarId);
								ei.event.setHref(href);
								ei.event.setEtag(eiHash);
								
								if (matchingEventId != null) {
									ei.event.setEventId(matchingEventId);
									boolean updated = doEventInputUpdate(con, cache, ei, ProcessReminder.NO);
									if (!updated) throw new WTException("Event not found [{}]", ei.event.getEventId());
									
								} else {
									doEventInputInsert(con, cache, ei, ProcessReminder.NO);
								}
							}
							
							hrefs.add(href); // Marks as processed!
						}
						
						if (syncByHref != null) { // Only if... (!full) see above!
							// Remaining hrefs -> deleted items
							for (VEventHrefSync hrefSync : syncByHref.values()) {
								logger.trace("Event deleted [{}]", hrefSync.getHref());
								doEventDelete(con, hrefSync.getEventId(), false);
							}
						}
						
						cache.clear();
						calDao.updateRemoteSyncById(con, calendarId, newLastSync, null);
						DbUtils.commitQuietly(con);

					} catch(Exception ex) {
						DbUtils.rollbackQuietly(con);
						throw new WTException(ex, "Error importing iCalendar");
					}
					
				} finally {
					if (tempFile != null) {
						logger.debug("Removing temp file [{}]", tempFile.getName());
						WT.deleteTempFile(tempFile);
					}
				}
				
			} else if (Calendar.Provider.CALDAV.equals(cal.getProvider())) {
				CalDav dav = getCalDav(params.username, params.password);
				
				try {
					DavCalendar dcal = dav.getCalendarSyncToken(params.url.toString());
					if (dcal == null) throw new WTException("DAV calendar not found");
					
					final boolean syncIsSupported = !StringUtils.isBlank(dcal.getSyncToken());
					final DateTime newLastSync = DateTimeUtils.now();
					
					if (!full && (syncIsSupported && !StringUtils.isBlank(cal.getRemoteSyncTag()))) { // Partial update using SYNC mode
						String newSyncToken = dcal.getSyncToken();
						
						logger.debug("Querying CalDAV endpoint for changes [{}, {}]", params.url.toString(), cal.getRemoteSyncTag());
						List<DavSyncStatus> changes = dav.getCalendarChanges(params.url.toString(), cal.getRemoteSyncTag());
						logger.debug("Returned {} items", changes.size());
						
						try {
							if (!changes.isEmpty()) {
								EventDAO evtDao = EventDAO.getInstance();
								Map<String, List<Integer>> eventIdsByHref = evtDao.selectHrefsByByCalendar(con, calendarId);
								
								// Process changes...
								logger.debug("Processing changes...");
								HashSet<String> hrefs = new HashSet<>();
								for (DavSyncStatus change : changes) {
									String href = FilenameUtils.getName(change.getPath());
									//String href = change.getPath();
									
									if (DavUtil.HTTP_SC_TEXT_OK.equals(change.getResponseStatus())) {
										hrefs.add(href);

									} else { // Event deleted
										List<Integer> eventIds = eventIdsByHref.get(href);
										Integer eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;
										if (eventId == null) {
											logger.warn("Deletion not possible. Event path not found [{}]", PathUtils.concatPaths(dcal.getPath(), FilenameUtils.getName(href)));
											continue;
										}
										doEventDelete(con, eventId, false);
									}
								}

								// Retrieves events list from DAV endpoint (using multiget)
								logger.debug("Retrieving inserted/updated events [{}]", hrefs.size());
								Collection<String> paths = hrefs.stream().map(href -> PathUtils.concatPaths(dcal.getPath(), FilenameUtils.getName(href))).collect(Collectors.toList());
								List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString(), paths);
								//List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString(), hrefs);

								// Inserts/Updates data...
								logger.debug("Inserting/Updating events...");
								HashMap<String, OEvent> cache = new HashMap<>();
								for (DavCalendarEvent devt : devts) {
									String href = FilenameUtils.getName(devt.getPath());
									//String href = devt.getPath();
									
									if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
									List<Integer> eventIds = eventIdsByHref.get(href);
									Integer eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;
									
									final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
									if (input.size() != 1) throw new WTException("iCal must contain one event");
									final EventInput ei = input.get(0);
									
									if (eventId != null) {
										doEventDelete(con, eventId, false);
									}
									
									ei.event.setCalendarId(calendarId);
									ei.event.setHref(href);
									ei.event.setEtag(devt.geteTag());
									doEventInputInsert(con, cache, ei, ProcessReminder.NO);
								}
							}
							
							calDao.updateRemoteSyncById(con, calendarId, newLastSync, newSyncToken);
							DbUtils.commitQuietly(con);
							
						} catch(Exception ex) {
							DbUtils.rollbackQuietly(con);
							throw new WTException(ex, "Error importing iCalendar");
						}
						
					} else { // Full update or partial computing hashes
						String newSyncToken = null;
						if (syncIsSupported) { // If supported, saves last sync-token issued by the server
							newSyncToken = dcal.getSyncToken();
						}
						
						// Retrieves cards from DAV endpoint
						logger.debug("Querying CalDAV endpoint [{}]", params.url.toString());
						List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString());
						logger.debug("Returned {} items", devts.size());
						
						// Handles data...
						try {
							Map<String, VEventHrefSync> syncByHref = null;
							
							if (full) {
								logger.debug("Cleaning up calendar [{}]", calendarId);
								doEventsDeleteByCalendar(con, calendarId, false);
							} else if (!full && !syncIsSupported) {
								// This hash-map is only needed when syncing using hashes
								EventDAO evtDao = EventDAO.getInstance();
								syncByHref = evtDao.viewHrefSyncDataByCalendar(con, calendarId);
							}	
							
							logger.debug("Processing results...");
							// Define a simple map in order to check duplicates.
							// eg. SOGo passes same card twice :(
							HashSet<String> hrefs = new HashSet<>();
							HashMap<String, OEvent> cache = new HashMap<>();
							for (DavCalendarEvent devt : devts) {
								String href = PathUtils.getFileName(devt.getPath());
								//String href = devt.getPath();
								String etag = devt.geteTag();
								
								if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
								if (hrefs.contains(href)) {
									logger.trace("Card duplicated. Skipped! [{}]", href);
									continue;
								}
								
								boolean skip = false;
								Integer matchingEventId = null;
								
								if (syncByHref != null) { // Only if... (!full && !syncIsSupported) see above!
									//String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
									//String hash = DigestUtils.md5Hex(new ICalendarOutput(prodId, true).write(devt.getCalendar()));
									String hash = DigestUtils.md5Hex(ICalendarUtils.getVEvent(devt.getCalendar()).toString());
									
									VEventHrefSync hrefSync = syncByHref.remove(href);
									if (hrefSync != null) { // Href found -> maybe updated item
										if (!StringUtils.equals(hrefSync.getEtag(), hash)) {
											matchingEventId = hrefSync.getEventId();
											etag = hash;
											logger.trace("Event updated [{}, {}]", href, hash);
										} else {
											skip = true;
											logger.trace("Event not modified [{}, {}]", href, hash);
										}
									} else { // Href not found -> added item
										logger.trace("Event newly added [{}]", href);
										etag = hash;
									}
								}
								
								if (!skip) {
									final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
									if (input.size() != 1) throw new WTException("iCal must contain one event");
									final EventInput ei = input.get(0);
									ei.event.setCalendarId(calendarId);
									ei.event.setHref(href);
									ei.event.setEtag(etag);
									
									if (matchingEventId == null) {
										doEventInputInsert(con, cache, ei, ProcessReminder.NO);
									} else {
										ei.event.setEventId(matchingEventId);
										boolean updated = doEventInputUpdate(con, cache, ei, ProcessReminder.NO);
										if (!updated) throw new WTException("Event not found [{}]", ei.event.getEventId());
									}
								}
								
								hrefs.add(href); // Marks as processed!
							}
							
							if (syncByHref != null) { // Only if... (!full && !syncIsSupported) see above!
								// Remaining hrefs -> deleted items
								for (VEventHrefSync hrefSync : syncByHref.values()) {
									logger.trace("Event deleted [{}]", hrefSync.getHref());
									doEventDelete(con, hrefSync.getEventId(), false);
								}
							}
							
							calDao.updateRemoteSyncById(con, calendarId, newLastSync, newSyncToken);
							DbUtils.commitQuietly(con);
							
						} catch(Exception ex) {
							DbUtils.rollbackQuietly(con);
							throw new WTException(ex, "Error importing iCalendar");
						}
					}
					
				} catch(DavException ex) {
					throw new WTException(ex, "CalDAV error");
				}
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
			pendingRemoteCalendarSyncs.remove(PENDING_KEY);
		}
	}
	
	private <T> T calculateFirstRecurringInstance(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTimeZone userTimezone) throws WTException {
		List<T> instances = calculateRecurringInstances_OLD(con, instanceMapper, null, null, userTimezone, 1);
		return instances.isEmpty() ? null : instances.get(0);
	}
	
	private Set<LocalDate> doGetExcludedDates(Connection con, int eventId, int recurrenceId) {
		/*
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		LinkedHashSet<LocalDate> exdates = new LinkedHashSet();
		
		List<ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, recurrenceId);
		for (ORecurrenceBroken obrec : obrecs) {
			exdates.add(obrec.getEventDate());
		}
		
		return exdates;
		*/
		
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		Map<LocalDate, ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, recurrenceId);
		return obrecs.keySet();
	}
	
	private LocalDate calculateRecurrenceStart(Connection con, int eventId, DateTime eventStart, DateTime eventEnd, DateTimeZone eventTimezone) throws WTException {
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		
		try {
			ORecurrence orec = recDao.selectByEvent(con, eventId);
			if (orec == null) {
				logger.warn("Unable to retrieve recurrence for event [{}]", eventId);
				return null;
			} else {
				Recur recur = orec.getRecur();
				if (recur == null) throw new WTException("Unable to parse rrule [{}]", orec.getRule());
				
				Set<LocalDate> exclDates = recbDao.selectDatesByEventRecurrence(con, eventId, orec.getRecurrenceId());
				List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, orec.getStartDate(), true, exclDates, eventStart, eventEnd, eventTimezone, null, null, 1);
				return !dates.isEmpty() ? dates.get(0) : null;
			}
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		}
	}
	
	@Deprecated
	private <T> List<T> calculateRecurringInstances_OLD(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return calculateRecurringInstances_OLD(con, instanceMapper, fromDate, toDate, userTimezone, -1);
	}
	
	@Deprecated
	private <T> List<T> calculateRecurringInstances_OLD(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime rangeFrom, DateTime rangeTo, DateTimeZone userTimezone, int limit) throws WTException {
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		ArrayList<T> instances = new ArrayList<>();
		
		int eventId = instanceMapper.getEventId();
		DateTime eventStart = instanceMapper.getEventStartDate();
		DateTime eventEnd = instanceMapper.getEventEndDate();
		DateTimeZone eventTimezone = instanceMapper.getEventTimezone();
		int eventDays = CalendarUtils.calculateLengthInDays(eventStart, eventEnd);
		LocalTime eventStartTime = eventStart.withZone(eventTimezone).toLocalTime();
		LocalTime eventEndTime = eventEnd.withZone(eventTimezone).toLocalTime();
		
		try {
			// Retrieves reccurence and broken dates (if any)
			ORecurrence orec = recDao.selectByEvent(con, eventId);
			if (orec == null) {
				logger.warn("Unable to retrieve recurrence for event [{}]", eventId);

			} else {
				if (rangeFrom == null) rangeFrom = orec.getStartDate();
				if (rangeTo == null) rangeTo = orec.getStartDate().plusYears(1);

				Recur recur = orec.getRecur();
				if (recur == null) throw new WTException("Unable to parse rrule [{}]", orec.getRule());
				
				Set<LocalDate> exclDates = recbDao.selectDatesByEventRecurrence(con, eventId, orec.getRecurrenceId());
				List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, orec.getStartDate(), exclDates, instanceMapper.isEventAllDay(), eventStart, eventEnd, eventTimezone, rangeFrom, rangeTo, limit);
				for (LocalDate recurringDate : dates) {
					DateTime start = DateTimeUtils.toDateTime(recurringDate, eventStartTime, eventTimezone).withZone(userTimezone);
					DateTime end = DateTimeUtils.toDateTime(recurringDate.plusDays(eventDays), eventEndTime, eventTimezone).withZone(userTimezone);
					String key = EventKey.buildKey(eventId, eventId, recurringDate);
					final EventInstanceId id = EventInstanceId.build(String.valueOf(eventId), start, eventTimezone);
					
					instances.add(instanceMapper.createInstance(key, start, end));
				}
				
				/*
				DateList dates = ICal4jUtils.calculateRecurrenceSet(recur, orec.getStartDate(), eventStart, eventEnd, eventTimezone, rangeFrom, rangeTo, limit);
				Iterator it = dates.iterator();
				while (it.hasNext()) {
					net.fortuna.ical4j.model.Date dt = (net.fortuna.ical4j.model.Date)it.next();
					LocalDate recurringDate = ICal4jUtils.toJodaLocalDate(dt, eventTimezone);
					if (obrecs.containsKey(recurringDate)) continue; // Skip broken date...

					DateTime start = recurringDate.toDateTime(eventStartTime, eventTimezone).withZone(userTimezone);
					DateTime end = recurringDate.plusDays(eventDays).toDateTime(eventEndTime, eventTimezone).withZone(userTimezone);
					String key = EventKey.buildKey(eventId, eventId, recurringDate);

					instances.add(instanceMapper.createInstance(key, start, end));
				}
				*/
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		}
		
		return instances;
	}
	
	public static String buildEventPublicUrl(String publicBaseUrl, String eventPublicId) {
		String s = PublicService.PUBPATH_CONTEXT_EVENT + "/" + eventPublicId;
		return PathUtils.concatPaths(publicBaseUrl, s);
	}
	
	public static String buildEventReplyPublicUrl(String publicBaseUrl, String eventPublicId, String attendeePublicId, String resp) {
		String s = PublicService.PUBPATH_CONTEXT_EVENT + "/" + eventPublicId + "/" + PublicService.EventUrlPath.TOKEN_REPLY + "?aid=" + attendeePublicId + "&resp=" + resp;
		return PathUtils.concatPaths(publicBaseUrl, s);
	}
	
	private void fillExportMapDates(HashMap<String, Object> map, DateTimeZone timezone, VVEventInstance sei) throws Exception {
		DateTime startDt = sei.getStartDate().withZone(timezone);
		map.put("startDate", startDt);
		map.put("startTime", startDt);
		DateTime endDt = sei.getEndDate().withZone(timezone);
		map.put("endDate", endDt);
		map.put("endTime", endDt);
		map.put("timezone", sei.getTimezone());
		map.put("duration", Minutes.minutesBetween(sei.getEndDate(), sei.getStartDate()).size());
	}
	
	private void fillExportMapBasic(HashMap<String, Object> map, CoreManager coreMgr, Connection con, VVEvent event) throws Exception {
		map.put("eventId", event.getEventId());
		map.put("title", event.getTitle());
		map.put("description", event.getDescription());
		
		if(event.getActivityId() != null) {
			ActivityDAO actDao = ActivityDAO.getInstance();
			OActivity activity = actDao.select(con, event.getActivityId());
			if(activity == null) throw new WTException("Activity [{0}] not found", event.getActivityId());

			map.put("activityId", activity.getActivityId());
			map.put("activityDescription", activity.getDescription());
			map.put("activityExternalId", activity.getExternalId());
		}
		
		if (event.getMasterDataId() != null) {
			MasterData md = coreMgr.getMasterData(event.getMasterDataId());
			map.put("masterDataId", md.getMasterDataId());
			map.put("masterDataDescription", md.getDescription());
			map.put("customerId", md.getMasterDataId());
			map.put("customerDescription", md.getDescription());
		}
		
		if(event.getCausalId() != null) {
			CausalDAO cauDao = CausalDAO.getInstance();
			OCausal causal = cauDao.select(con, event.getCausalId());
			if(causal == null) throw new WTException("Causal [{0}] not found", event.getCausalId());
			
			map.put("causalId", causal.getCausalId());
			map.put("causalDescription", causal.getDescription());
			map.put("causalExternalId", causal.getExternalId());
		}
	}
	
	private Calendar doCalendarGet(Connection con, int calendarId) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		return ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
	}
	
	private UserProfileId doCalendarGetOwner(int calendarId) throws WTException {
		OCalendarOwnerInfo ocoi = doCalendarGetOwnerInfo(calendarId);
		return (ocoi == null) ? null : new UserProfileId(ocoi.getDomainId(), ocoi.getUserId());
	}
	
	private OCalendarOwnerInfo doCalendarGetOwnerInfo(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGetOwnerInfo(con, calendarId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private OCalendarOwnerInfo doCalendarGetOwnerInfo(Connection con, int calendarId) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		return calDao.selectOwnerInfoById(con, calendarId);
	}
	
	private VCalendarDefaults doCalendarGetDefaults(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGetDefaults(con, calendarId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private VCalendarDefaults doCalendarGetDefaults(Connection con, int calendarId) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		return calDao.selectDefaultsById(con, calendarId);
	}
	
	private Map<Integer, Integer> mapCalendarByEvent(Collection<Integer> eventIds) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return evtDao.selectCalendarsByIds(con, eventIds);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private Calendar doCalendarInsert(Connection con, Calendar cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		
		OCalendar ocal = ManagerUtils.createOCalendar(cal);
		ocal.setCalendarId(calDao.getSequence(con).intValue());
		fillOCalendarWithDefaults(ocal);
		if (ocal.getIsDefault()) calDao.resetIsDefaultByProfile(con, ocal.getDomainId(), ocal.getUserId());
		
		calDao.insert(con, ocal);
		return ManagerUtils.createCalendar(ocal);
	}
	
	private boolean doCalendarUpdate(Connection con, Calendar cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		
		OCalendar ocal = ManagerUtils.createOCalendar(cal);
		fillOCalendarWithDefaults(ocal);
		if (ocal.getIsDefault()) calDao.resetIsDefaultByProfile(con, ocal.getDomainId(), ocal.getUserId());
		
		return calDao.update(con, ocal) == 1;
	}
	
	private ArrayList<ComparableEventBounds> doListEventsBounds(final Connection con, final Collection<Integer> calendarIds, final DateTime spanFrom, final DateTime spanTo, final DateTimeZone targetTimezone) throws WTException {
		Check.notNull(calendarIds, "calendarIds");
		Check.notNull(spanFrom, "spanFrom");
		Check.notNull(spanTo, "spanTo");
		EventDAO evtDao = EventDAO.getInstance();
		
		ArrayList<ComparableEventBounds> bounds = new ArrayList<>();
		for (VEventFootprint vfootprint : evtDao.viewRangesByCalendarRangeCondition(con, calendarIds, spanFrom, spanTo, EventDAO.toBusyCondition())) {
			final EventInstanceId id = EventInstanceId.build(String.valueOf(vfootprint.getEventId()), null);
			final DateTime start = vfootprint.getStartDate().withZone(targetTimezone);
			final DateTime end = vfootprint.getEndDate().withZone(targetTimezone);
			bounds.add(new ComparableEventBounds(start, end, id));
		}
		
		int noOfRecurringInst = Days.daysBetween(spanFrom, spanTo).getDays() + 2;
		for (VEventFootprint vfootprint : evtDao.viewRecurringRangesByCalendarRangeCondition(con, calendarIds, spanFrom, spanTo, EventDAO.toBusyCondition())) {
			bounds.addAll(calculateRecurringInstances(new VEventFootprintBoundsRecurringContext(con, vfootprint), spanFrom, spanTo, noOfRecurringInst));
		}
		return bounds;
	}
	
	private EventObject doEventObjectPrepare(Connection con, VEventObject vobj, EventObjectOutputType outputType) throws WTException {
		if (EventObjectOutputType.STAT.equals(outputType)) {
			return ManagerUtils.fillEventCalObject(new EventObject(), vobj);
			
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();

			Event event = ManagerUtils.fillEvent(new Event(), vobj);

			if (vobj.getRecurrenceId() != null) {
				ORecurrence orec = recDao.select(con, vobj.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to get recurrence [{}]", vobj.getRecurrenceId());
				
				Set<LocalDate> excludedDates = doGetExcludedDates(con, event.getEventId(), vobj.getRecurrenceId());
				event.setRecurrence(orec.getRule(), orec.getLocalStartDate(event.getDateTimeZone()), excludedDates);
			}
			if (vobj.hasAttendees()) {
				List<OEventAttendee> oatts = attDao.selectByEvent(con, event.getEventId());
				event.setAttendees(ManagerUtils.createEventAttendeeList(oatts));
			}
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.getIsPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			if (EventObjectOutputType.ICALENDAR.equals(outputType)) {
				EventObjectWithICalendar eco = ManagerUtils.fillEventCalObject(new EventObjectWithICalendar(), vobj);
				
				ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
				//TODO: add support to excluded dates
				net.fortuna.ical4j.model.Calendar iCal = out.toCalendar(event);
				if (vobj.getHasIcalendar()) {
					//TODO: in order to be fully compliant, merge generated vcard of the original one in db table!
				}
				try {
					eco.setIcalendar(out.write(iCal));
				} catch(IOException ex) {
					throw new WTException(ex, "Unable to write iCalendar");
				}
				return eco;
				
			} else {
				EventObjectWithBean eco = ManagerUtils.fillEventCalObject(new EventObjectWithBean(), vobj);
				eco.setEvent(event);
				return eco;
			}
		}
	}
	
	private Integer doEventGetId(Connection con, String publicUid, Collection<Integer> calendarIdMustBeIn) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		List<Integer> ids = null;
		if ((calendarIdMustBeIn == null) || calendarIdMustBeIn.isEmpty()) {
			// This kind of lookup is suitable for calls executed by admin from public service
			ids = evtDao.selectAliveIdsByPublicUid(con, publicUid);
			
		} else {
			logger.trace("Looking for publicId in restricted set of calendars...");
			ids = evtDao.selectAliveIdsByCalendarsPublicUid(con, calendarIdMustBeIn, publicUid);
		}
		
		// In case of invitations we can have multiple ids belonging to the same 
		// publicId. So here we can safely get the first one due to retured ids
		// are sorted by insertion-timestamp and by id sequence; the first is the
		// originating appointment.
		return ids.isEmpty() ? null : ids.get(0);
	}
	
	private Event doEventGet(Connection con, int eventId, boolean processAttachments, boolean processTags) throws DAOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventTagDAO tagDao = EventTagDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEvent oevt = evtDao.selectAliveById(con, eventId);
		if (oevt == null) return null;
		
		Event evt = ManagerUtils.createEvent(oevt);
		
		if (oevt.getRecurrenceId() != null) {
			ORecurrence orec = recDao.select(con, oevt.getRecurrenceId());
			if (orec == null) throw new WTException("Unable to get recurrence [{}]", oevt.getRecurrenceId());
			
			Set<LocalDate> excludedDates = doGetExcludedDates(con, oevt.getEventId(), oevt.getRecurrenceId());
			evt.setRecurrence(orec.getRule(), orec.getLocalStartDate(evt.getDateTimeZone()), excludedDates);
		}
		
		List<OEventAttendee> oattes = atteDao.selectByEvent(con, eventId);
		//evt.setAttendees(ManagerUtils.createEventAttendeeList(oattes));
		if (!oattes.isEmpty()) {
			evt.setAttendees(ManagerUtils.createEventAttendeeList(oattes));
		}
		if (processTags) {
			evt.setTags(tagDao.selectTagsByEvent(con, eventId));
		}
		if (processAttachments) {
			List<OEventAttachment> oattchs = attchDao.selectByEvent(con, eventId);
			evt.setAttachments(ManagerUtils.createEventAttachmentList(oattchs));
		}
		
		return evt;
	}
	
	private Event doEventGet(Connection con, GetEventScope scope, String publicUid) throws WTException {
		ArrayList<Integer> ids = new ArrayList<>();
		if (scope.equals(GetEventScope.PERSONAL) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			Integer eventId = doEventGetId(con, publicUid, listCalendarIds());
			if (eventId != null) ids.add(eventId);
		}
		if (scope.equals(GetEventScope.INCOMING) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			Integer eventId = doEventGetId(con, publicUid, listIncomingCalendarIds());
			if (eventId != null) ids.add(eventId);
		}
		
		if (ids.isEmpty()) return null;
		// Filled array could contains more than one result, eg. in case of 
		// invitation between two users of the same domain where the target
		// calendar of one user is shared to the other.
		// Returning the first result is the most appropriated action because
		// personal elements are returned first.
		//if (ids.size() > 1) throw new WTException("Multiple events found for public id [{}]", publicUid);
		return doEventGet(con, ids.get(0), false, false);
	}
	
	private void doEventMasterUpdateAndCommit(Connection con, Event event, boolean processAttachments, boolean processTags, boolean processCustomValues, boolean notifyAttendees, Set<String> validTags) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		OEvent oevtOrig = evtDao.selectById(con, event.getEventId());
		if (oevtOrig == null) throw new WTException("Unable get original event [{}]", event.getEventId());
		
		// 1 - Updates event of new data
		doEventUpdate(con, oevtOrig, event, true, true, true, processAttachments, processTags, processCustomValues, validTags);

		DbUtils.commitQuietly(con);
		if (isAuditEnabled()) {
			auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, event.getEventId(), null);
		}

		Event eventDump = getEvent(event.getEventId());
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(con, new EventMapper(eventDump), eventDump.getDateTimeZone());
		}
		
		// Notify last modification
		List<RecipientTuple> nmRcpts = getModificationRecipients(eventDump.getCalendarId(), Crud.UPDATE);
		if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.UPDATE);

		// Notify attendees
		if (notifyAttendees) {
			List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.UPDATE);
			if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.UPDATE);
		}
	}
	
	private EventInstance doEventInstanceGet(Connection con, int eventId, LocalDate date, boolean processAttachments, boolean processTags, boolean processCustomValues) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventTagDAO tagDao = EventTagDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		OEvent oevt = evtDao.selectById(con, eventId);
		if (oevt == null) throw new WTException("Unable to get event [{}]", eventId);
		
		EventInstance ei = ManagerUtils.fillEvent(new EventInstance(), oevt);
		
		// Fill recurrence (if necessary)
		ORecurrence orec = recDao.select(con, oevt.getRecurrenceId());
		if (orec != null) {
			if (date == null) throw new WTException("Date is required for recurring events [{}]", eventId);
			
			Set<LocalDate> excludedDates = doGetExcludedDates(con, oevt.getEventId(), oevt.getRecurrenceId());
			recalculateStartEndForInstanceDate(date, ei);
			ei.setRecurrence(orec.getRule(), orec.getLocalStartDate(ei.getDateTimeZone()), excludedDates);
			
			ei.setKey(EventKey.buildKey(eventId, eventId, date));
			ei.setRecurInfo(Event.RecurInfo.RECURRING);
			
		} else {
			Integer seriesEventId = evtDao.selectAliveSeriesEventIdById(con, eventId);
			ei.setKey(EventKey.buildKey(eventId, seriesEventId));
			ei.setRecurInfo((seriesEventId == null) ? Event.RecurInfo.NONE : Event.RecurInfo.BROKEN);
		}
		
		List<OEventAttendee> oattes = atteDao.selectByEvent(con, eventId);
		//evt.setAttendees(ManagerUtils.createEventAttendeeList(oattes));
		if (!oattes.isEmpty()) {
			ei.setAttendees(ManagerUtils.createEventAttendeeList(oattes));
		}
		if (processTags) {
			ei.setTags(tagDao.selectTagsByEvent(con, eventId));
		}
		if (processAttachments) {
			List<OEventAttachment> oattchs = attchDao.selectByEvent(con, eventId);
			ei.setAttachments(ManagerUtils.createEventAttachmentList(oattchs));
		}
		if (processCustomValues) {
			List<OEventCustomValue> ovals = cvalDao.selectByEvent(con, eventId);
			ei.setCustomValues(ManagerUtils.createCustomValuesMap(ovals));
		}
		
		return ei;
	}
	
	private void recalculateStartEndForInstanceDate(LocalDate instanceDate, EventInstance event) {
		int spanDays = Math.abs(CalendarUtils.calculateLengthInDays(event.getStartDate(), event.getEndDate()));
		event.setStartDate(ManagerUtils.instanceDateToDateTime(instanceDate, event.getStartDate(), event.getDateTimeZone()));
		event.setEndDate(ManagerUtils.instanceDateToDateTime(instanceDate.plusDays(spanDays), event.getEndDate(), event.getDateTimeZone()));
	}
		
	private void doEventInstanceUpdateAndCommit(Connection con, OEventInfo originalEventInfo, UpdateEventTarget target, EventKey eventKey, Event event, boolean processAttachments, boolean processTags, boolean processCustomValues, boolean notifyAttendees, Set<String> validTags) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEvent oevtOrig = evtDao.selectById(con, originalEventInfo.getEventId());
		if (oevtOrig == null) throw new WTException("Unable get original event [{}]", originalEventInfo.getEventId());
		
		Event eventDump = null;
		if (originalEventInfo.isRecurring()) {
			// If target is SINCE and the referenced instance is clearly 
			// the first of the recurrence, we can actually treat the 
			// modification as an update to the whole series.
			if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
				LocalDate firstInstanceDate = calculateRecurrenceStart(con, oevtOrig.getEventId(), oevtOrig.getStartDate(), oevtOrig.getEndDate(), oevtOrig.getDateTimezone());
				boolean firstInstance = firstInstanceDate != null ? eventKey.instanceDate.equals(firstInstanceDate) : false;
				if (firstInstance) target = UpdateEventTarget.ALL_SERIES;
			}
			
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts new broken event (attendees and rr are not supported here)
				EventInsertResult insert = doEventInsert(con, event, null, false, false, false, false, processTags, processCustomValues, ProcessReminder.YES, validTags);

				// 2 - Inserts new broken record (marks recurring event) on modified date
				doRecurrenceExcludeDate(con, oevtOrig, eventKey.instanceDate, insert.oevent.getEventId());

				// 3 - Updates revision of original event
				evtDao.updateRevision(con, oevtOrig.getEventId(), BaseDAO.createRevisionTimestamp());

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, oevtOrig.getEventId(), null);
					auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, insert.oevent.getEventId(), null);
				}

				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());

				eventDump = getEvent(originalEventInfo.getEventId());
				
			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				// 1 - Resize original recurrence (sets until date at the day before date)
				ORecurrence orec = recDao.select(con, originalEventInfo.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", originalEventInfo.getRecurrenceId());
				
				int oldDaysBetween = CalendarUtils.calculateLengthInDays(event.getStartDate(), event.getEndDate());
				Recur oldRecur = orec.getRecur(); // Dump old recur!
				
				LocalTime untilTime = originalEventInfo.getAllDay() ? DateTimeUtils.TIME_AT_STARTOFDAY : originalEventInfo.getStartDate().withZone(originalEventInfo.getDateTimeZone()).toLocalTime();
				orec.updateUntilDate(eventKey.instanceDate.minusDays(1), untilTime, originalEventInfo.getDateTimeZone());
				recDao.update(con, orec);

				// 2 - Updates revision of original event
				evtDao.updateRevision(con, originalEventInfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				// 3 - Insert new event recalculating start/end from rec. start preserving days duration
				event.setStartDate(event.getStartDate().withDate(eventKey.instanceDate));
				event.setEndDate(event.getEndDate().withDate(eventKey.instanceDate.plusDays(oldDaysBetween)));
				
				// We cannot keep original count, it would be wrong... so convert it to an until date!
				if (ICal4jUtils.recurHasCount(oldRecur)) {
					DateTime oldUntilReal = ICal4jUtils.calculateRecurrenceEnd(oldRecur, oevtOrig.getStartDate(), oevtOrig.getStartDate(), oevtOrig.getEndDate(), oevtOrig.getDateTimezone());
					ICal4jUtils.setRecurUntilDate(oldRecur, oldUntilReal);
				}
				event.setRecurrence(oldRecur.toString(), eventKey.instanceDate, null);
				EventInsertResult insert = doEventInsert(con, event, null, true, false, false, false, processTags, processCustomValues, ProcessReminder.YES, validTags);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, originalEventInfo.getEventId(), null);
					auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, insert.oevent.getEventId(), null);
				}
				
				// TODO: eventually add support to clone attendees in the newly inserted event and so sending invitation emails
				
				eventDump = getEvent(originalEventInfo.getEventId());
				
			} else if (UpdateEventTarget.ALL_SERIES.equals(target)) { // Changes are valid for all the instances (whole recurrence)
				// We need to restore original dates because current start/end refers
				// to instance and not to the master event.
				event.setStartDate(event.getStartDate().withDate(oevtOrig.getStartDate().toLocalDate()));
				event.setEndDate(event.getEndDate().withDate(oevtOrig.getEndDate().toLocalDate()));
				
				// 1 - Updates event of new data
				doEventUpdate(con, oevtOrig, event, true, false, true, processAttachments, processTags, processCustomValues, validTags);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, originalEventInfo.getEventId(), null);
				}

				eventDump = getEvent(originalEventInfo.getEventId());
			}
			
		} else if (originalEventInfo.isBroken()) {
			// 1 - Updates broken event (follow eventId) of new data
			doEventUpdate(con, oevtOrig, event, false, false, true, processAttachments, processTags, processCustomValues, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, originalEventInfo.getEventId(), null);
			}
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			eventDump = getEvent(originalEventInfo.getEventId());
			
		} else {
			// 1 - Updates this event of new data
			doEventUpdate(con, oevtOrig, event, true, false, true, processAttachments, processTags, processCustomValues, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, oevtOrig.getEventId(), null);
			}
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());

			eventDump = getEvent(originalEventInfo.getEventId());
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(con, new EventMapper(eventDump), eventDump.getDateTimeZone());
		}
		
		// Notify last modification
		List<RecipientTuple> nmRcpts = getModificationRecipients(eventDump.getCalendarId(), Crud.UPDATE);
		if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.UPDATE);

		// Notify attendees
		if (notifyAttendees) {
			List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.UPDATE);
			if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.UPDATE);
		}
	}
	
	private void doEventInstanceDeleteAndCommit(Connection con, UpdateEventTarget target, EventKey eventKey, boolean notifyAttendees, boolean notifyResourceOrganizer) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEventInfo einfo = evtDao.selectOEventInfoById(con, eventKey.eventId);
		if (einfo == null) throw new WTException("Unable to retrieve event-info [{}]", eventKey.eventId);
		
		Event eventDump = null;
		if (einfo.isRecurring()) {
			// If target is SINCE and the referenced instance is clearly 
			// the first of the recurrence, we can actually treat the 
			// modification as an update to the whole series.
			if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
				LocalDate firstInstanceDate = calculateRecurrenceStart(con, einfo.getEventId(), einfo.getStartDate(), einfo.getStartDate(), einfo.getDateTimeZone());
				boolean firstInstance = firstInstanceDate != null ? eventKey.instanceDate.equals(firstInstanceDate) : false;
				if (firstInstance) target = UpdateEventTarget.ALL_SERIES;
			}
			
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts new broken record (without new broken event) on deleted date
				doRecurrenceExcludeDate(con, einfo.getEventId(), einfo.getRecurrenceId(), eventKey.instanceDate, null);
				
				// 2 - Updates revision of this event
				evtDao.updateRevision(con, einfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, einfo.getEventId(), null);
				}
				
				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				// 1 - Resize original recurrence (sets until date at the day before date)
				ORecurrence orec = recDao.select(con, einfo.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", einfo.getRecurrenceId());
				
				//orec.updateUntilDate(eventKey.instanceDate, einfo.getDateTimeZone());
				LocalTime untilTime = einfo.getAllDay() ? DateTimeUtils.TIME_AT_STARTOFDAY : einfo.getStartDate().withZone(einfo.getDateTimeZone()).toLocalTime();
				orec.updateUntilDate(eventKey.instanceDate.minusDays(1), untilTime, einfo.getDateTimeZone());
				recDao.update(con, orec);
				
				// 2 - Updates revision of this event
				evtDao.updateRevision(con, einfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, einfo.getEventId(), null);
				}
				
				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.ALL_SERIES.equals(target)) { // Changes are valid for all the instances (whole recurrence)
				eventDump = getEvent(einfo.getEventId()); // Save for later use!

				// 1 - logically delete this event
				doEventDelete(con, einfo.getEventId(), true);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, einfo.getEventId(), null);
				}
			}
			
		} else if (einfo.isBroken()) {
			eventDump = getEventInstance(eventKey, false, false, false); // Save for later use!
			
			// 1 - Logically delete this event (the broken)
			doEventDelete(con, einfo.getEventId(), true);
			
			// 2 - Updates revision of linked event
			evtDao.updateRevision(con, einfo.getLinkedEventId(), BaseDAO.createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, einfo.getEventId(), null);
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, einfo.getLinkedEventId(), null);
			}
			
		} else {
			eventDump = getEventInstance(eventKey, false, false, false); // Save for later use!
			
			// 1 - logically delete this event
			doEventDelete(con, einfo.getEventId(), true);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, einfo.getEventId(), null);
			}
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(con, new EventMapper(eventDump), eventDump.getDateTimeZone());
		}
		
		// Notify last modification
		List<RecipientTuple> nmRcpts = getModificationRecipients(eventDump.getCalendarId(), Crud.DELETE);
		if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.DELETE);

		// Notify attendees
		if (notifyAttendees) {
			List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.DELETE);
			if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.DELETE);
		}
		
		// For resources, notify the organizer (with a DECLINE) that the reservation is no longer available
		if (notifyResourceOrganizer) {
			CalendarFSOrigin origin = shareCache.getOriginByFolderId(eventDump.getCalendarId());
			if (origin != null && origin.isResource() && eventDump.getAttendees().size() == 1) {
				final String resourceAttendeeId = eventDump.getAttendees().get(0).getAttendeeId();
				try {
					replyToOrganizer(origin.getProfileId(), eventDump, resourceAttendeeId, net.fortuna.ical4j.model.parameter.PartStat.DECLINED);
				} catch (WTException ex) {
					logger.error("Error generating organizer reply", ex);
				}
				UserProfileId organizerProfile = WT.guessProfileIdByPersonalAddress(eventDump.getOrganizerAddress());
				if (organizerProfile != null) {
					try {
						CoreManager coreMgr = WT.getCoreManager(origin.getProfileId());
						Resource resource = coreMgr.getResource(origin.getProfileId().getUserId(), BitFlags.noneOf(ResourceGetOption.class));
						WT.notify(organizerProfile, new ResourceReservationReplySM("com.sonicle.webtop.calendar", resource, eventDump.getPublicUid(), eventDump.getTitle(), net.fortuna.ical4j.model.parameter.PartStat.DECLINED));
					} catch (WTException ex) {
						logger.error("Error generating organizer notification", ex);
					}
				}
			}
		}
	}
	
	private void doEventInstanceRestoreAndCommit(Connection con, EventKey eventKey, boolean notifyAttendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEventInfo einfo = evtDao.selectOEventInfoById(con, eventKey.eventId);
		if (einfo == null) throw new WTException("Unable to retrieve event-info [{}]", eventKey.eventId);
		
		Event eventDump = null;
		if (einfo.isBroken()) {
			eventDump = getEventInstance(eventKey, false, false, false); // Save for later use!
			
			// 1 - Removes the broken record
			rbkDao.deleteByNewEvent(con, einfo.getEventId());
			
			// 2 - Logically delete this event (the broken)
			doEventDelete(con, einfo.getEventId(), true);
			
			// 3 - updates revision of linked event
			evtDao.updateRevision(con, einfo.getLinkedEventId(), BaseDAO.createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, einfo.getEventId(), null);
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, einfo.getLinkedEventId(), null);
			}
			
			// TODO: eventually add support to notify attendees of the linked event of date restoration
			
		} else {
			throw new WTException("Cannot restore an event that is not broken");
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(con, new EventMapper(eventDump), eventDump.getDateTimeZone());
		}
		
		// Notify last modification
		List<RecipientTuple> nmRcpts = getModificationRecipients(eventDump.getCalendarId(), Crud.DELETE);
		if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.DELETE);

		// Notify attendees
		if (notifyAttendees) {
			List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.DELETE);
			if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.DELETE);
		}
	}
	
	private boolean doEventInputUpdate(Connection con, HashMap<String, OEvent> cache, EventInput input, ProcessReminder processReminder) throws DAOException, IOException {
		//TODO: Make this smart avoiding delete/insert!
		doEventDelete(con, input.event.getEventId(), false);
		doEventInputInsert(con, cache, input, processReminder);
		return true;
	}
	
	private EventInsertResult doEventInputInsert(Connection con, HashMap<String, OEvent> cache, EventInput ei, ProcessReminder processReminder) throws DAOException, IOException {
		EventInsertResult insert = doEventInsert(con, ei.event, null, true, true, true, false, false, false, processReminder, null);
		if (insert.orecurrence != null) {
			// Cache recurring event for future use within broken references 
			cache.put(insert.oevent.getPublicUid(), insert.oevent);
			
		} else {
			if (ei.addsExOnMaster != null) {
				if (cache.containsKey(ei.exRefersToPublicUid)) {
					final OEvent oevt = cache.get(ei.exRefersToPublicUid);
					doRecurrenceExcludeDate(con, oevt, ei.addsExOnMaster, insert.oevent.getEventId());
				}
			}
		}
		return insert;
	}
	
	private EventInsertResult doEventInsert(Connection con, Event event, String rawICalendar, boolean processRecurrence, boolean processExcludedDates, boolean processAttendees, boolean processAttachments, boolean processTags, boolean processCustomValues, ProcessReminder processReminder, Set<String> validTags) throws DAOException, IOException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		EventTagDAO tagDao = EventTagDAO.getInstance();
		EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		OEvent oevt = ManagerUtils.createOEvent(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		oevt.setRevisionStatus(EnumUtils.toSerializedName(Event.RevisionStatus.NEW));
		fillOEventWithDefaults(oevt);
		oevt.ensureCoherence();
		
		ORecurrence orec = null;
		if (processRecurrence && event.hasRecurrence()) {
			Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
			orec = new ORecurrence();
			orec.set(recur, event.getRecurrenceStartDate(), event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
			orec.setRecurrenceId(recDao.getSequence(con).intValue());
			recDao.insert(con, orec);
		}
		
		oevt.setRecurrenceId((orec != null) ? orec.getRecurrenceId() : null);
		DateTime revTimestamp = BaseDAO.createRevisionTimestamp();
		if (oevt.getReminder() != null) {
			if (ProcessReminder.NO.equals(processReminder)) {
				oevt.setReminder(null);
			} else if (ProcessReminder.DISARM_PAST.equals(processReminder) && oevt.getStartDate().isBeforeNow()) {
				oevt.setRemindedOn(revTimestamp);
			}
		}	
		evtDao.insert(con, oevt, revTimestamp);
		
		ArrayList<ORecurrenceBroken> obrks = null;
		if ((orec != null) && processExcludedDates && event.hasExcludedDates()) {
			obrks = new ArrayList<>();
			for (LocalDate ld : event.getExcludedDates()) {
				obrks.add(doRecurrenceExcludeDate(con, oevt, ld));
			}
		}
		
		if (!StringUtils.isBlank(rawICalendar)) {
			doEventICalendarInsert(con, oevt.getEventId(), rawICalendar);
		}
		
		ArrayList<OEventAttendee> oattes = null;
		if (processAttendees && (event.getAttendees() != null)) {
			oattes = new ArrayList<>();
			for (EventAttendee att : event.getAttendees()) {
				if (!ManagerUtils.validateForInsert(att)) continue;
				OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(oevt.getEventId());
				attDao.insert(con, oatt);
				oattes.add(oatt);
			}
		}
		
		Set<String> otags = null;
		if (processTags && event.hasTags()) {
			otags = new LinkedHashSet<>();
			for (String tag : event.getTags()) {
				if (validTags != null && !validTags.contains(tag)) continue;
				//TODO: optimize insertion using multivalue insert
				tagDao.insert(con, oevt.getEventId(), tag);
			}
		}
		
		ArrayList<OEventAttachment> oattchs = null;
		if (processAttachments && event.hasAttachments()) {
			oattchs = new ArrayList<>();
			for (EventAttachment att : event.getAttachments()) {
				if (!(att instanceof EventAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				oattchs.add(doEventAttachmentInsert(con, oevt.getEventId(), (EventAttachmentWithStream)att));
			}
		}
		
		ArrayList<OEventCustomValue> ocvals = null;
		if (processCustomValues && event.hasCustomValues()) {
			ocvals = new ArrayList<>(event.getCustomValues().size());
			for (CustomFieldValue cfv : event.getCustomValues().values()) {
				OEventCustomValue ocv = ManagerUtils.createOEventCustomValue(cfv);
				ocv.setEventId(oevt.getEventId());
				ocvals.add(ocv);
			}
			cvalDao.batchInsert(con, ocvals);
		}
		
		return new EventInsertResult(oevt, orec, obrks, oattes, otags, oattchs, ocvals);
	}
	
	private boolean doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean processRecurrence, boolean processExcludedDates, boolean processAttendees, boolean processAttachments, boolean processTags, boolean processCustomValues, Set<String> validTags) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		EventTagDAO tagDao = EventTagDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		// Copy original data into new event object, otherwise they will be nulled
		// in fillOEvent method below. This is a workaround: better solution is
		// to separate Update and Create objects in ManagerUtils!
		event.setPublicUid(originalEvent.getPublicUid());
		event.setOrganizer(originalEvent.getOrganizer());
		event.setHref(originalEvent.getHref());
		event.setEtag(originalEvent.getEtag());
		//if (StringUtils.isBlank(event.getOrganizer())) {
		//	event.setOrganizer(ManagerUtils.buildOrganizer(getTargetProfileId())); // Make sure organizer is filled
		//}
		ManagerUtils.fillOEvent(originalEvent, event);
		originalEvent.ensureCoherence();
		
		if (processRecurrence) {
			ORecurrence orec = recDao.select(con, originalEvent.getRecurrenceId());
			if (event.hasRecurrence() && (orec != null)) { // New event has recurrence and the old too
				Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
				boolean recurIsChanged = !ICal4jUtils.equals(recur, orec.getRecur());
				
				// Updates recurrence
				orec.set(recur, event.getRecurrenceStartDate(), event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
				recDao.update(con, orec);
				
				// If rule is changed, cleanup stored exceptions (we lose any broken events restore information) 
				if (recurIsChanged) {
					logger.debug("Recurrence rule is changed, cleaning previous broken dates...");
					rbkDao.deleteByRecurrence(con, orec.getRecurrenceId());
				}
				
				// Inserts broken records that exclude some dates
				if (processExcludedDates && event.hasExcludedDates()) {
					Set<LocalDate> exDates = doGetExcludedDates(con, originalEvent.getEventId(), originalEvent.getRecurrenceId());
					for (LocalDate ld : event.getExcludedDates()) {
						if (exDates.contains(ld)) continue;
						doRecurrenceExcludeDate(con, originalEvent, ld);
					}
				}
				
			} else if (event.hasRecurrence() && (orec == null)) { // New event has recurrence but the old doesn't
				Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
				
				// Inserts recurrence
				orec = new ORecurrence();
				orec.set(recur, null, event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
				orec.setRecurrenceId(recDao.getSequence(con).intValue());
				recDao.insert(con, orec);
				originalEvent.setRecurrenceId(orec.getRecurrenceId());
				
				// Inserts broken records that exclude some dates
				if (processExcludedDates && event.hasExcludedDates()) {
					for (LocalDate ld : event.getExcludedDates()) {
						doRecurrenceExcludeDate(con, originalEvent, ld);
					}
				}
				
			} else if (!event.hasRecurrence() && (orec != null)) { // New event doesn't have recurrence but the old does
				rbkDao.deleteByRecurrence(con, orec.getRecurrenceId());
				recDao.deleteById(con, orec.getRecurrenceId());
				originalEvent.setRecurrenceId(null);
			}
		}
		
		boolean ret = evtDao.update(con, originalEvent, revision, originalEvent.getStartDate().isAfterNow()) == 1;
		
		if (processAttendees && (event.getAttendees() != null)) {
			List<EventAttendee> oldAtts = ManagerUtils.createEventAttendeeList(atteDao.selectByEvent(con, originalEvent.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(oldAtts, event.getAttendees());
			
			for (EventAttendee att : changeSet.inserted) {
				if (!ManagerUtils.validateForInsert(att)) continue;
				final OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(originalEvent.getEventId());
				atteDao.insert(con, oatt);
			}
			for (EventAttendee att : changeSet.updated) {
				if (!ManagerUtils.validateForUpdate(att)) continue;
				final OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				atteDao.update(con, oatt);
			}
			for (EventAttendee att : changeSet.deleted) {
				atteDao.delete(con, att.getAttendeeId());
			}
		}
		
		if (processTags && event.hasTags()) {
			Set<String> oldTags = tagDao.selectTagsByEvent(con, event.getEventId());
			CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(oldTags, event.getTags());
			for (String tag : changeSet.inserted) {
				if (validTags != null && !validTags.contains(tag)) continue;
				tagDao.insert(con, event.getEventId(), tag);
			}
			for (String tag : changeSet.deleted) {
				tagDao.delete(con, event.getEventId(), tag);
			}
		}
		
		if (processAttachments && event.hasAttachments()) {
			List<EventAttachment> oldAttchs = ManagerUtils.createEventAttachmentList(attchDao.selectByEvent(con, event.getEventId()));
			CollectionChangeSet<EventAttachment> changeSet = LangUtils.getCollectionChanges(oldAttchs, event.getAttachments());

			for (EventAttachment att : changeSet.inserted) {					
				if (!(att instanceof EventAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				doEventAttachmentInsert(con, originalEvent.getEventId(), (EventAttachmentWithStream)att);
			}
			for (EventAttachment att : changeSet.updated) {
				if (!(att instanceof EventAttachmentWithStream)) continue;
				doEventAttachmentUpdate(con, (EventAttachmentWithStream)att);
			}
			for (EventAttachment att : changeSet.deleted) {
				attchDao.delete(con, att.getAttachmentId());
			}
		}
		
		if (processCustomValues && event.hasCustomValues()) {
			ArrayList<String> customFieldIds = new ArrayList<>();
			ArrayList<OEventCustomValue> ocvals = new ArrayList<>(event.getCustomValues().size());
			for (CustomFieldValue cfv : event.getCustomValues().values()) {
				OEventCustomValue ocv = ManagerUtils.createOEventCustomValue(cfv);
				ocv.setEventId(originalEvent.getEventId());
				ocvals.add(ocv);
				customFieldIds.add(ocv.getCustomFieldId());
			}
			//TODO: use upsert when available
			cvalDao.deleteByEventFields(con, originalEvent.getEventId(), customFieldIds);
			cvalDao.batchInsert(con, ocvals);
		}
		
		return ret;
	}
	
	private int doEventDelete(Connection con, int eventId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteById(con, eventId, BaseDAO.createRevisionTimestamp());
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			
			recDao.deleteByEvent(con, eventId);
			recbkDao.deleteByEvent(con, eventId);
			return evtDao.deleteById(con, eventId);
		}
	}
	
	private int doEventsDeleteByCalendar(Connection con, int calendarId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteByCalendar(con, calendarId, BaseDAO.createRevisionTimestamp());
			
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			//EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			
			//attDao.deleteByCalendar(con, calendarId);
			recbkDao.deleteByCalendar(con, calendarId);
			recDao.deleteByCalendar(con, calendarId);
			return evtDao.deleteByCalendar(con, calendarId);
		}
	}
	
	/*
	private void doEventMove(Connection con, boolean copy, Event event, int targetCalendarId) throws DAOException, IOException {
		if (copy) {
			EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
			
			event.setCalendarId(targetCalendarId);
			event.setPublicUid(null); // Reset value in order to make inner function generate new one!
			event.setHref(null); // Reset value in order to make inner function generate new one!
			OEventICalendar oica = icaDao.selectById(con, event.getEventId());
			String rawICalendar = (oica != null) ? oica.getRawData() : null;
			//TODO: maybe add support to attachments copy
			doEventInsert(con, event, rawICalendar, true, false, true, false, ProcessReminder.YES);
			
		} else {
			EventDAO evtDao = EventDAO.getInstance();
			evtDao.updateCalendar(con, event.getEventId(), targetCalendarId, BaseDAO.createRevisionTimestamp());
		}
	}
	*/
	
	private EventInsertResult doEventCopy(Connection con, Event event, int targetCalendarId) throws DAOException, IOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		
		event.setCalendarId(targetCalendarId);
		event.setPublicUid(null); // Reset value in order to make inner function generate new one!
		event.setHref(null); // Reset value in order to make inner function generate new one!
		
		OEventICalendar oica = icaDao.selectById(con, event.getEventId());
		String rawICalendar = (oica != null) ? oica.getRawData() : null;
		
		//TODO: maybe add support to attachments copy
		
		return doEventInsert(con, event, rawICalendar, true, false, true, false, true, true, ProcessReminder.YES, null);
	}
	
	private boolean doEventMove(Connection con, int eventId, int targetCalendarId) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		return evtDao.updateCalendar(con, eventId, targetCalendarId, BaseDAO.createRevisionTimestamp()) == 1;
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, OEvent recurringEvent, LocalDate instanceDate) throws DAOException {
		return doRecurrenceExcludeDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, null);
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, OEvent recurringEvent, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		return doRecurrenceExcludeDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, brokenEventId);
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, int recurringEventId, int recurrenceId, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		// 1 - inserts a broken record on excluded date
		ORecurrenceBroken orb  = new ORecurrenceBroken();
		orb.setEventId(recurringEventId);
		orb.setRecurrenceId(recurrenceId);
		orb.setEventDate(instanceDate);
		orb.setNewEventId(brokenEventId);
		rbkDao.insert(con, orb);
		return orb;
	}
	
	private List<VExpEventInstance> doEventGetExpiredForUpdate(Connection con, DateTime fromDate, DateTime toDate) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		final ArrayList<VExpEventInstance> instances = new ArrayList<>();
		
		for (VExpEvent vee : evtDao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			VExpEventInstance item = new VExpEventInstance();
			Cloner.standard().copyPropertiesOfInheritedClass(vee, item);
			item.setKey(EventKey.buildKey(vee.getEventId(), vee.getSeriesEventId()));
			instances.add(item);
		}
		for (VExpEvent vee : evtDao.viewRecurringExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			// Returns 15 instances only, this should be enough for serving max day range from underlying reminder
			instances.addAll(calculateRecurringInstances_OLD(con, new VExpEventInstanceMapper(vee), fromDate, toDate, DateTimeZone.UTC, 14+1));
		}
		
		return instances;
	}
	
	private boolean doEventICalendarInsert(Connection con, int eventId, String rawICalendar) throws DAOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		
		OEventICalendar ovca = new OEventICalendar();
		ovca.setEventId(eventId);
		ovca.setRawData(rawICalendar);
		return icaDao.insert(con, ovca) == 1;
	}
	
	private OEventAttachment doEventAttachmentInsert(Connection con, int eventId, EventAttachmentWithStream attachment) throws DAOException, IOException {
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEventAttachment oattch = ManagerUtils.createOEventAttachment(attachment);
		oattch.setEventAttachmentId(IdentifierUtils.getUUIDTimeBased());
		oattch.setEventId(eventId);
		attchDao.insert(con, oattch, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attchDao.insertBytes(con, oattch.getEventAttachmentId(), IOUtils.toByteArray(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		return oattch;
	}
	
	private boolean doEventAttachmentUpdate(Connection con, EventAttachmentWithStream attachment) throws DAOException, IOException {
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEventAttachment oattch = ManagerUtils.createOEventAttachment(attachment);
		attchDao.update(con, oattch, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attchDao.deleteBytes(con, oattch.getEventAttachmentId());
			return attchDao.insertBytes(con, oattch.getEventAttachmentId(), IOUtils.toByteArray(is)) == 1;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private OCalendar fillOCalendarWithDefaults(OCalendar tgt) {
		if (tgt != null) {
			CalendarServiceSettings ss = getServiceSettings();
			if (tgt.getDomainId() == null) tgt.setDomainId(getTargetProfileId().getDomainId());
			if (tgt.getUserId() == null) tgt.setUserId(getTargetProfileId().getUserId());
			if (tgt.getBuiltIn() == null) tgt.setBuiltIn(false);
			if (StringUtils.isBlank(tgt.getProvider())) tgt.setProvider(EnumUtils.toSerializedName(Calendar.Provider.LOCAL));
			if (StringUtils.isBlank(tgt.getColor())) tgt.setColor("#FFFFFF");
			if (StringUtils.isBlank(tgt.getSync())) tgt.setSync(EnumUtils.toSerializedName(ss.getDefaultCalendarSync()));
			if (tgt.getIsDefault() == null) tgt.setIsDefault(false);
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
			if (tgt.getBusy() == null) tgt.setBusy(true);
			if (tgt.getInvitation() == null) tgt.setInvitation(false);
			//if (tgt.getNotifyOnSelfUpdate() == null) tgt.setNotifyOnSelfUpdate(false); // Not yet supported!
			if (tgt.getNotifyOnExtUpdate() == null) tgt.setNotifyOnExtUpdate(false);
			
			Calendar.Provider provider = EnumUtils.forSerializedName(tgt.getProvider(), Calendar.Provider.class);
			if (Calendar.Provider.WEBCAL.equals(provider) || Calendar.Provider.CALDAV.equals(provider)) {
				tgt.setIsDefault(false);
			}
		}
		return tgt;
	}
	
	private OEvent fillOEventWithDefaults(OEvent tgt) {
		if (tgt != null) {
			if (StringUtils.isBlank(tgt.getPublicUid())) {
				tgt.setPublicUid(ManagerUtils.buildEventUid(tgt.getEventId(), WT.getPrimaryDomainName(getTargetProfileId().getDomainId())));
			}
			if (StringUtils.isBlank(tgt.getHref())) tgt.setHref(ManagerUtils.buildHref(tgt.getEventId(), WT.getPrimaryDomainName(getTargetProfileId().getDomainId())));
			if (tgt.getReadOnly() == null) tgt.setReadOnly(false);
			if (StringUtils.isBlank(tgt.getOrganizer())) tgt.setOrganizer(ManagerUtils.buildOrganizer(getTargetProfileId()));
		}
		return tgt;
	}
	
	private List<RecipientTuple> getModificationRecipients(int calendarId, String crud) throws WTException {
		ArrayList<RecipientTuple> rcpts = new ArrayList<>();
		
		// Parameter crud is not used for now!
		
		OCalendarOwnerInfo ocoi = doCalendarGetOwnerInfo(calendarId);
		if (ocoi != null) {
			UserProfileId owner = ocoi.getProfileId();
			if (ocoi.getNotifyOnExtUpdate() && !owner.equals(RunContext.getRunProfileId())) {
				UserProfile.Data ud = WT.getUserData(owner);
				if (ud != null) {
					rcpts.add(new RecipientTuple(ud.getPersonalEmail(), owner));
				}
			}
		}
		return rcpts;
	}
	
	private void notifyForEventModification(UserProfileId fromProfileId, List<RecipientTuple> recipients, EventFootprint event, String crud) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, fromProfileId.getDomainId());
		UserProfile.Data udFrom = WT.getUserData(fromProfileId);
		InternetAddress from = udFrom.getPersonalEmail();
		
		Map<String, String> meetingProviders = css.getMeetingProviders();
		Session session = getMailSession();
		for (RecipientTuple rcpt : recipients) {
			if (!InternetAddressUtils.isAddressValid(rcpt.recipient)) {
				logger.warn("Recipient for event modification is invalid [{}]", rcpt.recipient);
				continue;
			}
			UserProfile.Data ud = WT.getUserData(rcpt.refProfileId);
			if (ud == null) continue;
			
			try {
				String title = TplHelper.buildEventModificationTitle(ud.getLocale(), event, crud);
				String customBodyHtml = TplHelper.buildEventModificationBody(event, ud.getLocale(), ud.getShortDateFormat(), ud.getShortTimeFormat(), meetingProviders);
				String source = EmailNotification.buildSource(ud.getLocale(), SERVICE_ID);
				String because = lookupResource(ud.getLocale(), CalendarLocale.EMAIL_EVENTMODIFICATION_FOOTER_BECAUSE);

				String subject = EmailNotification.buildSubject(ud.getLocale(), SERVICE_ID, title);
				String html = new EmailNotification.BecauseBuilder()
						.withCustomBody(title, customBodyHtml)
						.build(ud.getLocale(), source, because, rcpt.recipient.getAddress()).write();
				
				WT.sendEmail(session, true, from, rcpt.recipient, subject, html);

			} catch(IOException | TemplateException | MessagingException ex) {
				logger.error("Unable to notify recipient after event modification [{}]", ex, rcpt.recipient);
			}
		}	
	}
	
	private List<RecipientTuple> getInvitationRecipients(UserProfileId ownerProfile, Event event, String crud) {
		ArrayList<RecipientTuple> rcpts = new ArrayList<>();
		
		// Parameter crud is not used for now!
		
		if (!event.getAttendees().isEmpty()) {
			String organizerAddress = event.getOrganizerAddress();
			for (EventAttendee attendee : event.getAttendees()) {
				if (!attendee.getNotify()) continue;
				InternetAddress attendeeIa = attendee.getRecipientInternetAddress();
				if (attendeeIa == null) continue;
				if (StringUtils.equalsIgnoreCase(organizerAddress, attendeeIa.getAddress())
						&& !EventAttendee.ResponseStatus.NEEDS_ACTION.equals(attendee.getResponseStatus())) continue;
				
				UserProfileId attProfileId = WT.guessUserProfileIdByEmailAddress(attendeeIa.getAddress());
				rcpts.add(new RecipientTuple(attendeeIa, (attProfileId != null) ? attProfileId : ownerProfile));
			}
		}
		
		return rcpts;
	}
	
	private void notifyForInvitation(UserProfileId senderProfileId, List<RecipientTuple> recipients, Event event, String crud) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, senderProfileId.getDomainId());
		ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
		net.fortuna.ical4j.model.property.Method icalMethod = crud.equals(Crud.DELETE) ? net.fortuna.ical4j.model.property.Method.CANCEL : net.fortuna.ical4j.model.property.Method.REQUEST;
		
		try {
			InternetAddress from = WT.getUserData(senderProfileId).getPersonalEmail();
			String servicePublicUrl = WT.getServicePublicUrl(senderProfileId.getDomainId(), SERVICE_ID);
			
			// Creates ical content
			net.fortuna.ical4j.model.Calendar ical = out.toCalendar(icalMethod, event);
			//net.fortuna.ical4j.model.Calendar ical = ICalHelper.toCalendar(icalMethod, prodId, event);
			
			// Creates base message parts
			String icalText = ICalendarUtils.calendarToString(ical);
			MimeBodyPart calPart = ICalendarUtils.createInvitationCalendarPart(icalMethod, icalText);
			String filename = ICalendarUtils.buildICalendarAttachmentFilename(WT.getPlatformName());
			MimeBodyPart attPart = ICalendarUtils.createInvitationAttachmentPart(icalText, filename);
			
			Map<String, String> meetingProviders = css.getMeetingProviders();
			IMailManager mailMgr = (IMailManager)WT.getServiceManager("com.sonicle.webtop.mail");
			//FIXME: if mailMgr is not present, send of base SMTP
			Session session = getMailSession();
			for (RecipientTuple rcpt : recipients) {
				if (!InternetAddressUtils.isAddressValid(rcpt.recipient)) {
					logger.warn("Recipient for event invitation is invalid [{}]", rcpt.recipient);
					continue;
				}
				UserProfile.Data ud = WT.getUserData(rcpt.refProfileId);
				if (ud == null) continue;
				
				try {
					String title = TplHelper.buildEventInvitationTitle(ud.getLocale(), ud.getShortDateFormat(), ud.getShortTimeFormat(), event.getFootprint(), crud);
					String customBodyHtml = TplHelper.buildTplEventInvitationBody(crud, event, rcpt.recipient.getAddress(), ud.getLocale(), ud.getShortDateFormat(), ud.getShortTimeFormat(), meetingProviders, servicePublicUrl);
					String source = NotificationHelper.buildSource(ud.getLocale(), SERVICE_ID);
					String because = lookupResource(ud.getLocale(), CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_BECAUSE);

					String subject = EmailNotification.buildSubject(ud.getLocale(), SERVICE_ID, title);
					String html = TplHelper.buildEventInvitationHtml(ud.getLocale(), event.getTitle(), customBodyHtml, source, because, rcpt.recipient.getAddress(), crud);
					
					MimeMultipart mmp = ICalendarUtils.createInvitationPart(html, calPart, attPart);
					sendMail(session, mailMgr, from, rcpt.recipient, subject, mmp);
					
				} catch(IOException | TemplateException | MessagingException ex1) {
					logger.warn("Unable to send invitation [{}]", ex1, rcpt.recipient.getAddress());
				}
			}
			
		} catch(IOException | MessagingException | WTException ex) {
			logger.warn("Unable to prepare invite notification", ex);
		}
	}
	
	/*
	private static enum ReplyingAttendeeType {
		ID, ADDRESS;
	}
	
	private void notifyOrganizer2(final UserProfileId sendingProfileId, final Event event, final ReplyingAttendeeType replyingType, final String replyingAttendee) throws WTException {
		final String sentFolder = null;
		
		try {
			// Find the attendee (in event) that has updated its response
			EventAttendee targetAttendee = null;
			for (EventAttendee attendee : event.getAttendees()) {
				boolean match = false;
				if (ReplyingAttendeeType.ID.equals(replyingType)) {
					match = attendee.getAttendeeId().equals(replyingAttendee);
					
				} else if (ReplyingAttendeeType.ADDRESS.equals(replyingType)) {
					final InternetAddress ia = InternetAddressUtils.toInternetAddress(replyingAttendee);
					match = (ia != null && StringUtils.equals(ia.getAddress(), attendee.getRecipientAddress()));
				}
				
				if (match) {
					targetAttendee = attendee;
					break;
				}
			}
			if (targetAttendee == null) throw new WTException("Attendee not found [{0}]", updatedAttendeeId);
			
			
		}
		
		WT.sendEmail(sendingProfileId, email, sentFolder);
	}
	*/
	
	private void replyToOrganizer(final UserProfileId sendingProfileId, final Event event, final String replyingAttendeeId, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		// Find the attendee (in event) that has updated its response
		EventAttendee targetAttendee = null;
		for (EventAttendee attendee : event.getAttendees()) {
			if (attendee.getAttendeeId().equals(replyingAttendeeId)) {
				targetAttendee = attendee;
				break;
			}
		}
		if (targetAttendee == null) throw new WTException("Attendee not found [{0}]", replyingAttendeeId);
		
		ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
		final net.fortuna.ical4j.model.Calendar icalRequest = out.toCalendar(net.fortuna.ical4j.model.property.Method.REQUEST, event);
		replyToOrganizer(sendingProfileId, targetAttendee.getRecipientInternetAddress(), icalRequest, response);
	}
	
	public void replyToOrganizer(final InternetAddress forAddress, final net.fortuna.ical4j.model.Calendar icalRequest, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		replyToOrganizer(getTargetProfileId(), forAddress, icalRequest, response);
	}
	
	private void replyToOrganizer(final UserProfileId sendingProfileId, final InternetAddress forAddress, final net.fortuna.ical4j.model.Calendar icalRequest, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		final Locale locale = getProfileOrTargetLocale(sendingProfileId);
		
		final String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
		final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ICalendarUtils.getVEvent(icalRequest));
		final EmailMessage email = com.sonicle.webtop.core.util.ICalendarHelper.prepareICalendarReply(prodId, icalRequest, forAddress, iaOrganizer, response, locale);
		WT.sendEmailMessage(sendingProfileId, email);
	}
	
	private void notifyOrganizer(UserProfileId senderProfileId, Event event, String updatedAttendeeId) {
		CoreUserSettings cus = new CoreUserSettings(senderProfileId);
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, senderProfileId.getDomainId());
		String dateFormat = cus.getShortDateFormat();
		String timeFormat = cus.getShortTimeFormat();
		Locale locale = getProfileOrTargetLocale(senderProfileId);
		
		try {
			// Find the attendee (in event) that has updated its response
			EventAttendee targetAttendee = null;
			for(EventAttendee attendee : event.getAttendees()) {
				if (attendee.getAttendeeId().equals(updatedAttendeeId)) {
					targetAttendee = attendee;
					break;
				}
			}
			if (targetAttendee == null) throw new WTException("Attendee not found [{0}]", updatedAttendeeId);
			Map<String, String> meetingProviders = css.getMeetingProviders();
			
			InternetAddress from = WT.getNotificationAddress(senderProfileId.getDomainId());
			InternetAddress to = InternetAddressUtils.toInternetAddress(event.getOrganizer());
			if (!InternetAddressUtils.isAddressValid(to)) throw new WTException("Organizer address not valid [{0}]", event.getOrganizer());
			
			String servicePublicUrl = WT.getServicePublicUrl(senderProfileId.getDomainId(), SERVICE_ID);
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String subject = TplHelper.buildResponseUpdateTitle(locale, event, targetAttendee);
			String customBodyHtml = TplHelper.buildTplResponseUpdateBody(event, locale, dateFormat, timeFormat, meetingProviders, servicePublicUrl);
			
			EmailNotification.NoReplyBuilder builder = new EmailNotification.NoReplyBuilder()
					.withCustomBody(event.getTitle(), customBodyHtml);
			
			if (EventAttendee.ResponseStatus.ACCEPTED.equals(targetAttendee.getResponseStatus())) {
				builder.greenMessage(MessageFormat.format(lookupResource(locale, CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_ACCEPTED), targetAttendee.getRecipient()));
			} else if (EventAttendee.ResponseStatus.TENTATIVE.equals(targetAttendee.getResponseStatus())) {
				builder.yellowMessage(MessageFormat.format(lookupResource(locale, CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_TENTATIVE), targetAttendee.getRecipient()));
			} else if (EventAttendee.ResponseStatus.DECLINED.equals(targetAttendee.getResponseStatus())) {
				builder.redMessage(MessageFormat.format(lookupResource(locale, CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_DECLINED), targetAttendee.getRecipient()));
			} else {
				builder.greyMessage(MessageFormat.format(lookupResource(locale, CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_OTHER), targetAttendee.getRecipient()));
			}
			String html = builder.build(locale, source).write();
			WT.sendEmail(getMailSession(), true, from, to, subject, html);
			
		} catch(Exception ex) {
			logger.warn("Unable to notify organizer", ex);
		}	
	}
	
	/*
	private void notifyAttendees(String crud, Event event) {
		notifyAttendees(getTargetProfileId(), crud, event);
	}
	
	private void notifyAttendees(UserProfileId senderProfileId, String crud, Event event) {
		if (event.getAttendees().isEmpty()) return;
		
		try {
			String organizerAddress = event.getOrganizerAddress();
			
			// Finds attendees to be notified...
			ArrayList<EventAttendee> toBeNotified = new ArrayList<>();
			for (EventAttendee attendee : event.getAttendees()) {
				if (!attendee.getNotify()) continue;
				if (StringUtils.equalsIgnoreCase(organizerAddress, attendee.getRecipientAddress())
						&& !EventAttendee.RESPONSE_STATUS_NEEDSACTION.equals(attendee.getResponseStatus())) continue;
				toBeNotified.add(attendee);
				//if (attendee.getNotify()) toBeNotified.add(attendee);
			}
			
			if (!toBeNotified.isEmpty()) {
				UserProfile.Data ud = WT.getUserData(senderProfileId);
				CoreUserSettings cus = new CoreUserSettings(senderProfileId);
				String dateFormat = cus.getShortDateFormat();
				String timeFormat = cus.getShortTimeFormat();
				
				String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
				net.fortuna.ical4j.model.property.Method icalMethod = crud.equals(Crud.DELETE) ? net.fortuna.ical4j.model.property.Method.CANCEL : net.fortuna.ical4j.model.property.Method.REQUEST;
				
				// Creates ical content
				net.fortuna.ical4j.model.Calendar ical = ICalHelper.toCalendar(icalMethod, prodId, event);
				
				// Creates base message parts
				String icalText = ICalendarUtils.calendarToString(ical);
				MimeBodyPart calPart = ICalendarUtils.createInvitationCalendarPart(icalMethod, icalText);
				String filename = ICalendarUtils.buildICalendarAttachmentFilename(WT.getPlatformName());
				MimeBodyPart attPart = ICalendarUtils.createInvitationAttachmentPart(icalText, filename);
				
				String source = NotificationHelper.buildSource(ud.getLocale(), SERVICE_ID);
				String subject = TplHelper.buildEventInvitationEmailSubject(ud.getLocale(), dateFormat, timeFormat, event, crud);
				String because = lookupResource(ud.getLocale(), CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_BECAUSE);
				
				String servicePublicUrl = WT.getServicePublicUrl(senderProfileId.getDomainId(), SERVICE_ID);
				IMailManager mailMgr = (IMailManager)WT.getServiceManager("com.sonicle.webtop.mail");
				Session session = getMailSession();
				InternetAddress from = ud.getEmail();
				for (EventAttendee attendee : toBeNotified) {
					InternetAddress to = InternetAddressUtils.toInternetAddress(attendee.getRecipient());
					if (InternetAddressUtils.isAddressValid(to)) {
						final String customBody = TplHelper.buildEventInvitationBodyTpl(ud.getLocale(), dateFormat, timeFormat, event, crud, attendee.getAddress(), servicePublicUrl);
						final String html = TplHelper.buildInvitationTpl(ud.getLocale(), source, attendee.getAddress(), event.getTitle(), customBody, because, crud);
						
						try {
							MimeMultipart mmp = ICalendarUtils.createInvitationPart(html, calPart, attPart);
							if (mailMgr != null) {
								try {
									mailMgr.sendMessage(from, Arrays.asList(to), null, null, subject, mmp);
								} catch(WTException ex1) {
									logger.warn("Unable to send using mail service", ex1);
									WT.sendEmail(session, from, Arrays.asList(to), null, null, subject, mmp);
								}
							} else {
								WT.sendEmail(session, from, Arrays.asList(to), null, null, subject, mmp);
							}
							
						} catch(MessagingException ex) {
							logger.warn("Unable to send notification to attendee {}", to.toString());
						}
					}
				}
			}
		} catch(Exception ex) {
			logger.warn("Unable notify attendees", ex);
		}		
	}
	*/
	
	private boolean needsTreatAsPrivate(UserProfileId runningProfile, OEventInfo eventInfo) {
		return needsTreatAsPrivate(runningProfile, eventInfo.getIsPrivate(), eventInfo.getCalendarId());
	}
	
	private boolean needsTreatAsPrivate(UserProfileId runningProfile, boolean eventIsPrivate, int eventCalendarId) {
		if (!eventIsPrivate) return false;
		UserProfileId ownerProfile = ownerCache.get(eventCalendarId);
		return needsTreatAsPrivate(runningProfile, ownerProfile, eventIsPrivate);
	}
	
	private boolean needsTreatAsPrivate(UserProfileId runningProfile, UserProfileId eventOwner, boolean eventIsPrivate) {
		if (!eventIsPrivate) return false;
		if (RunContext.isWebTopAdmin(runningProfile)) return false;
		return !eventOwner.equals(runningProfile);
	}
	
	private void sendMail(Session session, IMailManager mailMgr, InternetAddress from, InternetAddress to, String subject, MimeMultipart mpart) throws MessagingException {
		boolean fallback = true;
		if (mailMgr != null) {
			try {
				mailMgr.sendMessage(from, Arrays.asList(to), null, null, subject, mpart);
				fallback = false;
			} catch(WTException ex) {
				logger.warn("Unable to send using mail service, falling back to standard send...", ex);
			}
		}
		if (fallback) WT.sendEmail(session, from, Arrays.asList(to), null, null, subject, mpart); 
	}
	
	private VVEventInstance cloneEvent(VVEventInstance sourceEvent, DateTime newStart, DateTime newEnd) {
		VVEventInstance event = new VVEventInstance(sourceEvent);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private void onAfterCalendarAction(int calendarId, UserProfileId owner) {
		if (!owner.equals(getTargetProfileId())) shareCache.init();
	}
	
	private void checkRightsOnCalendarOrigin(UserProfileId originPid, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if (RunContext.isWebTopAdmin()) return;
		if (originPid.equals(targetPid)) return;
		
		final CalendarFSOrigin origin = shareCache.getOrigin(originPid);
		if (origin == null) throw new WTException("Origin not found [{}]", originPid);
		CoreManager coreMgr = WT.getCoreManager(targetPid);
		
		boolean result = coreMgr.evaluateFolderSharePermission(SERVICE_ID, GROUPNAME_CALENDAR, origin.getProfileId(), FolderSharing.Scope.wildcard(), true, FolderShare.EvalTarget.FOLDER, action);
		if (result) return;
		UserProfileId runPid = RunContext.getRunProfileId();
		throw new AuthException("Action '{}' not allowed for '{}' on origin '{}' [{}, {}]", action, runPid, origin.getProfileId(), GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private boolean quietlyCheckRightsOnCalendar(int calendarId, BitFlagsEnum<? extends Enum> right) {
		try {
			checkRightsOnCalendar(calendarId, right);
			return true;
		} catch (AuthException ex1) {
			return false;
		} catch (WTException ex1) {
			logger.warn("Unable to check rights [{}]", calendarId, ex1);
			return false;
		}
	}
	
	private void checkRightsOnCalendar(Set<Integer> okCache, int calendarId, BitFlagsEnum<? extends Enum> right) throws WTException {
		if (!okCache.contains(calendarId)) {
			checkRightsOnCalendar(calendarId, right);
			okCache.add(calendarId);
		}
	}
	
	private void checkRightsOnCalendar(int calendarId, BitFlagsEnum<? extends Enum> right) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		Subject subject = RunContext.getSubject();
		UserProfileId runPid = RunContext.getRunProfileId(subject);
		
		FolderShare.EvalTarget target = null;
		if (right instanceof FolderShare.FolderRight) {
			target = FolderShare.EvalTarget.FOLDER;
		} else if (right instanceof FolderShare.ItemsRight) {
			target = FolderShare.EvalTarget.FOLDER_ITEMS;
		} else {
			throw new WTRuntimeException("Unsupported right");
		}
		
		final UserProfileId ownerPid = ownerCache.get(calendarId);
		if (ownerPid == null) throw new WTException("Owner not found [{}]", calendarId);
		
		if (RunContext.isWebTopAdmin(subject)) {
			// Skip checks for running wtAdmin and sysAdmin target
			if (targetPid.equals(RunContext.getSysAdminProfileId())) return;
			// Skip checks if target is the resource owner
			if (ownerPid.equals(targetPid)) return;
			// Skip checks if resource is a valid incoming folder
			if (shareCache.getFolderIds().contains(calendarId)) return;
			
			String exMsg = null;
			if (FolderShare.EvalTarget.FOLDER.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on folder '{}' [{}, {}]";
			} else if (FolderShare.EvalTarget.FOLDER_ITEMS.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on elements of folder '{}' [{}, {}]";
			}
			
			throw new AuthException(exMsg, right.name(), runPid, calendarId, GROUPNAME_CALENDAR, targetPid.toString());
			
		} else {
			// Skip checks if target is the resource owner and it's the running profile
			if (ownerPid.equals(targetPid) && targetPid.equals(runPid)) return;
			
			CalendarFSOrigin origin = shareCache.getOriginByFolderId(calendarId);
			if (origin == null) throw new WTException("Origin not found [{}]", calendarId);
			CoreManager coreMgr = WT.getCoreManager(targetPid);
			
			Boolean eval = null;
			if (origin.isResource()) {
				// Check right at wildcard scope
				eval = coreMgr.evaluateFolderSharePermission(CoreManifest.ID, "RESOURCE", ownerPid, FolderSharing.Scope.wildcard(), false, target, right.name());
				if (eval != null && eval == true) return;
			} else {
				// Check right at wildcard scope
				eval = coreMgr.evaluateFolderSharePermission(SERVICE_ID, GROUPNAME_CALENDAR, ownerPid, FolderSharing.Scope.wildcard(), false, target, right.name());
				if (eval != null && eval == true) return;
				// Check right at folder scope
				eval = coreMgr.evaluateFolderSharePermission(SERVICE_ID, GROUPNAME_CALENDAR, ownerPid, FolderSharing.Scope.folder(String.valueOf(calendarId)), false, target, right.name());
				if (eval != null && eval == true) return;
			}	
			
			String exMsg = null;
			if (FolderShare.EvalTarget.FOLDER.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on folder '{}' [{}, {}, {}]";
			} else if (FolderShare.EvalTarget.FOLDER_ITEMS.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on elements of folder '{}' [{}, {}, {}]";
			}
			throw new AuthException(exMsg, right.name(), runPid, calendarId, ownerPid, GROUPNAME_CALENDAR, targetPid.toString());
		}
	}
	
	private ReminderInApp createEventReminderAlertWeb(VExpEventInstance instance) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, instance.getCalendarProfileId(), "event", instance.getKey());
		alert.setTitle(instance.getTitle());
		alert.setDate(instance.getStartDate().withZone(instance.getDateTimeZone()));
		alert.setTimezone(instance.getTimezone());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, String dateFormat, String timeFormat, String recipientEmail, UserProfileId ownerId, EventInstance event) throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, ownerId.getDomainId());
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, ownerId, "event", event.getKey());
		
		try {
			Map<String, String> meetingProviders = css.getMeetingProviders();
			
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String because = lookupResource(locale, CalendarLocale.EMAIL_REMINDER_FOOTER_BECAUSE);
			String customBodyHtml = TplHelper.buildTplEventReminderBody(event, locale, dateFormat, timeFormat, meetingProviders);
			
			String title = TplHelper.buildEventReminderTitle(locale, dateFormat, timeFormat, event.getFootprint());
			String html = TplHelper.buildEventInvitationHtml(locale, event.getTitle(), customBodyHtml, source, because, recipientEmail, null);
			//String body = TplHelper.buildInvitationTpl(locale, source, recipientEmail, event.getTitle(), customBodyHtml, because, null);
			
			alert.setSubject(EmailNotification.buildSubject(locale, SERVICE_ID, title));
			alert.setBody(html);
			
		} catch(IOException | TemplateException | AddressException ex) {
			throw new WTException(ex);
		}
		return alert;
	}
	
	private void storeAsSuggestion(CoreManager coreMgr, String context, String value) {
		if (StringUtils.isBlank(value)) return;
		coreMgr.addServiceStoreEntry(SERVICE_ID, context, value.toUpperCase(), value);
	}
	
	
	private enum DoOption {
		SKIP, UPDATE
	}
	
	private class SchedEventInstanceMapper implements RecurringInstanceMapper<SchedEventInstance> {
		private final VVEvent event;
		private final boolean censorize;
		
		public SchedEventInstanceMapper(VVEvent event, boolean censorize) {
			this.event = event;
			this.censorize = censorize;
		}
		
		@Override
		public int getEventId() {
			return event.getEventId();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getEventStartDate() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEndDate() {
			return event.getEndDate();
		}
		
		@Override
		public DateTimeZone getEventTimezone() {
			return event.getDateTimezone();
		}
		
		@Override
		public SchedEventInstance createInstance(String key, DateTime startDate, DateTime endDate) {
			SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), event);
			item.setKey(key);
			item.setStartDate(startDate);
			item.setEndDate(endDate);
			if (censorize) item.censorize();
			return item;
		}
	}
	
	private class VExpEventInstanceMapper implements RecurringInstanceMapper<VExpEventInstance> {
		private final VExpEvent event;
		
		public VExpEventInstanceMapper(VExpEvent event) {
			this.event = event;
		}
		
		@Override
		public int getEventId() {
			return event.getEventId();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getEventStartDate() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEndDate() {
			return event.getEndDate();
		}
		
		@Override
		public DateTimeZone getEventTimezone() {
			return event.getDateTimeZone();
		}
		
		@Override
		public VExpEventInstance createInstance(String key, DateTime startDate, DateTime endDate) {
			VExpEventInstance item = new VExpEventInstance();
			Cloner.standard().copyPropertiesOfInheritedClass(event, item);
			item.setKey(key);
			item.setStartDate(startDate);
			item.setEndDate(endDate);
			
			/*
			VExpEventInstance item = new VExpEventInstance();
			item.setKey(key);
			item.setEventId(event.getEventId());
			item.setCalendarId(event.getCalendarId());
			item.setRecurrenceId(event.getRecurrenceId());
			item.setStartDate(startDate);
			item.setEndDate(endDate);
			item.setTimezone(event.getTimezone());
			item.setAllDay(event.getAllDay());
			item.setTitle(event.getTitle());
			item.setReminder(event.getReminder());
			item.setRemindedOn(event.getRemindedOn());
			item.setCalendarDomainId(event.getCalendarDomainId());
			item.setCalendarUserId(event.getCalendarUserId());
			item.setSeriesEventId(event.getSeriesEventId());
			item.setHasAttendees(event.getHasAttendees());
			//item.setRecurInfo(src.isEventRecurring(), src.isEventBroken());
			*/
			return item;
		}
	}
	
	private class EventMapper implements RecurringInstanceMapper<Event> {
		private final Event event;
		final Cloner cloner;
		
		public EventMapper(Event event) {
			this.event = event;
			this.cloner = Cloner.standard();
		}
		
		@Override
		public int getEventId() {
			return event.getEventId();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getEventStartDate() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEndDate() {
			return event.getEndDate();
		}
		
		@Override
		public DateTimeZone getEventTimezone() {
			return event.getDateTimeZone();
		}
		
		@Override
		public Event createInstance(String key, DateTime startDate, DateTime endDate) {
			Event clone = cloner.deepClone(event);
			clone.setStartDate(startDate);
			clone.setEndDate(endDate);
			return clone;
		}
	}
	
	private class VVEventInstanceMapper implements RecurringInstanceMapper<VVEventInstance> {
		private final VVEvent event;
		final Cloner cloner;
		
		public VVEventInstanceMapper(VVEvent event) {
			this.event = event;
			this.cloner = Cloner.standard();
		}
		
		@Override
		public int getEventId() {
			return event.getEventId();
		}

		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getEventStartDate() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEndDate() {
			return event.getEndDate();
		}
		
		@Override
		public DateTimeZone getEventTimezone() {
			return event.getDateTimezone();
		}
		
		@Override
		public VVEventInstance createInstance(String key, DateTime startDate, DateTime endDate) {
			VVEvent clone = cloner.deepClone(event);
			clone.setStartDate(startDate);
			clone.setEndDate(endDate);
			return new VVEventInstance(key, clone);	
		}
	}
	
	private interface RecurringInstanceMapper<T> {
		public int getEventId();
		public boolean isEventAllDay();
		public DateTime getEventStartDate();
		public DateTime getEventEndDate();
		public DateTimeZone getEventTimezone();
		public T createInstance(String key, DateTime startDate, DateTime endDate);
	}
	
	private class EventBoundsRecurringContext implements RecurringInstanceContext<ComparableEventBounds> {
		private final Event event;
		
		public EventBoundsRecurringContext(Event event) {
			this.event = event;
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return event.getDateTimeZone();
		}

		@Override
		public Integer getEventId() {
			return event.getEventId();
		}

		@Override
		public DateTime getEventStart() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEnd() {
			return event.getEndDate();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getRecurrenceStart() {
			LocalDate localDate = event.getRecurrenceStartDate();
			if (localDate != null) {
				DateTimeZone timezone = event.getDateTimeZone();
				return localDate.toDateTime(event.getStartDate().withZone(timezone).toLocalTime(), timezone);
			} else {
				return null;
			}
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return ICal4jUtils.parseRRule(event.getRecurrenceRule());
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return event.getExcludedDates();
		}
		
		@Override
		public ComparableEventBounds createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			return new ComparableEventBounds(start, end, id);
		}
	}
	
	private class VEventFootprintBoundsRecurringContext implements RecurringInstanceContext<ComparableEventBounds> {
		private final VEventFootprint event;
		private final ORecurrence recurrence;
		private final Set<LocalDate> recurrenceExclDates;
		
		public VEventFootprintBoundsRecurringContext(Connection con, VEventFootprint event) {
			this.event = event;
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
			this.recurrence = recDao.selectByEvent(con, event.getEventId());
			this.recurrenceExclDates = (recurrence != null) ? recbDao.selectDatesByEventRecurrence(con, event.getEventId(), recurrence.getRecurrenceId()) : null;
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return event.getTimezoneObject();
		}

		@Override
		public Integer getEventId() {
			return event.getEventId();
		}

		@Override
		public DateTime getEventStart() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEnd() {
			return event.getEndDate();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return recurrence != null ? recurrence.getStartDate(): null;
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return recurrence != null ? recurrence.getRecur() : null;
		}	

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return recurrenceExclDates;
		}
		
		@Override
		public ComparableEventBounds createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			return new ComparableEventBounds(start, end, id);
		}
	}
	
	private interface RecurringInstanceContext<T> {
		public DateTimeZone getTimezone();
		public Integer getEventId();
		public DateTime getEventStart();
		public DateTime getEventEnd();
		public boolean isEventAllDay();
		public DateTime getRecurrenceStart();
		public Recur getRecurrenceDefinition();
		public Set<LocalDate> getRecurrenceExclDates();
		public T createInstance(EventInstanceId id, DateTime start, DateTime due, String legacyKey);
	}
	
	private <T> List<T> calculateRecurringInstances(final RecurringInstanceContext<T> context, final DateTime spanFrom, final DateTime spanTo, final int limitNoOfInstances) throws WTException {
		Check.notNull(context, "context");
		ArrayList<T> instances = new ArrayList<>();
		
		final DateTimeZone timezone = context.getTimezone();
		final Integer eventId = context.getEventId();
		final DateTime eventStart = context.getEventStart();
		final DateTime eventEnd = context.getEventEnd();
		final DateTime recurStart = context.getRecurrenceStart();
		final Recur recur = context.getRecurrenceDefinition();
		
		if (eventStart != null && eventEnd != null && recurStart != null && recur != null) {
			LocalTime eventStartTime = eventStart.withZone(timezone).toLocalTime();
			LocalTime eventEndTime = eventEnd.withZone(timezone).toLocalTime();
			Integer dueDays = Math.abs(Days.daysBetween(eventStart.toLocalDate(), eventEnd.toLocalDate()).getDays());
			
			// Define initial computation range taken from parameters
			DateTime rangeFrom = spanFrom;
			DateTime rangeTo = spanTo;
			
			// No range specified, set a default window starting recurrence start
			if (rangeFrom == null) rangeFrom = recurStart;
			if (rangeTo == null) recurStart.plusYears(1);
			
			List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, recurStart, context.isEventAllDay(), context.getRecurrenceExclDates(), eventStart, eventEnd, timezone, rangeFrom, rangeTo, limitNoOfInstances);
			for (LocalDate date : dates) {
				final DateTime start = DateTimeUtils.toDateTime(date, eventStartTime, timezone);
				final DateTime end = DateTimeUtils.toDateTime(date.plusDays(dueDays), eventEndTime, timezone);
				final EventInstanceId id = EventInstanceId.build(String.valueOf(eventId), start, timezone);
				final String legacyKey = EventKey.buildKey(eventId, eventId, date);
				instances.add(context.createInstance(id, start, end, legacyKey));
			}
		} else {
			if (eventStart == null) logger.warn("Event has NO valid start instant [{}]", eventId);
			if (eventEnd == null) logger.warn("Event has NO valid end instant [{}]", eventId);
			if (recurStart == null) logger.warn("Event has NO recurrence start instant", eventId);
			if (recur == null) logger.warn("Event has NO recurrence rule [{}]", eventId);
		}
		return instances;
	}
	
	public static class EventInsertResult {
		public final OEvent oevent;
		public final ORecurrence orecurrence;
		public final List<ORecurrenceBroken> orecurrenceBrokens;
		public final List<OEventAttendee> oattendees;
		public final Set<String> otags;
		public final List<OEventAttachment> oattachments;
		public final List<OEventCustomValue> ocustomvalues;
		
		public EventInsertResult(OEvent oevent, ORecurrence orecurrence, List<ORecurrenceBroken> orecurrenceBrokens, ArrayList<OEventAttendee> oattendees, Set<String> otags, List<OEventAttachment> oattachments, ArrayList<OEventCustomValue> ocustomvalues) {
			this.oevent = oevent;
			this.orecurrence = orecurrence;
			this.orecurrenceBrokens = orecurrenceBrokens;
			this.oattendees = oattendees;
			this.otags = otags;
			this.oattachments = oattachments;
			this.ocustomvalues = ocustomvalues;
		}
	}
	
	public static class ProbeCalendarRemoteUrlResult {
		public final String displayName;
		
		public ProbeCalendarRemoteUrlResult(String displayName) {
			this.displayName = displayName;
		}
	}
	
	private static class RecipientTuple {
		public final InternetAddress recipient;
		public final UserProfileId refProfileId;
		
		public RecipientTuple(InternetAddress recipient, UserProfileId profileId) {
			this.recipient = recipient;
			this.refProfileId = profileId;
		}
	}
	
	private class OwnerCache extends AbstractMapCache<Integer, UserProfileId> {

		@Override
		protected void internalInitCache(Map<Integer, UserProfileId> mapObject) {}
		
		@Override
		protected void internalMissKey(Map<Integer, UserProfileId> mapObject, Integer key) {
			try {
				UserProfileId owner = doCalendarGetOwner(key);
				if (owner == null) throw new WTException("Owner not found [{0}]", key);
				mapObject.put(key, owner);
			} catch(WTException ex) {
				logger.trace("OwnerCache miss", ex);
			}
		}
	}
	
	private class ShareCache extends AbstractFolderShareCache<Integer, CalendarFSOrigin> {
		@Override
		protected void internalBuildCache() {
			final CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
			try {
				for (CalendarFSOrigin origin : getOrigins(coreMgr)) {
					origins.add(origin);
					originByProfile.put(origin.getProfileId(), origin);
					
					FolderShareOriginFolders folders = null;
					if (origin.isResource()) {
						folders = coreMgr.getFolderShareOriginFolders(CoreManifest.ID, "RESOURCE", origin.getProfileId());
					} else {
						folders = coreMgr.getFolderShareOriginFolders(SERVICE_ID, GROUPNAME_CALENDAR, origin.getProfileId());
					}
					foldersByProfile.put(origin.getProfileId(), folders);
					
					final Set<Integer> calendarIds;
					if (folders.wildcard()) {
						calendarIds = listCalendarIds(origin.getProfileId());
					} else {
						Set<Integer> ids = folders.getFolderIds().stream()
							.map(value -> Integer.valueOf(value))
							.collect(Collectors.toSet());
						calendarIds = listCalendarIdsIn(origin.getProfileId(), ids);
					}
					calendarIds.forEach(calendarId -> {originByFolderId.put(calendarId, origin);});
					folderIdsByProfile.putAll(origin.getProfileId(), calendarIds);
					folderIds.addAll(calendarIds);
				}
			} catch (WTException ex) {
				throw new WTRuntimeException(ex, "[ShareCache] Unable to build cache for '{}'", getTargetProfileId());
			}
		}
		
		private List<CalendarFSOrigin> getOrigins(final CoreManager coreMgr) throws WTException {
			List<CalendarFSOrigin> items = new ArrayList<>();
			for (ShareOrigin origin : coreMgr.listFolderShareOrigins(SERVICE_ID, GROUPNAME_CALENDAR)) {
				// Do permissions evaluation returning NULL in case of missing share: a root origin may not be shared!
				FolderShare.Permissions permissions = coreMgr.evaluateFolderSharePermissions(SERVICE_ID, GROUPNAME_CALENDAR, origin.getProfileId(), FolderSharing.Scope.wildcard(), false);
				if (permissions == null) {
					// If missing, simply treat it as NONE permission.
					permissions = FolderShare.Permissions.none();
				}
				items.add(new CalendarFSOrigin(origin, permissions, false));
			}
			for (ShareOrigin origin : coreMgr.listFolderShareOrigins(CoreManifest.ID, "RESOURCE")) {
				final FolderShare.Permissions permissions = coreMgr.evaluateFolderSharePermissions(CoreManifest.ID, "RESOURCE", origin.getProfileId(), FolderSharing.Scope.wildcard(), true);
				items.add(new CalendarFSOrigin(origin, permissions, true));
			}
			return items;
		}
	}
	
	private enum AuditContext {
		CALENDAR, EVENT
	}
	
	private enum AuditAction {
		CREATE, UPDATE, DELETE, MOVE, TAG
	}
	
	private class AuditEventObj implements AuditReferenceDataEntry {
		public final int eventId;
		
		public AuditEventObj(int eventId) {
			this.eventId = eventId;
		}

		@Override
		public String getReference() {
			return String.valueOf(eventId);
		}

		@Override
		public String getData() {
			return null;
		}
	}
	
	private class AuditEventMove implements AuditReferenceDataEntry {
		public final int eventId;
		public final int origCalendarId;
		
		public AuditEventMove(int eventId, int origCalendarId) {
			this.eventId = eventId;
			this.origCalendarId = origCalendarId;
		}

		@Override
		public String getReference() {
			return String.valueOf(eventId);
		}

		@Override
		public String getData() {
			return String.valueOf(origCalendarId);
		}
	}
	
	private class AuditEventCopy implements AuditReferenceDataEntry {
		public final int eventId;
		public final int origEventId;
		
		public AuditEventCopy(int eventId, int origEventId) {
			this.eventId = eventId;
			this.origEventId = origEventId;
		}

		@Override
		public String getReference() {
			return String.valueOf(eventId);
		}

		@Override
		public String getData() {
			return String.valueOf(origEventId);
		}
	}
	
	private enum ProcessReminder {
		YES, NO, DISARM_PAST
	}
	
	private enum ProcessOptions {
		ATTACHMENTS, TAGS
	}
}
