package com.ute.promoapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
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
import android.widget.Toast;

/**
 * G�owna klasa aplikacji uruchamiajaca w�tek �ledzenie
 * lokalizacji u�ytkownika, oraz sprawdzaj�ca czy u�ytkownik
 * znajduje si� w strefie kodu promocyjnego.
 * R�wnie� tworzy menu z przyciskami, kt�re umozliwiaj� przej�� do przegl�dania
 * historii lub uruchomi� map� z oznaczonymi kodami promocyjnymi.
 */
public class MainActivity extends Activity {
	
	/**
	 * Minimalny dystans dla kt�rego aplikacji rejestruje zmian�
	 * lokalizacji u�ytkownika.
	 */
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 4;

	/**
	 * Minimalny okres sprawdzanie lokalizacji u�ytkownika.
	 */
	private static final long MIN_TIME_BW_UPDATES = 1000 * 4;
	
	/**
	 * Plik z zapisanymi kodami promocyjnymi i ich lokalizacj�.
	 */
	private File promo = null;
	private String filename = "PROMO.txt";
	
	/**
	 * Plik do zapisywania historii kod�w promocyjnych u�ytkownika.
	 */
	private File history = null;
	private String filename1 = "history.txt";
	
	/**
	 * Kontener przechowuj�cy kody promocyjne.
	 */
	private ArrayList<String> promoCodes = new ArrayList<String>();
	
	/**
	 * Kontener przechowuj�cy d�ugo�� geograficzn� strefy w kt�rej
	 * znajduje si� kod promocyjny.
	 */
	private ArrayList<Double> latitudes = new ArrayList<Double>();
	
	/**
	 * Kontener przechowuj�cy szeroko�� geograficzn� strefy w kt�rej
	 * znajduje si� kod promocyjny.
	 */
	private ArrayList<Double> longitudes = new ArrayList<Double>();

	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean isGPSEnabled;

	/**
	 * Aktualna pozycja (szeroko�� geograficzna) u�ytkownika.
	 */
	protected static Double longitude;
	
	/**
	 * Aktualna pozycja (d�ugo�� geograficzna) u�ytkownika.
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
	 * Metoda wczytuj� kody promocyjne z ich lokalizacj� z pliku
	 * tekstowego promo.txt. I nast�pnie dodaje do odpowiednich kontener�w.
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
					showMessage("B��d!", "Sprawd� poprawno�� pliku z kodami promocji.");
				}
				line = reader.readLine();
	        } 
			reader.close();
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e);
		}
	}
	
	/**
	 * Metoda obs�uguj�ca przycisk "Zobacz histori�".
	 * Wy�wietla histori� kod�w promocyjnych.
	 * @param view
	 */
	public void historyOnClick(View view) {
		Intent intent = new Intent(this, HstoryActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Metoda obs�uguj�ca przycisk "Zobacz na mapie".
	 * Uruchamia map� w aplikacji.
	 * @param view
	 */
	public void mapOnClick(View view) {
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Metoda, kt�ra �ledzi lokalizacj� u�ytkownika.
	 * Je�eli lokalizacja u�ytkownika zosta�a zmieniona jest wywo�ywana metoda
	 * sprawdzaj�ca, czy u�ytkownik znajduje w strefie kodu promozyjnego.
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
	 * Metoda sprawdzaj�ca czy w aktualnej lokalizacji u�ytkownika nie znajduje si� 
	 * obiekt z kodem promocyjnym.
	 * @param latitude - aktualne po�o�enie u�ytkownika (latitude)
	 * @param longitude - aktualne po�o�enie u�ytkownika (longitude)
	 */
	public void checkIsInPromoLocalisation(Double _latitude, Double _longitude) {
		Double lat = _latitude;
		Double lon = _longitude;
		
		for (int i = 0; i < latitudes.size(); i++) {
			if (latitudes.get(i) <= lat+0.0002 && latitudes.get(i) >= lat-0.0002) {
				if (longitudes.get(i) <= lon+0.0002 && longitudes.get(i) >= lon-0.0002) {
					new SendUSSD().execute("Kod promocyjny\n Kod: "+promoCodes.get(i)+"\n");
					//showMessage("Kod promocyjny", "Kod: "+promoCodes.get(i)+"\n Lat: "+lat+"\n Lng: "+lon+"\n");
					Toast.makeText(getApplicationContext(), 
							"Kod promocyjny\n Kod: "+promoCodes.get(i)+"\n Lat: "+lat+"\n Lng: "+lon+"\n", 
							Toast.LENGTH_SHORT);
					try {
						OutputStream streamOut = new FileOutputStream(history, true);
						String historyLine = promoCodes.get(i)+"-"+latitudes.get(i)+"-"+longitudes.get(i)+"\n";
						streamOut.write(historyLine.getBytes());
						streamOut.close();
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "B��d w zapisywaniu historii.", Toast.LENGTH_SHORT).show();
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
	 * Metoda wy�wietlaj�ca u�ytkownikowi wiadomo�� podczas dzia�ania plikacji
	 * @param title - tyt� wiadomo�ci
	 * @param text - tekst wiadomo�ci
	 */
	private void showMessage(String title, String text) {
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(text);
		alertDialog.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
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
