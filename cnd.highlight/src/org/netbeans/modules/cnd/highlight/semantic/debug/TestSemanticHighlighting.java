/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.semantic.debug;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.highlight.semantic.SemanticHighlighter;
import org.netbeans.modules.cnd.modelutil.CsmFontColorManager;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.text.NbDocument;

/**
 *
 */
public class TestSemanticHighlighting {
    private static final String MIME_TYPE = "text/x-c++"; // NOI18N
    
    public static List<Highlight> gethighlightsBagForTests(Document doc, InterrupterImpl interrupter) {
        List<Highlight> ret = new ArrayList<>();
        
        PositionsBag fastBag = SemanticHighlighter.getSemanticBagForTests(doc, interrupter, true);
        PositionsBag slowBag = SemanticHighlighter.getSemanticBagForTests(doc, interrupter, false);
        PositionsBag bag = new PositionsBag(doc);
        bag.addAllHighlights(fastBag);
        bag.addAllHighlights(slowBag);
        HighlightsSequence hs = bag.getHighlights(0, doc.getLength());
        while (hs.moveNext() && !interrupter.cancelled()) {
            int start = hs.getStartOffset();
            int end = hs.getEndOffset();
            FontColorProvider.Entity type = CsmFontColorManager.instance().getSemanticEntityByAttributeSet(MIME_TYPE, hs.getAttributes());
            
            ret.add(new Highlight(doc, start, end, type));
        }

        return ret;
    }
    
    public static class Highlight {
        private final Document doc;
        private final int startOffset;
        private final int endOffset;
        private final FontColorProvider.Entity type;

        public Highlight(Document doc, int startOffset, int endOffset, FontColorProvider.Entity type) {
            this.doc = doc;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.type = type;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public String getStartPosition() {
            return getPositionByOffset(doc, startOffset);
        }

        public String getEndPosition() {
            return getPositionByOffset(doc, endOffset);
        }

        public FontColorProvider.Entity getType() {
            return type;
        }
        
        private String getPositionByOffset(Document doc, int offset) {
            int lineNumber = -1;
            int lineColumn = -1;
            try {
                lineNumber = 1 + NbDocument.findLineNumber((StyledDocument)doc, offset);
                lineColumn = 1 + NbDocument.findLineColumn((StyledDocument)doc, offset);
            } catch (IndexOutOfBoundsException e) {
            }
            return lineNumber + ":" + lineColumn; // NOI18N
        }
    }
}
