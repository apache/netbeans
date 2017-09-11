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
