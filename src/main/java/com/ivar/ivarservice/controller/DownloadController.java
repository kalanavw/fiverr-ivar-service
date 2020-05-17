package com.ivar.ivarservice.controller;

import com.ivar.ivarservice.mode.Result;
import com.ivar.ivarservice.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Controller
@RequestMapping("/")
public class DownloadController
{
	static List<String> symbols = new ArrayList<>();
	static List<String> names = new ArrayList<>();

	@Value("${data.folder.location}")
	private String dataFolderLocation;
	@Value("${htm.folder.location}")
	private String htmFolderLocation;
	@Value("${data.file.company.list.location}")
	private String companyListLocation;

	@Autowired
	private ResultRepository resultRepository;

	@GetMapping()
	public String index( Model model )
	{
		return "index";
	}

	@GetMapping("download")
	public String download( Model model )
	{
		Map<String, String> map = new HashMap<>();
		try
		{
			loadCompanyList();
			loadPriceCsv();
			loadPages();
		}
		catch ( Exception e )
		{
			map.put( "status", "-1" );
			map.put( "message", "Error" );
			model.addAllAttributes( map );
			return "index";
		}
		map.put( "status", "1" );
		map.put( "message", "success" );
		model.addAllAttributes( map );
		return "index";
	}

	/**
	 * This function loads name of symbols to array for further downloading data
	 */
	private void loadCompanyList()
	{
		try
		{
			File file = new File( companyListLocation );
			BufferedReader fileIn = new BufferedReader( new FileReader( file ) );
			String line;
			line = fileIn.readLine(); // read and discard header
			while ( ( line = fileIn.readLine() ) != null )
			{
				String[] lineSplitter = line.split( "\t" );

				symbols.add( lineSplitter[0] );
				names.add( lineSplitter[1] );

			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * This function loads history data for company list that have to be
	 * read by loadCompanyList
	 * Sleep time introduced to avoid ban from web server
	 */
	@Async
	private void loadPriceCsv()
	{
		String line1 = "https://query1.finance.yahoo.com/v7/finance/download/";
		String line2 = "?period1=1271203200&period2=1586822400&interval=1d&events=history";
		for ( int i = 0; i < symbols.size() * 0 + 10; i++ )
		{
			String middle_line = symbols.get( i );
			try (FileOutputStream fos = new FileOutputStream( this.dataFolderLocation.concat( File.separator ) + middle_line + ".csv" ))
			{
				System.out.println( i + symbols.get( i ) );
				URL website = new URL( line1 + middle_line + line2 );
				InputStream OS = website.openStream();
				ReadableByteChannel rbc = Channels.newChannel( OS );
				fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
				//				Thread.sleep( ( long ) ( 3000 + 2000 * Math.random() ) );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This function loads information about companies in form of html pages
	 * No parsing done here
	 * Sleep time introduced to avoid ban from web server
	 */
	@Async
	private void loadPages()
	{
		String line1 = "https://finance.yahoo.com/quote/";
		String[] p_name = { "", "profile", "key-statistics", "analysis", "sustainability", "holders", "financials" };
		for ( int i = 0; i < symbols.size() * 0 + 10; i++ )
		{
			System.out.println( i + symbols.get( i ) );
			String symbol = symbols.get( i );
			for ( int j = 0; j < 7; j++ )
			{
				try (FileOutputStream fos = new FileOutputStream( this.htmFolderLocation.concat( File.separator ) + symbol + "_" + p_name[j] + ".htm" ))
				{
					URL website = new URL( line1 + symbol + "/" + p_name[j] + "?p=" + symbol );
					InputStream OS = website.openStream();
					ReadableByteChannel rbc = Channels.newChannel( OS );
					fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	List<String> date = new ArrayList<>();
	List<Double> closePrice = new ArrayList<>();
	double firstPrice = 0;
	double lastPrice = 0;

	String endDate;
	public int lines = 0;
	public static String[][] resultTable_str = null;
	public static double[][] resultTable_num = null;

	@GetMapping("/calculate")
	public ResponseEntity<?> calculate()
	{
		loadCompanyList();
		int n = this.symbols.size();
		resultTable_str = new String[n + 3][13];
		resultTable_num = new double[13][n + 3];

		String[] header = { "Symbol", "1Y", "3Y", "5Y", "10Y", "Norm 1Y", "Norm 3Y", "Norm 5Y", "Norm 10Y", "Perf 1Y", "Perf 3Y", "Perf 5Y", "Perf 10Y" };

		String[] header_corr = { "Symbol", "Corr-1Y", "Corr-3Y", "Corr-5Y", "Corr-10Y", "Corr-Norm 1Y", "Corr-Norm 3Y", "Corr-Norm 5Y", "Corr-Norm 10Y", "", "", "", "" };
		resultTable_str[0] = header;
		for ( int i = 0; i < n; i++ )
		{
			System.out.println( i );
			String symbol = symbols.get( i );
			resultTable_str[i + 1][0] = symbol;
			try
			{
				loadList( this.dataFolderLocation.concat( File.separator ) + symbol + ".csv" );

				resultTable_num[1][i + 1] = ( iVarCalculate( 1, false ) );
				resultTable_num[9][i + 1] = ( lastPrice / firstPrice - 1 ) / lastPrice * lastPrice;
				resultTable_num[2][i + 1] = ( iVarCalculate( 3, false ) );
				resultTable_num[10][i + 1] = ( lastPrice / firstPrice - 1 ) / lastPrice * lastPrice;
				resultTable_num[3][i + 1] = ( iVarCalculate( 5, false ) );
				resultTable_num[11][i + 1] = ( lastPrice / firstPrice - 1 ) / lastPrice * lastPrice;
				resultTable_num[4][i + 1] = ( iVarCalculate( 10, false ) );
				resultTable_num[12][i + 1] = ( lastPrice / firstPrice - 1 ) / lastPrice * lastPrice;
				resultTable_num[5][i + 1] = ( iVarCalculate( 1, true ) );
				resultTable_num[6][i + 1] = ( iVarCalculate( 3, true ) );
				resultTable_num[7][i + 1] = ( iVarCalculate( 5, true ) );
				resultTable_num[8][i + 1] = ( iVarCalculate( 10, true ) );
				for ( int j = 1; j < 13; j++ )
				{
					resultTable_str[i + 1][j] = Double.toString( resultTable_num[j][i + 1] );
				}
			}
			catch ( Exception E )
			{
				for ( int j = 1; j < 13; j++ )
				{
					resultTable_num[j][i + 1] = 0.0 / 0.0;
					resultTable_str[i + 1][j] = "NaN";
				}
			}

		}
		resultTable_str[n + 1] = header_corr;
		resultTable_str[n + 2][0] = "Correlation";
		resultTable_str[n + 2][1] = Double.toString( Correlation( resultTable_num[1], resultTable_num[9] ) );
		resultTable_str[n + 2][2] = Double.toString( Correlation( resultTable_num[2], resultTable_num[10] ) );
		resultTable_str[n + 2][3] = Double.toString( Correlation( resultTable_num[3], resultTable_num[11] ) );
		resultTable_str[n + 2][4] = Double.toString( Correlation( resultTable_num[4], resultTable_num[12] ) );
		resultTable_str[n + 2][5] = Double.toString( Correlation( resultTable_num[5], resultTable_num[9] ) );
		resultTable_str[n + 2][6] = Double.toString( Correlation( resultTable_num[6], resultTable_num[10] ) );
		resultTable_str[n + 2][7] = Double.toString( Correlation( resultTable_num[7], resultTable_num[11] ) );
		resultTable_str[n + 2][8] = Double.toString( Correlation( resultTable_num[8], resultTable_num[12] ) );
		resultTable_str[n + 2][9] = "";
		resultTable_str[n + 2][10] = "";
		resultTable_str[n + 2][11] = "";
		resultTable_str[n + 2][12] = "";

		writeFile( resultTable_str, "RESULT.csv" );

		System.out.println( "results generated" );
		Map<String, String> map = new HashMap<>();
		map.put( "message", "file write success" );
		return ResponseEntity.ok( map );

	}

	public void writeFile( String[][] table, String fileName )
	{
		try (BufferedWriter writer = new BufferedWriter( new FileWriter( fileName ) ))
		{

			for ( int i = 0; i < table.length; i++ )
			{
				for ( int j = 0; j < table[i].length; j++ )
				{
					String str = table[i][j].trim().isEmpty() ? "_" : table[i][j];
					writer.write( str );
					writer.write( "," );
				}
				writer.write( "\n" );
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * This method loads data for further calculations
	 * Input data - fileName
	 * First column - data (keeps for future)
	 * Fifth column - close price
	 */
	private void loadList( String fileName ) throws IOException
	{
		BufferedReader fileIn = new BufferedReader( new FileReader( fileName ) );
		String line;
		String[] lineSplitter = { "0000-00-00" };
		fileIn.readLine();
		while ( ( line = fileIn.readLine() ) != null )
		{
			lineSplitter = line.split( "," );
			date.add( lineSplitter[0] );
			closePrice.add( Double.parseDouble( lineSplitter[4] ) );
		}
		endDate = lineSplitter[0];
		fileIn.readLine();
		fileIn.close();

	}

	/**
	 * Input data
	 * y - number of years
	 * norm - normalized values
	 * Output data - iVar integral
	 * Dimension:
	 * If normalized = false
	 * Unit of price multiplied by Unit of Time
	 * If normalized = true
	 * Unit of Time
	 * Unit of price - dimension of price in input data
	 * Unit of time - one tick in input data (no filtering for weekend implemented)
	 *
	 * @throws NumberFormatException
	 */
	public double iVarCalculate( int y, boolean norm )
	{
		double iVarIntegral = 0;
		double peakPrice = -1e50;
		firstPrice = 0;
		lastPrice = 0;
		int endDateCode = dateCode( endDate );
		if ( endDateCode - dateCode( date.get( 0 ) ) < y * 10000 - 1 )
		{
			return 0.0 / 0.0;
		}
		for ( int i = 0; i < date.size(); i++ )
		{
			int dateCode = dateCode( date.get( i ) );
			if ( endDateCode - dateCode >= y * 10000 )
			{
				continue;
			}
			if ( firstPrice <= 0 )
				firstPrice = closePrice.get( i );

			peakPrice = Math.max( peakPrice, closePrice.get( i ) );
			if ( norm )
			{
				iVarIntegral = iVarIntegral + ( peakPrice - closePrice.get( i ) ) / peakPrice;
			}
			else
			{
				iVarIntegral = iVarIntegral + peakPrice - closePrice.get( i );
			}

		}
		lastPrice = closePrice.get( closePrice.size() - 1 );

		return Math.round( iVarIntegral * 10000.0 ) / 10000.0;
	}

	/**
	 * Convert date to number for comparison
	 */
	private int dateCode( String dateLine )
	{
		int Year = Integer.parseInt( dateLine.substring( 0, 4 ) );
		int Month = Integer.parseInt( dateLine.substring( 5, 7 ) );
		int Day = Integer.parseInt( dateLine.substring( 8, 10 ) );
		return 10000 * Year + 100 * Month + Day;
	}

	/**
	 * Standard correlation function
	 * xs, ys - arrays of data for correlation
	 * output - correlation coefficient
	 */
	public static double Correlation( double[] xs, double[] ys )
	{

		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;

		int nn = xs.length;
		int n = 0;

		for ( int i = 0; i < nn; ++i )
		{
			double x = xs[i];
			double y = ys[i];

			if ( ( x == x && y == y ) )
			{
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
		double sigmax = Math.sqrt( sxx / n - sx * sx / n / n );
		// standard error of y
		double sigmay = Math.sqrt( syy / n - sy * sy / n / n );

		// correlation is just a normalized covariation
		return cov / sigmax / sigmay;
	}

	@GetMapping("saveResult")
	public String saveResult( Model model )
	{
		Map<String, String> map = new HashMap<>();
		try
		{
			File file = ResourceUtils.getFile( "RESULT.csv" );
			BufferedReader fileIn = new BufferedReader( new FileReader( file ) );
			String line;
			line = fileIn.readLine(); // read and discard header
			List<Result> results = new LinkedList<>();
			while ( ( line = fileIn.readLine() ) != null )
			{
				String[] data = line.split( "," );
				//				if ( !data[0].trim().isEmpty() && data[0].trim().equalsIgnoreCase( "Symbol" ) )
				//				{
				//					break;
				//				}
				if ( data.length < 13 )
				{
					String[] dataNew = new String[13];
					for ( int i = 0; i < data.length; i++ )
					{
						dataNew[i] = data[i];
					}
					for ( int i = data.length; i < 13; i++ )
					{
						dataNew[i] = "";
					}
					data = dataNew;
				}
				Result result = new Result( data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12] );
				System.out.println( data[0] );
				results.add( result );
			}
			this.resultRepository.deleteAll();
			this.resultRepository.saveAll( results );
		}
		catch ( IOException e )
		{
			map.put( "status", "-1" );
			map.put( "message", "error" );
			return "index";
		}

		map.put( "status", "1" );
		map.put( "message", "success" );
		model.addAllAttributes( map );
		return "index";
	}

	@GetMapping("viewIvar")
	public String viewIvar( Model model )
	{
		List<Result> all = this.resultRepository.findAll();
		model.addAttribute( "result", all );
		return "index";
	}
}
