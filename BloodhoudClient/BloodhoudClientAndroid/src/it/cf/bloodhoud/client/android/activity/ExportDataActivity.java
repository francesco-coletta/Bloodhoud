package it.cf.bloodhoud.client.android.activity;


import it.cf.bloodhoud.client.android.R;
import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.Utils;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalRead;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExportDataActivity
        extends Activity
        implements OnClickListener
	{
		static private final Logger LOG = LoggerFactory.getLogger(ExportDataActivity.class);
		
		static private final String FILE_NAME_SMS = "sms.xml";
		static private final String FILE_NAME_CALL = "call.xml";

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				this.setContentView(R.layout.activity_export_data_file);

				Button buttonExport = (Button) findViewById(R.exportActivity.buttonExport);
				buttonExport.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
							{
								new ExportTask(v.getContext()).doInBackground();
							}
					});

				Button buttonClearLog = (Button) findViewById(R.exportActivity.buttonClear);
				buttonClearLog.setOnClickListener(this);

				Button buttonCancel = (Button) findViewById(R.exportActivity.buttonCancel);
				buttonCancel.setOnClickListener(this);
			}

		@Override
		public void onClick(View v)
			{
				if (v.getId() == R.exportActivity.buttonClear)
					{
						LOG.debug("TODO: clear log");
						Toast toast = Toast.makeText(this, "TODO: clear log", Toast.LENGTH_SHORT);
						toast.show();

					}
				else if (v.getId() == R.exportActivity.buttonCancel)
					{
						this.finish();
					}
			}

		private class ExportTask
		        extends AsyncTask<String, Integer, String>
			{

				private final Context context;

				public ExportTask(Context context)
					{
						super();
						this.context = context;
					}

				@Override
				protected String doInBackground(String... arg0)
					{
						String messaggio = "";

						LOG.debug("Export Started");
						if (isExternalStorageAvailableAndWriteable())
							{
								cleanChacheDirIntoExternalStorage();
								exportDataFromDbToXmlFile();
								copyDataIntoExternalStorage();

								messaggio = "Export eseguito";
							}
						else
							{
								messaggio = "External storage isn't writable. Skip export";
								LOG.warn("External storage isn't writable. Skip export");
							}

						Toast toast = Toast.makeText(this.context, messaggio, Toast.LENGTH_SHORT);
						toast.show();

						TextView messaggioEsitoTask = (TextView) findViewById(R.exportActivity.labelMessage);
						messaggioEsitoTask.setText(messaggio);
						return null;
					}

				private void cleanChacheDirIntoExternalStorage()
					{
						LOG.debug("Pulisco la directory <{}>", context.getExternalCacheDir());
						File[] files = context.getExternalCacheDir().listFiles();
						for (File file : files)
							{
								file.delete();
							}
						LOG.debug("Eliminati {} file", files.length);
					}

				private void exportDataFromDbToXmlFile()
				{
					exportSmsFromDbToXmlFile();
					exportCallFromDbToXmlFile();
				}				
				
				private void exportCallFromDbToXmlFile() {
					RepositoryLocalRead repo = new RepositoryLocalSQLLite(context);
					List<Call> calls = repo.getCall();
					LOG.debug("Nel db sono presenti {} call", calls.size());
					
					FileOutputStream callOutputStream = null;
					try
						{
							callOutputStream = context.openFileOutput(FILE_NAME_CALL, Context.MODE_PRIVATE);
							Serializer serializer = new Persister(); 
							for (Call call : calls) {
								//LOG.debug("Export call {}", call.toString());
								serializer.write(call, callOutputStream);
							}
							callOutputStream.close();
						}
					catch (Exception e)
						{
							LOG.error(e.getMessage());
						}
					LOG.debug("Sono stati esportate tutte le call");
				}

				private void exportSmsFromDbToXmlFile() {
					RepositoryLocalRead repo = new RepositoryLocalSQLLite(context);
					List<Sms> smss = repo.getSms();
					LOG.debug("Nel db sono presenti {} sms", smss.size());
					
					FileOutputStream smsOutputStream = null;
					try
						{
							smsOutputStream = context.openFileOutput(FILE_NAME_SMS, Context.MODE_PRIVATE);
							Serializer serializer = new Persister(); 
							for (Sms sms : smss) {
								//LOG.debug("Export sms {}", sms.toString());
								serializer.write(sms, smsOutputStream);
							}
							smsOutputStream.close();
						}
					catch (Exception e)
						{
							LOG.error(e.getMessage());
						}
					LOG.debug("Sono stati esportai gli sms");
				}

				private void copyDataIntoExternalStorage()
					{
						try
							{
								String pathDataFiles = context.getFilesDir().getCanonicalPath() + File.separator;
								String pathWhereCopyDataFiles = context.getExternalCacheDir() + File.separator;
								LOG.debug("Copia dei file dei dati dalla dir <{}> nella dir <{}>", pathDataFiles, pathWhereCopyDataFiles);

								File[] files = context.getFilesDir().listFiles();
								String dataFileName;
								String fileNameWithPath;
								String newFileNameWithPath;
								for (File file : files)
									{
										dataFileName = file.getName();
										LOG.debug("Copy del file <{}>", dataFileName);
										fileNameWithPath = pathDataFiles + dataFileName;
										newFileNameWithPath = pathWhereCopyDataFiles + dataFileName;
										Utils.copy(fileNameWithPath, newFileNameWithPath);
									}
								LOG.debug("Copiati {} file", files.length);
							}
						catch (IOException e)
							{
								LOG.error(e.getMessage());
							}
					}

				private boolean isExternalStorageAvailableAndWriteable()
					{
						boolean mExternalStorageAvailable = false;
						boolean mExternalStorageWriteable = false;

						String state = Environment.getExternalStorageState();

						if (Environment.MEDIA_MOUNTED.equals(state))
							{
								// We can read and write the media
								mExternalStorageAvailable = mExternalStorageWriteable = true;
							}
						else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
							{
								// We can only read the media
								mExternalStorageAvailable = true;
								mExternalStorageWriteable = false;
							}
						else
							{
								// Something else is wrong. It may be one of many other states, but all we need
								// to know is we can neither read nor write
								mExternalStorageAvailable = mExternalStorageWriteable = false;
							}

						LOG.debug("State of external storage [available, writable]= [{}, {}]", mExternalStorageAvailable, mExternalStorageWriteable + "]");

						return mExternalStorageWriteable;
					}

			}

	}
