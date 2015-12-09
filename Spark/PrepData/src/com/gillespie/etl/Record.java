package com.gillespie.etl;
import scala.Serializable;


/**
 * 
 * @author Brian
 *
 *	Record class used to hold the desired featureset from the input data
 *	The providerType and npi are used to define unique doctors of a type
 */
public class Record implements Serializable{
    protected String npi;
    protected String providerType;
    protected String hcpcsCode;
    public String key;

    /**
     * 
     * @param npi
     * @param providerType
     * @param hcpcsCode
     */
    public Record(String npi, String providerType, String hcpcsCode){
	this.npi = npi;
	this.providerType = providerType;
	this.hcpcsCode = hcpcsCode;
	this.key = this.npi + ":" + this.providerType;
    }

    /**
     * 
     * @return
     */
    public String[] splitKey(){
	return this.key.split(":");
    }

    /**
     * 
     * @param providerType
     * @param hcpcsCode
     */
    public Record(String providerType, String hcpcsCode){
	this.npi = null;
	this.providerType = providerType;
	this.hcpcsCode = hcpcsCode;
	this.key = null;
    }

    /**
     * 
     * @return
     */
    public Boolean isValidRecord(){
	if (this == null || this == null){
	    return false;
	}
	else if (this.npi == null || this.npi.equals("") ||
		this.providerType == null || this.providerType.equals("") ||
		this.hcpcsCode == null || this.hcpcsCode.equals("") ||
		this.key == null || this.key.equals("") || this.key.split(":")[1] == "" || 
		this.key.split(":")[0] == ""){
	    return false;
	}
	else{
	    return true;
	}

    }

    /**
     * 
     * @param record
     * @return
     */
    public Boolean equals(Record record){
	if (record == null || this == null){
	    return false;
	} else {
	    return this.providerType.equalsIgnoreCase(record.getProviderType()) &&
		    this.hcpcsCode.equalsIgnoreCase(record.getHCPCSCode());
	}
    }
    
    /**
     * 
     * @param providerType
     */
    public void setProviderType(String providerType){
	this.providerType = providerType;
    }

    /**
     * 
     * @param hcpcsCode
     */
    public void setHCPCSCode(String hcpcsCode){
	this.hcpcsCode = hcpcsCode;
    }

    /**
     * 
     * @param key
     */
    public void setKey(String key){
	this.key = key;
    }

    public String toString(){
	return "[" + this.providerType + ", " + this.hcpcsCode + "]";
    }

    public String getKey(){
	return this.key;
    }

    public String getProviderType(){
	return this.providerType;
    }

    public String getHCPCSCode(){
	return this.hcpcsCode;
    }
}