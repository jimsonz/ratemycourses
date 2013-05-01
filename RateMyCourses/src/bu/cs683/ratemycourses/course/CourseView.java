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
	private static String url_get_avg_ratings = "http://eleven.luporz.com/ratemycourses/get_avg_ratings.php";
	private static String url_get_comments = "http://eleven.luporz.com/ratemycourses/get_comments.php";
	
	// json nodes
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_COURSE = "COURSE";
	private static final String TAG_COURSEID = "COURSEID";
	private static final String TAG_COURSECODE = "COURSECODE";
	private static final String TAG_COURSENAME = "COURSENAME";
	private static final String TAG_INSTRUCTOR = "INSTRUCTOR";
	private static final String TAG_PROGRAM_CODE = "PROGRAM_CODE";
	
	private static final String TAG_RATINGS = "RATINGS";
	private static final String TAG_TOTAL_COUNT = "TOTAL_COUNT";
	private static final String TAG_AVG_HELPFULNESS = "AVG_HELPFULNESS";
	private static final String TAG_AVG_INTEREST_LEVEL = "AVG_INTEREST_LEVEL";
	private static final String TAG_AVG_EASINESS = "AVG_EASINESS";
	private static final String TAG_AVG_WORKLOAD = "AVG_WORKLOAD";
	private static final String TAG_AVG_OVERALL = "AVG_OVERALL";
	
	JSONArray courseObj = null;
	JSONObject courseDetails = null;
	JSONArray ratingsObj = null;
	JSONObject ratingsDetails = null;
	
	// widgets
	TextView courseCode;
	TextView courseName;
	TextView instructor;
	TextView programCode;
	
	TextView totalCount;
	TextView avg_help;
	TextView avg_int;
	TextView avg_esn;
	TextView avg_work;
	TextView avg_ovl;
	
	// UI values
	String readCourseCode;
	String readCourseName;
	String readInstructor;
	String readProgramCode;
	
	String readTotalCount;
	String readAvg_Help;
	String readAvg_Int;
	String readAvg_Esn;
	String readAvg_Work;
	String readAvg_Ovl;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_view);
		
		courseCode = (TextView) findViewById(R.id.course_code_label);
		courseName = (TextView) findViewById(R.id.course_name_label);
		instructor = (TextView) findViewById(R.id.prof_name_label);
		programCode = (TextView) findViewById(R.id.program_name_label);
		
		totalCount = (TextView) findViewById(R.id.rating_value_label);
		avg_help = (TextView) findViewById(R.id.helpfuness_value_label);
		avg_int = (TextView) findViewById(R.id.interest_level_value_label);
		avg_esn = (TextView) findViewById(R.id.easiness_value_label);
		avg_work = (TextView) findViewById(R.id.workload_value_label);
		avg_ovl = (TextView) findViewById(R.id.overall_value_label);
		
		// getting courseid from intent
		Intent i = getIntent();
		courseId = i.getStringExtra(TAG_COURSEID);
		
		// load details of a course
		new GetCourseDetails().execute();
		
		// implement Rate this Course button
		rateThisCourse();
		
		// implement Tell a Friend button
		tellFriend();
		
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
			
			int success;
			
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			params.add(new BasicNameValuePair("courseid", courseId));
			
			// getting JSON string from URL
			JSONObject json = jHelper.makeHttpRequest(url_get_course_details, "GET", params);
			JSONObject json_ratings = jHelper.makeHttpRequest(url_get_avg_ratings, "GET", params);
			
			// check your log cat for JSON response
			Log.d("Course details: ", json.toString());
			Log.d("Ratings details: ", json_ratings.toString());
			
			try {
				//Log.d("Course name: ", courseDetails.getString(TAG_COURSECODE));
				// checking for SUCCESS TAG
				success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// course details found
					// getting array of details
					courseObj = json.getJSONArray(TAG_COURSE);
					
					courseDetails = courseObj.getJSONObject(0);
					readCourseCode = courseDetails.getString(TAG_COURSECODE);
					readCourseName = courseDetails.getString(TAG_COURSENAME);
					readInstructor = courseDetails.getString(TAG_INSTRUCTOR);
					readProgramCode = courseDetails.getString(TAG_PROGRAM_CODE);
					
					ratingsObj = json_ratings.getJSONArray(TAG_RATINGS);
					
					ratingsDetails = ratingsObj.getJSONObject(0);
					readTotalCount = ratingsDetails.getString(TAG_TOTAL_COUNT);
					readAvg_Help = ratingsDetails.getString(TAG_AVG_HELPFULNESS);
					readAvg_Int = ratingsDetails.getString(TAG_AVG_INTEREST_LEVEL);
					readAvg_Esn = ratingsDetails.getString(TAG_AVG_EASINESS);
					readAvg_Work = ratingsDetails.getString(TAG_AVG_WORKLOAD);
					readAvg_Ovl = ratingsDetails.getString(TAG_AVG_OVERALL);
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
			
			// updating UI from Background thread
			runOnUiThread(new Runnable() {
				public void run() {
					courseCode.setText(readCourseCode);
					courseName.setText(readCourseName);
					instructor.setText(readInstructor);
					programCode.setText(readProgramCode);
					
					totalCount.setText(readTotalCount);
					avg_help.setText(readAvg_Help);
					avg_int.setText(readAvg_Int);
					avg_esn.setText(readAvg_Esn);
					avg_work.setText(readAvg_Work);
					avg_ovl.setText(readAvg_Ovl);
				}	
			});
			
		}
	}

}
