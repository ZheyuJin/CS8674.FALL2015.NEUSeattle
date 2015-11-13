package org.hunter.medicare.test;

import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Provider;

/**
 * Cassandra tests with mock test commented out
 * 
 * @author Zheyu
 * @author Tim
 *
 */
public class CassandraQueryTest2 {

    public static void main(String[] args) {
        System.out.println("gonna print test result");
        //
        // for (Provider p :
        // CassandraQueryResponse.getInstance().getMostExpensive("CA", "*")) {
        // System.out.println(p);
        // }

        // get

        // CassandraQueryResponse cqr = new CassandraQueryResponse();
        // List<ProviderT> pt = cqr.getMostExpensive("CA", "*");
        // System.out.println("Returned " + pt.size() + " results");
        // for (ProviderT p : pt) {
        // System.out.println("Provider Id is: " + p.npi);
        // }
        Provider test = CassandraQueryResponse.getProviderById("1003000522F992132012");
        System.out.println("id is: " + test.id);
        System.out.println("year: " + test.year);
        System.out.println("day service count: " + test.beneficiaries_day_service_count);
        System.out.println("bene unique cnt: " + test.beneficiaries_unique_count);
        System.out.println("lastname: " + test.last_or_org_name);
        System.out.println("firstname: " + test.first_name);
        System.out.println("zip: " + test.zip);
        System.out.println("city: " + test.city);
        System.out.println("state: " + test.state);
        System.out.println("country " + test.country);
        System.out.println("credentials " + test.credentials);
        System.out.println("entity code" + test.entity_code);
        System.out.println("npi is: " + test.npi);
        System.out.println("provider type" + test.provider_type);
        System.out.println("place of service" + test.place_of_service);
        System.out.println("hcpcs code: " + test.hcpcs_code);
        System.out.println("hcpcs description: " + test.hcpcs_description);
        System.out.println("gender: " + test.providerDetails.gender);
        System.out.println("street1: " + test.providerDetails.streetAddress1);
        System.out.println("street2: " + test.providerDetails.streetAddress2);
        System.out.println("participation: " + test.providerDetails.medicare_participation);
        System.out.println("line count: " + test.line_service_count);
        System.out.println("avg medicare payment :"
                + test.providerDetails.averageMedicarePaymentAmount);
        System.out.println("avg medicare allowed :"
                + test.providerDetails.averageMedicareAllowedAmount);
        System.out.println("hcpcs_drug_indicator is: " + test.providerDetails.hcpcs_drug_indicator);
        System.out.println("avg submitted charge is "
                + test.providerDetails.averageSubmittedChargeAmount);

    }

}
