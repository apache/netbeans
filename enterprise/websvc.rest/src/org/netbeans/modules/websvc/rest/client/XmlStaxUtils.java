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

package org.netbeans.modules.websvc.rest.client;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class XmlStaxUtils {
    XMLInputFactory xmlif;
    
    public XmlStaxUtils() {
        try{
            xmlif = XMLInputFactory.newInstance();
            xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,Boolean.TRUE);
            xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
            xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE ,Boolean.TRUE);
            xmlif.setProperty(XMLInputFactory.IS_COALESCING ,
            Boolean.TRUE);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
}
    boolean isTarget(FileObject antScript, String targetName) throws IOException, javax.xml.stream.XMLStreamException {
        InputStream is = antScript.getInputStream();
        XMLStreamReader parser = xmlif.createXMLStreamReader(is);
        boolean found = false;
        int inHeader = 0;
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("target".equals(parser.getLocalName()) && targetName.equals(parser.getAttributeValue(null,"name"))) { //NOI18N
                    found = true;
                    break;
                }
            }
        } // end while
        parser.close();
        is.close();
        return found;
    }
}
