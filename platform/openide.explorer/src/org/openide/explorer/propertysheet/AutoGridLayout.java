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
/*
* AutoGridLayout.java
*
* Created on 04 October 2003, 16:01
*/
package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import java.util.Arrays;
import java.util.Comparator;


/**A layout manager which can layout like-heighted components in a grid
 * pattern based on their preferred sizes.  Can be used in packed or unpacked
 * mode.  Column positions are based on the preferred sizes of the first row
 * of components.<p>
 * When packed, the components are sorted from narrowest to widest;
 * subsequent rows will use these columns, with components two wide for a
 * single column spanning two or more columns.<p>
 * When not packed, the components are sorted from widest to narrowest,
 * resulting in more whitespace, but consistent rows of columns - no
 * component will span more than one column.<p>
 * Used by <code>RadioInplaceEditor</code> to manage the set of radio
 * buttons representing property editor tags.
 *
 * @author  Tim Boudreau
 */
class AutoGridLayout implements LayoutManager {
    int gapY = 5;
    boolean pack;

    public AutoGridLayout(boolean pack) {
        this.pack = pack;
    }

    public void addLayoutComponent(String name, Component comp) {
        //Do nothing
    }

    public void removeLayoutComponent(Component comp) {
        //Do nothing
    }

    private Comparator<Component> comparator() {
        return new PreferredSizeComparator(pack);
    }

    public void layoutContainer(Container parent) {
        Component[] c = parent.getComponents();

        if (c.length > 3) {
            Arrays.sort(c, comparator());
        }

        if (c.length == 2) {
            //we're probably a radio button editor in the property sheet - 
            //make sure that both buttons are displayed, even if some clipping
            //would occur - we don't have another row to go to
            Dimension d0 = c[0].getPreferredSize();
            Dimension d1 = c[1].getPreferredSize();
            c[0].setBounds(0, 0, d0.width, d0.height);
            c[1].setBounds(d0.width, 0, d1.width, d1.height);

            return;
        }

        Insets insets = parent.getInsets();

        int w = parent.getWidth() - insets.right;
        int h = parent.getHeight() - insets.bottom;
        int currx = insets.left;
        int curry = insets.top;
        boolean done = false;
        int cols = -1;

        //Layout the first row of components according to their preferred 
        //sizes.  Their positions will act as column positions.  If sorted
        //narrowest-first, results in a smaller, packedAutoGridLayout.  If sorted
        //widest-first, results in nice consistent columns, but uses more
        //space to do it.
        for (int i = 0; i < c.length; i++) {
            Dimension d = c[i].getPreferredSize();

            if ((d.width == 0) || (d.height == 0)) {
                //Can happen for foreign components that can't do
                //a proper preferred size w/o a graphics context
                d = PropUtils.getMinimumPanelSize();
            }

            if ((currx + d.width) > w) {
                curry += (d.height + gapY);
                currx = insets.left;

                if (cols == -1) {
                    cols = i;

                    break;
                }
            }

            c[i].setBounds(currx, curry, d.width, d.height);
            currx += d.width;
        }

        if (cols == -1) {
            cols = c.length;
        }

        int currCol = 0;

        for (int i = cols; i < c.length; i++) {
            Dimension d = c[i].getPreferredSize();

            if ((currx + d.width) > w) {
                //will only happen with inverse sort - we're starting
                //the loop with a position that won't fit, and should flip
                //to the next line
                curry += (d.height + gapY);
                currx = insets.left;
                currCol = 0;
            }

            //see if we're out of horizontal space and should punt
            done = (curry + d.height) > h;

            if (!done) {
                //fetch the width of this column, as the width of the first row component
                int currColWidth = c[currCol].getWidth();

                //will we fit at all?
                if (d.width <= w) {

                    //loop until we know how many columns we need
                    while (currColWidth <= d.width) {
                        currCol++;

                        if (currCol > cols) {
                            //out of columns?  Flip to the next line
                            currCol = 0;
                            curry += (d.height + gapY);
                            currx = insets.left;
                            currColWidth = 0;
                        }

                        //note the combined column width - it will be the
                        //next iteration's starting x position
                        currColWidth += c[currCol].getWidth();
                    }

                    c[i].setBounds(currx, curry, d.width, d.height);
                    currx += currColWidth;
                } else {
                    //Okay, we've got a component wider than its parent - give up
                    c[i].setBounds(currx, curry, d.width, d.height);
                    currx += d.width;

                    //just clip it if it's wider than max
                }

                if (currx > w) {
                    //see if we should flip to the next row or if there may
                    //still be space
                    currx = insets.left;
                    curry += (d.height + gapY);
                    currCol = 0;
                } else {
                    currCol++;
                }
            } else {
                //If we get here, we've run out of horizontal space - no
                //point in trying to do something reasonable with the component
                c[i].setBounds(0, 0, 0, 0);
            }
        }
    }

    public Dimension minimumLayoutSize(java.awt.Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(java.awt.Container parent) {
        Component[] c = parent.getComponents();

        if (c.length > 3) {
            Arrays.sort(c, comparator());
        }

        Dimension max = Toolkit.getDefaultToolkit().getScreenSize();
        max.width /= 2;
        max.height /= 2;

        Insets insets = parent.getInsets();

        int w = max.width - insets.right;

        int currx = insets.left;
        int cols = -1;
        int baseHeight = 0;
        Dimension[] dims = new Dimension[c.length];
        Dimension result = new Dimension();

        //establish the base columns and populate the dimensions array
        for (int i = 0; i < c.length; i++) {
            dims[i] = c[i].getPreferredSize();

            if ((dims[i].width == 0) || (dims[i].height == 0)) {
                //Can happen for foreign components that can't do
                //a proper preferred size w/o a graphics context
                dims[i] = PropUtils.getMinimumPanelSize();
            }

            baseHeight = Math.max(baseHeight, dims[i].height);

            if (cols == -1) {
                if ((currx + dims[i].width) > w) {
                    result.width = currx;
                    cols = i;
                }
            }

            if (cols != -1) {
                //Make sure we don't have one element wider than all the
                //column sizes
                result.width = Math.max(result.width, dims[i].width + insets.left + insets.right);
            }

            currx += dims[i].width;
        }

        if (cols == -1) { //we didn't overstretch the available width
            cols = c.length;
            result.width = currx;
        }

        if (!pack && (c.length > 3)) {
            //Then we can take a short cust - we know all will be 1 item per cell
            int rows = (c.length / cols) + (((c.length % cols) != 0) ? 1 : 0);
            result.height = (baseHeight * rows) + (gapY * rows) + insets.top + insets.bottom;
            result.width += 6;
            assert (result.width >= 0) && (result.height >= 0);

            return result;
        }

        int currRow = 0;
        int currCol = 0;
        currx = insets.left;

        //iterate the rest of the array, incrementing the row index
        //when the content won't fit, to find out the total rows needed
        for (int i = cols; i < c.length; i++) {
            int colspan = 1;
            int colwidth = dims[currCol].width;

            while (dims[i].width > colwidth) {
                currCol++;
                colwidth += dims[currCol].width;
                colspan++;

                if ((colwidth + currx) > max.width) {
                    currCol = 0;
                    currRow++;
                    colspan = 1;
                    colwidth = dims[currCol].width;
                }
            }

            currCol += colspan;
            currx += colwidth;

            if ((currCol > cols) && (i != (c.length - 1))) {
                currCol = 0;
                currRow++;
                currx = insets.left;
            }
        }

        result.height = (baseHeight * currRow) + insets.top + insets.bottom + (gapY * currRow);

        return result;
    }

    private static final class PreferredSizeComparator implements Comparator<Component> {
        boolean smallFirst;

        public PreferredSizeComparator(boolean smallFirst) {
            this.smallFirst = smallFirst;
        }

        public int compare(Component c1, Component c2) {
            Dimension d1 = c1.getPreferredSize();
            Dimension d2 = c2.getPreferredSize();

            return smallFirst ? (d1.width - d2.width) : (d2.width - d1.width);
        }
    }
}
