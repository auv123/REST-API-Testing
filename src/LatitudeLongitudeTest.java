

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LatitudeLongitudeTest {
	
	private double lat;
	private double lng;
	private String address;
	private Map<String, LatitudeLongitudeBean> map;
	private LatitudeLongitudeBean latLongObj;
	private FileInputStream file;
	private HttpURLConnection conn;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	//Used to validate if latitude and longitude coordinates are valid Doubles. This is based on Oracle JavaDocs for Double
	//and can be found at http://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29
	private static final Pattern DOUBLE_PATTERN = Pattern.compile(
		    "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
		    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
		    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
		    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
	
	@BeforeTest
	public void setUp() throws IOException {
		
		//Get address regarding URI from properties file called DataToTest.properties
		Properties prop = new Properties();
		FileInputStream stream = new FileInputStream("/workspace/REST-API-Testing/src/DataToTest.properties");
		prop.load(stream);
		URL url = new URL(prop.getProperty("addressInformation"));
		
		//Use HTTPClient library to send a GET request to the URI fetched from the properties file
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		//Creating a JSON object based on the GET response
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = br.readLine()) != null) {
			result.append(line);
		}
		JSONObject obj = new JSONObject(result.toString());
		
		//Using the JSON object to fetch the address, latitude and longitude associated with the address
		address = obj.getJSONArray("results").getJSONObject(0).getString("formatted_address");
		lat = obj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		lng = obj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		
		//Reading from an Excel file using Apache POI
		file = new FileInputStream(new File("/workspace/REST-API-Testing/src/AddressData.xls"));
		// Getting a workbook instance for a .xls file
		workbook = new HSSFWorkbook(file);
		// Getting the first sheet from the workbook
		sheet = workbook.getSheetAt(0);
		// Iterating through each row from first sheet
		Iterator<Row> rowIterator = sheet.iterator();
		rowIterator.next();
		
		//Adding all values from Excel file into a map where the Address is a key and the LatitudeLongitudeBean(with latitude and longitude as its 
		//attributes) is a value
		map = new HashMap<String, LatitudeLongitudeBean>();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			String cellAddr = row.getCell(0).getStringCellValue();
			double cellLat = row.getCell(1).getNumericCellValue();
			double cellLng = row.getCell(2).getNumericCellValue();
			latLongObj = new LatitudeLongitudeBean(cellLat, cellLng);
			map.put(cellAddr, latLongObj);
		}
	}
	
	@Test
	public void latitudeLongitudeOfDataToTestNotNullTest() {	
		Assert.assertNotNull(lat);
		Assert.assertNotNull(lng);
	}
	
	@Test
	public void addressOfDataToTestNotNullTest() {	
		Assert.assertNotNull(address);
	}
	
	@Test
	public void latitudeDataToTestBetweenNegative90And90() {	
		Assert.assertTrue(lat >= -90);
		Assert.assertTrue(lat <= 90);
	}
	
	@Test
	public void latitudeDataToTestIsAValidDouble() {	
		boolean match = DOUBLE_PATTERN.matcher(String.valueOf(lat)).matches();
		Assert.assertTrue(match);
	}
	
	@Test
	public void longitudeDataToTestBetweenNegative180And180() {	
		Assert.assertTrue(lng >= -180);
		Assert.assertTrue(lng <= 180);
	}
	
	@Test
	public void longitudeDataToTestIsAValidDouble() {	
		boolean match = DOUBLE_PATTERN.matcher(String.valueOf(lng)).matches();
		Assert.assertTrue(match);
	}
	
	@Test
	public void checkIfLatLngOfURIMatchesLatLngForCorrespondingAddressFromExcel() {	
		Assert.assertEquals(map.get(address).getLatitude(), lat);
		Assert.assertEquals(map.get(address).getLongitude(), lng);
	}
	
	@Test
	public void addressFromExcelNotNullTest() {			
		Assert.assertNotNull(map);
		Assert.assertNotNull(map.get(address).getLatitude());
		Assert.assertNotNull(map.get(address).getLongitude());
	}
	
	@AfterTest
	public void tearDown() throws IOException{
		file.close();
		workbook.close();
		conn.disconnect();
	}
	

}
