import java.io.*;
import java.net.*;
import java.util.*;

public class GitHubService {



    private static final String TOKEN = System.getenv("GITHUB_TOKEN");


    public static int highCount = 0;
    public static int mediumCount = 0;
    public static int lowCount = 0;
    public static int totalUsers = 0;

    private static Set<String> users =
            new HashSet<>();

    static class RepoData {

        String projects;
        String languages;

        RepoData(
                String projects,
                String languages
        ) {

            this.projects = projects;
            this.languages = languages;
        }
    }

    public static String analyzeUser(
            String username,
            boolean saveToFile
    ) {

        try {

            username = username.trim();

            if (users.contains(
                    username.toLowerCase()
            )) {

                return "Duplicate User Skipped";
            }

            users.add(
                    username.toLowerCase()
            );

            URL url =
                    new URL(
                            "https://api.github.com/users/"
                                    + username
                    );

            HttpURLConnection con =
                    (HttpURLConnection)
                            url.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty(
                    "User-Agent",
                    "Java-App"
            );

            con.setRequestProperty(
                    "Authorization",
                    "Bearer " + TOKEN
            );

            int code =
                    con.getResponseCode();

            if (code == 404) {

                totalUsers++;
                lowCount++;

                if (saveToFile) {

                    ExcelWriter.writeData(
                            username,
                            0,
                            0,
                            0,
                            "User Not Found",
                            "User Not Found"
                    );
                }

                return "User Not Found";
            }

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    con.getInputStream()
                            )
                    );

            StringBuilder response =
                    new StringBuilder();

            String line;

            while ((line =
                    in.readLine()) != null) {

                response.append(line);
            }

            in.close();

            String data =
                    response.toString();

            int followers =
                    extractValue(
                            data,
                            "followers"
                    );

            int repos =
                    extractValue(
                            data,
                            "public_repos"
                    );

            int score =
                    calculateScore(
                            followers,
                            repos
                    );

            totalUsers++;

            if (score > 70) {

                highCount++;
            }

            else if (score >= 45) {

                mediumCount++;
            }

            else {

                lowCount++;
            }

            RepoData repoData =
                    getRepoData(username);

            if (saveToFile) {

                ExcelWriter.writeData(
                        username,
                        followers,
                        repos,
                        score,
                        repoData.projects,
                        repoData.languages
                );
            }

            return
                    "Username : " + username +
                            "\nFollowers : " + followers +
                            "\nRepositories : " + repos +
                            "\nScore : " + score + "/100";
        }

        catch (Exception e) {

            e.printStackTrace();

            return "Error Fetching Data";
        }
    }

    public static String analyzeUsersFromFile(
            String filePath
    ) {

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(filePath)
                    );

            String username;

            int processed = 0;

            StringBuilder notFoundUsers =
                    new StringBuilder();

            while ((username =
                    reader.readLine()) != null) {

                username =
                        username.trim();

                if (!username.isEmpty()) {

                    String result =
                            analyzeUser(
                                    username,
                                    true
                            );

                    if (!result.equals(
                            "Duplicate User Skipped"
                    )) {

                        processed++;
                    }

                    if (result.equals(
                            "User Not Found"
                    )) {

                        notFoundUsers
                                .append(username)
                                .append("\n");
                    }
                }
            }

            reader.close();

            String finalResult =
                    "Bulk Analysis Completed\n\n" +

                            "Processed Users : "
                            + processed +

                            "\n\nUser Not Found IDs :\n";

            if (notFoundUsers.length() == 0) {

                finalResult += "None";
            }

            else {

                finalResult +=
                        notFoundUsers.toString();
            }

            return finalResult;
        }

        catch (Exception e) {

            e.printStackTrace();

            return "File Error";
        }
    }

    public static RepoData getRepoData(
            String username
    ) {

        StringBuilder projects =
                new StringBuilder();

        Set<String> languages =
                new HashSet<>();

        try {

            URL url =
                    new URL(
                            "https://api.github.com/users/"
                                    + username +
                                    "/repos"
                    );

            HttpURLConnection con =
                    (HttpURLConnection)
                            url.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty(
                    "User-Agent",
                    "Java-App"
            );

            con.setRequestProperty(
                    "Authorization",
                    "Bearer " + TOKEN
            );

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    con.getInputStream()
                            )
                    );

            StringBuilder response =
                    new StringBuilder();

            String line;

            while ((line =
                    in.readLine()) != null) {

                response.append(line);
            }

            in.close();

            String[] repos =
                    response.toString()
                            .split("\\},\\{");

            for (String repo : repos) {

                String name =
                        extractString(
                                repo,
                                "name"
                        );

                if (!name.equals(
                        "Not Available"
                )) {

                    if (!projects.toString()
                            .contains(name)) {

                        projects.append(name)
                                .append(" | ");
                    }
                }

                String lang =
                        extractString(
                                repo,
                                "language"
                        );

                if (!lang.equals("null")
                        &&
                        !lang.equals(
                                "Not Available"
                        )) {

                    languages.add(lang);
                }
            }
        }

        catch (Exception e) {

            return new RepoData(
                    "No Projects",
                    "No Languages"
            );
        }

        return new RepoData(

                projects.length() == 0
                        ? "No Projects"
                        : projects.toString(),

                languages.isEmpty()
                        ? "No Languages"
                        : String.join(
                        " | ",
                        languages
                )
        );
    }

    public static int extractValue(
            String json,
            String key
    ) {

        try {

            String search =
                    "\"" + key + "\":";

            int index =
                    json.indexOf(search);

            if (index == -1) {

                return 0;
            }

            int start =
                    index + search.length();

            int end =
                    json.indexOf(",", start);

            return Integer.parseInt(
                    json.substring(start, end)
            );
        }

        catch (Exception e) {

            return 0;
        }
    }

    public static String extractString(
            String json,
            String key
    ) {

        try {

            int index =
                    json.indexOf(
                            "\"" + key + "\":"
                    );

            if (index == -1) {

                return "Not Available";
            }

            int start =
                    json.indexOf(
                            "\"",
                            index + key.length() + 3
                    ) + 1;

            int end =
                    json.indexOf("\"", start);

            return json.substring(
                    start,
                    end
            );
        }

        catch (Exception e) {

            return "Not Available";
        }
    }

    public static int calculateScore(
            int followers,
            int repos
    ) {

        if (repos == 0) {

            return 0;
        }

        int score = 0;

        score += Math.min(
                followers / 10,
                30
        );

        score += Math.min(
                repos * 2,
                40
        );

        score +=
                (repos > 10 ? 30 : 10);

        return score;
    }
}