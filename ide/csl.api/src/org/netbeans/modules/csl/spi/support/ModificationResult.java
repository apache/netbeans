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

package org.netbeans.modules.csl.spi.support;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.Parameters;

/**
 * Class that collects changes built during a modification task run.
 *
 * @author Dusan Balek
 */
public final class ModificationResult implements org.netbeans.modules.refactoring.spi.ModificationResult {

    private boolean committed;
    private final Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();
    
    private static final Comparator<Difference> COMPARATOR = new Comparator<Difference>() {
        public int compare(Difference d1, Difference d2) {
            return d1.getStartPosition().getOffset() - d2.getStartPosition().getOffset();
        }
    };

    /** Creates a new instance of ModificationResult */
    public ModificationResult() {
    }

    // API of the class --------------------------------------------------------

    public void addDifferences(FileObject fo, List<Difference> differences) {
        List<Difference> fileDiffs = diffs.get(fo);
        if (fileDiffs == null) {
            fileDiffs = new ArrayList<Difference>();
            diffs.put(fo, fileDiffs);
        }
        fileDiffs.addAll(differences);

        // Sort the diffs, if applicable
        if (fileDiffs.size() > 0) {
            fileDiffs.sort(COMPARATOR);
        }
    }

    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }
    
    public List<? extends Difference> getDifferences(FileObject fo) {
        return diffs.get(fo);
    }
    
    public Set<File> getNewFiles() {
        Set<File> newFiles = new HashSet<File>();
        for (List<Difference> ds:diffs.values()) {
            for (Difference d: ds) {
                if (d.getKind() == Difference.Kind.CREATE) {
                    newFiles.add(((CreateFileDifference) d).getFile());
                }
            }
        }
        return newFiles;
    }
    
    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        if (this.committed) {
            throw new IllegalStateException ("Calling commit on already committed Modificationesult."); //NOI18N
        }
        try {
            for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
                commit(me.getKey(), me.getValue(), null);
            }
        } finally {
            this.committed = true;
        }
    }
            
    private void commit (final FileObject fo, final List<Difference> differences, final Writer out) throws IOException {
        DataObject dObj = DataObject.find(fo);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        if (ec != null && out == null) {
            final StyledDocument doc = ec.getDocument();
            if (doc != null) {
                final IOException[] exceptions = new IOException [1];
                NbDocument.runAtomic(doc, new Runnable () {
                    public void run () {
                        try {
                            commit2 (doc, differences, out);
                        } catch (IOException ex) {
                            exceptions [0] = ex;
                        }
                    }
                });
                if (exceptions [0] != null)
                    throw exceptions [0];
                return;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;           
        Reader in = null;
        Writer out2 = out;
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
            boolean ownOutput = out != null;
            if (out2 == null) {
                out2 = new OutputStreamWriter(fo.getOutputStream(), encoding);
            }
            int offset = 0;                
            for (Difference diff : differences) {
                if (diff.isExcluded())
                    continue;
                if (Difference.Kind.CREATE == diff.getKind()) {
                    if (!ownOutput) {
                        createUnit((CreateFileDifference)diff, null);
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
            while ((n = in.read(buff)) > 0)
                out2.write(buff, 0, n);
        } finally {
            if (ins != null)
                ins.close();
            if (baos != null)
                baos.close();
            if (in != null)
                in.close();
            if (out2 != null)
                out2.close();
        }            
    }

    private void commit2 (final StyledDocument doc, final List<Difference> differences, Writer out) throws IOException {
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
                    createUnit((CreateFileDifference)diff, out);
                    break;
            }
        }
    }
    
    private void processDocument(final StyledDocument doc, final Difference diff) throws IOException {
        final BadLocationException[] blex = new BadLocationException[1];
        Runnable task = new Runnable() {

            public void run() {
                try {
                    processDocumentLocked(doc, diff);
                } catch (BadLocationException ex) {
                    blex[0] = ex;
                }
            }
        };
        if (diff.isCommitToGuards()) {
            NbDocument.runAtomic(doc, task);
        } else {
            try {
                NbDocument.runAtomicAsUser(doc, task);
            } catch (BadLocationException ex) {
                blex[0] = ex;
            }
        }
        if (blex[0] != null) {
            IOException ioe = new IOException();
            ioe.initCause(blex[0]);
            throw ioe;
        }
    }
    
    private void processDocumentLocked(Document doc, Difference diff) throws BadLocationException {
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
    }

    private void createUnit(CreateFileDifference diff, Writer out) {
        Writer w = out;
        try {
            if (w == null) {
                w = new FileWriter(diff.getFile());
            }
            w.append(diff.getNewText());
        } catch (IOException e) {
            Logger.getLogger(ModificationResult.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    Logger.getLogger(ModificationResult.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
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
     * @throws  IllegalArgumentException if the provided {@link FileObject} is not
     *                                   modified in this {@link ModificationResult}
     */
    public String getResultingSource(FileObject fileObject) throws IOException, IllegalArgumentException {
        Parameters.notNull("fileObject", fileObject);

        if (!getModifiedFileObjects().contains(fileObject)) {
            throw new IllegalArgumentException("File: " + FileUtil.getFileDisplayName(fileObject) + " is not modified in this ModificationResult");
        }
        
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);
        
        return writer.toString();
    }

    public static class Difference {
        Kind kind;
        PositionRef startPos;
        PositionRef endPos;
        String oldText;
        String newText;
        String description;
        private boolean excluded;
        private boolean ignoreGuards = false;

        public Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
        }
        
        public Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText) {
            this(kind, startPos, endPos, oldText, newText, null);
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

        /**
         * Gets flag if it is possible to write to guarded sections.
         * @return {@code true} in case the difference may be written even into
         *          guarded sections.
         * @see #guards(boolean)
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
        
        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE,
            CREATE;
        }
    } // End of Difference class
    
    public static class CreateFileDifference extends Difference {
        private final File file;
        
        public CreateFileDifference(File file, String text) {
            super(Kind.CREATE, null, null, null, text, "Create file " + file.getPath());
            this.file = file;
        }

        public final File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return kind + "Create File: " + file.getName() + "; contents = \"\n" + newText + "\"";
        }
    } // End of CreateFileDifference class
}
