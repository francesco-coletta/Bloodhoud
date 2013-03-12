package it.cf.bloodhoud.client.android.activity;

import it.cf.bloodhoud.client.android.R;
import it.cf.bloodhoud.client.android.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DummyActivity extends Activity implements OnClickListener
    {

        static private final Logger LOG = LoggerFactory.getLogger(DummyActivity.class);
        TextView                    messaggioEsitoTask;

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                this.setContentView(R.layout.activity_dummy);
                Button buttonOk = (Button) findViewById(R.dummyActivity.buttonOk);
                buttonOk.setOnClickListener(this);

                TextView labelSecret = (TextView) findViewById(R.dummyActivity.labelSecret);
                labelSecret.setOnClickListener(this);

                TextView labelMessage = (TextView) findViewById(R.dummyActivity.labelMessage);
                String infoDevice = "Telephone info:\n\nImei: " + Utils.getDeviceId(this) + "\nName: " + Utils.getDeviceName() + "\n\n\n\n";
                labelMessage.setText(infoDevice);
            }

        @Override
        public void onClick(View v)
            {
                if (v.getId() == R.dummyActivity.labelSecret)
                    {
                        this.startActivity(new Intent(this, AccessCallSmsListenerActivity.class));
                    }
                this.finish();
            }
    }
