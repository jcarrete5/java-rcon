package me.jasonrcarrete.rcon;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Charsets;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

public class RconClient {
	private static final String cmdDelimiter = ";";

	@Option(names = {"-H", "--hostname"}, description = "The hostname to connect to", defaultValue = "localhost")
	static String hostname;

	@Option(names = {"-p", "--port"}, description = "The port to connect to", defaultValue = "25575")
	static int port;

	@Option(names = {"-P", "--password"}, description = "Password used for authentication")
	static String password;

	private enum ExitCode {
		CONNECTION_ERROR(1),
		AUTH_FAILED(2);

		public final int code;

		ExitCode(final int code) {
			this.code = code;
		}
	}

	public static void main(String[] args) {
		final Scanner in = new Scanner(System.in);
		if (password == null) {
		    final Console console = System.console();
			if (console == null) {
				System.out.print("Password: ");
				password = in.nextLine();
			} else {
				password = String.valueOf(console.readPassword("Password: "));
			}
		}

		ServerAPI api = ServerAPI.get();
		try {
			api.connect(hostname, port);
		} catch (IOException e) {
			System.err.printf("Failed to establish connection to %s:%d\n", hostname, port);
			e.printStackTrace();
			System.exit(ExitCode.CONNECTION_ERROR.code);
		}
		System.out.printf("Connected to %s:%d\n", hostname, port);
		
		try {
			int reqId = api.authenticate(password);
			ServerAPI.Packet p = api.parsePacket();
			if (p.getRequestID() == -1 && p.getRequestID() != reqId) {
				System.err.println("Auth failed: Invalid passphrase");
				System.exit(ExitCode.AUTH_FAILED.code);
			}
		} catch (IOException e) {
			System.err.printf("Failed to authenticate with %s:%d\n", hostname, port);
			e.printStackTrace();
			System.exit(ExitCode.CONNECTION_ERROR.code);
		}
		Logger.getLogger("net.ddns.jsonet.rcon").info("Authentication successful");
		
		if (commands == null) {
			handleInput(in);
		} else {
			// TODO this won't allow commands to contain and instances of cmdDelimiter
			for (String command : commands.split(cmdDelimiter)) {
				issueCommand(command);
			}
		}
	}
	
	private static void issueCommand(String cmd) {
		ServerAPI api = ServerAPI.get();
		int requestId;
		try {
			requestId = api.sendCommand(cmd);
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to send command '"+cmd+"'", e);
			return;
		}
		
		try {
			ServerAPI.Packet p = api.parsePacket();
			if (p.getRequestID() == requestId) {
				String resp = new String(p.getRawData(), Charsets.US_ASCII);
				System.out.println(resp);
			}
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to parse response packet", e);
		}
	}
	
	/**
	 * All input is sent "as-is" to the server.
	 */
	private static void handleInput(Scanner in) {
		while (true) {
			System.out.print("> ");
			String cmd = in.nextLine();
			if (cmd.startsWith(".")) {
				Command.executeCommandWithName(cmd.substring(1));
			} else {
				issueCommand(cmd);
			}
		}
	}
	
	private static CommandLine parseOptions(String[] args) {
		Options opts = new Options();
		opts.addOption("H", "hostname", true, "Specify the hostname to connect to");
		opts.addOption("p", "port", true, "Port to connect to");
		opts.addOption("P", "password", true, "Password used to connect to the server");
		opts.addOption("c", "command", true, "Sends commands delimited by '"+cmdDelimiter+"' to the remote server");
		CommandLineParser parser = new BasicParser();
		try {
			return parser.parse(opts, args);
		} catch (ParseException e) {
			HelpFormatter format = new HelpFormatter();
			format.printHelp("java -jar java-rcon.jar", opts, true);
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	private static void setupLogger() throws IOException {
		Logger logger = Logger.getLogger("net.ddns.jsonet.rcon");
		logger.setUseParentHandlers(false);
		
		FileHandler fileHandler = new FileHandler("client.log");
		fileHandler.setFormatter(new LogFileFormatter());
		fileHandler.setLevel(Level.ALL);
		logger.addHandler(fileHandler);
		
		ConsoleHandler cmdHandler = new ConsoleHandler();
		cmdHandler.setFormatter(new EndUserFormatter());
		logger.addHandler(cmdHandler);
	}
}
