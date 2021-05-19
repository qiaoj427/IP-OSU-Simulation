package rsvp.messages;

import rsvp.RSVPProtocolViolationException;
import rsvp.constructs.ErrorFlowDescriptor;
import rsvp.constructs.FFErrorFlowDescriptor;
import rsvp.constructs.SEErrorFlowDescriptor;
import rsvp.constructs.WFErrorFlowDescriptor;
import rsvp.objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;


/**
 * Resv Error Message.

         ResvErr (reservation error) messages report errors in
         processing Resv messages, or they may report the spontaneous
         disruption of a reservation, e.g., by administrative
         preemption.

         ResvErr messages travel downstream towards the appropriate
         receivers, routed hop-by-hop using the reservation state.  At
         each hop, the IP destination address is the unicast address of
         a next-hop node.
{@code
           <ResvErr Message> ::= <Common Header> [ <INTEGRITY> ]

                                      <SESSION>  <RSVP_HOP>

                                      <ERROR_SPEC>  [ <SCOPE> ]

                                      [ <POLICY_DATA> ...]

                                    <STYLE> [ <error flow descriptor> ]
}

         The ERROR_SPEC object specifies the error and includes the IP
         address of the node that detected the error (Error Node
         Address).  One or more POLICY_DATA objects may be included in
         an error message to provide relevant information (e.g.,, when a
         policy control error is being reported).  The RSVP_HOP object
         contains the previous hop address, and the STYLE object is
         copied from the Resv message in error.  The use of the SCOPE
         object in a ResvErr message is defined below in Section 3.4.
         The object order requirements are as given for Resv messages,
         but the above order is recommended.

         The following style-dependent rules define the composition of a
         valid error flow descriptor; the object order requirements are
         as given earlier for flow descriptor.
{@code
         o    WF Style:

                  <error flow descriptor> ::= <WF flow descriptor>


         o    FF style:

                  <error flow descriptor> ::= <FF flow descriptor>


              Each flow descriptor in a FF-style Resv message must be
              processed independently, and a separate ResvErr message
              must be generated for each one that is in error.

         o    SE style:

                  <error flow descriptor> ::= <SE flow descriptor>

              An SE-style ResvErr message may list the subset of the
              filter specs in the corresponding Resv message to which
              the error applies.

         Note that a ResvErr message contains only one flow descriptor.
         Therefore, a Resv message that contains N > 1 flow descriptors
         (FF style) may create up to N separate ResvErr messages.
}
         Generally speaking, a ResvErr message should be forwarded
         towards all receivers that may have caused the error being
         reported.  More specifically:

         o    The node that detects an error in a reservation request
              sends a ResvErr message to the next hop node from which
              the erroneous reservation came.

              This ResvErr message must contain the information required
              to define the error and to route the error message in
              later hops.  It therefore includes an ERROR_SPEC object, a
              copy of the STYLE object, and the appropriate error flow
              descriptor.  If the error is an admission control failure
              while attempting to increase an existing reservation, then
              the existing reservation must be left in place and the
              InPlace flag bit must be on in the ERROR_SPEC of the
              ResvErr message.

         o    Succeeding nodes forward the ResvErr message to next hops
              that have local reservation state.  For reservations with
              wildcard scope, there is an additional limitation on
              forwarding ResvErr messages, to avoid loops; see Section
              3.4.  There is also a rule restricting the forwarding of a
              Resv message after an Admission Control failure; see
              Section 3.5.

              A ResvErr message that is forwarded should carry the
              FILTER_SPEC(s) from the corresponding reservation state.

         o    When a ResvErr message reaches a receiver, the STYLE
              object, flow descriptor list, and ERROR_SPEC object
              (including its flags) should be delivered to the receiver
              application.

 * @author fmn
 *
 */

public class RSVPResvErrMessage extends RSVPMessage {

	/**
	 *   RSVP Common Header

                0             1              2             3
         +-------------+-------------+-------------+-------------+
         | Vers | Flags|  Msg Type   |       RSVP Checksum       |
         +-------------+-------------+-------------+-------------+
         |  Send_TTL   | (Reserved)  |        RSVP Length        |
         +-------------+-------------+-------------+-------------+
         
         The fields in the common header are as follows:

         Vers: 4 bits

              Protocol version number.  This is version 1.

         Flags: 4 bits

              0x01-0x08: Reserved

                   No flag bits are defined yet.

         Msg Type: 8 bits

              1 = Path

              2 = Resv

              3 = PathErr

              4 = ResvErr

              5 = PathTear

              6 = ResvTear

              7 = ResvConf

         RSVP Checksum: 16 bits

              The one's complement of the one's complement sum of the
              message, with the checksum field replaced by zero for the
              purpose of computing the checksum.  An all-zero value
              means that no checksum was transmitted.

         Send_TTL: 8 bits

              The IP TTL value with which the message was sent.  See
              Section 3.8.

         RSVP Length: 16 bits

              The total length of this RSVP message in bytes, including
              the common header and the variable-length objects that
              follow.
         
	 */
	
	private Integrity integrity;
	private Session session;
	private RSVPHop rsvpHop;
	private ErrorSpec errorSpec;
	private Scope scope;
	private LinkedList<PolicyData> policyData;
	private Style style;
	private ErrorFlowDescriptor errorFlowDescriptor;
	
	/**
	 * Log
	 */

  private static final Logger log = LoggerFactory.getLogger("ROADM");
	
	
	/**
	 * Constructor that has to be used in case of creating a new Resv Error Message to
	 * be sent.
	 */
	
	public RSVPResvErrMessage(){
		
		vers = 0x01;
		flags = 0x00;
		msgType = RSVPMessageTypes.MESSAGE_RESV;
		rsvpChecksum = 0xFF;
		sendTTL = 0x00;
		reserved = 0x00;
		length = rsvp.messages.RSVPMessageTypes.RSVP_MESSAGE_HEADER_LENGTH;
		
		policyData = new LinkedList<PolicyData>();
			
		log.debug("RSVP Resv Error Message Created");
	}
	
	/**
	 * Constructor to be used in case of creating a new Resv Error message to be decoded
	 * @param bytes bytes 
	 * @param length length 
	 */
	
	public RSVPResvErrMessage(byte[] bytes, int length){
		
		this.bytes = bytes;
		this.length = length;
		policyData = new LinkedList<PolicyData>();
		
		log.debug("RSVP Resv Error Message Created");
	}
	
	
	@Override
	public void encodeHeader() {

		bytes[0]= (byte)(((vers<<4) &0xF0) | (flags & 0x0F));
		bytes[1]= (byte) msgType;
		bytes[2]= (byte)((rsvpChecksum>>8) & 0xFF);
		bytes[3]= (byte)(rsvpChecksum & 0xFF);
		bytes[4]= (byte) sendTTL;
		bytes[5]= (byte) reserved;
		bytes[6]= (byte)((length>>8) & 0xFF);
		bytes[7]= (byte)(length & 0xFF);
		
	}

	@Override
	public void encode() throws RSVPProtocolViolationException{
		log.debug("Starting RSVP Resv Error Message encode");
		
		int commonHeaderSize = rsvp.messages.RSVPMessageTypes.RSVP_MESSAGE_HEADER_LENGTH;
		
	
		if(integrity != null){
			
			length = length + integrity.getLength();
			log.debug("Integrity RSVP Object found");
			
		}
		if(session != null){
			
			length = length + session.getLength();
			log.debug("Session RSVP Object found");
			
		}else{
			
			// Campo Obligatorio, si no existe hay fallo
			log.error("Session RSVP Object NOT found");
			throw new RSVPProtocolViolationException();
			
		}
		if(rsvpHop != null){
			
			length = length + rsvpHop.getLength();
			log.debug("Hop RSVP Object found");
			
		}else{
			
			// Campo Obligatorio, si no existe hay fallo
			log.error("Hop RSVP Object NOT found");
			throw new RSVPProtocolViolationException();
			
		}
		if(errorSpec != null){
			
			length = length + errorSpec.getLength();
			log.debug("Error Spec RSVP Object found");
			
		}else{
			
			// Campo Obligatorio, si no existe hay fallo
			log.error("Error Spec RSVP Object NOT found");
			throw new RSVPProtocolViolationException();			
			
		}
		if(scope != null){
			
			length = length + scope.getLength();
			log.debug("Scope RSVP Object found");
			
		}
		
		int pdSize = policyData.size();
				
		for(int i = 0; i < pdSize; i++){
				
			PolicyData pd = (PolicyData) policyData.get(i);
			length = length + pd.getLength();
			log.debug("Policy Data RSVP Object found");
				
		}					
		
		if(style != null){
			
			length = length + style.getLength();
			log.debug("Style RSVP Object found");
			
		}else{
			
			// Campo Obligatorio, si no existe hay fallo
			log.error("Style RSVP Object NOT found");
			throw new RSVPProtocolViolationException();			
			
		}
		

			
		if(errorFlowDescriptor != null){
			
			length = length + errorFlowDescriptor.getLength();
			log.debug("Error Flow RSVP Construct found");
			
		}
		
		bytes = new byte[length];
		encodeHeader();
		int currentIndex = commonHeaderSize;
		
		if(integrity != null){
			
			//Campo Opcional
			integrity.encode();
			System.arraycopy(integrity.getBytes(), 0, bytes, currentIndex, integrity.getLength());
			currentIndex = currentIndex + integrity.getLength();
			
		}
		
		// Campo Obligatorio
		session.encode();
		System.arraycopy(session.getBytes(), 0, bytes, currentIndex, session.getLength());
		currentIndex = currentIndex + session.getLength();
		// Campo Obligatorio
		rsvpHop.encode();
		System.arraycopy(rsvpHop.getBytes(), 0, bytes, currentIndex, rsvpHop.getLength());
		currentIndex = currentIndex + rsvpHop.getLength();
		// Campo Obligatorio
		errorSpec.encode();
		System.arraycopy(errorSpec.getBytes(), 0, bytes, currentIndex, errorSpec.getLength());
		currentIndex = currentIndex + errorSpec.getLength();
		
		if(scope != null){
			
			//Campo Opcional
			scope.encode();
			System.arraycopy(scope.getBytes(), 0, bytes, currentIndex, scope.getLength());
			currentIndex = currentIndex + scope.getLength();
			
		}
		// Campos Opcionales
		for(int i = 0; i < pdSize; i++){
				
			PolicyData pd = (PolicyData) policyData.get(i);
			pd.encode();
			System.arraycopy(pd.getBytes(), 0, bytes, currentIndex, pd.getLength());
			currentIndex = currentIndex + pd.getLength();
							
		}
	
		// Campo Obligatorio
		style.encode();
		System.arraycopy(style.getBytes(), 0, bytes, currentIndex, style.getLength());
		currentIndex = currentIndex + style.getLength();
		
		// Campo Obligatorio
		try{
			errorFlowDescriptor.encode();
			System.arraycopy(errorFlowDescriptor.getBytes(), 0, bytes, currentIndex, errorFlowDescriptor.getLength());
			currentIndex = currentIndex + errorFlowDescriptor.getLength();
		}catch(RSVPProtocolViolationException e){
			log.error("Failure encoding error Flow descriptor on RSVP Resv Error Message");
			
		}
	
		log.debug("RSVP Resv Error Message encoding accomplished");
		
	}

	@Override
	public void decode() throws RSVPProtocolViolationException {

		decodeHeader();
		
		int offset = RSVPMessageTypes.RSVP_MESSAGE_HEADER_LENGTH;
		while(offset < length){		// Mientras quede mensaje
			
			int classNum = RSVPObject.getClassNum(bytes,offset);
			if(classNum == 1){
				
				// Session Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					// Session IPv4
					session = new SessionIPv4();
					session.decode(bytes, offset);
					
					offset = offset + session.getLength();
					
				}else if(cType == 2){
					
					// Session IPv6
					session = new SessionIPv6();
					session.decode(bytes, offset);
					offset = offset + session.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}
			}
			else if(classNum == 3){
				
				// RSVPHop Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					// RSVPHop IPv4
					rsvpHop = new RSVPHopIPv4();
					rsvpHop.decode(bytes, offset);
					offset = offset + rsvpHop.getLength();
					
				}else if(cType == 2){
					
					// RSVPHop IPv6
					rsvpHop = new RSVPHopIPv6();
					rsvpHop.decode(bytes, offset);
					offset = offset + rsvpHop.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}				
			}
			else if(classNum == 4){
				
				// Integrity Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					integrity = new Integrity();
					integrity.decode(bytes, offset);
					offset = offset + integrity.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}
			}
			else if(classNum == 6){
				
				// Error Spec Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					// Error Spec IPv4
					errorSpec = new ErrorSpecIPv4();
					errorSpec.decode(bytes, offset);
					offset = offset + errorSpec.getLength();
					
				}else if(cType == 2){
					
					// Error Spec IPv6
					errorSpec = new ErrorSpecIPv6();
					errorSpec.decode(bytes, offset);
					offset = offset + errorSpec.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}				
			}else if(classNum == 7){
				
				// Scope Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					// Scope IPv4
					scope = new ScopeIPv4();
					scope.decode(bytes, offset);
					offset = offset + scope.getLength();
					
				}else if(cType == 2){
					
					// Scope IPv6
					scope = new ScopeIPv6();
					scope.decode(bytes, offset);
					offset = offset + scope.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}
			}else if(classNum == 13){
				
				// Policy Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					PolicyData pd = new PolicyData();
					pd.decode(bytes, offset);
					offset = offset + pd.getLength();
					policyData.add(pd);
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}				
			}
			else if(classNum == 8){
				
				// Style Object
				int cType = RSVPObject.getcType(bytes,offset);
				if(cType == 1){
					
					style = new Style();
					style.decode(bytes, offset);
					offset = offset + style.getLength();
					
				}else{
					
					// Fallo en cType
					throw new RSVPProtocolViolationException();
					
				}				
			}else if(classNum == 9){
				
				// Error Flow Descriptor Construct
				if(style != null){	// Style debe haberse decodificado porque en caso
									// contrario el mensaje no se habra construido bien
					if(style.getOptionVector()==rsvp.objects.RSVPObjectParameters.RSVP_STYLE_OPTION_VECTOR_FF_STYLE){
						
						errorFlowDescriptor = new FFErrorFlowDescriptor();
						errorFlowDescriptor.decode(bytes, offset);
						offset = offset + errorFlowDescriptor.getLength();
						
					}else if(style.getOptionVector()==rsvp.objects.RSVPObjectParameters.RSVP_STYLE_OPTION_VECTOR_WF_STYLE){

						errorFlowDescriptor = new WFErrorFlowDescriptor();
						errorFlowDescriptor.decode(bytes, offset);
						offset = offset + errorFlowDescriptor.getLength();
						
					}else if(style.getOptionVector()==rsvp.objects.RSVPObjectParameters.RSVP_STYLE_OPTION_VECTOR_SE_STYLE){

						errorFlowDescriptor = new SEErrorFlowDescriptor();
						errorFlowDescriptor.decode(bytes, offset);
						offset = offset + errorFlowDescriptor.getLength();
					}
					
				}else{
					
					// Malformed Resv Message
					log.error("Malformed RSVP Resv Error Message, Style Object not Found");
					throw new RSVPProtocolViolationException();
					
				}
				
				
			}	
			else{
				
				// Fallo en classNum
				log.error("Malformed RSVP Resv Error Message, Object classNum incorrect");
				throw new RSVPProtocolViolationException();
				
			}
			
		}
		
		
	}
	
}
