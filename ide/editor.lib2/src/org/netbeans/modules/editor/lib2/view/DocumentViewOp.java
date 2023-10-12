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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JLayeredPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Document View operation management.
 * 
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers") //NOI18N
public final class DocumentViewOp
        implements PropertyChangeListener, ChangeListener
{

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentViewOp.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentViewOp.class.getName());

    // Whether use fractional metrics rendering hint
    static final Map<Object, Object> extraRenderingHints = new HashMap<>();
    
    static final Boolean doubleBuffered;
    
    static {
        String aa = System.getProperty("org.netbeans.editor.aa");
        if (aa != null) {
            extraRenderingHints.put(RenderingHints.KEY_ANTIALIASING,
                    (Boolean.parseBoolean(aa) || "on".equals(aa)) ?
                            RenderingHints.VALUE_ANTIALIAS_ON :
                            ("false".equalsIgnoreCase(aa) || "off".equals(aa)) ?
                                    RenderingHints.VALUE_ANTIALIAS_OFF :
                                    RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    
        String aaText = System.getProperty("org.netbeans.editor.aa.text");
        if (aaText != null) {
            extraRenderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    (Boolean.parseBoolean(aaText) || "on".equals(aaText)) ?
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
                            "gasp".equals(aaText) ? RenderingHints.VALUE_TEXT_ANTIALIAS_GASP :
                            "hbgr".equals(aaText) ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR :
                            "hrgb".equals(aaText) ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB :
                            "vbgr".equals(aaText) ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR :
                            "vrgb".equals(aaText) ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB :
                            ("false".equalsIgnoreCase(aaText) || "off".equals(aaText)) ?
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF :
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        }

        String useFractionalMetrics = System.getProperty("org.netbeans.editor.aa.fractional");
        if (useFractionalMetrics != null) {
            extraRenderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                    (Boolean.parseBoolean(useFractionalMetrics) || "on".equals(useFractionalMetrics)) ?
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON :
                            ("false".equalsIgnoreCase(useFractionalMetrics) || "off".equals(useFractionalMetrics)) ?
                                RenderingHints.VALUE_FRACTIONALMETRICS_OFF :
                                RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        }
    
        String rendering = System.getProperty("org.netbeans.editor.aa.rendering");
        if (rendering != null) {
            extraRenderingHints.put(RenderingHints.KEY_RENDERING,
                    ("quality".equals(rendering)) ? RenderingHints.VALUE_RENDER_QUALITY :
                    ("speed".equals(rendering)) ? RenderingHints.VALUE_RENDER_SPEED :
                            RenderingHints.VALUE_RENDER_DEFAULT);
        }
    
        String strokeControl = System.getProperty("org.netbeans.editor.aa.stroke");
        if (strokeControl != null) {
            extraRenderingHints.put(RenderingHints.KEY_STROKE_CONTROL,
                    "normalize".equals(strokeControl) ? RenderingHints.VALUE_STROKE_NORMALIZE :
                    "pure".equals(strokeControl) ? RenderingHints.VALUE_STROKE_PURE :
                            RenderingHints.VALUE_STROKE_DEFAULT);
        }
    
        String contrast = System.getProperty("org.netbeans.editor.aa.contrast"); // Integer expected
        if (contrast != null) {
            try {
                extraRenderingHints.put(RenderingHints.KEY_TEXT_LCD_CONTRAST,
                        Integer.valueOf(contrast));
            } catch (NumberFormatException ex) {
                // Do not add the key
            }
        }
        
        String dBuffered = System.getProperty("org.netbeans.editor.double.buffered");
        doubleBuffered = (dBuffered != null) ? Boolean.parseBoolean(dBuffered) : null;
    }
    
    static final char PRINTING_SPACE = '\u00B7';
    static final char PRINTING_TAB = '\u2192';
    static final char PRINTING_TAB_ALTERNATE = '\u00BB';
    static final char PRINTING_NEWLINE = '\u00B6';
    static final char LINE_CONTINUATION = '\u21A9';
    static final char LINE_CONTINUATION_ALTERNATE = '\u2190';
    
    private static final int ALLOCATION_WIDTH_CHANGE            = 1;
    private static final int ALLOCATION_HEIGHT_CHANGE           = 2;
    private static final int WIDTH_CHANGE                       = 4;
    private static final int HEIGHT_CHANGE                      = 8;
    
    /**
     * Whether the "children" is currently reflecting the document state.
     * <br>
     * The children may hold a semi-valid view hierarchy which may still be partly used
     * to resolve queries in some cases.
     */
    private static final int CHILDREN_VALID                     = 16;
    
    /**
     * Whether there's a pending document modification so the view hierarchy should not be active
     * until it gets updated by the pending modification.
     */
    private static final int INCOMING_MODIFICATION              = 32;
    
    /**
     * Whether view hierarchy currently fires a change so any queries to view hierarchy are prohibited.
     */
    private static final int FIRING_CHANGE                      = 64;
    
    private static final int ACCURATE_SPAN                      = 128;

    private static final int AVAILABLE_WIDTH_VALID              = 256;
    
    private static final int NON_PRINTABLE_CHARACTERS_VISIBLE   = 512;
    
    private static final int UPDATE_VISIBLE_DIMENSION_PENDING   = 1024;
    
    private static final String ORIG_MOUSE_WHEEL_LISTENER_PROP = "orig-mouse-wheel-listener";
    
    private final DocumentView docView;

    private int statusBits;
    
    /**
     * Maintenance of view updates.
     * If this is a preview-only view e.g. for a collapsed fold preview
     * when located over collapsed fold's tooltip.
     */
    ViewUpdates viewUpdates; // pkg-private for tests
    
    private final TextLayoutCache textLayoutCache;

    /**
     * New width assigned by DocumentView.setSize() - it will be processed once a lock is acquired.
     */
    private float newAllocationWidth;
    
    private float newAllocationHeight;
    
    /**
     * Visible rectangle of the viewport or a text component if there is no viewport.
     * Initial size should not be zero since when querying DocView for preferred horizontal span
     * for a component that was not laid out yet (e.g. a FoldView)
     * with linewrap set to "anywhere" the views would attempt to fit into zero width.
     */
    private Rectangle visibleRect = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private float availableWidth;
    
    private float renderWrapWidth;
    
    private double repaintX0;
    
    private double repaintY0;
    
    private double repaintX1;
    
    private double repaintY1;
    
    /**
     * Cached font render context (in order not to call getContainer().getGraphics() etc. each time).
     * It appears the FontRenderContext class is not extended (inspected SunGraphics2D)
     * so it should be safe and work fine.
     */
    private FontRenderContext fontRenderContext;
    
    private AttributeSet defaultColoring;

    /**
     * Default row height computed as height of the defaultFont.
     */
    private int defaultRowHeightInt;

    private int defaultAscentInt;

    private float defaultCharWidth;

    private Color textLimitLineColor;

    private int textLimitLineX;
    
    private LineWrapType lineWrapType;

    private TextLayout newlineTextLayout;

    private TextLayout tabTextLayout;

    private TextLayout singleCharTabTextLayout;

    private TextLayout lineContinuationTextLayout;

    private LookupListener lookupListener;
    
    private JViewport activeViewport;

    private JScrollPane activeScrollPane;

    private Preferences prefs;

    private PreferenceChangeListener prefsListener;

    Map<?, ?> renderingHints;
    
    private int lengthyAtomicEdit; // Long atomic edit being performed

    ViewHierarchyImpl viewHierarchyImpl; // Assigned upon setParent()
    
    private Map<Font,FontInfo> fontInfos = new HashMap<Font, FontInfo>(4);
    
    private Font defaultFont;
    private Font defaultHintFont;
    
    private boolean fontRenderContextFromPaint;

    /**
     * Constant retrieved from preferences settings that allows the user to increase/decrease
     * paragraph view's height (which may help for problematic fonts that do not report its height
     * correctly for them to fit the line).
     * <br>
     * By default it's 1.0. All the ascents, descent and leadings of all fonts
     * are multiplied by the constant.
     */
    private float rowHeightCorrection = 1.0f;
    
    private int textZoom;
    
    /**
     * Extra height of 1/3 of viewport's window height added to the real views' allocation.
     */
    private float extraVirtualHeight;
    
    boolean asTextField;
    
    private boolean guideLinesEnable;
    private boolean inlineHintsEnable;
    
    private int indentLevelSize;
    
    private int tabSize;
    
    private Color guideLinesColor;
    
    private int[] guideLinesCache = { -1, -1, -1};
    
    public DocumentViewOp(DocumentView docView) {
        this.docView = docView;
        textLayoutCache = new TextLayoutCache();
    }

    public ViewHierarchyImpl viewHierarchyImpl() {
        return viewHierarchyImpl;
    }

    public boolean isChildrenValid() {
        return isAnyStatusBit(CHILDREN_VALID);
    }

    /**
     * Rebuild views if there are any pending highlight factory changes reported.
     * Method ensures proper locking of document and view hierarchy.
     */
    public void viewsRebuildOrMarkInvalid() {
        docView.runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                if (viewUpdates != null) {
                    viewUpdates.viewsRebuildOrMarkInvalidNeedsLock();
                }
            }
        });
    }
    
    void notifyWidthChange() {
        setStatusBits(WIDTH_CHANGE);
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV-WIDTH changed\n"); // NOI18N
        }
    }
    
    boolean isWidthChange() {
        return isAnyStatusBit(WIDTH_CHANGE);
    }
    
    private void resetWidthChange() {
        clearStatusBits(WIDTH_CHANGE);
    }
    
    /**
     * If width-change was notified then check if real change occurred comparing
     * current width and width of children.
     * @return true if real change occurred.
     */
    private boolean checkRealWidthChange() {
        if (isWidthChange()) {
            resetWidthChange();
            return docView.updatePreferredWidth();
        }
        return false;
    }
    
    void notifyHeightChange() {
        setStatusBits(HEIGHT_CHANGE);
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV-HEIGHT changed\n"); // NOI18N
        }
    }
    
    boolean isHeightChange() {
        return isAnyStatusBit(HEIGHT_CHANGE);
    }
    
    private void resetHeightChange() {
        clearStatusBits(HEIGHT_CHANGE);
    }
    
    /**
     * If height-change was notified then check if real change occurred comparing
     * current height and height of children.
     * @return true if real change occurred.
     */
    private boolean checkRealHeightChange() {
        if (isHeightChange()) {
            resetHeightChange();
            return docView.updatePreferredHeight();
        }
        return false;
    }
    
    void markAllocationWidthChange(float newWidth) {
        setStatusBits(ALLOCATION_WIDTH_CHANGE);
        newAllocationWidth = newWidth;
    }
    
    void markAllocationHeightChange(float newHeight) {
        setStatusBits(ALLOCATION_HEIGHT_CHANGE);
        newAllocationHeight = newHeight;
    }
    
    /**
     * Set given status bits to 1.
     */
    private void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    /**
     * Set given status bits to 0.
     */
    private void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }
    
    /**
     * Set all given status bits to the given value.
     * @param bits status bits to be updated.
     * @param value true to change all status bits to 1 or false to change them to 0.
     */
    private void updateStatusBits(int bits, boolean value) {
        if (value) {
            setStatusBits(bits);
        } else {
            clearStatusBits(bits);
        }
    }
    
    private boolean isAnyStatusBit(int bits) {
        return (statusBits & bits) != 0;
    }

    void lockCheck() {
        // Check if there's any unprocessed allocation width/height change
        if (isAnyStatusBit(ALLOCATION_HEIGHT_CHANGE)) {
            clearStatusBits(ALLOCATION_HEIGHT_CHANGE);
            docView.setAllocationHeight(newAllocationHeight);
        }
        if (isAnyStatusBit(ALLOCATION_WIDTH_CHANGE)) {
            docView.setAllocationWidth(newAllocationWidth);
            // Updating of visible dimension can only be performed in EDT (acquires AWT treelock)
            // and the AWT treelock must precede VH lock.
            if (!isAnyStatusBit(UPDATE_VISIBLE_DIMENSION_PENDING)) {
                setStatusBits(UPDATE_VISIBLE_DIMENSION_PENDING);
                if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
                    ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DVOp.logCheck: invokeLater(updateVisibleDimension())\n");
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateVisibleDimension(true);
                    }
                });
            }
        }
    }

    void unlockCheck() {
        checkRealSpanChange(); // Clears widthChange and heightChange
        checkRepaint();
        // Check if there's any unprocessed width/height change
        if (isAnyStatusBit(WIDTH_CHANGE | HEIGHT_CHANGE)) {
            // Do not throw error (only log) since unlock() call may be in another nested 'finally' section
            // which would lose original stacktrace.
            LOG.log(Level.INFO, "DocumentView invalid state upon unlock.", new Exception()); // NOI18N
        }
    }

    void checkRealSpanChange() {
        boolean widthChange = checkRealWidthChange();
        boolean heightChange = checkRealHeightChange();
        if (widthChange || heightChange) {
            if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
                String msg = "TC-preferenceChanged(" + // NOI18N
                        (widthChange ? "W" : "-") + "x" + (heightChange ? "H" : "-") + ")\n"; // NOI18N
                ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, msg);
            }
            // RootView.preferenceChanged() calls textComponent.revalidate (reposts to EDT if not called in it already)
            docView.superPreferenceChanged(widthChange, heightChange);
        }
    }
    
    public void notifyRepaint(double x0, double y0, double x1, double y1) {
        if (repaintX1 == 0d) {
            repaintX0 = x0;
            repaintY0 = y0;
            repaintX1 = x1;
            repaintY1 = y1;
        } else { // Merge regions
            repaintX0 = Math.min(repaintX0, x0);
            repaintX1 = Math.max(repaintX1, x1);
            repaintY0 = Math.min(repaintY0, y0);
            repaintY1 = Math.max(repaintY1, y1);
        }

        if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
            String msg = "NOTIFY-REPAINT XYWH[" + x0 + ";" + y0 + ";" + (x1-x0) + ";" + (y1-y0) + "] => [" // NOI18N
                     + repaintX0 + ";" + repaintY0 + ";" + (repaintX1-repaintX0) + ";" + (repaintY1-repaintY0) + "]\n"; // NOI18N
            ViewUtils.log(ViewHierarchyImpl.REPAINT_LOG, msg);
        }
    }
    
    void notifyRepaint(Rectangle2D repaintRect) {
        notifyRepaint(repaintRect.getX(), repaintRect.getY(), repaintRect.getMaxX(), repaintRect.getMaxY());
    }

    final void checkRepaint() {
        if (repaintX1 != 0d) {
            final int x0 = (int) repaintX0;
            final int x1 = (int) Math.ceil(repaintX1);
            final int y0 = (int) repaintY0;
            final int y1 = (int) Math.ceil(repaintY1);
            resetRepaintRegion();
            // Possibly post repaint into EDT since there was a deadlock in JDK related to this.
            ViewUtils.runInEDT(new Runnable() {
                @Override
                public void run() {
                    JTextComponent textComponent = docView.getTextComponent();
                    if (textComponent != null) {
                        if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
                            ViewHierarchyImpl.REPAINT_LOG.finer("REPAINT [x0,y0][x1,y1]: [" +
                                    x0 + "," + y0 + "][" + x1 + "," + y1 + "]\n"); // NOI18N
                        }
                        textComponent.repaint(x0, y0, x1 - x0, y1 - y0);
                    }
                }
            });
        }
    }
    
    private void resetRepaintRegion() {
        repaintX1 = 0d; // Make repaint region empty
    }
    
    void extendToVisibleWidth(Rectangle2D.Double r) {
        r.width = getVisibleRect().getMaxX();
    }
    
    void parentViewSet() {
        JTextComponent textComponent = docView.getTextComponent();
        assert (textComponent != null) : "Null textComponent"; // NOI18N
        updateStatusBits(ACCURATE_SPAN, Boolean.TRUE.equals(textComponent.getClientProperty(DocumentView.ACCURATE_SPAN_PROPERTY)));
        updateTextZoom(textComponent);
        viewUpdates = new ViewUpdates(docView);
        viewUpdates.initFactories();
        asTextField = Boolean.TRUE.equals(textComponent.getClientProperty("AsTextField"));
        textComponent.addPropertyChangeListener(this);
        viewHierarchyImpl = ViewHierarchyImpl.get(textComponent);
        viewHierarchyImpl.setDocumentView(docView);
        if (doubleBuffered != null) {
            // Following code has no real effect since parents (all JPanel instances)
            // will use buffer of parent components (even if all parents are set to false).
            textComponent.setDoubleBuffered(doubleBuffered);
            // Turn on/off double-buffering in repaint manager. Since the current implementation
            // uses a shared RM instance for the whole system this will affect painting
            // for the whole application.
            RepaintManager rm = RepaintManager.currentManager(textComponent);
            boolean doubleBufferingOrig = rm.isDoubleBufferingEnabled();
            ViewHierarchyImpl.SETTINGS_LOG.fine("RepaintManager.setDoubleBuffered() from " + // NOI18N
                    doubleBufferingOrig + " to " + doubleBuffered + "\n");
            rm.setDoubleBufferingEnabled(doubleBuffered);
        }
        if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINER)) {
            DebugRepaintManager.register(textComponent);
        }
    }
    
    void parentCleared() {
        JTextComponent textComponent = docView.getTextComponent(); // not null yet
        viewHierarchyImpl.setDocumentView(null);
        uninstallFromViewport();
        textComponent.removePropertyChangeListener(this);
        viewUpdates.released();
        viewUpdates = null;
    }
    
    void checkViewsInited() { // Must be called under mutex
        if (!isChildrenValid() && docView.getTextComponent() != null) {
            checkSettingsInfo();
            // checkSettingsInfo() might called component.setFont() which might
            // lead to BasicTextUI.modelChanged() and new DocumentView creation
            // so docView.getTextComponent() == null should be checked.
            if (docView.getTextComponent() != null) {
                if (checkFontRenderContext()) {
                    updateCharMetrics();
                }
                // Update start and end offsets since e.g. during lengthy-atomic-edit
                // the view hierarchy is not updated and start/end offsets are obsolete.
                docView.updateStartEndOffsets();
                ((EditorTabExpander) docView.getTabExpander()).updateTabSize();
                if (isBuildable()) {
                    LOG.fine("viewUpdates.reinitViews()\n");
                    // Signal early that the views will be valid - otherwise preferenceChange()
                    // that calls getPreferredSpan() would attempt to reinit the views again
                    // (failing in HighlightsViewFactory on usageCount).
                    setStatusBits(CHILDREN_VALID);
                    boolean success = false;
                    try {
                        viewUpdates.reinitAllViews();
                        success = true;
                    } finally {
                        // In case of an error in VH code the children would stay null
                        if (!success) {
                            // Prevent VH to look like active
                            markChildrenInvalid();
                        }
                    }
                }
            }
        }
    }
    
    void initParagraphs(int startIndex, int endIndex) {
        viewUpdates.initParagraphs(startIndex, endIndex);
    }
    
    private boolean checkFontRenderContext() { // check various things related to rendering
        if (fontRenderContext == null) {
            JTextComponent textComponent = docView.getTextComponent();
            Graphics graphics = (textComponent != null) ? textComponent.getGraphics() : null;
            if (graphics instanceof Graphics2D) {
                updateFontRenderContext((Graphics2D)graphics, false);
                return (fontRenderContext != null);
            }
        }
        return false;
    }
    
    void updateFontRenderContext(Graphics2D g, boolean paint) {
        if (g != null) {
            if (!paint && ViewHierarchyImpl.SETTINGS_LOG.isLoggable(Level.FINE)) {
                ViewHierarchyImpl.SETTINGS_LOG.fine(
                        "DocumentView.updateFontColorSettings() Antialiasing Rendering Hints:\n    Graphics: " + // NOI18N
                                g.getRenderingHints() + 
                                "\n    Desktop Hints: " + renderingHints + // NOI18N
                                "\n    Extra Hints: " + extraRenderingHints + '\n'); // NOI18N
            }

            // Use rendering hints (antialiasing etc.)
            if (renderingHints != null) {
                g.addRenderingHints(renderingHints);
            }
            if (extraRenderingHints.size() > 0) {
                g.addRenderingHints(extraRenderingHints);
            }
            if (paint) {
                if (!fontRenderContextFromPaint) {
                    fontRenderContextFromPaint = true;
                    fontRenderContext = g.getFontRenderContext();
                    // Release children since the original non-painting graphics does not have
                    // proper AA set.
                    releaseChildrenNeedsLock(); // Already locked in DV.paint()
                }
            } else {
                fontRenderContext = g.getFontRenderContext();
            }
        }
    }
    
    void releaseChildrenNeedsLock() { // It should be called with acquired mutex
        // Do not set children == null like in super.releaseChildren()
        // Instead mark them as invalid but allow to use them in certain limited cases
        markChildrenInvalid();
    }
    
    private void markChildrenInvalid() {
        clearStatusBits(CHILDREN_VALID);
    }
    
    /**
     * Release all pViews of the document view due to some global change.
     * The method does not build the new views directly just marks the current ones as obsolete.
     *
     * @param updateFonts whether font metrics should be recreated.
     */
    public void releaseChildren(final boolean updateFonts) { // It acquires document readlock and VH mutex first
        docView.runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                releaseChildrenNeedsLock();
                if (updateFonts) {
                    updateCharMetrics();
                }
            }
        });
    }

    /**
     * Whether the view should compute accurate spans (no lazy children views computation).
     * This is handy e.g. for fold preview computation since the fold preview
     * pane must be properly measured.
     *
     * @return whether accurate span measurements should be performed.
     */
    boolean isAccurateSpan() {
        return isAnyStatusBit(ACCURATE_SPAN);
    }

    void updateVisibleDimension(final boolean clearAllocationWidthChange) {
        // Must be called without VH mutex since viewport.getViewRect() acquires AWT treelock
        // and since e.g. paint is called with AWT treelock acquired there would otherwise be a deadlock.
//        assert SwingUtilities.isEventDispatchThread() : "Must be called in EDT"; // NOI18N
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) { // No longer active view
            return;
        }
        Component parent = textComponent.getParent();
        if(parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }
        final Rectangle newVisibleRect;
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            parent = viewport.getParent();
            if (parent instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) parent;
                if (activeViewport != viewport) {
                    uninstallFromViewport();
                    activeViewport = viewport;
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "DocumentViewOp.updateVisibleDimension(): viewport change pane={0}, viewport={1}\n",
                                new Object[]{obj2String(activeViewport.getView()), obj2String(activeViewport)});
                    }
                    if (activeViewport != null) {
                        activeViewport.addChangeListener(this);
                    }
                }
                if (activeScrollPane != scrollPane) {
                    uninstallFromScrollPane();
                    activeScrollPane = scrollPane;
                    MouseWheelDelegator.install(this, activeScrollPane);
                }
            }
            newVisibleRect = viewport.getViewRect(); // acquires AWT treelock

        } else { // No parent viewport
            uninstallFromViewport();
            Dimension size = textComponent.getSize();
            newVisibleRect = new Rectangle(0, 0, size.width, size.height);
        }

        final float extraHeight;
        if (!DocumentView.DISABLE_END_VIRTUAL_SPACE && activeViewport != null && !asTextField) {
            // Compute same value regardless whether there's a horizontal scrollbar visible or not.
            // This is important to avoid flickering caused by vertical shrinking of the text component
            // so that vertical scrollbar appears and then appearing of a horizontal scrollbar
            // (in case there was a line nearly wide as viewport's width without Vscrollbar)\
            // which in turn causes viewport's height to decrease and triggers recomputation again etc.
            float eHeight = activeViewport.getExtentSize().height;
            parent = activeViewport.getParent();
            if(parent instanceof JLayeredPane) {
                parent = parent.getParent();
            }
            if (parent instanceof JScrollPane) {
                JScrollBar hScrollBar = ((JScrollPane)parent).getHorizontalScrollBar();
                if (hScrollBar != null && hScrollBar.isVisible()) {
                    eHeight += hScrollBar.getHeight();
                }
            }
            extraHeight = eHeight / 3; // One third of viewport's extent height
        } else {
            extraHeight = 0f;
        }

        // No document read lock necessary
        docView.runTransaction(new Runnable() {
            @Override
            public void run() {
                boolean widthDiffers = (newVisibleRect.width != visibleRect.width);
                boolean heightDiffers = (newVisibleRect.height != visibleRect.height);
                if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
                    ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DVOp.updateVisibleDimension: widthDiffers=" + widthDiffers + // NOI18N
                            ", newVisibleRect=" + ViewUtils.toString(newVisibleRect) + // NOI18N
                            ", extraHeight=" + extraHeight + "\n"); // NOI18N
                }
                if (clearAllocationWidthChange) {
                    clearStatusBits(ALLOCATION_WIDTH_CHANGE | UPDATE_VISIBLE_DIMENSION_PENDING);
                }
                extraVirtualHeight = extraHeight;
                visibleRect = newVisibleRect;
                if (widthDiffers) {
                    clearStatusBits(AVAILABLE_WIDTH_VALID);
                    docView.markChildrenLayoutInvalid();
                }
                if (asTextField && heightDiffers) {
                    docView.updateBaseY();
                }
            }
        });
    }

    float getExtraVirtualHeight() {
        return extraVirtualHeight;
    }
    
    private void uninstallFromViewport() {
//        assert SwingUtilities.isEventDispatchThread() : "Must be called in EDT"; // NOI18N
        if (activeViewport != null) {
            uninstallFromScrollPane();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "DocumentViewOp.uninstallFromViewport(): pane={0}, viewport={1}\n",
                        new Object[] {obj2String(activeViewport.getView()), obj2String(activeViewport)});
            }
            activeViewport.removeChangeListener(this);
            activeViewport = null;
        }
    }
    
    private void uninstallFromScrollPane() {
        if (activeScrollPane != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "DocumentViewOp.uninstallFromScrollPane(): pane={0}, scrollPane={1}\n",
                        new Object[]{obj2String((activeViewport != null) ? activeViewport.getView() : null),
                            obj2String(activeScrollPane)});
            }
            MouseWheelDelegator.uninstall(activeScrollPane);
            activeScrollPane = null;
        }
    }
    
    private static final String obj2String(Object o) {
        return (o != null) ? (o.getClass().getSimpleName() + "@" + System.identityHashCode(o)) : "null";
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // First lock document and then monitor
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent != null) {
            updateVisibleDimension(false);
        }
    }

    private void checkSettingsInfo() {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return;
        }

        if (prefs == null) {
            String mimeType = DocumentUtilities.getMimeType(textComponent);
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            prefsListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    updatePreferencesSettings(true);
                }
            };
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
            updatePreferencesSettings(false);
        }

        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    final Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    SwingUtilities.invokeLater(new Runnable() { // Must run in AWT to apply fonts/colors to comp.
                        @Override
                        public void run() {
                            docView.runReadLockTransaction(new Runnable() {
                                @Override
                                public void run() {
                                    JTextComponent textComponent = docView.getTextComponent();
                                    if (textComponent != null) {
                                        // Reset zoom since when changing font in tools->options the existing zoom
                                        // would be still applied to the new font
                                        textComponent.putClientProperty(DocumentView.TEXT_ZOOM_PROPERTY, null);
                                        updateFontColorSettings(result, true);
                                    }
                                }
                            });
                        }
                    });
                }
            };
            String mimeType = DocumentUtilities.getMimeType(textComponent);
            Lookup lookup = MimeLookup.getLookup(mimeType);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            // Called without explicitly acquiring mutex but it's called only when lookup listener is null
            // so it should be acquired.
            updateFontColorSettings(result, false);

            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }

        if (lineWrapType == null) {
            updateLineWrapType();
            Document doc = docView.getDocument();
            updateTextLimitLine(doc);
            clearStatusBits(AVAILABLE_WIDTH_VALID);
            DocumentUtilities.addWeakPropertyChangeListener(doc, this);
        }
    }
    
    /* private */ void updatePreferencesSettings(boolean nonInitialUpdate) {
        boolean nonPrintableCharactersVisibleOrig = isAnyStatusBit(NON_PRINTABLE_CHARACTERS_VISIBLE);
        boolean nonPrintableCharactersVisible = prefs.getBoolean(SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE, false);
        updateStatusBits(NON_PRINTABLE_CHARACTERS_VISIBLE, nonPrintableCharactersVisible);
        // Line height correction
        float lineHeightCorrectionOrig = rowHeightCorrection;
        rowHeightCorrection = prefs.getFloat(SimpleValueNames.LINE_HEIGHT_CORRECTION, 1.0f);
        boolean inlineHintsEnableOrig = inlineHintsEnable;
        inlineHintsEnable = prefs.getBoolean("enable.inline.hints", false); // NOI18N
        boolean updateMetrics = (rowHeightCorrection != lineHeightCorrectionOrig);
        boolean releaseChildren = nonInitialUpdate && 
                ((nonPrintableCharactersVisible != nonPrintableCharactersVisibleOrig) ||
                 (rowHeightCorrection != lineHeightCorrectionOrig) ||
                 (inlineHintsEnable != inlineHintsEnableOrig));
        indentLevelSize = getIndentSize();
        tabSize = prefs.getInt(SimpleValueNames.TAB_SIZE, EditorPreferencesDefaults.defaultTabSize);
        if (updateMetrics) {
            updateCharMetrics();
        }
        if (releaseChildren) {
            releaseChildren(false);
        }
        boolean currentGuideLinesEnable = prefs.getBoolean("enable.guide.lines", true); // NOI18N
        if (nonInitialUpdate && guideLinesEnable != currentGuideLinesEnable) {
            docView.op.notifyRepaint(visibleRect.getMinX(), visibleRect.getMinY(), visibleRect.getMaxX(), visibleRect.getMaxY());    
        }
        guideLinesEnable = currentGuideLinesEnable;
        
    }

    /* private */ void updateFontColorSettings(Lookup.Result<FontColorSettings> result, boolean nonInitialUpdate) {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return;
        }
        AttributeSet defaultColoringOrig = defaultColoring;
        FontColorSettings fcs = result.allInstances().iterator().next();
        AttributeSet attribs = fcs.getFontColors(FontColorNames.INDENT_GUIDE_LINES);
        guideLinesColor = attribs != null ? (Color) attribs.getAttribute(StyleConstants.Foreground) : Color.LIGHT_GRAY;
        
        AttributeSet newDefaultColoring = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        // Attempt to always hold non-null content of "defaultColoring" variable once it became non-null
        if (newDefaultColoring != null) {
            defaultColoring = newDefaultColoring;
//            renderingHints = (Map<?, ?>) defaultColoring.getAttribute(EditorStyleConstants.RenderingHints);
        }

        // Use desktop hints
        renderingHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N
        // Possibly use fractional metrics

        if (asTextField) {
            return;
        }
        Color textLimitLineColorOrig = textLimitLineColor;
        AttributeSet textLimitLineColoring = fcs.getFontColors(FontColorNames.TEXT_LIMIT_LINE_COLORING);
        textLimitLineColor = (textLimitLineColoring != null) 
                ? (Color) textLimitLineColoring.getAttribute(StyleConstants.Foreground)
                : null;
        if (textLimitLineColor == null) {
            textLimitLineColor = Color.PINK;
        }
        final boolean applyDefaultColoring = (defaultColoring != defaultColoringOrig); // Do not do equals() (for renderingHints)
        final boolean releaseChildren = nonInitialUpdate && applyDefaultColoring;
        final boolean repaint = nonInitialUpdate
                && (textLimitLineColor == null || !textLimitLineColor.equals(textLimitLineColorOrig));
        if (applyDefaultColoring || releaseChildren || repaint) {
            ViewUtils.runInEDT(new Runnable() {
                @Override
                public void run() {
                    JTextComponent textComponent = docView.getTextComponent();
                    if (textComponent != null) {
                        if (applyDefaultColoring) {
                            applyDefaultColoring(textComponent);
                        }
                        if (releaseChildren) {
                            releaseChildren(true);
                        }
                        if (repaint) {
                            textComponent.repaint();
                        }
                    }
                }
            });

        }
    }
    
    /*private*/ void applyDefaultColoring(JTextComponent textComponent) { // Called in AWT to possibly apply default coloring from settings
        AttributeSet coloring = defaultColoring;
        Font font = ViewUtils.getFont(coloring);
        if (font != null) {
            textComponent.setFont(font);
        }
        Color foreColor = (Color) coloring.getAttribute(StyleConstants.Foreground);
        if (foreColor != null) {
            textComponent.setForeground(foreColor);
        }
        Color backColor = (Color) coloring.getAttribute(StyleConstants.Background);
        if (backColor != null) {
            textComponent.setBackground(backColor);
        }
    }
    
    private void updateTextLimitLine(Document doc) {
        // #183797 - most likely seeing a non-nb document during the editor pane creation
        Integer dllw = (Integer) doc.getProperty(SimpleValueNames.TEXT_LIMIT_WIDTH);
        int textLimitLineColumn = (dllw != null) ? dllw.intValue() : EditorPreferencesDefaults.defaultTextLimitWidth;
        Preferences prefsLocal = prefs;
        if (prefsLocal != null) {
            boolean drawTextLimitLine = prefsLocal.getBoolean(SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE, true);
            textLimitLineX = drawTextLimitLine ? (int) (textLimitLineColumn * defaultCharWidth) : -1;
        }
    }
    
    private void updateLineWrapType() {
        // Should be able to run without mutex
        String lwt = null;
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent != null) {
            lwt = (String) textComponent.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt == null) {
            Document doc = docView.getDocument();
            lwt = (String) doc.getProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt != null) {
            lineWrapType = LineWrapType.fromSettingValue(lwt);
        }
        if (asTextField || lineWrapType == null) {
            lineWrapType = LineWrapType.NONE;
        }
        clearStatusBits(AVAILABLE_WIDTH_VALID);
    }

    private void updateCharMetrics() { // Update default row height and other params
        checkFontRenderContext(); // Possibly get FRC created; ignore ret value since now actually updating the metrics
        FontRenderContext frc = getFontRenderContext();
        JTextComponent textComponent = docView.getTextComponent();
        Font font;
        if (frc != null && textComponent != null && ((font = textComponent.getFont()) != null)) {
            // Reset all the measurements to adhere just to default font.
            // Possible other fonts in fontInfos get eliminated.
            fontInfos.clear();
            FontInfo defaultFontInfo = new FontInfo(font, textComponent, frc, rowHeightCorrection, textZoom);
            fontInfos.put(font, defaultFontInfo);
            fontInfos.put(null, defaultFontInfo); // Alternative way to find default font info
            updateRowHeight(defaultFontInfo, true);
            defaultFont = font;
            defaultHintFont = font.deriveFont((float) (font.getSize2D() * 0.75));
            defaultCharWidth = defaultFontInfo.charWidth;
            
            tabTextLayout = null;
            singleCharTabTextLayout = null;
            newlineTextLayout = null;
            lineContinuationTextLayout = null;

            updateTextLimitLine(docView.getDocument());
            clearStatusBits(AVAILABLE_WIDTH_VALID);

            ViewHierarchyImpl.SETTINGS_LOG.fine("updateCharMetrics(): FontRenderContext: AA=" + frc.isAntiAliased() + // NOI18N
                    ", AATransformed=" + frc.isTransformed() + // NOI18N
                    ", AAFractMetrics=" + frc.usesFractionalMetrics() + // NOI18N
                    ", AAHint=" + frc.getAntiAliasingHint() + "\n"); // NOI18N
        }
    }
    
    private void updateRowHeight(FontInfo fontInfo, boolean force) {
        if (force || defaultAscentInt < fontInfo.ascentInt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("DV.updateRowHeight()" + (force ? "-forced" : "") + // NOI18N
                        ": defaultAscentInt from " + defaultAscentInt + " to " + fontInfo.ascentInt + "\n"); // NOI18N
            }
            defaultAscentInt = fontInfo.ascentInt;
        }
        if (force || defaultRowHeightInt < fontInfo.rowHeightInt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("DV.updateRowHeight()" + (force ? "-forced" : "") + // NOI18N
                        ": defaultRowHeightInt from " + defaultRowHeightInt + " to " + fontInfo.rowHeightInt + "\n"); // NOI18N
            }
            defaultRowHeightInt = fontInfo.rowHeightInt;
        }
    }
    
    void markIncomingModification() {
        setStatusBits(INCOMING_MODIFICATION);
    }
    
    void clearIncomingModification() {
        clearStatusBits(INCOMING_MODIFICATION);
    }

    boolean isActive() {
        if (isAnyStatusBit(FIRING_CHANGE)) {
            throw new IllegalStateException("View hierarchy must not be queried during change firing"); // NOI18N
        } else if (isAnyStatusBit(INCOMING_MODIFICATION)) {
            if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINER)) {
                ViewHierarchyImpl.OP_LOG.log(Level.INFO, "View Hierarchy Query during Incoming Modification\n", // NOI18N
                        new Exception());
            }
            return false;
        } else {
            return isUpdatable();
        }
    }

    boolean isUpdatable() { // Whether the view hierarchy can be updated (by insertUpdate() etc.)
        JTextComponent textComponent = docView.getTextComponent();
        return textComponent != null && isChildrenValid() && (lengthyAtomicEdit <= 0);
    }
    
    boolean isBuildable() {
        JTextComponent textComponent = docView.getTextComponent();
        return textComponent != null && fontRenderContext != null && fontInfos.size() > 0 &&
                (lengthyAtomicEdit <= 0) && !isAnyStatusBit(INCOMING_MODIFICATION);
    }
    
    // from org.netbeans.api.java.source.CodeStyle
    private int getIndentSize() {
        Integer indentLevelInteger = (Integer) docView.getDocument().getProperty(SimpleValueNames.INDENT_SHIFT_WIDTH);
        if (indentLevelInteger != null && indentLevelInteger > 0) {
            return indentLevelInteger;
        }
        int indentLevel = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 0);
        if (indentLevel > 0) {
            return indentLevel;
        }
        Boolean expandTabsBoolean = (Boolean) docView.getDocument().getProperty(SimpleValueNames.EXPAND_TABS);
        if (expandTabsBoolean != null) {
            if (Boolean.TRUE.equals(expandTabsBoolean)) {
                indentLevelInteger = (Integer) docView.getDocument().getProperty(SimpleValueNames.SPACES_PER_TAB);
                if (indentLevelInteger == null) {
                    return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
                }
            } else {
                indentLevelInteger = (Integer) docView.getDocument().getProperty(SimpleValueNames.TAB_SIZE);
                if (indentLevelInteger == null) {
                    return prefs.getInt(SimpleValueNames.TAB_SIZE, EditorPreferencesDefaults.defaultTabSize);
                }
            }
            return indentLevelInteger;
        }
        boolean expandTabs = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, EditorPreferencesDefaults.defaultExpandTabs);
        if (expandTabs) {
            indentLevel = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
        } else {
            indentLevel = prefs.getInt(SimpleValueNames.TAB_SIZE, EditorPreferencesDefaults.defaultTabSize);
        }
        return indentLevel;
    }
    
    public boolean isGuideLinesEnable() {
        return guideLinesEnable && !asTextField;
    }

    public boolean isInlineHintsEnable() {
        return inlineHintsEnable;
    }

    public int getIndentLevelSize() {
        return indentLevelSize;
    }
    
    public int getTabSize() {
        return tabSize;
    }
    
    public Color getGuideLinesColor() {
        return guideLinesColor;
    }
    
    public void setGuideLinesCache(int cacheAtOffset, int foundAtOffset, int length) {
        guideLinesCache = new int[] {cacheAtOffset, foundAtOffset, length};
    }
    
    public int[] getGuideLinesCache() {
        return guideLinesCache;
    }

    /**
     * It should be called with +1 once it's detected that there's a lengthy atomic edit
     * in progress and with -1 when such edit gets finished.
     * @param delta +1 or -1 when entering/leaving lengthy atomic edit.
     */
    public void updateLengthyAtomicEdit(int delta) {
        lengthyAtomicEdit += delta;
        if (LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(LOG, "updateLengthyAtomicEdit: delta=" + delta + // NOI18N
                    " lengthyAtomicEdit=" + lengthyAtomicEdit + "\n"); // NOI18N
        }
        if (lengthyAtomicEdit == 0) {
            releaseChildren(false);
        }
    }

    /**
     * Get displayed portion of the component (either viewport.getViewRect())
     * or (if viewport is missing) size of the component.
     * <br>
     * Note: The value may be obsolete during paint - clipping bounds may already
     *  incorporate a just performed scroll while visibleRect does not yet.
     *
     * @return visible rectangle of the editor either viewport's view or editor component
     *  bounds.
     */
    Rectangle getVisibleRect() {
        return visibleRect;
    }
    
    /**
     * Get width available for display of child views. For non-wrap case it's Integer.MAX_VALUE
     * and for wrapping it's a display width or (if display width would become too narrow)
     * a width of four chars to not overflow the word wrapping algorithm.
     * @return 
     */
    float getAvailableWidth() {
        if (!isAnyStatusBit(AVAILABLE_WIDTH_VALID)) {
            // Mark valid and assign early values to prevent stack overflow in getLineContinuationCharTextLayout()
            setStatusBits(AVAILABLE_WIDTH_VALID);
            availableWidth = Integer.MAX_VALUE;
            renderWrapWidth = availableWidth;
            if (getLineWrapType() != LineWrapType.NONE) {
                final TextLayout lineContTextLayout = getLineContinuationCharTextLayout();
                final float lineContTextLayoutAdvance =
                    lineContTextLayout == null ? 0f : lineContTextLayout.getAdvance();
                availableWidth = Math.max(getVisibleRect().width, 4 * getDefaultCharWidth() + lineContTextLayoutAdvance);
                renderWrapWidth = availableWidth - lineContTextLayoutAdvance;
            }
        }
        return availableWidth;
    }
    
    /**
     * Get width available for rendering of real text on a wrapped line.
     * It's {@link #getAvailableWidth()} minus width of wrap designating character.
     * @return 
     */
    float getRenderWrapWidth() {
        return renderWrapWidth;
    }

    TextLayoutCache getTextLayoutCache() {
        return textLayoutCache;
    }

    FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    public Font getDefaultFont() {
        return defaultFont;
    }

    public Font getDefaultHintFont() {
        return defaultHintFont;
    }

    public float getDefaultRowHeight() {
        checkSettingsInfo();
        return defaultRowHeightInt;
    }

    public float getDefaultAscent() {
        checkSettingsInfo();
        return defaultAscentInt;
    }

    /**
     * Return array of default:
     * <ol>
     *     <li>Underline offset.</li>
     *     <li>Underline thickness.</li>
     *     <li>Strike-through offset.</li>
     *     <li>Strike-through thickness.</li>
     * </ol>
     */
    public float[] getUnderlineAndStrike(Font font) {
        checkSettingsInfo();
        FontInfo fontInfo = fontInfos.get(font);
        if (fontInfo == null) { // Should not normally happen
            fontInfo = fontInfos.get(null);
        }
        return fontInfo.underlineAndStrike;
    }

    public float getDefaultCharWidth() {
        checkSettingsInfo();
        return defaultCharWidth;
    }

    public boolean isNonPrintableCharactersVisible() {
        checkSettingsInfo();
        return isAnyStatusBit(NON_PRINTABLE_CHARACTERS_VISIBLE) && !asTextField;
    }

    LineWrapType getLineWrapType() {
        checkSettingsInfo();
        return lineWrapType;
    }
    
    Color getTextLimitLineColor() {
        checkSettingsInfo();
        return textLimitLineColor;
    }

    int getTextLimitLineX() {
        return textLimitLineX;
    }

    TextLayout getNewlineCharTextLayout() {
        if (newlineTextLayout == null) {
            newlineTextLayout = createTextLayout(String.valueOf(PRINTING_NEWLINE), defaultFont);
        }
        return newlineTextLayout;
    }

    TextLayout getTabCharTextLayout(double availableWidth) {
        if (tabTextLayout == null) {
            char tabChar = defaultFont.canDisplay(PRINTING_TAB) ? PRINTING_TAB : PRINTING_TAB_ALTERNATE;
            tabTextLayout = createTextLayout(String.valueOf(tabChar), defaultFont);
        }
        TextLayout ret = tabTextLayout;
        if (tabTextLayout != null && availableWidth > 0 && tabTextLayout.getAdvance() > availableWidth) {
            if (singleCharTabTextLayout == null) {
                for (int i = defaultFont.getSize() - 1; i >= 0; i--) {
                    Font font = new Font(defaultFont.getName(), defaultFont.getStyle(), i);
                    char tabChar = font.canDisplay(PRINTING_TAB) ? PRINTING_TAB : PRINTING_TAB_ALTERNATE;
                    singleCharTabTextLayout = createTextLayout(String.valueOf(tabChar), font);
                    if (singleCharTabTextLayout != null) {
                        if (singleCharTabTextLayout.getAdvance() <= getDefaultCharWidth()) {
                            LOG.log(Level.FINE, "singleChar font size={0}\n", i);
                            break;
                        }
                    } else { // layout creation failed
                        break;
                    }
                }
            }
            ret = singleCharTabTextLayout;
        }
        return ret;
    }

    /**
     * @return will be null if the line continuation character should not be shown
     */
    TextLayout getLineContinuationCharTextLayout() {
        /* The line continuation character is used to show that a line is automatically being
        broken into multiple wrap lines via the line wrap feature. This causes a lot of visual
        clutter, and always takes up an extra character of horizontal space, so don't show it by
        default. The same information is communicated by the line numbers in the left-hand side of
        the editor anyway. */
        if (!docView.op.isNonPrintableCharactersVisible())
            return null;
        if (lineContinuationTextLayout == null) {
            char lineContinuationChar = LINE_CONTINUATION;
            if (!defaultFont.canDisplay(lineContinuationChar)) {
                lineContinuationChar = LINE_CONTINUATION_ALTERNATE;
            }
            lineContinuationTextLayout = createTextLayout(String.valueOf(lineContinuationChar), defaultFont);
        }
        return lineContinuationTextLayout;
    }

    public FontInfo getFontInfo(Font font) {
        FontInfo fontInfo = fontInfos.get(font);
        if (fontInfo == null) {
            fontInfo = new FontInfo(font, docView.getTextComponent(), getFontRenderContext(), rowHeightCorrection, textZoom);
            fontInfos.put(font, fontInfo);
        }
        return fontInfo;
    }

    TextLayout createTextLayout(String text, Font font) {
        checkSettingsInfo();
        if (fontRenderContext != null && font != null && text.length() > 0) {
            ViewStats.incrementTextLayoutCreated(text.length());
            FontInfo fontInfo = getFontInfo(font);
            TextLayout textLayout = new TextLayout(text, fontInfo.renderFont, fontRenderContext);
            if (fontInfo.updateRowHeight(textLayout, rowHeightCorrection)) {
                updateRowHeight(fontInfo, false);
                LOG.fine("RowHeight Updated -> release children");
                releaseChildren(false);
            }
            return textLayout;
        }
        return null;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return;
        }
        boolean releaseChildren = false;
        boolean updateFonts = false;
        Object src = evt.getSource();
        String propName = evt.getPropertyName();
        if (src instanceof Document) {
            if (propName == null || SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                LineWrapType origLineWrapType = lineWrapType;
                updateLineWrapType(); // can run without mutex
                if (origLineWrapType != lineWrapType) {
                    LOG.log(Level.FINE, "Changing lineWrapType from {0} to {1}", new Object [] { origLineWrapType, lineWrapType }); //NOI18N
                    releaseChildren = true;
                }
            }
            if (propName == null || SimpleValueNames.TAB_SIZE.equals(propName)) {
                releaseChildren = true;
                Integer tabSizeInteger = (Integer) docView.getDocument().getProperty(SimpleValueNames.TAB_SIZE);
                tabSize = (tabSizeInteger != null) ? tabSizeInteger : tabSize;
            }
            if (propName == null || SimpleValueNames.SPACES_PER_TAB.equals(propName) || SimpleValueNames.TAB_SIZE.equals(propName) || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(propName)) {
                releaseChildren = true;
                indentLevelSize = getIndentSize();
            }
            if (propName == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(propName)) {
                updateTextLimitLine(docView.getDocument());
                releaseChildren = true;
            }
        } else if (src instanceof JTextComponent) { // an event from JTextComponent
            if ("ancestor".equals(propName)) { // NOI18N

            } else if ("document".equals(propName)) { // NOI18N
                
            } else if ("font".equals(propName)) { // NOI18N
                releaseChildren = true;
                updateFonts = true;
            } else if ("foreground".equals(propName)) { //NOI18N
                releaseChildren = true; // Repaint should possibly suffice too
            } else if ("background".equals(propName)) { //NOI18N
                releaseChildren = true; // Repaint should possibly suffice too
            } else if (SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                updateLineWrapType(); // can run without mutex
                releaseChildren = true;
            } else if (DocumentView.START_POSITION_PROPERTY.equals(propName) ||
                    DocumentView.END_POSITION_PROPERTY.equals(propName))
            {
                docView.runReadLockTransaction(new Runnable() {
                    @Override
                    public void run() {
                        docView.updateStartEndOffsets();
                        releaseChildrenNeedsLock(); // Rebuild view hierarchy
                    }
                });
            } else if (DocumentView.TEXT_ZOOM_PROPERTY.equals(propName)) {
                updateTextZoom(textComponent);
                releaseChildren = true;
                updateFonts = true;
            } else if ("AsTextField".equals(propName)) {
                asTextField = Boolean.TRUE.equals(textComponent.getClientProperty("AsTextField"));
                updateLineWrapType();
                docView.updateBaseY();
                releaseChildren = true;
            }
        }
        if (releaseChildren) {
            releaseChildren(updateFonts);
        }
    }
    
    private void updateTextZoom(JTextComponent textComponent) {
        Integer textZoomInteger = (Integer) textComponent.getClientProperty(DocumentView.TEXT_ZOOM_PROPERTY);
        textZoom = (textZoomInteger != null) ? textZoomInteger : 0;
    }
    
    public void mouseWheelMoved(MouseWheelEvent evt, MouseWheelDelegator delegator) {
        // Since consume() idoes not prevent BasicScrollPaneUI.Handler from operation
        // the code in DocumentView.setParent() removes BasicScrollPaneUI.Handler and stores it
        // in origMouseWheelListener and installs "this" as MouseWheelListener instead.
        // This method only calls origMouseWheelListener if Ctrl is not pressed (zooming in/out).
        if (evt.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            delegator.delegateToOriginalListener(evt, activeScrollPane);
//          evt.consume(); // consuming the event has no effect
            return;
        }
        
        int modifiers = 0;
        if (evt.isControlDown()) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if (evt.isAltDown()) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if (evt.isShiftDown()) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (evt.isMetaDown()) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }
       
        JTextComponent textComponent = docView.getTextComponent();
        Keymap keymap = textComponent.getKeymap();
        double wheelRotation = evt.getPreciseWheelRotation();
        if (wheelRotation < 0) {
            int mouseUpKeyCode = Utilities.mouseWheelUpKeyCode();
            Action action = mouseUpKeyCode == KeyEvent.VK_UNDEFINED ? null
                    : keymap.getAction(KeyStroke.getKeyStroke(mouseUpKeyCode, modifiers));
            if (action != null) {
                action.actionPerformed(new ActionEvent(docView.getTextComponent(), 0, ""));
                textComponent.repaint(); // Consider repaint triggering elsewhere
            } else {
                delegator.delegateToOriginalListener(evt, activeScrollPane);
            }
        } else if (wheelRotation > 0) {
            int mouseDownKeyCode = Utilities.mouseWheelDownKeyCode();
            Action action = mouseDownKeyCode == KeyEvent.VK_UNDEFINED ? null
                    : keymap.getAction(KeyStroke.getKeyStroke(mouseDownKeyCode, modifiers));
            if (action != null) {
                action.actionPerformed(new ActionEvent(docView.getTextComponent(), 0, ""));
                textComponent.repaint(); // Consider repaint triggering elsewhere
            } else {
                delegator.delegateToOriginalListener(evt, activeScrollPane);
            }
        } // else: wheelRotation == 0 => do nothing
    }

    StringBuilder appendInfo(StringBuilder sb) {
        sb.append(" incomMod=").append(isAnyStatusBit(INCOMING_MODIFICATION)); // NOI18N
        sb.append(", lengthyAE=").append(lengthyAtomicEdit); // NOI18N
        sb.append('\n');
        sb.append(viewUpdates);
        sb.append(", ChgFlags:"); // NOI18N
        int len = sb.length();
        if (isWidthChange()) sb.append(" W");
        if (isHeightChange()) sb.append(" H");
        if (sb.length() == len) {
            sb.append(" <NONE>");
        }
        sb.append("; visWidth:").append(getVisibleRect().width); // NOI18N
        sb.append(", rowHeight=").append(getDefaultRowHeight()); // NOI18N
        sb.append(", ascent=").append(getDefaultAscent()); // NOI18N
        sb.append(", charWidth=").append(getDefaultCharWidth()); // NOI18N
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(200)).toString();
    }

    /**
     * Listener for changes of UI of a scroll pane.
     */
    private static final class MouseWheelDelegator implements MouseWheelListener, PropertyChangeListener {
        
        static void install(DocumentViewOp op, JScrollPane scrollPane) {
            MouseWheelDelegator mwd = (MouseWheelDelegator) scrollPane.getClientProperty(MouseWheelDelegator.class);
            if (mwd == null) {
                mwd = new MouseWheelDelegator(scrollPane);
                scrollPane.putClientProperty(MouseWheelDelegator.class, mwd);
            }
            mwd.setOp(op);
        }
        
        static void uninstall(JScrollPane scrollPane) {
            MouseWheelDelegator mwd = (MouseWheelDelegator) scrollPane.getClientProperty(MouseWheelDelegator.class);
            if (mwd != null) {
                mwd.uninstall();
                scrollPane.putClientProperty(MouseWheelDelegator.class, null);
            }
        }
        
        private Reference<DocumentViewOp> opRef;

        private final JScrollPane scrollPane;
        
        private ScrollPaneUI lastUI;
        
        private MouseWheelListener delegateListener;

        MouseWheelDelegator(JScrollPane scrollPane) {
            this.scrollPane = scrollPane;
            scrollPane.addPropertyChangeListener(this);
            updateListeners();
        }
        
        DocumentViewOp op() {
            Reference<DocumentViewOp> lOp = this.opRef;
            return (lOp != null) ? lOp.get() : null;
        }
        
        void setOp(DocumentViewOp op) {
            if (op != op()) {
                this.opRef = new WeakReference<DocumentViewOp>(op);
            }
        }
        
        private void updateListeners() {
            ScrollPaneUI ui = scrollPane.getUI();
            if (ui != lastUI) {
                // Update "ui" property listener
                if (ui != null) {
                    // Check mouse wheel listeners on scroll pane.
                    // Pair first non-MWDelegator listener with 
                    // Remove any other delegators than this one.
                    MouseWheelListener[] mwls = scrollPane.getListeners(MouseWheelListener.class);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "MouseWheelDelegator.updateListeners(): scrollPane change scrollPane={0}, MouseWheelListeners:{1}\n",
                                new Object[]{obj2String(scrollPane), Arrays.asList(mwls)});
                    }
                    delegateListener = null;
                    for (MouseWheelListener listener : mwls) {
                        if (listener instanceof MouseWheelDelegator) {
                            scrollPane.removeMouseWheelListener(listener);
                            if (delegateListener == null) {
                                delegateListener = ((MouseWheelDelegator) listener).delegateListener;
                                scrollPane.addMouseWheelListener(this);
                            }
                        } else { // Regular listener 
                            // Current patch only assumes one MW listener attached by the UI impl.
                            if (delegateListener == null) {
                                delegateListener = listener;
                                scrollPane.removeMouseWheelListener(listener);
                                scrollPane.addMouseWheelListener(this);
                            }
                        }
                    }
                }
                lastUI = ui;
            }
        }
        
        private void uninstall() {
            scrollPane.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("ui".equals(evt.getPropertyName()) || "UI".equals(evt.getPropertyName())) {
                updateListeners();
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Scrollpane changed with property name: {0}",
                                new Object[]{evt.getPropertyName()});
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            DocumentViewOp op = op();
            if (op != null && scrollPane != null ) {
                op.mouseWheelMoved(e, this);
            }
        }
        
        void delegateToOriginalListener(MouseWheelEvent e, JScrollPane activeScrollPane) {
            // Only delegate if the updated scrollpane is still actively used by the DocumentViewOp
            // and when it still uses the overriden listener (this)
            if (activeScrollPane == scrollPane) {
                MouseWheelListener[] mwls = scrollPane.getListeners(MouseWheelListener.class);
                for (int i = 0; i < mwls.length; i++) {
                    if (mwls[i] == this) {
                        delegateListener.mouseWheelMoved(e);
                        return;
                    }
                }
            }
        }
        
    }
    
}
