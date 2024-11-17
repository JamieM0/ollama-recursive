import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static final int LOOPS = 5; //Number of times that the LLM will be prompted.
    public static String systemPrompt = "You are a friendly AI helper. Assist the user in the best way possible.";
    public static String query = "Give me a list of potential genres that a story could have. These could be for any audience of any demographic.";
    public static String storageLocation = "~/Documents/Ollama Results"; //Change to empty directory where the outputs will be stored.

    public static void main(String[] args) {
        recursivelyPrompt();
    }

    public static void recursivelyPrompt()
    {
        long startTimeMs = System.currentTimeMillis();
        long firstProgressUpdate = 5000;
        long progressUpdates = 2000;
        long nextProgressUpdate = startTimeMs + firstProgressUpdate;

        System.out.println("Running ollama " + LOOPS + " times. This may take some time.");
        for(int i=0; i<LOOPS; i++)
        {
            OllamaAPI client = new OllamaAPI("ollama", systemPrompt, query);
            client.setTemperature(0.7);

            String response = "";
            try{
                response = client.makeRequest();
            }
            catch(Exception e){
                e.printStackTrace();
            }

            //Save to file
            saveFile(storageLocation + "/" + i + ".txt", extractContent(response));
            System.out.println(i+1 + "/" + LOOPS + " completed.");

            long currentTimeMs = System.currentTimeMillis();
            if(currentTimeMs>=nextProgressUpdate && i<LOOPS)
            {
                nextProgressUpdate = currentTimeMs + progressUpdates;
                long timeSpentMs = currentTimeMs - startTimeMs;
                long totalEstTimeMs = (LOOPS * timeSpentMs) / (i+1);
                long timeRemainMs = totalEstTimeMs - timeSpentMs;

                if(timeRemainMs/1000 < 60)
                    System.out.println("Approx. " + timeRemainMs/1000 + " seconds remaining.");
                else
                    System.out.println("Approx. " + (timeRemainMs/1000)/60 + " minutes remaining.");
            }
        }

        System.out.println("Completed.");
    }

    public static void saveFile(String path, String content) {
        try {
            // Create directory if it doesn't exist
            File file = new File(path);
            file.getParentFile().mkdirs(); // This creates parent directories if they don't exist

            // Write to file
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            System.out.println("Successfully wrote to file");
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String extractContent(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray choices = json.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }
}