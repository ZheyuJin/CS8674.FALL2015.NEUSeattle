package org.hunter.medicare.data;

import java.util.Map;

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
  public ExtendedInfo providerDetails;
  
  public Provider(String id)
  {
    this.id = id; 
  }
  
   // Constructor used by SolrJ
   public Provider(Map<String, Object> fields)
   {
     this.providerDetails = null;  // Null = Solr doesn't have these details
     
     this.id = fields.get("id").toString();
     for (String key : fields.keySet())
     {
       switch(key){
         case "id" :
             // Already set this one
             break;
         case "year" :
             this.year = (long)fields.get(key);
             break;
         case "NPI" :
           this.npi = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_LAST_ORG_NAME" :
           this.last_or_org_name = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_FIRST_NAME" :
           this.first_name = fields.get(key).toString();
           break;
         case "NPPES_CREDENTIALS" :
           this.credentials = fields.get(key).toString();
           break;
         case "NPPES_ENTITY_CODE" :
           this.entity_code = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_CITY" :
           this.city = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_ZIP" :
           this.zip = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_STATE" :
           this.state = fields.get(key).toString();
           break;
         case "NPPES_PROVIDER_COUNTRY" :
           this.country = fields.get(key).toString();
           break;
         case "PROVIDER_TYPE" :
           this.provider_type = fields.get(key).toString();
           break;
         case "PLACE_OF_SERVICE" :
           this.place_of_service = fields.get(key).toString();
           break;
         case "HCPCS_CODE" :
           this.hcpcs_code = fields.get(key).toString();
           break;
         case "HCPCS_DESCRIPTION" :
           this.hcpcs_description = fields.get(key).toString();
           break;
         case "LINE_SRVC_CNT" :
           this.line_service_count = (float)fields.get(key);
           break; 
         case "BENE_UNIQUE_CNT" :
           this.beneficiaries_unique_count = (long)fields.get(key);
           break;
         case "BENE_DAY_SRVC_CNT" :
           this.beneficiaries_day_service_count = (long)fields.get(key);
           break;
         case "_version_" :
           break;
         default: 
             // We just ignore fields we don't recognize
             break;
       }
     }
   }
}
