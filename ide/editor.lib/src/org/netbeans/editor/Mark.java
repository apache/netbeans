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

package org.netbeans.editor;

import java.util.Comparator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;

/**
* Marks hold the relative position in the document.
*
* @author Miloslav Metelka
* @version 1.00
* @deprecated Use {@link Position} instead.
*/
@Deprecated
public class Mark {

    private static final MarkComparator MARK_COMPARATOR = new MarkComparator();

    /** Document to which this mark belongs. */
    private BaseDocument doc;
    
    /** Position to which this mark delegates. */
    private Position pos;
    
    /** Bias of the mark. It is either
     * {@link javax.swing.text.Position.Bias#Forward}
     * or {@link javax.swing.text.Position.Bias#Backward}
     */
    private Position.Bias bias;
    
    /** Construct new mark with forward bias. */
    public Mark() {
        this(Position.Bias.Forward);
    }

    public Mark(Position.Bias bias) {
        this.bias = bias;
    }
    
    /** Construct new mark.
    * @param backwardBias whether the inserts performed right at the position
    *   of this mark will go after this mark i.e. this mark will not move
    *   forward when inserting right at its position. This flag corresponds
    *   to <code>Position.Bias.Backward</code>.
    */
    public Mark(boolean backwardBias) {
        this(backwardBias ? Position.Bias.Backward : Position.Bias.Forward);
    }
    
    void insert(BaseDocument doc, int offset) throws InvalidMarkException, BadLocationException {
        BaseDocument ldoc = this.doc;
        if (ldoc != null) {
            throw new InvalidMarkException("Mark already inserted: mark=" + this // NOI18N
                + ", class=" + this.getClass()); // NOI18N
        }

        this.doc = doc;
        ldoc = this.doc;
        synchronized (ldoc) {
            if (pos != null) {
                throw new IllegalStateException("Mark already inserted: mark=" + this // NOI18N
                + ", class=" + this.getClass()); // NOI18N
            }

			if (offset < 0 || offset > ldoc.getLength() + 1) { // doc.getEndPosition() is valid
				throw new BadLocationException("Invalid offset", offset); // NOI18N
			}

			// Deal with supplementary characters #164820

			if (offset <= ldoc.getLength() && Character.isLowSurrogate(org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(ldoc).charAt(offset))) {
				if (bias == Bias.Forward && offset < ldoc.getLength()) {
					offset++;

				} else if (bias == Bias.Backward && offset > 0) {
					offset--;
				}
				
				// If there is still a low surrogate after recalculating,
				// treat it as an invalid document, just ignore and pass through.
				// Since there should be a surrogate pair in Java and Unicode to
				// represent a supplementary character.
			}

            pos = doc.createPosition(offset, bias);
        }
    }
    
    void move(BaseDocument doc, int newOffset) throws InvalidMarkException, BadLocationException {
        dispose();
        insert(doc, newOffset);
    }
    
    /** Get the position of this mark */
    public final int getOffset() throws InvalidMarkException {
        BaseDocument ldoc = doc;
        if (ldoc != null) {
            synchronized (ldoc) {
                if (pos != null) {
                    return pos.getOffset();
                } else {
                    throw new InvalidMarkException();
                }
            }
        } else {
            throw new InvalidMarkException();
        }
    }

    /** Get the line number of this mark */
    public final int getLine() throws InvalidMarkException {
        BaseDocument ldoc = doc;
        if (ldoc != null) {
            synchronized (ldoc) {
                if (pos != null) {
                    int offset = pos.getOffset();
                    Element lineRoot = ldoc.getParagraphElement(0).getParentElement();
                    return lineRoot.getElementIndex(offset);

                } else {
                    throw new InvalidMarkException();
                }
            }
        } else {
            throw new InvalidMarkException();
        }
    }

    /** Get the insertAfter flag.
     * Replaced by {@link #getBackwardBias()}
     * @deprecated
     */
    public final boolean getInsertAfter() {
        return (bias == Position.Bias.Backward);
    }
    
    /** @return true if the mark has backward bias or false if it has forward bias.
     */
    public final boolean getBackwardBias() {
        return getInsertAfter();
    }
    
    /** @return the bias of this mark. It will be either
     * {@link javax.swing.text.Position.Bias#Forward}
     * or {@link javax.swing.text.Position.Bias#Backward}.
     */
    public final Position.Bias getBias() {
        return bias;
    }
    
    int getBiasAsInt() {
        return (bias == Position.Bias.Backward) ? -1 : +1;
    }
    
    /** Mark will no longer represent a valid place in the document.
     * Although it will not be removed from the structure that holds
     * the marks it will be done later automatically.
     */
    public final void dispose() {
        BaseDocument ldoc = doc;
        if (ldoc != null) {
            synchronized (ldoc) {
                if (pos != null) {
                    pos = null;
                    this.doc = null;
                    return;
                }
            }
        }

        throw new IllegalStateException("Mark already disposed: mark=" + this // NOI18N
                + ", class=" + this.getClass()); // NOI18N
    }
        
    /** Remove mark from the structure holding the marks. The mark can
    * be inserted again into some document.
    */
    public final void remove() throws InvalidMarkException {
        dispose();
    }


    /** Compare this mark to some position.
     * @param pos tested position
     * @return zero - if the marks have the same position
     *         less than zero - if this mark is before the position
     *         greater than zero - if this mark is after the position
     */
    public final int compare(int pos) throws InvalidMarkException {
        return getOffset() - pos;
    }

    /** This function is called from removeUpdater when mark occupies
     * the removal area. The mark can decide what to do next.
     * If it doesn't redefine this method it will be simply moved to
     * the begining of removal area. It is valid to add or remove other mark 
     * from this method. It is even possible (but not very useful)
     * to add the mark to the removal area. However that mark will not be
     * notified about current removal.
     * @deprecated It will not be supported in the future.
     */
    protected void removeUpdateAction(int pos, int len) {
    }


    /** @return true if this mark is currently inserted in the document
     * or false otherwise.
     */
    public final boolean isValid() {
        BaseDocument ldoc = doc;
        if (ldoc != null) {
            synchronized (ldoc) {
                return (pos != null);
            }
        }
        
        return false;
    }

    /** Get info about <CODE>Mark</CODE>. */
    public @Override String toString() {
        return "offset=" + (isValid() ? Integer.toString(pos.getOffset()) : "<invalid>") // NOI18N
               + ", bias=" + bias; // NOI18N
    }

    private static final class MarkComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            Mark m1 = ((Mark)o1);
            Mark m2 = ((Mark)o2);
            try {
                int offDiff = m1.getOffset() - m2.getOffset();
                if (offDiff != 0) {
                    return offDiff;
                } else {
                    return m1.getBiasAsInt() - m2.getBiasAsInt();
                }
            } catch (InvalidMarkException e) {
                throw new IllegalStateException(e.toString());
            }
        }

    }

}
