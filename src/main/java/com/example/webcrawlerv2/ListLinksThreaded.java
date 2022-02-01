package com.example.webcrawlerv2;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Example program to list links from a URL.
 */
@Component
public class ListLinksThreaded {
    static int connectCounter = 0;
    static long totalConnectionTime = 0;
    static int totalThread = 4;

    public static Set<String> start(String url1, int depth) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        //String url = "https://www.smashingmagazine.com";
        String url = url1;
        // String url = "https://www.milliyet.com.tr";
        Set<String> uniqueImageURL = new HashSet<String>();
        System.out.println("Please provide URL of the page and depth you want to crawl into");
        uniqueImageURL = connectToUrl(url1, depth-1);
        System.out.println();
        return uniqueImageURL;
    }



    private static Set<String> connectToUrl(String inputURL, int depth) throws InterruptedException, IOException {
        print("Fetching %s with depth " + depth + "...", inputURL);
        System.out.println();
        long a, b, c, d, f, g, h, k, l, m, n;
        a = System.currentTimeMillis();
        Set<String> uniqueURL = new HashSet<String>();
        Set<String> uniqueImageURL = new HashSet<String>();
        Set<String> uniqueTargetURL = new HashSet<String>();
        Set<String> testCase = new HashSet<String>();

        ArrayList<HashSet<String>> depthList = new ArrayList<HashSet<String>>();
        HashSet<String> ImageList = new HashSet<String>();
        b = System.currentTimeMillis();

        for (int i = 0; i < depth + 1; i++) {
            HashSet<String> sample = new HashSet<String>();
            depthList.add(sample);
        }

        System.out.println("Creation of variables and lists took " + (b - a) + " milliseconds");
        depthList.get(0).add(inputURL);

        String url = inputURL;
        Document doc;
        Set<String> unique = new HashSet<String>();

        try {
            if (depth == 0)
                System.out.println();
            else {
                uniqueURL.add(inputURL);
                c = System.currentTimeMillis();
                for (int i = 0; i < depth; i++) {

                    ArrayList<HashSet<String>> firstTraverseList = new ArrayList<HashSet<String>>();
                    for (int j = 0; j < 2; j++) {
                        HashSet<String> sample = new HashSet<String>();
                        depthList.add(sample);
                    }

                    List<String> listConverted = new ArrayList<String>(depthList.get(i));
                    firstTraverseList = passIntoThreads0(listConverted, url);
                    ImageList.addAll(firstTraverseList.get(0));
                    depthList.get(i + 1).addAll(firstTraverseList.get(1));
                }
                d = System.currentTimeMillis();
                System.out.println("Populating links based on depth took " + (d - c) + " milliseconds");
            }
            System.out.println("depth list length " + depthList.get(depth).size());
            f = System.currentTimeMillis();

            HashSet<String> lastTraverseList = new HashSet<String>();
            List<String> listConverted = new ArrayList<String>(depthList.get(depth));
            System.out.println(listConverted.size() + " difference");
            lastTraverseList = passIntoThreads1(listConverted, url);
            System.out.println(lastTraverseList.size() + " debug 3");
            ImageList.addAll(lastTraverseList);

            g = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Debug 2" + ImageList.size());
        return ImageList;

    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }

    private static HashSet<String> passIntoThreads1(List<String> listToDivide, String url) throws InterruptedException {
        ArrayList<myThread> threadList = new ArrayList<myThread>();

        int partitionSize = listToDivide.size() / totalThread;// 100/7 = 14 100/2 =50
        List<ArrayList<String>> partitions = new LinkedList<ArrayList<String>>();
        for (int i = 0; i < partitionSize; i++) {
            ArrayList<String> temp = new ArrayList<String>();
            partitions.add(temp);
        }
        int count = 0;
        for (int i = 0; i < listToDivide.size(); i += partitionSize) {
            if (partitionSize == count) {
                ArrayList<String> temp = new ArrayList<String>();
                temp = partitions.get(partitionSize - 1);
                List<String> temp2 = new ArrayList<String>();
                temp2 = listToDivide.subList(i, Math.min(i + partitionSize, listToDivide.size()));
                for (int j = 0; j < temp2.size(); j++) {
                    temp.add(temp2.get(j));
                }
                partitions.remove(partitionSize - 1);
                partitions.add(temp);
                // partitions.get(partitionSize - 1)
                // .addAll(listToDivide.subList(i, Math.min(i + partitionSize,
                // listToDivide.size())));
            } else {
                ArrayList<String> temp = new ArrayList<String>(
                        listToDivide.subList(i, Math.min(i + partitionSize, listToDivide.size())));
                partitions.set(count, temp);

            }
            count++;
        }
        System.out.println(listToDivide.size() + " Debug");

        for (int i = 0; i < totalThread; i++) {
            System.out.println(partitions.get(i).size() + " Debug " + i);
            System.out.println(partitions.get(i).toString());
            myThread newThread = new myThread(partitions.get(i), 1, url);
            threadList.add(newThread);
        }
        for (myThread T : threadList) {
            T.start();
        }

        for (myThread T : threadList) {
            T.join();
        }

        HashSet<String> newList = new HashSet<String>();
        for (myThread T : threadList) {
            System.out.println(T.resultList.size() + " " + T.getName());
            newList.addAll(T.resultList);
            T.interrupt();
        }
        threadList.clear();
        return newList;

    }

    @SuppressWarnings("deprecation")
    private static ArrayList<HashSet<String>> passIntoThreads0(List<String> listToDivide, String url)
            throws InterruptedException {
        ArrayList<myThread> threadList = new ArrayList<myThread>();
        int tempThreadSize=totalThread;
        System.out.println(listToDivide.get(0));
        int partitionSize = listToDivide.size() / totalThread;// 100/7 = 14 100/2 =50
        if (partitionSize == 0) {
            partitionSize++;
            totalThread = 1;
        }
        List<ArrayList<String>> partitions = new LinkedList<ArrayList<String>>();

        for (int i = 0; i < partitionSize; i++) {
            ArrayList<String> temp = new ArrayList<String>();
            partitions.add(temp);
        }
        int count = 0;

        if (partitionSize == 0) {
            partitionSize++;
        }
        for (int i = 0; i < listToDivide.size(); i += partitionSize) {
            if (partitionSize == count) {
                ArrayList<String> temp = new ArrayList<String>();
                System.out.println(partitionSize + " asdsaddsadsadsaads");
                temp = partitions.get(partitionSize - 1);
                List<String> temp2 = new ArrayList<String>();
                temp2 = listToDivide.subList(i, Math.min(i + partitionSize, listToDivide.size()));
                for (int j = 0; j < temp2.size(); j++) {
                    temp.add(temp2.get(j));
                }
                partitions.remove(partitionSize - 1);
                partitions.add(temp);
                // partitions.get(partitionSize - 1)
                // .addAll(listToDivide.subList(i, Math.min(i + partitionSize,
                // listToDivide.size())));
            } else {
                ArrayList<String> temp = new ArrayList<String>(
                        listToDivide.subList(i, Math.min(i + partitionSize, listToDivide.size())));
                partitions.set(count, temp);

            }
            count++;
        }
        System.out.println(listToDivide.size() + " Debug");

        for (int i = 0; i < totalThread; i++) {
            myThread newThread = new myThread(partitions.get(i), 0, url);
            threadList.add(newThread);
        }
        for (myThread T : threadList) {
            T.start();
        }

        for (myThread T : threadList) {
            T.join();
        }

        ArrayList<HashSet<String>> newList = new ArrayList<HashSet<String>>();
        for (int j = 0; j < 2; j++) {
            HashSet<String> sample = new HashSet<String>();
            newList.add(sample);
        }
        for (myThread T : threadList) {

            newList.get(0).addAll(T.resultList);
            newList.get(1).addAll(T.resultLinks);
            Thread.sleep((long) 100);
            T.interrupt();
        }
        threadList.clear();
        totalThread=tempThreadSize;
        return newList;

    }
    public static class myThread extends Thread {
        List<String> liste;
        HashSet<String> resultList;
        HashSet<String> resultLinks;
        Set<String> uniqueURL = new HashSet<String>();
        int option;
        String URL;
        public myThread(List<String> liste, int option, String URL) {
            this.URL=URL;
            this.liste = liste;
            resultList = new HashSet<String>();
            resultLinks = new HashSet<String>();
            this.option = option;
        }

        @Override
        public void run() {
            uniqueURL.add(URL);
            if (option == 1) {
                System.out.println("Thread number " + this.getName() + " started running.");

                Document doc;
                try {
                    for (String link : liste) {
                        if (!link.equalsIgnoreCase("https://www.smashingmagazine.com/category/servers")) {
                            long c1 = System.currentTimeMillis();

                            doc = Jsoup.connect(link).ignoreContentType(true).ignoreHttpErrors(true)
                                    .userAgent("Mozilla/5.0").timeout(300000).get();

                            long c2 = System.currentTimeMillis();
                            // System.out.println("It took " + (c2 - c1) + " milliseconds to connect " +
                            // link + " using thread "
                            // + this.getName());
                            Elements media = doc.select("[src]");
                            for (Element src : media) {
                                if (!src.attr("src").equals(""))
                                    if (src.normalName().equals("img") && !src.attr("src").substring(0, 1).equals("/")
                                            && !src.attr("src").substring(0, 1).equals(".")) {
                                        resultList.add(src.attr("src"));
                                    }
                            }
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if (option == 0) {
                System.out.println("Thread number " + this.getName() + " started running.");

                Document doc;
                try {
                    for (String link : liste) {
                        long c1 = System.currentTimeMillis();
                        doc = Jsoup.connect(link).ignoreContentType(true).ignoreHttpErrors(true)
                                .userAgent("Mozilla/5.0").timeout(300000).get();
                        long c2 = System.currentTimeMillis();

                        //totalConnectionTime += c2 - c1;
                        // System.out.println("It took "+(c2-c1)+" milliseconds to connect "+link);
                        //connectCounter++;
                        Elements links = doc.select("a[href]");
                        for (Element InnerLink : links) {
                            if (InnerLink.attr("abs:href").startsWith(URL)
                                    && InnerLink.attr("abs:href").substring(URL.length()).contains("/")) {
                                int gate = 1;
                                for (String linkCheck : uniqueURL) {
                                    if (linkCheck.equals(InnerLink.attr("abs:href")))
                                        gate = -1;
                                }

                                if (gate == 1) {
                                    uniqueURL.add(InnerLink.attr("abs:href"));
                                    //depthList.get(i + 1).add(InnerLink.attr("abs:href"));
                                    resultLinks.add(InnerLink.attr("abs:href"));

                                    Elements media = doc.select("[src]");
                                    for (Element src : media) {
                                        if (src.normalName().equals("img")
                                                && !src.attr("src").substring(0, 1).equals("/")
                                                && !src.attr("src").substring(0, 1).equals(".")) {
                                            resultList.add(src.attr("src"));
                                        }
                                    }
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }
}