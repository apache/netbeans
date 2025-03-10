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

import org.openide.util.NbBundle;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.border.Border;
import javax.swing.*;
import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;
import org.openide.awt.GraphicsUtils;
import org.openide.util.ImageUtilities;

/**
 * Split pane divider with Diff decorations.
 * 
 * @author Maros Sandor
 */
class DiffSplitPaneDivider extends BasicSplitPaneDivider implements MouseMotionListener, MouseListener, Accessible {
    
    private final Icon insertAllIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/move_all.png"); // NOI18N
    private final Icon insertAllActiveIcon = ImageUtilities.loadIcon("org/netbeans/modules/diff/builtin/visualizer/editable/move_all_active.png"); // NOI18N
    private final int actionIconsHeight;
    private final int actionIconsWidth;
    private final Point POINT_ZERO = new Point(0, 0);
    
    private final EditableDiffView master;

    private Point lastMousePosition = POINT_ZERO;
    private DividerAction lastHotSpot = null;
    private java.util.List<DividerAction> hotspots = new ArrayList<>(0);
    
    private DiffSplitDivider mydivider;
    
    private final Color fontColor;
    
    DiffSplitPaneDivider(BasicSplitPaneUI splitPaneUI, EditableDiffView master) {
        super(splitPaneUI);
        this.master = master;
        fontColor = new JLabel().getForeground();

        actionIconsHeight = insertAllIcon.getIconHeight();
        actionIconsWidth = insertAllIcon.getIconWidth();
        
        setBorder(null);
        setLayout(new BorderLayout());
        mydivider = new DiffSplitDivider();
        add(mydivider);
        
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            Action spot = getHotspotAt(e.getPoint());
            if (spot != null) {
                spot.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        lastMousePosition = POINT_ZERO;
        if (lastHotSpot != null) {
            mydivider.repaint(lastHotSpot.getRect());
        }
        lastHotSpot = null;
    }            

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }
    
    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        lastMousePosition = p;
        DividerAction spot = getHotspotAt(p);
        if (lastHotSpot != spot) {
            mydivider.repaint(lastHotSpot == null ? spot.getRect() : lastHotSpot.getRect());
        }
        lastHotSpot = spot;
        setCursor(spot != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        if (spot != null) {
            ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(mydivider, 0, 0, 0,
                    spot.getRect().x + 5 , spot.getRect().y + 5, 0, false));
        } else {
            ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(mydivider, 0, 0, 0, 0, 0, 0, false));
            mydivider.repaint();
        }
    }
    
    public void setBorder(Border border) {
        super.setBorder(BorderFactory.createEmptyBorder());
    }

    DiffSplitDivider getDivider() {
        return mydivider;
    }

    private DividerAction getHotspotAt(Point p) {
        for (DividerAction hotspot : hotspots) {
          if (hotspot.getRect().contains(p)) {
              return hotspot;
          }
        }
        return null;
    }
    
    @NbBundle.Messages({
        "TT_DiffPanel_JumpToCurrent=Go to Current Difference"
    })
    private class DiffSplitDivider extends JPanel {
        private final DividerAction rollbackAction = new DividerAction(NbBundle.getMessage(DiffSplitDivider.class, "TT_DiffPanel_MoveAll"), null) { //NOI18N

            @Override
            public void actionPerformed (ActionEvent e) {
                master.rollback(null);
            }

        };
        private final DividerAction jumpToCurrentAction = new DividerAction(Bundle.TT_DiffPanel_JumpToCurrent(), null) {

            @Override
            public void actionPerformed (ActionEvent e) {
                int diff = master.getCurrentDifference();
                master.setCurrentDifference(diff);
            }

        };

        public DiffSplitDivider() {
            setBackground(UIManager.getColor("SplitPane.background")); // NOI18N
            setOpaque(true);
            
            // aqua background workaround
            if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {         // NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); // NOI18N
            }
        }

        public String getToolTipText(MouseEvent event) {
            Point p = event.getPoint();
            DividerAction spot = getHotspotAt(p);
            return spot == null ? null : spot.getValue(Action.NAME).toString();
        }
        
        protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            Rectangle clip = g.getClipBounds();
            Stroke cs = g.getStroke();
            List<DividerAction> newActionIcons = new ArrayList<>();
            
            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

            if (master.getEditorPane1() == null) {
                g.dispose();
                return;
            }
        
            Rectangle rightView = master.getEditorPane2().getScrollPane().getViewport().getViewRect();
            Rectangle leftView = master.getEditorPane1().getScrollPane().getViewport().getViewRect();
            
            int editorsOffset = master.getEditorPane2().getLocation().y + master.getEditorPane2().getInsets().top;
            
            int rightOffset = -rightView.y + editorsOffset;
            int leftOffset = -leftView.y + editorsOffset;

            GraphicsUtils.configureDefaultRenderingHints(g);
            int currDiff = master.getCurrentDifference();
            String diffInfo = (currDiff + 1) + "/" + master.getDifferenceCount(); // NOI18N
            int width = g.getFontMetrics().stringWidth(diffInfo);
            g.setColor(fontColor);
            Rectangle hotSpot = new Rectangle((getWidth() - width) / 2, 0, width, g.getFontMetrics().getHeight());
            g.drawString(diffInfo, hotSpot.x, hotSpot.y + hotSpot.height);
            if (currDiff >= 0) {
                jumpToCurrentAction.initRect(hotSpot);
                newActionIcons.add(jumpToCurrentAction);
            }
            
            if (clip.y < editorsOffset) {
                g.setClip(clip.x, editorsOffset, clip.width, clip.height);
            }

            int rightY = getWidth() - 1;

            g.setColor(UIManager.getColor("controlShadow"));
            g.drawLine(0, clip.y, 0, clip.height);
            g.drawLine(rightY, clip.y, rightY, clip.height);

            int curDif = master.getCurrentDifference();
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            DiffViewManager.DecoratedDifference [] decoratedDiffs = master.getManager().getDecorations();
            int idx = 0;
            boolean everythingEditable = true;
            for (DiffViewManager.DecoratedDifference dd : decoratedDiffs) {
                everythingEditable &= dd.canRollback();
                g.setColor(master.getColor(dd.getDiff()));
                g.setStroke(curDif == idx++ ? master.getBoldStroke() : cs);                            
                if (dd.getBottomLeft() == -1) {
                    paintMatcher(g, master.getColor(dd.getDiff()), 0, rightY,
                            dd.getTopLeft() + leftOffset, dd.getTopRight() + rightOffset, 
                            dd.getBottomRight() + rightOffset, dd.getTopLeft() + leftOffset);
                } else if (dd.getBottomRight() == -1) {
                    paintMatcher(g, master.getColor(dd.getDiff()), 0, rightY,
                            dd.getTopLeft() + leftOffset, dd.getTopRight() + rightOffset, 
                            dd.getTopRight() + rightOffset, dd.getBottomLeft() + leftOffset);
                } else {
                    paintMatcher(g, master.getColor(dd.getDiff()), 0, rightY,
                            dd.getTopLeft() + leftOffset, dd.getTopRight() + rightOffset, 
                            dd.getBottomRight() + rightOffset, dd.getBottomLeft() + leftOffset);
                }
            }
            
            if (master.isActionsEnabled() && everythingEditable) {
                hotSpot = new Rectangle((getWidth() - actionIconsWidth) /2, editorsOffset, actionIconsWidth, actionIconsHeight);
                if (hotSpot.contains(lastMousePosition)) {
                    insertAllActiveIcon.paintIcon(null, g, hotSpot.x, hotSpot.y);
                } else {
                    insertAllIcon.paintIcon(null, g, hotSpot.x, hotSpot.y);
                }
                rollbackAction.initRect(hotSpot);
                newActionIcons.add(rollbackAction);
            }
            hotspots = newActionIcons;
            g.dispose();
        }
        
        private void paintMatcher(Graphics2D g, Color fillClr, 
                int leftX, int rightX, int upL, int upR, int doR, int doL) {
            int topY = Math.min(upL, upR), bottomY = Math.max(doL, doR);
            // try rendering only curves in viewable area
            if (!g.hitClip(leftX, topY, rightX - leftX, bottomY - topY)) {
                return;
            }
            CubicCurve2D upper = new CubicCurve2D.Float(leftX, upL,
                    (rightX -leftX)*.3f, upL,
                    (rightX -leftX)*.7f, upR,
                    rightX, upR);
            CubicCurve2D bottom = new CubicCurve2D.Float(rightX, doR,
                    (rightX - leftX)*.7f, doR,
                    (rightX -leftX)*.3f, doL,
                    leftX, doL);
            GeneralPath path = new GeneralPath();
            path.append(upper, false);
            path.append(bottom, true);
            path.closePath();
            g.setColor(fillClr);
            g.fill(path);
            g.setColor(master.getColorLines());
            g.draw(upper);
            g.draw(bottom);
        }
    }    
    
    private abstract static class DividerAction extends AbstractAction {
        private Rectangle rect;

        public DividerAction (String name, Rectangle rect) {
            super(name);
            this.rect = rect;
        }

        private Rectangle getRect () {
            return rect;
        }

        private void initRect (Rectangle rect) {
            this.rect = rect;
        }
        
    }
}
