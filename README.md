# java-rcon
Java implementation of the RCON protocol
## About
The RCON protocol is mainly used by Valve games written with the source engine. The protocol is used to **R**emotely **CON**trol a server. In this particular implementation, after a connection is established to a desired server running the RCON protocol, all inputs entered in the console are sent to the server in plain text and executed as a command; The output is sent back in plain text to the user.
## Usage
### Linux
`./java-rcon [-H <hostname>] [-P <password>] [-p <port>]`
### Windows
`java-rcon.bat [-H <hostname>] [-P <password>] [-p <port>]`
