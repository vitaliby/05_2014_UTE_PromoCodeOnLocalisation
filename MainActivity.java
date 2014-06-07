package com.ute.promoapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * G³owna klasa aplikacji uruchamiajaca w¹tek œledzenie
 * lokalizacji u¿ytkownika, oraz sprawdzaj¹ca czy u¿ytkownik
 * znajduje siê w strefie kodu promocyjnego.
 * Równie¿ tworzy menu z przyciskami, które umozliwiaj¹ przejœæ do przegl¹dania
 * historii lub uruchomiæ mapê z oznaczonymi kodami promocyjnymi.
 */
public class MainActivity extends Activity {
	
	/**
	 * Minimalny dystans dla którego aplikacji rejestruje zmianê
	 * lokalizacji u¿ytkownika.
	 */
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 4;

	/**
	 * Minimalny okres sprawdzanie lokalizacji u¿ytkownika.
	 */
	private static final long MIN_TIME_BW_UPDATES = 1000 * 4;
	
	private Button startButton;
	
	private boolean isStarted = false;
	
	/**
	 * Plik z zapisanymi kodami promocyjnymi i ich lokalizacj¹.
	 */
	private File promo = null;
	private String filename = "PROMO.txt";
	
	/**
	 * Plik do zapisywania historii kodów promocyjnych u¿ytkownika.
	 */
	private File history = null;
	private String filename1 = "history.txt";
	
	/**
	 * Kontener przechowuj¹cy kody promocyjne.
	 */
	private ArrayList<String> promoCodes = new ArrayList<String>();
	
	/**
	 * Kontener przechowuj¹cy d³ugoœæ geograficzn¹ strefy w której
	 * znajduje siê kod promocyjny.
	 */
	private ArrayList<Double> latitudes = new ArrayList<Double>();
	
	/**
	 * Kontener przechowuj¹cy szerokoœæ geograficzn¹ strefy w której
	 * znajduje siê kod promocyjny.
	 */
	private ArrayList<Double> longitudes = new ArrayList<Double>();

	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean isGPSEnabled;

	/**
	 * Aktualna pozycja (szerokoœæ geograficzna) u¿ytkownika.
	 */
	protected static Double longitude;
	
	/**
	 * Aktualna pozycja (d³ugoœæ geograficzna) u¿ytkownika.
	 */
	protected static Double latitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		readPromoFile();
		
		startLocListener();
		
	}
	
	/**
	 * Metoda wczytuj¹ kody promocyjne z ich lokalizacj¹ z pliku
	 * tekstowego promo.txt. I nastêpnie dodaje do odpowiednich kontenerów.
	 */
	private void readPromoFile() {
		promo = new File(Environment.getExternalStorageDirectory(), filename);
		history = new File(Environment.getExternalStorageDirectory(), filename1);
		try {
			InputStream streamIn = new FileInputStream(promo);
			BufferedReader reader = new BufferedReader(new InputStreamReader(streamIn));
			
			String line = reader.readLine();
			while(line != null){
				String[] splittedLine = line.split("-");
				if (splittedLine.length == 3) {
					promoCodes.add(splittedLine[0]);
					latitudes.add(Double.parseDouble(splittedLine[1].substring(0,7)));
					longitudes.add(Double.parseDouble(splittedLine[2].substring(0,7)));
				} else {
					showMessage("B³¹d!", "SprawdŸ poprawnoœæ pliku z kodami promocji.");
				}
				line = reader.readLine();
	        } 
			reader.close();
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e);
		}
	}
	
	public void startOnClick(View view) {
		//startButton = (Button) this.findViewById(R.id.button3);
		
		sendUSSD("TEST!");
		/*if (!isStarted) {
			isStarted = true;
			if (startButton != null) {
				startButton.setText("Stop application");
				startButton.setBackgroundColor(-65536);
			}
			
			
		} else {
			if (startButton != null) {
				startButton.setText("Start application");
				startButton.setBackgroundColor(Color.LTGRAY);
			}
			locationManager.removeUpdates(locationListener);
			isStarted = false;
		}*/
	}
	
	/**
	 * Metoda obs³uguj¹ca przycisk "Zobacz historiê".
	 * Wyœwietla historiê kodów promocyjnych.
	 * @param view
	 */
	public void historyOnClick(View view) {
		Intent intent = new Intent(this, HstoryActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Metoda obs³uguj¹ca przycisk "Zobacz na mapie".
	 * Uruchamia mapê w aplikacji.
	 * @param view
	 */
	public void mapOnClick(View view) {
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Metoda, która œledzi lokalizacjê u¿ytkownika.
	 * Je¿eli lokalizacja u¿ytkownika zosta³a zmieniona jest wywo³ywana metoda
	 * sprawdzaj¹ca, czy u¿ytkownik znajduje w strefie kodu promozyjnego.
	 */
	private void startLocListener() {
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
		                longitude = location.getLongitude();
		                latitude = location.getLatitude();
		                checkIsInPromoLocalisation(latitude, longitude);
		                
		        }

		        public void onStatusChanged(String provider, int status, Bundle extras) {
		        }

		        public void onProviderEnabled(String provider) {
		        }

		        public void onProviderDisabled(String provider) {
		        }
		};

		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {

				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
						MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
		        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		        if (location != null) {
		        	Toast.makeText(getApplicationContext(), "GPS ON", Toast.LENGTH_SHORT).show();
		        		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
		        				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
		        		longitude = location.getLongitude();
		                latitude = location.getLatitude();
		                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		        } else {
		                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		                if (location != null) {
		                	Toast.makeText(getApplicationContext(), "NETWORK ON", Toast.LENGTH_SHORT).show();
		                		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
		                				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
		                		longitude = location.getLongitude();
				                latitude = location.getLatitude();
		                        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		                } else {
		                        longitude = 0.00;
		                        latitude = 0.00;
		                }
		        }
		}
	}
	
	/**
	 * Metoda sprawdzaj¹ca czy w aktualnej lokalizacji u¿ytkownika nie znajduje siê 
	 * obiekt z kodem promocyjnym.
	 * @param latitude - aktualne po³o¿enie u¿ytkownika (latitude)
	 * @param longitude - aktualne po³o¿enie u¿ytkownika (longitude)
	 */
	public void checkIsInPromoLocalisation(Double _latitude, Double _longitude) {
		Double lat = _latitude;
		Double lon = _longitude;
		
		for (int i = 0; i < latitudes.size(); i++) {
			if (latitudes.get(i) <= lat+0.0003 && latitudes.get(i) >= lat-0.0003) {
				if (longitudes.get(i) <= lon+0.0003 && longitudes.get(i) >= lon-0.0003) {
					showMessage("Kod promocyjny", "Kod: "+promoCodes.get(i)+"\n Lat: "+lat+"\n Lng: "+lon+"\n");
					try {
						OutputStream streamOut = new FileOutputStream(history, true);
						String historyLine = promoCodes.get(i)+"-"+latitudes.get(i)+"-"+longitudes.get(i)+"\n";
						streamOut.write(historyLine.getBytes());
						streamOut.close();
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "B³¹d w zapisywaniu historii.", Toast.LENGTH_SHORT).show();
					}
					promoCodes.remove(i);
					latitudes.remove(i);
					longitudes.remove(i);
				} else {
					//Toast.makeText(getApplicationContext(), "Lat ok. Long bad.", Toast.LENGTH_SHORT).show();
				}
			} else {
				//Toast.makeText(getApplicationContext(), "Bad.", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * Metoda wyœwietlaj¹ca u¿ytkownikowi wiadomoœæ podczas dzia³ania plikacji
	 * @param title - tytó³ wiadomoœci
	 * @param text - tekst wiadomoœci
	 */
	private void showMessage(String title, String text) {
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(text);
		alertDialog.show();
	}
	
	public void sendUSSD(String msg){
        
		String to = "789101781";
	   	String adres = "https://api2.orange.pl/sendussd/?to="+to+"&msg="+msg ;
	   	try {    	 
	   		System.out.println("!!!!!1");
	      	  HttpHost targetHost = new HttpHost("api2.orange.pl", 443, "https"); 
	      	System.out.println("!!!!!2");
	      	  //HttpClient client = new DefaultHttpClient();  
	      	  DefaultHttpClient client = new DefaultHttpClient();  
	      	  client.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials("48789101781", "TSS7C347ENHMLE"));
	      	System.out.println("!!!!!3");
	      	  
	      	 // HttpClient httpclient = sslClient(client);
	      	 sslClient(client);  // ustaw certyfikacj¹ SSL dla danego klienta
	      	 
	      	System.out.println("!!!!!4");
	      	 //Log.i("USSD","przed wys³aniem");
	      	 HttpGet httpget = new HttpGet(adres); 
	      	System.out.println("!!!!!5");
	      	 //Log.i("USSD","po wys³aniem");
	      	 HttpResponse response = client.execute( httpget);
	      	System.out.println("!!!!!6");
	        //Log.i("USSD",response.getStatusLine().toString());
	       	 

	   	}catch (IllegalStateException e) {
	   		//Log.e("LOCATION", "IllegalStateException: "+e );
	   		System.out.println("!!!!!"+e);
	   	/*}catch (SAXException e) {
	   		Log.e("LOCATION", "SAXException: "+e );*/
	   	}catch (ClientProtocolException e) {
	   		//Log.e("LOCATION", "ClientProtocolException: "+e );
	   		System.out.println("!!!!!"+e);
	   	}catch (IOException e) {
	   		//Log.e("LOCATION", "IOException: "+e );
	   		System.out.println("!!!!!"+e);
	   	}catch (Exception e) {
	   		System.out.println("!!!!?"+e);
	   	}
	}
	
	private void sslClient(HttpClient client) {
        try {
            X509TrustManager tm = new X509TrustManager() { 
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            MySSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
          //  return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception ex) {
           // return null;
        }
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
