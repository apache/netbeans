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

            EditorCookie editor = dataObject.getCookie(EditorCookie.class);

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
