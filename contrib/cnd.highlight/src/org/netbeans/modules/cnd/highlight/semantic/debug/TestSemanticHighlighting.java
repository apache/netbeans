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
