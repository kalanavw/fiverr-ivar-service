package com.ivar.ivarservice.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is dedicated for downloading data from Finance Yahoo.
 */
public class downloader {

    static List<String> symbols = new ArrayList<String>();
    static List<String> names = new ArrayList<String>();
    static List<Double> marketCaps = new ArrayList<Double>();
    static List<String> sectors = new ArrayList<String>();


    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        loadCompanyList("list_2.csv");
        loadPriceCsv();

        //loadPages();
    }


    /**
     * This function loads name of symbols to array for further downloading data
     */
    public static void loadCompanyList(String listName) throws IOException {
        BufferedReader fileIn = new BufferedReader(new FileReader(listName));
        String line;
        line = fileIn.readLine(); // read and discard header
        while ((line = fileIn.readLine()) != null) {
            String[] lineSplitter = line.split(";");

            symbols.add(lineSplitter[0]);
            names.add(lineSplitter[1]);
            if (lineSplitter.length < 3) {
                marketCaps.add(-1.0);
            } else {
                marketCaps.add(Double.parseDouble(lineSplitter[2].replaceAll(",", "")));
            }
            if (lineSplitter.length < 4) {
                sectors.add("");
            } else {
                sectors.add(lineSplitter[3]);
            }
        }
        fileIn.close();
    }

    /**
     * This function loads history data for company list that have to be
     * read by loadCompanyList
     * Sleep time introduced to avoid ban from web server
     */
    private static void loadPriceCsv() throws IOException {
        String line1 = "https://query1.finance.yahoo.com/v7/finance/download/";
        String line2 = "?period1=1114128000&period2=1586822400&interval=1d&events=history";
        for (int i = 0; i < symbols.size(); i++) {
            System.out.println(i);
            String middle_line = symbols.get(i);
            URL website = new URL(line1 + middle_line + line2);
            try {
                InputStream OS = website.openStream();
                ReadableByteChannel rbc = Channels.newChannel(OS);
                FileOutputStream fos = new FileOutputStream("data//" + middle_line + ".csv");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                Thread.sleep((long) (3000 + 2000 * Math.random()));
                fos.close();
            } catch (Exception e) {
                FileOutputStream fos = new FileOutputStream("data//" + middle_line + ".csv");
                fos.close();
            } finally {

            }

        }
    }

    /**
     * This function loads information about companies in form of html pages
     * No parsing done here
     * Sleep time introduced to avoid ban from web server
     */
    private static void loadPages() throws IOException {
        String line1 = "https://finance.yahoo.com/quote/";
        String[] p_name = {"", "profile", "key-statistics", "analysis", "sustainability", "holders", "financials"};
        for (int i = 0; i < symbols.size(); i++) {
            System.out.println(i);
            String symbol = symbols.get(i);
            for (int j = 0; j < 7; j++) {
                URL website = new URL(line1 + symbol + "/" + p_name[j] + "?p=" + symbol);
                try {
                    InputStream OS = website.openStream();
                    ReadableByteChannel rbc = Channels.newChannel(OS);
                    FileOutputStream fos = new FileOutputStream("htm//" + symbol + "_" + p_name[j] + ".htm");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    Thread.sleep((long) (1500 + 1000 * Math.random()));
                    fos.close();
                } catch (Exception e) {
                    FileOutputStream fos = new FileOutputStream("htm//" + symbol + "_" + p_name[j] + ".htm");
                    fos.close();
                } finally {

                }
            }
        }
    }

}
