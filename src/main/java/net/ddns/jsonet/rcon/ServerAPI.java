package net.ddns.jsonet.rcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import java.util.logging.Logger;
import com.google.common.base.Charsets;

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
		client.setReceiveBufferSize(4096);
		client.connect(new InetSocketAddress(hostname, port));
	}
	
	public void disconnect() throws IOException {
		client.close();
	}
	
	/**
	 * Attempts to authenticate with the connected server.
	 * @param asciiPassword password to use for authentication.
	 * @return the request id.
	 * @throws IOException if auth message failed to send.
	 */
	public int authenticate(String asciiPassword) throws IOException {
		if (client.isClosed() || !client.isConnected()) {
			throw new IllegalStateException("Tried to authenticate with a server before connecting to it!");
		}
		
		Packet p = new Packet(Packet.TYPE_LOGIN, asciiPassword.getBytes(Charsets.US_ASCII));
		p.send();
		return p.getRequestID();
	}
	
	/**
	 * Attempts to send a command to the connected server.
	 * @param command command to execute on the server.
	 * @return the request id.
	 * @throws IOException if command failed to send
	 */
	public int sendCommand(String command) throws IOException {
		if (client.isClosed() || !client.isConnected()) {
			throw new IllegalStateException("Tried to send a command to a server before connecting to it!");
		}
		
		Packet p = new Packet(Packet.TYPE_COMMAND, command.getBytes());
		p.send();
		return p.getRequestID();
	}
	
	/**
	 * Parses an incoming packet.
	 * @return the parsed packet.
	 * @throws IOException if the packet failed to parse
	 */
	public Packet parsePacket() throws IOException {
		Packet p = new Packet();
		byte[] in = new byte[4096];
		ByteBuffer buf = ByteBuffer.wrap(in);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		int bytesRead = 0;
		while (bytesRead < 12) {
			buf.position(bytesRead);
			bytesRead += client.getInputStream().read(in, bytesRead, buf.remaining());
		}
		buf.rewind();
		p.length = buf.getInt();
		p.requestId = buf.getInt();
		p.type = buf.getInt();
		p.payload = new byte[p.length - 10];
		
		buf.mark();
		while (bytesRead - 12 < p.payload.length + 2) {
			buf.position(bytesRead);
			bytesRead += client.getInputStream().read(in, bytesRead, buf.remaining());
		}
		buf.reset();
		buf.get(p.payload);
		return p;
	}
	
	public class Packet {
		public static final int MAX_PACKET_SIZE = 1460;
		public static final int TYPE_LOGIN = 3, TYPE_COMMAND = 2, TYPE_CMD_RESPONSE = 0;
		
		private int length, requestId, type;
		private byte[] payload;
		
		private Packet() {}
		
		private Packet(int type, byte[] payload) {
			length = 10 + payload.length;
			this.requestId = UUID.randomUUID().hashCode();
			this.type = type;
			this.payload = payload;
			if (length + 4 > MAX_PACKET_SIZE) {
				Logger.getLogger("net.ddns.jsonet.rcon").warning("Packet #"+requestId+" size exceeds maximum packet size");
			}
		}
		
		private void send() throws IOException {
			byte[] out = new byte[length + 4];
			
			ByteBuffer buf = ByteBuffer.wrap(out);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			buf.putInt(length).putInt(requestId).putInt(type).put(payload).putShort((short)0);
			
			client.getOutputStream().write(out);
		}
		
		public int getLength() {
			return length;
		}
		
		public int getRequestID() {
			return requestId;
		}
		
		public int getType() {
			return type;
		}
		
		public byte[] getRawData() {
			return payload;
		}
	}
}
