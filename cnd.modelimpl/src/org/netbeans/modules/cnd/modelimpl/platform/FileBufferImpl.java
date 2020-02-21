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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferSnapshot;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * buffer 
 */
public class FileBufferImpl implements FileBuffer, PropertyChangeListener {
    // -J-Dorg.netbeans.modules.cnd.modelimpl.platform.FileBufferImpl.level=FINE

    private static final Logger LOG = Logger.getLogger(FileBufferImpl.class.getName());
    
    private final CharSequence absPath;
    private final FileSystem fileSystem;    
    private Reference<Document> docRef = new WeakReference<>(null);
    private final FileImpl fileImpl;
    private SoftReference<FileBufferSnapshot> snapRef = new SoftReference<>(null);
    private final APTFileBuffer.BufferType bufType;
    
    FileBufferImpl(FileObject fileObject, FileImpl fileImpl) {
        this(getFileSystem(fileObject), CndFileUtils.normalizePath(fileObject), fileImpl);
        attachListeners(fileObject);
    }

    FileBufferImpl(FileSystem fileSystem, CharSequence absPath, FileImpl fileImpl) {
        this.absPath = FilePathCache.getManager().getString(absPath);
        this.fileSystem = fileSystem;
        this.fileImpl = fileImpl;
        this.bufType = (fileImpl.getFileType() == FileImpl.FileType.HEADER_FILE) ? APTFileBuffer.BufferType.INCLUDED : APTFileBuffer.BufferType.START_FILE;
    }

    private String getEncoding() {
        FileObject fo = getFileObject();
        Charset cs = null;
        if (fo != null && fo.isValid()) {
            cs = FileEncodingQuery.getEncoding(fo);
        }
        if (cs == null) {
            cs = FileEncodingQuery.getDefaultEncoding();
        }
        return cs.name();
    }
    
    @Override
    public FileObject getFileObject() {
        FileObject result = CndFileUtils.toFileObject(fileSystem, absPath);
        if (result == null) {
            CndUtils.assertTrueInConsole(false, "can not find file object for " + absPath); //NOI18N
            result = InvalidFileObjectSupport.getInvalidFileObject(fileSystem, absPath);
        }
        return result;
    }
    
    private DataObject getDataObject() {
        return getDataObjectImpl(getFileObject());
    }

    @Override
    public APTFileBuffer.BufferType getType() {
        return bufType;
    }
    
    public DataObject getDataObjectImpl(FileObject fo) {
        DataObject dob = null;
        if (fo != null && fo.isValid()) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                //DataobjectNotFoundException may happen in case of deleting opened file
                //handled by returning null
            }
        }
        return dob;
    }    
    
    private static FileSystem getFileSystem(FileObject fileObject) {
        try {
            return fileObject.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return InvalidFileObjectSupport.getDummyFileSystem();
        }
    }
    
    private Document getDocument() {
        Document document = docRef.get();
        if (document != null) {
            return document;
        }
        DataObject dataObject = getDataObject();
        EditorCookie ec = dataObject == null ? null : dataObject.getLookup().lookup(EditorCookie.class);

        if (ec == null) {
            return null;
        }
        Document doc = ec.getDocument();
        docRef = new WeakReference<>(doc);
        return doc;
    }
    
    private void attachListeners(FileObject fo) {        
        if (fo.isValid() && fo.canWrite()) {
            DataObject dob = getDataObjectImpl(fo);
            if (dob != null) {
                Observable observable = dob.getLookup().lookup(EditorCookie.Observable.class);
                if (observable != null) {
                    observable.addPropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public String getText(int start, int end) throws IOException {
        return getSnapshot().getText(start, end);
    }

    @Override
    public CharSequence getText() throws IOException {
        return getSnapshot().getText();
    }

    @Override
    public int getLineCount() throws IOException {
        return getSnapshot().getLineColumnByOffset(Integer.MAX_VALUE)[0];
    }

    @Override
    public long lastModified() {
        Document doc = getDocument();
        if (doc != null) {
            final long documentTimestamp = org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentTimestamp(doc);
            return documentTimestamp;
        } else {
            return getFileObject().lastModified().getTime();
        }
    }

    @Override
    public long getCRC() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }    

    @Override
    public char[] getCharBuffer() throws IOException {
        return getSnapshot().getCharBuffer();
    }

    private FileBufferSnapshot getCharBufferDoc(final Document doc) throws IOException {
        final AtomicReference<BadLocationException> exc = new AtomicReference<>(null);
        final AtomicReference<FileBufferSnapshot> out = new AtomicReference<>(null);
        doc.render(new Runnable() {

            @Override
            public void run() {
                try {
                    final int length = doc.getLength();
                    char[] buf = new char[length];
                    org.netbeans.editor.DocumentUtilities.copyText(doc, 0, length, buf, 0);
                    long timeStamp = org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentTimestamp(doc);
                    out.set(new FileBufferSnapshot(fileSystem, absPath, bufType, buf, null, timeStamp));
                } catch (BadLocationException e) {
                    exc.set(e);
                }
            }
        });
        if (exc.get() != null) {
            throw toIOException(exc.get());
        }
        return out.get();
    }
    
    private IOException toIOException(BadLocationException e) {
        IOException ioe = new  IOException(e.getMessage());
        ioe.setStackTrace(e.getStackTrace());
        return ioe;
    }
    
    private FileBufferSnapshot getCharBufferFile(FileObject fileObject) throws IOException {
        final InputStream is = fileObject.getInputStream();
        assert is != null : "FileObject.getInputStream() returned null for FileObject: " + FileUtil.getFileDisplayName(fileObject); //NOI18N
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, getEncoding()));
            try {
                long timeStamp = fileObject.lastModified().getTime();
                StringBuilder output = new StringBuilder(Math.max(16, (int) fileObject.getSize()));
//                List<Integer> lso = new LinkedList<Integer>();
                boolean lastCharCR = false;
                char[] buffer = new char[1024];
                int size = -1;

                final char LF = '\n'; //NOI18N, Unicode line feed (0x000A)
                final char CR = '\r'; //NOI18N, Unicode carriage return (0x000D)
                final char LS = 0x2028; // Unicode line separator (0x2028)
                final char PS = 0x2029; // Unicode paragraph separator (0x2029)

//                lso.add(0);
                while (-1 != (size = reader.read(buffer, 0, buffer.length))) {
                    for (int i = 0; i < size; i++) {
                        char ch = buffer[i];
                        if (lastCharCR && ch == LF) { // found CRLF sequence
                            output.append(LF);
//                            lso.add(output.length());
                            lastCharCR = false;

                        } else { // not CRLF sequence
                            if (ch == CR) {
                                lastCharCR = true;
                            } else if (ch == LS || ch == PS) { // Unicode LS, PS
                                output.append(LF);
//                                lso.add(output.length());
                                lastCharCR = false;
                            } else { // current char not CR
                                lastCharCR = false;
                                output.append(ch);
                            }
                        }
                    }
                }

//                int[] lsoArr = new int[lso.size()];
//                int idx = 0;
//                for (Integer offset : lso) {
//                    lsoArr[idx++] = offset;
//                }

                char[] out = new char[output.length()];
                output.getChars(0, output.length(), out, 0);
                return new FileBufferSnapshot(fileSystem, absPath, bufType, out, null/*lsoArr*/, timeStamp);
            } finally {
                reader.close();
            }
        } finally {
            is.close();
        }
    }
    
    @Override
    public boolean isFileBased() {
        return getDocument() == null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LOG.log(Level.INFO, "{0}", evt);
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
            
        } else if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
            
        } else if (EditorCookie.Observable.PROP_MODIFIED.equals(evt.getPropertyName())) {
            
        }
    }
    
    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence getUrl() {
        return CndFileSystemProvider.toUrl(fileSystem, absPath);
    }

    @Override
    public int[] getLineColumnByOffset(int offset) throws IOException {
        return getSnapshot().getLineColumnByOffset(offset);
    }

    @Override
    public int getOffsetByLineColumn(int line, int column) throws IOException {
        return getSnapshot().getOffsetByLineColumn(line, column);
    }
    
    public FileBufferSnapshot getSnapshot() throws IOException {
        FileBufferSnapshot out = snapRef.get();
        if (out == null) {
            Document doc = getDocument();
            try {
                if (doc == null) {
                    // use file
                    out = getCharBufferFile(getFileObject());
                } else {
                    out = getCharBufferDoc(doc);
                }
            } catch (OutOfMemoryError oome) {
                // Use empty snapshot
                out = new FileBufferSnapshot(fileSystem, absPath, bufType, new char[0], new int[0], lastModified());

                // Diagnostics and workaround 
                LOG.log(Level.INFO, null, oome);

                if (doc != null) {
                    LOG.log(Level.WARNING, "Can''t create snapshot of {0}, size={1}, url={2}", new Object[]{doc, doc.getLength(), getUrl()}); //NOI18N
                } else {
                    LOG.log(Level.WARNING, "Can''t create snapshot of file based {0}", getUrl()); //NOI18N
                }
            }
            snapRef = new SoftReference<>(out);
        }
        return out;
    }
}
