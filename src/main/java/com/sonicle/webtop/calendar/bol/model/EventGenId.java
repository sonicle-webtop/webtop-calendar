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
package com.sonicle.webtop.calendar.bol.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class EventGenId {
	private static final Pattern PATTERN_GENID = Pattern.compile("^([0-9]+)_([0-9]+)$");
	private static final Pattern PATTERN_GENID_RECURRING = Pattern.compile("^([0-9]+)_([0-9]+)_([0-9]+)$");

	public Integer eventId;
	public Integer originalEventId;
	public LocalDate atDate;

	public EventGenId(String eventUid) {
		String decoded = null;
		try {
			decoded = new String(Hex.decodeHex(eventUid.toCharArray()));
		} catch(DecoderException ex) {
			throw new RuntimeException(ex);
		}
		
		Matcher matcher = null;
		if((matcher = PATTERN_GENID_RECURRING.matcher(decoded)).matches()) {
			originalEventId = Integer.valueOf(matcher.group(1));
			eventId = Integer.valueOf(matcher.group(2));
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);
			atDate = formatter.parseDateTime(matcher.group(3)).toLocalDate();
		} else if((matcher = PATTERN_GENID.matcher(decoded)).matches()) {
			originalEventId = Integer.valueOf(matcher.group(1));
			eventId = Integer.valueOf(matcher.group(2));
		}
	}
	
	public static String buildId(Integer eventId, Integer originalEventId) {
		String str = originalEventId + "_" + eventId;
		return Hex.encodeHexString(str.getBytes());
	}
	
	public static String buildId(Integer eventId, Integer originalEventId, LocalDate date) {
		String str = originalEventId + "_" + eventId + "_" + date.toString("yyyyMMdd");
		return Hex.encodeHexString(str.getBytes());
	}
}
