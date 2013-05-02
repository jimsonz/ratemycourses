package bu.cs683.ratemycourses.course;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class CourseRatingView extends Activity {
	
	private ProgressDialog pDialog;
	
	JSONHelper jHelper = new JSONHelper();
	String courseId;
	RatingBar inputHelpfulness;
	RatingBar inputInterest_level;
	RatingBar inputEasiness;
	RatingBar inputWorkload;
	RatingBar inputOverall;
	EditText inputCommentText;
	
	private static String url_create_rating = "http://eleven.luporz.com/ratemycourses/create_ratings.php";
	
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_COURSEID = "COURSEID";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_rating);
		
		// getting courseid from intent
		Intent i = getIntent();
		courseId = i.getStringExtra(TAG_COURSEID);
		
		// widgets
		inputHelpfulness = (RatingBar) findViewById(R.id.helpfulness_ratingBar);
		inputInterest_level = (RatingBar) findViewById(R.id.interest_level_ratingBar);
		inputEasiness = (RatingBar) findViewById(R.id.easiness_ratingBar);
		inputWorkload = (RatingBar) findViewById(R.id.workload_ratingBar);
		inputOverall = (RatingBar) findViewById(R.id.overall_ratingBar);
		inputCommentText = (EditText) findViewById(R.id.comment_text);
		
		inputCommentText.setSingleLine(false);
		
		// implement submit rating button
		submitRating();
	}

	private void submitRating() {
		Button submit;
		
		submit = (Button) findViewById(R.id.commit_rate_bt);
		submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// creating new rating details in the background thread
				new CreateNewRating().execute();
			}
		});
	}
	
	/**
	 * Background Async Task to create new rating
	 */
	class CreateNewRating extends AsyncTask<String, String, String> {
		
		/**
		 * Show progress dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CourseRatingView.this);
			pDialog.setMessage("Creating Rating...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		/**
		 * Creating the rating
		 */
		protected String doInBackground(String... args) {
			String helpfulness = Float.toString(inputHelpfulness.getRating());
			String interest_level = Float.toString(inputInterest_level.getRating());
			String easiness = Float.toString(inputEasiness.getRating());
			String workload = Float.toString(inputWorkload.getRating());
			String overall = Float.toString(inputOverall.getRating());
			String commentText = inputCommentText.getText().toString();
			
			// Building parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("course_id", courseId));
			params.add(new BasicNameValuePair("helpfulness", helpfulness));
			params.add(new BasicNameValuePair("interest_level", interest_level));
			params.add(new BasicNameValuePair("easiness", easiness));
			params.add(new BasicNameValuePair("workload", workload));
			params.add(new BasicNameValuePair("overall", overall));
			params.add(new BasicNameValuePair("comment_text", commentText));
			
			// getting JSON Object
			JSONObject json = jHelper.makeHttpRequest(url_create_rating, "POST", params);
			
			Log.d("Create Response", json.toString());
			
			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);
				
				if (success == 1) {
					// successfully created product
					Intent i = new Intent(getApplicationContext(), CourseView.class);
					i.putExtra(TAG_COURSEID, courseId);
					startActivity(i);
					
					// close this screen
					finish();
				} else {
					// creation fails
					// prompt a "courses not found" message
					Toast toast = Toast.makeText(getApplicationContext(), "Creating new ratings failed", Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Dismiss the progress dialog
		 */
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
		}
	}

}
