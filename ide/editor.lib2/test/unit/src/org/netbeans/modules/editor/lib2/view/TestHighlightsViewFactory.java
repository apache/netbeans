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
package org.netbeans.modules.editor.lib2.view;

import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
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
