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
import com.sonicle.commons.MailUtils;
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
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventInstance.RecurringInfo;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.model.EventRecurrence;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.CalendarPropsDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
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
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.FolderEvents;
import com.sonicle.webtop.calendar.model.SchedEvent;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.util.ICalendarInput;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.sdk.AbstractMapCache;
import com.sonicle.webtop.core.sdk.AbstractShareCache;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import org.apache.commons.codec.digest.DigestUtils;
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
	
	private String getProductName() {
		return WT.getPlatformName() + " Calendar";
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
		ArrayList<Integer> ids = new ArrayList<>();
		for (Calendar calendar : listCalendars()) {
			ids.add(calendar.getCalendarId());
		}
		return ids;
	}
	
	@Override
	public List<Integer> listIncomingCalendarIds() throws WTException {
		ArrayList<Integer> ids = new ArrayList<>();
		for (ShareRootCalendar root : listIncomingCalendarRoots()) {
			ids.addAll(listIncomingCalendarFolders(root.getShareId()).keySet());
		}
		return ids;
	}
	
	@Override
	public List<Calendar> listCalendars() throws WTException {
		return listCalendars(getTargetProfileId());
	}
	
	private List<Calendar> listCalendars(UserProfileId pid) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		ArrayList<Calendar> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCalendar ocal : calDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
				//checkRightsOnCalendarFolder(ocal.getCalendarId(), "READ");
				items.add(createCalendar(ocal));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID);
			return createCalendar(calDao.selectById(con, calendarId));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
			
			return createCalendar(ocal);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
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
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar updateCalendar(Calendar calendar) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendar.getCalendarId(), "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar = doCalendarUpdate(false, con, calendar);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_UPDATE", String.valueOf(calendar.getCalendarId()));
			
			return calendar;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public boolean deleteCalendar(int calendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		CalendarPropsDAO psetDao = CalendarPropsDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "DELETE");
			
			// Retrieve sharing status (for later)
			String sharingId = buildSharingId(calendarId);
			Sharing sharing = getSharing(sharingId);
			
			con = WT.getConnection(SERVICE_ID, false);
			Calendar cal = createCalendar(calDao.selectById(con, calendarId));
			if (cal == null) return false;
			
			int ret = calDao.deleteById(con, calendarId);
			psetDao.deleteByCalendar(con, calendarId);
			doDeleteEventsByCalendar(con, calendarId, !cal.isRemoteProvider());
			
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
			
			return ret == 1;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			throw ex;
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
			return (opset == null) ? new CalendarPropSet() : createCalendarPropSet(opset);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
				psets.put(categoryId, (opset == null) ? new CalendarPropSet() : createCalendarPropSet(opset));
			}
			return psets;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
			OCalendarPropSet opset = createOCalendarPropSet(propertySet);
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
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DateTime> listEventDates(Collection<Integer> calendarFolderIds, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO eveDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			HashSet<DateTime> dates = new HashSet<>();
			List<OCalendar> ocals = calDao.selectByDomainIn(con, getTargetProfileId().getDomainId(), calendarFolderIds);
			for (OCalendar ocal : ocals) {
				if (!quietlyCheckRightsOnCalendarFolder(ocal.getCalendarId(), "READ")) continue;
				
				final List<VVEvent> vevts1 = eveDao.viewByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null);
				for (VVEvent vevt : vevts1) {
					addExpandedEventDates(dates, vevt);
				}
				final List<VVEvent> vevts2 = eveDao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, null);
				for (VVEvent vevt : vevts2) {
					final List<SchedEventInstance> instances = calculateRecurringInstances(con, createSchedEvent(vevt), fromDate, toDate, userTimezone, 200);
					for (SchedEventInstance instance : instances) {
						addExpandedEventDates(dates, instance);
					}
				}
			}
			return new ArrayList<>(dates);
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<FolderEvents> listFolderEvents(Collection<Integer> calendarFolderIds, String pattern) throws WTException {
		return listFolderEvents(calendarFolderIds, null, null, pattern);
	}
	
	public List<FolderEvents> listFolderEvents(Collection<Integer> calendarFolderIds, DateTime fromDate, DateTime toDate) throws WTException {
		return listFolderEvents(calendarFolderIds, fromDate, toDate, null);
	}
	
	public List<FolderEvents> listFolderEvents(Collection<Integer> calendarFolderIds, DateTime fromDate, DateTime toDate, String pattern) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO eveDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			ArrayList<FolderEvents> foEvents = new ArrayList<>();
			List<OCalendar> ocals = calDao.selectByDomainIn(con, getTargetProfileId().getDomainId(), calendarFolderIds);
			for (OCalendar ocal : ocals) {
				if (!quietlyCheckRightsOnCalendarFolder(ocal.getCalendarId(), "READ")) continue;
				
				final ArrayList<SchedEvent> events = new ArrayList<>();
				final List<VVEvent> vevts1 = eveDao.viewByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, pattern);
				for (VVEvent vevt : vevts1) {
					events.add(createSchedEvent(vevt));
				}
				final List<VVEvent> vevts2 = eveDao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, pattern);
				for (VVEvent vevt : vevts2) {
					events.add(createSchedEvent(vevt));
				}
				foEvents.add(new FolderEvents(createCalendar(ocal), events));
			}
			return foEvents;
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
			
			ArrayList<Integer> validIds = new ArrayList<>();
			for (Integer catId : calendarFolderIds) {
				if (!quietlyCheckRightsOnCalendarFolder(catId, "READ")) continue;
				validIds.add(catId);
			}
			
			if (days > 15) days = 15;
			final DateTime toDate = startDate.withTimeAtStartOfDay().plusDays(days);
			
			ArrayList<SchedEventInstance> events = new ArrayList<>();
			for (VVEvent ve : eveDao.viewByCalendarFromToPattern(con, validIds, startDate, toDate, pattern)) {
				events.add(new SchedEventInstance(createSchedEvent(ve)));
			}
			for (VVEvent ve : eveDao.viewRecurringByCalendarFromToPattern(con, validIds, startDate, toDate, pattern)) {
				events.addAll(calculateRecurringInstances(con, createSchedEvent(ve), startDate, toDate, DateTimeZone.UTC, 200));
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
			throw new WTException(ex, "DB error");
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
					instances.add(new SchedEventInstance(createSchedEvent(vevt)));
				}
				for (VVEvent vevt : eveDao.viewRecurringByCalendarFromToPattern(con, ocal.getCalendarId(), fromDate, toDate, pattern)) {
					instances.addAll(calculateRecurringInstances(con, createSchedEvent(vevt), fromDate, toDate, userTimezone, 200));
				}
				foInstances.add(new FolderEventInstances(createCalendar(ocal), instances));
			}
			return foInstances;
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedEventInstance> toInstances(SchedEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		if (event.isRecurring()) {
			Connection con = null;
			try {
				con = WT.getConnection(SERVICE_ID);
				return calculateRecurringInstances(con, event, fromDate, toDate, userTimezone, 200);

			} catch(SQLException | DAOException ex) {
				throw new WTException(ex, "DB error");
			} finally {
				DbUtils.closeQuietly(con);
			}
		} else {
			return Arrays.asList(new SchedEventInstance(event));
		}
	}
	
	private VVEventInstance getSchedulerEventByUid(Connection con, String eventPublicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VVEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		return new VVEventInstance(ve);
	}
	
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
					if (!forceOriginal || publicUid.equals(buildEventUid(timeBasedPart, id, internetName))) {
						return id;
					}
				}
				
			} else {
				if (scope.equals(GetEventScope.PERSONAL) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
					ids = edao.selectAliveIdsByCalendarsPublicUid(con, listCalendarIds(), publicUid);
					for(Integer id : ids) {
						if (!forceOriginal || publicUid.equals(buildEventUid(timeBasedPart, id, internetName))) {
							return id;
						}
					}
				}
				if (scope.equals(GetEventScope.INCOMING) || scope.equals(GetEventScope.PERSONAL_AND_INCOMING)) {
					ids = edao.selectAliveIdsByCalendarsPublicUid(con, listIncomingCalendarIds(), publicUid);
					for(Integer id : ids) {
						if (!forceOriginal || publicUid.equals(buildEventUid(timeBasedPart, id, internetName))) {
							return id;
						}
					}
				}
			}	
			
			return null;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String eventKeyByPublicUid(String eventPublicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			VVEventInstance ve = getSchedulerEventByUid(con, eventPublicUid);
			return (ve == null) ? null : EventKey.buildKey(ve.getEventId(), ve.getOriginalEventId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(int eventId) throws WTException {
		return getEvent(eventId,false);
	}
	
	public Event getEvent(int eventId, boolean forZPushFix) throws WTException {
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
			evt.setRecurrence(createEventRecurrence(orec));
			return evt;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(GetEventScope scope, boolean forceOriginal, String publicUid) throws WTException {
		Integer eventId = getEventId(scope, forceOriginal, publicUid);
		return (eventId == null) ? null : getEvent(eventId);
	}
	
	@Override
	public Event addEvent(Event event) throws WTException {
		return addEvent(event, true);
	}
	
	@Override
	public Event addEvent(Event event, boolean notifyAttendees) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			Calendar cal = createCalendar(calDao.selectById(con, event.getCalendarId()));
			if (cal == null) throw new WTException("Calendar not found [{}]", event.getCalendarId());
			if (cal.isRemoteProvider()) throw new WTException("Calendar is read only");
			
			InsertResult insert = doEventInsert(con, event, true, true);
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
			
			Event evt = createEvent(insert.event);
			evt.setAttendees(createEventAttendeeList(insert.attendees));
			evt.setRecurrence(createEventRecurrence(insert.recurrence));
			return evt;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event addEventFromICal(int calendarId, net.fortuna.ical4j.model.Calendar ical) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		ArrayList<EventInput> parsed = new ICalendarInput(udata.getTimeZone()).fromICalendarFile(ical, null);
		if (parsed.size() > 1) throw new WTException("iCal must contain at least one event");
		
		Event event = parsed.get(0).event;
		event.setCalendarId(calendarId);
		return addEvent(event, false);
	}
	
	/*
	public void updateEventFromICalReply(net.fortuna.ical4j.model.Calendar ical) throws WTException {
		VEvent ve = ICalendarUtils.getVEvent(ical);
		if (ve == null) throw new WTException("Calendar does not contain any event");
		
		if (ical.getMethod().equals(Method.REPLY)) {
			UserProfile.Data ud = WT.getUserData(getTargetProfileId());
			
			// Attendee -> Organizer
			// The attendee replies to an event sending to the organizer a mail 
			// message with an attached ical (the above param), properly configured.
			// The ical should be kept untouched in the reply except for the 
			// attendee list: it should contains only the references of the 
			// attendee that replies to the invitation.
			
			String uid = ve.getUid().getValue();
			if (ve == null) throw new WTException("Calendar does not contain any event");
			
			// Gets the event looking also into incoming calendars...
			// (i can be the organizer of a meeting created for my boss that 
			// share his calendar with me; all received replies must be bringed
			// back to the event in the shared calendar)
			//Event evt = getEvent(uid);
			Event evt = getEvent(GetEventScope.PERSONAL_AND_INCOMING, true, uid);
			if (evt == null) {
				logger.debug("Event not found [{}]", uid);
				return;
			}
			
			boolean iAmOrganizer = StringUtils.equalsIgnoreCase(evt.getOrganizerAddress(), ud.getEmailAddress());
			if (!iAmOrganizer) {
				logger.debug("Only the organizer can update the reply [{}]", evt.getOrganizerAddress());
				return;
			}
			
			Attendee att = ICalendarUtils.getAttendee(ve);
			if (att == null) throw new WTException("Event does not provide any attendees");
			
			// Extract response info...
			PartStat partStat = (PartStat)att.getParameter(Parameter.PARTSTAT);
			String responseStatus = ICalHelper.partStatToResponseStatus(partStat);
			
			List<String> updatedAttIds = updateEventAttendeeResponseByRecipient(evt, att.getCalAddress().getSchemeSpecificPart(), responseStatus);
			if (!updatedAttIds.isEmpty()) {
				evt = getEvent(evt.getEventId());
				for(String attId : updatedAttIds) {
					notifyOrganizer(getLocale(), evt, attId);
				}
			}
			
		} else {
			throw new WTException("Unsupported Calendar's method [{0}]", ical.getMethod().toString());
		}
	}
	*/
	
	@Override
	public void updateEventFromICal(net.fortuna.ical4j.model.Calendar ical) throws WTException {
		Connection con = null;
		
		VEvent ve = ICalendarUtils.getVEvent(ical);
		if (ve == null) throw new WTException("Calendar does not contain any event");
		
		String uid = ICalendarUtils.getUidValue(ve);
		if (StringUtils.isBlank(uid)) throw new WTException("Event does not provide a valid Uid");
		
		if (ical.getMethod().equals(Method.REQUEST)) {
			// Organizer -> Attendee
			// The organizer after updating the event details send a mail message
			// to all attendees telling to update their saved information
			
			// Gets the event...
			//Event evt = getEventForICalUpdate(uid);
			Event evt = getEvent(GetEventScope.PERSONAL_AND_INCOMING, false, uid);
			if (evt == null) throw new WTException("Event not found [{0}]", uid);
			
			EventDAO edao = EventDAO.getInstance();
			final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
			
			// Parse the ical using the code used in import
			ArrayList<EventInput> parsed = new ICalendarInput(udata.getTimeZone()).fromICalendarFile(ical, null);
			Event parsedEvent = parsed.get(0).event;
			parsedEvent.setCalendarId(evt.getCalendarId());
			
			try {
				checkRightsOnCalendarElements(evt.getCalendarId(), "UPDATE");
				
				con = WT.getConnection(SERVICE_ID, false);
				OEvent original = edao.selectById(con, evt.getEventId());
				
				// Set into parsed all fields tha can't be changed by the iCal
				// update otherwise data can be lost inside doUpdateEvent
				parsedEvent.setEventId(original.getEventId());
				parsedEvent.setCalendarId(original.getCalendarId());
				parsedEvent.setReadOnly(original.getReadOnly());
				parsedEvent.setReminder(original.getReminder());
				parsedEvent.setHref(original.getHref());
				parsedEvent.setEtag(original.getEtag());
				parsedEvent.setActivityId(original.getActivityId());
				parsedEvent.setMasterDataId(original.getMasterDataId());
				parsedEvent.setStatMasterDataId(original.getStatMasterDataId());
				parsedEvent.setCausalId(original.getCausalId());
				
				doEventUpdate(con, original, parsedEvent, true);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));

			} catch(SQLException | DAOException ex) {
				DbUtils.rollbackQuietly(con);
				throw new WTException(ex, "DB error");
			} finally {
				DbUtils.closeQuietly(con);
			}
			
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
			Event evt = getEvent(GetEventScope.PERSONAL_AND_INCOMING, true, uid);
			if (evt == null) throw new WTException("Event not found [{0}]", uid);
			
			List<String> updatedAttIds = updateEventAttendeeResponseByRecipient(evt, att.getCalAddress().getSchemeSpecificPart(), responseStatus);
			
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
			Event evt = getEvent(GetEventScope.PERSONAL_AND_INCOMING, false, uid);
			if (evt == null) throw new WTException("Event not found [{0}]", uid);
			
			try {
				con = WT.getConnection(SERVICE_ID);
				doDeleteEvent(con, evt.getEventId(), true);

			} catch(SQLException | DAOException ex) {
				throw new WTException(ex, "DB error");
			} finally {
				DbUtils.closeQuietly(con);
			}
			
		} else {
			throw new WTException("Unsupported Calendar's method [{0}]", ical.getMethod().toString());
		}
	}
	
	public Event updateEventFromSite(String eventPublicUid, String attendeeUid, String responseStatus) throws WTException {
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			Event evt = getEvent(GetEventScope.ALL, true, eventPublicUid);
			if (evt == null) throw new WTException("Event not found [{0}]", eventPublicUid);
			
			con = WT.getConnection(SERVICE_ID);
			
			int ret = eadao.updateAttendeeResponseByIdEvent(con, responseStatus, attendeeUid, evt.getEventId());
			if(ret == 1) {
				evt = getEvent(evt.getEventId());
				notifyOrganizer(getLocale(), evt, attendeeUid);
				return evt;
			} else {
				return null;
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
			evtDao.updateRevision(con, event.getEventId(), createRevisionTimestamp());
			
			if (matchingIds.size() == ret) {
				DbUtils.commitQuietly(con);
				return matchingIds;
			} else {
				DbUtils.rollbackQuietly(con);
				return new ArrayList<>();
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
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
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventInstance getEventInstance(String eventKey) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO eattDao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarFolder(vevt.getCalendarId(), "READ");
			
			EventInstance ei = null;
			if (vevt.isRecurring()) {
				ORecurrence orec = recDao.select(con, vevt.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to retrieve recurrence [{0}]", ekey.originalEventId);
				
				ei = createEventInstance(eventKey, RecurringInfo.RECURRING, vevt);
				int eventDays = calculateEventLengthInDays(vevt);
				ei.setStartDate(ei.getStartDate().withDate(ekey.instanceDate));
				ei.setEndDate(ei.getEndDate().withDate(ei.getStartDate().plusDays(eventDays).toLocalDate()));
				ei.setRecurrence(createEventRecurrence(orec));
				
			} else {
				//TODO: se broken, recuperare il record ricorrenza per verifica?
				ei = createEventInstance(eventKey, vevt.isBroken() ? RecurringInfo.BROKEN : RecurringInfo.NONE, vevt);
				ei.setAttendees(createEventAttendeeList(eattDao.selectByEvent(con, vevt.getEventId())));
			}
			
			return ei;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
			
			if (vevt.isRecurring()) {
				throw new WTException("Not supported on recurring events [{}]", ekey.eventId);
			} else {
				// 1 - Updates event's dates/times (+revision)
				OEvent oevt = evtDao.selectById(con, ekey.eventId);
				if (oevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
				
				oevt.setStartDate(startDate);
				oevt.setEndDate(endDate);
				oevt.setTitle(title);
				oevt.ensureCoherence();
				evtDao.update(con, oevt, createRevisionTimestamp());
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(oevt.getEventId()));
				
				// Handle attendees invitation
				if (vevt.getHasAttendees()) notifyAttendees(Crud.UPDATE, getEventInstance(eventKey));
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
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
			
			InsertResult insert = doEventInsert(con, event, true, true);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteEventInstance(String target, String eventKey) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			VVEvent vevt = evtDao.viewById(con, ekey.eventId);
			if (vevt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			checkRightsOnCalendarElements(vevt.getCalendarId(), "DELETE");
			
			Event dump = null;
			if (vevt.getHasAttendees()) {
				dump = getEventInstance(eventKey); // Gets event for later use
			}
			
			if (vevt.isRecurring()) {
				Integer recurrenceId = evtDao.selectRecurrenceId(con, ekey.originalEventId);
				if (target.equals(TARGET_THIS)) {
					// 1 - inserts a broken record (without new event) on deleted date
					doExcludeRecurrenceDate(con, ekey.originalEventId, recurrenceId, ekey.instanceDate, null);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence orec = recdao.select(con, recurrenceId);
					if (orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", recurrenceId);
					
					orec.setUntilDate(ekey.instanceDate.toDateTimeAtStartOfDay().minusDays(1));
					orec.updateRRule(DateTimeZone.forID(vevt.getTimezone()));
					recdao.update(con, orec);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - logically delete original event
					doDeleteEvent(con, ekey.originalEventId, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.originalEventId));
				}
				
			} else if (vevt.isBroken()) {
				if (target.equals(TARGET_THIS)) {
					// 1 - logically delete newevent (broken one)
					doDeleteEvent(con, ekey.eventId, true);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, ekey.originalEventId, createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
					writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
					
					// Handle attendees invitation
					if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
				}
			} else {
				if (target.equals(TARGET_THIS)) {
					if (!ekey.eventId.equals(ekey.originalEventId)) throw new WTException("In this case both ids must be equals");
					doDeleteEvent(con, ekey.eventId, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.originalEventId));
					
					// Handle attendees invitation
					if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
				}
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
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
			
			if (!vevt.isBroken()) throw new WTException("Cannot restore an event that is not broken");
			
			Event dump = null;
			if (vevt.getHasAttendees()) {
				dump = getEventInstance(eventKey); // Gets event for later use
			}
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, ekey.eventId);
			// 2 - logically delete broken event
			doDeleteEvent(con, ekey.eventId, true);
			// 3 - updates revision of original event
			evtDao.updateRevision(con, ekey.originalEventId, createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
			writeLog("EVENT_UPDATE", String.valueOf(ekey.originalEventId));
			
			// Handle attendees invitation
			if (vevt.getHasAttendees()) notifyAttendees(Crud.DELETE, dump);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
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
				
				doMoveEvent(con, copy, evt, targetCalendarId);
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(evt.getEventId()));
			}
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public LinkedHashSet<String> calculateAvailabilitySpans(int minRange, UserProfileId pid, DateTime fromDate, DateTime toDate, DateTimeZone userTz, boolean busy) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		LinkedHashSet<String> hours = new LinkedHashSet<>();
		Connection con = null;
		
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
					final List<VVEventInstance> instances = calculateRecurringInstances(con, vei, fromDate, toDate, userTz);
					for (VVEventInstance instance : instances) {
						startDt = instance.getStartDate().withZone(userTz);
						endDt = instance.getEndDate().withZone(userTz);
						hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
					}
				}
			}
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
	
	public List<EventAttendee> listEventAttendees(Integer eventId, boolean notifiedOnly) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return getEventAttendees(con, eventId, notifiedOnly);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<EventAttendee> getEventAttendees(Connection con, Integer eventId, boolean notifiedOnly) throws WTException {
		List<OEventAttendee> attendees = null;
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		if(notifiedOnly) {
			attendees = eadao.selectByEventNotify(con, eventId, true);
		} else {
			attendees = eadao.selectByEvent(con, eventId);
		}
		return createEventAttendeeList(attendees);
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
				int del = doDeleteEventsByCalendar(con, calendarId, false);
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
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(WTException ex) {
			throw ex;
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
					final List<VVEventInstance> instances = calculateRecurringInstances(con, ve, fromDate, toDate, udata.getTimeZone());
					
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
				DateTime revTs = createRevisionTimestamp();
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
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
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
			for (SchedEventInstance event : doGetExpiredEventsForUpdate(con, from, from.plusDays(7), DateTimeZone.UTC)) {
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
						alerts.add(createEventReminderAlertEmail(ud.getLocale(), event));
					} else {
						alerts.add(createEventReminderAlertWeb(event));
					}
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error collecting reminder alerts", ex);
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
				String crud = null;
				if (OEvent.REV_STATUS_NEW.equals(oevt.getRevisionStatus())) {
					crud = Crud.CREATE;
				} else if (OEvent.REV_STATUS_MODIFIED.equals(oevt.getRevisionStatus())) {
					crud = Crud.UPDATE;
				} else if (OEvent.REV_STATUS_DELETED.equals(oevt.getRevisionStatus())) {
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
		
		if (!Calendar.Provider.WEBCAL.equals(provider) && !Calendar.Provider.CALDAV.equals(provider)) {
			throw new WTException("Provider is not valid or is not remote [{0}]", EnumUtils.toSerializedName(provider));
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
			Calendar cal = createCalendar(calDao.selectById(con, calendarId));
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
						ical = ICalendarUtils.parseAllRelaxed(is);
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
						doDeleteEventsByCalendar(con, calendarId, false);
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
							doDeleteEventsByCalendar(con, calendarId, false);
							HashMap<String, OEvent> cache = new HashMap<>();
							for(DavCalendarEvent devt : devts) {
								if (logger.isTraceEnabled()) logger.trace("{}", ICalendarUtils.print(ICalendarUtils.getVEvent(devt.getCalendar())));
								if (hrefs.contains(devt.getPath())) {
									logger.trace("iCal duplicated. Skipped! [{}]", devt.getPath());
									continue;
								}
								
								final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
								if (input.size() != 1) throw new WTException("Unexpected size");
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
						Map<String, Integer> eventIdsByHref = evtDao.selectHrefsByByCalendar(con, calendarId);
						
						logger.debug("Retrieving changes [{}, {}]", params.url.toString(), savedSyncToken);
						List<DavSyncStatus> changes = dav.getCalendarChanges(params.url.toString(), savedSyncToken);
						logger.debug("Endpoint returns {} items", changes.size());
						
						try {
							if (!changes.isEmpty()) {
								// Process changes...
								logger.debug("Processing changes...");
								HashSet<String> hrefs = new HashSet<>();
								for(DavSyncStatus change : changes) {
									if (DavUtil.HTTP_SC_TEXT_OK.equals(change.getResponseStatus())) {
										hrefs.add(change.getPath());

									} else { // Event deleted
										final Integer eventId = eventIdsByHref.get(change.getPath());

										if (eventId == null) throw new WTException("Event path not found [{0}]", change.getPath());
										doDeleteEvent(con, eventId, false);
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
									final Integer eventId = eventIdsByHref.get(devt.getPath());

									final ArrayList<EventInput> input = icalInput.fromICalendarFile(devt.getCalendar(), null);
									if (input.size() != 1) throw new WTException("Unexpected size");
									final EventInput ei = input.get(0);

									if (eventId != null) {
										doDeleteEvent(con, eventId, false);
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
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<SchedEventInstance> calculateRecurringInstances(Connection con, SchedEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return calculateRecurringInstances(con, event, fromDate, toDate, userTimezone, -1);
	}
	
	private List<SchedEventInstance> calculateRecurringInstances(Connection con, SchedEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone, int limit) throws WTException {
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
			ArrayList<SchedEventInstance> instances = new ArrayList<>();
			
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
					SchedEvent clone = Cloner.standard().deepClone(event);
					clone.setStartDate(newStart);
					clone.setEndDate(newEnd);
					instances.add(new SchedEventInstance(EventKey.buildKey(event.getEventId(), event.getEventId(), perStart), clone));	
				}
			}
			return instances;
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to parse rrule");
		}
	}
	
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
			throw new WTException(ex, "DB error");
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to parse rrule");
		}
	}
	
	private void notifyOrganizer(Locale locale, Event event, String updatedAttendeeId) {
		String targetDomainId = getTargetProfileId().getDomainId();
		CoreUserSettings cus = new CoreUserSettings(getTargetProfileId());
		String dateFormat = cus.getShortDateFormat();
		String timeFormat = cus.getShortTimeFormat();
		
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
			
			InternetAddress from = WT.getNotificationAddress(targetDomainId);
			InternetAddress to = InternetAddressUtils.toInternetAddress(event.getOrganizer());
			if (!InternetAddressUtils.isAddressValid(to)) throw new WTException("Organizer address not valid [{0}]", event.getOrganizer());
			
			String servicePublicUrl = WT.getServicePublicUrl(targetDomainId, SERVICE_ID);
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
				Session session=getMailSession();
				String dateFormat = cus.getShortDateFormat();
				String timeFormat = cus.getShortTimeFormat();
				
				String prodId = ICalendarUtils.buildProdId(getProductName());
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
				InternetAddress from = ud.getEmail();
				for(EventAttendee attendee : toBeNotified) {
					InternetAddress to = InternetAddressUtils.toInternetAddress(attendee.getRecipient());
					if (InternetAddressUtils.isAddressValid(to)) {
						final String customBody = TplHelper.buildEventInvitationBodyTpl(ud.getLocale(), dateFormat, timeFormat, event, crud, attendee.getAddress(), servicePublicUrl);
						final String html = TplHelper.buildInvitationTpl(ud.getLocale(), source, attendee.getAddress(), event.getTitle(), customBody, because, crud);
						try {
							MimeMultipart mmp = ICalendarUtils.createInvitationPart(html, calPart, attPart);
							WT.sendEmail(session, from, Arrays.asList(to), null, null, subject, mmp);
							
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
	
	/**
	 * Computes event length in days.
	 * For events that starts and ends in same date, returned lenght will be 0.
	 * @param event
	 * @return 
	 */
	private int calculateEventLengthInDays(IEvent event) {
		return Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
	}
	
	/**
	 * Fills passed dates hashmap within ones coming from specified event.
	 * If event starts on 21 Apr and ends on 25 Apr, 21->25 dates will be added to the set.
	 * @param dates
	 * @param event 
	 */
	private void addExpandedEventDates(HashSet<DateTime> dates, IEvent event) {
		int days = calculateEventLengthInDays(event)+1;
		DateTime date = event.getStartDate().withTimeAtStartOfDay();
		for(int count = 1; count <= days; count++) {
			dates.add(date);
			date = date.plusDays(1);
		}
	}
	
	private Calendar doCalendarUpdate(boolean insert, Connection con, Calendar cal) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		
		OCalendar ocal = createOCalendar(cal);
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
		return createCalendar(ocal);
	}
	
	private String buildEventUid(int eventId, String internetName) {
		return buildEventUid(IdentifierUtils.getUUIDTimeBased(true), eventId, internetName);
	}
	
	private String buildEventUid(String timeBasedPart, int eventId, String internetName) {
		return buildEventUid(timeBasedPart, DigestUtils.md5Hex(String.valueOf(eventId)), internetName);
	}
	
	private String buildEventUid(String timeBasedPart, String eventPart, String internetName) {
		// Generates the uid joining a dynamic time-based string with one 
		// calculated from the real event id. This may help in subsequent phases
		// especially to determine if the event is original or is coming from 
		// an invitation.
		return ICalendarUtils.buildUid(timeBasedPart + "." + eventPart, internetName);
	}
	
	private InsertResult doEventInputInsert(Connection con, HashMap<String, OEvent> cache, EventInput ei) throws DAOException {
		InsertResult insert = doEventInsert(con, ei.event, true, true);
		if (insert.recurrence != null) {
			// Cache recurring event for future use within broken references 
			cache.put(insert.event.getPublicUid(), insert.event);
			// If present, adds excluded dates as broken instances
			if (ei.excludedDates != null) {
				for(LocalDate date : ei.excludedDates) {
					doExcludeRecurrenceDate(con, insert.event, date);
				}
			}
		} else {
			if (ei.overwritesRecurringInstance != null) {
				if (cache.containsKey(insert.event.getPublicUid())) {
					final OEvent oevt = cache.get(insert.event.getPublicUid());
					doExcludeRecurrenceDate(con, oevt, ei.overwritesRecurringInstance, insert.event.getEventId());
				}
			}
		}
		return insert;
	}
	
	private InsertResult doEventInsert(Connection con, Event event, boolean insertRecurrence, boolean insertAttendees) {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		DateTime revision = createRevisionTimestamp();
		
		OEvent oevt = createOEvent(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		oevt.setRevisionStatus(OEvent.REV_STATUS_NEW);
		fillOEventWithDefaults(oevt);
		oevt.ensureCoherence();
		
		ArrayList<OEventAttendee> oatts = new ArrayList<>();
		if(insertAttendees && event.hasAttendees()) {
			for(EventAttendee att : event.getAttendees()) {
				final OEventAttendee oatt = createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(oevt.getEventId());
				attDao.insert(con, oatt);
				oatts.add(oatt);
			}
		}
		
		ORecurrence orec = null;
		if(insertRecurrence && event.hasRecurrence()) {
			orec = new ORecurrence();
			orec.fillFrom(event.getRecurrence(), oevt.getStartDate(), oevt.getEndDate(), oevt.getTimezone());
			orec.setRecurrenceId(recDao.getSequence(con).intValue());
			recDao.insert(con, orec);
		}
		
		if(orec != null) {
			oevt.setRecurrenceId(orec.getRecurrenceId());
		} else {
			oevt.setRecurrenceId(null);
		}
		evtDao.insert(con, oevt, revision);
		return new InsertResult(oevt, orec, oatts);
	}
	
	private OEvent doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean attendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		DateTime revision = createRevisionTimestamp();
		
		if (StringUtils.isBlank(event.getOrganizer())) event.setOrganizer(buildOrganizer()); // Make sure organizer is filled
		fillOEvent(originalEvent, event);
		originalEvent.ensureCoherence();
		
		if (attendees) {
			List<EventAttendee> fromList = createEventAttendeeList(attDao.selectByEvent(con, originalEvent.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(fromList, event.getAttendees());
			
			for(EventAttendee att : changeSet.inserted) {
				final OEventAttendee oatt = createOEventAttendee(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(originalEvent.getEventId());
				attDao.insert(con, oatt);
			}
			for(EventAttendee att : changeSet.updated) {
				final OEventAttendee oatt = createOEventAttendee(att);
				attDao.update(con, oatt);
			}
			for(EventAttendee att : changeSet.deleted) {
				attDao.delete(con, att.getAttendeeId());
			}
		}
		
		evtDao.update(con, originalEvent, revision);
		return originalEvent;
	}
	
	private void doDeleteEvent(Connection con, int eventId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			evtDao.logicDeleteById(con, eventId, createRevisionTimestamp());
			
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			
			recDao.deleteByEvent(con, eventId);
			recbkDao.deleteByEvent(con, eventId);
			attDao.deleteByEvent(con, eventId);
			evtDao.deleteById(con, eventId);
		}
	}
	
	private int doDeleteEventsByCalendar(Connection con, int calendarId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteByCalendar(con, calendarId, createRevisionTimestamp());
			
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
	
	private void doMoveEvent(Connection con, boolean copy, Event event, int targetCalendarId) throws DAOException {
		if(copy) {
			event.setCalendarId(targetCalendarId);
			doEventInsert(con, event, true, true);
			
		} else {
			EventDAO evtDao = EventDAO.getInstance();
			evtDao.updateCalendar(con, event.getEventId(), targetCalendarId, createRevisionTimestamp());
		}
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate) throws DAOException {
		return doExcludeRecurrenceDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, null);
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		return doExcludeRecurrenceDate(con, recurringEvent.getEventId(), recurringEvent.getRecurrenceId(), instanceDate, brokenEventId);
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, int recurringEventId, int recurrenceId, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
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
	
	private List<SchedEventInstance> doGetExpiredEventsForUpdate(Connection con, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		EventDAO eveDao = EventDAO.getInstance();
		//TODO: gestire alert su eventi ricorrenti
		final ArrayList<SchedEventInstance> instances = new ArrayList<>();
		for (VVEvent vevt : eveDao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			instances.add(new SchedEventInstance(createSchedEvent(vevt)));
		}
		/*
		for (VSchedulerEvent vevt : eveDao.viewRecurringExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			instances.addAll(calculateRecurringInstances(con, createSchedEvent(vevt), fromDate, toDate, DateTimeZone.UTC, 200));
		}
		*/
		return instances;
	}
	
	private String buildOrganizer() {
		UserProfile.Data ud = WT.getUserData(getTargetProfileId());
		InternetAddress ia = InternetAddressUtils.toInternetAddress(ud.getEmail().getAddress(), ud.getDisplayName());
		return ia.toString();
	}
	
	private Calendar createCalendar(OCalendar with) {
		return (with == null) ? null : fillCalendar(new Calendar(), with);
	}
	
	private Calendar fillCalendar(Calendar fill, OCalendar with) {
		if ((fill != null) && (with != null)) {
			fill.setCalendarId(with.getCalendarId());
			fill.setDomainId(with.getDomainId());
			fill.setUserId(with.getUserId());
			fill.setBuiltIn(with.getBuiltIn());
			fill.setProvider(EnumUtils.forSerializedName(with.getProvider(), Calendar.Provider.class));
			fill.setName(with.getName());
			fill.setDescription(with.getDescription());
			fill.setColor(with.getColor());
			fill.setSync(EnumUtils.forSerializedName(with.getSync(), Calendar.Sync.class));
			fill.setIsDefault(with.getIsDefault());
			fill.setIsPrivate(with.getIsPrivate());
			fill.setDefaultBusy(with.getBusy());
			fill.setDefaultReminder(with.getReminder());
			fill.setDefaultSendInvitation(with.getInvitation());
			fill.setParameters(with.getParameters());
		}
		return fill;
	}
	
	private OCalendar createOCalendar(Calendar with) {
		return fillOCalendar(new OCalendar(), with);
	}
	
	private OCalendar fillOCalendar(OCalendar fill, Calendar with) {
		if ((fill != null) && (with != null)) {
			fill.setCalendarId(with.getCalendarId());
			fill.setDomainId(with.getDomainId());
			fill.setUserId(with.getUserId());
			fill.setBuiltIn(with.getBuiltIn());
			fill.setProvider(EnumUtils.toSerializedName(with.getProvider()));
			fill.setName(with.getName());
			fill.setDescription(with.getDescription());
			fill.setColor(with.getColor());
			fill.setSync(EnumUtils.toSerializedName(with.getSync()));
			fill.setIsDefault(with.getIsDefault());
			fill.setIsPrivate(with.getIsPrivate());
			fill.setBusy(with.getDefaultBusy());
			fill.setReminder(with.getDefaultReminder());
			fill.setInvitation(with.getDefaultSendInvitation());
			fill.setParameters(with.getParameters());
		}
		return fill;
	}
	
	private OCalendar fillOCalendarWithDefaults(OCalendar fill) {
		if (fill != null) {
			CalendarServiceSettings ss = getServiceSettings();
			if (fill.getDomainId() == null) fill.setDomainId(getTargetProfileId().getDomainId());
			if (fill.getUserId() == null) fill.setUserId(getTargetProfileId().getUserId());
			if (fill.getBuiltIn() == null) fill.setBuiltIn(false);
			if (StringUtils.isBlank(fill.getProvider())) fill.setProvider(EnumUtils.toSerializedName(Calendar.Provider.LOCAL));
			if (StringUtils.isBlank(fill.getColor())) fill.setColor("#FFFFFF");
			if (StringUtils.isBlank(fill.getSync())) fill.setSync(EnumUtils.toSerializedName(ss.getDefaultCalendarSync()));
			if (fill.getIsDefault() == null) fill.setIsDefault(false);
			if (fill.getIsPrivate() == null) fill.setIsPrivate(false);
			if (fill.getBusy() == null) fill.setBusy(false);
			if (fill.getInvitation() == null) fill.setInvitation(false);
			
			Calendar.Provider provider = EnumUtils.forSerializedName(fill.getProvider(), Calendar.Provider.class);
			if (Calendar.Provider.WEBCAL.equals(provider) || Calendar.Provider.CALDAV.equals(provider)) {
				fill.setIsDefault(false);
			}
		}
		return fill;
	}
	
	private CalendarPropSet createCalendarPropSet(OCalendarPropSet with) {
		return fillCalendarPropSet(new CalendarPropSet(), with);
	}
	
	private CalendarPropSet fillCalendarPropSet(CalendarPropSet fill, OCalendarPropSet with) {
		if ((fill != null) && (with != null)) {
			fill.setHidden(with.getHidden());
			fill.setColor(with.getColor());
			fill.setSync(EnumUtils.forSerializedName(with.getSync(), Calendar.Sync.class));
		}
		return fill;
	}
	
	private OCalendarPropSet createOCalendarPropSet(CalendarPropSet with) {
		return fillOCalendarPropSet(new OCalendarPropSet(), with);
	}
	
	private OCalendarPropSet fillOCalendarPropSet(OCalendarPropSet fill, CalendarPropSet with) {
		if ((fill != null) && (with != null)) {
			fill.setHidden(with.getHidden());
			fill.setColor(with.getColor());
			fill.setSync(EnumUtils.toSerializedName(with.getSync()));
		}
		return fill;
	}
	
	private VVEventInstance cloneEvent(VVEventInstance sourceEvent, DateTime newStart, DateTime newEnd) {
		VVEventInstance event = new VVEventInstance(sourceEvent);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private SchedEvent createSchedEvent(VVEvent with) {
		return fillSchedEvent(new SchedEvent(), with);
	}
	
	private <T extends SchedEvent> T fillSchedEvent(T fill, VVEvent with) {
		if ((fill != null) && (with != null)) {
			fill.setEventId(with.getEventId());
			fill.setCalendarId(with.getCalendarId());
			fill.setPublicUid(with.getPublicUid());
			fill.setRecurrenceId(with.getRecurrenceId());
			fill.setRevisionTimestamp(with.getRevisionTimestamp());
			fill.setStartDate(with.getStartDate());
			fill.setEndDate(with.getEndDate());
			fill.setTimezone(with.getTimezone());
			fill.setAllDay(with.getAllDay());
			fill.setOrganizer(with.getOrganizer());
			fill.setTitle(with.getTitle());
			fill.setDescription(with.getDescription());
			fill.setLocation(with.getLocation());
			fill.setIsPrivate(with.getIsPrivate());
			fill.setBusy(with.getBusy());
			fill.setReminder(with.getReminder());
			fill.setOriginalEventId(with.getOriginalEventId());
			fill.setCalendarDomainId(with.getCalendarDomainId());
			fill.setCalendarUserId(with.getCalendarUserId());
			fill.setHasAttendees(with.getHasAttendees());
		}
		return fill;
	}
	
	private Event createEvent(OEvent with) {
		return (with == null) ? null : fillEvent(new Event(), with);
	}
	
	private <T extends Event> T fillEvent(T fill, OEvent with) {
		if ((fill != null) && (with != null)) {
			fill.setEventId(with.getEventId());
			fill.setCalendarId(with.getCalendarId());
			//fill.setRevisionStatus(EnumUtils.forSerializedName(with.getRevisionStatus(), Event.RevisionStatus.class));
			fill.setRevisionTimestamp(with.getRevisionTimestamp());
			//fill.setRevisionSequence(with.getRevisionSequence());
			fill.setPublicUid(with.getPublicUid());
			fill.setReadOnly(with.getReadOnly());
			fill.setStartDate(with.getStartDate());
			fill.setEndDate(with.getEndDate());
			fill.setTimezone(with.getTimezone());
			fill.setAllDay(with.getAllDay());
			fill.setOrganizer(with.getOrganizer());
			fill.setTitle(with.getTitle());
			fill.setDescription(with.getDescription());
			fill.setLocation(with.getLocation());
			fill.setIsPrivate(with.getIsPrivate());
			fill.setBusy(with.getBusy());
			fill.setReminder(with.getReminder());
			fill.setHref(with.getHref());
			fill.setEtag(with.getEtag());
			fill.setActivityId(with.getActivityId());
			fill.setMasterDataId(with.getMasterDataId());
			fill.setStatMasterDataId(with.getStatMasterDataId());
			fill.setCausalId(with.getCausalId());
		}
		return fill;
	}
	
	/*
	private void fillEventWithDefaults(Event fill) {
		if (fill != null) {
			if (StringUtils.isBlank(fill.getPublicUid())) {
				fill.setPublicUid(buildEventUid(fill.getEventId(), WT.getDomainInternetName(getTargetProfileId().getDomainId())));
			}
			if (fill.getReadOnly() == null) fill.setReadOnly(false);
			if (StringUtils.isBlank(fill.getOrganizer())) fill.setOrganizer(buildOrganizer());
		}
	}
	*/
	
	private OEvent createOEvent(Event with) {
		return fillOEvent(new OEvent(), with);
	}
	
	private OEvent fillOEvent(OEvent fill, Event with) {
		if ((fill != null) && (with != null)) {
			fill.setEventId(with.getEventId());
			fill.setCalendarId(with.getCalendarId());
			//fill.setRevisionStatus(EnumUtils.toSerializedName(with.getRevisionStatus()));
			fill.setRevisionTimestamp(with.getRevisionTimestamp());
			//fill.setRevisionSequence(with.getRevisionSequence());
			fill.setPublicUid(with.getPublicUid());
			fill.setReadOnly(with.getReadOnly());
			fill.setStartDate(with.getStartDate());
			fill.setEndDate(with.getEndDate());
			fill.setTimezone(with.getTimezone());
			fill.setAllDay(with.getAllDay());
			fill.setOrganizer(with.getOrganizer());
			fill.setTitle(with.getTitle());
			fill.setDescription(with.getDescription());
			fill.setLocation(with.getLocation());
			fill.setIsPrivate(with.getIsPrivate());
			fill.setBusy(with.getBusy());
			fill.setReminder(with.getReminder());
			fill.setHref(with.getHref());
			fill.setEtag(with.getEtag());
			fill.setActivityId(with.getActivityId());
			fill.setMasterDataId(with.getMasterDataId());
			fill.setStatMasterDataId(with.getStatMasterDataId());
			fill.setCausalId(with.getCausalId());
		}
		return fill;
	}
	
	private OEvent fillOEventWithDefaults(OEvent fill) {
		if (fill != null) {
			if (StringUtils.isBlank(fill.getPublicUid())) {
				fill.setPublicUid(buildEventUid(fill.getEventId(), WT.getDomainInternetName(getTargetProfileId().getDomainId())));
			}
			if (fill.getReadOnly() == null) fill.setReadOnly(false);
			if (StringUtils.isBlank(fill.getOrganizer())) fill.setOrganizer(buildOrganizer());
		}
		return fill;
	}
	
	private EventInstance createEventInstance(String key, RecurringInfo recurringInfo, VVEvent se) {
		EventInstance evti = new EventInstance(key, recurringInfo);
		evti.setEventId(se.getEventId());
		evti.setCalendarId(se.getCalendarId());
		evti.setRevisionTimestamp(se.getRevisionTimestamp());
		evti.setPublicUid(se.getPublicUid());
		evti.setStartDate(se.getStartDate());
		evti.setEndDate(se.getEndDate());
		evti.setTimezone(se.getTimezone());
		evti.setAllDay(se.getAllDay());
		evti.setOrganizer(se.getOrganizer());
		evti.setTitle(se.getTitle());
		evti.setDescription(se.getDescription());
		evti.setLocation(se.getLocation());
		evti.setIsPrivate(se.getIsPrivate());
		evti.setBusy(se.getBusy());
		evti.setReminder(se.getReminder());
		evti.setHref(se.getHref());
		evti.setEtag(se.getEtag());
		evti.setActivityId(se.getActivityId());
		evti.setMasterDataId(se.getMasterDataId());
		evti.setStatMasterDataId(se.getStatMasterDataId());
		evti.setCausalId(se.getCausalId());
		return evti;
	}
	
	private EventRecurrence createEventRecurrence(ORecurrence orec) {
		
		/**
		 * NB: aggiornare anche l'implementazione in Migration.java
		 */
		
		if (orec == null) return null;
		EventRecurrence rec = new EventRecurrence();
		rec.setType(orec.getType());
		rec.setDailyFreq(orec.getDailyFreq());
		rec.setWeeklyFreq(orec.getWeeklyFreq());
		rec.setWeeklyDay1(orec.getWeeklyDay_1());
		rec.setWeeklyDay2(orec.getWeeklyDay_2());
		rec.setWeeklyDay3(orec.getWeeklyDay_3());
		rec.setWeeklyDay4(orec.getWeeklyDay_4());
		rec.setWeeklyDay5(orec.getWeeklyDay_5());
		rec.setWeeklyDay6(orec.getWeeklyDay_6());
		rec.setWeeklyDay7(orec.getWeeklyDay_7());
		rec.setMonthlyFreq(orec.getMonthlyFreq());
		rec.setMonthlyDay(orec.getMonthlyDay());
		rec.setYearlyFreq(orec.getYearlyFreq());
		rec.setYearlyDay(orec.getYearlyDay());
		
		/*
		if(recurrence.getRepeat() != null) {
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(recurrence.getRepeat());
		} else {
			rec.setRepeatTimes(null);
			if(rec.getUntilDate().compareTo(ICal4jUtils.ifiniteDate()) == 0) {
				rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
			} else {
				rec.setEndsMode(Recurrence.ENDS_MODE_UNTIL);
			}
		}
		*/
		
		rec.setUntilDate(orec.getUntilDate());
		if(orec.isEndRepeat()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(orec.getRepeat());
		} else if(orec.isEndUntil()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_UNTIL);
		} else if(orec.isEndNever()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_NEVER);
		} else {
			throw new WTRuntimeException("Unable to set a valid endMode");
		}
		
		rec.setRRule(orec.getRule());
		return rec;
	}
	
	private EventAttendee createEventAttendee(OEventAttendee with) {
		return fillEventAttendee(new EventAttendee(), with);
	}
	
	private EventAttendee fillEventAttendee(EventAttendee fill, OEventAttendee with) {
		if ((fill != null) && (with != null)) {
			fill.setAttendeeId(with.getAttendeeId());
			fill.setRecipient(with.getRecipient());
			fill.setRecipientType(with.getRecipientType());
			fill.setRecipientRole(with.getRecipientRole());
			fill.setResponseStatus(with.getResponseStatus());
			fill.setNotify(with.getNotify());
		}
		return fill;
	}
	
	private OEventAttendee createOEventAttendee(EventAttendee with) {
		return fillOEventAttendee(new OEventAttendee(), with);
	}
	
	private OEventAttendee fillOEventAttendee(OEventAttendee fill, EventAttendee with) {
		if ((fill != null) && (with != null)) {
			fill.setAttendeeId(with.getAttendeeId());
			fill.setRecipient(with.getRecipient());
			fill.setRecipientType(with.getRecipientType());
			fill.setRecipientRole(with.getRecipientRole());
			fill.setResponseStatus(with.getResponseStatus());
			fill.setNotify(with.getNotify());
		}
		return fill;
	}
	
	private List<EventAttendee> createEventAttendeeList(List<OEventAttendee> attendees) {
		ArrayList<EventAttendee> atts = new ArrayList<>();
		for(OEventAttendee attendee : attendees) {
			atts.add(createEventAttendee(attendee));
		}
		return atts;
	}
	
	private UserProfileId findCalendarOwner(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO dao = CalendarDAO.getInstance();
			Owner owner = dao.selectOwnerById(con, calendarId);
			return (owner == null) ? null : new UserProfileId(owner.getDomainId(), owner.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
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
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, SchedEventInstance event) {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		//TODO: completare email
		return alert;
	}
	
	/**
	 * @deprecated 
	 */
	private ReminderInApp createEventReminderAlertWeb(VVEventInstance event) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		alert.setTitle(event.getTitle());
		alert.setDate(event.getStartDate().withZone(event.getDateTimeZone()));
		alert.setTimezone(event.getTimezone());
		return alert;
	}
	
	/**
	 * @deprecated 
	 */
	private ReminderEmail createEventReminderAlertEmail(Locale locale, VVEventInstance event) {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		//TODO: completare email
		return alert;
	}
	
	private DateTime createRevisionTimestamp() {
		return DateTime.now(DateTimeZone.UTC);
	}
	
	private void storeAsSuggestion(CoreManager coreMgr, String context, String value) {
		if (StringUtils.isBlank(value)) return;
		coreMgr.addServiceStoreEntry(SERVICE_ID, context, value.toUpperCase(), value);
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
				UserProfileId owner = findCalendarOwner(key);
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
							for (Calendar calendar : listCalendars(ownerPid)) {
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
	
	/*
	public void importICal(int calendarId, InputStream is, DateTimeZone defaultTz) throws Exception {
		Connection con = null;
		HashMap<String, OEvent> uidMap = new HashMap<>();
		LogEntries log = new LogEntries();
		log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Started at {0}", new DateTime()));
		
		try {
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Parsing iCal file..."));
			ArrayList<ParseResult> parsed = null;
			try {
				parsed = ICalHelper.parseICal(log, is, defaultTz);
			} catch(ParserException | IOException ex) {
				log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to complete parsing. Reason: {0}", ex.getMessage()));
				throw ex;
			}
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s found!", parsed.size()));
			
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Importing..."));
			con = WT.getConnection(SERVICE_ID, false);
			int count = 0;
			for(ParseResult parse : parsed) {
				parse.event.setCalendarId(calendarId);
				try {
					InsertResult insert = doEventInsert(con, parse.event, true, true);
					
					if(insert.recurrence != null) {
						// Cache recurring event for future use within broken references 
						uidMap.put(insert.event.getPublicUid(), insert.event);
						
						// If present, adds excluded dates as broken instances
						if(parse.excludedDates != null) {
							for(LocalDate date : parse.excludedDates) {
								doExcludeRecurrenceDate(con, insert.event, date);
							}
						}
					} else {
						if(parse.overwritesRecurringInstance != null) {
							if(uidMap.containsKey(insert.event.getPublicUid())) {
								OEvent oevt = uidMap.get(insert.event.getPublicUid());
								doExcludeRecurrenceDate(con, oevt, parse.overwritesRecurringInstance, insert.event.getEventId());
							}
						}
					}
					
					DbUtils.commitQuietly(con);
					count++;
				} catch(Exception ex) {
					ex.printStackTrace();
					DbUtils.rollbackQuietly(con);
					log.addMaster(new MessageLogEntry(LogEntry.Level.ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", parse.event.getTitle(), parse.event.getPublicUid(), ex.getMessage()));
				}
			}
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s imported!", count));
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			uidMap.clear();
			DbUtils.closeQuietly(con);
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Ended at {0}", new DateTime()));
			
			for(LogEntry entry : log) {
				logger.debug("{}", ((MessageLogEntry)entry).getMessage());
			}
		}
	}
	*/

	/*
	public List<OPostponedReminder> getExpiredPostponedReminders(Connection con, DateTime greaterInstant) throws WTException {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return prdao.selectExpiredForUpdateByInstant(con, greaterInstant);
	}
	
	public boolean deletePostponedReminder(Connection con, Integer eventId, DateTime remindOn) {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return (prdao.delete(con, eventId, remindOn) == 1);
	}
	
	public void postponeReminder(String eventKey, String reminderId, int minutes) throws WTException {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(SERVICE_ID, false);
			
			EventDAO edao = EventDAO.getInstance();
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			OEvent original = edao.selectById(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(original.getReminder() == null) throw new WTException("Event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.originalEventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_BROKEN)) {
				OEvent broken = edao.selectById(con, ekey.eventId);
				if(broken == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
				if(broken.getReminder() == null) throw new WTException("Broken event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.eventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_RECURRING)) {
				//TODO: gestire i reminder per gli eventi ricorrenti
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void snoozeReminder(String eventKey, String reminderId) throws WTException {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(SERVICE_ID, false);
			
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			prdao.delete(con, ekey.eventId, rid.remindOn);
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
}
