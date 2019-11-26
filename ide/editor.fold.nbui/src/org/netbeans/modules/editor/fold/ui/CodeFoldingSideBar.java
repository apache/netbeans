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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.spi.editor.SideBarFactory;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.view.LockedViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ParagraphViewDescriptor;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *  Code Folding Side Bar. Component responsible for drawing folding signs and responding 
 *  on user fold/unfold action.
 * <p/>
 * The class was copied/hidden from org.netbeans.editor.CodeFoldingSidebar. If any error is fixed here,
 * please fix it as well in {@link org.netbeans.editor.CodeFoldingSidebar} in this module for backward
 * compatibility with potential users.
 *
 *  @author  Martin Roskanin
 */
public final class CodeFoldingSideBar extends JComponent implements Accessible {
    public static final String PROP_SIDEBAR_MARK = "org.netbeans.editor.CodeFoldingSidebar"; // NOI18N
    
    // logging to catch issue #231362
    private static final Logger PREF_LOG = Logger.getLogger(FoldHierarchy.class.getName() + ".enabled");
    
    private static final Logger LOG = Logger.getLogger(CodeFoldingSideBar.class.getName());

    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Color backColor;
    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Color foreColor;
    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Font font;
    
    /** This field should be treated as final. Subclasses are forbidden to change it. */
    protected /*final*/ JTextComponent component;
    private volatile AttributeSet attribs;
    private Lookup.Result<? extends FontColorSettings> fcsLookupResult;
    private final LookupListener fcsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            attribs = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //EMI: This is needed as maybe the DEFAULT_COLORING is changed, the font is different
                    // and while getMarkSize() is used in paint() and will make the artifacts bigger,
                    // the component itself will be the same size and it must be changed.
                    // See http://www.netbeans.org/issues/show_bug.cgi?id=153316
                    updatePreferredSize();
                    CodeFoldingSideBar.this.repaint();
                }
            });
        }
    };
    private final Listener listener = new Listener();
    
    private boolean enabled = false;
    
    protected List<Mark> visibleMarks = new ArrayList<Mark>();
    
    /**
     * Mouse moved point, possibly {@code null}. Set from mouse-moved, mouse-entered
     * handlers, so that painting will paint this fold in bold. -1, if mouse is not
     * in the sidebar region. The value is used to compute highlighted portions of the 
     * folding outline.
     */
    private int   mousePoint = -1;
    
    /**
     * if true, the {@link #mousePoint} has been already used to make a PaintInfo active.
     * The flag is tested by {@link #traverseForward} and {@link #traverseBackward} after children
     * of the current fold are processed and cleared if the {@link #mousePoint} falls to the fold area -
     * fields of PaintInfo are set accordingly.
     * It's also used to compute (current) mouseBoundary, so mouse movement does not trigger 
     * refreshes eagerly
     */
    private boolean mousePointConsumed;
    
    /**
     * Boundaries of the current area under the mouse. Can be eiher the span of the
     * current fold (or part of it), or the span not occupied by any fold. Serves as an optimization
     * for mouse handler, which does not trigger painting (refresh) unless mouse 
     * leaves this region.
     */
    private Rectangle   mouseBoundary;
    
    /**
     * Y-end of the nearest fold that ends above the {@link #mousePoint}. Undefined if mousePoint is null.
     * These two variables are initialized at each level of folds, and help to compute {@link #mouseBoundary} for
     * the case the mousePointer is OUTSIDE all children (or outside all folds). 
     */
    private int lowestAboveMouse = -1;

    /**
     * Y-begin of the nearest fold, which starts below the {@link #mousePoint}. Undefined if mousePoint is null
     */
    private int topmostBelowMouse = Integer.MAX_VALUE;
    
    private boolean alreadyPresent;
    
    /** Paint operations */
    public static final int PAINT_NOOP             = 0;
    /**
     * Normal opening +- marker
     */
    public static final int PAINT_MARK             = 1;
    
    /**
     * Vertical line - typically at the end of the screen
     */
    public static final int PAINT_LINE             = 2;
    
    /**
     * End angled line, without a sign
     */
    public static final int PAINT_END_MARK         = 3;
    
    /**
     * Single-line marker, both start and end
     */
    public static final int SINGLE_PAINT_MARK      = 4;
    
    /**
     * Marker value for {@link #mousePoint} indicating that mouse is outside the Component.
     */
    private static final int NO_MOUSE_POINT = -1;
    
    /**
     * Stroke used to draw inactive (regular) fold outlines.
     */
    private static Stroke LINE_DASHED = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
            1f, new float[] { 1f, 1f }, 0f);
    
    /**
     * Stroke used to draw outlines for 'active' fold
     */
    private static final Stroke LINE_BOLD = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    
    private final Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt == null ? null : evt.getKey();
            if (key == null || SimpleValueNames.CODE_FOLDING_ENABLE.equals(key)) {
                updateColors();
                
                boolean newEnabled = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
                PREF_LOG.log(Level.FINE, "Sidebar folding-enable pref change: " + newEnabled);
                if (enabled != newEnabled) {
                    enabled = newEnabled;
                    updatePreferredSize();
                }
            }
        }
    };
    
    private void checkRepaint(ViewHierarchyEvent vhe) {
        if (!vhe.isChangeY()) {
            // does not obscur sidebar graphics
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatePreferredSize();
                CodeFoldingSideBar.this.repaint();
            }
        });
    }
    
    private static boolean canDisplay(JTextComponent component) {
        Object o = component.getClientProperty(PROP_SIDEBAR_MARK);
        return (o == null || ((o instanceof JComponent) && !((JComponent)o).isVisible()));
    }

    /**
     * removeNotify will be called during sidebar rebuild, but
     * before the constructor for a new sidebar is called
     */
    @Override
    public void removeNotify() {
        Object o = component.getClientProperty(PROP_SIDEBAR_MARK);
        if (o == this) {
            component.putClientProperty(PROP_SIDEBAR_MARK, null);
        }
        super.removeNotify();
    }

    /**
     * This is just in case; usually sidebars are created anew, so the constructor
     * will set the property. But if the components are just reattached, not rebuilt for
     * whatever reason, stuff back the flag removed in addNotify.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        Object o = component.getClientProperty(PROP_SIDEBAR_MARK);
        if (o == null) {
            component.putClientProperty(PROP_SIDEBAR_MARK, this);
        }
        prefsListener.preferenceChange(null);
    }
    
    
    
    public CodeFoldingSideBar(final JTextComponent component){
        super();
        this.component = component;

        // prevent from display CF sidebar twice
        if (canDisplay(component)) {
            component.putClientProperty(PROP_SIDEBAR_MARK, this);
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Folding sidebar already present at {0}", component);
            }
            alreadyPresent = true;
            prefs = null;
            return;
        }

        addMouseListener(listener);
        addMouseMotionListener(listener);

        final FoldHierarchy foldHierarchy = FoldHierarchy.get(component);
        foldHierarchy.addFoldHierarchyListener(WeakListeners.create(FoldHierarchyListener.class, listener, foldHierarchy));

        final Document doc = getDocument();
        doc.addDocumentListener(WeakListeners.document(listener, doc));
        setOpaque(true);
        
        prefs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Code folding sidebar initialized for: {0}", doc);
        }
        
        ViewHierarchy.get(component).addViewHierarchyListener(new ViewHierarchyListener() {

            @Override
            public void viewHierarchyChanged(ViewHierarchyEvent evt) {
                checkRepaint(evt);
            }
            
        });
    }
    
    private void updatePreferredSize() {
        // do not show at all, if there are no providers registered.
        if (enabled && !alreadyPresent) {
            setPreferredSize(new Dimension(getColoring().getFont().getSize(), component.getHeight()));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }else{
            setPreferredSize(new Dimension(0,0));
            setMaximumSize(new Dimension(0,0));
        }
        revalidate();
    }
    
    /**
     * Resolves background color. Must be called after getColoring(), satisfied in updateColors.
     * @return 
     */
    private Color resolveBackColor(boolean fallback) {
        AttributeSet attr = specificAttrs;
        Color x = (Color)attr.getAttribute(StyleConstants.ColorConstants.Background);
        if (x == null) {
            Container c = getParent();
            if (c != null) {
                x = c.getBackground();
            } else if (fallback) {
                // fallback in case of inherited color and uninitialized parent
                return getColoring().getBackColor();
            }
        }
        return x;
    }

    private void updateColors() {
        Coloring c = getColoring();
        this.backColor = resolveBackColor(false); // avoid caching fallback value
        this.foreColor = c.getForeColor();
        this.font = c.getFont();
    }

    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The background color used for painting this component.
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Color getBackColor() {
        if (backColor == null) {
            updateColors();
            // avoid caching the color if the parent was not yet properly set
            if (backColor == null) {
                return getColoring().getBackColor();
            }
        }
        return backColor;
    }
    
    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The foreground color used for painting this component.
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Color getForeColor() {
        if (foreColor == null) {
            updateColors();
        }
        return foreColor;
    }
    
    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The font used for painting this component.
     * @deprecated Without any replacement.
     */
    @Deprecated
    protected Font getColoringFont() {
        if (font == null) {
            updateColors();
        }
        return font;
    }
    
    // overriding due to issue #60304
    public @Override void update(Graphics g) {
    }
    
    protected void collectPaintInfos(
        View rootView, Fold fold, Map<Integer, PaintInfo> map, int level, int startIndex, int endIndex
    ) throws BadLocationException {
        //never called
    }

    /**
     * Adjust lowest/topmost boundaries from the Fold range y1-y2.
     * @param y1
     * @param y2
     * @param level 
     */
    private void setMouseBoundaries(int y1, int y2, int level) {
        if (!hasMousePoint() || mousePointConsumed) {
            return;
        }
        int y = mousePoint;
        if (y2 < y && lowestAboveMouse < y2) {
            LOG.log(Level.FINEST, "lowestAbove at {1}: {0}", new Object[] { y2, level });
            lowestAboveMouse = y2;
        }
        if (y1 > y && topmostBelowMouse > y1) {
            LOG.log(Level.FINEST, "topmostBelow at {1}: {0}", new Object[] { y1, level });
            topmostBelowMouse = y1;
        }
    }
    
    /*
     * Even collapsed fold MAY contain a continuation line, IF one of folds on the same line is NOT collapsed. Such a fold should
     * then visually span multiple lines && be marked as collapsed.
     */

    protected List<? extends PaintInfo> getPaintInfo(Rectangle clip) throws BadLocationException {
        javax.swing.plaf.TextUI textUI = component.getUI();
        if (!(textUI instanceof BaseTextUI)) {
            return Collections.<PaintInfo>emptyList();
        }
        BaseTextUI baseTextUI = (BaseTextUI)textUI;
        BaseDocument bdoc = Utilities.getDocument(component);
        if (bdoc == null) {
            return Collections.<PaintInfo>emptyList();
        }
        mousePointConsumed = false;
        mouseBoundary = null;
        topmostBelowMouse = Integer.MAX_VALUE;
        lowestAboveMouse = -1;
        bdoc.readLock();
        try {
            int startPos = baseTextUI.getPosFromY(clip.y);
            int endPos = baseTextUI.viewToModel(component, Short.MAX_VALUE / 2, clip.y + clip.height);
            
            if (startPos < 0 || endPos < 0) {
                // editor window is not properly sized yet; return no infos
                return Collections.<PaintInfo>emptyList();
            }
            
            // #218282: if the view hierarchy is not yet updated, the Y coordinate may map to an incorrect offset outside
            // the document.
            int docLen = bdoc.getLength();
            if (startPos >= docLen || endPos > docLen) {
                return Collections.<PaintInfo>emptyList();
            }
            
            startPos = Utilities.getRowStart(bdoc, startPos);
            endPos = Utilities.getRowEnd(bdoc, endPos);
            
            FoldHierarchy hierarchy = FoldHierarchy.get(component);
            hierarchy.lock();
            try {
                View rootView = Utilities.getDocumentView(component);
                if (rootView != null) {
                    Object [] arr = getFoldList(hierarchy.getRootFold(), startPos, endPos);
                    @SuppressWarnings("unchecked")
                    List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
                    int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

                    /*
                     * Note:
                     * 
                     * The Map is keyed by Y-VISUAL position of the fold mark, not the textual offset of line start.
                     * This is because several folds may occupy the same line, while only one + sign is displayed,
                     * and affect the last fold in the row.
                     */
                    NavigableMap<Integer, PaintInfo> map = new TreeMap<Integer, PaintInfo>();
                    // search backwards
                    for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                        Fold fold = foldList.get(i);
                        if (!traverseBackwards(fold, bdoc, baseTextUI, startPos, endPos, 0, map)) {
                            break;
                        }
                    }

                    // search forward
                    for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                        Fold fold = foldList.get(i);
                        if (!traverseForward(fold, bdoc, baseTextUI, startPos, endPos, 0, map)) {
                            break;
                        }
                    }
                    
                    if (map.isEmpty() && foldList.size() > 0) {
                        if (foldList.size() != 1) {
                            LOG.log(Level.WARNING, "More folds found on screen, but no fold in paint map. foldList: {0},"
                                    + "foldHierarchy: {1}", new Object[] {
                                    foldList, hierarchy.toString()
                                    });
                        }
                        assert foldList.size() == 1;
                        PaintInfo pi = new PaintInfo(PAINT_LINE, 0, clip.y, clip.height, -1, -1);
                        mouseBoundary = new Rectangle(0, 0, 0, clip.height);
                        LOG.log(Level.FINEST, "Mouse boundary for full side line set to: {0}", mouseBoundary);
                        if (hasMousePoint()) {
                            pi.markActive(true, true, true);
                        }
                        return Collections.singletonList(pi);
                    } else {
                        if (mouseBoundary == null) {
                            mouseBoundary = makeMouseBoundary(clip.y, clip.y + clip.height);
                            LOG.log(Level.FINEST, "Mouse boundary not set, defaulting to: {0}", mouseBoundary);
                        }
                        return new ArrayList<PaintInfo>(map.values());
                    }
                } else {
                    return Collections.<PaintInfo>emptyList();
                }
            } finally {
                hierarchy.unlock();
            }
        } finally {
            bdoc.readUnlock();
        }
    }
    
    /**
     * Adds a paint info to the map. If a paintinfo already exists, it merges
     * the structures, so the painting process can just follow the instructions.
     * 
     * @param infos
     * @param yOffset
     * @param nextInfo 
     */
    private void addPaintInfo(Map<Integer, PaintInfo> infos, int yOffset, PaintInfo nextInfo) {
        PaintInfo prevInfo = infos.get(yOffset);
        nextInfo.mergeWith(prevInfo);
        infos.put(yOffset, nextInfo);
    }

    private boolean traverseForward(Fold f, BaseDocument doc, BaseTextUI btui, int lowerBoundary, int upperBoundary,int level,  NavigableMap<Integer, PaintInfo> infos) throws BadLocationException {
//        System.out.println("~~~ traverseForward<" + lowerBoundary + ", " + upperBoundary
//                + ">: fold=<" + f.getStartOffset() + ", " + f.getEndOffset() + "> "
//                + (f.getStartOffset() > upperBoundary ? ", f.gSO > uB" : "")
//                + ", level=" + level);
        
        if (f.getStartOffset() > upperBoundary) {
            return false;
        }

        int lineStartOffset1 = Utilities.getRowStart(doc, f.getStartOffset());
        int lineStartOffset2 = Utilities.getRowStart(doc, f.getEndOffset());
        int y1 = btui.getYFromPos(lineStartOffset1);
        int h = btui.getEditorUI().getLineHeight();
        int y2 = btui.getYFromPos(lineStartOffset2);
         
        // the 'active' flags can be set only after children are processed; highlights
        // correspond to the innermost expanded child.
        boolean activeMark = false;
        boolean activeIn = false;
        boolean activeOut = false;
        PaintInfo spi;
        boolean activated;
        
        if (y1 == y2) {
            // whole fold is on a single line
            spi = new PaintInfo(SINGLE_PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2);
            if (activated = isActivated(y1, y1 + h)) {
                activeMark = true;
            }
            addPaintInfo(infos, y1, spi);
        } else {
            // fold spans multiple lines
            spi = new PaintInfo(PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2);
            if (activated = isActivated(y1, y2 + h / 2)) {
                activeMark = true;
                activeOut = true;
            }
            addPaintInfo(infos, y1, spi);
        }

        setMouseBoundaries(y1, y2 + h / 2, level);

        // Handle end mark after possible inner folds were processed because
        // otherwise if there would be two nested folds both ending at the same line
        // then the end mark for outer one would be replaced by an end mark for inner one
        // (same key in infos map) and the painting code would continue to paint line marking a fold
        // until next fold is reached (or end of doc).
        PaintInfo epi = null;
        if (y1 != y2 && !f.isCollapsed() && f.getEndOffset() <= upperBoundary) {
            epi = new PaintInfo(PAINT_END_MARK, level, y2, h, lineStartOffset1, lineStartOffset2);
            addPaintInfo(infos, y2, epi);
        }

        // save the topmost/lowest information, reset for child processing
        int topmost = topmostBelowMouse;
        int lowest = lowestAboveMouse;
        topmostBelowMouse = y2 + h / 2;
        lowestAboveMouse = y1;

        try {
            if (!f.isCollapsed()) {
                Object [] arr = getFoldList(f, lowerBoundary, upperBoundary);
                @SuppressWarnings("unchecked")
                List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
                int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

                // search backwards
                for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                    Fold fold = foldList.get(i);
                    if (!traverseBackwards(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                        break;
                    }
                }

                // search forward
                for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                    Fold fold = foldList.get(i);
                    if (!traverseForward(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                        return false;
                    }
                }
            }
            if (!mousePointConsumed && activated) {
                mousePointConsumed = true;
                mouseBoundary = makeMouseBoundary(y1, y2 + h);
                LOG.log(Level.FINEST, "Mouse boundary set to: {0}", mouseBoundary);
                spi.markActive(activeMark, activeIn, activeOut);
                if (epi != null) {
                    epi.markActive(true, true, false);
                }
                markDeepChildrenActive(infos, y1, y2, level);
            }
        } finally {
            topmostBelowMouse = topmost;
            lowestAboveMouse = lowest;
        }
        return true;
    }
     
    /**
     * Sets outlines of all children to 'active'. Assuming yFrom and yTo are from-to Y-coordinates of the parent
     * fold, it finds all nested folds (folds, which are in between yFrom and yTo) and changes their in/out lines
     * as active.
     * The method returns Y start coordinate of the 1st child found.
     * 
     * @param infos fold infos collected so far
     * @param yFrom upper Y-coordinate of the parent fold
     * @param yTo lower Y-coordinate of the parent fold
     * @param level level of the parent fold
     * @return Y-coordinate of the 1st child.
     */
    private int markDeepChildrenActive(NavigableMap<Integer, PaintInfo> infos, int yFrom, int yTo, int level) {
        int result = Integer.MAX_VALUE;
        Map<Integer, PaintInfo> m = infos.subMap(yFrom, yTo);
        for (Map.Entry<Integer, PaintInfo> me : m.entrySet()) {
            PaintInfo pi = me.getValue();
            int y = pi.getPaintY();
            if (y > yFrom && y < yTo) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Marking chind as active: {0}", pi);
                }
                pi.markActive(false, true, true);
                if (y < result) {
                    y = result;
                }
            }
        }
        return result;
    }
    
    /**
     * Returns stroke appropriate for painting (in)active outlines
     * @param s the default stroke
     * @param active true for active outlines
     * @return value of 's' or a Stroke which should be used to paint the outline.
     */
    private static Stroke getStroke(Stroke s, boolean active) {
        if (active) {
            return LINE_BOLD;
        } else {
            return s;
        }
    }
    
    private boolean traverseBackwards(Fold f, BaseDocument doc, BaseTextUI btui, int lowerBoundary, int upperBoundary, int level, NavigableMap<Integer, PaintInfo> infos) throws BadLocationException {
//        System.out.println("~~~ traverseBackwards<" + lowerBoundary + ", " + upperBoundary
//                + ">: fold=<" + f.getStartOffset() + ", " + f.getEndOffset() + "> "
//                + (f.getEndOffset() < lowerBoundary ? ", f.gEO < lB" : "")
//                + ", level=" + level);

        if (f.getEndOffset() < lowerBoundary) {
            return false;
        }

        int lineStartOffset1 = Utilities.getRowStart(doc, f.getStartOffset());
        int lineStartOffset2 = Utilities.getRowStart(doc, f.getEndOffset());
        int h = btui.getEditorUI().getLineHeight();

        boolean activeMark = false;
        boolean activeIn = false;
        boolean activeOut = false;
        PaintInfo spi = null;
        PaintInfo epi = null;
        boolean activated = false;
        int y1 = 0;
        int y2 = 0;
        
        if (lineStartOffset1 == lineStartOffset2) {
            // whole fold is on a single line
            y2 = y1 = btui.getYFromPos(lineStartOffset1);
            spi = new PaintInfo(SINGLE_PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset1);
            if (activated = isActivated(y1, y1 + h)) {
                activeMark = true;
            }
            addPaintInfo(infos, y1, spi);
        } else {
            y2 = btui.getYFromPos(lineStartOffset2);
            // fold spans multiple lines
            y1 = btui.getYFromPos(lineStartOffset1);
            activated = isActivated(y1, y2 + h / 2);
            if (f.getStartOffset() >= upperBoundary) {
                spi = new PaintInfo(PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2);
                if (activated) {
                    activeMark = true;
                    activeOut = true;
                }
                addPaintInfo(infos, y1, spi);
            }

            if (!f.isCollapsed() && f.getEndOffset() <= upperBoundary) {
                activated |= isActivated(y1, y2 + h / 2);
                epi = new PaintInfo(PAINT_END_MARK, level, y2, h, lineStartOffset1, lineStartOffset2);
                addPaintInfo(infos, y2, epi);
            }
        }
        
        setMouseBoundaries(y1, y2 + h / 2, level);

        // save the topmost/lowest information, reset for child processing
        int topmost = topmostBelowMouse;
        int lowest = lowestAboveMouse;
        topmostBelowMouse = y2 + h /2;
        lowestAboveMouse = y1;

        try {
            if (!f.isCollapsed()) {
                Object [] arr = getFoldList(f, lowerBoundary, upperBoundary);
                @SuppressWarnings("unchecked")
                List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
                int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

                // search backwards
                for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                    Fold fold = foldList.get(i);
                    if (!traverseBackwards(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                        return false;
                    }
                }

                // search forward
                for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                    Fold fold = foldList.get(i);
                    if (!traverseForward(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                        break;
                    }
                }
            }
            if (!mousePointConsumed && activated) {
                mousePointConsumed = true;
                mouseBoundary = makeMouseBoundary(y1, y2 + h);
                LOG.log(Level.FINEST, "Mouse boundary set to: {0}", mouseBoundary);
                if (spi != null) {
                    spi.markActive(activeMark, activeIn, activeOut);
                }
                if (epi != null) {
                    epi.markActive(true, true, false);
                }
                int lowestChild = markDeepChildrenActive(infos, y1, y2, level);
                if (lowestChild < Integer.MAX_VALUE && lineStartOffset1 < upperBoundary) {
                    // the fold starts above the screen clip region, and is 'activated'. We need to setup instructions to draw activated line up to the
                    // 1st child marker.
                    epi = new PaintInfo(PAINT_LINE, level, y1, y2 - y1, false, lineStartOffset1, lineStartOffset2);
                    epi.markActive(true, true, false);
                    addPaintInfo(infos, y1, epi);
                }
            }
        } finally {
            topmostBelowMouse = topmost;
            lowestAboveMouse = lowest;
        }
        return true;
    }
    
    private Rectangle makeMouseBoundary(int y1, int y2) {
        if (!hasMousePoint()) {
            return null;
        }
        if (topmostBelowMouse < Integer.MAX_VALUE) {
            y2 = topmostBelowMouse;
        }
        if (lowestAboveMouse  > -1) {
            y1 = lowestAboveMouse;
        }
        return new Rectangle(0, y1, 0, y2 - y1);
    }
    
    protected EditorUI getEditorUI(){
        return Utilities.getEditorUI(component);
    }
    
    protected Document getDocument(){
        return component.getDocument();
    }


    private Fold getLastLineFold(FoldHierarchy hierarchy, int rowStart, int rowEnd, boolean shift){
        Fold fold = FoldUtilities.findNearestFold(hierarchy, rowStart);
        Fold prevFold = fold;
        while (fold != null && fold.getStartOffset()<rowEnd){
            Fold nextFold = FoldUtilities.findNearestFold(hierarchy, (fold.isCollapsed()) ? fold.getEndOffset() : fold.getStartOffset()+1);
            if (nextFold == fold) return fold;
            if (nextFold!=null && nextFold.getStartOffset() < rowEnd){
                prevFold = shift ? fold : nextFold;
                fold = nextFold;
            }else{
                return prevFold;
            }
        }
        return prevFold;
    }
    
    protected void performAction(Mark mark) {
        performAction(mark, false, false);
    }
    
    private void performActionAt(Mark mark, int mouseY) throws BadLocationException {
        if (mark != null) {
            return;
        }
        BaseDocument bdoc = Utilities.getDocument(component);
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
        
        bdoc.readLock();
        try {
            int yOffset = textUI.getPosFromY(mouseY);
            FoldHierarchy hierarchy = FoldHierarchy.get(component);
            hierarchy.lock();
            try {
                Fold f = FoldUtilities.findOffsetFold(hierarchy, yOffset);
                if (f == null) {
                    return;
                }
                if (f.isCollapsed()) {
                    LOG.log(Level.WARNING, "Clicked on a collapsed fold {0} at {1}", new Object[] { f, mouseY });
                    return;
                }
                int startOffset = f.getStartOffset();
                int endOffset = f.getEndOffset();
                
                int startY = textUI.getYFromPos(startOffset);
                int nextLineOffset = Utilities.getRowStart(bdoc, startOffset, 1);
                int nextY = textUI.getYFromPos(nextLineOffset);

                if (mouseY >= startY && mouseY <= nextY) {
                    LOG.log(Level.FINEST, "Starting line clicked, ignoring. MouseY={0}, startY={1}, nextY={2}",
                            new Object[] { mouseY, startY, nextY });
                    return;
                }

                startY = textUI.getYFromPos(endOffset);
                nextLineOffset = Utilities.getRowStart(bdoc, endOffset, 1);
                nextY = textUI.getYFromPos(nextLineOffset);

                if (mouseY >= startY && mouseY <= nextY) {
                    // the mouse can be positioned above the marker (the fold found above), or
                    // below it; in that case, the immediate enclosing fold should be used - should be the fold
                    // that corresponds to the nextLineOffset, if any
                    int h2 = (startY + nextY) / 2;
                    if (mouseY >= h2) {
                        Fold f2 = f;
                        
                        f = FoldUtilities.findOffsetFold(hierarchy, nextLineOffset);
                        if (f == null) {
                            // fold does not exist for the position below end-of-fold indicator
                            return;
                        }
                    }
                    
                }
                
                LOG.log(Level.FINEST, "Collapsing fold: {0}", f);
                hierarchy.collapse(f);
            } finally {
                hierarchy.unlock();
            }
        } finally {
            bdoc.readUnlock();
        }        
    }
    
    private void performAction(final Mark mark, final boolean shiftFold, final boolean recursive) {
        Document doc = component.getDocument();
        doc.render(new Runnable() {
            @Override
            public void run() {
                ViewHierarchy vh = ViewHierarchy.get(component);
                LockedViewHierarchy lockedVH = vh.lock();
                try {
                    int pViewIndex = lockedVH.yToParagraphViewIndex(mark.y + mark.size / 2);
                    if (pViewIndex >= 0) {
                        ParagraphViewDescriptor pViewDesc = lockedVH.getParagraphViewDescriptor(pViewIndex);
                        int pViewStartOffset = pViewDesc.getStartOffset();
                        int pViewEndOffset = pViewStartOffset + pViewDesc.getLength();
                        // Find corresponding fold
                        FoldHierarchy foldHierarchy = FoldHierarchy.get(component);
                        foldHierarchy.lock();
                        try {
                            int rowStart = javax.swing.text.Utilities.getRowStart(component, pViewStartOffset);
                            int rowEnd = javax.swing.text.Utilities.getRowEnd(component, pViewStartOffset);
                            Fold clickedFold = getLastLineFold(foldHierarchy, rowStart, rowEnd, shiftFold);//FoldUtilities.findNearestFold(foldHierarchy, viewStartOffset);
                            if (clickedFold != null && clickedFold.getStartOffset() < pViewEndOffset) {
                                if (recursive) {
                                    List<Fold> folds = new ArrayList<Fold>(FoldUtilities.findRecursive(clickedFold));
                                    Collections.reverse(folds);
                                    folds.add(clickedFold);
                                    if (clickedFold.isCollapsed()) {
                                        foldHierarchy.expand(folds);
                                    } else {
                                        foldHierarchy.collapse(folds);
                                    }
                                } else {
                                    foldHierarchy.toggle(clickedFold);
                                }
                            }
                        } catch (BadLocationException ble) {
                            LOG.log(Level.WARNING, null, ble);
                        } finally {
                            foldHierarchy.unlock();
                        }
                    }
                } finally {
                    lockedVH.unlock();
                }
            }
        });
    }
    
    protected int getMarkSize(Graphics g){
        if (g != null){
            FontMetrics fm = g.getFontMetrics(getColoring().getFont());
            if (fm != null){
                int ret = fm.getAscent() - fm.getDescent();
                return ret - ret%2;
            }
        }
        return -1;
    }
    
    private boolean hasMousePoint() {
        return mousePoint >= 0;
    }
    
    private boolean isActivated(int y1, int y2) {
        return hasMousePoint() && 
               (mousePoint >= y1 && mousePoint < y2);
    }
    
    private void drawFoldLine(Graphics2D g2d, boolean active, int x1, int y1, int x2, int y2) {
        Stroke origStroke = g2d.getStroke();
        g2d.setStroke(getStroke(origStroke, active));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(origStroke);
    }
    
    protected @Override void paintComponent(Graphics g) {
        if (!enabled) {
            return;
        }
        
        Rectangle clip = getVisibleRect();//g.getClipBounds();
        visibleMarks.clear();
        
        Coloring coloring = getColoring();
        g.setColor(resolveBackColor(true));
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(coloring.getForeColor());

        AbstractDocument adoc = (AbstractDocument)component.getDocument();
        adoc.readLock();
        try {
            List<? extends PaintInfo> ps = getPaintInfo(clip);
            Font defFont = coloring.getFont();
            int markSize = getMarkSize(g);
            int halfMarkSize = markSize / 2;
            int markX = (defFont.getSize() - markSize) / 2; // x position of mark rectangle
            int plusGap = (int)Math.round(markSize / 3.8); // distance between mark rectangle vertical side and start/end of minus sign
            int lineX = markX + halfMarkSize; // x position of the centre of mark

            LOG.finer("CFSBar: PAINT START ------\n");
            int descent = g.getFontMetrics(defFont).getDescent();
            PaintInfo previousInfo = null;
            Graphics2D g2d = (Graphics2D)g;
            LOG.log(Level.FINEST, "MousePoint: {0}", mousePoint);

            for(PaintInfo paintInfo : ps) {
                boolean isFolded = paintInfo.isCollapsed();
                int y = paintInfo.getPaintY();
                int height = paintInfo.getPaintHeight();
                int markY = y + descent; // y position of mark rectangle
                int paintOperation = paintInfo.getPaintOperation();

                if (previousInfo == null) {
                    if (paintInfo.hasLineIn()) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("prevInfo=NULL; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                        drawFoldLine(g2d, paintInfo.lineInActive, lineX, clip.y, lineX, y);
                    }
                } else {
                    if (previousInfo.hasLineOut() || paintInfo.hasLineIn()) {
                        // Draw middle vertical line
                        int prevY = previousInfo.getPaintY();
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.log(Level.FINER, "prevInfo={0}; y=" + y + ", PI:" + paintInfo + "\n", previousInfo); // NOI18N
                        }
                        drawFoldLine(g2d, previousInfo.lineOutActive || paintInfo.lineInActive, lineX, prevY + previousInfo.getPaintHeight(), lineX, y);
                    }
                }

                if (paintInfo.hasSign()) {
                    g.drawRect(markX, markY, markSize, markSize);
                    g.drawLine(plusGap + markX, markY + halfMarkSize, markSize + markX - plusGap, markY + halfMarkSize);
                    String opStr = (paintOperation == PAINT_MARK) ? "PAINT_MARK" : "SINGLE_PAINT_MARK"; // NOI18N
                    if (isFolded) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer(opStr + ": folded; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                        g.drawLine(lineX, markY + plusGap, lineX, markY + markSize - plusGap);
                    }
                    if (paintOperation != SINGLE_PAINT_MARK) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer(opStr + ": non-single; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                    }
                    if (paintInfo.hasLineIn()) { //[PENDING]
                        drawFoldLine(g2d, paintInfo.lineInActive, lineX, y, lineX, markY);
                    }
                    if (paintInfo.hasLineOut()) {
                        // This is an error in case there's a next paint info at the same y which is an end mark
                        // for this mark (it must be cleared explicitly).
                        drawFoldLine(g2d, paintInfo.lineOutActive, lineX, markY + markSize, lineX, y + height);
                    }
                    visibleMarks.add(new Mark(markX, markY, markSize, isFolded));

                } else if (paintOperation == PAINT_LINE) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("PAINT_LINE: y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                    }
                    // FIXME !!
                    drawFoldLine(g2d, paintInfo.signActive, lineX, y, lineX, y + height );
                } else if (paintOperation == PAINT_END_MARK) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("PAINT_END_MARK: y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                    }
                    if (previousInfo == null || y != previousInfo.getPaintY()) {
                        drawFoldLine(g2d, paintInfo.lineInActive, lineX, y, lineX, y + height / 2);
                        drawFoldLine(g2d, paintInfo.signActive, lineX, y + height / 2, lineX + halfMarkSize, y + height / 2);
                        if (paintInfo.getInnerLevel() > 0) {//[PENDING]
                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.finer("  PAINT middle-line\n"); // NOI18N
                            }
                            drawFoldLine(g2d, paintInfo.lineOutActive, lineX, y + height / 2, lineX, y + height);
                        }
                    }
                }

                previousInfo = paintInfo;
            }

            if (previousInfo != null &&
                (previousInfo.getInnerLevel() > 0 ||
                 (previousInfo.getPaintOperation() == PAINT_MARK && !previousInfo.isCollapsed()))
            ) {
                drawFoldLine(g2d, previousInfo.lineOutActive, 
                        lineX, previousInfo.getPaintY() + previousInfo.getPaintHeight(), lineX, clip.y + clip.height);
            }

        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
        } finally {
            LOG.finer("CFSBar: PAINT END ------\n\n");
            adoc.readUnlock();
        }
    }
    
    private static Object [] getFoldList(Fold parentFold, int start, int end) {
        List<Fold> ret = new ArrayList<Fold>();

        int index = FoldUtilities.findFoldEndIndex(parentFold, start);
        int foldCount = parentFold.getFoldCount();
        int idxOfFirstFoldStartingInside = -1;
        while (index < foldCount) {
            Fold f = parentFold.getFold(index);
            if (f.getStartOffset() <= end) {
                ret.add(f);
            } else {
                break; // no more relevant folds
            }
            if (idxOfFirstFoldStartingInside == -1 && f.getStartOffset() >= start) {
                idxOfFirstFoldStartingInside = ret.size() - 1;
            }
            index++;
        }

        return new Object [] { ret, idxOfFirstFoldStartingInside != -1 ? idxOfFirstFoldStartingInside : ret.size() };
    }

    /**
     * This class should be never used by other code; will be made private
     */
    public class PaintInfo {
        
        int paintOperation;
        /**
         * level of the 1st marker on the line
         */
        int innerLevel;
        
        /**
         * Y-coordinate of the cell
         */
        int paintY;
        
        /**
         * Height of the paint cell
         */
        int paintHeight;
        
        /**
         * State of the marker (+/-)
         */
        boolean isCollapsed;
        
        /**
         * all markers on the line are collapsed
         */
        boolean allCollapsed;
        int startOffset;
        int endOffset;
        /**
         * nesting level of the last marker on the line
         */
        int outgoingLevel;
        
        /**
         * Force incoming line (from above) to be present
         */
        boolean lineIn;
        
        /**
         * Force outgoing line (down from marker) to be present
         */
        boolean lineOut;
        
        /**
         * The 'incoming' (upper) line should be painted as active
         */
        boolean lineInActive;
        
        /**
         * The 'outgoing' (down) line should be painted as active
         */
        boolean lineOutActive;
        
        /**
         * The sign/marker itself should be painted as active
         */
        boolean signActive;
        
        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight, boolean isCollapsed, int startOffset, int endOffset){
            this.paintOperation = paintOperation;
            this.innerLevel = this.outgoingLevel = innerLevel;
            this.paintY = paintY;
            this.paintHeight = paintHeight;
            this.isCollapsed = this.allCollapsed = isCollapsed;
            this.startOffset = startOffset;
            this.endOffset = endOffset;

            switch (paintOperation) {
                case PAINT_MARK:
                    lineIn = false;
                    lineOut = true;
                    outgoingLevel++;
                    break;
                case SINGLE_PAINT_MARK:
                    lineIn = false;
                    lineOut = false;
                    break;
                case PAINT_END_MARK:
                    lineIn = true;
                    lineOut = false;
                    isCollapsed = true;
                    allCollapsed = true;
                    break;
                case PAINT_LINE:
                    lineIn = lineOut = true;
                    break;
            }
        }
        
        /**
         * Sets active flags on inidivual parts of the mark
         * @param mark
         * @param lineIn
         * @param lineOut S
         */
        void markActive(boolean mark, boolean lineIn, boolean lineOut) {
            this.signActive |= mark;
            this.lineInActive |= lineIn;
            this.lineOutActive |= lineOut;
        }
        
        boolean hasLineIn() {
            return lineIn || innerLevel > 0;
        }
        
        boolean hasLineOut() {
            return lineOut || outgoingLevel > 0 || (paintOperation != SINGLE_PAINT_MARK && !isAllCollapsed());
        }

        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight, int startOffset, int endOffset){
            this(paintOperation, innerLevel, paintY, paintHeight, false, startOffset, endOffset);
        }
        
        public int getPaintOperation(){
            return paintOperation;
        }
        
        public int getInnerLevel(){
            return innerLevel;
        }
        
        public int getPaintY(){
            return paintY;
        }
        
        public int getPaintHeight(){
            return paintHeight;
        }
        
        public boolean isCollapsed(){
            return isCollapsed;
        }
        
         boolean isAllCollapsed() {
            return allCollapsed;
        }
        
        public void setPaintOperation(int paintOperation){
            this.paintOperation = paintOperation;
        }
        
        public void setInnerLevel(int innerLevel){
            this.innerLevel = innerLevel;
        }
        
        public @Override String toString(){
            StringBuffer sb = new StringBuffer("");
            if (paintOperation == PAINT_MARK){
                sb.append("PAINT_MARK"); // NOI18N
            }else if (paintOperation == PAINT_LINE){
                sb.append("PAINT_LINE"); // NOI18N
            }else if (paintOperation == PAINT_END_MARK) {
                sb.append("PAINT_END_MARK"); // NOI18N
            }else if (paintOperation == SINGLE_PAINT_MARK) {
                sb.append("SINGLE_PAINT_MARK");
            }
            sb.append(",L:").append(innerLevel).append("/").append(outgoingLevel); // NOI18N
            sb.append(',').append(isCollapsed ? "C" : "E"); // NOI18N
            sb.append(", start=").append(startOffset).append(", end=").append(endOffset);
            sb.append(", lineIn=").append(lineIn).append(", lineOut=").append(lineOut);
            return sb.toString();
        }
        
        boolean hasSign() {
            return paintOperation == PAINT_MARK || paintOperation == SINGLE_PAINT_MARK;
        }
        
        
        void mergeWith(PaintInfo prevInfo) {
            if (prevInfo == null) {
                return;
            }

            int operation = this.paintOperation;
            boolean lineIn = prevInfo.lineIn;
            boolean lineOut = prevInfo.lineOut;
            
            LOG.log(Level.FINER, "Merging {0} with {1}: ", new Object[] { this, prevInfo });
            if (prevInfo.getPaintOperation() == PAINT_END_MARK) {
                // merge with start|single -> start mark + line-in
                lineIn = true;
            } else if (prevInfo.getPaintOperation() != SINGLE_PAINT_MARK) {
                operation = PAINT_MARK;
            }

            int level1 = Math.min(prevInfo.innerLevel, innerLevel);
            int level2 = prevInfo.outgoingLevel;

            if (getPaintOperation() == PAINT_END_MARK 
                && innerLevel == prevInfo.outgoingLevel) {
                // if merging end marker at the last level, update to the new outgoing level
                level2 = outgoingLevel;
            } else if (!isCollapsed) {
                level2 = Math.max(prevInfo.outgoingLevel, outgoingLevel);
            }

            if (prevInfo.getInnerLevel() < getInnerLevel()) {
                int paintFrom = Math.min(prevInfo.paintY, paintY);
                int paintTo = Math.max(prevInfo.paintY + prevInfo.paintHeight, paintY + paintHeight);
                // at least one collapsed -> paint plus sign
                boolean collapsed = prevInfo.isCollapsed() || isCollapsed();
                int offsetFrom = Math.min(prevInfo.startOffset, startOffset);
                int offsetTo = Math.max(prevInfo.endOffset, endOffset);
                
                this.paintY = paintFrom;
                this.paintHeight = paintTo - paintFrom;
                this.isCollapsed = collapsed;
                this.startOffset = offsetFrom;
                this.endOffset = offsetTo;
            }
            this.paintOperation = operation;
            this.allCollapsed = prevInfo.allCollapsed && allCollapsed;
            this.innerLevel = level1;
            this.outgoingLevel = level2;
            this.lineIn |= lineIn;
            this.lineOut |= lineOut;
            
            this.signActive |= prevInfo.signActive;
            this.lineInActive |= prevInfo.lineInActive;
            this.lineOutActive |= prevInfo.lineOutActive;
            
            LOG.log(Level.FINER, "Merged result: {0}", this);
        }
    }
    
    /** Keeps info of visible folding mark */
    public class Mark{
        public int x;
        public int y;
        public int size;
        public boolean isFolded;
        
        public Mark(int x, int y, int size, boolean isFolded){
            this.x = x;
            this.y = y;
            this.size = size;
            this.isFolded = isFolded;
        }
    }
    
    private final class Listener extends MouseAdapter implements FoldHierarchyListener, DocumentListener, Runnable {
    
        public Listener(){
        }

        // --------------------------------------------------------------------
        // FoldHierarchyListener implementation
        // --------------------------------------------------------------------

        public void foldHierarchyChanged(FoldHierarchyEvent evt) {
            refresh();
        }

        // --------------------------------------------------------------------
        // DocumentListener implementation
        // --------------------------------------------------------------------

        public void insertUpdate(DocumentEvent evt) {
            if (!(evt instanceof BaseDocumentEvent)) return;

            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if (bevt.getLFCount() > 0) { // one or more lines inserted
                refresh();
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            if (!(evt instanceof BaseDocumentEvent)) return;

            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if (bevt.getLFCount() > 0) { // one or more lines removed
                refresh();
            }
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        // --------------------------------------------------------------------
        // MouseListener implementation
        // --------------------------------------------------------------------

        @Override
        public void mousePressed (MouseEvent e) {
            Mark mark = getClickedMark(e);
            if (mark!=null){
                e.consume();
                performAction(mark, (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // #102288 - missing event consuming caused quick doubleclicks to break
            // fold expanding/collapsing and move caret to the particular line
            if (e.getClickCount() > 1) {
                LOG.log(Level.FINEST, "Mouse {0}click at {1}", new Object[] { e.getClickCount(), e.getY()});
                Mark mark = getClickedMark(e);
                try {
                    performActionAt(mark, e.getY());
                } catch (BadLocationException ex) {
                    LOG.log(Level.WARNING, "Error during fold expansion using sideline", ex);
                }
            } else {
                e.consume();
            }
        }

        private void refreshIfMouseOutside(Point pt) {
            mousePoint = (int)pt.getY();
            if (LOG.isLoggable(Level.FINEST)) {
                if (mouseBoundary == null) {
                    LOG.log(Level.FINEST, "Mouse boundary not set, refreshing: {0}", mousePoint);
                } else {
                    LOG.log(Level.FINEST, "Mouse {0} inside known mouse boundary: {1}-{2}", 
                            new Object[] { mousePoint, mouseBoundary.y, mouseBoundary.getMaxY() });
                }
            }
            if (mouseBoundary == null || mousePoint < mouseBoundary.y || mousePoint > mouseBoundary.getMaxY()) {
                refresh();
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            refreshIfMouseOutside(e.getPoint());
        }
        
        public void mouseEntered(MouseEvent e) {
            refreshIfMouseOutside(e.getPoint());
        }
        
        public void mouseExited(MouseEvent e) {
            mousePoint = NO_MOUSE_POINT;
            refresh();
        }
        

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private Mark getClickedMark(MouseEvent e){
            if (e == null || !SwingUtilities.isLeftMouseButton(e)) {
                return null;
            }
            
            int x = e.getX();
            int y = e.getY();
            for (Mark mark : visibleMarks) {
                if (x >= mark.x && x <= (mark.x + mark.size) && y >= mark.y && y <= (mark.y + mark.size)) {
                    return mark;
                }
            }
            return null;
        }

        private void refresh() {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            if (getPreferredSize().width == 0 && enabled) {
                updatePreferredSize();
            }
            repaint();
        }
    } // End of Listener class
    
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                public @Override AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
            accessibleContext.setAccessibleName(NbBundle.getMessage(CodeFoldingSideBar.class, "ACSN_CodeFoldingSideBar")); //NOI18N
        accessibleContext.setAccessibleDescription(NbBundle.getMessage(CodeFoldingSideBar.class, "ACSD_CodeFoldingSideBar")); //NOI18N
        }
        return accessibleContext;
    }
    
    private AttributeSet specificAttrs;

    private Coloring getColoring() {
        if (attribs == null) {
            if (fcsLookupResult == null) {
                fcsLookupResult = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component))
                        .lookupResult(FontColorSettings.class);
                fcsLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, fcsTracker, fcsLookupResult));
            }
            
            FontColorSettings fcs = fcsLookupResult.allInstances().iterator().next();
            AttributeSet attr = fcs.getFontColors(FontColorNames.CODE_FOLDING_BAR_COLORING);
            specificAttrs = attr;
            if (attr == null) {
                attr = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            } else {
                attr = AttributesUtilities.createComposite(
                        attr, 
                        fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
            }
            attribs = attr;
        }        
        return Coloring.fromAttributeSet(attribs);
    }

    /**
     * Factory for the sidebars. Use {@link FoldUtilities#getFoldingSidebarFactory()} to
     * obtain an instance.
     */
    public static class Factory implements SideBarFactory {
        @Override
        public JComponent createSideBar(JTextComponent target) {
            if (!canDisplay(target)) {
                return null;
            }
            FoldHierarchy fh = FoldHierarchy.get(target);
            if (fh == null || !fh.isActive()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Fold hierarchy not active for: {0}", target);
                }
                return null;
            }
            return new CodeFoldingSideBar(target);
        }
    }
}
