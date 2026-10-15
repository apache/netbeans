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

package org.netbeans.modules.editor.settings.storage.keybindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author Jan Jancura, Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class KeyMapsStorage implements StorageDescription<Collection<KeyStroke>, MultiKeyBinding> {

    // -J-Dorg.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage.level=FINE
    private static final Logger LOG = Logger.getLogger(KeyMapsStorage.class.getName());

    public static final String ID = "Keybindings"; //NOI18N
    /* test */ static final String MIME_TYPE = "text/x-nbeditor-keybindingsettings"; //NOI18N
        
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    public KeyMapsStorage() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isUsingProfiles() {
        return true;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public String getLegacyFileName() {
        return "keybindings.xml"; //NOI18N
    }

    @Override
    public StorageReader<Collection<KeyStroke>, MultiKeyBinding> createReader(FileObject f, String mimePath) {
        return new KeyMapsReader(f, mimePath);
    }

    @Override
    public StorageWriter<Collection<KeyStroke>, MultiKeyBinding> createWriter(FileObject f, String mimePath) {
        return new KeyMapsWriter();
    }

    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------
    
    private static final String ROOT = "bindings"; //NOI18N
    private static final String E_BIND = "bind"; //NOI18N
    private static final String A_ACTION_NAME = "actionName"; //NOI18N
    private static final String A_KEY = "key"; //NOI18N
    private static final String A_REMOVE = "remove"; //NOI18N
    private static final String V_TRUE = "true"; //NOI18N
        
    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor KeyBindings settings 1.1//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorKeyBindings-1_1.dtd"; //NOI18N

    private static class KeyMapsReader extends StorageReader<Collection<KeyStroke>, MultiKeyBinding> {
        private final Map<Collection<KeyStroke>, MultiKeyBinding> keyMap = new HashMap<>();
        private final Set<Collection<KeyStroke>> removedShortcuts = new HashSet<>();

        public KeyMapsReader(FileObject f, String mimePath) {
            super(f, mimePath);
            LOG.log(Level.FINEST, "Processing file: {0}", f.getPath());
        }
        
        @Override
        public Map<Collection<KeyStroke>, MultiKeyBinding> getAdded() {
            return keyMap;
        }
        
        @Override
        public Set<Collection<KeyStroke>> getRemoved() {
            return removedShortcuts;
        }
        
        public @Override void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes
        ) throws SAXException {
            try {
                if (name.equals(ROOT)) {
                    // We don't read anything from the root element
                    
                } else if (name.equals(E_BIND)) {
                    String key = attributes.getValue(A_KEY);
                    
                    if (isModuleFile() && isDefaultProfile() && key != null && key.length() > 0) {
                        // check the key, it should never start with 'A' or 'C', because
                        // these characters do not work on MAC, Alt should be coded as 'O'
                        // and Ctrl as 'D'
                        int idx = key.indexOf('-'); //NOI18N
                        String proccessedFilePath = getProcessedFile().getPath();
                        if (idx != -1 && (key.charAt(0) == 'A' || key.charAt(0) == 'C') && !proccessedFilePath.endsWith("-mac.xml")) { //NOI18N
                            LOG.warning("The keybinding '" + key + //NOI18N
                                "' in " + proccessedFilePath + " may not work correctly on Mac. " + //NOI18N
                                "Keybindings starting with Alt or Ctrl should " + //NOI18N
                                "be coded with latin capital letters 'O' " + //NOI18N
                                "or 'D' respectively. For details see org.openide.util.Utilities.stringToKey()."); //NOI18N
                        }
                    }
                    
                    KeyStroke[] shortcut = Utilities.stringToKeys(key.replaceAll("\\$", " ")); // NOI18N
                    String remove = attributes.getValue(A_REMOVE);
                    
                    if (Boolean.valueOf(remove)) {
                        removedShortcuts.add(Arrays.asList(shortcut));
                    } else {
                        String actionName = attributes.getValue(A_ACTION_NAME);
                        if (actionName != null) {
                            MultiKeyBinding mkb = new MultiKeyBinding(shortcut, actionName);
                            LOG.fine("Adding: Key: '" + key + "' Action: '" + mkb.getActionName() + "'");
                            MultiKeyBinding duplicate = keyMap.put(mkb.getKeyStrokeList(), mkb);
                            if (duplicate != null && !duplicate.getActionName().equals(mkb.getActionName())) {
                                LOG.warning("Duplicate shortcut '" + key + "' definition; rebound from '" + duplicate.getActionName() //NOI18N
                                        + "' to '" + mkb.getActionName() + "' in (" + getProcessedFile().getPath() + ")."); //NOI18N
                            }
                        } else {
                            LOG.warning("Ignoring keybinding '" + key + "' with no action name."); //NOI18N
                        }
                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Can't parse keybindings file " + getProcessedFile().getPath(), ex); //NOI18N
            }
        }
    } // End of KeyMapsReader class

    private static final class KeyMapsWriter extends StorageWriter<Collection<KeyStroke>, MultiKeyBinding> {
        
        public KeyMapsWriter() {
        }
        
        @Override
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(ROOT).item(0);

            List<MultiKeyBinding> added = new ArrayList<>(getAdded().values());
            added.sort(ACTION_NAME_COMPARATOR);
            for(MultiKeyBinding mkb : added) {
                Element bind = doc.createElement(E_BIND);
                root.appendChild(bind);

                bind.setAttribute(A_ACTION_NAME, mkb.getActionName());
                bind.setAttribute(A_KEY, StorageSupport.keyStrokesToString(mkb.getKeyStrokeList(), true));
            }

            for(Collection<KeyStroke> keyStrokes : getRemoved()) {
                String shortcut = StorageSupport.keyStrokesToString(keyStrokes, true);
                Element bind = doc.createElement(E_BIND);
                root.appendChild(bind);

                bind.setAttribute(A_KEY, shortcut);
                bind.setAttribute(A_REMOVE, V_TRUE);
            }
            
            return doc;
        }

        private static final Comparator<MultiKeyBinding> ACTION_NAME_COMPARATOR = Comparator.comparing(MultiKeyBinding::getActionName);
    } // End of KeyMapsWriter class
    
}
