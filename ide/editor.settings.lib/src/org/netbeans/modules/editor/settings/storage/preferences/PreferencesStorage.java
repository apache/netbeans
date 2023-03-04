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
package org.netbeans.modules.editor.settings.storage.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.modules.editor.settings.storage.Utils;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.openide.filesystems.FileObject;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class PreferencesStorage implements StorageDescription<String, TypedValue> {

    private static final Logger LOG = Logger.getLogger(PreferencesStorage.class.getName());

    public static final String ID = "Preferences"; //NOI18N

    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    public PreferencesStorage() {
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
        return "properties.xml"; //NOI18N
    }

    public StorageReader<String, TypedValue> createReader(FileObject f, String mimePath) {
        if (MIME_TYPE.equals(f.getMIMEType())) {
            return new Reader(f, mimePath);
        } else {
            // assume legacy file
            return new LegacyReader(f, mimePath);
        }
    }

    public StorageWriter<String, TypedValue> createWriter(FileObject f, String mimePath) {
        return new Writer();
    }

    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------
    
    private static final String E_ROOT = "editor-preferences"; //NOI18N
    private static final String E_ENTRY = "entry"; //NOI18N
    private static final String E_VALUE = "value"; //NOI18N
    private static final String A_NAME = "name"; //NOI18N
    private static final String A_VALUE = "value"; //NOI18N
    private static final String A_VALUE_ID = "valueId"; //NOI18N
    private static final String A_JAVA_TYPE = "javaType"; //NOI18N
    private static final String A_CATEGORY = "category"; //NOI18N
    private static final String A_REMOVE = "remove"; //NOI18N
    private static final String A_XML_SPACE = "xml:space"; //NOI18N
    private static final String V_PRESERVE = "preserve"; //NOI18N

    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor Preferences 1.0//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorPreferences-1_0.dtd"; //NOI18N
            
    private static final String MIME_TYPE = "text/x-nbeditor-preferences"; //NOI18N
    
    private abstract static class PreferencesReader extends StorageReader<String, TypedValue> {
        protected PreferencesReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        public abstract Map<String, TypedValue> getAdded();
        public abstract Set<String> getRemoved();
    }
    
    private static final class Reader extends PreferencesReader {

        private Map<String, TypedValue> entriesMap = new HashMap<String, TypedValue>();
        private Set<String> removedEntries = new HashSet<String>();
        
        // The entry being processed
        private String name = null;
        private String value = null;
        private String javaType = null;
        private String apiCategory = null;

        private StringBuilder text = null;
        private StringBuilder cdataText = null;
        private boolean insideCdata = false;
        
        public Reader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, TypedValue> getAdded() {
            return entriesMap;
        }

        public Set<String> getRemoved() {
            return removedEntries;
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
            } else if (qName.equals(E_ENTRY)) {
                boolean removed = Boolean.valueOf(attributes.getValue(A_REMOVE));
                
                name = null;
                value = null;
                javaType = null;
                text = null;
                cdataText = null;
                
                if (removed) {
                    String entryName = attributes.getValue(A_NAME);
                    removedEntries.add(entryName);
                    
                } else {
                    // Read the name
                    name = attributes.getValue(A_NAME);
                    
                    // Read the value and localize it
                    String valueId = attributes.getValue(A_VALUE_ID);
                    if (valueId != null) {
                        String localizedValue = getLocalizingBundleMessage(
                                getProcessedFile(), valueId, null);
                        if (localizedValue != null) {
                            value = localizedValue;
                        }
                    }

                    String valueValue = attributes.getValue(A_VALUE);
                    if (valueValue != null) {
                        if (value == null) {
                            value = valueValue;
                        } else {
                            LOG.warning("The 'valueId' attribute specified valid resource bundle key, ignoring the 'value' attribute!"); //NOI18N
                        }
                    }
                    
                    // Read the type of the value
                    javaType = attributes.getValue(A_JAVA_TYPE);
                    
                    // Read the API category
                    apiCategory = attributes.getValue(A_CATEGORY);
                }
            } else if (name != null && qName.equals(E_VALUE)) {
                // Initiate the new builder for the entry value
                if (value == null) {
                    text = new StringBuilder();
                    cdataText = new StringBuilder();
                    insideCdata = false;
                } else {
                    LOG.warning("The 'value' or 'valueId' attribute was specified, ignoring the <value/> element!"); //NOI18N
                }
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(E_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(E_ENTRY)) {
                if (name != null) {
                    if (value != null) {
                        if (!entriesMap.containsKey(name)) {
                            TypedValue typedValue = new TypedValue(CharacterConversions.lineSeparatorToLineFeed(value), javaType);
                            if (apiCategory != null && apiCategory.length() > 0) {
                                typedValue.setApiCategory(checkApiCategory(apiCategory));
                            }
                            entriesMap.put(name, typedValue);
                        } else {
                            LOG.warning("Ignoring duplicate editor preferences entry '" + name + "'!"); //NOI18N
                        }
                    } else {
                        LOG.warning("Ignoring editor preferences entry '" + name + "' that does not specify any value!"); //NOI18N
                    }
                }
            } else if (qName.equals(E_VALUE)) {
                if (text != null) {
                    value = cdataText.length() > 0 ? cdataText.toString() : text.toString();
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
        
        // for the list see EditorPreferences-1_0.dtd
        private static final String [] ALL_API_CATEGORIES = new String [] { "private", "stable", "devel", "friend", "deprecated" }; //NOI18N
        private static String checkApiCategory(String apiCategory) {
            for(String c : ALL_API_CATEGORIES) {
                if (c.equalsIgnoreCase(apiCategory)) {
                    return c;
                }
            }
            return ALL_API_CATEGORIES[0];
        }
    } // End of Reader class
    
    private static final class LegacyReader extends PreferencesReader {

        private static final String EL_ROOT = "properties"; //NOI18N
        private static final String EL_PROPERTY = "property"; //NOI18N
        private static final String AL_NAME = "name"; //NOI18N
        private static final String AL_CLASS = "class"; //NOI18N   we can safely ignore this
        private static final String AL_VALUE = "value"; //NOI18N
        
        private Map<String, TypedValue> entriesMap = new HashMap<String, TypedValue>();
        
        // The entry being processed
        private String name = null;
        private String value = null;
        private String javaType = null;
        
        public LegacyReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, TypedValue> getAdded() {
            return entriesMap;
        }

        public Set<String> getRemoved() {
            return Collections.<String>emptySet();
        }

        public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_PROPERTY)) {
                name = attributes.getValue(AL_NAME);
                value = attributes.getValue(AL_VALUE);
                javaType = attributes.getValue(AL_CLASS);
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_PROPERTY)) {
                if (name != null && value != null) {
                    entriesMap.put(name, new TypedValue(value, javaType));
                } else {
                    LOG.warning("Ignoring editor preferences legacy entry {'" + name + "', '" + value + "'}!"); //NOI18N
                }
            }
        }
    } // End of LegacyReader class
    
    private static final class Writer extends StorageWriter<String, TypedValue> {

        public Writer() {
        }
        
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(E_ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(E_ROOT).item(0);

            TreeMap<String, TypedValue> added = new  TreeMap<String, TypedValue>(getAdded());
            for(String name : added.keySet()) {
                Element element = doc.createElement(E_ENTRY);
                root.appendChild(element);

                // Store entry's name
                element.setAttribute(A_NAME, name);

                // Store entry's value
                String value = getAdded().get(name).getValue();
                if (value.length() > 0) {
                    Element valueElement = doc.createElement(E_VALUE);
                    valueElement.appendChild(doc.createCDATASection(
                        CharacterConversions.lineFeedToLineSeparator(value)));
                    element.appendChild(valueElement);
                } else {
                    element.setAttribute(name, value);
                }

                // Store entry's value type (if any)
                String javaType = getAdded().get(name).getJavaType();
                if (javaType != null && javaType.length() > 0) {
                    element.setAttribute(A_JAVA_TYPE, javaType);
                }

                // Store entry's API stability (if any)
                String apiCategory = getAdded().get(name).getApiCategory();
                if (apiCategory != null && apiCategory.length() > 0) {
                    element.setAttribute(A_CATEGORY, apiCategory);
                }
                
                // Just some XML crap to preserve whitespace
                element.setAttribute(A_XML_SPACE, V_PRESERVE);
            }

            List<String> removed = new ArrayList<String>(getRemoved());
            Collections.sort(removed);
            for(String name : removed) {
                Element element = doc.createElement(E_ENTRY);
                root.appendChild(element);

                element.setAttribute(A_NAME, name);
                element.setAttribute(A_REMOVE, Boolean.TRUE.toString());
            }

            return doc;
        }
    } // End of Writer class

    public static String getLocalizingBundleMessage(FileObject fo, String key, String defaultValue) {
        return Utils.getLocalizedName(fo, key, defaultValue, false);
    }

}
