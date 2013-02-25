package it.cf.bloodhoud.client.android;

import it.cf.bloodhoud.client.android.activity.ControlDataActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class Utils
    {
        static private final Logger LOG = LoggerFactory.getLogger(Utils.class);

        public static String getTab(final int numTabs)
            {
                int nt = numTabs;
                if (nt < 0)
                    {
                        nt = 0;
                    }
                if (nt > 9)
                    {
                        nt = 9;
                    }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nt; i++)
                    {
                        sb.append("   ");
                    }
                return sb.toString();
            }

        public static String formatDatetime(Date datetime)
            {
                String datetimeFormatted = "";

                if (datetime != null)
                    {
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        datetimeFormatted = sdfDate.format(datetime);
                    }
                return datetimeFormatted;
            }

        public static String formatDatetime(long datetime)
            {
                String datetimeFormatted = "";
                if (datetime > 0)
                    {
                        datetimeFormatted = formatDatetime(new Date(datetime));
                    }
                return datetimeFormatted;
            }

        public static void copy(String filenameSrc, String filenameDest) throws IOException
            {
                InputStream in = new FileInputStream(filenameSrc);
                OutputStream out = new FileOutputStream(filenameDest);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    {
                        out.write(buf, 0, len);
                    }
                in.close();
                out.close();
            }

        static public String getDeviceName()
            {
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                if (model.startsWith(manufacturer))
                    {
                        return model;
                    }
                else
                    {
                        return manufacturer + " " + model;
                    }
            }

        static public String getDeviceId(Context context)
            {
                TelephonyManager telephonyManager = null;
                telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = telephonyManager.getDeviceId();
                return deviceId;
            }

        static public void copyFiles(final String fromPath, final String toPath)
            {
                try
                    {
                        LOG.debug("Copia dei file dei dati dalla dir <{}> nella dir <{}>", fromPath, toPath);
                        File[] files = new File(fromPath).listFiles();
                        String dataFileName;
                        String fileNameWithPath;
                        String newFileNameWithPath;
                        for (File file : files)
                            {
                                dataFileName = file.getName();
                                LOG.debug("Copy del file <{}>", dataFileName);
                                fileNameWithPath = fromPath + dataFileName;
                                newFileNameWithPath = toPath + dataFileName;
                                Utils.copy(fileNameWithPath, newFileNameWithPath);
                            }
                        LOG.debug("Copiati {} file", files.length);
                    }
                catch (IOException e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        public static void cleanChacheDirIntoExternalStorage(Context context)
            {
                try
                    {
                        LOG.debug("Pulisco la directory <{}>", context.getExternalCacheDir());
                        File[] files = context.getExternalCacheDir().listFiles();
                        for (File file : files)
                            {
                                file.delete();
                            }
                        LOG.debug("Eliminati {} file", files.length);
                    }
                catch (Exception e)
                    {
                        LOG.error("Errore: {}", e.getMessage());
                    }
            }

        public static boolean isExternalStorageAvailableAndWriteable()
            {
                boolean mExternalStorageWriteable = false;
                try
                    {
                        boolean mExternalStorageAvailable = false;
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
                                // Something else is wrong. It may be one of many other states, but all we need to know is we can neither read nor write
                                mExternalStorageAvailable = mExternalStorageWriteable = false;
                            }
                        LOG.debug("State of external storage [available, writable]= [{}, {}]", mExternalStorageAvailable, mExternalStorageWriteable + "]");
                    }
                catch (Exception e)
                    {
                        LOG.error("Errore: {}", e.getMessage());
                    }
                return mExternalStorageWriteable;
            }

        public static Cursor getSmsAllCursor(Context context)
            {
                // Retrieves the URI, you can select a different content address depending on which SMS you want
                // 1. Inbox = "content://sms/inbox"
                // 2. Failed = "content://sms/failed"
                // 3. Queued = "content://sms/queued"
                // 4. Sent = "content://sms/sent"
                // 5. Draft = "content://sms/draft"
                // 6. Outbox = "content://sms/outbox"
                // 7. Undelivered = "content://sms/undelivered"
                // 8. All = "content://sms/all"
                // 9. Conversations = "content://sms/conversations"

                // SMS type (TextBasedSmsColumns )
                // MESSAGE_TYPE_ALL = 0;
                // MESSAGE_TYPE_INBOX = 1;
                // MESSAGE_TYPE_SENT = 2;
                // MESSAGE_TYPE_DRAFT = 3;
                // MESSAGE_TYPE_OUTBOX = 4;
                // MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
                // MESSAGE_TYPE_QUEUED = 6; // for messages to send later

                // Starting from myCursor(0) to myCursor(15):
                // 0: _id (long)
                // 1: thread_id (long)
                // 2: address (String)
                // 3: person (String)
                // 4: date (long)
                // 5: protocol
                // 6: read
                // 7: status
                // 8: type
                // 9: reply_path_present
                // 10: subject (String)
                // 11: body (String)
                // 12: service_center
                // 13: locked
                // 14: error_code
                // 15: seen

                String smsDate = "date";
                String smsAddress = "address";
                //String smsPerson = "person";
                String smsType = "type";
                String smsBody = "body";

                //Uri smsUri = Uri.parse("content://sms/all");

                // Retrieves all SMS (if you want only unread SMS, put "read = 0" for the 3rd parameter)
                Uri uri = Uri.parse("content://sms/");
                String[] projection = new String[] { smsDate, smsAddress, smsBody, smsType };
                String selection = null; //smsType + " = ? AND " + smsDate + " > ?";
                String[] selectionArgs = null ; //new String[] { "2", String.valueOf(timeLastChecked) };
                String sortOrder = null; //smsDate + " DESC";

                return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
            }
    }
