/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.windows.view.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

/**
 * Utility class that provides window decorations, custom border and resize
 * handler for borders useful for window resizing.
 *
 * @author Dafe Simonek
 */
final class DecorationUtils {
    
    /** No instances, utils class. */
    private DecorationUtils () {
    }

    /** Creates and returns border suitable for decorating separate windows
     * in window system.
     *
     * @return Border for separate windows
     */
    public static Border createSeparateBorder () {
        return new SeparateBorder();
    }

    /** Creates and returns handler of window resizing, which works in given
     * insets.
     * @return The handler for resizing.
     */
    public static ResizeHandler createResizeHandler (Insets insets) {
        return new ResizeHandler(insets);
    }

    /** Simple border with line and the space */
    private static class SeparateBorder extends AbstractBorder {

        public Insets getBorderInsets (Component c) {
            return new Insets(3, 3, 3, 3);
        }

        public void paintBorder (Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, width - 1, height - 1);
        }

    } // end of SeparateBorder

    /** Takes care about resizing of the window on mouse drag,
     * with proper resize cursors.
     *
     * Usage: Attach handler as mouse and mouse motion listener to the content pane of
     * the window:<br>
     * <code>rootPaneContainer.getContentPane().addMouseListener(resizeHandler);</code>
     * <code>rootPaneContainer.getContentPane().addMouseMotionListener(resizeHandler);</code>
     *
    */
    static class ResizeHandler extends MouseAdapter implements MouseMotionListener {

        private Insets insets;

        private int cursorType;

        private boolean isPressed = false;

        /** Window resize bounds, class fields to prevent from allocating new
         * objects */
        private Rectangle resizedBounds = new Rectangle();
        private Rectangle movedBounds = new Rectangle();

        private Point startDragLoc;
        private Rectangle startWinBounds;

        /** holds minimum size of the window being resized */
        private Dimension minSize;

        public ResizeHandler (Insets insets) {
            this.insets = insets;
        }

        public void mouseDragged(MouseEvent e) {
            check(e);
            Window w = SwingUtilities.getWindowAncestor((Component)e.getSource());

            if (Cursor.DEFAULT_CURSOR == cursorType) {
                // resize only when mouse pointer in resize areas
                return;
            }

            Rectangle newBounds = computeNewBounds(w, getScreenLoc(e));
            if (!w.getBounds().equals(newBounds)) {
                w.setBounds(newBounds);
            }
        }

        public void mouseMoved(MouseEvent e) {
            check(e);
            Component comp = (Component)e.getSource();
            movedBounds = comp.getBounds(movedBounds);

            cursorType = getCursorType(movedBounds, e.getPoint());
            comp.setCursor(Cursor.getPredefinedCursor(cursorType));
        }

        public void mousePressed(MouseEvent e) {
            isPressed = true;
            startDragLoc = getScreenLoc(e);

            Window w = SwingUtilities.getWindowAncestor((Component)e.getSource());
            startWinBounds = w.getBounds();
            resizedBounds.setBounds(startWinBounds);
            minSize = w.getMinimumSize();
        }

        public void mouseReleased(MouseEvent e) {
            isPressed = false;
            startDragLoc = null;
            startWinBounds = null;
            minSize = null;
        }

        public void mouseExited(MouseEvent e) {
            Component comp = (Component)e.getSource();
            comp.setCursor(Cursor.getDefaultCursor());
        }

        private int getCursorType (Rectangle b, Point p) {
            int leftDist = p.x - b.x;
            int rightDist = (b.x + b.width) - p.x;
            int topDist = p.y - b.y;
            int bottomDist = (b.y + b.height) - p.y;

            boolean isNearTop = topDist >= 0 && topDist <= insets.top;
            boolean isNearBottom = bottomDist >= 0 && bottomDist <= insets.bottom;
            boolean isNearLeft = leftDist >= 0 && leftDist <= insets.left;
            boolean isNearRight = rightDist >= 0 && rightDist <= insets.right;

            boolean isInTopPart = topDist >= 0 && topDist <= insets.top + 10;
            boolean isInBottomPart = bottomDist >= 0 && bottomDist <= insets.bottom + 10;
            boolean isInLeftPart = leftDist >= 0 && leftDist <= insets.left + 10;
            boolean isInRightPart = rightDist >= 0 && rightDist <= insets.right + 10;

            if (isNearTop && isInLeftPart || isInTopPart && isNearLeft) {
                return Cursor.NW_RESIZE_CURSOR;
            }
            if (isNearTop && isInRightPart || isInTopPart && isNearRight) {
                return Cursor.NE_RESIZE_CURSOR;
            }
            if (isNearBottom && isInLeftPart || isInBottomPart && isNearLeft) {
                return Cursor.SW_RESIZE_CURSOR;
            }
            if (isNearBottom && isInRightPart || isInBottomPart && isNearRight) {
                return Cursor.SE_RESIZE_CURSOR;
            }
            if (isNearTop) {
                return Cursor.N_RESIZE_CURSOR;
            }
            if (isNearLeft) {
                return Cursor.W_RESIZE_CURSOR;
            }
            if (isNearRight) {
                return Cursor.E_RESIZE_CURSOR;
            }
            if (isNearBottom) {
                return Cursor.S_RESIZE_CURSOR;
            }
            return Cursor.DEFAULT_CURSOR;
        }

        private Rectangle computeNewBounds (Window w, Point dragLoc) {
            if (startDragLoc == null) {
                throw new IllegalArgumentException("Can't compute bounds when startDragLoc is null");  //NOI18N
            }
            int xDiff = dragLoc.x - startDragLoc.x;
            int yDiff = dragLoc.y - startDragLoc.y;
            resizedBounds.setBounds(startWinBounds);

            switch (cursorType) {
                case Cursor.E_RESIZE_CURSOR:
                    resizedBounds.width = startWinBounds.width + (dragLoc.x - startDragLoc.x);
                    break;

                case Cursor.W_RESIZE_CURSOR:
                    resizedBounds.width = startWinBounds.width - xDiff;
                    resizedBounds.x = startWinBounds.x + xDiff;
                    break;

                case Cursor.N_RESIZE_CURSOR:
                    resizedBounds.height = startWinBounds.height - yDiff;
                    resizedBounds.y = startWinBounds.y + yDiff;
                    break;

                case Cursor.S_RESIZE_CURSOR:
                    resizedBounds.height = startWinBounds.height + (dragLoc.y - startDragLoc.y);
                    break;

                case Cursor.NE_RESIZE_CURSOR:
                    resize(resizedBounds, 0, yDiff, xDiff, -yDiff, minSize);
                    break;

                case Cursor.NW_RESIZE_CURSOR:
                    resize(resizedBounds, xDiff, yDiff, -xDiff, -yDiff, minSize);
                    break;

                case Cursor.SE_RESIZE_CURSOR:
                    resize(resizedBounds, 0, 0, xDiff, yDiff, minSize);
                    break;

                case Cursor.SW_RESIZE_CURSOR:
                    resize(resizedBounds, xDiff, 0, -xDiff, yDiff, minSize);
                    break;

                default:
                    System.out.println("unknown cursor type : " + cursorType);
                    //throw new IllegalArgumentException("Unknown/illegal cursor type: " + cursorType);  //NOI18N
                    break;
            }
            return resizedBounds;
        }

        private static void resize (Rectangle rect, int xDiff, int yDiff, int widthDiff, int heightDiff, Dimension minSize) {
            rect.x += xDiff;
            rect.y += yDiff;
            rect.height += heightDiff;
            rect.width += widthDiff;
            // keep size at least at minSize
            rect.height = Math.max(rect.height, minSize.height);
            rect.width = Math.max(rect.width, minSize.width);
        }

        private Point getScreenLoc (MouseEvent e) {
            Point screenP = new Point(e.getPoint());
            SwingUtilities.convertPointToScreen(screenP, (Component) e.getSource());
            return screenP;
        }

        /* Checks that handler is correctly attached to the window */
        private void check(MouseEvent e) {
            Object o = e.getSource();
            if (!(o instanceof Component)) {
                throw new IllegalArgumentException("ResizeHandler works only with Component, not with " + o);  //NOI18N
            }
            Window w = SwingUtilities.getWindowAncestor((Component)o);
            if (w == null) {
                throw new IllegalStateException("Can't find and resize the window, not attached.");   //NOI18N
            }
        }

    } // end of ResizeHandler

}
