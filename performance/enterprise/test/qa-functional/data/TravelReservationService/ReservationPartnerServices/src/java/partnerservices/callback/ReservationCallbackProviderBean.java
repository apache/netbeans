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

package partnerservices.callback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.ejb.*;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ReservationCallbackProviderBean implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext context;
    
    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(MessageDrivenContext aContext) {
        context = aContext;
    }
    
    /**
     * See section 15.4.4 of the EJB 2.0 specification
     * See section 15.7.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
    }
    
    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() {
    }
    
    public void onMessage(Message aMessage) {
        try {
            String messageType, uniqueID;
            
            MapMessage mapMessage = (MapMessage) aMessage;
            messageType = mapMessage.getString(MESSAGE_TYPE);
            uniqueID = mapMessage.getString(UNIQUE_ID);
            
            String soapStr = "";
            URL cbURL = null;
            
            // Initialise to defaults.
            String airlineCallbackURL = "http://localhost:18181/TravelReservationService/airlineReserved";
            String vehicleCallbackURL = "http://localhost:18181/TravelReservationService/vehicleReserved";
            String hotelCallbackURL = "http://localhost:18181/TravelReservationService/hotelReserved";
            
            // Lookup URL's defined in the deployment descriptor.
            try {
                InitialContext ic = new InitialContext();
                airlineCallbackURL = (String) ic.lookup("java:comp/env/AirlineCallbackURL");
                vehicleCallbackURL = (String) ic.lookup("java:comp/env/VehicleCallbackURL");
                hotelCallbackURL = (String) ic.lookup("java:comp/env/HotelCallbackURL");
            } catch (NamingException ne) {
            }
            
            if(messageType.equals(AIRLINE_RESERVATION)  ) {
                cbURL = new URL(airlineCallbackURL);
                soapStr = createSOAPString(AIRLINE_RESERVATION_SIM_FILE);
                
            } else if(messageType.equals(VEHICLE_RESERVATION)  ) {
                cbURL = new URL(vehicleCallbackURL);
                soapStr = createSOAPString(VEHICLE_RESERVATION_SIM_FILE);
                
            } else if(messageType.equals(HOTEL_RESERVATION)  ) {
                cbURL = new URL(hotelCallbackURL);
                soapStr = createSOAPString(HOTEL_RESERVATION_SIM_FILE);
            } else {
                return;
            }
            soapStr = soapStr.replaceAll("ENTER_ID_HERE", uniqueID);
            sendSOAPMsg(cbURL, soapStr);
            
        } catch( javax.jms.JMSException  jmse) {
            jmse.printStackTrace();
        }
        
        catch( SOAPException  soapE) {
            soapE.printStackTrace();
        } catch( RemoteException  re) {
            re.printStackTrace();
        } catch( IOException  ioe) {
            ioe.printStackTrace();
        } catch( ParserConfigurationException  pce) {
            pce.printStackTrace();
        } catch( SAXException  saxe) {
            saxe.printStackTrace();
        } catch( TransformerConfigurationException  tce) {
            tce.printStackTrace();
        } catch( TransformerException  te) {
            te.printStackTrace();
        }
    }
    
    private String createSOAPString(String simulationMsgFileName)
            throws SOAPException, IOException, ParserConfigurationException, SAXException,
            TransformerConfigurationException, TransformerException{
        
        BufferedInputStream source = null;
        StringBuffer sb = new StringBuffer();
        
        try {
            source = new BufferedInputStream(
                    getClass().getResourceAsStream(simulationMsgFileName));
            
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(source));
            String s;
            while((s = reader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            if (source != null) source.close();
        }
    }
    
    private void sendSOAPMsg(URL dest, String msg )  throws IOException {
        HttpURLConnection urlC  = null;
        OutputStream os = null;
        try {
            
            urlC = (HttpURLConnection) dest.openConnection();
            urlC.setDoOutput(true);
            
            urlC.setRequestProperty("Content-Type", "text/xml");
            urlC.setRequestMethod("POST");
            os = urlC.getOutputStream();
            os.write(msg.getBytes());
            os.flush();
            urlC.getResponseMessage();
        }
        
        catch(MalformedURLException e) {
            e.printStackTrace();
            
        } finally {
            try {
                if(os != null)
                    os.close();
            } catch(Exception e) {
            }
        }
    }
    
    public static final String AIRLINE_RESERVATION = "airline";
    public static final String VEHICLE_RESERVATION = "vehicle";
    public static final String HOTEL_RESERVATION = "hotel";
    public static final String AIRLINE_RESERVATION_SIM_FILE = "ItineraryPlusAirline.xml";
    public static final String VEHICLE_RESERVATION_SIM_FILE = "ItineraryPlusVehicle.xml";
    public static final String HOTEL_RESERVATION_SIM_FILE = "ItineraryPlusHotel.xml";
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    public static final String UNIQUE_ID = "UNIQUE_ID";
    public static final String BPEL_PROC_NS = "http://www.sun.com/javaone/05/ItineraryReservationService";
    public static final String OTA_NS = "http://www.opentravel.org/OTA/2003/05";
}
