package com.ivar.ivarservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalana.w on 5/27/2020.
 */
@Service
public class IndexService {
    @Value("${data.folder.location}")
    private String dataFolderLocation;
    @Value("${htm.folder.location}")
    private String htmFolderLocation;
    @Value("${data.file.company.list.location}")
    private String companyListLocation;

    static List<String> symbols = new ArrayList<String>();
    static List<String> names = new ArrayList<String>();
    static List<Double> marketCaps = new ArrayList<Double>();
    static List<String> sectors = new ArrayList<String>();

    public int loadCompanyList() {
        try (BufferedReader fileIn = new BufferedReader(new FileReader(new File(companyListLocation)))) {
            String line;
            line = fileIn.readLine(); // read and discard header
            while ((line = fileIn.readLine()) != null) {
                String[] lineSplitter = line.split(";");

                symbols.add(lineSplitter[0]);
                System.out.println(lineSplitter[0]);
                names.add(lineSplitter[1]);
                if (lineSplitter.length < 3) {
                    marketCaps.add(-1.0);
                } else {
                    if (!lineSplitter[2].trim().isEmpty()) {
                        marketCaps.add(Double.parseDouble(lineSplitter[2].replaceAll(",", "")));
                    }
                }
                if (lineSplitter.length < 4) {
                    sectors.add("");
                } else {
                    sectors.add(lineSplitter[3]);
                }
            }
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int loadPriceCsv() {
        String line1 = "https://query1.finance.yahoo.com/v7/finance/download/";
        String line2 = "?period1=1271203200&period2=1586822400&interval=1d&events=history";
        for (int i = 0; i < symbols.size() * 0 + 10; i++) {
            String middle_line = symbols.get(i);
            try (FileOutputStream fos = new FileOutputStream(this.dataFolderLocation.concat(File.separator) + middle_line + ".csv")) {
                System.out.println(i + symbols.get(i));
                URL website = new URL(line1 + middle_line + line2);
                InputStream OS = website.openStream();
                ReadableByteChannel rbc = Channels.newChannel(OS);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                //				Thread.sleep( ( long ) ( 3000 + 2000 * Math.random() ) );
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public int loadPages() {
        String line1 = "https://finance.yahoo.com/quote/";
        String[] p_name = {"", "profile", "key-statistics", "analysis", "sustainability", "holders", "financials"};
        for (int i = 0; i < symbols.size() * 0 + 10; i++) {
            System.out.println(i + symbols.get(i));
            String symbol = symbols.get(i);
            for (int j = 0; j < 7; j++) {
                try (FileOutputStream fos = new FileOutputStream(this.htmFolderLocation.concat(File.separator) + symbol + "_" + p_name[j] + ".htm")) {
                    URL website = new URL(line1 + symbol + "/" + p_name[j] + "?p=" + symbol);
                    InputStream OS = website.openStream();
                    ReadableByteChannel rbc = Channels.newChannel(OS);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    return 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        }
        return 0;
    }
}
