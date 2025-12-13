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

package org.netbeans.modules.editor.fold.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.ViewRenderContext;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.openide.util.NbBundle;

import static org.netbeans.modules.editor.fold.ui.Bundle.*;

import org.netbeans.modules.editor.lib2.caret.CaretFoldExpander;


/**
 * View with highlights. This is the most used view.
 *
 * @author Miloslav Metelka
 */

final class FoldView extends EditorView {
    
    static {
        CaretFoldExpander.register(new CaretFoldExpanderImpl());
    }

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(FoldView.class.getName());

    /**
     * Extra space added to each side of description text of a fold view.
     */
    private static final float EXTRA_MARGIN_WIDTH = 3;

    /** Raw end offset of this view. */
    private int rawEndOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    private final JTextComponent textComponent; // 32 + 4 = 36 bytes

    private final Fold fold; // 36 + 4 = 40 bytes
    
    private TextLayout collapsedTextLayout; // 40 + 4 = 44 bytes
    
    /**
     * Colors for the text of the placeholder.
     */
    private AttributeSet    foldingColors;
    
    /**
     * Color for the border, ONLY foreground is used.
     */
    private AttributeSet    foldingBorderColors;
    
    private AttributeSet    selectedColors;
    
    private int options;

    /**
     * Coloring that will be used for code folding icons displayed in editor
     */
    public static final String COLORING_PLACEHOLDER_TEXT = "code-folding"; // NOI18N

    /**
     * Coloring for the placeholder view and tooltip borders
     */
    public static final String COLORING_PLACEHOLDER_BORDER = "code-folding-border"; // NOI18N

    
    public FoldView(JTextComponent textComponent, Fold fold, FontColorSettings colorSettings, int options) {
        super(null);
        int offset = fold.getStartOffset();
        int len = fold.getEndOffset() - offset;
        assert (len > 0) : "length=" + len + " <= 0"; // NOI18N
        this.length = len;
        this.textComponent = textComponent;
        this.fold = fold;
        this.foldingColors = colorSettings.getFontColors(FontColorNames.CODE_FOLDING_COLORING);
        this.selectedColors = colorSettings.getFontColors(FontColorNames.SELECTION_COLORING);
        this.foldingBorderColors = colorSettings.getFontColors(COLORING_PLACEHOLDER_BORDER);
        this.options = options;
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (axis == View.X_AXIS) {
            String desc = fold.getDescription(); // For empty desc a single-space text layout is returned
            float advance = 0;
            if (desc.length() > 0) {
                TextLayout textLayout = getTextLayout();
                if (textLayout == null) {
                    return 0f;
                }
                advance = textLayout.getAdvance();
            }
            return advance + (2 * EXTRA_MARGIN_WIDTH);
        } else {
            EditorView.Parent parent = (EditorView.Parent) getParent();
            return (parent != null) ? parent.getViewRenderContext().getDefaultRowHeight() : 0f;
        }
    }

    @Override
    public int getRawEndOffset() {
        return rawEndOffset;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        this.rawEndOffset = rawOffset;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getStartOffset() {
        return getEndOffset() - getLength();
    }

    @Override
    public int getEndOffset() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getViewEndOffset(rawEndOffset) : rawEndOffset;
    }

    @Override
    public Document getDocument() {
        View parent = getParent();
        return (parent != null) ? parent.getDocument() : null;
    }

    @Override
    public AttributeSet getAttributes() {
        return null;
    }
    
    @NbBundle.Messages({
        "# {0} - number of lines of folded code",
        "FMT_contentSummary={0} {0,choice,0#lines|1#line|1<lines}"
    })
    private String resolvePlaceholder(String text, int at) {
        if ((options & 3) == 0) {
            return text;
        }
        Document d = getDocument();
        if (!(d instanceof BaseDocument)) {
            return null;
        }
        BaseDocument bd = (BaseDocument)d;
        CharSequence contentSeq = ""; // NOI18N
        String summary = ""; // NOI18N
        
        int mask = options;
        try {
            if ((options & 1) > 0) {
                    contentSeq = FoldContentReaders.get().readContent(
                           org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(textComponent),
                           d, 
                           fold, 
                           fold.getType().getTemplate());
                    if (contentSeq == null) {
                        mask &= ~1;
                    }
            }
            if ((options & 2) > 0) {
                int start = fold.getStartOffset();
                int end = fold.getEndOffset();
                int startLine = LineDocumentUtils.getLineIndex(bd, start);
                int endLine = LineDocumentUtils.getLineIndex(bd, end) + 1;
                
                if (endLine <= startLine + 1) {
                    mask &= ~2;
                } else {
                    summary = FMT_contentSummary((endLine - startLine));
                }
            }
        } catch (BadLocationException ex) {
        }
        if (mask == 0) {
            return text;
        }
        String replacement = NbBundle.getMessage(FoldView.class, "FMT_ContentPlaceholder_" + (mask & 3), contentSeq, summary); // NOI18N
        StringBuilder sb = new StringBuilder(text.length() + replacement.length());
        sb.append(text.subSequence(0, at));
        sb.append(replacement);
        sb.append(text.subSequence(at + FoldTemplate.CONTENT_PLACEHOLDER.length(), text.length()));
        return sb.toString();
    }

    private TextLayout getTextLayout() {
        if (collapsedTextLayout == null) {
            EditorView.Parent parent = (EditorView.Parent) getParent();
            ViewRenderContext context = parent.getViewRenderContext();
            FontRenderContext frc = context.getFontRenderContext();
            assert (frc != null) : "Null FontRenderContext"; // NOI18N
            Font font = context.getRenderFont(textComponent.getFont());
            String text = fold.getDescription();
            if (text.length() == 0) {
                text = " "; // Use single space (mainly for height measurement etc.
            }
            int placeIndex = text.indexOf(FoldTemplate.CONTENT_PLACEHOLDER);
            if (placeIndex > -1) {
                text = resolvePlaceholder(text, placeIndex);
            }
            collapsedTextLayout = new TextLayout(text, font, frc);
        }
        return collapsedTextLayout;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
//        TextLayout textLayout = getTextLayout();
//        if (textLayout == null) {
//            return alloc; // Leave given bounds
//        }
//        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
//        return bounds;
        return alloc;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        int startOffset = getStartOffset();
        return startOffset;
    }

    static TextHitInfo x2RelOffset(TextLayout textLayout, float x) {
        TextHitInfo hit;
        x -= EXTRA_MARGIN_WIDTH;
        if (x >= textLayout.getAdvance()) {
            hit = TextHitInfo.trailing(textLayout.getCharacterCount());
        } else {
            hit = textLayout.hitTestChar(x, 0); // What about backward bias -> with higher offsets it may go back visually
        }
        return hit;

    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        int startOffset = getStartOffset();
        int retOffset = -1;
        switch (direction) {
            case WEST:
                if (offset == -1) {
                    retOffset = startOffset;
                } else {
                    retOffset = -1;
                }
                break;

            case EAST:
                if (offset == -1) {
                    retOffset = startOffset;
                } else {
                    retOffset = -1;
                }
                break;

            case NORTH:
            case SOUTH:
                break;
            default:
                throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
        }
        return retOffset;
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape allocation) {
        Container container = getContainer();
        if (container instanceof JEditorPane) {
            JEditorPane editorPane = (JEditorPane) getContainer();
            JEditorPane tooltipPane = new JEditorPane();
            EditorKit kit = editorPane.getEditorKit();
            Document doc = getDocument();
            if (kit != null && doc != null) {
                Element lineRootElement = doc.getDefaultRootElement();
                tooltipPane.putClientProperty(FoldViewFactory.DISPLAY_ALL_FOLDS_EXPANDED_PROPERTY, true);
                try {
                    // Start-offset of the fold => line start => position
                    int lineIndex = lineRootElement.getElementIndex(fold.getStartOffset());
                    Position pos = doc.createPosition(
                            lineRootElement.getElement(lineIndex).getStartOffset());
                    // DocumentView.START_POSITION_PROPERTY
                    tooltipPane.putClientProperty("document-view-start-position", pos); // NOI18N
                    // End-offset of the fold => line end => position
                    lineIndex = lineRootElement.getElementIndex(fold.getEndOffset());
                    pos = doc.createPosition(lineRootElement.getElement(lineIndex).getEndOffset());
                    // DocumentView.END_POSITION_PROPERTY
                    tooltipPane.putClientProperty("document-view-end-position", pos); // NOI18N
                    tooltipPane.putClientProperty("document-view-accurate-span", true); // NOI18N
                    // Set the same kit and document
                    tooltipPane.setEditorKit(kit);
                    tooltipPane.setDocument(doc);
                    tooltipPane.setEditable(false);
                    return new FoldToolTip(editorPane, tooltipPane, getBorderColor());
                } catch (BadLocationException e) {
                    // => return null
                }
            }
        }
        return null;
    }
    
    /**
     * Border color for the placeholder view or the tooltip.
     * If not defined, falls back to the {@link #getForegroundColor()}.
     * @return Color for the border
     */
    private Color getBorderColor() {
        if (foldingBorderColors == null) {
            return getForegroundColor();
        }
        Object fgColorObj = foldingBorderColors.getAttribute(StyleConstants.Foreground);
        if (fgColorObj instanceof Color) {
            return (Color)fgColorObj;
        }
        return getForegroundColor();
    }
    
    private Color getForegroundColor() {
        if (foldingColors == null) {
            return textComponent.getForeground();
        }
        Object bgColorObj = foldingColors.getAttribute(StyleConstants.Foreground);
        if (bgColorObj instanceof Color) {
            return (Color)bgColorObj;
        } else {
            return textComponent.getForeground();
        }
    }

    private Color getBackgroundColor() {
        if (foldingColors == null) {
            return textComponent.getBackground();
        }
        int start = textComponent.getSelectionStart();
        int end = textComponent.getSelectionEnd();
        boolean partSelected = false;
        if (start != end) {
            int foldStart = fold.getStartOffset();
            int foldEnd = fold.getEndOffset();
            partSelected = start < foldEnd && end > foldStart;
        }
        Object bgColorObj = partSelected ?
                selectedColors.getAttribute(StyleConstants.Background) :
                foldingColors.getAttribute(StyleConstants.Background);
        if (bgColorObj instanceof Color) {
            return (Color)bgColorObj;
        } else {
            return textComponent.getBackground();
        }
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        if (allocBounds.intersects(clipBounds)) {
            Font origFont = g.getFont();
            Color origColor = g.getColor();
            Color origBkColor = g.getBackground();
            Shape origClip = g.getClip();
            try {
                // Leave component font
                
                g.setBackground(getBackgroundColor());

                int xInt = (int) allocBounds.getX();
                int yInt = (int) allocBounds.getY();
                int endXInt = (int) (allocBounds.getX() + allocBounds.getWidth() - 1);
                int endYInt = (int) (allocBounds.getY() + allocBounds.getHeight() - 1);
                g.setColor(getBorderColor());
                g.drawRect(xInt, yInt, endXInt - xInt, endYInt - yInt);
                
                g.setColor(getForegroundColor());
                g.clearRect(xInt + 1, yInt + 1, endXInt - xInt - 1, endYInt - yInt - 1);
                g.clip(alloc);
                TextLayout textLayout = getTextLayout();
                if (textLayout != null) {
                    EditorView.Parent parent = (EditorView.Parent) getParent();
                    float ascent = parent.getViewRenderContext().getDefaultAscent();
                    String desc = fold.getDescription(); // For empty desc a single-space text layout is returned
                    float x = (float) (allocBounds.getX() + EXTRA_MARGIN_WIDTH);
                    float y = (float) allocBounds.getY();
                    if (desc.length() > 0) {
                        
                        textLayout.draw(g, x, y + ascent);
                    }
                }
            } finally {
                g.setClip(origClip);
                g.setBackground(origBkColor);
                g.setColor(origColor);
                g.setFont(origFont);
            }
        }
    }

    @Override
    protected String getDumpName() {
        return "FV"; // NOI18N
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString(); // NOI18N
    }

}
