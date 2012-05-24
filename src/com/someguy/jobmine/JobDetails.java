package com.someguy.jobmine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class JobDetails extends SherlockActivity {

	SharedPreferences settings;
	String title;
	String id;
	String employer;
	String jobStatus;
	String appStatus;
	String resumes;
	Spanned descriptionText;

	TextView titleView;
	TextView descriptionView;
	TextView resumeView;
	TextView appStatusView;
	TextView jobStatusView;
	TextView employerView;
	
	
	public class getJobInfo extends AsyncTask<Void, Void, Void> {

		ProgressDialog dialog;
		private getJobInfo selfReference;
		Activity activity;

		public getJobInfo(Activity activity) {
			selfReference = this;
			this.activity = activity;
			dialog = new ProgressDialog(activity);
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog
					.show(activity, "", "Loading...", true, false);
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (selfReference != null) {
						selfReference.cancel(true);
					}
				}
			});
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			getJobInfo();
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			dialog.dismiss();
			descriptionView.setText(descriptionText);
		}

	}
	

	private void getJobInfo() {
		
		String userName = settings.getString(MainActivity.userNameKey, "");
		String pwd = settings.getString(MainActivity.pwdKey, "");
		
		try {
			
			DefaultHttpClient client = new DefaultHttpClient();
			List<Cookie> a = client.getCookieStore().getCookies();

			HttpPost post = new HttpPost(
					"https://jobmine.ccol.uwaterloo.ca/psp/SS/?cmd=login&"
							+ "userid=" + userName + "&" + "pwd=" + pwd + "&" +

							"submit=Submit");
			
			HttpResponse resp = client.execute(post);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			resp.getEntity().writeTo(stream);

			post = new HttpPost("https://jobmine.ccol.uwaterloo.ca/psc/SS/EMPLOYEE/WORK/c/UW_CO_STUDENTS.UW_CO_JOBDTLS?UW_CO_JOB_ID="+id);
			resp = client.execute(post);
			stream = new ByteArrayOutputStream();
			resp.getEntity().writeTo(stream);
			Document table = Jsoup.parse(new String(stream.toByteArray()));
			Element description = table.getElementById("UW_CO_JOBDTL_VW_UW_CO_JOB_DESCR");
			descriptionText = Html.fromHtml( description.html()); 
			post = new HttpPost(
					"https://jobmine.ccol.uwaterloo.ca/psp/SS/EMPLOYEE/WORK/?cmd=logout");
			client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.job_details);
		
		title = getIntent().getStringExtra(MainActivity.titleKey);
		id = getIntent().getStringExtra(MainActivity.idKey);
		employer = getIntent().getStringExtra(MainActivity.employerKey);
		jobStatus = getIntent().getStringExtra(MainActivity.jobStatusKey);
		appStatus = getIntent().getStringExtra(MainActivity.appStatusKey);
		resumes = getIntent().getStringExtra(MainActivity.resumeKey);
		
		settings = new EncryptedSharedPreferences( 
			    this, this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE) );
	
		titleView = (TextView)findViewById(R.id.title);
		employerView = (TextView)findViewById(R.id.employer);
		jobStatusView = (TextView)findViewById(R.id.jobstatus);
		appStatusView = (TextView)findViewById(R.id.appstatus);
		resumeView = (TextView)findViewById(R.id.resumes);
		descriptionView = (TextView)findViewById(R.id.description);
		
		titleView.setText(title);
		employerView.setText(employer);
		jobStatusView.setText(jobStatus);
		appStatusView.setText(appStatus);
		resumeView.setText(resumes+" Applicants");
		
		new getJobInfo(JobDetails.this).execute(new Void[3]);
	}

	
}
