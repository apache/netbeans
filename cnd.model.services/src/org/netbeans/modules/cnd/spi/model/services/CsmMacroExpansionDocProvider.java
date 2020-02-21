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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
