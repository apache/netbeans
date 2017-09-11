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

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;

/**
* Encapsulation of a special static segment used
* by syntax scanners. Unfortunately document cache cannot
* guarantee that its fragment(s) will hold more than one character
* at the time so syntax scanning cannot be done by finder. Instead
* all the syntax analyzes are done over the syntax segment's data.
* Although it's shared across all instances of editors
* the loads into it should be fast as they are done from cache fragments
* by arraycopy() method.
* The syntax segment is separated into the slots because there
* can be more scanning necessary at one time.
* All the scanning must be done 
*
* @author Miloslav Metelka
* @version 1.00
*/
class SyntaxSeg extends Segment {

    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    private static final int MAX_SLOT_COUNT = 100;

    private static final int REALLOC_INCREMENT = 2048;

    private static ArrayList slotList = new ArrayList();

    static synchronized Slot getFreeSlot() {
        int cnt = slotList.size();
        return (cnt > 0) ? (Slot)slotList.remove(cnt - 1) : new Slot();
    }

    static synchronized void releaseSlot(Slot slot) {
        slotList.add(slot);
    }

    /** From this position on, the data in syntax segment must be marked
    * invalid.
    */
    static synchronized void invalidate(BaseDocument doc, int pos) {
        int cnt = slotList.size();
        for (int i = 0; i < cnt; i++) {
            ((Slot)slotList.get(i)).invalidate(doc, pos);
        }
    }

    static class Slot extends Segment {

        /** Document from which the data in syntax segment come from */
        WeakReference segDocRef = new WeakReference(null);

        /** Begining of valid data in syntax segment */
        int segPos;

        /** Begining of valid data in syntax segment */
        int segLen;

        Slot() {
            this.array = EMPTY_CHAR_ARRAY;
        }

        /** Load the syntax segment if necessary from some location in some
        * document. For best performance there's no pos or len correctness
        * checking. Therefore caller must guarantee the correctness.
        * @return real length that was loaded (syntax segment has limitation
        *   in size)
        */
        int load(BaseDocument doc, int pos, int len)
        throws BadLocationException {
            if (len <= 0) {
                if (len == 0) {
                    count = 0;
                    return 0;
                }
                throw new RuntimeException("len=" + len); // Critical error NOI18N
            }

            BaseDocument segDoc = (BaseDocument)segDocRef.get();
            boolean difDoc = (doc != segDoc);
            if (difDoc) {
                segDoc = doc;
                segDocRef = new WeakReference(segDoc);
            }

            if (difDoc // different documents
                    || pos < segPos // position too low
                    || pos > segPos + segLen // position too high
                    || pos - segPos + len > array.length
               ) { // wouldn't fit

                // possibly realloc the array
                if (len > array.length) {
                    char tmp[] = new char[len + REALLOC_INCREMENT];
                    array = tmp; // original data are not recopied
                }

                segPos = pos;
                segLen = len;

                doc.getChars(pos, array, 0, len); // read chars into array

            } else { // inside array and will fit

                int endSegPos = segPos + segLen;
                int restLen = pos + len - endSegPos;
                if (restLen > 0) { // not fully inside
                    doc.getChars(endSegPos, array, segLen, restLen);
                    segLen += restLen;
                }

            }

            offset = pos - segPos;
            count = len;
            if (offset < 0 || len < 0) {
                throw new BadLocationException("pos=" + pos + ", offset=" + offset // NOI18N
                                + "len=" + len, offset); // Critical error NOI18N
            }
            return len;
        }

        /** Is the area inside the segment? */
        boolean isAreaInside(BaseDocument doc, int pos, int len) {
            return (doc == (BaseDocument)segDocRef.get()
                    && pos >= segPos && pos + len <= segPos + segLen);
        }

        /** Invalidate the slot if it contains the data from the given document.
        * @param doc document in which the change occured
        * @param pos position in the document where the change occured
        */
        void invalidate(BaseDocument doc, int pos) {
            if (doc == (BaseDocument)segDocRef.get()) {
                if (pos < segPos) {
                    segLen = 0;
                } else if (pos < segPos + segLen) {
                    segLen = pos - segPos;
                }
            }
        }

    }

}
