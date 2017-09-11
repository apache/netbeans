/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.dd.common;

import java.io.IOException;
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
    public static String getVersion(java.io.InputStream is) throws java.io.IOException, SAXException {
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
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+(version==null?Persistence.VERSION_2_1:version));
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
                throw new SAXException(ParseUtils.EXCEPTION_PREFIX+(version==null?Persistence.VERSION_2_1:version));
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
            if (systemId!=null && systemId.endsWith("persistence_2_1.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_2_1.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_2_0.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_2_0.xsd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("persistence_1_0.xsd")) {
                resource="/org/netbeans/modules/j2ee/persistence/dd/resources/persistence_1_0.xsd"; //NOI18N
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
        // no need to close the stream, will be closed by the parser, see @org.xml.sax.InputSource
        return parse(new InputSource(fo.getInputStream()));
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
