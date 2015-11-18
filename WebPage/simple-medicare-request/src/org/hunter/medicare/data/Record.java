package org.hunter.medicare.data;

//import org.apache.spark.api.java.function.*;
//import org.apache.spark.api.java.*;
import scala.Serializable;


public class Record implements Serializable{
    protected String npi;
    protected String providerType;
    protected String hcpcsCode;
    public String key;
    
    public Record(String npi, String providerType, String hcpcsCode){
	this.npi = npi;
	this.providerType = providerType;
	this.hcpcsCode = hcpcsCode;
	this.key = this.npi + "," + this.providerType;
    }
    
    public Record(String providerType, String hcpcsCode){
	this.npi = null;
	this.providerType = providerType;
	this.hcpcsCode = hcpcsCode;
	this.key = null;
    }
 
    public Boolean equals(Record record){
	return this.providerType.equalsIgnoreCase(record.getProviderType()) &&
		this.hcpcsCode.equalsIgnoreCase(record.getHCPCSCode());
    }
    
    public String toString(){
	return "[" + this.providerType + ", " + this.hcpcsCode + "]";
    }
    
    public String getKey(){
	return this.key;
    }
    
    public void setKey(String key){
	this.key = key;
    }
    
    public String getProviderType(){
	return this.providerType;
    }
    
    public String getHCPCSCode(){
	return this.hcpcsCode;
    }
    
    public void setProviderType(String providerType){
	this.providerType = providerType;
    }
    
    public void setHCPCSCode(String hcpcsCode){
	this.hcpcsCode = hcpcsCode;
    }
}
