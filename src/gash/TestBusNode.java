package gash;

import java.util.ArrayList;

import gash.messaging.Message;
//import gash.messaging.transports.Line.LineNode;

public class TestBusNode {
	public static void main(String[] args) throws InterruptedException {
		// create 50 messages
		ArrayList<Message> messages = new ArrayList<Message>();
		for(int i = 0; i <= 50; i++) {
			Message m = new Message(i);
			m.setMessage("message " + i);
			messages.add(m);
		}
		Bus bus = new Bus(10);
		
		//Bus bus = new Bus(10);
		bus.broadcastMessage(3, "hello everyone");
		Thread.sleep(10000);

		bus.showReport();
    }
}
