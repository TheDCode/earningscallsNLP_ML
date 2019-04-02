package scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */


public class App
{
    public static class Proxy{

        String IP;
        String port;

        public Proxy(String IP, String port){
            this.IP = IP;
            this.port = port;
        }

    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private static ArrayList<Proxy> getProxies(){
        // list of proxy objects
        ArrayList<Proxy> proxiesList = new ArrayList<>();

        // create a file to store proxies' IP and ports
        File proxiesFile = new File("proxies.txt");

        FileWriter fr = null;

        String ip = "";
        String port = "";

        // scrapping proxies
        final String urlFreeProxies = "https://free-proxy-list.net/";

        try{
            final Document documentProxies = Jsoup.connect(urlFreeProxies)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .get();

            try {
                fr = new FileWriter(proxiesFile);

                for (Element tr : documentProxies.select("table.table tbody tr")){
                    String line = tr.select("td").text();

                    StringTokenizer st = new StringTokenizer(line);

                    int num = 2;

                    if(st.hasMoreTokens()){
                        String mystring = st.nextToken();
                        char character = mystring.charAt(0);
                        if (Character.isDigit(character)) {
                            ip = mystring;
                            port = st.nextToken();
                        }
                    }


                        Proxy newProxy = new Proxy(ip, port);
                        proxiesList.add(newProxy);





                /*while (st.hasMoreTokens() && num > 0) {

                    String mystring = st.nextToken();
                    char character = mystring.charAt(0);
                    if (Character.isDigit(character)) {
                        fr.write(mystring);
                        fr.write("  ");
                        num--;
                    }
                    else {
                        continue;
                    }

                    Proxy newProxy = new Proxy()

                }*/


                    fr.write("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                //close resources
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println("There are: " + proxiesList.size() + " proxies");

        // print list of proxies
        for (Proxy p : proxiesList)
            System.out.println(p.IP + " " + p.port );

        return proxiesList;
    }

    public static void main( String[] args ) throws IOException, InterruptedException {

        ArrayList<Proxy> proxiesList = getProxies();

        int successCounter = 1853;
        int failsCounter = 0;

        for (int i = 100; i < 150; i++){

            System.out.println("----------------------------------------------------------------");

            final String url = "https://www.fool.com/earnings-call-transcripts/?page=" + i;

            int randomNum = getRandomNumberInRange(0,proxiesList.size()-12);

            System.out.println("Ramdom number:" + randomNum );

            Proxy currentProxy = proxiesList.get(randomNum);

            System.setProperty("http.proxyHost", currentProxy.IP);
            System.setProperty("http.proxyPort", currentProxy.port);

            System.out.println(System.getProperty("http.proxyHost"));
            System.out.println("Using IP: " + currentProxy.IP + " Port: " +  currentProxy.port);

            try{
                final Document document = Jsoup.connect(url)
                        .userAgent("Mozilla")
                        .get();

                //for (Element row : document.select("li.list-group-item h3 a")){

                    for (Element row : document.select("div.text h4 a")){

                        System.out.println(row);


                    String fileName = "transcript" + successCounter + ".txt";

                    // create a file to store proxies' IP and ports
                    File currFile = new File(fileName);

                    FileWriter fr = null;

                    fr = new FileWriter(currFile);


                    randomNum = getRandomNumberInRange(0,proxiesList.size()-12);

                    currentProxy = proxiesList.get(randomNum);

                    System.out.println("Using IP: " + currentProxy.IP + " Port: " +  currentProxy.port);

                    //set HTTP proxy host to 127.0.0.1 (localhost)
                    System.setProperty("http.proxyHost", currentProxy.IP);

                    //set HTTP proxy port to 3128
                    System.setProperty("http.proxyPort", currentProxy.port);

                    System.out.println("Current IP:" + System.getProperty("http.proxyHost"));
                    System.out.println("Current Port:" + System.getProperty("http.proxyPort"));

                    // get url from href attribute
                    String href = row.attr("href");

                    // build full url to transcript
                    String fullurl = "https://www.fool.com" + href;

                    System.out.println(fullurl);

                    TimeUnit.SECONDS.sleep(10);

                    try {
                        final Document document1 = Jsoup.connect(fullurl)
                                .userAgent("Mozilla")
                                .header("Accept", "text/html")
                                .header("Accept-Encoding", "gzip,deflate")
                                .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                                .header("Connection", "keep-alive")
                                .ignoreContentType(true)
                                .get();

                        successCounter++;

                        Elements row1 = document1.select("section.article-body");

                        for (Element myrow : row1.select("p")){


                            fr.write(myrow.text());

                            fr.write("\n");

                            System.out.println(myrow.text());
                        }

                        //System.out.println(document1.select("div.sa-art"));

                        fr.close();

                    }catch(Exception e){
                        e.printStackTrace();
                        failsCounter++;
                        continue;
                    }

                    }

            }
            catch (Exception e){

                e.printStackTrace();
                //failsCounter++;
                TimeUnit.SECONDS.sleep(10);
                continue;
                //e.getLocalizedMessage();
            }
        }


        System.out.println( "SUCCESS:" + successCounter );
        System.out.println( "FAILS:" + failsCounter );
    }
}
