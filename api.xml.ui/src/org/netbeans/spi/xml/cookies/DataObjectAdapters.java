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

package org.netbeans.spi.xml.cookies;

import java.io.Reader;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.api.xml.services.UserCatalog;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Adapt <code>DataObject</code> to other common XML interfaces.
 *
 * @author      Petr Kuzel
 * @since       0.9
 */
public final class DataObjectAdapters {
    
    /** SAX feature: Perform namespace processing. */
    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N
    
    /** cached SAXParserFactory instance. */
    private static SAXParserFactory saxParserFactory;
    
    private  DataObjectAdapters() {
    }
    
    /**
     * Create InputSource from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @return <code>DataObject</code> never <code>null</code>
     */           
    public static InputSource inputSource (DataObject dataObject) {
        if (dataObject == null) throw new NullPointerException();
        return new DataObjectInputSource(dataObject);
    }

    /**
     * Lazy evaluated wrapper.
     */
    private static class DataObjectInputSource extends InputSource {
        
        private final DataObject dataObject;
        
        public DataObjectInputSource (DataObject dataObject) {
            this.dataObject = dataObject;
        }
                
        public String getSystemId() {
            return DataObjectAdapters.getSystemId (dataObject);
        }
        
        public Reader getCharacterStream() {

            EditorCookie editor = (EditorCookie) dataObject.getCookie(EditorCookie.class);

            if (editor != null) {
                Document doc = editor.getDocument();
                if (doc != null) {
                    return  new DocumentInputSource(doc).getCharacterStream();
                }
            }             
            
            return null;
        }
        
    }

    
    /**
     * Create Source from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @return <code>DataObject</code> never <code>null</code>
     */               
    public static Source source (DataObject dataObject) {
        if (dataObject == null) throw new NullPointerException();        
        return new DataObjectSAXSource(dataObject);
    }

    /**
     * Lazy evaluated wrapper.
     */    
    private static class DataObjectSAXSource extends SAXSource {
        
        private final DataObject dataObject;
        
        public DataObjectSAXSource(DataObject dataObject) {
            this.dataObject = dataObject;
        }
        
        public String getSystemId() {
            return DataObjectAdapters.getSystemId (dataObject);
        }
        
        public XMLReader getXMLReader() {
            try {
                XMLReader reader = newXMLReader();
                reader.setEntityResolver (getEntityResolver());
                return reader;
            } catch (ParserConfigurationException ex) {
                Util.THIS.debug(ex);
            } catch (SAXNotRecognizedException ex) {
                Util.THIS.debug(ex);
            } catch (SAXNotSupportedException ex) {
                Util.THIS.debug(ex);
            } catch (SAXException ex) {
                Util.THIS.debug(ex);
            }
            return null;            
        }
        
        public InputSource getInputSource() {
            return inputSource (dataObject);
        }

    } // class DataObjectSAXSource


    /** Try to find the best URL name of <code>dataObject</code>.
     * @return system Id of <code>dataObject</code>
     */
    private static String getSystemId (DataObject dataObject) {
        return dataObject.getPrimaryFile().toURI().toASCIIString();
    }

    private static synchronized SAXParserFactory getSAXParserFactory () throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if ( saxParserFactory == null ) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature (SAX_FEATURES_NAMESPACES, true);
        }
        return saxParserFactory;
    }

    /**
     *
     * @throws ParserConfigurationException if a parser cannot
     *         be created which satisfies the requested configuration.
     * @throws SAXException if a parser cannot be created which satisfies the requested configuration.
     */
    private static XMLReader newXMLReader () throws ParserConfigurationException, SAXException {
        SAXParser parser = getSAXParserFactory().newSAXParser();  //!!! it is expensive!
        return parser.getXMLReader();
    }
    
    private static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }        
}
