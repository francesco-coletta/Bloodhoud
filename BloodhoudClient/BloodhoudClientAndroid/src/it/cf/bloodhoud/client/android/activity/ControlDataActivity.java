package it.cf.bloodhoud.client.android.activity;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.R;
import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.Utils;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalRead;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalWrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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

public class ControlDataActivity extends Activity implements OnClickListener
    {
        static private final Logger LOG                 = LoggerFactory.getLogger(ControlDataActivity.class);

        static private final String FILE_NAME_SMS_XML   = "sms.xml";
        static private final String FILE_NAME_CALL_XML  = "call.xml";
        static private final String FILE_NAME_SMS_JSON  = "sms.json";
        static private final String FILE_NAME_CALL_JSON = "call.json";

        private ObjectWriter        writerObjectInJson  = new ObjectMapper().writerWithDefaultPrettyPrinter();

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                this.setContentView(R.layout.activity_control_data);

                Button buttonExport = (Button) findViewById(R.controlActivity.buttonExport);
                buttonExport.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                            {
                                new ExportDataTask(v.getContext()).doInBackground();
                            }
                    });

                Button buttonClearLog = (Button) findViewById(R.controlActivity.buttonClear);
                buttonClearLog.setOnClickListener(this);

                Button buttonCancel = (Button) findViewById(R.controlActivity.buttonCancel);
                buttonCancel.setOnClickListener(this);

                Button buttonExportDb = (Button) findViewById(R.controlActivity.buttonExportDB);
                buttonExportDb.setOnClickListener(this);

                Button buttonSendAgaiDataToServer = (Button) findViewById(R.controlActivity.buttonSendAgaiDataToServer);
                buttonSendAgaiDataToServer.setOnClickListener(this);
            }

        @Override
        public void onClick(View v)
            {
                if (v.getId() == R.controlActivity.buttonClear)
                    {
                        LOG.debug("TODO: clear log");
                        Toast toast = Toast.makeText(this, "TODO: clear log", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                else if (v.getId() == R.controlActivity.buttonCancel)
                    {
                        this.finish();
                    }
                else if (v.getId() == R.controlActivity.buttonExportDB)
                    {
                        new  CopyDbTask(v.getContext()).doInBackground();
                    }
                else if (v.getId() == R.controlActivity.buttonSendAgaiDataToServer)
                    {
                        new ConfigureDataForSendItAgainDataToServerTask(v.getContext()).doInBackground();
                    }
            }

        // Private class for task in background

        // export sms, call in xml and json format
        private class ExportDataTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;

                public ExportDataTask(Context context)
                    {
                        super();
                        this.context = context;
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messaggio = "";
                        LOG.debug("Export data Started");
                        if (Utils.isExternalStorageAvailableAndWriteable())
                            {
                                Utils.cleanChacheDirIntoExternalStorage(context);
                                exportData();
                                copyExportedDataIntoExternalStorage(context);
                                messaggio = "Export eseguito";
                            }
                        else
                            {
                                messaggio = "External storage isn't writable. Skip export";
                                LOG.warn("External storage isn't writable. Skip export");
                            }

                        Toast toast = Toast.makeText(this.context, messaggio, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(messaggio);
                        return null;
                    }

                private void copyExportedDataIntoExternalStorage(Context context)
                    {
                        try
                            {
                                LOG.debug("Copy file to external storage");
                                String fromPath = context.getFilesDir().getCanonicalPath() + File.separator;
                                String toPath = context.getExternalCacheDir() + File.separator;
                                Utils.copyFiles(fromPath, toPath);
                            }
                        catch (IOException e)
                            {
                                LOG.error(e.getMessage());
                            }
                    }

                private void exportData()
                    {
                        exportSms();
                        exportCall();
                    }

                private void exportSms()
                    {
                        RepositoryLocalRead repo = new RepositoryLocalSQLLite(context);
                        List<Sms> smss = repo.getSms();
                        LOG.debug("Nel db sono presenti {} sms", smss.size());

                        exportSmsToFileXml(smss);
                        exportSmsToFileJson(smss);

                        LOG.debug("Sono stati esportai gli sms");
                    }

                private void exportCall()
                    {
                        RepositoryLocalRead repo = new RepositoryLocalSQLLite(context);
                        List<Call> calls = repo.getCall();
                        LOG.debug("Nel db sono presenti {} call", calls.size());

                        exportCallToFileXml(calls);
                        exportCallToFileJson(calls);
                    }

                private void exportCallToFileXml(List<Call> calls)
                    {
                        if (calls != null && calls.size() > 0)
                            {

                                FileOutputStream callOutputStream = null;
                                try
                                    {
                                        callOutputStream = context.openFileOutput(FILE_NAME_CALL_XML, Context.MODE_PRIVATE);
                                        Serializer serializer = new Persister();
                                        for (Call call : calls)
                                            {
                                                // LOG.debug("Export call {}", call.toString());
                                                serializer.write(call, callOutputStream);
                                                callOutputStream.write("\n".getBytes());
                                            }
                                        callOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

                private void exportCallToFileJson(List<Call> calls)
                    {
                        if (calls != null && calls.size() > 0)
                            {
                                FileOutputStream callOutputStream = null;
                                try
                                    {
                                        callOutputStream = context.openFileOutput(FILE_NAME_CALL_JSON, Context.MODE_PRIVATE);
                                        for (Call call : calls)
                                            {
                                                callOutputStream.write(writerObjectInJson.writeValueAsString(call).getBytes());
                                                callOutputStream.write("\n".getBytes());
                                            }
                                        callOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }

                    }

                private void exportSmsToFileXml(List<Sms> smss)
                    {
                        if (smss != null && smss.size() > 0)
                            {
                                FileOutputStream smsOutputStream = null;
                                try
                                    {
                                        smsOutputStream = context.openFileOutput(FILE_NAME_SMS_XML, Context.MODE_PRIVATE);
                                        Serializer serializer = new Persister();
                                        for (Sms sms : smss)
                                            {
                                                serializer.write(sms, smsOutputStream);
                                                smsOutputStream.write("\n".getBytes());
                                            }
                                        smsOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

                private void exportSmsToFileJson(List<Sms> smss)
                    {
                        if (smss != null && smss.size() > 0)
                            {
                                FileOutputStream smsOutputStream = null;
                                try
                                    {
                                        smsOutputStream = context.openFileOutput(FILE_NAME_SMS_JSON, Context.MODE_PRIVATE);
                                        for (Sms sms : smss)
                                            {
                                                smsOutputStream.write(writerObjectInJson.writeValueAsString(sms).getBytes());
                                                smsOutputStream.write("\n".getBytes());
                                            }
                                        smsOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

            }// ExportTask Class

        
        // export DB file
        private class CopyDbTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;
                public CopyDbTask(Context context)
                    {
                        super();
                        this.context = context;
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messaggio = "";
                        LOG.debug("Copy DB started");
                        if (Utils.isExternalStorageAvailableAndWriteable())
                            {
                                Utils.cleanChacheDirIntoExternalStorage(context);
                                LOG.debug("Copy DB to external storage");
                                try
                                    {
                                        String from = context.getDatabasePath(App.DATABASE_NAME).getCanonicalPath();
                                        String to = context.getExternalCacheDir() + File.separator + App.DATABASE_NAME;
                                        Utils.copy(from, to);                                
                                        messaggio = "Copy Db ok";
                                        LOG.debug(messaggio);
                                    }
                                catch (IOException e)
                                    {
                                        messaggio = "Problem during DB copy: " + e.getMessage();
                                        LOG.error(messaggio);
                                    }
                            }
                        else
                            {
                                messaggio = "External storage isn't writable. Skip export";
                                LOG.warn(messaggio);
                            }

                        Toast toast = Toast.makeText(this.context, messaggio, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(messaggio);
                        return null;
                    }
            }// ExportDbTask Class

        
        // Send again sms, call to server
        private class ConfigureDataForSendItAgainDataToServerTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;
                public ConfigureDataForSendItAgainDataToServerTask(Context context)
                    {
                        super();
                        this.context = context;
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messagge = "Flag send to server has been reseted";
                        
                        RepositoryLocalWrite repo = new RepositoryLocalSQLLite(context);
                        repo.markDataForSendedAgainToServer();
                        LOG.debug(messagge);

                        Toast toast = Toast.makeText(this.context, messagge, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(messagge);
                        return null;
                    }
            }// ExportDbTask Class
        
    }
