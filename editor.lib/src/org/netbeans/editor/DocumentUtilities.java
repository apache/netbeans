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
