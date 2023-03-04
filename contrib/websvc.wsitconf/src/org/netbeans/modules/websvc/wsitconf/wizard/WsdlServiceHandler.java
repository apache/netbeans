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

package org.netbeans.modules.websvc.wsitconf.wizard;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** ConteHandler that gives information of the first service and port
 *
 * @author mkuchtiak
 */
public class WsdlServiceHandler extends DefaultHandler{
    
    public static final String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/"; //NOI18N
    
    private boolean insideService;
    private String serviceName, portName;
    
    public static WsdlServiceHandler parse(String wsdlUrl) throws ParserConfigurationException, SAXException, IOException {
        WsdlServiceHandler handler = new WsdlServiceHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(wsdlUrl, handler);
        return handler;
    }
    
    /** Creates a new instance of WsdlWrapperHandler */
    private WsdlServiceHandler() {
    }    
    
    public void startElement(String uri, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if (WSDL_SOAP_URI.equals(uri) && "service".equals(localName)) { // NOI18N
            insideService=true;
            if (serviceName==null) {
                serviceName = attributes.getValue("name");// NOI18N
            }
        } else if("port".equals(localName) && insideService) { // NOI18N
            if (portName==null) {
                portName = attributes.getValue("name"); // NOI18N
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (WSDL_SOAP_URI.equals(uri) && "service".equals(localName)) {
            insideService=false;
        }
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getPortName() {
        return portName;
    }
}
