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
package org.openide.text;

import org.openide.util.NbBundle;

import java.io.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;


/** A range bounded by two {@link PositionRef}s.
*
* @author Petr Hamernik
*/
public final class PositionBounds extends Object implements Serializable {
    static final long serialVersionUID = 3338509625548836633L;

    /** Begin */
    private PositionRef begin;

    /** End */
    private PositionRef end;

    /** Creates new <code>PositionBounds</code>.
     * @param begin the start position of the range
     * @param end the end position of the range
    */
    public PositionBounds(PositionRef begin, PositionRef end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Get the starting position of this range.
     * @return the begin position
     */
    public PositionRef getBegin() {
        return begin;
    }

    /**
     * Get the ending position of this range.
     * @return the end position
     */
    public PositionRef getEnd() {
        return end;
    }

    /** Replaces the text contained in this range.
    * This replacement is done atomically, and so is preferable to manual inserts & removes.
    * <p>If you are running this from user-oriented code, you may want to wrap it in {@link NbDocument#runAtomicAsUser}.
    * @param text new text to insert over existing text
    * @exception IOException if any problem occurred during document loading (if that was necessary)
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public void setText(final String text) throws IOException, BadLocationException {
        final CloneableEditorSupport editor = begin.getCloneableEditorSupport();
        final StyledDocument doc = editor.openDocument();
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

                            if ((p2 - p1) >= 2) {
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
                                    begin = editor.createPositionRef(p1, begin.getPositionBias());
                                }

                                if ((end.getOffset() - p1) != len) {
                                    end = editor.createPositionRef(p1 + len, end.getPositionBias());
                                }
                            }
                        }
                    } catch (BadLocationException e) {
                        hold[0] = e;
                    }
                }
            };

        NbDocument.runAtomic(doc, run);

        if (hold[0] != null) {
            throw hold[0];
        }
    }

    /** Inserts the text after this PositionBounds.
    * @param text The text to insert. The text must not be empty.
    * @return the range of inserted text.
    * @exception IOException if any problem occurred during document loading (if that was necessary)
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public PositionBounds insertAfter(final String text)
    throws IOException, BadLocationException {
        if (text.length() == 0) {
            throw new BadLocationException(
                NbBundle.getBundle(PositionBounds.class).getString("MSG_Empty_string"), begin.getOffset()
            );
        }

        final CloneableEditorSupport editor = begin.getCloneableEditorSupport();
        final StyledDocument doc = editor.openDocument();
        final Object[] hold = new Object[] { null, null };

        Runnable run = new Runnable() {
                public void run() {
                    synchronized (editor.getLock()) {
                        /* editor.getLock(): fixes deadlock - this lock is acquired later anyhow,
                        so we are changing the order in which the locks are acquired */
                        try {
                            // [MaM] remember doclen to compute new length
                            // of the inserted string (the length changes
                            // because insertString removes \r characters
                            // from it)
                            int docLen = doc.getLength();

                            int p1 = end.getOffset();
                            doc.insertString(p1, text, null);

                            int p2 = (p1 + doc.getLength()) - docLen;

                            end = editor.createPositionRef(p1, end.getPositionBias());

                            PositionRef posBegin = editor.createPositionRef(p1, Position.Bias.Forward);
                            PositionRef posEnd = editor.createPositionRef(p2, Position.Bias.Backward);
                            hold[1] = new PositionBounds(posBegin, posEnd);
                        } catch (BadLocationException e) {
                            hold[0] = e;
                        }
                    }
                }
            };

        NbDocument.runAtomic(doc, run);

        if (hold[0] != null) {
            throw (BadLocationException) hold[0];
        } else {
            return (PositionBounds) hold[1];
        }
    }

    /** Finds the text contained in this range.
    * @return the text
    * @exception IOException if any I/O problem occurred during document loading (if that was necessary)
    * @exception BadLocationException if the positions are out of the bounds of the document
    */
    public String getText() throws BadLocationException, IOException {
        StyledDocument doc = begin.getCloneableEditorSupport().openDocument();
        int p1 = begin.getOffset();
        int p2 = end.getOffset();

        return doc.getText(p1, p2 - p1);
    }

    /* @return the bounds as the string. */
    public String toString() {
        StringBuffer buf = new StringBuffer("Position bounds["); // NOI18N

        try {
            String content = getText();
            buf.append(begin);
            buf.append(","); // NOI18N
            buf.append(end);
            buf.append(",\""); // NOI18N
            buf.append(content);
            buf.append("\""); // NOI18N
        } catch (IOException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        } catch (BadLocationException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        }

        buf.append("]"); // NOI18N

        return buf.toString();
    }
}
