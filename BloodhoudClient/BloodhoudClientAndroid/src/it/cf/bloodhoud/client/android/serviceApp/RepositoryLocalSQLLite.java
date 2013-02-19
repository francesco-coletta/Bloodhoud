package it.cf.bloodhoud.client.android.serviceApp;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.dao.TableCall;
import it.cf.bloodhoud.client.android.dao.TablePhone;
import it.cf.bloodhoud.client.android.dao.TableSms;
import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.CallFactory;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.SmsFactory;
import it.cf.bloodhoud.client.android.model.Utils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RepositoryLocalSQLLite extends SQLiteOpenHelper implements RepositoryLocalWrite, RepositoryLocalRead
    {
        static private final Logger LOG              = LoggerFactory.getLogger(RepositoryLocalSQLLite.class);


        public RepositoryLocalSQLLite(Context context)
            {
                super(context, App.DATABASE_NAME, null, App.DATABASE_VERSION);
            }


        @Override
        public Phone getPhone(String deviceId)
            {
                Phone phone = null;
                String table = TablePhone.TABLE_NAME;
                String[] columns = null;
                String selection = TablePhone.COLUMN_IMEI + " = ?";
                String[] selectionArgs = { StringUtils.trimToEmpty(deviceId) };
                String groupBy = null;
                String having = null;
                String orderBy = null;

                SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = null;
                try
                    {
                        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
                        if (cursor.moveToNext())
                            {
                                String imei = cursor.getString(cursor.getColumnIndex(TablePhone.COLUMN_IMEI));
                                int id = cursor.getInt(cursor.getColumnIndex(TablePhone.COLUMN_ID));
                                String modelPhone = cursor.getString(cursor.getColumnIndex(TablePhone.COLUMN_MODEL));
                                int serverSyncro = cursor.getInt(cursor.getColumnIndex(TablePhone.COLUMN_SERVER_SYNCRO));
                                String serverId = cursor.getString(cursor.getColumnIndex(TablePhone.COLUMN_SERVER_ID));

                                phone = new Phone(imei, modelPhone);
                                phone.setServerSyncro(serverSyncro);
                                phone.setServerId(serverId);
                                phone.setLocalId(id);
                                LOG.debug("Il phone di deviceId {} presente nel db", deviceId);
                            }
                        else
                            {
                                LOG.info("Il phone di deviceId {} non presente nel db", deviceId);
                            }
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nella lettura del phone di deviceId {}", deviceId);
                    }
                finally
                    {
                        if (cursor != null)
                            {
                                cursor.close();
                            }
                        db.close();
                    }

                return phone;
            }

        @Override
        public List<Sms> getSms()
            {
                return findAllSms();
            }

        @Override
        public List<Call> getCall()
            {
                return findAllCall();
            }

        @Override
        public void writePhone(Phone phone)
            {
                if (phone != null)
                    {
                        Phone ph = getPhone(phone.getDeviceId());
                        if (ph == null)
                            {
                                ContentValues values = new ContentValues();
                                values.put(TablePhone.COLUMN_IMEI, phone.getDeviceId());
                                values.put(TablePhone.COLUMN_MODEL, phone.getModelPhone());
                                values.put(TablePhone.COLUMN_NUMSIM1, phone.getNumberSim1());
                                values.put(TablePhone.COLUMN_NUMSIM2, phone.getNumberSim2());
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 0);
                                values.put(TablePhone.COLUMN_SERVER_ID, 0);
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);

                                SQLiteDatabase db = this.getWritableDatabase();
                                try
                                    {
                                        db.insertOrThrow(TablePhone.TABLE_NAME, null, values);
                                        LOG.debug("Salvataggio del phone {}", phone.toString());
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error("Problemi nel salvataggio del phone {}", phone.toString());
                                    }
                                finally
                                    {
                                        db.close();
                                    }
                            }
                        else
                            {
                                LOG.info("Già presente nel db il phone {}", phone.toString());
                            }
                    }
                else
                    {
                        LOG.error("Il phone in input è null");
                    }
            }

        @Override
        public void writeSms(Sms sms)
            {
                if (sms == null)
                    {
                        LOG.error("Sms null");
                    }
                else
                    {
                        writeSingleSmsToDb(sms);
                    }
            }

        @Override
        public void writeSms(List<Sms> smss)
            {
                if (smss == null)
                    {
                        LOG.error("Sms null");
                    }
                else
                    {
                        writeMultipleSmsToDb(smss);
                    }
            }

        @Override
        public void writeCall(Call call)
            {
                if (call == null)
                    {
                        LOG.error("Call null");
                    }
                else
                    {
                        writeSingleCallToDb(call);
                    }
            }

        @Override
        public List<Sms> getSmsNotAlreadySendedToServer()
            {
                String selection = TableSms.COLUMN_SERVER_SYNCRO + " = ?";
                String[] selectionArgs = { "0" };

                return findSms(selection, selectionArgs);
            }

        @Override
        public List<Call> getCallNotAlreadySendedToServer()
            {
                String selection = TableCall.COLUMN_SERVER_SYNCRO + " = ?";
                String[] selectionArgs = { "0" };

                return findCall(selection, selectionArgs);
            }
        
        @Override
        public void markLikeSendedToServer(Phone phone, String serverId)
            {
                SQLiteDatabase db = this.getWritableDatabase();
                if (db != null && phone != null)
                    {
                        try
                            {
                                String selection = " id = ?";
                                String[] selectionArgs = { String.valueOf(phone.getLocalId()) };
                                
                                ContentValues values = new ContentValues();
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 1);
                                values.put(TablePhone.COLUMN_SERVER_ID, serverId);
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP,  GregorianCalendar.getInstance().getTimeInMillis());

                                db.update(TablePhone.TABLE_NAME, values, selection, selectionArgs);
                                db.close();
                                LOG.debug("Phone marcato come inviato al server {}", phone.toString());
                            }
                        catch (Exception e)
                            {
                                LOG.error("Problemi nell'aggiornamento del phone {}: {}", phone.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        LOG.error("(db == null) oppure (phone == null)");
                    }
            }
        

        @Override
        public void markLikeSendedToServer(Sms sms, String serverId)
            {
                SQLiteDatabase db = this.getWritableDatabase();
                if (db != null && sms != null)
                    {
                        try
                            {
                                String selection = " id = ?";
                                String[] selectionArgs = { String.valueOf(sms.getLocalId()) };
                                
                                ContentValues values = new ContentValues();
                                values.put(TableSms.COLUMN_SERVER_SYNCRO, 1);
                                values.put(TableSms.COLUMN_SERVER_ID, serverId);
                                values.put(TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP,  GregorianCalendar.getInstance().getTimeInMillis());

                                db.update(TableSms.TABLE_NAME, values, selection, selectionArgs);
                                db.close();
                                LOG.debug("Sms marcato come inviato al server {}", sms.toString());
                            }
                        catch (Exception e)
                            {
                                LOG.error("Problemi nell'aggiornamento del sms {}: {}", sms.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        LOG.error("(db == null) oppure (sms == null)");
                    }
            }

        @Override
        public void markLikeSendedToServer(Call call, String serverId)
            {
                SQLiteDatabase db = this.getWritableDatabase();
                if (db != null && call != null)
                    {
                        try
                            {
                                String selection = " id = ?";
                                String[] selectionArgs = { String.valueOf(call.getLocalId()) };
                                
                                ContentValues values = new ContentValues();
                                values.put(TableCall.COLUMN_SERVER_SYNCRO, 1);
                                values.put(TableCall.COLUMN_SERVER_ID, serverId);
                                values.put(TableCall.COLUMN_SERVER_SYNCRO_TIMESTAMP,  GregorianCalendar.getInstance().getTimeInMillis());

                                db.update(TableCall.TABLE_NAME, values, selection, selectionArgs);
                                db.close();
                                LOG.debug("Call marcato come inviato al server {}", call.toString());
                            }
                        catch (Exception e)
                            {
                                LOG.error("Problemi nell'aggiornamento del call {}: {}", call.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        LOG.error("(db == null) oppure ( call == null)");
                    }
            }

        
        
        
        @Override
        public void markDataForSendedAgainToServer()
            {
                SQLiteDatabase db = this.getWritableDatabase();
                if (db != null)
                    {
                        try
                            {
                                ContentValues values = new ContentValues();
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 0);
                                values.put(TablePhone.COLUMN_SERVER_ID, "");
                                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP,  0);
                                db.update(TablePhone.TABLE_NAME, values, null, null);
                                LOG.debug("Flag sended to server resetted for PHONE");

                                values.put(TableSms.COLUMN_SERVER_SYNCRO, 0);
                                values.put(TableSms.COLUMN_SERVER_ID, "");
                                values.put(TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP,  0);
                                db.update(TableSms.TABLE_NAME, values, null, null);
                                LOG.debug("Flag sended to server resetted for SMS");
                                
                                values.put(TableCall.COLUMN_SERVER_SYNCRO, 0);
                                values.put(TableCall.COLUMN_SERVER_ID, "");
                                values.put(TableCall.COLUMN_SERVER_SYNCRO_TIMESTAMP,  0);
                                db.update(TableCall.TABLE_NAME, values, null, null);
                                LOG.debug("Flag sended to server resetted for CALL");
                                
                                db.close();
                            }
                        catch (Exception e)
                            {
                                LOG.error("Problemi nel reset del flag di inviato al server: {}", e.getMessage());
                            }
                    }
                else
                    {
                        LOG.error("(db == null) oppure ( call == null)");
                    }
            }


        @Override
        public void onCreate(SQLiteDatabase db)
            {
                createTableCall(db);
                createTablePhone(db);
                createTableSms(db);
            }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {
                // NOP

            }

        private List<Sms> findAllSms()
            {
                return findSms(null, null);
            }

        private List<Sms> findSms(String selection, String[] selectionArgs)
            {
                List<Sms> smss = new ArrayList<Sms>();

                String table = TableSms.TABLE_NAME;
                String[] columns = null; // all columns
                // String selection = null; // all rows
                // String[] selectionArgs = null;
                String groupBy = null;
                String having = null;
                String orderBy = TableSms.COLUMN_TIMESTAMP + " ASC";

                SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = null;
                try
                    {
                        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
                        while (cursor.moveToNext())
                            {
                                int localId = cursor.getInt(cursor.getColumnIndex("id"));
                                String direction = cursor.getString(cursor.getColumnIndex(TableSms.COLUMN_DIRECTION));
                                String phoneNumber = cursor.getString(cursor.getColumnIndex(TableSms.COLUMN_PHONENUMBER));
                                String contactName = cursor.getString(cursor.getColumnIndex(TableSms.COLUMN_CONTACT));
                                long timestamp = cursor.getLong(cursor.getColumnIndex(TableSms.COLUMN_TIMESTAMP));
                                String text = cursor.getString(cursor.getColumnIndex(TableSms.COLUMN_TEXT));
                                int serverSyncro = cursor.getInt(cursor.getColumnIndex(TableSms.COLUMN_SERVER_SYNCRO));
                                String serverId = cursor.getString(cursor.getColumnIndex(TableSms.COLUMN_SERVER_ID));
                                long serverSyncroTimestamp = cursor.getLong(cursor.getColumnIndex(TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP));

                                Sms sms = SmsFactory.sms(localId, direction, phoneNumber, timestamp, text);
                                sms.setNameContact(contactName);
                                sms.setServerId(serverId);
                                sms.setServerSyncro(serverSyncro);
                                sms.setServerSyncroTimestamp(serverSyncroTimestamp);
                                smss.add(sms);
                            }
                        LOG.info("Recuperato {} sms dal db", smss.size());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nella lettura degli sms");
                    }
                finally
                    {
                        if (cursor != null)
                            {
                                cursor.close();
                            }
                        db.close();
                    }
                return smss;
            }

        private List<Call> findAllCall()
            {
                return findCall(null, null);
            }

        private List<Call> findCall(String selection, String[] selectionArgs)
            {
                List<Call> calls = new ArrayList<Call>();

                String table = TableCall.TABLE_NAME;
                String[] columns = null; // all columns
                // String selection = null; // all rows
                // String[] selectionArgs = null;
                String groupBy = null;
                String having = null;
                String orderBy = TableCall.COLUMN_TIMESTAMP_START + " ASC, " + TableCall.COLUMN_TIMESTAMP_END + " ASC";

                SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = null;
                try
                    {
                        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
                        while (cursor.moveToNext())
                            {
                                int localId = cursor.getInt(cursor.getColumnIndex("id"));
                                String direction = cursor.getString(cursor.getColumnIndex(TableCall.COLUMN_DIRECTION));
                                String state = cursor.getString(cursor.getColumnIndex(TableCall.COLUMN_STATE));
                                String phoneNumber = cursor.getString(cursor.getColumnIndex(TableCall.COLUMN_PHONENUMBER));
                                String contactName = cursor.getString(cursor.getColumnIndex(TableCall.COLUMN_CONTACT));
                                long timestampStart = cursor.getLong(cursor.getColumnIndex(TableCall.COLUMN_TIMESTAMP_START));
                                long timestampEnd = cursor.getLong(cursor.getColumnIndex(TableCall.COLUMN_TIMESTAMP_END));
                                int serverSyncro = cursor.getInt(cursor.getColumnIndex(TableCall.COLUMN_SERVER_SYNCRO));
                                int serverId = cursor.getInt(cursor.getColumnIndex(TableCall.COLUMN_SERVER_ID));
                                long serverSyncroTimestamp = cursor.getLong(cursor.getColumnIndex(TableCall.COLUMN_SERVER_SYNCRO_TIMESTAMP));

                                Call call = CallFactory.getInstance().call(localId, phoneNumber, contactName, timestampStart, timestampEnd, direction, state);
                                call.setServerId(serverId);
                                call.setServerSyncro(serverSyncro);
                                call.setServerSyncroTimestamp(serverSyncroTimestamp);
                                calls.add(call);
                            }
                        LOG.info("Recuperato {} call dal db", calls.size());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nella lettura delle call");
                    }
                finally
                    {
                        if (cursor != null)
                            {
                                cursor.close();
                            }
                        db.close();
                    }
                return calls;
            }

        private void writeMultipleSmsToDb(List<Sms> smss)
            {
                try
                    {
                        SQLiteDatabase db = this.getWritableDatabase();
                        if (db != null)
                            {
                                LOG.debug("Db opened");
                                for (Sms smsMessage : smss)
                                    {
                                        writeSingleSmsToDb(db, smsMessage);
                                    }
                                db.close();
                                LOG.debug("Db closed");
                            }
                    }
                catch (Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        private void writeSingleSmsToDb(Sms sms)
            {
                try
                    {
                        SQLiteDatabase db = this.getWritableDatabase();
                        if (db != null)
                            {
                                LOG.debug("Db opened");
                                writeSingleSmsToDb(db, sms);
                                db.close();
                                LOG.debug("Db closed");
                            }
                    }
                catch (Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        private void writeSingleSmsToDb(SQLiteDatabase db, Sms sms)
            {
                if (db != null && sms != null)
                    {
                        try
                            {
                                ContentValues values = new ContentValues();
                                values.put(TableSms.COLUMN_DIRECTION, sms.getDirection().name());
                                values.put(TableSms.COLUMN_TIMESTAMP, sms.getTimestamp());
                                values.put(TableSms.COLUMN_PHONENUMBER, sms.getPhoneNumber());
                                values.put(TableSms.COLUMN_CONTACT, sms.getNameContact());
                                values.put(TableSms.COLUMN_TEXT, sms.getText());
                                values.put(TableSms.COLUMN_SERVER_SYNCRO, 0);
                                values.put(TableSms.COLUMN_SERVER_ID, 0);
                                values.put(TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);

                                db.insertOrThrow(TableSms.TABLE_NAME, null, values);
                                LOG.debug("Salvataggio del sms {}", sms.toString());
                            }
                        catch (Exception e)
                            {
                                LOG.error("Problemi nel salvataggio del sms {}: {}", sms.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        LOG.error("(outputStream == null) oppure (sms == null)");
                    }
            }

        private void writeSingleCallToDb(Call call)
            {
                try
                    {
                        SQLiteDatabase db = this.getWritableDatabase();
                        LOG.debug("Db opened");
                        ContentValues values = new ContentValues();
                        values.put(TableCall.COLUMN_DIRECTION, call.getDirection().name());
                        values.put(TableCall.COLUMN_TIMESTAMP_START, call.getTimestampStartCall());
                        values.put(TableCall.COLUMN_TIMESTAMP_END, call.getTimestampEndCall());
                        values.put(TableCall.COLUMN_PHONENUMBER, call.getPhoneNumber());
                        values.put(TableCall.COLUMN_CONTACT, call.getNameContact());
                        values.put(TableCall.COLUMN_DURATION, call.getCallDurationSec());
                        values.put(TableCall.COLUMN_STATE, call.getState().name());
                        values.put(TableCall.COLUMN_SERVER_SYNCRO, 0);
                        values.put(TableCall.COLUMN_SERVER_ID, 0);
                        values.put(TableCall.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);

                        db.insertOrThrow(TableCall.TABLE_NAME, null, values);

                        LOG.debug("Salvataggio call {}", call.toString());
                        db.close();
                        LOG.debug("Db closed");
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nel salvataggio della call {}: ", call.toString(), e.getMessage());
                    }
            }

        private void createTableSms(SQLiteDatabase db)
            {

                String create = "";
                create += "CREATE TABLE " + TableSms.TABLE_NAME + " (";
                create += "  " + TableSms.COLUMN_ID + " INTEGER PRIMARY KEY,";
                create += "  " + TableSms.COLUMN_DIRECTION + " TEXT NOT NULL,";
                create += "  " + TableSms.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,";
                create += "  " + TableSms.COLUMN_PHONENUMBER + " TEXT NOT NULL,";
                create += "  " + TableSms.COLUMN_CONTACT + " TEXT NOT NULL,";
                create += "  " + TableSms.COLUMN_TEXT + " TEXT NOT NULL,";
                create += "  " + TableSms.COLUMN_SERVER_SYNCRO + " INTEGER NOT NULL DEFAULT 0,";
                create += "  " + TableSms.COLUMN_SERVER_ID + " INTEGER NOT NULL DEFAULT 0, ";
                create += "  " + TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP + " TIMESTAMP";
                create += ")";
                db.execSQL(create);
                LOG.debug("Create table {}", TableSms.TABLE_NAME);
            }

        private void createTableCall(SQLiteDatabase db)
            {
                String create = "";
                create += "CREATE TABLE " + TableCall.TABLE_NAME + " (";
                create += "  " + TableCall.COLUMN_ID + " INTEGER PRIMARY KEY,";
                create += "  " + TableCall.COLUMN_DIRECTION + " TEXT NOT NULL,";
                create += "  " + TableCall.COLUMN_TIMESTAMP_START + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,";
                create += "  " + TableCall.COLUMN_TIMESTAMP_END + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,";
                create += "  " + TableCall.COLUMN_PHONENUMBER + " TEXT NOT NULL,";
                create += "  " + TableCall.COLUMN_CONTACT + " TEXT NOT NULL,";
                create += "  " + TableCall.COLUMN_STATE + " TEXT NOT NULL,";
                create += "  " + TableCall.COLUMN_DURATION + " INTEGER DEFAULT 0,";
                create += "  " + TableCall.COLUMN_SERVER_SYNCRO + " INTEGER NOT NULL DEFAULT 0,";
                create += "  " + TableCall.COLUMN_SERVER_ID + " INTEGER NOT NULL DEFAULT 0, ";
                create += "  " + TableCall.COLUMN_SERVER_SYNCRO_TIMESTAMP + " TIMESTAMP";

                create += ")";
                db.execSQL(create);
                LOG.debug("Create table {}", TableCall.TABLE_NAME);
            }

        private void createTablePhone(SQLiteDatabase db)
            {
                String create = "";
                create += "CREATE TABLE " + TablePhone.TABLE_NAME + " (";
                create += "  " + TablePhone.COLUMN_ID + " INTEGER PRIMARY KEY,";
                create += "  " + TablePhone.COLUMN_IMEI + " TEXT NOT NULL,";
                create += "  " + TablePhone.COLUMN_MODEL + " TEXT NOT NULL,";
                create += "  " + TablePhone.COLUMN_NUMSIM1 + " TEXT NOT NULL,";
                create += "  " + TablePhone.COLUMN_NUMSIM2 + " TEXT NOT NULL,";
                create += "  " + TablePhone.COLUMN_SERVER_SYNCRO + " INTEGER NOT NULL DEFAULT 0,";
                create += "  " + TablePhone.COLUMN_SERVER_ID + " INTEGER NOT NULL DEFAULT 0, ";
                create += "  " + TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP + " TIMESTAMP";
                create += ")";
                db.execSQL(create);
                LOG.debug("Create table {}", TablePhone.TABLE_NAME);

            }

    }
