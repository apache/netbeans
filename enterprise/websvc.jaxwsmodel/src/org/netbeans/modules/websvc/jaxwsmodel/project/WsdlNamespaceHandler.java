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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author mkuchtiak
 */
public class WsdlNamespaceHandler extends DefaultHandler {
    public static final String SAX_PARSER_FINISHED_OK="sax_parser_finished_correctly"; //NOI18N
    
    public static final String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/";
    private String targetNamespace;

    public void parse(File file) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(file, this);
    }
    public String getTargetNamespace() {
        return targetNamespace;
    }
    public void startElement(String uri, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if ("definitions".equals(localName)) { // NOI18N
            targetNamespace = attributes.getValue("targetNamespace");// NOI18N
            throw new SAXException(SAX_PARSER_FINISHED_OK);
        }
    }
}
