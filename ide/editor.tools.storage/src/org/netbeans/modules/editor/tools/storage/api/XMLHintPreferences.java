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
package org.netbeans.modules.editor.tools.storage.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author lahvac
 */
class XMLHintPreferences extends AbstractPreferences {

    private final HintPreferencesProviderImpl driver;
    private final Element parentNode;
    private final Element node;

    private XMLHintPreferences(HintPreferencesProviderImpl driver, XMLHintPreferences parent, String nodeName, Element node, Element parentNode, boolean recordedInParent) {
        super(parent, nodeName);
        this.driver = driver;
        this.node = node;
        this.parentNode = parentNode;
        this.recordedInParent = recordedInParent;
    }

    private Element findAttribute(String key) {
        NodeList nl = node.getElementsByTagName("attribute");
        
        for (int i = 0; i < nl.getLength(); i++) {
            Element attribute = (Element) nl.item(i);
            
            if (key.equals(resolve(attribute.getAttribute("name")))) {
                return attribute;
            }
        }
        
        return null;
    }
    
    @Override
    protected void putSpi(String key, String value) {
        Element found = findAttribute(key);
        
        if (found == null) {
            found = node.getOwnerDocument().createElement("attribute");
            found.setAttribute("name", escape(key));
            node.appendChild(found);
        }
        
        found.setAttribute("value", value);
        
        ensureRecordedInParent();
        driver.writeNotify();
    }

    @Override
    protected String getSpi(String key) {
        Element found = findAttribute(key);
        
        return found != null ? found.getAttribute("value") : null;
    }

    @Override
    protected void removeSpi(String key) {
        Element found = findAttribute(key);
        
        node.removeChild(found);
        
        driver.writeNotify();
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        node.getParentNode().removeChild(node);
        
        driver.writeNotify();
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        List<String> keys = new ArrayList<>();
        NodeList nl = node.getElementsByTagName("attribute");
        
        for (int i = 0; i < nl.getLength(); i++) {
            keys.add(resolve(((Element) nl.item(i)).getAttribute("name")));
        }

        return keys.toArray(new String[0]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        List<String> names = new ArrayList<>();
        NodeList nl = node.getElementsByTagName("node");
        
        for (int i = 0; i < nl.getLength(); i++) {
            names.add(resolve(((Element) nl.item(i)).getAttribute("name")));
        }

        return names.toArray(new String[0]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        String escapedName = escape(name);
        NodeList nl = node.getElementsByTagName("node");
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);

            if (n instanceof Element && escapedName.equals(((Element) n).getAttribute("name"))) {
                return new XMLHintPreferences(driver, this, name, (Element) n, node, true);
            }
        }

        Element nue = node.getOwnerDocument().createElement("node");
        
        nue.setAttribute("name", escapedName);

        return new XMLHintPreferences(driver, this, name, nue, node, false);
    }
    
    private boolean recordedInParent;
    protected synchronized void ensureRecordedInParent() {
        if (recordedInParent) return;
        recordedInParent = true;
        Preferences parent = parent();
        if (parent instanceof XMLHintPreferences) {
            ((XMLHintPreferences) parent).ensureRecordedInParent();
        }
        parentNode.appendChild(node);
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        //TODO:
    }

    @Override
    public void flush() throws BackingStoreException {
        driver.save();
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        throw new IllegalStateException();
    }

    private static String escape(String what) {
        return what;
    }

    private static String resolve(String what) {
        return what;
    }

    private static final Map<URI, Reference<HintPreferencesProviderImpl>> uri2Cache = new HashMap<>();
    
    public static HintPreferencesProviderImpl from(@NonNull URI settings) {
        Reference<HintPreferencesProviderImpl> ref = uri2Cache.get(settings);
        HintPreferencesProviderImpl cachedResult = ref != null ? ref.get() : null;
        
        if (cachedResult != null) return cachedResult;
        
        Document doc = null;
        File file = Utilities.toFile(settings); //XXX: non-file:// scheme
        
        if (file.canRead()) {
            try(InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                doc = XMLUtil.parse(new InputSource(in), false, false, null, EntityCatalog.getDefault());
            } catch (SAXException | IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        
        if (doc == null) {
            doc = XMLUtil.createDocument("configuration", null, "-//NetBeans//DTD Tool Configuration 1.0//EN", "http://www.netbeans.org/dtds/ToolConfiguration-1_0.dtd");
        }
        
        synchronized (uri2Cache) {
            ref = uri2Cache.get(settings);
            cachedResult = ref != null ? ref.get() : null;

            if (cachedResult != null) return cachedResult;
            
            uri2Cache.put(settings, new CleaneableSoftReference(cachedResult = new HintPreferencesProviderImpl(settings, doc), settings));
        }
        
        return cachedResult;
    }
    
    private static final Logger LOG = Logger.getLogger(XMLHintPreferences.class.getName());

    public static class HintPreferencesProviderImpl {//implements HintPreferencesProvider {

        private final URI settings;
        private final Document doc;

        public HintPreferencesProviderImpl(URI settings, Document doc) {
            this.settings = settings;
            this.doc = doc;
        }

        public Preferences getPreferences(String toolKind, String mimeType) {
            Element docEl = doc.getDocumentElement();
            NodeList nl = docEl.getElementsByTagName("tool");
            String escapedToolKind = escape(toolKind);
            String escapedMimeType = escape(mimeType);

            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                
                if (escapedToolKind.equals(el.getAttribute("kind")) && escapedMimeType.equals(el.getAttribute("type"))) {
                    return new XMLHintPreferences(this, null, "", el, docEl, true);
                }
            }
            
            Element el = doc.createElement("tool");
            
            el.setAttribute("kind", escapedToolKind);
            el.setAttribute("type", escapedMimeType);
            
            docEl.appendChild(el);
            
            return new XMLHintPreferences(this, null, "", el, docEl, false);
        }
        
        private long modificationCount = 0;
        private long lastSave = 0;
        
        public void save() {
            synchronized (this) {
                if (lastSave >= modificationCount) return ; //already saved
                lastSave = modificationCount;
            }
            
            File file = Utilities.toFile(settings); //XXX: non-file:// scheme
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                XMLUtil.write(doc, out, "UTF-8");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private synchronized void writeNotify() {
            modificationCount++;
            SAVER.post(new Runnable() {
                @Override public void run() {
                    save();
                }
            }, SAVE_DELAY);
        }
        
        private static final RequestProcessor SAVER = new RequestProcessor(XMLHintPreferences.class.getName(), 1, false, false);
        private static final int SAVE_DELAY = 30000;
    }

    private static final class CleaneableSoftReference extends SoftReference<HintPreferencesProviderImpl> implements Runnable {

        private static URI settings;
        public CleaneableSoftReference(HintPreferencesProviderImpl referent, URI settings) {
            super(referent, Utilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            synchronized (uri2Cache) {
                if (uri2Cache.get(settings) == this)
                    uri2Cache.remove(settings);
            }
        }
        
    }
}
