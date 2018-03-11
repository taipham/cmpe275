package gash.router.app;

import gash.router.client.CommConnection;
import gash.router.client.CommListener;
import gash.router.client.MessageClient;
import routing.Pipe.Route;
import org.apache.commons.cli.*;

public class StreamingApp implements CommListener{
	private MessageClient mc;

	public StreamingApp(MessageClient mc) {
		init(mc);
	}

	private void init(MessageClient mc) {
		this.mc = mc;
		this.mc.addListener(this);
	}

	private void ping(int N) {
		// test round-trip overhead (note overhead for initial connection)
		final int maxN = 10;
		long[] dt = new long[N];
		long st = System.currentTimeMillis(), ft = 0;
		for (int n = 0; n < N; n++) {
			mc.ping();
			ft = System.currentTimeMillis();
			dt[n] = ft - st;
			st = ft;
		}

		System.out.println("Round-trip ping times (msec)");
		for (int n = 0; n < N; n++)
			System.out.print(dt[n] + " ");
		System.out.println("");

		// send a message
		st = System.currentTimeMillis();
		ft = 0;
		for (int n = 0; n < N; n++) {
			mc.postMessage("hello world " + n);
			ft = System.currentTimeMillis();
			dt[n] = ft - st;
			st = ft;
		}

		System.out.println("Round-trip post times (msec)");
		for (int n = 0; n < N; n++)
			System.out.print(dt[n] + " ");
		System.out.println("");
	}

	private void testtSend(String filename) {
		System.out.println("Start sending");
		mc.sendMessage(filename);
		System.out.println("End sending");
	}
	
	@Override
	public String getListenerID() {
		return "MyApp";
	}

	@Override
	public void onMessage(Route msg) {
		System.out.println("---> " + msg);
	}

	/**
	 * sample application (client) use of our messaging service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = 4567;

		// Command line argument parsing
		Options options = new Options();

        Option oWrite = new Option("w", "write", true, "input file path to write");
        oWrite.setRequired(false);
        options.addOption(oWrite);

        //Option oPing = new Option("p", "ping", true, "ping the sever");
        //oPing.setRequired(false);
        //options.addOption(oPing);

        Option oHost = new Option("h", "host", true, "server host");
        oHost.setRequired(true);
        options.addOption(oHost);
        
        Option oPort = new Option("p", "port", true, "server port");
        oPort.setRequired(true);
        options.addOption(oPort);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }
        
        
        // Main stuff
		try {
			host = cmd.getOptionValue("h");
			port = Integer.parseInt(cmd.getOptionValue("p"));
			
			MessageClient mc = new MessageClient(host, port);
			StreamingApp da = new StreamingApp(mc);
			
			// do stuff w/ the connection
			//if (cmd.hasOption("p")) {
			//	System.out.println("Pinginr the server");
			//	da.ping(4000);
			//}
			if (cmd.hasOption("w")) {
				String filePath = cmd.getOptionValue("w");
				da.testtSend(filePath);
			}
			
			System.out.println("\n** exiting in 10 seconds. **");
			System.out.flush();
			Thread.sleep(10 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommConnection.getInstance().release();
		}
	}
}
