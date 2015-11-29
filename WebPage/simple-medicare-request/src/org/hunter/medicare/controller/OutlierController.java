package org.hunter.medicare.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.gaussian.GMM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.bytecode.opencsv.CSVReader;

@Controller
@RequestMapping("/main2")
public class OutlierController {

	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String getCase1Form() {
		System.out.println("Initialized Cassandra query page");
		return "outlier-form";
	}

	@RequestMapping(value = "/result-jsp", method = RequestMethod.GET)
	public String getCase1_ResultForm(@RequestParam(value = "proc_code", required = true) String proc_code,
			@RequestParam(value = "percentage", required = true) String percentage, Model model) {
		List<Provider> list = new ArrayList<Provider>();

		try {
			list = getGaussianOutliers(proc_code, Double.parseDouble(percentage));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Add to model
		model.addAttribute("providerlist", list);

		return "ProviserListView";
	}

	private List<Provider> getGaussianOutliers(String proc_code, double topPercentage) throws Exception {

		GMM gmm = new GMM(null);
		gmm.load();
		NormalDistribution nd = gmm.getIndividualModel(proc_code);

		double cuttingPrice = nd.inverseCumulativeProbability(1 - topPercentage / 100);
		System.err.printf("mu: %f, stddev: %f cutting: %f\n", nd.getMean(), nd.getStandardDeviation(), cuttingPrice);
		// read and filterout all price higher than the cutting line.
		// read from S3 and parse.

		List<Provider> lst = filterByPrice(cuttingPrice);
		return lst;
	}

	/**
	 * filter all provisers who submitted charge for certain proc more than
	 * given price.
	 * 
	 * @param price
	 * @return
	 * @throws Exception
	 */
	private List<Provider> filterByPrice(double price) throws IOException {
		// tmp file path. will later read form Cassandra.
		final String path = "22524.txt"; // spine procedure collection.
		List<Provider> ret = new ArrayList<>();
		try (CSVReader stream = new CSVReader(new InputStreamReader(new FileInputStream(path)), '\t');) {
			String[] ss = null;

			while (null != (ss = stream.readNext())) {
				Provider p = parseProvider(ss);
				if (p.providerDetails.averageSubmittedChargeAmount >= price) {
					ret.add(p);
				}
			}
		}

		return ret;
	}

	@SuppressWarnings("unused")
	private Provider parseProvider(String[] ss) {
		int NPI = 0;
		int NPPES_PROVIDER_LAST_ORG_NAME = 1;
		int NPPES_PROVIDER_FIRST_NAME = 2;
		int NPPES_PROVIDER_MI = 3;
		int NPPES_CREDENTIALS = 4;
		int NPPES_PROVIDER_GENDER = 5;
		int NPPES_ENTITY_CODE = 6;
		int NPPES_PROVIDER_STREET1 = 7;
		int NPPES_PROVIDER_STREET2 = 8;
		int NPPES_PROVIDER_CITY = 9;
		int NPPES_PROVIDER_ZIP = 10;
		int NPPES_PROVIDER_STATE = 11;
		int NPPES_PROVIDER_COUNTRY = 12;
		int PROVIDER_TYPE = 13;
		int MEDICARE_PARTICIPATION_INDICATOR = 14;
		int PLACE_OF_SERVICE = 15;
		int HCPCS_CODE = 16;
		int HCPCS_DESCRIPTION = 17;
		int HCPCS_DRUG_INDICATOR = 18;
		int LINE_SRVC_CNT = 19;
		int BENE_UNIQUE_CNT = 20;
		int BENE_DAY_SRVC_CNT = 21;
		int AVERAGE_MEDICARE_ALLOWED_AMT = 22;
		int STDEV_MEDICARE_ALLOWED_AMT = 23;
		int AVERAGE_SUBMITTED_CHRG_AMT = 24;
		int STDEV_SUBMITTED_CHRG_AMT = 25;
		int AVERAGE_MEDICARE_PAYMENT_AMT = 26;
		int STDEV_MEDICARE_PAYMENT_AMT = 27;

		Provider p = new Provider();

		// basic info
		p.beneficiaries_day_service_count = Integer.parseInt(ss[BENE_DAY_SRVC_CNT]);
		p.first_name = ss[NPPES_PROVIDER_FIRST_NAME];
		p.last_or_org_name = ss[NPPES_PROVIDER_LAST_ORG_NAME];
		p.zip = ss[NPPES_PROVIDER_ZIP];

		// extended info
		p.providerDetails = p.new ExtendedInfo();
		p.providerDetails.averageSubmittedChargeAmount = Float.parseFloat(ss[AVERAGE_SUBMITTED_CHRG_AMT]);

		return p;
	}

	public static void main(String[] args) throws Exception {
		OutlierController con = new OutlierController();
		con.getGaussianOutliers("22524" /* spine procedure */, 0.1 /* percent */).forEach(System.out::println);
	}
}
