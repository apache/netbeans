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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Function;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marekfukala
 */
public final class FaceletsLibraryDescriptor implements LibraryDescriptor {

    static FaceletsLibraryDescriptor create(FileObject definitionFile) throws LibraryDescriptorException {
        return new FaceletsLibraryDescriptor(definitionFile);
    }
    private FileObject definitionFile;
    private String uri;
    private String prefix;
    private Map<String, Tag> tags = new HashMap<>();
    private Map<String, Function> functions = new HashMap<>();

    private FaceletsLibraryDescriptor(FileObject definitionFile) throws LibraryDescriptorException {
        this.definitionFile = definitionFile;
        parseLibrary();
    }

    public FileObject getDefinitionFile() {
        return definitionFile;
    }

    @Override
    public String getNamespace() {
        return uri;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public Map<String, Tag> getTags() {
        return tags;
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public static String parseNamespace(InputStream content) throws IOException {
        return parseNamespace(content, "facelet-taglib", "namespace"); //NOI18N
    }

    protected void parseLibrary() throws LibraryDescriptorException {
        try {
            parseLibrary(getDefinitionFile().getInputStream());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected void parseLibrary(InputStream content) throws LibraryDescriptorException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource(content); //the ecoding should be autodetected
            EntityResolver resolver = UserCatalog.getDefault().getEntityResolver(); //we count on TaglibCatalog from web.core module
            if(resolver != null) {
                docBuilder.setEntityResolver(resolver); 
            }
            Document doc = docBuilder.parse(is);

            Node tagLib = getNodeByName(doc, "facelet-taglib"); //NOI18N

            uri = getTextContent(tagLib, "namespace"); //NOI18N
            if (uri == null) {
                throw new IllegalStateException("Missing namespace entry in " + getDefinitionFile().getPath() + " library.", null);
            }

            Node idAttrItem = tagLib.getAttributes().getNamedItem("id");
            prefix = idAttrItem != null ? idAttrItem.getNodeValue() : null;
            
            //scan the <tag> nodes content - the tag descriptions
            NodeList tagNodes = doc.getElementsByTagName("tag"); //NOI18N
            if (tagNodes != null) {
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    Node tag = tagNodes.item(i);
                    String tagName = getTextContent(tag, "tag-name"); //NOI18N
                    String tagDescription = getTextContent(tag, "description"); //NOI18N

                    Map<String, Attribute> attrs = new HashMap<>();
                    //find attributes
                    for (Node attrNode : getNodesByName(tag, "attribute")) { //NOI18N
                        String aName = getTextContent(attrNode, "name"); //NOI18N
                        String aType = getTextContent(attrNode, "type"); //NOI18N
                        String aDescription = getTextContent(attrNode, "description"); //NOI18N
                        String aMethodSignature = getTextContent(attrNode, "method-signature"); //NOI18N
                        boolean aRequired = Boolean.parseBoolean(getTextContent(attrNode, "required")); //NOI18N

                        attrs.put(aName, new Attribute.DefaultAttribute(aName, aDescription, aType, aRequired, aMethodSignature));
                    }

                    tags.put(tagName, new TagImpl(tagName, tagDescription, attrs));

                }
            }

            //scan the <function> nodes content - the function descriptions
            NodeList functionNodes = doc.getElementsByTagName("function"); //NOI18N
            if (functionNodes != null) {
                for (int i = 0; i < functionNodes.getLength(); i++) {
                    Node function = functionNodes.item(i);
                    String funcName = getTextContent(function, "function-name"); //NOI18N
                    String funcSignature = getTextContent(function, "function-signature"); //NOI18N
                    String funcDescription = getTextContent(function, "description"); //NOI18N

                    functions.put(funcName, new FunctionImpl(funcName, funcSignature, funcDescription));
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new LibraryDescriptorException("Error parsing facelets library: ", ex); //NOI18N
        }


    }
    private static final String STOP_PARSING_MGS = "regularly_stopped"; //NOI18N

    public static String parseNamespace(InputStream content, final String tagTagName, final String namespaceTagName) throws IOException {
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
                    if (inURI) {
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
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        } catch (SAXException ex) {
            if (!STOP_PARSING_MGS.equals(ex.getMessage())) {
                throw new IOException(ex);
            }
        }

        return ns[0];
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getDefinitionFile() != null ? getDefinitionFile().getFileSystem().getRoot().toURL().toString() + ";" + getDefinitionFile().getPath() : ""); //NOI18N
            sb.append("; uri = ").append(getNamespace()).append("; tags={"); //NOI18N
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

    static Node getNodeByName(Node parent, String childName) {
        Collection<Node> found = getNodesByName(parent, childName);
        if (!found.isEmpty()) {
            return found.iterator().next();
        } else {
            return null;
        }
    }

    static Collection<Node> getNodesByName(Node parent, String childName) {
        Collection<Node> nodes = new ArrayList<>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(childName)) {
                nodes.add(n);
            }
        }
        return nodes;
    }

}
