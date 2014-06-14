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
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
	
	/**
	 * Kontener przechowuj¹cy historiê kodów promocyjnych. 
	 * Kody, które zosta³y ju¿ wys³ane u¿ytkownikowi.
	 */
	private ArrayList<String> historyCodes = new ArrayList<String>();

	private LocationManager locationManager;
	private LocationListener locationListener;
	
	/**
	 * Flaga, który mówa czy GPS jest w³¹czony na telefonie komórkowym
	 * u¿ytkownika.
	 */
	private boolean isGPSEnabled;

	/**
	 * Aktualna pozycja (szerokoœæ geograficzna) u¿ytkownika.
	 */
	protected static Double longitude;
	
	/**
	 * Aktualna pozycja (d³ugoœæ geograficzna) u¿ytkownika.
	 */
	protected static Double latitude;
	
	/**
	 * Grupa przycisków typu RadioButton. Pozwala u¿ytkownikowi
	 * wybraæ w jaki sposób otrzymywaæ powiadomienia o kodach promocyjnych.
	 * Do wyboru jest notyfikacja USSD lub SMS.
	 */
	private RadioGroup radioGroup1;
	private RadioButton radioUssd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		readPromoFile();
		readHistoryFile();
		
		startLocListener();
		
	}
	
	/**
	 * Metoda wczytuj¹ kody promocyjne z ich lokalizacj¹ z pliku
	 * tekstowego promo.txt. I nastêpnie dodaje do odpowiednich kontenerów.
	 */
	private void readPromoFile() {
		promo = new File(Environment.getExternalStorageDirectory(), filename);
		
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
			Toast.makeText(getApplicationContext(), "Plik zosta³ przeczytany", Toast.LENGTH_SHORT).show();;
			reader.close();
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e);
		}
	}
	
	private void readHistoryFile() {
		history = new File(Environment.getExternalStorageDirectory(), filename1);
		
		try {
			InputStream streamIn = new FileInputStream(history);
			BufferedReader reader = new BufferedReader(new InputStreamReader(streamIn));
			
			String line = reader.readLine();
			while(line != null){
				String[] splittedLine = line.split("-");
				if (splittedLine.length == 3) {
					historyCodes.add(splittedLine[0]);
				} else {
					showMessage("B³¹d!", "SprawdŸ poprawnoœæ pliku z histori¹ kodów promocyjnych.");
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e);
		}
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
	
	public void testUssdOnClick(View view) {
		
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		Integer selectedId = radioGroup1.getCheckedRadioButtonId();
		
		radioUssd = (RadioButton) findViewById(R.id.radioButton1);
		if (selectedId == radioUssd.getId()) {
			new SendUSSD().execute(promoCodes.get(4));
		} else {
			new SendSMS().execute(promoCodes.get(4));
		}
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
		        	//Toast.makeText(getApplicationContext(), "GPS ON", Toast.LENGTH_SHORT).show();
		        		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
		        				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
		        		longitude = location.getLongitude();
		                latitude = location.getLatitude();
		                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		        } else {
		                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		                if (location != null) {
		                	//Toast.makeText(getApplicationContext(), "NETWORK ON", Toast.LENGTH_SHORT).show();
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
		
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		Integer selectedId = radioGroup1.getCheckedRadioButtonId();
		radioUssd = (RadioButton) findViewById(R.id.radioButton1);
		
		for (int i = 0; i < latitudes.size(); i++) {
			if (latitudes.get(i) <= lat+0.0002 && latitudes.get(i) >= lat-0.0002) {
				if (longitudes.get(i) <= lon+0.0002 && longitudes.get(i) >= lon-0.0002) {
					String msg = promoCodes.get(i);
					try {
						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
					}
					
					if (selectedId == radioUssd.getId()) {
						new SendUSSD().execute(msg);
					} else {
						new SendSMS().execute(msg);
					}
					try {
						if (!historyCodes.contains(promoCodes.get(i))) {
							OutputStream streamOut = new FileOutputStream(history, true);
							String historyLine = promoCodes.get(i)+"-"+latitudes.get(i)+"-"+longitudes.get(i)+"\n";
							streamOut.write(historyLine.getBytes());
							streamOut.close();
						} else {
							Toast.makeText(getApplicationContext(), "Dany kod promocyjny ju¿ istnieje.", Toast.LENGTH_SHORT).show();
						}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
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
