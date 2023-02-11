import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
public class WebLog {
	public static final String BLACKHOLE = "sbl.spamhaus.org";
	public static boolean isSpammer(String ip) {
		try {
			InetAddress address = InetAddress.getByName(ip);
			byte[] quad = address.getAddress();
			String query = BLACKHOLE;
			for (byte octet : quad) {
				int unsignedByte = octet < 0 ? octet + 256 : octet;
				query = unsignedByte + "." + query;
			}
			InetAddress.getByName(query);
			return true;
		} catch (UnknownHostException e) {
			return false;
		}
	}
	public static boolean isLocalIP(String ip) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            return inet.isSiteLocalAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	public static void main(String[] args) {
		ArrayList<String> spamIPs = new ArrayList<String>();
		Map<String, Integer> ipCount = new HashMap<>();
		// specify the folder path
        String folderPath = "weblogs";
        File folder = new File(folderPath);

        // check if the folder exists and it's a directory
        if (folder.exists() && folder.isDirectory()) {
            // get all the files in the folder
            File[] files = folder.listFiles();
            for (File file : files) {
                // check if the file is a regular file
                if (file.isFile()) {
                    // print the file name
                    System.out.println(file.getName());
                    try (FileInputStream fin = new FileInputStream(file);
            				Reader in = new InputStreamReader(fin);
            				BufferedReader bin = new BufferedReader(in);) {
            			for (String entry = bin.readLine();
            					entry != null;
            					entry = bin.readLine()) {
            				// separate out the IP address
            				int index = entry.indexOf(' ');
            				String ip = entry.substring(0, index);
            				if(!isLocalIP(ip)) {
            					if(isSpammer(ip)) {
            						spamIPs.add(ip);
            					}
            				}
            				// increment the count for this IP
                            int count = ipCount.getOrDefault(ip, 0);
                            ipCount.put(ip, count + 1);
            				String theRest = entry.substring(index);
            				// Ask DNS for the hostname and print it out
            				try {
            					InetAddress address = InetAddress.getByName(ip);
            					System.out.println(address.getHostName() + theRest);
            				} catch (UnknownHostException ex) {
            					System.err.println(entry);
            				}
            			}
            		} catch (IOException ex) {
            			System.out.println("Exception: " + ex);
            		}
                }
            }
        } else {
            System.out.println("Error: The specified folder does not exist or is not a directory.");
        }
        String filePath = "spamips.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : spamIPs) {
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Spam IPs written to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Sorting the map in descending order by the number of accesses
        Map<String, Integer> sortedIpCount = ipCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println("Number of access of each IP:");
        // print out the count for each IP address
        for (Map.Entry<String, Integer> entry : sortedIpCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
	}
}
