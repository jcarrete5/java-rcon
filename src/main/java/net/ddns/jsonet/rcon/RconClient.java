package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import net.ddns.jsonet.rcon.logging.EndUserFormatter;
import net.ddns.jsonet.rcon.logging.LogFileFormatter;

public class RconClient {
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
		in.close();
		
		// Attempt a connection
		ServerAPI api = ServerAPI.get();
		try {
			api.connect(hostname, port);
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to establish connection to " + hostname + ":" + port, e);
			System.exit(1);
		}
		
		try {
			api.authenticate(passwd);
		} catch (IOException e) {
			Logger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to authenticate with " + hostname + ":" + port, e);
			System.exit(1);
		}
	}
	
	private static CommandLine parseOptions(String[] args) {
		Options opts = new Options();
		opts.addOption("H", "hostname", true, "Specify the hostname to connect to");
		opts.addOption("p", "port", true, "Port to connect to");
		opts.addOption("P", "password", true, "Password used to connect to the server");
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
		
		StreamHandler cmdHandler = new StreamHandler(System.out, new EndUserFormatter());
		cmdHandler.setLevel(Level.INFO);
		logger.addHandler(cmdHandler);
	}
}
