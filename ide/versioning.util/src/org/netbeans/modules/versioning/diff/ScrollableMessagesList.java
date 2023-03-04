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

package org.netbeans.modules.versioning.diff;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Collection;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import static javax.swing.SwingConstants.VERTICAL;

/**
 *
 * @author Marian Petras
 */
class ScrollableMessagesList extends JPanel implements Scrollable {

    private static final int DEFAULT_VISIBLE_ROW_COUNT = 7;

    private final int rowCount;
    private int visibleRowCount = DEFAULT_VISIBLE_ROW_COUNT;
    private Dimension preferredSize;

    public ScrollableMessagesList(Collection<String> messages) {
        super(new GridLayout(0, 1));

        int lines = 0;
        for (String msg : messages) {
            add(new JLabel(msg));
            lines++;
        }
        rowCount = lines;
    }

    public void setVisibleRowCount(int visibleRowCount) {
        if (visibleRowCount != this.visibleRowCount) {
            this.visibleRowCount = visibleRowCount;
            preferredSize = null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (preferredSize == null) {
            preferredSize = super.getPreferredSize();
        }
        return preferredSize;
    }

    public Dimension getPreferredScrollableViewportSize() {
        Dimension prefSize = getPreferredSize();
        if (rowCount <= visibleRowCount) {
            return prefSize;
        }

        Dimension prefScrollableSize = new Dimension(prefSize);
        int rowHeight = prefSize.height / rowCount;
        prefScrollableSize.height = visibleRowCount * rowHeight;
        return prefScrollableSize;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        final int rowHeight = getRowHeight();

        int scrollBy;

        if (orientation == VERTICAL) {
            int visibleSize = visibleRect.height;

            /*
             * number of pixels covered from the top of the topmost visible
             * line
             */
            int coveredTop = visibleRect.y % rowHeight;

            if (visibleSize >= rowHeight) {
                if (direction < 0) {    //UP
                    scrollBy = coveredTop == 0 ? rowHeight : coveredTop;
                } else {                //DOWN
                    scrollBy = rowHeight - coveredTop;
                }
            } else if (visibleSize >= (4 * rowHeight / 5)) {

                int delta = rowHeight - visibleSize;

                /*
                 * number of pixels covered from the bottom of the bottommost
                 * visible line
                 */
                int coveredBottom = rowHeight - visibleSize - coveredTop;
                if (coveredBottom < 0) {
                    coveredBottom += rowHeight;
                }

                if (direction < 0) {    //UP
                    int deltaHalf = (delta + 1) / 2;    //upper margin
                    scrollBy = coveredTop <= deltaHalf
                               ? coveredTop - deltaHalf + rowHeight
                               : coveredTop - deltaHalf;
                } else {                //DOWN
                    int deltaHalf = delta / 2;          //lower margin
                    scrollBy = coveredBottom <= deltaHalf
                               ? coveredBottom - deltaHalf + rowHeight
                               : coveredBottom - deltaHalf;
                }
            } else {
                scrollBy = (visibleSize + 2) / 3;
            }
        } else {        //HORIZONTAL
            scrollBy = (rowHeight * 3 + 1) / 2;
        }

        final int remainder = computeRemainder(visibleRect,
                                               orientation,
                                               direction);
        return Math.min(remainder, scrollBy);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        int rowHeight = getRowHeight();
        int visibleSize;
        int scrollBy;

        if (orientation == VERTICAL) {
            visibleSize = visibleRect.height;

            scrollBy = visibleSize * 9 / 10;        //TODO use something better
            if (scrollBy == 0) {
                scrollBy = 1;
            }

        } else {        //HORIZONTAL
            visibleSize = visibleRect.width;
            scrollBy = visibleSize * 6 / 10;
            if (scrollBy == 0) {
                scrollBy = 1;
            }
        }

        final int remainder = computeRemainder(visibleRect,
                                               orientation,
                                               direction);
        return Math.min(remainder, scrollBy);
    }

    private int computeRemainder(Rectangle visibleRect,
                                 int orientation,
                                 int direction) {
        int remainder;
        if (orientation == VERTICAL) {
            if (direction < 0) {    //UP
                remainder = visibleRect.y;
            } else {                //DOWN
                remainder = getSize().height
                            - (visibleRect.y + visibleRect.height);
            }
        } else {        //HORIZONTAL
            if (direction < 0) {    //LEFT
                remainder = visibleRect.x;
            } else {                //RIGHT
                remainder = getSize().width
                            - (visibleRect.x + visibleRect.width);
            }
        }
        return remainder;
    }

    private int getRowHeight() {
        return getPreferredSize().height / rowCount;
    }

    @Override
    public void setUI(PanelUI ui) {
        preferredSize = null;
        super.setUI(ui);
    }

    @Override
    protected void setUI(ComponentUI newUI) {
        preferredSize = null;
        super.setUI(newUI);
    }

}
