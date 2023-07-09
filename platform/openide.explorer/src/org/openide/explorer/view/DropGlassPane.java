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
package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.awt.GraphicsUtils;
import org.openide.util.Parameters;


/**
 * Glass pane which is used for paint of a drop line over <code>JComponent</code>.
 *
 * @author  Jiri Rechtacek
 *
 * @see java.awt.dnd.DropTarget
 * @see org.openide.explorer.view.TreeViewDropSupport
 */
final class DropGlassPane extends JPanel {
    private static final HashMap<Integer, DropGlassPane> map = new HashMap<>();
    private static final int MIN_X = 5;
    private static final int MIN_Y = 3;
    private static final int MIN_WIDTH = 10;
    private static final int MIN_HEIGTH = 3;
    private static transient Component oldPane;
    private static transient JComponent originalSource;
    private static transient boolean wasVisible;
    private Line2D line = null;

    private DropGlassPane() {
    }

    /** Check the bounds of given line with the bounds of this pane. Optionally
     * calculate the new bounds in current pane's boundary.
     * @param comp
     * @return  */
    public static synchronized DropGlassPane getDefault(JComponent comp) {
        Integer id = System.identityHashCode(comp);

        if ((map.get(id)) == null) {
            DropGlassPane dgp = new DropGlassPane();
            dgp.setOpaque(false);
            map.put(id, dgp);
        }

        return map.get(id);
    }

    /** Stores the original glass pane on given tree.
     * @param source the active container
     * @param pane the original glass
     * @param visible was glass pane visible
     */
    static void setOriginalPane(JComponent source, Component pane, boolean visible) {
        if (oldPane != null) {
            throw new IllegalStateException("Original pane already present");
        }
        Parameters.notNull("source", source);
        Parameters.notNull("pane", pane);
        oldPane = pane;
        originalSource = source;
        wasVisible = visible;
    }

    /** Is any original glass pane stored?
     * @return true if true; false otherwise
     */
    static boolean isOriginalPaneStored() {
        return oldPane != null;
    }

    /** Sets the original glass pane to the root pane of stored container.
     */
    static void putBackOriginal() {
        if (oldPane == null) {
            throw new IllegalStateException("No original pane present");
        }
        final JRootPane rp = originalSource.getRootPane();
        if (rp == null) {
            if( null != SwingUtilities.getWindowAncestor( originalSource ) ) //#232187 - only complain when the originalSource is still in component hierarchy
                throw new IllegalStateException("originalSource " + originalSource + " has no root pane: " + rp); // NOI18N
        } else {
            rp.setGlassPane(oldPane);
            oldPane.setVisible(wasVisible);
        }
        oldPane = null;
    }

    /** Unset drop line if setVisible to false.
     * @param boolean aFlag new state */
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        if (!aFlag) {
            setDropLine(null);
        }
    }

    /** Set drop line. Given line is used by paint method.
     * @param line drop line */
    public void setDropLine(Line2D line) {
        Line2D oldLine = this.line;
        this.line = line;

        if( (null == oldLine && null != line)
                || (null != oldLine && null == line ) )
            repaint ();
    }

    /** Check the bounds of given line with the bounds of this pane. Optionally
     * calculate the new bounds in current pane's boundary.
     * @param line a line for check
     * @return  a line with bounds inside the pane's boundary */
    private Line2D checkLineBounds(Line2D line) {
        Rectangle bounds = getBounds();
        double startPointX;
        double startPointY;
        double endPointX;
        double endPointY;

        // check start point
        startPointX = Math.max(line.getX1(), bounds.x + MIN_X);
        startPointY = Math.max(line.getY1(), bounds.y + MIN_Y);

        // check end point
        endPointX = Math.min(line.getX2(), (bounds.x + bounds.width) - MIN_WIDTH);
        endPointY = Math.min(line.getY2(), (bounds.y + bounds.height) - MIN_HEIGTH);

        // set new bounds
        line.setLine(startPointX, startPointY, endPointX, endPointY);

        return line;
    }

    /** Paint drop line on glass pane.
     * @param Graphics g Obtained graphics */
    @Override
    public void paint(Graphics g) {
        if (line != null) {
            Color c = UIManager.getColor("Tree.dropLine");
            if (c != null) {
                g.setColor(c);
            }
            
            // check bounds
            line = checkLineBounds(line);

            int x1 = (int) line.getX1();
            int x2 = (int) line.getX2();
            int y1 = (int) line.getY1();

            g.setColor( UIManager.getColor( "Tree.selectionBackground" ) );

            if (g instanceof Graphics2D) {
                // create drop line shape
                Path2D shape = new Path2D.Float();
                shape.moveTo(x1, y1 - 3); // left-top edge
                shape.lineTo(x1 + 3, y1); // horizontal line left
                shape.lineTo(x2 - 3, y1); // horizontal line right
                shape.lineTo(x2, y1 - 3); // right-top edge
                shape.lineTo(x2, y1 + 5); // right-bottom edge
                shape.lineTo(x2 - 3, y1 + 2); // horizontal line right
                shape.lineTo(x1 + 3, y1 + 2); // horizontal line left
                shape.lineTo(x1, y1 + 5); // left-bottom edge
                shape.closePath();

                // paint drop line shape
                GraphicsUtils.configureDefaultRenderingHints(g);
                ((Graphics2D)g).fill(shape);
            } else {
                // probably no longer used, but keep it for compatibility for the case
                // that graphics context is not a Graphics2D

                // horizontal line
                g.drawLine(x1 + 2, y1, x2 - 2, y1);
                g.drawLine(x1 + 2, y1 + 1, x2 - 2, y1 + 1);

                // left
                g.drawLine(x1, y1 - 2, x1, y1 + 3);
                g.drawLine(x1 + 1, y1 - 1, x1 + 1, y1 + 2);

                // right
                g.drawLine(x2, y1 - 2, x2, y1 + 3);
                g.drawLine(x2 - 1, y1 - 1, x2 - 1, y1 + 2);
            }
        }

        // help indication of glass pane for debugging

        /*g.drawLine (0, getBounds ().height / 2, getBounds ().width, getBounds ().height / 2);
        g.drawLine (0, getBounds ().height / 2+1, getBounds ().width, getBounds ().height / 2+1);
        g.drawLine (getBounds ().width / 2, 0, getBounds ().width / 2, getBounds ().height);
        g.drawLine (getBounds ().width / 2+1, 0, getBounds ().width / 2+1, getBounds ().height);
         */
    }
}
