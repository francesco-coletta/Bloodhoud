package it.cf.bloodhoud.client.android;

import java.util.List;

public interface Repository
	{

		void writeSms(Sms sms);

		void writeSms(List<Sms> sms);

		void writeCall(Call call);

	}
