package org.hunter.medicare.data;

//import org.apache.spark.api.java.function.*;
//import org.apache.spark.api.java.*;
import scala.Serializable;


public class Record implements Serializable{
    protected String providerType;
    protected String hcpcsCode;
    
    public Record(String providerType, String hcpcsCode){
	this.providerType = providerType;
	this.hcpcsCode = hcpcsCode;
    }
 
    public Boolean equals(Record record){
	return this.providerType.equalsIgnoreCase(record.getProviderType()) &&
		this.hcpcsCode.equalsIgnoreCase(record.getHCPCSCode());
    }
    
    public String toString(){
	return "[" + this.providerType + ", " + this.hcpcsCode + "]";
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
