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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/** A cache manager which parses the layers according to the Filesystems 1.x DTDs.
 * This class just handles the parsing during cache rewrite time; subclasses are
 * responsible for loading from and saving to the storage format.
 * @author Jesse Glick
 */
abstract class ParsingLayerCacheManager extends LayerCacheManager implements ContentHandler, ErrorHandler, EntityResolver {
    
    private static final String[] ATTR_TYPES = {
        "boolvalue",
        "bundlevalue",
        "bytevalue",
        "charvalue",
        "doublevalue",
        "floatvalue",
        "intvalue",
        "longvalue",
        "methodvalue",
        "newvalue",
        "serialvalue",
        "shortvalue",
        "stringvalue",
        "urlvalue",
    };
    
    private static final String DTD_1_0 = "-//NetBeans//DTD Filesystem 1.0//EN";
    private static final String DTD_1_1 = "-//NetBeans//DTD Filesystem 1.1//EN";
    private static final String DTD_1_2 = "-//NetBeans//DTD Filesystem 1.2//EN";
    
    private Locator locator;
    private MemFolder root;
    private Stack<Object> curr; // Stack<MemFileOrFolder | MemAttr>
    private URL base;
    private final StringBuilder buf = new StringBuilder();
    private URL ref;
    private int weight;
    private int fileCount, folderCount, attrCount;
    // Folders, files, and attrs already encountered in this layer.
    // By path; attrs as folder/file path plus "//" plus attr name.
    private Set<String> oneLayerFiles; // Set<String>
    // Related:
    private String currPath;
    private boolean atLeastOneFileOrFolderInLayer;

    /** Constructor for subclasses.
     */
    protected ParsingLayerCacheManager() {
    }
    
    /** Implements storage by parsing the layers and calling
     * store(FileSystem,ParsingLayerCacheManager.MemFolder).
     */
    @Override
    public final void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException {
        store(fs, createRoot(urls), os);
    }
    
    /**
     * Do the actual parsing.
     */
    private MemFolder createRoot(List<URL> urls) throws IOException {
        root = new MemFolder(null);
        curr = new Stack<Object>();
        curr.push(root);
        try {
            XMLReader r = XMLUtil.createXMLReader();
            // Speed enhancements.
            // XXX these are not really necessary; OK to run validation here!
            r.setFeature("http://xml.org/sax/features/validation", false);
            r.setFeature("http://xml.org/sax/features/namespaces", false);
            try {
                r.setFeature("http://xml.org/sax/features/string-interning", true);
            } catch (SAXException x) {
                Logger.getLogger(ParsingLayerCacheManager.class.getName()).log(Level.INFO,
                        "#127537: could not set string-interning feature on parser; are you using a nonstandard XML parser?",
                        x);
            }
            r.setContentHandler(this);
            r.setErrorHandler(this);
            r.setEntityResolver(this);
            Exception carrier = null;
            // #23609: reverse these...
            urls = new ArrayList<URL>(urls);
            Collections.reverse(urls);
            Iterator<URL> it = urls.iterator();
            while (it.hasNext()) {
                base = it.next(); // store base for resolving in parser
                oneLayerFiles = new HashSet<String>(100);
                currPath = null;
                LayerCacheManager.err.log(Level.FINE, "Parsing: {0}", base);
                atLeastOneFileOrFolderInLayer = false;
                try {
                    r.parse(base.toURI().toASCIIString());
                    if (!atLeastOneFileOrFolderInLayer && root.attrs == null) {
                        LayerCacheManager.err.log(Level.WARNING, "Inefficient to include an empty layer in a module: {0}", base);
                    }
                } catch (Exception e) {
                    curr.clear();
                    curr.push(root);
                    Exceptions.attachMessage(e, "While parsing " + base);
                    if (carrier == null) {
                        carrier = e;
                    } else {
                        Throwable t = carrier;
                        while (t.getCause() != null) {
                            t = t.getCause();
                        }
                        t.initCause(e);
                    }
                }
            }
            if (carrier != null) throw carrier;
            LayerCacheManager.err.fine("Finished layer parsing; " + fileCount + " files, " + folderCount + " folders, " + attrCount + " attributes");
            return root;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        } finally {
            fileCount = folderCount = attrCount = 0;
            base = null;
            locator = null;
            curr = null;
            root = null;
            oneLayerFiles = null;
            currPath = null;
        }
    }

    /** Delegated storage method supplied with a merged layer parse.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    protected abstract void store(FileSystem fs, MemFolder root, OutputStream os) throws IOException;
    
    /** If true, file content URLs should be opened and the contents extracted,
     * if they are of an appropriate type (locally stored). If false, the original
     * URLs should be left alone.
     */
    protected abstract boolean openURLs();
    
    public void startElement(String ns, String lname, String qname, Attributes attrs) throws SAXException {
        if (qname.equals("filesystem")) {
            return;
        } else if (qname.equals("folder")) {
            fileOrFolder(qname, attrs);
        } else if (qname.equals("file")) {
            MemFileOrFolder mfof = fileOrFolder(qname, attrs);
            if (!(mfof instanceof MemFile)) { 
                // a collision between modules
            } else {
                buf.setLength(0);
                ref = null;
                String u = attrs.getValue("url");
                if (u != null) {
                    try {
                        ref = new URL(base, u);
                    } catch (MalformedURLException mfue) {
                        throw (SAXException) new SAXException(mfue.toString()).initCause(mfue);
                    }
                }
                weight = 0;
            }
        } else if (qname.equals("attr")) {
            attrCount++;
            MemAttr attr = new MemAttr();
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                String attrName = attrs.getQName(i);
                if ("name".equals(attrName)) {
                    attr.name = attrs.getValue(i);
                }
                else {
                    int idx = Arrays.binarySearch(ATTR_TYPES, attrName);
                    if (idx >= 0) {
                        attr.type = ATTR_TYPES[idx];
                        attr.data = attrs.getValue(i);
                    }
                }
                if (attr.name != null && attr.data != null) {
                    break;
                }
            }
            if (/*MultiFileObject.WEIGHT_ATTRIBUTE*/"weight".equals(attr.name)) {
                if ("intvalue".equals(attr.type)) {
                    try {
                        weight = Integer.parseInt(attr.data);
                    } catch (NumberFormatException x) {
                        // ignore here (other places should report it)
                    }
                } else {
                    LayerCacheManager.err.log(Level.WARNING, "currently unsupported value type for weight attribute in {0}: {1}", new Object[] {base, attr.type});
                }
            }
//            System.out.println("found attr "+attr);
            /*
            attr.name = attrs.getValue("name");
            for (int i = 0; i < ATTR_TYPES.length; i++) {
                String v = attrs.getValue(ATTR_TYPES[i]);
                if (v != null) {
                    attr.type = ATTR_TYPES[i];
                    attr.data = v;
                    break;
                }
            }
             */
            if (attr.type == null) throw new SAXParseException("unknown <attr> value type for " + attr.name, locator);
            MemFileOrFolder parent = (MemFileOrFolder)curr.peek();
            if (parent.attrs == null) parent.attrs = new LinkedList<MemAttr>();
            Iterator<MemAttr> it = parent.attrs.iterator();
            while (it.hasNext()) {
                if (it.next().name.equals(attr.name)) {
                    attrCount--;
                    it.remove();
                }
            }
            parent.attrs.add(attr);
            if (!oneLayerFiles.add(currPath + "//" + attr.name)) { // NOI18N
                LayerCacheManager.err.warning("layer " + base + " contains duplicate attributes " + attr.name + " for " + currPath);
            }
        } else {
            throw new SAXException(qname);
        }
    }
    
    private MemFileOrFolder fileOrFolder(String qname, Attributes attrs) {
        atLeastOneFileOrFolderInLayer = true;
        String name = attrs.getValue("name");
        if (name == null) throw new NullPointerException("No name"); // NOI18N
        if (!(curr.peek() instanceof MemFolder)) throw new ClassCastException("Stack: " + curr); // NOI18N
        MemFolder parent = (MemFolder)curr.peek();
        MemFileOrFolder f = null;
        if (parent.children == null) {
            parent.children = new LinkedList<MemFileOrFolder>();
        }
        else {
            for (MemFileOrFolder f2 : parent.children) {
                if (f2.name.equals(name)) {
                    f = f2;
                    f.registerURL(base);
                    break;
                }
            }
        }
        if (f == null) {
            if (qname.equals("folder")) { // NOI18N
                f = new MemFolder(base);
                folderCount++;
            } else {
                f = new MemFile(base);
                fileCount++;
            }
            f.name = name;
            parent.children.add(f);
        }
        curr.push(f);
        if (currPath == null) {
            currPath = name;
        } else {
            currPath += "/" + name;
        }
        if (!oneLayerFiles.add(currPath)) {
            LayerCacheManager.err.warning("layer " + base + " contains duplicate " + qname + "s named " + currPath);
        }
        return f;
    }
    
    public void endElement(String ns, String lname, String qname) throws SAXException {
        Object poke;
        if (qname.equals("file") && (poke = curr.peek()) instanceof MemFile) {
            MemFile file = (MemFile) poke;
            if (weight /* #23609: reversed, so not > */>= file.weight) {
                file.weight = weight;
                file.contents = null;
                if (buf.length() > 0) {
                    String text = buf.toString().trim();
                    if (text.length() > 0) {
                        if (ref != null) {
                            throw new SAXParseException("CDATA plus url= in <file>", locator);
                        }
                        /* May be used legitimately by e.g. @HelpSetRegistration:
                        LayerCacheManager.err.warning("use of inline CDATA text contents in <file name=\"" + file.name + "\"> deprecated for performance and charset safety at " + locator.getSystemId() + ":" + locator.getLineNumber() + ". Please use the 'url' attribute instead, or the file attribute 'originalFile' on *.shadow files.");
                         */
                        // Note: platform default encoding used. If you care about the encoding,
                        // you had better be using url= instead.
                        file.contents = text.getBytes();
                    }
                }
                file.ref = ref;
                if (openURLs()) {
                    // Only open simple URLs. Assume that JARs are the same JARs with the layers.
                    if (file.ref != null && file.contents == null && file.ref.toExternalForm().startsWith("jar:file:")) { // NOI18N
                        try {
                            URLConnection conn = file.ref.openConnection();
                            conn.connect();
                            byte[] readBuf = new byte[conn.getContentLength()];
                            InputStream is = conn.getInputStream();
                            try {
                                int pos = 0;
                                while (pos < readBuf.length) {
                                    int read = is.read(readBuf, pos, readBuf.length - pos);
                                    if (read < 1) throw new IOException("Premature EOF on " + file.ref.toExternalForm()); // NOI18N
                                    pos += read;
                                }
                                if (is.read() != -1) throw new IOException("Delayed EOF on " + file.ref.toExternalForm()); // NOI18N
                            } finally {
                                is.close();
                            }
                            file.contents = readBuf;
                            file.ref = null;
                        } catch (IOException ioe) {
                            throw new SAXException(ioe);
                        }
                    }
                }
            }
        }
        if (qname.equals("file") || qname.equals("folder")) { // NOI18N
            curr.pop();
            int i = currPath.lastIndexOf('/'); // NOI18N
            if (i == -1) {
                currPath = null;
            } else {
                currPath = currPath.substring(0, i);
            }
        }
    }
    
    public void characters(char[] ch, int start, int len) throws SAXException {
        Object currF = curr.peek();
        if (!(currF instanceof MemFile)) {
            return;
        }
        buf.append(ch, start, len);
    }
    
    public void warning(SAXParseException e) throws SAXException {
        LayerCacheManager.err.log(Level.WARNING, null, e);
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
        if (pubid != null && (pubid.equals(DTD_1_0) || pubid.equals(DTD_1_1) || pubid.equals(DTD_1_2))) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        } else {
            return null;
        }
    }
    
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    public void endDocument() throws SAXException {
        if (curr.size() != 1) throw new SAXException("Wrong stack: " + curr); // NOI18N
    }
    
    public void startDocument() throws SAXException {}
    
    public void startPrefixMapping(String str, String str1) throws SAXException {}
    
    public void skippedEntity(String str) throws SAXException {}
    
    public void processingInstruction(String str, String str1) throws SAXException {}
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws SAXException {}
    
    public void endPrefixMapping(String str) throws SAXException {}

    /** Struct for <file> or <folder>.
     */
    protected abstract static class MemFileOrFolder {
        public String name;
        public List<MemAttr> attrs = null; // {null | List<MemAttr>}
        private Object base;
        
        public MemFileOrFolder (URL base) {
            this.base = base;
        }

        @SuppressWarnings("unchecked")
        final URL getBase() {
            Object o = base;
            if (o instanceof URL) {
                return (URL)o;
            } else {
                return (URL)((Iterable)o).iterator().next();
            }
        }

        @SuppressWarnings("unchecked")
        final List<URL> getURLs() {
            Object o = base;
            if (o instanceof URL) {
                return Collections.singletonList((URL)o);
            } else {
                return o != null ? (List<URL>)o : Collections.<URL>emptyList();
            }
        }

        @SuppressWarnings("unchecked")
        final void registerURL(URL url) {
            List<URL> urls;
            if (base instanceof URL) {
                URL u = (URL)base;
                base = urls = new ArrayList<URL>();
                urls.add(u);
            } else {
                urls = (List<URL>)base;
            }
            urls.add(url);
        }

    }
    
    /** Struct for <folder>.
     */
    protected static final class MemFolder extends MemFileOrFolder {
        public List<MemFileOrFolder> children = null;
        
        public MemFolder (URL base) {
            super (base);
        }
        
        @Override
        public String toString() {
            return "MemFolder[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for <file>.
     */
    protected static final class MemFile extends MemFileOrFolder {
        public byte[] contents = null; // {null | byte[]}
        public URL ref = null; // {null | URL}

        private int weight = Integer.MIN_VALUE;
        
        public MemFile (URL base) {
            super (base);
        }
        
        @Override
        public String toString() {
            return "MemFile[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for &lt;attr&gt;.
     */
    protected static final class MemAttr {
        public String name;
        public String type;
        public String data;
        @Override
        public String toString() {
            return "MemAttr[" + name + "," + type + "," + data + "]"; // NOI18N
        }
    }
    
}
