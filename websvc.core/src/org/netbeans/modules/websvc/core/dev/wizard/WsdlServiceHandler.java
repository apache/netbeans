/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.core.dev.wizard;

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
