package com.someguy.jobmine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

import android.view.View;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


public class MainActivity extends SherlockActivity {
	/** Called when the activity is first created. */
	public static String userName = "";
	public static String pwd = "";
	ListView mListView;
	boolean displayApplied,displaySelected,displayNotSelected,displayRanked;
	ArrayList<String> title, id, emplyer, job, jobStatus, appStatus, resumes;
	SharedPreferences settings;
	Editor editor;
	
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String appliedKey ="displayApplied";
    public static final String selectedKey = "displaySelected";
    public static final String notSelectedKey = "displayNotSelected";
    public static final String rankedKey = "displayRanked";
    public static final String userNameKey ="USERNAMEKEY";
    public static final String pwdKey = "PWDKEY";
	public static final String idKey = "idkey";
	public static final String titleKey = "titlekey";
	public static final String employerKey = "employerkey";
	public static final String jobStatusKey = "jobstatuskey";
	public static final String appStatusKey = "appstatuskey";
	public static final String resumeKey = "resumekey";
	
    
    byte[] key;
    
	public void getJobmine() {

		title = new ArrayList<String>();
		id = new ArrayList<String>();
		emplyer = new ArrayList<String>();
		job = new ArrayList<String>();
		jobStatus = new ArrayList<String>();
		appStatus = new ArrayList<String>();
		resumes = new ArrayList<String>();
		
		DefaultHttpClient client = new DefaultHttpClient();
		List<Cookie> a = client.getCookieStore().getCookies();

		HttpPost post = new HttpPost(
				"https://jobmine.ccol.uwaterloo.ca/psp/SS/?cmd=login&"
						+ "userid=" + userName + "&" + "pwd=" + pwd + "&" +

						"submit=Submit");
		try {
			HttpResponse resp = client.execute(post);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			resp.getEntity().consumeContent();

			post = new HttpPost(
					"https://jobmine.ccol.uwaterloo.ca/psc/SS/EMPLOYEE/WORK/c/UW_CO_STUDENTS.UW_CO_APP_SUMMARY.GBL?pslnkid=UW_CO_APP_SUMMARY_LINK&FolderPath=PORTAL_ROOT_OBJECT.UW_CO_APP_SUMMARY_LINK&IsFolder=false&IgnoreParamTempl=FolderPath%2cIsFolder&PortalActualURL=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2fEMPLOYEE%2fWORK%2fc%2fUW_CO_STUDENTS.UW_CO_APP_SUMMARY.GBL%3fpslnkid%3dUW_CO_APP_SUMMARY_LINK&PortalContentURL=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2fEMPLOYEE%2fWORK%2fc%2fUW_CO_STUDENTS.UW_CO_APP_SUMMARY.GBL%3fpslnkid%3dUW_CO_APP_SUMMARY_LINK&PortalContentProvider=WORK&PortalCRefLabel=Applications&PortalRegistryName=EMPLOYEE&PortalServletURI=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsp%2fSS%2f&PortalURI=https%3a%2f%2fjobmine.ccol.uwaterloo.ca%2fpsc%2fSS%2f&PortalHostNode=WORK&NoCrumbs=yes&PortalKeyStruct=yes");// ?ICType=Panel&Menu=UW_CO_STUDENTS&Market=GBL&PanelGroupName=UW_CO_APP_SUMMARY&RL=&target=main0&navc=5170");
			resp = client.execute(post);
			stream = new ByteArrayOutputStream();
			resp.getEntity().writeTo(stream);
			Document table = Jsoup.parse(new String(stream.toByteArray()));

			post = new HttpPost(
					"https://jobmine.ccol.uwaterloo.ca/psp/SS/EMPLOYEE/WORK/?cmd=logout");
			client.execute(post);
			Elements element = table.getElementsByTag("table");
			Element b = element.get(5);
			Elements c = b.getAllElements();
			for (int i = 0; i < c.size(); i++) {

				if (c.get(i).id().contains("UW_CO_JB_TITLE2")
						&& c.get(i).hasText()) {
					title.add(c.get(i).ownText());
				}
				if (c.get(i).id().contains("UW_CO_APPS_VW2_UW_CO_JOB_ID")
						&& c.get(i).hasText()) {
					id.add(c.get(i).ownText());
				}
				if (c.get(i).id().contains("UW_CO_JOBINFOVW_UW_CO_PARENT_NAME")
						&& (c.get(i).id().contains("$$")) && c.get(i).hasText()) {
					emplyer.add(c.get(i).ownText());
				}
				if (c.get(i).id().contains("UW_CO_TERMCALND_UW_CO_DESCR_30")
						&& (c.get(i).id().contains("$$")) && c.get(i).hasText()) {
					job.add(c.get(i).ownText());
				}
				if (c.get(i).id().contains("UW_CO_JOBSTATVW_UW_CO_JOB_STATUS")
						&& (c.get(i).id().contains("$$")) && c.get(i).hasText()) {
					jobStatus.add(c.get(i).ownText());
					if(c.get(i).ownText().contains("Ranking Completed")){
						appStatus.add(" ");
						appStatus.add(" ");
					}
				}
				if (c.get(i).id().contains("UW_CO_APPSTATVW_UW_CO_APPL_STATUS")
						&& (c.get(i).id().contains("$$")) && c.get(i).hasText()) {
					appStatus.add(c.get(i).ownText());
				}
				if (c.get(i).id().contains("UW_CO_JOBAPP_CT_UW_CO_MAX_RESUMES")
						&& (c.get(i).id().contains("$$")) && c.get(i).hasText()) {
					resumes.add(c.get(i).ownText());
				}
				
				
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class getData extends AsyncTask<Void, Void, Void> {

		ProgressDialog dialog;
		private getData selfReference;
		Activity activity;

		public getData(Activity activity) {
			selfReference = this;
			this.activity = activity;
			dialog = new ProgressDialog(activity);
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog
					.show(activity, "", "Loading...", true, true);
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
			getJobmine();
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			dialog.dismiss();

			setContent();

		}

	}

	private void setContent() {
		LinearLayout list = (LinearLayout) findViewById(R.id.linearlayout1);
		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		list.removeAllViews();
		for ( int i = 0; i < resumes.size(); i++) {
			if (!title.get(i).equals("") ) {
				final int position = i;
			
				View v = li.inflate(R.layout.jobentry, null);
				
				TextView jobTitle = (TextView) v.findViewById(R.id.textView1);
				TextView jobEmployer = (TextView) v
						.findViewById(R.id.textView5);
				TextView jobStatusText = (TextView) v
						.findViewById(R.id.textView2);
				TextView appStatusText = (TextView) v
						.findViewById(R.id.textView3);
				TextView resumesText = (TextView) v
						.findViewById(R.id.textView4);

				jobTitle.setText(title.get(position));
				jobEmployer.setText(emplyer.get(position));
				jobStatusText.setText(jobStatus.get(position));
				appStatusText.setText(appStatus.get(position));
				if (appStatus.get(position).contains("Not Selected")) {
					appStatusText.setBackgroundResource(R.color.red);
				} else if (appStatus.get(position).contains("Selected")) {
					appStatusText.setBackgroundResource(R.color.green);
				}
				resumesText.setText(resumes.get(position) + " Applicants");
				v.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_UP){
							Intent intent = new Intent(MainActivity.this, JobDetails.class);
							intent.putExtra(titleKey, title.get(position));
							intent.putExtra(idKey, id.get(position));
							intent.putExtra(employerKey, emplyer.get(position));
							intent.putExtra(jobStatusKey, jobStatus.get(position));
							intent.putExtra(appStatusKey, appStatus.get(position));
							intent.putExtra(resumeKey, resumes.get(position));
							startActivity(intent);
						}
						return true;
					}
				});
				
				if((displayApplied && appStatus.get(position).contains("Applied") ||
						(displaySelected && appStatus.get(position).contains("Selected") && !appStatus.get(position).contains("Not")) ||
						(displaySelected && appStatus.get(position).contains("Alternate")) ||
						(displayNotSelected && appStatus.get(position).contains("Not Selected") ||
						(displayRanked && jobStatus.get(position).contains("Ranking Completed"))))){
					list.addView(v);
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		settings = new EncryptedSharedPreferences( 
			    this, this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) );
		editor = settings.edit();
		title = new ArrayList<String>();
		id = new ArrayList<String>();
		emplyer = new ArrayList<String>();
		job = new ArrayList<String>();
		jobStatus = new ArrayList<String>();
		appStatus = new ArrayList<String>();
		resumes = new ArrayList<String>();
		displayApplied =settings.getBoolean(appliedKey, true);
		displaySelected = settings.getBoolean(selectedKey, true);
		displayNotSelected = settings.getBoolean(notSelectedKey, true);
		displayRanked = settings.getBoolean(rankedKey, true);
		
	}


	private void createDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = vi.inflate(R.layout.dialog, null);
		builder.setView(layout);
		builder.setMessage("Please enter your username and password here:")
				.setCancelable(false)
				.setPositiveButton("Log In",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if yes, then get the password from the
								// dialog, and begin the authemailaccoutn
								// asynctask
								EditText usernameField = (EditText) layout
										.findViewById(R.id.username1);
								EditText passwordField = (EditText) layout
										.findViewById(R.id.password1);
								String passwordFieldContent = passwordField
										.getEditableText().toString();
								String usernameFieldContent = usernameField
										.getEditableText().toString();

								userName = usernameFieldContent;
								pwd = passwordFieldContent;
								try {
									editor.putString(userNameKey, userName);
									editor.putString(pwdKey,pwd);
									editor.commit();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								new getData(MainActivity.this).execute(new Void[3]);
							}
						})
				// otherwise,just cancel the dialog
				.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.setOnCancelListener(new OnCancelListener() {
			// if the dialog is cancelled, then reset the pressed and email
			// enabled states back to false
			public void onCancel(DialogInterface arg0) {
				finish();
			}
		});
		// only show the dialog if there's no password (TODO: have a way for the
		// user to reset the password)
		builder.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (title.size() > 0) {
			setContent();
		} else {
			if(settings.contains(userNameKey) && settings.contains(pwdKey)){
				try {
					userName = settings.getString(userNameKey, "");
					pwd = settings.getString(pwdKey, "");
					new getData(MainActivity.this).execute(new Void[3]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				createDialog();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getSupportMenuInflater();
		   inflater.inflate(R.menu.options, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case R.id.refresh:
	    	new getData(this).execute(new Void[3]);
	    	break;
	    case R.id.filter:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	final View layout = vi.inflate(R.layout.filter_dialog, null);
	    	builder.setView(layout);
	    	final CheckBox applCheckBox = (CheckBox)layout.findViewById(R.id.checkBox1);
	    	final CheckBox selCheckBox = (CheckBox)layout.findViewById(R.id.checkBox2);
	    	final CheckBox notSelCheckBox = (CheckBox)layout.findViewById(R.id.checkBox4);
	    	final CheckBox rankCmpltCheckBox = (CheckBox)layout.findViewById(R.id.checkBox3);
	    	
	    	rankCmpltCheckBox.setChecked(displayRanked);
	    	applCheckBox.setChecked(displayApplied);
	    	selCheckBox.setChecked(displaySelected);
	    	notSelCheckBox.setChecked(displayNotSelected);
	    	builder.setMessage("Select application type to display:");
	    	builder.setPositiveButton("OK", new OnClickListener() {
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    			displayApplied = applCheckBox.isChecked();
	    			displaySelected = selCheckBox.isChecked();
	    			displayNotSelected = notSelCheckBox.isChecked();
	    			displayRanked = rankCmpltCheckBox.isChecked();
	    			editor.putBoolean(appliedKey, displayApplied);
	    			editor.putBoolean(selectedKey, displaySelected);
	    			editor.putBoolean(notSelectedKey, displayNotSelected);
	    			editor.putBoolean(rankedKey, displayRanked);
	    			editor.commit();
	    			setContent();
	    		}
	    	});
	    	builder.show();
	    	break;
	    case R.id.logout:
	    	editor.remove(userNameKey);
	    	editor.remove(pwdKey);
	    	editor.commit();
	    	LinearLayout linearLayout1 = (LinearLayout)findViewById(R.id.linearlayout1);
	    	linearLayout1.removeAllViews();
	    	createDialog();
	    	break;
	    default:
	    	break;
	            
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	
}