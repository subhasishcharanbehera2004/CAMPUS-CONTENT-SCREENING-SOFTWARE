package com.college.server;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
public class YouTubeCategoryFinder {

    // Replace with your actual Google API Key
    private static final String API_KEY = "AIzaSyCpW_E7TQ3FQPiAM2gG1APlMpYLc_ZhdPk";
	
	
    // Mapping of YouTube Category IDs to Names
    private static final Map<String, String> CATEGORIES = Map.of(
        "1", "Film & Animation",
        "10", "Music",
        "20", "Gaming",
        "24", "Entertainment",
        "27", "Education",
        "28", "Science & Technology"
    );
	private String videoId;
	public YouTubeCategoryFinder(String videoId)
	{
	this.videoId=videoId;
	}
	
	private String channelName = "";
	public String getChannelName() {
    return channelName;
	}

    /*public static void main(String[] args) {
    //    String videoId = "GriXfu4tL30"; // Example Video ID
       
        try {
            String category = getYouTubeCategory(videoId);
            //System.out.println("Video ID: " + videoId);
            //System.out.println("Detected Category: " + category);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }*/

    public String getYouTubeCategory() throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/videos" +"?part=snippet&id="
                     + this.videoId + "&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error: " + response.body());
        }

        // Simple manual parsing to find "categoryId" without a heavy JSON library
        String body = response.body();
		String category = "Category Not Found";
		
        if (body.contains("\"categoryId\": \"")) {
            int start = body.indexOf("\"categoryId\": \"") + 15;
            int end = body.indexOf("\"", start);
            String id = body.substring(start, end);
            category= CATEGORIES.getOrDefault(id, "Unknown Category");
		}
			// Extract channel name
		if (body.contains("\"channelTitle\": \"")) {
				int start = body.indexOf("\"channelTitle\": \"") + 17;
				int end = body.indexOf("\"", start);
				this.channelName = body.substring(start, end);
			}
        

        return category;
    }
	

	public static boolean isAllowed(String category, String channelName) {
		// 1. Quick check for categories
		Set<String> allowedCategories = Set.of("education", "devotional", "music", "science & technology");
		if (category != null && allowedCategories.contains(category.toLowerCase())) {
			return true;
		}

		// 2. Check channel name keywords
		if (channelName == null || channelName.isEmpty()) {
			return false;
		}

		String ch = channelName.toLowerCase();
		String[] keywords = {
			"iit", "nptel", "gate", "academy", "lecture", "course", "university", 
			"nit", "iiit", "swayam", "ignou", "ugc", "gate smashers", "college", 
			"institute", "cyber", "security", "programming", "coding", 
			"computer science", "data science", "machine learning", "deep learning", 
			"artificial intelligence", "ai", "tutorial", "training", "education", 
			"learning", "class", "faculty", "professor", "stanford", "mit", 
			"harvard", "coursera", "edx", "khan", "byju", "unacademy", "physics wallah"
		};

		return Arrays.stream(keywords).anyMatch(ch::contains);
	}


}
