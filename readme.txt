To run this file one need to have "Maven" compiler and jdk file to compile
Can compile by going to root directory and then open cmd and run "maven clean install" or " "<full path to mvnd.exe>" clean install " and if you want to recompile then delete the "target"  file then again compile.
After compiling one needs to open the folder named target which has "VideoServer-jar-with-dependencies.jar" to be clicked. 
(If somebody wants to see logs then "java -jar VideoServer-jar-with-dependencies.jar" by opening the cmd terminal in target)

Then use ip: "<ServerIP>:8080  in web browser to open and check
One also requires to have yt-dlp, so the server can download it by "winget install yt-dlp" running it in command prompt for windows. 
User is advised to download yt-dlp before running the VideoServer jar file with the help of command "java -jar VideoServer-jar-with-dependencies.jar"
To close the server running behind . one has to open task manager and search for "JAVAW" and stop it. or open command prompt and type "netstat -ano | findstr :8080" then copy the process id 
and use "tasklist | findstr <PID>" to kill it.