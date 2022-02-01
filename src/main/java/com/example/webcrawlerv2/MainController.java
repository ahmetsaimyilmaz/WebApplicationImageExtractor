package com.example.webcrawlerv2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class MainController {
    Set<String> globalList = new HashSet<String>();

    @Autowired
    ListLinksThreaded listLinksThreaded;



    @GetMapping("")
    public String showHomePage(){
        return "index";
    }
    @GetMapping("secondPage")
    public String showSecondPage(){
        return "loadingPage";
    }

    @GetMapping("/downloadJson")
    public ResponseEntity<byte[]> downloadJsonFile() {

        GsonBuilder GsonBuilder = new GsonBuilder();

        Gson gson = GsonBuilder.create();
        String JSONObject = gson.toJson(globalList);

        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = prettyGson.toJson(globalList);


        byte[] customerJsonBytes = prettyJson.getBytes();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=customers.json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(customerJsonBytes.length)
                .body(customerJsonBytes);
    }

    @GetMapping({"{url}/{depth}"})
    public String crawlPage(@PathVariable("url") String theURL, @PathVariable("depth") int depth) throws IOException, InterruptedException {
        //listLinksThreaded.start(theURL,depth);
        String toBeReplaced="https://";
        toBeReplaced+=theURL;
        theURL=toBeReplaced;
        long startTime = System.currentTimeMillis();
        ListLinksThreaded sample = new ListLinksThreaded();
        Set<String> uniqueImageURL = new HashSet<String>();
        uniqueImageURL = sample.start(theURL, depth);
        globalList=uniqueImageURL;
        System.out.println("Printing out the images: ");
        System.out.println();
        GsonBuilder GsonBuilder = new GsonBuilder();

        Gson gson = GsonBuilder.create();
        String JSONObject = gson.toJson(uniqueImageURL);

        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = prettyGson.toJson(uniqueImageURL);



        try (Writer writer = new FileWriter("threadComparison1967.json")) {

            prettyGson.toJson(uniqueImageURL, writer);

        }
        long endTime = System.currentTimeMillis();
        long totalConnectionTime = endTime - startTime;
        System.out.println("That took " + (endTime - startTime) + " milliseconds");
        System.out.println("Total connection time based on Jsoup.connect is " + totalConnectionTime + " milliseconds");
      return "index";
    }
}
