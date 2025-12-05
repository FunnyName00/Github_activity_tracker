
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            List<Event> content = fetchUserEvents("FunnyName00");

            for (int i = 0; i < content.size(); i++){
                Event event = content.get(i);
                System.out.println(event.repo + ": " + event.type + ", " + event.created_at);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Event> fetchUserEvents(String user){
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

            return parseEvents(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int index = json.indexOf(pattern);
        if (index == -1) return null;

        index += pattern.length();

        while (json.charAt(index) == ' ' || json.charAt(index) == '\"') index++;

        StringBuilder sb = new StringBuilder();
        char c = json.charAt(index);
        boolean quoted = c == '"';

        if (quoted) index++;

        while (index < json.length()) {
            c = json.charAt(index);
            if (quoted && c == '"') break;
            if (!quoted && (c == ',' || c == '}' || c == ']')) break;
            sb.append(c);
            index++;
        }

        return sb.toString().trim();
    }

    private static String extractObject(String json, String key) {
        String pattern = "\"" + key + "\":";
        int index = json.indexOf(pattern);
        if (index == -1) return null;

        // on avance jusqu’au '{'
        index = json.indexOf("{", index);
        if (index == -1) return null;

        int depth = 0;
        int start = index;

        for (; index < json.length(); index++) {
            char c = json.charAt(index);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (depth == 0) break;
        }

        return json.substring(start, index + 1);
    }

    private static Event parseEvent(String json) {
        Event event = new Event();

        event.id = extractValue(json, "id");
        event.type = extractValue(json, "type");
        event.created_at = extractValue(json, "created_at");

        String repoObj = extractObject(json, "repo");
        if (repoObj != null) {
            Repo repo = new Repo();
            repo.name = extractValue(repoObj, "name");
            event.repo = repo;
        }

        return event;
    }

    public static List<Event> parseEvents(String jsonArray) {
        List<Event> events = new ArrayList<>();

        // enlever [ au début et ] à la fin
        String body = jsonArray.trim();
        if (body.startsWith("[")) body = body.substring(1);
        if (body.endsWith("]")) body = body.substring(0, body.length() - 1);

        // séparer en objets
        String[] objects = body.split("\\},\\{");

        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];

            // remettre les accolades perdues par le split
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            events.add(parseEvent(obj));
        }

        return events;
    }



}
