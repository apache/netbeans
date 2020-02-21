/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.test;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * In test environment MIME types are not always correctly loaded.
 * 
 * ATM, this resolver is not used due to extensive changes in tests output
 * brought in by loading NetBeans FontColor settings profiles.
 *
 */
public class HelperMIMEResolver extends MIMEResolver {

    private static final String FCSDOCTYPE = "-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN";

    public HelperMIMEResolver() {
        super("text/x-nbeditor-fontcolorsettings");
    }
    
    @Override
    public String findMIMEType(FileObject fo) {
        if (fo.hasExt("xml")) {
            try {
                SAXParser p = SAXParserFactory.newInstance().newSAXParser();
                DocTypeHunter dh = new DocTypeHunter();
                p.parse(fo.getInputStream(), dh);
                
                //BufferedReader isr = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                //for(String st = "xml: " + fo; st != null; st = isr.readLine() ) System.err.println(st);
                
                if (FCSDOCTYPE.equals(dh.docTypeValue)) {
                    return "text/x-nbeditor-fontcolorsettings";
                }
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private static class DocTypeHunter extends DefaultHandler {

        String docTypeValue;

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            docTypeValue = publicId;
            return super.resolveEntity(publicId, systemId);
        }
    }
}
