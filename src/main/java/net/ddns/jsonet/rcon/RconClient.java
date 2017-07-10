package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.google.common.base.Charsets;
import net.ddns.jsonet.rcon.ServerAPI.Packet;
import net.ddns.jsonet.rcon.logging.EndUserFormatter;
import net.ddns.jsonet.rcon.logging.LogFileFormatter;

public class RconClient {
	private static final String cmdDelimiter = ";";
	
	public static void main(String[] args) {
		try {
			setupLogger();
		} catch (IOException e) {
			System.err.println("Failed to setup logger!");
			e.printStackTrace();
		}
		
		CommandLine cmd = parseOptions(args);
		
		String hostname = cmd.getOptionValue('H');
		int port = 25575;
		String passwd =	cmd.getOptionValue('P');
		String commands = cmd.getOptionValue('c');
		
		Scanner in = new Scanner(System.in);
		if (hostname == null) {
			System.out.print("Hostname: ");
			hostname = in.nextLine();
		}
		if (cmd.getOptionValue('p') == null) {
			System.out.print("Port (Default: 25575): ");
			String line = in.nextLine();
			if (!line.equals("")) {
				port = Integer.parseInt(line);
			}
		}
		if (passwd == null) {
			if (System.console() == null) {
				System.out.print("Password: ");
				passwd = in.nextLine();
			} else {
				passwd = String.valueOf(System.console().readPassword("Password: "));
			}
		}
		
		// Attempt a connection
		ServerAPI api = ServerAPI.get();
		try {
			api.connect(hostname, port);
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to establish connection to " + hostname + ":" + port, e);
			System.exit(1);
		}
		Logger.getLogger("net.ddns.jsonet.rcon").info("Connected to "+hostname+":"+port);
		
		// Authenticate
		try {
			int reqId = api.authenticate(passwd);
			Packet p = api.parsePacket();
			if (p.getRequestID() == -1 && p.getRequestID() != reqId) {
				// Auth failed
				Logger.getLogger("net.ddns.jsonet.rcon").severe("Auth failed: Invalid passphrase");
				System.exit(1);
			}
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to authenticate with " + hostname + ":" + port, e);
			System.exit(1);
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
			Packet p = api.parsePacket();
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
	 * TODO Parse commands before sending them for specific application commands
	 */
	private static void handleInput(Scanner in) {
		while (true) {
			System.out.print("> ");
			String cmd = in.nextLine();
			issueCommand(cmd);
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
