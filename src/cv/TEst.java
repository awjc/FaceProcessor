package cv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class TEst {
	public static void main(String[] args) throws IOException {
		//Try to get our IP address from Amazon AWS
		URL aws = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(aws.openStream()));
		String ip = in.readLine();
		System.out.println(" on IP: " + ip + ":" + 0);
	}
}
