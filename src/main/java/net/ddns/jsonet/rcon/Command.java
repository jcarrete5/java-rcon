package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Command {
	quit((args) -> {
		try {
			ServerAPI api = ServerAPI.get();
			Logger.getLogger("net.ddns.jsonet.rcon").info("Closing socket from server");
			api.disconnect();
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Error closing socket from server", e);
		}
		System.exit(0);
		return null;
	}),
	help((args) -> {
		return null;
	});

	private Function<Object[], ?> action;

	Command(Function<Object[], ?> action) {
		this.action = action;
	}

	public static void executeCommandWithName(String name, Object... args) {
		try {
			Command cmd = Command.valueOf(name);
			cmd.action.apply(args);
		} catch (IllegalArgumentException e) {
			//TODO Handle this exception
		}
	}
}
