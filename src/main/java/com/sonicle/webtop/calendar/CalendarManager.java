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

import com.github.rutledgepaulv.qbuilders.conditions.Condition;
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
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateRange;
import com.sonicle.commons.time.DateTimeRange;
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
import com.sonicle.webtop.calendar.bol.OCalendarOwnerInfo;
import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttachment;
import com.sonicle.webtop.calendar.bol.OEventAttachmentData;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OEventICalendar;
import com.sonicle.webtop.calendar.bol.OEventInfo;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VEventObjectChanged;
import com.sonicle.webtop.calendar.bol.VEventHrefSync;
import com.sonicle.webtop.calendar.bol.VExpEvent;
import com.sonicle.webtop.calendar.bol.VExpEventInstance;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.CalendarPropsDAO;
import com.sonicle.webtop.calendar.dal.EventAttachmentDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.EventICalendarDAO;
import com.sonicle.webtop.calendar.dal.EventPredicateVisitor;
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
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.EventObjectChanged;
import com.sonicle.webtop.calendar.model.EventFootprint;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.calendar.io.ICalendarInput;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventAttachmentWithStream;
import com.sonicle.webtop.calendar.model.EventObjectWithBean;
import com.sonicle.webtop.calendar.model.EventObjectWithICalendar;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.sdk.AbstractMapCache;
import com.sonicle.webtop.core.sdk.AbstractShareCache;
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
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMultipart;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateList;
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
import org.joda.time.Duration;

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
	
	private static final ConcurrentHashMap<String, UserProfileId> pendingRemoteCalendarSyncs = new ConcurrentHashMap<>();
	
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
			throw wrapException(ex);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
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
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public UserProfileId getCalendarOwner(int calendarId) throws WTException {
		return ownerCache.get(calendarId);
	}
	
	public String getIncomingCalendarShareRootId(int calendarId) throws WTException {
		return shareCache.getShareRootIdByFolderId(calendarId);
	}
	
	@Override
	public boolean existCalendar(int calendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			boolean ret = calDao.existsById(con, calendarId);
			if (ret) checkRightsOnCalendarFolder(calendarId, "READ");
			return ret;
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Calendar getCalendar(int calendarId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			con = WT.getConnection(SERVICE_ID);
			return doCalendarGet(con, calendarId);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
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
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, String> getCalendarLinks(int calendarId) throws WTException {
		checkRightsOnCalendarFolder(calendarId, "READ");
		
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
			checkRightsOnCalendarRoot(calendar.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			calendar.setBuiltIn(false);
			calendar = doCalendarInsert(con, calendar);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_INSERT", String.valueOf(calendar.getCalendarId()));
			
			return calendar;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			cal = doCalendarInsert(con, cal);
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_INSERT",  String.valueOf(cal.getCalendarId()));
			
			return cal;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateCalendar(Calendar calendar) throws WTException {
		Connection con = null;
		
		try {
			int calendarId = calendar.getCalendarId();
			checkRightsOnCalendarFolder(calendarId, "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			boolean updated = doCalendarUpdate(con, calendar);
			if (!updated) throw new NotFoundException("Calendar not found [{}]", calendarId);
			
			DbUtils.commitQuietly(con);
			writeLog("CALENDAR_UPDATE", String.valueOf(calendarId));
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			if (cal == null) throw new NotFoundException("Calendar not found [{}]", calendarId);
			
			int deleted = calDao.deleteById(con, calendarId);
			psetDao.deleteByCalendar(con, calendarId);
			doEventsDeleteByCalendar(con, calendarId, !cal.isProviderRemote());
			
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
			throw wrapException(ex);
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
			throw wrapException(ex);
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
			throw wrapException(ex);
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
			throw wrapException(ex);
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
					.filter(calendarId -> quietlyCheckRightsOnCalendarFolder(calendarId, "READ"))
					.collect(Collectors.toList());
			
			HashSet<LocalDate> dates = new HashSet<>();
			for (VVEvent vevt : evtDao.viewByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				dates.addAll(CalendarUtils.getDatesSpan(vevt.getAllDay(), vevt.getStartDate(), vevt.getEndDate(), DateTimeZone.forID(vevt.getTimezone())));
			}
			int noOfRecurringInst = Days.daysBetween(from, to).getDays() + 2;
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, okCalendarIds, from, to, null)) {
				final List<SchedEventInstance> instances = calculateRecurringInstances(con, new SchedEventInstanceMapper(vevt), from, to, refTimezone, noOfRecurringInst);
				for (SchedEventInstance instance : instances) {
					dates.addAll(CalendarUtils.getDatesSpan(instance.getAllDay(), instance.getStartDate(), instance.getEndDate(), instance.getDateTimeZone()));
				}
			}
			return new ArrayList<>(dates);
		
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
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
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CollectionChangeSet<EventObjectChanged> listEventObjectsChanges(int calendarId, DateTime since, Integer limit) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			ArrayList<EventObjectChanged> inserted = new ArrayList<>();
			ArrayList<EventObjectChanged> updated = new ArrayList<>();
			ArrayList<EventObjectChanged> deleted = new ArrayList<>();
			
			if (limit == null) limit = Integer.MAX_VALUE;
			if (since == null) {
				List<VEventObjectChanged> vevts = evtDao.viewChangedLiveCalObjectsByCalendar(con, calendarId, limit);
				for (VEventObjectChanged vevt : vevts) {
					inserted.add(new EventObjectChanged(vevt.getEventId(), vevt.getRevisionTimestamp(), vevt.getHref()));
				}
			} else {
				List<VEventObjectChanged> vevts = evtDao.viewChangedCalObjectsByCalendarSince(con, calendarId, since, limit);
				for (VEventObjectChanged vevt : vevts) {
					Event.RevisionStatus revStatus = EnumUtils.forSerializedName(vevt.getRevisionStatus(), Event.RevisionStatus.class);
					if (Event.RevisionStatus.DELETED.equals(revStatus)) {
						deleted.add(new EventObjectChanged(vevt.getEventId(), vevt.getRevisionTimestamp(), vevt.getHref()));
					} else {
						if (Event.RevisionStatus.NEW.equals(revStatus) || (vevt.getCreationTimestamp().compareTo(since) >= 0)) {
							inserted.add(new EventObjectChanged(vevt.getEventId(), vevt.getRevisionTimestamp(), vevt.getHref()));
						} else {
							updated.add(new EventObjectChanged(vevt.getEventId(), vevt.getRevisionTimestamp(), vevt.getHref()));
						}
					}
				}
			}
			
			return new CollectionChangeSet<>(inserted, updated, deleted);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
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
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
			
			checkRightsOnCalendarFolder(calendarId, "READ");
			
			VEventObject vevt = evtDao.viewCalObjectById(con, calendarId, eventId);
			return (vevt == null) ? null : doEventObjectPrepare(con, vevt, outputType);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
	public void updateEventObject(int calendarId, String href, net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
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
		updateEvent(refInput.event, true);
		
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
				} catch(Throwable t) {
					logger.error("Unable to insert exception on {} as new event", eiEx.addsExOnMaster, t);
				}
			}
		}
	}
	
	@Override
	public void deleteEventObject(int calendarId, String href) throws WTException {
		int eventId = getEventIdByCategoryHref(calendarId, href, true);
		deleteEvent(eventId, true);
	}
	
	private int getEventIdByCategoryHref(int calendarId, String href, boolean throwExIfManyMatchesFound) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			List<Integer> ids = evtDao.selectAliveIdsByCalendarHrefs(con, calendarId, href);
			if (ids.isEmpty()) throw new NotFoundException("Event not found [{}, {}]", calendarId, href);
			if (throwExIfManyMatchesFound && (ids.size() > 1)) throw new WTException("Many matches for href [{}]", href);
			return ids.get(ids.size()-1);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
					.filter(calendarId -> quietlyCheckRightsOnCalendarFolder(calendarId, "READ"))
					.collect(Collectors.toList());
			
			EventPredicateVisitor epv = new EventPredicateVisitor(true, EventPredicateVisitor.Target.NORMAL);
			org.jooq.Condition norCondition = null;
			org.jooq.Condition recCondition = null;
			if (conditionPredicate != null) {
				norCondition = BaseDAO.createCondition(conditionPredicate, epv);
				recCondition = BaseDAO.createCondition(conditionPredicate, new EventPredicateVisitor(true, EventPredicateVisitor.Target.RECURRING));
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
				List<SchedEventInstance> instances = calculateRecurringInstances(con, new SchedEventInstanceMapper(vevt), instFrom, instTo, targetTimezone, noOfRecurringInst);
				if (!instances.isEmpty()) return true;
			}
			return false;
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
			List<Integer> okCalendarIds = calendarIds.stream()
					.filter(calendarId -> quietlyCheckRightsOnCalendarFolder(calendarId, "READ"))
					.collect(Collectors.toList());
			
			EventPredicateVisitor epv = new EventPredicateVisitor(true, EventPredicateVisitor.Target.NORMAL);
			org.jooq.Condition norCondition = null;
			org.jooq.Condition recCondition = null;
			if (conditionPredicate != null) {
				norCondition = BaseDAO.createCondition(conditionPredicate, epv);
				recCondition = BaseDAO.createCondition(conditionPredicate, new EventPredicateVisitor(true, EventPredicateVisitor.Target.RECURRING));
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
				SchedEventInstance item = ManagerUtils.fillSchedEvent(new SchedEventInstance(), vevt);
				item.setKey(EventKey.buildKey(vevt.getEventId(), vevt.getSeriesEventId()));
				instances.add(item);
			}
			for (VVEvent vevt : evtDao.viewRecurringByCalendarRangeCondition(con, okCalendarIds, from, to, recCondition)) {
				instances.addAll(calculateRecurringInstances(con, new SchedEventInstanceMapper(vevt), instFrom, instTo, targetTimezone, noOfRecurringInst));
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
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
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
				throw wrapException(ex);
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
			throw wrapException(ex);
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
			throw wrapException(ex);
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
			
			Event event = doEventGet(con, eventId, false, false);
			if (event == null) throw new WTException("Event not found [{0}]", eventId);
			return event;
		
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
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
			
			Integer eventId = doEventGetId(con, null, publicUid);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
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
			Event event = doEventGet(con, eventId, forZPushFix ? false : true, forZPushFix);
			if (event == null) return null;
			checkRightsOnCalendarFolder(event.getCalendarId(), "READ");
			
			return event;
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event getEvent(GetEventScope scope, String publicUid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doEventGet(con, scope, publicUid);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventAttachmentWithBytes getEventAttachment(int eventId, String attachmentId) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttachmentDAO attDao = EventAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer calId = evtDao.selectCalendarId(con, eventId);
			if (calId == null) return null;
			checkRightsOnCalendarFolder(calId, "READ");
			
			OEventAttachment oatt = attDao.selectByIdEvent(con, attachmentId, eventId);
			if (oatt == null) return null;
			
			OEventAttachmentData oattData = attDao.selectBytes(con, attachmentId);
			return ManagerUtils.fillEventAttachment(new EventAttachmentWithBytes(oattData.getBytes()), oatt);
		
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
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
		
		event.ensureCoherence();
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			EventInsertResult insert = doEventInsert(con, event, iCalendarRawData, true, true, true, true, false);
			DbUtils.commitQuietly(con);
			writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
			
			storeAsSuggestion(core, SUGGESTION_EVENT_TITLE, event.getTitle());
			storeAsSuggestion(core, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			Event eventDump = getEvent(insert.event.getEventId());
			
			// Notify last modification
			List<RecipientTuple> nmRcpts = getModificationRecipients(insert.event.getCalendarId(), Crud.CREATE);
			if (!nmRcpts.isEmpty()) notifyForEventModification(RunContext.getRunProfileId(), nmRcpts, eventDump.getFootprint(), Crud.CREATE);
			
			// Notify attendees
			if (notifyAttendees) {
				List<RecipientTuple> attRcpts = getInvitationRecipients(getTargetProfileId(), eventDump, Crud.CREATE);
				if (!attRcpts.isEmpty()) notifyForInvitation(getTargetProfileId(), attRcpts, eventDump, Crud.CREATE);
			}
			
			//TODO: IS THIS STILL NECESSARY????????????????????????????????????????????????????????????
			
			Event evt = ManagerUtils.createEvent(insert.event);
			if (insert.recurrence != null) {
				evt.setRecurrence(insert.recurrence.getRule(), insert.recurrence.getLocalStartDate(evt.getDateTimeZone()), null);
			} else {
				evt.setRecurrence(null, null, null);
			}
			evt.setAttendees(ManagerUtils.createEventAttendeeList(insert.attendees));
			evt.setAttachments(ManagerUtils.createEventAttachmentList(insert.attachments));
			
			
			return evt;
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
	
	public void updateEventOLD(Event event, boolean notifyAttendees) throws WTException {
		//TODO: make some tests with calDAV in order to check if can we move to updateEvent below!
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "UPDATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			doEventInstanceUpdateAndCommit(con, UpdateEventTarget.ALL_SERIES, new EventKey(event.getEventId()), event, notifyAttendees);
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEvent(Event event, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			checkRightsOnCalendarElements(event.getCalendarId(), "UPDATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			String provider = calDao.selectProviderById(con, event.getCalendarId());
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", event.getCalendarId());
			
			doEventMasterUpdateAndCommit(con, event, notifyAttendees);
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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

				doEventUpdate(con, original, parsedEvent, true, false);
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

				// Gets the event looking also into incoming calendars...
				// (i can be the organizer of a meeting created for my boss that 
				// share his calendar with me; all received replies must be bringed
				// back to the event in the shared calendar)
				//Event evt = getEvent(uid);
				// Previous impl. forced (forceOriginal == true)
				Event evt = doEventGet(con, GetEventScope.PERSONAL_AND_INCOMING, uid);
				if (evt == null) throw new WTException("Event not found [{0}]", uid);
				
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
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			checkRightsOnCalendarElements(calendarId, "DELETE");
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceDeleteAndCommit(con, UpdateEventTarget.ALL_SERIES, new EventKey(eventId), notifyAttendees);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
		
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public EventInstance getEventInstance(String eventKey) throws WTException {
		return getEventInstance(new EventKey(eventKey));
	}
	
	public EventInstance getEventInstance(EventKey key) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doEventInstanceGet(con, key.eventId, key.instanceDate, true);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventInstance event, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		event.ensureCoherence();
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			Integer calendarId = evtDao.selectCalendarId(con, event.getEventId());
			if (calendarId == null) throw new WTException("Unable to retrieve event [{}]", event.getEventId());
			
			checkRightsOnCalendarElements(calendarId, "UPDATE");
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			// TODO: avoid this!!!
			EventKey eventKey = new EventKey(event.getKey());
			doEventInstanceUpdateAndCommit(con, target, eventKey, event, notifyAttendees);
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateEventInstance(UpdateEventTarget target, EventKey key, DateTime newStart, DateTime newEnd, String newTitle, boolean notifyAttendees) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			EventInstance ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true);
			
			int calendarId = ei.getCalendarId();
			checkRightsOnCalendarElements(calendarId, "UPDATE");
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
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
			doEventInstanceUpdateAndCommit(con, target, key, ei, notifyAttendees);
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			
			checkRightsOnCalendarElements(calendarId, "DELETE");
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceDeleteAndCommit(con, target, key, notifyAttendees);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			
			checkRightsOnCalendarElements(calendarId, "UPDATE");
			
			String provider = calDao.selectProviderById(con, calendarId);
			if (Calendar.isProviderRemote(provider)) throw new WTException("Calendar is remote and therefore read-only [{}]", calendarId);
			
			doEventInstanceRestoreAndCommit(con, key, true);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Event cloneEventInstance(EventKey key, Integer newCalendarId, DateTime newStart, DateTime newEnd, boolean notifyAttendees) throws WTException {
		Connection con = null;
		EventInstance ei = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true);
			checkRightsOnCalendarFolder(ei.getCalendarId(), "READ");
			int calendarId = (newCalendarId != null) ? newCalendarId : ei.getCalendarId();
			
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
			
		} catch(SQLException | DAOException | WTException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return addEvent(ei, notifyAttendees);
	}
	
	@Override
	public void moveEventInstance(EventKey key, int targetCalendarId) throws WTException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		Connection con = null;
		EventInstance ei = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			ei = doEventInstanceGet(con, key.eventId, key.instanceDate, true);
			checkRightsOnCalendarFolder(ei.getCalendarId(), "READ");
			
			if (targetCalendarId != ei.getCalendarId()) {
				checkRightsOnCalendarElements(ei.getCalendarId(), "DELETE");
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
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			throw wrapException(ex);
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
				for (VVEvent ve : evtDao.viewByCalendarRangeCondition(con, ocal.getCalendarId(), fromDate, toDate, null)) {
					veis.add(new VVEventInstance(ve));
				}
				for (VVEvent ve : evtDao.viewRecurringByCalendarRangeCondition(con, ocal.getCalendarId(), fromDate, toDate, null)) {
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
			throw wrapException(ex);
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
			throw wrapException(ex);
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
				int del = doEventsDeleteByCalendar(con, calendarId, false);
				log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "{0} event/s deleted!", del));
				DbUtils.commitQuietly(con);
			}
			
			log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Importing..."));
			int count = 0;
			for(EventInput ei : input) {
				ei.event.setCalendarId(calendarId);
				try {
					doEventInputInsert(con, uidMap, ei, true);
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
			throw wrapException(ex);
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
				
				for (VVEvent ve : edao.viewByCalendarRangeCondition(con, ocal.getCalendarId(), fromDate, toDate, null)) {
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
				for(VVEvent ve : edao.viewRecurringByCalendarRangeCondition(con, ocal.getCalendarId(), fromDate, toDate, null)) {
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
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		EventDAO evtDao = EventDAO.getInstance();
		Connection con = null;
		
		ensureSysAdmin();
		ArrayList<VExpEventInstance> evtInstCandidates = new ArrayList<>();
		
		logger.trace("Analyzing event instances...");
		try {
			final DateTime from = now.withTimeAtStartOfDay();
			con = WT.getConnection(SERVICE_ID, false);
			
			for (VExpEventInstance evtInst : doEventGetExpiredForUpdate(con, from, from.plusDays(7*2+1))) {
				final DateTime remindOn = evtInst.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(evtInst.getReminder());
				if (now.compareTo(remindOn) < 0) continue;
				// If instance should have been reminded in past...
				if (evtInst.getRemindedOn() != null) {
					// Only recurring event instances should pass here, classic events are already excluded by the db query
					if (evtInst.getRecurrenceId() == null) throw new WTException("This should never happen (famous last words)");
					final DateTime lastRemindedOn = evtInst.getRemindedOn().withZone(DateTimeZone.UTC);
					if (remindOn.compareTo(lastRemindedOn) <= 0) continue;
					// If instance should have been reminded after last remind...
				}
				
				int ret = evtDao.updateRemindedOn(con, evtInst.getEventId(), now);
				evtInstCandidates.add(evtInst);
			}
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error collecting instances", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		logger.debug("Found {} instances to be reminded", evtInstCandidates.size());
		
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
		
		logger.trace("Preparing alerts...");
		for (VExpEventInstance evtInst : evtInstCandidates) {
			logger.debug("Working on instance [{}, {}]", evtInst.getEventId(), evtInst.getStartDate());
			if (!byEmailCache.containsKey(evtInst.getCalendarProfileId())) {
				CalendarUserSettings cus = new CalendarUserSettings(SERVICE_ID, evtInst.getCalendarProfileId());
				boolean bool = cus.getEventReminderDelivery().equals(CalendarSettings.EVENT_REMINDER_DELIVERY_EMAIL);
				byEmailCache.put(evtInst.getCalendarProfileId(), bool);
			}
			
			if (byEmailCache.get(evtInst.getCalendarProfileId())) {
				UserProfile.Data ud = WT.getUserData(evtInst.getCalendarProfileId());
				CoreUserSettings cus = new CoreUserSettings(evtInst.getCalendarProfileId());
				
				try {
					EventInstance eventInstance = getEventInstance(evtInst.getKey());
					alerts.add(createEventReminderAlertEmail(ud.getLocale(), cus.getShortDateFormat(), cus.getShortTimeFormat(), ud.getPersonalEmailAddress(), evtInst.getCalendarProfileId(), eventInstance));
				} catch(WTException ex) {
					logger.error("Error preparing email", ex);
				}
			} else {
				alerts.add(createEventReminderAlertWeb(evtInst));
			}
		}
		
		//FIXME: remove this when zpush is using manager methods
		sendInvitationForZPushEvents();
		// ----------------------------
		
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
		final ICalendarInput icalInput = new ICalendarInput(udata.getTimeZone())
				.withIgnoreAlarms(true);
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
						ICalendarUtils.relaxParsingAndCompatibility();
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
									boolean updated = doEventInputUpdate(con, cache, ei, true);
									if (!updated) throw new WTException("Event not found [{}]", ei.event.getEventId());
									
								} else {
									doEventInputInsert(con, cache, ei, true);
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
									doEventInputInsert(con, cache, ei, true);
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
										doEventInputInsert(con, cache, ei, true);
									} else {
										ei.event.setEventId(matchingEventId);
										boolean updated = doEventInputUpdate(con, cache, ei, true);
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
			pendingRemoteCalendarSyncs.remove(PENDING_KEY);
		}
	}
	
	private <T> T calculateFirstRecurringInstance(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTimeZone userTimezone) throws WTException {
		List<T> instances = calculateRecurringInstances(con, instanceMapper, null, null, userTimezone, 1);
		return instances.isEmpty() ? null : instances.get(0);
	}
	
	private <T> List<T> calculateRecurringInstances(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone) throws WTException {
		return calculateRecurringInstances(con, instanceMapper, fromDate, toDate, userTimezone, -1);
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
	
	private <T> List<T> calculateRecurringInstances(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime rangeFrom, DateTime rangeTo, DateTimeZone userTimezone, int limit) throws WTException {
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
				
				Map<LocalDate, ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, orec.getRecurrenceId());
				List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, orec.getStartDate(), eventStart, eventEnd, eventTimezone, rangeFrom, rangeTo, limit);
				for (LocalDate recurringDate : dates) {
					if (obrecs.containsKey(recurringDate)) continue; // Skip broken date...

					DateTime start = recurringDate.toDateTime(eventStartTime, eventTimezone).withZone(userTimezone);
					DateTime end = recurringDate.plusDays(eventDays).toDateTime(eventEndTime, eventTimezone).withZone(userTimezone);
					String key = EventKey.buildKey(eventId, eventId, recurringDate);

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
			
		} catch(DAOException ex) {
			throw wrapException(ex);
		}
		
		return instances;
	}
	
	private <T> List<T> calculateRecurringInstances_OLD(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime fromDate, DateTime toDate, DateTimeZone userTimezone, int limit) throws WTException {
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
			if (fromDate == null) fromDate = orec.getStartDate();
			if (toDate == null) toDate = orec.getStartDate().plusYears(1);
			
			/*
			List<ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, orec.getRecurrenceId());
			//TODO: ritornare direttamente l'hashmap da jooq
			// Builds a hashset of broken dates for increasing performances
			HashMap<String, ORecurrenceBroken> brokenDates = new HashMap<>();
			for (ORecurrenceBroken obrec : obrecs) {
				brokenDates.put(obrec.getEventDate().toString(), obrec);
			}
			*/
			
			Map<LocalDate, ORecurrenceBroken> obrecs = recbDao.selectByEventRecurrence(con, eventId, orec.getRecurrenceId());
			HashSet<String> brokenDates = new HashSet<>();
			for (LocalDate ld : obrecs.keySet()) {
				brokenDates.add(ld.toString());
			}
			
			
			try {
				// Calculate event length in order to generate events like original one
				int eventDays = CalendarUtils.calculateLengthInDays(eventStartDate, eventEndDate);
				RRule rr = new RRule(orec.getRule());
				
				// Calcutate recurrence set for required dates range
				PeriodList periods = ICal4jUtils.calculateRecurrenceSet(eventStartDate, eventEndDate, orec.getStartDate(), rr, fromDate, toDate, userTimezone);
				
				// Recurrence start is useful to skip undesired dates at beginning.
				// If event does not starts at recurrence real beginning (eg. event
				// start on MO but first recurrence begin on WE), ical4j lib includes 
				// those dates in calculated recurrence set, as stated in RFC 
				// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
				LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(orec.getStartDate(), rr.getRecur(), userTimezone).toLocalDate();
				//LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(eventStartDate, rr.getRecur(), userTimezone).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
				LocalDate rrEnd = orec.getUntilDate().toLocalDate();

				// Iterates returned recurring periods and builds cloned events...
				int count = -1;
				for (net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
					count++;
					if ((limit != -1) && (count > limit)) break;
					final LocalDate perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
					final LocalDate perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();

					if (brokenDates.contains(perStart.toString())) continue; // Skip broken dates...
					if ((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
						final DateTime newStart = eventStartDate.withDate(perStart);
						final DateTime newEnd = eventEndDate.withDate(newStart.plusDays(eventDays).toLocalDate());
						final String key = EventKey.buildKey(eventId, eventId, perStart);
						
						instances.add(instanceMapper.createInstance(key, newStart, newEnd));
					}
				}

			} catch(DAOException ex) {
				throw wrapException(ex);
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
			throw wrapException(ex);
		} catch(ParseException ex) {
			throw new WTException(ex, "Unable to parse rrule");
		}
	}
	*/
	
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
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private OCalendarOwnerInfo doCalendarGetOwnerInfo(Connection con, int calendarId) throws DAOException {
		CalendarDAO calDao = CalendarDAO.getInstance();
		return calDao.selectOwnerInfoById(con, calendarId);
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
			
			if (EventObjectOutputType.ICALENDAR.equals(outputType)) {
				EventObjectWithICalendar eco = ManagerUtils.fillEventCalObject(new EventObjectWithICalendar(), vobj);
				
				ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
				//TODO: add support to excluded dates
				net.fortuna.ical4j.model.Calendar iCal = out.toCalendar(event);
				if (vobj.getHasIcalendar()) {
					//TODO: in order to be fully compliant, merge generated vcard with the original one in db table!
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
	
	private Integer doEventGetId(Connection con, Collection<Integer> calendarIdMustBeIn, String publicUid) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		List<Integer> ids = null;
		if ((calendarIdMustBeIn == null) || calendarIdMustBeIn.isEmpty()) {
			// This kind of lookup is suitable for calls executed by admin from public service
			ids = evtDao.selectAliveIdsByPublicUid(con, publicUid);
			
		} else {
			logger.trace("Looking for publicId in restricted set of calendars...");
			ids = evtDao.selectAliveIdsByCalendarsPublicUid(con, calendarIdMustBeIn, publicUid);
		}
		if (ids.isEmpty()) return null;
		if (ids.size() > 1) logger.warn("Multiple events found for public id [{}]", publicUid);
		return ids.get(0);
	}
	
	private Event doEventGet(Connection con, int eventId, boolean attachments, boolean forZPushFix) throws DAOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEvent oevt = forZPushFix ? evtDao.selectById(con, eventId) : evtDao.selectAliveById(con, eventId);
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
		// Fill attachments (if necessary)
		if (attachments) {
			List<OEventAttachment> oattchs = attchDao.selectByEvent(con, eventId);
			evt.setAttachments(ManagerUtils.createEventAttachmentList(oattchs));
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
		// Filled array could contains more than one result, eg. in case of 
		// invitation between two users of the same domain where the target
		// calendar of one user is shared to the other.
		// Returning the first result is the most appropriated action because
		// personal elements are returned first.
		//if (ids.size() > 1) throw new WTException("Multiple events found for public id [{}]", publicUid);
		return doEventGet(con, ids.get(0), false, false);
	}
	
	private void doEventMasterUpdateAndCommit(Connection con, Event event, boolean notifyAttendees) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		
		OEvent oevtOrig = evtDao.selectById(con, event.getEventId());
		if (oevtOrig == null) throw new WTException("Unable get original event [{}]", event.getEventId());
		
		// 1 - Updates event with new data
		doEventUpdate(con, oevtOrig, event, true, true, true, true);

		DbUtils.commitQuietly(con);
		writeLog("EVENT_UPDATE", String.valueOf(event.getEventId()));

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
	
	private EventInstance doEventInstanceGet(Connection con, int eventId, LocalDate date, boolean attachments) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEvent oevt = evtDao.selectById(con, eventId);
		if (oevt == null) throw new WTException("Unable to get event [{}]", eventId);
		
		EventInstance ei = ManagerUtils.fillEvent(new EventInstance(), oevt);
		
		// Fill recurrence (if necessary)
		ORecurrence orec = recDao.select(con, oevt.getRecurrenceId());
		if (orec != null) {
			if (date == null) throw new WTException("Date is required for recurring events [{}]", eventId);
			
			Set<LocalDate> excludedDates = doGetExcludedDates(con, oevt.getEventId(), oevt.getRecurrenceId());
			int eventDays = CalendarUtils.calculateLengthInDays(oevt.getStartDate(), oevt.getEndDate());
			ei.setStartDate(ei.getStartDate().withDate(date));
			ei.setEndDate(ei.getEndDate().withDate(ei.getStartDate().plusDays(eventDays).toLocalDate()));
			ei.setRecurrence(orec.getRule(), orec.getLocalStartDate(ei.getDateTimeZone()), excludedDates);
			
			ei.setKey(EventKey.buildKey(eventId, eventId, date));
			ei.setRecurInfo(Event.RecurInfo.RECURRING);
			
		} else {
			Integer seriesEventId = evtDao.selectAliveSeriesEventIdById(con, eventId);
			ei.setKey(EventKey.buildKey(eventId, seriesEventId));
			ei.setRecurInfo((seriesEventId == null) ? Event.RecurInfo.NONE : Event.RecurInfo.BROKEN);
		}
		
		// Fill attendees
		List<OEventAttendee> oattes = atteDao.selectByEvent(con, eventId);
		ei.setAttendees(ManagerUtils.createEventAttendeeList(oattes));
		
		// Fill attachments (if necessary)
		if (attachments) {
			List<OEventAttachment> oattchs = attchDao.selectByEvent(con, eventId);
			ei.setAttachments(ManagerUtils.createEventAttachmentList(oattchs));
		}
		
		return ei;
	}
		
	private void doEventInstanceUpdateAndCommit(Connection con, UpdateEventTarget target, EventKey eventKey, Event event, boolean notifyAttendees) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEventInfo einfo = evtDao.selectOEventInfoById(con, event.getEventId());
		if (einfo == null) throw new WTException("Unable to retrieve event info [{}]", event.getEventId());
		
		OEvent oevtOrig = evtDao.selectById(con, einfo.getEventId());
		if (oevtOrig == null) throw new WTException("Unable get original event [{}]", einfo.getEventId());
		
		Event eventDump = null;
		if (einfo.isRecurring()) {
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts new broken event (attendees and rr are not supported here)
				EventInsertResult insert = doEventInsert(con, event, null, false, false, false, false, false);

				// 2 - Inserts new broken record (marks recurring event) on modified date
				doRecurrenceExcludeDate(con, oevtOrig, eventKey.instanceDate, insert.event.getEventId());

				// 3 - Updates revision of original event
				evtDao.updateRevision(con, oevtOrig.getEventId(), BaseDAO.createRevisionTimestamp());

				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
				writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));

				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, insert.event.getTitle());
				//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, insert.event.getLocation());

				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				// 1 - Resize original recurrence (sets until date at the day before date)
				ORecurrence orec = recDao.select(con, einfo.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", einfo.getRecurrenceId());
				
				int oldDaysBetween = CalendarUtils.calculateLengthInDays(event.getStartDate(), event.getEndDate());
				Recur oldRecur = orec.getRecur(); // Dump old recur!
				
				LocalTime untilTime = einfo.getAllDay() ? DateTimeUtils.TIME_AT_STARTOFDAY : einfo.getStartDate().withZone(einfo.getDateTimeZone()).toLocalTime();
				orec.updateUntilDate(eventKey.instanceDate.minusDays(1), untilTime, einfo.getDateTimeZone());
				recDao.update(con, orec);

				// 2 - Updates revision of original event
				evtDao.updateRevision(con, einfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				// 3 - Insert new event recalculating start/end from rec. start preserving days duration
				event.setStartDate(event.getStartDate().withDate(eventKey.instanceDate));
				event.setEndDate(event.getEndDate().withDate(eventKey.instanceDate.plusDays(oldDaysBetween)));
				
				// We cannot keep original count, it would be wrong... so convert it to an until date!
				if (ICal4jUtils.recurHasCount(oldRecur)) {
					DateTime oldUntilReal = ICal4jUtils.calculateRecurrenceEnd(oldRecur, oevtOrig.getStartDate(), oevtOrig.getStartDate(), oevtOrig.getEndDate(), oevtOrig.getDateTimezone());
					ICal4jUtils.setRecurUntilDate(oldRecur, oldUntilReal);
				}
				event.setRecurrence(oldRecur.toString(), eventKey.instanceDate, null);
				EventInsertResult insert = doEventInsert(con, event, null, true, false, false, false, false);

				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(einfo.getEventId()));
				writeLog("EVENT_INSERT", String.valueOf(insert.event.getEventId()));
				
				// TODO: eventually add support to clone attendees in the newly inserted event and so sending invitation emails
				
				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.ALL_SERIES.equals(target)) { // Changes are valid for all the instances (whole recurrence)
				// We need to restore original dates because current start/end refers
				// to instance and not to the master event.
				event.setStartDate(event.getStartDate().withDate(oevtOrig.getStartDate().toLocalDate()));
				event.setEndDate(event.getEndDate().withDate(oevtOrig.getEndDate().toLocalDate()));
				
				// 1 - Updates event with new data
				doEventUpdate(con, oevtOrig, event, true, false, true, true);

				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(einfo.getEventId()));

				eventDump = getEvent(einfo.getEventId());
			}
			
		} else if (einfo.isBroken()) {
			// 1 - Updates broken event (follow eventId) with new data
			doEventUpdate(con, oevtOrig, event, false, false, true, true);
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_UPDATE", String.valueOf(einfo.getEventId()));
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());
			
			eventDump = getEvent(einfo.getEventId());
			
		} else {
			// 1 - Updates this event with new data
			doEventUpdate(con, oevtOrig, event, true, false, true, true);
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_UPDATE", String.valueOf(oevtOrig.getEventId()));
			
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_TITLE, event.getTitle());
			//core.addServiceSuggestionEntry(SERVICE_ID, SUGGESTION_EVENT_LOCATION, event.getLocation());

			eventDump = getEvent(einfo.getEventId());
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
	
	private void doEventInstanceDeleteAndCommit(Connection con, UpdateEventTarget target, EventKey eventKey, boolean notifyAttendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEventInfo einfo = evtDao.selectOEventInfoById(con, eventKey.eventId);
		if (einfo == null) throw new WTException("Unable to retrieve event info [{}]", eventKey.eventId);
		
		Event eventDump = null;
		if (einfo.isRecurring()) {
			if (UpdateEventTarget.THIS_INSTANCE.equals(target)) { // Changes are valid for this specific instance
				// 1 - Inserts new broken record (without new broken event) on deleted date
				doRecurrenceExcludeDate(con, einfo.getEventId(), einfo.getRecurrenceId(), eventKey.instanceDate, null);
				
				// 2 - Updates revision of this event
				evtDao.updateRevision(con, einfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(einfo.getEventId()));
				
				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.SINCE_INSTANCE.equals(target)) { // Changes are valid from this instance onward
				// 1 - Resize original recurrence (sets until date at the day before date)
				ORecurrence orec = recDao.select(con, einfo.getRecurrenceId());
				if (orec == null) throw new WTException("Unable to get master event's recurrence [{}]", einfo.getRecurrenceId());
				
				//orec.updateUntilDate(eventKey.instanceDate, einfo.getDateTimeZone());
				LocalTime untilTime = einfo.getAllDay() ? DateTimeUtils.TIME_AT_STARTOFDAY : einfo.getStartDate().withZone(einfo.getDateTimeZone()).toLocalTime();
				orec.updateUntilDate(eventKey.instanceDate, untilTime, einfo.getDateTimeZone());
				recDao.update(con, orec);
				
				// 2 - Updates revision of this event
				evtDao.updateRevision(con, einfo.getEventId(), BaseDAO.createRevisionTimestamp());
				
				DbUtils.commitQuietly(con);
				writeLog("EVENT_UPDATE", String.valueOf(einfo.getEventId()));
				
				eventDump = getEvent(einfo.getEventId());
				
			} else if (UpdateEventTarget.ALL_SERIES.equals(target)) { // Changes are valid for all the instances (whole recurrence)
				eventDump = getEvent(einfo.getEventId()); // Save for later use!

				// 1 - logically delete this event
				doEventDelete(con, einfo.getEventId(), true);

				DbUtils.commitQuietly(con);
				writeLog("EVENT_DELETE", String.valueOf(einfo.getEventId()));
			}
			
		} else if (einfo.isBroken()) {
			eventDump = getEventInstance(eventKey); // Save for later use!
			
			// 1 - Logically delete this event (the broken)
			doEventDelete(con, einfo.getEventId(), true);
			
			// 2 - Updates revision of linked event
			evtDao.updateRevision(con, einfo.getLinkedEventId(), BaseDAO.createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(einfo.getEventId()));
			writeLog("EVENT_UPDATE", String.valueOf(einfo.getLinkedEventId()));
			
		} else {
			eventDump = getEventInstance(eventKey); // Save for later use!
			
			// 1 - logically delete this event
			doEventDelete(con, einfo.getEventId(), true);
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(einfo.getEventId()));
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
	
	private void doEventInstanceRestoreAndCommit(Connection con, EventKey eventKey, boolean notifyAttendees) throws WTException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		
		//if (con.getAutoCommit()) throw new WTException("This method should not be called in this way. Connection must not be in auto-commit mode!");
		
		OEventInfo einfo = evtDao.selectOEventInfoById(con, eventKey.eventId);
		if (einfo == null) throw new WTException("Unable to retrieve event info [{}]", eventKey.eventId);
		
		Event eventDump = null;
		if (einfo.isBroken()) {
			eventDump = getEventInstance(eventKey); // Save for later use!
			
			// 1 - Removes the broken record
			rbkDao.deleteByNewEvent(con, einfo.getEventId());
			
			// 2 - Logically delete this event (the broken)
			doEventDelete(con, einfo.getEventId(), true);
			
			// 3 - updates revision of linked event
			evtDao.updateRevision(con, einfo.getLinkedEventId(), BaseDAO.createRevisionTimestamp());
			
			DbUtils.commitQuietly(con);
			writeLog("EVENT_DELETE", String.valueOf(einfo.getEventId()));
			writeLog("EVENT_UPDATE", String.valueOf(einfo.getLinkedEventId()));
			
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
	
	private boolean doEventInputUpdate(Connection con, HashMap<String, OEvent> cache, EventInput input, boolean disarmPastReminder) throws DAOException, IOException {
		//TODO: Make this smart avoiding delete/insert!
		doEventDelete(con, input.event.getEventId(), false);
		doEventInputInsert(con, cache, input, disarmPastReminder);
		return true;
	}
	
	private EventInsertResult doEventInputInsert(Connection con, HashMap<String, OEvent> cache, EventInput ei, boolean disarmPastReminder) throws DAOException, IOException {
		EventInsertResult insert = doEventInsert(con, ei.event, null, true, true, true, false, disarmPastReminder);
		if (insert.recurrence != null) {
			// Cache recurring event for future use within broken references 
			cache.put(insert.event.getPublicUid(), insert.event);
			
		} else {
			if (ei.addsExOnMaster != null) {
				if (cache.containsKey(ei.exRefersToPublicUid)) {
					final OEvent oevt = cache.get(ei.exRefersToPublicUid);
					doRecurrenceExcludeDate(con, oevt, ei.addsExOnMaster, insert.event.getEventId());
				}
			}
		}
		return insert;
	}
	
	private EventInsertResult doEventInsert(Connection con, Event event, String rawICalendar, boolean processRecurrence, boolean processExcludedDates, boolean processAttendees, boolean processAttachments, boolean disarmPastReminder) throws DAOException, IOException {
		EventDAO evtDao = EventDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
		
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
		if (disarmPastReminder && (oevt.getReminder() != null) && oevt.getStartDate().isBeforeNow()) {
			oevt.setRemindedOn(revTimestamp);
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
		
		ArrayList<OEventAttachment> oattchs = null;
		if (processAttachments && (event.getAttachments() != null)) {
			oattchs = new ArrayList<>();
			for (EventAttachment att : event.getAttachments()) {
				if (!(att instanceof EventAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				oattchs.add(doEventAttachmentInsert(con, oevt.getEventId(), (EventAttachmentWithStream)att));
			}
		}
		
		return new EventInsertResult(oevt, orec, obrks, oattes, oattchs);
	}
	
	private boolean doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean processAttendees, boolean processAttachments) throws IOException, WTException {
		return doEventUpdate(con, originalEvent, event, false, false, processAttendees, processAttachments);
	}
	
	private boolean doEventUpdate(Connection con, OEvent originalEvent, Event event, boolean processRecurrence, boolean processExcludedDates, boolean processAttendees, boolean processAttachments) throws IOException, WTException {
		EventDAO evtDao = EventDAO.getInstance();
		EventAttendeeDAO atteDao = EventAttendeeDAO.getInstance();
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		RecurrenceDAO recDao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbkDao = RecurrenceBrokenDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		if (StringUtils.isBlank(event.getOrganizer())) {
			event.setOrganizer(ManagerUtils.buildOrganizer(getTargetProfileId())); // Make sure organizer is filled
		}
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
		if (processAttachments && (event.getAttachments() != null)) {
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
		return ret;
	}
	
	private int doEventDelete(Connection con, int eventId, boolean logicDelete) throws DAOException {
		EventDAO evtDao = EventDAO.getInstance();
		
		if (logicDelete) {
			return evtDao.logicDeleteById(con, eventId, BaseDAO.createRevisionTimestamp());
		} else {
			RecurrenceDAO recDao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO recbkDao = RecurrenceBrokenDAO.getInstance();
			//EventAttendeeDAO attDao = EventAttendeeDAO.getInstance();
			
			recDao.deleteByEvent(con, eventId);
			recbkDao.deleteByEvent(con, eventId);
			//attDao.deleteByEvent(con, eventId);
			//doEventICalendarDelete(con, eventId);
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
	
	private void doEventMove(Connection con, boolean copy, Event event, int targetCalendarId) throws DAOException, IOException {
		if (copy) {
			EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
			
			event.setCalendarId(targetCalendarId);
			event.setPublicUid(null); // Reset value in order to make inner function generate new one!
			event.setHref(null); // Reset value in order to make inner function generate new one!
			OEventICalendar oica = icaDao.selectById(con, event.getEventId());
			String rawICalendar = (oica != null) ? oica.getRawData() : null;
			//TODO: maybe add support to attachments copy
			doEventInsert(con, event, rawICalendar, true, false, true, false, false);
			
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
			instances.addAll(calculateRecurringInstances(con, new VExpEventInstanceMapper(vee), fromDate, toDate, DateTimeZone.UTC, 14+1));
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
	
	private boolean doEventICalendarDelete(Connection con, int eventId) throws DAOException {
		EventICalendarDAO icaDao = EventICalendarDAO.getInstance();
		return icaDao.deleteById(con, eventId) == 1;
	}
	
	private OEventAttachment doEventAttachmentInsert(Connection con, int eventId, EventAttachmentWithStream attachment) throws DAOException, IOException {
		EventAttachmentDAO attchDao = EventAttachmentDAO.getInstance();
		
		OEventAttachment oattch = ManagerUtils.createOTaskAttachment(attachment);
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
		
		OEventAttachment oattch = ManagerUtils.createOTaskAttachment(attachment);
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
			if (tgt.getBusy() == null) tgt.setBusy(false);
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
				tgt.setPublicUid(ManagerUtils.buildEventUid(tgt.getEventId(), WT.getDomainInternetName(getTargetProfileId().getDomainId())));
			}
			if (StringUtils.isBlank(tgt.getHref())) tgt.setHref(ManagerUtils.buildHref(tgt.getPublicUid()));
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
		UserProfile.Data udFrom = WT.getUserData(fromProfileId);
		InternetAddress from = udFrom.getPersonalEmail();
		
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
				String customBodyHtml = TplHelper.buildEventModificationBody(ud.getLocale(), ud.getShortDateFormat(), ud.getShortTimeFormat(), event);
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
			
			IMailManager mailMgr = (IMailManager)WT.getServiceManager("com.sonicle.webtop.mail");
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
					String customBodyHtml = TplHelper.buildTplEventInvitationBody(ud.getLocale(), ud.getShortDateFormat(), ud.getShortTimeFormat(), event, crud, rcpt.recipient.getAddress(), servicePublicUrl);
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
			String subject = TplHelper.buildResponseUpdateTitle(locale, event, targetAttendee);
			String customBodyHtml = TplHelper.buildTplResponseUpdateBody(locale, dateFormat, timeFormat, event, servicePublicUrl);
			
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
	
	private void checkRightsOnCalendarRoot(UserProfileId owner, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if (RunContext.isWebTopAdmin()) return;
		if (owner.equals(targetPid)) return;
		
		String shareId = shareCache.getShareRootIdByOwner(owner);
		if (shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", owner);
		CoreManager core = WT.getCoreManager(targetPid);
		if (core.isShareRootPermitted(shareId, action)) return;
		
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
		}
		
		// Checks rights on calendar instance
		String shareId = shareCache.getShareFolderIdByFolderId(calendarId);
		if (shareId == null) throw new WTException("calendarToLeafShareId({0}) -> null", calendarId);
		if (core.isShareFolderPermitted(shareId, action)) return;
		
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
	
	private ReminderInApp createEventReminderAlertWeb(VExpEventInstance instance) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, instance.getCalendarProfileId(), "event", instance.getKey());
		alert.setTitle(instance.getTitle());
		alert.setDate(instance.getStartDate().withZone(instance.getDateTimeZone()));
		alert.setTimezone(instance.getTimezone());
		return alert;
	}
	
	private ReminderEmail createEventReminderAlertEmail(Locale locale, String dateFormat, String timeFormat, String recipientEmail, UserProfileId ownerId, EventInstance event) throws WTException {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, ownerId, "event", event.getKey());
		
		try {
			String source = NotificationHelper.buildSource(locale, SERVICE_ID);
			String because = lookupResource(locale, CalendarLocale.EMAIL_REMINDER_FOOTER_BECAUSE);
			String customBodyHtml = TplHelper.buildTplEventReminderBody(locale, dateFormat, timeFormat, event);
			
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
		public DateTimeZone getEventTimezone() {
			return event.getDateTimezone();
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
		public DateTime getEventStartDate();
		public DateTime getEventEndDate();
		public DateTimeZone getEventTimezone();
		public T createInstance(String key, DateTime startDate, DateTime endDate);
	}
	
	public static class EventInsertResult {
		public final OEvent event;
		public final ORecurrence recurrence;
		public final List<ORecurrenceBroken> recurrenceBrokens;
		public final List<OEventAttendee> attendees;
		public final List<OEventAttachment> attachments;
		
		public EventInsertResult(OEvent event, ORecurrence recurrence, List<ORecurrenceBroken> recurrenceBrokens, ArrayList<OEventAttendee> attendees, List<OEventAttachment> attachments) {
			this.event = event;
			this.recurrence = recurrence;
			this.recurrenceBrokens = recurrenceBrokens;
			this.attendees = attendees;
			this.attachments = attachments;
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
