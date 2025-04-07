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

package org.netbeans.modules.j2ee.dd.impl.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.impl.common.ParseUtils;
import org.openide.filesystems.FileObject;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/** Class that collects XML parsing utility methods for appclient applications. It is 
 * implementation private for this module, however it is also intended to be used by 
 * the DDLoaders modules, which requires tighter coupling with ddapi and has an 
 * implementation dependency on it.
 *
 * @author Petr Jiricka
 */
public class ClientParseUtils {
  
    private static final Logger LOGGER = Logger.getLogger(ClientParseUtils.class.getName());

    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(java.io.InputStream is) throws java.io.IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }

    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(FileObject fo) throws java.io.IOException, SAXException {
        InputStream inputStream = fo.getInputStream();
        try {
            return ParseUtils.getVersion(inputStream, new VersionHandler(), DDResolver.getInstance());
        } finally {
            inputStream.close();
        }
    }
    
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is) throws IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }
    
    private static class VersionHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("application-client".equals(rawName)) { //NOI18N
                String version = parseVersion(atts.getValue("version")); //NOI18N
                String msg = version != null ? ParseUtils.EXCEPTION_PREFIX + version : "Invalid version: null"; //NO18N
                throw new SAXException(msg);
            }
        }
        
        private String parseVersion(String version){
            if (version == null){
                return null;
            }
            try {
                Double.valueOf(version);
                return version;
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.INFO, "Not a valid version: " + version, nfe); //NO18N
                return null;
            }
        }
    }
    
    //XXX: note that this resolver does not handle entities from included schemas
    // correctly. See #116379.
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }
        public InputSource resolveEntity(String publicId, String systemId) {
            if ("-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN".equals(publicId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_1_3.dtd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_1_4.xsd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/javaee/application-client_5.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_5.xsd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/javaee/application-client_6.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_6.xsd"); //NOI18N
            } else if ("http://xmlns.jcp.org/xml/ns/javaee/application-client_7.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_7.xsd"); //NOI18N
            } else if ("http://xmlns.jcp.org/xml/ns/javaee/application-client_8.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_8.xsd"); //NOI18N
            } else if ("https://jakarta.ee/xml/ns/jakartaee/application-client_9.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_9.xsd"); //NOI18N
            } else if ("https://jakarta.ee/xml/ns/jakartaee/application-client_10.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_10.xsd"); //NOI18N
            } else if ("https://jakarta.ee/xml/ns/jakartaee/application-client_11.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_11.xsd"); //NOI18N
            } else {
                // use the default behaviour
                return null;
            }
        }
    }


    public static SAXParseException parse(FileObject fo)
    throws org.xml.sax.SAXException, java.io.IOException {
        InputStream inputStream = fo.getInputStream();
        try {
            return parse(new InputSource(inputStream));
        } finally {
            inputStream.close();
        }
    }
    
    public static SAXParseException parse (InputSource is) 
            throws org.xml.sax.SAXException, java.io.IOException {
        return ParseUtils.parseDD(is, DDResolver.getInstance());
    }

    /**
     * Parses the given <code>inputSource</code> using the given <code>resolver</code>.
     * @param inputSource the source to parse.
     * @param resolver the resolver to use for parsing.
     * @return the SAX exception encountered during parsing or null if there was
     * no exception.
     */
    public static SAXParseException parse(InputSource inputSource, EntityResolver resolver)
            throws org.xml.sax.SAXException, java.io.IOException {
        return ParseUtils.parseDD(inputSource, resolver);
    }
}
