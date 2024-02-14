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

package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Stamps;
import org.netbeans.core.startup.preferences.RelPaths;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.BaseUtilities;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.io.NbObjectInputStream;

/**
 * Read-only and fixed FileSystem over a binary file.
 * Its content can't be changed in any way, it can only be replaced by a new
 * instance over a different binary file.
 *
 * @author Petr Nejedly
 */
final class BinaryFS extends FileSystem implements DataInput {
    static final Logger LOG = Logger.getLogger(BinaryFS.class.getName());
    /* Format:
     *     MAGIC
     *  4B length of the full image
     *  4B offset to the start of the FS data (beyond modifications list)
     *     Modifications list
     *     FS data
     */

    static final byte[] MAGIC = "org.netbeans.core.projects.cache.BinaryV6".getBytes(); // NOI18N

    /** An empty array of FileObjects. */
    static final FileObject[] NO_CHILDREN = new FileObject[0];

    String binaryFile;

    // a template-buffer. Should not be operated directly, only duplicated.
    ByteBuffer content;

    private FileObject root;
    /** list of URLs or time of their modification */
    private final List<String> urls;
    private final List<Long> modifications;
    private final Date lastModified = new Date(Stamps.getModulesJARs().lastModified());
    private final Map<Integer,String> texts;
    private final ByteBuffer strings;

    @SuppressWarnings("deprecation")
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    /** Creates a new instance of BinaryFS */
    public BinaryFS(String binaryFile, ByteBuffer buff) throws IOException {
        try {
            _setSystemName("BinaryFS" + binaryFile.replace('/', '-').replace(File.separatorChar, '-')); // NOI18N
        } catch (PropertyVetoException ex) {
            throw new IOException(ex);
        }
        
        LayerCacheManager.err.log(Level.FINE, "Reading {0} buffer: {1}", new Object[]{binaryFile, buff.limit()});
        
        this.binaryFile = binaryFile;

        // verify the magic in header and expected image length
        byte[] magic = new byte[MAGIC.length];
        buff.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IOException("Bad magic header: " + new String(magic)); // NOI18N
        }
        long storedLen = buff.getInt();
        if (buff.limit() != storedLen) {
            throw new IOException("Corrupted image, correct length=" + storedLen + ", found=" + buff.limit()); // NOI18N
        }
        LayerCacheManager.err.log(Level.FINER, "Stored Len OK: {0}", storedLen);

        int strLen = buff.getInt();
        this.texts = new HashMap<Integer, String>();
        this.strings = buff.slice().asReadOnlyBuffer();
        buff.position(buff.position() + strLen);

        // fill the modifications array
        int stop = buff.getInt() + 8 + MAGIC.length + strLen + 4;
        urls = new ArrayList<String>();
        List<Long> _modifications = new ArrayList<Long>();
        while (buff.position() < stop) {
            urls.add(toAbsoluteURL(getString(buff)));
            _modifications.add(null);
        }
        modifications = Collections.synchronizedList(_modifications);


        // prepare the content buffer and root
        content = buff.slice().order(ByteOrder.LITTLE_ENDIAN);
        LayerCacheManager.err.log(Level.FINER, "Reading root");
        root = new BFSFolder("", null, 0);
        LayerCacheManager.err.log(Level.FINER, "Root ready: {0}", root);
    }

    /** Finds a file given its full resource path.
     * @param name the resource path, e.g. "dir/subdir/file.ext" or "dir/subdir" or "dir"
     * @return a file object with the given path or
     *   <CODE>null</CODE> if no such file exists
     *
     */
    @Override
    public FileObject findResource(String name) {
        if (name.length () == 0) {
            return root;
        }

        StringTokenizer st = new StringTokenizer(name, "/");
        FileObject fo = root;
        while (fo != null && st.hasMoreTokens()) {
            String next = st.nextToken();
            fo = fo.getFileObject(next, null); // XXX ??
        }

        return fo;
    }

    /** Provides a name for the system that can be presented to the user.
     * <P>
     * This call should <STRONG>never</STRONG> be used to attempt to identify the file root
     * of the filesystem. On some systems it may happen to look the same but this is a
     * coincidence and may well change in the future. Either check whether
     * you are working with a {@link org.openide.filesystems.LocalFileSystem} or similar implementation and use
     * {@link org.openide.filesystems.LocalFileSystem#getRootDirectory}; or better, try
     * {@link org.openide.filesystems.FileUtil#toFile} which is designed to do this correctly.
     *
     * @return user presentable name of the filesystem
     *
     */
    public String getDisplayName() {
        return "BinaryFS[" + binaryFile + "]";
    }

    /** Getter for root folder in the filesystem.
     *
     * @return root folder of whole filesystem
     *
     */
    public FileObject getRoot() {
//        System.err.println("returning root: " + root);
        return root;
    }

    /** Test if the filesystem is read-only or not.
     * @return true if the system is read-only
     *
     */
    public boolean isReadOnly() {
        return true;
    }

    final String getString(final ByteBuffer buffer) throws IOException {
        synchronized (strings) {
            int offset = buffer.getInt();
            String t = texts.get(offset);
            if (t != null) {
                return t;
            }
            strings.position(offset);
            String s = DataInputStream.readUTF(this);
            texts.put(offset, s);
            return s;
        }
    }
    private static String toAbsoluteURL(String relURL) {
        if (relURL.startsWith("home@")) {
            return toJarURI(System.getProperty("netbeans.home")) + relURL.substring(5);
        }
        if (relURL.startsWith("user@")) {
            return toJarURI(System.getProperty("netbeans.user")) + relURL.substring(5);
        }
        if (relURL.startsWith("abs@")) {
            return "jar:file:" + relURL.substring(4);
        }
        int indx = relURL.indexOf('@');
        if (indx == -1) {
            return relURL;
        }
        int cluster = Integer.parseInt(relURL.substring(0, indx));
        return toJarURI(RelPaths.cluster(cluster)) + relURL.substring(indx + 1);
    }

    private static String toJarURI(String path) {
        class DirFile extends File {
            public DirFile(String pathname) {
                super(pathname);
            }

            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public File getAbsoluteFile() {
                return this;
            }
        }
        String ret = "jar:" + BaseUtilities.toURI(new DirFile(path)).toString(); // NOI18N
        assert !ret.contains("//") : ret;
        return ret;
    }
    
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        Thread.holdsLock(strings);
        strings.get(b, off, len);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        Thread.holdsLock(strings);
        int ch1 = strings.get();
        if (ch1 < 0) {
            ch1 += 256;
        }
        int ch2 = strings.get();
        if (ch2 < 0) {
            ch2 += 256;
        }
        return (ch1 << 8) + ch2;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        throw new IOException();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        throw new IOException();
    }

    @Override
    public boolean readBoolean() throws IOException {
        throw new IOException();
    }

    @Override
    public byte readByte() throws IOException {
        throw new IOException();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        throw new IOException();
    }

    @Override
    public short readShort() throws IOException {
        throw new IOException();
    }


    @Override
    public char readChar() throws IOException {
        throw new IOException();
    }

    @Override
    public int readInt() throws IOException {
        throw new IOException();
    }

    @Override
    public long readLong() throws IOException {
        throw new IOException();
    }

    @Override
    public float readFloat() throws IOException {
        throw new IOException();
    }

    @Override
    public double readDouble() throws IOException {
        throw new IOException();
    }

    @Override
    public String readLine() throws IOException {
        throw new IOException();
    }

    @Override
    public String readUTF() throws IOException {
        throw new IOException();
    }


    /** Base class for read-only FileObject, both file and folder
     * Handles common operations and throws exception for any modification
     * attempt.
     * Also handles attribute reading/parsing.
     * The layout in the bfs file is (beginning from the offset):
     *  <pre>
     *   int attrCount
     *   Attribute[attrCount] attrs
     *   int fileCount/filesize
     *   content
     * </pre>
     */
    private abstract class BFSBase extends FileObject { // PENDING make static to save references
        // fields from the constructor
        private final FileObject parent;
        protected final String name;
        protected final int offset;

        // fetched fields
        private boolean initialized = false;
        private Map<String, AttrImpl> attrs = Collections.emptyMap();

        public BFSBase(String name, FileObject parent, int offset) {
            this.name = name;
            this.parent = parent;
            this.offset = offset;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof BFSBase)) return false;
            if (o == this) return true;
            BFSBase f = (BFSBase)o;
            return f.getPath().equals(getPath()) && specificEquals(f) && attributeEquals(f);
        }

        private final boolean attributeEquals(BFSBase base) {
            initialize();
            base.initialize();
            return attrs.equals(base.attrs);
        }

        @Override
        public final int hashCode() {
            return getPath().hashCode();
        }

        protected abstract boolean specificEquals(BFSBase f);

        /** no-op implementations of read-only, fixed, never firing FS */
        public void addFileChangeListener(FileChangeListener fcl) {}
        public void removeFileChangeListener(FileChangeListener fcl) {}
        @Deprecated
        public boolean isReadOnly() { return true; }
        @Deprecated
        public void setImportant(boolean b) {}

        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException(); // Read-only
        }
        public FileObject createFolder(String name) throws IOException {
            throw new IOException(); // Read-only
        }
        public void delete(FileLock lock) throws IOException {
            throw new IOException(); // Read-only
        }
        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException(); // Read-only
        }
        public FileLock lock() throws IOException {
            throw new IOException(); // Read-only
        }
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new IOException(); // Read-only
        }
        public void setAttribute(String attrName, Object value) throws IOException {
            throw new IOException(); // Read-only
        }


        // simple common implementations:

        /** Get the filesystem containing this file. */
        public FileSystem getFileSystem() {
            return BinaryFS.this;
        }

        /** Get the parent folder. */
        public FileObject getParent() {
            return parent;
        }

        /** Test whether this object is the root folder. */
        public boolean isRoot() {
            return parent == null;
        }

        public boolean isValid() {
            return true;
        }

        public java.util.Date lastModified() {
            return lastModified;
        }

        /** Get the name without extension of this file or folder. */
        public String getName () {
            int i = name.lastIndexOf ('.');
            return i <= 0 ? name : name.substring (0, i);
        }

        /** Get the extension of this file or folder. */
        public String getExt () {
            int i = name.lastIndexOf ('.') + 1;
            return i <= 1 || i == name.length () ? "" : name.substring (i); // NOI18N
        }

        /** Getter for name and extension of a file object. */
        @Override
        public String getNameExt () {
            return name;
        }

        // Attributes implementation:

        /** Get the file attribute with the specified name. */
        @Override
        public Object getAttribute(String attrName) {
            initialize();
            AttrImpl attr = attrs.get(attrName);
            if (attr == null && attrName.startsWith("class:")) { // NOI18N
                attr = attrs.get(attrName.substring(6));
                return attr == null ? null : attr.getType(this);
            }
            if (attr == null && attrName.startsWith("raw:")) { // NOI18N
                attr = attrs.get(attrName.substring(4));
                try {
                    if (attr != null && attr.index == 10) {
                        return attr.methodValue(attr.value, this, attrName).getMethod();
                    }
                    if (attr != null && attr.index == 11) {
                        return attr.getType(this);
                    }
                } catch (Exception ex) {
                }
                return null;
            }
            if (attr == null && attrName.equals("layers")) { // NOI18N
                return getLayersAttr();
            }

            return (attr != null) ? attr.getValue(this, attrName) : null;
        }
        
        public FileObject getFileObjectForAttr() {
            FileObject topFO = null;
            // XXX big hack! See #29356 for explanation for this nonsense.
            try {
                Class<?> mfoClass = Class.forName("org.openide.filesystems.MultiFileObject"); //NOI18N
                Field field = mfoClass.getDeclaredField("attrAskedFileObject"); //NOI18N
                field.setAccessible(true);

                ThreadLocal<?> attrAskedFileObject = ThreadLocal.class.cast(field.get(null));
                topFO = (FileObject)attrAskedFileObject.get();
                //attrAskedFileObject.set(null);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            return topFO == null ? this : topFO;
        }

        /** Get all file attribute names for this file. */
        public Enumeration<String> getAttributes() {
            initialize();
            if (attrs == null) {
                return Enumerations.empty();
            }
            return Collections.enumeration(attrs.keySet());
        }

        /**
         * A method for fetching/parsing the content of the file/folder
         * to the memory. Should be called before any access operation
         * to check the content of the entity.
         */
        protected final void initialize() {
            if (initialized) return;

            try {
                ByteBuffer sub = (ByteBuffer)content.duplicate().order(ByteOrder.LITTLE_ENDIAN).position(offset);
                if (!isRoot()) {
                    while (sub.getInt() >= 0) {
                        // skip URL list
                    }
                }

                int attrCount = sub.getInt();
                if (attrCount > 0) attrs = new HashMap<String, AttrImpl>(attrCount*4/3+1);

                for (int i=0; i< attrCount; i++) {  // do read attribute
                    // attribute names are highly duplicated!
                    String attrName = getString(sub);   // String attrName
                    byte type = sub.get();          // byte attrType
                    // values have high redundancy as well,
                    // like "true", "JSeparator", paths to orig files from
                    // shadow, ...
                    String value = getString(sub);  // String attrValue
                    attrs.put(attrName, new AttrImpl(type, value));
                }

                doInitialize(sub);
            } catch (Exception e) {
                System.err.println("exception in initialize() on " + name + ": " + e);
            }
            initialized = true;
        }

        /** A method called to finish the initialization in the subclasses.
         * When this method is called, the contentOffset field is already set up.
         */
        protected abstract void doInitialize(ByteBuffer sub) throws Exception;

        private Object getLayersAttr() {
            // NOI18N
            List<URL> ret = new ArrayList<URL>();
            if (isRoot()) {
                // all layers
                for (String u : urls) {
                    try {
                        ret.add(new URL(u));
                    } catch (MalformedURLException ex) {
                        LOG.warning("Cannot create URL: " + u); // NOI18N
                    }
                }
            } else {
                ByteBuffer sub = (ByteBuffer) content.duplicate().order(ByteOrder.LITTLE_ENDIAN).position(offset);
                boolean cont = true;
                while (cont) {
                    int index = sub.getInt();
                    if (index < 0) {
                        cont = false;
                    }
                    if (index <= -10) {
                        index = -(index + 10);
                    }
                    try {
                        ret.add(new URL(urls.get(index)));
                    } catch (MalformedURLException ex) {
                        LOG.warning("Cannot create URL: " + urls.get(index)); // NOI18N
                    }
                }
            }
            return ret.toArray(new URL[0]);
        }
    }

    static final class AttrImpl {
        private int index;
        private String value;

        AttrImpl(int type, String textValue) {
            index = type;
            value = textValue;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AttrImpl) {
                AttrImpl impl = (AttrImpl)o;
                return index == impl.index && value.equals(impl.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 2343 + index + value.hashCode();
        }

        public Object getValue( BFSBase foProvider, String attrName) {
            try {
                switch(index) {
                    case 0: // bytevalue
                        return Byte.valueOf(value);
                    case 1: // shortvalue
                        return Short.valueOf(value);
                    case 2: // intvalue
                        return Integer.valueOf(value);
                    case 3: // longvalue
                        return Long.valueOf(value);
                    case 4: // floatvalue
                        return Float.valueOf(value);
                    case 5: // doublevalue
                        return Double.valueOf(value);
                    case 6: // boolvalue
                        return Boolean.valueOf(value);
                    case 7: // charvalue
                        if (value.trim().length() != 1) break;
                        return value.charAt(0);
                    case 8: // stringvalue
                        return value;
                    case 9: // urlvalue
                        return new URL(value);
                    case 10: // methodvalue
                        return methodValue(value, foProvider, attrName).invoke();
                    case 11: // newvalue
                        Class<?> cls =  findClass (value);
                        // special support for singletons
                        if (SharedClassObject.class.isAssignableFrom(cls)) {
                            return SharedClassObject.findObject(cls.asSubclass(SharedClassObject.class), true);
                        } else {
                            Constructor<?> init = cls.getDeclaredConstructor();
                            init.setAccessible(true);
                            return init.newInstance((Object[]) null);
                        }

                    case 12: // serialvalue
                        return decodeValue(value);
                    case 13: // bundle value
                        String[] arr = value.split("#", 2); // NOI18N
                        return NbBundle.getBundle(arr[0]).getObject(arr[1]);
                    default:
                        throw new IllegalStateException("Bad index: " + index); // NOI18N
                }
            } catch (Exception exc) {
                LOG.log(notified.add(foProvider.getPath()) ? Level.INFO : Level.FINE, "value = " + value + " from " + foProvider.getPath(), exc); // NOI18N
            }
            return null; // problem getting the value...
        }
        private static final Set<String> notified = Collections.synchronizedSet(new HashSet<String>());

        public Class<?> getType( BFSBase foProvider) {
            try {
                switch(index) {
                    case 0: return Byte.class;
                    case 1: return Short.class;
                    case 2: return Integer.class;
                    case 3: return Long.class;
                    case 4: return Float.class;
                    case 5: return Double.class;
                    case 6: return Boolean.class;
                    case 7: return Character.class;
                    case 8: return String.class;
                    case 9: return URL.class;
                    case 10: // methodvalue
                        return methodValue(value, foProvider, null).getMethod().getReturnType();
                    case 11: // newvalue
                        return findClass (value);
                    case 12: // serialvalue
                        return null;
                    case 13: // bundle value
                        return String.class;
                    default:
                        throw new IllegalStateException("Bad index: " + index); // NOI18N
                }
            } catch (Exception exc) {
                Exceptions.attachMessage(exc, "value = " + value + " from " + foProvider.getPath()); //NOI18N
                LOG.log(notified.add(foProvider.getPath()) ? Level.INFO : Level.FINE, null, exc);
            }
            return null; // problem getting the value...
        }

        /** Used to store Method and its parameters. */
        private static class MethodAndParams {
            private Method method;
            private Object[] params;

            MethodAndParams(Method method, Object[] params) {
                this.method = method;
                this.params = params;
            }

            public Object invoke() throws Exception {
                method.setAccessible(true); //otherwise cannot invoke private
                return method.invoke(null, params);
            }
            
            public Method getMethod() {
                return method;
            }
        }

        /** Constructs new attribute as Object. Used for dynamic creation: methodvalue. 
         * @return MethodAndParams object or throws InstantiationException if method is not found
         */
        private MethodAndParams methodValue(String method, BFSBase foProvider, String attr) throws Exception {
            int i = method.lastIndexOf('.');
            if (i != -1) {
                // Cf. XMLMapAttr.Attr.methodValue:
                Class<?> cls = findClass(method.substring(0, i));
                String methodName = method.substring(i + 1);
                Class<?>[][] paramArray = {
                    {FileObject.class, String.class}, {String.class, FileObject.class},
                    {FileObject.class}, {String.class}, {},
                    {Map.class, String.class}, {Map.class},
                };
                for (Class<?>[] paramTypes : paramArray) {
                    Method m;
                    try {
                        m = cls.getDeclaredMethod(methodName, paramTypes);
                    } catch (NoSuchMethodException x) {
                        continue;
                    }
                    Object[] values = new Object[paramTypes.length];
                    for (int j = 0; j < paramTypes.length; j++) {
                        if (paramTypes[j] == FileObject.class) {
                            values[j] = foProvider.getFileObjectForAttr();
                        } else if (paramTypes[j] == String.class) {
                            values[j] = attr;
                        } else {
                            assert paramTypes[j] == Map.class;
                            values[j] = wrapToMap(foProvider.getFileObjectForAttr());
                        }
                    }
                    return new MethodAndParams(m, values);
                }
            }
            // Some message to logFile
            throw new InstantiationException(method);
        }

        private Object decodeValue(String value) throws Exception {
            if ((value == null) ||(value.length() == 0)) return null;

            byte[] bytes = new byte[value.length()/2];
            int count = 0;
            for (int i = 0; i < value.length(); i += 2) {
                // way from effective!!!!!
                int tempI = Integer.parseInt(value.substring(i,i+2),16);
                if (tempI > 127) tempI -=256; // ?? necessary ??
                bytes[count++] = (byte) tempI;
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);
            return new NbObjectInputStream(bis).readObject();
        }

        /** Loads a class of given name
         * @param name name of the class
         * @return the class
         * @exception ClassNotFoundException if class was not found
         */
        private Class<?> findClass (String name) throws ClassNotFoundException {
            ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);
            String tname = BaseUtilities.translate (name);
            try {
                if (c == null) {
                    return Class.forName (tname);
                } else {
                    return Class.forName (tname, true, c);
                }
            } catch (LinkageError err) {
                throw new ClassNotFoundException ("Cannot read " + name, err);
            }
        }
    }


    /** FileContent:
          int attrCount
          Attribute[attrCount] attrs
          int contentLength (or -1 for uri)
          byte[contentLength] content (or String uri)
     */
    final class BFSFile extends BFSBase {
        private int len;
        private int size = -1;
        private int contentOffset;
        private String uri;

        public BFSFile(String name, FileObject parent, int offset) {
            super(name, parent, offset);
        }


        /** Get all children of this folder (files and subfolders). */
        public FileObject[] getChildren() {
            return NO_CHILDREN;
        }

        /** Retrieve file or folder contained in this folder by name. */
        public FileObject getFileObject(String name, String ext) {
            return null; // File have no children
        }

        /** Test whether this object is a data object. */
        public boolean isData() {
            return true;
        }

        /** Test whether this object is a folder. */
        public boolean isFolder() {
            return false;
        }

        private byte[] data() throws IOException {
            if (len == -1) throw new IllegalArgumentException();
            byte[] data = new byte[len];
            ((ByteBuffer)content.duplicate().position(contentOffset)).get(data);
            return data;
        }

        /** Get input stream. */
        public InputStream getInputStream() throws FileNotFoundException {
            initialize();
            try {
                return len == -1 ?          // URI or not
                    new URL(uri).openConnection().getInputStream() : // len from URI
                    new ByteArrayInputStream(data());
            } catch (Exception e) {
                throw (FileNotFoundException) new FileNotFoundException("Cannot find '" + uri + "'").initCause(e);
            }
        }

        /** Get the size of the file. */
        public long getSize() {
            initialize();
            try {
                if (len == -1) { // URI or not
                    if (size == -1) { // #141110: cache it; otherwise FileObject.getSize() on SFS wastes resources
                        size = new URL(uri).openConnection().getContentLength(); // len from URI
                    }
                    return size;
                } else {
                    return len; // len from dump
                }
            } catch (Exception e) {
                System.err.println("exception in getSize() on " + name + ": " + e);
                return 0;
            }
        }
        @Override
        public Date lastModified() {
            initialize();
            try {
                int index = -1;
                java.net.URLConnection conn = null;
                if (len == -1) {
                    if (uri.startsWith("jar:")) { // NOI18N
                        return super.lastModified();
                    }
                    conn = new URL(uri).openConnection();
                } else {
                    ByteBuffer sub = (ByteBuffer) content.duplicate().order(ByteOrder.LITTLE_ENDIAN).position(offset);
                    index = sub.getInt();
                    if (index <= -10) {
                        index = -(index + 10);
                    }
                    final String url = urls.get(index);
                    if (url.startsWith("jar:")) { // NOI18N
                        return super.lastModified();
                    }
                    Long obj = modifications.get(index);
                    if (obj != null) {
                        return new Date(obj);
                    }
                    conn = new URL(url).openConnection();
                }

                /*
                if (conn instanceof java.net.JarURLConnection) {
                conn = ((java.net.JarURLConnection)conn).getJarFileURL ().openConnection ();
                }
                 */

                if (conn != null) {
                    long date = conn.getLastModified();
                    if (date > 0) {
                        if (index >= 0) {
                            modifications.set(index, date);
                        }
                        return new Date(date);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
            return super.lastModified();
        }

        /** A method called to finish the initialization in the subclasses.
         * When this method is called, the contentOffset field is already set up.
         *
         * the rest of the FileContent:
         * int contentLength (or -1)
         * byte[contentLength] content (or String uri);
         */
        protected void doInitialize(ByteBuffer sub) throws Exception {
            len = sub.getInt();
            contentOffset = sub.position();
            if (len == -1) {
                uri = toAbsoluteURL(getString(sub));
            } else {
                sub.position(contentOffset+len);
            }
        }

        // equals compares data: URI or byte contents.

        protected boolean specificEquals(BFSBase _f) {
            if (!(_f instanceof BFSFile)) return false;
            BFSFile f = (BFSFile)_f;
            initialize();
            f.initialize();
            if (len == -1 && f.len == -1) {
                return uri.equals(f.uri);
            } else if (len != -1 && f.len != -1) {
                byte[] data, fdata;
                try {
                    data = data();
                    fdata = f.data();
                } catch (IOException ioe) {
                    return false;
                }
                if (data.length != fdata.length) return false;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != fdata[i]) return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private class BFSFolder extends BFSBase {
        Map<String,BFSBase> childrenMap = Collections.<String,BFSBase>emptyMap();

        public BFSFolder(String name, FileObject parent, int offset) {
            super(name, parent, offset);
        }

        /** Test whether this object is a data object. */
        public boolean isData() {
            return false;
        }

        /** Test whether this object is a folder. */
        public boolean isFolder() {
            return true;
        }

        /** Get the size of the file. */
        public long getSize() {
            return 0; // Folder is zero
        }

        /** Get input stream. */
        public InputStream getInputStream() throws FileNotFoundException {
            throw new FileNotFoundException("Is a directory: " + this);
        }


        /** Get all children of this folder (files and subfolders). */
        public FileObject[] getChildren() {
            initialize();
                return childrenMap.values().toArray(NO_CHILDREN);
        }

        /** Retrieve file or folder contained in this folder by name. */
        @Override
        public FileObject getFileObject(String name, String ext) {
            if ("..".equals(name) && ext == null) { // NOI18N
                return getParent();
            }
            initialize();
            String fullName = ext == null ? name : name + "." + ext; // XXX - ??
            return childrenMap.get(fullName);
        }

        /** A method called to finish the initialization in the subclasses.
         * When this method is called, the contentOffset field is already set up.
         *
         * the rest of the FolderContent:
         *   int fileCount
         * File[fileCount] references
         * File/FolderContent[fileCount] contents
         */
        protected void doInitialize(ByteBuffer sub) throws Exception {
            int files = sub.getInt();
            if (files > 0) {
                HashMap<String, BFSBase> map = new HashMap<String, BFSBase>(files * 4 / 3 + 1);
                for (int i=0; i<files; i++) { //read file desc:
                    String nm = getString(sub);   // String name
                    byte isFolder = sub.get();      // boolean isFolder
                    int off = sub.getInt();          // int contentRef
                    map.put(nm, isFolder == 0 ?
                        new BFSFile(nm, this, off) :
                        new BFSFolder(nm, this, off));
                }
                childrenMap = map;
                if (LayerCacheManager.err.isLoggable(Level.FINEST)) {
                    LayerCacheManager.err.log(Level.FINEST, "  children for " + getPath() + " are: " + childrenMap.keySet());
                }
            }
        }

        // equals compares contents recursively.

        protected boolean specificEquals(BFSBase _f) {
            if (!(_f instanceof BFSFolder)) return false;
            BFSFolder f = (BFSFolder)_f;
            initialize();
            return childrenMap.equals(f.childrenMap);
        }

    }

    static final Map wrapToMap(FileObject fo) {
        return fo == null ? Collections.emptyMap() : new FileMap(fo);
    }


    private static final class FileMap extends AbstractMap<String,Object> {
        private FileObject fo;

        private FileMap(FileObject fo) {
            this.fo = fo;
        }

        public Set<Map.Entry<String,Object>> entrySet() {
            return new AttrFileSet(fo);
        }

        @Override
        public Object get(Object key) {
            if (key == FileObject.class) {
                return fo;
            }
            return key instanceof String ? fo.getAttribute((String)key) : null;
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Map)) {
                return false;
            }
            Object o = ((Map)obj).get(FileObject.class);
            if (o != null) {
                return fo.equals(o);
            }
            return obj.equals(this);
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.fo != null ? this.fo.hashCode() : 0);
            return hash;
        }
        @Override
        public String toString() {
            return "BinaryFileMap@" + Integer.toHexString(System.identityHashCode(this)) + "{" + "fo=" + fo + '}'; // NOI18N
        }
    } // end of FileMap
    private static final class AttrFileSet extends AbstractSet<Map.Entry<String,Object>> {
        private FileObject fo;

        private AttrFileSet(FileObject fo) {
            this.fo = fo;
        }

        public Iterator<Map.Entry<String, Object>> iterator() {
            class Iter implements Iterator<Map.Entry<String, Object>> {
                Enumeration<String> attrs = fo.getAttributes();

                public boolean hasNext() {
                    return attrs.hasMoreElements();
                }

                public Map.Entry<String, Object> next() {
                    String s = attrs.nextElement();
                    return new FOEntry(fo, s);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            }
            return new Iter();
        }

        public int size() {
            Enumeration<String> all = fo.getAttributes();
            int cnt = 0;
            while (all.hasMoreElements()) {
                cnt++;
                all.nextElement();
            }
            return cnt;
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    } // end of AttrFileSet

    private static final class FOEntry implements Map.Entry<String, Object> {
        private FileObject fo;
        private String attr;

        private FOEntry(FileObject fo, String attr) {
            this.fo = fo;
            this.attr = attr;
        }

        public String getKey() {
            return attr;
        }

        public Object getValue() {
            return fo.getAttribute(attr);
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    } // end of FOEntry
}
