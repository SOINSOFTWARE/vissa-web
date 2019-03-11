package com.soinsoftware.vissa.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsExample {

	public static final String ACCOUNT_SID = "ACfbf915f466296865b9b52d4c72dbb348";
	  public static final String AUTH_TOKEN = "9aaf1af339f307b3019a32cf27a40628";

	  public static void main(String[] args) {
	    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

	    Message message = Message.creator(new PhoneNumber("+573002007694"),
	        new PhoneNumber("+14064121262"), 
	        "Hola").create();

	    System.out.println(message.getSid());
	  }
}
