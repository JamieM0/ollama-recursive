import org.json.JSONObject;
import org.json.JSONArray;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class OllamaAPI {
    private static final String OPENAI_API_URL = "http://localhost:11434/v1/chat/completions";
    private final String apiKey;
    private final HttpClient client;
    private String model;
    private double temperature;
    private String systemPrompt;
    private String query;

    public OllamaAPI(String apiKey, String systemPrompt, String query) {
        this.apiKey = apiKey;
        this.systemPrompt = systemPrompt;
        this.query = query;
        this.client = HttpClient.newHttpClient();
        this.model = "llama3.1";
        this.temperature = 0.7;
    }

    public String makeRequest() throws Exception {
        String requestBody = createRequestBody();

        // Print the request body for debugging
        //System.out.println("Request Body:");
        //System.out.println(new JSONObject(requestBody).toString(2));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API call failed with status " +
                    response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String createRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        requestBody.put("temperature", this.temperature);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", query));
        requestBody.put("messages", messages);

//        JSONArray functions = new JSONArray();
//        JSONObject function = new JSONObject()
//                .put("name", "get_structured_response")
//                .put("description", "Get a structured response following the schema")
//                .put("parameters", structure.toJsonSchema()); // This now returns direct schema
//        functions.put(function);

//        requestBody.put("functions", functions);
//        requestBody.put("function_call", new JSONObject()
//                .put("name", "get_structured_response"));

        return requestBody.toString();
    }

    //Setters for query, systemPrompt, model, and temperature
    public void setQuery(String query) {
        this.query = query;
    }
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setTemperature(double temperature) {
        if(temperature>=0 && temperature<=1) {
            this.temperature = temperature;
        }
        else{
            throw new IllegalArgumentException("Temperature must be between 0 and 1");
        }
    }
}
