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

package org.netbeans.modules.j2ee.dd.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.impl.common.ParseUtils;
import org.openide.filesystems.FileObject;
import org.xml.sax.*;


/** Class that collects XML parsing utility methods for web applications. It is
 * implementation private for this module, however it is also intended to be used by
 * the DDLoaders modules, which requires tighter coupling with ddapi and has an
 * implementation dependency on it.
 *
 * @author Petr Jiricka
 */
public class WebParseUtils {

    private static final Logger LOGGER = Logger.getLogger(WebParseUtils.class.getName());

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

    private static class VersionHandler extends org.xml.sax.helpers.DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("web-app".equals(rawName)) { //NOI18N
                String version = atts.getValue("version"); //NOI18N
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+(version==null?"2.3":version)); // NOI18N
            }
            if ("web-fragment".equals(rawName)) { //NOI18N
                String version = atts.getValue("version"); //NOI18N
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+version);
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
            // additional logging for #127276
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Resolving entity [publicId: '" + publicId + "', systemId: '" + systemId + "']");
            }
            String resource=null;
            // return a proper input source
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_3.dtd"; //NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_2.dtd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_2_4.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_4.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_2_5.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_5.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_3_0.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_3_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_3_1.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_3_1.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_4_0.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_4_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-fragment_3_0.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-fragment_3_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-fragment_3_1.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-fragment_3_1.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-fragment_4_0.xsd")) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-fragment_4_0.xsd"; //NOI18N
            }
            // additional logging for #127276
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Got resource: " + resource);
            }
            if (resource==null) {
                return null;
            }
            java.net.URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
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
    public static SAXParseException parse (InputSource is, EntityResolver resolver)
            throws org.xml.sax.SAXException, java.io.IOException {
        return ParseUtils.parseDD(is, resolver);
    }

}
