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

package org.netbeans.core.windows.view.ui.toolbars;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * Panel which holds one row of toolbars. The order of toolbars is defined
 * by the order in they are being added to this row and their left/right align attribute.
 *
 * @author S. Aubrecht
 */
class ToolbarRow extends JPanel {
    
    /**
     * Maps toolbar name to its constraints.
     */
    private final Map<String, ToolbarConstraints> name2constraint = new HashMap<String, ToolbarConstraints>(20);
    /**
     * List of toolbar constraints in the order they are shown, includes constraints for drop feedback.
     */
    private final List<ToolbarConstraints> constraints = new ArrayList<ToolbarConstraints>( 20 );

    private static final String FAKE_NAME = "__fake_drag_container__"; //NOI18N


    //drag context
    private ToolbarConstraints dragConstraints;
    private Component dragContainer;
    private Point dragOriginalLocation;

    //drop context
    private JLabel dropReplacement;
    private ToolbarConstraints dropConstraints;
    private ToolbarContainer dropContainter;

    public ToolbarRow() {
        setLayout( new ToolbarLayout() );
        setOpaque(false);
        addDropConstraints();
    }

    public void addConstraint( ToolbarConstraints tc ) {
        ToolbarConstraints current = name2constraint.get(tc.getName());
        if( null != current ) {
            constraints.remove(current);
            Logger.getLogger(ToolbarRow.class.getName()).log(Level.FINE,
                    "Duplicate toolbar defintion " + tc.getName()); //NOI18N
        }
        List<ToolbarConstraints> left = getConstraints( ToolbarConstraints.Align.left );
        List<ToolbarConstraints> right = getConstraints( ToolbarConstraints.Align.right );
        constraints.clear();
        constraints.addAll(left);
        if( tc.getAlign() == ToolbarConstraints.Align.left )
            constraints.add(tc);
        constraints.addAll(right);
        if( tc.getAlign() == ToolbarConstraints.Align.right )
            constraints.add(tc);
        name2constraint.put(tc.getName(), tc);
    }

    boolean removeConstraint(ToolbarConstraints tc) {
        if( null != name2constraint.get( tc.getName() ) ) {
            name2constraint.remove(tc.getName());
            constraints.remove(tc);
            return true;
        }
        return false;
    }

    @Override
    public void removeAll() {
        super.removeAll();
        if( null != dropReplacement )
            add( dropReplacement );
    }

    @Override
    public boolean isVisible() {
        for( ToolbarConstraints tc : constraints ) {
            if( tc.isVisible() )
                return true;
        }
        return false;
    }

    /**
     * @return True if the row doesn't contain any toolbar.
     */
    public boolean isEmpty() {
        return name2constraint.isEmpty()
                || (null != dragContainer && name2constraint.size() == 1);
    }

    /**
     * Adds a fake component and appropriate constraints that will be used to
     * visualize toolbar drop feedback.
     */
    private void addDropConstraints() {
        dropConstraints = new ToolbarConstraints(FAKE_NAME, ToolbarConstraints.Align.left, false, true);
        dropReplacement = new JLabel();
        dropReplacement.setName(dropConstraints.getName());
        add( dropReplacement );
        constraints.add( dropConstraints );
    }

    /**
     * D'n'd has just started.
     * @param container Toolbar container that is being dragged.
     */
    void dragStarted(ToolbarContainer container) {
        dragConstraints = findConstraints( container.getName() );
        if( null != dragConstraints ) {
            dragContainer = findComponent(dragConstraints.getName());
            dragConstraints.setVisible(false);
            dragOriginalLocation = new Point( dragContainer.getLocationOnScreen() );
            container.setVisible(false);
            invalidate();
            revalidate();
            repaint();
        }
    }

    /**
     * Visualize drop feedback for the given toolbar.
     * @param container Toolbar container being dragged over this row.
     * @param screenLocation Screen coords of mouse cursor.
     */
    void showDropFeedback(ToolbarContainer container, Point screenLocation, Image dragImage) {
        Component targetComp = null;
        Rectangle bounds = null;
        //find component under the cursor
        for( Component c : getComponents() ) {
            if( !c.isVisible() )
                continue;
            bounds = c.getBounds();
            bounds.setLocation(c.getLocationOnScreen());
            if( bounds.contains(screenLocation) ) {
                targetComp = c;
                break;
            }
        }

        dropReplacement.setPreferredSize( container.getPreferredSize() );
        dropReplacement.setMinimumSize( container.getMinimumSize() );
        if( dropContainter != container ) {
            //create image of dragged toolbar and show it in the area where toolbar will be dropped
            dropContainter = container;
            dropReplacement.setIcon(ImageUtilities.image2Icon(dragImage));
        }
        
        if( null != targetComp ) {
            //mouse is above some other toolbar in this row, calculate drop index & coords

            if( targetComp == dropReplacement ) {
                //mouse cursor is still above the same component as during the last call
                return;
            }

            boolean dropAfter = bounds.x + bounds.width/2 < screenLocation.x;
            ToolbarConstraints targetTc = findConstraints( targetComp.getName());
            dropConstraints.setAlign(targetTc.getAlign());
            int dropIndex = constraints.indexOf( targetTc );
            if( dropAfter )
                dropIndex++;

            if( dropIndex > constraints.indexOf(dropConstraints) )
                dropIndex--;
            if( isLastVisibleToolbar(targetTc) && isStretchLastToolbar() ) {
                if( bounds.x + bounds.width - bounds.width/4 < screenLocation.x ) {
                    dropConstraints.setAlign(ToolbarConstraints.Align.right);
                    dropIndex = constraints.size()+1;
                }
            }
            constraints.remove(dropConstraints);
            if( dropIndex <= constraints.size() )
                constraints.add(dropIndex, dropConstraints);
            else
                constraints.add( dropConstraints );
            dropConstraints.setVisible(true);
            dropReplacement.setVisible(true);
        } else {
            //drop into a free area, the new position will be either the last left bar
            //or the first right bar
            Rectangle freeAreaBounds = getFreeAreaBounds();
            if( freeAreaBounds.contains(screenLocation) ) {
                boolean leftAlign = freeAreaBounds.x + freeAreaBounds.width/2 >= screenLocation.x;
                constraints.remove(dropConstraints);
                int dropIndex = -1;
                if( leftAlign ) {
                    dropConstraints.setAlign( ToolbarConstraints.Align.left );
                    for( int i=0; i<constraints.size(); i++ ) {
                        ToolbarConstraints tc = constraints.get(i);
                        if( !tc.isVisible() )
                            continue;
                        if( tc.getAlign() != ToolbarConstraints.Align.left ) {
                            dropIndex = i;
                            break;
                        }
                    }
                } else {
                    dropConstraints.setAlign( ToolbarConstraints.Align.right );
                    for( int i=constraints.size()-1; i>=0; i-- ) {
                        ToolbarConstraints tc = constraints.get(i);
                        if( !tc.isVisible() )
                            continue;
                        if( tc.getAlign() != ToolbarConstraints.Align.right ) {
                            dropIndex = i;
                            break;
                        }
                    }
                    if( dropIndex < 0 )
                        dropIndex = 0; //no right bar
                }
                if( dropIndex >= 0 )
                    constraints.add( dropIndex, dropConstraints );
                else
                    constraints.add(dropConstraints);
                dropConstraints.setVisible(true);
                dropReplacement.setVisible(true);
            } else {
                //none of the above - probably won't happen...
                dropConstraints.setVisible(false);
                dropReplacement.setVisible(false);
            }
        }
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Hide dnd feedback (when the drag cursor moves out of this row).
     */
    void hideDropFeedback() {
        dropConstraints.setVisible(false);
        dropReplacement.setVisible(false);
        dropContainter = null;
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Toolbar has been dropped into this row.
     * @return New screen location of the dropped toolbar.
     */
    Point drop() {
        Point res = null;
        if( null == dropContainter )
            return res;
        if( dropReplacement.isShowing() )
            res = dropReplacement.getLocationOnScreen();
        if( null != dragConstraints ) {
            //the dropped toolbar was originally in this row, just reorder constraints
            add( dragContainer );
            constraints.remove(dragConstraints);
            dragConstraints.setVisible(true);
            dragConstraints.setAlign(dropConstraints.getAlign());
            constraints.add( constraints.indexOf(dropConstraints), dragConstraints );
        } else {
            //we've got a new toolbar, create new constraints for it
            ToolbarConstraints newConstraints = new ToolbarConstraints( dropContainter.getName(), dropConstraints.getAlign(), true, true);
            add( dropContainter );
            constraints.add( constraints.indexOf(dropConstraints), newConstraints );
            name2constraint.put(newConstraints.getName(), newConstraints);
        }

        dropConstraints.setVisible(false);
        dropReplacement.setVisible(false);
        invalidate();
        revalidate();
        repaint();
        dropContainter = null;
        dragConstraints = null;
        dragContainer = null;
        return res;
    }

    /**
     * D'n'd has finished successfully and the toolbar dragged from this row
     * has been dropped elsewhere.
     */
    void dragSuccess() {
        if( null != dragConstraints ) {
            Component c = findComponent(dragConstraints.getName());
            if( null != c )
                remove( c );
            constraints.remove(dragConstraints);
            name2constraint.remove(dragConstraints.getName());
            dragConstraints = null;
            dragContainer = null;
        }
    }

    /**
     * D'n'd has been aborted (or dropped outside toolbar area) and the toolbar
     * dragged from this row should return to its original location.
     */
    Point dragAbort() {
        Point res = null;
        if( null != dragConstraints ) {
            add( dragContainer );
            dragContainer.setVisible(true);
            dragConstraints.setVisible(true);
            invalidate();
            revalidate();
            repaint();
            res = dragOriginalLocation;
            dragConstraints = null;
            dragContainer = null;
        }
        return res;
    }

    Iterable<? extends ToolbarConstraints> getConstraints() {
        ArrayList<ToolbarConstraints> res = new ArrayList<ToolbarConstraints>(constraints.size());
        //filter out fake constraints for drop feedback
        for( ToolbarConstraints tc : constraints ) {
            if( null == name2constraint.get(tc.getName()) )
                continue;
            res.add( tc );
        }
        return res;
    }

    /**
     * @return The number of toolbars (including hidden ones) shown in this row.
     */
    int countVisibleToolbars() {
        int count = 0;
        for( ToolbarConstraints tc : name2constraint.values() ) {
            if( tc.isVisible() )
                count++;
        }
        return count;
    }

    private boolean isLastVisibleToolbar( ToolbarConstraints toolbarConstraints ) {
        for( int i=constraints.size()-1; i>=0; i-- ) {
            ToolbarConstraints tc = constraints.get(i);
            if( !tc.isVisible() || FAKE_NAME.equals(tc.getName()) )
                continue;
            return tc == toolbarConstraints;
        }
        return false;
    }

    /**
     * @param align
     * @return List of components that are stacked to the given side.
     */
    private List<Component> getContainers( ToolbarConstraints.Align align ) {
        List<Component> res = new ArrayList<Component>(getComponentCount());
        for( ToolbarConstraints tc : constraints ) {
            if( !tc.isVisible() || tc.getAlign() != align )
                continue;
            Component c = findComponent( tc.getName() );
            if( null != c )
                res.add( c );
        }
        return res;
    }

    /**
     * @param name
     * @return Toolbar constraints associated with the given name.
     */
    private ToolbarConstraints findConstraints( String name ) {
        for( ToolbarConstraints tc : constraints ) {
            if( tc.getName().equals(name) )
                return tc;
        }
        return null;
    }

    /**
     * @param name
     * @return Component associated with the given name.
     */
    private Component findComponent(String name) {
        for( Component c : getComponents() ) {
            if( name.equals(c.getName()) )
                return c;
        }
        return null;
    }

    private List<ToolbarConstraints> getConstraints( ToolbarConstraints.Align align ) {
        ArrayList<ToolbarConstraints> res = new ArrayList<ToolbarConstraints>(constraints.size());
        for( ToolbarConstraints tc : constraints ) {
            if( tc.getAlign() == align ) {
                res.add( tc );
            }
        }
        return res;
    }

    /**
     * @return The bounds in screen coords of the free area between left and right toolbars.
     */
    Rectangle getFreeAreaBounds() {
        int x1 = 0;
        int x2 = getWidth();
        for( int i=constraints.size()-1; i>=0; i-- ) {
            ToolbarConstraints tc = constraints.get(i);
            if( !tc.isVisible() || tc == dragConstraints)
                continue;
            if( tc.getAlign() == ToolbarConstraints.Align.left ) {
                Component c = findComponent(tc.getName());
                if( null != c ) {
                    x1 = c.getLocation().x + c.getWidth();
                    break;
                }
            }
        }

        for( int i=0; i<constraints.size(); i++ ) {
            ToolbarConstraints tc = constraints.get(i);
            if( !tc.isVisible() || tc == dragConstraints)
                continue;
            if( tc.getAlign() == ToolbarConstraints.Align.right ) {
                Component c = findComponent(tc.getName());
                if( null != c ) {
                    x2 = c.getLocation().x;
                    break;
                }
            }
        }
        Rectangle res = new Rectangle( x1, 0, x2-x1, getHeight() );
        Point location = res.getLocation();
        if( isShowing() ) {
            SwingUtilities.convertPointToScreen(location, this);
            res.setLocation( location );
        }
        return res;
    }

    private boolean isStretchLastToolbar() {
        return isMetalLaF || isNimbusLaF || isGTKLaF || isAquaLaF;
    }

    private static final boolean isMetalLaF = "Metal".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final boolean isNimbusLaF = "Nimbus".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final boolean isGTKLaF = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final boolean isAquaLaF = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    /**
     * Layout of a single toolbar row.
     */
    private class ToolbarLayout implements LayoutManager {


        public ToolbarLayout() {
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension d = new Dimension( 0,0 );
            d.height = getPreferredHeight();
            for( Component c : getComponents() ) {
                if( !c.isVisible() )
                    continue;
                d.width += c.getPreferredSize().width;
            }
            Insets borderInsets = parent.getInsets();
            if( null != borderInsets ) {
                d.height += borderInsets.top;
                d.height += borderInsets.bottom;
            }
            return d;
        }

        public Dimension minimumLayoutSize(Container parent) {
            Dimension d = new Dimension( 0,0 );
            d.height = getMinimumHeight();
            for( Component c : getComponents() ) {
                if( !c.isVisible() )
                    continue;
                d.width += c.getMinimumSize().width;
            }
            Insets borderInsets = parent.getInsets();
            if( null != borderInsets ) {
                d.height += borderInsets.top;
                d.height += borderInsets.bottom;
            }
            return d;
        }

        public void layoutContainer(Container parent) {
            int w = parent.getWidth();
            int h = parent.getHeight();
            int top = 0;
            Insets borderInsets = parent.getInsets();
            if( null != borderInsets ) {
                h -= borderInsets.top + borderInsets.bottom;
                top = borderInsets.top;
            }
            Dimension prefSize = preferredLayoutSize(parent);

            List<Component> leftBars = getContainers( ToolbarConstraints.Align.left );
            List<Component> rightBars = getContainers(  ToolbarConstraints.Align.right );

            Map<Component, Integer> bar2width = new HashMap<Component, Integer>(leftBars.size() + rightBars.size());
            if( prefSize.width > w ) {
                //we need more horizontal space than what's available, some bars will be truncated

                //start truncating bars stacked to the right
                int toCut = prefSize.width - w;
                List<Component> reversed = new ArrayList<Component>(rightBars);
                Collections.reverse(reversed);
                for( Component c : reversed ) {
                    int barPrefWidth = c.getPreferredSize().width;
                    int barMinWidth = c.getMinimumSize().width;
                    int availableToCut = barPrefWidth - barMinWidth;
                    if( toCut <= availableToCut ) {
                        bar2width.put( c, barPrefWidth-toCut );
                        toCut = 0;
                    } else {
                        bar2width.put( c, barMinWidth );
                        toCut -= availableToCut;
                    }
                }

                reversed = new ArrayList<Component>(leftBars);
                Collections.reverse(reversed);
                for( Component c : reversed ) {
                    int barPrefWidth = c.getPreferredSize().width;
                    int barMinWidth = c.getMinimumSize().width;
                    int availableToCut = barPrefWidth - barMinWidth;
                    if( toCut <= availableToCut ) {
                        bar2width.put( c, barPrefWidth-toCut );
                        toCut = 0;
                    } else {
                        bar2width.put( c, barMinWidth );
                        toCut -= availableToCut;
                    }
                }
            } else {
                for( Component c : leftBars )
                    bar2width.put(c, c.getPreferredSize().width);

                for( Component c : rightBars )
                    bar2width.put(c, c.getPreferredSize().width);
            }

            //layout left bars
            int x = 0;
            for( Component c : leftBars ) {
                int barWidth = bar2width.get(c);
                if( (isStretchLastToolbar()) && leftBars.indexOf(c) == leftBars.size()-1 ) {
                    //stretch the last left bar across the remaining free space
                    //up to the first right bar / right border of the toolbar row
                    int rightBarsWidth = 0;
                    for( Component rb : rightBars ) {
                        rightBarsWidth += bar2width.get(rb);
                    }
                    barWidth = w - x - rightBarsWidth;
                }
                c.setBounds(x, top, barWidth, h);
                x += barWidth;
            }

            x = w;
            Collections.reverse(rightBars);
            for( Component c : rightBars ) {
                int barWidth = bar2width.get(c);
                x -= barWidth;
                c.setBounds(x, top, barWidth, h);
            }
        }

        private int getPreferredHeight() {
            int h = 0;
            for( Component c : getComponents() ) {
                if( !c.isVisible() )
                    continue;
                Dimension d = c.getPreferredSize();
                if( d.height > h )
                    h = d.height;
            }
            return h;
        }

        private int getMinimumHeight() {
            int h = 0;
            for( Component c : getComponents() ) {
                if( !c.isVisible() )
                    continue;
                Dimension d = c.getMinimumSize();
                if( d.height > h )
                    h = d.height;
            }
            return h;
        }
    }
} // end of class ToolbarRow

