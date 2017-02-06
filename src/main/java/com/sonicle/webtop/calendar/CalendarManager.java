/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.calendar;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.calendar.bol.model.SchedulerEventInstance;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.model.CalendarFolder;
import com.sonicle.webtop.calendar.bol.model.CalendarRoot;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.EventInstance;
import com.sonicle.webtop.calendar.bol.model.EventInstance.RecurringInfo;
import com.sonicle.webtop.calendar.bol.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.calendar.io.EventICalFileReader;
import com.sonicle.webtop.calendar.io.EventReadResult;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.bol.model.IncomingShareRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.bol.model.SharePermsFolder;
import com.sonicle.webtop.core.bol.model.SharePermsElements;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtDateTime;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import net.fortuna.ical4j.model.Calendar;
import com.sonicle.webtop.calendar.io.EventFileReader;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.util.TimeZone;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseManager {
	public static final Logger logger = WT.getLogger(CalendarManager.class);
	private static final String GROUPNAME_CALENDAR = "CALENDAR";
	private static final String EVENT_NORMAL = "normal";
	private static final String EVENT_BROKEN = "broken";
	private static final String EVENT_RECURRING = "recurring";
	public static final String TARGET_THIS = "this";
	public static final String TARGET_SINCE = "since";
	public static final String TARGET_ALL = "all";
	public static final String SUGGESTION_EVENT_TITLE = "eventtitle";
	public static final String SUGGESTION_EVENT_LOCATION = "eventlocation";
	
	private final HashMap<Integer, UserProfile.Id> cacheCalendarToOwner = new HashMap<>();
	private final Object shareCacheLock = new Object();
	private final HashMap<UserProfile.Id, String> cacheOwnerToRootShare = new HashMap<>();
	private final HashMap<UserProfile.Id, String> cacheOwnerToWildcardFolderShare = new HashMap<>();
	private final HashMap<Integer, String> cacheCalendarToFolderShare = new HashMap<>();
	
	public CalendarManager(boolean fastInit, UserProfile.Id targetProfileId) {
		super(fastInit, targetProfileId);
	}
	
	private String getProductName() {
		return WT.getPlatformName() + " Calendar";
	}
	
	public static DateTime parseYmdHmsWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date + " " + time, tz);
	}
	
	public static DateTime parseYmdHmsWithZone(String dateTime, DateTimeZone tz) {
		String dt = StringUtils.replace(dateTime, "T", " ");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return formatter.parseDateTime(dt);
	}
	
	public List<CalendarRoot> listIncomingCalendarRoots() throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		ArrayList<CalendarRoot> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		
		List<IncomingShareRoot> shares = core.listIncomingShareRoots(SERVICE_ID, GROUPNAME_CALENDAR);
		for(IncomingShareRoot share : shares) {
			SharePermsRoot perms = core.getShareRootPermissions(share.getShareId());
			CalendarRoot root = new CalendarRoot(share, perms);
			if(hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public HashMap<Integer, CalendarFolder> listIncomingCalendarFolders(String rootShareId) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		LinkedHashMap<Integer, CalendarFolder> folders = new LinkedHashMap<>();
		
		// Retrieves incoming folders (from sharing). This lookup already 
		// returns readable shares (we don't need to test READ permission)
		List<OShare> shares = core.listIncomingShareFolders(rootShareId, GROUPNAME_CALENDAR);
		for(OShare share : shares) {
			
			List<OCalendar> cals = null;
			if(share.hasWildcard()) {
				UserProfile.Id ownerId = core.userUidToProfileId(share.getUserUid());
				cals = listCalendars(ownerId);
			} else {
				cals = Arrays.asList(getCalendar(Integer.valueOf(share.getInstance())));
			}
			
			for(OCalendar cal : cals) {
				SharePermsFolder fperms = core.getShareFolderPermissions(share.getShareId().toString());
				SharePermsElements eperms = core.getShareElementsPermissions(share.getShareId().toString());
				
				if(folders.containsKey(cal.getCalendarId())) {
					CalendarFolder folder = folders.get(cal.getCalendarId());
					folder.getPerms().merge(fperms);
					folder.getElementsPerms().merge(eperms);
				} else {
					folders.put(cal.getCalendarId(), new CalendarFolder(share.getShareId().toString(), fperms, eperms, cal));
				}
			}
		}
		return folders;
	}
	
	public Sharing getSharing(String shareId) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		return core.getSharing(SERVICE_ID, GROUPNAME_CALENDAR, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		core.updateSharing(SERVICE_ID, GROUPNAME_CALENDAR, sharing);
	}
	
	public UserProfile.Id getCalendarOwner(int calendarId) throws WTException {
		return calendarToOwner(calendarId);
	}
	
	public List<OCalendar> listCalendars() throws WTException {
		return listCalendars(getTargetProfileId());
	}
	
	private List<OCalendar> listCalendars(UserProfile.Id pid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO dao = CalendarDAO.getInstance();
			return dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar getCalendar(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "READ");
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO dao = CalendarDAO.getInstance();
			return dao.selectById(con, calendarId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar getBuiltInCalendar() throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO dao = CalendarDAO.getInstance();
			OCalendar cal = dao.selectBuiltInByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(cal == null) return null;
			checkRightsOnCalendarFolder(cal.getCalendarId(), "READ");
			return cal;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar addCalendar(OCalendar item) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarRoot(item.getProfileId(), "MANAGE");
			con = WT.getConnection(SERVICE_ID, false);
			
			item.setBuiltIn(false);
			item = doInsertCalendar(con, item);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_INSERT", item.getCalendarId().toString());
			return item;
			
			/*
			CalendarDAO dao = CalendarDAO.getInstance();
			
			item.setCalendarId(dao.getSequence(con).intValue());
			item.setBuiltIn(false);
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.insert(con, item, createUpdateInfo());
			DbUtils.commitQuietly(con);
			return item;
			*/
			
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
	
	public OCalendar addBuiltInCalendar() throws WTException {
		CalendarDAO dao = CalendarDAO.getInstance();
		Connection con = null;
		OCalendar item = null;
		
		try {
			checkRightsOnCalendarRoot(getTargetProfileId(), "MANAGE");
			con = WT.getConnection(SERVICE_ID, false);
			
			item = dao.selectBuiltInByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(item != null) {
				logger.debug("Built-in category already present");
				return null;
			}
			
			item = new OCalendar();
			item.setDomainId(getTargetProfileId().getDomainId());
			item.setUserId(getTargetProfileId().getUserId());
			item.setBuiltIn(true);
			item.setName(WT.getPlatformName());
			item.setDescription("");
			item.setColor("#FFFFFF");
			item.setIsPrivate(false);
			item.setBusy(false);
			item.setReminder(null);
			item.setSync(OCalendar.SYNC_OFF);
			item.setInvitation(false);
			item.setIsDefault(true);
			item.setBusy(false);
			item = doInsertCalendar(con, item);
			DbUtils.commitQuietly(con);
			
			writeLog("CALENDAR_INSERT",  String.valueOf(item.getCalendarId()));
			return item;
			
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
	
	public OCalendar updateCalendar(OCalendar item) throws WTException {
		CalendarDAO dao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(item.getCalendarId(), "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.update(con, item);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_UPDATE",  String.valueOf(item.getCalendarId()));
			return item;
			
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
	
	public void deleteCalendar(int calendarId) throws WTException {
		CalendarDAO dao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "DELETE");
			
			con = WT.getConnection(SERVICE_ID, false);
			dao.deleteById(con, calendarId);
			doDeleteEventsByCalendar(con, calendarId);
			
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_DELETE",  String.valueOf(calendarId));
			writeLog("EVENT_DELETE",  "*");
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DateTime> listEventsDates(CalendarRoot root, Integer[] calendars, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
		return listEventsDates(root.getOwnerProfileId(), calendars, fromDate, toDate, userTz);
	}
	
	public List<DateTime> listEventsDates(UserProfile.Id pid, Integer[] calendars, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
		Connection con = null;
		HashSet<DateTime> dates = new HashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEventInstance> expEvents = null;
			for(OCalendar cal : cals) {
				checkRightsOnCalendarFolder(cal.getCalendarId(), "READ");
				for(VSchedulerEvent se : edao.viewDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					addExpandedEventDates(dates, se);
				}
				for(VSchedulerEvent se : edao.viewRecurringDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					expEvents = CalendarManager.this.calculateRecurringInstances(con, new SchedulerEventInstance(se), fromDate, toDate, userTz);
					for(SchedulerEventInstance expEvent : expEvents) {
						addExpandedEventDates(dates, expEvent);
					}
				}
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return new ArrayList<>(dates);
	}
	
	private SchedulerEventInstance getSchedulerEventByUid(Connection con, String eventPublicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		ve.updateCalculatedFields();
		return new SchedulerEventInstance(ve);
	}
	
	public SchedulerEventInstance getSchedulerEvent(Integer eventId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return CalendarManager.this.getSchedulerEvent(con, eventId);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private SchedulerEventInstance getSchedulerEvent(Connection con, int eventId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent se = edao.viewById(con, eventId);
		se.updateCalculatedFields();
		return new SchedulerEventInstance(se);
	}
	
	public List<CalendarEvents> listSchedulerEvents(CalendarRoot root, Integer[] calendars, DateTime fromDate, DateTime toDate) throws WTException {
		return listSchedulerEvents(root.getOwnerProfileId(), calendars, fromDate, toDate);
	}
	
	public List<CalendarEvents> listSchedulerEvents(UserProfile.Id pid, Integer[] calendars, DateTime fromDate, DateTime toDate) throws WTException {
		Connection con = null;
		ArrayList<CalendarEvents> calEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEventInstance> sevs = null;
			for(OCalendar cal : cals) {
				checkRightsOnCalendarFolder(cal.getCalendarId(), "READ");
				
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
				calEvts.add(new CalendarEvents(cal, sevs));
			}
			return calEvts;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<SchedulerEventInstance> listExpiredSchedulerEvents(Connection con, DateTime fromDate, DateTime toDate) throws WTException {
		//TODO: auth
		List<SchedulerEventInstance> sevs = new ArrayList<>();
		EventDAO edao = EventDAO.getInstance();
		
		for(VSchedulerEvent se : edao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			se.updateCalculatedFields();
			sevs.add(new SchedulerEventInstance(se));
		}
		/*
		for(VSchedulerEvent se : edao.viewRecurringExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			se.updateCalculatedFields();
			sevs.add(new SchedulerEvent(se));
		}
		*/
		return sevs;
	}
	
	public List<CalendarEvents> searchSchedulerEvents(UserProfile.Id pid, Integer[] calendars, String query) throws WTException {
		Connection con = null;
		ArrayList<CalendarEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEventInstance> sevs = null;
			for(OCalendar cal : cals) {
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.searchByCalendarQuery(con, cal.getCalendarId(), query)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
				for(VSchedulerEvent se : edao.searchRecurringByCalendarQuery(con, cal.getCalendarId(), query)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
				grpEvts.add(new CalendarEvents(cal, sevs));
			}
			return grpEvts;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedulerEventInstance> calculateRecurringInstances(SchedulerEventInstance recurringEvent, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return calculateRecurringInstances(con, recurringEvent, fromDate, toDate, userTz);
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
	
	public Integer getEventId(String publicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			List<Integer> ids = edao.selectAliveIdsByPublicUid(con, publicUid);
			if (ids.isEmpty()) return null;
			if (ids.size() != 1) throw new WTException("Multiple events found [{0}]", publicUid);
			return ids.get(0);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event getEvent(int eventId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OEvent oevt = edao.selectAliveById(con, eventId);
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
	
	public Event getEvent(String publicUid) throws WTException {
		Integer eventId = getEventId(publicUid);
		if(eventId == null) return null;
		return getEvent(eventId);
	}
	
	public Event addEvent(Event event) throws WTException {
		return addEvent(event, true);
	}
	
	public Event addEvent(Event event, boolean notifyAttendees) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		Connection con = null;
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
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
	
	public Event addEventFromICal(int calendarId, Calendar ical) throws WTException {
		final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		
		EventICalFileReader rea = new EventICalFileReader(udata.getTimeZone());
		ArrayList<EventReadResult> parsed = rea.readCalendar(new LogEntries(), ical);
		if (parsed.size() > 1) throw new WTException("iCal must contain at least one event");
		
		Event event = parsed.get(0).event;
		event.setCalendarId(calendarId);
		return addEvent(event, false);
	}
	
	public void updateEventFromICal(Calendar ical) throws WTException {
		Connection con = null;
		
		VEvent ve = ICalendarUtils.getVEvent(ical);
		if (ve == null) throw new WTException("Calendar does not contain any event");
		
		String uid = ve.getUid().getValue(); 
		if (StringUtils.isBlank(uid)) throw new WTException("Event does not provide a valid Uid");
		
		Event evt = getEvent(uid);
		
		if (ical.getMethod().equals(Method.REQUEST)) {
			EventDAO edao = EventDAO.getInstance();
			final UserProfile.Data udata = WT.getUserData(getTargetProfileId());
			
			EventICalFileReader rea = new EventICalFileReader(udata.getTimeZone());
			ArrayList<EventReadResult> parsed = rea.readCalendar(new LogEntries(), ical);
			if (parsed.size() > 1) throw new WTException("iCal must contain at least one event");
			
			Event parsedEvent = parsed.get(0).event;
			parsedEvent.setCalendarId(evt.getCalendarId());
			
			try {
				checkRightsOnCalendarElements(evt.getCalendarId(), "UPDATE");
				
				con = WT.getConnection(SERVICE_ID, false);
				OEvent original = edao.selectById(con, evt.getEventId());
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
			Attendee att = ICalendarUtils.getAttendee(ve);
			if (att == null) throw new WTException("Event does not provide any attendee");
			
			PartStat partStat = (PartStat)att.getParameter(Parameter.PARTSTAT);
			String responseStatus = ICalHelper.partStatToResponseStatus(partStat);
			List<String> updatedAttIds = updateEventAttendeeResponseByRecipient(evt, att.getCalAddress().getSchemeSpecificPart(), responseStatus);
			if (!updatedAttIds.isEmpty()) {
				evt = getEvent(evt.getEventId());
				for(String attId : updatedAttIds) notifyOrganizer(getLocale(), evt, attId);
			}
			
		} else if (ical.getMethod().equals(Method.CANCEL)) {
			
			try {
				con = WT.getConnection(SERVICE_ID);
				doDeleteEvent(con, evt.getEventId());

			} catch(SQLException | DAOException ex) {
				throw new WTException(ex, "DB error");
			} finally {
				DbUtils.closeQuietly(con);
			}
			
		} else {
			throw new WTException("Unsupported Calendar's method [{0}]", ical.getMethod().toString());
		}
	}
	
	private List<String> updateEventAttendeeResponseByRecipient(Event event, String recipient, String responseStatus) throws WTException {
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			// Find matching attendees
			ArrayList<String> matchingIds = new ArrayList<>();
			List<OEventAttendee> atts = eadao.selectByEvent(con, event.getEventId());
			for (OEventAttendee att : atts) {
				final InternetAddress ia = MailUtils.buildInternetAddress(att.getRecipient());
				if (ia == null) continue;
				
				if (StringUtils.equals(ia.getAddress(), recipient)) matchingIds.add(att.getAttendeeId());
			}
			
			// Update responses
			int ret = eadao.updateAttendeeResponseByIds(con, responseStatus, matchingIds);
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
	
	public Event updateEventAttendeeResponse(String eventPublicUid, String attendeeUid, String responseStatus) throws WTException {
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			Event evt = getEvent(eventPublicUid);
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
	
	public EventInstance getEventInstance(String eventKey) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ek = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			
			OEvent original = edao.selectById(con, ek.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{0}]", ek.originalEventId);
			checkRightsOnCalendarFolder(original.getCalendarId(), "READ"); // Rights check!
			
			VSchedulerEvent sevt = edao.viewById(con, ek.eventId);
			sevt.updateCalculatedFields(); // TODO: Serve??????????????????????
			if(sevt == null) throw new WTException("Unable to retrieve event [{}]", ek.eventId);
			
			EventInstance evti = null;
			String type = guessEventType(ek, original);
			if(type.equals(EVENT_NORMAL)) {
				evti = createEventInstance(eventKey, RecurringInfo.SINGLE, sevt);
				evti.setAttendees(createEventAttendeeList(adao.selectByEvent(con, sevt.getEventId())));
				
			} else if(type.equals(EVENT_RECURRING)) {
				ORecurrence rec = rdao.select(con, sevt.getRecurrenceId());
				if(rec == null) throw new WTException("Unable to retrieve recurrence [{0}]", ek.originalEventId);
				
				evti = createEventInstance(eventKey, RecurringInfo.RECURRING, sevt);
				int eventDays = calculateEventLengthInDays(sevt);
				evti.setStartDate(evti.getStartDate().withDate(ek.instanceDate));
				evti.setEndDate(evti.getEndDate().withDate(evti.getStartDate().plusDays(eventDays).toLocalDate()));
				evti.setRecurrence(createEventRecurrence(rec));
				
			} else if(type.equals(EVENT_BROKEN)) {
				//TODO: recuperare il record ricorrenza per verifica?
				evti = createEventInstance(eventKey, RecurringInfo.BROKEN, sevt);
				evti.setAttendees(createEventAttendeeList(adao.selectByEvent(con, sevt.getEventId())));
			}
			
			return evti;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEventInstance(String target, EventInstance event) throws WTException {
		//CoreManager core = WT.getCoreManager(getTargetProfileId());
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		//TODO: gestire i suggerimenti (titolo + luogo)
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(SERVICE_ID, false);
			
			OEvent original = edao.selectById(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			checkRightsOnCalendarElements(original.getCalendarId(), "UPDATE");
			
			//TODO: gestire le notifiche invito per gli eventi ricorrenti
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates the event with new data
					doEventUpdate(con, original, event, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEventInstance(event.getKey()));
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = edao.selectById(con, ekey.eventId);
					if(oevt == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
					doEventUpdate(con, oevt, event, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.eventId));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
					
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEventInstance(event.getKey()));
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Inserts new broken event
					InsertResult insert = doEventInsert(con, event, false, false);
					// 2 - Marks recurring event date inserting a broken record
					doExcludeRecurrenceDate(con, original, ekey.instanceDate, insert.event.getEventId());
					// 3 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					if(orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					DateTime until = orec.getUntilDate();
					orec.applyEndUntil(ekey.instanceDate.minusDays(1).toDateTimeAtStartOfDay(), DateTimeZone.forID(original.getTimezone()), true);
					rdao.update(con, orec);
					// 2 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionTimestamp());
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					// 3 - Insert new event adjusting recurrence a bit
					event.getRecurrence().setEndsMode(Recurrence.ENDS_MODE_UNTIL);
					event.getRecurrence().setUntilDate(until);
					InsertResult insert = doEventInsert(con, event, true, false);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
					writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
					
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
					//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - Updates recurring event data (dates must be preserved) (+revision)
					event.setStartDate(event.getStartDate().withDate(original.getStartDate().toLocalDate()));
					event.setEndDate(event.getEndDate().withDate(original.getEndDate().toLocalDate()));
					doEventUpdate(con, original, event, false);
					// 2 - Updates recurrence data
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					orec.fillFrom(event.getRecurrence(), original.getStartDate(), original.getEndDate(), original.getTimezone());
					rdao.update(con, orec);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(original.getEventId()));
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
	
	public void updateEventInstance(String eventKey, DateTime startDate, DateTime endDate, String title) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			OEvent originalEvent = edao.selectById(con, ekey.originalEventId);
			if(originalEvent == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			checkRightsOnCalendarElements(originalEvent.getCalendarId(), "UPDATE");
			
			String type = guessEventType(ekey, originalEvent);
			if(type.equals(EVENT_NORMAL) || type.equals(EVENT_BROKEN)) {
				// 1 - Updates event's dates/times (+revision)
				OEvent evt = edao.selectById(con, ekey.eventId);
				if(evt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
				evt.setStartDate(startDate);
				evt.setEndDate(endDate);
				evt.setTitle(title);
				edao.update(con, evt, createRevisionTimestamp());
				
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(evt.getEventId()));
				// Handle attendees invitation
				notifyAttendees(Crud.UPDATE, getEventInstance(eventKey));
				
			} else {
				throw new WTException("Unable to move recurring event instance [{}]", ekey.eventId);
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
	
	public void deleteEventInstance(String target, String eventKey) throws WTException {
		EventDAO evtdao = EventDAO.getInstance();
		RecurrenceDAO recdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID, false);
			
			OEvent originalEvent = evtdao.selectById(con, ekey.originalEventId);
			if(originalEvent == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			checkRightsOnCalendarElements(originalEvent.getCalendarId(), "DELETE");
			
			Event evtBase = getEventInstance(eventKey); // Gets event for later use
			
			String type = guessEventType(ekey, originalEvent);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					if(!ekey.eventId.equals(originalEvent.getEventId())) throw new WTException("In this case both ids must be equals");
					doDeleteEvent(con, ekey.eventId);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(originalEvent.getEventId()));
					// Handle attendees invitation
					notifyAttendees(Crud.DELETE, evtBase);
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - logically delete newevent (broken one)
					doDeleteEvent(con, ekey.eventId);
					// 2 - updates revision of original event
					evtdao.updateRevision(con, originalEvent.getEventId(), createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
					writeLog("EVENT_UPDATE", String.valueOf(originalEvent.getEventId()));
					// Handle attendees invitation
					notifyAttendees(Crud.DELETE, evtBase);
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - inserts a broken record (without new event) on deleted date
					doExcludeRecurrenceDate(con, originalEvent, ekey.instanceDate);
					// 2 - updates revision of original event
					evtdao.updateRevision(con, originalEvent.getEventId(), createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(originalEvent.getEventId()));
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence rec = recdao.select(con, originalEvent.getRecurrenceId());
					if(rec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", originalEvent.getRecurrenceId());
					rec.setUntilDate(ekey.instanceDate.toDateTimeAtStartOfDay().minusDays(1));
					rec.updateRRule(DateTimeZone.forID(originalEvent.getTimezone()));
					recdao.update(con, rec);
					// 2 - updates revision of original event
					evtdao.updateRevision(con, originalEvent.getEventId(), createRevisionTimestamp());
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(originalEvent.getEventId()));
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - logically delete original event
					doDeleteEvent(con, ekey.eventId);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(ekey.eventId));
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
	
	public void restoreEventInstance(String eventKey) throws WTException {
		Connection con = null;
		
		try {
			// Scheduler IDs always contains an eventId reference.
			// For broken events extracted eventId is not equal to extracted
			// originalEventId (event generator or database event), since
			// it belongs to the recurring event
			EventKey ekey = new EventKey(eventKey);
			if(ekey.originalEventId.equals(ekey.eventId)) throw new WTException("Cannot restore an event that is not broken");
			con = WT.getConnection(SERVICE_ID, false);
			
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, ekey.eventId);
			// 2 - logically delete broken event
			doDeleteEvent(con, ekey.eventId);
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_UPDATE", String.valueOf(ekey.eventId));
			
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
	
	public void moveEventInstance(boolean copy, String eventKey, int targetCalendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Event evt = getEventInstance(eventKey);
			if(copy || (targetCalendarId != evt.getCalendarId())) {
				checkRightsOnCalendarElements(targetCalendarId, "CREATE"); // Rights check!
				
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
	
	public LinkedHashSet<String> calculateAvailabilitySpans(int minRange, UserProfile.Id pid, DateTime fromDate, DateTime toDate, DateTimeZone userTz, boolean busy) throws WTException {
		Connection con = null;
		LinkedHashSet<String> hours = new LinkedHashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired calendars by profile
			List<OCalendar> cals = cdao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			List<SchedulerEventInstance> sevs = new ArrayList<>();
			for(OCalendar cal : cals) {
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEventInstance(se));
				}
			}
			
			DateTime startDt, endDt;
			List<SchedulerEventInstance> recInstances = null;
			for(SchedulerEventInstance se : sevs) {
				// Ignore events that are not marked as busy!
				if(se.getBusy() != busy) continue;
				
				if(se.getRecurrenceId() == null) {
					startDt = se.getStartDate().withZone(userTz);
					endDt = se.getEndDate().withZone(userTz);
					hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
				} else {
					recInstances = calculateRecurringInstances(se, fromDate, toDate, userTz);
					for(SchedulerEventInstance recInstance : recInstances) {
						startDt = recInstance.getStartDate().withZone(userTz);
						endDt = recInstance.getEndDate().withZone(userTz);
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
		
		/*
		LocalDate date = fromDate;
		DateTime instant = null, boundaryInstant = null;
		while(date.compareTo(toDate) <= 0) {
			instant = new DateTime(tz).withDate(date).withTime(fromTime).withSecondOfMinute(0).withMillisOfSecond(0);
			boundaryInstant = new DateTime(tz).withDate(date).withTime(toTime).withSecondOfMinute(0).withMillisOfSecond(0);
			
			while(instant.compareTo(boundaryInstant) < 0) {
				hours.add(ymdhmZoneFmt.print(instant));
				instant = instant.plusMinutes(minRange);
			}
			date = date.plusDays(1);
		}
		*/
		
		return hours;
	}
	
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
	
	public String eventKeyByPublicUid(String eventPublicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			SchedulerEventInstance ve = getSchedulerEventByUid(con, eventPublicUid);
			return (ve == null) ? null : EventKey.buildKey(ve.getEventId(), ve.getOriginalEventId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
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
			checkRightsOnCalendarElements(calendarId, "CREATE"); // Rights check!
			if(mode.equals("copy")) checkRightsOnCalendarElements(calendarId, "DELETE"); // Rights check!
			
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Started at {0}", new DateTime()));
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Reading source file..."));
			ArrayList<EventReadResult> parsed = null;
			try {
				parsed = rea.listEvents(log, file);
			} catch(IOException | UnsupportedOperationException ex) {
				log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Unable to complete reading. Reason: {0}", ex.getMessage()));
				throw new WTException(ex);
			}
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "{0} event/s found!", parsed.size()));
			
			con = WT.getConnection(SERVICE_ID, false);
			
			if(mode.equals("copy")) {
				log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Cleaning previous events..."));
				int del = doDeleteEventsByCalendar(con, calendarId);
				log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "{0} event/s deleted!", del));
			}
			
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Importing..."));
			int count = 0;
			for(EventReadResult parse : parsed) {
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
					logger.trace("Error inserting event", ex);
					DbUtils.rollbackQuietly(con);
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", parse.event.getTitle(), parse.event.getPublicUid(), ex.getMessage()));
				}
			}
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "{0} event/s imported!", count));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(WTException ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Ended at {0}", new DateTime()));
		}
		return log;
	}
	
	/*
	public void importICal(int calendarId, InputStream is, DateTimeZone defaultTz) throws Exception {
		Connection con = null;
		HashMap<String, OEvent> uidMap = new HashMap<>();
		LogEntries log = new LogEntries();
		log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Started at {0}", new DateTime()));
		
		try {
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Parsing iCal file..."));
			ArrayList<ParseResult> parsed = null;
			try {
				parsed = ICalHelper.parseICal(log, is, defaultTz);
			} catch(ParserException | IOException ex) {
				log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Unable to complete parsing. Reason: {0}", ex.getMessage()));
				throw ex;
			}
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "{0} event/s found!", parsed.size()));
			
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Importing..."));
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
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Unable to import event [{0}, {1}]. Reason: {2}", parse.event.getTitle(), parse.event.getPublicUid(), ex.getMessage()));
				}
			}
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "{0} event/s imported!", count));
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			uidMap.clear();
			DbUtils.closeQuietly(con);
			log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Ended at {0}", new DateTime()));
			
			for(LogEntry entry : log) {
				logger.debug("{}", ((MessageLogEntry)entry).getMessage());
			}
		}
	}
	*/
	
	public void exportEvents(LogEntries log, String domainId, DateTime fromDate, DateTime toDate, OutputStream os) throws Exception {
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
				"customerId", "customerDescription"
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
			
			con = WT.getConnection(SERVICE_ID);
			ccon = WT.getCoreConnection();
			
			HashMap<String, Object> map = null;
			List<OCalendar> cals = calDao.selectByDomain(con, domainId);
			for(OCalendar cal : cals) {
				final UserProfile.Id pid = new UserProfile.Id(cal.getDomainId(), cal.getUserId());
				final OUser user = userDao.selectByDomainUser(ccon, cal.getDomainId(), cal.getUserId());
				if(user == null) throw new WTException("User [{0}] not found", pid.toString());
				final UserProfile.Data udata = WT.getUserData(pid);
				
				for(VSchedulerEvent vse : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					vse.updateCalculatedFields();
					final SchedulerEventInstance sei = new SchedulerEventInstance(vse);
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
						fillExportMapBasic(map, con, sei);
						fillExportMapDates(map, sei);
						mapw.write(map, headers, processors);
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Event skipped [{0}]. Reason: {1}", vse.getEventId(), ex.getMessage()));
					}
				}
				for(VSchedulerEvent vse : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					vse.updateCalculatedFields();
					final SchedulerEventInstance sei = new SchedulerEventInstance(vse);
					final List<SchedulerEventInstance> instances = calculateRecurringInstances(sei, fromDate, toDate, udata.getTimeZone());
					
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
						fillExportMapBasic(map, con, sei);
						for(SchedulerEventInstance inst : instances) {
							fillExportMapDates(map, sei);
							mapw.write(map, headers, processors);
						}	
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Event skipped [{0}]. Reason: {1}", vse.getEventId(), ex.getMessage()));
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
	
	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		HashMap<UserProfile.Id, Boolean> byEmailCache = new HashMap<>();
		EventDAO edao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			DateTime from = now.withTimeAtStartOfDay();
			DateTime remindOn = null;
			logger.debug("Getting expired events");
			List<SchedulerEventInstance> events = listExpiredSchedulerEvents(con, from, from.plusDays(7));
			for(SchedulerEventInstance event : events) {
				//TODO: implementare gestione reminder anche per le ricorrenze
				remindOn = event.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(event.getReminder());
				if(now.compareTo(remindOn) >= 0) {
					if(!byEmailCache.containsKey(event.getCalendarProfileId())) {
						CalendarUserSettings cus = new CalendarUserSettings(SERVICE_ID, event.getCalendarProfileId());
						boolean bool = cus.getEventReminderDelivery().equals(CalendarSettings.EVENT_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(event.getCalendarProfileId(), bool);
					}
					
					int ret = edao.updateRemindedOnIfNull(con, event.getEventId(), now);
					if(ret != 1) continue;
					
					if(byEmailCache.get(event.getCalendarProfileId())) {
						UserProfile.Data ud = WT.getUserData(event.getCalendarProfileId());
						alerts.add(createEventReminderAlertEmail(ud.getLocale(), event));
					} else {
						alerts.add(createEventReminderAlertWeb(event));
					}
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			logger.error("Error collecting reminder alerts", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
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
				if(attendee.getAttendeeId().equals(updatedAttendeeId)) {
					targetAttendee = attendee;
					break;
				}
			}
			if(targetAttendee == null) throw new WTException("Attendee not found [{0}]", updatedAttendeeId);
			
			InternetAddress from = WT.getNotificationAddress(targetDomainId);
			InternetAddress to = MailUtils.buildInternetAddress(event.getOrganizer());
			if(!MailUtils.isAddressValid(to)) throw new WTException("Organizer address not valid [{0}]", event.getOrganizer());
			
			String servicePublicUrl = WT.getServicePublicUrl(targetDomainId, SERVICE_ID);
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String subject = TplHelper.buildResponseUpdateEmailSubject(locale, event, targetAttendee);
			String customBody = TplHelper.buildResponseUpdateBodyTpl(locale, dateFormat, timeFormat, event, servicePublicUrl);
			
			String html = TplHelper.buildResponseUpdateTpl(locale, source, event.getTitle(), customBody, targetAttendee);
			WT.sendEmail(getTargetProfileId(), true, from, to, subject, html);
		} catch(Exception ex) {
			logger.warn("Unable to notify organizer", ex);
		}	
	}
	
	private void notifyAttendees(String crud, Event event) {
		try {
			// Finds attendees to be notified...
			ArrayList<EventAttendee> toBeNotified = new ArrayList<>();
			for(EventAttendee attendee : event.getAttendees()) {
				if(attendee.getNotify()) toBeNotified.add(attendee);
			}
			
			if(!toBeNotified.isEmpty()) {
				UserProfile.Data ud = WT.getUserData(getTargetProfileId());
				CoreUserSettings cus = new CoreUserSettings(getTargetProfileId());
				String dateFormat = cus.getShortDateFormat();
				String timeFormat = cus.getShortTimeFormat();
				
				boolean methodCancel = crud.equals(Crud.DELETE);
				
				// Creates ical content
				ArrayList<Event> events = new ArrayList<>();
				events.add(event);
				String prodId = ICalendarUtils.buildProdId(getProductName());
				Calendar ical = ICalHelper.exportICal(prodId, methodCancel, events);
				String icalText = ICalendarUtils.calendarToString(ical);
				
				// Creates message parts
				String filename = WT.getPlatformName().toLowerCase() + "-invite.ics";
				MimeBodyPart attPart = ICalendarUtils.createInvitationAttachmentPart(icalText, filename);
				MimeBodyPart calendarPart = ICalendarUtils.createInvitationCalendarPart(methodCancel, icalText);
				
				String source = NotificationHelper.buildSource(getLocale(), SERVICE_ID);
				String subject = TplHelper.buildEventInvitationEmailSubject(getLocale(), dateFormat, timeFormat, event, crud);
				String because = lookupResource(getLocale(), CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_BECAUSE);
				
				String servicePublicUrl = WT.getServicePublicUrl(getTargetProfileId().getDomainId(), SERVICE_ID);
				InternetAddress from = ud.getEmail();
				for(EventAttendee attendee : toBeNotified) {
					InternetAddress to = MailUtils.buildInternetAddress(attendee.getRecipient());
					if(MailUtils.isAddressValid(to)) {
						final String customBody = TplHelper.buildEventInvitationBodyTpl(getLocale(), dateFormat, timeFormat, event, crud, attendee.getAddress(), servicePublicUrl);
						final String html = TplHelper.buildInvitationTpl(getLocale(), source, attendee.getAddress(), event.getTitle(), customBody, because, crud);
						try {
							WT.sendEmail(getTargetProfileId(), true, from, new InternetAddress[]{to}, null, null, subject, html, new MimeBodyPart[]{attPart, calendarPart});
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
	
	
	
	private void fillExportMapDates(HashMap<String, Object> map, SchedulerEventInstance sei) throws Exception {
		DateTime startDt = sei.getStartDate().withZone(DateTimeZone.UTC);
		map.put("startDate", startDt);
		map.put("startTime", startDt);
		DateTime endDt = sei.getEndDate().withZone(DateTimeZone.UTC);
		map.put("endDate", endDt);
		map.put("endTime", endDt);
		map.put("timezone", sei.getTimezone());
		map.put("duration", Minutes.minutesBetween(sei.getEndDate(), sei.getStartDate()).size());
	}
	
	private void fillExportMapBasic(HashMap<String, Object> map, Connection con, SchedulerEventInstance sei) throws Exception {
		map.put("eventId", sei.getEventId());
		map.put("title", sei.getTitle());
		map.put("description", sei.getDescription());
		
		if(sei.getActivityId() != null) {
			ActivityDAO actDao = ActivityDAO.getInstance();
			OActivity activity = actDao.select(con, sei.getActivityId());
			if(activity == null) throw new WTException("Activity [{0}] not found", sei.getActivityId());

			map.put("activityId", activity.getActivityId());
			map.put("activityDescription", activity.getDescription());
			map.put("activityExternalId", activity.getExternalId());
		}
		
		if(sei.getCustomerId() != null) {
			CustomerDAO cusDao = CustomerDAO.getInstance();
			OCustomer customer = cusDao.viewById(con, sei.getCustomerId());
			map.put("customerId", customer.getCustomerId());
			map.put("customerDescription", customer.getDescription());
		}
		
		if(sei.getCausalId() != null) {
			CausalDAO cauDao = CausalDAO.getInstance();
			OCausal causal = cauDao.select(con, sei.getCausalId());
			if(causal == null) throw new WTException("Causal [{0}] not found", sei.getCausalId());
			
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
	private int calculateEventLengthInDays(OEvent event) {
		return Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
	}
	
	/**
	 * Fills passed dates hashmap within ones coming from specified event.
	 * If event starts on 21 Apr and ends on 25 Apr, 21->25 dates will be added to the set.
	 * @param dates
	 * @param event 
	 */
	private void addExpandedEventDates(HashSet<DateTime> dates, OEvent event) {
		int days = calculateEventLengthInDays(event)+1;
		DateTime date = event.getStartDate().withTimeAtStartOfDay();
		for(int count = 1; count <= days; count++) {
			dates.add(date);
			date = date.plusDays(1);
		}
	}
	
	private List<SchedulerEventInstance> calculateRecurringInstances(Connection con, SchedulerEventInstance event, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
		ArrayList<SchedulerEventInstance> events = new ArrayList<>();
		ORecurrence rec = null;
		List<ORecurrenceBroken> brokenRecs;
		HashMap<String, ORecurrenceBroken> brokenDates;
		PeriodList periods = null;
		
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
		
		// Retrieves reccurence and broken dates (if any)
		if(event.getRecurrenceId() == null) throw new WTException("Specified event [{}] does not have a recurrence set", event.getEventId());
		rec = rdao.select(con, event.getRecurrenceId());
		if(rec == null) throw new WTException("Unable to retrieve recurrence [{}]", event.getRecurrenceId());
		brokenRecs = rbdao.selectByEventRecurrence(con, event.getEventId(), event.getRecurrenceId());
		
		// Builds a hashset of broken dates for increasing performances
		brokenDates = new HashMap<>();
		for(ORecurrenceBroken brokenRec : brokenRecs) {
			brokenDates.put(brokenRec.getEventDate().toString(), brokenRec);
		}
		
		// If not present, updates rrule
		if(StringUtils.isEmpty(rec.getRule())) {
			rec.setRule(rec.buildRRule(DateTimeZone.UTC).getValue());
			rdao.updateRRule(con, rec.getRecurrenceId(), rec.getRule());
		}
		
		try {
			// Calculate event length in order to generate events like original one
			int eventDays = calculateEventLengthInDays(event);
			RRule rr = new RRule(rec.getRule());
			
			// Calcutate recurrence set for required dates range
			periods = ICal4jUtils.calculateRecurrenceSet(event.getStartDate(), event.getEndDate(), rec.getStartDate(), rr, fromDate, toDate, userTz);
			
			// Recurrence start is useful to skip undesired dates at beginning.
			// If event does not starts at recurrence real beginning (eg. event
			// start on MO but first recurrence begin on WE), ical4j lib includes 
			// those dates in calculated recurrence set, as stated in RFC 
			// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
			LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), userTz).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
			LocalDate rrEnd = rec.getUntilDate().toLocalDate();
			
			// Iterates returned recurring periods and builds cloned events...
			SchedulerEventInstance recEvent;
			LocalDate perStart, perEnd;
			DateTime newStart, newEnd;
			for(net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
				perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();
				
				if(brokenDates.containsKey(perStart.toString())) continue; // Skip broken dates...
				if((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
					newStart = event.getStartDate().withDate(perStart);
					newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					// Generate cloned event like original one
					recEvent = cloneEvent(event, newStart, newEnd);
					recEvent.setKey(EventKey.buildKey(event.getEventId(), event.getEventId(), perStart));
					events.add(recEvent);
				}
			}
			return events;
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to parse rrule");
		}
	}
	
	private String guessEventType(EventKey eventKey, OEvent original) {
		if(original.getRecurrenceId() == null) { // Normal event
			return EVENT_NORMAL;
		} else { // Event linked to a recurrence
			if(eventKey.eventId.equals(original.getEventId())) { // Recurring event
				return EVENT_RECURRING;
			} else { // Broken event
				return EVENT_BROKEN;
			}
		}
	}
	
	private OCalendar doInsertCalendar(Connection con, OCalendar item) throws WTException {
		CalendarDAO dao = CalendarDAO.getInstance();
		item.setCalendarId(dao.getSequence(con).intValue());
		if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
		dao.insert(con, item);
		return item;
	}
	
	private InsertResult doEventInsert(Connection con, Event event, boolean insertRecurrence, boolean insertAttendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		DateTime revision = createRevisionTimestamp();
		
		if(StringUtils.isBlank(event.getOrganizer())) event.setOrganizer(buildOrganizer());
		if(StringUtils.isBlank(event.getPublicUid())) {
			final String uid = ICalendarUtils.buildUid(IdentifierUtils.getUUIDTimeBased(), WT.getDomainInternetName(getTargetProfileId().getDomainId()));
			event.setPublicUid(uid);
			//event.setPublicUid(IdentifierUtils.getUUID());
		}
		
		OEvent oevt = new OEvent();
		oevt.fillFrom(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		
		ArrayList<OEventAttendee> oatts = new ArrayList<>();
		if(insertAttendees && event.hasAttendees()) {
			OEventAttendee oatt = null;
			for(EventAttendee att : event.getAttendees()) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
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
		EventDAO edao = EventDAO.getInstance();
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		DateTime revision = createRevisionTimestamp();
		
		if(StringUtils.isBlank(event.getOrganizer())) event.setOrganizer(buildOrganizer());
		
		originalEvent.fillFrom(event);
		if(attendees) {
			eadao.selectByEvent(con, originalEvent.getEventId());
			List<EventAttendee> fromList = createEventAttendeeList(eadao.selectByEvent(con, originalEvent.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(fromList, event.getAttendees());
			
			OEventAttendee oatt = null;
			for(EventAttendee att : changeSet.inserted) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(IdentifierUtils.getUUID());
				oatt.setEventId(originalEvent.getEventId());
				eadao.insert(con, oatt);
			}
			for(EventAttendee att : changeSet.updated) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				eadao.update(con, oatt);
			}
			for(EventAttendee att : changeSet.deleted) {
				eadao.delete(con, att.getAttendeeId());
			}
		}
		
		edao.update(con, originalEvent, revision);
		return originalEvent;
	}
	
	private void doDeleteEvent(Connection con, int eventId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		edao.logicDeleteById(con, eventId, createRevisionTimestamp());
		//TODO: cancellare reminder
		//TODO: se ricorrenza, eliminare tutte le broken dove newid!=null ??? Non servi più dato che verifico il D dell'evento ricorrente
	}
	
	private int doDeleteEventsByCalendar(Connection con, int calendarId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		return edao.logicDeleteByCalendarId(con, calendarId, createRevisionTimestamp());
		//TODO: cancellare reminder
	}
	
	private void doMoveEvent(Connection con, boolean copy, Event event, int targetCalendarId) throws WTException {
		if(copy) {
			event.setCalendarId(targetCalendarId);
			doEventInsert(con, event, true, true);
		} else {
			EventDAO edao = EventDAO.getInstance();
			edao.updateCalendar(con, event.getEventId(), targetCalendarId, createRevisionTimestamp());
		}
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate) throws DAOException {
		return doExcludeRecurrenceDate(con, recurringEvent, instanceDate, null);
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate, Integer brokenEventId) throws DAOException {
		RecurrenceBrokenDAO broDao = RecurrenceBrokenDAO.getInstance();
		// 1 - inserts a broken record on excluded date
		ORecurrenceBroken orb  = new ORecurrenceBroken();
		orb.setEventId(recurringEvent.getEventId());
		orb.setRecurrenceId(recurringEvent.getRecurrenceId());
		orb.setEventDate(instanceDate);
		orb.setNewEventId(brokenEventId);
		broDao.insert(con, orb);
		return orb;
	}
	
	private String buildOrganizer() {
		UserProfile.Data ud = WT.getUserData(getTargetProfileId());
		InternetAddress ia = MailUtils.buildInternetAddress(ud.getEmail().getAddress(), ud.getDisplayName());
		return ia.toString();
	}
	
	private void buildShareCache() {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		
		try {
			cacheOwnerToRootShare.clear();
			cacheOwnerToWildcardFolderShare.clear();
			cacheCalendarToFolderShare.clear();
			for(CalendarRoot root : listIncomingCalendarRoots()) {
				cacheOwnerToRootShare.put(root.getOwnerProfileId(), root.getShareId());
				for(OShare folder : core.listIncomingShareFolders(root.getShareId(), GROUPNAME_CALENDAR)) {
					if(folder.hasWildcard()) {
						UserProfile.Id ownerId = core.userUidToProfileId(folder.getUserUid());
						cacheOwnerToWildcardFolderShare.put(ownerId, folder.getShareId().toString());
					} else {
						cacheCalendarToFolderShare.put(Integer.valueOf(folder.getInstance()), folder.getShareId().toString());
					}
				}
			}
		} catch(WTException ex) {
			throw new WTRuntimeException(ex.getMessage());
		}
	}
	
	private String ownerToRootShareId(UserProfile.Id owner) {
		synchronized(shareCacheLock) {
			if(!cacheOwnerToRootShare.containsKey(owner)) buildShareCache();
			return cacheOwnerToRootShare.get(owner);
		}
	}
	
	private String ownerToWildcardFolderShareId(UserProfile.Id ownerPid) {
		synchronized(shareCacheLock) {
			if(!cacheOwnerToWildcardFolderShare.containsKey(ownerPid) && cacheOwnerToRootShare.isEmpty()) buildShareCache();
			return cacheOwnerToWildcardFolderShare.get(ownerPid);
		}
	}
	
	private String calendarToFolderShareId(int calendarId) {
		synchronized(shareCacheLock) {
			if(!cacheCalendarToFolderShare.containsKey(calendarId)) buildShareCache();
			return cacheCalendarToFolderShare.get(calendarId);
		}
	}
	
	private UserProfile.Id calendarToOwner(int calendarId) {
		synchronized(cacheCalendarToOwner) {
			if(cacheCalendarToOwner.containsKey(calendarId)) {
				return cacheCalendarToOwner.get(calendarId);
			} else {
				try {
					UserProfile.Id owner = findCalendarOwner(calendarId);
					cacheCalendarToOwner.put(calendarId, owner);
					return owner;
				} catch(WTException ex) {
					throw new WTRuntimeException(ex.getMessage());
				}
			}
		}
	}
	
	private void checkRightsOnCalendarRoot(UserProfile.Id ownerPid, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		if(ownerPid.equals(targetPid)) return;
		
		String shareId = ownerToRootShareId(ownerPid);
		if(shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", ownerPid);
		CoreManager core = WT.getCoreManager(targetPid);
		if(core.isShareRootPermitted(shareId, action)) return;
		//if(core.isShareRootPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private void checkRightsOnCalendarFolder(int calendarId, String action) throws WTException {
		if(RunContext.isWebTopAdmin()) return;
		UserProfile.Id targetPid = getTargetProfileId();
		
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = calendarToOwner(calendarId);
		if(ownerPid.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareFolderPermitted(wildcardShareId, action)) return;
			//if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = calendarToFolderShareId(calendarId);
		if(shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if(core.isShareFolderPermitted(shareId, action)) return;
		//if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folder share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private void checkRightsOnCalendarElements(int calendarId, String action) throws WTException {
		if(RunContext.isWebTopAdmin()) return;
		UserProfile.Id targetPid = getTargetProfileId();
		
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = calendarToOwner(calendarId);
		if(ownerPid.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareElementsPermitted(wildcardShareId, action)) return;
			//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = calendarToFolderShareId(calendarId);
		if(shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if(core.isShareElementsPermitted(shareId, action)) return;
		//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on elements share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CALENDAR, targetPid.toString());
	}
	
	private UserProfile.Id findCalendarOwner(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO dao = CalendarDAO.getInstance();
			Owner owner = dao.selectOwnerById(con, calendarId);
			if(owner == null) throw new WTException("Calendar not found [{0}]", calendarId);
			return new UserProfile.Id(owner.getDomainId(), owner.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private SchedulerEventInstance cloneEvent(SchedulerEventInstance sourceEvent, DateTime newStart, DateTime newEnd) {
		SchedulerEventInstance event = new SchedulerEventInstance(sourceEvent);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private Event createEvent(OEvent oevt) {
		Event evt = new Event();
		evt.setEventId(oevt.getEventId());
		evt.setCalendarId(oevt.getCalendarId());
		evt.setRevisionTimestamp(oevt.getRevisionTimestamp());
		evt.setPublicUid(oevt.getPublicUid());
		evt.setStartDate(oevt.getStartDate());
		evt.setEndDate(oevt.getEndDate());
		evt.setTimezone(oevt.getTimezone());
		evt.setAllDay(oevt.getAllDay());
		evt.setOrganizer(oevt.getOrganizer());
		evt.setTitle(oevt.getTitle());
		evt.setDescription(oevt.getDescription());
		evt.setLocation(oevt.getLocation());
		evt.setIsPrivate(oevt.getIsPrivate());
		evt.setBusy(oevt.getBusy());
		evt.setReminder(oevt.getReminder());
		evt.setActivityId(oevt.getActivityId());
		evt.setCustomerId(oevt.getCustomerId());
		evt.setStatisticId(oevt.getStatisticId());
		evt.setCausalId(oevt.getCausalId());
		return evt;
	}
	
	private EventInstance createEventInstance(String key, RecurringInfo recurringInfo, VSchedulerEvent se) {
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
		evti.setActivityId(se.getActivityId());
		evti.setCustomerId(se.getCustomerId());
		evti.setStatisticId(se.getStatisticId());
		evti.setCausalId(se.getCausalId());
		return evti;
	}
	
	private Recurrence createEventRecurrence(ORecurrence orec) {
		if (orec == null) return null;
		Recurrence rec = new Recurrence();
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
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(orec.getRepeat());
		} else if(orec.isEndUntil()) {
			rec.setEndsMode(Recurrence.ENDS_MODE_UNTIL);
		} else if(orec.isEndNever()) {
			rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
		} else {
			throw new WTRuntimeException("Unable to set a valid endMode");
		}
		
		rec.setRRule(orec.getRule());
		return rec;
	}
	
	private EventAttendee createEventAttendee(OEventAttendee attendee) {
		EventAttendee att = new EventAttendee();
		att.setAttendeeId(attendee.getAttendeeId());
		att.setRecipient(attendee.getRecipient());
		att.setRecipientType(attendee.getRecipientType());
		att.setRecipientRole(attendee.getRecipientRole());
		att.setResponseStatus(attendee.getResponseStatus());
		att.setNotify(attendee.getNotify());
		return att;
	}
	
	private List<EventAttendee> createEventAttendeeList(List<OEventAttendee> attendees) {
		ArrayList<EventAttendee> atts = new ArrayList<>();
		for(OEventAttendee attendee : attendees) {
			atts.add(createEventAttendee(attendee));
		}
		return atts;
	}
	
	private ReminderInApp createEventReminderAlertWeb(SchedulerEventInstance event) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		alert.setTitle(event.getTitle());
		alert.setDate(event.getStartDate().withZone(event.getDateTimeZone()));
		alert.setTimezone(event.getTimezone());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, SchedulerEventInstance event) {
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
	
	public static class CalendarEvents {
		public final OCalendar calendar;
		public final List<SchedulerEventInstance> events;
		
		public CalendarEvents(OCalendar calendar, List<SchedulerEventInstance> events) {
			this.calendar = calendar;
			this.events = events;
		}
	}
}
