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

package org.netbeans.modules.j2ee.persistence.dd.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * similar to web app parser, can be used to get different useful information about persistence.xml
 *
 * @author sp153251
 */
public class JPAParseUtils {

    private static final Logger LOGGER = Logger.getLogger(JPAParseUtils.class.getName());

    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(InputStream is) throws IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }

    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is) throws IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }

    private static class VersionHandler extends org.xml.sax.helpers.DefaultHandler {
        
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("persistence".equals(rawName)) { //NOI18N
                String version = atts.getValue("version"); //NOI18N
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+(version==null?Persistence.VERSION_3_1:version));
            }
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            return super.resolveEntity(publicId, systemId);
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if ("persistence".equals(qName)) { //NOI18N
                String version = attributes.getValue("version"); //NOI18N
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+(version==null?Persistence.VERSION_3_1:version));
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

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            // additional logging for #127276
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Resolving entity [publicId: ''{0}'', systemId: ''{1}'']", new Object[]{publicId, systemId});
            }
            String resource=null;
            // return a proper input source
            if (systemId!=null && systemId.endsWith("persistence_3_2.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_3_2.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_3_0.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_3_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_2_2.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_2_2.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_2_1.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_2_1.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_2_0.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_2_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_1_0.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_1_0.xsd"; //NOI18N
            }
            // additional logging for #127276
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Got resource: {0}", resource);
            }
            if (resource==null) {
                return null;
            }
            URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }

    public static SAXParseException parse(FileObject fo)
            throws SAXException, IOException {
        // no need to close the stream, will be closed by the parser, see @org.xml.sax.InputSource
        return parse(new InputSource(fo.getInputStream()));
    }

    public static SAXParseException parse (InputSource is)
            throws SAXException, IOException {
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
            throws SAXException, IOException {
        return ParseUtils.parseDD(is, resolver);
    }


}
