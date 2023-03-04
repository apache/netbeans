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
package org.netbeans.modules.web.core.syntax.tld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marekfukala
 */
public abstract class LibraryDescriptor {

    private FileObject definitionFile;
    private InputStream content;
    protected String prefix;
    protected String uri;
    protected String displayName;
    protected Map<String, Tag> tags = new HashMap<String, Tag>();
    protected Map<String, Function> functions = new HashMap<String, Function>();

    protected LibraryDescriptor() {
    }

    protected LibraryDescriptor(FileObject definitionFile) throws LibraryDescriptorException {
        this.definitionFile = definitionFile;
    }

    protected LibraryDescriptor(InputStream content) throws LibraryDescriptorException {
        this.content = content;
    }

    public FileObject getDefinitionFile() {
        return definitionFile;
    }

    public String getURI() {
        return uri;
    }

    public String getDefaultPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Map<String, Tag> getTags() {
        return tags;
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    protected void parseLibrary() throws LibraryDescriptorException {
        try {
            parseLibrary(getDefinitionFile().getInputStream());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected abstract void parseLibrary(InputStream content) throws LibraryDescriptorException;

    private static final String STOP_PARSING_MGS = "regularly_stopped"; //NOI18N

    protected static String parseNamespace(InputStream content, final String tagTagName, final String namespaceTagName ) {
        final String[] ns = new String[1];
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();

            class Handler extends DefaultHandler {

                private boolean inTaglib = false;
                private boolean inURI = false;

                @Override
                public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                    String tagName = qname.toLowerCase();
                    if (tagTagName.equals(tagName)) { //NOI18N
                        inTaglib = true;
                    }
                    if (inTaglib) {
                        if (namespaceTagName.equals(tagName)) { //NOI18N
                            inURI = true;
                        }

                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if(inURI) {
                        ns[0] = new String(ch, start, length).trim();
                        //stop parsing
                        throw new SAXException(STOP_PARSING_MGS);
                    }
                }

                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    return new InputSource(new StringReader("")); //prevent the parser to use catalog entity resolver // NOI18N
                }
            }


            parser.parse(content, new Handler());

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            if(!STOP_PARSING_MGS.equals(ex.getMessage())) {
                Exceptions.printStackTrace(ex);
            }
        }

        return ns[0];
    }

    @Override
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(getDefinitionFile() != null ? getDefinitionFile().getFileSystem().getRoot().getURL().toString() + ";" + getDefinitionFile().getPath() : ""); //NOI18N
            sb.append("; defaultPrefix = " + getDefaultPrefix() + "; uri = " + getURI() + "; tags={"); //NOI18N
            for (Tag t : getTags().values()) {
                sb.append(t.toString());
            }
            sb.append("}]"); //NOI18N
            return sb.toString();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    protected static String getTextContent(Node parent, String childName) {
        Node found = getNodeByName(parent, childName);
        return found == null ? null : found.getTextContent().trim();
    }

    protected static Node getNodeByName(Node parent, String childName) {
        Collection<Node> found = getNodesByName(parent, childName);
        if (!found.isEmpty()) {
            return found.iterator().next();
        } else {
            return null;
        }
    }

    protected static Collection<Node> getNodesByName(Node parent, String childName) {
        Collection<Node> nodes = new ArrayList<Node>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(childName)) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    public static interface Tag {
        
        public String getName();

        public String getDescription();

        public boolean hasNonGenenericAttributes();

        public Collection<Attribute> getAttributes();

        public Attribute getAttribute(String name);
    }

    public static interface Function {

        public String getName();

        public String getSignature();

        public String getDescription();

        public String getExample();
    }

    public static class TagImpl implements Tag {

        private static final String ID_ATTR_NAME = "id"; //NOI18N
        private String name;
        private String description;
        private Map<String, Attribute> attrs;

        public TagImpl(String name, String description, Map<String, Attribute> attrs) {
            this.name = name;
            this.description = description;
            this.attrs = attrs;

            //add the default ID attribute
            if (getAttribute(ID_ATTR_NAME) == null) {
                attrs.put(ID_ATTR_NAME, new Attribute(ID_ATTR_NAME, "", false));
            }
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean hasNonGenenericAttributes() {
            return getAttributes().size() > 1; //the ID attribute is the default generic one
        }

        public Collection<Attribute> getAttributes() {
            return attrs.values();
        }

        public Attribute getAttribute(String name) {
            return attrs.get(name);
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Tag[name=" + getName() + /*", description=" + getDescription() +*/ ", attributes={"); //NOI18N
            for (Attribute attr : getAttributes()) {
                sb.append(attr.toString() + ",");
            }
            sb.append("}]");
            return sb.toString();
        }
    }

    public static class Attribute {

        private String name;
        private String description;
        private boolean required;

        public Attribute(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public String toString() {
            return "Attribute[name=" + getName() + /*", description=" + getDescription() + */ ", required=" + isRequired() + "]"; //NOI18N
        }
    }

    public static class FunctionImpl implements Function {

        private String name;
        private String signature;
        private String desc;
        private String example;

        public FunctionImpl(String name, String signature, String desc, String example) {
            this.name = name;
            this.signature = signature;
            this.desc = desc;
            this.example = example;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public String getDescription() {
            return desc;
        }

        @Override
        public String getExample() {
            return example;
        }

        @Override
        public String toString() {
            return "Function[name=" + getName() + ", signature" + getSignature() + "]";
        }
    }
}
