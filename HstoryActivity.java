package com.ute.promoapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Klasa odpowiadaj�ca za wy�wietlanie historii kod�w promocyjnych.
 * U�ytkownik klikaj�c na przycisk "Zobacz histori�" widzi list� 
 * wszystkich kod�w promocyjnych wraz z lokalizacj�.
 *
 */
public class HstoryActivity extends Activity {
	
	/**
	 * Lista z histori� kod�w promocyjnych, kt�ra jest wy�wietlana u�ytkownikowi
	 */
	private ListView list;
	/**
	 * Kontener przechowuj�cy histori� kod�w promocyjnych 
	 * wczytanych z pliku history.txt
	 */
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	
	/**
	 * Plik przechowuj�cy kody promocyjne
	 */
	private File history = null;
	private String filename1 = "history.txt";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hstory);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}
	
	/**
	 * Metoda dynamicznie dodaj�ca kody promocyjne do listy,
	 * kt�ra jest wy�wietlana u�ytkownikowi.
	 */
	public void addItemToListView(String mItem) {
		listItems.add(mItem);
		adapter.notifyDataSetChanged();
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_hstory,
					container, false);
			
			list = (ListView) rootView.findViewById(R.id.listView1);
			adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listItems) {
				/**
				 * Metoda zmieniaj�ca kolor tekstu v listView.
				 */
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
				    View view = super.getView(position, convertView, parent);
				    TextView text = (TextView) view.findViewById(android.R.id.text1);
				    text.setTextColor(Color.BLACK); 
				    return view;
				  }
			};
			list.setAdapter(adapter);
			
			readAndAddHistory();
			
			return rootView;
		}
		
		/**
		 * Metoda wczytuj�ca histori� kod�w promocyjnych z pliku history.txt i
		 * i dodaje do listy, kt�ra jest wy�wietlana u�ytkownikowi.
		 */
		private void readAndAddHistory() {
			history = new File(Environment.getExternalStorageDirectory(), filename1);
			
			try {
				InputStream streamIn = new FileInputStream(history);
				BufferedReader reader = new BufferedReader(new InputStreamReader(streamIn));
				
				String line = reader.readLine();
				while(line != null){
					String[] splittedLine = line.split("-");
					String beatyLine = "Kod: "+splittedLine[0]+", ["+splittedLine[1]+"; "+splittedLine[2]+"]\n";
					addItemToListView(beatyLine);
					line = reader.readLine();
		        } 
				reader.close();
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println(e);
			}
		}
	}

}
