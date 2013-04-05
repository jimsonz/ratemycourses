package com.example.ratemycourses.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.ratemycourses.R;
import com.example.ratemycourses.listview.DeptList.LoadAllDepts;
import com.example.ratemycourses.service.JSONHelper;

public class ProgramList extends ListActivity{
	
	String deptCode;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	// Creating JSON Parser object
	JSONHelper jHelper = new JSONHelper();
	
	ArrayList<HashMap<String, String>> proList;
	
	// url to get all courses list, use 10.0.2.2 instead of localhost
	private static String url_all_pro = "http://eleven.luporz.com/ratemycourses/get_all_programs.php";
	
	// JSON node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PROGRAM = "PROGRAM";
	private static final String TAG_PROCODE = "PROCODE";
	private static final String TAG_PRONAME = "PRONAME";
	private static final String TAG_DEPTCODE = "DEPTCODE";
	
	// courses JSONArray
	JSONArray programs = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		
		// getting dept details from intent
		Intent i = getIntent();
		
		// getting dept code from intent
		deptCode = i.getStringExtra(TAG_DEPTCODE);
		
		// hashmap for ListView
		proList = new ArrayList<HashMap<String, String>>();
		
		// loading programs in background thread
		new LoadAllPrograms().execute();
		
		// get listview
		ListView lv = getListView();
		
		// on selecting single program
		// launch the course view
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(), "testing selectable " + position, Toast.LENGTH_LONG).show();
				/*
				// getting values from selected ListItem
				String deptCode = ((TextView) view.findViewById(R.id.deptcode)).getText().toString();
				
				// Starting new intent
				Intent i = new Intent(getApplicationContext(), ProgramList.class);
				
				// Sending deptCode to next activity
				i.putExtra(TAG_DEPTCODE, deptCode);
				startActivity(i);*/
			}
		});
	}
	
	class LoadAllPrograms extends AsyncTask<String, String, String> {
		
		/**
		 * Before starting background thread show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ProgramList.this);
			pDialog.setMessage("Loading programs. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		/**
		 * getting all programs from url
		 */
		protected String doInBackground(String... args) {
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			params.add(new BasicNameValuePair("DEPTCODE", "MET"));
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_all_pro, "GET", params);
			
			// check your log cat for JSON response
			Log.d("All Programs: ", json.toString());
			
			try {
				// checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// courses found
					// getting array of programs
					programs = json.getJSONArray(TAG_PROGRAM);
					
					// looping through all courses
					for (int i=0; i<programs.length(); i++) {
						JSONObject c = programs.getJSONObject(i);
						
						// stroing each json item in variable
						String proCode = c.getString(TAG_PROCODE);
						String proName = c.getString(TAG_PRONAME);
						String deptCode = c.getString(TAG_DEPTCODE);
						
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						
						// adding each child node to HashMap key => value
						map.put(TAG_PROCODE, proCode);
						map.put(TAG_PRONAME, proName);
						map.put(TAG_DEPTCODE, deptCode);
						
						// adding HashList to ArrayList
						proList.add(map);
					}
				} else {
					// no program found
					// prompt a "programs not found" message
					Toast toast = Toast.makeText(getApplicationContext(), "No programs found", Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		/**
		 * After completing background task Dismiss the progress dialog
		 */
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 */
					ListAdapter adapter = new SimpleAdapter(
							ProgramList.this, proList,
							R.layout.program_list, new String[] {TAG_PROCODE, TAG_PRONAME},
							new int[] {R.id.programcode, R.id.programname});
					// updating listview
					setListAdapter(adapter);
				}
			});
		}
	}
}
