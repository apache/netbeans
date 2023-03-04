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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.parsing.AbstractSourceFileObject.Handle;
import org.netbeans.modules.parsing.api.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Tomas Zezula
 */
@Trusted
public class SourceFileObject extends AbstractSourceFileObject implements DocumentProvider {
    
    private static final Logger LOG = Logger.getLogger(SourceFileObject.class.getName());

    private final boolean hasFilter;

    @CheckForNull
    public static SourceFileObject create (
            @NonNull final FileObject file,
            @NonNull final FileObject root) {
        try {
            return new SourceFileObject (
                new Handle(file, root),
                null,
                null,
                false);
        } catch (IOException ioe) {
            if (LOG.isLoggable(Level.SEVERE))
                LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
            return null;
        }
    }    

    public SourceFileObject (
        @NonNull final Handle handle,
        @NullAllowed final JavaFileFilterImplementation filter,
        @NullAllowed final CharSequence content,
        final boolean renderNow) throws IOException {
        super(handle, filter, content != null);
        this.hasFilter = filter != null;
        if (content != null || renderNow) {
            update(content);
        }
    }            
    
    @Override
    protected final Long isDirty() {
        final FileObject file = getHandle().resolveFileObject(false);
        if (file == null) {
            return null;
        }
        final DataObject.Registry regs = DataObject.getRegistry();
        final Set<DataObject> modified = regs.getModifiedSet();
        for (DataObject dobj : modified) {
            if (file.equals(dobj.getPrimaryFile())) {
                final EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    final Document doc = ec.getDocument();
                    if (doc != null) {
                        return DocumentUtilities.getDocumentTimestamp(doc);
                    }
                }
            }
        }
        return null;
    }

    @Override
    @CheckForNull
    protected final OutputStream createOutputStream() throws IOException {
        final FileObject file = getHandle().resolveFileObject(true);
        if (file == null) {
            throw new IOException("Cannot create file: " + toString());   //NOI18N
        }
        final StyledDocument doc = getDocument();
        if (doc != null) {
            return new DocumentStream (doc);
        }
        return null;
    }

    @Override
    @NonNull
    protected final CharSequence createContent() throws IOException {
        final FileObject file = getHandle().resolveFileObject(false);
        if (file == null) {
            throw new FileNotFoundException("Cannot open file: " + toString());
        }
        final Source source = Source.create(file);
        if (source == null) {
            throw new IOException("No source for: " + FileUtil.getFileDisplayName(file));   //NOI18N
        }
        CharSequence content = source.createSnapshot().getText();
        if (hasFilter && source.getDocument(false) == null) {
            content = filter(content);
        }
        return content;
    }

    @Override
    public StyledDocument getDocument() {
        final FileObject file = getHandle().resolveFileObject(false);
        if (file == null) {
            return null;
        }
        final Source src = Source.create(file);
        if (src == null) {
            return null;
        }
        final Document doc = src.getDocument(false);
        return (doc instanceof StyledDocument) ?  ((StyledDocument)doc) : null;
    }

    @Override
    public void runAtomic(final Runnable r) {
        assert r != null;
        final StyledDocument doc = getDocument();
        if (doc == null) {
            throw new IllegalStateException ();
        }
        else {
            NbDocument.runAtomic(doc,r);
        }
    }

    private class DocumentStream extends OutputStream {

        private static final int BUF_SIZ=2048;

        private final StyledDocument doc;
        private byte[] data;
        private int pos;

        public DocumentStream (final StyledDocument doc) {
            assert doc != null;
            this.doc = doc;
            this.data = new byte[BUF_SIZ];
            this.pos = 0;
        }

        public synchronized @Override void write(byte[] b, int off, int len) throws IOException {
            ensureSize (len);
            System.arraycopy(b,off,this.data,this.pos,len);
            this.pos+=len;
        }

        public synchronized @Override void write(byte[] b) throws IOException {
            ensureSize (b.length);
            System.arraycopy(b,0,this.data,this.pos,b.length);
            this.pos+=b.length;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            ensureSize (1);
            this.data[this.pos++]=(byte)(b&0xff);
        }

        private void ensureSize (int delta) {
            int requiredLength = this.pos + delta;
            if (this.data.length<requiredLength) {
                int newSize = this.data.length + BUF_SIZ;
                while (newSize<requiredLength) {
                    newSize+=BUF_SIZ;
                }
                byte[] newData = new byte[newSize];
                System.arraycopy(this.data,0,newData,0,this.pos);
                this.data = newData;
            }
        }

        public synchronized @Override void close() throws IOException {
            try {
                NbDocument.runAtomic(this.doc,
                    new Runnable () {
                        @Override
                        public void run () {
                            try {
                                doc.remove(0,doc.getLength());
                                doc.insertString(0,new String(
                                    data,
                                    0,
                                    pos,
                                    FileEncodingQuery.getEncoding(getHandle().resolveFileObject(false))),
                                null);
                            } catch (BadLocationException e) {
                                if (LOG.isLoggable(Level.SEVERE))
                                    LOG.log(Level.SEVERE, e.getMessage(), e);
                            }
                        }
                    });
            } finally {
                resetCaches();
            }
        }
    }
}
