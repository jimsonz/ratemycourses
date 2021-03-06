package bu.cs683.ratemycourses.listview;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import bu.cs683.ratemycourses.JSONHelper;

import com.example.ratemycourses.R;

public class ProgramList extends ListActivity{
	
	String deptCode;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	// Creating JSON Parser object
	JSONHelper jHelper = new JSONHelper();
	
	private ArrayList<HashMap<String, String>> proList = new ArrayList<HashMap<String, String>>();
	
	// url to get all courses list, use 10.0.2.2 instead of localhost
	private static String url_programs = "http://eleven.luporz.com/ratemycourses/get_programs.php";
	
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
		
		// getting dept details from intent
		Intent i = getIntent();
		
		// getting dept code from intent
		deptCode = i.getStringExtra(TAG_DEPTCODE);
		
		// loading programs in background thread
		new LoadAllPrograms().execute();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		// getting values from selected ListItem
		String proCode = ((TextView) v.findViewById(R.id.programcode)).getText().toString();
		
		// Starting new intent
		Intent i = new Intent(getApplicationContext(), CourseList.class);
		
		// Sending deptCode to next activity
		i.putExtra(TAG_PROCODE, proCode);
		startActivity(i);
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
			params.add(new BasicNameValuePair("DEPTCODE", deptCode));
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_programs, "GET", params);
			
			// check your log cat for JSON response
			Log.d("Programs: ", json.toString());
			
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
			// dismiss the dialog after getting the programs
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
