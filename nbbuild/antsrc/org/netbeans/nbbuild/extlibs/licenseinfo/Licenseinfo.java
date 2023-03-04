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
package org.netbeans.nbbuild.extlibs.licenseinfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Licenseinfo {

    private File licenseinfoFile = null;
    private final List<Fileset> filesets = new ArrayList<>();

    public File getLicenseinfoFile() {
        return licenseinfoFile;
    }

    public void setLicenseinfoFile(File licenseinfoFile) {
        this.licenseinfoFile = licenseinfoFile;
    }

    public List<Fileset> getFilesets() {
        return filesets;
    }
    
    private static final DocumentBuilderFactory documentBuilderFactory;
    static {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
    }
    
    public static Licenseinfo parse(File licenseinfo) throws IOException {
        try {
            Document doc = documentBuilderFactory.newDocumentBuilder().parse(licenseinfo);
            
            if(! "licenseinfo".equals(doc.getDocumentElement().getTagName())) {
                throw new IOException("Document Element is not licenseinfo");
            }
            return parse(licenseinfo, doc.getDocumentElement());
                        
        } catch (SAXException | ParserConfigurationException | RuntimeException ex) {
            throw new IOException(ex);
        }
    }

    private static Licenseinfo parse(File licenseinfo, Element licenseinfoElement) {
        Licenseinfo li = new Licenseinfo();
        li.setLicenseinfoFile(licenseinfo);
        NodeList childNodes = licenseinfoElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element e = (Element) childNodes.item(i);
                if ("fileset".equals(e.getTagName())) {
                    Fileset fs = new Fileset();
                    fs.parse(licenseinfo, e);
                    li.getFilesets().add(fs);
                }
            }
        }
        return li;
    }
}
