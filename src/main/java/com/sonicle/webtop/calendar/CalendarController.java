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
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.events.ResourceAvailabilityChangeEvent;
import com.sonicle.webtop.core.app.events.ResourceUpdateEvent;
import com.sonicle.webtop.core.app.events.UserUpdateEvent;
import com.sonicle.webtop.core.app.model.ResourcePermissions;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.List;
import org.joda.time.DateTime;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerRemindersHooks;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerServiceHooks;
import com.sonicle.webtop.core.msg.ResourceAvailChangeSM;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import net.engio.mbassy.listener.Handler;

/**
 *
 * @author malbinola
 */
public class CalendarController extends BaseController implements IControllerServiceHooks, IControllerRemindersHooks {
	//private static final Logger LOGGER = WT.getLogger(CalendarController.class);
	
	public CalendarController() {
		super();
	}
	
	@Override
	public void initProfile(ServiceVersion current, UserProfileId profileId) throws WTException {}
	
	@Override
	public void upgradeProfile(ServiceVersion current, UserProfileId profileId, ServiceVersion profileLastSeen) throws WTException {}

	@Override
	public List<BaseReminder> returnReminders(DateTime now) {
		CalendarManager manager = new CalendarManager(true, RunContext.getRunProfileId());
		return manager.getRemindersToBeNotified(now);
	}
	
	@Handler
	public void onUserUpdateEvent(UserUpdateEvent event) {
		if (UserUpdateEvent.Type.CREATE.equals(event.getType())) {
			try {
				CalendarManager manager = createManager(event.getUserProfileId());
				manager.addBuiltInCalendar();
				
			} catch (Exception ex) {
				throw new WTRuntimeException("Error creating built-in calendar for '{}': \"{}\"", event.getUserProfileId().toString(), ex.getMessage());
			}
		} else if (UserUpdateEvent.Type.DELETE.equals(event.getType())) {
			try {
				CalendarManager manager = createManager(event.getUserProfileId());
				manager.eraseData(true);
				
			} catch (Exception ex) {
				throw new WTRuntimeException("Error clearing data for '{}': \"{}\"", event.getUserProfileId().toString(), ex.getMessage());
			}
		}
	}
	
	@Handler
	public void onResourceUpdateEvent(ResourceUpdateEvent event) {
		if (ResourceUpdateEvent.Type.CREATE.equals(event.getType())) {
			try {
				CalendarManager manager = createManager(event.getResourceProfileId());
				manager.addBuiltInCalendar();
				
			} catch (Exception ex) {
				throw new WTRuntimeException("Error creating built-in calendar for '{}': \"{}\"", event.getResourceProfileId().toString(), ex.getMessage());
			}
		} else if (ResourceUpdateEvent.Type.DELETE.equals(event.getType())) {
			try {
				CalendarManager manager = createManager(event.getResourceProfileId());
				manager.eraseData(true);
				
			} catch (Exception ex) {
				throw new WTRuntimeException("Error clearing data for '{}': \"{}\"", event.getResourceProfileId().toString(), ex.getMessage());
			}
		}
	}
	
	@Handler
	public void onResourceAvailabilityChangeEvent(ResourceAvailabilityChangeEvent event) {
		final ResourceAvailChangeSM message = createResourceAvailChangeMessage(event);
		if (ResourceAvailabilityChangeEvent.Type.ENABLE.equals(event.getType())) {
			try {
				final Set<UserProfileId> profileIds = collectResourceUsersToBeNotified(event.getResourceProfileId());
				for (UserProfileId profileId : profileIds) {
					WT.notify(profileId, message);
				}
			} catch (Exception ex) {
				throw new WTRuntimeException("Error notifying users: {}", ex.getMessage());
			}
		} else if (ResourceAvailabilityChangeEvent.Type.DISABLE.equals(event.getType())) {
			try {
				final Set<UserProfileId> profileIds = collectResourceUsersToBeNotified(event.getResourceProfileId());
				for (UserProfileId profileId : profileIds) {
					WT.notify(profileId, message);
				}
			} catch (Exception ex) {
				throw new WTRuntimeException("Error notifying users: {}", ex.getMessage());
			}
		}
	}
	
	private Set<UserProfileId> collectResourceUsersToBeNotified(UserProfileId resourceProfile) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(true, RunContext.buildDomainAdminProfileId(resourceProfile.getDomainId()));
		
		Set<UserProfileId> items = new LinkedHashSet<>();
		ResourcePermissions permissions = coreMgr.getResourcePermissions(resourceProfile.getUserId(), true);
		items.addAll(coreMgr.expandSubjectsToUserProfiles(Arrays.asList(permissions.getManagerSubject()), true));
		items.addAll(coreMgr.expandSubjectsToUserProfiles(permissions.getAllowedSubjects(), true));
		return items;
	}
	
	private ResourceAvailChangeSM createResourceAvailChangeMessage(ResourceAvailabilityChangeEvent event) {
		final String dn = WT.getProfileData(event.getResourceProfileId()).getDisplayName();
		final boolean available = ResourceAvailabilityChangeEvent.Type.ENABLE.equals(event.getType());
		return new ResourceAvailChangeSM(SERVICE_ID, event.getResourceProfileId().getUserId(), dn, event.getResourceType(), available);
	}
	
	private CalendarManager createManager(UserProfileId profileId) {
		return new CalendarManager(true, profileId);
	}
}
