# LobbyGameServer

Java network library that helps creating complex server and client logic with a clean design and lots of features out of the box: 
- Simple classes are used as package data, no parsing or formatting into messages needed
- "Sites" are classes that implement a part of the client/server behavior and can be added and removed to a running endpoint
- A server hosts multiple lobbies in parallel 
- Clients can create or join existing lobbies
- Player-sessions are recovered after a client disconnects and wants to reconnect 
- CLI utilties for controlling the server remotely
- Uses the fast [KryoNet](http://https://github.com/EsotericSoftware/kryonet "KryoNet") network library with support for TCP and UDP


## Basic Client/Server Setup
The GameServer and GameClient classes provide basic networking functionality, upon which the LobbyGameServer and LobbyGameClient implement the higher-level functionality. They can be used in case you dont need lobbies at all and help understanding the general structure and concepts. 
###### Network Classes
Communication between the server and client happens through registered network classes, which are serialized using [Kryo](http://https://github.com/EsotericSoftware/kryo "Kryo"). These classes should be kept simple using only public fields, since they wont contain any logic at all. A good rule is to use primtive data types or other network classes as the field types. To prevent any confusion with regular classes it helps giving them a prefix (like "NT_" or "NET_") and keeping them in a separate package:

```java
public class NT_TestPackage{
	public String msg;
}
```
The server and client need to register the same classes in the same order, therefore we implement the IRegisterNetwork interface in a central spot once:
```java
public class BasicNetworkRegistry implements IRegisterNetwork {
    @Override
    public void register(NetworkRegister register) {
        register.registerNetworkType(BasicExample.class);
    }
}
```
If we keep network classes in a separate package we can keep the network registry code very simple:
```java
    register.registerNetworkPackage("com.mynetpackage");
```
###### Gameclient

The GameClient can be instantiated using the previously created network registry. The client has the usual methods for connecting to a server and shutting down.
```java
public class BasicClientApplication {
    public static void main(String[] args) {
        GameClient client = new GameClient(new BasicNetworkRegistry());
        client.connect("localhost");
        client.shutdown();
    }
}
```
Now we can send and receive our different network packages. Usually one would check with a big switch case what package was received and react accordingly. However, imagine we have a lot of different package types and our client has multiple states in which it only wants to receive specific packages, or react differently alltogether. The code would soon become a unmanagable mess!    

Thats exactly the scenario where this library wants to make our live easy, writing networking code doesnt have to be a pain. What if the network library handles all that for us? This is how: we write "Sites", classes that are interested in a few of our network packages and manage their own state. These sites then can be added and removed to the endpoint at runtime. Instead of having to filter through the network listener and search for a specific package, we simply define in a Site which packages we are intersted in and the library will handle the routing. Sites keep the client/server logic sleek and modular, since the networking context will be injected automatically. 

A very simple ClientSite implementation that prints the content of a received NT_TestPackage:
```java
public class BasicClientSite extends ClientSite {

    @PackageReceiver
    public void received(NT_TestPackage testPackage){
        System.out.println("The server told me: "+testPackage.msg);
    }

}
```
The PackageReceiver annotation marks the method as a receiver. Receiver methods must have exactly one argument with the type of the network class they want to handle. Every ClientSite can call the method getClient(), which provides access to the client for sending packages.

Extending the previous example, lets add the new site to the client:
```java
GameClient client = new GameClient(new BasicNetworkRegistry());
client.register(new BasicClientSite());
client.connect("localhost");
client.shutdown();
```

###### Gameserver

The GameServer is similar in usage to the GameClient, that it requires a network registry:
```java
public class BasicServerApplication {
    public static void main(String[] args) {
        GameServer server = new GameServer(new BasicNetworkRegistry());
        server.open();
    }
}
```
Sites show their full potential on the server-side, since they always have the correct connection-context injected. The following class implements a simple server site that sends a NT_TestPackage message to any newly connected client:
```java
public class BasicServerSite extends ServerSite {

    @Override
    public void onConnect(NetworkConnection connection) {
        super.onConnect(connection);
        NT_TestPackage testPackage = new NT_TestPackage();
        testPackage.msg = "Hello Client!";
        getConnection().sendTCP(testPackage);
    }

}
```
getConnection() can be used in all receiver methods of a server site to access the current client connection from which the message originated.

In the same way we added sites to the client we add the new site to our example server:
```java
GameServer server = new GameServer(new BasicNetworkRegistry());
server.open();
server.register(new BasicServerSite());
```
###### Minimal setup example

The code below is a self-contained example of a minimal setup for playing around:

```java
public class BasicExample {

    public static void main(String[] args) {
        GameClient client = new GameClient(BasicExample::registerNetwork);
        GameServer server = new GameServer(BasicExample::registerNetwork);
        client.register(new BasicClientSite());
        server.register(new BasicServerSite());
        server.open();
        client.connect("localhost");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.shutdown();
        server.shutdown();
    }

    private static class BasicServerSite extends ServerSite{
        @Override
        public void onConnect(NetworkConnection connection) {
            super.onConnect(connection);
            NT_TestPackage testPackage = new NT_TestPackage();
            testPackage.msg = "Hello Client!";
            getConnection().sendTCP(testPackage);
        }
    }

    private static class BasicClientSite extends ClientSite {
        @PackageReceiver
        public void received(NT_TestPackage testPackage){
            System.out.println("The server told me: "+testPackage.msg);
        }
    }

    private static void registerNetwork(NetworkRegister register){
        register.registerNetworkType(NT_TestPackage.class);
    }

    private static class NT_TestPackage {
        public String msg;
    }

}
```
It should be clear now how to setup a basic server & client and what Sites are. 

## Lobby Network Setup

A huge part of this library is its lobby handling. Lobbies are rooms on the server which contain players and eventually run a game instance. The usual lobby-flow looks like this: 
1. A new client connects to the server
1. The client lists the open lobbies on the server
1.  After entering a player-name, the client joins one of the lobbies or creates a new one
1. When a lobby is full or all its players are ready the game starts
1. The lobby is locked, no new players can join 
1. After the game is finished the lobby is disbanded, its players can join a different lobby or create a new one 

Since lots of games use that lobby behavior, why start from scratch? 
The LobbyGaneServer and LobbyGameClient classes provide most of that out of the box and are implemented themselves with Sites. With them we can easily organize players in lobbies and create a isolated context for our gamelogic. 

###### Lobby Client
The LobbyGameClient has additional methods to list, create and connect to lobbies. All async methods return a CompletableFuture with the result. Below is an example listing the lobbies of a server and connecting to the first one found.
```java
String playerName = "Peter";
LobbyGameClient client = new LobbyGameClient(new LobbyNetworkRegistry());
client.listLobbies("localhost").thenAccept(lobbies -> {
            System.out.println("# of listed lobbies: " + lobbies.getLobbies().size());
            GameLobby lobby = lobbies.getLobbies().get(0);
            client.joinLobby(lobby, playerName);
}).exceptionally(exc -> {
            System.out.println("Failed to list lobbies: " + exc.getMessage());
            return null;
});
```
Site classes extend from LobbyClientSite instead of ClientSite. It comes with new methods to access the lobby-context:
- getLobby() : returns the currently joined lobby, or null 
- getPlayer(): returns our player of the lobby (Same as getLobby().getMyPlayer()) 

###### Lobby Server

## Further Use Cases

###### Lobby Handling

###### Reconnect Check

###### Data Handling in Serversites

###### Arrays in network classes

###### Custom ports

###### Temporary Files

######  Connection Restriction

###### Threading Tips

###### Buffer Size
