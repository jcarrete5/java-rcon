package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
		in.close();
		
		Logger.getGlobal().info(hostname + ":" + port);
		
		// Attempt a connection
		try {
			ServerAPI.get().connect(hostname, port);
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to establish connection", e);
			System.exit(1);
		}
	}
	
	private static CommandLine parseOptions(String[] args) {
		Options opts = new Options();
		opts.addOption("H", "hostname", true, "Specify the hostname to connect to");
		opts.addOption("p", "port", true, "Port to connect to");
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
		Logger global = Logger.getGlobal();
		FileHandler handle = new FileHandler("client.log", true);
		handle.setFormatter(new SimpleFormatter());
		global.addHandler(handle);
	}
}
