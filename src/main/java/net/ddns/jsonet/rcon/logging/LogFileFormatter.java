package net.ddns.jsonet.rcon.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFileFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		StringBuilder formatted = new StringBuilder();
		Date date = Date.from(Instant.ofEpochMilli(record.getMillis()));
		DateFormat df = new SimpleDateFormat("[HH:mm:ss:SSS]");
		formatted.append(df.format(date));
		formatted.append(' ');
		formatted.append('[' + record.getLevel().getName() + ']');
		formatted.append(' ');
		formatted.append(formatMessage(record));
		if (record.getThrown() != null) {
			formatted.append('\n');
			formatted.append(record.getThrown());
		}
		formatted.append('\n');
		return formatted.toString();
	}
}
