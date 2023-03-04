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
package org.netbeans.modules.editor.macros.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.modules.editor.macros.MacroDialogSupport;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Vita Stejskal
 */
@MIMEResolver.NamespaceRegistration(
    displayName="org.netbeans.modules.editor.macros.Bundle#MacrosResolver",
    position=530,
    doctypePublicId="-//NetBeans//DTD Editor Macros settings 1.1//EN",
    mimeType="text/x-nbeditor-macrosettings",
    elementName="editor-macros"
)
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class MacrosStorage implements StorageDescription<String, MacroDescription> {

    private static final Logger LOG = Logger.getLogger(MacrosStorage.class.getName());

    public static final String ID = "Macros"; //NOI18N
        
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    public MacrosStorage() {
    }

    public String getId() {
        return ID;
    }

    public boolean isUsingProfiles() {
        return false;
    }

    public String getMimeType() {
        return MIME_TYPE;
    }

    public String getLegacyFileName() {
        return "macros.xml"; //NOI18N
    }

    public StorageReader<String, MacroDescription> createReader(FileObject f, String mimePath) {
        if (MIME_TYPE.equals(f.getMIMEType())) {
            return new Reader(f, mimePath);
        } else {
            // assume legacy file
            return new LegacyReader(f, mimePath);
        }
    }

    public StorageWriter<String, MacroDescription> createWriter(FileObject f, String mimePath) {
        return new Writer();
    }

    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------
    
    private static final String E_ROOT = "editor-macros"; //NOI18N
    private static final String E_MACRO = "macro"; //NOI18N
    private static final String E_DESCRIPTION = "description"; //NOI18N
    private static final String E_CODE = "code"; //NOI18N
    private static final String E_SHORTCUT = "shortcut"; //NOI18N
    private static final String A_NAME = "name"; //NOI18N
    private static final String A_DESCRIPTION_ID = "descriptionId"; //NOI18N
    private static final String A_KEYSTROKES = "keystrokes"; //NOI18N
    private static final String A_REMOVE = "remove"; //NOI18N
    private static final String A_XML_SPACE = "xml:space"; //NOI18N
    private static final String V_PRESERVE = "preserve"; //NOI18N

    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor Macros settings 1.1//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorMacros-1_1.dtd"; //NOI18N
            
    private static final String MIME_TYPE = "text/x-nbeditor-macrosettings"; //NOI18N
    
    private abstract static class MacrosReader extends StorageReader<String, MacroDescription> {
        protected MacrosReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        public abstract Map<String, MacroDescription> getAdded();
        public abstract Set<String> getRemoved();
    }
    
    private static final class Reader extends MacrosReader {

        private Map<String, MacroDescription> macrosMap = new HashMap<String, MacroDescription>();
        private Set<String> removedMacros = new HashSet<String>();
        
        // The macro being processed
        private String name = null;
        private String description = null;
        private String code = null;
        private List<MultiKeyBinding> shortcuts = null;

        private StringBuilder text = null;
        private StringBuilder cdataText = null;
        private boolean insideCdata = false;
        
        public Reader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, MacroDescription> getAdded() {
            return macrosMap;
        }

        public Set<String> getRemoved() {
            return removedMacros;
        }

        public @Override void characters(char[] ch, int start, int length) throws SAXException {
            if (text != null) {
                text.append(ch, start, length);
                
                if (insideCdata) {
                    cdataText.append(ch, start, length);
                }
            }
        }

        public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(E_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(E_MACRO)) {
                boolean removed = Boolean.valueOf(attributes.getValue(A_REMOVE));
                
                name = null;
                description = null;
                shortcuts = null;
                text = null;
                cdataText = null;
                
                if (removed) {
                    String macroName = attributes.getValue(A_NAME);
                    removedMacros.add(macroName);
                    
                } else {
                    // Read the name
                    name = attributes.getValue(A_NAME);
                    
                    // Read the description and localize it
                    description = attributes.getValue(A_DESCRIPTION_ID);
                    if (description != null) {
                        String localizedDescription = StorageSupport.getLocalizingBundleMessage(
                                getProcessedFile(), description, null);
                        if (localizedDescription != null) {
                            description = localizedDescription;
                        }
                    }
                    
                    shortcuts = new ArrayList<MultiKeyBinding>();
                }
            } else if (name != null && qName.equals(E_CODE)) {
                // Initiate the new builder for the code template parametrized text
                text = new StringBuilder();
                cdataText = new StringBuilder();
                insideCdata = false;
            } else if (name != null && qName.equals(E_DESCRIPTION)) {
                // Initiate the new builder for the code template description
                text = new StringBuilder();
                cdataText = new StringBuilder();
                insideCdata = false;
            } else if (name != null && qName.equals(E_SHORTCUT)) {
                String keystrokes = attributes.getValue(A_KEYSTROKES);
                
                if (isModuleFile() && isDefaultProfile() && keystrokes != null && keystrokes.length() > 0) {
                    // check the key, it should never start with 'A' or 'C', because
                    // these characters do not work on MAC, Alt should be coded as 'O'
                    // and Ctrl as 'D'
                    int idx = keystrokes.indexOf('-'); //NOI18N
                    if (idx != -1 && (keystrokes.charAt(0) == 'A' || keystrokes.charAt(0) == 'C')) { //NOI18N
                        LOG.warning("The keybinding '" + keystrokes + //NOI18N
                            "' in " + getProcessedFile().getPath() + " may not work correctly on Mac. " + //NOI18N
                            "Keybindings starting with Alt or Ctrl should " + //NOI18N
                            "be coded with latin capital letters 'O' " + //NOI18N
                            "or 'D' respectively. For details see org.openide.util.Utilities.stringToKey()."); //NOI18N
                    }
                }

                KeyStroke[] arr = StorageSupport.stringToKeyStrokes(keystrokes, true);
                if (arr == null) {
                    LOG.warning("Cannot decode key bindings: " + keystrokes);
                } else {
                    shortcuts.add(new MultiKeyBinding(arr, MacroDialogSupport.RunMacroAction.runMacroAction)); //NOI18N
                }
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(E_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(E_MACRO)) {
                if (name != null) {
                    MacroDescription macro = new MacroDescription(
                        name,
                        code == null ? "" : CharacterConversions.lineSeparatorToLineFeed(code), //NOI18N
                        description == null ? null : CharacterConversions.lineSeparatorToLineFeed(description),
                        Collections.unmodifiableList(shortcuts)
                    );
                    macrosMap.put(name, macro);
                }
            } else if (qName.equals(E_CODE)) {
                if (text != null) {
                    code = cdataText.length() > 0 ? cdataText.toString() : text.toString();
                }
            } else if (qName.equals(E_DESCRIPTION)) {
                if (text != null) {
                    if (cdataText.length() > 0) {
                        description = cdataText.toString();
                    } else if (text.length() > 0) {
                        description = text.toString();
                    }
                }
            }
        }

        public @Override void startCDATA() throws SAXException {
            if (cdataText != null) {
                insideCdata = true;
            }
        }
        
        public @Override void endCDATA() throws SAXException {
            if (cdataText != null) {
                insideCdata = false;
            }
        }
    } // End of Reader class
    
    private static final class LegacyReader extends MacrosReader {

        private static final String EL_ROOT = "macros"; //NOI18N
        private static final String EL_MACRO = "macro"; //NOI18N
        private static final String AL_NAME = "name"; //NOI18N
        private static final String AL_REMOVE = "remove"; //NOI18N
        
        private Map<String, MacroDescription> macrosMap = new HashMap<String, MacroDescription>();
        private Set<String> removedMacros = new HashSet<String>();
        
        // The macro being processed
        private String name = null;
        private StringBuilder text = null;
        
        public LegacyReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, MacroDescription> getAdded() {
            return macrosMap;
        }

        public Set<String> getRemoved() {
            return removedMacros;
        }

        public @Override void characters(char[] ch, int start, int length) throws SAXException {
            if (text != null) {
                text.append(ch, start, length);
            }
        }

        public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_MACRO)) {
                boolean removed = Boolean.valueOf(attributes.getValue(AL_REMOVE));
                
                if (removed) {
                    String abbrev = attributes.getValue(AL_NAME);
                    removedMacros.add(abbrev);
                    
                    name = null;
                    text = null;
                } else {
                    name = attributes.getValue(AL_NAME);
                    text = new StringBuilder();
                }
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_MACRO)) {
                if (name != null) {
                    MacroDescription macro = new MacroDescription(
                        name,
                        CharacterConversions.lineSeparatorToLineFeed(text),
                        null,
                        Collections.<MultiKeyBinding>emptyList()
                    );
                    macrosMap.put(name, macro);
                }
            }
        }
    } // End of LegacyReader class
    
    private static final class Writer extends StorageWriter<String, MacroDescription> {

        public Writer() {
        }
        
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(E_ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(E_ROOT).item(0);

            for(MacroDescription macro : getAdded().values()) {
                Element element = doc.createElement(E_MACRO);
                root.appendChild(element);

                // Store macro's name
                element.setAttribute(A_NAME, macro.getName());

                // Store macro's shortcuts
                List<? extends MultiKeyBinding> shortcuts = macro.getShortcuts();
                if (shortcuts != null && shortcuts.size() > 0) {
                    for(MultiKeyBinding mkb : shortcuts) {
                        Element shortcutElement = doc.createElement(E_SHORTCUT);
                        shortcutElement.setAttribute(A_KEYSTROKES, StorageSupport.keyStrokesToString(mkb.getKeyStrokeList(), true));
                        element.appendChild(shortcutElement);
                    }
                }

                // Just some XML crap to preserve whitespace
                element.setAttribute(A_XML_SPACE, V_PRESERVE);

                // Store macro's code
                String code = macro.getCode();
                if (code.length() > 0) {
                    Element codeElement = doc.createElement(E_CODE);
                    codeElement.appendChild(doc.createCDATASection(
                        CharacterConversions.lineFeedToLineSeparator(code)));
                    element.appendChild(codeElement);
                }

                // Store macro's description
                String description = macro.getDescription();
                if (description != null && description.length() > 0) {
                    Element descriptionElement = doc.createElement(E_DESCRIPTION);
                    descriptionElement.appendChild(doc.createCDATASection(
                        CharacterConversions.lineFeedToLineSeparator(description)));
                    element.appendChild(descriptionElement);
                }
            }

            for(String name : getRemoved()) {
                Element element = doc.createElement(E_MACRO);
                root.appendChild(element);

                element.setAttribute(A_NAME, name);
                element.setAttribute(A_REMOVE, Boolean.TRUE.toString());
            }

            return doc;
        }
    } // End of Writer class
}
