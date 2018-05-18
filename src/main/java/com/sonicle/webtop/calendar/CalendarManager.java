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

import com.sonicle.webtop.calendar.model.GetEventScope;
import com.rits.cloning.Cloner;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.CompositeId;
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
import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OEventICalendar;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.VEventCalObject;
import com.sonicle.webtop.calendar.bol.VEventCalObjectChanged;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.CalendarPropsDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.EventICalendarDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.calendar.io.EventInput;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.model.IncomingShareRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.IdentifierUtils;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.property.RRule;
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
import com.sonicle.webtop.calendar.model.EventCalObject;
import com.sonicle.webtop.calendar.model.EventCalObjectChanged;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.util.ICalendarInput;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.sdk.AbstractMapCache;
import com.sonicle.webtop.core.sdk.AbstractShareCache;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTNotFoundException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.mail.IMailManager;
import freemarker.template.TemplateException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMultipart;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;

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
	
	public CalendarManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		if (!fastInit) {
			shareCache.init();
		}
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
	
	private List<ShareRootCalendar> internalListIncomingCalendarShareRoots() throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		List<ShareRootCalendar> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		for (IncomingShareRoot share : coreMgr.listIncomingShareRoots(SERVICE_ID, GROUPNAME_CALENDAR)) {
			final SharePermsRoot perms = coreMgr.getShareRootPermissions(share.getShareId());
			ShareRootCalendar root = new ShareRootCalendar(share, perms);
			if (hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public String buildSharingId(int calendarId) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		// Skip rights check if running user is resource's owner
		UserProfileId owner = ownerCache.get(calendarId);
		if (owner == null) throw new WTException("owner({0}) -> null", calendarId);
		
		String rootShareId = null;
		if (owner.equals(targetPid)) {
			rootShareId = MyShareRootCalendar.SHARE_ID;
		} else {
			rootShareId = shareCache.getShareRootIdByFolderId(calendarId);
		}
		if (rootShareId == null) throw new WTException("Unable to find a root share [{0}]", calendarId);
		return new CompositeId().setTokens(rootShareId, calendarId).toString();
	}
	
	public Sharing getSharing(String shareId) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		return coreMgr.getSharing(SERVICE_ID, GROUPNAME_CALENDAR, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		coreMgr.updateSharing(SERVICE_ID, GROUPNAME_CALENDAR, sharing);
	}
	
	@Override
	public List<ShareRootCalendar> listIncomingCalendarRoots() throws WTException {
		return shareCache.getShareRoots();
	}
	
	@Override
	public Map<Integer, ShareFolderCalendar> listIncomingCalendarFolders(String rootShareId) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		LinkedHashMap<Integer, ShareFolderCalendar> folders = new LinkedHashMap<>();
		
		for (Integer folderId : shareCache.getFolderIdsByShareRoot(rootShareId)) {
			final String shareFolderId = shareCache.getShareFolderIdByFolderId(folderId);
			if (StringUtils.isBlank(shareFolderId)) continue;
			SharePermsFolder fperms = coreMgr.getShareFolderPermissions(shareFolderId);
			SharePermsElements eperms = coreMgr.getShareElementsPermissions(shareFolderId);
			if (folders.containsKey(folderId)) {
				final ShareFolderCalendar shareFolder = folders.get(folderId);
				if (shareFolder == null) continue;
				shareFolder.getPerms().merge(fperms);
				shareFolder.getElementsPerms().merge(eperms);
			} else {
				final Calendar calendar = getCalendar(folderId);
				if (calendar == null) continue;
				folders.put(folderId, new ShareFolderCalendar(shareFolderId, fperms, eperms, calendar));
			}
		}
		return folders;
	}
	
	@Override
	public List<Integer> listCalendarIds() throws WTException {
		return new ArrayList<>(listCalendars().keySet());
	}
	
	@Override
	public List<Integer> listIncomingCalendarIds() throws WTException {
		return shareCache.getFolderIds();
	}
	
	@Override
	public Map<Integer, Calendar> listCalendars() throws WTException {
		return listCalendars(getTargetProfileId());
	}
	
	private Map<Integer, Calendar> listCalendars(UserProfileId pid) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		LinkedHashMap<Integer, Calendar> items = new LinkedHashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCalendar ocal : calDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
				//checkRightsOnCalendarFolder(ocal.getCalendarId(), "READ");
				items.put(ocal.getCalendarId(), ManagerUtils.createCalendar(ocal));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
					.filter(categoryId -> quietlyCheckRightsOnCalendarFolder(categoryId, "READ"))
					.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			return evtDao.selectMaxRevTimestampByCalendars(con, okCalendarIds);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public UserProfileId getCalendarOwner(int calendarId) throws WTException {
		return ownerCache.get(calendarId);
	}
	
	@Override
	public Calendar getCalendar(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGet(con, calendarId);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
			
			checkRightsOnCalendarFolder(ocal.getCalendarId(), "READ");
			
			return ManagerUtils.createCalendar(ocal);
			
		} catch(SQLException | DAOException | WTException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar addCalendar(Calendar calendar) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarRoot(calendar.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar.setBuiltIn(false);
			calendar = doCalendarUpdate(true, con, calendar);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_INSERT", String.valueOf(calendar.getCalendarId()));
			
			return calendar;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar addBuiltInCalendar() throws WTException {
		CalendarDAO caldao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarRoot(getTargetProfileId(), "MANAGE");
			
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
			cal.setIsDefault(true);
			cal = doCalendarUpdate(true, con, cal);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_INSERT",  String.valueOf(cal.getCalendarId()));
			
			return cal;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar updateCalendar(Calendar calendar) throws WTException {
		Connection con = null;
		
		try {
			int calendarId = calendar.getCalendarId();
			checkRightsOnCalendarFolder(calendarId, "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar = doCalendarUpdate(false, con, calendar);
			if (calendar == null) throw new WTNotFoundException("Calendar not found [{}]", calendarId);
			
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_UPDATE", String.valueOf(calendarId));
			
			return calendar;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteCalendar(int calendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "DELETE");
			
			// Retrieve sharing status (for later)
			String sharingId = buildSharingId(calendarId);
			Sharing sharing = getSharing(sharingId);
			
			con = WT.getConnection(SERVICE_ID, false);
			Calendar cal = ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) throw new WTNotFoundException("Calendar not found [{}]", calendarId);
			
			int deleted = calDao.deleteById(con, calendarId);
			psetDao.deleteByCalendar(con, calendarId);
			doEventDeleteByCalendar(con, calendarId, !cal.isProviderRemote());
			
			// Cleanup sharing, if necessary
			if ((sharing != null) && !sharing.getRights().isEmpty()) {
				logger.debug("Removing {} active sharing [{}]", sharing.getRights().size(), sharing.getId());
				sharing.getRights().clear();
				updateSharing(sharing);
			}
			
			DbUtils.commitQuietly(con);
			
			final String ref = String.valueOf(calendarId);
			writeLog("CALENDAR_DELETE", ref);
			writeLog("EVENT_DELETE",  "*@"+ref);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DateTime> listEventDates(Collection<Integer> calendarIds, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> okCalendarIds = calendarIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendarFolder(calendarId, "READ"))
					.collect(Collectors.toList());
			
			HashSet<DateTime> dates = new HashSet<>();
			for (VVEvent vevt : evtDao.viewByCalendarFromToPattern(con, okCalendarIds, fromDate, toDate, null)) {
				ManagerUtils.pushDatesBetweenStartEnd(dates, vevt.getStartDate(), vevt.getEndDate());
			}
			for (VVEvent vevt : evtDao.viewRecurringByCalendarFromToPattern(con, okCalendarIds, fromDate, toDate, null)) {
				final List<SchedEventInstance> instances = calculateRecurringInstances(con, new SchedEventInstanceMapper(vevt), fromDate, toDate, userTimezone, 200);
				for (SchedEventInstance instance : instances) {
					ManagerUtils.pushDatesBetweenStartEnd(dates, instance.getStartDate(), instance.getEndDate());
				}
			}
			return new ArrayList<>(dates);
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<EventCalObject> listEventCalObjects(int calendarId) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			ArrayList<EventCalObject> items = new ArrayList<>();
			Map<String, List<VEventCalObject>> vobjMap = evtDao.viewCalObjectsByCalendar(con, calendarId);
			for (List<VEventCalObject> vobjs : vobjMap.values()) {
				if (vobjs.isEmpty()) continue;
				VEventCalObject vobj = vobjs.get(vobjs.size()-1);
				if (vobjs.size() > 1) {
					logger.trace("Many CalObjects ({}) found for same href [{} -> {}]", vobjs.size(), vobj.getHref(), vobj.getEventId());
				}
				
				items.add(doEventCalObjectPrepare(con, vobj));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public CollectionChangeSet<EventCalObjectChanged> listEventCalObjectsChanges(int calendarId, DateTime since, Integer limit) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			ArrayList<EventCalObjectChanged> inserted = new ArrayList<>();
			ArrayList<EventCalObjectChanged> updated = new ArrayList<>();
			ArrayList<EventCalObjectChanged> deleted = new ArrayList<>();
			
			if (limit == null) limit = Integer.MAX_VALUE;
			if (since == null) {
				List<VEventCalObjectChanged> vobjs = evtDao.viewChangedByCalendar(con, calendarId, limit);
				for (VEventCalObjectChanged vobj : vobjs) {
					inserted.add(new EventCalObjectChanged(vobj.getEventId(), vobj.getRevisionTimestamp(), vobj.getHref()));
				}
			} else {
				List<VEventCalObjectChanged> vobjs = evtDao.viewChangedByCalendarSince(con, calendarId, since, limit);
				for (VEventCalObjectChanged vobj : vobjs) {
					Event.RevisionStatus revStatus = EnumUtils.forSerializedName(vobj.getRevisionStatus(), Event.RevisionStatus.class);
					if (Event.RevisionStatus.DELETED.equals(revStatus)) {
						deleted.add(new EventCalObjectChanged(vobj.getEventId(), vobj.getRevisionTimestamp(), vobj.getHref()));
					} else {
						if (Event.RevisionStatus.NEW.equals(revStatus) || (vobj.getCreationTimestamp().compareTo(since) >= 0)) {
							inserted.add(new EventCalObjectChanged(vobj.getEventId(), vobj.getRevisionTimestamp(), vobj.getHref()));
						} else {
							updated.add(new EventCalObjectChanged(vobj.getEventId(), vobj.getRevisionTimestamp(), vobj.getHref()));
						}
					}
				}
			}
			
			return new CollectionChangeSet<>(inserted, updated, deleted);
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public EventCalObject getEventCalObject(int calendarId, String href) throws WTException {
		List<EventCalObject> ccs = getEventCalObjects(calendarId, Arrays.asList(href));
		return ccs.isEmpty() ? null : ccs.get(0);
	}
	
	public List<EventCalObject> getEventCalObjects(int calendarId, Collection<String> hrefs) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			ArrayList<EventCalObject> items = new ArrayList<>();
			Map<String, List<VEventCalObject>> vobjMap = evtDao.viewCalObjectsByCalendarHrefs(con, calendarId, hrefs);
			for (String href : hrefs) {
				List<VEventCalObject> vobjs = vobjMap.get(href);
				if (vobjs == null) continue;
				if (vobjs.isEmpty()) continue;
				VEventCalObject vobj = vobjs.get(vobjs.size()-1);
				if (vobjs.size() > 1) {
					logger.trace("Many CalObjects ({}) found for same href [{} -> {}]", vobjs.size(), vobj.getHref(), vobj.getEventId());
				}
				
				items.add(doEventCalObjectPrepare(con, vobj));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addEventCalObject(int calendarId, String href, net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		ICalendarInput in = new ICalendarInput(udata.getTimeZone());
		ArrayList<EventInput> eis = in.fromICalendarFile(iCalendar, null);
		if (eis.isEmpty()) throw new WTException("iCalendar object does not contain any events");
		if (eis.size() > 1) throw new WTException("iCalendar object should contain one event");
		EventInput ei = eis.get(0);
		ei.event.setCalendarId(calendarId);
		ei.event.setHref(href);
		
		String rawData = null;
		if (iCalendar != null) {
			String prodId = ICalendarUtils.buildProdId(ManagerUtils.buildProductName());
			try {
				rawData = new ICalendarOutput(prodId).write(iCalendar);
			} catch(IOException ex) {
				throw new WTException(ex, "Error serializing iCalendar");
			}
		}
		
		addEvent(ei.event, rawData, true);
	}
	
	public void updateEventCalObject(int calendarId, String href, net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		int eventId = getEventIdByCategoryHref(calendarId, href, true);
		
		ICalendarInput in = new ICalendarInput(udata.getTimeZone());
		ArrayList<EventInput> eis = in.fromICalendarFile(iCalendar, null);
		if (eis.isEmpty()) throw new WTException("iCalendar object does not contain any events");
		EventInput ei = eis.get(0);
		ei.event.setEventId(eventId);
		ei.event.setCalendarId(calendarId);
		
		updateEvent(ei.event, true);
	}
	
	public void deleteEventCalObject(int calendarId, String href) throws WTException {
		int eventId = getEventIdByCategoryHref(calendarId, href, true);
		deleteEvent(eventId, true);
	}
	
	private int getEventIdByCategoryHref(int calendarId, String href, boolean throwExIfManyMatchesFound) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> ids = evtDao.selectAliveIdsByCalendarHrefs(con, calendarId, href);
			if (ids.isEmpty()) throw new WTNotFoundException("Event not found [{}, {}]", calendarId, href);
			if (throwExIfManyMatchesFound && (ids.size() > 1)) throw new WTException("Many matches for href [{}]", href);
			return ids.get(ids.size()-1);
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarFolderIds, DateTime startDate) throws WTException {
		return listUpcomingEventInstances(calendarFolderIds, startDate, 3, null);
	}
	
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarFolderIds, DateTime startDate, String pattern) throws WTException {
		return listUpcomingEventInstances(calendarFolderIds, startDate, 3, pattern);
	}
	
	public List<SchedEventInstance> listUpcomingEventInstances(Collection<Integer> calendarFolderIds, DateTime startDate, int days, String pattern) throws WTException {
		EventDAO eveDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> okCalendarIds = calendarFolderIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendarFolder(calendarId, "READ"))
					.collect(Collectors.toList());
			
			if (days > 15) days = 15;
			final DateTime toDate = startDate.withTimeAtStartOfDay().plusDays(days);
			
			ArrayList<SchedEventInstance> events = new ArrayList<>();
			for (VVEvent ve : eveDao.viewByCalendarFromToPattern(con, okCalendarIds, startDate, toDate, pattern)) {
				SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), ve);
				item.setKey(EventKey.buildKey(ve.getEventId(), ve.getSeriesEventId()));
				events.add(item);
			}
			for (VVEvent ve : eveDao.viewRecurringByCalendarFromToPattern(con, okCalendarIds, startDate, toDate, pattern)) {
				events.addAll(calculateRecurringInstances(con, new SchedEventInstanceMapper(ve), startDate, toDate, DateTimeZone.UTC, 200));
			}
			
			// Sorts events by their startDate
			Collections.sort(events, new Comparator<SchedEventInstance>() {
				@Override
				public int compare(final SchedEventInstance se1, final SchedEventInstance se2) {
					return se1.getStartDate().compareTo(se2.getStartDate());
				}
			});
			
			return events;
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<FolderEventInstances> listFolderEventInstances(Collection<Integer> calendarFolderIds, String pattern, DateTimeZone userTimezone) throws WTException {
		return listFolderEventInstances(calendarFolderIds, null, null, pattern, userTimezone);
	}
	
	public List<FolderEventInstances> listFolderEventInstances(Collection<Integer> calendarFolderIds, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return listFolderEventInstances(calendarFolderIds, fromDate, toDate, null, userTimezone);
	}
	
	public List<FolderEventInstances> listFolderEventInstances(Collection<Integer> calendarFolderIds, DateTime fromDate, DateTime toDate, String pattern, DateTimeZone userTimezone) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO eveDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			ArrayList<FolderEventInstances> foInstances = new ArrayList<>();
			List<OCalendar> ocals = calDao.selectByDomainIn(con, getTargetProfileId().getDomainId(), calendarFolderIds);
			for (OCalendar ocal : ocals) {
				if (!quietlyCheckRightsOnCalendarFolder(ocal.getCalendarId(), "READ")) continue;
				
				final ArrayList<SchedEventInstance> instances = new ArrayList<>();
				for (VVEvent vevt : eveDao.viewByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, pattern)) {
					SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), vevt);
					item.setKey(EventKey.buildKey(vevt.getEventId(), vevt.getSeriesEventId()));
					instances.add(item);
				}
				for (VVEvent vevt : eveDao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, pattern)) {
					instances.addAll(calculateRecurringInstances(con, new SchedEventInstanceMapper(vevt), fromDate, toDate, userTimezone, 200));
				}
				foInstances.add(new FolderEventInstances(ManagerUtils.createCalendar(ocal), instances));
			}
			return foInstances;
			
		} catch (SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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

			} catch(SQLException | DAOException ex) {
				throw wrapThrowable(ex);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event getEventFromSite(String publicUid) throws WTException {
		Connection con = null;
		
		// TODO: permission check
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			Integer eventId = doEventGetId(con, null, publicUid);
			if (eventId == null) throw new WTException("Event ID lookup failed [{0}]", publicUid);
			
			Event event = doEventGet(con, eventId, false);
			if (event == null) throw new WTException("Event not found [{0}]", eventId);
			return event;
		
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event updateEventFromSite(String publicUid, String attendeeUid, String responseStatus) throws WTException {
		EventAttendeeDAO evtDao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		// TODO: permission check
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			Integer eventId = doEventGetId(con, null, publicUid);
			if (eventId == null) throw new WTException("Event ID lookup failed [{0}]", publicUid);

			int ret = evtDao.updateAttendeeResponseByIdEvent(con, responseStatus, attendeeUid, eventId);
			if (ret == 1) {
				Event event = doEventGet(con, eventId, false);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(int eventId) throws WTException {
		return getEvent(eventId, false);
	}
	
	public Event getEvent(int eventId, boolean forZPushFix) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Event event = doEventGet(con, eventId, forZPushFix);
			checkRightsOnCalendarFolder(event.getCalendarId(), "READ");
			
			return event;
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	public Event getEvent_old_checkIf_doEventGet_works(int eventId, boolean forZPushFix) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OEvent oevt = forZPushFix?edao.selectById(con, eventId):edao.selectAliveById(con, eventId);
			if(oevt == null) return null;
			checkRightsOnCalendarFolder(oevt.getCalendarId(), "READ"); // Rights check!
			
			List<OEventAttendee> oatts = adao.selectByEvent(con, eventId);
			ORecurrence orec = null;
			if (oevt.getRecurrenceId() != null) {
				orec = rdao.select(con, oevt.getRecurrenceId());
				if(orec == null) throw new WTException("Unable to retrieve recurrence [{0}]", oevt.getRecurrenceId());
			}
			
			Event evt = createEvent(oevt);
			evt.setAttendees(createEventAttendeeList(oatts));
			evt.setRecurrenceRule((orec != null) ? orec.getRule() : null);
			//evt.setRecurrence(createEventRecurrence(orec));
			return evt;
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	
	
	@Override
	public Event getEvent(GetEventScope scope, String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doEventGet(con, scope, publicUid);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			InsertResult insert = doEventInsert(con, event, true, true, iCalendarRawData);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
			
			storeAsSuggestion(core, SUGGESTION_EVENT_TITLE, event.getTitle());
			storeAsSuggestion(core, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			//TODO: gestire le notifiche invito per gli eventi ricorrenti
			// Handle attendees invitation
			if (notifyAttendees && !insert.attendees.isEmpty()) { // Checking it here avoids unuseful calls!
				if (insert.recurrence == null) {
					String eventKey = EventKey.buildKey(insert.event.getEventId(), insert.event.getEventId());
					notifyAttendees(Crud.CREATE, getEventInstance(eventKey));
				} else {
					logger.warn("Attendees notification within recurrence is not supported");
				}
			}
			
			Event evt = ManagerUtils.createEvent(insert.event);
			evt.setAttendees(ManagerUtils.createEventAttendeeList(insert.attendees));
			evt.setRecurrenceRule((insert.recurrence != null) ? insert.recurrence.getRule() : null);
			return evt;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event addEventFromICal(int calendarId, net.fortuna.ical4j.model.Calendar ical) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		ArrayList<EventInput> parsed = new ICalendarInput(udata.getTimeZone()).fromICalendarFile(ical, null);
		if (parsed.isEmpty()) throw new WTException("iCal must contain at least one event");
		
		Event event = parsed.get(0).event;
		event.setCalendarId(calendarId);
		return addEvent(event, false);
	}
	
	public void updateEvent(Event event, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "UPDATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			OEvent oevtOrig = evtDao.selectById(con, event.getEventId());
			if (oevtOrig == null) throw new WTException();
			
			doEventUpdate(con, oevtOrig, event, DoOption.UPDATE, true);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));

			// Handle attendees notification
			if (notifyAttendees && event.hasAttendees()) {
				//TODO: create valid dates for eventDump in case of recurring events
				notifyAttendees(Crud.UPDATE, getEvent(oevtOrig.getEventId()));
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
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
			
			if (ical.getMethod().equals(Method.REQUEST)) {
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

				checkRightsOnCalendarElements(evt.getCalendarId(), "UPDATE");

				OEvent original = edao.selectById(con, evt.getEventId());

				// Set into parsed all fields tha can't be changed by the iCal
				// update otherwise data can be lost inside doEventUpdate
				parsedEvent.setEventId(original.getEventId());
				parsedEvent.setCalendarId(original.getCalendarId());
				parsedEvent.setReadOnly(original.getReadOnly());
				parsedEvent.setReminder(original.getReminder());
				parsedEvent.setEtag(original.getEtag());
				parsedEvent.setActivityId(original.getActivityId());
				parsedEvent.setMasterDataId(original.getMasterDataId());
				parsedEvent.setStatMasterDataId(original.getStatMasterDataId());
				parsedEvent.setCausalId(original.getCausalId());

				doEventUpdate(con, original, parsedEvent, true);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
				
			} else if (ical.getMethod().equals(Method.REPLY)) {
				// Attendee -> Organizer
				// The attendee replies to an event sending to the organizer a mail 
				// message with an attached ical (the above param), properly configured.
				// The ical should be kept untouched in the reply except for the 
				// attendee list: it should contains only the references of the 
				// attendee that replies to the invitation.

				Attendee att = ICalendarUtils.getAttendee(ve);
				if (att == null) throw new WTException("Event does not provide any attendees");

				// Extract response info...
				PartStat partStat = (PartStat)att.getParameter(Parameter.PARTSTAT);
				String responseStatus = ICalHelper.partStatToResponseStatus(partStat);

				// Gets the event looking also into incoming calendars...
				// (i can be the organizer of a meeting created for my boss that 
				// share his calendar with me; all received replies must be bringed
				// back to the event in the shared calendar)
				//Event evt = getEvent(uid);
				// Previous impl. forced (forceOriginal == true)
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{0}]", uid);
				
				List<String> updatedAttIds = doEventAttendeeUpdateResponseByRecipient(con, evt, att.getCalAddress().getSchemeSpecificPart(), responseStatus);
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
				
			} else if (ical.getMethod().equals(Method.CANCEL)) {
				// Organizer -> Attendee
				// The organizer after cancelling the event send a mail message
				// to all attendees telling to update their saved information

				// Gets the event...
				//Event evt = getEventForICalUpdate(uid);
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{0}]", uid);
				
				doEventDelete(con, evt.getEventId(), true);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_DELETE", String.valueOf(evt.getEventId()));
				
			} else {
				throw new WTException("Unsupported Calendar's method [{0}]", ical.getMethod().toString());
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean deleteEvent(int eventId, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Event eventDump = doEventGet(con, eventId, false);
			if (eventDump == null) return false;
			
			checkRightsOnCalendarElements(eventDump.getCalendarId(), "DELETE");
			
			String provider = calDao.selectProviderById(con, eventDump.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", eventDump.getCalendarId());
			
			doEventDelete(con, eventId, true);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(eventDump.getEventId()));
			
			// Handle attendees notification
			if (notifyAttendees && eventDump.hasAttendees()) {
				//TODO: create valid dates for eventDump in case of recurring events
				notifyAttendees(Crud.DELETE, eventDump);
			}
			return true;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<String> doEventAttendeeUpdateResponseByRecipient(Connection con, Event event, String recipient, String responseStatus) throws WTException {
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
		int ret = attDao.updateAttendeeResponseByIds(con, responseStatus, matchingIds);
		evtDao.updateRevision(con, event.getEventId(), BaseDAO.createRevisionTimestamp());
		if (matchingIds.size() == ret) {
			return matchingIds;
		} else {
			throw new WTException("# of attendees to update don't match the uptated ones");
		}
	}
	
	/*
	private List<String> updateEventAttendeeResponseByRecipient(Event event, String recipient, String responseStatus) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			// Find matching attendees
			ArrayList<String> matchingIds = new ArrayList<>();
			List<OEventAttendee> atts = attDao.selectByEvent(con, event.getEventId());
			for (OEventAttendee att : atts) {
				final InternetAddress ia = InternetAddressUtils.toInternetAddress(att.getRecipient());
				if (ia == null) continue;
				
				if (StringUtils.equalsIgnoreCase(ia.getAddress(), recipient)) matchingIds.add(att.getAttendeeId());
			}
			
			// Update responses
			int ret = attDao.updateAttendeeResponseByIds(con, responseStatus, matchingIds);
			evtDao.updateRevision(con, event.getEventId(), BaseDAO.createRevisionTimestamp());
			
			if (matchingIds.size() == ret) {
				DbUtils.commitQuietly(con);
				return matchingIds;
			} else {
				DbUtils.rollbackQuietly(con);
				return new ArrayList<>();
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
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
		
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventInstance getEventInstance(String eventKey) throws WTException {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			
			return doEventInstanceGet(con, ekey.eventId, ekey.instanceDate);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventInstance event) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "UPDATE");
			//TODO: controllare se categoryId non Ã¨ readonly (calendario remoto)
			
			OEvent oevtOrig = evtDao.selectById(con, ekey.originalEventId);
			if (oevtOrig == null) throw new WTException("Unable get original event [{}]", ekey.originalEventId);
			
			if (vevt.isEventRecurring()) {
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					// Changes are only for this specific instance
					
					// 1 - Inserts new broken event (attendees and rr are not supported here)
					InsertResult insert = doEventInsert(con, event, false, false, null);
					
					// 2 - Inserts new broken record (marks recurring event) on modified date
					doRecurrenceExcludeDate(con, oevtOrig, ekey.instanceDate, insert.event.getEventId());
					
					// 3 - Updates revision of original event
					evtDao.updateRevision(con, oevtOrig.getEventId(), BaseDAO.createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
					
					//TODO: gestire notifica invitati?
					
				} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
					// Changes are only valid from this instance onward
					
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = recDao.select(con, oevtOrig.getRecurrenceId());
					if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", oevtOrig.getRecurrenceId());
					
					int oldDaysBetween = Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
					Recur oldRecur = orec.getRecur(); // Dump old recur!
					
					// NB: keep UTC here, until date needs to be at midnight in UTC time
					DateTime newUntil = ekey.instanceDate.toDateTimeAtStartOfDay(DateTimeZone.UTC);
					orec.set(newUntil);
					recDao.update(con, orec);
					
					// 2 - Updates revision of original event
					evtDao.updateRevision(con, oevtOrig.getEventId(), BaseDAO.createRevisionTimestamp());
					
					// 3 - Insert new event recalculating start/end from instance date preserving days duration
					event.setStartDate(event.getStartDate().withDate(ekey.instanceDate));
					event.setEndDate(event.getEndDate().withDate(ekey.instanceDate.plusDays(oldDaysBetween)));
					
					if (ICal4jUtils.recurHasCount(oldRecur)) {
						DateTime oldRealUntil = ICal4jUtils.calculateRecurEnd(oldRecur, oevtOrig.getStartDate(), oevtOrig.getEndDate(), oevtOrig.getDateTimezone());
						// NB: keep UTC here, until date needs to be at midnight in UTC time
						DateTime recUntil = oldRealUntil.toLocalDate().plusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC);
						ICal4jUtils.setRecurUntilDate(oldRecur, recUntil);
					}
					event.setRecurrenceRule(oldRecur.toString());
					InsertResult insert = doEventInsert(con, event, true, false, null);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					
					//TODO: gestire notifica invitati?
					
				} else if (UpdateEventTarget.ALL_SERIES.equals(target)) {
					// Changes are valid for all the instances (whole recurrence)
					// We need to restore original dates because current start/end refers
					// to instance and not to the master event.
					event.setStartDate(event.getStartDate().withDate(oevtOrig.getStartDate().toLocalDate()));
					event.setEndDate(event.getEndDate().withDate(oevtOrig.getEndDate().toLocalDate()));
					
					/*
					// 1 - Updates/Deletes recurrence data
					ORecurrence orec = recDao.select(con, oevtOrig.getRecurrenceId());
					if (event.hasRecurrence()) {
						Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
						orec.set(recur, event.getStartDate(), event.getEndDate(), event.getDateTimezone());
						recDao.update(con, orec);
					} else {
						rbkDao.deleteByRecurrence(con, orec.getRecurrenceId());
						recDao.deleteById(con, orec.getRecurrenceId());
						oevtOrig.setRecurrenceId(null);
					}
					*/
					
					// 1 - Updates event with new data
					doEventUpdate(con, oevtOrig, event, DoOption.UPDATE, false);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
					
					//TODO: gestire notifica invitati?
				}
			} else if (vevt.isEventBroken()) {
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = evtDao.selectById(con, ekey.eventId);
					if (oevt == null) throw new WTException("Unable to get broken event [{}]", ekey.eventId);
					doEventUpdate(con, oevt, event, DoOption.SKIP, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.eventId));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEvent(oevtOrig.getEventId()));
				}
			} else {
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					// 1 - Updates event with new data
					doEventUpdate(con, oevtOrig, event, DoOption.UPDATE, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEvent(oevtOrig.getEventId()));
				}
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	@Override
	public void updateEventInstance(String target, EventInstance event) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		//TODO: gestire i suggerimenti (titolo + luogo)
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "UPDATE");
			
			//TODO: controllare se categoryId non è readonly (remoto)
			
			OEvent original = evtDao.selectById(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			//TODO: gestire le notifiche invito per gli eventi ricorrenti
			if (vevt.isRecurring()) {
				if (target.equals(TARGET_THIS)) {
					// 1 - Inserts new broken event
					InsertResult insert = doEventInsert(con, event, false, false);
					// 2 - Marks recurring event date inserting a broken record
					doExcludeRecurrenceDate(con, original, ekey.instanceDate, insert.event.getEventId());
					// 3 - Updates revision of original event
					evtDao.updateRevision(con, original.getEventId(), createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
					
				} else if (target.equals(TARGET_SINCE)) {
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = recDao.select(con, original.getRecurrenceId());
					if(orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					DateTime until = orec.getUntilDate();
					orec.applyEndUntil(ekey.instanceDate.minusDays(1).toDateTimeAtStartOfDay(), DateTimeZone.forID(original.getTimezone()), true);
					recDao.update(con, orec);
					// 2 - Updates revision of original event
					evtDao.updateRevision(con, original.getEventId(), createRevisionTimestamp());
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					// 3 - Insert new event adjusting recurrence a bit
					event.getRecurrence().setEndsMode(EventRecurrence.ENDS_MODE_UNTIL);
					event.getRecurrence().setUntilDate(until);
					InsertResult insert = doEventInsert(con, event, true, false);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
					
				} else if (target.equals(TARGET_ALL)) {
					// 1 - Updates recurring event data (dates must be preserved) (+revision)
					event.setStartDate(event.getStartDate().withDate(original.getStartDate().toLocalDate()));
					event.setEndDate(event.getEndDate().withDate(original.getEndDate().toLocalDate()));
					doEventUpdate(con, original, event, false);
					// 2 - Updates recurrence data
					ORecurrence orec = recDao.select(con, original.getRecurrenceId());
					orec.fillFrom(event.getRecurrence(), original.getStartDate(), original.getEndDate(), original.getTimezone());
					recDao.update(con, orec);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
				}
				
			} else if (vevt.isBroken()) {
				if (target.equals(TARGET_THIS)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = evtDao.selectById(con, ekey.eventId);
					if(oevt == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
					doEventUpdate(con, oevt, event, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.eventId));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEventInstance(event.getKey()));
				}
				
			} else {
				if (target.equals(TARGET_THIS)) {
					// 1 - Updates the event with new data
					doEventUpdate(con, original, event, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEventInstance(event.getKey()));
				}
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public void updateEventInstance(String eventKey, DateTime startDate, DateTime endDate, String title) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "UPDATE");
			
			if (vevt.isEventRecurring()) {
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
				
				// Handle attendees invitation
				if (vevt.getHasAttendees()) notifyAttendees(Crud.UPDATE, getEventInstance(eventKey));
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void cloneEventInstance(String eventKey, DateTime startDate, DateTime endDate) throws WTException {
		Connection con = null;
		
		try {
			Event event = getEventInstance(eventKey);
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			
			event.setStartDate(startDate);
			event.setEndDate(endDate);
			
			con = WT.getConnection(SERVICE_ID, false);
			
			InsertResult insert = doEventInsert(con, event, true, true, null);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEventInstance(UpdateEventTarget target, String eventKey) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "DELETE");
			
			if (vevt.isEventRecurring()) {
				Integer recurrenceId = evtDao.selectRecurrenceId(con, ekey.originalEventId);
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					// Changes are only for this specific instance
					
					// 1 - Inserts new broken record (without new broken event) on deleted date
					doRecurrenceExcludeDate(con, ekey.originalEventId, recurrenceId, ekey.instanceDate, null);
					
					// 2 - updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, BaseDAO.createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
				} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) {
					// Changes are only valid from this instance onward
					
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = recDao.select(con, recurrenceId);
					if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", recurrenceId);
					
					// NB: keep UTC here, until date needs to be at midnight in UTC time
					DateTime newUntil = ekey.instanceDate.toDateTimeAtStartOfDay(DateTimeZone.UTC);
					orec.set(newUntil);
					recDao.update(con, orec);
					
					// 2 - Updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, BaseDAO.createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
				} else if (UpdateEventTarget.ALL_SERIES.equals(target)) {
					// Changes are valid for all the instances (whole recurrence)
					
					// 1 - logically delete original event
					doEventDelete(con, ekey.originalEventId, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.originalEventId));
				}
				
			} else if (vevt.isEventBroken()) {
				Event dump = (vevt.getHasAttendees()) ? getEventInstance(eventKey) : null;
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					// 1 - logically delete newevent (broken one)
					doEventDelete(con, ekey.eventId, true);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, BaseDAO.createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
					// Handle attendees invitation
					if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
				}
				
			} else {
				Event dump = (vevt.getHasAttendees()) ? getEventInstance(eventKey) : null;
				if (UpdateEventTarget.THIS_INSTANCE.equals(target)) {
					if (!ekey.eventId.equals(ekey.originalEventId)) throw new WTException("In this case both ids must be equals");
					doEventDelete(con, ekey.eventId, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.originalEventId));
					
					// Handle attendees invitation
					if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
				}
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void restoreEventInstance(String eventKey) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
		Connection con = null;
		
		try {
			final EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "UPDATE");
			
			if (!vevt.isEventBroken()) throw new WTException("Cannot restore an event that is not broken");
			
			Event dump = null;
			if (vevt.getHasAttendees()) {
				dump = getEventInstance(eventKey); // Gets event for later use
			}
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, ekey.eventId);
			// 2 - logically delete broken event
			doEventDelete(con, ekey.eventId, true);
			// 3 - updates revision of original event
			evtDao.updateRevision(con, ekey.originalEventId, BaseDAO.createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
			writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
			
			// Handle attendees invitation
			if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void moveEventInstance(boolean copy, String eventKey, int targetCalendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			Event evt = getEventInstance(eventKey);
			checkRightsOnCalendarFolder(evt.getCalendarId(), "READ");
			
			if (copy || (targetCalendarId != evt.getCalendarId())) {
				checkRightsOnCalendarElements(targetCalendarId, "CREATE");
				if (!copy) checkRightsOnCalendarElements(evt.getCalendarId(), "DELETE");
				
				doEventMove(con, copy, evt, targetCalendarId);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(evt.getEventId()));
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public LinkedHashSet<String> calculateAvailabilitySpans(int minRange, UserProfileId pid, DateTime fromDate, DateTime toDate, DateTimeZone userTz, boolean busy) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		LinkedHashSet<String> hours = new LinkedHashSet<>();
		Connection con = null;
		
		//TODO: review this method
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired calendars by profile
			final List<VVEventInstance> veis = new ArrayList<>();
			for (OCalendar ocal : calDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
				for (VVEvent ve : evtDao.viewByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null)) {
					veis.add(new VVEventInstance(ve));
				}
				for (VVEvent ve : evtDao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null)) {
					veis.add(new VVEventInstance(ve));
				}
			}
			
			DateTime startDt, endDt;
			for (VVEventInstance vei : veis) {
				if (vei.getBusy() != busy) continue; // Ignore events that are not marked as busy!
				
				if (vei.getRecurrenceId() == null) {
					startDt = vei.getStartDate().withZone(userTz);
					endDt = vei.getEndDate().withZone(userTz);
					hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
				} else {
					final List<VVEventInstance> instances = calculateRecurringInstances(con, new VVEventInstanceMapper(vei), fromDate, toDate, userTz);
					for (VVEventInstance instance : instances) {
						startDt = instance.getStartDate().withZone(userTz);
						endDt = instance.getEndDate().withZone(userTz);
						hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
					}
				}
			}
		
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return hours;
	}
	
	public ArrayList<String> generateTimeSpans(int minRange, LocalDate fromDate, LocalDate toDate, LocalTime fromTime, LocalTime toTime, DateTimeZone tz) {
		ArrayList<String> hours = new ArrayList<>();
		DateTimeFormatter ymdhmZoneFmt = DateTimeUtils.createYmdHmFormatter(tz);
		
		DateTime instant = new DateTime(tz).withDate(fromDate).withTime(fromTime).withSecondOfMinute(0).withMillisOfSecond(0);
		DateTime boundaryInstant = new DateTime(tz).withDate(toDate).withTime(toTime).withSecondOfMinute(0).withMillisOfSecond(0);
		while(instant.compareTo(boundaryInstant) < 0) {
			hours.add(ymdhmZoneFmt.print(instant));
			instant = instant.plusMinutes(minRange);
		}
		
		return hours;
	}
	
	public List<EventAttendee> listEventAttendees(int eventId, boolean notifiedOnly) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return getEventAttendees(con, eventId, notifiedOnly);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
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
			checkRightsOnCalendarElements(calendarId, "CREATE");
			if(mode.equals("copy")) checkRightsOnCalendarElements(calendarId, "DELETE");
			
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
				int del = doEventDeleteByCalendar(con, calendarId, false);
				log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s deleted!", del));
				DbUtils.commitQuietly(con);
			}
			
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Importing..."));
			int count = 0;
			for(EventInput ei : input) {
				ei.event.setCalendarId(calendarId);
				try {
					doEventInputInsert(con, uidMap, ei);
					DbUtils.commitQuietly(con);
					count++;
				} catch(Exception ex) {
					logger.trace("Error inserting event", ex);
					DbUtils.rollbackQuietly(con);
					log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", ei.event.getTitle(), ei.event.getPublicUid(), ex.getMessage()));
				}
			}
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s imported!", count));
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Ended at {0}", new DateTime()));
		}
		return log;
	}
	
	public void exportEvents(LogEntries log, DateTime fromDate, DateTime toDate, OutputStream os) throws Exception {
		Connection con = null, ccon = null;
		ICsvMapWriter mapw = null;
		UserDAO userDao = UserDAO.getInstance();
		CalendarDAO calDao = CalendarDAO.getInstance();
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
			
			HashMap<String, Object> map = null;
			for (OCalendar ocal : calDao.selectByDomain(con, getTargetProfileId().getDomainId())) {
				final UserProfileId pid = new UserProfileId(ocal.getDomainId(), ocal.getUserId());
				final OUser user = userDao.selectByDomainUser(ccon, ocal.getDomainId(), ocal.getUserId());
				if (user == null) throw new WTException("User [{0}] not found", pid.toString());
				final UserProfile.Data udata = WT.getUserData(pid);
				
				for (VVEvent ve : edao.viewByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null)) {
					final VVEventInstance vei = new VVEventInstance(ve);
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
						fillExportMapBasic(map, coreMgr, con, vei);
						fillExportMapDates(map, udata.getTimeZone(), vei);
						mapw.write(map, headers, processors);
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Event skipped [{0}]. Reason: {1}", ve.getEventId(), ex.getMessage()));
					}
				}
				for(VVEvent ve : edao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null)) {
					final List<VVEventInstance> instances = calculateRecurringInstances(con, new VVEventInstanceMapper(ve), fromDate, toDate, udata.getTimeZone());
					
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
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
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		Connection con = null;
		
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			UserProfileId pid = getTargetProfileId();
			
			// Erase events and related tables
			if (deep) {
				for (OCalendar ocal : caldao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					icaDao.deleteByCalendar(con, ocal.getCalendarId());
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
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapThrowable(ex);
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw t;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		EventDAO edao = EventDAO.getInstance();
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			logger.debug("Getting expired events");
			final DateTime from = now.withTimeAtStartOfDay();
			for (SchedEventInstance event : doEventGetExpiredForUpdate(con, from, from.plusDays(7), DateTimeZone.UTC)) {
				//TODO: implementare gestione reminder anche per le ricorrenze
				final DateTime remindOn = event.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(event.getReminder());
				if (now.compareTo(remindOn) >= 0) {
					if (!byEmailCache.containsKey(event.getCalendarProfileId())) {
					CalendarUserSettings cus = new CalendarUserSettings(SERVICE_ID, event.getCalendarProfileId());
						boolean bool = cus.getEventReminderDelivery().equals(CalendarSettings.EVENT_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(event.getCalendarProfileId(), bool);
					}
					
					int ret = edao.updateRemindedOnIfNull(con, event.getEventId(), now);
					if (ret != 1) continue;
					
					if (byEmailCache.get(event.getCalendarProfileId())) {
						UserProfile.Data ud = WT.getUserData(event.getCalendarProfileId());
						CoreUserSettings cus = new CoreUserSettings(event.getCalendarProfileId());
						
						try {
							EventInstance eventInstance = getEventInstance(event.getKey());
							alerts.add(createEventReminderAlertEmail(ud.getLocale(), cus.getShortDateFormat(), cus.getShortTimeFormat(), ud.getPersonalEmailAddress(), event.getCalendarProfileId(), eventInstance));
						} catch(WTException ex1) {
							logger.warn("Unable to create reminder alert body", ex1);
						}	
					} else {
						alerts.add(createEventReminderAlertWeb(event));
					}
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error collecting reminder alerts", t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		sendInvitationForZPushEvents();
		
		return alerts;
	}

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
					notifyAttendees(calendar.getProfileId(), crud, event);
				}
				processed.add(oevt.getEventId());
			}
			
			evtDao.updateHandleInvitationIn(con, processed, false);
			DbUtils.commitQuietly(con);
			
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error collecting reminder alerts", t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ProbeCalendarRemoteUrlResult probeCalendarRemoteUrl(Calendar.Provider provider, URI url, String username, String password) throws WTException {
		
		if (!Calendar.isProviderRemote(provider)) {
			throw new WTException("Provider is not valid or is not remote [{}]", EnumUtils.toSerializedName(provider));
		}
		
		try {
			if (Calendar.Provider.WEBCAL.equals(provider)) {
				URIBuilder builder = new URIBuilder(url);
				if (StringUtils.equalsIgnoreCase(builder.getScheme(), "webcal")) {
					builder.setScheme("http"); // Force http scheme
				}
				if (!StringUtils.isBlank(username) && !StringUtils.isBlank(username)) {
					builder.setUserInfo(username, password);
					logger.debug("Checking remote calendar URL with provided credentials [{}, {}]", url.toString(), username);
				} else {
					logger.debug("Checking remote calendar URL [{}]", url.toString());
				}
				
				HttpClient httpCli = null;
				try {
					httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), url);
					return HttpClientUtils.exists(httpCli, url) ? new ProbeCalendarRemoteUrlResult(FilenameUtils.getBaseName(url.getPath())) : null;
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
	
	public void syncRemoteCalendar(int calendarId) throws WTException {
		syncRemoteCalendar(calendarId, false);
	}
	
	public void syncRemoteCalendar(int calendarId, boolean full) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		final ICalendarInput icalInput = new ICalendarInput(udata.getTimeZone());
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			//checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID, false);
			Calendar cal = ManagerUtils.createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) throw new WTException("Calendar not found [{0}]", calendarId);
			if (!Calendar.Provider.WEBCAL.equals(cal.getProvider()) && !Calendar.Provider.CALDAV.equals(cal.getProvider())) {
				throw new WTException("Specified calendar is not remote (webcal or CalDAV) [{0}]", calendarId);
			}
			
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
					tempFile = WT.createTempFile(PREFIX, null);
					
					// Retrieve webcal content (iCalendar) from the specified URL 
					// and save it locally
					logger.debug("Retrieving iCalendar file from URL [{}]", newUrl);
					HttpClient httpCli = null;
					FileOutputStream os = null;
					try {
						httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), newUrl);
						os = new FileOutputStream(tempFile);
						HttpClientUtils.get(httpCli, newUrl, os);
						
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
						ICalendarUtils.relaxParsingAndCompatibility();
						ical = ICalendarUtils.parse(is);
						//TODO: add support to FILENAME property (Google https://github.com/ical4j/ical4j/issues/69)
					} catch(IOException | ParserException ex) {
						throw new WTException(ex, "Unable to read webcal");
					} finally {
						IOUtils.closeQuietly(os);
					}

					// Convert data to internal event model
					logger.debug("Converting data to internal model");
					ArrayList<EventInput> input = icalInput.fromICalendarFile(ical, null);
					logger.debug("Converted {} events", input.size());

					// Inserts data...
					logger.debug("Inserting events...");
					try {
						doEventDeleteByCalendar(con, calendarId, false);
						HashMap<String, OEvent> cache = new HashMap<>();
						for(EventInput ei : input) {
							if (logger.isTraceEnabled()) logger.trace("{}", ei.event.toString());
							ei.event.setCalendarId(calendarId);
							doEventInputInsert(con, cache, ei);
						}
						cache.clear();
						DbUtils.commitQuietly(con);

					} catch(Throwable t) {
						DbUtils.rollbackQuietly(con);
						throw new WTException(t, "Error importing iCalendar");
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
					final String savedSyncToken = params.syncToken;
					
					if (!syncIsSupported || (syncIsSupported && StringUtils.isBlank(savedSyncToken)) || full) { // Full update
						// If supported, saves last sync-token issued
						if (syncIsSupported) {
							params.syncToken = dcal.getSyncToken();
						}
						
						// Retrieves events list from DAV endpoint
						logger.debug("Retrieving whole list [{}]", params.url.toString());
						List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString());
						logger.debug("Endpoint returns {} items", devts.size());
						
						// Inserts data...
						try {
							logger.debug("Processing results...");
							// Define a simple map in order to check duplicates.
							// eg. SOGo passes same card twice :(
							HashSet<String> hrefs = new HashSet<>();
							doEventDeleteByCalendar(con, calendarId, false);
							HashMap<String, OEvent> cache = new HashMap<>();
							for(DavCalendarEvent devt : devts) {
								if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
								if (hrefs.contains(devt.getPath())) {
									logger.trace("iCal duplicated. Skipped! [{}]", devt.getPath());
									continue;
								}
								
								final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
								if (input.size() != 1) throw new WTException("iCal must contain one event");
								final EventInput ei = input.get(0);
								
								ei.event.setCalendarId(calendarId);
								ei.event.setHref(devt.getPath());
								ei.event.setEtag(devt.geteTag());
								doEventInputInsert(con, cache, ei);
								hrefs.add(devt.getPath());
							}
							cache.clear();
							hrefs.clear();
							
							calDao.updateParametersById(con, calendarId, LangUtils.serialize(params, CalendarRemoteParameters.class));
							DbUtils.commitQuietly(con);
							
						} catch(Throwable t) {
							DbUtils.rollbackQuietly(con);
							throw new WTException(t, "Error importing iCalendar");
						}
						
					} else { // Partial update
						params.syncToken = dcal.getSyncToken();
						
						EventDAO evtDao = EventDAO.getInstance();
						Map<String, List<Integer>> eventIdsByHref = evtDao.selectHrefsByByCalendar(con, calendarId);
						
						logger.debug("Retrieving changes [{}, {}]", params.url.toString(), savedSyncToken);
						List<DavSyncStatus> changes = dav.getCalendarChanges(params.url.toString(), savedSyncToken);
						logger.debug("Endpoint returns {} items", changes.size());
						
						try {
							if (!changes.isEmpty()) {
								// Process changes...
								logger.debug("Processing changes...");
								HashSet<String> hrefs = new HashSet<>();
								for (DavSyncStatus change : changes) {
									if (DavUtil.HTTP_SC_TEXT_OK.equals(change.getResponseStatus())) {
										hrefs.add(change.getPath());

									} else { // Event deleted
										List<Integer> eventIds = eventIdsByHref.get(change.getPath());
										Integer eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;
										if (eventId == null) {
											logger.warn("Deletion not possible. Event path not found [{}]", change.getPath());
											continue;
										}
										doEventDelete(con, eventId, false);
									}
								}

								// Retrieves events list from DAV endpoint (using multiget)
								logger.debug("Retrieving inserted/updated events [{}]", hrefs.size());
								List<DavCalendarEvent> devts = dav.listCalendarEvents(params.url.toString(), hrefs);

								// Inserts/Updates data...
								logger.debug("Inserting/Updating events...");
								HashMap<String, OEvent> cache = new HashMap<>();
								for(DavCalendarEvent devt : devts) {
									if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
									List<Integer> eventIds = eventIdsByHref.get(devt.getPath());
									Integer eventId = (eventIds != null) ? eventIds.get(eventIds.size()-1) : null;

									final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
									if (input.size() != 1) throw new WTException("iCal must contain one event");
									final EventInput ei = input.get(0);

									if (eventId != null) {
										doEventDelete(con, eventId, false);
									}

									ei.event.setCalendarId(calendarId);
									ei.event.setHref(devt.getPath());
									ei.event.setEtag(devt.geteTag());
									doEventInputInsert(con, cache, ei);
								}
								cache.clear();
							}
							
							calDao.updateParametersById(con, calendarId, LangUtils.serialize(params, CalendarRemoteParameters.class));
							DbUtils.commitQuietly(con);
							
						} catch(Throwable t) {
							DbUtils.rollbackQuietly(con);
							throw new WTException(t, "Error importing iCalendar");
						}
					}
					
				} catch(DavException ex) {
					throw new WTException(ex, "CalDAV error");
				}
			}
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private <T> List<T> calculateRecurringInstances(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return calculateRecurringInstances(con, instanceMapper, fromDate, toDate, userTimezone, -1);
	}
	
	private <T> List<T> calculateRecurringInstances(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone, int limit) throws WTException {
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		ArrayList<T> instances = new ArrayList<>();
		
		int eventId = instanceMapper.getEventId();
		DateTime eventStartDate = instanceMapper.getEventStartDate();
		DateTime eventEndDate = instanceMapper.getEventEndDate();
		
		// Retrieves reccurence and broken dates (if any)
		ORecurrence orec = recDao.selectByEvent(con, eventId);
		if (orec == null) {
			logger.warn("Unable to retrieve recurrence for event [{}]", eventId);
			
		} else {
			List<ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, orec.getRecurrenceId());
			
			if (fromDate == null) fromDate = orec.getStartDate();
			if (toDate == null) toDate = orec.getStartDate().plusYears(5);
			
			//TODO: ritornare direttamente l'hashmap da jooq
			// Builds a hashset of broken dates for increasing performances
			HashMap<String, ORecurrenceBroken> brokenDates = new HashMap<>();
			for (ORecurrenceBroken obrec : obrecs) {
				brokenDates.put(obrec.getEventDate().toString(), obrec);
			}
			
			try {
				// Calculate event length in order to generate events like original one
				int eventDays = ManagerUtils.calculateEventLengthInDays(eventStartDate, eventEndDate);
				RRule rr = new RRule(orec.getRule());

				// Calcutate recurrence set for required dates range
				PeriodList periods = ICal4jUtils.calculateRecurrenceSet(eventStartDate, eventEndDate, orec.getStartDate(), rr, fromDate, toDate, userTimezone);

				// Recurrence start is useful to skip undesired dates at beginning.
				// If event does not starts at recurrence real beginning (eg. event
				// start on MO but first recurrence begin on WE), ical4j lib includes 
				// those dates in calculated recurrence set, as stated in RFC 
				// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
				LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(eventStartDate, rr.getRecur(), userTimezone).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
				LocalDate rrEnd = orec.getUntilDate().toLocalDate();

				// Iterates returned recurring periods and builds cloned events...
				int count = -1;
				for (net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
					count++;
					if ((limit != -1) && (count > limit)) break;
					final LocalDate perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
					final LocalDate perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();

					if (brokenDates.containsKey(perStart.toString())) continue; // Skip broken dates...
					if ((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
						final DateTime newStart = eventStartDate.withDate(perStart);
						final DateTime newEnd = eventEndDate.withDate(newStart.plusDays(eventDays).toLocalDate());
						final String key = EventKey.buildKey(eventId, eventId, perStart);
						
						instances.add(instanceMapper.createInstance(key, newStart, newEnd));
					}
				}

			} catch(DAOException ex) {
				throw wrapThrowable(ex);
			} catch(ParseException ex) {
				throw new WTException(ex, "Unable to parse rrule");
			}
		}
		return instances;
	}
	
	
	
	/*
	private List<VVEventInstance> calculateRecurringInstances(Connection con, VVEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return calculateRecurringInstances(con, event, fromDate, toDate, userTimezone, -1);
	}
	
	private List<VVEventInstance> calculateRecurringInstances(Connection con, VVEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone, int limit) throws WTException {
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO recbDao = RecurrenceBrokenDAO.getInstance();
		
		if (event.getRecurrenceId() == null) throw new WTException("Specified event [{}] does not have a recurrence set", event.getEventId());
		
		// Retrieves reccurence and broken dates (if any)
		ORecurrence orec = recDao.select(con, event.getRecurrenceId());
		if (orec == null) throw new WTException("Unable to retrieve recurrence [{}]", event.getRecurrenceId());
		List<ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, event.getEventId(), event.getRecurrenceId());
		
		if (fromDate == null) fromDate = orec.getStartDate();
		if (toDate == null) toDate = orec.getStartDate().plusYears(5);
		
		//TODO: ritornare direttamente l'hashmap da jooq
		// Builds a hashset of broken dates for increasing performances
		HashMap<String, ORecurrenceBroken> brokenDates = new HashMap<>();
		for(ORecurrenceBroken obrec : obrecs) {
			brokenDates.put(obrec.getEventDate().toString(), obrec);
		}
		
		// If not present, updates rrule
		if (StringUtils.isBlank(orec.getRule())) {
			orec.setRule(orec.buildRRule(DateTimeZone.UTC).getValue());
			recDao.updateRRule(con, orec.getRecurrenceId(), orec.getRule());
		}
		
		try {
			ArrayList<VVEventInstance> instances = new ArrayList<>();
			
			// Calculate event length in order to generate events like original one
			int eventDays = calculateEventLengthInDays(event);
			RRule rr = new RRule(orec.getRule());
			
			// Calcutate recurrence set for required dates range
			PeriodList periods = ICal4jUtils.calculateRecurrenceSet(event.getStartDate(), event.getEndDate(), orec.getStartDate(), rr, fromDate, toDate, userTimezone);
			
			// Recurrence start is useful to skip undesired dates at beginning.
			// If event does not starts at recurrence real beginning (eg. event
			// start on MO but first recurrence begin on WE), ical4j lib includes 
			// those dates in calculated recurrence set, as stated in RFC 
			// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
			LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), userTimezone).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
			LocalDate rrEnd = orec.getUntilDate().toLocalDate();
			
			// Iterates returned recurring periods and builds cloned events...
			int count = -1;
			final Cloner cloner = Cloner.standard();
			for (net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				count++;
				if ((limit != -1) && (count > limit)) break;
				final LocalDate perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
				final LocalDate perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();
				
				if (brokenDates.containsKey(perStart.toString())) continue; // Skip broken dates...
				if ((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
					final DateTime newStart = event.getStartDate().withDate(perStart);
					final DateTime newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					
					// Generate cloned event like original one
					VVEvent clone = cloner.deepClone(event);
					clone.setStartDate(newStart);
					clone.setEndDate(newEnd);
					instances.add(new VVEventInstance(EventKey.buildKey(event.getEventId(), event.getEventId(), perStart), clone));	
				}
			}
			return instances;
			
		} catch(DAOException ex) {
			throw wrapThrowable(ex);
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to parse rrule");
		}
	}
	*/
	
	private void notifyOrganizer(UserProfileId senderProfileId, Event event, String updatedAttendeeId) {
		CoreUserSettings cus = new CoreUserSettings(senderProfileId);
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
			
			InternetAddress from = WT.getNotificationAddress(senderProfileId.getDomainId());
			InternetAddress to = InternetAddressUtils.toInternetAddress(event.getOrganizer());
			if (!InternetAddressUtils.isAddressValid(to)) throw new WTException("Organizer address not valid [{0}]", event.getOrganizer());
			
			String servicePublicUrl = WT.getServicePublicUrl(senderProfileId.getDomainId(), SERVICE_ID);
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String subject = TplHelper.buildResponseUpdateEmailSubject(locale, event, targetAttendee);
			String customBody = TplHelper.buildResponseUpdateBodyTpl(locale, dateFormat, timeFormat, event, servicePublicUrl);
			
			String html = TplHelper.buildResponseUpdateTpl(locale, source, event.getTitle(), customBody, targetAttendee);
			WT.sendEmail(getMailSession(), true, from, to, subject, html);
			
		} catch(Exception ex) {
			logger.warn("Unable to notify organizer", ex);
		}	
	}
	
	private void notifyAttendees(String crud, Event event) {
		notifyAttendees(getTargetProfileId(), crud, event);
	}
	
	private void notifyAttendees(UserProfileId senderProfileId, String crud, Event event) {
		try {
			// Finds attendees to be notified...
			ArrayList<EventAttendee> toBeNotified = new ArrayList<>();
			for(EventAttendee attendee : event.getAttendees()) {
				if (attendee.getNotify()) toBeNotified.add(attendee);
			}
			
			if (!toBeNotified.isEmpty()) {
				UserProfile.Data ud = WT.getUserData(senderProfileId);
				CoreUserSettings cus = new CoreUserSettings(senderProfileId);
				String dateFormat = cus.getShortDateFormat();
				String timeFormat = cus.getShortTimeFormat();
				
				String prodId = ICalendarUtils.buildProdId(ManagerUtils.buildProductName());
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
				for(EventAttendee attendee : toBeNotified) {
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
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGetOwner(con, calendarId);
			
		} catch(SQLException | DAOException ex) {
			throw wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private UserProfileId doCalendarGetOwner(Connection con, int calendarId) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Owner owner = calDao.selectOwnerById(con, calendarId);
		return (owner == null) ? null : new UserProfileId(owner.getDomainId(), owner.getUserId());
	}
	
	private Calendar doCalendarUpdate(boolean insert, Connection con, Calendar cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		
		OCalendar ocal = ManagerUtils.createOCalendar(cal);
		if (insert) {
			ocal.setCalendarId(calDao.getSequence(con).intValue());
		}
		fillOCalendarWithDefaults(ocal);
		if (ocal.getIsDefault()) calDao.resetIsDefaultByProfile(con, ocal.getDomainId(), ocal.getUserId());
		if (insert) {
			calDao.insert(con, ocal);
		} else {
			calDao.update(con, ocal);
		}
		return ManagerUtils.createCalendar(ocal);
	}
	
	private EventCalObject doEventCalObjectPrepare(Connection con, VEventCalObject vobj) throws WTException {
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		
		Event event = ManagerUtils.fillEvent(new Event(), vobj);
		
		if (vobj.getRecurrenceId() != null) {
			ORecurrence orec = recDao.select(con, vobj.getRecurrenceId());
			if (orec == null) throw new WTException("Unable to get recurrence [{}]", vobj.getRecurrenceId());
			event.setRecurrenceRule(orec.getRule());
		}
		if (vobj.getHasAttendees()) {
			List<OEventAttendee> oatts = attDao.selectByEvent(con, event.getEventId());
			event.setAttendees(ManagerUtils.createEventAttendeeList(oatts));
		}
		
		String prodId = ICalendarUtils.buildProdId(ManagerUtils.buildProductName());
		ICalendarOutput out = new ICalendarOutput(prodId);
		net.fortuna.ical4j.model.Calendar iCal = out.toCalendar(event);
		if (vobj.getHasIcalendar()) {
			//TODO: in order to be fully compliant, merge generated vcard with the original one in db table!
		}
		
		String raw = null;
		try {
			raw = out.write(iCal);
		} catch(IOException ex) {
			throw new WTException(ex, "Unable to write iCalendar");
		}
		
		EventCalObject eco = ManagerUtils.fillEventCalObject(new EventCalObject(), event);
		eco.setSize(raw.getBytes().length);
		eco.setIcalendar(raw);
		
		return eco;
	}
	
	private Integer doEventGetId(Connection con, Collection<Integer> calendarIdMustBeIn, String publicUid) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		List<Integer> ids = null;
		if ((calendarIdMustBeIn == null) || calendarIdMustBeIn.isEmpty()) {
			// This kind of lookup is suitable for calls executed by admin from public service
			ids = evtDao.selectAliveIdsByPublicUid(con, publicUid);
			
		} else {
			logger.debug("Looking for publicId in restricted set of calendars...");
			ids = evtDao.selectAliveIdsByCalendarsPublicUid(con, calendarIdMustBeIn, publicUid);
		}
		if (ids.isEmpty()) return null;
		if (ids.size() > 1) logger.warn("Multiple events found for public id [{}]", publicUid);
		return ids.get(0);
	}
	
	private Event doEventGet(Connection con, int eventId, boolean forZPushFix) throws DAOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		OEvent oevt = forZPushFix ? evtDao.selectById(con, eventId) : evtDao.selectAliveById(con, eventId);
		if (oevt == null) return null;
		
		Event evt = ManagerUtils.createEvent(oevt);
		
		if (oevt.getRecurrenceId() != null) {
			ORecurrence orec = recDao.select(con, oevt.getRecurrenceId());
			if (orec == null) throw new WTException("Unable to get recurrence [{}]", oevt.getRecurrenceId());
			evt.setRecurrenceRule(orec.getRule());
		}
		
		List<OEventAttendee> oatts = attDao.selectByEvent(con, eventId);
		if (!oatts.isEmpty()) {
			evt.setAttendees(ManagerUtils.createEventAttendeeList(oatts));
		}
		
		return evt;
	}
	
	private Event doEventGet(Connection con, GetEventScope scope, String publicUid) throws WTException {
		ArrayList<Integer> ids = new ArrayList<>();
		if (scope.equals(GetEventScope.PERSONAL) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			Integer eventId = doEventGetId(con, listCalendarIds(), publicUid);
			if (eventId != null) ids.add(eventId);
		}
		if (scope.equals(GetEventScope.INCOMING) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
			Integer eventId = doEventGetId(con, listIncomingCalendarIds(), publicUid);
			if (eventId != null) ids.add(eventId);
		}
		
		if (ids.isEmpty()) return null;
		if (ids.size() > 1) throw new WTException("Multiple events found for public id [{}]", publicUid);
		return doEventGet(con, ids.get(0), false);
	}
	
	private EventInstance doEventInstanceGet(Connection con, int eventId, LocalDate date) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO eattDao = EventAttendeeDAO.getInstance();
		
		OEvent oevt = evtDao.selectById(con, eventId);
		if (oevt == null) throw new WTException("Unable to get event [{}]", eventId);
		
		EventInstance ei = ManagerUtils.fillEvent(new EventInstance(), oevt);
		
		// Fill recurrence (if necessary)
		ORecurrence orec = recDao.select(con, oevt.getRecurrenceId());
		if (orec != null) {
			if (date == null) throw new WTException("Date is required for recurring events [{}]", eventId);
			
			int eventDays = ManagerUtils.calculateEventLengthInDays(oevt.getStartDate(), oevt.getEndDate());
			ei.setStartDate(ei.getStartDate().withDate(date));
			ei.setEndDate(ei.getEndDate().withDate(ei.getStartDate().plusDays(eventDays).toLocalDate()));
			ei.setRecurrenceRule(orec.getRule());
			
			ei.setKey(EventKey.buildKey(eventId, eventId, date));
			ei.setRecurInfo(Event.RecurInfo.RECURRING);
			
		} else {
			Integer seriesEventId = evtDao.selectAliveSeriesEventIdById(con, eventId);
			ei.setKey(EventKey.buildKey(eventId, seriesEventId));
			ei.setRecurInfo((seriesEventId == null) ? Event.RecurInfo.NONE : Event.RecurInfo.BROKEN);
		}
		
		// Fill attendees
		List<OEventAttendee> attendees = eattDao.selectByEvent(con, eventId);
		ei.setAttendees(ManagerUtils.createEventAttendeeList(attendees));
		
		return ei;
	}
	
	private InsertResult doEventInputInsert(Connection con, HashMap<String, OEvent> cache, EventInput ei) throws DAOException {
		InsertResult insert = doEventInsert(con, ei.event, true, true, null);
		if (insert.recurrence != null) {
			// Cache recurring event for future use within broken references 
			cache.put(insert.event.getPublicUid(), insert.event);
			// If present, adds excluded dates as broken instances
			if (ei.excludedDates != null) {
				for(LocalDate date : ei.excludedDates) {
					doRecurrenceExcludeDate(con, insert.event, date);
				}
			}
		} else {
			if (ei.overwritesRecurringInstance != null) {
				if (cache.containsKey(insert.event.getPublicUid())) {
					final OEvent oevt = cache.get(insert.event.getPublicUid());
					doRecurrenceExcludeDate(con, oevt, ei.overwritesRecurringInstance, insert.event.getEventId());
				}
			}
		}
		return insert;
	}
	
	private InsertResult doEventInsert(Connection con, Event event, boolean insertRecurrence, boolean insertAttendees, String rawICalendar) {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		OEvent oevt = ManagerUtils.createOEvent(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		oevt.setRevisionStatus(EnumUtils.toSerializedName(Event.RevisionStatus.NEW));
		fillOEventWithDefaults(oevt);
		oevt.ensureCoherence();
		
		ArrayList<OEventAttendee> oatts = new ArrayList<>();
		if (insertAttendees && event.hasAttendees()) {
			for(EventAttendee att : event.getAttendees()) {
				final OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(oevt.getEventId());
				attDao.insert(con, oatt);
				oatts.add(oatt);
			}
		}
		
		ORecurrence orec = null;
		if (insertRecurrence && event.hasRecurrence()) {
			/*
			orec = new ORecurrence();
			orec.fillFrom(event.getRecurrence(), oevt.getStartDate(), oevt.getEndDate(), oevt.getTimezone());
			orec.setRecurrenceId(recDao.getSequence(con).intValue());
			recDao.insert(con, orec);
			*/
			Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
			if (recur != null) {
				orec = ManagerUtils.createORecurrence(recur, event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
				orec.setRecurrenceId(recDao.getSequence(con).intValue());
				recDao.insert(con, orec);
			}
		}
		
		if (!StringUtils.isBlank(rawICalendar)) {
			doEventICalendarInsert(con, oevt.getEventId(), rawICalendar);
		}
		
		oevt.setRecurrenceId((orec != null) ? orec.getRecurrenceId() : null);
		evtDao.insert(con, oevt, revision);
		return new InsertResult(oevt, orec, oatts);
	}
	
	private OEvent doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean attendees) throws WTException {
		return doEventUpdate(con, originalEvent, event, DoOption.SKIP, attendees);
	}
	
	private OEvent doEventUpdate(Connection con, OEvent originalEvent, Event event, DoOption recurrence, boolean attendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		if (StringUtils.isBlank(event.getOrganizer())) {
			event.setOrganizer(ManagerUtils.buildOrganizer(getTargetProfileId())); // Make sure organizer is filled
		}
		ManagerUtils.fillOEvent(originalEvent, event);
		originalEvent.ensureCoherence();
		
		if (attendees) {
			List<EventAttendee> fromList = ManagerUtils.createEventAttendeeList(attDao.selectByEvent(con, originalEvent.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(fromList, event.getAttendees());
			
			for(EventAttendee att : changeSet.inserted) {
				final OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(originalEvent.getEventId());
				attDao.insert(con, oatt);
			}
			for(EventAttendee att : changeSet.updated) {
				final OEventAttendee oatt = ManagerUtils.createOEventAttendee(att);
				attDao.update(con, oatt);
			}
			for(EventAttendee att : changeSet.deleted) {
				attDao.delete(con, att.getAttendeeId());
			}
		}
		
		if (DoOption.UPDATE.equals(recurrence)) {
			ORecurrence orec = recDao.select(con, originalEvent.getRecurrenceId());
			if (event.hasRecurrence() && (orec != null)) {
				Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
				orec.set(recur, event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
				recDao.update(con, orec);
				
			} else if (event.hasRecurrence() && (orec == null)) {
				Recur recur = ICal4jUtils.parseRRule(event.getRecurrenceRule());
				orec = ManagerUtils.createORecurrence(recur, event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
				orec.setRecurrenceId(recDao.getSequence(con).intValue());
				recDao.insert(con, orec);
				originalEvent.setRecurrenceId(orec.getRecurrenceId());
				
			} else if (!event.hasRecurrence() && (orec != null)) {
				rbkDao.deleteByRecurrence(con, orec.getRecurrenceId());
				recDao.deleteById(con, orec.getRecurrenceId());
				originalEvent.setRecurrenceId(null);
			}
		}
		
		evtDao.update(con, originalEvent, revision, originalEvent.getStartDate().isAfterNow());
		return originalEvent;
	}
	
	private boolean doEventICalendarInsert(Connection con, int eventId, String rawICalendar) throws DAOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		
		OEventICalendar ovca = new OEventICalendar();
		ovca.setEventId(eventId);
		ovca.setRawData(rawICalendar);
		return icaDao.insert(con, ovca) == 1;
	}
	
	private boolean doEventICalendarDelete(Connection con, int eventId) throws DAOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		return icaDao.deleteById(con, eventId) == 1;
	}
	
	private int doEventDelete(Connection con, int eventId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteById(con, eventId, BaseDAO.createRevisionTimestamp());
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			
			recDao.deleteByEvent(con, eventId);
			recbkDao.deleteByEvent(con, eventId);
			attDao.deleteByEvent(con, eventId);
			doEventICalendarDelete(con, eventId);
			return evtDao.deleteById(con, eventId);
		}
	}
	
	private int doEventDeleteByCalendar(Connection con, int calendarId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteByCalendar(con, calendarId, BaseDAO.createRevisionTimestamp());
			
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			
			attDao.deleteByCalendar(con, calendarId);
			recbkDao.deleteByCalendar(con, calendarId);
			recDao.deleteByCalendar(con, calendarId);
			return evtDao.deleteByCalendar(con, calendarId);
		}
	}
	
	private void doEventMove(Connection con, boolean copy, Event event, int targetCalendarId) throws DAOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		
		if (copy) {
			event.setCalendarId(targetCalendarId);
			OEventICalendar oica = icaDao.selectById(con, event.getEventId());
			String rawICalendar = (oica != null) ? oica.getRawData() : null;
			doEventInsert(con, event, true, true, rawICalendar);
			
		} else {
			EventDAO evtDao = EventDAO.getInstance();
			evtDao.updateCalendar(con, event.getEventId(), targetCalendarId, BaseDAO.createRevisionTimestamp());
		}
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, OEvent recurringEvent, LocalDate instanceDate) throws DAOException {
		return doRecurrenceExcludeDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, null);
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, OEvent recurringEvent, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		return doRecurrenceExcludeDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, brokenEventId);
	}
	
	private ORecurrenceBroken doRecurrenceExcludeDate(Connection con, int recurringEventId, int recurrenceId, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		RecurrenceBrokenDAO broDao = RecurrenceBrokenDAO.getInstance();
		// 1 - inserts a broken record on excluded date
		ORecurrenceBroken orb  = new ORecurrenceBroken();
		orb.setEventId(recurringEventId);
		orb.setRecurrenceId(recurrenceId);
		orb.setEventDate(instanceDate);
		orb.setNewEventId(brokenEventId);
		broDao.insert(con, orb);
		return orb;
	}
	
	private List<SchedEventInstance> doEventGetExpiredForUpdate(Connection con, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		EventDAO eveDao = EventDAO.getInstance();
		//TODO: gestire alert su eventi ricorrenti
		final ArrayList<SchedEventInstance> instances = new ArrayList<>();
		for (VVEvent vevt : eveDao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), vevt);
			item.setKey(EventKey.buildKey(vevt.getEventId(), vevt.getSeriesEventId()));
			instances.add(item);
		}
		/*
		for (VSchedulerEvent vevt : eveDao.viewRecurringExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			instances.addAll(calculateRecurringInstances(con, createSchedEvent(vevt), fromDate, toDate, DateTimeZone.UTC, 200));
		}
		*/
		return instances;
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
			if (tgt.getBusy() == null) tgt.setBusy(false);
			if (tgt.getInvitation() == null) tgt.setInvitation(false);
			
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
				tgt.setPublicUid(ManagerUtils.buildEventUid(tgt.getEventId(), WT.getDomainInternetName(getTargetProfileId().getDomainId())));
			}
			tgt.setHref(ManagerUtils.buildHref(tgt.getPublicUid()));
			if (tgt.getReadOnly() == null) tgt.setReadOnly(false);
			if (StringUtils.isBlank(tgt.getOrganizer())) tgt.setOrganizer(ManagerUtils.buildOrganizer(getTargetProfileId()));
		}
		return tgt;
	}
	
	private VVEventInstance cloneEvent(VVEventInstance sourceEvent, DateTime newStart, DateTime newEnd) {
		VVEventInstance event = new VVEventInstance(sourceEvent);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private void checkRightsOnCalendarRoot(UserProfileId owner, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if (RunContext.isWebTopAdmin()) return;
		if (owner.equals(targetPid)) return;
		
		String shareId = shareCache.getShareRootIdByOwner(owner);
		if (shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", owner);
		CoreManager core = WT.getCoreManager(targetPid);
		if (core.isShareRootPermitted(shareId, action)) return;
		//if (core.isShareRootPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private boolean quietlyCheckRightsOnCalendarFolder(int calendarId, String action) {
		try {
			checkRightsOnCalendarFolder(calendarId, action);
			return true;
		} catch(AuthException ex1) {
			return false;
		} catch(WTException ex1) {
			logger.warn("Unable to check rights [{}]", calendarId);
			return false;
		}
	}
	
	private void checkRightsOnCalendarFolder(int calendarId, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		
		// Skip rights check if running user is resource's owner
		UserProfileId owner = ownerCache.get(calendarId);
		if (owner == null) throw new WTException("calendarToOwner({0}) -> null", calendarId);
		if (owner.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = shareCache.getWildcardShareFolderIdByOwner(owner);
		if (wildcardShareId != null) {
			if (core.isShareFolderPermitted(wildcardShareId, action)) return;
			//if (core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = shareCache.getShareFolderIdByFolderId(calendarId);
		if (shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if (core.isShareFolderPermitted(shareId, action)) return;
		//if (core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folder share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private void checkRightsOnCalendarElements(int calendarId, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		
		// Skip rights check if running user is resource's owner
		UserProfileId owner = ownerCache.get(calendarId);
		if (owner == null) throw new WTException("calendarToOwner({0}) -> null", calendarId);
		if (owner.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = shareCache.getWildcardShareFolderIdByOwner(owner);
		if (wildcardShareId != null) {
			if (core.isShareElementsPermitted(wildcardShareId, action)) return;
			//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = shareCache.getShareFolderIdByFolderId(calendarId);
		if (shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if (core.isShareElementsPermitted(shareId, action)) return;
		//if (core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on elements share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private ReminderInApp createEventReminderAlertWeb(SchedEventInstance event) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		alert.setTitle(event.getTitle());
		alert.setDate(event.getStartDate().withZone(event.getDateTimeZone()));
		alert.setTimezone(event.getTimezone());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, String dateFormat, String timeFormat, String recipientEmail, UserProfileId ownerId, EventInstance event) throws WTException {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, ownerId, "event", event.getKey());
		
		String subject = TplHelper.buildEventReminderEmailSubject(locale, dateFormat, timeFormat, event);
		alert.setSubject(subject);
		
		try {
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String because = lookupResource(locale, CalendarLocale.EMAIL_REMINDER_FOOTER_BECAUSE);
			String customBody = TplHelper.buildEventReminderBodyTpl(locale, dateFormat, timeFormat, event);
			String body = TplHelper.buildInvitationTpl(locale, source, recipientEmail, event.getTitle(), customBody, because, null);
			alert.setBody(body);
			
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
		
		public SchedEventInstanceMapper(VVEvent event) {
			this.event = event;
		}
		
		@Override
		public int getEventId() {
			return event.getEventId();
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
		public SchedEventInstance createInstance(String key, DateTime startDate, DateTime endDate) {
			SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), event);
			item.setKey(key);
			item.setStartDate(startDate);
			item.setEndDate(endDate);
			return item;
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
		public DateTime getEventStartDate() {
			return event.getStartDate();
		}

		@Override
		public DateTime getEventEndDate() {
			return event.getEndDate();
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
		public DateTime getEventStartDate();
		public DateTime getEventEndDate();
		public T createInstance(String key, DateTime startDate, DateTime endDate);
	}
	
	public static class InsertResult {
		OEvent event;
		ORecurrence recurrence;
		ArrayList<OEventAttendee> attendees;
		
		public InsertResult(OEvent event, ORecurrence recurrence, ArrayList<OEventAttendee> attendees) {
			this.event = event;
			this.recurrence = recurrence;
			this.attendees = attendees;
			
		}
	}
	
	public static class ProbeCalendarRemoteUrlResult {
		public final String displayName;
		
		public ProbeCalendarRemoteUrlResult(String displayName) {
			this.displayName = displayName;
		}
	}
	
	private class OwnerCache extends AbstractMapCache<Integer, UserProfileId> {

		@Override
		protected void internalInitCache() {}

		@Override
		protected void internalMissKey(Integer key) {
			try {
				UserProfileId owner = doCalendarGetOwner(key);
				if (owner == null) throw new WTException("Owner not found [{0}]", key);
				put(key, owner);
			} catch(WTException ex) {
				throw new WTRuntimeException(ex.getMessage());
			}
		}
	}
	
	private class ShareCache extends AbstractShareCache<Integer, ShareRootCalendar> {

		@Override
		protected void internalInitCache() {
			final CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
			try {
				for (ShareRootCalendar root : internalListIncomingCalendarShareRoots()) {
					shareRoots.add(root);
					ownerToShareRoot.put(root.getOwnerProfileId(), root);
					for (OShare folder : coreMgr.listIncomingShareFolders(root.getShareId(), GROUPNAME_CALENDAR)) {
						if (folder.hasWildcard()) {
							final UserProfileId ownerPid = coreMgr.userUidToProfileId(folder.getUserUid());
							ownerToWildcardShareFolder.put(ownerPid, folder.getShareId().toString());
							for (Calendar calendar : listCalendars(ownerPid).values()) {
								folderTo.add(calendar.getCalendarId());
								rootShareToFolderShare.put(root.getShareId(), calendar.getCalendarId());
								folderToWildcardShareFolder.put(calendar.getCalendarId(), folder.getShareId().toString());
							}
						} else {
							int categoryId = Integer.valueOf(folder.getInstance());
							folderTo.add(categoryId);
							rootShareToFolderShare.put(root.getShareId(), categoryId);
							folderToShareFolder.put(categoryId, folder.getShareId().toString());
						}
					}
				}
				ready = true;
			} catch(WTException ex) {
				throw new WTRuntimeException(ex.getMessage());
			}
		}
	}
}
