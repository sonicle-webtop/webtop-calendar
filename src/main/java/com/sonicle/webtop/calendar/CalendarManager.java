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
import com.sonicle.commons.Check;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.ChangeSet;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.beans.ItemsListResult;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.cache.AbstractPassiveExpiringBulkMap;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
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
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OCalendarOwnerInfo;
import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttachment;
import com.sonicle.webtop.calendar.bol.OEventAttachmentData;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OEventCustomValue;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VEventHrefSync;
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
import com.sonicle.webtop.calendar.dal.EventTagDAO;
import com.sonicle.webtop.calendar.io.EventInput;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
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
import com.sonicle.webtop.calendar.io.ICalendarOutput;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.io.ICalendarInput;
import com.sonicle.webtop.calendar.model.CalendarFSFolder;
import com.sonicle.webtop.calendar.model.CalendarFSOrigin;
import com.sonicle.webtop.calendar.model.ComparableEventRange;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.calendar.model.EventObjectWithBean;
import com.sonicle.webtop.calendar.model.EventObjectWithICalendar;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.mail.internet.AddressException;
import java.util.concurrent.TimeUnit;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.subject.Subject;
import org.joda.time.Duration;
import com.sonicle.commons.flags.BitFlagsEnum;
import com.sonicle.commons.qbuilders.QBuilderUtils;
import com.sonicle.commons.time.DateTimeRange2;
import com.sonicle.commons.time.DateTimeWindow;
import com.sonicle.commons.time.DateWindow;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.commons.time.TimeRange;
import com.sonicle.commons.web.json.CId;
import com.sonicle.mail.email.CalendarMethod;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.Recipients;
import com.sonicle.webtop.calendar.bol.OEventInstanceInfo;
import com.sonicle.webtop.calendar.bol.OEventRecurrence;
import com.sonicle.webtop.calendar.bol.VCalendarDefaults;
import com.sonicle.webtop.calendar.bol.VEventAttachmentWithBytes;
import com.sonicle.webtop.calendar.bol.VEventBounds;
import com.sonicle.webtop.calendar.bol.VEventLookup;
import com.sonicle.webtop.calendar.bol.VEventObjectChanged;
import com.sonicle.webtop.calendar.dal.CalendarConditionBuildingVisitor;
import com.sonicle.webtop.calendar.dal.EventConditionBuildingVisitor;
import com.sonicle.webtop.calendar.dal.EventRecurrenceDAO;
import com.sonicle.webtop.calendar.dal.EventUIConditionBuildingVisitor;
import com.sonicle.webtop.calendar.dal.HistoryDAO;
import com.sonicle.webtop.calendar.io.EventInputConsumer;
import com.sonicle.webtop.calendar.io.EventStreamReader;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.CalendarQuery;
import com.sonicle.webtop.calendar.model.EventAlertLookup;
import com.sonicle.webtop.calendar.model.EventAlertLookupInstance;
import com.sonicle.webtop.calendar.model.EventAttachmentWithInput;
import com.sonicle.webtop.calendar.model.EventAttachmentWithInputRef;
import com.sonicle.webtop.calendar.model.EventAttachmentWithInputStream;
import com.sonicle.webtop.calendar.model.EventBase;
import com.sonicle.webtop.calendar.model.EventEx;
import com.sonicle.webtop.calendar.model.EventLookupInstance;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.calendar.model.EventRecurrence;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.app.util.log.LogHandler;
import com.sonicle.webtop.core.app.util.log.LogMessage;
import com.sonicle.webtop.core.model.ChangedItem;
import com.sonicle.webtop.core.model.Delta;
import com.sonicle.webtop.core.model.ProfileI18n;
import com.sonicle.webtop.core.msg.ResourceReservationReplySM;
import com.sonicle.webtop.core.util.ICalendarHelper;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.joda.time.LocalDateTime;
import com.sonicle.webtop.calendar.model.EventBounds;
import com.sonicle.webtop.calendar.model.EventBoundsImpl;
import com.sonicle.webtop.calendar.model.EventBoundsSeries;
import com.sonicle.webtop.core.app.model.HomedThrowable;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.model.CustomField;

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
	private final CustomFieldsNameToIDCache cacheCustomFieldsNameToID = new CustomFieldsNameToIDCache(5, TimeUnit.SECONDS);
	private final CustomFieldIDToTypeCache cacheCustomFieldsIDToType = new CustomFieldIDToTypeCache(5, TimeUnit.SECONDS);
	private final KeyedReentrantLocks<String> locks = new KeyedReentrantLocks<>();
	
	private static final ConcurrentHashMap<String, UserProfileId> pendingRemoteCalendarSyncs = new ConcurrentHashMap<>();
	
	public CalendarManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		if (!fastInit) {
			shareCache.init();
		}
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
	
	public Result<Integer[]> cleanupHistory(final int retentionYears) {
		HistoryDAO hisDao = HistoryDAO.getInstance();
		Connection con = null;
		
		HomedThrowable exc = null;
		Integer[] ret = new Integer[2];
		try {
			con = WT.getConnection(SERVICE_ID);
			ret[0] = hisDao.deleteCalendarsHistoryByAge(con, retentionYears);
			ret[1] = hisDao.deleteEventsHistoryByAge(con, retentionYears);
			
		} catch (Exception ex) {
			exc = HomedThrowable.wrap(SERVICE_ID, ExceptionUtils.wrapThrowable(ex));
		} finally {
			DbUtils.closeQuietly(con);
		}
		return new Result<>(ret, exc);
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
	public Set<Integer> listIncomingCalendarIds(final UserProfileId originProfile) throws WTException {
		if (originProfile == null) {
			return listIncomingCalendarIds();
		} else {
			return LangUtils.asSet(shareCache.getFolderIdsByOrigin(originProfile));
		}
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
	
	@Override
	public ItemsListResult<Calendar> listCalendars(final Condition<CalendarQuery> filterQuery, final Set<SortInfo> sortInfo, final Integer page, final Integer limit, final boolean returnFullCount) throws WTException {
		return listCalendars(QBuilderUtils.toStringQuery(filterQuery), sortInfo, page, limit, returnFullCount);
	}
	
	@Override
	public ItemsListResult<Calendar> listCalendars(final String filterQuery, final Set<SortInfo> sortInfo, final Integer page, final Integer limit, final boolean returnFullCount) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		final int myLimit = limit == null ? Integer.MAX_VALUE : limit;
		final int myPage = page == null ? 1 : page;
		
		try {
			List<Integer> okCalendarIds = listAllCalendarIds().stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			org.jooq.Condition condition = BaseDAO.createCondition(filterQuery, new CalendarConditionBuildingVisitor());
			
			final String domainId = getTargetProfileId().getDomainId();
			final ArrayList<Calendar> items = new ArrayList<>();
			Integer fullCount = null;
			con = WT.getConnection(SERVICE_ID);
			if (returnFullCount) fullCount = calDao.countByDomainIn(con, domainId, okCalendarIds, condition);
			for (OCalendar ocal : calDao.selectByDomainIn(con, domainId, okCalendarIds, condition, sortInfo, myLimit, ManagerUtils.toOffset(myPage, myLimit))) {
				items.add(ManagerUtils.createCalendar(ocal));
			}

			return new ItemsListResult(items, fullCount);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<Integer, Calendar> listMyCalendars() throws WTException {
		return listCalendars(getTargetProfileId(), null, true);
	}
	
	@Override
	public Map<Integer, Calendar> listMyCalendars(final Collection<Integer> calendarIds) throws WTException {
		return listCalendars(getTargetProfileId(), calendarIds, true);
	}
	
	private Map<Integer, Calendar> listCalendars(final UserProfileId ownerPid, final Collection<Integer> calendarIds, final boolean evalRights) throws WTException {
		Check.notNull(ownerPid, "ownerPid");
		CalendarDAO calDao = CalendarDAO.getInstance();
		LinkedHashMap<Integer, Calendar> items = new LinkedHashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			List<OCalendar> ocals = (calendarIds != null) ? calDao.selectByProfileIn(con, ownerPid.getDomainId(), ownerPid.getUserId(), calendarIds) : calDao.selectByProfile(con, ownerPid.getDomainId(), ownerPid.getUserId());
			for (OCalendar ocal : ocals) {
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
			for (OCalendar ocal : calDao.selectByProvider(con, Arrays.asList(CalendarBase.Provider.WEBCAL, CalendarBase.Provider.CALDAV))) {
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
	public Map<Integer, DateTime> getCalendarsItemsLastRevision(Collection<Integer> calendarIds) throws WTException {
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
		
		UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		String davServerBaseUrl = WT.getDavServerBaseUrl(getTargetProfileId().getDomainId());
		String calendarUid = ManagerUtils.encodeAsCalendarUid(calendarId);
		String calendarUrl = MessageFormat.format(ManagerUtils.CALDAV_CALENDAR_URL, pdata.getProfileEmailAddress(), calendarUid);
		
		LinkedHashMap<String, String> links = new LinkedHashMap<>();
		links.put(ManagerUtils.CALENDAR_LINK_CALDAV, PathUtils.concatPathParts(davServerBaseUrl, calendarUrl));
		return links;
	}
	
	@Override
	public Calendar addCalendar(final CalendarBase calendar) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarOrigin(calendar.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar.setBuiltIn(false);
			Calendar result = doCalendarInsert(con, calendar);
			
			DbUtils.commitQuietly(con);
			onAfterCalendarAction(result.getCalendarId(), result.getProfileId());
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CALENDAR, AuditAction.CREATE, result.getCalendarId(), null);
			}
			
			return result;
			
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
			cal.setDefaultTransparency(EventBase.Transparency.OPAQUE);
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
	public void updateCalendar(final int calendarId, final CalendarBase calendar) throws WTNotFoundException, WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.UPDATE);
			
			con = WT.getConnection(SERVICE_ID, false);
			boolean ret = doCalendarUpdate(con, calendarId, calendar);
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
			
			doEventDeleteByCalendar(con, calendarId, !cal.isProviderRemote());
			psetDao.deleteByCalendar(con, calendarId);
			int ret = calDao.deleteById(con, calendarId);
			
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
	
	@Override
	public List<ComparableEventRange> listEventsBounds(final Collection<Integer> calendarIds, final DateTimeRange2 viewRange, final DateTimeZone targetTimezone) throws WTException {
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
	public List<EventObject> listEventObjects(final int calendarId, final EventObjectOutputType outputType) throws WTException {
		return listEventObjects(calendarId, null, outputType);
	}
	
	@Override
	public List<EventObject> listEventObjects(final int calendarId, final DateTime since, final EventObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			ArrayList<EventObject> items = new ArrayList<>();
			Map<String, List<VEventObject>> map = null;
			if (since == null) {
				map = evtDao.viewOnlineEventObjectsByCalendar(con, EventObjectOutputType.STAT.equals(outputType), calendarId);
			} else {
				map = evtDao.viewOnlineEventObjectsByCalendarSince(con, EventObjectOutputType.STAT.equals(outputType), calendarId, since);
			}
			for (List<VEventObject> vobjs : map.values()) {
				if (vobjs.isEmpty()) continue;
				VEventObject vobj = vobjs.get(vobjs.size()-1);
				if (vobjs.size() > 1) {
					logger.trace("Many events ({}) found for same href [{} -> {}]", vobjs.size(), vobj.getHref(), vobj.getEventId());
				}
				items.add(doEventObjectPrepare(con, vobj, outputType, tagNamesByIdMap));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ItemsListResult<EventObject> listEventObjects2(final Collection<Integer> calendarIds, final DateTime since, final Condition<EventQuery> filterQuery, final Integer page, final Integer limit, final boolean returnFullCount, final EventObjectOutputType outputType) throws WTException {
		return listEventObjects2(calendarIds, since, QBuilderUtils.toStringQuery(filterQuery), page, limit, returnFullCount, outputType);
	}
	
	public ItemsListResult<EventObject> listEventObjects2(final Collection<Integer> calendarIds, final DateTime since, final String filterQuery, final Integer page, final Integer limit, final boolean returnFullCount, final EventObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		final int myLimit = limit == null ? Integer.MAX_VALUE : limit;
		final int myPage = page == null ? 1 : page;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			org.jooq.Condition condition = BaseDAO.createCondition(filterQuery, new EventConditionBuildingVisitor()
				.withFieldNameAsCustomFieldName(cacheCustomFieldsNameToID.shallowCopy(), cacheCustomFieldsIDToType.shallowCopy())
			);
			
			con = WT.getConnection(SERVICE_ID);
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			
			final ArrayList<EventObject> items = new ArrayList<>();
			Integer fullCount = null;
			if (returnFullCount) fullCount = evtDao.countOnlineEventObjectsByCalendarSinceCondition(con, okCalendarIds, since, condition);
			evtDao.lazy_viewOnlineEventObjectsByCalendarSinceCondition(
				con,
				okCalendarIds,
				since,
				condition,
				EventObjectOutputType.STAT.equals(outputType),
				myLimit,
				ManagerUtils.toOffset(myPage, myLimit),
				(VEventObject veo, Connection con1) -> {
					items.add(doEventObjectPrepare(con1, veo, outputType, tagNamesByIdMap));
				}
			);
			return new ItemsListResult(items, fullCount);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Delta<EventObject> listEventObjectsDelta(final int calendarId, final String syncToken, final EventObjectOutputType outputType) throws WTException {
		final DateTime since = (syncToken == null) ? null : Delta.parseSyncToken(syncToken, false);
		return listEventObjectsDelta(calendarId, since, outputType);
	}
	
	@Override
	public Delta<EventObject> listEventObjectsDelta(final int calendarId, final DateTime since, final EventObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		// Do NOT support page/limit for now
		//final int myLimit = limit == null ? Integer.MAX_VALUE : limit;
		//final int myPage = page == null ? 1 : page;
		final int myLimit = Integer.MAX_VALUE;
		final int myPage = 1;
		
		try {
			final boolean fullSync = (since == null);
			final DateTime until = JodaTimeUtils.now(true);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			final BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.TAGS);
			final ArrayList<ChangedItem<EventObject>> items = new ArrayList<>();
			con = WT.getConnection(SERVICE_ID);
			if (fullSync) {
				evtDao.lazy_viewChangedEventObjects(
					con,
					Arrays.asList(calendarId),
					EventDAO.createEventsChangedNewOrModifiedCondition(),
					EventObjectOutputType.STAT.equals(outputType),
					myLimit,
					ManagerUtils.toOffset(myPage, myLimit),
					(VEventObjectChanged veoc, Connection con1) -> {
						items.add(new ChangedItem<>(ChangedItem.ChangeType.ADDED, doEventObjectPrepare(con1, veoc, outputType, processOpts, tagNamesByIdMap)));
					}
				);
				
			} else {
				evtDao.lazy_viewChangedEventObjects(
					con,
					Arrays.asList(calendarId),
					EventDAO.createEventsChangedSinceUntilCondition(since, until),
					EventObjectOutputType.STAT.equals(outputType),
					myLimit,
					ManagerUtils.toOffset(myPage, myLimit),
					(VEventObjectChanged veoc, Connection con1) -> {
						if (veoc.isChangeInsertion()) {
							items.add(new ChangedItem<>(ChangedItem.ChangeType.ADDED, doEventObjectPrepare(con1, veoc, outputType, processOpts, tagNamesByIdMap)));
						} else if (veoc.isChangeUpdate()) {
							items.add(new ChangedItem<>(ChangedItem.ChangeType.UPDATED, doEventObjectPrepare(con1, veoc, outputType, processOpts, tagNamesByIdMap)));
						} else if (veoc.isChangeDeletion()) {
							items.add(new ChangedItem<>(ChangedItem.ChangeType.DELETED, doEventObjectPrepare(con1, veoc, outputType, processOpts, tagNamesByIdMap)));
						}
					}
				);
			}
			return new Delta<>(until, items);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<EventObject> getEventObjects(final int calendarId, final Collection<String> hrefs, final EventObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			ArrayList<EventObject> items = new ArrayList<>();
			Map<String, List<VEventObject>> map = evtDao.viewOnlineEventObjectsByCalendarHrefs(con, EventObjectOutputType.STAT.equals(outputType), calendarId, hrefs);
			for (String href : hrefs) {
				List<VEventObject> vevts = map.get(href);
				if (vevts == null) continue;
				if (vevts.isEmpty()) continue;
				VEventObject vevt = vevts.get(vevts.size()-1);
				if (vevts.size() > 1) {
					logger.trace("Many events ({}) found for same href [{} -> {}]", vevts.size(), vevt.getHref(), vevt.getEventId());
				}
				
				items.add(doEventObjectPrepare(con, vevt, outputType, tagNamesByIdMap));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventObject getEventObject(int calendarId, String eventId, EventObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			
			VEventObject vevt = evtDao.viewEventObjectById(con, Arrays.asList(calendarId), eventId);
			return (vevt == null) ? null : doEventObjectPrepare(con, vevt, outputType, coreMgr.listTagNamesById());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void addEventObject(final int calendarId, final String href, final net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		addEventObject(calendarId, href, iCalendar, true);
	}
	
	public void addEventObject(final int calendarId, final String href, final net.fortuna.ical4j.model.Calendar iCalendar, boolean notifyAttendees) throws WTException {
		CoreManager coreMgr = getCoreManager();
		final UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		Connection con = null;
		
		String newEventId = null;
		Event newEvent = null;
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
			
			ICalendarInput in = new ICalendarInput(pdata.getTimeZone(), coreMgr.listTagNamesById(), coreMgr.listTagIdsByName())
				.withDefaultAttendeeNotify(false);
			ArrayList<EventInput> eis = in.parseEventObjects(iCalendar);
			if (eis.isEmpty()) throw new WTException("iCalendar does not contain any VEVENT");
			if (eis.size() > 1) throw new WTException("iCalendar should contain one VEVENT");
			EventInput input = eis.get(0);
			input.event.setCalendarId(calendarId);
			input.event.setHref(href);
			
			con = WT.getConnection(SERVICE_ID, false);
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.RAW_ICAL);
			EventInsertResult result = doEventInputInsert(con, input, processOpts, BitFlags.with(EventReminderOption.IGNORE), coreMgr.listTagIds());
			newEventId = result.oevent.getEventId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, newEventId, null);
			}
			
			newEvent = doEventGet(con, newEventId, processOpts);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (newEvent != null) {
			// In this case, invitation messages should be sent only in two cases:
			// 1) Organizer addr. matches current-user's personal address
			// 2) Organizer addr. matches current-user's profile address
			BitFlags<EventNotifyOption> notifyOpts = BitFlags.noneOf(EventNotifyOption.class);
			if (notifyAttendees && iAmTheOrganizer(newEvent.getOrganizerInternetAddress(), pdata)) {
				EventNotifyOption.enableAllAttendeesNotifications(notifyOpts);
				newEvent.getAttendees().stream().forEach(att -> att.setNotify(true));
			}
			onAfterEventOperation(Crud.CREATE, newEvent, notifyOpts);
		}
	}
	
	@Override
	public void updateEventObject(final int calendarId, final String href, final net.fortuna.ical4j.model.Calendar iCalendar) throws WTNotFoundException, WTException {
		updateEventObject(calendarId, href, iCalendar, true);
	}
	
	public void updateEventObject(final int calendarId, final String href, final net.fortuna.ical4j.model.Calendar iCalendar, final boolean notifyAttendees) throws WTNotFoundException, WTException {
		CoreManager coreMgr = getCoreManager();
		UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			
			ICalendarInput in = new ICalendarInput(pdata.getTimeZone(), coreMgr.listTagNamesById(), coreMgr.listTagIdsByName())
				.withDefaultAttendeeNotify(false)
				.withIncludeSourceComponentInOutput(true);
			ArrayList<EventInput> inputs = in.parseEventObjects(iCalendar);
			if (inputs.isEmpty()) throw new WTException("iCalendar does not contain any VEVENT");
			if (inputs.size() > 1) throw new WTException("iCalendar should contain one VEVENT");
			inputs.get(0).event.setCalendarId(calendarId);
			inputs.get(0).event.setHref(href);
			
			con = WT.getConnection(SERVICE_ID, false);
			String eventId = doEventGetIdByCalendarHref(con, calendarId, href, true);
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.RAW_ICAL);
			boolean ret = doEventInputUpdate(con, eventId, inputs, processOpts, coreMgr.listTagIds());
			if (!ret) throw new WTNotFoundException("Event not found [{}]", eventId);
			
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEventObject(final int calendarId, final String href) throws WTException {
		deleteEventObject(calendarId, href, EventNotifyOption.withAllAttendeesNotifications());
	}
	
	@Override
	public void deleteEventObject(final int calendarId, final String href, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		Connection con = null;
		String eventId = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			eventId = doEventGetIdByCalendarHref(con, calendarId, href, true);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		deleteEventInstance(UpdateEventTarget.WHOLE_SERIES, EventInstanceId.buildMaster(eventId), notifyOptions);
	}
	
	/*
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
	*/
	
	/*
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
	*/
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final DateWindow timeWindow, final boolean sort, final DateTimeZone targetTimezone) throws WTException {
		return listEventInstances(calendarIds, timeWindow, null, sort, targetTimezone);
	}
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final DateWindow timeWindow, final Condition<EventQuery> conditionPredicate, final boolean sort, final DateTimeZone targetTimezone) throws WTException {
		DateTimeWindow newTimeWindow = (timeWindow == null) ? null : DateTimeWindow.builder()
			.with(timeWindow.getStart().toDateTimeAtStartOfDay(targetTimezone), timeWindow.getEnd().plusDays(1).toDateTimeAtStartOfDay(targetTimezone))
			.build();
		return listEventInstances(calendarIds, newTimeWindow, conditionPredicate, sort, targetTimezone);
	}
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final DateTimeZone targetTimezone) throws WTException {
		return listEventInstances(calendarIds, (DateTimeWindow)null, (String)null, true, targetTimezone);
	}
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final Condition<EventQuery> filterQuery, final boolean sort, final DateTimeZone targetTimezone) throws WTException {
		return listEventInstances(calendarIds, (DateTimeWindow)null, QBuilderUtils.toStringQuery(filterQuery), sort, targetTimezone);
	}
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final DateTimeWindow timeWindow, final Condition<EventQuery> filterQuery, final boolean sort, final DateTimeZone targetTimezone) throws WTException {
		return listEventInstances(calendarIds, timeWindow, QBuilderUtils.toStringQuery(filterQuery), sort, targetTimezone);
	}
	
	@Override
	public List<EventLookupInstance> listEventInstances(final Collection<Integer> calendarIds, final DateTimeWindow timeWindow, final String filterQuery, final boolean sort, final DateTimeZone targetTimezone) throws WTException {
		Check.notNull(calendarIds, "calendarIds");
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			UserProfileId runProfile = RunContext.getRunProfileId();
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			org.jooq.Condition queryCondition = null;
			DateTime from = null;
			DateTime to = null;
			int noOfRecurringInst = 1*365;
			
			EventUIConditionBuildingVisitor cbv = new EventUIConditionBuildingVisitor();
			queryCondition = BaseDAO.createCondition(filterQuery, cbv);
			
			boolean hasTimeWindow = (timeWindow != null);
			from = hasTimeWindow ? timeWindow.getStart() : null;
			to = hasTimeWindow ? timeWindow.getEnd() : null;
			from = cbv.getRangeStartOrDefault(from);
			to = cbv.getRangeEndOrDefault(to);
			
			Days daysInRange = JodaTimeUtils.daysBetween(from, to);
			if (daysInRange != null) noOfRecurringInst = daysInRange.getDays() + 1;
			
			con = WT.getConnection(SERVICE_ID);
			ArrayList<EventLookupInstance> instances = new ArrayList<>();
			for (VEventLookup vevt : evtDao.viewOnlineByCalendarRangeCondition(con, okCalendarIds, from, to, queryCondition)) {
				final boolean keepPrivate = needsTreatAsPrivate(runProfile, vevt.getCalendarProfileId(), vevt.isVisibilityPrivate());
				if (!vevt.getHasRecurrence()) {
					EventLookupInstance item = ManagerUtils.fillEventLookup(new EventLookupInstance(), vevt);
					item.setId(EventInstanceId.build(vevt.getEventId(), vevt.getSeriesEventId(), vevt.getSeriesInstanceId()));
					item.setOriginalEventId(vevt.getEventId());
					if (keepPrivate) item.censorize();
					
					instances.add(item);
					
				} else {
					final List<EventLookupInstance> items = calculateRecurringInstances(new VEL2ELI_RRContext(con, vevt, keepPrivate), from, to, noOfRecurringInst);
					instances.addAll(items);
				}
			}
			
			//TODO: transform to an ordered insert
			if (sort) {
				Collections.sort(instances, (final EventLookupInstance se1, final EventLookupInstance se2) -> se1.getStart().compareTo(se2.getStart()));
			}
			
			return instances;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Set<LocalDate> listEventDates(final Collection<Integer> calendarIds, final DateTimeWindow timeWindow, final DateTimeZone targetTimezone) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			boolean hasTimeWindow = (timeWindow != null);
			DateTime from = hasTimeWindow ? timeWindow.getStart() : null;
			DateTime to = hasTimeWindow ? timeWindow.getEnd() : null;
			int noOfRecurringInst = 1*365;
			
			Days daysInRange = JodaTimeUtils.daysBetween(from, to);
			if (daysInRange != null) noOfRecurringInst = daysInRange.getDays() + 1;
			
			con = WT.getConnection(SERVICE_ID);
			HashSet<LocalDate> dates = new HashSet<>();
			for (VEventBounds veb : evtDao.viewOnlineBoundsByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				if (!veb.hasRecurrence()) {
					dates.addAll(CalendarUtils.getDisplayDatesSpan(veb.getAllDay(), veb.getStart(), veb.getEnd(), veb.getTimezoneObject()));
					
				} else {
					for (EventBounds eb : calculateRecurringInstances(new VEB2EB_RRContext(veb), from, to, noOfRecurringInst)) {
						dates.addAll(CalendarUtils.getDisplayDatesSpan(eb.isAllDay(), eb.getStart(), eb.getEnd(), eb.getTimezoneObject()));
					}
				}
			}
			// DEPRECATED: old lookup reusing viewOnlineByCalendarRangeCondition
			/*
			for (VEventLookup vevt : evtDao.viewOnlineByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				if (!vevt.getHasRecurrence()) {
					dates.addAll(CalendarUtils.getDisplayDatesSpan(vevt.getAllDay(), vevt.getStart(), vevt.getEnd(), vevt.getTimezoneObject()));
					
				} else {
					final List<EventLookupInstance> items = calculateRecurringInstances(new VEL2ELI_RRContext(con, vevt, false), from, to, noOfRecurringInst);
					for (EventLookupInstance item : items) {
						dates.addAll(CalendarUtils.getDisplayDatesSpan(item.getAllDay(), item.getStart(), item.getEnd(), item.getTimezoneObject()));
					}
				}
			}
			*/
			// -----------------
			return dates;
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Set<LocalDate> listEventInstancesDates(final Collection<Integer> calendarIds, final DateTimeZone targetTimezone) throws WTException {
		return listEventInstancesDates(calendarIds, (DateTimeWindow)null, (String)null, targetTimezone);
	}
	
	@Override
	public Set<LocalDate> listEventInstancesDates(final Collection<Integer> calendarIds, final DateTimeWindow timeWindow, final String filterQuery, final DateTimeZone targetTimezone) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCalendarIds = calendarIds.stream()
				.filter(calendarId -> quietlyCheckRightsOnCalendar(calendarId, FolderShare.FolderRight.READ))
				.collect(Collectors.toList());
			
			org.jooq.Condition queryCondition = null;
			int noOfRecurringInst = 1*365;
			
			EventUIConditionBuildingVisitor cbv = new EventUIConditionBuildingVisitor();
			queryCondition = BaseDAO.createCondition(filterQuery, cbv);
			
			boolean hasTimeWindow = (timeWindow != null);
			DateTime from = hasTimeWindow ? timeWindow.getStart() : null;
			DateTime to = hasTimeWindow ? timeWindow.getEnd() : null;
			
			Days daysInRange = JodaTimeUtils.daysBetween(from, to);
			if (daysInRange != null) noOfRecurringInst = daysInRange.getDays() + 1;
			
			con = WT.getConnection(SERVICE_ID);
			HashSet<LocalDate> dates = new HashSet<>();
			for (VEventBounds veb : evtDao.viewOnlineBoundsByCalendarRangeCondition(con, okCalendarIds, from, to, queryCondition)) {
				if (!veb.hasRecurrence()) {
					dates.addAll(CalendarUtils.getDisplayDatesSpan(veb.getAllDay(), veb.getStart(), veb.getEnd(), veb.getTimezoneObject()));
					
				} else {
					for (EventBounds eb : calculateRecurringInstances(new VEB2EB_RRContext(veb), from, to, noOfRecurringInst)) {
						dates.addAll(CalendarUtils.getDisplayDatesSpan(eb.isAllDay(), eb.getStart(), eb.getEnd(), eb.getTimezoneObject()));
					}
				}
			}
			return dates;
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<String, DateTime> computeTransparencyTimeSpans(final UserProfileId targetProfileId, final EventBase.Transparency transparency, final DateWindow dateWindow, final TimeRange timeRange, final int minuteResolution, final DateTimeZone targetTimezone) throws WTException {
		Check.stateIsTrue(!(minuteResolution < 1 || 60 % minuteResolution != 0), "minuteResolution must be a divider of 60");
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		LinkedHashMap<String, DateTime> spans = new LinkedHashMap<>();
		try {
			DateTime from = new DateTime(targetTimezone).withDate(dateWindow.getStart()).withTimeAtStartOfDay();
			DateTime to = new DateTime(targetTimezone).withDate(dateWindow.getEnd()).withTimeAtStartOfDay();
			org.jooq.Condition queryCondition = EventDAO.createTransparencyCondition(transparency);
			
			con = WT.getConnection(SERVICE_ID);
			int noOfRecurringInst = 365+2;
			final Set<Integer> okCalendarIds = doListCalendarIdsIn(con, targetProfileId, null);
			for (VEventLookup vevt : evtDao.viewOnlineByCalendarRangeCondition(con, okCalendarIds, from, to, queryCondition)) {
				if (!vevt.getHasRecurrence()) {
					final DateTime rouStart = JodaTimeUtils.roundToNearestMinute(vevt.getStart().withZone(targetTimezone), minuteResolution);
					final DateTime rouEnd = JodaTimeUtils.roundToNearestMinute(vevt.getEnd().withZone(targetTimezone), minuteResolution);
					final TimeRange rouTimeRange = TimeRange.builder().with(rouStart.toLocalTime(), rouEnd.toLocalTime()).build();
					spans.putAll(generateTimeSpans(rouStart.toLocalDate(), rouEnd.toLocalDate(), rouTimeRange, false, minuteResolution, targetTimezone));
					
				} else {
					final List<EventLookupInstance> items = calculateRecurringInstances(new VEL2ELI_RRContext(con, vevt, false), from, to, noOfRecurringInst);
					for (EventLookupInstance item : items) {
						final DateTime rouStart = JodaTimeUtils.roundToNearestMinute(item.getStart().withZone(targetTimezone), minuteResolution);
						final DateTime rouEnd = JodaTimeUtils.roundToNearestMinute(item.getEnd().withZone(targetTimezone), minuteResolution);
						final TimeRange rouTimeRange = TimeRange.builder().with(rouStart.toLocalTime(), rouEnd.toLocalTime()).build();
						spans.putAll(generateTimeSpans(rouStart.toLocalDate(), rouEnd.toLocalDate(), rouTimeRange, false, minuteResolution, targetTimezone));
					}
				}
			}
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return spans;
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
	
	public String findEventId(final String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doEventLookupId(con, publicUid, null);
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	@Deprecated public Event getEvent(GetEventScope scope, String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Event event = doEventGet(con, scope, publicUid);
			if (event == null) return null;
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.isVisibilityPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			return event;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event addEvent(final EventEx event) throws WTException {
		return addEvent(event, null, EventNotifyOption.withAllAttendeesNotifications());
	}
	
	@Override
	public Event addEvent(final EventEx event, final boolean notifyAttendees) throws WTException {
		return addEvent(event, null, notifyAttendees ? EventNotifyOption.withAllAttendeesNotifications() : EventNotifyOption.withoutAnyAttendeesNotifications());
	}
	
	@Override
	public Event addEvent(final EventEx event, final BitFlags<EventNotifyOption> notifyOtions) throws WTException {
		return addEvent(event, null, notifyOtions);
	}
	
	@Override
	public Event addEvent(final EventEx event, final String rawICalendar, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		String newEventId = null;
		Event newEvent = null;
		try {
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.ItemsRight.CREATE);
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			Set<String> availTags = coreMgr.listTagIds();
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES);
			if (!StringUtils.isBlank(rawICalendar)) processOpts.set(EventProcessOpt.RAW_ICAL);
			
			EventInsertResult result = doEventInsert(con, event, null, null, rawICalendar, processOpts, BitFlags.noneOf(EventReminderOption.class), availTags);
			newEventId = result.oevent.getEventId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, newEventId, null);
			}
			
			storeAsSuggestion(coreMgr, SUGGESTION_EVENT_TITLE, event.getTitle());
			if (!StringUtils.startsWithIgnoreCase(event.getLocation(), "http")) {
				storeAsSuggestion(coreMgr, SUGGESTION_EVENT_LOCATION, event.getLocation());
			}
			
			newEvent = doEventGet(con, newEventId, processOpts);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (newEvent != null) {
			onAfterEventOperation(Crud.CREATE, newEvent, notifyOptions);
		} else {
			logger.warn("Unable to get newEvent [{}]", newEventId);
		}
		
		return newEvent;
	}
	
	@Override
	public Event addEvent(final int calendarId, final net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		Check.notNull(iCalendar, "iCalendar");
		CoreManager coreMgr = getCoreManager();
		final UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		Connection con = null;
		
		String newEventId = null;
		Event newEvent = null;
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
			
			Set<String> validTags = coreMgr.listTagIds();
			
			ICalendarInput in = new ICalendarInput(pdata.getTimeZone())
				.withDefaultAttendeeNotify(false);
			ArrayList<EventInput> eis = in.parseEventObjects(iCalendar);
			if (eis.isEmpty()) throw new WTException("iCalendar object does not contain any VEVENT");
			if (eis.size() > 1) throw new WTException("iCalendar object should contain one VEVENT");
			EventInput input = eis.get(0);
			input.event.setCalendarId(calendarId);
			
			con = WT.getConnection(SERVICE_ID, false);
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.RAW_ICAL);
			EventInsertResult result = doEventInputInsert(con, input, processOpts, BitFlags.with(EventReminderOption.IGNORE), validTags, new HashMap<>());
			newEventId = result.oevent.getEventId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, newEventId, null);
			}
			
			newEvent = doEventGet(con, newEventId, processOpts);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (newEvent != null) {
			onAfterEventOperation(Crud.CREATE, newEvent, EventNotifyOption.withoutAnyAttendeesNotifications());
		} else {
			logger.warn("Unable to get newEvent [{}]", newEventId);
		}
		
		return newEvent;
	}
	
	@Override
	public EventInstance getEventInstance(final EventInstanceId instanceId) throws WTException {
		return getEventInstance(instanceId, BitFlags.with(EventGetOption.ATTENDEES, EventGetOption.ATTACHMENTS, EventGetOption.TAGS, EventGetOption.CUSTOM_VALUES));
	}
	
	@Override
	public EventInstance getEventInstance(final EventInstanceId instanceId, final BitFlags<EventGetOption> options) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			BitFlags<EventProcessOpt> processOpts = EventProcessOpt.parseEventGetOptions(options)
				.set(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
			EventInstance event = doEventInstanceGet(con, instanceId, processOpts);
			if (event == null) return null;
			
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.FolderRight.READ);
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.isVisibilityPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			return event;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventAttachmentWithBytes getEventInstanceAttachment(final EventInstanceId instanceId, final String attachmentId) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttachmentDAO attDao = EventAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			InstanceInfo info = doEventGetInstanceInfo(con, instanceId);
			String eventId = info.originalEventId();
			Integer calendarId = evtDao.selectCalendarById(con, eventId);
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			OEventAttachment oatt = attDao.selectByIdEvent(con, attachmentId, eventId);
			if (oatt == null) return null;
			//TODO: check private?
			//boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.getIsPrivate(), event.getCalendarId());
			
			OEventAttachmentData oattData = attDao.selectBytesById(con, attachmentId);
			return ManagerUtils.fillEventAttachment(new EventAttachmentWithBytes(oattData.getBytes()), oatt);
		
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<String, CustomFieldValue> getEventInstanceCustomValues(final EventInstanceId instanceId) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			String eventId = info.originalEventId();
			Integer calendarId = evtDao.selectCalendarById(con, eventId);
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			
			//TODO: check private?
			//boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.getIsPrivate(), event.getCalendarId());
			
			List<OEventCustomValue> ovals = cvalDao.selectByEvent(con, eventId);
			return ManagerUtils.createCustomValuesMap(ovals);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event cloneEventInstance(final EventInstanceId sourceInstanceId, final Integer newCalendarId, final DateTime newStart, final DateTime newEnd, final String newTitle, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		Connection con = null;
		
		String newEventId = null;
		Event newEvent = null;
		try {
			Set<String> validTags = coreMgr.listTagIds();
			String copyPrefix = lookupResource(getLocale(), "event.copy.prefix");
			con = WT.getConnection(SERVICE_ID, false);
			
			// Retrieve event instance
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(
				EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES
			);
			EventInstance event = doEventInstanceGet(con, sourceInstanceId, processOpts);
			if (event == null) throw new WTNotFoundException("Event not found [{}]", sourceInstanceId);
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.isVisibilityPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			checkRightsOnCalendar(event.getCalendarId(), FolderShare.FolderRight.READ);
			
			// Fill event with new data
			if ((newStart != null) && (newEnd != null)) {
				event.setStart(newStart);
				event.setEnd(newEnd);
			} else if (newStart != null) {
				Duration length = new Duration(event.getStart(), event.getEnd());
				event.setStart(newStart);
				event.setEnd(newStart.plus(length));
			} else if (newEnd != null) {
				Duration length = new Duration(event.getStart(), event.getEnd());
				event.setStart(newEnd.minus(length));
				event.setEnd(newEnd);
			}
			if (newTitle != null) {
				event.setTitle(newTitle);
			} else {
				event.prependToTitle(copyPrefix);
			}
			event.ensureCoherence();
			EventInsertResult insertResult = doEventInstanceCopy(con, new InstanceInfo(event), event, newCalendarId, processOpts, validTags);
			newEventId = insertResult.oevent.getEventId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, newEventId, null);
			}
			
			newEvent = doEventGet(con, newEventId, processOpts);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (newEvent != null) {
			onAfterEventOperation(Crud.CREATE, newEvent, notifyOptions);
		} else {
			logger.warn("Unable to notify: event null [{}]", newEventId);
		}
		
		return newEvent;
	}
	
	@Override
	public void updateEventInstance(final UpdateEventTarget target, final EventInstanceId instanceId, final EventEx event) throws WTException {
		BitFlags<EventNotifyOption> notifyOpts = EventNotifyOption.withAllAttendeesNotifications();
		updateEventInstance(target, instanceId, event, notifyOpts);
	}
	
	@Override
	public void updateEventInstance(final UpdateEventTarget target, final EventInstanceId instanceId, final EventEx event, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		BitFlags<EventUpdateOption> updateOpts = BitFlags.with(EventUpdateOption.ATTENDEES, EventUpdateOption.ATTACHMENTS, EventUpdateOption.TAGS, EventUpdateOption.CUSTOM_VALUES);
		updateEventInstance(target, instanceId, event, updateOpts, notifyOptions);
	}
	
	@Override
	public void updateEventInstance(final UpdateEventTarget target, final EventInstanceId instanceId, final EventEx event, final BitFlags<EventUpdateOption> options, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			if (needsTreatAsPrivate(RunContext.getRunProfileId(), info.eventIsPrivate, calendarId)) {
				throw new AuthException("Event is private and therefore it cannot be updated [{}]", instanceId);
			}
			
			BitFlags<EventProcessOpt> processOpts = EventProcessOpt.parseEventUpdateOptions(options)
				.set(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
			Set<String> validTags = processOpts.has(EventProcessOpt.TAGS) ? coreMgr.listTagIds() : null;
			doEventInstanceUpdateAndCommit(con, target, info, event, processOpts, notifyOptions, validTags);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstanceQuick(final UpdateEventTarget target, final EventInstanceId instanceId, final DateTime newStart, final DateTime newEnd, final String newTitle) throws WTException {
		BitFlags<EventNotifyOption> notifyOpts = EventNotifyOption.withAllAttendeesNotifications();
		updateEventInstanceQuick(target, instanceId, newStart, newEnd, newTitle, notifyOpts);
	}
	
	@Override
	public void updateEventInstanceQuick(final UpdateEventTarget target, final EventInstanceId instanceId, final DateTime newStart, final DateTime newEnd, final String newTitle, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CoreManager coreMgr = getCoreManager();
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			if (needsTreatAsPrivate(RunContext.getRunProfileId(), info.eventIsPrivate, calendarId)) {
				throw new AuthException("Event is private and therefore it cannot be updated [{}]", instanceId);
			}
			
			// Retrieve event instance
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(
				EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES
			);
			EventInstance event = doEventInstanceGet(con, instanceId, processOpts);
			if (event == null) throw new WTNotFoundException("Event not found [{}]", instanceId);
			
			// Fill event with new data
			if ((newStart != null) && (newEnd != null)) {
				event.setStart(newStart.withZone(event.getTimezoneObject()));
				event.setEnd(newEnd.withZone(event.getTimezoneObject()));
				if (UpdateEventTarget.WHOLE_SERIES.equals(target) && event.hasRecurrence()) {
					event.getRecurrence().setStart(event.getStart());
				}
			} else if (newStart != null) {
				Duration length = new Duration(event.getStart(), event.getEnd());
				event.setStart(newStart.withZone(event.getTimezoneObject()));
				event.setEnd(newStart.withZone(event.getTimezoneObject()).plus(length));
				if (UpdateEventTarget.WHOLE_SERIES.equals(target) && event.hasRecurrence()) {
					event.getRecurrence().setStart(event.getStart());
				}
			} else if (newEnd != null) {
				Duration length = new Duration(event.getStart(), event.getEnd());
				event.setStart(newEnd.withZone(event.getTimezoneObject()).minus(length));
				event.setEnd(newEnd.withZone(event.getTimezoneObject()));
			}
			if (newTitle != null) {
				event.setTitle(newTitle);
			}
			event.ensureCoherence();
			
			Set<String> validTags = processOpts.has(EventProcessOpt.TAGS) ? coreMgr.listTagIds() : null;
			doEventInstanceUpdateAndCommit(con, target, info, event, processOpts, notifyOptions, validTags);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEventInstanceAttendeeResponse(final EventInstanceId instanceId, final EventAttendee.ResponseStatus responseStatus, final String comment, final boolean notifyOrganizer) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			
			List<String> attendeeIds = doEventAttendeeUpdateResponseByProfile(con, info.originalEventId(), getTargetProfileId(), responseStatus, true);
			if (attendeeIds.isEmpty()) throw new WTNotFoundException("Attendee not found [{}, {}]", instanceId, getTargetProfileId());
			DbUtils.commitQuietly(con);
			
			if (notifyOrganizer) {
				BitFlags<EventProcessOpt> processOpts = BitFlags.with(
					EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES
				);
				Event event = doEventGet(con, info.originalEventId(), processOpts);
				if (event == null) throw new WTException("Unable to notify organizer: event is null [{}]", info.originalEventId());
				UserProfileId senderProfile = getCalendarOwner(calendarId);
				if (senderProfile == null) senderProfile = getTargetProfileId();
				notifyOrganizer(senderProfile, event, attendeeIds.get(0));
			}
		
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEventInstanceAttendeeResponse(final EventInstanceId instanceId, final String attendeeId, final EventAttendee.ResponseStatus responseStatus, final boolean notifyOrganizer) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
			if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			
			int ret = doEventAttendeeUpdateResponse(con, info.originalEventId(), attendeeId, responseStatus, true);
			if (ret == 0) throw new WTNotFoundException("Attendee not found [{}, {}]", instanceId, attendeeId);
			DbUtils.commitQuietly(con);
			
			if (notifyOrganizer) {
				BitFlags<EventProcessOpt> processOpts = BitFlags.with(
					EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES
				);
				Event event = doEventGet(con, info.originalEventId(), processOpts);
				if (event == null) throw new WTException("Unable to notify organizer: event is null [{}]", info.originalEventId());
				UserProfileId senderProfile = getCalendarOwner(calendarId);
				if (senderProfile == null) senderProfile = getTargetProfileId();
				notifyOrganizer(senderProfile, event, attendeeId);
			}
		
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstanceTags(final UpdateTagsOperation operation, final Collection<EventInstanceId> instanceIds, final Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			Set<String> availTags = coreMgr.listTagIds();
			List<String> okTagIds = tagIds.stream()
				.filter(tagId -> availTags.contains(tagId))
				.collect(Collectors.toList());
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.TAGS);
			BitFlags<EventNotifyOption> notifyOpts = EventNotifyOption.withoutAnyAttendeesNotifications();
			
			if (instanceIds.size() == 1) {
				final EventInstanceId instanceId = instanceIds.iterator().next();
				
				con = WT.getConnection(SERVICE_ID, false);
				InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
				Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
				if (calendarId == null) throw new WTException("Unable to get owning calendar [{}]", instanceId);
				
				checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
				
				Event targetEvent = doEventGet(con, info.originalEventId(), processOpts);
				if (targetEvent == null) throw new WTNotFoundException("Event not found [{}]", info.originalEventId());
				
				// Audit: dump tags for later use
				ArrayList<String> auditOldTags = null;				
				if (isAuditEnabled()) auditOldTags = new ArrayList<>(targetEvent.getTagsOrEmpty());
				
				// Perform tag operation in target event
				if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
					if (UpdateTagsOperation.RESET.equals(operation)) targetEvent.getTagsOrEmpty().clear();
					targetEvent.getTagsOrEmpty().addAll(okTagIds);
				} else if (UpdateTagsOperation.UNSET.equals(operation)) {
					targetEvent.getTagsOrEmpty().removeAll(okTagIds);
				}
				
				EventInsertResult insertResult = null;
				try {
					insertResult = doEventInstanceUpdateAndCommit(con, UpdateEventTarget.WHOLE_SERIES, info, targetEvent, processOpts, notifyOpts, availTags);
				} catch (IOException ex1) {/* Due configuration here this will never happen! */}
				
				// Audit: record activities...
				if (isAuditEnabled()) {
					String auditEventId = insertResult != null ? insertResult.oevent.getEventId() : targetEvent.getEventId();
					if (UpdateTagsOperation.RESET.equals(operation)) {
						HashMap<String, List<String>> data = coreMgr.compareTags(auditOldTags, new ArrayList<>(okTagIds));
						auditLogWrite(
							AuditContext.EVENT,
							AuditAction.TAG,
							auditEventId,
							JsonResult.gson().toJson(data)
						);

					} else {
						String tagAction = UpdateTagsOperation.SET.equals(operation) ? "set" : "unset";
						HashMap<String, List<String>> data = new HashMap<>();
						data.put(tagAction, okTagIds);
						auditLogWrite(
							AuditContext.EVENT,
							AuditAction.TAG,
							auditEventId,
							JsonResult.gson().toJson(data)
						);
					}
				}
				
			} else {
				con = WT.getConnection(SERVICE_ID, false);
				
				// Collect necessary data
				InstancesBulkInfo bulkInfos = collectInstancesBulkInfo(con, instanceIds);
				Map<String, Integer> calMap = evtDao.selectCalendarsByIds(con, bulkInfos.involvedIds);
				
				// Audit: prepare batch
				AuditLogManager.Batch auditBatch = null;
				if (isAuditEnabled()) auditBatch = auditLogGetBatch(AuditContext.EVENT, AuditAction.TAG);
				
				// Perform update operation on instances
				Set<Integer> updateOkCache = new HashSet<>();
				for (Map.Entry<EventInstanceId, InstanceInfo> entry : bulkInfos.infoMap.entrySet()) {
					final InstanceInfo info = entry.getValue();
					final String eventId = info.originalEventId();
					if (eventId == null) {
						logger.debug("Event ID is null");
						continue;
					}
					if (!calMap.containsKey(eventId)) throw new WTException("Calendar missing [{}]", eventId);
					checkRightsOnCalendar(updateOkCache, calMap.get(eventId), FolderShare.ItemsRight.UPDATE);

					Event targetEvent = doEventGet(con, eventId, processOpts);
					if (targetEvent == null) {
						logger.warn("Event not found [{}]", eventId);
						continue;
					}
					
					// Audit: dump tags for later use
					ArrayList<String> auditOldTags = null;				
					if (auditBatch != null) auditOldTags = new ArrayList<>(targetEvent.getTagsOrEmpty());
					
					if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
						if (UpdateTagsOperation.RESET.equals(operation)) targetEvent.getTagsOrEmpty().clear();
						targetEvent.getTagsOrEmpty().addAll(okTagIds);
					} else if (UpdateTagsOperation.UNSET.equals(operation)) {
						targetEvent.getTagsOrEmpty().removeAll(okTagIds);
					}
					
					EventInsertResult insertResult = null;
					try {
						insertResult = doEventInstanceUpdateAndCommit(con, UpdateEventTarget.WHOLE_SERIES, info, targetEvent, processOpts, notifyOpts, availTags);
					} catch (IOException ex1) {/* Due configuration here this will never happen! */}

					if (auditBatch != null) {
						String auditEventId = insertResult != null ? insertResult.oevent.getEventId() : targetEvent.getEventId();
						if (UpdateTagsOperation.RESET.equals(operation)) {
							HashMap<String, List<String>> data = coreMgr.compareTags(auditOldTags, new ArrayList<>(okTagIds));
							auditBatch.write(
								auditEventId,
								JsonResult.gson().toJson(data)
							);
							
						} else {
							String tagAction = UpdateTagsOperation.SET.equals(operation) ? "set" : "unset";
							HashMap<String, List<String>> data = new HashMap<>();
							data.put(tagAction, okTagIds);
							auditBatch.write(
								auditEventId,
								JsonResult.gson().toJson(data)
							);
						}
					}
				}
				
				// Audit: finalize batch if available
				if (auditBatch != null) auditBatch.flush();
			}
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEvent(final int calendarId, final String publicUid, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			String eventId = doEventLookupId(con, publicUid, Arrays.asList(calendarId));
			if (eventId == null) throw new WTNotFoundException("Event not found [{}]", publicUid);
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			EventInstanceId instanceId = EventInstanceId.buildMaster(eventId);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			
			doEventInstanceDeleteAndCommit(con, UpdateEventTarget.WHOLE_SERIES, info, notifyOptions, true);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEventInstance(final UpdateEventTarget target, final EventInstanceId instanceId, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		deleteEventInstance(target, Arrays.asList(instanceId), notifyOptions);
	}
	
	@Override
	public void deleteEventInstance(final UpdateEventTarget target, final Collection<EventInstanceId> instanceIds, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			if (instanceIds.size() == 1) {
				EventInstanceId instanceId = instanceIds.iterator().next();
				con = WT.getConnection(SERVICE_ID, false);
				InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
				Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
				if (calendarId == null) throw new WTException("Calendar missing [{}]", info.originalEventId());
				
				checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
				
				//TODO: should we block on privates?
				//if (needsTreatAsPrivate(RunContext.getRunProfileId(), info.eventIsPrivate, calId)) {
				//	throw new AuthException("Event is private and therefore it cannot be updated [{}]", info.eventId);
				//}

				doEventInstanceDeleteAndCommit(con, target, info, notifyOptions, true);
				
			} else {
				//TODO: test this impl.
				con = WT.getConnection(SERVICE_ID, false);
			
				// Collect necessary data
				InstancesBulkInfo bulkInfos = collectInstancesBulkInfo(con, instanceIds);
				Map<String, Integer> calMap = evtDao.selectCalendarsByIds(con, bulkInfos.involvedIds);
				
				// Perform delete operation on instances
				Set<Integer> deleteOkCache = new HashSet<>();
				ArrayList<AuditReferenceDataEntry> deleted = new ArrayList<>();
				for (Map.Entry<EventInstanceId, InstanceInfo> entry : bulkInfos.infoMap.entrySet()) {
					final InstanceInfo info = entry.getValue();
					final String eventId = info.originalEventId();
					if (eventId == null) {
						logger.debug("Event ID is null");
						continue;
					}
					if (!calMap.containsKey(eventId)) throw new WTException("Calendar missing [{}]", eventId);
					checkRightsOnCalendar(deleteOkCache, calMap.get(eventId), FolderShare.ItemsRight.DELETE);
					
					doEventInstanceDeleteAndCommit(con, target, info, notifyOptions, true);
					deleted.add(new AuditEvent(eventId));
				}
				
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, deleted);
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
	public void restoreEventInstance(final EventInstanceId instanceId, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			Integer calendarId = evtDao.selectCalendarById(con, info.originalEventId());
			if (calendarId == null) throw new WTNotFoundException("Event calendar not found [{}]", info.originalEventId());
			
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceRestoreAndCommit(con, info, notifyOptions);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void moveEventInstance(final Collection<EventInstanceId> instanceIds, final int targetCalendarId, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		moveEventInstance(false, instanceIds, targetCalendarId, BitFlags.noneOf(EventGetOption.class), notifyOptions);
	}
	
	@Override
	public void moveEventInstance(final boolean copy, final Collection<EventInstanceId> instanceIds, final int targetCalendarId, final BitFlags<EventGetOption> copyOptions, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			String copyPrefix = lookupResource(getLocale(), "event.copy.prefix");
			BitFlags<EventProcessOpt> processOpts = EventProcessOpt.parseEventGetOptions(copyOptions)
				.set(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
			
			checkRightsOnCalendar(targetCalendarId, FolderShare.ItemsRight.CREATE);
			con = WT.getConnection(SERVICE_ID, false);
			
			// Collect necessary data
			InstancesBulkInfo bulkInfos = collectInstancesBulkInfo(con, instanceIds);
			Map<String, Integer> calMap = evtDao.selectCalendarsByIds(con, bulkInfos.involvedIds);
			Set<String> availTags = coreMgr.listTagIds();
			
			// Perform delete operation
			Set<Integer> readOkCache = new HashSet<>();
			Set<Integer> deleteOkCache = new HashSet<>();
			Map<String, String> operations = new LinkedHashMap<>();
			ArrayList<AuditReferenceDataEntry> copied = new ArrayList<>();
			ArrayList<AuditReferenceDataEntry> moved = new ArrayList<>();
			for (Map.Entry<EventInstanceId, InstanceInfo> entry : bulkInfos.infoMap.entrySet()) {
				InstanceInfo info = entry.getValue();
				final String eventId = info.originalEventId();
				if (eventId == null) {
					logger.debug("Event ID is null");
					continue;
				}
				if (!calMap.containsKey(eventId)) throw new WTException("Calendar missing [{}]", eventId);
				final int calendarId = calMap.get(eventId);
				checkRightsOnCalendar(readOkCache, calendarId, FolderShare.FolderRight.READ);
				
				if (copy || (targetCalendarId != calendarId)) {
					if (copy) {
						Event sourceEvent = doEventGet(con, eventId, processOpts);
						if (sourceEvent == null) throw new WTNotFoundException("Event not found [{}]", eventId);
						
						sourceEvent.prependToTitle(copyPrefix);
						boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), sourceEvent.isVisibilityPrivate(), sourceEvent.getCalendarId());
						if (keepPrivate) sourceEvent.censorize();
						
						if (EventInstanceId.isSeriesItem(entry.getKey(), eventId)) {
							info = new InstanceInfo(EventInstanceId.asMasterInstanceId(entry.getKey()), eventId, sourceEvent);
						}
						EventInsertResult result = doEventInstanceCopy(con, info, sourceEvent, targetCalendarId, processOpts, availTags);
						copied.add(new AuditEventCopy(result.oevent.getEventId(), sourceEvent.getEventId()));
						operations.put(result.oevent.getEventId(), Crud.CREATE);
						
					} else {
						checkRightsOnCalendar(deleteOkCache, calendarId, FolderShare.ItemsRight.DELETE);
						
						List<String> movedEventIds = doEventInstanceMove(con, info, targetCalendarId);
						for (String id : movedEventIds) {
							moved.add(new AuditEventMove(id, calendarId));
							operations.put(id, Crud.CREATE);
						}
					}
				}
			}
			
			DbUtils.commitQuietly(con);
			afterEventOperation(operations, copy ? notifyOptions : EventNotifyOption.withoutAnyAttendeesNotifications(), null);
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
	public Event handleInvitationFromICal(final net.fortuna.ical4j.model.Calendar iCalendar, final Integer calendarId, final BitFlags<HandleICalInviationOption> options) throws WTParseException, WTNotFoundException, WTConstraintException, WTException {
		Check.notNull(iCalendar, "iCalendar");
		Check.notNull(options, "options");
		final UserProfile.Data udata = WT.getProfileData(getTargetProfileId());
		Connection con = null;
		
		final VEvent ve = ICalendarUtils.getVEvent(iCalendar);
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
			
			if (Method.REQUEST.equals(iCalendar.getMethod())) { // A request has been received
				// Organizer -(invite)-> Attendee
				// The organizer sends a REQUEST to an attendee when creates a new 
				// appointment and also when makes some modifications to a placed one
				
				EventBase.Visibility defVisibility = EventBase.Visibility.PUBLIC;
				EventBase.Transparency defTransparency = EventBase.Transparency.OPAQUE;
				if (calendarId != null) {
					VCalendarDefaults defaults = doCalendarGetDefaults(calendarId);
					if (defaults != null) {
						defVisibility = defaults.getDefVisibility();
						defTransparency = defaults.getDefTransparency();
					}
				}
				
				EventInput eventInput = new ICalendarInput(udata.getTimeZone()/*, WT.getCoreManager().listTagNamesById(), WT.getCoreManager().listTagIdsByName()*/)
					.withIgnoreClassification(options.has(HandleICalInviationOption.IGNORE_ICAL_CLASSIFICATION))
					.withIgnoreTransparency(options.has(HandleICalInviationOption.IGNORE_ICAL_TRASPARENCY))
					.withIgnoreAlarms(options.has(HandleICalInviationOption.IGNORE_ICAL_ALARMS))
					.withIgnoreCategories(true) // Ignore tags (see listTagNamesById/listTagIdsByName in ICalendarInput constructor)
					.withIgnoreAttachments(true)
					.withIgnoreCustomValues(true)
					.withDefaultVisibility(defVisibility)
					.withDefaultTransparency(defTransparency)
					.parseEventObject(ve);
				
				String lookupEventId = doEventLookupId(con, scope, eventInput.event.getPublicUid());
				//TODO: add support to recurrence
				// Get saved appointment event, if any...
				BitFlags<EventProcessOpt> getOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES);
				EventInstance oldEvent = null;
				if (lookupEventId != null) {
					EventInstanceId masterInstanceId = EventInstanceId.buildMaster(lookupEventId);
					oldEvent = doEventInstanceGet(con, masterInstanceId, getOpts);
				}
				
				if (options.has(HandleICalInviationOption.CONSTRAIN_AVAILABILITY)) {
					// For now, availability is checked only against built-in calendar
					Integer builtinCalendarId = getBuiltInCalendarId();
					if (builtinCalendarId == null) throw new WTNotFoundException("Built-in calendar not found [{}]", getTargetProfileId());
					
					// Extract event bounds from parsed event
					final List<ComparableEventRange> inputBounds;
					if (eventInput.event.hasRecurrence()) {
						inputBounds = calculateRecurringInstances(new EI2CER_RRContext(new EventInstance(EventInstanceId.buildDummy(), EventInstanceId.DUMMY_EVENT_ID, eventInput.event)), null, null, 365);
					} else {
						inputBounds = Arrays.asList(new ComparableEventRange(EventInstanceId.buildDummy(), eventInput.event.getStart(), eventInput.event.getEnd()));
					}
					
					// Add actual bound (if applicable) to exclusion list
					Set<DateTimeRange2> exclusions = null;
					if (oldEvent != null) {
						exclusions = new LinkedHashSet<>();
						if (oldEvent.hasRecurrence()) {
							exclusions.addAll(calculateRecurringInstances(new EI2CER_RRContext(oldEvent), null, null, 365));
						} else {
							exclusions.add(new ComparableEventRange(EventInstanceId.buildDummy(), oldEvent.getStart(), oldEvent.getEnd()));
						}
					}
					
					DateTimeRange2 boundingRange = findBoundingRange(inputBounds);
					List<ComparableEventRange> otherBounds = doListEventsBounds(con, Arrays.asList(builtinCalendarId), boundingRange.getStart(), boundingRange.getEnd(), udata.getTimeZone());
					if (areMyBoundsOverlappingWith(inputBounds, otherBounds, exclusions)) throw new WTConstraintException("Availability constraint cannot be satisfied");
				}
				
				if (oldEvent == null) { // New invitation
					Check.notNull(calendarId, "calendarId");
					if (!doListCalendarIdsIn(con, getTargetProfileId(), null).contains(calendarId)) {
						throw new WTException("Invitations must be inserted into personal calendar");
					}
					
					checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
					
					eventInput.event.setCalendarId(calendarId);
					
					// Add event turning OFF any notifications: we are simply the 
					// receiver of an invitation and we want to generate bounce invitation messages!
					BitFlags<EventReminderOption> reminderOpts = BitFlags.with(EventReminderOption.IGNORE);
					EventInsertResult result = doEventInputInsert(con, eventInput, getOpts, reminderOpts, null);
					String newEventId = result.oevent.getEventId();
					
					DbUtils.commitQuietly(con);
					if (isAuditEnabled()) {
						auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, newEventId, null);
					}
					
					return doEventGet(con, newEventId, getOpts);
					
				} else { // Invitation update
					checkRightsOnCalendar(oldEvent.getCalendarId(), FolderShare.ItemsRight.UPDATE);
					
					InstanceInfo instanceInfo = new InstanceInfo(oldEvent);
					eventInput.mergeFieldsForInvitation(oldEvent);
					
					BitFlags<EventProcessOpt> updateOpts = BitFlags.with(EventProcessOpt.ATTENDEES);
					doEventInstanceUpdateAndCommit(con, UpdateEventTarget.WHOLE_SERIES, instanceInfo, oldEvent, updateOpts, EventNotifyOption.withoutAnyAttendeesNotifications(), null);
					
					return null;
				}
				
			} else if (Method.REPLY.equals(iCalendar.getMethod())) { // An invitation reply has been received
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
				List<String> updatedAttIds = doEventAttendeeUpdateResponseByRecipient(con, evt.getEventId(), att.getCalAddress().getSchemeSpecificPart(), ICalendarInput.toAttendeeResponseStatus(partStat), true);
				
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
				
			} else if (Method.CANCEL.equals(iCalendar.getMethod())) { // Cancellation has been received
				// Organizer -(cancelled invite)-> Attendee
				// The organizer after cancelling the event send a mail message
				// to all attendees telling to update their saved information.
				
				EventInput eventInput = new ICalendarInput(udata.getTimeZone())
					.withIncludeSourceComponentInOutput(true)
					.parseEventObject(ve);
				
				if (eventInput.isSourceEventCancelled()) {
					String lookupEventId = doEventLookupId(con, scope, eventInput.event.getPublicUid());
					//TODO: add support to recurrence 
					EventInstanceId masterInstanceId = EventInstanceId.buildMaster(lookupEventId);
					InstanceInfo instanceInfo = doEventGetInstanceInfo(con, masterInstanceId);
					
					doEventInstanceDeleteAndCommit(con, UpdateEventTarget.WHOLE_SERIES, instanceInfo, EventNotifyOption.withoutAnyAttendeesNotifications(), false);
					
					//if (eventInput.exRefersToPublicUid != null) {
						//TODO: add support to single event instance (of recurrence) cancellation
					//} else {
					//}
				}		
				return null;
				
			} else {
				throw new WTUnsupportedOperationException("Unsupported Calendar's method [{}]", iCalendar.getMethod() != null ? iCalendar.getMethod().toString() : null);
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
	
	private Set<ComparableEventRange> findOverlappingBounds(Collection<ComparableEventRange> bounds) {
		ArrayList<ComparableEventRange> list = new ArrayList<>(bounds);
		LangUtils.quickSort(list);
		//https://massivealgorithms.blogspot.com/2015/06/given-n-appointments-find-all.html
		Set<ComparableEventRange> overlapping = new HashSet<>();
		ComparableEventRange latest = null;
        Iterator<ComparableEventRange> iter = list.iterator();
        while (iter.hasNext()) {
            ComparableEventRange next = iter.next();
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
	
	private boolean areMyBoundsOverlappingWith(List<ComparableEventRange> myBounds, List<ComparableEventRange> withBounds, Set<? extends DateTimeRange2> matchExclusions) {
		List<ComparableEventRange> list = new ArrayList<>(myBounds.size() + withBounds.size());
		list.addAll(myBounds);
		list.addAll(withBounds);
		list = LangUtils.quickSort(list);
		
		if (logger.isTraceEnabled()) {
			StringBuilder sb = null;
			
			sb = new StringBuilder();
			for (ComparableEventRange ceb : myBounds) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    myBounds:{}", sb.toString());
			
			sb = new StringBuilder();
			for (ComparableEventRange ceb : withBounds) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    withBounds:{}", sb.toString());
			
			if (matchExclusions != null) {
				sb = new StringBuilder();
				for (DateTimeRange2 ceb : matchExclusions) sb.append("\n").append("    (").append(ceb.toString()).append(")");
				logger.trace("[areMyBoundsOverlappingWith]\n    exclusions:{}", sb.toString());
			}	
			
			sb = new StringBuilder();
			for (ComparableEventRange ceb : list) sb.append("\n").append("    (").append(ceb.toString()).append(")");
			logger.trace("[areMyBoundsOverlappingWith]\n    list (sorted):{}", sb.toString());
		}
		
		ComparableEventRange latest = null;
        Iterator<ComparableEventRange> iter = list.iterator();
        while (iter.hasNext()) {
            ComparableEventRange next = iter.next();
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
	
	private void afterEventOperation(final Map<String, String> operations, final BitFlags<EventNotifyOption> notifyOptions, final Map<String, EventEx> eventCache) throws WTException {
		// Param operations is a linkedHashMap of performed operations, where key is the eventId and value is the CUD action
		Map<String, Integer> eventCalendarMap = mapCalendarByEvent(operations.keySet());
		
		for (Map.Entry<String, String> entry : operations.entrySet()) {
			String eventId = entry.getKey();
			String cudAction = entry.getValue();
			
			int calendarId = eventCalendarMap.get(eventId);
			EventEx eventDump = (eventCache != null) ? eventCache.get(eventId) : null;
			if (eventDump == null) eventDump = getEventInstance(EventInstanceId.buildMaster(eventId));
			
			// Notify last modification
			List<RecipientTuple> nmRcpts = getModificationRecipients(calendarId, cudAction);
			if (!nmRcpts.isEmpty()) {
				notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump, cudAction);
			}
			
			// Notify attendees
			List<RecipientTuple> attRcpts = getNotifiableRecipients(cudAction, getTargetProfileId(), eventDump.getOrganizerAddress(), eventDump.getAttendees(), notifyOptions);
			if (!attRcpts.isEmpty()) {
				notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, cudAction);
			}
		}
	}
	
	private void onAfterEventOperation(final String cudAction, final EventEx event, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		// Notify last modification
		List<RecipientTuple> nmRcpts = getModificationRecipients(event.getCalendarId(), cudAction);
		if (!nmRcpts.isEmpty()) {
			notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, event, cudAction);
		}
		
		// Notify attendees
		if (notifyOptions.hasAny(EventNotifyOption.NOTIFY_INDIVIDUAL_ATTENDEE, EventNotifyOption.NOTIFY_RESOURCE_ATTENDEE)) {
			List<RecipientTuple> attRcpts = getNotifiableRecipients(cudAction, getTargetProfileId(), event.getOrganizerAddress(), event.getAttendees(), notifyOptions);
			if (!attRcpts.isEmpty()) {
				notifyForInvitation(getTargetProfileId(), attRcpts, event, cudAction);
			}
		}
	}
	
	/*
	private void afterEventOperation(final String cudAction, final Event event, final boolean notifyIndividualAttendees, final Map<String, Integer> eventIdToCalendarIdMap, final Map<String, Event> eventIdToEventMap) throws WTException {
		Check.notEmpty(cudAction, "cudAction");
		Check.notNull(event, "event");
		afterEventOperation(Collections.singletonMap(event.getEventId(), cudAction), notifyIndividualAttendees, Collections.singletonMap(event.getEventId(), event));
	}
	
	private void afterEventOperation(final Map<String, String> operations, final boolean notifyIndividualAttendees, final Map<String, Integer> eventIdToCalendarIdMap, final Map<String, Event> eventIdToEventMap) throws WTException {
		Check.notNull(eventIdToCalendarIdMap, "eventIdToCalendarIdMap");
		for (Map.Entry<String, String> entry : operations.entrySet()) {
			String eventId = entry.getKey();
			String cudAction = entry.getValue();
			
			Event eventDump = (eventIdToEventMap != null) ? eventIdToEventMap.get(eventId) : null;
			if (eventDump == null) eventDump = getEvent(eventId);
			Integer calendarId = eventIdToCalendarIdMap.get(eventId);
			if (calendarId == null) calendarId = eventDump.getCalendarId();
			
			// Notify last modification
			List<RecipientTuple> nmRcpts = getModificationRecipients(calendarId, cudAction);
			if (!nmRcpts.isEmpty()) {
				notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), cudAction);
			}
			
			// Notify attendees
			List<RecipientTuple> attRcpts = getNotifiableRecipients(cudAction, getTargetProfileId(), eventDump.getOrganizerAddress(), eventDump.getAttendees(), !notifyIndividualAttendees);
			if (!attRcpts.isEmpty()) {
				notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, cudAction);
			}
		}
	}
	*/
	
	@Override
	public void updateEventCalendarTags(final UpdateTagsOperation operation, final int calendarId, final Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		EventTagDAO etagDao = EventTagDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.UPDATE);
			List<String> auditTags = new ArrayList<>();
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				Set<String> availTags = coreMgr.listTagIds();
				List<String> okTagIds = tagIds.stream()
					.filter(tagId -> availTags.contains(tagId))
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
	
	public void importEvents(final int calendarId, final EventStreamReader reader, final InputStream is, final ImportMode mode, final LogHandler logHandler) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		HashMap<String, String> publicIdToMap = new HashMap<>();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.CREATE);
			if (ImportMode.COPY.equals(mode)) checkRightsOnCalendar(calendarId, FolderShare.ItemsRight.DELETE);
			
			logHandler.handleMessage(0, LogMessage.Level.INFO, "Started at {}", new DateTime());
			logHandler.handleMessage(0, LogMessage.Level.INFO, "Reading source...");
			
			List<EventInput> items = null;
			try {
				items = reader.read(is);
			} catch (IOException ex) {
				logHandler.handleMessage(0, LogMessage.Level.ERROR, "Unable to read. Reason: {}", ex.getMessage());
				throw new WTException(ex);
			}
			logHandler.handleMessage(0, LogMessage.Level.INFO, "{} events/s found!", items.size());
			
			Set<String> availTags = coreMgr.listTagIds();
			con = WT.getConnection(SERVICE_ID, false);
			
			if (ImportMode.COPY.equals(mode)) {
				logHandler.handleMessage(0, LogMessage.Level.INFO, "Cleaning events...");
				int del = doEventDeleteByCalendar(con, calendarId, false);
				//TODO: audit delete * operation
				logHandler.handleMessage(0, LogMessage.Level.INFO, "{} event/s deleted!", del);
			}
			
			logHandler.handleMessage(0, LogMessage.Level.INFO, "Importing...");
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.RAW_ICAL);
			BitFlags<EventReminderOption> reminderOpts = BitFlags.with(EventReminderOption.DISARM_PAST);
			int count = 0;
			for (EventInput item : items) {
				item.event.setCalendarId(calendarId);
				try {
					doEventInputInsert(con, item, processOpts, reminderOpts, availTags, publicIdToMap);
					DbUtils.commitQuietly(con);
					count++;
					
				} catch (Throwable t1) {
					logger.trace("Error inserting event", t1);
					DbUtils.rollbackQuietly(con);
					logHandler.handleMessage(0, LogMessage.Level.ERROR, "Unable to import [{}, {}]. Reason: {}", item.event.getTitle(), item.event.getPublicUid(), LangUtils.getDeepestCauseMessage(t1));
				}
			}
			
			DbUtils.commitQuietly(con);
			logHandler.handleMessage(0, LogMessage.Level.INFO, "{} events/s imported!", count);
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Ended at {}", new DateTime()));
		}
	}
	
	@Override
	public Map<String, DateTime> generateTimeSpans(final LocalDate fromDate, final LocalDate toDate, final TimeRange timeRange, final boolean applyTimeRangeForEachDay, final int minuteResolution, final DateTimeZone targetTimezone) {
		LinkedHashMap<String, DateTime> spans = new LinkedHashMap<>();
		DateTimeFormatter ymdhmZoneFmt = JodaTimeUtils.createFormatterYMDHM(targetTimezone);
		
		DateTime boundaryInstant = new DateTime(targetTimezone).withDate(toDate.plusDays(1)).withTimeAtStartOfDay();
		DateTime instant = new DateTime(targetTimezone).withDate(fromDate).withTimeAtStartOfDay();
		while (instant.compareTo(boundaryInstant) < 0) {
			boolean add = false;
			if (timeRange != null) {
				final LocalTime time = instant.toLocalTime();
				if (applyTimeRangeForEachDay) {
					add = JodaTimeUtils.between(time, timeRange.getStart(), timeRange.getEnd());
				} else {
					final LocalDate date = instant.toLocalDate();
					if (date.isEqual(fromDate) && date.isEqual(toDate)) {
						add = JodaTimeUtils.between(time, timeRange.getStart(), timeRange.getEnd());
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
	
	class EventInputConsumerImpl implements EventInputConsumer {
		final BitFlags<EventProcessOpt> processOpts;
		final BitFlags<EventReminderOption> reminderOpts;
		Connection con;
		Integer calendarId;
		HashMap<String, OEvent> uidMap;
		Map<String, String> tagNamesById;
		Map<String, List<String>> tagIdsByName;
		LogEntries log;
		int count = 0;
		
		EventInputConsumerImpl(Connection con, Integer calendarId, HashMap<String, OEvent> uidMap, Map<String, String> tagNamesById, Map<String, List<String>> tagIdsByName, LogEntries log) {
			this.processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES);
			this.reminderOpts = BitFlags.with(EventReminderOption.DISARM_PAST);
			this.con = con;
			this.calendarId = calendarId;
			this.uidMap = uidMap;
			this.tagNamesById = tagNamesById;
			this.tagIdsByName = tagIdsByName;
			this.log = log;
		}
		
		@Override
		public void consume(EventInput ei) {
			ei.event.setCalendarId(calendarId);
			
			try {
				
				doEventInputInsert(con, ei, processOpts, reminderOpts, tagNamesById.keySet());
				DbUtils.commitQuietly(con);
				count++;
				
			} catch (Exception ex) {
				logger.error("Error inserting event", ex);
				DbUtils.rollbackQuietly(con);
				log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", ei.event.getTitle(), ei.event.getPublicUid(), ex.getMessage()));
			}
		}
		
		public int getCount() {
			return count;
		}

	}
	
	public void exportEvents(DateTime fromDate, DateTime toDate, OutputStream os, final LogHandler logHandler) throws Exception {
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
			final UserProfile.Data pdata = WT.getProfileData(pid);
			HashMap<String, Object> map = null;
			for (Integer calendarId : listMyCalendarIds()) {
				for (VEventLookup vel : edao.viewOnlineByCalendarRangeCondition(con, Arrays.asList(calendarId), toDate, toDate, null)) {
					if (vel.getHasRecurrence()) {
						EventLookupInstance eli = new EventLookupInstance(vel.createInstanceId(), vel.getEventId(), /* unused here */ false);
						try {
							map = new HashMap<>();
							map.put("userId", pid.getUserId());
							map.put("descriptionId", pdata.getDisplayName());
							fillExportMapBasic(map, coreMgr, con, eli);
							fillExportMapDates(map, pdata.getTimeZone(), eli);
							mapw.write(map, headers, processors);
						} catch (Exception ex) {
							logHandler.handleMessage(0, LogMessage.Level.ERROR, "Event skipped [{}]. Reason: {}", vel.getEventId(), ex.getMessage());
						}
						
					} else {
						for (EventLookupInstance eli : calculateRecurringInstances(new VEL2ELI_RRContext(con, vel, false), fromDate, toDate, 1000)) {
							try {
								map = new HashMap<>();
								map.put("userId", pid.getUserId());
								map.put("descriptionId", pdata.getDisplayName());
								fillExportMapBasic(map, coreMgr, con, eli);
								fillExportMapDates(map, pdata.getTimeZone(), eli);
								mapw.write(map, headers, processors);
							} catch (Exception ex) {
								logHandler.handleMessage(0, LogMessage.Level.ERROR, "Event skipped [{}]. Reason: {}", vel.getEventId(), ex.getMessage());
							}
						}
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
		Connection con = null;
		
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			UserProfileId pid = getTargetProfileId();
			
			// Erase events and related tables
			if (deep) {
				for (OCalendar ocal : caldao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
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
			if (shouldLog) logger.debug("[reminders] Retrieving expiring instances... [{} -> {}]", from, to);
			
			int noOfRecurringInst = 7*2+1;
			ArrayList<EventAlertLookupInstance> instances = new ArrayList<>();
			for (VEventLookup vel : evtDao.viewOnlineExpiredByRangeForUpdate(con, from, to)) {
				if (!vel.getHasRecurrence()) {
					instances.add(ManagerUtils.fillEventAlertLookup(new EventAlertLookupInstance(vel.createInstanceId(), vel.getEventId()), vel));
				} else {
					instances.addAll(calculateRecurringInstances(new EALI2VEL_RRContext(con, vel), from, to, noOfRecurringInst));
				}
			}
			
			HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
			
			int i = 0;
			if (shouldLog) logger.debug("[reminders] Found {} candidates", instances.size());
			for (EventAlertLookupInstance instance : instances) {
				i++;
				try {
					if (shouldLog) logger.debug("[reminders][{}] Working on instance [{}]", i, instance.getId().toString());
					
					UserProfile.Data pdata = WT.getProfileData(instance.getCalendarProfileId());
					if (pdata == null) throw new WTException("UserData is null [{}]", instance.getCalendarProfileId());
					final DateTime profileNow = now.withZone(pdata.getTimeZone());
					final DateTime remindAt = instance.getStart().withZone(pdata.getTimeZone()).minusMinutes(EventBase.Reminder.getMinutesValue(instance.getReminder()));
					if (profileNow.compareTo(remindAt) < 0) {
						if (shouldLog) logger.debug("[reminders][{}] Skipped: remind instant '{}' not yet reached [{}]", i, remindAt, instance.getId().toString());
						continue; // Skip if now is not after remindOn
					}
					
					if (instance.getRemindedAt() != null) { // Instance could have been reminded in the past (only for series)
						final DateTime lastRemindedAt = instance.getRemindedAt().withZone(pdata.getTimeZone());
						if (remindAt.compareTo(lastRemindedAt) <= 0) {
							if (shouldLog) logger.debug("[reminders][{}] Skipped: remind instant '{}' is in past [{}]", i, remindAt, lastRemindedAt, instance.getId().toString());
							continue; // Skip if remindOn is not after last remindOn
						}
					}
					
					if (!byEmailCache.containsKey(instance.getCalendarProfileId())) {
						CalendarUserSettings us = new CalendarUserSettings(SERVICE_ID, instance.getCalendarProfileId());
						boolean bool = us.getEventReminderDelivery().equals(CalendarSettings.EVENT_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(instance.getCalendarProfileId(), bool);
					}
					
					if (shouldLog) logger.debug("[reminders][{}] Building alert [{}]", i, instance.getId().toString());
					
					BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES);
					EventInstance event = doEventInstanceGet(con, instance.getId(), processOpts);
					if (event == null) {
						if (shouldLog) logger.debug("[reminders][{}] Unable to get instance data [{}]", i, instance.getId().toString());
						continue;
					}
					
					BaseReminder alert = null;
					if (byEmailCache.get(instance.getCalendarProfileId())) {
						alert = createEventReminderAlertEmail(pdata.toProfileI18n(), instance.getId().toString(), instance, event, pdata.getPersonalEmailAddress());
					} else {
						alert = createEventReminderAlertWeb(pdata.toProfileI18n(), instance.getId().toString(), instance, event);
					}
					
					if (shouldLog) logger.debug("[reminders][{}] Updating event record [{}]", i, instance.getId().toString());
					int ret = evtDao.updateRemindedOn(con, instance.getOriginalEventId(), now);
					if (ret != 1) continue;
					
					alerts.add(alert);
					if (shouldLog) logger.debug("[reminders][{}] Alert queued [{}]", i, instance.getId().toString());
					
				} catch (Throwable t1) {
					logger.warn("[reminders][{}] Unable to prepare alert [{}]", i, instance.getId().toString(), t1);
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			logger.error("[reminders] Error collecting alerts", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
	}
	
	/**
	 * @deprecated update signature to pass IDs instead of Calendar objects
	 */
	@Deprecated public void outputICalEventsByCalendarId(Calendar calendar, OutputStream out) throws WTException, IOException {
		CoreManager coreMgr = getCoreManager();
		ICalendarOutput icout = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()), coreMgr.listTagNamesById());
		outputICalEvents(calendar.getCalendarId(), icout, out);
	}
	
	/**
	 * @deprecated update signature to pass IDs instead of Calendar objects
	 */
	@Deprecated public void outputICalEventsAsZipEntries(List<Calendar> calendars, ZipOutputStream zos) throws WTException, IOException {
		CoreManager coreMgr = getCoreManager();
		ICalendarOutput icout = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()), coreMgr.listTagNamesById());
		for(Calendar calendar: calendars) {
			ZipEntry ze=new ZipEntry("Events-"+calendar.getUserId()+"-"+calendar.getName()+".ics");
			zos.putNextEntry(ze);
			outputICalEvents(calendar.getCalendarId(), icout, zos);
			zos.closeEntry();
			zos.flush();
		}
	}
	
	private void outputICalEvents(int calendarId, ICalendarOutput vout, OutputStream out) throws WTException, IOException {
		CoreManager coreMgr = getCoreManager();
		EventDAO eventDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendar(calendarId, FolderShare.FolderRight.READ);
			con = WT.getConnection(SERVICE_ID);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES);
			String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
			String icalPre = "BEGIN:VCALENDAR\r\n"+ICalendarUtils.newCalendar(prodId, null).getProperties();
			String icalEnd = "END:VCALENDAR\r\n";
			IOUtils.copy(IOUtils.toInputStream(icalPre, StandardCharsets.UTF_8), out);
			con = WT.getConnection(SERVICE_ID);
			eventDao.lazy_viewOnlineEventObjectsByCalendarSinceCondition(
				con,
				Arrays.asList(calendarId),
				null,
				null,
				false,
				Integer.MAX_VALUE,
				ManagerUtils.toOffset(1, Integer.MAX_VALUE),
				new VEventObject.Consumer() {
					@Override
					public void consume(VEventObject vco, Connection con) throws WTException {
						EventObjectWithBean eventObj = (EventObjectWithBean)doEventObjectPrepare(con, vco, EventObjectOutputType.BEAN, processOpts, tagNamesByIdMap);
						VEvent ve = vout.createEventObject(eventObj.getEvent(), null);
						InputStream is = IOUtils.toInputStream(ve.toString(), StandardCharsets.UTF_8);
						try {
							IOUtils.copy(is, out);
							is.close();
						} catch(IOException exc) {
							throw new WTException(exc);
						}
					}
				}
			);
			IOUtils.copy(IOUtils.toInputStream(icalEnd, StandardCharsets.UTF_8), out);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
		CoreManager coreMgr = getCoreManager();
		final UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		final ICalendarInput icalInput = new ICalendarInput(pdata.getTimeZone());
		final String PENDING_KEY = String.valueOf(calendarId);
		final String logPrefix = "RemoteSync-" + PENDING_KEY;
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		if (pendingRemoteCalendarSyncs.putIfAbsent(PENDING_KEY, RunContext.getRunProfileId()) != null) {
			throw new ConcurrentSyncException("Sync activity is already running [{}, {}]", calendarId, RunContext.getRunProfileId());
		}
		
		try {
			//checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID, false);
			HistoryDAO.getInstance().ignoreEventsHistoryForCurrentTransaction(con);
			
			Calendar cal = ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) throw new WTException("Calendar not found [{}]", calendarId);
			if (!Calendar.Provider.WEBCAL.equals(cal.getProvider()) && !Calendar.Provider.CALDAV.equals(cal.getProvider())) {
				throw new WTException("Specified calendar is not remote (webcal or CalDAV) [{}]", calendarId);
			}
			
			// Force a full update if last-sync date is null
			if (cal.getRemoteSyncTimestamp() == null) {
				full = true;
				logger.debug("[{}] Last sync timestamp is missing: a full sync will be performed!", logPrefix, logPrefix);
			}
			
			CalendarRemoteParameters params = LangUtils.deserialize(cal.getParameters(), CalendarRemoteParameters.class);
			if (params == null) throw new WTException("Unable to deserialize remote parameters [{}]", calendarId);
			if (params.url == null) throw new WTException("Remote URL is undefined [{}]", calendarId);
			
			final Set<String> validTags = coreMgr.listTagIds();
			final BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, /*EventProcessOpt.ATTACHMENTS,*/ EventProcessOpt.TAGS, EventProcessOpt.CUSTOM_VALUES);
			final BitFlags<EventReminderOption> reminderOpts = BitFlags.with(EventReminderOption.IGNORE);
			
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
					final DateTime newLastSync = JodaTimeUtils.now();
					tempFile = WT.createTempFile(PREFIX, null);
					
					// Retrieve webcal content (iCalendar) from the specified URL and save it locally
					logger.debug("[{}] Downloading iCalendar file from URL [{}]", logPrefix, newUrl);
					HttpClient httpCli = null;
					FileOutputStream os = null;
					try {
						httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), newUrl);
						os = new FileOutputStream(tempFile);
						HttpClientUtils.writeContent(httpCli, newUrl, os);
						
					} catch(IOException ex) {
						throw new WTException(ex, "Unable to retrieve webcal at '{}'", newUrl);
					} finally {
						IOUtils.closeQuietly(os);
						HttpClientUtils.closeQuietly(httpCli);
					}
					logger.debug("[{}] iCalendar file saved to temp [{}]", logPrefix, tempFile.getName());

					// Parse downloaded iCalendar
					logger.debug("[{}] Parsing iCalendar file", logPrefix);
					net.fortuna.ical4j.model.Calendar ical = null;
					FileInputStream is = null;
					try {
						is = new FileInputStream(tempFile);
						ical = ICalendarUtils.parse(is);
						//TODO: add support to FILENAME property (Google https://github.com/ical4j/ical4j/issues/69)
					} catch (IOException | ParserException ex) {
						throw new WTException(ex, "Unable to read iCalendar [{}]", tempFile.getName());
					} finally {
						IOUtils.closeQuietly(os);
					}
					
					icalInput.withIncludeSourceComponentInOutput(true);
					ArrayList<EventInput> input = icalInput.parseEventObjects(ical);
					logger.debug("[{}] Parsing done, found {} events", logPrefix, input.size());
					
					Map<String, VEventHrefSync> syncByHref = null;
							
					if (full) {
						logger.debug("[{}] Empting calendar '{}'", logPrefix, calendarId);
						doEventDeleteByCalendar(con, calendarId, false);
					} else {
						EventDAO evtDao = EventDAO.getInstance();
						syncByHref = evtDao.viewHrefSyncDataByCalendar(con, calendarId);
					}
					
					// Inserts/Updates data...
					logger.debug("[{}] Begin processing events", logPrefix);
					try {
						String autoUidPrefix = DigestUtils.md5Hex(newUrl.toString()); // auto-gen base prefix in case of missing UID
						HashSet<String> hrefs = new HashSet<>();
						Map<String, String> eventIdByPublicIdMap = new HashMap<>();
						int i = 0;
						for (EventInput ei : input) {
							if (StringUtils.isBlank(ei.event.getPublicUid())) {
								String autoUid = autoUidPrefix + "-" + i;
								ei.event.setPublicUid(autoUid);
								if (logger.isTraceEnabled()) logger.trace("[{}] Missing UID: using auto-gen value '{}'", logPrefix, autoUid);
							}
							String href = ManagerUtils.buildHref(ei.event.getPublicUid());
							if (logger.isTraceEnabled()) logger.trace("[{}] Working on event '{}'", logPrefix, href);
							
							//if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
							if (hrefs.contains(href)) {
								if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' is duplicated, skipping it", logPrefix, href);
								continue;
							}
							
							boolean skip = false;
							String matchingEventId = null;
							String eiHash = ei.computeDataHash();
							
							if (syncByHref != null) { // Only if... (!full) see above!
								VEventHrefSync hrefSync = syncByHref.remove(href);
								if (hrefSync != null) { // Href found -> maybe updated item
									if (!StringUtils.equals(hrefSync.getEtag(), eiHash)) {
										matchingEventId = hrefSync.getEventId();
										if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' was updated [{}]", logPrefix, href, eiHash);
									} else {
										skip = true;
										if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' is not modified [{}]", logPrefix, href, eiHash);
									}
								} else { // Href not found -> added item
									if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' was newly added [{}]", logPrefix, href, eiHash);
								}
							}
							
							if (!skip) {
								ei.event.setCalendarId(calendarId);
								ei.event.setHref(href);
								ei.event.setEtag(eiHash);
								
								if (matchingEventId != null) {
									if (logger.isTraceEnabled()) logger.trace("[{}] Updating event '{}'", logPrefix, matchingEventId);
									boolean updated = doEventInputUpdateLegacy(con, matchingEventId, ei, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
									//boolean updated = doEventInputUpdate(con, matchingEventId, new ArrayList(Arrays.asList(ei)), processOpts, validTags);
									if (!updated) throw new WTException("Event not found [{}]", matchingEventId);
									
								} else {
									if (logger.isTraceEnabled()) logger.trace("[{}] Inserting event '{}'", logPrefix, href);
									doEventInputInsert(con, ei, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
								}
							}
							
							hrefs.add(href); // Marks as processed!
						}
						
						if (syncByHref != null) { // Only if... (!full) see above!
							// Remaining hrefs -> deleted items
							for (VEventHrefSync hrefSync : syncByHref.values()) {
								if (logger.isTraceEnabled()) {
									logger.trace("[{}] Event was deleted '{}'", logPrefix, hrefSync.getHref());
									logger.trace("[{}] Deleting event '{}'", logPrefix, hrefSync.getEventId());
								}
								doEventDelete(con, hrefSync.getEventId(), false);
							}
						}
						
						eventIdByPublicIdMap.clear();
						calDao.updateRemoteSyncById(con, calendarId, newLastSync, null);
						DbUtils.commitQuietly(con);

					} catch (Exception ex) {
						DbUtils.rollbackQuietly(con);
						logger.error("[{}] {}", logPrefix, ex.getMessage());
						throw new WTException(ex, "Error importing iCalendar");
					}
					
				} finally {
					if (tempFile != null) {
						logger.debug("[{}] Removing temp file '{}'", logPrefix, tempFile.getName());
						WT.deleteTempFile(tempFile);
					}
				}
				
			} else if (Calendar.Provider.CALDAV.equals(cal.getProvider())) {
				CalDav dav = getCalDav(params.username, params.password);
				
				try {
					DavCalendar dcal = dav.getCalendarSyncToken(params.url.toString());
					if (dcal == null) throw new WTException("DAV calendar not found");
					
					final boolean syncIsSupported = !StringUtils.isBlank(dcal.getSyncToken());
					final DateTime newLastSync = JodaTimeUtils.now();
					
					if (!full && (syncIsSupported && !StringUtils.isBlank(cal.getRemoteSyncTag()))) { // Partial update using SYNC mode
						String newSyncToken = dcal.getSyncToken();
						
						logger.debug("[{}] Querying CalDAV endpoint for changes [{}, {}]", logPrefix, params.url.toString(), cal.getRemoteSyncTag());
						List<DavSyncStatus> changes = dav.getCalendarChanges(params.url.toString(), cal.getRemoteSyncTag());
						logger.debug("[{}] Returned {} items", logPrefix, changes.size());
						
						try {
							if (!changes.isEmpty()) {
								EventDAO evtDao = EventDAO.getInstance();
								Map<String, List<String>> eventIdsByHref = evtDao.selectHrefsByByCalendar(con, calendarId);
								
								// Process changes...
								logger.debug("[{}] Processing changes", logPrefix);
								HashSet<String> hrefs = new HashSet<>();
								for (DavSyncStatus change : changes) {
									String href = FilenameUtils.getName(change.getPath());
									//String href = change.getPath();
									
									if (DavUtil.HTTP_SC_TEXT_OK.equals(change.getResponseStatus())) {
										hrefs.add(href);

									} else { // Event deleted
										List<String> eventIds = eventIdsByHref.get(href);
										String eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;
										if (eventId == null) {
											logger.warn("[{}] Deletion not possible. Event path not found [{}]", logPrefix, PathUtils.concatPaths(dcal.getPath(), FilenameUtils.getName(href)));
											continue;
										}
										doEventDelete(con, eventId, false);
									}
								}

								// Retrieves events list from DAV endpoint (using multiget)
								logger.debug("[{}] Retrieving inserted/updated events [{}]", logPrefix, hrefs.size());
								Collection<String> paths = hrefs.stream().map(href -> PathUtils.concatPaths(dcal.getPath(), FilenameUtils.getName(href))).collect(Collectors.toList());
								List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString(), paths);
								//List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString(), hrefs);

								// Inserts/Updates data...
								logger.debug("[{}] Inserting/Updating events", logPrefix);
								Map<String, String> eventIdByPublicIdMap = new HashMap<>();
								for (DavCalendarEvent devt : devts) {
									String href = FilenameUtils.getName(devt.getPath());
									//String href = devt.getPath();
									
									if (logger.isTraceEnabled()) logger.trace("[{}]\n{}", logPrefix, ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
									List<String> eventIds = eventIdsByHref.get(href);
									String eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;
									
									final ArrayList<EventInput> input = icalInput.parseEventObjects(devt.getCalendar());
									if (input.size() != 1) throw new WTException("iCal must contain one event");
									final EventInput ei = input.get(0);
									
									if (eventId != null) {
										doEventDelete(con, eventId, false);
									}
									
									ei.event.setCalendarId(calendarId);
									ei.event.setHref(href);
									ei.event.setEtag(devt.geteTag());
									doEventInputInsert(con, ei, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
								}
							}
							
							calDao.updateRemoteSyncById(con, calendarId, newLastSync, newSyncToken);
							DbUtils.commitQuietly(con);
							
						} catch(Exception ex) {
							DbUtils.rollbackQuietly(con);
							logger.error("[{}] {}", logPrefix, ex.getMessage());
							throw new WTException(ex, "Error importing iCalendar");
						}
						
					} else { // Full update or partial computing hashes
						String newSyncToken = null;
						if (syncIsSupported) { // If supported, saves last sync-token issued by the server
							newSyncToken = dcal.getSyncToken();
						}
						
						// Retrieves cards from DAV endpoint
						logger.debug("[{}] Querying CalDAV endpoint [{}]", logPrefix, params.url.toString());
						List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString());
						logger.debug("[{}] Returned {} items", devts.size());
						
						// Handles data...
						try {
							Map<String, VEventHrefSync> syncByHref = null;
							
							if (full) {
								logger.debug("[{}] Cleaning up calendar [{}]", logPrefix, calendarId);
								doEventDeleteByCalendar(con, calendarId, false);
							} else if (!full && !syncIsSupported) {
								// This hash-map is only needed when syncing using hashes
								EventDAO evtDao = EventDAO.getInstance();
								syncByHref = evtDao.viewHrefSyncDataByCalendar(con, calendarId);
							}	
							
							logger.debug("[{}] Processing results...", logPrefix);
							// Define a simple map in order to check duplicates.
							// eg. SOGo passes same card twice :(
							HashSet<String> hrefs = new HashSet<>();
							Map<String, String> eventIdByPublicIdMap = new HashMap<>();
							for (DavCalendarEvent devt : devts) {
								String href = PathUtils.getFileName(devt.getPath());
								//String href = devt.getPath();
								String etag = devt.geteTag();
								
								if (logger.isTraceEnabled()) logger.trace("[{}]\n{}", logPrefix, ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
								if (hrefs.contains(href)) {
									if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' is duplicated, skipping it", logPrefix, href);
									continue;
								}
								
								boolean skip = false;
								String matchingEventId = null;
								
								if (syncByHref != null) { // Only if... (!full && !syncIsSupported) see above!
									//String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
									//String hash = DigestUtils.md5Hex(new ICalendarOutput(prodId, true).write(devt.getCalendar()));
									String hash = DigestUtils.md5Hex(ICalendarUtils.getVEvent(devt.getCalendar()).toString());
									
									VEventHrefSync hrefSync = syncByHref.remove(href);
									if (hrefSync != null) { // Href found -> maybe updated item
										if (!StringUtils.equals(hrefSync.getEtag(), hash)) {
											matchingEventId = hrefSync.getEventId();
											etag = hash;
											if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' was updated [{}]", logPrefix, href, hash);
										} else {
											skip = true;
											if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' is not modified [{}]", logPrefix, href, hash);
										}
									} else { // Href not found -> added item
										if (logger.isTraceEnabled()) logger.trace("[{}] Event '{}' was newly added [{}]", logPrefix, href, hash);
										etag = hash;
									}
								}
								
								if (!skip) {
									final ArrayList<EventInput> input = icalInput.parseEventObjects(devt.getCalendar());
									if (input.size() != 1) throw new WTException("iCal must contain one event");
									final EventInput ei = input.get(0);
									ei.event.setCalendarId(calendarId);
									ei.event.setHref(href);
									ei.event.setEtag(etag);
									
									if (matchingEventId == null) {
										doEventInputInsert(con, ei, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
									} else {
										boolean updated = doEventInputUpdateLegacy(con, matchingEventId, ei, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
										//boolean updated = doEventInputUpdate(con, matchingEventId, new ArrayList(Arrays.asList(ei)), processOpts, validTags);
										if (!updated) throw new WTException("Event not found [{}]", matchingEventId);
									}
								}
								
								hrefs.add(href); // Marks as processed!
							}
							
							if (syncByHref != null) { // Only if... (!full && !syncIsSupported) see above!
								// Remaining hrefs -> deleted items
								for (VEventHrefSync hrefSync : syncByHref.values()) {
									if (logger.isTraceEnabled()) {
										logger.trace("[{}] Event was deleted '{}'", logPrefix, hrefSync.getHref());
										logger.trace("[{}] Deleting event '{}'", logPrefix, hrefSync.getEventId());
									}
									doEventDelete(con, hrefSync.getEventId(), false);
								}
							}
							
							calDao.updateRemoteSyncById(con, calendarId, newLastSync, newSyncToken);
							DbUtils.commitQuietly(con);
							
						} catch (Exception ex) {
							DbUtils.rollbackQuietly(con);
							logger.error("[{}] {}", logPrefix, ex.getMessage());
							throw new WTException(ex, "Error importing iCalendar");
						}
					}
					
				} catch (DavException ex) {
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
	
	public static String buildEventPublicUrl(String publicBaseUrl, String eventPublicId) {
		String s = PublicService.PUBPATH_CONTEXT_EVENT + "/" + eventPublicId;
		return PathUtils.concatPaths(publicBaseUrl, s);
	}
	
	public static String buildEventReplyPublicUrl(String publicBaseUrl, String eventPublicId, String attendeePublicId, String resp) {
		String s = PublicService.PUBPATH_CONTEXT_EVENT + "/" + eventPublicId + "/" + PublicService.EventUrlPath.TOKEN_REPLY + "?aid=" + attendeePublicId + "&resp=" + resp;
		return PathUtils.concatPaths(publicBaseUrl, s);
	}
	
	private void fillExportMapDates(HashMap<String, Object> map, DateTimeZone timezone, EventLookupInstance sei) throws Exception {
		DateTime startDt = sei.getStart().withZone(timezone);
		map.put("startDate", startDt);
		map.put("startTime", startDt);
		DateTime endDt = sei.getEnd().withZone(timezone);
		map.put("endDate", endDt);
		map.put("endTime", endDt);
		map.put("timezone", sei.getTimezone());
		map.put("duration", Minutes.minutesBetween(sei.getEnd(), sei.getStart()).size());
	}
	
	private void fillExportMapBasic(HashMap<String, Object> map, CoreManager coreMgr, Connection con, EventLookupInstance event) throws Exception {
		map.put("eventId", event.getOriginalEventId());
		map.put("title", event.getTitle());
		map.put("description", event.getDescription());
		/*
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
		*/
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
	
	private Map<String, Integer> mapCalendarByEvent(Collection<String> eventIds) throws WTException {
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
	
	private Calendar doCalendarInsert(Connection con, CalendarBase cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		OCalendar ocal = ManagerUtils.createOCalendar(cal);
		ocal.setCalendarId(calDao.getSequence(con).intValue());
		ManagerUtils.fillOCalendarWithDefaults(ocal, getTargetProfileId(), getServiceSettings());
		
		calDao.insert(con, ocal, revisionTimestamp);
		return ManagerUtils.createCalendar(ocal);
	}
	
	private boolean doCalendarUpdate(Connection con, int calendarId, CalendarBase cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		OCalendar ocal = ManagerUtils.createOCalendar(cal);
		ocal.setCalendarId(calendarId);
		ManagerUtils.fillOCalendarWithDefaults(ocal, getTargetProfileId(), getServiceSettings());
		
		return calDao.update(con, ocal, revisionTimestamp) == 1;
	}
	
	
	
	private EventObject doEventObjectPrepare(Connection con, VEventObject vevent, EventObjectOutputType outputType, Map<String, String> tagNamesByIdMap) throws WTException {
		BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.TAGS);
		return doEventObjectPrepare(con, vevent, outputType, processOpts, tagNamesByIdMap);
	}
	
	private EventObject doEventObjectPrepare(Connection con, VEventObject vevent, EventObjectOutputType outputType, final BitFlags<EventProcessOpt> processOptions, Map<String, String> tagNamesByIdMap) throws WTException {
		if (EventObjectOutputType.STAT.equals(outputType)) {
			return ManagerUtils.fillEventObject(new EventObject(), vevent);
			
		} else {
			EventEx event = ManagerUtils.fillEvent(new EventEx(), vevent);
			if (processOptions.has(EventProcessOpt.RECUR) && vevent.getHasRecurrence()) {
				EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
				OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, vevent.getEventId());
				if (orec != null) {
					EventRecurrence rec = new EventRecurrence(orec.getRule(), orec.getStart());
					if (processOptions.has(EventProcessOpt.RECUR_EX)) {
						rec.setExcludedDates(recDao.selectRecurrenceExByEvent(con, vevent.getEventId()));
					}
					event.setRecurrence(rec);
				}
			}
			if (processOptions.has(EventProcessOpt.ATTENDEES)) {
				EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
				List<OEventAttendee> oattendees = atteDao.selectByEvent(con, vevent.getEventId());
				event.setAttendees(ManagerUtils.createEventAttendeeList(oattendees));
			}
			if (processOptions.has(EventProcessOpt.ATTACHMENTS) && vevent.getHasAttachments()) {
				EventAttachmentDAO attDao = EventAttachmentDAO.getInstance();
				List<VEventAttachmentWithBytes> oatts = attDao.selectByEventWithBytes(con, vevent.getEventId());
				event.setAttachments(ManagerUtils.createEventAttachmentListWithBytes(oatts));
			}
			if (processOptions.has(EventProcessOpt.TAGS) && !StringUtils.isBlank(vevent.getTags())) {
				event.setTags(new LinkedHashSet(new CId(vevent.getTags()).getTokens()));
			}
			if (processOptions.has(EventProcessOpt.CUSTOM_VALUES) && vevent.getHasCustomValues()) {
				EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
				List<OEventCustomValue> ovals = cvalDao.selectByEvent(con, vevent.getEventId());
				event.setCustomValues(ManagerUtils.createCustomValuesMap(ovals));
			}
			
			boolean keepPrivate = needsTreatAsPrivate(RunContext.getRunProfileId(), event.isVisibilityPrivate(), event.getCalendarId());
			if (keepPrivate) event.censorize();
			
			if (EventObjectOutputType.ICALENDAR.equals(outputType)) {
				EventICalendarDAO icalDao = EventICalendarDAO.getInstance();
				
				net.fortuna.ical4j.model.PropertyList extraProps = null;
				if (vevent.getHasIcalendar()) {
					String rawICalendar = icalDao.selectRawDataById(con, vevent.getEventId());
					if (rawICalendar != null) {
						try {
							extraProps = ICalendarUtils.extractProperties(ICalendarUtils.parse(rawICalendar), net.fortuna.ical4j.model.component.VEvent.class);
						} catch (IOException | ParserException ex) {
							logger.debug("ICalendarUtils.extractProperties", ex);
						}
					}
				}
				
				EventObjectWithICalendar ret = ManagerUtils.fillEventObject(new EventObjectWithICalendar(), vevent);
				ICalendarOutput output = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()), tagNamesByIdMap);
				try {
					ret.setIcalendar(output.writeICalendar(null, event, extraProps));
				} catch (Exception ex) {
					// Extra properties carried over from external clients (eg. DavX5/ical4j) can make
					// ical4j's output validation fail, surfacing here as a (wrapped) exception and leaving
					// the iCalendar body null: that null then breaks the CalDAV calendar-query on the client.
					// Fall back to the pre-refactor behaviour and regenerate the body from the model only,
					// so a valid, parseable iCalendar is always produced. The cached raw_data is left
					// untouched, preserving merge-on-write fidelity.
					if (extraProps != null) {
						logger.warn("writeICalendar failed for event {} ({}); retrying without extra properties", vevent.getEventId(), vevent.getPublicUid(), ex);
						try {
							ret.setIcalendar(output.writeICalendar(null, event, null));
						} catch (Exception ex2) {
							logger.error("writeICalendar failed for event {} ({}) even without extra properties; iCalendar body will be empty", vevent.getEventId(), vevent.getPublicUid(), ex2);
						}
					} else {
						// No extra properties to drop: the failure is in the base event itself, so a retry
						// would fail identically. Log the offending event id rather than failing silently.
						logger.error("writeICalendar failed for event {} ({}); iCalendar body will be empty", vevent.getEventId(), vevent.getPublicUid(), ex);
					}
				}
				return ret;
				
			} else {
				EventObjectWithBean ret = ManagerUtils.fillEventObject(new EventObjectWithBean(), vevent);
				ret.setEvent(event);
				return ret;
			}
		}
	}
	
	private String doEventGetIdByCalendarHref(Connection con, int calendarId, String href, boolean throwExIfManyMatchesFound) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		List<String> ids = evtDao.selectOnlineIdsByCalendarHrefs(con, calendarId, href);
		if (ids.isEmpty()) throw new WTNotFoundException("Event not found [{}, {}]", calendarId, href);
		if (throwExIfManyMatchesFound && (ids.size() > 1)) throw new WTException("Many matches for href [{}]", href);
		return ids.get(ids.size()-1);
	}
	
	private EventInsertResult doEventInputInsert(Connection con, EventInput input, BitFlags<EventProcessOpt> processOpts, BitFlags<EventReminderOption> reminderOpts, Set<String> validTags) throws DAOException, IOException {
		return doEventInputInsert(con, input, processOpts, reminderOpts, validTags, new HashMap<>());
	}
	
	private EventInsertResult doEventInputInsert(Connection con, EventInput input, BitFlags<EventProcessOpt> processOpts, BitFlags<EventReminderOption> reminderOpts, Set<String> validTags, Map<String, String> eventIdByPublicIdMap) throws DAOException, IOException {
		
		String rawICalendar = null;
		if (input.extraProps != null) {
			// Creates a rawICalendar using only not-used properties
			rawICalendar = ICalendarUtils.printProperties(input.extraProps, "VEVENT");
		}
		
		EventInsertResult insert = doEventInsert(con, input.event, null, null, rawICalendar, processOpts, reminderOpts, validTags);
		
		if (insert.orecurrence != null) {
			if (insert.oevent.getPublicUid() != null) {
				// Cache for future use...
				eventIdByPublicIdMap.put(insert.oevent.getPublicUid(), insert.oevent.getEventId());
			} else {
				//TODO: warn no publicid
			}
		} else {
			if (input.recurringRefs != null && eventIdByPublicIdMap.containsKey(input.recurringRefs.exRefersToMasterUid)) {
				EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
				String eventId = eventIdByPublicIdMap.get(input.recurringRefs.exRefersToMasterUid);
				recDao.insertRecurrenceEx(con, eventId, input.recurringRefs.exRefersToDate);
			}
		}
		
		return insert;
	}
	
	@Deprecated
	private boolean doEventInputUpdateLegacy(Connection con, String eventId, EventInput input, BitFlags<EventProcessOpt> processOpts, BitFlags<EventReminderOption> reminderOpts, Set<String> validTags, Map<String, String> eventIdByPublicIdMap) throws DAOException, IOException {
		doEventDelete(con, eventId, false);
		doEventInputInsert(con, input, processOpts, reminderOpts, validTags, eventIdByPublicIdMap);
		return true;
	}
	
	private boolean doEventInputUpdate(Connection con, String eventId, List<EventInput> inputs, BitFlags<EventProcessOpt> processOpts, Set<String> validTags) throws IOException, WTException {
		// Google may not place the master in first element, so:
		// pick the master = the (single) VEVENT without a RECURRENCE-ID
		int masterIdx = -1;
		for (int i = 0; i < inputs.size(); i++) {
			EventInput ei = inputs.get(i);
			boolean isOverride = ei.recurringRefs != null && ei.recurringRefs.exRefersToMasterUid != null;
			if (!isOverride) { masterIdx = i; break; }
		}
		if (masterIdx < 0) {
			// no master in payload — Google sent only override instances
			// -> fetch/locate existing master by UID, or skip/handle accordingly
			// ...
			return false;
		}
		EventInput input1 = inputs.remove(masterIdx);		
		
		if (StringUtils.isEmpty(input1.event.getPublicUid())) throw new WTException("Public ID not set");
		if (input1.recurringRefs != null && input1.recurringRefs.exRefersToMasterUid != null) throw new WTException("First VEVENT should not have a RECURRENCE-ID set");
		
		Map<String, String> eventIdByPublicIdMap = new HashMap<>();
		eventIdByPublicIdMap.put(eventId, input1.event.getPublicUid());
		
		// Prepares items for generating exceptions dates and objects
		ArrayList<EventInput> exInputs = new ArrayList<>();
		LinkedHashSet<LocalDate> exDates = new LinkedHashSet<>();
		for (EventInput ei : inputs) {
			if (ei.recurringRefs == null) continue;
			if (!StringUtils.equals(ei.recurringRefs.exRefersToMasterUid, input1.event.getPublicUid())) continue;
			if (exDates.contains(ei.recurringRefs.exRefersToDate)) continue;

			exDates.add(ei.recurringRefs.exRefersToDate);
			//if (!ti.isSourceEventCancelled()) tiExs.add(ti);
			exInputs.add(ei);
		}
		
		// Adds new collected dates exceptions and update the reference event
		if (input1.event.hasRecurrence() && !exDates.isEmpty()) {
			input1.event.getRecurrence().addExcludedDates(exDates);
		}
		
		String rawICalendar = null;
		if (input1.extraProps != null) {
			// Creates a rawICalendar using only not-used properties
			rawICalendar = ICalendarUtils.printProperties(input1.extraProps, "VEVENT");
		}
		
		EventUpdateResult update = doEventUpdate(con, eventId, input1.event, rawICalendar, processOpts, validTags);
		if (update == null) return false;
		
		// Inserts collected object exceptions
		for (EventInput eiEx : exInputs) {
			eiEx.event.setCalendarId(update.oevent.getCalendarId());
			eiEx.event.setPublicUid(null); // Reset value in order to make inner function generate new one!
			EventInsertResult result = doEventInputInsert(con, eiEx, BitFlags.noneOf(EventProcessOpt.class), BitFlags.with(EventReminderOption.IGNORE), validTags, eventIdByPublicIdMap);
		}
		return true;
	}
	
	/*
	private EventInsertResult doEventInputInsert_OLD(Connection con, HashMap<String, OEvent> cache, EventInput ei, ProcessReminder processReminder) throws DAOException, IOException {
		EventInsertResult insert = doEventInsert(con, ei.event, null, true, true, true, true, true, true, processReminder, null);
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
	*/
	
	/*
	private boolean doEventInputUpdate_OLD(Connection con, HashMap<String, OEvent> cache, EventInput input, ProcessReminder processReminder) throws DAOException, IOException {
		//TODO: Make this smart avoiding delete/insert!
		doEventDelete(con, input.event.getEventId(), false);
		doEventInputInsert(con, cache, input, processReminder);
		return true;
	}
	*/
	
	private InstanceInfo doEventGetInstanceInfo(Connection con, EventInstanceId instanceId) {
		EventDAO evtDao = EventDAO.getInstance();
		OEventInstanceInfo oeii = evtDao.selectInstanceInfo(con, instanceId);
		return (oeii == null) ? null : new InstanceInfo(instanceId, oeii);
	}
	
	private String doEventGetInstanceEventId(Connection con, EventInstanceId instanceId) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		return evtDao.selectOnlineIdBySeriesInstance(con, instanceId.getEventId(), instanceId.getInstance());
	}
	
	private String doEventLookupId(Connection con, GetEventScope scope, String publicUid) throws WTException {
		ArrayList<String> ids = new ArrayList<>();
		if (scope.equals(GetEventScope.PERSONAL) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			String eventId = doEventLookupId(con, publicUid, listMyCalendarIds());
			if (eventId != null) ids.add(eventId);
		}
		if (scope.equals(GetEventScope.INCOMING) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			String eventId = doEventLookupId(con, publicUid, listIncomingCalendarIds());
			if (eventId != null) ids.add(eventId);
		}
		// Filled array could contains more than one result, eg. in case of 
		// invitation between two users of the same domain where the target
		// calendar of one user is shared to the other.
		// Returning the first result is the most appropriated action because
		// personal elements are returned first.
		//if (ids.size() > 1) throw new WTException("Multiple events found for public id [{}]", publicUid);
		return ids.isEmpty() ? null : ids.get(0);
	}
	
	private List<String> doEventAttendeeUpdateResponseByProfile(final Connection con, final String eventId, final UserProfileId profileId, final EventAttendee.ResponseStatus responseStatus, final boolean setTimestamp) throws WTException {
		InternetAddress ia = WT.getProfilePersonalAddress(profileId);
		if (ia == null) throw new WTException("Profile personalAddress is empty [{}]", profileId);
		return doEventAttendeeUpdateResponseByRecipient(con, eventId, ia.getAddress(), responseStatus, setTimestamp);
	}
	
	private List<String> doEventAttendeeUpdateResponseByRecipient(final Connection con, final String eventId, final String recipientAddress, final EventAttendee.ResponseStatus responseStatus, final boolean setTimestamp) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		
		// Find matching attendees...
		ArrayList<String> matchingIds = new ArrayList<>();
		List<OEventAttendee> atts = attDao.selectByEvent(con, eventId);
		for (OEventAttendee att : atts) {
			final InternetAddress ia = InternetAddressUtils.toInternetAddress(att.getRecipient());
			if (ia == null) continue;
			if (StringUtils.equalsIgnoreCase(ia.getAddress(), recipientAddress)) matchingIds.add(att.getAttendeeId());
		}
		
		// Update responses
		DateTime responseTimestamp = setTimestamp ? BaseDAO.createRevisionTimestamp() : null;
		int ret = attDao.updateAttendeeResponseByIdsEvent(con, EnumUtils.toSerializedName(responseStatus), responseTimestamp, matchingIds, eventId);
		if (ret > 0) evtDao.updateRevision(con, eventId, BaseDAO.createRevisionTimestamp());
		if (matchingIds.size() == ret) {
			return matchingIds;
		} else {
			throw new WTException("Updated attendees count ({}) does NOT match the target count ({})", ret, matchingIds.size());
		}
	}
	
	private int doEventAttendeeUpdateResponse(final Connection con, final String eventId, final String attendeeId, final EventAttendee.ResponseStatus responseStatus, final boolean setTimestamp) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		
		DateTime responseTimestamp = setTimestamp ? BaseDAO.createRevisionTimestamp() : null;	
		int ret = attDao.updateAttendeeResponseByIdsEvent(con, EnumUtils.toSerializedName(responseStatus), responseTimestamp, Arrays.asList(attendeeId), eventId);
		if (ret == 1) evtDao.updateRevision(con, eventId, BaseDAO.createRevisionTimestamp());
		return ret;
	}
	
	/*
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
		DateTime responseTimestamp = BaseDAO.createRevisionTimestamp();
		int ret = attDao.updateAttendeeResponseByIds(con, EnumUtils.toSerializedName(responseStatus), responseTimestamp, matchingIds);
		if (ret > 0) evtDao.updateRevision(con, event.getEventId(), BaseDAO.createRevisionTimestamp());
		if (matchingIds.size() == ret) {
			return matchingIds;
		} else {
			throw new WTException("# of attendees to update don't match the uptated ones");
		}
	}
	*/
	
	private String doEventLookupId(Connection con, String publicUid, Collection<Integer> calendarIdMustBeIn) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		List<String> ids = null;
		if ((calendarIdMustBeIn == null) || calendarIdMustBeIn.isEmpty()) {
			logger.debug("Try guessing event by public ID... [{}]", publicUid);
			ids = evtDao.selectOnlineIdsByPublicUid(con, publicUid);
			
		} else {
			logger.debug("Try guessing event by public ID and calendar set... [{}, [{}]]", publicUid);
			ids = evtDao.selectOnlineIdsByCalendarsPublicUid(con, calendarIdMustBeIn, publicUid);
		}
		logger.debug("Found {} results for '{}'", ids.size(), publicUid);
		
		// In case of invitations we can have multiple ids belonging to the same 
		// publicId. So here we can safely get the first one due to retured ids
		// are sorted by insertion-timestamp and by id sequence; the first is the
		// originating appointment.
		return ids.isEmpty() ? null : ids.get(0);
	}
	
	// Rename to doEventLookup
	private Event doEventGet(Connection con, GetEventScope scope, String publicUid) throws WTException {
		BitFlags<EventProcessOpt> processOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.CUSTOM_VALUES);
		return doEventLookup(con, scope, publicUid, processOpts);
	}
	
	private Event doEventLookup(Connection con, GetEventScope scope, String publicUid, BitFlags<EventProcessOpt> processOptions) throws WTException {
		String eventId = doEventLookupId(con, scope, publicUid);
		return (eventId == null) ? null : doEventGet(con, eventId, processOptions);
	}
	
	private Event doEventGet(final Connection con, final String eventId, final BitFlags<EventProcessOpt> processOptions) throws DAOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		OEvent oevt = evtDao.selectOnlineEventById(con, eventId);
		if (oevt == null) return null;
		Event event = ManagerUtils.fillEvent(new Event(), oevt);
		
		if (processOptions.has(EventProcessOpt.RECUR)) {
			EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
			OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, eventId);
			if (orec != null) {
				EventRecurrence rec = new EventRecurrence(orec.getRule(), orec.getStart());
				if (processOptions.has(EventProcessOpt.RECUR_EX)) {
					rec.setExcludedDates(recDao.selectRecurrenceExByEvent(con, eventId));
				}
				event.setRecurrence(rec);
			}
		}
		if (processOptions.has(EventProcessOpt.ATTENDEES)) {
			EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
			List<OEventAttendee> oattendee = atteDao.selectByEvent(con, eventId);
			event.setAttendees(ManagerUtils.createEventAttendeeList(oattendee));
		}
		if (processOptions.has(EventProcessOpt.ATTACHMENTS)) {
			EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
			List<OEventAttachment> oatts = attchDao.selectByEvent(con, eventId);
			event.setAttachments(ManagerUtils.createEventAttachmentList(oatts));
		}
		if (processOptions.has(EventProcessOpt.TAGS)) {
			EventTagDAO tagDao = EventTagDAO.getInstance();
			event.setTags(tagDao.selectTagsByEvent(con, eventId));
		}
		if (processOptions.has(EventProcessOpt.CUSTOM_VALUES)) {
			EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
			List<OEventCustomValue> ovals = cvalDao.selectByEvent(con, eventId);
			event.setCustomValues(ManagerUtils.createCustomValuesMap(ovals));
		}
		return event;
	}
	
	private EventInstance doEventInstanceGet(Connection con, EventInstanceId instanceId, BitFlags<EventProcessOpt> processOpts) throws DAOException, WTException {
		Event event = null;
		String originalEventId = doEventGetInstanceEventId(con, instanceId);
		if (originalEventId != null) {
			event = doEventGet(con, originalEventId, processOpts);
		} else {
			event = doEventGet(con, instanceId.getEventId(), processOpts);
		}
		if (event == null) return null;
		
		if (EventInstanceId.isSeriesItem(instanceId, event.getEventId())) {
			event.recalculateStartEndForInstance(instanceId.getInstanceAsDate());
		}
		return new EventInstance(instanceId, event);
	}
	
	private EventInstance doEventInstanceGet(Connection con, InstanceInfo info, BitFlags<EventProcessOpt> processOpts) throws DAOException, WTException {
		Event event = doEventGet(con, info.originalEventId(), processOpts);
		if (event == null) return null;
		
		return new EventInstance(EventInstanceId.build(event.getEventId(), event.getSeriesEventId(), event.getSeriesInstanceId()), info.originalEventId(), event);
	}
	
	private EventInsertResult doEventInsert(Connection con, EventEx event, String seriesEventId, String seriesInstance, String rawICalendar, BitFlags<EventProcessOpt> processOpts, BitFlags<EventReminderOption> reminderOpts, Set<String> validTags) throws DAOException, IOException {
		EventDAO evtDao = EventDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		final String newEventId = IdentifierUtils.getUUIDTimeBased(true);
		OEvent oevent = ManagerUtils.fillOEvent(new OEvent(), event);
		oevent.setEventId(newEventId);
		oevent.setSeriesEventId(seriesEventId);
		oevent.setSeriesInstanceId(seriesInstance);
		ManagerUtils.fillOEventWithDefaultsForInsert(oevent, getTargetProfileId(), revisionTimestamp);
		oevent.ensureCoherence();
		
		if (oevent.getReminder() != null) {
			if (reminderOpts.has(EventReminderOption.IGNORE)) {
				oevent.setReminder(null);
			} else if (reminderOpts.has(EventReminderOption.DISARM_PAST) && oevent.getStart().isBeforeNow()) {
				oevent.setRemindedAt(revisionTimestamp);
			}
		}
		
		boolean ret = evtDao.insertEvent(con, oevent, revisionTimestamp) == 1;
		if (!ret) return null;
		
		/*
		if (!StringUtils.isBlank(rawICalendar)) {
			doEventICalendarInsert(con, oevt.getEventId(), rawICalendar);
		}
		*/
		if (processOpts.has(EventProcessOpt.RAW_ICAL) && !StringUtils.isBlank(rawICalendar)) {
			EventICalendarDAO icalDao = EventICalendarDAO.getInstance();
			icalDao.upsert(con, newEventId, rawICalendar);
		}
		
		OEventRecurrence orecurrence = null;
		if (processOpts.has(EventProcessOpt.RECUR) && event.hasRecurrence()) {
			EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
			Recur recur = event.getRecurrence().getRecurRule();
			
			orecurrence = new OEventRecurrence();
			orecurrence.setEventId(newEventId);
			orecurrence.set(recur, event.getRecurrence().getStart(), event.getStart(), event.getTimezoneObject());
			recDao.insertRecurrence(con, orecurrence);
			
			if (processOpts.has(EventProcessOpt.RECUR_EX) && (event.getRecurrence().getExcludedDates() != null)) {
				recDao.batchInsertRecurrenceEx(con, newEventId, event.getRecurrence().getExcludedDates());
			}
		}
		
		ArrayList<OEventAttendee> oattendees = null;
		if (processOpts.has(EventProcessOpt.ATTENDEES) && event.hasAttendees()) {
			EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			oattendees = new ArrayList<>();
			for (EventAttendee attendee : event.getAttendees()) {
				if (!ManagerUtils.validateForInsert(attendee)) continue;
				OEventAttendee oattendee = ManagerUtils.fillOEventAttendee(new OEventAttendee(), attendee);
				oattendee.setAttendeeId(IdentifierUtils.getUUIDTimeBased(true));
				oattendee.setEventId(newEventId);
				attDao.insert(con, oattendee);
				oattendees.add(oattendee);
			}
		}
		
		Set<String> otags = null;
		if (processOpts.has(EventProcessOpt.TAGS) && event.hasTags()) {
			EventTagDAO tagDao = EventTagDAO.getInstance();
			tagDao.batchInsert(con, newEventId, sanitizeTagsCollection(event.getTags(), validTags));
		}
		
		ArrayList<OEventAttachment> oattachments = null;
		if (processOpts.has(EventProcessOpt.ATTACHMENTS) && event.hasAttachments()) {
			oattachments = new ArrayList<>();
			for (EventAttachment att : event.getAttachments()) {
				if (att instanceof EventAttachmentWithInput) {
					oattachments.add(doEventAttachmentInsert(con, newEventId, (EventAttachmentWithInput)att));
				} else {
					throw new IOException("Attachment object not supported [" + att.getAttachmentId() + "]");
				}
			}
		}
		
		ArrayList<OEventCustomValue> ocvalues = null;
		if (processOpts.has(EventProcessOpt.CUSTOM_VALUES) && event.hasCustomValues()) {
			EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
			ocvalues = new ArrayList<>(event.getCustomValues().size());
			for (CustomFieldValue cfv : event.getCustomValues().values()) {
				OEventCustomValue ocv = ManagerUtils.fillOEventCustomValue(new OEventCustomValue(), cfv);
				ocv.setEventId(newEventId);
				ocvalues.add(ocv);
			}
			cvalDao.batchInsert(con, ocvalues);
		}
		
		return new EventInsertResult(oevent, orecurrence, oattendees, otags, oattachments, ocvalues);
	}
	
	private Set<String> sanitizeTagsCollection(Set<String> inputSet, Set<String> validSet) {
		if (inputSet == null || validSet == null) return inputSet;
		return inputSet.stream()
			.filter(tagId -> validSet.contains(tagId))
			.collect(Collectors.toSet());
	}
	
	//private EventUpdateResult doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean processRecurrence, boolean processExcludedDates, boolean processAttendees, boolean processAttachments, boolean processTags, boolean processCustomValues, Set<String> validTags) throws IOException, WTException {
	private EventUpdateResult doEventUpdate(Connection con, String eventId, EventEx event, String rawICalendar, BitFlags<EventProcessOpt> processOpts, Set<String> validTags) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		Check.notNull(event.getTimezone(), "event.getTimezone() must not be null!");
		OEvent oevent = ManagerUtils.fillOEvent(new OEvent(), event);
		oevent.setEventId(eventId);
		ManagerUtils.fillOEventWithDefaultsForUpdate(oevent, revisionTimestamp);
		
		boolean clearRemindedOn = (event.getStart() != null) ? event.getStart().isAfterNow() : false;
		boolean ret = evtDao.updateEvent(con, oevent, revisionTimestamp, clearRemindedOn) == 1;
		if (!ret) return null;
		
		if (processOpts.has(EventProcessOpt.RAW_ICAL)) {
			EventICalendarDAO icalDao = EventICalendarDAO.getInstance();
			icalDao.upsert(con, eventId, rawICalendar);
		}
		
		if (processOpts.has(EventProcessOpt.RECUR)) {
			EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
			OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, eventId);
			if ((orec != null) && event.hasRecurrence()) { // New event has recurrence and the old too
				Recur recur = event.getRecurrence().getRecurRule();
				boolean recurChanged = !ICal4jUtils.equals(recur, orec.getRecurrenceObject());
				
				orec.set(recur, event.getRecurrence().getStart(), event.getStart(), event.getTimezoneObject());
				recDao.updateRecurrence(con, orec);
				
				if (processOpts.has(EventProcessOpt.RECUR_EX)) {
					// If rule is changed, cleanup stored exceptions (we lose any broken events restore information)
					if (recurChanged) recDao.deleteRecurrenceExByEvent(con, eventId);
					// Inserts broken records that exclude some dates
					Set<LocalDate> newDates = (event.getRecurrence().getExcludedDates() != null) ? event.getRecurrence().getExcludedDates() : new LinkedHashSet<>();
					Set<LocalDate> oldDates = recDao.selectRecurrenceExByEvent(con, eventId);
					ChangeSet<LocalDate> changeSet = LangUtils.computeChangeSet(oldDates, newDates);
					recDao.batchInsertRecurrenceEx(con, eventId, changeSet.getAdded());
					recDao.deleteRecurrenceExByEventDates(con, eventId, changeSet.getRemoved());
				}
				
			} else if ((orec == null) &&  event.hasRecurrence()) { // New event has recurrence but the old doesn't
				Recur recur = event.getRecurrence().getRecurRule();
				
				// Inserts recurrence
				orec = new OEventRecurrence();
				orec.setEventId(eventId);
				orec.set(recur, event.getRecurrence().getStart(), event.getStart(), event.getTimezoneObject());
				recDao.insertRecurrence(con, orec);
				
				// Inserts broken records that exclude some dates
				if (processOpts.has(EventProcessOpt.RECUR_EX) && (event.getRecurrence().getExcludedDates() != null)) {
					recDao.batchInsertRecurrenceEx(con, eventId, event.getRecurrence().getExcludedDates());
				}
				
			} else if ((orec != null) && !event.hasRecurrence()) { // New event doesn't have recurrence but the old does
				recDao.deleteRecurrenceByEvent(con, eventId);
				recDao.deleteRecurrenceExByEvent(con, eventId);
			}
		}
		
		ChangeSet<EventAttendee> attendeeChanges = null;
		if (processOpts.has(EventProcessOpt.ATTENDEES)) {
			final boolean ignoreResponse = processOpts.has(EventProcessOpt.IGNORE_ATTENDEE_RESPONSE);
			final boolean ignoreNotify = processOpts.has(EventProcessOpt.IGNORE_ATTENDEE_NOTIFY);
			EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
			List<EventAttendee> oldAttendees = ManagerUtils.createEventAttendeeList(atteDao.selectByEvent(con, eventId));
			ChangeSet<EventAttendee> changeSet = LangUtils.computeChangeSet(oldAttendees, event.getAttendeesOrEmpty());
			
			if (changeSet.hasAdded()) {
				for (EventAttendee attendee : changeSet.getAdded()) {
					ManagerUtils.fillWithDefaults(attendee);
					if (!ManagerUtils.validateForInsert(attendee)) continue;
					final OEventAttendee oattendee = ManagerUtils.fillOEventAttendee(new OEventAttendee(), attendee);
					oattendee.setAttendeeId(IdentifierUtils.getUUIDTimeBased(true));
					oattendee.setEventId(eventId);
					atteDao.insert(con, oattendee);
				}
			}
			if (changeSet.hasUpdated()) {
				for (EventAttendee attendee : changeSet.getUpdated()) {
					ManagerUtils.fillWithDefaults(attendee);
					if (!ManagerUtils.validateForUpdate(attendee, ignoreResponse, ignoreNotify)) continue;
					final OEventAttendee oattendee = ManagerUtils.fillOEventAttendee(new OEventAttendee(), attendee);
					atteDao.update(con, oattendee, ignoreResponse, ignoreNotify);
				}
			}
			if (changeSet.hasRemoved()) atteDao.deleteByIdsEvent(con, changeSet.getRemoved().stream().map(att -> att.getAttendeeId()).collect(Collectors.toList()), eventId);
			attendeeChanges = new ChangeSet<>(changeSet.getAdded(), null, changeSet.getRemoved());
		}
		
		if (processOpts.has(EventProcessOpt.TAGS)) {
			EventTagDAO tagDao = EventTagDAO.getInstance();
			Set<String> oldTags = sanitizeTagsCollection(tagDao.selectTagsByEvent(con, eventId), validTags);
			ChangeSet<String> changeSet = LangUtils.computeChangeSet(oldTags, event.getTagsOrEmpty());
			if (changeSet.hasAdded()) tagDao.batchInsert(con, eventId, changeSet.getAdded());
			if (changeSet.hasRemoved()) tagDao.deleteByIdsEvent(con, eventId, changeSet.getRemoved());
		}
		
		if (processOpts.has(EventProcessOpt.ATTACHMENTS)) {
			EventAttachmentDAO attcDao = EventAttachmentDAO.getInstance();
			List<EventAttachment> oldAttchs = ManagerUtils.createEventAttachmentList(attcDao.selectByEvent(con, eventId));
			ChangeSet<EventAttachment> changeSet = LangUtils.computeChangeSet(oldAttchs, event.getAttachmentsOrEmpty());
			
			if (changeSet.hasAdded()) {
				for (EventAttachment att : changeSet.getAdded()) {
					if (att instanceof EventAttachmentWithInput) {
						doEventAttachmentInsert(con, eventId, (EventAttachmentWithInput)att);
					} else {
						throw new IOException("Attachment object not supported [" + att.getAttachmentId() + "]");
					}
				}
			}
			if (changeSet.hasUpdated()) {
				for (EventAttachment att : changeSet.getUpdated()) {
					if (!(att instanceof EventAttachmentWithInputStream)) continue;
					doEventAttachmentUpdate(con, (EventAttachmentWithInputStream)att);
				}
			}
			if (changeSet.hasRemoved()) attcDao.deleteByIdsEvent(con, changeSet.getRemoved().stream().map(att -> att.getAttachmentId()).collect(Collectors.toList()), eventId);
		}
		
		if (processOpts.has(EventProcessOpt.CUSTOM_VALUES) && event.hasCustomValues()) {
			EventCustomValueDAO cvalDao = EventCustomValueDAO.getInstance();
			ArrayList<String> customFieldIds = new ArrayList<>();
			ArrayList<OEventCustomValue> ocvals = new ArrayList<>(event.getCustomValues().size());
			for (CustomFieldValue cfv : event.getCustomValues().values()) {
				OEventCustomValue ocv = ManagerUtils.fillOEventCustomValue(new OEventCustomValue(), cfv);
				ocv.setEventId(eventId);
				ocvals.add(ocv);
				customFieldIds.add(ocv.getCustomFieldId());
			}
			//TODO: use upsert when available
			cvalDao.deleteByEventFields(con, eventId, customFieldIds);
			cvalDao.batchInsert(con, ocvals);
		}
		
		return new EventUpdateResult(oevent, attendeeChanges);
	}
	
	private int doEventDelete(Connection con, String eventId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteById(con, eventId, BaseDAO.createRevisionTimestamp());
			
		} else {
			return evtDao.deleteById(con, eventId);
		}
	}
	
	private int doEventDeleteByCalendar(Connection con, int calendarId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		if (logicDelete) {
			return evtDao.logicDeleteByCalendar(con, calendarId, BaseDAO.createRevisionTimestamp());
			
		} else {
			HistoryDAO hisDao = HistoryDAO.getInstance();
			hisDao.deleteEventsHistoryByCalendar(con, calendarId);
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
	
	//processRecurrence:true, processExcludedDates:false, processAttendees:true, boolean processAttachments:false, boolean processTags:true, boolean processCustomValues:true, processReminder:YES
	private EventInsertResult doEventCopy(Connection con, String sourceEventId, EventEx event, int targetCalendarId, BitFlags<EventProcessOpt> processOptions, Set<String> validTags) throws DAOException, IOException {
		BitFlags<EventProcessOpt> insertOpts = processOptions.copy().set(EventProcessOpt.RAW_ICAL);
		
		event.setCalendarId(targetCalendarId);
		event.setPublicUid(null); // Reset value in order to make inner function generate new one!
		event.setRevisionTimestamp(null); // Reset value in order to make inner function generate new one!
		event.setRevisionSequence(null); // Reset value in order to make inner function generate new one!
		event.setCreationTimestamp(null); // Reset value in order to make inner function generate new one!
		event.setOrganizer(null); // Reset value in order to make inner function generate new one!
		event.setOrganizerId(null); // Reset value in order to make inner function generate new one!
		event.setHref(null); // Reset value in order to make inner function generate new one!
		event.setEtag(null); // Reset value in order to make inner function generate new one!
		
		String rawICalendar = null;
		if (insertOpts.has(EventProcessOpt.RAW_ICAL)) {
			EventICalendarDAO icalDao = EventICalendarDAO.getInstance();
			rawICalendar = icalDao.selectRawDataById(con, sourceEventId);
			//TODO: does rawICal contains ex? What would happen if RECUR_EX is off and raw contains ex dates?
		}
		if (insertOpts.has(EventProcessOpt.ATTACHMENTS) && event.hasAttachments()) {
			event.setAttachments(EventAttachment.asListOfEventAttachmentsWithInputRef(event.getAttachmentsOrEmpty()));
		}
		
		return doEventInsert(con, event, null, null, rawICalendar, insertOpts, BitFlags.noneOf(EventReminderOption.class), validTags);
	}
	
	private boolean doEventMove(Connection con, String eventId, int targetCalendarId) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		return evtDao.updateCalendar(con, eventId, targetCalendarId, BaseDAO.createRevisionTimestamp()) == 1;
	}
	
	private Set<LocalDate> doRecurrenceGetExcludedDates(Connection con, String eventId) {
		EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
		return recDao.selectRecurrenceExByEvent(con, eventId);
	}
	
	private int doRecurrenceAddExcludedDate(Connection con, String eventId, LocalDate exDate) {
		EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
		return recDao.insertRecurrenceEx(con, eventId, exDate);
	}
	
	private OEventAttachment doEventAttachmentInsert(Connection con, String eventId, EventAttachmentWithInput attachment) throws DAOException, IOException {
		Check.notNull(attachment, "attachment");
		EventAttachmentDAO attDao = EventAttachmentDAO.getInstance();
		
		OEventAttachment oatt = ManagerUtils.fillOEventAttachment(new OEventAttachment(), attachment);
		oatt.setEventAttachmentId(IdentifierUtils.getUUIDTimeBased(true));
		oatt.setEventId(eventId);
		attDao.insert(con, oatt, BaseDAO.createRevisionTimestamp());
		
		if (attachment instanceof EventAttachmentWithInputStream) {
			InputStream is = ((EventAttachmentWithInputStream)attachment).getStream();
			try {
				attDao.insertBytes(con, oatt.getEventAttachmentId(), IOUtils.toByteArray(is));
			} finally {
				IOUtils.closeQuietly(is);
			}
			
		} else if (attachment instanceof EventAttachmentWithInputRef) {
			final String sourceAttachmentId = ((EventAttachmentWithInputRef)attachment).getAttachmentIdToClone();
			attDao.insertBytesFromClone(con, oatt.getEventAttachmentId(), sourceAttachmentId);
			
		} else {
			throw new IOException("Attachment data not provided");
		}
		
		return oatt;
	}
	
	private boolean doEventAttachmentUpdate(Connection con, EventAttachmentWithInputStream attachment) throws DAOException, IOException {
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEventAttachment oattch = ManagerUtils.createOEventAttachment(attachment);
		attchDao.update(con, oattch, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attchDao.deleteBytesById(con, oattch.getEventAttachmentId());
			return attchDao.insertBytes(con, oattch.getEventAttachmentId(), IOUtils.toByteArray(is)) == 1;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private ArrayList<ComparableEventRange> doListEventsBounds(final Connection con, final Collection<Integer> calendarIds, final DateTime spanFrom, final DateTime spanTo, final DateTimeZone targetTimezone) throws WTException {
		Check.notNull(calendarIds, "calendarIds");
		Check.notNull(spanFrom, "spanFrom");
		Check.notNull(spanTo, "spanTo");
		EventDAO evtDao = EventDAO.getInstance();
		
		int noOfRecurringInst = Days.daysBetween(spanFrom, spanTo).getDays() + 2;
		ArrayList<ComparableEventRange> bounds = new ArrayList<>();
		for (VEventBounds vetf : evtDao.viewOnlineBoundsByCalendarRangeCondition(con, calendarIds, spanFrom, spanTo, EventDAO.createTransparencyCondition(EventBase.Transparency.OPAQUE))) {
			if (!vetf.hasRecurrence()) {
				final EventInstanceId id = EventInstanceId.build(vetf.getEventId(), vetf.getSeriesEventId(), vetf.getSeriesInstanceId());
				final DateTime start = vetf.getStart().withZone(targetTimezone);
				final DateTime end = vetf.getEnd().withZone(targetTimezone);
				bounds.add(new ComparableEventRange(id, start, end));
				
			} else {
				bounds.addAll(calculateRecurringInstances(new VEB2CEB_RRContext(con, vetf), spanFrom, spanTo, noOfRecurringInst));
			}
		}
		return bounds;
	}
	
	private LocalDate calculateRecurrenceStart(EventBounds event, Recur recurrenceObject, DateTime recurenceStart, Set<LocalDate> recurrenceExDates) {
		List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recurrenceObject, recurenceStart, true, recurrenceExDates, event.getStart(), event.getEnd(), event.getTimezoneObject(), null, null, 1);
		return (dates != null && !dates.isEmpty()) ? dates.get(0) : null;
	}
	
	private boolean isThisEventInstanceTheFirstInstance(final Connection con, final InstanceInfo info) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		EventBoundsSeries masterBoundary = evtDao.selectBoundsSeries(con, info.masterEventId);
		if (masterBoundary == null) throw new WTException("Unable to get master event [{}]", info.masterEventId);
		Set<LocalDate> exDates = (masterBoundary.recurrenceHasExDates()) ? doRecurrenceGetExcludedDates(con, info.masterEventId) : null;
		LocalDate firstInstanceDate = calculateRecurrenceStart(masterBoundary, masterBoundary.getRecurrenceObject(), masterBoundary.getRecurrenceStart(), exDates);
		return firstInstanceDate != null ? info.seriesInstanceDate.equals(firstInstanceDate) : false;
	}
	
	private EventInsertResult doEventInstanceUpdateAndCommit(final Connection con, UpdateEventTarget target, final InstanceInfo info, final EventEx event, final BitFlags<EventProcessOpt> processOpts, final BitFlags<EventNotifyOption> notifyOptions, final Set<String> validTags) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
		EventInsertResult eventInsert = null;
		boolean isApplyTags = processOpts.hasOnly(EventProcessOpt.TAGS);
		int ret;
		
		EventInstance eventDump = null;
		BitFlags<EventProcessOpt> getOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES);
		EventUpdateResult eventUpdate = null;
		event.setTimezone(info.eventTimezone);
		if (info.belongsToSeries && info.isSeriesException) { // -> BROKEN INSTANCE
			BitFlags<EventProcessOpt> updateOpts = processOpts.copy()
				.unset(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.RAW_ICAL);
			
			// 1 - Updates the broken item with new data
			eventUpdate = doEventUpdate(con, info.eventId, event, null, updateOpts, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				if (!isApplyTags) auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.eventId, null);
			}
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			eventDump = doEventInstanceGet(con, info, getOpts);
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			// If target is SINCE and the referenced instance is clearly 
			// the first of the recurrence, we can actually treat the 
			// modification as an update to the whole series.
			if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
				boolean thisIsTheFirstInstance = isThisEventInstanceTheFirstInstance(con, info);
				if (thisIsTheFirstInstance) target = UpdateEventTarget.WHOLE_SERIES;
			}
			
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts new broken item (RR is not supported here)
				BitFlags<EventProcessOpt> insertOpts = processOpts.copy()
					.unset(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.RAW_ICAL);
				EventInsertResult insert = doEventInsert(con, event, info.masterEventId, info.seriesInstance, null, insertOpts, BitFlags.noneOf(EventReminderOption.class), validTags);
				
				// 2 - Inserts an exception on modified date
				recDao.insertRecurrenceEx(con, info.masterEventId, info.seriesInstanceDate);

				// 3 - Updates revision of master event
				evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
					auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, insert.oevent.getEventId(), null);
				}
				eventInsert = insert;
				
				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
				
				eventDump = doEventInstanceGet(con, info, getOpts);

			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				EventBounds masterBoundary = evtDao.selectBounds(con, info.masterEventId);
				if (masterBoundary == null) throw new WTException("Unable to get master event [{}]", info.masterEventId);
				
				// 1 - Resize original recurrence (sets until date at the day before date)
				OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, info.masterEventId);
				if (orec == null) throw new WTException("Unable to get master recurrence [{}]", info.masterEventId);
				
				final DateTime origRecurStart = orec.getStart(); // Dump orig start!
				final Recur origRecur = orec.getRecurrenceObject(); // Dump orig recur!
				final Set<LocalDate> origExDates = recDao.selectRecurrenceExByEvent(con, info.masterEventId); // Dump orig ex dates!
				
				EventBounds itemBoundary = event.getEventBounds();
				LocalTime origNewUntilTime = masterBoundary.isAllDay() ? JodaTimeUtils.TIME_AT_STARTOFDAY : masterBoundary.getStart().withZone(masterBoundary.getTimezoneObject()).toLocalTime();
				orec.resize(info.seriesInstanceDate.minusDays(1), origNewUntilTime, masterBoundary.getTimezoneObject());
				ret = recDao.updateRecurrence(con, orec);
				if (ret != 1) throw new WTException("Unable to update master recurrence [{}]", info.masterEventId);

				// 2 - Updates revision of original event
				ret = evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());
				if (ret != 1) throw new WTException("Unable to update master event [{}]", info.masterEventId);
				
				// 3 - Insert new event trimming recurrence to adjust size
				// 3 - Insert new event recalculating start/end from rec. start preserving days duration
				//int itemDaysBetween = JodaTimeUtils.calendarDaysBetween(itemBoundary.getStart(), itemBoundary.getEnd());
				//event.setStart(itemBoundary.getStart().withDate(info.seriesInstanceDate));
				//event.setEnd(itemBoundary.getEnd().withDate(info.seriesInstanceDate.plusDays(itemDaysBetween)));
				
				String newRecur = origRecur.toString();
				DateTime newRecStart = itemBoundary.getStart();
				Set<LocalDate> newExDates = EventRecurrence.filterExDates(origExDates, newRecStart.toLocalDate());
				
				if (ICal4jUtils.recurHasCount(origRecur)) { // When we have a Count configured...
					boolean transformToUntil = true;
					
					// If count is limited, try to keep count configuration recalculating 
					// its value based on remaining instances starting from the break.
					if (origRecur.getCount() < 365) {
						List<LocalDate> origDates = ICal4jUtils.calculateRecurrenceSet(origRecur, masterBoundary.getStart(), itemBoundary.isAllDay(), origExDates, masterBoundary.getStart(), masterBoundary.getEnd(), itemBoundary.getTimezoneObject(), null, null, -1);
						int iof = origDates.indexOf(info.seriesInstanceDate);
						if (iof != -1) {
							int remainingCount = origDates.size() - iof;
							newRecur = ICal4jUtils.setRecurCount(ICal4jUtils.cloneRecur(origRecur), remainingCount).toString();
							transformToUntil = false;
						}
					}
					
					// Otherwise fallback on UntilDate
					if (transformToUntil) {
						final LocalTime recurUntilTime = EventRecurrence.getRecurUntilTime(itemBoundary);
						final int daysDelta = JodaTimeUtils.calendarDaysDelta(info.seriesInstanceDate.toDateTime(recurUntilTime, itemBoundary.getTimezoneObject()), itemBoundary.getStart());
						
						DateTime origUntil = ICal4jUtils.calculateRecurrenceEnd(origRecur, origRecurStart, masterBoundary.getStart(), masterBoundary.getEnd(), masterBoundary.getTimezoneObject());
						DateTime newUntil = origUntil.plusDays(daysDelta).withTime(recurUntilTime);
						newRecur = ICal4jUtils.setRecurUntilDate(ICal4jUtils.cloneRecur(origRecur), newUntil).toString();
					}	
					
				} else if (ICal4jUtils.recurHasUntilDate(origRecur)) { // When we have an UntilDate configured...
					final LocalTime recurUntilTime = EventRecurrence.getRecurUntilTime(itemBoundary);
					final int daysDelta = JodaTimeUtils.calendarDaysDelta(info.seriesInstanceDate.toDateTime(recurUntilTime, itemBoundary.getTimezoneObject()), itemBoundary.getStart());
					final LocalDate origRecurUntilDate = EventRecurrence.getRecurUntilDate(origRecur, itemBoundary.getTimezoneObject());
					
					DateTime newRecurUntil = EventRecurrence.toRecurUntilDate(origRecurUntilDate.plusDays(daysDelta), recurUntilTime, itemBoundary.getTimezoneObject());
					newRecur = ICal4jUtils.setRecurUntilDate(ICal4jUtils.cloneRecur(origRecur), newRecurUntil).toString();
				}
				event.setRecurrence(new EventRecurrence(newRecur, newRecStart, newExDates));
				
				BitFlags<EventProcessOpt> insertOpts = processOpts.copy()
					.set(EventProcessOpt.RECUR)
					.unset(EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES, EventProcessOpt.ATTACHMENTS, EventProcessOpt.RAW_ICAL);
				EventInsertResult insert = doEventInsert(con, event, null, null, null, insertOpts, BitFlags.noneOf(EventReminderOption.class), validTags);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
					auditLogWrite(AuditContext.EVENT, AuditAction.CREATE, insert.oevent.getEventId(), null);
				}
				
				// TODO: eventually add support to clone attendees in the newly inserted event and so sending invitation emails
				
				eventDump = doEventInstanceGet(con, info, getOpts);
				
			} else if (UpdateEventTarget.WHOLE_SERIES.equals(target)) { // Changes are valid for all the instances
				EventBounds masterBoundary = evtDao.selectBounds(con, info.masterEventId);
				if (masterBoundary == null) throw new WTException("Unable to get master event [{}]", info.masterEventId);
				
				// 1 - Updates master event with new data
				OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, info.masterEventId);
				if (orec == null) throw new WTException("Unable to get master recurrence [{}]", info.masterEventId);
				
				EventBounds itemBoundary = event.getEventBounds();
				
				// If we have an UntilDate, there may be a need to update it 
				// according to the day-shift possibly included in che change!
				EventRecurrence newRecurrence = null;
				if (ICal4jUtils.recurHasUntilDate(orec.getRecurrenceObject())) {
					final DateTime origRecurStart = orec.getStart(); // Dump orig start!
					final Recur origRecur = orec.getRecurrenceObject(); // Dump orig recur!
					
					final LocalTime recurUntilTime = EventRecurrence.getRecurUntilTime(itemBoundary);
					final DateTime newRecStart = itemBoundary.getStart();
					final int daysDelta = JodaTimeUtils.calendarDaysDelta(origRecurStart, newRecStart);
					if (daysDelta != 0) {
						final Set<LocalDate> origExDates = recDao.selectRecurrenceExByEvent(con, info.masterEventId); // Dump orig ex dates!
						final LocalDate origRecurUntilDate = EventRecurrence.getRecurUntilDate(origRecur, itemBoundary.getTimezoneObject());
						
						DateTime newRecurUntil = EventRecurrence.toRecurUntilDate(origRecurUntilDate.plusDays(daysDelta), recurUntilTime, itemBoundary.getTimezoneObject());
						String newRecur = ICal4jUtils.setRecurUntilDate(ICal4jUtils.cloneRecur(origRecur), newRecurUntil).toString();
						newRecurrence = new EventRecurrence(newRecur, newRecStart, EventRecurrence.traslateExDates(origExDates, daysDelta));
					}
				}
				
				BitFlags<EventProcessOpt> updateOpts = processOpts.copy()
					.unset(EventProcessOpt.RECUR_EX, EventProcessOpt.RAW_ICAL);
				if (newRecurrence != null) {
					event.setRecurrence(newRecurrence);
					updateOpts.set(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
				}
				eventUpdate = doEventUpdate(con, info.masterEventId, event, null, updateOpts, validTags);
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					if (!isApplyTags) auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
				}
				
				eventDump = doEventInstanceGet(con, info, getOpts);
			}
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			BitFlags<EventProcessOpt> updateOpts = processOpts.copy()
				.unset(EventProcessOpt.RECUR_EX, EventProcessOpt.RAW_ICAL);
			
			// 1 - Updates this item with new data
			eventUpdate = doEventUpdate(con, info.eventId, event, null, updateOpts, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				if (!isApplyTags) auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.eventId, null);
			}
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			eventDump = doEventInstanceGet(con, info, getOpts);
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(new EI2EI_RRContext(eventDump));
		}
		
		// Notify resource attendee
		if (eventUpdate != null && eventUpdate.attendeeChanges != null) {
			// Notify any replaced resource, this will force the cancellation of the previous booking
			List<RecipientTuple> resRcpts = getNotifiableRecipients(Crud.DELETE, getTargetProfileId(), event.getOrganizerAddress(), 
					eventUpdate.attendeeChanges.getRemoved().stream()
						.filter((att) -> {
							return EventAttendee.RecipientType.RESOURCE.equals(att.getRecipientType());
						})
						.collect(Collectors.toList())
				, false, true);
			if (!resRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), resRcpts, eventDump, Crud.DELETE);
		}
		onAfterEventOperation(Crud.UPDATE, eventDump, notifyOptions);
		
		return eventInsert;
	}
	
	private void doEventInstanceDeleteAndCommit(final Connection con, UpdateEventTarget target, final InstanceInfo info, final BitFlags<EventNotifyOption> notifyOptions, boolean notifyResourceOrganizer) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
		int ret;
		
		BitFlags<EventProcessOpt> getOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES);
		EventInstance eventDump = null;
		if (info.belongsToSeries && info.isSeriesException) { // -> BROKEN INSTANCE
			eventDump = doEventInstanceGet(con, info, getOpts); // Save for later use!
			
			// 1 - Logically delete this event (the broken)
			ret = doEventDelete(con, info.eventId, true);
			if (ret != 1) throw new WTNotFoundException("Unable to update event [{}]", info.eventId);
			
			// 2 - Updates revision of master event
			ret = evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());
			if (ret != 1) throw new WTNotFoundException("Unable to update master event [{}]", info.masterEventId);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, info.eventId, null);
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
			}
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			// If target is SINCE and the referenced instance is clearly 
			// the first of the recurrence, we can actually treat the 
			// modification as an update to the whole series.
			if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
				boolean thisIsTheFirstInstance = isThisEventInstanceTheFirstInstance(con, info);
				if (thisIsTheFirstInstance) target = UpdateEventTarget.WHOLE_SERIES;
			}
			
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts an exception on deleted date (no broken item is created)
				ret = doRecurrenceAddExcludedDate(con, info.masterEventId, info.seriesInstanceDate);
				if (ret != 1) throw new WTNotFoundException("Unable to update master recurrence [{}]", info.masterEventId);
				
				// 2 - Updates revision of master event
				ret = evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());
				if (ret != 1) throw new WTNotFoundException("Unable to update master event [{}]", info.masterEventId);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
				}

				eventDump = doEventInstanceGet(con, info, getOpts);

			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				EventBounds masterBoundary = evtDao.selectBounds(con, info.masterEventId);
				if (masterBoundary == null) throw new WTNotFoundException("Unable to get master event [{}]", info.masterEventId);
				
				// 1 - Resize original recurrence (sets until date at the day before date)
				OEventRecurrence orec = recDao.selectRecurrenceByEvent(con, info.masterEventId);
				if (orec == null) throw new WTException("Unable to get master recurrence [{}]", info.masterEventId);
				
				LocalTime origNewUntilTime = masterBoundary.isAllDay() ? JodaTimeUtils.TIME_AT_STARTOFDAY : masterBoundary.getStart().withZone(masterBoundary.getTimezoneObject()).toLocalTime();
				orec.resize(info.seriesInstanceDate.minusDays(1), origNewUntilTime, masterBoundary.getTimezoneObject());
				ret = recDao.updateRecurrence(con, orec);
				if (ret != 1) throw new WTException("Unable to update master recurrence [{}]", info.masterEventId);
				
				// 2 - Updates revision of original event
				ret = evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());
				if (ret != 1) throw new WTNotFoundException("Unable to update master event [{}]", info.masterEventId);
				
				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
				}
				
				eventDump = doEventInstanceGet(con, info, getOpts);
				
			} else if (UpdateEventTarget.WHOLE_SERIES.equals(target)) { // Changes are valid for all the instances (whole recurrence)
				eventDump = doEventInstanceGet(con, info, getOpts); // Save for later use!
				
				// 1 - logically delete master event
				ret = doEventDelete(con, info.masterEventId, true);
				if (ret != 1) throw new WTNotFoundException("Unable to update master event [{}]", info.masterEventId);

				DbUtils.commitQuietly(con);
				if (isAuditEnabled()) {
					auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, info.masterEventId, null);
				}
			}
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			eventDump = doEventInstanceGet(con, info, getOpts); // Save for later use!
			
			// 1 - Deletes (logically) this event
			ret = doEventDelete(con, info.eventId, true);
			if (ret != 1) throw new WTNotFoundException("Unable to update event [{}]", info.eventId);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, info.eventId, null);
			}
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(new EI2EI_RRContext(eventDump));
		}
		
		onAfterEventOperation(Crud.DELETE, eventDump, notifyOptions);
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
	
	private EventInsertResult doEventInstanceCopy(final Connection con, final InstanceInfo info, final EventEx event, final int targetCalendarId, final BitFlags<EventProcessOpt> processOptions, final Set<String> validTags) throws DAOException, IOException {
		final String eventId = info.originalEventId();
		
		if (info.belongsToSeries && info.isSeriesException) { // -> BROKEN INSTANCE
			BitFlags<EventProcessOpt> copyOpts = processOptions.unset(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
			return doEventCopy(con, eventId, event, targetCalendarId, copyOpts, validTags);
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			BitFlags<EventProcessOpt> copyOpts = processOptions.unset(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX);
			event.recalculateStartEndForInstance(info.seriesInstanceDate);
			return doEventCopy(con, eventId, event, targetCalendarId, copyOpts, validTags);
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			return doEventCopy(con, eventId, event, targetCalendarId, processOptions, validTags);
		}
	}
	
	private List<String> doEventInstanceMove(final Connection con, final InstanceInfo info, final int targetCategoryId) throws DAOException, WTException {
		EventDAO evt = EventDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		if (info.belongsToSeries) { // -> MASTER INSTANCE, BROKEN INSTANCE or SERIES TEMPLATE INSTANCE
			ArrayList<String> moved = new ArrayList<>();
			moved.addAll(evt.updateCategoryBySeries(con, info.masterEventId, targetCategoryId, revision));
			if (moved.isEmpty())  throw new WTException("Unable to move series events [{}]", info.masterEventId);
			return moved;
			
		} else { // -> SINGLE INSTANCE
			int ret = evt.updateCalendar(con, info.eventId, targetCategoryId, revision);
			if (ret != 1) throw new WTException("Unable to move event [{}]", info.eventId);
			return Arrays.asList(info.eventId);
		}
	}
	
	private void doEventInstanceRestoreAndCommit(final Connection con, final InstanceInfo info, final BitFlags<EventNotifyOption> notifyOptions) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
		int ret;
		
		BitFlags<EventProcessOpt> getOpts = BitFlags.with(EventProcessOpt.RECUR, EventProcessOpt.RECUR_EX, EventProcessOpt.ATTENDEES);
		EventInstance eventDump = null;
		if (info.belongsToSeries && info.isSeriesException) { // -> BROKEN INSTANCE
			eventDump = doEventInstanceGet(con, info, getOpts); // Save for later use!
			
			// 1 - Removes the broken exception
			ret = recDao.deleteRecurrenceExByEventDates(con, info.masterEventId, Arrays.asList(info.seriesInstanceDate));
			if (ret == 0) {
				logger.warn("Missing date exception [{}, {}]", info.masterEventId, info.seriesInstanceDate);
			} else if (ret != 1) {
				throw new WTException("Unable to update master event [{}]", info.masterEventId);
			}
			
			// 2 - Logically delete this event (the broken)
			ret = doEventDelete(con, info.eventId, true);
			if (ret != 1) throw new WTException("Unable to update event [{}]", info.eventId);
			
			// 3 - Updates revision of master event
			ret = evtDao.updateRevision(con, info.masterEventId, BaseDAO.createRevisionTimestamp());
			if (ret != 1) throw new WTException("Unable to update master event [{}]", info.masterEventId);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.EVENT, AuditAction.DELETE, info.eventId, null);
				auditLogWrite(AuditContext.EVENT, AuditAction.UPDATE, info.masterEventId, null);
			}
			
			// TODO: eventually add support to notify attendees of the linked event of date restoration
			
		} else {
			throw new WTException("Unable to restone an instance that is NOT broken [{}]");
		}
		
		if (eventDump == null) throw new WTException("Missing eventDump");
		if (eventDump.hasRecurrence()) {
			// Gets the first valid instance in case of recurring event
			eventDump = calculateFirstRecurringInstance(new EI2EI_RRContext(eventDump));
		}
		
		onAfterEventOperation(Crud.DELETE, eventDump, notifyOptions);
	}
	
	private List<RecipientTuple> getModificationRecipients(int calendarId, String crud) throws WTException {
		ArrayList<RecipientTuple> rcpts = new ArrayList<>();
		
		// Parameter crud is not used for now!
		
		OCalendarOwnerInfo ocoi = doCalendarGetOwnerInfo(calendarId);
		if (ocoi != null) {
			UserProfileId owner = ocoi.getProfileId();
			if (ocoi.getNotifyOnExtUpdate() && !owner.equals(RunContext.getRunProfileId())) {
				UserProfile.Data ud = WT.getProfileData(owner);
				if (ud != null) {
					rcpts.add(new RecipientTuple(ud.getPersonalEmail(), owner));
				}
			}
		}
		return rcpts;
	}
	
	private void notifyForEventModification(UserProfileId fromProfileId, List<RecipientTuple> recipients, EventEx event, String crud) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, fromProfileId.getDomainId());
		UserProfile.Data pdata = WT.getProfileData(fromProfileId);
		InternetAddress from = pdata.getPersonalEmail();
		
		Map<String, String> meetingProviders = css.getMeetingProviders();
		for (RecipientTuple rcpt : recipients) {
			if (!InternetAddressUtils.isAddressValid(rcpt.recipient)) {
				logger.warn("Recipient for event modification is invalid [{}]", rcpt.recipient);
				continue;
			}
			UserProfile.Data ud = WT.getProfileData(rcpt.refProfileId);
			if (ud == null) continue;
			
			try {
				ProfileI18n pI18n = ud.toProfileI18n();
				String title = TplHelper.buildEventModificationTitle(pI18n.getLocale(), event, crud);
				String customBodyHtml = TplHelper.buildEventModificationBody(pI18n, event, meetingProviders);
				String source = EmailNotification.buildSource(pI18n.getLocale(), SERVICE_ID);
				String because = lookupResource(ud.getLocale(), CalendarLocale.EMAIL_EVENTMODIFICATION_FOOTER_BECAUSE);

				String subject = EmailNotification.buildSubject(ud.getLocale(), SERVICE_ID, title);
				String html = new EmailNotification.BecauseBuilder()
					.withCustomBody(title, customBodyHtml)
					.build(ud.getLocale(), source, because, rcpt.recipient.getAddress()).write();
				
				WT.sendEmailMessage(getTargetProfileId(), from, Recipients.to(rcpt.recipient).asList(), subject, html);

			} catch (IOException | TemplateException | AddressException | WTEmailSendException ex) {
				logger.error("Unable to notify recipient after event modification [{}]", ex, rcpt.recipient);
			}
		}	
	}
	
	private List<RecipientTuple> getNotifiableRecipients(final String cudAction, final UserProfileId ownerProfile, final String organizerAddress, final Collection<EventAttendee> attendees, final BitFlags<EventNotifyOption> notifyOptions) {
		return getNotifiableRecipients(cudAction, ownerProfile, organizerAddress, attendees, notifyOptions.has(EventNotifyOption.NOTIFY_INDIVIDUAL_ATTENDEE), notifyOptions.has(EventNotifyOption.NOTIFY_RESOURCE_ATTENDEE));
	}
	
	private List<RecipientTuple> getNotifiableRecipients(final String cudAction, final UserProfileId ownerProfile, final String organizerAddress, final Collection<EventAttendee> attendees, final boolean includeIndividuals, final boolean includeResources) {
		ArrayList<RecipientTuple> items = new ArrayList<>();
		
		// Parameter cudAction is not used for now!
		
		if (attendees != null && !attendees.isEmpty()) {
			for (EventAttendee attendee : attendees) {
				if (EventAttendee.RecipientType.INDIVIDUAL.equals(attendee.getRecipientType()) && (!includeIndividuals || !attendee.getNotify())) {
					// Ignore INDIVIDUAL attendees if notification flag is `false` or they are turned OFF!
					continue;
				}
				if (EventAttendee.RecipientType.RESOURCE.equals(attendee.getRecipientType()) && !includeResources) {
					// Ignore RESOURCE attendees if they are turned OFF!
					continue;
				}
				
				// Skip attendees with a missing recipient address
				InternetAddress attendeeIa = attendee.getRecipientInternetAddress();
				if (attendeeIa == null) continue;
				// Skip the organizer attendee (if present) when its response-status is different from NA
				if (StringUtils.equalsIgnoreCase(organizerAddress, attendeeIa.getAddress())
					&& !EventAttendee.ResponseStatus.NEEDS_ACTION.equals(attendee.getResponseStatus())) continue;

				UserProfileId attProfileId = WT.guessProfileIdByPersonalAddress(attendeeIa.getAddress());
				items.add(new RecipientTuple(attendeeIa, (attProfileId != null) ? attProfileId : ownerProfile));
			}
		}
		return items;
	}
	
	private void notifyForInvitation(UserProfileId senderProfileId, List<RecipientTuple> recipients, EventEx event, String crud) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, senderProfileId.getDomainId());
		ICalendarOutput output = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
		net.fortuna.ical4j.model.property.Method icalMethod = crud.equals(Crud.DELETE) ? net.fortuna.ical4j.model.property.Method.CANCEL : net.fortuna.ical4j.model.property.Method.REQUEST;
		CalendarMethod calMethod = crud.equals(Crud.DELETE) ? CalendarMethod.CANCEL : CalendarMethod.REQUEST;
		
		try {
			String sentFolder = lookupMailSentFolderName(senderProfileId);
			InternetAddress from = WT.getProfileData(senderProfileId).getPersonalEmail();
			String servicePublicUrl = WT.getServicePublicUrl(senderProfileId.getDomainId(), SERVICE_ID);
			
			// Creates ical content
			net.fortuna.ical4j.model.Calendar ical = output.createCalendar(icalMethod, event, null);
			//net.fortuna.ical4j.model.Calendar ical = ICalHelper.toCalendar(icalMethod, prodId, event);
			
			// Creates base message parts
			//String icalText = ICalendarUtils.calendarToString(ical);
			//MimeBodyPart calPart = ICalendarUtils.createInvitationCalendarPart(icalMethod, icalText);
			//String filename = ICalendarUtils.buildICalendarAttachmentFilename(WT.getPlatformName());
			//MimeBodyPart attPart = ICalendarUtils.createInvitationAttachmentPart(icalText, filename);
			
			Map<String, String> meetingProviders = css.getMeetingProviders();
			//IMailManager mailMgr = (IMailManager)WT.getServiceManager("com.sonicle.webtop.mail");
			//FIXME: if mailMgr is not present, send of base SMTP
			//Session session = getMailSession();
			for (RecipientTuple rcpt : recipients) {
				if (!InternetAddressUtils.isAddressValid(rcpt.recipient)) {
					logger.warn("Recipient for event invitation is invalid [{}]", rcpt.recipient);
					continue;
				}
				
				try {
					final ProfileI18n pI18n = coalesceI18nInfo(rcpt.refProfileId);
					String title = TplHelper.buildEventInvitationTitle(pI18n, event, crud);
					String customBodyHtml = TplHelper.buildTplEventInvitationBody(pI18n, crud, event, rcpt.recipient.getAddress(), meetingProviders, servicePublicUrl);
					String source = EmailNotification.buildSource(pI18n.getLocale(), SERVICE_ID);
					String because = lookupResource(pI18n.getLocale(), CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_BECAUSE);

					String subject = EmailNotification.buildSubject(pI18n.getLocale(), SERVICE_ID, title);
					String html = TplHelper.buildEventInvitationHtml(pI18n, event.getTitle(), customBodyHtml, source, because, rcpt.recipient.getAddress(), crud);
					
					//MimeMultipart mmp = ICalendarUtils.createInvitationPart(html, calPart, attPart);
					//sendMail(session, mailMgr, from, rcpt.recipient, subject, mmp);
					EmailMessage message = ICalendarHelper.prepareICalendarMessage(calMethod, ical, from, rcpt.recipient, subject, html);
					WT.sendEmailMessage(senderProfileId, message, sentFolder);
					
				} catch(IOException | TemplateException | MessagingException ex1) {
					logger.warn("Unable to send invitation [{}]", ex1, rcpt.recipient.getAddress());
				}
			}
			
		} catch(/*IOException | MessagingException |*/ WTException ex) {
			logger.warn("Unable to prepare invite notification", ex);
		}
	}
	
	private String lookupMailSentFolderName(UserProfileId sendingProfileId) {
		IMailManager mailMgr = (IMailManager)WT.getServiceManager("com.sonicle.webtop.mail", sendingProfileId);
		return (mailMgr != null) ? mailMgr.getFolderSent() : null;
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
	
	private void replyToOrganizer(final UserProfileId sendingProfileId, final EventEx event, final String replyingAttendeeId, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		// Find the attendee (in event) that has updated its response
		EventAttendee targetAttendee = null;
		for (EventAttendee attendee : event.getAttendees()) {
			if (attendee.getAttendeeId().equals(replyingAttendeeId)) {
				targetAttendee = attendee;
				break;
			}
		}
		if (targetAttendee == null) throw new WTException("Attendee not found [{}]", replyingAttendeeId);
		
		ICalendarOutput output = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
		final net.fortuna.ical4j.model.Calendar icalRequest = output.createCalendar(net.fortuna.ical4j.model.property.Method.REQUEST, event, null);
		replyToOrganizer(sendingProfileId, targetAttendee.getRecipientInternetAddress(), icalRequest, response);
	}
	
	public void replyToOrganizer(final InternetAddress forAddress, final net.fortuna.ical4j.model.Calendar icalRequest, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		replyToOrganizer(getTargetProfileId(), forAddress, icalRequest, response);
	}
	
	private void replyToOrganizer(final UserProfileId sendingProfileId, final InternetAddress forAddress, final net.fortuna.ical4j.model.Calendar icalRequest, final net.fortuna.ical4j.model.parameter.PartStat response) throws WTParseException, WTException {
		final Locale locale = getProfileOrTargetLocale(sendingProfileId);
		
		final String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
		final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ICalendarUtils.getVEvent(icalRequest));
		final EmailMessage email = ICalendarHelper.prepareICalendarReply(prodId, icalRequest, forAddress, iaOrganizer, response, locale);
		WT.sendEmailMessage(sendingProfileId, email);
	}
	
	private void notifyOrganizer(UserProfileId senderProfileId, Event event, String updatedAttendeeId) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, senderProfileId.getDomainId());
		
		try {
			final ProfileI18n pI18n = coalesceI18nInfo(senderProfileId);
			
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
			String source = EmailNotification.buildSource(pI18n.getLocale(), SERVICE_ID);
			String subject = TplHelper.buildResponseUpdateTitle(pI18n, event, targetAttendee);
			String customBodyHtml = TplHelper.buildTplResponseUpdateBody(pI18n, event, meetingProviders, servicePublicUrl);
			
			EmailNotification.NoReplyBuilder builder = new EmailNotification.NoReplyBuilder()
				.withCustomBody(event.getTitle(), customBodyHtml);
			
			if (EventAttendee.ResponseStatus.ACCEPTED.equals(targetAttendee.getResponseStatus())) {
				builder.greenMessage(MessageFormat.format(lookupResource(pI18n.getLocale(), CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_ACCEPTED), targetAttendee.getRecipient()));
			} else if (EventAttendee.ResponseStatus.TENTATIVE.equals(targetAttendee.getResponseStatus())) {
				builder.yellowMessage(MessageFormat.format(lookupResource(pI18n.getLocale(), CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_TENTATIVE), targetAttendee.getRecipient()));
			} else if (EventAttendee.ResponseStatus.DECLINED.equals(targetAttendee.getResponseStatus())) {
				builder.redMessage(MessageFormat.format(lookupResource(pI18n.getLocale(), CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_DECLINED), targetAttendee.getRecipient()));
			} else {
				builder.greyMessage(MessageFormat.format(lookupResource(pI18n.getLocale(), CalendarLocale.TPL_EMAIL_RESPONSEUPDATE_MSG_OTHER), targetAttendee.getRecipient()));
			}
			String html = builder.build(pI18n.getLocale(), source).write();
			WT.sendEmailMessage(senderProfileId, from, Recipients.to(to).asList(), subject, html);
			
		} catch(Exception ex) {
			logger.warn("Unable to notify organizer", ex);
		}	
	}
	
	public boolean iAmTheOrganizer(final InternetAddress eventOrganizer, final UserProfile.Data myData) {
		if (myData == null) return false;
		return iAmTheOrganizer(eventOrganizer, myData.getPersonalEmailAddress(), myData.getProfileEmailAddress());
	}
	
	/**
	 * Checks if passed addresses belongs to event's organizer.
	 * This will return a positive match in these two cases (OR):
	 * - organizer addr. matches personal address
	 * - organizer addr. matches profile address
	 * 
	 * @param eventOrganizer
	 * @param myPersonalEmailAddress
	 * @param myProfileEmailAddress
	 * @return 
	 */
	public boolean iAmTheOrganizer(final InternetAddress eventOrganizer, final String myPersonalEmailAddress, final String myProfileEmailAddress) {
		if (eventOrganizer == null) return false;
		String orgAddress = InternetAddressUtils.getAddress(eventOrganizer);
		return StringUtils.equals(orgAddress, myPersonalEmailAddress) || StringUtils.equals(orgAddress, myProfileEmailAddress);
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
	
	/*
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
	*/
	
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
	
	private ReminderInApp createEventReminderAlertWeb(ProfileI18n profileI18n, String eventInstanceId, EventAlertLookupInstance eventLookup, EventEx event) {
		String type = eventLookup.getHasRecurrence() ? "event" /*"event-recurring"*/ : "event";
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, eventLookup.getCalendarProfileId(), type, eventInstanceId);
		alert.setTitle(event.getTitle());
		alert.setDate(eventLookup.getStart().withZone(profileI18n.getTimezone()));
		alert.setTimezone(profileI18n.getTimezone().getID());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(ProfileI18n profileI18n, String eventInstanceId, EventAlertLookup eventLookup, EventEx event, String recipientEmail) throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, eventLookup.getCalendarDomainId());
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, eventLookup.getCalendarProfileId(), "event", eventInstanceId);
		
		try {
			Map<String, String> meetingProviders = css.getMeetingProviders();
			
			String source = NotificationHelper.buildSource(profileI18n.getLocale(), SERVICE_ID);
			String because = lookupResource(profileI18n.getLocale(), CalendarLocale.EMAIL_REMINDER_FOOTER_BECAUSE);
			String customBodyHtml = TplHelper.buildTplEventReminderBody(profileI18n, event, meetingProviders);
			
			String title = TplHelper.buildEventReminderTitle(profileI18n, event);
			String html = TplHelper.buildEventInvitationHtml(profileI18n, event.getTitle(), customBodyHtml, source, because, recipientEmail, null);
			//String body = TplHelper.buildInvitationTpl(profileI18n.getLocale(), source, recipientEmail, event.getTitle(), customBodyHtml, because, null);
			
			alert.setSubject(EmailNotification.buildSubject(profileI18n.getLocale(), SERVICE_ID, title));
			alert.setBody(html);
			
		} catch (IOException | TemplateException | AddressException ex) {
			throw new WTException(ex);
		}
		return alert;
	}
	
	private void storeAsSuggestion(CoreManager coreMgr, String context, String value) {
		if (StringUtils.isBlank(value)) return;
		coreMgr.addServiceStoreEntry(SERVICE_ID, context, value.toUpperCase(), value);
	}
	
	private InstancesBulkInfo collectInstancesBulkInfo(Connection con, Collection<EventInstanceId> instanceIds) {
		EventDAO evtDao = EventDAO.getInstance();
		
		LinkedHashMap<EventInstanceId, InstanceInfo> infoMap = new LinkedHashMap<>(instanceIds.size());
		LinkedHashSet<String> involvedEventIds = new LinkedHashSet<>();
		for (EventInstanceId instanceId : instanceIds) {
			InstanceInfo info = new InstanceInfo(instanceId, evtDao.selectInstanceInfo(con, instanceId));
			infoMap.put(instanceId, info);
			// Fill involvedEventIds collection only if we have a valid event ID!
			String eventId = info.originalEventId();
			if (eventId != null) involvedEventIds.add(eventId);
		}
		return new InstancesBulkInfo(infoMap, involvedEventIds);
	}
	
	private static class InstancesBulkInfo {
		public final Map<EventInstanceId, InstanceInfo> infoMap;
		public final Set<String> involvedIds;
		
		public InstancesBulkInfo(Map<EventInstanceId, InstanceInfo> infoMap, Set<String> involvedIds) {
			this.infoMap = infoMap;
			this.involvedIds = involvedIds;
		}
	}
	
	private enum DoOption {
		SKIP, UPDATE
	}
	
	private class VEB2EB_RRContext implements RecurringInstanceContext<EventBounds> {
		private final VEventBounds bounds;
		private final Set<LocalDate> recurrenceExclDates;
		
		public VEB2EB_RRContext(VEventBounds bounds) {
			this.bounds = bounds;
			this.recurrenceExclDates = lookupExceptions(bounds);
		}
		
		private Set<LocalDate> lookupExceptions(VEventBounds bounds) {
			if (!StringUtils.isBlank(bounds.getRecurrenceExceptions())) {
				LinkedHashSet<LocalDate> set = new LinkedHashSet<>();
				for (String s : StringUtils.split(bounds.getRecurrenceExceptions(), "|")) {
					LocalDate ld = JodaTimeUtils.parseLocalDateYMD(s);
					if (ld != null) set.add(ld);
				}
				return set;
			} else {
				return null;
			}
		}
		
		@Override
		public String getEventId() {
			return bounds.getEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return bounds.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return bounds.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return bounds.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return bounds.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return bounds.getRecurrenceStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return bounds.getRecurrenceObject();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return recurrenceExclDates;
		}
		
		@Override
		public EventBounds createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			return new EventBoundsImpl(bounds.getAllDay(), start, end, bounds.getTimezoneObject());
		}
	}
	
	private class VEL2ELI_RRContext implements RecurringInstanceContext<EventLookupInstance> {
		private final VEventLookup event;
		private final boolean censorize;
		private final Set<LocalDate> recurrenceExclDates;
		private final LocalDate firstInstanceDate;
		
		public VEL2ELI_RRContext(Connection con, VEventLookup event, boolean censorize) {
			this.event = event;
			this.censorize = censorize;
			this.recurrenceExclDates = lookupExceptions(con, event);
			this.firstInstanceDate = calculateRecurrenceStart(event, recurrenceExclDates);
		}
		
		private Set<LocalDate> lookupExceptions(Connection con, VEventLookup event) {
			if (event.getHasRecurrenceEx()) {
				EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
				return recDao.selectRecurrenceExByEvent(con, event.getEventId());
			} else {
				return null;
			}
		}
		
		private LocalDate calculateRecurrenceStart(VEventLookup event, Set<LocalDate> recurrenceExDates) {
			List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(event.getRecurrenceObject(), event.getRecurrenceStart(), true, recurrenceExDates, event.getStart(), event.getEnd(), event.getTimezoneObject(), null, null, 1);
			return (dates != null && !dates.isEmpty()) ? dates.get(0) : null;
		}
		
		@Override
		public String getEventId() {
			return event.getEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return event.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return event.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return event.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return event.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return event.getRecurrenceStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return event.getRecurrenceObject();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return recurrenceExclDates;
		}

		@Override
		public EventLookupInstance createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			EventLookupInstance item = ManagerUtils.fillEventLookup(new EventLookupInstance(id, event.getEventId(), id.getInstanceAsDate().equals(firstInstanceDate)), event);
			item.setStart(start);
			item.setEnd(end);
			if (censorize) item.censorize();
			return item;
		}
	}
	
	private class EI2EI_RRContext implements RecurringInstanceContext<EventInstance> {
		private final EventInstance masterEvent;
		
		public EI2EI_RRContext(EventInstance masterEvent) {
			this.masterEvent = masterEvent;
		}
		
		@Override
		public String getEventId() {
			return masterEvent.getOriginalEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return masterEvent.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return masterEvent.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return masterEvent.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return masterEvent.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return masterEvent.getRecurrence().getStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return masterEvent.getRecurrence().getRecurRule();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return masterEvent.getRecurrence().getExcludedDatesOrEmpty();
		}
		
		@Override
		public EventInstance createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			EventInstance item = new EventInstance(id, masterEvent.getOriginalEventId(), masterEvent);
			item.setStart(start);
			item.setEnd(end);
			return item;
		}
	}
	
	private class VEB2CEB_RRContext implements RecurringInstanceContext<ComparableEventRange> {
		private final VEventBounds seriesEvent;
		private final Set<LocalDate> recurrenceExDates;
		
		public VEB2CEB_RRContext(Connection con, VEventBounds seriesEvent) {
			this.seriesEvent = seriesEvent;
			this.recurrenceExDates = EventRecurrenceDAO.getInstance().selectRecurrenceExByEvent(con, seriesEvent.getEventId());
		}
		
		@Override
		public String getEventId() {
			return seriesEvent.getEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return seriesEvent.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return seriesEvent.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return seriesEvent.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return seriesEvent.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return seriesEvent.getRecurrenceStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return seriesEvent.getRecurrenceObject();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return recurrenceExDates;
		}
		
		@Override
		public ComparableEventRange createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			return new ComparableEventRange(id, start, end);
		}
	}
	
	private class EI2CER_RRContext implements RecurringInstanceContext<ComparableEventRange> {
		private final EventInstance masterEvent;
		
		public EI2CER_RRContext(EventInstance masterEvent) {
			this.masterEvent = masterEvent;
		}
		
		@Override
		public String getEventId() {
			return masterEvent.getOriginalEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return masterEvent.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return masterEvent.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return masterEvent.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return masterEvent.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return masterEvent.getRecurrence().getStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return masterEvent.getRecurrence().getRecurRule();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return masterEvent.getRecurrence().getExcludedDatesOrEmpty();
		}
		
		@Override
		public ComparableEventRange createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			return new ComparableEventRange(id, start, end);
		}
	}
	
	private class EALI2VEL_RRContext implements RecurringInstanceContext<EventAlertLookupInstance> {
		private final VEventLookup seriesEvent;
		private final OEventRecurrence recurrence;
		private final Set<LocalDate> recurrenceExclDates;
		
		public EALI2VEL_RRContext(Connection con, VEventLookup seriesEvent) {
			this.seriesEvent = Check.notNull(seriesEvent, "seriesEvent");
			EventRecurrenceDAO recDao = EventRecurrenceDAO.getInstance();
			this.recurrence = recDao.selectRecurrenceByEvent(con, seriesEvent.getEventId());
			this.recurrenceExclDates = recDao.selectRecurrenceExByEvent(con, seriesEvent.getEventId());
		}

		@Override
		public String getEventId() {
			return seriesEvent.getEventId();
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return seriesEvent.getTimezoneObject();
		}
		
		@Override
		public boolean isEventAllDay() {
			return seriesEvent.getAllDay();
		}

		@Override
		public DateTime getEventStart() {
			return seriesEvent.getStart();
		}

		@Override
		public DateTime getEventEnd() {
			return seriesEvent.getEnd();
		}

		@Override
		public DateTime getRecurrenceStart() {
			return recurrence.getStart();
		}

		@Override
		public Recur getRecurrenceDefinition() {
			return recurrence.getRecurrenceObject();
		}

		@Override
		public Set<LocalDate> getRecurrenceExclDates() {
			return recurrenceExclDates;
		}
		
		@Override
		public EventAlertLookupInstance createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey) {
			EventAlertLookupInstance item = ManagerUtils.fillEventAlertLookup(new EventAlertLookupInstance(id, seriesEvent.getEventId()), seriesEvent);
			item.setStart(start);
			item.setEnd(end);
			return item;
		}
	}
	
	private interface RecurringInstanceContext<T> {
		public String getEventId();
		public DateTimeZone getTimezone();
		public boolean isEventAllDay();
		public DateTime getEventStart();
		public DateTime getEventEnd();
		public DateTime getRecurrenceStart();
		public Recur getRecurrenceDefinition();
		public Set<LocalDate> getRecurrenceExclDates();
		public T createInstance(EventInstanceId id, DateTime start, DateTime end, String legacyKey);
	}
	
	private <T> T calculateFirstRecurringInstance(final RecurringInstanceContext<T> context) throws WTException {
		final List<T> instances = calculateRecurringInstances(context, null, null, 1);
		return instances.isEmpty() ? null : instances.get(0);
	}
	
	private <T> List<T> calculateRecurringInstances(final RecurringInstanceContext<T> context, final DateTime spanFrom, final DateTime spanTo, final int limitNoOfInstances) throws WTException {
		Check.notNull(context, "context");
		ArrayList<T> instances = new ArrayList<>();
		
		final DateTimeZone timezone = context.getTimezone();
		final String eventId = context.getEventId();
		final DateTime eventStart = context.getEventStart();
		final DateTime eventEnd = context.getEventEnd();
		final DateTime recurStart = context.getRecurrenceStart();
		final Recur recur = context.getRecurrenceDefinition();
		
		if (eventStart != null && eventEnd != null && recurStart != null && recur != null) {
			final LocalDateTime eventLocalStart = eventStart.withZone(timezone).toLocalDateTime();
			final LocalDateTime eventLocalEnd = eventEnd.withZone(timezone).toLocalDateTime();
			
			// Define initial computation range taken from parameters
			DateTime rangeFrom = spanFrom;
			DateTime rangeTo = spanTo;
			
			// No range specified, set a default window starting recurrence start
			if (rangeFrom == null) rangeFrom = recurStart;
			if (rangeTo == null) recurStart.plusYears(1);
			
			List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, recurStart, context.isEventAllDay(), context.getRecurrenceExclDates(), eventStart, eventEnd, timezone, rangeFrom, rangeTo, limitNoOfInstances);
			for (LocalDate date : dates) {
				final DateTimeWindow eventSpan = CalendarUtils.computeStartEndForEventInstance(
					date,
					eventLocalStart,
					eventLocalEnd,
					timezone);
				
				final EventInstanceId id = EventInstanceId.build(eventId, eventSpan.getStart(), timezone);
				final String legacyKey = EventKey.buildKey(eventId, eventId, date);
				instances.add(context.createInstance(id, eventSpan.getStart(), eventSpan.getEnd(), legacyKey));
			}
		} else {
			if (eventStart == null) logger.warn("Event has NO valid start instant [{}]", eventId);
			if (eventEnd == null) logger.warn("Event has NO valid end instant [{}]", eventId);
			if (recurStart == null) logger.warn("Event has NO recurrence start instant", eventId);
			if (recur == null) logger.warn("Event has NO recurrence rule [{}]", eventId);
		}
		return instances;
	}
	
	public static class EventUpdateResult {
		public final OEvent oevent;
		public final ChangeSet<EventAttendee> attendeeChanges;
		
		public EventUpdateResult(OEvent oevent, ChangeSet<EventAttendee> attendeeChanges) {
			this.oevent = oevent;
			this.attendeeChanges = attendeeChanges;
		}
	}
	
	public static class EventInsertResult {
		public final OEvent oevent;
		public final OEventRecurrence orecurrence;
		public final List<OEventAttendee> oattendees;
		public final Set<String> otags;
		public final List<OEventAttachment> oattachments;
		public final List<OEventCustomValue> ocustomvalues;
		
		public EventInsertResult(OEvent oevent, OEventRecurrence orecurrence, ArrayList<OEventAttendee> oattendees, Set<String> otags, List<OEventAttachment> oattachments, ArrayList<OEventCustomValue> ocustomvalues) {
			this.oevent = oevent;
			this.orecurrence = orecurrence;
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
	
	private class CustomFieldsNameToIDCache extends AbstractPassiveExpiringBulkMap<String, String> {
		
		public CustomFieldsNameToIDCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Map<String, String> internalGetMap() {
			try {
				CoreManager coreMgr = WT.getCoreManager();
				return coreMgr.getCustomFieldNamesMap(SERVICE_ID, BitFlags.noneOf(CoreManager.CustomFieldListOption.class));
				
			} catch(Throwable t) {
				logger.error("[CustomFieldsNameToIDCache] Unable to build cache", t);
				throw new UnsupportedOperationException();
			}
		}
	}
	
	private class CustomFieldIDToTypeCache extends AbstractPassiveExpiringBulkMap<String, CustomField.Type> {
		
		public CustomFieldIDToTypeCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Map<String, CustomField.Type> internalGetMap() {
			try {
				CoreManager coreMgr = WT.getCoreManager();
				return coreMgr.listCustomFieldTypesById(SERVICE_ID, BitFlags.noneOf(CoreManager.CustomFieldListOption.class));
				
			} catch(Throwable t) {
				logger.error("[CustomFieldIDToTypeCache] Unable to build cache", t);
				throw new UnsupportedOperationException();
			}
		}
	}
	
	private enum AuditContext {
		CALENDAR, EVENT
	}
	
	private enum AuditAction {
		CREATE, UPDATE, DELETE, MOVE, TAG
	}
	
	private class AuditEvent implements AuditReferenceDataEntry {
		public final String eventId;
		
		public AuditEvent(String eventId) {
			this.eventId = eventId;
		}

		@Override
		public String getReference() {
			return eventId;
		}

		@Override
		public String getData() {
			return null;
		}
	}
	
	private class AuditEventMove implements AuditReferenceDataEntry {
		public final String eventId;
		public final int origCalendarId;
		
		public AuditEventMove(String eventId, int origCalendarId) {
			this.eventId = eventId;
			this.origCalendarId = origCalendarId;
		}

		@Override
		public String getReference() {
			return eventId;
		}

		@Override
		public String getData() {
			return String.valueOf(origCalendarId);
		}
	}
	
	private class AuditEventCopy implements AuditReferenceDataEntry {
		public final String eventId;
		public final String origEventId;
		
		public AuditEventCopy(String eventId, String origEventId) {
			this.eventId = eventId;
			this.origEventId = origEventId;
		}

		@Override
		public String getReference() {
			return eventId;
		}

		@Override
		public String getData() {
			return origEventId;
		}
	}
	
	private static class InstanceInfo {
		// TABLE events
		// | eventId                           | series_event_id                   | series_instance
		//   6e5070066a5d11eb9c2d0d2374d38e09   (Null)                             (Null)
		//   d62a21876a5d11eb9c2d4d84360a8aaa   (Null)                             (Null)
		//   89fa4e3a899f11eb86b139828f248cb5   d62a21876a5d11eb9c2d4d84360a8aaa   20210402
		//
		// TABLE events
		// | eventId                           | start_date              | until_date              | rule
		//   d62a21876a5d11eb9c2d4d84360a8aaa   2021-04-01 15:00:00+02    2021-04-05 15:00:00+02    RRULE:FREQ=DAILY;COUNT=5

		// Thesea are query results for above tables:

		// 6e5070066a5d11eb9c2d0d2374d38e09.20210401 (series instance not existing)
		// belongsToSeries (existsRecurrenceByEvent): false
		// eventIdByInstance (selectIdBySeriesInstance): null

		// 6e5070066a5d11eb9c2d0d2374d38e09.00000000 (single instance)
		// belongsToSeries (existsRecurrenceByEvent): false
		// eventIdByInstance (selectIdBySeriesInstance): null

		// d62a21876a5d11eb9c2d4d84360a8aaa.00000000 (series master)
		// belongsToSeries (existsRecurrenceByEvent): true
		// eventIdByInstance (selectIdBySeriesInstance): null

		// d62a21876a5d11eb9c2d4d84360a8aaa.20210401 (series instance)
		// belongsToSeries (existsRecurrenceByEvent): true
		// eventIdByInstance (selectIdBySeriesInstance): null

		// d62a21876a5d11eb9c2d4d84360a8aaa.20210402 (series broken instance)
		// belongsToSeries (existsRecurrenceByEvent): true
		// eventIdByInstance (selectIdBySeriesInstance): 89fa4e3a899f11eb86b139828f248cb5
		
		/**
		 * Reports if the instance is attributable to a series, whether it is 
		 * a master instance (series generator) or other instances (series templates or broken).
		 */
		public final boolean belongsToSeries;
		
		/**
		 * Reports if the instance is a broken element of the series.
		 * After being edited, a series instance template become a broken instance.
		 */
		public final boolean isSeriesException;
		
		/**
		 * Reports the real event ID of the instance: valued for master, broken and single
		 * instances, null for series templates. This will be null too for unexistent instances.
		 */
		public final String eventId;
		
		/**
		 * Reports the timezone of the instance: taken from the master element in 
		 * case of instance template (or master element itself), or from the event 
		 * in case of broken or single instances
		 */
		public final String eventTimezone;
		
		/**
		 * Reports if the instance is private for the owner.
		 */
		public final boolean eventIsPrivate;
		
		/**
		 * Reports the eventId of the master series, only if this instance clearly
		 * belongs to a series (for brokens too). This will be null for single instances.
		 */
		public final String masterEventId;
		
		/**
		 * Reports the instance key: not null for instances different from 00000000
		 */
		public final String seriesInstance;
		
		/**
		 * Reports the instance key as date: not null for instances different from 00000000
		 */
		public final LocalDate seriesInstanceDate;
		
		public InstanceInfo(EventInstanceId instanceId, OEventInstanceInfo oeii) {
			if (instanceId.hasNoInstance()) {
				this.belongsToSeries = oeii.getHasRecurrence();
				this.masterEventId = belongsToSeries ? instanceId.getEventId() : null;
				this.eventId = instanceId.getEventId();
				this.eventTimezone = oeii.getTimezone();
				this.eventIsPrivate = oeii.isVisibilityPrivate();
				this.isSeriesException = false;
				this.seriesInstance = null;
				this.seriesInstanceDate = null;
				
			} else {
				this.belongsToSeries = oeii.getHasRecurrence();
				this.masterEventId = belongsToSeries ? instanceId.getEventId() : null;
				this.eventId = oeii.getEventIdByInstance();
				this.eventTimezone = oeii.getTimezone();
				this.eventIsPrivate = oeii.isVisibilityPrivate();
				this.isSeriesException = (eventId != null);
				this.seriesInstance = instanceId.getInstance();
				this.seriesInstanceDate = instanceId.getInstanceAsDate();
			}
		}
		
		public InstanceInfo(EventInstance instance) {
			this(instance.getId(), instance.getOriginalEventId(), instance);
		}
		
		public InstanceInfo(EventInstanceId instanceId, String originalEventId, EventEx event) {
			if (EventInstanceId.isSeriesMaster(instanceId, originalEventId)) {
				this.belongsToSeries = event.hasRecurrence();
				this.masterEventId = belongsToSeries ? instanceId.getEventId() : null;
				this.eventId = instanceId.getEventId();
				this.eventTimezone = event.getTimezone();
				this.eventIsPrivate = event.isVisibilityPrivate();
				this.isSeriesException = false;
				this.seriesInstance = null;
				this.seriesInstanceDate = null;
				
			} else {
				this.belongsToSeries = event.hasRecurrence();
				this.masterEventId = belongsToSeries ? instanceId.getEventId() : null;
				this.eventId = originalEventId;
				this.eventTimezone = event.getTimezone();
				this.eventIsPrivate = event.isVisibilityPrivate();
				this.isSeriesException = EventInstanceId.isSeriesException(instanceId, originalEventId);
				this.seriesInstance = instanceId.getInstance();
				this.seriesInstanceDate = instanceId.getInstanceAsDate();
			}
		}
		
		public String originalEventId() {
			return LangUtils.coalesceStrings(eventId, masterEventId);
		}
		
		public DateTimeZone eventTimezoneAsObject() {
			return JodaTimeUtils.parseTimezone(this.eventTimezone);
		}
	}
	
	private enum EventProcessOpt implements BitFlagsEnum<EventProcessOpt> {
		RECUR(1<<0), RECUR_EX(1<<1), ATTENDEES(1<<2), ATTACHMENTS(1<<3), TAGS(1<<4), CUSTOM_VALUES(1<<5), RAW_ICAL(1<<6), IGNORE_ATTENDEE_RESPONSE(1<<7), IGNORE_ATTENDEE_NOTIFY(1<<8);
		
		private int mask = 0;
		private EventProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<EventProcessOpt> parseEventGetOptions(BitFlags<EventGetOption> flags) {
			BitFlags<EventProcessOpt> ret = new BitFlags<>(EventProcessOpt.class);
			if (flags.has(EventGetOption.ATTENDEES)) ret.set(EventProcessOpt.ATTENDEES);
			if (flags.has(EventGetOption.ATTACHMENTS)) ret.set(EventProcessOpt.ATTACHMENTS);
			if (flags.has(EventGetOption.TAGS)) ret.set(EventProcessOpt.TAGS);
			if (flags.has(EventGetOption.CUSTOM_VALUES)) ret.set(EventProcessOpt.CUSTOM_VALUES);
			return ret;
		}
		
		public static BitFlags<EventProcessOpt> parseEventUpdateOptions(BitFlags<EventUpdateOption> flags) {
			BitFlags<EventProcessOpt> ret = new BitFlags<>(EventProcessOpt.class);
			if (flags.has(EventUpdateOption.ATTENDEES)) ret.set(EventProcessOpt.ATTENDEES);
			if (flags.has(EventUpdateOption.ATTACHMENTS)) ret.set(EventProcessOpt.ATTACHMENTS);
			if (flags.has(EventUpdateOption.TAGS)) ret.set(EventProcessOpt.TAGS);
			if (flags.has(EventUpdateOption.CUSTOM_VALUES)) ret.set(EventProcessOpt.CUSTOM_VALUES);
			if (flags.has(EventUpdateOption.IGNORE_ATTENDEE_RESPONSE)) ret.set(EventProcessOpt.IGNORE_ATTENDEE_RESPONSE);
			if (flags.has(EventUpdateOption.IGNORE_ATTENDEE_NOTIFY)) ret.set(EventProcessOpt.IGNORE_ATTENDEE_NOTIFY);
			return ret;
		}
	}
}
