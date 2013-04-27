package bu.cs683.ratemycourses.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bu.cs683.ratemycourses.JSONHelper;
import bu.cs683.ratemycourses.listview.CourseList;

import com.example.ratemycourses.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CourseView extends Activity {
	
	private ProgressDialog pDialog;
	
	private String courseId;
	
	JSONHelper jHelper = new JSONHelper();
	
	private static String url_get_course_details = "http://eleven.luporz.com/ratemycourses/get_course_details.php";
	
	// json nodes
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_COURSE = "COURSE";
	private static final String TAG_COURSEID = "COURSEID";
	private static final String TAG_COURSECODE = "COURSECODE";
	private static final String TAG_COURSENAME = "COURSENAME";
	private static final String TAG_INSTRUCTOR = "INSTRUCTOR";
	private static final String TAG_PROGRAM_CODE = "PROGRAM_CODE";
	
	// depts JSONArray
	JSONObject courseDetails = null;
	
	// widgets
	TextView courseCode;
	TextView courseName;
	TextView instructor;
	TextView programCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_view);
		
		courseCode = (TextView) findViewById(R.id.course_code_label);
		courseName = (TextView) findViewById(R.id.course_name_label);
		instructor = (TextView) findViewById(R.id.prof_name_label);
		programCode = (TextView) findViewById(R.id.program_name_label);
		
		// getting courseid from intent
		Intent i = getIntent();
		courseId = i.getStringExtra(TAG_COURSEID);
		
		// implement Rate this Course button
		rateThisCourse();
		
		// implement Tell a Friend button
		tellFriend();
		
		// load details of a course
		new GetCourseDetails().execute();
	}

	private void rateThisCourse() {
		Button rateBtn;
		
		rateBtn = (Button) findViewById(R.id.rate_course_bt);
		rateBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CourseRatingView.class);
				startActivity(i);
			}
		});
	}
	
	private void tellFriend() {
		Button tellFBtn;
		
		tellFBtn = (Button) findViewById(R.id.tell_friend_bt);
		tellFBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CourseRatingView.class);
				startActivity(i);
			}
		});
	}
	
	/**
	 * Background Async Task to get course details
	 */
	class GetCourseDetails extends AsyncTask<String, String, String> {
		
		/**
		 * Show progress dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CourseView.this);
			pDialog.setMessage("Loading course detail, please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		/**
		 * Getting details for a course in background thread
		 */
		protected String doInBackground(String... args) {
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			params.add(new BasicNameValuePair("courseid", courseId));
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_get_course_details, "GET", params);
			
			// check your log cat for JSON response
			Log.d("Course details: ", json.toString());
			
			try {
				// checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// course details found
					// getting array of details
					courseDetails = json.getJSONObject(TAG_COURSE);
					
					courseCode.setText(courseDetails.getString(TAG_COURSECODE));
					courseName.setText(courseDetails.getString(TAG_COURSENAME));
					instructor.setText(courseDetails.getString(TAG_INSTRUCTOR));
					programCode.setText(courseDetails.getString(TAG_PROGRAM_CODE));
					System.out.println(courseDetails.getString(TAG_COURSECODE));
					
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
			// dismiss the dialog after getting all details
			pDialog.dismiss();
		}
	}

}
