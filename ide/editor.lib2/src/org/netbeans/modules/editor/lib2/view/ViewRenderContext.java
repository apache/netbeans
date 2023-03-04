/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.lib2.view;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * Additional info related to view rendering.
 * <br>
 * Provided by {@link EditorView.Parent }.
 *
 * @author Miloslav Metelka
 */
public final class ViewRenderContext {
    
    private final DocumentView docView;
    
    ViewRenderContext(DocumentView docView) {
        this.docView = docView;
    }

    /**
     * Get font rendering context that for example may be used for text layout
     * creation.
     *
     * @return font rendering context.
     */
    public FontRenderContext getFontRenderContext() {
        return docView.getFontRenderContext();
    }

    /**
     * Get special highlighting sequence that is a merge of attributes of the
     * view with top painting highlights. <br> It's only allowed to call this
     * method (and use the returned value) during view's paint() methods
     * execution otherwise it throws an IllegalStateException.
     *
     * @param view non-null child editor view.
     * @param shift &gt;=0 shift inside the view (first highlight will start at
     * view.getStartOffset() + shift).
     * @return special highlights sequence that covers whole requested area (has
     * getAttributes() == null in areas without highlights).
     */
    public HighlightsSequence getPaintHighlights(EditorView view, int shift) {
        return docView.getPaintHighlights(view, shift);
    }

    /**
     * Get row height of an single row of views being rendered by a paragraph view.
     * <br>
     * For views that only render text the views should return this height
     * as their vertical span since the user may forcibly decrease row height
     * (see DocumentViewOp.rowHeightCorrection).
     *
     * @return &gt;=0 row height.
     */
    public float getDefaultRowHeight() {
        return docView.op.getDefaultRowHeight();
    }


    /**
     * Get ascent (distance between start y of a view and font's baseline)
     * used for views.
     *
     * @return &gt;=0 ascent.
     */
    public float getDefaultAscent() {
        return docView.op.getDefaultAscent();
    }

    /**
     * Get font for text rendering that incorporates a possible text zoom
     * (Alt+MouseWheel function).
     * <br>
     * Ideally all the fonts rendered by views should be "translated" by this method.
     */
    public Font getRenderFont(Font font) {
        return docView.op.getFontInfo(font).renderFont;
    }

}
