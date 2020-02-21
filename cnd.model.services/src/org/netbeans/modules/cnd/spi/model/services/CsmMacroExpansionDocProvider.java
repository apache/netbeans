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
package org.netbeans.modules.cnd.spi.model.services;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 * Service that provides macro expansions.
 *
 */
public interface CsmMacroExpansionDocProvider {
    /**
     * Macro expands content of one document to another.
     *
     * @param inDoc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @param outDoc - result
     * @return - number of expansions
     */
    public int expand(Document inDoc, int startOffset, int endOffset, Document outDoc, AtomicBoolean canceled);

    /**
     * Macro expands content of the document.
     *
     * @param doc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion, null otherwise
     */
    public String expand(Document doc, int startOffset, int endOffset);

    /**
     * Macro expands content of the document.
     * If we already knew file for document it's better to use this function, because it's faster.
     *
     * @param doc - document for macro expansion
     * @param doc - file of the document
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion, null otherwise
     */
    public String expand(Document doc, CsmFile file, int startOffset, int endOffset, boolean updateIfNeeded);

    /**
     * Macro expands specified string in specified contest and excludes comments.
     *
     * @param doc - document for macro expansion
     * @param doc - file of the document
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion, null otherwise
     */
    public String expand(Document doc, int offset, String code);

    /**
     * returns interval of macro expansion for offset in original text
     * @param doc document
     * @param offset offset in document
     * @param wait flag indicating if existing info must be updated to the most recent state
     *  (which could takes time) or return what exists now, but without any blocks (for AWT calls)
     * @return array of two elements [start;end] of expansion in document
     */
    public int[] getMacroExpansionSpan(Document doc, int offset, boolean wait);

    /**
     * Transforms original offset to offset in expanded text.
     *
     * @param expandedDoc - document
     * @param originalOffset - original offset
     * @return offset in expanded text
     */
    public int getOffsetInExpandedText(Document expandedDoc, int originalOffset);

    /**
     * Transforms offset in expanded text to original offset.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return original offset
     */
    public int getOffsetInOriginalText(Document expandedDoc, int expandedOffset);

    /**
     * Returns offset of the next macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset);

    /**
     * Returns offset of the previous macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset);

    /**
     * Returns usages of token on offset
     *
     * @param expandedDoc - document
     * @param offset - offset
     * @return array of usages
     */
    public int[][] getUsages(Document expandedDoc, int offset);
}
