/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        super(handle, filter);
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
