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
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.security.DomainAccount;
import com.sonicle.webtop.calendar.ICalHelper.ParseResult;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OPostponedReminder;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.model.CalendarFolder;
import com.sonicle.webtop.calendar.bol.model.CalendarRoot;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.calendar.bol.model.ReminderGenId;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.PostponedReminderDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.BaseDAO.RevisionInfo;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseServiceManager;
import com.sonicle.webtop.core.RunContext;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.bol.model.RootShare;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import net.fortuna.ical4j.data.ParserException;
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

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseServiceManager {
	public static final Logger logger = WT.getLogger(CalendarManager.class);
	private static final String RESOURCE_CALENDAR = "CALENDAR";
	private static final String EVENT_NORMAL = "normal";
	private static final String EVENT_BROKEN = "broken";
	private static final String EVENT_RECURRING = "recurring";
	public static final String TARGET_THIS = "this";
	public static final String TARGET_SINCE = "since";
	public static final String TARGET_ALL = "all";
	
	private String host;
	private final HashMap<Integer, UserProfile.Id> calendarToOwnerCache = new HashMap<>();
	private final Object shareCacheLock = new Object();
	private final HashMap<UserProfile.Id, String> ownerToRootShareCache = new HashMap<>();
	private final HashMap<Integer, String> calendarToFolderShareCache = new HashMap<>();

	public CalendarManager(String serviceId, RunContext context) {
		super(serviceId, context);
		host = "unknown";
	}
	
	private void buildShareCache() {
		CoreManager core = WT.getCoreManager(getRunContext());
		UserProfile.Id pid = getTargetProfileId();
		try {
			ownerToRootShareCache.clear();
			calendarToFolderShareCache.clear();
			for(CalendarRoot root : listIncomingCalendarRoots()) {
				ownerToRootShareCache.put(pid, root.getShareId());
				for(OShare leafShare : core.listIncomingShareFolders(pid, root.getShareId(), getServiceId(), RESOURCE_CALENDAR)) {
					calendarToFolderShareCache.put(Integer.valueOf(leafShare.getInstance()), leafShare.getShareId().toString());
				}
			}
		} catch(WTException ex) {
			throw new WTRuntimeException(ex.getMessage());
		}
	}
	
	private String ownerToRootShareId(UserProfile.Id owner) {
		synchronized(shareCacheLock) {
			if(!ownerToRootShareCache.containsKey(owner)) buildShareCache();
			return ownerToRootShareCache.get(owner);
		}
	}
	
	private String calendarToFolderShareId(int calendarId) {
		synchronized(shareCacheLock) {
			if(!calendarToFolderShareCache.containsKey(calendarId)) buildShareCache();
			return calendarToFolderShareCache.get(calendarId);
		}
	}
	
	private UserProfile.Id calendarToOwner(int calendarId) {
		synchronized(calendarToOwnerCache) {
			if(calendarToOwnerCache.containsKey(calendarId)) {
				return calendarToOwnerCache.get(calendarId);
			} else {
				try {
					UserProfile.Id owner = getCalendarOwner(calendarId);
					calendarToOwnerCache.put(calendarId, owner);
					return owner;
				} catch(WTException ex) {
					throw new WTRuntimeException(ex.getMessage());
				}
			}
		}
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String value) {
		host = value;
	}
	
	private RevisionInfo createRevisionInfo() {
		return new RevisionInfo("WT", getRunProfileId().toString());
	}
	
	
	//supernet
	
	private void ensureRightsOnCalendarLeaf(int calendarId, String action) throws WTException {
		if(WT.isWebTopAdmin(getRunProfileId())) return;
		
		UserProfile.Id ownerPid = calendarToOwner(calendarId);
		if(ownerPid.equals(getTargetProfileId())) return;
		
		String shareId = calendarToFolderShareId(calendarId);
		if(shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		CoreManager core = WT.getCoreManager(getRunContext());
		if(!core.isPermittedOnFolderShare(getRunProfileId(), getServiceId(), RESOURCE_CALENDAR, action, shareId)) {
			throw new AuthException("");
		}
	}
	
	private void ensureRightsOnCalendarRoot(UserProfile.Id ownerPid, String action) throws WTException {
		if(WT.isWebTopAdmin(getRunProfileId())) return;
		if(ownerPid.equals(getTargetProfileId())) return;
		
		String shareId = ownerToRootShareId(ownerPid);
		if(shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", ownerPid);
		CoreManager core = WT.getCoreManager(getRunContext());
		if(!core.isPermittedOnRootShare(getRunProfileId(), getServiceId(), RESOURCE_CALENDAR, action, shareId)) {
			throw new AuthException("");
		}
	}
	
	private String permsToString(boolean[] perms) {
		String s = "";
		if(perms[0]) s += "c";
		if(perms[1]) s += "r";
		if(perms[2]) s += "u";
		if(perms[3]) s += "d";
		return s;
	}
	
	public List<CalendarRoot> listIncomingCalendarRoots() throws WTException {
		CoreManager core = WT.getCoreManager(getRunContext());
		ArrayList<CalendarRoot> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		
		List<RootShare> shares = core.listIncomingShareRoots(getTargetProfileId(), getServiceId(), RESOURCE_CALENDAR);
		CalendarRoot root = null;
		for(RootShare share : shares) {
			boolean[] perms = core.getRootSharePermissions(getTargetProfileId(), getServiceId(), RESOURCE_CALENDAR, share.getShareId());
			root = new CalendarRoot(share, permsToString(perms));
			if(hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public List<CalendarFolder> listIncomingCalendarFolders(String rootShareId) throws WTException {
		CoreManager core = WT.getCoreManager(getRunContext());
		ArrayList<CalendarFolder> folders = new ArrayList<>();
		UserProfile.Id pid = getTargetProfileId();
		
		// Retrieves incoming leafs (from sharing). This lookup already 
		// returns readable shares (we don't need to test READ permission)
		List<OShare> shares = core.listIncomingShareFolders(pid, rootShareId, getServiceId(), RESOURCE_CALENDAR);
		OCalendar cal;
		for(OShare share : shares) {
			cal = getCalendar(Integer.valueOf(share.getInstance()));
			boolean[] perms = core.getFolderSharePermissions(getTargetProfileId(), getServiceId(), RESOURCE_CALENDAR, share.getShareId().toString());
			folders.add(new CalendarFolder(share.getShareId().toString(), permsToString(perms), cal));
		}
		return folders;
	}
	
	public List<OCalendar> listCalendars() throws WTException {
		Connection con = null;
		UserProfile.Id pid = getTargetProfileId();
		
		try {
			con = WT.getConnection(getManifest());
			CalendarDAO dao = CalendarDAO.getInstance();
			return dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to list calendars for {0}", pid.toString());
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public UserProfile.Id getCalendarOwner(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			CalendarDAO dao = CalendarDAO.getInstance();
			Owner owner = dao.selectOwnerById(con, calendarId);
			if(owner == null) throw new WTException("Calendar not found [{0}]", calendarId);
			return new UserProfile.Id(owner.getDomainId(), owner.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to get calendar's owner [{0}]", calendarId);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar getCalendar(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			ensureRightsOnCalendarLeaf(calendarId, "READ");
			con = WT.getConnection(getManifest());
			CalendarDAO dao = CalendarDAO.getInstance();
			return dao.selectById(con, calendarId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to get calendar [{0}]", calendarId);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar addCalendar(OCalendar item) throws Exception {
		Connection con = null;
		
		try {
			ensureRightsOnCalendarRoot(item.getProfileId(), "WRITE");
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			CalendarDAO dao = CalendarDAO.getInstance();
			
			item.setCalendarId(dao.getSequence(con).intValue());
			item.setBuiltIn(false);
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.insert(con, item);
			DbUtils.commitQuietly(con);
			return item;
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCalendar updateCalendar(OCalendar item) throws Exception {
		Connection con = null;
		
		try {
			ensureRightsOnCalendarLeaf(item.getCalendarId(), "WRITE");
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			CalendarDAO dao = CalendarDAO.getInstance();
			
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.update(con, item);
			DbUtils.commitQuietly(con);
			return item;
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteCalendar(int calendarId) throws Exception {
		Connection con = null;
		
		try {
			ensureRightsOnCalendarLeaf(calendarId, "WRITE");
			con = WT.getConnection(getManifest());
			CalendarDAO dao = CalendarDAO.getInstance();
			dao.delete(con, calendarId);
			//TODO: cancellare eventi collegati
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DateTime> listEventsDates(CalendarRoot root, Integer[] calendars, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		return listEventsDates(root.getOwnerProfileId(), calendars, fromDate, toDate, userTz);
	}
	
	public List<DateTime> listEventsDates(UserProfile.Id pid, Integer[] calendars, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		Connection con = null;
		HashSet<DateTime> dates = new HashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(getManifest());
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEvent> expEvents = null;
			for(OCalendar cal : cals) {
				
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
			
		} finally {
			DbUtils.closeQuietly(con);
		}
		return new ArrayList<>(dates);
	}
	
	public SchedulerEvent viewEvent(Integer eventId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			return viewEvent(con, eventId);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private SchedulerEvent viewEvent(Connection con, Integer eventId) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent se = edao.view(con, eventId);
		se.updateCalculatedFields();
		return new SchedulerEvent(se);
	}
	
	public LinkedHashSet<String> calculateAvailabilitySpans(int minRange, UserProfile.Id pid, DateTime fromDate, DateTime toDate, DateTimeZone userTz, boolean busy) throws Exception {
		Connection con = null;
		LinkedHashSet<String> hours = new LinkedHashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(getManifest());
			
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
					/*
					if(se.getAllDay()) {
						startDt = se.getStartDate().withTimeAtStartOfDay();
						endDt = DateTimeUtils.withTimeAtEndOfDay(se.getEndDate());
						//startDt = se.getStartDate().withTimeAtStartOfDay().withZone(userTz);
						//endDt = DateTimeUtils.withTimeAtEndOfDay(se.getEndDate()).withZone(userTz);
					} else {
						startDt = se.getStartDate().withZone(userTz);
						endDt = se.getEndDate().withZone(userTz);
					}
					*/
					startDt = se.getStartDate().withZone(userTz);
					endDt = se.getEndDate().withZone(userTz);
					hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
				} else {
					recInstances = calculateRecurringInstances(se, fromDate, toDate, userTz);
					for(SchedulerEvent recInstance : recInstances) {
						/*
						if(recInstance.getAllDay()) {
							startDt = recInstance.getStartDate().withTimeAtStartOfDay();
							endDt = DateTimeUtils.withTimeAtEndOfDay(recInstance.getEndDate());
							//startDt = recInstance.getStartDate().withTimeAtStartOfDay().withZone(userTz);
							//endDt = DateTimeUtils.withTimeAtEndOfDay(recInstance.getEndDate()).withZone(userTz);
						} else {
							startDt = recInstance.getStartDate().withZone(userTz);
							endDt = recInstance.getEndDate().withZone(userTz);
						}
						*/
						startDt = recInstance.getStartDate().withZone(userTz);
						endDt = recInstance.getEndDate().withZone(userTz);
						hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
					}
				}
			}
		
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
	
	
	
	
	
	
	public static DateTime parseYmdHmsWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date + " " + time, tz);
	}
	
	public static DateTime parseYmdHmsWithZone(String dateTime, DateTimeZone tz) {
		String dt = StringUtils.replace(dateTime, "T", " ");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return formatter.parseDateTime(dt);
	}
	
	public List<CalendarEvents> searchEvents(UserProfile.Id pid, Integer[] calendars, String query) throws Exception {
		Connection con = null;
		ArrayList<CalendarEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(getManifest());
			
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
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<CalendarEvents> viewEvents(CalendarRoot root, Integer[] calendars, DateTime fromDate, DateTime toDate) throws Exception {
		return viewEvents(root.getOwnerProfileId(), calendars, fromDate, toDate);
	}
	
	public List<CalendarEvents> viewEvents(UserProfile.Id pid, Integer[] calendars, DateTime fromDate, DateTime toDate) throws Exception {
		Connection con = null;
		ArrayList<CalendarEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(getManifest());
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEvent> sevs = null;
			for(OCalendar cal : cals) {
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				grpEvts.add(new CalendarEvents(cal, sevs));
			}
			return grpEvts;
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedulerEvent> viewExpiredEvents(Connection con, DateTime fromDate, DateTime toDate) throws Exception {
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
	
	public List<SchedulerEvent> calculateRecurringInstances(SchedulerEvent recurringEvent, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			return calculateRecurringInstances(con, recurringEvent, fromDate, toDate, userTz);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event readEvent(String eventKey) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(getManifest());
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			VSchedulerEvent se = edao.view(con, ekey.eventId);
			se.updateCalculatedFields(); // TODO: Serve??????????????????????
			if(se == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			
			Event event = null;
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				event = createEvent(eventKey, Event.RecurringInfo.SINGLE, se);
				event.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
				
			} else if(type.equals(EVENT_RECURRING)) {
				ORecurrence rec = rdao.select(con, se.getRecurrenceId());
				if(rec == null) throw new WTException("Unable to retrieve recurrence [{}]", ekey.originalEventId);
				
				event = createEvent(eventKey, Event.RecurringInfo.RECURRING, se);
				int eventDays = calculateEventLengthInDays(se);
				event.setStartDate(event.getStartDate().withDate(ekey.atDate));
				event.setEndDate(event.getEndDate().withDate(event.getStartDate().plusDays(eventDays).toLocalDate()));
				event.setRecurrence(createEventRecurrence(rec));
				
			} else if(type.equals(EVENT_BROKEN)) {
				//TODO: recuperare il record ricorrenza per verifica?
				event = createEvent(eventKey, Event.RecurringInfo.BROKEN, se);
				event.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
			}
			
			return event;
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addEvent(Event event) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			doEventInsert(con, event, true, true);
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void editEvent(String target, Event event, DateTimeZone userTz) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates the event with new data
					doEventUpdate(con, original, event, true);
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = edao.select(con, ekey.eventId);
					if(oevt == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
					doEventUpdate(con, oevt, event, true);
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Inserts new broken event
					InsertResult insert = doEventInsert(con, event, false, false);
					// 2 - Marks recurring event date inserting a broken record
					doExcludeRecurrenceDate(con, original, ekey.atDate, insert.event.getEventId());
					// 3 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					if(orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					DateTime until = orec.getUntilDate();
					orec.applyEndUntil(ekey.atDate.minusDays(1).toDateTimeAtStartOfDay(), DateTimeZone.forID(original.getTimezone()), true);
					rdao.update(con, orec);
					// 2 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					// 3 - Insert new event adjusting recurrence a bit
					event.getRecurrence().setEndsMode(Recurrence.ENDS_MODE_UNTIL);
					event.getRecurrence().setUntilDate(until);
					doEventInsert(con, event, true, false);
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - Updates recurring event data (dates must be preserved) (+revision)
					event.setStartDate(event.getStartDate().withDate(original.getStartDate().toLocalDate()));
					event.setEndDate(event.getEndDate().withDate(original.getEndDate().toLocalDate()));
					doEventUpdate(con, original, event, false);
					// 2 - Updates recurrence data
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					orec.fillFrom(event.getRecurrence(), original.getStartDate(), original.getEndDate(), original.getTimezone());
					rdao.update(con, orec);
				}
			}
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void copyEvent(String eventKey, DateTime startDate, DateTime endDate) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			Event event = readEvent(eventKey);
			if(event == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			event.setStartDate(startDate);
			event.setEndDate(endDate);
			
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			doEventInsert(con, event, true, true);
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEvent(String eventKey, DateTime startDate, DateTime endDate, String title) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL) || type.equals(EVENT_BROKEN)) {
				// 1 - Updates event's dates/times (+revision)
				OEvent evt = edao.select(con, ekey.eventId);
				if(evt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
				evt.setStartDate(startDate);
				evt.setEndDate(endDate);
				evt.setTitle(title);
				evt.setStatus(OEvent.STATUS_MODIFIED);
				evt.setRevisionInfo(createRevisionInfo());
				edao.update(con, evt);
				
			} else {
				throw new WTException("Unable to move recurring event instance [{}]", ekey.eventId);
			}
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(String target, String eventKey) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			EventDAO evtDao = EventDAO.getInstance();
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO broDao = RecurrenceBrokenDAO.getInstance();
			
			OEvent original = evtDao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					if(!ekey.eventId.equals(original.getEventId())) throw new Exception("In this case both ids must be equals");
					deleteEvent(con, ekey.eventId);
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - logically delete newevent (broken one)
					deleteEvent(con, ekey.eventId);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, original.getEventId(), createRevisionInfo());
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - inserts a broken record (without new event) on deleted date
					//--ORecurrenceBroken rb = new ORecurrenceBroken();
					//--rb.setEventId(original.getEventId());
					//--rb.setRecurrenceId(original.getRecurrenceId());
					//--rb.setEventDate(ekey.atDate);
					//--rb.setNewEventId(null);
					//--broDao.insert(con, rb);
					doExcludeRecurrenceDate(con, original, ekey.atDate);
					
					// 2 - updates revision of original event
					evtDao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence rec = recDao.select(con, original.getRecurrenceId());
					if(rec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					rec.setUntilDate(ekey.atDate.toDateTimeAtStartOfDay().minusDays(1));
					rec.updateRRule(DateTimeZone.forID(original.getTimezone()));
					recDao.update(con, rec);
					// 2 - updates revision of original event
					evtDao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - logically delete original event
					deleteEvent(con, ekey.eventId);
				}
			}
			
			/*
			if(target.equals("this")) {
				if(original.getRecurrenceId() == null) { // Normal event
					if(!gid.eventId.equals(original.getEventId())) throw new Exception("In this case both ids must be equals");
					deleteEvent(con, gid.eventId);
					
				} else { // Event linked to a recurrence
					if(gid.eventId.equals(original.getEventId())) { // Recurring event
						// 1 - inserts a broken record (without new event) on deleted date
						ORecurrenceBroken rb = new ORecurrenceBroken();
						rb.setEventId(original.getEventId());
						rb.setRecurrenceId(original.getRecurrenceId());
						rb.setEventDate(gid.atDate);
						rb.setNewEventId(null);
						rbdao.insert(con, rb);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
						
					} else { // Broken event
						// 1 - logically delete newevent (broken one)
						deleteEvent(con, gid.eventId);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
					}
				}
			} else if(target.equals("since")) {
				if(gid.eventId.equals(original.getEventId())) { // Recurring event
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence rec = rdao.select(con, original.getRecurrenceId());
					if(rec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					rec.setUntilDate(gid.atDate.toDateTimeAtStartOfDay().minusDays(1));
					rec.updateRRule(DateTimeZone.forID(original.getTimezone()));
					rdao.update(con, rec);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
				}
				
			} else if(target.equals("all")) {
				if(original.getRecurrenceId() != null) { // We process only event linked to a recurrence
					if(gid.eventId.equals(original.getEventId())) {
						// 1 - logically delete original event
						deleteEvent(con, gid.eventId);
					}
				}
			}
			*/
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(Connection con, Integer eventId) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		edao.logicDelete(con, eventId, createRevisionInfo());
		//TODO: cancellare reminder
		//TODO: se ricorrenza, eliminare tutte le broken dove newid!=null ??? Non servi più dato che verifico il D dell'evento ricorrente
	}
	
	public void restoreEvent(String eventKey) throws Exception {
		Connection con = null;
		
		try {
			// Scheduler IDs always contains an eventId reference.
			// For broken events extracted eventId is not equal to extracted
			// originalEventId (event generator or database event), since
			// it belongs to the recurring event
			EventKey ekey = new EventKey(eventKey);
			if(ekey.originalEventId.equals(ekey.eventId)) throw new Exception("Cannot restore an event that is not broken");
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, ekey.eventId);
			// 2 - logically delete broken event
			deleteEvent(con, ekey.eventId);
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OPostponedReminder> getExpiredPostponedReminders(Connection con, DateTime greaterInstant) throws Exception {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return prdao.selectExpiredForUpdateByInstant(con, greaterInstant);
	}
	
	public boolean deletePostponedReminder(Connection con, Integer eventId, DateTime remindOn) {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return (prdao.delete(con, eventId, remindOn) == 1);
	}
	
	public void postponeReminder(String eventKey, String reminderId, int minutes) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(original.getReminder() == null) throw new WTException("Event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.originalEventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_BROKEN)) {
				OEvent broken = edao.select(con, ekey.eventId);
				if(broken == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
				if(broken.getReminder() == null) throw new WTException("Broken event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.eventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_RECURRING)) {
				//TODO: gestire i reminder per gli eventi ricorrenti
			}
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void snoozeReminder(String eventKey, String reminderId) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			prdao.delete(con, ekey.eventId, rid.remindOn);
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event readEventByPublicUid(String eventPublicUid) throws Exception {
		String eventKey = eventKeyByPublicUid(eventPublicUid);
		if(eventKey == null) throw new WTException("Unable to find event with UID [{0}]", eventPublicUid);
		return readEvent(eventKey);
	}
	
	private SchedulerEvent viewEventByUid(Connection con, String eventPublicUid) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		ve.updateCalculatedFields();
		return new SchedulerEvent(ve);
	}
	
	public String eventKeyByPublicUid(String eventPublicUid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			SchedulerEvent ve = viewEventByUid(con, eventPublicUid);
			return (ve == null) ? null : EventKey.buildKey(ve.getEventId(), ve.getOriginalEventId());
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<EventAttendee> getAttendees(Integer eventId, boolean notifiedOnly) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			return getAttendees(con, eventId, notifiedOnly);
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<EventAttendee> getAttendees(Connection con, Integer eventId, boolean notifiedOnly) throws Exception {
		List<OEventAttendee> attendees = null;
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		if(notifiedOnly) {
			attendees = eadao.selectByEventNotify(con, eventId, true);
		} else {
			attendees = eadao.selectByEvent(con, eventId);
		}
		return createEventAttendeeList(attendees);
	}
	
	public Event updateAttendeeReply(String eventUid, String attendeeUid, String response) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			SchedulerEvent ve = viewEventByUid(con, eventUid);
			
			EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
			int ret = eadao.updateAttendeeResponse(con, attendeeUid, ve.getEventId(), response);
			
			return (ret == 1) ? readEvent(ve.getKey()) : null;
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void importICal(Integer calendarId, InputStream is, DateTimeZone defaultTz) throws Exception {
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
			con = WT.getConnection(getManifest());
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
			
			con = WT.getConnection(getManifest());
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
	
	private List<SchedulerEvent> calculateRecurringInstances(Connection con, SchedulerEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
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
			
		} catch(Exception ex) {
			throw ex;
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
	
	private OEvent doEventUpdate(Connection con, OEvent oevt, Event event, boolean attendees) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		oevt.fillFrom(event);
		oevt.setStatus(OEvent.STATUS_MODIFIED);
		oevt.setRevisionInfo(createRevisionInfo());
		
		eadao.selectByEvent(con, oevt.getEventId());
		
		if(attendees) {
			List<EventAttendee> fromList = createEventAttendeeList(eadao.selectByEvent(con, oevt.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(fromList, event.getAttendees());
			
			OEventAttendee oatt = null;
			for(EventAttendee att : changeSet.created) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(WT.generateUUID());
				oatt.setEventId(oevt.getEventId());
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
		
		edao.update(con, oevt);
		return oevt;
	}
	
	private String generateEventUid() {
		return ICalendarUtils.buildUid(WT.generateUUID(), host);
	}
	
	private InsertResult doEventInsert(Connection con, Event event, boolean recurrence, boolean attendees) throws Exception {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		
		OEvent oevt = new OEvent();
		oevt.fillFrom(event);
		oevt.setEventId(evtDao.getSequence(con).intValue());
		if(StringUtils.isEmpty(event.getPublicUid())) {
			oevt.setPublicUid(WT.generateUUID());
		} else {
			oevt.setPublicUid(event.getPublicUid());
			
		}
		oevt.setStatus(OEvent.STATUS_NEW);
		oevt.setRevisionInfo(createRevisionInfo());
		
		ArrayList<OEventAttendee> oatts = new ArrayList<>();
		if(attendees && event.hasAttendees()) {
			OEventAttendee oatt = null;
			for(EventAttendee att : event.getAttendees()) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(WT.generateUUID());
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
		evtDao.insert(con, oevt);
		
		return new InsertResult(oevt, orec, oatts);
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate) throws Exception {
		return doExcludeRecurrenceDate(con, recurringEvent, instanceDate, null);
	}
	
	private ORecurrenceBroken doExcludeRecurrenceDate(Connection con, OEvent recurringEvent, LocalDate instanceDate, Integer brokenEventId) throws Exception {
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
	
	private SchedulerEvent cloneEvent(SchedulerEvent source, DateTime newStart, DateTime newEnd) {
		SchedulerEvent event = new SchedulerEvent(source);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private Event createEvent(String key, Event.RecurringInfo recurringInfo, VSchedulerEvent se) {
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
		event.setActivityId(se.getActivityId());
		event.setCustomerId(se.getCustomerId());
		event.setStatisticId(se.getStatisticId());
		event.setCausalId(se.getCausalId());
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
