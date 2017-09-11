/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
