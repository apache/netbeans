/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
class MulticaretHandler {

    static MulticaretHandler create(JTextComponent c) {
        return new MulticaretHandler(c);                
    }

    private final Document doc;    
    private ArrayList<MutablePositionRegion> regions = null;

    private MulticaretHandler(final JTextComponent c) {
        this.doc = c.getDocument();
        doc.render(() -> {
            Caret caret = c.getCaret();
            if (caret instanceof EditorCaret) {
                List<CaretInfo> carets = ((EditorCaret) caret).getCarets();
                if (carets.size() > 1) {
                    this.regions = new ArrayList<>(carets.size());
                    carets.forEach((ci) -> {
                        try {
                            int[] block = ci.isSelectionShowing() ? null : Utilities.getIdentifierBlock(c, ci.getDot());
                            Position start = NbDocument.createPosition(doc, block != null ? block[0] : ci.getSelectionStart(), Position.Bias.Backward);
                            Position end = NbDocument.createPosition(doc, block != null ? block[1] : ci.getSelectionEnd(), Position.Bias.Forward);
                            regions.add(new MutablePositionRegion(start, end));
                        } catch (BadLocationException ex) {}
                    });
                    Collections.reverse(regions);
                }
            }
        });
    }

    void release() {
        String firstRegionText = getFirstRegionText();
        if (firstRegionText != null) {
            int regionCount = regions.size();
            for (int i = 1; i < regionCount; i++) {
                MutablePositionRegion region = regions.get(i);
                int offset = region.getStartOffset();
                int length = region.getEndOffset() - offset;
                try {
                    final CharSequence old = DocumentUtilities.getText(doc, offset, length);
                    if (!CharSequenceUtilities.textEquals(firstRegionText, old)) {
                        int res = -1;
                        for(int k = 0; k < Math.min(old.length(), firstRegionText.length()); k++) {
                            if (old.charAt(k) == firstRegionText.charAt(k)) {
                                res = k;
                            } else {
                                break;
                            }
                        }
                        String insert = firstRegionText.substring(res+1);
                        CharSequence remove = old.subSequence(res + 1, old.length());
                        if (insert.length() > 0) {
                            doc.insertString(offset + res + 1, insert, null);
                        }
                        if (remove.length() > 0) {
                            doc.remove(offset + res + 1 + insert.length(), remove.length());
                        }
                    }
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }        
    }

    private String getFirstRegionText() {
        return getRegionText(0);
    }
    
    private String getRegionText(int regionIndex) {
        if (regions != null) {
            try {
                MutablePositionRegion region = regions.get(regionIndex);
                int offset = region.getStartOffset();
                int length = region.getEndOffset() - offset;
                return doc.getText(offset, length);
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }
}
