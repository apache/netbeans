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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.editor.view.spi.ViewLayoutState;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.view.GapDocumentView;

/**
 * Line view implementation. It works over LineElement and 
 * delegates drawing to DrawEngine.
 *
 * @author  Martin Roskanin
 */
public class DrawEngineLineView extends View implements ViewLayoutState, EstimatedSpanView {

    private static final Logger LOG = Logger.getLogger(DrawEngineLineView.class.getName());
    private static final boolean loggable = LOG.isLoggable(Level.FINEST);
    private static final long PERF_TRESHOLD = Long.getLong("DrawEngineLineView.PERF_TRESHOLD", -1); //NOI18N, log events lasting longer then PERF_TRASHOLD msec
    
    /**
     * Bit that indicates whether x is the major axis.
     */
    private static final int X_MAJOR_AXIS_BIT = 1;
    
    /**
     * Bit that indicates that the major axis info is valid.
     */
    private static final int MAJOR_AXIS_PREFERENCE_CHANGED_BIT = 2;

    /**
     * Bit that indicates that the minor axis info is valid.
     */
    private static final int MINOR_AXIS_PREFERENCE_CHANGED_BIT = 4;
    
    /**
     * Bit that indicates that size of the view is valid.
     */
    private static final int VIEW_SIZE_INVALID_BIT = 8;
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether there is a pending layout update scheduled
     * for this layout state.
     */
    private static final int UPDATE_LAYOUT_PENDING_BIT = 16;
    
    private static final int ESTIMATED_SPAN_BIT = 32;

    protected static final int LAST_USED_BIT = ESTIMATED_SPAN_BIT;

    /**
     * Bit composition being used to test whether 
     * the layout is up-to-date or not.
     */
    private static final int ANY_INVALID
        = MAJOR_AXIS_PREFERENCE_CHANGED_BIT
        | MINOR_AXIS_PREFERENCE_CHANGED_BIT
        | VIEW_SIZE_INVALID_BIT;


    private int statusBits; // 4 bytes

    private int viewRawIndex; // 8 bytes

    private double layoutMajorAxisRawOffset; // double => 16 bytes

    // major axis
    private float layoutMajorAxisPreferredSpan; // 20 bytes
    
    // minor axis
    private float layoutMinorAxisPreferredSpan; // 24 bytes
    
    
    /** Draw graphics for converting position to coords */
    //ModelToViewDG modelToViewDG; // 28 bytes
    
    /** Draw graphics for converting coords to position */
    private ViewToModelDG viewToModelDG; // 32 bytes
    

    public DrawEngineLineView(Element elem) {
        super(elem);
    }
    
    private int getBaseX(int orig) {
        return orig + getEditorUI().getTextMargin().left;
    }
    
    private JTextComponent getComponent() {
        return (JTextComponent)getContainer();
    }
    
    private BaseTextUI getBaseTextUI(){
        return (BaseTextUI)getComponent().getUI();
    }
    
    private EditorUI getEditorUI(){
        return getBaseTextUI().getEditorUI();
    }
    
    private ModelToViewDG getModelToViewDG() {
        /* fix of issue #55419
        if (modelToViewDG == null) {
            modelToViewDG = new ModelToViewDG();
        }
        return modelToViewDG;
         */
        return new ModelToViewDG();
    }
    
    private ViewToModelDG getViewToModelDG() {
        if (viewToModelDG == null) {
            viewToModelDG = new ViewToModelDG();
        }
        return viewToModelDG;
    }
    
    public boolean isEstimatedSpan() {
        return isStatusBitsNonZero(ESTIMATED_SPAN_BIT);
    }
    
    public void setEstimatedSpan(boolean estimatedSpan) {
        if (isEstimatedSpan() != estimatedSpan) { // really changed
            if (estimatedSpan) {
                setStatusBits(ESTIMATED_SPAN_BIT);
            } else { // changing from true to false
                clearStatusBits(ESTIMATED_SPAN_BIT);

                getParent().preferenceChanged(this, true, true);
            }
        }
    }
    
    protected boolean isFragment(){
        return false;
    }
    
    /**
     * Get the offset prior to ending '\n' in the corresponding line element.
     */
    private int getEOLffset(){
        return super.getEndOffset() - 1; // offset prior to ending '\n'
    }
    
    /**
     * Get either the EOL offset or the end of the fragment
     * if the fragment is inside the view.
     */
    private int getAdjustedEOLOffset() {
        return Math.min(getEndOffset(), getEOLffset());
    }
    
    public @Override void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        resetMarkers(e.getOffset());
        preferenceChanged(this, true, false);
    }
    
    public @Override void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        resetMarkers(e.getOffset());
        preferenceChanged(this, true, false);
    }
    
    public @Override float getAlignment(int axis) {
	return 0f;
    }
    
    public void paint(Graphics g, Shape a) {
        if (!(getDocument() instanceof BaseDocument)) return; //#48134
        // When painting make sure the estimated span is set to false
        setEstimatedSpan(false);
        // No modifications to allocReadOnly variable!
        Rectangle allocReadOnly = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        int startOffset = getStartOffset();
        int endOffset = getAdjustedEOLOffset();
        try{
            if (isFragment()){
                Rectangle oldClipRect = g.getClipBounds();                
                Rectangle newClip = new Rectangle(oldClipRect);
                Rectangle startOffsetClip = modelToView(startOffset, a, Position.Bias.Forward).getBounds();
                Rectangle endOffsetClip = modelToView(endOffset, a, Position.Bias.Forward).getBounds();
                View parent = getParent();
                if (parent instanceof FoldMultiLineView && !equals(parent.getView(parent.getViewCount() - 1))) {
                    newClip.width = Math.min(oldClipRect.width, endOffsetClip.x);
                    
                    if (newClip.width + newClip.x > endOffsetClip.x) {
                        newClip.width = newClip.width - (newClip.width + newClip.x - endOffsetClip.x);
                    }
                    
                    g.setClip(newClip);
                }

                int shift = startOffsetClip.x - getEditorUI().getTextMargin().left - allocReadOnly.x;
                g.translate(-shift,0);
                
                DrawEngine.getDrawEngine().draw(this, new DrawGraphics.GraphicsDG(g),
                getEditorUI(), startOffset, endOffset,
                getBaseX(allocReadOnly.x), allocReadOnly.y, Integer.MAX_VALUE);
                
                g.translate(shift,0);                
                g.setClip(oldClipRect);

            }else{
                JTextComponent component = getComponent();
                if (component!=null){
                    long ts1 = 0, ts2 = 0;
                    if (loggable) {
                        ts1 = System.currentTimeMillis();
                    }
                    
                    // Translate the graphics clip region to the document offsets
                    // and their view region
                    Rectangle clip = g.getClipBounds();
                    int fromOffset = viewToModel(clip.x, clip.y, allocReadOnly, null);
                    int toOffset = viewToModel(clip.x + clip.width, clip.y, allocReadOnly, null);
                    
                    fromOffset = Math.max(fromOffset - 1, getStartOffset());
                    toOffset = Math.min(toOffset + 1, getAdjustedEOLOffset());
                    
                    Rectangle rr = modelToView(fromOffset, allocReadOnly, Position.Bias.Forward).getBounds();
                    
                    DrawEngine.getDrawEngine().draw(this, new DrawGraphics.GraphicsDG(g),
                        getEditorUI(), fromOffset, toOffset,
                        rr.x, rr.y, Integer.MAX_VALUE);
                    
                    if (loggable) {
                        ts2 = System.currentTimeMillis();
                        if (ts2 - ts1 > PERF_TRESHOLD) {
                            LOG.finest("paint: " + //NOI18N
                                "<" + fromOffset + ", " + toOffset + ">, " + //NOI18N
                                "DrawEngine.startX = " + rr.x + ", DrawEngine.startY = " + rr.y + ", " + //NOI18N
                                "shape = [" + allocReadOnly.x + ", " + allocReadOnly.y + ", " + allocReadOnly.width + ", " + allocReadOnly.height + "], " + //NOI18N
                                "clip = [" + clip.x + ", " + clip.y + ", " + clip.width + ", " + clip.height + "] " + //NOI18N
                                "took " + (ts2 - ts1) + " msec"); //NOI18N
                        }
                    }
                }
            }
        } catch (BadLocationException ble) {
            LOG.log(Level.INFO, "Painting the view failed", ble); //NOI18N
        }
    }
    
    public float getPreferredSpan(int axis) {
        switch (axis) {
            case Y_AXIS:
                return getEditorUI().getLineHeight();
            case X_AXIS:
//                try {
                    int offset = Math.max(0, getEndOffset() - 1);
                    Shape retShape = modelToView(offset, new Rectangle(), Position.Bias.Forward, false);
                    int ret = retShape.getBounds().x + retShape.getBounds().width;
                    return Math.max(ret, 1f);
//                } catch (BadLocationException ble) {
//                    LOG.log(Level.INFO, "Can't determine x-axis span", ble); //NOI18N
//                }
        }
        
        return 1f;
    }

    // Markers are placed every MARKERS_DIST characters. The first marker
    // at the startOffset and has x-coordinate 0 (in the view's internal coordinate system).
    // Each marker's value is its distance from the beginning of the view in the
    // view's internal coordinate system. Please note that the view's internal
    // coordinate system is different from the JTextComponent's system, which is
    // for example the system of x, y and shape parameters passed to m2v and v2m methods.
    private static final int MARKERS_DIST;
    static {
        int markersDist = 128;
        try {
            markersDist = (Integer) Class.forName("org.netbeans.editor.DrawEngineTest").getField("TEST_MARKERS_DIST").get(null); //NOI18N
        } catch (Exception e) {
            // ignore
        }
        MARKERS_DIST = markersDist;
        LOG.fine("DrawEngineLineView.MARKERS_DIST = " + MARKERS_DIST); //NOI18N
    }
    private int [] markers = new int [] { 0 };
    private int markersLength = markers.length;
    
    public void highlightsChanged(int changeStart, int changeEnd) {
        checkViewAccess();
        resetMarkers(changeStart);
        preferenceChanged(this, true, false);
    }
    
    private void resetMarkers(int offset) {
        if (offset < getStartOffset() || offset > getEndOffset()) {
            // may happen when undo, see #115122
            markersLength = 1;
        } else {
            markersLength = Math.min(markersLength, (offset - getStartOffset()) / MARKERS_DIST + 1);
        }
        
        if (loggable) {
            LOG.finest("resetMarkers: " + //NOI18N
                "<" + getStartOffset() + ", " + getEndOffset() + ">, offset = " + offset + //NOI18N
                " -> markersLength = " + markersLength); //NOI18N
        }
    }
    
    // startX, startY are in the JTextComponent's coordinate space
    private Rectangle getModel2ViewRect(int startOffset, int endOffset, int startX, int startY, int targetOffset) {
        long ts1 = 0, ts2 = 0;
        EditorUI eui = getEditorUI();
        Rectangle ret;
        
        View parent;
        if ((((parent = getParent()) instanceof GapDocumentView) 
                && ((GapDocumentView)parent).isPendingUpdate())
            || isEstimatedSpan()
        ) {
            ret = new Rectangle(getBaseX(startX), startY, 1, eui.getLineHeight());

        } else {
            if (loggable) {
                ts1 = System.currentTimeMillis();
            }

// XXX: nearly works for monospaced fonts, the only problem is with tabs
//            ret = new Rectangle(
//                getBaseX((targetOffset - startOffset) * charWidth + startX),
//                startY,
//                charWidth,
//                eui.getLineHeight()
//            );

            int targetMarkerIdx = (targetOffset - startOffset) / MARKERS_DIST;
            int markerIdx = Math.min(targetMarkerIdx, markersLength - 1);
            int markerX = markers[markerIdx];
            int markerOffset = startOffset + markerIdx * MARKERS_DIST;

            ret = new Rectangle(getBaseX(markerX), startY, 1, eui.getLineHeight());
            try {
                ModelToViewDG g = getModelToViewDG();
                g.setRectangle(ret); // set the current rectangle

                if (markers.length <= targetMarkerIdx) {
                    int [] arr = new int [targetMarkerIdx + 1];
                    System.arraycopy(markers, 0, arr, 0, markers.length);
                    markers = arr;
                }

                for( ; markerIdx < targetMarkerIdx; markerIdx++) {
                    DrawEngine.getDrawEngine().draw(this, g, eui,
                        markerOffset, markerOffset + MARKERS_DIST,
                        markerX, startY, markerOffset + MARKERS_DIST);

                    markerOffset += MARKERS_DIST;
                    markerX = ret.x;
                    markers[markerIdx + 1] = markerX;
                }

                if (targetMarkerIdx >= markersLength) {
                    markersLength = targetMarkerIdx + 1;
                }

                DrawEngine.getDrawEngine().draw(this, g, eui,
                    markerOffset, endOffset,
                    getBaseX(markerX + startX), startY, targetOffset);
                
                g.setRectangle(null);
            } catch (BadLocationException ble) {
                // Log and return an estimated view
                LOG.log(Level.INFO, "Model-to-view translation failed", ble); //NOI18N
                ret = new Rectangle(getBaseX(startX), startY, 1, eui.getLineHeight());
            }
            
            if (loggable) {
                ts2 = System.currentTimeMillis();
            }
        }
        
        if (loggable && ts2 - ts1 > PERF_TRESHOLD) {
            LOG.finest("m2v: " + //NOI18N
                "<" + startOffset + ", " + endOffset + ">, targetOffset = " + targetOffset + ", " + //NOI18N
                "[" + startX + ", " + startY + "] " + //NOI18N
                "-> [" + ret.getBounds().x + ", " + ret.getBounds().y + ", " + ret.getBounds().width + ", " + ret.getBounds().height + "]" + //NOI18N
                " took " + (ts2 - ts1) + " msec"); //NOI18N
        }
        return ret;
    }
    
    // shape is in the JTextComponent's coordinate space
    public Shape modelToView(int pos, Shape shape, Position.Bias b) {
        return modelToView(pos, shape, b, true); // ensure exact span (not estimated)
    }

    // shape is in the JTextComponent's coordinate space
    public Shape modelToView(int pos, Shape shape, Position.Bias bias, boolean exactSpan) {
        assert shape != null : "The shape parameter must not be null"; //NOI18N
        checkViewAccess();

        if (!(getDocument() instanceof BaseDocument)) {
            return new Rectangle();
        }
        
        if (exactSpan) { // ensure that span will not be estimated
            setEstimatedSpan(false);
        }

        if (bias == Position.Bias.Forward && (pos < super.getStartOffset() || pos >= super.getEndOffset()) ||
            bias == Position.Bias.Backward && (pos <= super.getStartOffset() || pos > super.getEndOffset())
        ) {
            BadLocationException ble = new BadLocationException("Invalid offset = " + pos //NOI18N
                + ", bias = " + bias //NOI18N
                + ", outside of the view <" + super.getStartOffset() + ", " + super.getEndOffset() + ">" //NOI18N
                + ", isFragment = " + isFragment() //NOI18N
                + (isFragment() ? ", fragment boundaries <" + getStartOffset() + ", " + getEndOffset() + ">" : ""), pos); // NOI18N
            LOG.log(Level.INFO, null, ble);
            return new Rectangle(getBaseX(shape.getBounds().x), shape.getBounds().y, 1, getEditorUI().getLineHeight());
        }
        
        if (isFragment() && (pos < getStartOffset() || pos > getEndOffset())) {
            BadLocationException ble = new BadLocationException("Invalid offset = " + pos //NOI18N
                + ", bias = " + bias //NOI18N
                + ", outside of the fragment view"  //NOI18N
                + " <" + getStartOffset() + ", " + getEndOffset() + ">", pos); // NOI18N
            LOG.log(Level.INFO, null, ble);
            return new Rectangle(getBaseX(shape.getBounds().x), shape.getBounds().y, 1, getEditorUI().getLineHeight());
        }

        if (bias == Position.Bias.Backward) {
            pos--;
        }
        
        Rectangle ret = getModel2ViewRect(
            getStartOffset(), 
            getEndOffset(),
            shape.getBounds().x, 
            shape.getBounds().y,
            pos
        );
        
        return ret;
    }
    
    // x, y, shape are in the JTextComponent's coordinate space
    public int viewToModel(float x, float y, Shape shape, Position.Bias[] biasReturn) {
        assert shape != null : "The shape parameter must not be null"; //NOI18N
        checkViewAccess();
        
        if (!(getDocument() instanceof BaseDocument)) {
            return 0;
        }
        
        long ts1 = 0, ts2 = 0;
        int pos = getStartOffset();
        
        if (biasReturn != null) {
            biasReturn[0] = Position.Bias.Forward;
        }
        
        if (!isEstimatedSpan() && x > shape.getBounds().x) {
            if (loggable) {
                ts1 = System.currentTimeMillis();
            }

            EditorUI eui = getEditorUI();
            int xx = Math.max(0, (int)x - shape.getBounds().x - eui.getTextMargin().left);

// XXX: nearly works for monospaced fonts, the only problem is with tabs
//            int chars = xx / charWidth;
//            if (chars > getAdjustedEOLOffset() - getStartOffset()) {
//                chars = getAdjustedEOLOffset() - getStartOffset();
//            }
//            pos += chars;

            int markerIdx = ArrayUtilities.binarySearch(markers, 0, markersLength - 1, xx);
            if (markerIdx >= 0) {
                // hit the marker
                pos = getStartOffset() + markerIdx * MARKERS_DIST;
            } else {
                // get the index of the last marker before xx
                markerIdx = -markerIdx - 2;
                int markerX = markers[markerIdx];
                int markerOffset = getStartOffset() + markerIdx * MARKERS_DIST;

                try {
                    ViewToModelDG g = getViewToModelDG();

                    for ( ; ; ) {
                        int nextOffset = Math.min(getAdjustedEOLOffset(), markerOffset + MARKERS_DIST - 1);

                        g.setTargetX(xx);
                        g.setEOLOffset(nextOffset);

                        DrawEngine.getDrawEngine().draw(
                            this, g, eui, markerOffset, nextOffset,
                            markerX, shape.getBounds().y, -1
                        );

                        if (g.getX() >= xx || g.getOffset() >= getAdjustedEOLOffset()) {
                            break;
                        }

                        markerOffset += MARKERS_DIST;
                        markerX = g.getX();

                        if (markerIdx + 1 >= markers.length) {
                            int [] arr = new int [markers.length + 10];
                            System.arraycopy(markers, 0, arr, 0, markers.length);
                            markers = arr;
                        }
                        markers[++markerIdx] = markerX;
                        markersLength = markerIdx + 1;
                    }

                    pos = Math.min(g.getOffset(), getAdjustedEOLOffset());
                } catch (BadLocationException ble) {
                    // Log and return start offset
                    LOG.log(Level.INFO, "View-to-model translation failed", ble); //NOI18N
                }
            }
            
            if (loggable) {
                ts2 = System.currentTimeMillis();
            }
        }
        
        if (loggable && ts2 - ts1 > PERF_TRESHOLD) {
            LOG.finest("v2m: " + //NOI18N
                "[" + x + ", " + y + "], " + //NOI18N
                "[" + shape.getBounds().x + ", " + shape.getBounds().y + ", " + shape.getBounds().width + ", " + shape.getBounds().height + "] " + //NOI18N
                "-> " + pos + //NOI18N
                " took " + (ts2 - ts1) + " msec"); //NOI18N
        }
        return pos;
    }

    private void checkViewAccess() {
        LockView view = LockView.get(this);
        if (view != null && (view.getLockThread() != Thread.currentThread())){
            throw new IllegalStateException("View access without view lock"); // NOI18N
        }
    }
    
    private final class ViewToModelDG extends DrawGraphics.SimpleDG {

        private int targetX;
        private int offset;
        private int eolOffset;
        
        public void setTargetX(int targetX) {
            this.targetX = targetX;
        }
        
        public void setEOLOffset(int eolOffset) {
            this.eolOffset = eolOffset;
            this.offset = eolOffset;
        }
        
        public int getOffset() {
            return offset;
        }
        
        public @Override boolean targetOffsetReached(int offset, char ch, int x, int charWidth, DrawContext ctx) {
            if (offset <= eolOffset) {
                if (x + charWidth < targetX) {
                    this.offset = offset;
                    return true;
                } else { // target position inside the char
                    this.offset = offset;
                    if (targetX > x + charWidth / 2) {
                        Document doc = getDocument();
                        if (ch != '\n' && doc != null && offset < doc.getLength()) { //NOI18N
                            this.offset++;
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        }

    } // End of ViewToModelDG class
    
    private final class ModelToViewDG extends DrawGraphics.SimpleDG {
        
        private Rectangle r;
        
        public Rectangle getRectangle() {
            return r;
        }
        
        public void setRectangle(Rectangle r) {
            this.r = r;
        }
        
        public @Override boolean targetOffsetReached(int pos, char ch, int x, int charWidth, DrawContext ctx) {
            r.x = x;
            r.y = getY();
            r.width = charWidth;
            r.height = getEditorUI().getLineHeight();
            return false;
        }
    } // End of ModelToViewDG class

    public @Override View createFragment(int p0, int p1) {
        Element elem = getElement();
        return  // necessary conditions in accordance with javadoc
                p0>=0 && p0>=elem.getStartOffset() && p0<elem.getEndOffset() &&
                p1>0 && p1<=elem.getEndOffset() && p1>elem.getStartOffset() &&
                // create fragment only if one of the element differs from valid start or end offset
                (p0!=elem.getStartOffset() || p1!=elem.getEndOffset()) ?
                    new FragmentView(getElement(), p0 - elem.getStartOffset(), p1 - p0) :
                    this;
    }

    public double getLayoutMajorAxisPreferredSpan() {
        return layoutMajorAxisPreferredSpan;
    }    
    
    public float getLayoutMajorAxisPreferredSpanFloat() {
        return layoutMajorAxisPreferredSpan;
    }

    protected void setLayoutMajorAxisPreferredSpan(float layoutMajorAxisPreferredSpan) {
        this.layoutMajorAxisPreferredSpan = layoutMajorAxisPreferredSpan;
    }
    
    public double getLayoutMajorAxisRawOffset() {
        return layoutMajorAxisRawOffset;
    }
    
    public void setLayoutMajorAxisRawOffset(double layoutMajorAxisRawOffset) {
        this.layoutMajorAxisRawOffset = layoutMajorAxisRawOffset;
    }
    
    public float getLayoutMinorAxisAlignment() {
        return getAlignment(getMinorAxis()); // not cached
    }
    
    public float getLayoutMinorAxisMaximumSpan() {
        return getLayoutMinorAxisPreferredSpan();
    }
    
    public float getLayoutMinorAxisMinimumSpan() {
        return getLayoutMinorAxisPreferredSpan();
    }
    
    public float getLayoutMinorAxisPreferredSpan() {
        return layoutMinorAxisPreferredSpan;
    }
    
    protected void setLayoutMinorAxisPreferredSpan(float layoutMinorAxisPreferredSpan) {
        this.layoutMinorAxisPreferredSpan = layoutMinorAxisPreferredSpan;
    }

    public View getView() {
        return this;
    }
    
    public int getViewRawIndex() {
        return viewRawIndex;
    }
    
    public void setViewRawIndex(int viewRawIndex) {
        this.viewRawIndex = viewRawIndex;
    }
    
    public boolean isFlyweight() {
        return false;
    }
    
    public ViewLayoutState selectLayoutMajorAxis(int majorAxis) {
//        assert ViewUtilities.isAxisValid(majorAxis);

        if (majorAxis == View.X_AXIS) {
            setStatusBits(X_MAJOR_AXIS_BIT);
        } else { // y axis
            clearStatusBits(X_MAJOR_AXIS_BIT);
        }
        
        return this;
    }
    
    protected final ViewLayoutState.Parent getLayoutStateParent() {
        View parent = getView().getParent();
        return (parent instanceof ViewLayoutState.Parent)
            ? ((ViewLayoutState.Parent)parent)
            : null;
    }

    public void updateLayout() {
        // First check whether the layout still need updates
        if (isLayoutValid()) {
            return; // nothing to do
        }

        ViewLayoutState.Parent lsParent = getLayoutStateParent();
        if (lsParent == null) {
            return;
        }

        // Check whether minor axis has changed
        if (isStatusBitsNonZero(MINOR_AXIS_PREFERENCE_CHANGED_BIT)) { // minor not valid
            clearStatusBits(MINOR_AXIS_PREFERENCE_CHANGED_BIT);

            int minorAxis = getMinorAxis();
            if (minorAxisUpdateLayout(minorAxis)) {
                lsParent.minorAxisPreferenceChanged(this);
            }
        }

        // Check whether major axis has changed
        if (isStatusBitsNonZero(MAJOR_AXIS_PREFERENCE_CHANGED_BIT)) { // major not valid
            clearStatusBits(MAJOR_AXIS_PREFERENCE_CHANGED_BIT);

            float oldSpan = getLayoutMajorAxisPreferredSpanFloat();
            float newSpan = getPreferredSpan(getMajorAxis());
            setLayoutMajorAxisPreferredSpan(newSpan);
            double majorAxisSpanDelta = newSpan - oldSpan;
            if (majorAxisSpanDelta != 0) {
                lsParent.majorAxisPreferenceChanged(this, majorAxisSpanDelta);
            }
        }

        // Check whether size must be set on the view
        if (isStatusBitsNonZero(VIEW_SIZE_INVALID_BIT)) {
            clearStatusBits(VIEW_SIZE_INVALID_BIT);

            float width;
            float height;
            float majorAxisSpan = (float)getLayoutMajorAxisPreferredSpan();
            float minorAxisSpan = lsParent.getMinorAxisSpan(this);
            if (isXMajorAxis()) { // x is major axis
                width = majorAxisSpan;
                height = minorAxisSpan;
            } else {
                width = minorAxisSpan;
                height = majorAxisSpan;
            }

            setSize(width, height);
        }
        
        // Possibly update layout again
        updateLayout();
    }
    
    protected boolean minorAxisUpdateLayout(int minorAxis) {
        boolean minorAxisPreferenceChanged = false;
        float val;
        
        val = getPreferredSpan(minorAxis);
        if (val != getLayoutMinorAxisPreferredSpan()) {
            setLayoutMinorAxisPreferredSpan(val);
            minorAxisPreferenceChanged = true;
        }
        
        return minorAxisPreferenceChanged;
    }

    public void viewPreferenceChanged(boolean width, boolean height) {
        if (isXMajorAxis()) { // x is major axis
            if (width) {
                setStatusBits(MAJOR_AXIS_PREFERENCE_CHANGED_BIT); // major no longer valid
            }
            if (height) {
                setStatusBits(MINOR_AXIS_PREFERENCE_CHANGED_BIT); // minor no longer valid
            }
        } else {
            if (width) {
                setStatusBits(MINOR_AXIS_PREFERENCE_CHANGED_BIT); // minor no longer valid
            }
            if (height) {
                setStatusBits(MAJOR_AXIS_PREFERENCE_CHANGED_BIT); // major no longer valid
            }
        }
        setStatusBits(VIEW_SIZE_INVALID_BIT); // child size no longer valid
    }
    
    public void markViewSizeInvalid() {
        setStatusBits(VIEW_SIZE_INVALID_BIT);
    }

    public boolean isLayoutValid() {
        return !isStatusBitsNonZero(ANY_INVALID);
    }

    protected final boolean isXMajorAxis() {
        return isStatusBitsNonZero(X_MAJOR_AXIS_BIT);
    }
    
    protected final int getMajorAxis() {
        return isXMajorAxis() ? View.X_AXIS : View.Y_AXIS;
    }
    
    protected final int getMinorAxis() {
        return isXMajorAxis() ? View.Y_AXIS : View.X_AXIS;
    }
    
    protected final int getStatusBits(int bits) {
        return (statusBits & bits);
    }
    
    protected final boolean isStatusBitsNonZero(int bits) {
        return (getStatusBits(bits) != 0);
    }
    
    protected final void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    protected final void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }

    
    /** Fragment View of DrawEngineLineView, typicaly created via createFragment method */
    private static final class FragmentView extends DrawEngineLineView {
        
        private Position startPos;
        private Position endPos;
        
        public FragmentView(Element elem, int offset, int length){
            super(elem);
            try {
                Document doc = elem.getDocument();
                this.startPos = doc.createPosition(super.getStartOffset() + offset);
                this.endPos = doc.createPosition(startPos.getOffset() + length);
            } catch (BadLocationException ble) {
                LOG.log(Level.INFO, "Can't create fragment view, offset = " + offset + ", length = " + length, ble); //NOI18N
            }
        }

        protected @Override boolean isFragment(){
            return true;
        }

        public @Override int getStartOffset() {
            return startPos.getOffset();
        }
        
        public @Override int getEndOffset() {
            return endPos.getOffset();
        }
        
    } // End of FragmentView class

	// #164820
	@Override
	public int getNextVisualPositionFrom(int pos, Bias b, Shape a, int direction, Bias[] biasRet) throws BadLocationException {
		
		switch (direction) {
		case WEST:
			{
				pos = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
				if (Character.isLowSurrogate(org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(getDocument()).charAt(pos))) {
					// Supplementary character
					return super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
				}
			}
			break;

		case EAST:
			{
				pos = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
				if (Character.isLowSurrogate(org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(getDocument()).charAt(pos))) {
					// Supplementary character
					return super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
				}
			}
			break;

		default:
			pos = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
			break;
		}

		return pos;
	}
	
}
