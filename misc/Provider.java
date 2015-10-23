package com.crunchify.controller;  // ToDo - change this once we know our package name

public class Provider {  
  
  // See here for quick reference on the medicare derived fields
  // http://www.t1cg.io/medicare-glossary
  
  public String id;
  public long year;
  public String npi;
  public String last_or_org_name;
  public String first_name;
  public String credentials;
  public String entity_code; // or enum?
  public String city;
  public String zip;
  public String state;
  public String country;
  public String provider_type;
  public String place_of_service;  // or enum?
  public String hcpcs_code;
  public String hcpcs_description;
  public float line_service_count;
  public long beneficiaries_unique_count;
  public long beneficiaries_day_service_count;
  
  // These are only in Cassandra.
  public class ExtendedInfo {
    public String middle_initial;
    public String gender; // Or could be an enum?
    public String streetAddress1;
    public String streetAddress2;
    public boolean medicare_participation;  // true or false (y/n in the csv)
    public boolean hcpcs_drug_indicator;  // true or false (y/n in the csv)
    public float averageMedicareAllowedAmount;
    public float stddevMedicareAllowedAmount;
    public float averageSubmittedChargeAmount;
    public float stddevSubmittedChargeAmount;
    public float averageMedicarePaymentAmount;
    public float stddevMedicarePaymentAmount;
  }
  
  // Set this to null for Solr queries?
  public ExtendedInfo providerInfo;
  
  public Provider(String id)
  {
    this.id = id; 
  }
}
