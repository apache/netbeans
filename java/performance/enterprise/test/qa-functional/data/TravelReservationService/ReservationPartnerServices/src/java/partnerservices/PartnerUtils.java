/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package partnerservices;

import java.rmi.RemoteException;
import javax.jms.MapMessage;
import javax.xml.soap.SOAPElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import partnerservices.callback.ReservationCallbackProviderBean;

public class PartnerUtils {
    
    public PartnerUtils() {}
    
    public static javax.jms.Message createJMSMessageForReservationCallbackProviderDestination(
            javax.jms.Session session, java.lang.String messsageType, String uniqueID)
            throws javax.jms.JMSException {
        
        MapMessage mapMessage = session.createMapMessage();
        mapMessage.setString(ReservationCallbackProviderBean.MESSAGE_TYPE, messsageType);
        mapMessage.setString(ReservationCallbackProviderBean.UNIQUE_ID, uniqueID);
        
        return mapMessage;
    }
    
    public static void sendJMSMessageToReservationCallbackProviderDestination(
            String messageType, String uniqueID)
            throws RemoteException {
        javax.jms.Connection conn = null;
        javax.jms.Session s = null;
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            javax.jms.ConnectionFactory cf = (javax.jms.ConnectionFactory) c.lookup("java:comp/env/jms/ReservationCallbackProviderDestinationFactory");
            
            conn = cf.createConnection();
            s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);
            javax.jms.Destination destination = (javax.jms.Destination) c.lookup("java:comp/env/jms/ReservationCallbackProviderDestination");
            javax.jms.MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForReservationCallbackProviderDestination(s,messageType, uniqueID));
            
            if (s != null) {
                s.close();
            }
            
            if (conn != null) {
                conn.close();
            }
            
        } catch( javax.naming.NamingException ne ) {
            throw new java.rmi.RemoteException("NamingException", ne);
        } catch( javax.jms.JMSException  jmse) {
            throw new java.rmi.RemoteException("JMSException", jmse);
        }
    }
    
    public static String getUniqueID(SOAPElement itinerary) {
        String uniqueID = "";
        
        NodeList nodeList = itinerary.getChildNodes();
        try {
            for(int index1=0; index1<nodeList.getLength() ; index1++) {
                Node node1 = nodeList.item(index1);
                if(node1.getNodeName().endsWith("ItineraryRef")) {
                    for (int index2=0 ; index2<node1.getChildNodes().getLength() ; index2++) {
                        Node node2 = node1.getChildNodes().item(index2);
                        if(node2.getNodeName().endsWith("UniqueID")) {
                            uniqueID = node2.getTextContent();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return uniqueID;
        
    }
}
