package me.jasonrcarrete.rcon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "java-rcon")
public class RconClient implements Callable<Integer> {
	@CommandLine.Option(names = {"-H", "--hostname"}, description = "The hostname to connect to", defaultValue = "localhost")
	String hostname;
	@CommandLine.Option(names = {"-p", "--port"}, description = "The port to connect to", defaultValue = "25575")
	int port;
	@CommandLine.Option(names = {"-P", "--password"}, description = "Password used for authentication", interactive = true)
	String password;
	@CommandLine.Option(names = {"-h", "--help"}, description = "Show help information")
	boolean help;
	@CommandLine.Parameters
	List<String> commands;

	private enum ExitCode {
		CONNECTION_ERROR(1),
		AUTH_FAILED(2);

		public final int code;

		ExitCode(final int code) {
			this.code = code;
		}
	}

	public static void main(String[] args) {
		System.exit(new CommandLine(new RconClient()).execute(args));
	}

	@Override
	public Integer call() throws Exception {
		final Scanner in = new Scanner(System.in);

		ServerAPI api = ServerAPI.get();
		try {
			api.connect(hostname, port);
		} catch (IOException e) {
			System.err.printf("Failed to establish connection to %s:%d\n", hostname, port);
			e.printStackTrace();
			return ExitCode.CONNECTION_ERROR.code;
		}
		System.out.printf("Connected to %s:%d\n", hostname, port);
		
		try {
			int reqId = api.authenticate(password);
			ServerAPI.Packet p = api.parsePacket();
			if (p.getRequestID() == -1 && p.getRequestID() != reqId) {
				System.err.println("Auth failed: Invalid passphrase");
				return ExitCode.AUTH_FAILED.code;
			}
		} catch (IOException e) {
			System.err.printf("Failed to authenticate with %s:%d\n", hostname, port);
			e.printStackTrace();
			return ExitCode.CONNECTION_ERROR.code;
		}
		System.out.println("Authentication successful");
		
		if (commands.isEmpty()) {
			handleInput(in);
		} else {
		    commands.forEach(RconClient::issueCommand);
		}

		return 0;
	}
	
	private static void issueCommand(String cmd) {
		final ServerAPI api = ServerAPI.get();
		int requestId;
		try {
			requestId = api.sendCommand(cmd);
		} catch (IOException e) {
			System.err.printf("Failed to send command: '%s'\n", cmd);
			e.printStackTrace();
			return;
		}
		
		try {
			ServerAPI.Packet p = api.parsePacket();
			if (p.getRequestID() == requestId) {
				String resp = new String(p.getRawData(), StandardCharsets.US_ASCII);
				System.out.println(resp);
			}
		} catch (IOException e) {
			System.err.println("Failed to parse response packet");
			e.printStackTrace();
		}
	}
	
	/**
	 * All input is sent "as-is" to the server.
	 */
	private static void handleInput(final Scanner in) {
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
}
