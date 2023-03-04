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
