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
    
    private static abstract class MacrosReader extends StorageReader<String, MacroDescription> {
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
