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

import com.example.ratemycourses.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CourseView extends Activity {
	
	private ProgressDialog pDialog;
	
	private String courseId;
	
	JSONHelper jHelper = new JSONHelper();
	
	private ArrayList<HashMap<String, String>> commentsList = new ArrayList<HashMap<String, String>>();
	
	private static String url_get_course_details = "http://eleven.luporz.com/ratemycourses/get_course_details.php";
	private static String url_get_avg_ratings = "http://eleven.luporz.com/ratemycourses/get_avg_ratings.php";
	private static String url_get_comments_by_courseid = "http://eleven.luporz.com/ratemycourses/get_comments_by_courseid.php";
	
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
	
	private static final String TAG_COMMENT_ID = "COMMENT_ID";
	private static final String TAG_COMMENT_TEXT = "COMMENT_TEXT";
	private static final String TAG_COMMENT_DATE = "COMMENT_DATE";
	
	JSONArray course = null;
	JSONObject courseObj = null;
	JSONArray ratings = null;
	JSONObject ratingsObj = null;
	JSONArray comments = null;
	JSONObject commentsObj = null;
	
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
	
	ListView commentLV;
	
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
		
		commentLV = (ListView) findViewById(R.id.comment_list);
		
		// getting courseid from intent
		Intent i = getIntent();
		courseId = i.getStringExtra(TAG_COURSEID);
		
		// load details of a course
		new GetCourseDetails().execute();
		
		// implement Rate this Course button
		rateThisCourse();
		
		// implement Tell a Friend button
		tellFriend();
		
		commentLV.setOnItemClickListener(new OnItemClickListener() {
			 
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String commentId = ((TextView) view.findViewById(R.id.comment_id)).getText()
                        .toString();
 
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        CommentView.class);
                // sending pid to next activity
                in.putExtra(TAG_COURSEID, courseId);
                in.putExtra(TAG_COMMENT_ID, commentId);
 
                // starting new activity and expecting some response back
                startActivity(in);
            }
        });
		
	}

	private void rateThisCourse() {
		Button rateBtn;
		
		rateBtn = (Button) findViewById(R.id.rate_course_bt);
		rateBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CourseRatingView.class);
				i.putExtra(TAG_COURSEID, courseId);
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
				
				i.putExtra(TAG_COURSEID, courseId);
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
			
			int success_course_info;
			int success_ratings;
			int success_comments;
			
			// building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			params.add(new BasicNameValuePair("courseid", courseId));
			
			// getting JSON string from URL
			JSONObject json_course_info = jHelper.makeHttpRequest(url_get_course_details, "GET", params);
			JSONObject json_ratings = jHelper.makeHttpRequest(url_get_avg_ratings, "GET", params);
			JSONObject json_comments = jHelper.makeHttpRequest(url_get_comments_by_courseid, "GET", params);
			
			// check your log cat for JSON response
			Log.d("Course details: ", json_course_info.toString());
			Log.d("Ratings details: ", json_ratings.toString());
			Log.d("Comments: ", json_comments.toString());
			
			try {
				//Log.d("Course name: ", courseDetails.getString(TAG_COURSECODE));
				// checking for SUCCESS TAG
				success_course_info = json_course_info.getInt(TAG_SUCCESS);
				success_ratings = json_ratings.getInt(TAG_SUCCESS);
				success_comments = json_comments.getInt(TAG_SUCCESS);
				
				if (success_course_info == 1 && success_ratings == 1
						&& success_comments == 1) {
					// getting course info
					course = json_course_info.getJSONArray(TAG_COURSE);
					
					courseObj = course.getJSONObject(0);
					readCourseCode = courseObj.getString(TAG_COURSECODE);
					readCourseName = courseObj.getString(TAG_COURSENAME);
					readInstructor = courseObj.getString(TAG_INSTRUCTOR);
					readProgramCode = courseObj.getString(TAG_PROGRAM_CODE);
					
					// getting ratings info
					ratings = json_ratings.getJSONArray(TAG_RATINGS);
					
					ratingsObj = ratings.getJSONObject(0);
					readTotalCount = ratingsObj.getString(TAG_TOTAL_COUNT);
					readAvg_Help = ratingsObj.getString(TAG_AVG_HELPFULNESS);
					readAvg_Int = ratingsObj.getString(TAG_AVG_INTEREST_LEVEL);
					readAvg_Esn = ratingsObj.getString(TAG_AVG_EASINESS);
					readAvg_Work = ratingsObj.getString(TAG_AVG_WORKLOAD);
					readAvg_Ovl = ratingsObj.getString(TAG_AVG_OVERALL);
					
					// getting list of comments
					comments = json_comments.getJSONArray(TAG_RATINGS);
					
					// looping through all comments
					for (int i=0; i<comments.length(); i++) {
						JSONObject commentsObj = comments.getJSONObject(i);
						
						String commentId = commentsObj.getString(TAG_COMMENT_ID);
						String commentText = commentsObj.getString(TAG_COMMENT_TEXT);
						String commentDate = commentsObj.getString(TAG_COMMENT_DATE);
						
						HashMap<String, String> map = new HashMap<String, String>();
						
						map.put(TAG_COMMENT_ID, commentId);
						map.put(TAG_COMMENT_TEXT, commentText);
						map.put(TAG_COMMENT_DATE, commentDate);
						
						commentsList.add(map);
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
					
					SimpleAdapter adapter = new SimpleAdapter(
							CourseView.this,
							commentsList,
							R.layout.comment_list,
							new String[] {TAG_COMMENT_ID, TAG_COMMENT_TEXT,
									TAG_COMMENT_DATE},
							new int[] {R.id.comment_id, R.id.comment_text, R.id.comment_date});
					
					commentLV.setAdapter(adapter);
				}	
			});
			
		}
	}

}
