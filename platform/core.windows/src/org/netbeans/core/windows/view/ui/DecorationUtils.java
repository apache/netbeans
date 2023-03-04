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

        @Override
        public Insets getBorderInsets (Component c) {
            return new Insets(3, 3, 3, 3);
        }

        @Override
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

        @Override
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

        @Override
        public void mouseMoved(MouseEvent e) {
            check(e);
            Component comp = (Component)e.getSource();
            movedBounds = comp.getBounds(movedBounds);

            cursorType = getCursorType(movedBounds, e.getPoint());
            comp.setCursor(Cursor.getPredefinedCursor(cursorType));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            isPressed = true;
            startDragLoc = getScreenLoc(e);

            Window w = SwingUtilities.getWindowAncestor((Component)e.getSource());
            startWinBounds = w.getBounds();
            resizedBounds.setBounds(startWinBounds);
            minSize = w.getMinimumSize();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isPressed = false;
            startDragLoc = null;
            startWinBounds = null;
            minSize = null;
        }

        @Override
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
