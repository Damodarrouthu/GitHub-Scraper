import java.io.*;
import java.net.*;

public class GitHubService {

    public static String analyzeUser(String username) {
        try {
            URL url = new URL("https://api.github.com/users/" + username);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int respond_code= con.getResponseCode();
            if(respond_code==404)
            {
                return "User Not Found";
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String data = response.toString();

            int followers = extractValue(data, "followers");
            int repos = extractValue(data, "public_repos");

            int score = calculateScore(followers, repos);

            return "Followers: " + followers +
                    "\nRepos: " + repos +
                    "\nFinal Score: " + score + "/100 ";

        } catch (Exception e) {
            return "oops!InterNet Connections are UnAvailable OR Server Issue!";
        }
    }


    public static int extractValue(String json, String key) {
        try {
            int index = json.indexOf("\"" + key + "\":");
            int start = index + key.length() + 3;
            int end = json.indexOf(",", start);
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }


    public static int calculateScore(int followers, int repos) {
        int score = 0;

        score += Math.min(followers /10, 10);
        score += Math.min(repos * 5, 10);


        return score;
    }
}