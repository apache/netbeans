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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses user library xml document.
 */
final class UserLibraryParser {
    
    private UserLibraryParser() {/* empty constructor */}
    
    /** Returns jars contained in the given user library. */
    static boolean getJars(String libName, String xmlDoc, List<String> jars, List<String> javadocs, List<String> sources) throws IOException {
        assert jars.size() == 0 && javadocs.size() == 0 && sources.size() == 0;
        UserLibraryParser parser = new UserLibraryParser();
        Document xml;
        try {
            xml = XMLUtil.parse(new InputSource(new StringReader(xmlDoc)), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException("Library '"+libName+"' cannot be parsed: " + e.toString()).initCause(e); // NOI18N
            throw ioe;
        }
        
        Element root = xml.getDocumentElement();
        if (!"userlibrary".equals(root.getLocalName())) { //NOI18N
            return false;
        }
        for (Element el : XMLUtil.findSubElements(root)) {
            if (!el.getNodeName().equals("archive")) { //NOI18N
                continue;
            }
            jars.add(el.getAttribute("path")); //NOI18N
            String src = el.getAttribute("sourceattachment"); //NOI18N
            if (src.length() > 0) {
                sources.add(src);
            }
            Element el2 = XMLUtil.findElement(el, "attributes", null); //NOI18N
            if (el2 == null) {
                continue;
            }
            for (Element el3 : XMLUtil.findSubElements(el2)) {
                if (el3.getNodeName().equals("attribute") && "javadoc_location".equals(el3.getAttribute("name"))) { //NOI18N
                    String javadoc = el3.getAttribute("value"); //NOI18N
                    if (javadoc != null) {
                        javadocs.add(javadoc);
                    }
                }
            }
        }
        return jars.size() > 0;
    }
    
}
