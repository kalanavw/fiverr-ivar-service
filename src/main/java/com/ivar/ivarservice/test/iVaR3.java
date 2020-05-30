package com.ivar.ivarservice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class iVaR3 {

    List<String> date = new ArrayList<String>();
    List<Double> closePrice = new ArrayList<Double>();

    public static double[][] r2_adj;
    public static double[][] t1_stat;

    public double firstPrice = 0;
    public double lastPrice = 0;
    public String endDate;

    int[] iVaRIndex;
    int[] perfIndex;
    int baseIndex;


    static public int baseDataCode;
    public int lines = 0;
    public static String[][] resultTable_str = null;
    public static double[][] resultTable_num = null;

    public static List<subSet> quartileSet = new ArrayList<subSet>();
    public static List<subSet> sectorSet = new ArrayList<subSet>();
    public static subSet allSet;
    /*
     * indexes:
     * 1 - performance: 1m 2m 3m 4m 6m 9m 1y 2y 3y 5y
     * 2 - iVaR       : 1m 2m 3m 4m 6m 9m 1y 2y 3y
     * 3 - number of stock
     * */
    public static int[] iVaRMonth = {1, 2, 3, 4, 6, 9, 12, 18, 24, 36, 60};
    public static int[] perfMonth = {1, 2, 3, 4, 6, 9, 12, 18, 24, 36, 60, 120};
    public static double iVaR_table[][];
    public static double perf_table[][];

    public static void main(String[] args) throws IOException {
        downloader.loadCompanyList("list_2.csv");
        formSubSets();


        int ni = downloader.symbols.size();
        int nj = iVaRMonth.length;
        int nk = perfMonth.length;


        iVaR_table = new double[nj][ni];
        perf_table = new double[nk][ni];
        r2_adj = new double[nj][nk];
        t1_stat = new double[nj][nk];


        String baseDate = "2017-01-01";
        baseDataCode = dateCode(baseDate);
        for (int i = 0; i < ni; i++) {

            String symbol = downloader.symbols.get(i);
            System.out.println(i);
            iVaR3 iv = new iVaR3();
            iv.loadList("data/" + symbol + ".csv");
            iv.dateAnalysis();
            for (int j = 0; j < nj; j++) {
                iVaR_table[j][i] = iv.iVarCalculate(iv.iVaRIndex[j]);
            }

            for (int k = 0; k < nk; k++) {
                perf_table[k][i] = iv.perfCalculate(iv.perfIndex[k]);
            }
        }


        subSetRegression(nj, nk, allSet);
        for (int s = 0; s < quartileSet.size(); s++) {
            subSetRegression(nj, nk, quartileSet.get(s));
        }
        for (int s = 0; s < sectorSet.size(); s++) {
            subSetRegression(nj, nk, sectorSet.get(s));
        }
        outputIvarPerformance();
        return;
    }


    public static void outputIvarPerformance() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("iVaRperf.csv"));
		/*
		public static int[] iVaRMonth={1,2,3,4,6,9,12,18,24,36,60};
		public static int[] perfMonth={1,2,3,4,6,9,12,18,24,36,60,120};
		*/
        writer.write("symbol");

        for (int i = 0; i < iVaRMonth.length; i++) {
            writer.write("iVaR ");
            writer.write(Integer.toString(iVaRMonth[i]));
            writer.write("M,");
        }
        for (int i = 0; i < perfMonth.length; i++) {
            writer.write("perf ");
            writer.write(Integer.toString(perfMonth[i]));
            if (i == perfMonth.length - 1) {
                writer.write("M\n");
            } else {
                writer.write("M,");
            }
        }
        for (int i = 0; i < iVaR_table[0].length; i++) {
            writer.write(downloader.symbols.get(i));
            for (int j = 0; j < iVaRMonth.length; j++) {
                writer.write(String.format(Locale.US, "%.4f", iVaR_table[j][i]));
                writer.write(",");
            }
            for (int j = 0; j < perfMonth.length; j++) {
                writer.write(String.format(Locale.US, "%.4f", perf_table[j][i]));
                if (j < perfMonth.length - 1) {
                    writer.write(",");
                } else {
                    writer.write("\n");
                }
            }

        }
        writer.close();
        return;
    }


    public static void subSetRegression(int nj, int nk, subSet set1) throws IOException {
        LinearRegression[][] lin = new LinearRegression[nj][nk];
        for (int j = 0; j < nj; j++) {
            for (int k = 0; k < nk; k++) {
                lin[j][k] = new LinearRegression(iVaR_table[j], perf_table[k], set1);
                r2_adj[j][k] = lin[j][k].R2_adj();
                t1_stat[j][k] = lin[j][k].T1_stat();
            }
        }
        writeTable(r2_adj, iVaRMonth, perfMonth, set1.name + "_r2_adj.csv");
        writeTable(t1_stat, iVaRMonth, perfMonth, set1.name + "_t1_stat.csv");
    }

    public static void formSubSets() {
        List<Double> marketCaps_sort = new ArrayList<Double>(downloader.marketCaps);

        Collections.sort(marketCaps_sort);


        int i;
        for (i = 0; i < marketCaps_sort.size(); i++) {
            if (marketCaps_sort.get(i) > 0) break;
        }
        double quart1_bar = marketCaps_sort.get((marketCaps_sort.size() - i) / 4);
        double quart2_bar = marketCaps_sort.get((marketCaps_sort.size() - i) * 2 / 4);
        double quart3_bar = marketCaps_sort.get((marketCaps_sort.size() - i) * 3 / 4);

        for (int j = 0; j < 4; j++) quartileSet.add(new subSet("Quartile" + (j + 1)));

        for (i = 0; i < marketCaps_sort.size(); i++) {
            double t_cap = downloader.marketCaps.get(i);
            int q = ((t_cap > quart1_bar) ? 1 : 0) + ((t_cap > quart2_bar) ? 1 : 0) + ((t_cap > quart3_bar) ? 1 : 0);
            quartileSet.get(q).indexes.add(i);
        }

        List<String> sector_check = new ArrayList<String>(downloader.sectors);

        List<String> sector_list = new ArrayList<String>();

        while (!sector_check.isEmpty()) {
            if (sector_check.get(0).length() == 0) {
                sector_check.remove(0);
                continue;
            }
            sector_list.add(sector_check.get(0));
            sector_check.removeAll(sector_list);
        }

        for (i = 0; i < sector_list.size(); i++) {
            sectorSet.add(new subSet(sector_list.get(i)));
        }
        for (i = 0; i < downloader.sectors.size(); i++) {
            int ind = sector_list.indexOf(downloader.sectors.get(i));
            if (ind >= 0) {
                sectorSet.get(ind).indexes.add(i);
            }
        }
        allSet = new subSet("All");
        for (i = 0; i < downloader.sectors.size(); i++) allSet.indexes.add(i);
    }


    public static void writeTable(double[][] table, int[] rowHeader, int[] columnHeader, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("perf,");
        for (int j = 0; j < table[0].length; j++) {
            writer.write(Integer.toString(columnHeader[j]));
            if (j < table[0].length - 1) writer.write(",");
        }
        writer.write("\n");
        for (int i = 0; i < table.length; i++) {
            writer.write(Integer.toString(rowHeader[i]));
            writer.write(",");
            for (int j = 0; j < table[i].length; j++) {
                writer.write(String.format(Locale.US, "%.4f", table[i][j]));
                if (j < table[i].length - 1) writer.write(",");
            }
            writer.write("\n");
        }
        writer.close();
        return;
    }
	/*
	public static void writeFile(String[][] table,String fileName) throws IOException {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
	    for(int i=0;i<table.length;i++) {
		    for(int j=0;j<table[i].length;j++) {
		    	writer.write(table[i][j]);
	    		writer.write(",");
		    }
    		writer.write("\n");
	    }
	    writer.close();
		return;
	}
	*/

    /**
     * This method loads data for one stock for further calculations
     * Input data - fileName
     * First column - data (keeps for future)
     * Fifth column - close price
     */
    private void loadList(String fileName) throws IOException {
        BufferedReader fileIn = new BufferedReader(new FileReader(fileName));
        String line;
        String[] lineSplitter = {"0000-00-00"};
        fileIn.readLine();
        while ((line = fileIn.readLine()) != null) {
            lineSplitter = line.split(",");
            try {
                closePrice.add(Double.parseDouble(lineSplitter[4]));
                date.add(lineSplitter[0]);
            } catch (Exception e) {

            }

        }
        endDate = lineSplitter[0];
        fileIn.readLine();
        fileIn.close();

    }

    /**
     * Input data
     * y - number of years
     * norm - normalized values
     * <p>
     * Output data - iVar integral
     * <p>
     * Dimension:
     * If normalized = false
     * Unit of price multiplied by Unit of Time
     * <p>
     * If normalized = true
     * Unit of Time
     * <p>
     * Unit of price - dimension of price in input data
     * Unit of time - one tick in input data (no filtering for weekend implemented)
     *
     * @throws NumberFormatException
     */

    public void dateAnalysis() {

//	int[] iVaRMonth={1,2,3,4,6,9,12,18,24,36,60};
//	int[] perfMonth={1,2,3,4,6,9,12,18,24,36,60,120};
        iVaRIndex = new int[iVaRMonth.length];
        perfIndex = new int[perfMonth.length];
        for (int k = 0; k < iVaRMonth.length; k++) iVaRIndex[k] = -1;
        for (int k = 0; k < perfMonth.length; k++) perfIndex[k] = -1;

        int ind = iVaRMonth.length - 1;
        int i;
        for (i = 0; i < date.size() && ind >= 0; i++) {
            int dateShift = dateCode(date.get(i)) - baseDataCode;
            if (dateShift >= -100 * iVaRMonth[ind]) {
                if (i == 0) {
                    ind = ind - 1;
                    i = i - 1;
                } else {
                    iVaRIndex[ind] = i;
                    ind = ind - 1;
                }
            }
        }
        for (; i < date.size(); i++) {
            int dateShift = dateCode(date.get(i)) - baseDataCode;
            if (dateShift >= 0) {
                baseIndex = i;
                break;
            }
        }
        ind = 0;
        for (; i < date.size(); i++) {
            int dateShift = dateCode(date.get(i)) - baseDataCode;
            if (dateShift >= 100 * perfMonth[ind]) {
                perfIndex[ind] = i;
                ind = ind + 1;
            }
        }
    }

/*
public double performanceCalculate(int ex_m, String baseDate) {
	int endDateCode=dateCode(endDate);
		// There is not enough data for this iVaR and performance period
		if(date.isEmpty()||endDateCode-baseDataCode<ex_m*100){
			return 0.0/0.0;
		}
		
}

*/

    public double iVarCalculate(int start_ind) {

        // There is not enough data for this iVaR and performance period
        double iVarIntegral = 0;
        double peakPrice = -1e50;
        if (start_ind == -1) return 0.0 / 0.0;
        for (int i = start_ind; i <= this.baseIndex; i++) {
            peakPrice = Math.max(peakPrice, closePrice.get(i));
            iVarIntegral = iVarIntegral + (peakPrice - closePrice.get(i)) / peakPrice;
        }
        return iVarIntegral;
    }

    public double perfCalculate(int end_ind) {
        if (end_ind == -1) return 0.0 / 0.0;
        return Math.log(closePrice.get(end_ind) / closePrice.get(this.baseIndex));
    }

    /**
     * Convert date to number for comparison
     */
    private static int dateCode(String dateLine) {
        int Year = Integer.parseInt(dateLine.substring(0, 4));
        int Month = Integer.parseInt(dateLine.substring(5, 7));
        int Day = Integer.parseInt(dateLine.substring(8, 10));
        return 1200 * Year + 100 * Month + Day;
    }

    /**
     * Standard correlation function
     * xs, ys - arrays of data for correlation
     * output - correlation coefficient
     */
    public static double Correlation(double[] xs, double[] ys) {

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int nn = xs.length;
        int n = 0;

        for (int i = 0; i < nn; ++i) {
            double x = xs[i];
            double y = ys[i];

            if ((x == x && y == y)) {
                sx += x;
                sy += y;
                sxx += x * x;
                syy += y * y;
                sxy += x * y;
                n = n + 1;
            }
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }
}




