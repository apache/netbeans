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

package org.netbeans.modules.payara.common.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser that invokes a user defined node reader(s) on a list of xpath
 * designated nodes.
 * 
 * @author Peter Williams
 */
public final class TreeParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger("payara");
    private static final boolean isFinestLoggable = LOGGER.isLoggable(Level.FINEST);
    private static final boolean isFinerLoggable = LOGGER.isLoggable(Level.FINER);

    public static boolean readXml(File xmlFile, List<Path> pathList) throws IllegalStateException {
        boolean result = false;
        InputStreamReader reader = null;
        try {
            // !PW FIXME what to do about entity resolvers?  Timed out when
            // looking up doctype for sun-resources.xml earlier today (Jul 10)
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // !PW If namespace-aware is enabled, make sure localpart and
            // qname are treated correctly in the handler code.
            //                
            factory.setNamespaceAware(false);
            SAXParser saxParser = factory.newSAXParser();            
            DefaultHandler handler = new TreeParser(pathList);
            reader = new FileReader(xmlFile);
            InputSource source = new InputSource(reader);
            saxParser.parse(source, handler);
            result = true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return result;
    }
    
    // Parser internal state
    private final Node root;
    private Node rover;
    
    // For skipping node blocks
    private String skipping;
    private int depth;
    private NodeReader childNodeReader;

    
    private TreeParser(List<Path> pathList) {
        root = buildTree(pathList);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(childNodeReader != null) {
            childNodeReader.readCData(skipping, ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
        if(skipping != null) {
            depth++;
            if(childNodeReader != null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Skip: reading {0}", qname);
                childNodeReader.readChildren(qname, attributes);
            }
            if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: descend, depth is {0}, qn is {1}", new Object[]{depth, qname});
        } else {
            Node child = rover.findChild(qname);
            if(child != null) {
                rover = child;
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover descend to {0}", rover);
                
                NodeReader reader = rover.getReader();
                if(reader != null) {
                    if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover enter & read node {0}", qname);
                    reader.readAttributes(qname, attributes);
                }
            } else {
                skipping = qname;
                depth = 1;
                childNodeReader = rover.getReader();
                if(childNodeReader != null) {
                    if(isFinerLoggable) LOGGER.log(Level.FINER, "Skip: reading {0}", qname);
                    childNodeReader.readChildren(qname, attributes);
                }
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: start, depth is {0}, qn is {1}", new Object[]{depth, qname});
            }
        }
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if(skipping != null) {
            if(--depth == 0) {
                if(!skipping.equals(qname)) {
                    LOGGER.log(Level.WARNING, "Skip: {0} does not match {1} at depth {2}", new Object[]{skipping, qname, depth});
                }
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: ascend, depth is {0}", depth);
                skipping = null;
                childNodeReader = null;
            } else {
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: ascend, depth is {0}", depth);
            }
        } else {
            NodeReader reader = rover.getReader();
            if(reader != null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover exit & read node {0}", qname);
                reader.endNode(qname);
            }
            rover = rover.getParent();
            if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover ascend to {0}", rover);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        rover = root;
        skipping = null;
        depth = 0;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        LOGGER.log(Level.INFO, "Requested Entity: public id = {0}, system id = {1}", new Object[]{publicId, systemId});

        // We only expect a few entries here so use linear search directly.  If
        // this changes, considering caching using HashMap<String, String>
        //
        InputSource source = null;
        FileObject folder = FileUtil.getConfigFile("DTDs/Payara");
        if(folder != null) {
            for(FileObject fo: folder.getChildren()) {
                Object attr;
                if((attr = fo.getAttribute("publicId")) instanceof String && attr.equals(publicId)) {
                    source = new InputSource(fo.getInputStream());
                    break;
                } else if((attr = fo.getAttribute("systemId")) instanceof String && attr.equals(systemId)) {
                    source = new InputSource(fo.getInputStream());
                    break;
                }
            }
        }

        return source;
    }

    public static abstract class NodeReader {

        public void readAttributes(String qname, Attributes attributes) throws SAXException {
        }

        public void readChildren(String qname, Attributes attributes) throws SAXException {
        }
        
        public void readCData(String qname, char [] ch, int start, int length) throws SAXException {
        }
        
        public void endNode(String qname) throws SAXException {
        }

    }
    
    public static class Path {
        
        private final String path;
        private final NodeReader reader;
        
        public Path(String path) {
            this(path, null);
        }
        
        public Path(String path, NodeReader reader) {
            this.path = path;
            this.reader = reader;
        }
        
        public String getPath() {
            return path;
        }
        
        public NodeReader getReader() {
            return reader;
        }
        
        @Override
        public String toString() {
            return path;
        }
        
    }

    private static Node buildTree(List<Path> paths) {
        Node root = null;
        for(Path path: paths) {
            String [] parts = path.getPath().split("/");
            if(parts == null || parts.length == 0) {
                LOGGER.log(Level.WARNING, "Invalid entry, no parts, skipping: {0}", path);
                continue;
            }
            if(parts[0] == null) {
                LOGGER.log(Level.WARNING, "Invalid entry, null root, skipping: {0}", path);
                continue;
            }
            if(root == null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Root node created: {0}", parts[0]);
                root = new Node(parts[0]);
            }
            Node rover = root;
            for(int i = 1; i < parts.length; i++) {
                if(parts[i] != null && parts[i].length() > 0) {
                    Node existing = rover.findChild(parts[i]);
                    if(existing != null) {
                        if(isFinerLoggable) LOGGER.log(Level.FINER, "Existing node {0} at level {1}", new Object[]{parts[i], i});
                        rover = existing;
                    } else {
                        if(isFinerLoggable) LOGGER.log(Level.FINER, "Adding node {0} at level {1}", new Object[]{parts[i], i});
                        rover = rover.addChild(parts[i]);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Broken parts found in {0} at level {1}", new Object[]{path, i});
                }
            }
            if(rover != null) {
                rover.setReader(path.getReader());
            }
        }
        return root;
    }
    
    private static class Node implements Comparable<Node> {
        
        private final String element;
        private final Map<String, Node> children;
        private Node parent;
        private NodeReader reader;
        
        public Node(String element) {
            this(element, null);
        }
        
        private Node(String element, Node parent) {
            this.element = element;
            this.children = new HashMap<String, Node>();
            this.parent = parent;
        }

        public Node addChild(String tag) {
            Node child = new Node(tag, this);
            children.put(tag, child);
            return child;
        }
        
        public Node findChild(String tag) {
            return children.get(tag);
        }
        
        public Node getParent() {
            return parent;
        }
        
        public NodeReader getReader() {
            return reader;
        }
        
        public void setReader(NodeReader reader) {
            this.reader = reader;
        }
        
        @Override
        public int compareTo(Node o) {
            return element.compareTo(o.element);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.element != other.element && 
                    (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.element != null ? this.element.hashCode() : 0);
            return hash;            
        }

        @Override
        public String toString() {
            boolean comma = false;
            StringBuilder buf = new StringBuilder(500);
            buf.append("{ ");
            if(element != null && element.length() > 0) {
                buf.append(element);
                comma = true;
            }
            if(parent == null) {
                if(comma) {
                    buf.append(", ");
                }
                buf.append("root");
                comma = true;
            }
            if(children.size() > 0) {
                if(comma) {
                    buf.append(", ");
                }
                buf.append(children.size());
                buf.append(" sub(s)");
            }
            buf.append(" }");
            return buf.toString();
        }
        
    }
}
