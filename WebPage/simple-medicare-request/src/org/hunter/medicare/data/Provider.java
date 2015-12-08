package org.hunter.medicare.data;

import java.util.Map;

import com.datastax.driver.core.Row;

public class Provider {
    @Override
    public String toString() {
        String ret = "";
        if (providerDetails != null)
            ret += String.format("averageSubmittedChargeAmount %.2f,",
                    providerDetails.averageSubmittedChargeAmount);

        ret += String.format("daycount %d,\t firstname: %s, \tlastname: %s \t zip:%s",
                beneficiaries_day_service_count, first_name, last_or_org_name, zip);

        return ret;
    }

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
    public String place_of_service; // or enum?
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
        public boolean medicare_participation; // true or false (y/n in the csv)
        public boolean hcpcs_drug_indicator; // true or false (y/n in the csv)
        public float averageMedicareAllowedAmount;
        public float stddevMedicareAllowedAmount;
        public float averageSubmittedChargeAmount; // sort this field to find
        // top expensive ones.
        public float stddevSubmittedChargeAmount;
        public float averageMedicarePaymentAmount;
        public float stddevMedicarePaymentAmount;
    }

    // Set this to null for Solr queries?
    public ExtendedInfo providerDetails;

    public Provider() {
    }

    public Provider(String id) {
        this.id = id;
    }

    // Constructor used by SolrJ
    public Provider(Map<String, Object> fields) {
        this.providerDetails = null; // Null = Solr doesn't have these details

        this.id = fields.get("id").toString(); // required.
        for (String key : fields.keySet()) {
            switch (key) {
            case "id":
                // Already set this one
                break;
            case "year":
                this.year = (long) fields.get(key);
                break;
            case "NPI":
                this.npi = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_LAST_ORG_NAME":
                this.last_or_org_name = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_FIRST_NAME":
                this.first_name = fields.get(key).toString();
                break;
            case "NPPES_CREDENTIALS":
                this.credentials = fields.get(key).toString();
                break;
            case "NPPES_ENTITY_CODE":
                this.entity_code = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_CITY":
                this.city = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_ZIP":
                this.zip = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_STATE":
                this.state = fields.get(key).toString();
                break;
            case "NPPES_PROVIDER_COUNTRY":
                this.country = fields.get(key).toString();
                break;
            case "PROVIDER_TYPE":
                this.provider_type = fields.get(key).toString();
                break;
            case "PLACE_OF_SERVICE":
                this.place_of_service = fields.get(key).toString();
                break;
            case "HCPCS_CODE":
                this.hcpcs_code = fields.get(key).toString();
                break;
            case "HCPCS_DESCRIPTION":
                this.hcpcs_description = fields.get(key).toString();
                break;
            case "LINE_SRVC_CNT":
                Object value = fields.get(key);
                this.line_service_count = Float.parseFloat(value.toString());
                break;
            case "BENE_UNIQUE_CNT":
                this.beneficiaries_unique_count = (long) fields.get(key);
                break;
            case "BENE_DAY_SRVC_CNT":
                this.beneficiaries_day_service_count = (long) fields.get(key);
                break;
            case "_version_":
                break;
            default:
                // We just ignore fields we don't recognize
                break;
            }
        }
    }

    /**
     * Cassandra Constructor
     * 
     * Author Tim
     * 
     * @param mvRow
     */
    public Provider(Row mvRow) {
        providerDetails = new ExtendedInfo();
        if (!mvRow.isNull("npi")) {
            npi = mvRow.getString("npi");
        }
        if (!mvRow.isNull("nppes_provider_last_org_name")) {
            last_or_org_name = mvRow.getString("nppes_provider_last_org_name");
        }
        if (!mvRow.isNull("nppes_provider_first_name")) {
            first_name = mvRow.getString("nppes_provider_first_name");
        }
        if (!mvRow.isNull("nppes_provider_zip")) {
            zip = "" + mvRow.getString("nppes_provider_zip");
        }
        if (!mvRow.isNull("nppes_provider_state")) {
            state = mvRow.getString("nppes_provider_state");
        }
        if (!mvRow.isNull("nppes_provider_mi")) {
            providerDetails.middle_initial = mvRow.getString("nppes_provider_mi");
        }
        if (!mvRow.isNull("hcpcs_code")) {
            hcpcs_code = mvRow.getString("hcpcs_code");
        }
        if (!mvRow.isNull("year")) {
            year = (long) mvRow.getInt("year");
        }
        if (!mvRow.isNull("place_of_service")) {
            place_of_service = mvRow.getString("place_of_service");
        }
        if (!mvRow.isNull("average_submitted_chrg_amt")) {
            providerDetails.averageSubmittedChargeAmount = mvRow
                    .getFloat("average_submitted_chrg_amt");
        }
        if (!mvRow.isNull("stdev_submitted_chrg_amt")) {
            providerDetails.stddevSubmittedChargeAmount = mvRow
                    .getFloat("stdev_submitted_chrg_amt");
        }
    }

    // FIXME will either remove this older version or refactor it if still
    // needed
    /**
     * Cassandra Constructor Author Tim
     * 
     * @param providerRow
     * @param procedureRow
     * @param procedureInfoRow
     */
    public Provider(Row providerRow, Row procedureRow, Row procedureInfoRow) {
        providerDetails = new ExtendedInfo();
        if (!providerRow.isNull("npi")) {
            npi = providerRow.getString("npi");
        }
        if (!providerRow.isNull("nppes_provider_last_org_name")) {
            last_or_org_name = providerRow.getString("nppes_provider_last_org_name");
        }
        if (!providerRow.isNull("nppes_provider_first_name")) {
            first_name = providerRow.getString("nppes_provider_first_name");
        }
        if (!providerRow.isNull("nppes_credentials")) {
            credentials = providerRow.getString("nppes_credentials");
        }
        if (!providerRow.isNull("nppes_entity_code")) {
            entity_code = providerRow.getString("nppes_entity_code");
        }
        if (!providerRow.isNull("nppes_provider_city")) {
            city = providerRow.getString("nppes_provider_city");
        }
        if (!providerRow.isNull("nppes_provider_zip")) {
            zip = "" + providerRow.getInt("nppes_provider_zip");
        }
        if (!providerRow.isNull("nppes_provider_state")) {
            state = providerRow.getString("nppes_provider_state");
        }
        if (!providerRow.isNull("nppes_provider_country")) {
            country = providerRow.getString("nppes_provider_country");
        }
        if (!providerRow.isNull("provider_type")) {
            provider_type = providerRow.getString("provider_type");
        }
        if (!providerRow.isNull("nppes_provider_mi")) {
            providerDetails.middle_initial = providerRow.getString("nppes_provider_mi");
        }
        if (!providerRow.isNull("nppes_provider_gender")) {
            providerDetails.gender = providerRow.getString("nppes_provider_gender");
        }
        if (!providerRow.isNull("nppes_provider_street1")) {
            providerDetails.streetAddress1 = providerRow.getString("nppes_provider_street1");
        }
        if (!providerRow.isNull("nppes_provider_street2")) {
            providerDetails.streetAddress2 = providerRow.getString("nppes_provider_street2");
        }
        if (!providerRow.isNull("medicare_participation_indicator")) {
            if (providerRow.getString("medicare_participation_indicator").equals("y")) {
                providerDetails.medicare_participation = true;
            } else {
                providerDetails.medicare_participation = false;
            }
        }
        if (!procedureRow.isNull("id")) {
            id = procedureRow.getString("id");
        }
        if (!procedureRow.isNull("hcpcs_code")) {
            hcpcs_code = procedureRow.getString("hcpcs_code");
        }
        if (!procedureRow.isNull("year")) {
            year = (long) procedureRow.getInt("year");
        }
        if (!procedureRow.isNull("place_of_service")) {
            place_of_service = procedureRow.getString("place_of_service");
        }
        if (!procedureRow.isNull("line_srvc_cnt")) {
            line_service_count = procedureRow.getFloat("line_srvc_cnt");
        }
        if (!procedureRow.isNull("bene_unique_cnt")) {
            beneficiaries_unique_count = (long) procedureRow.getFloat("bene_unique_cnt");
        }
        if (!procedureRow.isNull("bene_day_srvc_cnt")) {
            beneficiaries_day_service_count = (long) procedureRow.getFloat("bene_day_srvc_cnt");
        }
        if (!procedureRow.isNull("average_medicare_allowed_amt")) {
            providerDetails.averageMedicareAllowedAmount = procedureRow
                    .getFloat("average_medicare_allowed_amt");
        }
        if (!procedureRow.isNull("stdev_medicare_allowed_amt")) {
            providerDetails.stddevMedicareAllowedAmount = procedureRow
                    .getFloat("stdev_medicare_allowed_amt");
        }
        if (!procedureRow.isNull("average_submitted_chrg_amt")) {
            providerDetails.averageSubmittedChargeAmount = procedureRow
                    .getFloat("average_submitted_chrg_amt");
        }
        if (!procedureRow.isNull("stdev_submitted_chrg_amt")) {
            providerDetails.stddevSubmittedChargeAmount = procedureRow
                    .getFloat("stdev_submitted_chrg_amt");
        }
        if (!procedureRow.isNull("average_medicare_payment_amt")) {
            providerDetails.averageMedicarePaymentAmount = procedureRow
                    .getFloat("average_medicare_payment_amt");
        }
        if (!procedureRow.isNull("stdev_medicare_payment_amt")) {
            providerDetails.stddevMedicarePaymentAmount = procedureRow
                    .getFloat("stdev_medicare_payment_amt");
        }
        if (!procedureInfoRow.isNull("hcpcs_drug_indicator")) {
            if (procedureInfoRow.getString("hcpcs_drug_indicator").equals("y")) {
                providerDetails.hcpcs_drug_indicator = true;
            } else {
                providerDetails.hcpcs_drug_indicator = false;
            }
            if (!procedureInfoRow.isNull("hcpcs_description")) {
                hcpcs_description = procedureInfoRow.getString("hcpcs_description");
            }
        }
    }
}
