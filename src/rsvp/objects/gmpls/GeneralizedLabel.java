package rsvp.objects.gmpls;

import rsvp.RSVPProtocolViolationException;
import rsvp.objects.Label;
import rsvp.objects.RSVPObjectParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fernando Munoz del Nuevo
 * 
 * 
		0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |            Length             | Class-Num (16)|   C-Type (2)  |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                             Label                             |
	   |                              ...                              |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		
		See [RFC3471] for a description of parameters.

	RFC 3473
	
	*/


public class GeneralizedLabel extends Label {
	
	private int label;
	
	/**
	 * Log
	 */

  private static final Logger log = LoggerFactory.getLogger("ROADM");
	
	
	public GeneralizedLabel(int label){
		classNum = 16;
		cType = 2;

		this.label = label;

		
		length = RSVPObjectParameters.RSVP_OBJECT_COMMON_HEADER_SIZE + 4;
		
		log.debug("Generalized Label Request Object Created");
		
		
	}
	
	/**
	 * Constructor to be used when a new Label Request With ATM Label Range Object wanted to be decoded from a received
	 * message.
	 * @param bytes bytes
	 * @param offset offset
	 */
	
	public GeneralizedLabel(byte[] bytes, int offset){
		this.decodeHeader(bytes,offset);
		this.bytes = new byte[this.getLength()];
		
		log.debug("Generalized Label Request Object Created");
		
	}	
	
	public void encode() throws RSVPProtocolViolationException{
		
		bytes = new byte[this.length];
		encodeHeader();
		
		bytes[4] = (byte)((label >> 24) & 0xFF);
		bytes[5] = (byte)((label >> 16) & 0xFF);
		bytes[6] = (byte)((label >> 8) & 0xFF);
		bytes[7] = (byte)((label) & 0xFF);
	
	}
	
	public void decode(byte[] bytes, int offset) throws RSVPProtocolViolationException{
		label  = (int) (((int)bytes[offset+4] << 24)  | ((int)bytes[offset+5] << 16)
	 			  | ((int)bytes[offset+6] << 8)
	 			  | ((int)bytes[offset+7]));
	}
	
	// Getters & Setters
	
	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

}
