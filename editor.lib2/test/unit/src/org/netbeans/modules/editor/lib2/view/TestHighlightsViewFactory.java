/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author Miloslav Metelka
 */
final class TestHighlightsViewFactory extends EditorViewFactory {
    
    static TestHighlightsViewFactory get(JTextComponent c) {
        return (TestHighlightsViewFactory) c.getClientProperty(TestHighlightsViewFactory.class);
    }

    List<TestHighlight> highlights;
    
    int highlightIndex;
    
    TestHighlight highlight;
    
    int expectedContinueCreationStartOffset = -1;
    
    int expectedContinueCreationEndOffset;

    TestHighlightsViewFactory(View documentView) {
        super(documentView);
        textComponent().putClientProperty(TestHighlightsViewFactory.class, this);
    }

    void fireChange(int fireStartOffset, int fireEndOffset) {
        super.fireEvent(EditorViewFactoryChange.createList(fireStartOffset, fireEndOffset,
                EditorViewFactoryChange.Type.CHARACTER_CHANGE));
    }

    public List<TestHighlight> getHighlights() {
        return highlights;
    }
    
    public void setHighlights(List<TestHighlight> highlights) {
        this.highlights = highlights;
    }
    
    public void setContinueCreationRange(int startOffset, int endOffset) {
        this.expectedContinueCreationStartOffset = startOffset;
        this.expectedContinueCreationEndOffset = endOffset;
    }
    
    public boolean isContinueCreationUnset() {
        return expectedContinueCreationStartOffset == -1;
    }

    @Override
    public void restart(int startOffset, int matchOffset,boolean createViews) {
        fetchHighlightContaining(startOffset);
    }

    @Override
    public void continueCreation(int startOffset, int endOffset) {
        if (expectedContinueCreationStartOffset != -1) {
            TestCase.assertEquals("continueCreation(): Start offset", expectedContinueCreationStartOffset, startOffset);
            TestCase.assertEquals("continueCreation(): End offset", expectedContinueCreationEndOffset, endOffset);
            expectedContinueCreationStartOffset = -1;
        }
    }
    
    private void fetchHighlightContaining(int offset) {
        while (highlights != null && highlightIndex < highlights.size()) {
            highlight = highlights.get(highlightIndex++);
            if (highlight.endOffset() > offset) {
                return;
            }
        }
        highlight = null;
    }

    @Override
    public int nextViewStartOffset(int offset) {
        if (highlight != null && highlight.startOffset() < offset) {
            fetchHighlightContaining(offset);
        }
        if (highlight != null) {
            return highlight.startOffset();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset, boolean forceLimit,
    EditorView origView, int nextOrigViewOffset) {
        int endOffset = highlight.endOffset();
        if (endOffset > limitOffset && forceLimit) {
            endOffset = limitOffset;
        }
        return new TestHighlightsView(startOffset, highlight.endOffset() - startOffset, highlight.attrs);
    }

    @Override
    public int viewEndOffset(int startOffset, int limitOffset, boolean forceLimit) {
        int endOffset = highlight.endOffset();
        if (endOffset > limitOffset && forceLimit) {
            endOffset = limitOffset;
        }
        return limitOffset;
    }

    @Override
    public void finishCreation() {
        highlight = null;
        highlightIndex = 0;
    }
    
    static final class FactoryImpl implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new TestHighlightsViewFactory(documentView);
        }

        @Override
        public int weight() {
            return 10;
        }
        
    }

}
