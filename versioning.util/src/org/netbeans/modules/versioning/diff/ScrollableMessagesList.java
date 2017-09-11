/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
