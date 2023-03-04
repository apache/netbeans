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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;

/**
 * Various document-related utilities.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DocumentUtilities {

    private DocumentUtilities() {
        // no instances
    }

    /**
     * @return &gt;=0 offset of the gap start in the document's content.
     *         -1 if the document does not export <CODE>GapStart</CODE> interface.
     */
    public static int getGapStart(Document doc) {
        GapStart gs = (GapStart)doc.getProperty(GapStart.class);
        return (gs != null) ? gs.getGapStart() : -1;
    }
    
    /**
     * Copy portion of the document into target character array.
     * 
     * @param srcDoc document from which to copy.
     * @param srcStartOffset offset of the first character to copy.
     * @param srcEndOffset offset that follows the last character to copy.
     * @param dst destination character array into which the data will be copied.
     * @param dstOffset offset in the destination array at which the putting
     *  of the characters starts.
     * 
     * @throws javax.swing.text.BadLocationException 
     */
    public static void copyText(Document srcDoc, int srcStartOffset,
    int srcEndOffset, char[] dst, int dstOffset) throws BadLocationException {
        Segment text = new Segment();
        int gapStart = getGapStart(srcDoc);
        if (gapStart != -1 && srcStartOffset < gapStart && gapStart < srcEndOffset) {
            // Get part below gap
            srcDoc.getText(srcStartOffset, gapStart - srcStartOffset, text);
            System.arraycopy(text.array, text.offset, dst, dstOffset, text.count);
            dstOffset += text.count;
            srcStartOffset = gapStart;
        }

        srcDoc.getText(srcStartOffset, srcEndOffset - srcStartOffset, text);
        System.arraycopy(text.array, text.offset, dst, dstOffset, srcEndOffset - srcStartOffset);
    }

}
