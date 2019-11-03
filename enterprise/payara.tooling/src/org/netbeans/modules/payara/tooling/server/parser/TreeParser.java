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
package org.netbeans.modules.payara.tooling.server.parser;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser that invokes a user defined node reader(s) on a list of xpath
 * designated nodes.
 * <p/>
 * @author Peter Williams
 */
public final class TreeParser extends DefaultHandler {

    /** XML elements path items separator. */
    static final String PATH_SEPARATOR = "/";

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////
    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(TreeParser.class);

    private static final boolean isFinestLoggable = LOGGER.isLoggable(
            Level.FINEST);

    private static final boolean isFinerLoggable = LOGGER.
            isLoggable(Level.FINER);

    /**
     * Stops SAX parser from accessing remote DTDs or schemas.
     */
    private static final EntityResolver DUMMY_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String string, String string1) throws
                SAXException,
                IOException {
            return new InputSource(new StringReader(""));
        }
    };

    public static boolean readXml(File xmlFile, XMLReader... pathList) {
        return readXml(xmlFile, null, pathList);
    }

    public static boolean readXml(File xmlFile, Charset charset, XMLReader... pathList) {
        final String METHOD = "readXml";
        try {
            // !PW FIXME what to do about entity resolvers?  Timed out when
            // looking up doctype for sun-resources.xml earlier today (Jul 10)
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // !PW If namespace-aware is enabled, make sure localpart and
            // qname are treated correctly in the handler code.
            //                
            factory.setNamespaceAware(false);
            SAXParser saxParser = factory.newSAXParser();
            org.xml.sax.XMLReader reader = saxParser.getXMLReader();

            reader.setEntityResolver(DUMMY_RESOLVER);
            DefaultHandler handler = new TreeParser(pathList);
            reader.setContentHandler(handler);

            if (charset == null) {
                InputStream is = new BufferedInputStream(new FileInputStream(xmlFile));
                try {
                    reader.parse(new InputSource(is));
                    return true;
                } finally {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, METHOD, "cantClose", ex);
                    }
                }
            } else {
                Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(xmlFile)), charset);
                try {
                    reader.parse(new InputSource(r));
                    return true;
                } finally {
                    try {
                        r.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, METHOD, "cantClose", ex);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return false;
    }

    public static boolean readXml(URL xmlFile, XMLReader... pathList)
            throws IllegalStateException {
        final String METHOD = "readXml";
        boolean result = false;
        InputStream is = null;
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
            is = new BufferedInputStream(xmlFile.openStream());
            saxParser.parse(new InputSource(is), handler);
            result = true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, METHOD, "cantClose", ex);
                }
            }
        }
        return result;
    }

    // Parser internal state
    private final TreeParser.Node root;

    private TreeParser.Node rover;

    // For skipping node blocks
    private String skipping;

    private int depth;

    private TreeParser.NodeListener childNodeReader;

    private TreeParser(XMLReader[] readers) {
        ArrayList<Path> pathList = new ArrayList<>();
        for (XMLReader r : readers) {
            pathList.addAll(r.getPathsToListen());
        }
        root = buildTree(pathList);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (childNodeReader != null) {
            childNodeReader.readCData(skipping, ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localname, String qname,
            Attributes attributes) throws SAXException {
        final String METHOD = "startElement";
        if (skipping != null) {
            depth++;
            if (childNodeReader != null) {
                if (isFinerLoggable) {
                    LOGGER.log(Level.FINER, METHOD, "skipReading", qname);
                }
                childNodeReader.readChildren(qname, attributes);
            }
            if (isFinestLoggable) {
                LOGGER.log(Level.FINEST, METHOD,
                        "skipDescend", new Object[] {depth, qname});
            }
        } else {
            TreeParser.Node child = rover.findChild(qname);
            if (child != null) {
                rover = child;
                if (isFinerLoggable) {
                    LOGGER.log(Level.FINER, METHOD, "roverDescend", rover);
                }

                TreeParser.NodeListener reader = rover.getReader();
                if (reader != null) {
                    if (isFinerLoggable) {
                        LOGGER.log(Level.FINER, METHOD, "roverEnter", qname);
                    }
                    reader.readAttributes(qname, attributes);
                }
            } else {
                skipping = qname;
                depth = 1;
                childNodeReader = rover.getReader();
                if (childNodeReader != null) {
                    if (isFinerLoggable) {
                        LOGGER.log(Level.FINER, METHOD, "skipReading", qname);
                    }
                    childNodeReader.readChildren(qname, attributes);
                }
                if (isFinestLoggable) {
                    LOGGER.log(Level.FINEST, METHOD,
                            "skipStart", new Object[] {depth, qname});
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localname, String qname)
            throws SAXException {
        final String METHOD = "endElement";
        if (skipping != null) {
            if (childNodeReader != null) {
                childNodeReader.endNode(qname);
            }
            if (--depth == 0) {
                if (!skipping.equals(qname)) {
                    LOGGER.log(Level.WARNING, METHOD, "doesNotMatch",
                            new Object[] {skipping, qname, depth});
                }
                skipping = null;
                childNodeReader = null;
            }
            LOGGER.log(Level.FINER, METHOD, "skipAscend", depth);
        } else {
            TreeParser.NodeListener reader = rover.getReader();
            if (reader != null) {
                if (isFinerLoggable) {
                    LOGGER.log(Level.FINER, METHOD, "roverEnter", qname);
                }
                reader.endNode(qname);
            }
            rover = rover.getParent();
            if (isFinerLoggable) {
                LOGGER.log(Level.FINER, METHOD, "roverAscend", rover);
            }
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
    public InputSource resolveEntity(String string, String string1) throws
            IOException,
            SAXException {
        return super.resolveEntity(string, string1);
    }

    public static abstract class NodeListener {

        public void readAttributes(String qname, Attributes attributes) throws
                SAXException {
        }

        public void readChildren(String qname, Attributes attributes) throws
                SAXException {
        }

        public void readCData(String qname, char[] ch, int start, int length)
                throws SAXException {
        }

        public void endNode(String qname) throws SAXException {
        }
    }

    public static class Path {

        private final String path;

        private final TreeParser.NodeListener reader;

        public Path(String path) {
            this(path, null);
        }

        public Path(String path, TreeParser.NodeListener reader) {
            this.path = path;
            this.reader = reader;
        }

        public String getPath() {
            return path;
        }

        public TreeParser.NodeListener getReader() {
            return reader;
        }

        @Override
        public String toString() {
            return path;
        }
    }

    private static TreeParser.Node buildTree(List<TreeParser.Path> paths) {
        final String METHOD = "buildTree";
        TreeParser.Node root = null;
        for (TreeParser.Path path : paths) {
            String[] parts = path.getPath().split("/");
            if (parts == null || parts.length == 0) {
                LOGGER.log(Level.WARNING, METHOD, "invalidNoParts", path);
                continue;
            }
            if (parts[0] == null) {
                LOGGER.log(Level.WARNING, METHOD, "invalidNullRoot", path);
                continue;
            }
            if (root == null) {
                if (isFinerLoggable) {
                    LOGGER.log(Level.FINER, METHOD,
                            "createdRootNode", parts[0]);
                }
                root = new TreeParser.Node(parts[0]);
            }
            TreeParser.Node rover = root;
            for (int i = 1 ; i < parts.length ; i++) {
                if (parts[i] != null && parts[i].length() > 0) {
                    TreeParser.Node existing = rover.findChild(parts[i]);
                    if (existing != null) {
                        if (isFinerLoggable) {
                            LOGGER.log(Level.FINER, METHOD, "existing",
                                    new Object[] {parts[i],
                                        Integer.toString(i)});
                        }
                        rover = existing;
                    } else {
                        if (isFinerLoggable) {
                            LOGGER.log(Level.FINER, METHOD, "add",
                                    new Object[] {parts[i],
                                        Integer.toString(i)});
                        }
                        rover = rover.addChild(parts[i]);
                    }
                } else {
                    LOGGER.log(Level.WARNING, METHOD, "broken",
                            new Object[] {path, Integer.toString(i)});
                }
            }
            if (rover != null) {
                rover.setReader(path.getReader());
            }
        }
        return root;
    }

    private static class Node implements Comparable<TreeParser.Node> {

        private final String element;

        private final Map<String, TreeParser.Node> children;

        private TreeParser.Node parent;

        private TreeParser.NodeListener reader;

        public Node(String element) {
            this(element, null);
        }

        private Node(String element, TreeParser.Node parent) {
            this.element = element;
            this.children = new HashMap<>();
            this.parent = parent;
        }

        public TreeParser.Node addChild(String tag) {
            TreeParser.Node child = new TreeParser.Node(tag, this);
            children.put(tag, child);
            return child;
        }

        public TreeParser.Node findChild(String tag) {
            return children.get(tag);
        }

        public TreeParser.Node getParent() {
            return parent;
        }

        public TreeParser.NodeListener getReader() {
            return reader;
        }

        public void setReader(TreeParser.NodeListener reader) {
            this.reader = reader;
        }

        @Override
        public int compareTo(TreeParser.Node o) {
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
            final TreeParser.Node other = (TreeParser.Node)obj;
            if (this.element != other.element
                    && (this.element == null
                    || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.element != null
                    ? this.element.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            boolean comma = false;
            StringBuilder buf = new StringBuilder(500);
            buf.append("{ ");
            if (element != null && element.length() > 0) {
                buf.append(element);
                comma = true;
            }
            if (parent == null) {
                if (comma) {
                    buf.append(", ");
                }
                buf.append("root");
                comma = true;
            }
            if (children.size() > 0) {
                if (comma) {
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
