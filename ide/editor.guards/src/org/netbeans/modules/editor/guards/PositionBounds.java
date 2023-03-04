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
package org.netbeans.modules.editor.guards;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

/**
 * A range bounded by two {@link Position}s. This class is derived from
 * {@link org.openide.text.PositionBounds} in fact.
 *
 * @author Petr Hamernik
 */
public final class PositionBounds {

    /** Begin */
    private Position begin;

    /** End */
    private Position end;

    private final GuardedSectionsImpl guards;
    
    private static final class UnresolvedPosition implements Position {

        private int offset;
        
        public UnresolvedPosition(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return this.offset;
        }
    }
    
    private static final class BiasedPosition implements Position {
        
        private Position delegate;
        private Bias bias;
        
        public BiasedPosition(Position delegate, Bias bias) {
            this.delegate = delegate;
            this.bias = bias;
        }
    
        public int getOffset() {
            return bias == Bias.Backward
                    ? this.delegate.getOffset() + 1
                    : this.delegate.getOffset() - 1;
        }

        void resolve(StyledDocument doc) throws BadLocationException {
            if (delegate instanceof UnresolvedPosition) {
                delegate = doc.createPosition(delegate.getOffset());
            }
        }
    }

    /** Creates new <code>PositionBounds</code>.
     * @param begin the start position of the range
     * @param end the end position of the range
    */
    public PositionBounds(Position begin, Position end, GuardedSectionsImpl guards) {
        this.begin = begin;
        this.end = end;
        this.guards = guards;
        assertPositionBounds();
    }
    
    public static PositionBounds create(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(doc.createPosition(begin), doc.createPosition(end), guards);
    }
    
    /**
     * creates bounds with backward begin position allowing to insert text to 
     * begin position while the begin position remains unchanged. The behavior
     * desired for body sections but not for header or footer sections.
     */
    public static PositionBounds createBodyBounds(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(
                new BiasedPosition(doc.createPosition(begin - 1), Position.Bias.Backward),
                new BiasedPosition(doc.createPosition(end + 1), Position.Bias.Forward),
                guards);
    }
    
    /**
     * creates a position bounds object without checking position validity since the document may be empty yet.
     * @see #resolvePositions
     */
    public static PositionBounds createUnresolved(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        StyledDocument doc = guards.getDocument();
        return new PositionBounds(new UnresolvedPosition(begin), new UnresolvedPosition(end), guards);
    }
    
    /**
     * @see #createBodyBounds
     * @see #resolvePositions
     */
    public static PositionBounds createBodyUnresolved(int begin, int end, GuardedSectionsImpl guards) throws BadLocationException {
        return new PositionBounds(
                new BiasedPosition(new UnresolvedPosition(begin - 1), Position.Bias.Backward),
                new BiasedPosition(new UnresolvedPosition(end + 1), Position.Bias.Forward),
                guards);
    }
    
    public void resolvePositions() throws BadLocationException {
        StyledDocument doc = guards.getDocument();

        if (begin instanceof UnresolvedPosition) {
            begin = doc.createPosition(begin.getOffset());
        } else if (begin instanceof BiasedPosition) {
            ((BiasedPosition) begin).resolve(doc);
        }

        if (end instanceof UnresolvedPosition) {
            end = doc.createPosition(end.getOffset());
        } else if (end instanceof BiasedPosition) {
            ((BiasedPosition) end).resolve(doc);
        }
        assertPositionBounds();
    }

    /**
     * Get the starting position of this range.
     * @return the begin position
     */
    public Position getBegin() {
        return begin;
    }

    /**
     * Get the ending position of this range.
     * @return the end position
     */
    public Position getEnd() {
        return end;
    }

    /** Replaces the text contained in this range.
    * This replacement is done atomically, and so is preferable to manual inserts & removes.
    * <p>If you are running this from user-oriented code, you may want to wrap it in {@link NbDocument#runAtomicAsUser}.
    * @param text new text to insert over existing text
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public void setText(final String text) throws BadLocationException  {
        final StyledDocument doc = guards.getDocument();
        final BadLocationException[] hold = new BadLocationException[] { null };
        Runnable run = new Runnable() {
                public void run() {
                    try {
                        int p1 = begin.getOffset();
                        int p2 = end.getOffset();
                        int len = text.length();

                        if (len == 0) { // 1) set empty string

                            if (p2 > p1) {
                                doc.remove(p1, p2 - p1);
                            }
                        } else { // 2) set non empty string

                            int docLen = doc.getLength();

                            if ((p2 - p1) >= 1) {
                                doc.insertString(p1 + 1, text, null);

                                // [MaM] compute length of inserted string
                                len = doc.getLength() - docLen;
                                doc.remove(p1 + 1 + len, p2 - p1 - 1);
                                doc.remove(p1, 1);
                            } else {
                                // zero or exactly one character:
                                // adjust the positions if they are
                                // biased to not absorb the text inserted at the start/end
                                // it would be ridiculous not to have text set by setText
                                // be part of the bounds.
                                doc.insertString(p1, text, null);

                                // [MaM] compute length of inserted string
                                len = doc.getLength() - docLen;

                                if (p2 > p1) {
                                    doc.remove(p1 + len, p2 - p1);
                                }

                                if (begin.getOffset() != p1) {
                                    begin = doc.createPosition(p1);
                                }

                                if ((end.getOffset() - p1) != len) {
                                    end = doc.createPosition(p1 + len);
                                }
                                assertPositionBounds();
                            }
                        }
                    } catch (BadLocationException e) {
                        hold[0] = e;
                    }
                }
            };

        GuardedSectionsImpl.doRunAtomic(doc, run);

        if (hold[0] != null) {
            throw hold[0];
        }
    }

    /** Finds the text contained in this range.
    * @return the text
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public String getText() throws BadLocationException {
        StyledDocument doc = this.guards.getDocument();
        int p1 = begin.getOffset();
        int p2 = end.getOffset();
        // #148542 - hotfix for negative length when p2 > p1 => return ""
        return (p1 <= p2) ? doc.getText(p1, p2 - p1) : "";
    }

    private void assertPositionBounds() {
        // Disabled due to #148542 until a cleaner design will be implemented
//        assert (begin.getOffset() <= end.getOffset()) :
//            "Invalid position bounds: begin-offset=" + begin.getOffset() + " > end-offset=" + end.getOffset();
    }

    /* @return the bounds as the string. */
    public String toString() {
        StringBuilder buf = new StringBuilder("Position bounds["); // NOI18N

        try {
            String content = getText();
            buf.append(begin);
            buf.append(","); // NOI18N
            buf.append(end);
            buf.append(",\""); // NOI18N
            buf.append(content);
            buf.append("\""); // NOI18N
        } catch (BadLocationException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        }

        buf.append("]"); // NOI18N

        return buf.toString();
    }
}
