package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerAPI {
	private static ServerAPI instance;
	
	public static ServerAPI get() {
		if (instance == null) {
			return instance = new ServerAPI();
		} else {
			return instance;
		}
	}
	
	private Socket client;
	
	private ServerAPI() {
		client = new Socket();
	}
	
	/**
	 * Attempts a to connect to the remote rcon server. If the ServerAPI is alreader connected
	 * to a remote server, invoking this method will close the connection and create a new connection.
	 * @param hostname address of the remote server.
	 * @param port port the remote server is listening on.
	 * @throws IOException if the connection fails for some reason.
	 */
	public void connect(String hostname, int port) throws IOException {
		if (client.isConnected()) {
			client.close();
		}
		if (client.isClosed()) {
			client = new Socket();
		}
		
		client.setKeepAlive(true);
		client.setTrafficClass(0x04);
		client.setSendBufferSize(1460);
		client.setReceiveBufferSize(1234);
		client.connect(new InetSocketAddress(hostname, port));
	}
	
	public void disconnect() throws IOException {
		client.close();
	}
	
	/**
	 * Attempts to authenticate with the connected server.
	 * @param asciiPassword password to use for authentication.
	 * @return the request id
	 */
	public int authenticate(String asciiPassword) {
		if (client.isClosed() || !client.isConnected()) {
			throw new IllegalStateException("Tried to authenticate with a server before connecting to it!");
		}
		
		return 0;
	}
	
	/**
	 * Attempts to send a command to the connected server.
	 * @param command command to execute on the server.
	 * @return the request id.
	 */
	public int sendCommand(String command) {
		if (client.isClosed() || !client.isConnected()) {
			throw new IllegalStateException("Tried to send a command to a server before connecting to it!");
		}
		
		return 0;
	}
}
