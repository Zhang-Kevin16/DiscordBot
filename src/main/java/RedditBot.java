import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class  RedditBot {
    private String tokenId;
    private static String HEADER1 = "Authorization";
    public RedditBot(String clientID) throws IOException {
        String auth = clientID.concat(":");
        HttpURLConnection connection = (HttpURLConnection) new URL("https://www.reddit.com/api/v1/access_token").openConnection();
        connection.setRequestProperty(HEADER1, "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "DegenerateBot/0.1 by Fak");
        byte[] postData = "grant_type=https://oauth.reddit.com/grants/installed_client&device_id=DO_NOT_TRACK_THIS_DEVICE".getBytes(StandardCharsets.UTF_8);
        int dataLength = postData.length;
        connection.setRequestProperty("Content-Length", Integer.toString(dataLength));
        connection.setDoOutput(true);
        connection.getOutputStream().write(postData);
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder finalToken = new StringBuilder("");
        while ((line = input.readLine()) != null) {
            finalToken.append(line);
        }
        System.out.println(finalToken);
        connection.disconnect();
        JsonObject token = JsonParser.parseString(finalToken.toString()).getAsJsonObject();
        tokenId = token.get("access_token").getAsString();
        connection.disconnect();
    }

    public String hot() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://oauth.reddit.com/r/Animemes+anime_irl/hot").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty(HEADER1, "bearer " + tokenId);;
        connection.setRequestProperty("User-Agent", "DegenerateBot/0.1 by Fak");
        int place = (int) (Math.random() * 10.0);
        BufferedReader jsonIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder("");
        String line = "";
        while ((line = jsonIn.readLine()) != null) {
            content.append(line);
        }
        jsonIn.close();
        connection.disconnect();
        return JsonParser.parseString(content.toString()).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(place).getAsJsonObject().get("data").getAsJsonObject().get("url").getAsString();
    }
}
