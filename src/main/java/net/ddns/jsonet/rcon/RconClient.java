package net.ddns.jsonet.rcon;

public class RconClient {
	public String getGreeting() {
		return "Hello world.";
	}

	public static void main(String[] args) {
		System.out.println(new RconClient().getGreeting());
	}
}
