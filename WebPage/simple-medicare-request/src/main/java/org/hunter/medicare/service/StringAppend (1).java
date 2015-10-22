package org.hunter.medicare.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Service enables the class to be used as a Spring service
 * @Transactional enables transaction support for this class
 */
@Service("springService")
@Transactional
public class StringAppend {
	
	protected static Logger logger = Logger.getLogger("service");
	
	/**
	 * Adds two numbers
	 */
	public String add(String str1, String str2) {
		logger.debug("Appending 2 Strings");
		// A simple arithmetic addition
		return str1 + str2;
	}
	
}
