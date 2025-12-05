
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            ArrayList content = fetchUserEvents("FunnyName00");

            for (int i = 0; i < content.size(); i++){
                System.out.println(content.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> fetchUserEvents(String user){
        try {
            URL url = new URL("https://api.github.com/users/"+user+"/events");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int status = con.getResponseCode();
            System.out.println("Status: " + status);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            ArrayList<String> content = new ArrayList<String>();

            while ((line = in.readLine()) != null) {
                content.add(line);
            }

            in.close();
            con.disconnect();

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
