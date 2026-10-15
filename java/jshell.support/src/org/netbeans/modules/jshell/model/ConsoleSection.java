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
package org.netbeans.modules.jshell.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Describes a part of the console I/O. A section can be either input or output one.
 * Input sections are entered by the user (or at least contain jshell commands or java
 * code), output sections are produced by the JShell.
 * <p/>
 * Each section <b>consists of full lines</b>, each section contents must be terminated
 * by newline - <b>except</b> the current input section, which may be terminated
 * by EOT.
 * <p/>
 * Each section can be subdivided into <i>snippets</i> each representing a whole command
 * or java code to be executed by JShell. Non-java sections have no 'snippets' in the terms
 * of JShell API, but the entire contents form a 'snippet' to help the user to avoid
 * JShell output markers (prompt, message prefixes, etc).
 * <p/>
 * Once created and produced by ConsoleModel, the ConsoleSection instance is immutable.
 */
public final class ConsoleSection {
    private final int start;
    private int len;
    private final Type type;
    private boolean incomplete;
    private int contentStart = -1;

    /**
     * Possibly null, but may contain offsets of individual snippets in this
     * section. If null, the entire section forms a single snippet. The last
     * snippet in the section may be unterminated.
     * <p/>
     * Positions are relative to the start of this section.
     */
    private Rng[] partOffsets;

    /**
     * For java sections, boundaries of individual snippets found in the text.
     */
    private Rng[] snippetOffsets;

    public ConsoleSection(int start, Type type, int len) {
        this.start = start;
        this.type = type;
        this.len = len;
    }

    public ConsoleSection(int start, Type type) {
        this.start = start;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return start + len;
    }

    public int getLen() {
        return len;
    }

    public ConsoleSection.Type getType() {
        return type;
    }

    public boolean hasMoreParts() {
        return partOffsets != null;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public int getPartBegin() {
        return contentStart;
    }

    public int getPartLen() {
        return contentStart == -1 ? len : len - (contentStart - start);
    }

    public Rng[] getPartRanges() {
        if (hasMoreParts()) {
            return partOffsets;
        } else if (contentStart == -1) {
            return new Rng[0];
        } else {
            return new Rng[]{new Rng(getPartBegin(), start + len)};
        }
    }

    /**
     * Return bounds for an individual snippet. See
     * {@link #getAllSnippetBounds()} for discussion.
     * <p/>
     * It is permitted to use index 0 for Sections with no snippets. Bounds for
     * the section contents will be returned then.
     *
     * @param index snippet index
     * @return bounds
     */
    public Rng getSnippetBounds(int index) {
        if (snippetOffsets == null) {
            if (index > 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            return new Rng(getPartBegin(), start + len);
        } else {
            return snippetOffsets[index];
        }
    }

    /**
     * Returns ranges for all snippets. If there are no snippets (command,
     * output section), returns entire contents of the section.
     * <p/>
     * Each of the returned ranges contains start and end position, in the
     * document offsets. The text between snippet bounds may be discontinuous -
     * use {@link
     * #computeFragments(org.netbeans.modules.jshell.support.Rng)} to get
     * continuous fragments of the text.
     * <p/>
     * @return boundaries of all snippets or boundaries for the contents.
     */
    public Rng[] getAllSnippetBounds() {
        if (snippetOffsets == null) {
            return new Rng[]{new Rng(getPartBegin(), start + len)};
        } else {
            return snippetOffsets;
        }
    }

    void extendTo(int endOffset) {
        this.len = endOffset - start;
    }

    void extendToWithRanges(List<Rng> ranges) {
        Rng last = ranges.get(ranges.size() - 1);
        this.len = last.end - start;
        Rng first = ranges.get(0);
        this.contentStart = first.start;
        if (ranges.size() > 1) {
            this.partOffsets = ranges.toArray(Rng[]::new);
        }
    }

    void setSnippetRanges(List<Rng> ranges) {
        if (ranges.size() > 1) {
            this.snippetOffsets = ranges.toArray(Rng[]::new);
        }
    }

    void extendWithPart(int s, int e) {
        if (contentStart == -1) {
            this.contentStart = s;
            this.len = e - this.start;
            return;
        } else if (partOffsets == null) {
            // there MAY be a preceding part: from the contentStart to the end of the section:
            int x = contentStart + getPartLen();
            if (x >= s) {
                if (e < x) {
                    return;
                }
                this.len = e - start;
                return;
            }
            partOffsets = new Rng[2];
            partOffsets[0] = new Rng(contentStart, getEnd());
        } else {
            partOffsets = Arrays.copyOf(partOffsets, partOffsets.length + 1);
        }
        Rng r = new Rng(s, e);
        assert this.start + this.len <= e;
        partOffsets[partOffsets.length - 1] = r;
    }

    void setComplete(boolean complete) {
        this.incomplete = !complete;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString() + "[").append(start).append("-").append(start + len).append("");
        if (partOffsets != null) {
            sb.append("; snippets = ").append(Arrays.asList(partOffsets).toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public String getRangeContents(Document d, Rng snippetRange) {
        try {
            StringBuilder sb = new StringBuilder();

            Rng[] ranges = computeFragments(snippetRange);
            for (Rng r : ranges) {
                sb.append(d.getText(r.start, r.len()));
            }
            return sb.toString();
        } catch (BadLocationException ex) {
            return "";
        }
    }

    public String getRangeContents(CharSequence s, Rng snippetRange) {
        StringBuilder sb = new StringBuilder();

        Rng[] ranges = computeFragments(snippetRange);
        for (Rng r : ranges) {
            sb.append(s.subSequence(r.start, r.start + r.len()));
        }
        return sb.toString();
    }
    /**
     * Gets text contents of the section. Respect fragment boundaries. Must be
     * called under document read-lock.
     *
     * @param d the document
     * @return text
     */
    public String getContents(Document d) {
        return getContents(d, 0);
    }

    private String getContents(Document d, int startFrom) {
        try {
            int l = d.getLength() + 1;
            if (startFrom > l) {
                startFrom = l;
            }
            if (hasMoreParts()) {
                StringBuilder sb = new StringBuilder();
                for (Rng r : partOffsets) {
                    if (startFrom >= r.end) {
                        continue;
                    } else if (startFrom > r.start) {
                        sb.append(d.getText(startFrom, Math.min(l, r.end) - startFrom));
                    } else {
                        startFrom = Math.min(l, r.start);
                        sb.append(d.getText(r.start, Math.min(l, r.end) - startFrom));
                    }
                }
                return sb.toString();
            } else if (startFrom >= getEnd()) {
                return ""; // NOI18N
            } else if (startFrom > contentStart) {
                int e = Math.min(l, contentStart + getPartLen());
                return d.getText(startFrom, e - startFrom);
            } else {
                startFrom = Math.min(l, contentStart);
                return d.getText(startFrom, Math.min(l - startFrom, getPartLen()));
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Computes fragments of possibly discontinuous contents. Given start/end of
     * a part of contents, and known start/end of section fragments (that is
     * excluding prompts or starting marks), computes fragments of the passed
     * content area; each of the fragments forms a continuous part of the
     * document.
     *
     * @param jr content range
     * @return fragments.
     */
    public Rng[] computeFragments(Rng jr) {
        Rng[] parts = getPartRanges();
        int partIndex = 0;
        List<Rng> res = new ArrayList<>();
        Rng pr = parts[partIndex];
        assert pr.start <= jr.start;

        OUT:
        if (jr.end <= pr.end) {
            res.add(jr);
        } else {
            int l = pr.end - jr.start;
            res.add(new Rng(jr.start, pr.end));
            partIndex++;

            while (jr.end > pr.end) {
                if (partIndex >= parts.length) {
                    break OUT;
                }
                res.add(parts[partIndex++]);
            }
            assert partIndex < parts.length;

            pr = parts[partIndex];

            if (jr.end == pr.end) {
                res.add(pr);
            } else {
                res.add(new Rng(pr.start, jr.end));
            }
        }
        return res.toArray(Rng[]::new);
    }

    /**
     * Converts document offset into section content offset. If `acceptOutside'
     * is true, positions outside section contents are treated as first or
     * position just past the section contents respectively. Returns -1 if the
     * offset cannot be converted.
     *
     * @param offset document offset
     * @param acceptOutside if true, accepts offsets outside the section. Never
     * returns -1
     * @return offset into section contents which corresponds to the original
     * document offset.
     */
    public int offsetToContents(int offset, boolean acceptOutside) {
        int idx = 0;
        for (Rng r : getPartRanges()) {
            if (offset < r.start) {
                return acceptOutside ? idx : -1;
            } else if (offset < r.end) {
                return idx + offset - r.start;
            }
            idx += r.len();
        }
        return acceptOutside ? idx : -1;
    }

    public int offsetFromContents(int offset) {
        for (Rng r : getPartRanges()) {
            if (offset < r.len()) {
                return r.start + offset;
            }
            offset -= r.len();
        }
        return getEnd();
    }

    /**
     *
     * @author sdedic
     */
    public static enum Type {
        /**
         * Java input
         */
        JAVA(true, true), 
        
        /**
         * Incomplete section, must extend
         */
        JAVA_INCOMPLETE(true, true), /**
         * Message from JShell, such as error message, or informational message
         */
        MESSAGE(false, false), /**
         * Output from JShell engine
         */
        OUTPUT(false, false), /**
         * JShell console command
         */
        COMMAND(true, false);
        
        public final boolean input;
    
            public final boolean java;

        private Type(boolean input, boolean java) {
            this.input = input;
            this.java = java;
        }
    }
}
