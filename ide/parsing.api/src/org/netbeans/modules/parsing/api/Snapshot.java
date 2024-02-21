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

package org.netbeans.modules.parsing.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.parsing.impl.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;


/**
 * Snapshot represents some part of text. Snapshot can be created from 
 * {@link Source} representing file or document, or from some other Snapshot.
 * So Snapshot can represent some block of code written 
 * in different language embedded inside some top level language too. It can contain
 * some generated parts of code that is not contained in the original 
 * file. Snapshot is immutable. It means that Snapshot created 
 * from document opened in editor contains some copy of original text. 
 * You do not need to call Snapshot methods under 
 * any locks, but on other hand Snapshot may not be up to date.
 * 

 * @author Jan Jancura
 */
public final class Snapshot {

    private static final Logger LOG = Logger.getLogger(Snapshot.class.getName());
    
    private final CharSequence text;
    /* package */ final int[] lineStartOffsets;
    private final MimePath mimePath;
    /* package */ final int[][] currentToOriginal;
    private final int[][] originalToCurrent;
    private final Source source;
    private TokenHierarchy<?> tokenHierarchy;
    
   
    private Snapshot (
        CharSequence        text,
        int []              lineStartOffsets,
        Source              source,
        MimePath            mimePath,
        int[][]             currentToOriginal,
        int[][]             originalToCurrent
    ) {
        this.text =         text;
        this.lineStartOffsets = lineStartOffsets;
        this.source =       source;
        this.mimePath =     mimePath;
        this.currentToOriginal =    
                            currentToOriginal;
        this.originalToCurrent = 
                            originalToCurrent;
    }

    @NonNull
    static Snapshot create(
        CharSequence        text,
        int []              lineStartOffsets,
        Source              source,
        MimePath            mimePath,
        int[][]             currentToOriginal,
        int[][]             originalToCurrent) {
        final int textLength = text.length();
        if (textLength > Utilities.getMaxFileSize()) {
            text = "";  //NOI18N
            LOG.log(
                Level.WARNING,
                "Embedding in file {0} of type: {1} of size: {2} has been ignored due to large size. Embeddings large then {3} chars are ignored, you can increase the size by parse.max.file.size property.",  //NOI18N
                new Object[] {
                    source.getFileObject() == null ?
                        "<unknown>" : //NOI18N
                        FileUtil.getFileDisplayName(source.getFileObject()),
                    mimePath,
                    textLength,
                    Utilities.getMaxFileSize()
                });
        }
        return new Snapshot(
            text,
            lineStartOffsets,
            source,
            mimePath,
            currentToOriginal,
            originalToCurrent);
    }
    
    /**
     * Creates a new embedding form part of this snapshot defined by offset and length.
     * 
     * @param offset        A start offset of the new embedding. Start offset
     *                      is relative to the current snapshot.
     * @param length        A length of the new embedding.
     * @param mimeType      Mime type of the new embedding.
     * @return              The new embedding.
     * @throws IndexOutOfBoundsException when bounds of the new embedding exceeds 
     *                      original snapshot.
     */
    public Embedding create (
        int                 offset, 
        int                 length, 
        String              mimeType
    ) {
        if (offset < 0 || length < 0)
            throw new ArrayIndexOutOfBoundsException ("offset=" + offset + ", length=" + length); //NOI18N
        if (offset + length > getText ().length ())
            throw new ArrayIndexOutOfBoundsException ("offset=" + offset + ", length=" + length + ", snapshot-length=" + getText().length()); //NOI18N
        List<int[]> newCurrentToOriginal = new ArrayList<int[]> ();
        List<int[]> newOriginalToCurrent = new ArrayList<int[]> ();
        int i = 1;
        while (i < currentToOriginal.length && currentToOriginal [i] [0] <= offset) i++;
        if (currentToOriginal [i - 1] [1] < 0)
            newCurrentToOriginal.add (new int[] {
                0, currentToOriginal [i - 1] [1]
            });
        else {
            newCurrentToOriginal.add (new int[] {
                0, currentToOriginal [i - 1] [1] + offset - currentToOriginal [i - 1] [0]
            });
            newOriginalToCurrent.add (new int[] {
                currentToOriginal [i - 1] [1] + offset - currentToOriginal [i - 1] [0], 0
            });
        }
        for (; i < currentToOriginal.length && currentToOriginal [i] [0] < offset + length; i++) {
            newCurrentToOriginal.add (new int[] {
                currentToOriginal [i] [0] - offset, currentToOriginal [i] [1]
            });
            if (currentToOriginal [i] [1] >= 0)
                newOriginalToCurrent.add (new int[] {
                    currentToOriginal [i] [1], currentToOriginal [i] [0] - offset
                });
            else
                newOriginalToCurrent.add (new int[] {
                    currentToOriginal [i - 1] [1] + currentToOriginal [i] [0] - currentToOriginal [i - 1] [0], -1
                });
        }
        if (newOriginalToCurrent.size () > 0 &&
            newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [1] >= 0
        )
            newOriginalToCurrent.add (new int[] {
                newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [0] + 
                    length - 
                    newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [1], 
                -1
            });
        MimePath newMimePath = MimePath.get (mimePath, mimeType);
        Snapshot snapshot = create (
            new CharSequenceView(getText(), offset, offset + length),
            null,
            source,
            newMimePath,
            newCurrentToOriginal.toArray (new int [0][]),
            newOriginalToCurrent.toArray (new int [0][])
        );
        return new Embedding (
            snapshot, 
            newMimePath
        );
    }
    
    /**
     * Creates a new embedding for given charSequence. 
     * 
     * @param charSequence  A text of new embedding.
     * @param mimeType      Mime type of the new embedding.
     * @return              The new embedding.
     */
    public Embedding create (
        CharSequence        charSequence, 
        String              mimeType
    ) {
        MimePath newMimePath = MimePath.get (mimePath, mimeType);
        return new Embedding (
            create (
                charSequence,
                null,
                source,
                newMimePath,
                new int[][] {new int[] {0, -1}}, new int[][] {}
            ),
            newMimePath
        );
    }
    
    /**
     * Returns content of this snapshot.
     * 
     * @return              text of this snapshot
     */
    public CharSequence getText (
    ) {
        return text;
    }

    /**
     * Returns this snapshot's mime type.
     * 
     * @return              this snapshot mime type.
     */
    public String getMimeType (
    ) {
        return mimePath.getMimeType (mimePath.size () - 1);
    }

    /**
     * Returns this snapshot's mime path.
     *
     * @return              this snapshot mime type.
     */
    public MimePath getMimePath (
    ) {
        return mimePath;
    }

    /**
     * Get the <code>TokenHierarchy</code> lexed from this snapshot.
     * 
     * @return <code>TokenHierarchy</code> created by a <code>Lexer</code> registered
     *   for this <code>Snapshot</code>'s mime type or <code>null</code> if there is
     *   no such <code>Lexer</code>.
     * @since 1.1
     */
    public TokenHierarchy<?> getTokenHierarchy() {
        if (tokenHierarchy == null) {
            Language<? extends TokenId> lang = Language.find(getMimeType());
            if (lang != null) {
                //copy the InputAttributes from the source document to the
                //charsequence token hierarchy
                Document sourceDocument = source.getDocument(false);
                InputAttributes inputAttrs = sourceDocument != null ? (InputAttributes)sourceDocument.getProperty(InputAttributes.class) : null;
                tokenHierarchy = TokenHierarchy.create(text, false, lang, null, inputAttrs);
            }
        }
        return tokenHierarchy;
    }

    /**
     * Gets an offset in the original source corresponding to an offset in this snapshot.
     * The method will return <code>-1</code> if <code>snapshotOffset</code> can't
     * be translated back to the original source. For example on the <code>snapshotOffset</code>
     * is in a "virtual" area of text. That is in a text generated by some <code>EmbeddingProvider</code>,
     * which has no representation in the top level source code.
     * 
     * @param snapshotOffset The offset in this snapshot.
     *
     * @return The offset in the original source or <code>-1</code>.
     */
    public int getOriginalOffset (
        int snapshotOffset
    ) {
        if (snapshotOffset < 0)
            return -1;
        if (snapshotOffset > getText ().length ())
            return -1;

        int low = 0;
        int high = currentToOriginal.length - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = currentToOriginal [mid] [0];
            if (cmp > snapshotOffset)
                high = mid - 1;
            else
            if (mid == currentToOriginal.length - 1 ||
                currentToOriginal [mid + 1] [0] > snapshotOffset
            ) {
                if (currentToOriginal [mid] [1] < 0) {
                    if (snapshotOffset == cmp && mid > 0)
                        return snapshotOffset - currentToOriginal [mid - 1] [0] + currentToOriginal [mid - 1] [1];
                    else
                        return currentToOriginal [mid] [1];
                } else
                    return snapshotOffset - currentToOriginal [mid] [0] + currentToOriginal [mid] [1];
            } else
                low = mid + 1;
        } // while

        return -1;
    }
    
    /**
     * Gets an offset in this snapshot corresponding to an offset
     * in the original source. The method can return <code>-1</code> if <code>originalOffset</code>
     * points to an area in the original source, which is not part of this snapshot.
     * 
     * @param originalOffset The offset in the original source.
     * 
     * @return The offset in this snapshot or <code>-1</code>.
     */
    public int getEmbeddedOffset (
        int                 originalOffset
    ) {
        int low = 0;
        int high = originalToCurrent.length - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = originalToCurrent [mid] [0];

            if (cmp > originalOffset)
                high = mid - 1;
            else
            if (mid == originalToCurrent.length - 1 ||
                originalToCurrent [mid + 1] [0] > originalOffset
            )
                if (originalToCurrent [mid] [1] < 0)
                    if (originalOffset == cmp && mid > 0)
                        return originalOffset - originalToCurrent [mid - 1] [0] + originalToCurrent [mid - 1] [1];
                    else
                        return originalToCurrent [mid] [1];
                else
                    return originalOffset - originalToCurrent [mid] [0] + originalToCurrent [mid] [1];
            else
                low = mid + 1;
        } // while
        return -1;
    }
    
    /**
     * Returns source this snapshot has originally been created from.
     * 
     * @return              a source this snapshot has originally been created from.
     */
    public Source getSource () {
        return source;
    }
    
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ("Snapshot ");
        sb.append (hashCode ());
        sb.append (": ");
        Source _source = getSource ();
        FileObject fileObject = _source.getFileObject ();
        if (fileObject != null)
            sb.append (fileObject.getNameExt ());
        else
            sb.append (mimePath).append (" ").append (_source.getDocument (false));
        if (!getMimeType ().equals (_source.getMimeType ())) {
            sb.append ("( ").append (getMimeType ()).append (" ");
            sb.append (getOriginalOffset (0)).append ("-").append(getOriginalOffset (getText ().length () - 1)).append (")");
        }
        return sb.toString ();
    }

    private static final class CharSequenceView implements CharSequence {
        private final CharSequence seq;
        private final int from;
        private final int to;

        CharSequenceView(
                @NonNull final CharSequence str,
                final int from,
                final int to) {
            Parameters.notNull("str", str); //NOI18N
            checkRanges(from, to, 0, str.length());
            this.seq = str;
            this.from = from;
            this.to = to;
        }

        @Override
        public int length() {
            return to - from;
        }

        @Override
        public char charAt(final int index) {
            return seq.charAt(from+index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            checkRanges(start, end, from, to);
            return new CharSequenceView(seq, from+start, from+end);
        }

        private static void checkRanges(
            final int from,
            final int to,
            final int lbound,
            final int ubound) {
            if (from < 0) {
                throw new IllegalArgumentException("Start index: " + from + " < 0 ");   //NOI18N
            }
            if (from > to) {
                throw new IllegalArgumentException("Start index: " + from + " > To index:" + to);   //NOI18N
            }
            if (from + lbound > ubound) {
                throw new IllegalArgumentException("Start index: " + from + " > length: " + (ubound - lbound));   //NOI18N
            }
            if (to + lbound > ubound) {
                throw new IllegalArgumentException("To index: " + to + " > length: " + (ubound - lbound));   //NOI18N
            }
        }

        @Override
        public String toString() {
            if (seq instanceof String) {
                return ((String)seq).substring(from, to);
            } else {
                final char[] data = new char[to-from];
                for (int i=0; i<data.length; i++) {
                    data[i] = seq.charAt(from+i);
                }
                return new String(data);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CharSequenceView)) {
                return false;
            }
            final CharSequenceView other = (CharSequenceView) obj;
            return from == other.from &&
                    to == other.to &&
                    seq.equals(other.seq);
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + from;
            hash = hash * 31 + to;
            hash = hash * 31 + seq.hashCode();
            return hash;
        }
    }
}
