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
import org.apache.http.params.HttpProtocolParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.app.SherlockActivity;
import com.someguy.jobmine.JobDetails.getJobInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InterviewList extends SherlockActivity {

	SharedPreferences settings;
	
	ArrayList<String> emplyNameList,titleList,dateList,lengthList,timeList,interviewerList,idList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interview_list);
		getSupportActionBar().setTitle("Interviews");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		settings = new EncryptedSharedPreferences( 
			    this, this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE) );
		new getInterviewTask(InterviewList.this).execute(new Void[3]);
	}

	public class getInterviewTask extends AsyncTask<Void, Void, Boolean> {

		ProgressDialog dialog;
		private getInterviewTask selfReference;
		Activity activity;

		public getInterviewTask(Activity activity) {
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
			return getInterviews();
		}

		@Override
		protected void onPostExecute(Boolean param) {
			dialog.dismiss();
			if(param){
				updateUI();
			}
			else{
				Toast.makeText(InterviewList.this, "Login Failed.", Toast.LENGTH_SHORT).show();
			}
			
		}

	}	

private boolean getInterviews() {
		
		String userName = settings.getString(MainActivity.userNameKey, "");
		String pwd = settings.getString(MainActivity.pwdKey, "");
		emplyNameList = new ArrayList<String>();
		titleList = new ArrayList<String>();
		dateList = new ArrayList<String>();
		lengthList = new ArrayList<String>();
		timeList = new ArrayList<String>();
		interviewerList = new ArrayList<String>();
		idList = new ArrayList<String>();
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
		
			post = new HttpPost("https://jobmine.ccol.uwaterloo.ca/psc/SS/EMPLOYEE/WORK/c/UW_CO_STUDENTS.UW_CO_STU_INTVS.GBL?UW_CO_STU_ID="+settings.getString(MainActivity.userIdKey,"")+"&amp;PortalActualURL=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2fEMPLOYEE%2fWORK%2fc%2fUW_CO_STUDENTS.UW_CO_STU_INTVS.GBL%3fUW_CO_STU_ID%3d20378462&amp;PortalContentURL=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2fEMPLOYEE%2fWORK%2fc%2fUW_CO_STUDENTS.UW_CO_STU_INTVS.GBL&amp;PortalContentProvider=WORK&amp;PortalCRefLabel=Interviews&amp;PortalRegistryName=EMPLOYEE&amp;PortalServletURI=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsp%2fSS%2f&amp;PortalURI=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2f&amp;PortalHostNode=WORK&amp;NoCrumbs=yes&amp;PortalKeyStruct=yes");
			resp = client.execute(post);
			stream = new ByteArrayOutputStream();
			resp.getEntity().writeTo(stream);
			Document webpage = Jsoup.parse(stream.toString());
			
			Element table = webpage.getElementById("UW_CO_STUD_INTV$scroll$0");
			Elements tableElements = table.getAllElements();
			for(int i = 0; i<tableElements.size(); i++){
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_JOB_ID")
						&& tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					idList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_PARENT_NAME")
						&& tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					emplyNameList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_JOBID_HL")
						&& tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					titleList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_CHAR_DATE")
						 && tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					dateList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_CHAR_STIME") && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					timeList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_INTV_DUR")
						 && tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					lengthList.add(tableElements.get(i).text());
				}
				if (tableElements.get(i).id().contains("UW_CO_STUD_INTV_UW_CO_DESCR_50")
						 && tableElements.get(i).hasText() && !(tableElements.get(i).text().equals(tableElements.get(i+1).text()))) {
					interviewerList.add(tableElements.get(i).text());
				}
			}
			
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

public void updateUI() {
	LinearLayout list = (LinearLayout) findViewById(R.id.interviewlist);
	LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	list.removeAllViews();

	for ( int i = 0; i < emplyNameList.size(); i++) {

		View v = li.inflate(R.layout.jobentry,null);
		final int position = i;
		TextView jobTitle = (TextView) v.findViewById(R.id.textView1);
		TextView jobEmployer = (TextView) v
				.findViewById(R.id.textView5);
		TextView dateText = (TextView) v
				.findViewById(R.id.textView2);
		TextView timeText = (TextView) v
				.findViewById(R.id.textView3);
		TextView interviewerText = (TextView) v
				.findViewById(R.id.textView4);

		jobTitle.setText(titleList.get(position));
		jobEmployer.setText(emplyNameList.get(position));
		dateText.setText(dateList.get(position));
		timeText.setText(timeList.get(position));
		interviewerText.setText(interviewerList.get(position));
		
		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){
					Intent intent = new Intent(InterviewList.this, JobDetails.class);
					intent.putExtra(MainActivity.text1Key, titleList.get(position));
					intent.putExtra(MainActivity.text2Key, idList.get(position));
					intent.putExtra(MainActivity.text3Key, emplyNameList.get(position));
					intent.putExtra(MainActivity.text5Key, dateList.get(position));
					intent.putExtra(MainActivity.text6Key, timeList.get(position));
					intent.putExtra(MainActivity.text7key, interviewerList.get(position));
					startActivity(intent);
				}
				return true;
			}
		});

		list.addView(v);
	}

}
}
