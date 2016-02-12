package com.broll.gameserver.test;

import org.junit.Test;

import com.broll.gameserver.GSCServer;
import com.broll.gameserver.ServerControl;
import com.broll.gameserver.player.Player;
import com.broll.gameserver.sites.RequestFilter;
import com.broll.gameserver.sites.ServerSite;

public class ServerSetupTest {

	@Test
	public void test() {

		GSCServer server = new GSCServer();
		server.register(new TestSite());
		server.start();
	}

	private class TestProtocol {
		String text;
	}

	@RequestFilter(protocol = TestProtocol.class)
	private class TestSite implements ServerSite {
		@Override
		public void receive(ServerControl server, Object message, Player from) {
			TestProtocol msg=(TestProtocol)message;
			System.out.println("Received: "+msg.text);
			
		}
	}
}
