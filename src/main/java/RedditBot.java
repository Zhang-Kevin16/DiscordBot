import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RedditBot {
    //Access token
    private String tokenId;
    //HTTP Authorization Header
    private static String HEADER1 = "Authorization";
    public RedditBot(String clientID) throws IOException {
        //Reddit API can connect with just clientID and no password.
        String auth = clientID.concat(":");
        HttpURLConnection connection = (HttpURLConnection) new URL("https://www.reddit.com/api/v1/access_token").openConnection();
        //Convert our username/password to Base64
        connection.setRequestProperty(HEADER1, "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "DegenerateBot/0.1 by Fak");
        //Body of POST Request.
        byte[] postData = "grant_type=https://oauth.reddit.com/grants/installed_client&device_id=DO_NOT_TRACK_THIS_DEVICE".getBytes(StandardCharsets.UTF_8);
        int dataLength = postData.length;
        connection.setRequestProperty("Content-Length", Integer.toString(dataLength));
        connection.setDoOutput(true);
        //Send body of request to server
        connection.getOutputStream().write(postData);
        //Getting response which is a JSONObject that includes the access token
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder finalToken = new StringBuilder("");
        while ((line = input.readLine()) != null) {
            finalToken.append(line);
        }
        connection.disconnect();
        //We only care about the access token tag.
        JsonObject token = JsonParser.parseString(finalToken.toString()).getAsJsonObject();
        tokenId = token.get("access_token").getAsString();
        connection.disconnect();
    }

    /**
     * Gets the URL of a random top ten post from the MultiReddit that contains Animemes and anime_irl
     * @return the URL of the post
     * @throws IOException if we fail to read from the URL.
     */
    public String hot() throws IOException {
        //API Request
        HttpURLConnection connection = (HttpURLConnection) new URL("https://oauth.reddit.com/r/Animemes+anime_irl/hot").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty(HEADER1, "bearer " + tokenId);;
        connection.setRequestProperty("User-Agent", "DegenerateBot/0.1 by Fak");
        //Right now we only get one of the top 10 posts. This is completely arbitrary.
        int place = (int) (Math.random() * 10.0);
        //The request gives us a JSONObject.
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
