# LobbyGameServer

![](https://github.com/Rolleander/GameServerControl/actions/workflows/gradle.yml/badge.svg)

Java network library that helps creating complex server and client logic with a clean design and lots of features out of the box: 
- Simple classes are used as package data, no parsing or formatting into messages needed
- "Sites" are classes that implement a part of the client/server behavior and can be added and removed to a running endpoint
- A server hosts multiple lobbies in parallel 
- Clients can create or join existing lobbies
- Player-sessions are recovered after a client disconnects and wants to reconnect 
- CLI utilties for controlling the server remotely
- Uses the fast [KryoNet](https://github.com/EsotericSoftware/kryonet "KryoNet") network library with support for TCP and UDP

## Table of contents

- [Installation](#installation)
- [Basic Client/Server Setup](#basic-clientserver-setup)
    * [Network Classes](#network-classes)
    * [Gameclient](#gameclient)
    * [Gamserver](#gameserver)
    * [Minimal setup example](#minimal-setup-example)
- [Lobby Network Setup](#lobby-network-setup)
    * [Lobbyclient](#lobbyclient)
    * [Lobbyserver](#lobbyserver)
- [Further Use Cases](#further-use-cases)  
    * [Lobby handling](#lobby-handling)
    * [Reconnect check](#reconnect-check)
    * [Data handling in serversites](#data-handling-in-serversites)
    * [Arrays in network classes](#arrays-in-network-classes)
    * [Custom ports](#custom-ports)
    * [Temporary files](#temporary-files)
    * [Connection restriction](#connection-restriction)
    * [Threading tips](#threading-tips)
    * [Buffer size](#buffer-size)
    * [Bots](#bots)

## Installation

The library is not yet released on a public repository like maven central. To use it in your projects currently you need to checkout the project and publish the library to your local maven repository.
Execute the following command in the project folder:
```
gradlew publishToMavenLocal
```
After that you can add the dependency...

...to your gradle...
```gradle
implementation group: 'com.broll', name: 'networklib', version: '1.0'
```
...or maven project build. 
```xml
<dependency>
   <groupId>com.broll</groupId>
   <artifactId>networklib</artifactId>
   <version>1.0</version>
</dependency>
```
Make sure that that local maven repository is regarded in the build! 

## Basic Client/Server Setup
The GameServer and GameClient classes provide basic networking functionality, upon which the LobbyGameServer and LobbyGameClient implement the higher-level functionality. They can be used in case you dont need lobbies at all and help understanding the general structure and concepts. 
### Network Classes
Communication between the server and client happens through registered network classes, which are serialized using [Kryo](https://github.com/EsotericSoftware/kryo "Kryo"). These classes should be kept simple using only public fields, since they wont contain any logic at all. A good rule is to use primtive data types or other network classes as the field types. To prevent any confusion with regular classes it helps giving them a prefix (like "NT_" or "NET_") and keeping them in a separate package:

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
### Gameclient

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

### Gameserver

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
### Minimal setup example

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

### Lobbyclient
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

### Lobbyserver

Explaining how the lobby server can be used is best done with an example. Imagine we want to develop a server for a Monopoly-like game. 
In this example we want to realize the following requirements:
- The Lobby-owner should be able to set the starting cash for the round
- Every player can select a token which will influence the rendering of the gameboard
- When all players are ready the game should automatically start

Some game-specific data is handled in the lobby and other data on each player-connection. The server works as our single-source-of-truth and network classes should only have the minimal required information the client or server needs. 
Lets start by writing a class for handling the lobby data by implementing the interface ILobbyData:

```java
public class MonopolyLobbyData implements ILobbyData {

    private int startMoney = 500;

    public void setStartMoney(int startMoney) {
        this.startMoney = startMoney;
    }

    @Override
    public Object nt() {
        NT_LobbySettings settings = new NT_LobbySettings();
        settings.startMoney = startMoney;
        return settings;
    }

}
```
ILobbyData requires us to specify the method nt(), which is responsible for assembling the network data that is sent to the client. The network class NT_LobbySettings is a simple container with a single field for the startMoney.
We continue by adding a simple network class for the board tokens:
```java
public enum NT_TokenType {
    RACECAR,SUNGLASSES,YACHT,BOWTIE,JET,HELICOPTER,WRISTWATCH,TOPHAT
}
```
Now we can implement the player-data that knows the current token and ready-state for each player:
```java
public class MonopolyPlayerData implements ILobbyData {

    private NT_TokenType tokenType;

    private boolean ready;

    public void setTokenType(NT_TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public NT_TokenType getTokenType() {
        return tokenType;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public Object nt() {
        NT_PlayerSettings settings = new NT_PlayerSettings();
        settings.tokenType = tokenType;
        settings.ready = ready;
        return settings;
    }
}

```
The MonopolyLobbyData and MonopolyPlayerData classes can be provided as generic type arguments for the Lobby-Server, which enable type-safe access to them. The network class NT_PlayerSettings has two fields, one for the token type and one for the ready state. 
Lets start a LobbyGameServer that creates a test-lobby and attaches an instance of our data-class:
 ```java
public class LobbyServerApplication {

    public static void main(String[] args) {
        String serverName = "MonopolyServer";
        LobbyGameServer<MonopolyLobbyData, MonopolyPlayerData> server = new LobbyGameServer<>(serverName, new LobbyNetworkRegistry());
        server.open();
        String lobbyName = "TestLobby";
        ServerLobby<MonopolyLobbyData, MonopolyPlayerData> testLobby = server.getLobbyHandler().openLobby(lobbyName);
        MonopolyLobbyData lobbyData = new MonopolyLobbyData();
        lobbyData.setStartMoney(1500);
        testLobby.setData(lobbyData);
        testLobby.setPlayerLimit(NT_TokenType.values().length);
    }
}
 ```
The lobbydata can be simply attached with the method lobby.setData(). Usually we want to instantiate and attach the playerdata as soon as a player joins our lobby.
For that we write a custom IServerLobbyListener:
 ```java
public class MonopolyLobbyListener implements IServerLobbyListener<MonopolyLobbyData, MonopolyPlayerData> {
    @Override
    public void playerJoined(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {
        MonopolyPlayerData playerData = new MonopolyPlayerData();
        player.setData(playerData);
        playerData.assignFreeToken(lobby);
    }

    @Override
    public void playerLeft(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void playerDisconnected(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void playerReconnected(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void lobbyClosed(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby) {

    }
}
 ```
In the playerJoined method we create a new data object and attach it to the new player. The player should automatically receive a token that is not occupied by another player already.
Therefore lets add the method assignFreeToken to our MonopolyPlayerData class:
 ```java
    public void assignFreeToken(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby) {
        List<NT_TokenType> freeTokens = Lists.newArrayList(NT_TokenType.values());
        List<NT_TokenType> takenTokens = lobby.getPlayersData().stream().map(it -> it.tokenType).collect(Collectors.toList());
        freeTokens.removeAll(takenTokens);
        this.tokenType = freeTokens.get(0);
    }
 ```
lobby.getPlayersData() returns a list of all player data, similarly to lobby.getPlayers().stream().map(Player::getData). 
After adding the missing method we apply the MonopolyLobbyListener to our test-lobby:
 ```java
testLobby.setListener(new MonopolyLobbyListener());
 ```
Finally, we can start the LobbyServerApplication and then run the LobbyGameClient from earlier. As a result the client should find the test-lobby and join it. On the serverside the new player will receive an unassigned token. After the setup nothing exciting will happen anymore, so lets start implementing a LobbyServerSite and add our logic. We want a player to be able to change his token and update his ready-state, for that we introduce two new network classes NT_ChangeToken (with the new token) and NT_PlayerReady (with the new ready-value).  
The new site could look like this:
 ```java
public class MonopolyServerSite extends LobbyServerSite<MonopolyLobbyData, MonopolyPlayerData> {

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    public void received(NT_ChangeToken changeToken) {
        NT_TokenType newToken = changeToken.tokenType;
        if (getLobby().getPlayersData().stream().map(MonopolyPlayerData::getTokenType).anyMatch(token -> token == newToken)) {
            //cannot switch to the new token, because another player uses that already
            return;
        }
        getPlayer().getData().setTokenType(newToken);
        getLobby().sendLobbyUpdate();
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    public void received(NT_PlayerReady playerReady) {
        getPlayer().getData().setReady(playerReady.ready);
        getLobby().sendLobbyUpdate();
        if (getLobby().getPlayersData().stream().map(MonopolyPlayerData::isReady).reduce(true, Boolean::logicalAnd)) {
            //all players are ready, lets start the game!
        }
    }
}
 ```
Lets go through the new concepts:
- getLobby().sendLobbyUpdate(): After making changes to lobby- or player-data we want to update the clients in our lobby with the changed values. Calling this method will send a lobby update package to the clients.
- ConnectionRestriction: Restricts the receiver-method to only handle packages from specific clients, in that case only clients in unlocked lobbies. [more about connection restrictions](#connection-restriction)

..to be continued..

## Further Use Cases

### Lobby handling

todo

### Reconnect check

todo

### Data handling in serversites

todo

### Arrays in network classes

todo

### Custom ports

todo

### Temporary files

todo

### Connection restriction

todo

### Threading tips

todo

### Buffer size

todo

### Bots

todo
