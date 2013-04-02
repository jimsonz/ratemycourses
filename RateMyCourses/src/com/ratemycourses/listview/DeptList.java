package com.ratemycourses.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ratemycourses.R;
import com.ratemycourses.service.JSONHelper;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DeptList extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;
	
	// Creating JSON Parser object
	JSONHelper jHelper = new JSONHelper();
	
	ArrayList<HashMap<String, String>> deptList;
	
	// url to get all courses list, use 10.0.2.2 instead of localhost
	private static String url_all_dept = "http://10.0.2.2/RateMyCourses/get_all_depts.php";
	
	// JSON node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_DEPT = "DEPT";
	private static final String TAG_DEPTCODE = "DEPTCODE";
	private static final String TAG_DEPTNAME = "DEPTNAME";
	
	// courses JSONArray
	JSONArray depts = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		
		// hashmap for ListView
		deptList = new ArrayList<HashMap<String, String>>();
		
		// loading depts in background thread
		new LoadAllDepts().execute();
		
		// get listview
		ListView lv = getListView();
		
		// on selecting single dept
/*		// launching program list view
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});*/
	}
	
	class LoadAllDepts extends AsyncTask<String, String, String> {
		
		/**
		 * Before starting background thread show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(DeptList.this);
			pDialog.setMessage("Loading departments. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		/**
		 * getting all depts from url
		 */
		protected String doInBackground(String... args) {
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_all_dept, "GET", params);
			
			// check your log cat for JSON response
			Log.d("All Depts: ", json.toString());
			
			try {
				// checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// courses found
					// getting array of courses
					depts = json.getJSONArray(TAG_DEPT);
					
					// looping through all courses
					for (int i=0; i<depts.length(); i++) {
						JSONObject c = depts.getJSONObject(i);
						
						// stroing each json item in variable
						String deptCode = c.getString(TAG_DEPTCODE);
						String deptName = c.getString(TAG_DEPTNAME);
						
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						
						// adding each child node to HashMap key => value
						map.put(TAG_DEPTCODE, deptCode);
						map.put(TAG_DEPTNAME, deptName);
						
						// adding HashList to ArrayList
						deptList.add(map);
					}
				} else {
					// no dept found
					// prompt a "courses not found" message
					Toast toast = Toast.makeText(getApplicationContext(), "No departments found", Toast.LENGTH_SHORT);
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
							DeptList.this, deptList,
							R.layout.list_item, new String[] {TAG_DEPTNAME},
							new int[] {R.id.deptname });
					// updating listview
					setListAdapter(adapter);
				}
			});
		}
	}

}
