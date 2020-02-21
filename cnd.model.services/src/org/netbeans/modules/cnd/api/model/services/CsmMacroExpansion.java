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
package org.netbeans.modules.cnd.api.model.services;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.util.Lookup;

/**
 * Service that provides macro expansions.
 *
 */
public final class CsmMacroExpansion {

    // Flags for document of macro expansion view panel
    /** Flag of macro expansion view document. */
    public static final String MACRO_EXPANSION_VIEW_DOCUMENT = "macro-expansion-view-document"; // NOI18N
    public final static String USE_OWN_CARET_POSITION = "use-own-caret-position"; // NOI18N
    /** A dummy providers that never returns any results.*/
    private static final CsmMacroExpansionDocProvider EMPTY_MACRO_EXPANSION_DOC_PROVIDER = new EmptyMacroExpansionDoc();
    /** A dummy providers that never returns any results.*/
    private static final CsmMacroExpansionViewProvider EMPTY_MACRO_EXPANSION_VIEW_PROVIDER = new EmptyMacroExpansionView();
    /** Default macro expansion provider. */
    private static CsmMacroExpansionDocProvider defaultMacroExpansionDocProvider;
    /** Default macro expansion view provider. */
    private static CsmMacroExpansionViewProvider defaultMacroExpansionViewProvider;

    /**
     * Constructor.
     */
    private CsmMacroExpansion() {
    }
    
    /** Static method to obtain the provider.
     * @return the provider
     */
    private static synchronized CsmMacroExpansionDocProvider getMacroExpansionDocProvider() {
        if (defaultMacroExpansionDocProvider != null) {
            return defaultMacroExpansionDocProvider;
        }
        defaultMacroExpansionDocProvider = Lookup.getDefault().lookup(CsmMacroExpansionDocProvider.class);
        return defaultMacroExpansionDocProvider == null ? EMPTY_MACRO_EXPANSION_DOC_PROVIDER : defaultMacroExpansionDocProvider;
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    private static synchronized CsmMacroExpansionViewProvider getMacroExpansionViewProvider() {
        if (defaultMacroExpansionViewProvider != null) {
            return defaultMacroExpansionViewProvider;
        }
        defaultMacroExpansionViewProvider = Lookup.getDefault().lookup(CsmMacroExpansionViewProvider.class);
        return defaultMacroExpansionViewProvider == null ? EMPTY_MACRO_EXPANSION_VIEW_PROVIDER : defaultMacroExpansionViewProvider;
    }

//    /**
//     * Returns instantiation of template.
//     *
//     * @param template - template for instantiation
//     * @param params - template paramrters
//     * @return - instantiation
//     */
//    public static String getExpandedText(CsmFile file, int startOffset, int endOffset) {
//        return getMacroExpansionProvider().getExpandedText(file, startOffset, endOffset);
//    }

    /**
     * Macro expands content of one document to another.
     *
     * @param inDoc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @param outDoc - result
     * @return - number of expansions
     */
    public static int expand(Document inDoc, int startOffset, int endOffset, Document outDoc, AtomicBoolean canceled) {
        return getMacroExpansionDocProvider().expand(inDoc, startOffset, endOffset, outDoc, canceled);
    }

    /**
     * Macro expands content of the document.
     *
     * @param doc - document for macro expansion
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion, null otherwise
     */
    public static String expand(Document doc, int startOffset, int endOffset) {
        return getMacroExpansionDocProvider().expand(doc, startOffset, endOffset);
    }

    /**
     * Macro expands content of the document.
     * If we already knew file for document it's better to use this function, because it's faster.
     *
     * @param doc - document for macro expansion
     * @param file - file of the document
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @param updateIfNeeded - update if needed (slow and should not be done from EDT or under doc locks)
     * @return - expansion, null otherwise
     */
    public static String expand(Document doc, CsmFile file, int startOffset, int endOffset, boolean updateIfNeeded) {
        return getMacroExpansionDocProvider().expand(doc, file, startOffset, endOffset, updateIfNeeded);
    }

    /**
     * Macro expands specified string in specified contest and excludes comments.
     *
     * @param doc - document for macro expansion
     * @param doc - file of the document
     * @param startOffset - start offset for expansion
     * @param endOffset - end offset for expansion
     * @return - expansion, null otherwise
     */
    public static String expand(Document doc, int offset, String code) {
        return getMacroExpansionDocProvider().expand(doc, offset, code);
    }

    /**
     * returns interval of macro expansion for offset in original text
     * @param doc document
     * @param offset offset in document
     * @param wait flag indicating if existing info should be updated to the most recent state
     *  (which could takes time) or return what exists now, but without any blocks (for AWT calls)
     * @return array of two elements [start;end] of expansion in document
     */
    public static int[] getMacroExpansionSpan(Document doc, int offset, boolean wait) {
        return getMacroExpansionDocProvider().getMacroExpansionSpan(doc, offset, wait);
    }
    /**
     * Transforms original offset to offset in expanded text.
     *
     * @param expandedDoc - document
     * @param originalOffset - original offset
     * @return offset in expanded text
     */
    public static int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
        return getMacroExpansionDocProvider().getOffsetInExpandedText(expandedDoc, originalOffset);
    }

    /**
     * Transforms offset in expanded text to original offset.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return original offset
     */
    public static int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getOffsetInOriginalText(expandedDoc, expandedOffset);
    }

    /**
     * Returns offset of the next macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public static int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getNextMacroExpansionStartOffset(expandedDoc, expandedOffset);
    }

    /**
     * Returns offset of the previous macro expansion.
     *
     * @param expandedDoc - document
     * @param expandedOffset - offset in expanded text
     * @return offset of the next macro expansion
     */
    public static int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        return getMacroExpansionDocProvider().getPrevMacroExpansionStartOffset(expandedDoc, expandedOffset);
    }

    /**
     * Returns usages of token on offset
     *
     * @param expandedDoc - document
     * @param offset - offset
     * @return array of usages
     */
    public static int[][] getUsages(Document expandedDoc, int offset) {
        return getMacroExpansionDocProvider().getUsages(expandedDoc, offset);
    }

    /**
     * Expands document on specified position and shows Macro Expansion View panel.
     *
     * @param doc - document
     * @param offset - offset in document
     */
    public static void showMacroExpansionView(Document doc, int offset) {
        UIGesturesSupport.submit("USG_CND_SHOW_MACRO_EXPANSION"); //NOI18N
        getMacroExpansionViewProvider().showMacroExpansionView(doc, offset);
    }

    //
    // Implementation of the default provider
    //
    private static final class EmptyMacroExpansionDoc implements CsmMacroExpansionDocProvider {

        EmptyMacroExpansionDoc() {
        }

        @Override
        public int expand(Document inDoc, int startOffset, int endOffset, Document outDoc, AtomicBoolean canceled) {
            return 0;
        }

        @Override
        public String expand(Document doc, int startOffset, int endOffset) {
            return null;
        }

        @Override
        public String expand(Document doc, CsmFile file, int startOffset, int endOffset, boolean updateIfNeeded) {
            return null;
        }
        
        @Override
        public String expand(Document doc, int offset, String code) {
            return null;
        }

        @Override
        public int[] getMacroExpansionSpan(Document doc, int offset, boolean wait) {
            // returns empty expansion
            return new int[]{offset, offset};
        }

        @Override
        public int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
            return originalOffset;
        }

        @Override
        public int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }

        @Override
        public int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }

        @Override
        public int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
            return expandedOffset;
        }

        @Override
        public int[][] getUsages(Document expandedDoc, int offset) {
            return null;
        }
    }

    //
    // Implementation of the default provider
    //
    private static final class EmptyMacroExpansionView implements CsmMacroExpansionViewProvider {

        EmptyMacroExpansionView() {
        }

        @Override
        public void showMacroExpansionView(Document doc, int offset) {
        }

    }
}
