package net.ddns.jsonet.rcon.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class EndUserFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		StringBuilder formatted = new StringBuilder();
		formatted.append('[' + record.getLevel().getName() + ']');
		formatted.append(' ');
		formatted.append(formatMessage(record));
		return formatted.toString();
	}
}
