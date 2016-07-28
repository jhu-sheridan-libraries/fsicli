package fsi.fsicli;

import java.io.IOException;
import java.net.URL;

public class Tool {

	private static void report_usage() {
		System.err.println("Usage server command arg...");
		System.exit(1);
	}

	// "http://your.fsi-server.com/fsi/";

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			report_usage();
		}

		String fsi_url = args[0];
		String cmd = args[1];

		FsiClient client = new FsiClient(new URL(fsi_url));

		switch (cmd) {
		case "login": {
			String user = args[2];
			String pass = args[3];

			String ses = client.login(user, pass);
			
			System.out.println("Session: " + ses);
		}
		
		break;

		case "list": {
			String ses = args[2];
			String path = args[3];
			
			client.setSession(ses);
			client.list(path).forEach(i -> System.out.println(i));
		}
		
		case "upload": {
			String ses = args[2];
			String server_path = args[3];
			String file_path = args[4];
			
			client.setSession(ses);
			
			client.upload(server_path, file_path);
		}
		
		break;

		default:
			System.err.println("Unknown command: " + cmd);
			report_usage();
			break;
		}
	}
}
