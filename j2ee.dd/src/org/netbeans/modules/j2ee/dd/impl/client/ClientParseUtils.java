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
