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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.View;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorDebug;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.FontMetricsCache;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.Mark;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.lib.EditorPackageAccessor;
import org.netbeans.modules.editor.lib.impl.MarkVector;
import org.netbeans.modules.editor.lib.impl.MultiMark;

/**
 * This class is responsible for drawing editor components. It's a singleton.
 *
 * TODO: Describe how the drawing works, all the terminology, features, constrains, etc.
 * 
 * @author Miloslav Metelka
 */
public final class DrawEngine {

    private static final Logger LOG = Logger.getLogger(DrawEngine.class.getName());
    
    /** Only one instance of draw-engine */
    private static DrawEngine drawEngine;
    
    private static final char[] SPACE = new char[] { ' ' };

    /** Prevent creation */
    private DrawEngine() {
    }

    /**
     * Gets the <code>DrawEngine</code> instance.
     * 
     * @return The draw engine.
     */
    public static synchronized DrawEngine getDrawEngine() {
        if (drawEngine == null) {
            drawEngine = new DrawEngine();
        }
        return drawEngine;
    }
    
    private void initLineNumbering(DrawInfo ctx, EditorUI eui) {
        EditorUiAccessor accessor = EditorUiAccessor.get();
        // Resolve whether line numbers will be painted
        ctx.lineNumbering = accessor.isLineNumberVisible(eui)
                            && ctx.drawGraphics.supportsLineNumbers();

        // create buffer for showing line numbers
        if (ctx.lineNumbering) {
            try {
                ctx.startLineNumber = LineDocumentUtils.getLineIndex(ctx.doc, ctx.startOffset) + 1;
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, null, e);
            }

            ctx.lineNumberColoring = accessor.getColoring(eui, FontColorNames.LINE_NUMBER_COLORING);
            if (ctx.lineNumberColoring == null) {
                ctx.lineNumberColoring = ctx.defaultColoring; // no number coloring found

            } else { // lineNumberColoring not null
                ctx.lineNumberColoring = ctx.lineNumberColoring.apply(ctx.defaultColoring);
            }

            Font lnFont = ctx.lineNumberColoring.getFont();
            if (lnFont == null) {
                lnFont = ctx.defaultColoring.getFont();
            }

            Color lnBackColor = ctx.lineNumberColoring.getBackColor();
            if (lnBackColor == null) {
                lnBackColor = ctx.defaultColoring.getBackColor();
            }

            Color lnForeColor = ctx.lineNumberColoring.getForeColor();
            if (lnForeColor == null) {
                lnForeColor = ctx.defaultColoring.getForeColor();
            }

            ctx.lineNumberChars = new char[Math.max(accessor.getLineNumberMaxDigitCount(eui), 1)];
            ctx.lineNumberWidth = accessor.getLineNumberWidth(eui);
            ctx.lineNumberDigitWidth = accessor.getLineNumberDigitWidth(eui);
            ctx.lineNumberMargin = accessor.getLineNumberMargin(eui);
            if (ctx.graphics == null) {
                ctx.syncedLineNumbering = true;

            } else { // non-synced line numbering - need to remember line start offsets
                try {
                    int endLineNumber = LineDocumentUtils.getLineIndex(ctx.doc, ctx.endOffset) + 1;
                    ctx.lineStartOffsets = new int[endLineNumber - ctx.startLineNumber + 2]; // reserve more
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
        }
    }

    private void initInfo(DrawInfo ctx, EditorUI eui) throws BadLocationException {
        if (ctx.startOffset < ctx.lineStartOffset) {
            throw new BadLocationException("Invalid startOffset: " + ctx.startOffset + " < line start offset = " + ctx.lineStartOffset, ctx.startOffset); //NOI18N
        }
        if (ctx.endOffset > ctx.lineEndOffset) {
            throw new BadLocationException("Invalid endOffset: " + ctx.endOffset + " > line end offset = " + ctx.lineEndOffset, ctx.endOffset); //NOI18N
        }

        EditorUiAccessor accessor = EditorUiAccessor.get();

        ctx.x = ctx.startX;
        ctx.y = ctx.startY;
        ctx.lineHeight = accessor.getLineHeight(eui);
        ctx.defaultColoring = accessor.getDefaultColoring(eui);
        ctx.tabSize = ctx.doc.getTabSize();
        ctx.defaultSpaceWidth = accessor.getDefaultSpaceWidth(eui);
        ctx.fragmentOffset = ctx.startOffset; // actual painting position
        ctx.graphics = ctx.drawGraphics.getGraphics();

        if (ctx.graphics != null) {
            Map<?, ?> hints = accessor.getRenderingHints(eui);
            if (hints != null) {
                ((Graphics2D)ctx.graphics).addRenderingHints(hints);
            }
        }

        ctx.textMargin = accessor.getTextMargin(eui);
        ctx.textLeftMarginWidth = accessor.getTextLeftMarginWidth(eui);
        ctx.textLimitLineVisible = accessor.getTextLimitLineVisible(eui);
        ctx.textLimitLineColor = accessor.getTextLimitLineColor(eui);
        ctx.textLimitWidth = accessor.getTextLimitWidth(eui);
        initLineNumbering(ctx, eui);

        // Initialize draw context
        ctx.foreColor = ctx.defaultColoring.getForeColor();
        ctx.backColor = ctx.defaultColoring.getBackColor();
        ctx.font = ctx.defaultColoring.getFont();
        ctx.bol = ctx.lineStartOffset == ctx.startOffset;

        // Init draw graphics
        ctx.drawGraphics.init(ctx);
        ctx.drawGraphics.setDefaultBackColor(ctx.defaultColoring.getBackColor());
        ctx.drawGraphics.setLineHeight(ctx.lineHeight);
        ctx.drawGraphics.setLineAscent(accessor.getLineAscent(eui));
        ctx.drawGraphics.setX(ctx.x);
        ctx.drawGraphics.setY(ctx.y);

        // Init all draw-layers
        ctx.layers = accessor.getDrawLayerList(eui).currentLayers();
        int layersLength = ctx.layers.length;
        ctx.layerActives = new boolean[layersLength];
        ctx.layerActivityChangeOffsets = new int[layersLength];

        for (int i = 0; i < layersLength; i++) {
            ctx.layers[i].init(ctx); // init all layers
        }

        ctx.drawMarkList = new ArrayList<DrawMark>();
        MarkVector docMarksStorage = EditorPackageAccessor.get().BaseDocument_getMarksStorage(ctx.doc);
        synchronized (docMarksStorage) {
            int offset = ctx.startOffset;
            int low = 0;
            int markCount = docMarksStorage.getMarkCount();
            int high = markCount - 1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                int cmp = docMarksStorage.getMarkOffsetInternal(mid) - offset;

                if (cmp < 0)
                    low = mid + 1;
                else if (cmp > 0)
                    high = mid - 1;
                else { // found
                    while (--mid >= 0
                        && docMarksStorage.getMarkOffsetInternal(mid) == offset
                    ) { }
                    low = mid + 1;
                    break;
                }

            }

            // the low contains first mark to consider
            offset = ctx.endOffset;
            while (low < markCount) {
                MultiMark m = docMarksStorage.getMark(low);
                if (m.isValid()) {
                    if (m.getOffset() > offset) {
                        break;
                    }

                    Mark mark = EditorPackageAccessor.get().BaseDocument_getMark(ctx.doc, m);
                    if (mark == null) {
                        throw new IllegalStateException("No mark for m=" + m); // NOI18N
                    }
                    if (mark instanceof DrawMark) {
                        ctx.drawMarkList.add((DrawMark)mark);
                    }
                }

                low++;
            }
        }
        
        
        // Get current draw mark
        ctx.drawMarkUpdate = false;
        ctx.drawMarkOffset = Integer.MAX_VALUE;
        for(ctx.drawMarkIndex = 0; ctx.drawMarkIndex < ctx.drawMarkList.size(); ) {
            ctx.drawMark = ctx.drawMarkList.get(ctx.drawMarkIndex++);
            if (ctx.drawMark.isValid()) {
                try {
                    ctx.drawMarkOffset = ctx.drawMark.getOffset();
                } catch (InvalidMarkException ime) {
                    LOG.log(Level.FINE, null, ime);
                    continue;
                }
                if (ctx.drawMarkOffset < ctx.updateOffset) {
                    ctx.updateOffset = ctx.drawMarkOffset;
                    ctx.drawMarkUpdate = true;
                }
                break;
            }
        }

        // Load the portion of text that is being rendered
        ctx.text = new Segment();
        ctx.doc.getText(ctx.lineStartOffset, ctx.lineEndOffset - ctx.lineStartOffset, ctx.text);
        
        // Set up the buffer
        ctx.textArray = ctx.text.array;
        ctx.buffer = ctx.textArray;
        
        // TODO: this needs better explanation, it's basically for translating indicies
        // pointing to ctx.text.array to document offsets; the formula is
        // index_in_ctx.text.array + ctx.bufferStartOffset -> offset_in_ctx.doc
        //
        ctx.bufferStartOffset = ctx.lineStartOffset - ctx.text.offset;
        
        ctx.drawGraphics.setBuffer(ctx.textArray);
        if (ctx.drawGraphics instanceof DrawGraphics.GraphicsDG) {
            ((DrawGraphics.GraphicsDG)ctx.drawGraphics).setBufferStartOffset(ctx.bufferStartOffset);
        }

        ctx.continueDraw = true;
        
        // Initialize the visual column, if drawing does not start at the beginning of line
        if (ctx.bol) {
            ctx.visualColumn = 0;
        } else {
            ctx.visualColumn = Analyzer.getColumn(ctx.textArray, ctx.lineStartOffset - ctx.bufferStartOffset, ctx.startOffset - ctx.lineStartOffset, ctx.tabSize, 0);
        }
    }

    private void handleBOL(DrawInfo ctx) {
        if (ctx.lineNumbering) {
            if (ctx.syncedLineNumbering) {
                // Draw line numbers synchronously at begining of each line

                // Init context
                ctx.foreColor = ctx.lineNumberColoring.getForeColor();
                ctx.backColor = ctx.lineNumberColoring.getBackColor();
                ctx.font = ctx.lineNumberColoring.getFont();
                ctx.strikeThroughColor = null;
                ctx.underlineColor = null;
                ctx.waveUnderlineColor = null;
                ctx.topBorderLineColor = null;
                ctx.rightBorderLineColor = null;
                ctx.bottomBorderLineColor = null;
                ctx.leftBorderLineColor = null;

                int lineNumber = ctx.startLineNumber + ctx.lineIndex;
                // Update line-number by layers
                int layersLength = ctx.layers.length;
                for (int i = 0; i < layersLength; i++) {
                    lineNumber = ctx.layers[i].updateLineNumberContext(lineNumber, ctx);
                }

                // Fill the buffer with digit chars
                int i = Math.max(ctx.lineNumberChars.length - 1, 0);
                do {
                    ctx.lineNumberChars[i--] = (char)('0' + (lineNumber % 10));
                    lineNumber /= 10;
                } while (lineNumber != 0 && i >= 0);

                // Fill the rest with spaces
                while (i >= 0) {
                    ctx.lineNumberChars[i--] = ' ';
                }

                // Fill the DG's attributes and draw
                int numX = ctx.x - ctx.lineNumberWidth;
                if (ctx.lineNumberMargin != null) {
                    numX += ctx.lineNumberMargin.left;
                }
                ctx.drawGraphics.setX(numX);

                ctx.drawGraphics.setBuffer(ctx.lineNumberChars);
                ctx.drawGraphics.setForeColor(ctx.foreColor);
                ctx.drawGraphics.setBackColor(ctx.backColor);
                ctx.drawGraphics.setStrikeThroughColor(ctx.strikeThroughColor);
                ctx.drawGraphics.setUnderlineColor(ctx.underlineColor);
                ctx.drawGraphics.setWaveUnderlineColor(ctx.waveUnderlineColor);
                ctx.drawGraphics.setTopBorderLineColor(ctx.topBorderLineColor);
                ctx.drawGraphics.setRightBorderLineColor(ctx.rightBorderLineColor);
                ctx.drawGraphics.setBottomBorderLineColor(ctx.bottomBorderLineColor);
                ctx.drawGraphics.setLeftBorderLineColor(ctx.leftBorderLineColor);
                ctx.drawGraphics.setFont(ctx.font);
                ctx.drawGraphics.drawChars(0, ctx.lineNumberChars.length, ctx.lineNumberWidth);

                // When printing there should be an additional space between
                // line number and the text
                if (ctx.drawGraphics.getGraphics() == null) {
                    ctx.drawGraphics.setBuffer(SPACE);
                    ctx.drawGraphics.drawChars(0, 1, ctx.lineNumberDigitWidth);
                }

                ctx.drawGraphics.setX(ctx.x);
                ctx.drawGraphics.setBuffer(ctx.textArray);

            } else { // non-synced line numbering
                ctx.lineStartOffsets[ctx.lineIndex] = ctx.fragmentOffset; // store the line number
            }
        }

        ctx.lineIndex++;
    }


    /** Handle the end-of-line */
    private void handleEOL(DrawInfo ctx) {
        ctx.drawGraphics.setX(ctx.x);
        ctx.drawGraphics.setY(ctx.y);
        
        ctx.drawGraphics.eol(); // sign EOL to DG
        
        // when printing we are asked to draw more than one line
        ctx.visualColumn = 0;
        ctx.x = ctx.startX;
        ctx.y += ctx.lineHeight;
    }


    /** Called when the current fragment starts at the offset that corresponds
    * to the update-offset.
    */
    private void updateOffsetReached(DrawInfo ctx) {
        if (ctx.drawMarkUpdate) { // update because of draw mark
            // means no-mark update yet performed
            int layersLength = ctx.layers.length;
            for (int i = 0; i < layersLength; i++) {
                DrawLayer l = ctx.layers[i];
                if (l.getName().equals(ctx.drawMark.layerName)
                        && (ctx.drawMark.isDocumentMark()
                            || ctx.editorUI == ctx.drawMark.getEditorUI())
                   ) {
                    ctx.layerActives[i] = l.isActive(ctx, ctx.drawMark);
                    int naco = l.getNextActivityChangeOffset(ctx);
                    ctx.layerActivityChangeOffsets[i] = naco;
                    if (naco > ctx.fragmentOffset && naco < ctx.layerUpdateOffset) {
                        ctx.layerUpdateOffset = naco;
                    }
                }
            }

            // Get next mark
            while(ctx.drawMarkIndex < ctx.drawMarkList.size()) {
                ctx.drawMark = ctx.drawMarkList.get(ctx.drawMarkIndex++);
                if (ctx.drawMark.isValid()) {
                    try {
                        ctx.drawMarkOffset = ctx.drawMark.getOffset();
                        break;
                    } catch (InvalidMarkException ime) {
                        LOG.log(Level.FINE, null, ime);
                        continue;
                    }
                }
            }
            
            if (ctx.drawMarkIndex >= ctx.drawMarkList.size()) { // no more draw marks
                ctx.drawMark = null;
                ctx.drawMarkOffset = Integer.MAX_VALUE;
            }

        } else { // update because activity-change-offset set in some layer
            ctx.layerUpdateOffset = Integer.MAX_VALUE;
            int layersLength = ctx.layers.length;
            for (int i = 0; i < layersLength; i++) {
                // Update only layers with the same offset as fragmentOffset
                int naco = ctx.layerActivityChangeOffsets[i];
                if (naco == ctx.fragmentOffset) {
                    DrawLayer l = ctx.layers[i];
                    ctx.layerActives[i] = l.isActive(ctx, null);
                    naco = l.getNextActivityChangeOffset(ctx);
                    ctx.layerActivityChangeOffsets[i] = naco;
                }

                if (naco > ctx.fragmentOffset && naco < ctx.layerUpdateOffset) {
                    ctx.layerUpdateOffset = naco;
                }
            }
        }

        // Check next update position
        if (ctx.drawMarkOffset < ctx.layerUpdateOffset) {
            ctx.drawMarkUpdate = true;
            ctx.updateOffset = ctx.drawMarkOffset;

        } else {
            ctx.drawMarkUpdate = false;
            ctx.updateOffset = ctx.layerUpdateOffset;
        }

    }

    /** Compute the length of the fragment. */
    private void computeFragmentLength(DrawInfo ctx) {
        // Compute initial fragment (of token) length
        ctx.fragmentStartIndex = ctx.fragmentOffset - ctx.bufferStartOffset;
        ctx.fragmentLength = Math.min(ctx.updateOffset - ctx.fragmentOffset,
                                      ctx.areaLength - ctx.drawnLength);

        // Find first TAB or LF
        int stopIndex = Analyzer.findFirstTabOrLF(ctx.textArray,
                        ctx.fragmentStartIndex, ctx.fragmentLength);

        // There must be extra EOL at the end of the document
        ctx.eol = (ctx.fragmentOffset == ctx.docLen);
        ctx.tabsFragment = false;

        // Check whether there are no tabs in the fragment and possibly shrink
        // Get the first offset of the tab character or -1 if no tabs in fragment
        if (stopIndex >= 0) { // either '\t' or '\n' found
            if (stopIndex == ctx.fragmentStartIndex) { // since fragment start
                if (ctx.textArray[stopIndex] == '\t') { // NOI18N
                    ctx.tabsFragment = true;
                    // Find first non-tab char
                    int ntInd = Analyzer.findFirstNonTab(ctx.textArray, ctx.fragmentStartIndex,
                                                         ctx.fragmentLength);

                    if (ntInd != -1) { // not whole fragment are tabs
                        ctx.fragmentLength = ntInd - ctx.fragmentStartIndex;
                    }
                } else { // '\n' found
                    ctx.eol = true;
                    ctx.fragmentLength = 1; // only one EOL in fragment
                }

            } else { // inside fragment start
                ctx.fragmentLength = stopIndex - ctx.fragmentStartIndex; // shrink fragment size
            }
        }
    }

    /** Compute the display width of the fragment */
    private void computeFragmentDisplayWidth(DrawInfo ctx) {
        // First go through all layers to update draw context
        // to get up-to-date fonts and colors
        if (!ctx.eol) { // handled later
            int layersLength = ctx.layers.length;
            for (int i = 0; i < layersLength; i++) {
                if (ctx.layerActives[i]) {
                    ctx.layers[i].updateContext(ctx); // Let the layer to update the context
                }
            }
        }

        // Handle possible white space expansion and compute display width
        FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(ctx.font);
        ctx.spaceWidth = (ctx.component != null)
            ? fmcInfo.getSpaceWidth(ctx.component) : ctx.defaultSpaceWidth;

        // Compute real count of chars in fragment - can differ if tabs
        ctx.fragmentCharCount = ctx.fragmentLength;
        if (ctx.tabsFragment) { // tabs in fragment
            ctx.fragmentCharCount = Analyzer.getColumn(ctx.textArray,
                                    ctx.fragmentStartIndex, ctx.fragmentLength, ctx.tabSize, ctx.visualColumn) - ctx.visualColumn;
            ctx.fragmentWidth = ctx.fragmentCharCount * ctx.spaceWidth;
            
        } else if (ctx.eol) { // EOL will have the spaceWidth
            ctx.fragmentWidth = ctx.spaceWidth;

        } else { // regular fragment
            if (ctx.fragmentLength > 0) {
                if (ctx.component != null) {
                    ctx.fragmentWidth = FontMetricsCache.getFontMetrics(ctx.font, ctx.component).charsWidth(
                                            ctx.textArray, ctx.fragmentStartIndex, ctx.fragmentLength);

                } else { // non-valid component
                    ctx.fragmentWidth = ctx.fragmentLength * ctx.spaceWidth;
                }

            } else {
                ctx.fragmentWidth = 0; // empty fragment
            }
        }

    }


    /** Draw the fragment. Handle the EOL in special way
    * as it needs to care about the empty-lines.
    */
    private void drawItNow(DrawInfo ctx) {
        if (ctx.eol) { // special handling for EOL
            int layersLength = ctx.layers.length;
            boolean emptyLine = false;
            int blankWidth = ctx.fragmentWidth;

            /** Need to do one or two cycles.
            * In the first pass
            * the check is performed whether the line is empty.
            * If so all the layers that extend the empty line are
            * called to update the context and the resulting half-space
            * is drawn.
            * In the second pass all the layers that extend EOL
            * are called to update the context and the resulting
            * whitespace is drawn.
            */
            do {
                blankWidth = 0;
                if (ctx.bol) { // empty line found
                    if (!emptyLine) { // not yet processed
                        for (int i = 0; i < layersLength; i++) {
                            if (ctx.layerActives[i]) {
                                DrawLayer l = ctx.layers[i];
                                if (l.extendsEmptyLine()) {
                                    emptyLine = true; // for at least one layer
                                    l.updateContext(ctx);
                                }
                            }
                        }

                        if (emptyLine) { // count only if necessary
                            blankWidth = ctx.spaceWidth / 2; // display half of char
                        }
                    } else { // already went through the cycle once for empty line
                        emptyLine = false;
                    }
                }

                if (!emptyLine) { // EOL and currently not servicing empty line
                    boolean extendEOL = false;
                    for (int i = 0; i < layersLength; i++) {
                        if (ctx.layerActives[i]) {
                            DrawLayer l = ctx.layers[i];
                            if (l.extendsEOL()) {
                                extendEOL = true; // for at least one layer
                                l.updateContext(ctx);
                            }
                        }
                    }
                    if (extendEOL && ctx.component != null) {
                        // Disable the underlines and strikethrough for EOL extension
                        ctx.drawGraphics.setStrikeThroughColor(null);
                        ctx.drawGraphics.setUnderlineColor(null);
                        ctx.drawGraphics.setWaveUnderlineColor(null);
                        ctx.drawGraphics.setTopBorderLineColor(null);
                        ctx.drawGraphics.setRightBorderLineColor(null);
                        ctx.drawGraphics.setBottomBorderLineColor(null);
                        ctx.drawGraphics.setLeftBorderLineColor(null);
                        blankWidth = ctx.component.getWidth();
                    }
                }
                
                // only do the half-char trick when really drawing, otherwise it
                // will affect measurements
                if (blankWidth > 0 && ctx.drawGraphics instanceof DrawGraphics.GraphicsDG) {
                    if (LOG.isLoggable(Level.FINE)) {
                        DrawGraphics.GraphicsDG dg = (DrawGraphics.GraphicsDG) ctx.drawGraphics;
                        String msg = "fillRect: bg = " + ctx.backColor //NOI18N
                            + ", startOffset = " + ctx.startOffset + ", endOffset = " + ctx.endOffset //NOI18N
                            + ", [" + dg.x + ", " + dg.y + ", " + blankWidth + ", " + dg.lineHeight + "]"; //NOI18N
                        LOG.fine(msg);
                    }
                    
                    ctx.drawGraphics.setBackColor(ctx.backColor);
                    ctx.drawGraphics.fillRect(blankWidth);
                    if (emptyLine && ctx.x <= ctx.textMargin.left) { //#58652
                        ctx.x += blankWidth;
                    }
                }
            } while (emptyLine);

        } else { // Draw regular fragment

            ctx.drawGraphics.setBackColor(ctx.backColor);
            ctx.drawGraphics.setForeColor(ctx.foreColor);
            ctx.drawGraphics.setStrikeThroughColor(ctx.strikeThroughColor);
            ctx.drawGraphics.setUnderlineColor(ctx.underlineColor);
            ctx.drawGraphics.setWaveUnderlineColor(ctx.waveUnderlineColor);
            ctx.drawGraphics.setTopBorderLineColor(ctx.topBorderLineColor);
            ctx.drawGraphics.setRightBorderLineColor(ctx.rightBorderLineColor);
            ctx.drawGraphics.setBottomBorderLineColor(ctx.bottomBorderLineColor);
            ctx.drawGraphics.setLeftBorderLineColor(ctx.leftBorderLineColor);
            ctx.drawGraphics.setFont(ctx.font);

            if (ctx.tabsFragment) {
                ctx.drawGraphics.drawTabs(ctx.fragmentStartIndex,
                    ctx.fragmentLength, ctx.fragmentCharCount, ctx.fragmentWidth);

            } else { // non-tabs
                ctx.drawGraphics.drawChars(ctx.fragmentStartIndex,
                    ctx.fragmentLength, ctx.fragmentWidth);
            }
        }
    }


    /** Check whether the target offset was reached. */
    private void checkTargetOffsetReached(DrawInfo ctx) {
        // Check whether at the end of the line
        if (ctx.eol
            && (ctx.targetOffset == ctx.fragmentOffset || (ctx.targetOffset == -1))
        ) {
            /** Special case for the emulating the EOL at the end of the document
            * The EOL is emulated to process the layers that extend empty-line or EOL
            */
            char ch = '\n';
            ctx.continueDraw = ctx.drawGraphics.targetOffsetReached(
                    ctx.fragmentOffset, ch, ctx.x, ctx.spaceWidth, ctx);

        // Check whether targeting all characters
        } else if (ctx.targetOffset == -1
          && ctx.fragmentLength > 0 // Not sure whether it's necessary
        ) { // When targeting all chars
            FontMetrics fm = FontMetricsCache.getFontMetrics(ctx.font, ctx.component);
            
            // Use binary search to find the right offset
            int low = -1;
            int high = ctx.fragmentLength - 1;
            
            // Cache the widths and first check whether past the end of fragment
            int lastMid = high;
            int lastWidth; // cache 
            if (ctx.tabsFragment) { // fragment contains tabs
                int spaceCount = Analyzer.getColumn(ctx.textArray,
                    ctx.fragmentStartIndex, high, ctx.tabSize, ctx.visualColumn)
                        - ctx.visualColumn;
                lastWidth = spaceCount * ctx.spaceWidth;

            } else { // no tabs inside fragment
                lastWidth = fm.charsWidth(ctx.textArray, ctx.fragmentStartIndex,  high);
            }

            int lastWidthP1; // plus one char
            if (ctx.tabsFragment) { // fragment contains tabs
                int spaceCount = Analyzer.getColumn(ctx.textArray,
                    ctx.fragmentStartIndex, ctx.fragmentLength, ctx.tabSize, ctx.visualColumn)
                        - ctx.visualColumn;
                lastWidthP1 = spaceCount * ctx.spaceWidth;

            } else { // no tabs inside fragment
                lastWidthP1 = fm.charsWidth(ctx.textArray, ctx.fragmentStartIndex,  ctx.fragmentLength);
            }

            // Test whether the end of the fragment is accepted
            ctx.continueDraw = ctx.drawGraphics.targetOffsetReached(
                ctx.fragmentOffset + high, ctx.textArray[ctx.fragmentStartIndex + high],
                ctx.x + lastWidth, lastWidthP1 - lastWidth, ctx
            );
            
            if (!ctx.continueDraw) {
                // Binary search of the first offset that returns false follows
                while (low <= high) {
                    int mid =(low + high) / 2;

                    // Compute width that will be passed as x coordinate
                    int width = 0;
                    if (mid == lastMid + 1) { // try to use cached value
                        width = lastWidthP1;

                    } else {
                        if (ctx.tabsFragment) { // fragment contains tabs
                            int spaceCount = Analyzer.getColumn(ctx.textArray,
                                ctx.fragmentStartIndex, mid, ctx.tabSize, ctx.visualColumn)
                                - ctx.visualColumn;
                            width = spaceCount * ctx.spaceWidth;

                        } else { // no tabs inside fragment
                            width = fm.charsWidth(ctx.textArray, ctx.fragmentStartIndex,  mid);
                        }
                    }

                    // Compute width plus one char and substract the previous width
                    // to get the width of the char
                    int widthP1 = 0;
                    if (mid == lastMid - 1) { // try to use cached value
                        widthP1 = lastWidth;

                    } else {
                        if (ctx.tabsFragment) { // fragment contains tabs
                            int spaceCount = Analyzer.getColumn(ctx.textArray,
                                ctx.fragmentStartIndex, mid + 1, ctx.tabSize, ctx.visualColumn)
                                - ctx.visualColumn;
                            widthP1 = spaceCount * ctx.spaceWidth;

                        } else { // no tabs inside fragment
                            widthP1 = fm.charsWidth(ctx.textArray, ctx.fragmentStartIndex,  mid + 1);
                        }
                    }

                    lastWidth = width;
                    lastWidthP1 = widthP1;
                    lastMid = mid;

                    ctx.continueDraw = ctx.drawGraphics.targetOffsetReached(
                        ctx.fragmentOffset + mid, ctx.textArray[ctx.fragmentStartIndex + mid],
                        ctx.x + width, widthP1 - width, ctx
                    );

                    if (ctx.continueDraw) {
                        low = mid + 1;
                    } else {
                        //Bug #34130, search should not end when mid == low + 1
                        if (mid > low && mid != high) { 
                            high = mid; // last that rejected
                        } else {
                            break;
                        }
                    }
                }
            }
            


        // Check whether target offset is inside current fragment
        } else if (ctx.targetOffset < ctx.fragmentOffset + ctx.fragmentLength
                   && ctx.fragmentOffset <= ctx.targetOffset
        ) {
            int curWidth;
            int prevWidth = 0;
            int i = (ctx.targetOffset - ctx.fragmentOffset);

            if (i > 0) {
                if (ctx.tabsFragment) { // fragment contains tabs
                    int spaceCount = Analyzer.getColumn(ctx.textArray,
                        ctx.fragmentStartIndex, i, ctx.tabSize, ctx.visualColumn)
                            - ctx.visualColumn;
                    prevWidth = spaceCount * ctx.spaceWidth;

                } else { // no tabs inside fragment
                    prevWidth = FontMetricsCache.getFontMetrics(ctx.font,
                        ctx.component).charsWidth(ctx.textArray, ctx.fragmentStartIndex, i);
                }
            }

            if (ctx.tabsFragment) { // fragment contains tabs
                int spaceCount = Analyzer.getColumn(ctx.textArray,
                    ctx.fragmentStartIndex, i + 1, ctx.tabSize, ctx.visualColumn)
                        - ctx.visualColumn;
                curWidth = spaceCount * ctx.spaceWidth;

            } else { // no tabs inside fragment
                curWidth = FontMetricsCache.getFontMetrics(ctx.font,
                           ctx.component).charsWidth(ctx.textArray, ctx.fragmentStartIndex, i + 1);
            }

            ctx.continueDraw = ctx.drawGraphics.targetOffsetReached(
                ctx.fragmentOffset + i, ctx.textArray[ctx.fragmentStartIndex + i],
                ctx.x + prevWidth, curWidth - prevWidth, ctx);
        }
    }

    private void drawFragment(DrawInfo ctx) {
        // Fill in the draw context
        ctx.foreColor = ctx.defaultColoring.getForeColor();
        ctx.backColor = ctx.defaultColoring.getBackColor();
        ctx.font = ctx.defaultColoring.getFont();
        ctx.strikeThroughColor = null;
        ctx.underlineColor = null;
        ctx.waveUnderlineColor = null;
        ctx.topBorderLineColor = null;
        ctx.rightBorderLineColor = null;
        ctx.bottomBorderLineColor = null;
        ctx.leftBorderLineColor = null;

        if (ctx.bol) { // if we are on the line begining
            Color fg = ctx.foreColor;
            Color bg = ctx.backColor;
            handleBOL(ctx);
            ctx.foreColor = fg;
            ctx.backColor = bg;
        }

        // Check for status updates in planes at the begining of this fragment
        while (ctx.fragmentOffset == ctx.updateOffset) {
            updateOffsetReached(ctx); // while() because can be more marks at same pos
        }

        // Compute the length of the fragment
        computeFragmentLength(ctx);

        // Compute the display width of the fragment
        computeFragmentDisplayWidth(ctx);

        // Draw the fragment
        drawItNow(ctx);

        if (LOG.isLoggable(Level.FINER)) {
            String msg = "   FRAGMENT='" // NOI18N
                    + EditorDebug.debugChars(ctx.buffer, ctx.fragmentStartIndex, ctx.fragmentLength)
                    + "', at offset=" + ctx.fragmentOffset // NOI18N
                    + ", bol=" + ctx.bol + ", eol=" + ctx.eol; // NOI18N
            LOG.finer(msg);
        }

        // Check whether target-offset was reached
        if (ctx.component != null) {
            checkTargetOffsetReached(ctx);
        }

        // Move the variables to the next fragment in token
        ctx.fragmentOffset += ctx.fragmentLength;
        ctx.drawnLength += ctx.fragmentLength;
        ctx.visualColumn += ctx.fragmentCharCount;
        ctx.x += ctx.fragmentWidth;
        ctx.bol = false;

        // Update coordinates at the end of each line
        if (ctx.eol) {
            handleEOL(ctx);
            ctx.bol = true; // now at BOL
            ctx.eol = false;
        }

        // This is tricky, but we have to stop drawing when at the end of the area
        // unless the area spans behind the end of the document. Note there is
        // one more newline behind the doc.getLength().
        if (ctx.fragmentOffset >= ctx.endOffset && ctx.endOffset <= ctx.docLen) {
            ctx.continueDraw = false;
        }
    }
    
    private void drawArea(DrawInfo ctx) {
        ctx.areaOffset = ctx.startOffset;
        ctx.areaLength = ctx.endOffset - ctx.startOffset;

        // Ask all the contexts first to possibly find out
        // the first fragment length
        ctx.layerUpdateOffset = Integer.MAX_VALUE;
        int layersLength = ctx.layers.length;
        for (int i = 0; i < layersLength; i++) { // update status of all layers
            DrawLayer l = ctx.layers[i];
            ctx.layerActives[i] = l.isActive(ctx, null);

            int naco = l.getNextActivityChangeOffset(ctx);
            ctx.layerActivityChangeOffsets[i] = naco;
            if (naco > ctx.fragmentOffset && naco < ctx.layerUpdateOffset) {
                ctx.layerUpdateOffset = naco;
            }
        }
        ctx.updateOffset = Math.min(ctx.layerUpdateOffset, ctx.drawMarkOffset);

        ctx.drawnLength = 0;
        ctx.fragmentLength = 0; // length of current token fragment

        // Process all the fragments of one token
        do {
            drawFragment(ctx);
        } while (ctx.continueDraw && ctx.drawnLength < ctx.areaLength);

        if (ctx.continueDraw) {
            // at the end of the document
            ctx.areaOffset = ctx.fragmentOffset;
            ctx.areaLength = ctx.docLen - ctx.areaOffset;
            ctx.drawnLength = 0;
            drawFragment(ctx);
            ctx.continueDraw = false;
        }
    }

    private void drawTheRestOfTextLine(DrawInfo ctx) {
        handleEOL(ctx);

        if (ctx.textLimitLineVisible && ctx.textLimitWidth > 0) { // draw limit line
            int lineX = ctx.textMargin.left + ctx.textLimitWidth * ctx.defaultSpaceWidth;
            if (ctx.graphics !=null){
                ctx.graphics.setColor(ctx.textLimitLineColor);
                Rectangle clip = ctx.graphics.getClipBounds();
                if (clip.height>ctx.lineHeight){
                    ctx.graphics.drawLine(lineX, ctx.y, lineX, ctx.y+clip.height);
                }
            }
        }
    }
    
    private void graphicsSpecificUpdates(DrawInfo ctx, EditorUI eui) {
        EditorUiAccessor accessor = EditorUiAccessor.get();
        Rectangle bounds = accessor.getExtentBounds(eui);
        Rectangle clip = ctx.graphics.getClipBounds();
        Insets textMargin = ctx.textMargin;
        int leftMarginWidth = textMargin.left - ctx.lineNumberWidth - ctx.textLeftMarginWidth;

        // Draw line numbers bar and all the line nummbers
        if (ctx.lineNumbering && !ctx.syncedLineNumbering) {
            Color lnBackColor = ctx.lineNumberColoring.getBackColor();
            int numY = ctx.startY;
            int lnBarX = bounds.x + leftMarginWidth;
            if (!lnBackColor.equals(ctx.defaultColoring.getBackColor()) || bounds.x > 0) {
                ctx.graphics.setColor(lnBackColor);
                ctx.graphics.fillRect(lnBarX, numY, ctx.lineNumberWidth,
                                      ctx.lineIndex * ctx.lineHeight); // can't use dg because of height
            }

            ctx.drawGraphics.setDefaultBackColor(lnBackColor); // will paint into bar

            int lastDigitInd = Math.max(ctx.lineNumberChars.length - 1, 0);
            int numX = lnBarX;
            if (ctx.lineNumberMargin != null) {
                numX += ctx.lineNumberMargin.left;
            }

            ctx.bol = true; //
            for (int j = 0; j < ctx.lineIndex; j++) { // draw all line numbers

                // Init the context
                ctx.fragmentOffset = ctx.lineStartOffsets[j];
                ctx.foreColor = ctx.lineNumberColoring.getForeColor();
                ctx.backColor = lnBackColor;
                ctx.font = ctx.lineNumberColoring.getFont();
                ctx.strikeThroughColor = null;
                ctx.underlineColor = null;
                ctx.waveUnderlineColor = null;
                ctx.topBorderLineColor = null;
                ctx.rightBorderLineColor = null;
                ctx.bottomBorderLineColor = null;
                ctx.leftBorderLineColor = null;

                int lineNumber = ctx.startLineNumber + j;
                // Update line-number by layers
                int layersLength = ctx.layers.length;
                for (int i = 0; i < layersLength; i++) {
                    lineNumber = ctx.layers[i].updateLineNumberContext(lineNumber, ctx);
                }

                int i = lastDigitInd;
                // Fill in the digit chars
                do {
                    ctx.lineNumberChars[i--] = (char)('0' + (lineNumber % 10));
                    lineNumber /= 10;
                } while (lineNumber != 0 && i >= 0);
                // Fill in the spaces
                while (i >= 0) {
                    ctx.lineNumberChars[i--] = ' ';
                }

                ctx.drawGraphics.setY(numY);
                ctx.drawGraphics.setBuffer(ctx.lineNumberChars);
                ctx.drawGraphics.setForeColor(ctx.foreColor);
                ctx.drawGraphics.setBackColor(ctx.backColor);
                ctx.drawGraphics.setStrikeThroughColor(ctx.strikeThroughColor);
                ctx.drawGraphics.setUnderlineColor(ctx.underlineColor);
                ctx.drawGraphics.setWaveUnderlineColor(ctx.waveUnderlineColor);
                ctx.drawGraphics.setTopBorderLineColor(ctx.topBorderLineColor);
                ctx.drawGraphics.setRightBorderLineColor(ctx.rightBorderLineColor);
                ctx.drawGraphics.setBottomBorderLineColor(ctx.bottomBorderLineColor);
                ctx.drawGraphics.setLeftBorderLineColor(ctx.leftBorderLineColor);
                ctx.drawGraphics.setFont(ctx.font);

                ctx.drawGraphics.setX(lnBarX);
                ctx.drawGraphics.fillRect(ctx.lineNumberWidth);
                ctx.drawGraphics.setX(numX);

                ctx.drawGraphics.drawChars(0, ctx.lineNumberChars.length,
                    ctx.lineNumberChars.length * ctx.lineNumberDigitWidth);
                
                ctx.drawGraphics.setBuffer(null); // will do changes in buffer

                numY += ctx.lineHeight;
            }
        }

        // Clear margins
        ctx.graphics.setColor(ctx.defaultColoring.getBackColor());
    }

    /** 
     * Draw on the specified area. Simply calls the other <code>draw</code> method
     * passing in <code>null</code> for the <code>view</code> parameter.
     * 
     * @param drawGraphics draw graphics through which the drawing is done
     * @param editorUI extended UI to use
     * @param startOffset position from which the drawing starts.
     *  It must BOL of the first line to be drawn.
     * @param endOffset position where the drawing stops.
     *  It must be EOL of the last line to be drawn.
     * @param startX x-coordinate at which the drawing starts
     * @param startY x-coordinate at which the drawing starts
     * @param targetOffset position where the targetOffsetReached() method
     *   of drawGraphics is called. This is useful for caret update or modelToView.
     *   The Integer.MAX_VALUE can be passed to ignore that behavior. The -1 value
     *   has special meaning there so that it calls targetOffsetReached() after each
     *   character processed. This is used by viewToModel to find the position
     *   for some point.
     * 
     * @throws javax.swing.text.BadLocationException 
     */
    public final void draw(DrawGraphics drawGraphics, EditorUI editorUI, int startOffset, int endOffset,
              int startX, int startY, int targetOffset) throws BadLocationException {
                  
        draw(null, drawGraphics, editorUI, startOffset, endOffset,
            startX, startY, targetOffset);
    }

    /** 
     * Draw on the specified area.
     * 
     * @param view The view to use for drawing, if <code>null</code> the whole
     *   document is used.
     * @param drawGraphics draw graphics through which the drawing is done
     * @param editorUI extended UI to use
     * @param startOffset position from which the drawing starts.
     *  It must BOL of the first line to be drawn.
     * @param endOffset position where the drawing stops.
     *  It must be EOL of the last line to be drawn.
     * @param startX x-coordinate at which the drawing starts
     * @param startY x-coordinate at which the drawing starts
     * @param targetOffset position where the targetOffsetReached() method
     *   of drawGraphics is called. This is useful for caret update or modelToView.
     *   The Integer.MAX_VALUE can be passed to ignore that behavior. The -1 value
     *   has special meaning there so that it calls targetOffsetReached() after each
     *   character processed. This is used by viewToModel to find the position
     *   for some point.
     * 
     * @throws javax.swing.text.BadLocationException 
     */
    public final void draw(View view, DrawGraphics drawGraphics, EditorUI editorUI, int startOffset, int endOffset,
              int startX, int startY, int targetOffset) throws BadLocationException
    {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("draw-start: [" + startX + ", " + startY + "] --------------------"); //NOI18N
        }
        try {
            drawInternal(view, drawGraphics, editorUI, startOffset, endOffset, startX, startY, targetOffset);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("draw-end: [" + startX + ", " + startY + "] --------------------"); //NOI18N
            }
        }
    }
    
    private final void drawInternal(View view, DrawGraphics drawGraphics, EditorUI editorUI, int startOffset, int endOffset,
              int startX, int startY, int targetOffset) throws BadLocationException
    {
        assert drawGraphics != null : "The parameter 'drawGraphics' is null."; //NOI18N
        assert editorUI != null : "The parameter 'editorUI' is null."; //NOI18N
        
        final Document doc = ((view != null) ? view.getDocument() : editorUI.getDocument());
        assert doc instanceof BaseDocument : "No BaseDocument available, doc = " + doc; //NOI18N
        
        // Some correctness tests at the begining
        if (startOffset < 0 || endOffset < 0 || startOffset > endOffset || startX < 0 || startY < 0 ||
            endOffset > doc.getLength() + 1 || 
            (targetOffset != -1 && targetOffset != Integer.MAX_VALUE && (targetOffset < startOffset || targetOffset > endOffset))
        ) {
            String msg = "Invalid parameters: startOffset = " + startOffset //NOI18N
                + ", endOffset = " + endOffset + ", targetOffset = " + targetOffset
                + ", startX = " + startX + ", startY = " + startY //NOI18N
                + ", docLen = " + doc.getLength(); //NOI18N
            throw new BadLocationException(msg, Integer.MIN_VALUE);
        }

        if (LOG.isLoggable(Level.FINER)) {
            javax.swing.text.Element lineRoot = ((BaseDocument) doc).getParagraphElement(0).getParentElement();
            int startLine = lineRoot.getElementIndex(startOffset);
            int startLineOffset = lineRoot.getElement(startLine).getStartOffset();
            int endLine = lineRoot.getElementIndex(endOffset);
            int endLineOffset = lineRoot.getElement(endLine).getStartOffset();
            Graphics g = drawGraphics.getGraphics();
            
            String msg = "---------- DRAWING startOffset=" //NOI18N
                + startOffset + ", startLine=" + startLine // NOI18N
                + "(o=" + startLineOffset + "), endOffset=" + endOffset // NOI18N
                + ", endLine=" + endLine + "(o=" + endLineOffset // NOI18N
                + "), targetOffset = " + targetOffset //NOI18N
                + ", clip=" + ((g != null) ? g.getClipBounds().toString() : "null") // NOI18N
                + "  ------------------"; // NOI18N
            LOG.finer(msg);
        }

        synchronized (editorUI) { // lock operations manipulating draw layer chain

            // Create draw-info and initialize basic things
            DrawInfo ctx = new DrawInfo();
            ctx.doc = (BaseDocument) doc;
            ctx.component = editorUI.getComponent();
            ctx.editorUI = editorUI;

            ctx.drawGraphics = drawGraphics;
            ctx.drawGraphics.setView(view);
            ctx.view = view;
            ctx.startX = startX;
            ctx.startY = startY;
            
            // Lock the document
            ctx.doc.readLock();
            try {
                ctx.docLen = ctx.doc.getLength();
                
                ctx.startOffset = startOffset;
                ctx.endOffset = endOffset;
                ctx.targetOffset = targetOffset;
                if (ctx.view != null) {
                    assert ctx.view instanceof DrawEngineLineView : "DrawEngine should only be accessed from DrawEngineLineView"; //NOI18N
                    Element line = ctx.doc.getParagraphElement(ctx.startOffset);
                    ctx.lineStartOffset = line.getStartOffset();
                    ctx.lineEndOffset = line.getEndOffset();
                } else {
                    ctx.lineStartOffset = ctx.startOffset;
                    ctx.lineEndOffset = ctx.endOffset;
                }

                /* Correct the ending position to be at the begining
                 * of the next line. The only exception is when the
                 * endOffset is equal to docLen.
                 */
                if (ctx.endOffset < ctx.lineEndOffset) {
                    ctx.endOffset++;
                } else if (ctx.endOffset == ctx.docLen) {
                    ctx.endOffset++;
                    ctx.lineEndOffset++;
                }

                // Initialize the draw-info
                initInfo(ctx, editorUI);

                // Draw the area
                drawArea(ctx);
                
                // Draw the text limit line till the end of visible editor area
                if (ctx.endOffset >= ctx.docLen) {
                    drawTheRestOfTextLine(ctx);
                }

                // When drawing to graphics, the line numbers and insets will be drawn now
                if (ctx.graphics != null) {
                    graphicsSpecificUpdates(ctx, editorUI);
                }
            } finally {
                ctx.doc.readUnlock();
                
                // Additional cleanup
                ctx.drawGraphics.setBuffer(null);
                ctx.drawGraphics.finish();
            }
        } // synchronized on editorUI
    }


    static class DrawInfo implements DrawContext {

        // DrawContext -----------------------------------------------
        /** Current foreground color. */
        Color foreColor;

        /** Current background color. */
        Color backColor;

        /** Current background color. */
        Color underlineColor;

        /** Current background color. */
        Color waveUnderlineColor;

        /** Color of the strike-through line or null. */
        Color strikeThroughColor;

        Color topBorderLineColor;
        Color rightBorderLineColor;
        Color bottomBorderLineColor;
        Color leftBorderLineColor;
        
        /** Current font. */
        Font font;

        /** Starting position of the drawing. */
        int startOffset;

        /** Ending position of the drawing. */
        int endOffset;

        int lineStartOffset;
        int lineEndOffset;
        
        /** Whether we are currently at the line begining. */
        boolean bol;

        /** Whether we are currently at the line end. */
        boolean eol;

        /** Editor-UI of the component for which we are drawing. */
        EditorUI editorUI;

        /** Buffer from which the chars are being drawn. */
        char[] buffer;

        /** Starting poisition of the buffer inside the document. */
        int bufferStartOffset;

        /** The position in a document of the fragment being drawn. */
        int fragmentOffset;

        /** The length of the fragment. */
        int fragmentLength;

        // Other variables ---------------------------------------------

        /** The position in a document of the area being drawn. */
        int areaOffset;
        
        /** The lenght of the area being drawn. */
        int areaLength;

        /** Draw graphics */
        DrawGraphics drawGraphics;

        /** Target document position for the drawing or -1 if all the positions
        * are the potential targets.
        */
        int targetOffset;

        /** Text of the document being used by drawing. */
        Segment text;

        /** Char array in the text segment. */
        char[] textArray;

        /** View being painted */
        View view;

        /** Component being painted */
        JTextComponent component;

        /** Document of the component. */
        BaseDocument doc;

        /** Current length of the document */
        int docLen;

        /** Current visual column. */
        int visualColumn;

        /** Current x-coordinate. */
        int x;

        /** Current y-coordinate. */
        int y;

        /** Starting x-coordinate. */
        int startX;

        /** Starting y-coordinate. */
        int startY;

        /** Height of the line being drawn. */
        int lineHeight;

        /** Default coloring of the component. */
        Coloring defaultColoring;

        /** Size of the TAB character being drawn. */
        int tabSize;

        /** Whether the draw should continue or not. */
        boolean continueDraw;

        /** Line number of the first painted line. */
        int startLineNumber;

        /** Index of the line being drawn. It is added
        * to the startLineNumber to form the resulting line number.
        */
        int lineIndex;

        /** Array of the start positions of all the lines drawn. */
        int[] lineStartOffsets;

        /** Characters forming the line-number. It is reused for drawing
        * all the lines.
        */
        char[] lineNumberChars;

        /** Coloring for the line-number. */
        Coloring lineNumberColoring;

        int lineNumberWidth;
        int lineNumberDigitWidth;
        Insets lineNumberMargin;

        /** Graphics object. It can be null when not drawing to the component. */
        Graphics graphics;

        /** Whether line-numbers are visible and allowed by the draw-graphics. */
        boolean lineNumbering;

        /** Whether the line-numbers should be painted after each is painted.
        * By default the line-numbers are drawn as one block at the end
        * of the drawing.
        */
        boolean syncedLineNumbering;

        /** Array of the draw-layers to be used in the painting */
        DrawLayer[] layers;

        /** Whether the particular layer is currently active or not. */
        boolean[] layerActives;

        /** Next position where the layer will be asked whether it's active
        * or not.
        */
        int[] layerActivityChangeOffsets;

        /** Position where either the next draw-mark is
        * or which matches the activity-change-offset of one or more layers.
        */
        int updateOffset;

        /** Next activity-change-offset of one or more layers. */
        int layerUpdateOffset;

        /** Update of the layers because of the draw-mark is at the given position.
        * False means the update is because the activity-change-offset
        * was reached.
        */
        boolean drawMarkUpdate;

        /** List of draw marks in the painted area. */
        List<DrawMark> drawMarkList;
        
        /** Current index in the drawMarkList */
        int drawMarkIndex;

        /** Current draw-mark */
        DrawMark drawMark;

        /** Position of the current draw-mark */
        int drawMarkOffset;

        /** Length of the current token that was already drawn. */
        int drawnLength;

        /** Offset of the fragment starting character in the buffer */
        int fragmentStartIndex;

        /** Whether the fragment contains TABs only. */
        boolean tabsFragment;

        /** Width of one space character for the current context font. */
        int spaceWidth;
        int defaultSpaceWidth;

        /** Display width of the fragment */
        int fragmentWidth;

        /** Number of characters that the fragment stands for. It can differ
        * from fragmentLength for tabsFragment.
        */
        int fragmentCharCount;

        Insets textMargin;
        int textLeftMarginWidth;

        boolean textLimitLineVisible;
        Color textLimitLineColor;
        int textLimitWidth;
        
        public Color getForeColor() {
            return foreColor;
        }

        public void setForeColor(Color foreColor) {
            this.foreColor = foreColor;
        }

        public Color getBackColor() {
            return backColor;
        }

        public void setBackColor(Color backColor) {
            this.backColor = backColor;
        }

        public Color getUnderlineColor() {
            return underlineColor;
        }

        public void setUnderlineColor(Color underlineColor) {
            this.underlineColor = underlineColor;
        }

        public Color getWaveUnderlineColor() {
            return waveUnderlineColor;
        }

        public void setWaveUnderlineColor(Color waveUnderlineColor) {
            this.waveUnderlineColor = waveUnderlineColor;
        }

        public Color getStrikeThroughColor() {
            return strikeThroughColor;
        }

        public void setStrikeThroughColor(Color strikeThroughColor) {
            this.strikeThroughColor = strikeThroughColor;
        }

        public Color getTopBorderLineColor() {
            return topBorderLineColor;
        }

        public void setTopBorderLineColor(Color topBorderLineColor) {
            this.topBorderLineColor = topBorderLineColor;
        }

        public Color getRightBorderLineColor() {
            return rightBorderLineColor;
        }

        public void setRightBorderLineColor(Color rightBorderLineColor) {
            this.rightBorderLineColor = rightBorderLineColor;
        }

        public Color getBottomBorderLineColor() {
            return bottomBorderLineColor;
        }

        public void setBottomBorderLineColor(Color bottomBorderLineColor) {
            this.bottomBorderLineColor = bottomBorderLineColor;
        }

        public Color getLeftBorderLineColor() {
            return leftBorderLineColor;
        }

        public void setLeftBorderLineColor(Color leftBorderLineColor) {
            this.leftBorderLineColor = leftBorderLineColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public boolean isBOL() {
            return bol;
        }

        public boolean isEOL() {
            return eol;
        }

        public EditorUI getEditorUI() {
            return editorUI;
        }

        public char[] getBuffer() {
            return buffer;
        }

        public int getBufferStartOffset() {
            return bufferStartOffset;
        }

        public TokenID getTokenID() {
            return null; // XXX: return tokenID;
        }

        public TokenContextPath getTokenContextPath() {
            return null; // XXX: return tokenContextPath;
        }

        public int getTokenOffset() {
            return areaOffset; // XXX: return tokenOffset;
        }

        public int getTokenLength() {
            return areaLength; // XXX: return tokenLength;
        }

        public int getFragmentOffset() {
            return fragmentOffset;
        }

        public int getFragmentLength() {
            return fragmentLength;
        }

    } // End of DrawInfo class
    
}
