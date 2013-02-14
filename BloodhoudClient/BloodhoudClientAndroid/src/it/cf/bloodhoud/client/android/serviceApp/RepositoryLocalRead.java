package it.cf.bloodhoud.client.android.serviceApp;

import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.model.Sms;

import java.util.List;

public interface RepositoryLocalRead
	{
		Phone getPhone(String deviceId);
		List<Sms> getSms();
		List<Call> getCall();
	}
