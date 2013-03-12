package it.cf.bloodhoud.client.android.serviceApp;

import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.model.Sms;

public interface RepositoryLocalWrite
	{
		long writePhone(Phone phone);
		long writeSms(Sms sms);
		//void writeSms(List<Sms> sms);
		long writeCall(Call call);
		void markLikeSendedToServer(Phone phone, String serverId);
		void markLikeSendedToServer(Sms sms, String serverId);
		void markLikeSendedToServer(Call call, String serverId);
		void markDataForSendedAgainToServer();
		void markPhoneLikeNotSendedToServer();
	}
