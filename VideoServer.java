package com.college.server;

import static spark.Spark.*;
//import com.college.server.AdvancedClassifier;
//import com.college.server.YouTubeCategoryFinder;
//import com.college.server.VideoInfo;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class VideoServer {

    static AdvancedClassifier classifier = new AdvancedClassifier();

    // Cache to avoid repeated API + yt-dlp calls
    static Map<String, String> titleCache = new ConcurrentHashMap<>();
    static Map<String, VideoInfo> categoryCache = new ConcurrentHashMap<>();

    // Thread pool
	// Pool for long-running video streams (Heavy IO)
    static ExecutorService executor = Executors.newFixedThreadPool(10);
	// Pool for quick metadata lookups (Light IO)
	
	// We use a larger pool here because these tasks finish in milliseconds
	static ExecutorService metadataExecutor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {

        port(8080);
        classifier.trainModel();

        System.out.println("🚀 Server running on http://localhost:8080");

        // 🎬 HOME PAGE (Netflix Style)
        get("/", (req, res) -> {
            res.type("text/html");

		return "<html>" +
       "<head>" +
       "<title>SSSIHL Campus YT Streamer</title>" +
       "<style>" +
       "  body, html { height: 100%; margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
       "  body { " +
       "    background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), " +
       "    url('https://images.unsplash.com'); " +
       "    background-size: cover; background-position: center; " +
       "    display: flex; align-items: center; justify-content: center; color: white; " +
       "  }" +
       "  .container { background: rgba(0, 0, 0, 0.6); padding: 50px; border-radius: 20px; box-shadow: 0 8px 32px rgba(0,0,0,0.8); text-align: center; width: 80%; max-width: 500px; }" +
       "  h1 { color: #E50914; font-size: 2.5rem; margin-bottom: 30px; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }" +
       "  input { " +
       "    padding: 15px; width: 100%; border: none; border-radius: 30px; " +
       "    margin-bottom: 20px; font-size: 16px; outline: none; background: #333; color: white; " +
       "  }" +
       "  button { " +
	   "    padding: 15px 40px; border: none; border-radius: 30px; " +
       "    background: linear-gradient(45deg, #E50914, #B20710); " +
       "    color: white; font-size: 18px; font-weight: bold; cursor: pointer; " +
       "    transition: transform 0.2s, background 0.3s; box-shadow: 0 4px 15px rgba(229, 9, 20, 0.4); " +
       "  }" +
       "  button:hover { transform: scale(1.05); background: linear-gradient(45deg, #ff0f1a, #E50914); }" +
       "</style>" +
       "</head>" +
       "<body>" +
       "  <div class='container'>" +
       "    <h1>SSSIHL YT Streamer</h1>" +
       "    <form action='/watch'>" +
       "      <input name='id' placeholder='Paste YouTube URL or Video ID here...' required/>" +
       "      <br>" +
       "      <button type='submit'>Start Streaming</button>" +
       "    </form>" +
       "  </div>" +
       "</body>" +
       "</html>";
        });

        // 🎥 WATCH PAGE
		//new code
		// 🎥 WATCH PAGE (Security Check happens here now)
		get("/watch", (req, res) -> {
			String input = req.queryParams("id");
			String videoId = extractVideoId(input);
			if (videoId == null) return "Invalid YouTube URL";

			String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

			// --- SECURITY CHECK START ---
			String title = getVideoTitle(videoUrl);
			//String ytCategory = categoryCache.getOrDefault(videoId, new YouTubeCategoryFinder(videoId).getYouTubeCategory());
			//categoryCache.put(videoId, ytCategory);
			
			VideoInfo info = categoryCache.get(videoId);

			if (info == null) {
				YouTubeCategoryFinder finder = new YouTubeCategoryFinder(videoId);
				String category = finder.getYouTubeCategory();
				String channel = finder.getChannelName();

				info = new VideoInfo(category, channel);
				categoryCache.put(videoId, info);
			}

			String ytCategory = info.category;
			String channelName = info.channel;
			
			String aiCategory = classifier.predict(title);
			
			System.out.println("🎬 Title: " + title);
            System.out.println("🤖 AI Category: " + aiCategory);
            System.out.println("📺 YT Category: " + ytCategory);
			
			//changes to get owner channel name to allow few channels for this 
			//YouTubeVideoManager manager = new YouTubeVideoManager();
			//String channel_name=manager.getOwnerChannelName(videoId);
			
			boolean allowed = false;
			// 1. PRIMARY CHECK: Try official YouTube Metadata first
			if (ytCategory != null && !ytCategory.isEmpty() && !ytCategory.equalsIgnoreCase("Unknown")) {
				allowed = YouTubeCategoryFinder.isAllowed(ytCategory,channelName);
				
				if (allowed) {
					System.out.println("✅ Allowed by YouTube Metadata: " + ytCategory);
				} else {
					System.out.println("❌ Blocked by YouTube Metadata: " + ytCategory);
				}
			} 
			// 2. EXTREME FALLBACK: Metadata failed/missing, use AI as the final judge
			else {
				System.out.println("⚠️ Metadata missing! Falling back to AI Classifier...");
				allowed = classifier.isAllowed(aiCategory);
				
				if (allowed) {
					System.out.println("🤖 AI Allowed as Fallback: " + aiCategory);
				} else {
					System.out.println("🛑 AI Blocked as Fallback: " + aiCategory);
				}
			}


			if (!allowed) {
				System.out.println("🛑 BLOCKING VIDEO: " + title);
				 res.status(403);
				res.type("text/html; charset=utf-8");
				return "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px; background:#f8f9fa;'>" +
					   "<div style='display:inline-block; padding:40px; background:white; border-radius:10px;'>" +
					   "<h1 style='color:#dc3545;'>🚫 Access Denied</h1>" +
					   "<p>The content: <b>" + title + "</b> is not allowed.</p>" +
					   "<button onclick='window.history.back()'>Go Back</button></div></body></html>";
			}
			// --- SECURITY CHECK END ---

			
				req.session(true); // Create a session if it doesn't exist
				req.session().attribute("authorized_video_id", videoId);
				//for the HTMl Page 
				/*return "<html><body style='background:black; margin:0'>" +
				   "<video width='100%' height='100%' controls autoplay preload='metadata' playsinline>" +
				   "<source src='/stream?id=" + videoId + "' type='video/mp4'>" +
					"</video></body></html>";*/
				
				return "<html>" +
				"<head>" +
				"<meta name='viewport' content='width=device-width, initial-scale=1'>" +
				"<style>" +
				"body{margin:0;background:black;}" +
				"video{width:100vw;height:100vh;background:black;}" +
				"</style>" +
				"</head>" +
				"<body>" +
				"<video controls autoplay muted playsinline preload='metadata' id='player'>" +
				"<source src='/stream?id=" + videoId + "' type='video/mp4'>" +
				"</video>" +

				"<script>" +
				"const v=document.getElementById('player');" +
				"v.addEventListener('click',()=>{v.muted=false;});" +
				"</script>" +

				"</body></html>";

			
				
		});
//new 
        // 🚀 STREAM ENDPOINT
        get("/stream", (req, res) -> {

            String videoId = req.queryParams("id");
			
			String sessionVideoId = req.session().attribute("authorized_video_id");

			// CRITICAL: If the ID in the URL isn't exactly what the Session says is allowed, BLOCK.
			if (sessionVideoId == null || !sessionVideoId.equals(videoId)) {
				res.status(403);
				res.type("text/html; charset=utf-8");
				return "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px; background:#f8f9fa;'>" +
					   "<div style='display:inline-block; padding:40px; background:white; border-radius:10px;'>" +
					   "<h1 style='color:#dc3545;'>🚫 Access Denied Get it verified in the searchpage</h1>" +
					   "<p>The content: <b>" + sessionVideoId + "</b> is not allowed.</p>" +
					   "<button onclick='window.history.back()'>Go Back</button></div></body></html>";
			}

            if (videoId == null || videoId.isEmpty()) {
                res.status(400);
                return "No video ID";
            }

            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
		
			
    try {

                // 🔥 MULTITHREADING TASKS
                Future<String> titleFuture = metadataExecutor.submit(() -> getVideoTitle(videoUrl));

				Future<VideoInfo> ytCategoryFuture = metadataExecutor.submit(() -> {
						if (categoryCache.containsKey(videoId)) {
							return categoryCache.get(videoId);
						}

						YouTubeCategoryFinder finder = new YouTubeCategoryFinder(videoId);
						String category = finder.getYouTubeCategory();
						String channel = finder.getChannelName();
						if(YouTubeCategoryFinder.isAllowed(category,channel)) category="Education";//i made changes here
						VideoInfo info = new VideoInfo(category, channel);
						categoryCache.put(videoId, info);

						return info;
					});

                String title = titleFuture.get(5,TimeUnit.SECONDS);
				
       
				VideoInfo info = ytCategoryFuture.get(5,TimeUnit.SECONDS);
				String ytCategory = info.category;
				String channelName = info.channel;

                if (title == null) {
                    res.status(500);
                    return "Failed to fetch video metadata";
                }

						

                // 🎥 STREAM
				
				String format;
				//
				if (ytCategory == null) {
					format = "best"; // Fallback
				} else if (ytCategory.equalsIgnoreCase("Education") || ytCategory.equalsIgnoreCase("Science & Technology")) {
					// 720p limit
					format = "best[height<=720][ext=mp4]/best[height<=720]/best";
				} else if (ytCategory.equalsIgnoreCase("Music")) {
					// Less than 360p
					format = "best[height<=240][ext=mp4]/best[height<=240]/best";
				} else if (ytCategory.equalsIgnoreCase("Devotional") || ytCategory.contains("Religious")) {
					// 480p fixed (or best available up to 480p)
					format = "best[height=480][ext=mp4]/best[height<=480]/best";
				} else {
					format = "best[height<=480]/best"; // Default for other categories
				}

					String tempFile = "temp_" + videoId + ".mp4";
					File file = new File(tempFile);
					System.out.println("Downloading video: " + videoId);
				
				
				
				
				//newly added stream block from gemini
				Process process = null; // Declare outside try
		try {
					ProcessBuilder pb = new ProcessBuilder(
							"yt-dlp",
							"-f", format,
							"-o", tempFile,
							"--merge-output-format", "mp4",
							"--no-playlist",
							"--quiet",
							videoUrl
					);
					

					process = pb.start();
					process.waitFor();
					//res.type("video/mp4");
					
					if (!file.exists()) {
					res.status(500);
						return "Download failed";
					}
					String range = req.headers("Range");
						long fileLength = file.length();
						long start = 0;
						long end = fileLength - 1;

						if (range != null && range.startsWith("bytes=")) {
							String[] ranges = range.substring(6).split("-");
							start = Long.parseLong(ranges[0]);

							if (ranges.length > 1) {
								end = Long.parseLong(ranges[1]);
							}

							res.status(206);
						} else {
							res.status(200);
						}

						long contentLength = end - start + 1;

						res.raw().setContentType("video/mp4");
						res.raw().setHeader("Accept-Ranges", "bytes");
						res.raw().setHeader("Content-Length", String.valueOf(contentLength));
						res.raw().setHeader("Content-Range",
								"bytes " + start + "-" + end + "/" + fileLength);

						try (RandomAccessFile raf = new RandomAccessFile(file, "r");
							 OutputStream out = res.raw().getOutputStream()) {

							raf.seek(start);

							byte[] buffer = new byte[65536];
							long bytesLeft = contentLength;
							int len;

							while (bytesLeft > 0 &&
								   (len = raf.read(buffer, 0,
									(int)Math.min(buffer.length, bytesLeft))) != -1) {

								out.write(buffer, 0, len);
								bytesLeft -= len;
							}

						} catch (IOException e) {
							System.out.println("Client disconnected: " + videoId);
						}

						return null;
					
					
			}catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error: " + e.getMessage();
            //changes made here
			 }
		}catch (spark.HaltException e) {
			// 🔥 CRITICAL: Re-throw this so Spark knows to stop the request!
				throw e; 
        } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error: " + e.getMessage();}
		});
	}
    

    // 🔍 Extract Video ID
    public static String extractVideoId(String input) {
        if (input == null) return null;

        if (input.contains("v=")) {
            return input.split("v=")[1].split("&")[0];
        }

        if (input.contains("youtu.be/")) {
            return input.split("youtu.be/")[1].split("\\?")[0];
        }
		 // New condition for embed URLs (e.g., ://youtube.com)
	    if (input.contains("embed/")) {
        return input.split("embed/")[1].split("\\?")[0];
    }

        if (input.length() == 11) return input;

        return null;
    }

    // 📺 Get Video Title using yt-dlp
 public static String getVideoTitle(String videoUrl) {
    try {
        // 1. Check cache first
        if (titleCache.containsKey(videoUrl)) {
            return titleCache.get(videoUrl);
        }

        // 2. Setup Process
        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "--dump-json",
                "--no-playlist",
                "--quiet",
                videoUrl
        );
        
        // CRITICAL: This lets us see error messages if Sophos blocks the app
        pb.redirectErrorStream(true); 

        Process process = pb.start();
        StringBuilder output = new StringBuilder();

        // 3. Read Output (using try-with-resources for safety)
        try (Scanner sc = new Scanner(process.getInputStream(), "UTF-8")) {
            while (sc.hasNextLine()) {
                output.append(sc.nextLine());
            }
        }
        
        // 4. Wait for process to finish
        int exitCode = process.waitFor();
        String data = output.toString();

        if (exitCode != 0 || data.isEmpty()) {
            System.err.println("yt-dlp failed (Exit: " + exitCode + "). Check Sophos/URL. Output: " + data);
            return "Error: Could not fetch title";
        }

        // 5. Safe Parsing Logic

        String pattern = "\"title\": \"";
        int startPos = data.indexOf(pattern);
        
        if (startPos == -1) {
            return "Title field not found in JSON";
        }

        int start = startPos + pattern.length();
        // Look for the closing quote of the title
        int end = data.indexOf("\"", start);

        if (end == -1) {
            return "Malformed JSON response";
        }

        String title = data.substring(start, end);
        
        // 6. Cache and return
        titleCache.put(videoUrl, title);
        return title;

    } catch (Exception e) {
        System.err.println("Exception in getVideoTitle: " + e.getMessage());
        e.printStackTrace();
        return null;
		}
	}
    }



