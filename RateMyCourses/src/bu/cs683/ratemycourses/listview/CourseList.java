package bu.cs683.ratemycourses.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bu.cs683.ratemycourses.JSONHelper;
import bu.cs683.ratemycourses.course.CourseView;

import com.example.ratemycourses.R;

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

public class CourseList extends ListActivity {
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	private String proCode;
	
	// Creating JSON Parser object
	JSONHelper jHelper = new JSONHelper();
	
	private ArrayList<HashMap<String, String>> coursesList = new ArrayList<HashMap<String, String>>();
	
	// url to get all courses list, use 10.0.2.2 instead of localhost
	private static String url_all_courses = "http://eleven.luporz.com/ratemycourses/get_courses.php";
	
	// JSON node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_COURSES = "COURSES";
	private static final String TAG_COURSEID = "COURSEID";
	private static final String TAG_COURSENAME = "COURSENAME";
	private static final String TAG_PROCODE = "PROCODE";
	//private static final String TAG_DEPTCODE = "DEPTCODE";
	
	// courses JSONArray
	JSONArray courses = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// getting dept details from intent
		Intent i = getIntent();
		
		// getting dept code from intent
		proCode = i.getStringExtra(TAG_PROCODE);
		
		// loading courses in Background Thread
		new LoadAllCourses().execute();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	    //Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
		
		// getting values from selected ListItem
		String courseId = ((TextView) v.findViewById(R.id.courseid)).getText().toString();
		
		// Starting new intent
		Intent i = new Intent(getApplicationContext(), CourseView.class);
		
		// Sending deptCode to next activity
		i.putExtra(TAG_COURSEID, courseId);
		startActivity(i);
	}
	
	/**
	 * Background Async Task to load all course by making HTTP request
	 */
	class LoadAllCourses extends AsyncTask<String, String, String> {
		
		/**
		 * Before starting background thread show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CourseList.this);
			pDialog.setMessage("Loading courses. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		/**
		 * getting all courses from url
		 */
		protected String doInBackground(String... args) {
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			params.add(new BasicNameValuePair("PROGRAM_CODE", proCode));
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_all_courses, "GET", params);
			
			// check your log cat for JSON response
			Log.d("All Courses: ", json.toString());
			
			try {
				// checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// courses found
					// getting array of courses
					courses = json.getJSONArray(TAG_COURSES);
					
					// looping through all courses
					for (int i=0; i<courses.length(); i++) {
						JSONObject c = courses.getJSONObject(i);
						
						// stroing each json item in variable
						String courseid = c.getString(TAG_COURSEID);
						String coursename = c.getString(TAG_COURSENAME);
						
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						
						// adding each child node to HashMap key => value
						map.put(TAG_COURSEID, courseid);
						map.put(TAG_COURSENAME, coursename);
						
						// adding HashList to ArrayList
						coursesList.add(map);
					}
				} else {
					// no courses found
					// prompt a "courses not found" message
					Toast toast = Toast.makeText(getApplicationContext(), "No courses found", Toast.LENGTH_SHORT);
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
							CourseList.this, coursesList,
							R.layout.course_list, new String[] {TAG_COURSEID, TAG_COURSENAME},
							new int[] { R.id.courseid, R.id.coursename });
					// updating listview
					setListAdapter(adapter);
				}
			});
		}
	}

}
