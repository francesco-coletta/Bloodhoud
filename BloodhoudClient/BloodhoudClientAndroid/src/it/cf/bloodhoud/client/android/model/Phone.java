package it.cf.bloodhoud.client.android.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Phone {
	
	private long id=-1;
	private String deviceId = null;
	private String modelPhone = null;
	private String numberSim1="";
	private String numberSim2="";
	
	
	public Phone(String deviceId, String modelPhone) {
		super();
		this.deviceId = StringUtils.trimToEmpty(deviceId);
		this.modelPhone = StringUtils.trimToEmpty(modelPhone);
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getNumberSim1() {
		return numberSim1;
	}


	public void setNumberSim1(String numberSim1) {
		this.numberSim1 = numberSim1;
	}


	public String getNumberSim2() {
		return numberSim2;
	}


	public void setNumberSim2(String numberSim2) {
		this.numberSim2 = numberSim2;
	}


	public String getDeviceId() {
		return deviceId;
	}


	public String getModelPhone() {
		return modelPhone;
	}
	
	@Override
	public String toString()
		{
			ToStringBuilder toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
			toString.append("id", id);
			toString.append(" Imei", deviceId);
			toString.append(" Model", modelPhone);
			toString.append(" SIM1", this.numberSim1);
			toString.append(" SIM2", this.numberSim2);
			return toString.build();
		}
}
