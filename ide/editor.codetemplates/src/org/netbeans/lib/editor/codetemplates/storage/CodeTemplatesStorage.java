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
package org.netbeans.lib.editor.codetemplates.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
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
    displayName="org.netbeans.lib.editor.codetemplates.Bundle#CodeTemplatesResolver",
    position=520,
    mimeType="text/x-nbeditor-codetemplatesettings",
    elementName="codetemplates",
    doctypePublicId="-//NetBeans//DTD Editor Code Templates settings 1.0//EN"
)
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class CodeTemplatesStorage implements StorageDescription<String, CodeTemplateDescription> {

    private static final Logger LOG = Logger.getLogger(CodeTemplatesStorage.class.getName());

    public static final String ID = "CodeTemplates"; //NOI18N
        
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    public CodeTemplatesStorage() {
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
        return "abbreviations.xml"; //NOI18N
    }

    public StorageReader<String, CodeTemplateDescription> createReader(FileObject f, String mimePath) {
        if (MIME_TYPE.equals(f.getMIMEType())) {
            return new Reader(f, mimePath);
        } else {
            // assume legacy file
            return new LegacyReader(f, mimePath);
        }
    }

    public StorageWriter<String, CodeTemplateDescription> createWriter(FileObject f, String mimePath) {
        return new Writer();
    }

    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------
    
    private static final String E_ROOT = "codetemplates"; //NOI18N
    private static final String E_CODETEMPLATE = "codetemplate"; //NOI18N
    private static final String E_DESCRIPTION = "description"; //NOI18N
    private static final String E_CODE = "code"; //NOI18N
    private static final String A_ABBREV = "abbreviation"; //NOI18N
    private static final String A_DESCRIPTION_ID = "descriptionId"; //NOI18N
    private static final String A_CONTEXTS = "contexts"; //NOI18N
    private static final String A_UUID = "uuid"; //NOI18N
    private static final String A_REMOVE = "remove"; //NOI18N
    private static final String A_XML_SPACE = "xml:space"; //NOI18N
    private static final String V_PRESERVE = "preserve"; //NOI18N

    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor Code Templates settings 1.0//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorCodeTemplates-1_0.dtd"; //NOI18N
            
    private static final String MIME_TYPE = "text/x-nbeditor-codetemplatesettings"; //NOI18N
    
    private abstract static class TemplatesReader extends StorageReader<String, CodeTemplateDescription> {
        protected TemplatesReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        public abstract Map<String, CodeTemplateDescription> getAdded();
        public abstract Set<String> getRemoved();
    }
    
    private static final class Reader extends TemplatesReader {

        private Map<String, CodeTemplateDescription> codeTemplatesMap = new HashMap<String, CodeTemplateDescription>();
        private Set<String> removedTemplates = new HashSet<String>();
        
        // The code template being processed
        private String abbreviation = null;
        private String description = null;
        private String code = null;
        private List<String> contexts = null;
        private String uuid = null;

        private StringBuilder text = null;
        private StringBuilder cdataText = null;
        private boolean insideCdata = false;
        
        public Reader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, CodeTemplateDescription> getAdded() {
            return codeTemplatesMap;
        }

        public Set<String> getRemoved() {
            return removedTemplates;
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
            } else if (qName.equals(E_CODETEMPLATE)) {
                boolean removed = Boolean.valueOf(attributes.getValue(A_REMOVE));
                
                abbreviation = null;
                description = null;
                contexts = null;
                uuid = null;
                text = null;
                cdataText = null;
                
                if (removed) {
                    String abbrev = attributes.getValue(A_ABBREV);
                    removedTemplates.add(abbrev);
                    
                } else {
                    // Read the abbreviation
                    abbreviation = attributes.getValue(A_ABBREV);
                    
                    // Read the description and localize it
                    description = attributes.getValue(A_DESCRIPTION_ID);
                    if (description != null) {
                        String localizedDescription = StorageSupport.getLocalizingBundleMessage(
                                getProcessedFile(), description, null);
                        if (localizedDescription != null) {
                            description = localizedDescription;
                        }
                    }
                    
                    // Read contexts associated with this template
                    String ctxs = attributes.getValue(A_CONTEXTS);
                    if (ctxs != null) {
                        String [] arr = ctxs.split(","); //NOI18N
                        contexts = new ArrayList<String>(arr.length);
                        
                        for(String context : arr) {
                            context = context.trim();
                            if (context.length() > 0) {
                                contexts.add(context);
                            }
                        }
                    } else {
                        contexts = null;
                    }
                    
                    // Read the unique id
                    uuid = attributes.getValue(A_UUID);
                }
            } else if (qName.equals(E_CODE)) {
                if (abbreviation != null) {
                    // Initiate the new builder for the code template parametrized text
                    text = new StringBuilder();
                    cdataText = new StringBuilder();
                    insideCdata = false;
                }
            } else if (qName.equals(E_DESCRIPTION)) {
                if (abbreviation != null) {
                    // Initiate the new builder for the code template description
                    text = new StringBuilder();
                    cdataText = new StringBuilder();
                    insideCdata = false;
                }
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(E_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(E_CODETEMPLATE)) {
                if (abbreviation != null) {
                    CodeTemplateDescription template = new CodeTemplateDescription(
                        abbreviation,
                        description == null ? null : CharacterConversions.lineSeparatorToLineFeed(description),
                        code == null ? "" : CharacterConversions.lineSeparatorToLineFeed(code), //NOI18N
                        contexts,
                        uuid,
                        getMimePath()
                    );
                    codeTemplatesMap.put(abbreviation, template);
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
    
    private static final class LegacyReader extends TemplatesReader {

        private static final String EL_ROOT = "abbrevs"; //NOI18N
        private static final String EL_CODETEMPLATE = "abbrev"; //NOI18N
        private static final String AL_ABBREV = "key"; //NOI18N
        private static final String AL_REMOVE = "remove"; //NOI18N
        
        private Map<String, CodeTemplateDescription> codeTemplatesMap = new HashMap<String, CodeTemplateDescription>();
        private Set<String> removedTemplates = new HashSet<String>();
        
        // The code template being processed
        private String abbreviation = null;
        private StringBuilder text = null;
        
        public LegacyReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }
        
        public Map<String, CodeTemplateDescription> getAdded() {
            return codeTemplatesMap;
        }

        public Set<String> getRemoved() {
            return removedTemplates;
        }

        public @Override void characters(char[] ch, int start, int length) throws SAXException {
            if (text != null) {
                text.append(ch, start, length);
            }
        }

        public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_CODETEMPLATE)) {
                boolean removed = Boolean.valueOf(attributes.getValue(AL_REMOVE));
                
                if (removed) {
                    String abbrev = attributes.getValue(AL_ABBREV);
                    removedTemplates.add(abbrev);
                    
                    abbreviation = null;
                    text = null;
                } else {
                    abbreviation = attributes.getValue(AL_ABBREV);
                    text = new StringBuilder();
                }
            }
        }

        public @Override void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(EL_ROOT)) {
                // We don't read anything from the root
            } else if (qName.equals(EL_CODETEMPLATE)) {
                if (abbreviation != null) {
                    String parametrizedText = text.toString().replaceFirst(
                        "([^|]+)[|]([^|]+)", "$1\\${cursor}$2"); // NOI18N
                    
                    CodeTemplateDescription template = new CodeTemplateDescription(
                        abbreviation,
                        null,
                        CharacterConversions.lineSeparatorToLineFeed(parametrizedText),
                        null,
                        null,
                        getMimePath()
                    );
                    codeTemplatesMap.put(abbreviation, template);
                }
            }
        }
    } // End of LegacyReader class
    
    private static final class Writer extends StorageWriter<String, CodeTemplateDescription> {

        public Writer() {
        }
        
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(E_ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(E_ROOT).item(0);

            for(CodeTemplateDescription codeTemplate : getAdded().values()) {
                Element element = doc.createElement(E_CODETEMPLATE);
                root.appendChild(element);

                // Store template's abbreviation
                element.setAttribute(A_ABBREV, codeTemplate.getAbbreviation());

                // Store template's contexts
                List<String> contexts = codeTemplate.getContexts();
                if (contexts != null && !contexts.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < contexts.size(); i++) {
                        String ctx = contexts.get(i);
                        if (ctx != null) {
                            ctx = ctx.trim();
                            if (ctx.length() > 0) {
                                if (i > 0) {
                                    sb.append(","); //NOI18N
                                }
                                sb.append(ctx);
                            }
                        }
                    }

                    if (sb.length() > 0) {
                        element.setAttribute(A_CONTEXTS, sb.toString());
                    }
                }

                // Store template's unique id
                String uuid = codeTemplate.getUniqueId();
                if (uuid != null) {
                    element.setAttribute(A_UUID, uuid);
                }

                // Just some XML crap to preserve whitespace
                element.setAttribute(A_XML_SPACE, V_PRESERVE);

                // Store template's code
                String code = codeTemplate.getParametrizedText();
                if (code.length() > 0) {
                    Element codeElement = doc.createElement(E_CODE);
                    codeElement.appendChild(doc.createCDATASection(code)); // No EOL translations, see #130095.
                    element.appendChild(codeElement);
                }

                // Store template's description
                String description = codeTemplate.getDescription();
                if (description != null && description.length() > 0) {
                    Element descriptionElement = doc.createElement(E_DESCRIPTION);
                    descriptionElement.appendChild(doc.createCDATASection(description)); // No EOL translations, see #130095.
                    element.appendChild(descriptionElement);
                }
            }

            for(String abbreviation : getRemoved()) {
                Element element = doc.createElement(E_CODETEMPLATE);
                root.appendChild(element);

                element.setAttribute(A_ABBREV, abbreviation);
                element.setAttribute(A_REMOVE, Boolean.TRUE.toString());
            }

            return doc;
        }
    } // End of Writer class
}
