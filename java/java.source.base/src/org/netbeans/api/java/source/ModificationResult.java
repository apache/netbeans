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

package org.netbeans.api.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Log;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.ModificationResult.CreateChange;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Class that collects changes built during a modification task run.
 *
 * @author Dusan Balek
 */
public final class ModificationResult {

    private static final Logger LOG = Logger.getLogger(ModificationResult.class.getName());
    
    private boolean committed;
    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();
    Map<?, int[]> tag2Span = new IdentityHashMap<Object, int[]>();
    
    /** Creates a new instance of ModificationResult */
    ModificationResult() {
    }

    private final Throwable creator;
    
    {
        boolean keepStackTrace = false;
        assert keepStackTrace = true;
        creator = keepStackTrace ? new Throwable() : null;
    }
    
    // API of the class --------------------------------------------------------

    /**
     * Runs a task over given sources, the task has an access to the {@link WorkingCopy}
     * using the {@link WorkingCopy#get(org.netbeans.modules.parsing.spi.Parser.Result)} method.
     * @param sources on which the given task will be performed
     * @param task to be performed
     * @return the {@link ModificationResult}
     * @throws org.netbeans.modules.parsing.spi.ParseException
     * @since 0.42
     */
    public static @NonNull ModificationResult runModificationTask(final @NonNull Collection<Source> sources, final @NonNull UserTask task) throws ParseException {
        final ModificationResult result = new ModificationResult();
        final ElementOverlay overlay = ElementOverlay.getOrCreateOverlay();
        ParserManager.parse(sources, new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = JavacParser.MIME_TYPE.equals(resultIterator.getSnapshot().getMimeType()) ? resultIterator : findEmbeddedJava(resultIterator);
                if (resultIterator != null) {
                    Parser.Result parserResult = resultIterator.getParserResult();
                    final CompilationController cc = CompilationController.get(parserResult);
                    assert cc != null;
                    final WorkingCopy copy = new WorkingCopy (cc.impl, overlay);
                    assert WorkingCopy.instance == null;
                    WorkingCopy.instance = new WeakReference<WorkingCopy>(copy);
                    try {
                        task.run(resultIterator);
                    } finally {
                        WorkingCopy.instance = null;
                    }
                    final JavacTaskImpl jt = copy.impl.getJavacTask();
                    Log.instance(jt.getContext()).nerrors = 0;
                    final List<ModificationResult.Difference> diffs = copy.getChanges(result.tag2Span);
                    if (diffs != null && diffs.size() > 0)
                        result.diffs.put(copy.getFileObject(), diffs);
                }
            }
            private ResultIterator findEmbeddedJava(final ResultIterator theMess) throws ParseException {
                final Collection<Embedding> todo = new LinkedList<Embedding>();
                //BFS should perform better than DFS in this dark.
                for (Embedding embedding : theMess.getEmbeddings()) {
                    if (JavacParser.MIME_TYPE.equals(embedding.getMimeType()))
                        return theMess.getResultIterator(embedding);
                    else
                        todo.add(embedding);
                }
                for (Embedding embedding : todo) {
                    ResultIterator result = findEmbeddedJava(theMess.getResultIterator(embedding));
                    if (result != null)
                        return result;
                }
                return null;
            }
        });
        return result;
    }
    
    public @NonNull Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }
    
    public List<? extends Difference> getDifferences(@NonNull FileObject fo) {
        return diffs.get(fo);
    }
    
    public @NonNull Set<File> getNewFiles() {
        Set<File> newFiles = new HashSet<File>();
        for (List<Difference> ds:diffs.values()) {
            for (Difference d: ds) {
                if (d.getKind() == Difference.Kind.CREATE) {
                    newFiles.add(org.openide.util.BaseUtilities.toFile(((CreateChange) d).getFileObject().toUri()));
                }
            }
        }
        return newFiles;
    }
    
    static LinkedList<Throwable> lastCommitted = new LinkedList<Throwable>();

    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        if (this.committed) {
            throw new IllegalStateException ("Calling commit on already committed Modificationesult."); //NOI18N
        }
        try {
            IndexingController.getDefault().enterProtectedMode();
            try {
//                RepositoryUpdater.getDefault().lockRU();
                for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
                    commit(me.getKey(), me.getValue(), null);
                }
            } finally {
//                RepositoryUpdater.getDefault().unlockRU();
                Set<FileObject> alreadyRefreshed = new HashSet<>();
                try {
                    final SourceFileManager.ModifiedFiles modifiedFiles = SourceFileManager.getModifiedFiles();
                    for (FileObject srcFile : diffs.keySet()) {
                        Utilities.revalidate(srcFile);
                        alreadyRefreshed.add(srcFile);
                        modifiedFiles.fileModified(srcFile.toURI());
                    }
                } finally {
                    IndexingController.getDefault().exitProtectedMode(null);
                }
                ActiveDocumentProvider provider = Lookup.getDefault().lookup(ActiveDocumentProvider.class);
                if (provider != null) {
                    for (Document activeDocument : provider.getActiveDocuments()) {
                        FileObject fileObject = Utilities.getFileObject(activeDocument);
                        if (fileObject != null && !alreadyRefreshed.contains(fileObject)) {
                            Source source = Source.create(fileObject);
                            if (source != null) {
                                Utilities.revalidate(source);
                            }
                        }
                    }
                }
            }
            while (lastCommitted.size() > 10) {
                lastCommitted.removeLast();
            }
            lastCommitted.addFirst(creator);
        } finally {
            this.committed = true;
        }
    }
    
    static void commit (final FileObject fo, final List<Difference> differences, final Writer out) throws IOException {
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        Source source = Source.create(fo);
        if (source != null && out == null) {
            final Document doc = source.getDocument(false);
            if (doc != null) {
                final IOException[] exceptions = new IOException [1];
                LineDocumentUtils.asRequired(doc, AtomicLockDocument.class).runAtomic(new Runnable () {
                    public void run () {
                        try {
                            commit2 (doc, differences, out);
                        } catch (IOException ex) {
                            exceptions [0] = ex;
                        }
                    }
                });
                if (exceptions [0] != null) {
                    LOG.log(Level.INFO, "Cannot commit changes into " + fo, exceptions[0]);
                    int s = lastCommitted.size();
                    for (Throwable t : lastCommitted) {
                        LOG.log(Level.INFO, "Previous commit number " + s--, t);
                    }
                    throw exceptions [0];
                }
            return;
            }
        }
        Reader in = null;
        Writer out2 = out;
        JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(fo);
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fo);
            boolean[] hasReturnChar = new boolean[1];
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fo.asBytes()), encoding));
            if (filter != null) {
                in = filter.filterReader(in);
            }
            in = new FilteringReader(in, hasReturnChar);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            boolean ownOutput = out != null;
            if (out2 == null) {
                out2 = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(), encoding));
                //going through filter only when writing to disk. When creating source for getResultingSource,
                //it is not passed through the write-filter (as that is what refactoring preview expects).
                if (filter != null) {
                    out2 = filter.filterWriter(out2);
                }
                out2 = new FilteringWriter(out2, hasReturnChar);
            }
            int offset = 0;                
            for (Difference diff : differences) {
                if (diff.isExcluded())
                    continue;
                if (Difference.Kind.CREATE == diff.getKind()) {
                    if (!ownOutput) {
                        createUnit(diff, null);
                    }
                    continue;
                }
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int n;
                int rc = 0;
                while ((n = in.read(buff,0, toread - rc)) > 0 && rc < toread) {
                    out2.write(buff, 0, n);
                    rc+=n;
                    offset += n;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        out2.write(diff.getNewText());
                        break;
                    case REMOVE:
                        int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        break;
                    case CHANGE:
                        len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        out2.write(diff.getNewText());
                        break;
                }
            }                    
            char[] buff = new char[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                out2.write(buff, 0, n);
            }
        } finally {
            if (in != null)
                in.close();
            if (out2 != null)
                out2.close();
        }            
    }

    private static void commit2 (final Document doc, final List<Difference> differences, Writer out) throws IOException {
        for (Difference diff : differences) {
            if (diff.isExcluded())
                continue;
            switch (diff.getKind()) {
                case INSERT:
                case REMOVE:
                case CHANGE:
                    processDocument(doc, diff);
                    break;
                case CREATE:
                    createUnit(diff, out);
                    break;
            }
        }
    }
    
    private static void processDocument(final Document doc, final Difference diff) throws IOException {
        final BadLocationException[] blex = new BadLocationException[1];
        Runnable task = new Runnable() {

            public void run() {
                DocumentListener l = null;
                try {
                    // The listener was added while trying to fix #63323
                    // IMPORTANT: it has to be removed immediatelly after the change to stop marking the events
                    // as ignoring caret changes
                    l = new DocumentListener() {

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            DocumentUtilities.putEventPropertyIfSupported(e, "caretIgnore", Boolean.TRUE); // NOI18N
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            DocumentUtilities.putEventPropertyIfSupported(e, "caretIgnore", Boolean.TRUE); // NOI18N
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            DocumentUtilities.putEventPropertyIfSupported(e, "caretIgnore", Boolean.TRUE); // NOI18N
                        }
                    };
                    doc.addDocumentListener(l);
                    processDocumentLocked(doc, diff);
                } catch (BadLocationException ex) {
                    blex[0] = ex;
                } finally {
                    if (l != null) {
                        doc.removeDocumentListener(l);
                    }
                }
            }
        };
        AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
        assert ald != null : "Missing AtomicLockDocument stub";
        if (diff.isCommitToGuards()) {
            ald.runAtomic(task);
        } else {
            ald.runAtomicAsUser(task);
        }
        if (blex[0] != null) {
            IOException ioe = new IOException();
            ioe.initCause(blex[0]);
            throw ioe;
        }
    }
    
    private static void processDocumentLocked(Document doc, Difference diff) throws BadLocationException {
        switch (diff.getKind()) {
            case INSERT:
                doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                break;
            case REMOVE:
                doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                break;
            case CHANGE: {
                // first insert the new content, THEN remove the old one. In situations where the content AFTER the
                // change is not writable this ordering allows to replace the content, but if we first delete, 
                // replacement cannot be inserted into the nonwritable area.
                int offs = diff.getStartPosition().getOffset();
                int removeLen = diff.getEndPosition().getOffset() - offs;
                
                // [NETBEANS-4270] Can't use "delta = diff.getNewText().length()".
                // doc.insertString may filter chars, e.g. '\r', and change length.
                int initialLength = doc.getLength();
                doc.insertString(offs, diff.getNewText(), null);
                int delta = doc.getLength() - initialLength;
                doc.remove(delta + offs, removeLen);
                break;
            }
        }
    }

    private static void createUnit(Difference diff, Writer out) {
        CreateChange change = (CreateChange) diff;
        Writer w = out;
        try {
            if (w == null) {
                change.getFileObject().openOutputStream();
                w = change.getFileObject().openWriter();
            }
            w.append(change.getNewText());
        } catch (IOException e) {
            Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     * 
     * @param fileObject there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     * @throws  IllegalArgumentException if the provided {@link FileObject} is not
     *                                   modified in this {@link ModificationResult}
     */
    public @NonNull String getResultingSource(@NonNull FileObject fileObject) throws IOException, IllegalArgumentException {
        Parameters.notNull("fileObject", fileObject);

        if (!getModifiedFileObjects().contains(fileObject)) {
            throw new IllegalArgumentException("File: " + FileUtil.getFileDisplayName(fileObject) + " is not modified in this ModificationResult");
        }
        
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);
        
        return writer.toString();
    }

    /**
     * Provides span of tree tagged with {@code tag}
     * @param tag
     * @return borders in target document
     * @since 0.37
     */
    public @NullUnknown int[] getSpan(@NonNull Object tag) {
        return tag2Span.get(tag);
    }
    
    public static class Difference {
              Kind kind;
        final Position startPos;
        final Position endPos;
              String oldText;
              String newText;
        final String description;
        private boolean excluded;
        private boolean ignoreGuards = false;
        
        /**
         * The Lookup acquired from the Source
         */
        private final Lookup ctxLookup;
        /**
         * The FileObject backing the original Source. If the Source is not backed
         * by a file, the Source instance will be stored in {@link #theSource} field.
         */
        private final FileObject sourceFile;
        
        /**
         * The Source object, only in the case the Source is not backed by a FileObject.
         */
        private final Source theSource;

        Difference(Kind kind, Position startPos, Position endPos, String oldText, String newText, String description, Source theSource) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
            if (theSource != null) {
                this.ctxLookup = theSource.getLookup();
                assert ctxLookup != null;
                this.sourceFile = theSource.getFileObject();
                if (sourceFile == null) {
                    this.theSource = theSource;
                } else {
                    this.theSource = null;
                }
            } else {
                this.ctxLookup = null;
                this.sourceFile = null;
                this.theSource = null;
            }
            // conservatively assume that pos could be null. They shouldn't be, but no doc states so.
            assert startPos == null || endPos == null || (startPos.getOffset() <= endPos.getOffset());
        }
        
        Difference(Kind kind, Position startPos, Position endPos, String oldText, String newText, Source theSource) {
            this(kind, startPos, endPos, oldText, newText, null, theSource);
        }
        
        public @NonNull Kind getKind() {
            return kind;
        }
        
        public @NonNull Position getStartPosition() {
            return startPos;
        }
        
        public @NonNull Position getEndPosition() {
            return endPos;
        }
        
        public @NonNull String getOldText() {
            return oldText;
        }
        
        public @NonNull String getNewText() {
            return newText;
        }
        
        public boolean isExcluded() {
            return excluded;
        }
        
        public void exclude(boolean b) {
            excluded = b;
        }

        /**
         * Gets flag if it is possible to write to guarded sections.
         * @return {@code true} in case the difference may be written even into
         *          guarded sections.
         * @see #setCommitToGuards(boolean)
         * @since 0.33
         */
        public boolean isCommitToGuards() {
            return ignoreGuards;
        }
        
        /**
         * Sets flag if it is possible to write to guarded sections.
         * @param b flag if it is possible to write to guarded sections
         * @since 0.33
         */
        public void setCommitToGuards(boolean b) {
            ignoreGuards = b;
        }

        @Override
        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText;
        }
        public String getDescription() {
            return description;
        }
        
        /**
         * Opens a Document where the Difference is to be applied. Throws IOException,
         * if the document open fails. If the document is already opened, returns the opened
         * instance. If the difference is CREATE and the document does not exist yet,
         * the method may return {@code null}
         * 
         * @throws IOException when document open fails
         * @return Document to apply the Difference.
         * @since java.source.base 1.1
         */
        @CheckForNull
        public Document openDocument() throws IOException {
            if (ctxLookup == null) {
                return null;
            } else if (theSource != null) {
                return theSource.getDocument(true);
            }
            
            // obtain the source again:
            Source s = Source.create(sourceFile, ctxLookup);
            if (s == null) {
                return null;
            }
            return s.getDocument(true);
        }
        
        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE,
            CREATE;
        }
    }
    
    static class CreateChange extends Difference {
        JavaFileObject fileObject;
        
        CreateChange(JavaFileObject fileObject, String text) {
            super(Kind.CREATE, null, null, null, text, 
                    NbBundle.getMessage(ModificationResult.class, "TXT_CreateFile", fileObject.getName()), null);
            this.fileObject = fileObject;
        }

        public JavaFileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String toString() {
            return kind + "Create File: " + fileObject.getName() + "; contents = \"\n" + newText + "\"";
        }
    }
    
    private static final class FilteringReader extends Reader {
        
        private final Reader delegate;
        private final boolean[] hasReturnChar;
        private boolean beforeFirstLine = true;

        public FilteringReader(Reader delegate, boolean[] hasReturnChar) {
            this.delegate = delegate;
            this.hasReturnChar = hasReturnChar;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read;
            int j;
            
            do {
                read = delegate.read(cbuf, off, len);
                if (read == -1) {
                    return -1;
                }
                j = 0;

                for (int i = off; i < off + read; i++) {
                    if (cbuf[i] != '\r') {
                        cbuf[j++] = cbuf[i];
                        if (beforeFirstLine && cbuf[i] == '\n') {
                            beforeFirstLine = false;
                        }
                    } else if (beforeFirstLine) {
                        hasReturnChar[0] = true;
                        beforeFirstLine = false;
                    }
                }
            } while (j == 0 && read > 0);
            
            return j;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
    
    private static final class FilteringWriter extends Writer {
        private final boolean[] hasReturnChar;
        private final Writer delegate;

        public FilteringWriter(Writer delegate, boolean[] hasReturnChar) {
            this.hasReturnChar = hasReturnChar;
            this.delegate = delegate;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (hasReturnChar[0]) {
                char[] buf = new char[len * 2];
                int j = 0;
                
                for (int i = off; i < off + len; i++) {
                    if (cbuf[i] == '\n') {
                        buf[j++] = '\r';
                        buf[j++] = '\n';
                    } else {
                        buf[j++] = cbuf[i];
                    }
                }
                
                delegate.write(buf, 0, j);
            } else {
                delegate.write(cbuf, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
        
    }
}
