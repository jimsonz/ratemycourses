package bu.cs683.ratemycourses.course;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ratemycourses.R;

import bu.cs683.ratemycourses.JSONHelper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class CommentView extends Activity {
	private ProgressDialog pDialog;
	
	private String courseId;
	private String commentId;
	
	JSONHelper jHelper = new JSONHelper();

	private static String url_get_course_details = "http://eleven.luporz.com/ratemycourses/get_course_details.php";
	private static String url_get_comments_by_commentid = "http://eleven.luporz.com/ratemycourses/get_comments_by_commentid.php";
	
	private static final String TAG_SUCCESS = "success";
	
	private static final String TAG_COURSE = "COURSE";
	private static final String TAG_COURSENAME = "COURSENAME";
	
	private static final String TAG_RATINGS = "RATINGS";
	private static final String TAG_COURSEID = "COURSEID";
	private static final String TAG_COMMENT_ID = "COMMENT_ID";
	private static final String TAG_HELPFULNESS = "HELPFULNESS";
	private static final String TAG_INTEREST_LEVEL = "INTEREST_LEVEL";
	private static final String TAG_EASINESS = "EASINESS";
	private static final String TAG_WORKLOAD = "WORKLOAD";
	private static final String TAG_OVERALL = "OVERALL";
	private static final String TAG_COMMENT_FROM = "COMMENT_FROM";
	private static final String TAG_COMMENT_TEXT = "COMMENT_TEXT";
	private static final String TAG_COMMENT_DATE = "COMMENT_DATE";
	
	JSONArray course = null;
	JSONObject courseObj = null;
	JSONArray comment = null;
	JSONObject commentObj = null;
	
	TextView courseName;
	TextView reviewedBy;
	TextView reviewedDate;
	TextView commentText;
	
	RatingBar helpfulness;
	RatingBar interest_level;
	RatingBar easiness;
	RatingBar workload;
	RatingBar overall;
	
	String readCourseName;
	
	String readHelp;
	String readInt;
	String readEsn;
	String readWork;
	String readOvl;
	String readCommentId;
	String readCommentText;
	String readCommentFrom;
	String readCommentDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_view);
		
		// getting courseid from intent
		Intent i = getIntent();
		courseId = i.getStringExtra(TAG_COURSEID);
		commentId = i.getStringExtra(TAG_COMMENT_ID);
		
		courseName = (TextView) findViewById(R.id.cv_coursename_value);
		reviewedBy = (TextView) findViewById(R.id.cv_reviewby_value);
		reviewedDate = (TextView) findViewById(R.id.cv_reviewDate_value);
		commentText = (TextView) findViewById(R.id.cv_comment);
		
		helpfulness = (RatingBar) findViewById(R.id.cv_helpfulness_ratingBar);
		interest_level = (RatingBar) findViewById(R.id.cv_interest_level_ratingBar);
		easiness = (RatingBar) findViewById(R.id.cv_easiness_ratingBar);
		workload = (RatingBar) findViewById(R.id.cv_workload_ratingBar);
		overall = (RatingBar) findViewById(R.id.cv_overall_ratingBar);
		
		// implement Rate this Course button
		doneComment();
		
		// load details of a comment
		new GetCommentDetails().execute();
		
	}
	
	private void doneComment() {
		Button doneBtn;
		
		doneBtn = (Button) findViewById(R.id.cv_btn);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CourseView.class);
				i.putExtra(TAG_COURSEID, courseId);
				startActivity(i);
			}
		});
	}
	
	/**
	 * Background Async Task to get course details
	 */
	class GetCommentDetails extends AsyncTask<String, String, String> {
		
		/**
		 * Show progress dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CommentView.this);
			pDialog.setMessage("Loading comment details, please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		/**
		 * Getting details for a comment in background thread
		 */
		protected String doInBackground(String... args) {
			
			int success_course_info;
			int success_comments;
			
			// building parameters
			List<NameValuePair> params_course = new ArrayList<NameValuePair> ();
			params_course.add(new BasicNameValuePair("courseid", courseId));
			
			List<NameValuePair> params_comment = new ArrayList<NameValuePair> ();
			params_comment.add(new BasicNameValuePair("commentid", commentId));
			
			// getting JSON string from URL
			JSONObject json_course_info = jHelper.makeHttpRequest(url_get_course_details, "GET", params_course);
			JSONObject json_comments = jHelper.makeHttpRequest(url_get_comments_by_commentid, "GET", params_comment);
			
			// check your log cat for JSON response
			Log.d("Course name: ", json_course_info.toString());
			Log.d("Comments: ", json_comments.toString());
			
			try {
				//Log.d("Course name: ", courseDetails.getString(TAG_COURSECODE));
				// checking for SUCCESS TAG
				success_course_info = json_course_info.getInt(TAG_SUCCESS);
				success_comments = json_comments.getInt(TAG_SUCCESS);
				
				if (success_course_info == 1 && success_comments == 1) {
					// getting course info
					course = json_course_info.getJSONArray(TAG_COURSE);
					
					courseObj = course.getJSONObject(0);
					readCourseName = courseObj.getString(TAG_COURSENAME);
					
					// getting ratings info
					comment = json_comments.getJSONArray(TAG_RATINGS);
					JSONObject commentsObj = comment.getJSONObject(0);
					
					readHelp = commentsObj.getString(TAG_HELPFULNESS);
					readInt = commentsObj.getString(TAG_INTEREST_LEVEL);
					readEsn = commentsObj.getString(TAG_EASINESS);
					readWork = commentsObj.getString(TAG_WORKLOAD);
					readOvl = commentsObj.getString(TAG_OVERALL);
					
					readCommentId = commentsObj.getString(TAG_COMMENT_ID);
					readCommentText = commentsObj.getString(TAG_COMMENT_TEXT);
					readCommentFrom = commentsObj.getString(TAG_COMMENT_FROM);
					readCommentDate = commentsObj.getString(TAG_COMMENT_DATE);
					
				} else {
					// no comments found
					// prompt a "comments not found" message
					Toast toast = Toast.makeText(getApplicationContext(), "No comments found", Toast.LENGTH_SHORT);
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
					courseName.setText(readCourseName);
					reviewedBy.setText(readCommentFrom);
					reviewedDate.setText(readCommentDate);
					
					helpfulness.setRating(Float.valueOf(readHelp));
					interest_level.setRating(Float.valueOf(readInt));
					easiness.setRating(Float.valueOf(readEsn));
					workload.setRating(Float.valueOf(readWork));
					overall.setRating(Float.valueOf(readOvl));
					
					commentText.setText(readCommentText);
				}	
			});
			
		}
	}
	
}
