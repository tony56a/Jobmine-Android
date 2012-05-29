package com.someguy.jobmine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

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
	
	
	public class getJobInfo extends AsyncTask<Void, Void, Boolean> {

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
		protected Boolean doInBackground(Void... arg0) {
			return getJobInfo();
		}

		@Override
		protected void onPostExecute(Boolean param) {
			dialog.dismiss();
			if(param){
				descriptionView.setText(descriptionText);
				Linkify.addLinks(descriptionView, Linkify.WEB_URLS);
			}
			else{
				Toast.makeText(JobDetails.this, "Login Failed.", Toast.LENGTH_SHORT).show();
			}
		}

	}
	

	private boolean getJobInfo() {
		
		String userName = settings.getString(MainActivity.userNameKey, "");
		String pwd = settings.getString(MainActivity.pwdKey, "");
		
		try {
			
			DefaultHttpClient client = new DefaultHttpClient();
			List<Cookie> a = client.getCookieStore().getCookies();
			HttpProtocolParams.setUserAgent(client.getParams(),"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			HttpPost post = new HttpPost(
					"https://jobmine.ccol.uwaterloo.ca/psp/SS/?cmd=login&"
							+ "userid=" + userName + "&" + "pwd=" + pwd + "&" +

							"submit=Submit");
			
			HttpResponse resp = client.execute(post);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			resp.getEntity().consumeContent();
		
			post = new HttpPost("https://jobmine.ccol.uwaterloo.ca/psc/SS/EMPLOYEE/WORK/c/UW_CO_STUDENTS.UW_CO_JOBDTLS?UW_CO_JOB_ID="+id);
			resp = client.execute(post);
			stream = new ByteArrayOutputStream();
			resp.getEntity().writeTo(stream);
			Document table = Jsoup.parse(new String(stream.toByteArray()));
			Element description = table.getElementById("UW_CO_JOBDTL_VW_UW_CO_JOB_DESCR");
			
			if(description.equals(null)){
				return false;
			}
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
		} catch (NullPointerException e){
			return false;
		} 
		return true;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.job_details);
		
		title = getIntent().getStringExtra(MainActivity.text1Key);
		id = getIntent().getStringExtra(MainActivity.text2Key);
		employer = getIntent().getStringExtra(MainActivity.text3Key);
		jobStatus = getIntent().getStringExtra(MainActivity.text5Key);
		appStatus = getIntent().getStringExtra(MainActivity.text6Key);
		resumes = getIntent().getStringExtra(MainActivity.text7key);
		
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
		resumeView.setText(resumes);
		
		employerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
				intent.putExtra(SearchManager.QUERY, employer);
				startActivity(intent);
			}
		});
		
		new getJobInfo(JobDetails.this).execute(new Void[3]);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  setContentView(R.layout.job_details);
	}
}
