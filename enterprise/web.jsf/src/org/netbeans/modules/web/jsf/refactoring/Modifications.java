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
package org.netbeans.modules.web.jsf.refactoring;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionRef;

/**
 * Calss for holding all modifications
 *
 * @author Petr Pisl, Dusan Balek
 */
public final class Modifications {

    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();

    /**
     * Creates a new instance of ModificationResult
     */
    public Modifications() {
    }

    public void addDifference(FileObject fileObject, Difference difference) {
        List<Difference> differences = diffs.get(fileObject);

        if (differences == null) {
            differences = new ArrayList<Difference>();
            differences.add(difference);
            diffs.put(fileObject, differences);
        } else {
            differences.add(difference);
        }
    }

    /**
     * Once all of the changes have been collected, this method can be used to commit the changes to the source files
     */
    public void commit() throws IOException {
        for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
            commit(me.getKey(), me.getValue(), null);
        }
    }

    protected void commit(final FileObject fileObject, final List<Difference> differences, Writer outWriter) throws IOException {
        DataObject dataObj = DataObject.find(fileObject);
        EditorCookie editorCookie = dataObj != null ? (EditorCookie) dataObj.getCookie(EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to
        // found document.
        if (editorCookie != null && outWriter == null) {
            final Document doc = editorCookie.getDocument();
            if (doc != null) {
                final BadLocationException[] blex = new BadLocationException[1];
                if (doc instanceof BaseDocument) {
                    ((BaseDocument) doc).runAtomic(new Runnable() {
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
                                    blex[0] = ex;
                                }
                            }
                        }
                    });
                }
                if (blex[0] != null) {
                    throw new IOException(blex[0]);
                }
                return;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;
        Reader inputReader = null;
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fileObject);
            ins = fileObject.getInputStream();
            baos = new ByteArrayOutputStream();
            FileUtil.copy(ins, baos);

            ins.close();
            ins = null;
            byte[] arr = baos.toByteArray();
            int arrLength = convertToLF(arr);
            baos.close();
            baos = null;
            inputReader = new InputStreamReader(new ByteArrayInputStream(arr, 0, arrLength), encoding);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            if (outWriter == null) {
                outWriter = new OutputStreamWriter(fileObject.getOutputStream(), encoding);
            }
            int offset = 0;

            // need to be sure, that the differences will be sorted acocrding
            // their start offset
            Difference aDifferences[] = differences.toArray(new Difference[0]);
            Arrays.sort(aDifferences, new Comparator<Difference>() {
                public int compare(Difference diff1, Difference diff2) {
                    int result = 0;
                    if (diff1.getStartPosition().getOffset() < diff2.getStartPosition().getOffset()) {
                        result = -1;
                    } else if (diff1.getStartPosition().getOffset() > diff2.getStartPosition().getOffset()) {
                        result = 1;
                    }
                    return result;
                }
            });

            for (Difference diff : aDifferences) {
                if (diff.isExcluded()) {
                    continue;
                }
                int pos = diff.getStartPosition().getOffset();
                if (pos < offset) {
                    continue;
                }
                int toread = pos - offset;
                char[] buff = new char[toread];
                int length;
                int rcounter = 0;
                while ((length = inputReader.read(buff, 0, toread - rcounter)) > 0 && rcounter < toread) {
                    outWriter.write(buff, 0, length);
                    rcounter += length;
                    offset += length;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        outWriter.write(diff.getNewText());
                        break;
                    case REMOVE:
                        int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        inputReader.skip(len);
                        offset += len;
                        break;
                    case CHANGE:
                        len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        inputReader.skip(len);
                        offset += len;
                        outWriter.write(diff.getNewText());
                        break;
                }
            }
            char[] buff = new char[1024];
            int count;
            while ((count = inputReader.read(buff)) > 0) {
                outWriter.write(buff, 0, count);
            }
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (baos != null) {
                baos.close();
            }
            if (inputReader != null) {
                inputReader.close();
            }
            if (outWriter != null) {
                outWriter.close();
            }
        }
    }

    private int convertToLF(byte[] buff) {
        int index = 0;
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] != '\r') {
                buff[index++] = buff[i];
            }
        }
        return index;
    }

    /**
     * Returned string represents preview of resulting source. No difference really is applied. Respects
     * {@code isExcluded()} flag of difference.
     *
     * @param there can be more resulting source, user has to specify which wants to preview.
     * @return if changes are applied source looks like return string
     */
    public String getResultingSource(FileObject fileObject) throws IOException {
        assert fileObject != null : "Provided fileObject is null";
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);

        return writer.toString();
    }

    public static final class Difference {

        Kind kind;
        PositionRef startPos;
        PositionRef endPos;
        String oldText;
        String newText;
        String description;
        private boolean excluded;

        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.excluded = false;
            this.description = description;
        }

        public Kind getKind() {
            return kind;
        }

        public String getDesription() {
            return description;
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

        public void setExclude(boolean exclude) {
            this.excluded = exclude;
        }

        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText; //NOI18N
        }

        public static enum Kind {

            INSERT,
            REMOVE,
            CHANGE
        }
    }
}
