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
