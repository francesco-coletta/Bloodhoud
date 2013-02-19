package it.cf.bloodhoud.client.android.serviceApp;

import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.model.Sms;

import java.util.List;

public interface RepositoryLocalWrite
	{
		void writePhone(Phone phone);
		void writeSms(Sms sms);
		void writeSms(List<Sms> sms);
		void writeCall(Call call);
		void markLikeSendedToServer(Phone phone, String serverId);
		void markLikeSendedToServer(Sms sms, String serverId);
		void markLikeSendedToServer(Call call, String serverId);
		void markDataForSendedAgainToServer();
	}
