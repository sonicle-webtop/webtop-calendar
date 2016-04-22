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
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.security.DomainAccount;
import com.sonicle.webtop.calendar.bol.model.EventBase;
import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.model.CalendarFolder;
import com.sonicle.webtop.calendar.bol.model.CalendarRoot;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.calendar.io.EventFileReader;
import com.sonicle.webtop.calendar.io.EventReadResult;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
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
import com.sonicle.webtop.core.app.RunContext;
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
import com.sonicle.webtop.core.sdk.WTOperationException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import com.sonicle.webtop.core.util.NotificationHelper;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.io.IOUtils;
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

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseManager {
	public static final Logger logger = WT.getLogger(CalendarManager.class);
	private static final String RESOURCE_CALENDAR = "CALENDAR";
	private static final String EVENT_NORMAL = "normal";
	private static final String EVENT_BROKEN = "broken";
	private static final String EVENT_RECURRING = "recurring";
	public static final String TARGET_THIS = "this";
	public static final String TARGET_SINCE = "since";
	public static final String TARGET_ALL = "all";
	
	private final HashMap<Integer, UserProfile.Id> cacheCalendarToOwner = new HashMap<>();
	private final Object shareCacheLock = new Object();
	private final HashMap<UserProfile.Id, String> cacheOwnerToRootShare = new HashMap<>();
	private final HashMap<UserProfile.Id, String> cacheOwnerToWildcardFolderShare = new HashMap<>();
	private final HashMap<Integer, String> cacheCalendarToFolderShare = new HashMap<>();

	public CalendarManager(RunContext context) {
		super(context);
	}
	
	public CalendarManager(RunContext context, UserProfile.Id targetProfileId) {
		super(context, targetProfileId);
	}
	
	private void writeLog(String action, String data) {
		CoreManager core = WT.getCoreManager(getRunContext());
		core.setSoftwareName(getSoftwareName());
		core.writeLog(action, data);
	}
	
	public static DateTime parseYmdHmsWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date + " " + time, tz);
	}
	
	public static DateTime parseYmdHmsWithZone(String dateTime, DateTimeZone tz) {
		String dt = StringUtils.replace(dateTime, "T", " ");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return formatter.parseDateTime(dt);
	}
	
	private void checkRightsOnCalendarRoot(UserProfile.Id ownerPid, String action) throws WTException {
		if(WT.isWebTopAdmin(getRunProfileId())) return;
		if(ownerPid.equals(getTargetProfileId())) return;
		
		String shareId = ownerToRootShareId(ownerPid);
		if(shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", ownerPid);
		CoreManager core = WT.getCoreManager(getRunContext());
		if(core.isShareRootPermitted(getRunProfileId(), SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CALENDAR, getRunProfileId().toString());
	}
	
	public List<CalendarRoot> listIncomingCalendarRoots() throws WTException {
		CoreManager core = WT.getCoreManager(getRunContext());
		ArrayList<CalendarRoot> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		
		List<IncomingShareRoot> shares = core.listIncomingShareRoots(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR);
		for(IncomingShareRoot share : shares) {
			SharePermsRoot perms = core.getShareRootPermissions(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR, share.getShareId());
			CalendarRoot root = new CalendarRoot(share, perms);
			if(hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public HashMap<Integer, CalendarFolder> listIncomingCalendarFolders(String rootShareId) throws WTException {
		CoreManager core = WT.getCoreManager(getRunContext());
		LinkedHashMap<Integer, CalendarFolder> folders = new LinkedHashMap<>();
		UserProfile.Id pid = getTargetProfileId();
		
		// Retrieves incoming folders (from sharing). This lookup already 
		// returns readable shares (we don't need to test READ permission)
		List<OShare> shares = core.listIncomingShareFolders(pid, rootShareId, SERVICE_ID, RESOURCE_CALENDAR);
		for(OShare share : shares) {
			
			List<OCalendar> cals = null;
			if(share.hasWildcard()) {
				UserProfile.Id ownerId = core.userUidToProfileId(share.getUserUid());
				cals = listCalendars(ownerId);
			} else {
				cals = Arrays.asList(getCalendar(Integer.valueOf(share.getInstance())));
			}
			
			for(OCalendar cal : cals) {
				SharePermsFolder fperms = core.getShareFolderPermissions(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR, share.getShareId().toString());
				SharePermsElements eperms = core.getShareElementsPermissions(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR, share.getShareId().toString());
				
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
		CoreManager core = WT.getCoreManager(getRunContext());
		return core.getSharing(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager core = WT.getCoreManager(getRunContext());
		core.updateSharing(getTargetProfileId(), SERVICE_ID, RESOURCE_CALENDAR, sharing);
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
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
		Connection con = null;
		OCalendar item = null;
		
		try {
			checkRightsOnCalendarRoot(getTargetProfileId(), "MANAGE");
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			CalendarDAO dao = CalendarDAO.getInstance();
			
			item = dao.selectBuiltInByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(item != null) throw new WTOperationException("Built-in calendar already present");
			
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
			item.setSync(true);
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
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
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
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
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
			List<SchedulerEvent> expEvents = null;
			for(OCalendar cal : cals) {
				checkRightsOnCalendarFolder(cal.getCalendarId(), "READ");
				for(VSchedulerEvent se : edao.viewDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					addExpandedEventDates(dates, se);
				}
				for(VSchedulerEvent se : edao.viewRecurringDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					expEvents = CalendarManager.this.calculateRecurringInstances(con, new SchedulerEvent(se), fromDate, toDate, userTz);
					for(SchedulerEvent expEvent : expEvents) {
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
	
	private SchedulerEvent getSchedulerEventByUid(Connection con, String eventPublicUid) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		ve.updateCalculatedFields();
		return new SchedulerEvent(ve);
	}
	
	public SchedulerEvent getSchedulerEvent(Integer eventId) throws WTException {
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
	
	private SchedulerEvent getSchedulerEvent(Connection con, Integer eventId) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent se = edao.view(con, eventId);
		se.updateCalculatedFields();
		return new SchedulerEvent(se);
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
			List<SchedulerEvent> sevs = null;
			for(OCalendar cal : cals) {
				checkRightsOnCalendarFolder(cal.getCalendarId(), "READ");
				
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
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
	
	private List<SchedulerEvent> listExpiredSchedulerEvents(Connection con, DateTime fromDate, DateTime toDate) throws WTException {
		//TODO: auth
		List<SchedulerEvent> sevs = new ArrayList<>();
		EventDAO edao = EventDAO.getInstance();
		
		for(VSchedulerEvent se : edao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			se.updateCalculatedFields();
			sevs.add(new SchedulerEvent(se));
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
			List<SchedulerEvent> sevs = null;
			for(OCalendar cal : cals) {
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.searchByCalendarQuery(con, cal.getCalendarId(), query)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.searchRecurringByCalendarQuery(con, cal.getCalendarId(), query)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
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
	
	public List<SchedulerEvent> calculateRecurringInstances(SchedulerEvent recurringEvent, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
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
	
	public EventBase getEventByPublicUid(String publicUid) throws WTException {
		String eventKey = eventKeyByPublicUid(publicUid);
		if(eventKey == null) throw new WTException("Unable to find event with UID [{0}]", publicUid);
		return getEvent(eventKey);
	}
	
	public Event getEvent(String eventKey) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			
			OEvent original = edao.selectById(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			checkRightsOnCalendarFolder(original.getCalendarId(), "READ"); // Rights check!
			
			VSchedulerEvent se = edao.view(con, ekey.eventId);
			se.updateCalculatedFields(); // TODO: Serve??????????????????????
			if(se == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			
			Event evt = null;
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				evt = createEvent(eventKey, EventBase.RecurringInfo.SINGLE, se);
				evt.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
				
			} else if(type.equals(EVENT_RECURRING)) {
				ORecurrence rec = rdao.select(con, se.getRecurrenceId());
				if(rec == null) throw new WTException("Unable to retrieve recurrence [{}]", ekey.originalEventId);
				
				evt = createEvent(eventKey, EventBase.RecurringInfo.RECURRING, se);
				int eventDays = calculateEventLengthInDays(se);
				evt.setStartDate(evt.getStartDate().withDate(ekey.instanceDate));
				evt.setEndDate(evt.getEndDate().withDate(evt.getStartDate().plusDays(eventDays).toLocalDate()));
				evt.setRecurrence(createEventRecurrence(rec));
				
			} else if(type.equals(EVENT_BROKEN)) {
				//TODO: recuperare il record ricorrenza per verifica?
				evt = createEvent(eventKey, EventBase.RecurringInfo.BROKEN, se);
				evt.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
			}
			
			return evt;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addEvent(EventBase event) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			InsertResult insert = doEventInsert(con, event, true, true);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
			
			//TODO: gestire le notifiche invito per gli eventi ricorrenti
			// Handle attendees invitation
			if(!insert.attendees.isEmpty()) { // Checking it here avoids unuseful calls!
				if(insert.recurrence == null) {
					String eventKey = EventKey.buildKey(insert.event.getEventId(), insert.event.getEventId());
					notifyAttendees(Crud.CREATE, getEvent(eventKey));
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
	
	public void editEvent(String target, Event event) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEvent(event.getKey()));
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = edao.selectById(con, ekey.eventId);
					if(oevt == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
					doEventUpdate(con, oevt, event, true);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_UPDATE", String.valueOf(ekey.eventId));
					// Handle attendees invitation
					notifyAttendees(Crud.UPDATE, getEvent(event.getKey()));
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
	
	public void cloneEvent(String eventKey, DateTime startDate, DateTime endDate) throws WTException {
		Connection con = null;
		
		try {
			EventBase event = getEvent(eventKey);
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			
			event.setStartDate(startDate);
			event.setEndDate(endDate);
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
	
	public void updateEvent(String eventKey, DateTime startDate, DateTime endDate, String title) throws WTException {
		EventDAO edao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
	
	public void deleteEvent(String target, String eventKey) throws WTException {
		EventDAO evtdao = EventDAO.getInstance();
		RecurrenceDAO recdao = RecurrenceDAO.getInstance();
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			OEvent originalEvent = evtdao.selectById(con, ekey.originalEventId);
			if(originalEvent == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			checkRightsOnCalendarElements(originalEvent.getCalendarId(), "DELETE");
			
			String type = guessEventType(ekey, originalEvent);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					if(!ekey.eventId.equals(originalEvent.getEventId())) throw new WTException("In this case both ids must be equals");
					doDeleteEvent(con, ekey.eventId);
					
					DbUtils.commitQuietly(con);
					writeLog("EVENT_DELETE", String.valueOf(originalEvent.getEventId()));
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
	
	public void restoreEvent(String eventKey) throws WTException {
		Connection con = null;
		
		try {
			// Scheduler IDs always contains an eventId reference.
			// For broken events extracted eventId is not equal to extracted
			// originalEventId (event generator or database event), since
			// it belongs to the recurring event
			EventKey ekey = new EventKey(eventKey);
			if(ekey.originalEventId.equals(ekey.eventId)) throw new WTException("Cannot restore an event that is not broken");
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
	
	public void moveEvent(boolean copy, String eventKey, int targetCalendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			EventBase evt = getEvent(eventKey);
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
			List<SchedulerEvent> sevs = new ArrayList<>();
			for(OCalendar cal : cals) {
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
			}
			
			DateTime startDt, endDt;
			List<SchedulerEvent> recInstances = null;
			for(SchedulerEvent se : sevs) {
				// Ignore events that are not marked as busy!
				if(se.getBusy() != busy) continue;
				
				if(se.getRecurrenceId() == null) {
					startDt = se.getStartDate().withZone(userTz);
					endDt = se.getEndDate().withZone(userTz);
					hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
				} else {
					recInstances = calculateRecurringInstances(se, fromDate, toDate, userTz);
					for(SchedulerEvent recInstance : recInstances) {
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
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
			SchedulerEvent ve = getSchedulerEventByUid(con, eventPublicUid);
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
	
	public EventBase updateEventAttendeeReply(String eventUid, String attendeeUid, String response) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			SchedulerEvent ve = getSchedulerEventByUid(con, eventUid);
			
			EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
			int ret = eadao.updateAttendeeResponse(con, attendeeUid, ve.getEventId(), response);
			return (ret == 1) ? getEvent(ve.getKey()) : null;
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
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
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
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
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
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
			List<SchedulerEvent> instances = null;
			SchedulerEvent se = null;
			List<OCalendar> cals = calDao.selectByDomain(con, domainId);
			for(OCalendar cal : cals) {
				OUser user = userDao.selectByDomainUser(ccon, cal.getDomainId(), cal.getUserId());
				if(user == null) throw new WTException("User [{0}] not found", DomainAccount.buildName(cal.getDomainId(), cal.getUserId()));
				
				for(VSchedulerEvent vse : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					vse.updateCalculatedFields();
					se = new SchedulerEvent(vse);
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
						fillExportMapBasic(map, con, se);
						fillExportMapDates(map, se);
						mapw.write(map, headers, processors);
						
					} catch(Exception ex) {
						log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "Event skipped [{0}]. Reason: {1}", vse.getEventId(), ex.getMessage()));
					}
				}
				for(VSchedulerEvent vse : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					vse.updateCalculatedFields();
					se = new SchedulerEvent(vse);
					instances = calculateRecurringInstances(se, fromDate, toDate, user.getTimeZone());
					
					try {
						map = new HashMap<>();
						map.put("userId", user.getUserId());
						map.put("descriptionId", user.getDisplayName());
						fillExportMapBasic(map, con, se);
						for(SchedulerEvent inst : instances) {
							fillExportMapDates(map, se);
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
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			DateTime from = now.withTimeAtStartOfDay();
			DateTime remindOn = null;
			logger.debug("Getting expired events");
			List<SchedulerEvent> events = listExpiredSchedulerEvents(con, from, from.plusDays(7));
			for(SchedulerEvent event : events) {
				//TODO: implementare gestione reminder anche per le ricorrenze
				remindOn = event.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(event.getReminder());
				if(now.compareTo(remindOn) >= 0) {
					if(!byEmailCache.containsKey(event.getCalendarProfileId())) {
						CalendarUserSettings cus = new CalendarUserSettings(SERVICE_ID, event.getCalendarProfileId());
						boolean bool = cus.getEventReminderDelivery().equals(CalendarUserSettings.EVENT_REMINDER_DELIVERY_EMAIL);
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
	
	private void notifyAttendees(String crud, EventBase event) {
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
				InternetAddress from = ud.getEmail();
				String subjectFmt = lookupResource(getLocale(), MessageFormat.format(CalendarLocale.INVITATION_SUBJECT_X, crud));
				String subject = MessageFormat.format(subjectFmt, buildEventInvitationEmailSubject(getLocale(), dateFormat, timeFormat, event));
				
				// Creates ical content
				String icalText = null;
				ByteArrayOutputStream baos = null;
				try {
					baos = new ByteArrayOutputStream();
					ArrayList<EventBase> events = new ArrayList<>();
					events.add(event);
					String prodId = ICalHelper.buildProdId(WT.getPlatformName(), "Calendar");
					ICalHelper.exportICal(prodId, methodCancel, events, baos);
					icalText = baos.toString("UTF8");
				} finally {
					IOUtils.closeQuietly(baos);
				}
				
				// Creates message parts
				//String filename = "calendar-invite.ics";
				String filename = MessageFormat.format("{0}-invite.ics", WT.getPlatformName().toLowerCase());
				MimeBodyPart icsPart = ICalHelper.createInvitationICalPart(icalText, filename);
				MimeBodyPart calendarPart = ICalHelper.createInvitationCalendarPart(methodCancel, icalText, filename);
				
				for(EventAttendee attendee : toBeNotified) {
					InternetAddress to = new InternetAddress(attendee.getRecipient());
					if(MailUtils.isAddressValid(to)) {
						String body = buildEventInvitationEmailBody(getLocale(), dateFormat, timeFormat, attendee.getAddress(), event);
						try {
							WT.sendEmail(getTargetProfileId(), true, from, new InternetAddress[]{to}, null, null, subject, body, new MimeBodyPart[]{icsPart, calendarPart});
						} catch(MessagingException ex) {
							logger.warn("Problems encountered sending notification to {}", to.toString());
						}
					}
				}
			}
		} catch(Exception ex) {
			logger.error("Error notifying attendees", ex);
		}		
	}
	
	public String buildEventInvitationEmailSubject(Locale locale, String dateFormat, String timeFormat, EventBase event) {
		DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat + " " + timeFormat, DateTimeZone.forID(event.getTimezone()));
		StringBuilder sb = new StringBuilder();
		
		sb.append(StringUtils.abbreviate(event.getTitle(), 30));
		sb.append(" - ");
		sb.append(fmt.print(event.getStartDate()));
		sb.append(" -> ");
		sb.append(fmt.print(event.getEndDate()));
		sb.append(" (");
		sb.append(event.getTimezone());
		sb.append(")");
		
		return sb.toString();
	}
	
	public String buildEventInvitationEmailBody(Locale locale, String dateFormat, String timeFormat, String recipientEmail, EventBase event) {
		try {
			OCalendar calendar = getCalendar(event.getCalendarId());
			if(calendar == null) throw new WTException("Calendar not found [{0}]", event.getCalendarId());
			
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String because = lookupResource(locale, CalendarLocale.TPL_INVITATION_FOOTER_BECAUSE);
			
			MapItem i18nMap = new MapItem();
			i18nMap.putAll(TplHelper.generateFooterI18nStrings(SERVICE_ID, locale, source, recipientEmail, because));
			i18nMap.putAll(TplHelper.generateEventI18nStrings(SERVICE_ID, locale));
			
			DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat + " " + timeFormat, DateTimeZone.forID(event.getTimezone()));
			MapItem eventMap = new MapItem();
			eventMap.put("title", StringUtils.defaultIfBlank(event.getTitle(), ""));
			eventMap.put("description", StringUtils.defaultIfBlank(event.getDescription(), ""));
			eventMap.put("timezone", event.getTimezone());
			eventMap.put("startDate", fmt.print(event.getStartDate()));
			eventMap.put("endDate", fmt.print(event.getEndDate()));
			eventMap.put("location", StringUtils.defaultIfBlank(event.getLocation(), null));
			eventMap.put("calendarName", calendar.getName());
			eventMap.put("organizer", StringUtils.defaultIfBlank(event.getOrganizerCN(), event.getOrganizerAddress()));
			
			MapItemList eventAttendees = new MapItemList();
			for(EventAttendee attendee : event.getAttendees()) {
				MapItem item = new MapItem();
				String cn = attendee.getCN();
				String address = attendee.getAddress();
				item.put("cn", StringUtils.isBlank(cn) ? null : cn);
				item.put("address", StringUtils.isBlank(address) ? null : address);
				eventAttendees.add(item);
			}
			
			MapItem map = new MapItem();
			map.put("i18n", i18nMap);
			map.put("event", eventMap);
			map.put("eventAttendees", eventAttendees);
			map.put("recipientEmail", StringUtils.defaultString(recipientEmail));
			return WT.buildTemplate(SERVICE_ID, "tpl/event-invitation.html", map);
			
		} catch(IOException | TemplateException | AddressException | WTException ex) {
			logger.error("Error generating body", ex);
			return null;
		}
	}
	
	
	
	
	private void fillExportMapDates(HashMap<String, Object> map, SchedulerEvent se) throws Exception {
		DateTime startDt = se.getStartDate().withZone(DateTimeZone.UTC);
		map.put("startDate", startDt);
		map.put("startTime", startDt);
		DateTime endDt = se.getEndDate().withZone(DateTimeZone.UTC);
		map.put("endDate", endDt);
		map.put("endTime", endDt);
		map.put("timezone", se.getTimezone());
		map.put("duration", Minutes.minutesBetween(se.getEndDate(), se.getStartDate()).size());
	}
	
	private void fillExportMapBasic(HashMap<String, Object> map, Connection con, SchedulerEvent se) throws Exception {
		map.put("eventId", se.getEventId());
		map.put("title", se.getTitle());
		map.put("description", se.getDescription());
		
		if(se.getActivityId() != null) {
			ActivityDAO actDao = ActivityDAO.getInstance();
			OActivity activity = actDao.select(con, se.getActivityId());
			if(activity == null) throw new WTException("Activity [{0}] not found", se.getActivityId());

			map.put("activityId", activity.getActivityId());
			map.put("activityDescription", activity.getDescription());
			map.put("activityExternalId", activity.getExternalId());
		}
		
		if(se.getCustomerId() != null) {
			CustomerDAO cusDao = CustomerDAO.getInstance();
			OCustomer customer = cusDao.viewById(con, se.getCustomerId());
			map.put("customerId", customer.getCustomerId());
			map.put("customerDescription", customer.getDescription());
		}
		
		if(se.getCausalId() != null) {
			CausalDAO cauDao = CausalDAO.getInstance();
			OCausal causal = cauDao.select(con, se.getCausalId());
			if(causal == null) throw new WTException("Causal [{0}] not found", se.getCausalId());
			
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
	
	private List<SchedulerEvent> calculateRecurringInstances(Connection con, SchedulerEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws WTException {
		ArrayList<SchedulerEvent> events = new ArrayList<>();
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
			SchedulerEvent recEvent;
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
	
	private InsertResult doEventInsert(Connection con, EventBase event, boolean recurrence, boolean attendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		DateTime revision = createRevisionTimestamp();
		
		if(StringUtils.isBlank(event.getOrganizer())) event.setOrganizer(buildOrganizer());
		if(StringUtils.isBlank(event.getPublicUid())) event.setPublicUid(IdentifierUtils.getUUID());
		
		OEvent oevt = new OEvent();
		oevt.fillFrom(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		
		ArrayList<OEventAttendee> oatts = new ArrayList<>();
		if(attendees && event.hasAttendees()) {
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
		if(recurrence && event.hasRecurrence()) {
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
	
	private OEvent doEventUpdate(Connection con, OEvent originalEvent, EventBase event, boolean attendees) throws WTException {
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
			for(EventAttendee att : changeSet.created) {
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
	
	private void doMoveEvent(Connection con, boolean copy, EventBase event, int targetCalendarId) throws WTException {
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
		CoreManager core = WT.getCoreManager(getRunContext());
		UserProfile.Id pid = getTargetProfileId();
		try {
			cacheOwnerToRootShare.clear();
			cacheOwnerToWildcardFolderShare.clear();
			cacheCalendarToFolderShare.clear();
			for(CalendarRoot root : listIncomingCalendarRoots()) {
				cacheOwnerToRootShare.put(root.getOwnerProfileId(), root.getShareId());
				for(OShare folder : core.listIncomingShareFolders(pid, root.getShareId(), SERVICE_ID, RESOURCE_CALENDAR)) {
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
	
	private void checkRightsOnCalendarFolder(int calendarId, String action) throws WTException {
		if(WT.isWebTopAdmin(getRunProfileId())) return;
		
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = calendarToOwner(calendarId);
		if(ownerPid.equals(getTargetProfileId())) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(getRunContext());
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareFolderPermitted(getRunProfileId(), SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = calendarToFolderShareId(calendarId);
		if(shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if(core.isShareFolderPermitted(getRunProfileId(), SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folder share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CALENDAR, getRunProfileId().toString());
	}
	
	private void checkRightsOnCalendarElements(int calendarId, String action) throws WTException {
		if(WT.isWebTopAdmin(getRunProfileId())) return;
		
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = calendarToOwner(calendarId);
		if(ownerPid.equals(getTargetProfileId())) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(getRunContext());
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareElementsPermitted(getRunProfileId(), SERVICE_ID, RESOURCE_CALENDAR, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = calendarToFolderShareId(calendarId);
		if(shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if(core.isShareElementsPermitted(getRunProfileId(), SERVICE_ID, RESOURCE_CALENDAR, action, shareId)) return;
		
		throw new AuthException("Action not allowed on elements share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CALENDAR, getRunProfileId().toString());
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
	
	private SchedulerEvent cloneEvent(SchedulerEvent source, DateTime newStart, DateTime newEnd) {
		SchedulerEvent event = new SchedulerEvent(source);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private Event createEvent(String key, EventBase.RecurringInfo recurringInfo, VSchedulerEvent se) {
		Event event = new Event(key, recurringInfo, se);
		event.setEventId(se.getEventId());
		event.setPublicUid(se.getPublicUid());
		event.setCalendarId(se.getCalendarId());
		event.setStartDate(se.getStartDate());
		event.setEndDate(se.getEndDate());
		event.setTimezone(se.getTimezone());
		event.setAllDay(se.getAllDay());
		event.setTitle(se.getTitle());
		event.setDescription(se.getDescription());
		event.setLocation(se.getLocation());
		event.setIsPrivate(se.getIsPrivate());
		event.setBusy(se.getBusy());
		event.setReminder(se.getReminder());
		event.setOrganizer(se.getOrganizer());
		event.setActivityId(se.getActivityId());
		event.setCustomerId(se.getCustomerId());
		event.setStatisticId(se.getStatisticId());
		event.setCausalId(se.getCausalId());
		event.setRevisionTimestamp(se.getRevisionTimestamp());
		return event;
	}
	
	private Recurrence createEventRecurrence(ORecurrence recurrence) {
		Recurrence rec = new Recurrence();
		
		rec.setType(recurrence.getType());
		rec.setDailyFreq(recurrence.getDailyFreq());
		rec.setWeeklyFreq(recurrence.getWeeklyFreq());
		rec.setWeeklyDay1(recurrence.getWeeklyDay_1());
		rec.setWeeklyDay2(recurrence.getWeeklyDay_2());
		rec.setWeeklyDay3(recurrence.getWeeklyDay_3());
		rec.setWeeklyDay4(recurrence.getWeeklyDay_4());
		rec.setWeeklyDay5(recurrence.getWeeklyDay_5());
		rec.setWeeklyDay6(recurrence.getWeeklyDay_6());
		rec.setWeeklyDay7(recurrence.getWeeklyDay_7());
		rec.setMonthlyFreq(recurrence.getMonthlyFreq());
		rec.setMonthlyDay(recurrence.getMonthlyDay());
		rec.setYearlyFreq(recurrence.getYearlyFreq());
		rec.setYearlyDay(recurrence.getYearlyDay());
		
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
		
		rec.setUntilDate(recurrence.getUntilDate());
		if(recurrence.isEndRepeat()) {
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(recurrence.getRepeat());
		} else if(recurrence.isEndUntil()) {
			rec.setEndsMode(Recurrence.ENDS_MODE_UNTIL);
		} else if(recurrence.isEndNever()) {
			rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
		} else {
			throw new WTRuntimeException("Unable to set a valid endMode");
		}
		
		rec.setRRule(recurrence.getRule());
		return rec;
	}
	
	private EventAttendee createEventAttendee(OEventAttendee attendee) {
		EventAttendee att = new EventAttendee();
		att.setAttendeeId(attendee.getAttendeeId());
		att.setRecipient(attendee.getRecipient());
		att.setRecipientType(attendee.getRecipientType());
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
	
	private ReminderInApp createEventReminderAlertWeb(SchedulerEvent event) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		alert.setTitle(event.getTitle());
		alert.setDate(event.getStartDate().withZone(event.getDateTimeZone()));
		alert.setTimezone(event.getTimezone());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, SchedulerEvent event) {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, event.getCalendarProfileId(), "event", event.getKey());
		//TODO: completare email
		return alert;
	}
	
	private DateTime createRevisionTimestamp() {
		return DateTime.now(DateTimeZone.UTC);
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
		public final List<SchedulerEvent> events;
		
		public CalendarEvents(OCalendar calendar, List<SchedulerEvent> events) {
			this.calendar = calendar;
			this.events = events;
		}
	}
}
