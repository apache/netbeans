/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
