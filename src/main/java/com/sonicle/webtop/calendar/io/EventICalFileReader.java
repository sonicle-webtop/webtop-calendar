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
package com.sonicle.webtop.calendar.io;

import com.sonicle.webtop.core.app.ical4j.LazyCalendarComponentConsumer;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.LogEntries;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class EventICalFileReader implements EventFileReader {
	private final DateTimeZone defaultTz;
	
	public EventICalFileReader(DateTimeZone defaultTz) {
		this.defaultTz = defaultTz;
	}
	
	@Override
	public ArrayList<EventInput> listEvents(LogEntries log, File file) throws IOException, UnsupportedOperationException, WTException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return listEvents(log, fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	@Override
	public void listEvents(LogEntries log, File file, EventInputConsumer consumer) throws IOException, ParserException, UnsupportedOperationException, WTException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			listEvents(log, fis, consumer);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public ArrayList<EventInput> listEvents(LogEntries log, InputStream is) throws IOException, UnsupportedOperationException, WTException {
		Calendar cal = null;
		try {
			cal = ICalendarUtils.parse(is);
		} catch(ParserException ex) {
			throw new UnsupportedOperationException(ex);
		}
		return new ICalendarInput(defaultTz).fromICalendarFile(cal, log);
	}
	
	public void listEvents(LogEntries log, InputStream is, EventInputConsumer consumer) throws IOException, ParserException, UnsupportedOperationException, WTException {
		new ICalendarInput(defaultTz)
				.withIncludeVEventSourceInOutput(true)
				.fromICalendarStream(is, log, consumer);
	}
	
}
