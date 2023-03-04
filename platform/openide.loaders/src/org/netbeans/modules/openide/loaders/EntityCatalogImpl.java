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

package org.netbeans.modules.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.util.RequestProcessor;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Recognizes catalogs defined by {@link EntityCatalog#PUBLIC_ID}, which presumably must 
 * Do not use this style; {@link FileEntityResolver} implements the preferred system.
 * @author  Petr Kuzel
 */
@Deprecated
public final class EntityCatalogImpl extends EntityCatalog {

    /** map between publicId and privateId (String, String); must be synchronized */
    private Map<String, String> id2uri;  // accessed from SystemCatalogReader in xml/catalog by reflection

    private static final RequestProcessor catalogRP = new RequestProcessor("EntityCatalog/parser"); // NOI18N

    /** Creates new EntityCatalogImpl */
    private EntityCatalogImpl(Map<String,String> map) {
        id2uri = map;
    }
    
    /**
     * Resolve an entity using cached mapping.
     */
    public InputSource resolveEntity(String publicID, String systemID) {
        if (publicID == null) return null;

        String res = id2uri.get(publicID); // note this is synchronized Hashtable

        InputSource ret = null;
        if (res != null) {
            ret = new InputSource(res);
        }
            
//            System.err.println("" + publicID + " => " + ret);
        return ret;
    }

    /** 
     * XMLDataObject.Processor implementation recognizing EntityCatalog.PUBLIC_ID DTDs
     * giving them instance cookie returning registered entries.
     */
    public static class RegistrationProcessor extends DefaultHandler implements XMLDataObject.Processor, InstanceCookie, Runnable, PropertyChangeListener {

        private XMLDataObject peer;
        private Map<String, String> map;
        private RequestProcessor.Task parsingTask = catalogRP.create(this);
        private EntityCatalogImpl instance = null;

        // Processor impl

        public void attachTo (XMLDataObject xmlDO) {
            
            if (xmlDO == peer) return;  //ignore double attachements
            
            peer = xmlDO;                        
            peer.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(this, peer));  //listen at PROP_DOCUMENT
            parsingTask.schedule(0);
        }

        // DefaultHandler extension

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if ("public".equals(qName)) {  //NOI18N
                String key = atts.getValue("publicId");  //NOI18N
                String val = atts.getValue("uri");  //NOI18N

                if (key != null && val != null) {
                    map.put(key, val);
                } else {
                    throw new SAXException ("invalid <public> element: missing publicId or uri"); // NOI18N
                }
            }
        }

        @Override
        public InputSource resolveEntity(String pid, String sid) {
            if (EntityCatalog.PUBLIC_ID.equals(pid)) {
                // Don't use a nbres: URL here; can deadlock NbURLStreamHandlerFactory during startup
                return new InputSource(EntityCatalogImpl.class.getResource("EntityCatalog.dtd").toExternalForm()); // NOI18N
            }
            return null;
        }

        // Runnable impl (can be a task body)

        public void run() {
            map = new Hashtable<String, String>();  //be synchronized

            try {
                String loc = peer.getPrimaryFile().getURL().toExternalForm();
                InputSource src = new InputSource(loc);

                // XXX(-ttran) don't validate
                XMLReader reader = XMLUtil.createXMLReader(false);
                reader.setErrorHandler(this);
                reader.setContentHandler(this);
                reader.setEntityResolver(this);
                reader.parse(src);
            } catch (SAXException ex) {
                // ignore
                Logger.getLogger(EntityCatalogImpl.class.getName()).log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                // ignore
        	Logger.getLogger(EntityCatalogImpl.class.getName()).log(Level.WARNING, null, ex);
    	    }
        }

        // InstanceCookie impl

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return EntityCatalog.class;
        }

        /** We return singleton instance */
        public Object instanceCreate() throws IOException, ClassNotFoundException {
            
            synchronized (this) {
                if (instance == null) {
                    parsingTask.waitFinished();                        
                    instance = new EntityCatalogImpl (map);
                }
            }
            return instance;
        }

        public String instanceName() {
            return "org.openide.xml.EntityCatalog"; // NOI18N
        }

        /**
          * Perform synchronous update on fileobject change.
          */
        public void propertyChange(PropertyChangeEvent e) {
            
            synchronized(this) {
                if (instance == null) return;
            }
            
            if (XMLDataObject.PROP_DOCUMENT.equals(e.getPropertyName())) {
                // Please use ErrorManager if debugging messages are required
                // (try TM.getErrorManager().getInstance(thisClassName).log(message))
                //System.err.println("XML file have changed. reparsing " + peer.getPrimaryFile() ); // NOI18N
                //update it sync
                run();
                instance.id2uri = map;  //replace map
            }
        }
        
    }
}
