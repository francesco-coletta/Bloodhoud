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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;

public class RepositoryLocalSQLLite extends SQLiteOpenHelper implements RepositoryLocalWrite, RepositoryLocalRead
    {
        static private final Logger           LOG  = LoggerFactory.getLogger(RepositoryLocalSQLLite.class);

        static private RepositoryLocalSQLLite repo = null;
        
        static private SQLiteDatabase dbWrite = null;

        private RepositoryLocalSQLLite(Context context)
            {
                super(context, App.DATABASE_NAME, null, App.DATABASE_VERSION);
            }

        static public synchronized RepositoryLocalSQLLite getRepository(Context context)
            {
                if (repo == null)
                    {
                        repo = new RepositoryLocalSQLLite(context);
                    }
                return repo;
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

                SQLiteDatabase db = null;
                Cursor cursor = null;
                try
                    {
                        db = openDbInRead();
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
                                LOG.debug("Il phone di deviceId {} presente nel db {}", deviceId, phone.toString());
                            }
                        else
                            {
                                LOG.warn("Il phone di deviceId {} non presente nel db", deviceId);
                            }
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nella lettura del phone di deviceId {}: {}", deviceId, e.getMessage());
                    }
                finally
                    {
                        if (cursor != null)
                            {
                                cursor.close();
                            }
                        closeDb(db);
                    }

                return phone;
            }

        @Override
        public List<Sms> getSms()
            {
                List<Sms> sms = findAllSms();
                LOG.debug("Recuperati dal db {} sms", sms.size());
                return sms;
            }

        @Override
        public List<Call> getCall()
            {
                List<Call> call = findAllCall();
                LOG.debug("Recuperati dal db {} call", call.size());
                return call;
            }

        @Override
        public long writePhone(Phone phone)
            {
                long id = -1;
                if (phone != null)
                    {
                        LOG.debug("Verifico se il Phone è già prsente nel DB. {}", phone.toString());
                        Phone ph = getPhone(phone.getDeviceId());
                        if (ph == null)
                            {
                                LOG.debug("Phone non prsente nel DB lo inserisco. {}", phone.toString());
                                try
                                    {
                                        id = insertPhoneIntoTable(phone);
                                        phone.setLocalId((int) id);
                                        LOG.info("Writed into DB {}", phone.toString());
                                    }
                                catch (Exception e)
                                    {
                                        id = -1;
                                        LOG.error("Problemi nel salvataggio del {}. {}", phone.toString(), e.getMessage());
                                    }
                            }
                        else
                            {
                                id = ph.getLocalId();
                                LOG.info("Già presente nel db il {}", phone.toString());
                            }
                    }
                else
                    {
                        id = -1;
                        LOG.error("Il phone in input è null");
                    }
                return id;
            }

        @Override
        public long writeSms(Sms sms)
            {
                long id = -1;
                if (sms != null)
                    {
                        try
                            {
                                id = insertSmsIntoTable(sms);
                                sms.setLocalId((int) id);
                                LOG.info("Writed into DB {}", sms.toString());
                            }
                        catch (Exception e)
                            {
                                id = -1;
                                LOG.error("Problemi nel salvataggio del {}. {}", sms.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        id = -1;
                        LOG.error("Sms in input è null");
                    }
                return id;
            }

        @Override
        public long writeCall(Call call)
            {
                long id = -1;
                if (call != null)
                    {
                        try
                            {
                                id = insertCallIntoTable(call);
                                call.setLocalId((int) id);
                                LOG.info("Writed into DB {}", call.toString());
                            }
                        catch (Exception e)
                            {
                                id = -1;
                                LOG.error("Problemi nel salvataggio del {}. {}", call.toString(), e.getMessage());
                            }
                    }
                else
                    {
                        id = -1;
                        LOG.error("Sms in input è null");
                    }
                return id;
            }

        @Override
        public List<Sms> getSmsNotAlreadySendedToServer()
            {
                String selection = TableSms.COLUMN_SERVER_SYNCRO + " = ?";
                String[] selectionArgs = { "0" };
                List<Sms> sms = findSms(selection, selectionArgs);
                LOG.debug("Recuperati dal db {} sms non inviati al server", sms.size());
                return sms;
            }

        @Override
        public List<Call> getCallNotAlreadySendedToServer()
            {
                String selection = TableCall.COLUMN_SERVER_SYNCRO + " = ?";
                String[] selectionArgs = { "0" };
                List<Call> call = findCall(selection, selectionArgs);
                LOG.debug("Recuperati dal db {} call non inviati al server", call.size());
                return call;
            }

        @Override
        public void markLikeSendedToServer(Phone phone, String serverId)
            {
                try
                    {
                        markLikeSendedToServer(((phone != null) ? phone.getLocalId() : -1), serverId, TablePhone.TABLE_NAME);
                        LOG.debug("Phone marcato come inviato al server {}", phone.toString());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nell'aggiornamento del phone {}: {}", ((phone != null) ? phone.toString() : "sms = null"), e.getMessage());
                    }
            }

        @Override
        public void markLikeSendedToServer(Sms sms, String serverId)
            {
                try
                    {
                        markLikeSendedToServer(((sms != null) ? sms.getLocalId() : -1), serverId, TableSms.TABLE_NAME);
                        LOG.debug("Sms marcato come inviato al server {}", sms.toString());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nell'aggiornamento del sms {}: {}", ((sms != null) ? sms.toString() : "sms = null"), e.getMessage());
                    }
            }

        @Override
        public void markLikeSendedToServer(Call call, String serverId)
            {
                try
                    {
                        markLikeSendedToServer(((call != null) ? call.getLocalId() : -1), serverId, TableCall.TABLE_NAME);
                        LOG.debug("Sms marcato come inviato al server {}", call.toString());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nell'aggiornamento del sms {}: {}", ((call != null) ? call.toString() : "call = null"), e.getMessage());
                    }
            }

        @Override
        public void markDataForSendedAgainToServer()
            {
                SQLiteDatabase db = null;
                try
                    {
                        db = openDbInWrite();
                        ContentValues values = getContentValuesForUpdateLikeNotSendedToServer();
                        db.update(TablePhone.TABLE_NAME, values, null, null);
                        LOG.debug("Flag sended to server resetted for PHONE");

                        db.update(TableSms.TABLE_NAME, values, null, null);
                        LOG.debug("Flag sended to server resetted for SMS");

                        db.update(TableCall.TABLE_NAME, values, null, null);
                        LOG.debug("Flag sended to server resetted for CALL");
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nel reset del flag di inviato al server: {}", e.getMessage());
                    }
                finally
                    {
                        closeDb(db);
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

        // PRIVATE

        // metodi per apertura del DB
        private SQLiteDatabase openDbInRead() throws SQLiteException
            {
                return openDbInWrite();
            }

        private SQLiteDatabase openDbInWrite() throws SQLiteException
            {
                if (dbWrite == null){
                    dbWrite = openDbWithRetry(true);
                }
                if (!dbWrite.isOpen()){
                    dbWrite = openDbWithRetry(true);
                }
                return dbWrite;
            }

        private SQLiteDatabase openDbWithRetry(final boolean writable) throws SQLiteException
            {
                final int numRetry = 10;
                SQLiteDatabase db = null;
                int indexTries = 1;
                while ((db == null) && (indexTries <= numRetry))
                    {
                        try
                            {
                                db = openDb(writable);
                            }
                        catch (SQLiteException e)
                            {
                                db = null;
                                LOG.error("Tentativo {} apertura DB in errore, attendo e riprovo. Errore: {}", String.valueOf(indexTries), e.getMessage());
                                SystemClock.sleep(200);
                            }
                        indexTries++;
                    }

                if (db == null)
                    {
                        StringBuilder message = new StringBuilder();
                        message.append("Impossibile aprire in ").append((writable ? "WRITE" : "READ")).append(" DB dopo ").append(String.valueOf(numRetry)).append(" tentativi");
                        LOG.error(message.toString());
                        throw new SQLiteException(message.toString());
                    }
                LOG.debug("Dabatase APERTO in {}", (writable ? "WRITE" : "READ"));
                return db;
            }

        private SQLiteDatabase openDb(boolean writable) throws SQLiteException
            {
                SQLiteDatabase db = null;
                try
                    {
                        if (writable)
                            {
                                //db = this.getReadableDatabase();
                                db = this.getWritableDatabase();
                            }
                        else
                            {
                                db = this.getWritableDatabase();
                            }
                        LOG.debug("DB aperto in {}", (writable ? "WRITE" : "READ"));
                    }
                catch (SQLiteException e)
                    {
                        db = null;
                        StringBuilder message = new StringBuilder();
                        message.append("Problemi nell'apertura del DB in ").append((writable ? "scrittura" : "lettura")).append(" ").append(e.getMessage());
                        LOG.error(message.toString());
                        throw new SQLiteException(message.toString());
                    }
                return db;
            }

        private void closeDb(SQLiteDatabase db)
            {
                // this.close();
                if (db != null)
                    {
                        if (db.isOpen())
                            {
                                //db.close();
                                LOG.debug("Dabatase CHIUSO (non viene chiuso realmente)");
                            }
                        else
                            {
                                LOG.warn("Dabatase già CHIUSO");
                            }
                    }
                else
                    {
                        LOG.error("Dabatase is null");
                    }
            }

        // scrittura PHONE

        private ContentValues getContentValuesFromPhone(Phone phone) throws SQLiteException
            {

                if (phone == null)
                    {
                        throw new SQLiteException("phone == null");
                    }

                ContentValues values = new ContentValues();
                values.put(TablePhone.COLUMN_IMEI, phone.getDeviceId());
                values.put(TablePhone.COLUMN_MODEL, phone.getModelPhone());
                values.put(TablePhone.COLUMN_NUMSIM1, phone.getNumberSim1());
                values.put(TablePhone.COLUMN_NUMSIM2, phone.getNumberSim2());
                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 0);
                values.put(TablePhone.COLUMN_SERVER_ID, 0);
                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);
                return values;
            }

        private ContentValues getContentValuesForUpdateLikeSendedToServer(String serverId) throws SQLiteException
            {
                if (StringUtils.isBlank(serverId))
                    {
                        throw new SQLiteException("serverId is blank");
                    }
                ContentValues values = new ContentValues();
                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 1);
                values.put(TablePhone.COLUMN_SERVER_ID, serverId);
                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP, GregorianCalendar.getInstance().getTimeInMillis());
                return values;
            }

        private ContentValues getContentValuesForUpdateLikeNotSendedToServer() throws SQLiteException
            {
                ContentValues values = new ContentValues();
                values.put(TablePhone.COLUMN_SERVER_SYNCRO, 0);
                values.put(TablePhone.COLUMN_SERVER_ID, "");
                values.put(TablePhone.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);
                return values;
            }

        private ContentValues getContentValuesFromSms(Sms sms) throws SQLiteException
            {

                if (sms == null)
                    {
                        throw new SQLiteException("sms == null");
                    }
                ContentValues values = new ContentValues();
                values.put(TableSms.COLUMN_DIRECTION, sms.getDirection().name());
                values.put(TableSms.COLUMN_TIMESTAMP, sms.getTimestamp());
                values.put(TableSms.COLUMN_PHONENUMBER, sms.getPhoneNumber());
                values.put(TableSms.COLUMN_CONTACT, sms.getNameContact());
                values.put(TableSms.COLUMN_TEXT, sms.getText());
                values.put(TableSms.COLUMN_SERVER_SYNCRO, 0);
                values.put(TableSms.COLUMN_SERVER_ID, 0);
                values.put(TableSms.COLUMN_SERVER_SYNCRO_TIMESTAMP, 0);
                return values;
            }

        private ContentValues getContentValuesFromCall(Call call) throws SQLiteException
            {

                if (call == null)
                    {
                        throw new SQLiteException("call == null");
                    }
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
                return values;
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

                SQLiteDatabase db = null;
                Cursor cursor = null;
                try
                    {
                        db = openDbInRead();
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
                        LOG.debug("Recuperato {} sms dal db", smss.size());
                    }
                catch (Exception e)
                    {
                        LOG.error("Problemi nella lettura degli sms. {}", e.getMessage());
                    }
                finally
                    {
                        if (cursor != null)
                            {
                                cursor.close();
                            }
                        closeDb(db);
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

                SQLiteDatabase db = null;
                Cursor cursor = null;
                try
                    {
                        db = openDbInRead();
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
                        LOG.debug("Recuperato {} call dal db", calls.size());
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
                        closeDb(db);
                    }
                return calls;
            }

        private long insertPhoneIntoTable(Phone phone) throws SQLiteException
            {
                if (phone == null)
                    {
                        throw new SQLiteException("phone == null");
                    }

                ContentValues values = getContentValuesFromPhone(phone);

                long id = -1;
                try
                    {
                        id = insertIntoTableWithRetry(TablePhone.TABLE_NAME, values);
                    }
                catch (SQLException e)
                    {
                        id = -1;
                        LOG.error("Problemi inserimento {}. {}", phone.toString(), e.getMessage());
                        throw new SQLiteException("Problemi durate inserimento  " + phone.toString() + ". Errore: " + e.getMessage());
                    }
                return id;
            }

        private long insertCallIntoTable(Call call) throws SQLiteException
            {
                if (call == null)
                    {
                        throw new SQLiteException("call == null");
                    }

                ContentValues values = getContentValuesFromCall(call);

                long id = -1;
                try
                    {
                        id = insertIntoTableWithRetry(TableCall.TABLE_NAME, values);
                    }
                catch (SQLException e)
                    {
                        id = -1;
                        LOG.error("Problemi inserimento {}. {}", call.toString(), e.getMessage());
                        throw new SQLiteException("Problemi durate inserimento  " + call.toString() + ". Errore: " + e.getMessage());
                    }
                return id;
            }

        private long insertSmsIntoTable(Sms sms) throws SQLiteException
            {
                if (sms == null)
                    {
                        throw new SQLiteException("sms == null");
                    }

                ContentValues values = getContentValuesFromSms(sms);

                long id = -1;
                try
                    {
                        id = insertIntoTableWithRetry(TableSms.TABLE_NAME, values);
                    }
                catch (SQLException e)
                    {
                        id = -1;
                        LOG.error("Problemi inserimento {}. {}", sms.toString(), e.getMessage());
                        throw new SQLiteException("Problemi durate inserimento  " + sms.toString() + ". Errore: " + e.getMessage());
                    }
                return id;
            }

        private synchronized long insertIntoTable(final String tableName, final ContentValues values) throws SQLiteException
            {

                if (StringUtils.isBlank(tableName))
                    {
                        throw new SQLiteException("tableName is empty");
                    }
                if (values == null)
                    {
                        throw new SQLiteException("values == null");
                    }

                long id = -1;
                try
                    {
                        SQLiteDatabase db = openDbInWrite();
                        id = db.insert(tableName, null, values);
                        closeDb(db);
                        if (id < 0)
                            {
                                LOG.error("Problemi insert nella tabella {}.", tableName);
                            }
                        else
                            {
                                LOG.debug("Insert into table {} OK", tableName);
                            }
                    }
                catch (SQLException e)
                    {
                        id = -1;
                        LOG.error("Problemi durate inserimento nella tabella {}. {}", tableName, e.getMessage());
                        throw new SQLiteException("Problemi durate inserimento nella tabella  " + tableName + ". Errore: " + e.getMessage());
                    }
                return id;
            }

        private long insertIntoTableWithRetry(final String tableName, final ContentValues values) throws SQLiteException
            {
                // input check fatto in insertIntoTable
                long id = -1;
                final int numMaxRetry = 10;
                int indexRetry = 1;
                while ((id < 0) && (indexRetry <= numMaxRetry))
                    {
                        try
                            {
                                id = insertIntoTable(tableName, values);
                            }
                        catch (SQLiteException e)
                            {
                                id = -1;
                                LOG.error("Tentativo {} di insert in tabella, attendo e riprovo. Errore: {}", String.valueOf(indexRetry), e.getMessage());
                                SystemClock.sleep(200);
                            }
                        indexRetry++;
                    }

                if (id < 0)
                    {
                        StringBuilder message = new StringBuilder();
                        message.append("Impossibile insert in tabella ").append(tableName).append(" dopo ").append(String.valueOf(numMaxRetry)).append(" tentativi");
                        LOG.error(message.toString());
                        throw new SQLiteException(message.toString());
                    }
                return id;
            }

        private void markLikeSendedToServer(final int id, String serverId, final String tableName) throws SQLiteException
            {
                if (id < 0)
                    {
                        throw new SQLiteException("Input errato: id < 0");
                    }
                if (StringUtils.isBlank(serverId))
                    {
                        throw new SQLiteException("ServerId is empty");
                    }
                if (StringUtils.isBlank(tableName))
                    {
                        throw new SQLiteException("tableName is empty");
                    }

                try
                    {
                        String selection = " id = ?";
                        String[] selectionArgs = { String.valueOf(id) };
                        ContentValues values = getContentValuesForUpdateLikeSendedToServer(serverId);

                        int numRecordUpdated = 0;
                        final int numMaxRetry = 10;
                        int indexRetry = 1;
                        while ((numRecordUpdated < 1) && (indexRetry <= numMaxRetry))
                            {
                                numRecordUpdated = updateRecordIntoTable(tableName, values, selection, selectionArgs);
                                if (numRecordUpdated == 0)
                                    {
                                        LOG.warn(
                                            "Tentativo {} update tabella {} senza effetto: nessun record modificato. Riprovo dopo un wait.",
                                            String.valueOf(indexRetry),
                                            tableName);
                                        SystemClock.sleep(200);
                                    }
                                indexRetry++;
                            }

                        if (numRecordUpdated < 1)
                            {
                                StringBuilder message = new StringBuilder();
                                message.append("Tabella ").append(tableName).append(" non aggiornata per id = ").append(String.valueOf(id)).append(" dopo ")
                                        .append(String.valueOf(numMaxRetry)).append(" tentativi");
                                LOG.error(message.toString());
                                throw new SQLiteException(message.toString());
                            }
                    }
                catch (Exception e)
                    {
                        StringBuilder message = new StringBuilder();
                        message.append("Problemi nell'aggiornamento della tabella ").append(tableName).append(" per id = ").append(String.valueOf(id)).append(". Errore: ")
                                .append(e.getMessage());
                        LOG.error(message.toString());
                        throw new SQLiteException(message.toString());
                    }
            }

        private synchronized int updateRecordIntoTable(final String tableName, final ContentValues values, final String selection, final String[] selectionArgs)
            {
                if (StringUtils.isBlank(tableName))
                    {
                        throw new SQLiteException("tableName is empty");
                    }
                if (values == null)
                    {
                        throw new SQLiteException("values == null");
                    }

                SQLiteDatabase db = openDbInWrite();
                int numRecordUpdated = 0;
                numRecordUpdated = db.update(tableName, values, selection, selectionArgs);
                closeDb(db);
                LOG.debug("Aggiornati {} record nella table {}", String.valueOf(numRecordUpdated), tableName);
                if (numRecordUpdated == 0)
                    {
                        LOG.warn("Nessun record aggiornato nella tabella {}.", tableName);
                    }
                return numRecordUpdated;
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
