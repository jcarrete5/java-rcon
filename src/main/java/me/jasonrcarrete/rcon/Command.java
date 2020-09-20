package me.jasonrcarrete.rcon;

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

	private Function<Object[], Void> action;

	Command(Function<Object[], Void> action) {
		this.action = action;
	}

	/**
	 * Attempts to execute a command called {@code name} with {@code args}.
	 * @param name Name of the command
	 * @param args Arguments for the command
	 * @return {@code true} if the command exists and was executed, otherwise {@code false}
	 */
	public static boolean executeCommandWithName(String name, Object... args) {
		try {
			Command cmd = Command.valueOf(name);
			cmd.execute(args);
			return true;
		} catch (IllegalArgumentException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").info("Unknown client command: "+name);
			return false;
		}
	}

	public void execute(Object... args) {
		action.apply(args);
	}
}
