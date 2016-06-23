# REST-API-Testing

A sample project where the Google Maps API can be used to get details for any address in a JSON format. This response is extracted and tested it for latitude and longitude correctness.

For input data, the external data provider is the DataToTest.properties file where address information such as the following can be entered:

https://maps.googleapis.com/maps/api/geocode/json?address=3111%20mission%20college%20blvd%20santa%20clara%20ca%2095054
 
This information is then passed into the LongitudeLatitudeTest.java. AddressData.xls can contain various addresses and their latitude and longitude information. 
 
Based on the data provided in the properties file, information such as address, longitude and latitude is extracted from the JSON response rendered from the Google Maps API.This extracted information is then compared with information in the AddressData.xls file. 
 
A mapping of addresses and their latitudes and longitudes is maintained by using a Map where the Address is the key and the LatitudeLongitudeBean(with latitude and longitude as its attributes) is the value.
 
Here are some scenarios that were tested:
 
   a) Latitude, longitude and address to test should not be null.
 
   b) Latitude should be between -90 and 90.
 
   c) Longitude should be between -180 and 180.
 
   d) Longitude and latitude should be valid doubles and should not have commas, characters and other symbols.
