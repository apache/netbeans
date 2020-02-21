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
package org.netbeans.modules.cnd.refactoring.support;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionRef;

/**
 * based on org.netbeans.api.java.source.ModificationResult
 *
 * Class that collects changes for one project
 *
 */
public final class ModificationResult implements org.netbeans.modules.refactoring.spi.ModificationResult {

    private final CsmProject project;
    Map<FileObject, Collection<Difference>> diffs = new HashMap<>();

    /** Creates a new instance of ModificationResult */
    public ModificationResult(CsmProject project) {
        this.project = project;
    }

    // API of the class --------------------------------------------------------
    public void addDifference(FileObject fo, Difference diff) {
        Collection<Difference> foDiffs = diffs.get(fo);
        if (foDiffs == null) {
            foDiffs = new LinkedHashSet<>();
            diffs.put(fo, foDiffs);
        }
        foDiffs.add(diff);
    }

    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }

    public Collection<? extends Difference> getDifferences(FileObject fo) {
        return diffs.get(fo);
    }

    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        Set<Document> changedDocument = new HashSet<>(diffs.size());
        try {
            // on commit we collect modified documents
            for (Map.Entry<FileObject, Collection<Difference>> me : diffs.entrySet()) {
                Document doc = commit(me.getKey(), me.getValue(), null);
                if (doc != null) {
                    changedDocument.add(doc);
                }
            }
        } finally {
            // to minimize editStart/editStop in ModelSupport we force save all
            // and only then clean marker
            LifecycleManager.getDefault().saveAll();
            for (Document doc : changedDocument) {
                // clear marker used by ModelSupport to prevent edit start activity
                doc.putProperty(CsmUtilities.CND_REFACTORING_MARKER, null);
            }
            if (this.project != null) {
                // need to reparse project
                this.project.waitParse();
            }
        }
    }

    private Document commit(final FileObject fo, final Collection<Difference> differences, Writer out) throws IOException {
        DataObject dObj = DataObject.find(fo);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        if (ec != null && out == null) {
            final Document doc = CsmUtilities.openDocument(ec);
            if (doc != null) {
                final IOException ioe[] = new IOException[]{null};
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        for (Difference diff : differences) {
                            if (diff.isExcluded()) {
                                continue;

                            }
                            try {
                                switch (diff.getKind()) {
                                    case INSERT:
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                    case REMOVE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        break;
                                    case CHANGE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                }
                            } catch (BadLocationException ex) {
                                IOException ioex = new IOException();
                                ioex.initCause(ex);
                                ioe[0] = ioex;
                            }
                        }
                    }
                };
                if (doc instanceof BaseDocument) {
                    // set marker used by ModelSupport to prevent edit start activity
                    doc.putProperty(CsmUtilities.CND_REFACTORING_MARKER, Boolean.TRUE); // NOI18N
                    ((BaseDocument) doc).runAtomic(runnable);
                } else {
                    runnable.run();
                }
                if (ioe[0] != null) {
                    throw ioe[0];
                }
                return doc;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;
        Reader in = null;
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fo);
            ins = fo.getInputStream();
            baos = new ByteArrayOutputStream();
            FileUtil.copy(ins, baos);

            ins.close();
            ins = null;
            byte[] arr = baos.toByteArray();
            int arrLength = convertToLF(arr);
            baos.close();
            baos = null;
            in = new InputStreamReader(new ByteArrayInputStream(arr, 0, arrLength), encoding);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            if (out == null) {
                out = new OutputStreamWriter(fo.getOutputStream(), encoding);
            }
            int offset = 0;
            for (Difference diff : differences) {
                if (diff.isExcluded()) {
                    continue;
                }
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int n;
                int rc = 0;
                while ((n = in.read(buff, 0, toread - rc)) > 0 && rc < toread) {
                    out.write(buff, 0, n);
                    rc += n;
                    offset += n;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        out.write(diff.getNewText());
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
                        out.write(diff.getNewText());
                        break;
                }
            }
            char[] buff = new char[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (baos != null) {
                baos.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return null;
    }

    private int convertToLF(byte[] buff) {
        int j = 0;
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] != '\r') {
                buff[j++] = buff[i];
            }
        }
        return j;
    }

    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     * 
     * @param   there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     */
    public String getResultingSource(FileObject fileObject) throws IOException {
        assert fileObject != null : "Provided fileObject is null";
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);

        return writer.toString();
    }

    public Set<File> getNewFiles() {
        Set<File> newFiles = new HashSet<>();
        for (Collection<Difference> ds : diffs.values()) {
            for (Difference d : ds) {
                if (d.getKind() == Difference.Kind.CREATE) {
                    //newFiles.add(new File(((CreateChange) d).getFileObject().toUri()));
                }
            }
        }
        return newFiles;
    }

    public static final class Difference {

        final Kind kind;
        final PositionRef startPos;
        final PositionRef endPos;
        final String oldText;
        final String newText;
        final String description;
        private boolean excluded;

        public Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
        }

        public Kind getKind() {
            return kind;
        }

        public PositionRef getStartPosition() {
            return startPos;
        }

        public PositionRef getEndPosition() {
            return endPos;
        }

        public String getOldText() {
            return oldText;
        }

        public String getNewText() {
            return newText;
        }

        public boolean isExcluded() {
            return excluded;
        }

        public void exclude(boolean b) {
            excluded = b;
        }

        @Override
        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText; // NOI18N
        }

        public String getDescription() {
            return description;
        }

        public static enum Kind {

            INSERT,
            REMOVE,
            CHANGE,
            CREATE
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Difference other = (Difference) obj;
            if (this.kind != other.kind || this.kind != Kind.CHANGE) {
                return false;
            }
            if (!equalPos(this.startPos, other.startPos)) {
                return false;
            }
            if (!equalPos(this.endPos, other.endPos)) {
                return false;
            }
            return true;
        }

        private boolean equalPos(PositionRef first, PositionRef second) {
            return first == second || (first != null && second != null && first.getOffset() == second.getOffset());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.kind.ordinal();
            hash = 79 * hash + (this.startPos != null ? this.startPos.getOffset() : 0);
            hash = 79 * hash + (this.endPos != null ? this.endPos.getOffset() : 0);
            return hash;
        }
        
    }
}
