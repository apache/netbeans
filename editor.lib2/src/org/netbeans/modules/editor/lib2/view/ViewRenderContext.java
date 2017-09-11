/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
