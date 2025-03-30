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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.swing.text.BadLocationException;
import org.netbeans.api.diff.Difference;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.ImageUtilities;

/**
 * Draws both line numbers and diff actions for a decorated editor pane.
 * 
 * @author Maros Sandor
 */
class LineNumbersActionsBar extends JPanel implements Scrollable, MouseMotionListener, MouseListener, PropertyChangeListener {

    private static final int ACTIONS_BAR_WIDTH = 16;
    private static final int LINES_BORDER_WIDTH = 4;
    private static final Point POINT_ZERO = new Point(0, 0);
    
    private final Icon insertIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/insert.png"); // NOI18N
    private final Icon removeIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/remove.png"); // NOI18N

    private final Icon insertActiveIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/insert_active.png"); // NOI18N
    private final Icon removeActiveIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/remove_active.png"); // NOI18N
    
    private final DiffContentPanel master;
    private final boolean actionsEnabled;
    private final int actionIconsHeight;
    private final int actionIconsWidth;

    private final String  lineNumberPadding = "        "; // NOI18N

    private int     linesWidth;
    private int     actionsWidth;
    
    private Color   linesColor;
    private int     linesCount;
    private int     maxNumberCount;

    private Point   lastMousePosition = POINT_ZERO;
    private HotSpot lastHotSpot = null;
    
    private List<HotSpot> hotspots = new ArrayList<HotSpot>(0);

    public LineNumbersActionsBar(DiffContentPanel master, boolean actionsEnabled) {
        this.master = master;
        this.actionsEnabled = actionsEnabled;
        actionsWidth = actionsEnabled ? ACTIONS_BAR_WIDTH : 0;
        actionIconsHeight = insertIcon.getIconHeight();
        actionIconsWidth = insertIcon.getIconWidth();
        setOpaque(true);
        setToolTipText(""); // NOI18N
        master.getMaster().addPropertyChangeListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void addNotify() {
        super.addNotify();
        initUI();
    }

    public void removeNotify() {
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }
    
    private Font getLinesFont() {
        String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        Coloring col = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING));
        Font font = col.getFont();
        if (font == null) {
            font = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.DEFAULT_COLORING)).getFont();
        }
        return font;
    }
    
    private void initUI() {
        String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        AttributeSet attrs = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
        AttributeSet defAttrs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        
        linesColor = (Color) attrs.getAttribute(StyleConstants.Foreground);
        if (linesColor == null) {
            linesColor = (Color) defAttrs.getAttribute(StyleConstants.Foreground);
        }
        Color bg = (Color) attrs.getAttribute(StyleConstants.Background);
        if (bg == null) {
            bg = (Color) defAttrs.getAttribute(StyleConstants.Background);
        }
        setBackground(bg);
        
        updateStateOnDocumentChange();
    }

    private HotSpot getHotspotAt(Point p) {
        for (HotSpot hotspot : hotspots) {
          if (hotspot.getRect().contains(p)) {
              return hotspot;
          }
        }
        return null;
    }
    
    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        HotSpot spot = getHotspotAt(p);
        if (spot == null) return null;
        Difference diff = spot.getDiff();
        if (diff.getType() == Difference.ADD) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Remove"); // NOI18N
        } else if (diff.getType() == Difference.CHANGE) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Replace"); // NOI18N
        } else {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Insert"); // NOI18N
        }
    }

    private void performAction(HotSpot spot) {
        master.getMaster().rollback(spot.getDiff());
    }
    
    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            HotSpot spot = getHotspotAt(e.getPoint());
            if (spot != null) {
                performAction(spot);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        lastMousePosition = POINT_ZERO;
        if (lastHotSpot != null) {
            repaint(lastHotSpot.getRect());
        }
        lastHotSpot = null;
    }
    
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        lastMousePosition = p;
        HotSpot spot = getHotspotAt(p);
        if (lastHotSpot != spot) {
            repaint(lastHotSpot == null ? spot.getRect() : lastHotSpot.getRect());
        }
        lastHotSpot = spot;
        setCursor(spot != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }
    
    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    void onUISettingsChanged() {
        initUI();        
        updateStateOnDocumentChange();
        repaint();
    }
    
    private void updateStateOnDocumentChange() {
        assert SwingUtilities.isEventDispatchThread();
        StyledDocument doc = (StyledDocument) master.getEditorPane().getDocument();
        int lastOffset = doc.getEndPosition().getOffset();
        linesCount = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);            

        Graphics g = getGraphics(); 
        if (g != null) checkLinesWidth(g);
        maxNumberCount = getNumberCount(linesCount);
        revalidate();
    }
    
    private int oldLinesWidth;
    
    private boolean checkLinesWidth(Graphics g) {
        FontMetrics fm = g.getFontMetrics(getLinesFont());
        Rectangle2D rect = fm.getStringBounds(Integer.toString(linesCount), g);
        linesWidth = (int) rect.getWidth() + LINES_BORDER_WIDTH * 2;
        if (linesWidth != oldLinesWidth) {
            oldLinesWidth = linesWidth;
            revalidate();
            repaint();
            return true;
        }
        return false;
    }
    
    private int getNumberCount(int n) {
        int nc = 0;
        for (; n > 0; n /= 10, nc++);
        return nc;
    }

    public Dimension getPreferredScrollableViewportSize() {
        Dimension dim = master.getEditorPane().getPreferredScrollableViewportSize();
        return new Dimension(getBarWidth(), dim.height);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return master.getEditorPane().getScrollableUnitIncrement(visibleRect, orientation, direction);//123
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return master.getEditorPane().getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getBarWidth(), Integer.MAX_VALUE >> 2);
    }

    private int getBarWidth() {
        return actionsWidth + linesWidth;
    }

    public void onDiffSetChanged() {
        updateStateOnDocumentChange();
        repaint();
    }

    protected void paintComponent(Graphics gr) {
        final Graphics2D g = (Graphics2D) gr;
        final Rectangle clip = g.getClipBounds();
        Stroke cs = g.getStroke();

        if (checkLinesWidth(gr)) return;
        
        String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        Map renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        if (!renderingHints.isEmpty()) {
            g.addRenderingHints(renderingHints);
        }
        
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(master.getEditorPane());
        final int lineHeight = editorUI.getLineHeight();
        
        g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.setColor(UIManager.getColor("controlShadow"));
        int x = master.isFirst() ? 0 : getBarWidth() - 1;
        g.drawLine(x, clip.y, x, clip.y + clip.height - 1);

        DiffViewManager.DecoratedDifference [] diffs = master.getMaster().getManager().getDecorations();

        int actionsYOffset = (lineHeight - actionIconsHeight) / 2;
        int offset = linesWidth;

        int currentDifference = master.getMaster().getCurrentDifference();
        List<HotSpot> newActionIcons = new ArrayList<HotSpot>();
        int idx = 0;
        for (DiffViewManager.DecoratedDifference dd : diffs) {
            int bottom = master.isFirst() ? dd.getBottomLeft() : dd.getBottomRight();
            int top = master.isFirst() ? dd.getTopLeft() : dd.getTopRight();
            g.setColor(master.getMaster().getColorLines());
            g.setStroke(currentDifference == idx ? master.getMaster().getBoldStroke() : cs);
            g.drawLine(0, top, clip.width, top);
            if (bottom != -1) {
                g.drawLine(0, bottom, clip.width, bottom);
            }
            if (actionsEnabled && dd.canRollback()) {
                if (master.isFirst() && dd.getDiff().getType() != Difference.ADD
                        || !master.isFirst() && dd.getDiff().getType() == Difference.ADD) {
                    Icon activeIcon = master.isFirst() ? insertActiveIcon : removeActiveIcon;
                    Icon icon = master.isFirst() ? insertIcon : removeIcon;
                    Rectangle hotSpot = new Rectangle((master.isFirst() ? 0 : offset) + 1, top + actionsYOffset, actionIconsWidth, actionIconsHeight);
                    if (hotSpot.contains(lastMousePosition) || idx == currentDifference) {
                        activeIcon.paintIcon(null, g, hotSpot.x, hotSpot.y);
                    } else {
                        icon.paintIcon(null, g, hotSpot.x, hotSpot.y);
                    }
                    newActionIcons.add(new HotSpot(hotSpot, dd.getDiff()));
                }
            }
            idx++;
        }
        hotspots = newActionIcons;
        
        final int linesXOffset = (master.isFirst() ? actionsWidth : 0) + LINES_BORDER_WIDTH;
        
        g.setFont(getLinesFont()); 
        g.setColor(linesColor);
        
        Utilities.runViewHierarchyTransaction(master.getEditorPane(), true, new Runnable() {
            @Override
            public void run() {
                try {
                    View rootView = Utilities.getDocumentView(master.getEditorPane());
                    if(rootView == null) { // this might happen
                        return;
                    }
                    int lineNumber = Utilities.getLineOffset((BaseDocument) master.getEditorPane().getDocument(), master.getEditorPane().viewToModel(new Point(clip.x, clip.y)));
                    if (lineNumber > 0) {
                        --lineNumber;
                    }
                    View view = rootView.getView(lineNumber);
                    if(view == null) { // this might happen
                        return;
                    }
                    Rectangle rec = master.getEditorPane().modelToView(view.getStartOffset());
                    if (rec == null) {
                        return;
                    }
                    int yOffset;
                    int localLineHeight = rec.height;
                    int linesDrawn = clip.height / localLineHeight + 4;  // draw past clipping rectangle to avoid partially drawn numbers
                    int docLines = Utilities.getRowCount((BaseDocument) master.getEditorPane().getDocument());
                    if (lineNumber + linesDrawn > docLines) {
                        linesDrawn = docLines - lineNumber;
                    }
                    for (int i = 0; i < linesDrawn; i++) {
                        view = rootView.getView(lineNumber);
                        if (view == null) {
                            break;
                        }
                        Rectangle rec1 = master.getEditorPane().modelToView(view.getStartOffset());
                        Rectangle rec2 = master.getEditorPane().modelToView(view.getEndOffset() - 1);
                        if (rec1 == null || rec2 == null) {
                            break;
                        }
                        yOffset = rec1.y + rec1.height - rec1.height / 4;
                        localLineHeight = (int) (rec2.getY() + rec2.getHeight() - rec1.getY());
                        g.drawString(formatLineNumber(++lineNumber), linesXOffset, yOffset);
                    }
                } catch (BadLocationException ex) {
                    //
                }
            }
        });
    }
    
    private String formatLineNumber(int lineNumber) {
        String strNumber = Integer.toString(lineNumber);
        int nc = getNumberCount(lineNumber);
        if (nc < maxNumberCount) {
            StringBuilder sb = new StringBuilder(10);
            sb.append(lineNumberPadding, 0, maxNumberCount - nc);
            sb.append(strNumber);
            return sb.toString();
        } else {
            return strNumber;
        }
    }

}
