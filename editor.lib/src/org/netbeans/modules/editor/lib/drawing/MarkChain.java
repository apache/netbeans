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

package org.netbeans.modules.editor.lib.drawing;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.lib.EditorPackageAccessor;

/**
* Support class for chain of MarkBlocks
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class MarkChain {

    /** Chain of all marks */
    protected ChainDrawMark chain;

    /** Current mark to make checks faster */
    protected ChainDrawMark curMark;

    /** Document for this mark */
    protected BaseDocument doc;

    /** If this chain uses draw marks, then this is the name for the draw layer
    * that will be used for the marks
    */
    protected String layerName;

    /** The mark created by addMark() method is stored in this variable. In case
     * the mark was not created, because there already was some on this position,
     * the already existing mark is returned. */
    private ChainDrawMark recentlyAddedMark;
    
    /** Construct chain using draw marks */
    public MarkChain(BaseDocument doc, String layerName) {
        this.doc = doc;
        this.layerName = layerName;
    }

    public final ChainDrawMark getChain() {
        return chain;
    }

    public final ChainDrawMark getCurMark() {
        return curMark;
    }

    /** Tests whether the position range is partly or fully inside
    * some mark block from the chain.
    * @param pos compared position
    * @return relation of curMark to the given position
    */
    public int compareMark(int pos) {
        try {
            if (curMark == null) {
                curMark = chain;
                if (curMark == null) {
                    return -1; // no marks yet
                }
            }

            int rel;
            boolean after = false;
            boolean before = false;
            while ((rel = curMark.compare(pos)) != 0) { // just match
                if (rel > 0) { // this mark after pos
                    if (before) {
                        return rel;
                    }
                    if (curMark.prev != null) {
                        after = true;
                        curMark = curMark.prev;
                    } else { // end of chain
                        return rel;
                    }
                } else { // this mark before pos
                    if (after) {
                        return rel;
                    }
                    if (curMark.next != null) {
                        before = true;
                        curMark = curMark.next;
                    } else { // start of chain
                        return rel;
                    }
                }
            }
            return 0; // match
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
            return -1; // don't match, but what to return?
        }
    }

    protected ChainDrawMark createAndInsertNewMark(int pos)
    throws BadLocationException {
        ChainDrawMark mark = createMark();
        try {
            EditorPackageAccessor.get().Mark_insert(mark, doc, pos);
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
        return mark;
    }

    protected ChainDrawMark createMark() {
        ChainDrawMark mark = new ChainDrawMark(layerName, null, Position.Bias.Backward);
        mark.activateLayer = true;
        return mark;
    }

    public boolean addMark(int pos) throws BadLocationException {
        return addMark(pos, false);
    }

    /** Add mark to the chain
     * @param pos position at which the mark should be added
     * @param forceAdd force adding of a fresh mark
     *  even if the mark at the same position already exists
     * @return true if the mark was added
     *         false if there's already mark at that pos
     */
    public boolean addMark(int pos, boolean forceAdd) throws BadLocationException {
        int rel = compareMark(pos);
        if (rel == 0) {
            if (forceAdd) { // create fresh
                ChainDrawMark mark = createAndInsertNewMark(pos);
                recentlyAddedMark = mark;
                if (curMark == chain) { // curMark is first mark
                    chain = curMark.insertChain(mark);
                } else { // curMark is not first mark
                    curMark.insertChain(mark);
                }
                
            } else { // return existing
                recentlyAddedMark = curMark;
                return false; // already exists
            }

        } else if (rel > 0) { // curMark after pos
            ChainDrawMark mark = createAndInsertNewMark(pos);
            recentlyAddedMark = mark;
            if (curMark != null) {
                if (curMark == chain) { // curMark is first mark
                    chain = curMark.insertChain(mark);
                } else { // curMark is not first mark
                    curMark.insertChain(mark);
                }
            } else { // no marks in chain
                chain = mark;
            }
        } else { // curMark before pos
            ChainDrawMark mark = createAndInsertNewMark(pos);
            recentlyAddedMark = mark;
            if (curMark != null) {
                if (curMark.next != null) {
                    curMark.next.insertChain(mark);
                } else { // last mark in chain
                    curMark.setNextChain(mark);
                }
            } else { // no marks in chain
                chain = mark;
            }
        }
        return true;
    }

    /** The mark created by addMark() method is returned by this method. In case
     * the mark was not created, because there already was some on requested position,
     * the already existing mark is returned. */
    public ChainDrawMark getAddedMark() {
        return recentlyAddedMark;
    }
    
    /** Remove non-empty block from area covered by blocks from chain */
    public boolean removeMark(int pos) {
        int rel = compareMark(pos);
        if (rel == 0) {
            boolean first = (curMark == chain);
            curMark = curMark.removeChain();
            if (first) {
                chain = curMark;
            }
            return true;
        } else { // not found
            return false;
        }
    }
    
    public boolean removeMark(ChainDrawMark mark) {
        if (mark == null) {
            throw new NullPointerException();
        }
        
        if (curMark != mark) {
            // dumb impl
            curMark = chain;
            while (curMark != null) {
                if (curMark == mark) {
                    break;
                }
                curMark = curMark.next;
            }
        }
        
        if (curMark != null) {
            boolean first = (curMark == chain);
            curMark = curMark.removeChain();
            if (first) {
                chain = curMark;
            }
            return true;
        } else {
            return false;
        }
    }

    /** Is there mark at given position? */
    public boolean isMark(int pos) {
        return (compareMark(pos) == 0);
    }

    /** Toggle the mark so that if it didn't exist it is created
    * and if it existed it's removed
    * @return true if the new mark was added
    *         false if the existing mark was removed
    */
    public boolean toggleMark(int pos) throws BadLocationException {
        int rel = compareMark(pos);
        if (rel == 0) { // exists
            removeMark(pos);
            return false;
        } else { // didn't exist
            addMark(pos);
            return true;
        }
    }


    public @Override String toString() {
        return "MarkChain: curMark=" + curMark + ", mark chain: " // NOI18N
               + (chain != null ? ("\n" + chain.toStringChain()) : "Empty"); // NOI18N
    }

}
