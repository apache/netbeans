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

package org.netbeans.modules.diff;

import java.io.*;
import java.lang.reflect.Method;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileLock;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 * Factory of readers that are encoded according to best known approach how to
 * get the encoding information.
 * <p>
 * This factory should ideally be replaced by some public APIs. This uses just
 * heuristics combined with a lot of reflection calls to find things out.
 * This is intended to be only a temporary solution.
 * <p>
 * Use on your own risk.
 *
 * @author Martin Entlicher
 * @deprecated it is here only for compatibility with vcsgeneric modules, otherwise unused
 */
@Deprecated
public class EncodedReaderFactory {
    
    /** The FileObject attribute that defines the encoding of the FileObject content. */
    private static final String CHAR_SET_ATTRIBUTE = "Content-Encoding"; // NOI18N

    private static EncodedReaderFactory factory;
    
    /** Creates a new instance of EncodedReaderFactory */
    private EncodedReaderFactory() {
    }
    
    /** Get the default implementation. */
    public static synchronized EncodedReaderFactory getDefault() {
        if (factory == null) {
            factory = new EncodedReaderFactory();
        }
        return factory;
    }
    
    /**
     * Get the reader from file of given MIME type, it tries to find the best encoding itself.
     */
    public Reader getReader(File file, String mimeType) throws FileNotFoundException {
        return getReader(file, mimeType, getEncoding(file));
    }
    
    /**
     * Get the reader from file of given MIME type, suggest the encoding, if known.
     */
    public Reader getReader(File file, String mimeType, String encoding) throws FileNotFoundException {
        if (encoding != null) {
            try {
                return new InputStreamReader(new FileInputStream(file), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Reader r = null;
        String name = file.getName();
        int endingIndex = name.lastIndexOf('.');
        String ext = (endingIndex >= 0 && endingIndex < (name.length() - 1)) ? name.substring(endingIndex + 1) : "";
        if (!"java".equalsIgnoreCase(ext)) { // We read the encoding for Java files explicitely
            try {                            // If it's not defined, read with default encoding from stream (because of guarded blocks)
                file = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    r = getReaderFromEditorSupport(fo, fo);
                }
            } catch (IllegalArgumentException iaex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iaex);
            }
            if (r == null) {
                r = getReaderFromKit(file, null, mimeType);
            }
        }
        if (r == null) {
            // Fallback, use current encoding
            r = new InputStreamReader(new FileInputStream(file));
        }
        return r;
    }
    
    public Reader getReader(FileObject fo, String encoding) throws FileNotFoundException {
        return getReader(fo, encoding, fo.getExt());
    }
    
    public Reader getReader(FileObject fo, String encoding, String secondFileExt) throws FileNotFoundException {
        return getReader(fo, encoding, fo, secondFileExt);
    }
    
    public Reader getReader(FileObject fo, String encoding, FileObject type) throws FileNotFoundException {
        return getReader(fo, encoding, type, type.getExt());
    }
    
    private Reader getReader(FileObject fo, String encoding, FileObject type, String secondFileExt) throws FileNotFoundException {
        if (encoding != null) {
            try {
                return new InputStreamReader(fo.getInputStream(), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Reader r = null;
        String ext = type.getExt();
        if (!"java".equalsIgnoreCase(ext) || !ext.equals(secondFileExt)) {// We read the encoding for Java files explicitely
                                            // If it's not defined, read with default encoding from stream (because of guarded blocks)
                                            // But when the extensions of the two files are different (comparing Java files with something else),
                                            // we have to use the Document approach for both due to possible different line-endings.
            r = getReaderFromEditorSupport(fo, type);
            if (r == null) {
                r = getReaderFromKit(null, fo, type.getMIMEType());
            }
        }
        if (r == null) {
            // Fallback, use current encoding
            r = new InputStreamReader(fo.getInputStream());
        }
        return r;
    }
    
    /** @return The reader or <code>null</code>. */
    private Reader getReaderFromEditorSupport(FileObject fo, FileObject type) throws FileNotFoundException {
        //System.out.println("getReaderFromEditorSupport("+fo+")");
        DataObject dobj;
        try {
            dobj = DataObject.find(type);
        } catch (DataObjectNotFoundException donfex) {
            return null;
        }
        if (!type.equals(dobj.getPrimaryFile())) {
            return null;
        }
        EditCookie edit = (EditCookie) dobj.getCookie(EditCookie.class);
        CloneableEditorSupport editorSupport = null;
        if (edit instanceof CloneableEditorSupport) {
            editorSupport = (CloneableEditorSupport) edit;
        }
        //System.out.println("  editorSupport = "+editorSupport);
        if (editorSupport == null) {
            return null;
        }
        try {
            Method createKitMethod = getDeclaredMethod(editorSupport.getClass(), "createEditorKit", new Class[] {});
            createKitMethod.setAccessible(true);
            EditorKit kit = (EditorKit) createKitMethod.invoke(editorSupport, new Object[] {});
            //System.out.println("  KIT from cloneable editor support = "+kit);
            Method createStyledDocumentMethod = getDeclaredMethod(editorSupport.getClass(),
                    "createStyledDocument", new Class[] { EditorKit.class });
            createStyledDocumentMethod.setAccessible(true);
            StyledDocument doc = (StyledDocument) createStyledDocumentMethod.invoke(editorSupport, new Object[] { kit });
            Method loadFromStreamToKitMethod = getDeclaredMethod(editorSupport.getClass(),
                    "loadFromStreamToKit", new Class[] { StyledDocument.class, InputStream.class, EditorKit.class });
            loadFromStreamToKitMethod.setAccessible(true);
            InputStream in = fo.getInputStream();
            try {
                loadFromStreamToKitMethod.invoke(editorSupport, new Object[] { doc, in, kit });
            } finally {
                try { in.close(); } catch (IOException ioex) {}
            }
            String text = doc.getText(0, doc.getLength());
            doc = null; // Release it, we have the text
            return new StringReader(text);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /** @return The writer or <code>null</code>. */
    private Writer getWriterFromEditorSupport(final FileObject fo, FileLock lock) throws FileNotFoundException {
        //System.out.println("getWriterFromEditorSupport("+fo+")");
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException donfex) {
            return null;
        }
        if (!fo.equals(dobj.getPrimaryFile())) {
            return null;
        }
        EditCookie edit = (EditCookie) dobj.getCookie(EditCookie.class);
        final CloneableEditorSupport editorSupport;
        if (edit instanceof CloneableEditorSupport) {
            editorSupport = (CloneableEditorSupport) edit;
        } else {
            editorSupport = null;
        }
        //System.out.println("  editorSupport = "+editorSupport);
        if (editorSupport == null) {
            return null;
        }
        try {
            Method createKitMethod = getDeclaredMethod(editorSupport.getClass(), "createEditorKit", new Class[] {});
            createKitMethod.setAccessible(true);
            final EditorKit kit = (EditorKit) createKitMethod.invoke(editorSupport, new Object[] {});
            //System.out.println("  KIT from cloneable editor support = "+kit);
            Method createStyledDocumentMethod = getDeclaredMethod(editorSupport.getClass(),
                    "createStyledDocument", new Class[] { EditorKit.class });
            createStyledDocumentMethod.setAccessible(true);
            final StyledDocument doc = (StyledDocument) createStyledDocumentMethod.invoke(editorSupport, new Object[] { kit });
            final Method saveFromKitToStreamMethod = getDeclaredMethod(editorSupport.getClass(),
                    "saveFromKitToStream", new Class[] { StyledDocument.class, EditorKit.class, OutputStream.class });
            saveFromKitToStreamMethod.setAccessible(true);
            
            return new DocWriter(doc, fo, lock, null, kit, editorSupport, saveFromKitToStreamMethod);
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }
    
    private static Method getDeclaredMethod(Class<?> objClass, String name, Class[] args) throws NoSuchMethodException, SecurityException {
        try {
            return objClass.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException nsmex) {
            Class superClass = objClass.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethod(superClass, name, args);
            } else {
                throw nsmex;
            }
        }
    }
    
    /** @return The reader or <code>null</code>. */
    private Reader getReaderFromKit(File file, FileObject fo, String mimeType) throws FileNotFoundException {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        if (kit.getContentType().equalsIgnoreCase("text/plain") && "text/x-dtd".equalsIgnoreCase(mimeType)) {
             // Use XML kit for DTDs if not defined otherwise
            kit = CloneableEditorSupport.getEditorKit("text/xml");
        }
        //System.out.println("  KIT for "+mimeType+" = "+kit);
        if (kit != null) {
            Document doc = kit.createDefaultDocument();
            InputStream stream = null;
            try {
                if (file != null) {
                    stream = new FileInputStream(file);
                } else {
                    stream = fo.getInputStream();
                }
                kit.read(stream, doc, 0);
                String text = doc.getText(0, doc.getLength());
                //System.out.println("  TEXT = "+text);
                doc = null; // Release it, we have the text
                return new StringReader(text);
            } catch (IOException ioex) {
                FileNotFoundException fnfex;
                if (file != null) {
                    fnfex = new FileNotFoundException("Can not read file "+file.getAbsolutePath());
                } else {
                    fnfex = new FileNotFoundException("Can not read file "+fo);
                }
                fnfex.initCause(ioex);
                throw fnfex;
            } catch (BadLocationException blex) { // Something wrong???
                ErrorManager.getDefault().notify(blex);
            } finally {
                if (stream != null) {
                    try { stream.close(); } catch (IOException e) {}
                }
            }
        }
        return null;
    }
    
    /** @return The writer or <code>null</code>. */
    private Writer getWriterFromKit(File file, FileObject fo, FileLock lock, String mimeType) throws FileNotFoundException {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        if (kit.getContentType().equalsIgnoreCase("text/plain") && "text/x-dtd".equalsIgnoreCase(mimeType)) {
             // Use XML kit for DTDs if not defined otherwise
            kit = CloneableEditorSupport.getEditorKit("text/xml");
        }
        //System.out.println("  KIT for "+mimeType+" = "+kit);
        if (kit != null) {
            Document doc = kit.createDefaultDocument();
            return new DocWriter(doc, fo, lock, file, kit, null, null);
        }
        return null;
    }
    
    /**
     * Get the writer to file of given MIME type, it tries to find the best encoding itself.
     */
    public Writer getWriter(File file, String mimeType) throws FileNotFoundException {
        return getWriter(file, mimeType, getEncoding(file));
    }
    
    /**
     * Get the writer to file of given MIME type, suggest the encoding, if known.
     */
    public Writer getWriter(File file, String mimeType, String encoding) throws FileNotFoundException {
        if (encoding != null) {
            try {
                return new OutputStreamWriter(new FileOutputStream(file), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Writer w = null;
        String name = file.getName();
        int endingIndex = name.lastIndexOf('.');
        String ext = (endingIndex >= 0 && endingIndex < (name.length() - 1)) ? name.substring(endingIndex + 1) : "";
        if (!"java".equalsIgnoreCase(ext)) { // We read the encoding for Java files explicitely
            try {                            // If it's not defined, read with default encoding from stream (because of guarded blocks)
                file = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    FileLock lock;
                    try {
                        lock = fo.lock();
                    } catch (IOException ioex) {
                        FileNotFoundException fnfex = new FileNotFoundException(ioex.getLocalizedMessage());
                        fnfex.initCause(ioex);
                        throw fnfex;
                    }
                    w = getWriterFromEditorSupport(fo, lock);
                }
            } catch (IllegalArgumentException iaex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iaex);
            }
            if (w == null) {
                w = getWriterFromKit(file, null, null, mimeType);
            }
        }
        if (w == null) {
            // Fallback, use current encoding
            w = new OutputStreamWriter(new FileOutputStream(file));
        }
        return w;
    }
    
    /**
     * Get the writer to file, suggest the encoding, if known.
     */
    public Writer getWriter(FileObject fo, FileLock lock, String encoding) throws IOException {
        if (lock == null) {
            lock = fo.lock();
        }
        if (encoding != null) {
            try {
                return new OutputStreamWriter(fo.getOutputStream(lock), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Writer w = null;
        String ext = fo.getExt();
        if (!"java".equalsIgnoreCase(ext)) { // We read the encoding for Java files explicitely
                                             // If it's not defined, read with default encoding from stream (because of guarded blocks)
            w = getWriterFromEditorSupport(fo, lock);
            if (w == null) {
                w = getWriterFromKit(null, fo, lock, fo.getMIMEType());
            }
        }
        if (w == null) {
            // Fallback, use current encoding
            w = new OutputStreamWriter(fo.getOutputStream(lock));
        }
        return w;
    }

    /** Uses heuritisc to detect file encoding or null. */
    public String getEncoding(File file) {
        return getEncoding(FileUtil.toFileObject(FileUtil.normalizeFile(file)));
    }

    public static String decodeName(FileObject fo) {
        String ret = fo.getNameExt();
        if (fo.getParent() != null && fo.getParent().getPath().endsWith("CVS" + File.separator + "RevisionCache")) { // NOI18N
            String name = fo.getName();
            int hashOffset = name.lastIndexOf("#");  // NOI18N
            if (hashOffset != 1) {
                ret = name.substring(0, hashOffset);
            }
        }
        return ret;
    }

    public String getEncoding(FileObject fo) {
        String name = decodeName(fo).toLowerCase();

        if (name.endsWith(".properties")) {
            return findPropertiesEncoding();
        }
        if (name.endsWith(".form")) {
            return "utf8";
        }

        Object encoding = null;
        if (fo != null) {
            if (name.endsWith(".java")) {
                encoding = findJavaEncoding(fo); // is not in cache
            }
            if (encoding == null) {
                encoding = fo.getAttribute(CHAR_SET_ATTRIBUTE);  // XXX is not in cache
            }
        }

        if (name.endsWith(".xml") || name.endsWith(".dtd") || name.endsWith(".xsd") || name.endsWith(".xsl")) {  // NOI18N
            InputStream in = null;
            try {
                in = new BufferedInputStream(fo.getInputStream(), 2048);
                encoding = XMLEncodingHelper.detectEncoding(in);
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Can not detect encoding for: " + fo.getPath());  // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (encoding != null) {
            return encoding.toString();
        } else {
            return null;
        }
    }
    
    private static String findJavaEncoding(FileObject fo) {
        ClassLoader systemClassLoader =
                (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
        Method org_netbeans_modules_java_Util_getFileEncoding = null;
        try {
            Class<?> c = systemClassLoader.
                    loadClass("org.netbeans.modules.java.Util"); // NOI18N
            org_netbeans_modules_java_Util_getFileEncoding =
                c.getMethod("getFileEncoding", new Class[] {FileObject.class});
        } catch (Exception e) {
            // Ignore
        }
        if (org_netbeans_modules_java_Util_getFileEncoding != null) {
            try {
                String encoding = (String) org_netbeans_modules_java_Util_getFileEncoding.
                    invoke(null, new Object[] {fo});
                return encoding;
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
    private static String findPropertiesEncoding() {
        return "ISO-8859-1"; // NOI18N
    }
    
    private static class DocWriter extends Writer {

        private Document doc;
        private FileObject fo;
        private FileLock foLock;
        private File file;
        private EditorKit kit;
        private CloneableEditorSupport editorSupport;
        private Method saveFromKitToStreamMethod;
        private boolean closed;

        public DocWriter(Document doc, FileObject fo, FileLock foLock, File file,
                         EditorKit kit, CloneableEditorSupport editorSupport,
                         Method saveFromKitToStreamMethod) {
            this.doc = doc;
            this.fo = fo;
            this.foLock = foLock;
            this.file = file;
            this.kit = kit;
            this.editorSupport = editorSupport;
            this.saveFromKitToStreamMethod = saveFromKitToStreamMethod;
        }

        /** Write a single character. */
        public void write(int c) throws IOException {
            try {
                doc.insertString(doc.getLength(), Character.toString((char) c), null);
            } catch (BadLocationException blex) {
                IOException ioex = new IOException(blex.getLocalizedMessage());
                ioex.initCause(blex);
                throw ioex;
            }
        }

        /**
         * Write a portion of an array of characters.
         *
         * @param  cbuf  Array of characters
         * @param  off   Offset from which to start writing characters
         * @param  len   Number of characters to write
         *
         * @exception  IOException  If an I/O error occurs
         */
        public void write(char cbuf[], int off, int len) throws IOException {
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            try {
                doc.insertString(doc.getLength(), new String(cbuf, off, len), null);
            } catch (BadLocationException blex) {
                IOException ioex = new IOException(blex.getLocalizedMessage());
                ioex.initCause(blex);
                throw ioex;
            }
        }

        /**
         * Write a string.
         */
        public void write(String str) throws IOException {
            try {
                doc.insertString(doc.getLength(), str, null);
            } catch (BadLocationException blex) {
                IOException ioex = new IOException(blex.getLocalizedMessage());
                ioex.initCause(blex);
                throw ioex;
            }
        }

        public void flush() throws IOException {}

        /**
         * Close the stream, flushing it first.  Once a stream has been closed,
         * further write() or flush() invocations will cause an IOException to be
         * thrown.  Closing a previously-closed stream, however, has no effect.
         *
         * @exception  IOException  If an I/O error occurs
         */
        public void close() throws IOException {
            if (closed) return ;
            if (saveFromKitToStreamMethod != null) {
                OutputStream out = fo.getOutputStream(foLock);
                try {
                    saveFromKitToStreamMethod.invoke(editorSupport, new Object[] { doc, kit, out });
                } catch (Exception e) {
                    IOException ioex = new IOException(e.getLocalizedMessage());
                    ioex.initCause(e);
                    throw ioex;
                } finally {
                    try { out.close(); } catch (IOException ioex) {}
                    foLock.releaseLock();
                }
            } else {
                OutputStream out;
                if (file != null) {
                    out = new FileOutputStream(file);
                } else {
                    out = fo.getOutputStream(foLock);
                }
                try {
                    kit.write(out, doc, 0, doc.getLength());
                } catch (BadLocationException blex) {
                    IOException ioex = new IOException(blex.getLocalizedMessage());
                    ioex.initCause(blex);
                    throw ioex;
                } finally {
                    out.close();
                }
            }
            closed = true;
        }
    }
}
