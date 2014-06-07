package com.ute.promoapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Klasa odpowiadaj¹ca za uruchomienie i wyœwietlenie mapy Google.
 * Klasa wczytuje kody promocyjne z ich lokalizacj¹ z pliku
 * historii history.txt i nastêpnie rozmieszcza punkty na mapie. 
 *
 */
public class MapActivity extends Activity {
	
	/**
	 * Obiekt mapy Google wykorzystywany w aplikacji.
	 */
	private GoogleMap googleMap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class PlaceholderFragment extends Fragment {

		private LatLng WARSAW = new LatLng(52.218032, 20.985639);
		
		private File history = null;
		private String filename1 = "history.txt";
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_map, container,
					false);
			
			try {
				initilizeMap();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			setStartPosition();
				
			addMarkersAll();
			
			return rootView;
		}

		/**
		 * Ustawia startow¹ pozycjê na mapie. Je¿eli jest mo¿liwoœæ pobraæ aktualn¹
		 * pozycjê u¿ytkownika, to wykorzystujê te koordynaty. W przeciwnym przypadku
		 * pozycj¹ startow¹ jest centrum Warszawy.
		 */
		private void setStartPosition() {
			if (MainActivity.latitude != null && MainActivity.longitude != null) {
				Double lat = MainActivity.latitude;
				Double lng = MainActivity.longitude;
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14));
			} else {
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WARSAW, 14));
			}
		}
		
		/**
		 * Wczytuje z pliku history.txt historiê kodów promocyjnych u¿ytkownika i
		 * nastêpnie dodaje ich na mapê Google.
		 */
		private void addMarkersAll() {		
			history = new File(Environment.getExternalStorageDirectory(), filename1);
			try {
				InputStream streamIn = new FileInputStream(history);
				BufferedReader reader = new BufferedReader(new InputStreamReader(streamIn));
				
				String line = reader.readLine();
				while(line != null){
					String[] splittedLine = line.split("-");
					if (splittedLine.length == 3) {
						MarkerOptions marker = new MarkerOptions().position(new LatLng(
								Double.parseDouble(splittedLine[1]), Double.parseDouble(splittedLine[2])))
								.title(splittedLine[0]);
						marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
						googleMap.addMarker(marker);
					} else {
						
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
		 * Metoda uruchamiaj¹ca mapê.
		 */
		private void initilizeMap() {
			
			if (googleMap == null) {
	            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	 
	            if (googleMap == null) {
	                Toast.makeText(getApplicationContext(),
	                        "B³¹d!. Nie mo¿na stworzyæ mapê.", Toast.LENGTH_SHORT)
	                        .show();
	            }
	        }
			
		}
	}

}
