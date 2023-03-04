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
 * ToolbarTabDisplayerUI.java
 *
 * Created on June 1, 2004, 12:31 AM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;
import org.openide.util.Utilities;

/**
 * A TabDisplayerUI which uses a JToolBar and JButtons.  This look is used
 * in various places such as the property sheet and component inspector.
 *
 * @author  Tim Boudreau
 */
public class ToolbarTabDisplayerUI extends AbstractTabDisplayerUI {
    private JToolBar toolbar = null;
    private static final Border buttonBorder;
    private static final boolean isMac = "Aqua".equals(UIManager.getLookAndFeel().getID());
    
    static {
        //Get the HIE requested button border via an ugly hack
        Border b = (Border) UIManager.get("nb.tabbutton.border"); //NOI18N

        if (b == null) {
            JToolBar toolbar = new JToolBar();
            JButton button = new JButton();
            toolbar.setRollover(true);
            toolbar.add(button);
            b = button.getBorder();
            toolbar.remove(button);
        }
        
        buttonBorder = b;
    }
    
    /** Creates a new instance of ToolbarTabDisplayerUI */
    public ToolbarTabDisplayerUI(TabDisplayer disp) {
        super (disp);
    }
    
    public static ComponentUI createUI (JComponent jc) {
        return new ToolbarTabDisplayerUI ((TabDisplayer) jc);
    }
    
    protected TabLayoutModel createLayoutModel() {
        //not used
        return null;
    }
    
    protected void install() {
        toolbar = new TabToolbar();
        toolbar.setLayout (new AutoGridLayout());
        toolbar.setFloatable (false);
        toolbar.setRollover( true );
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            toolbar.setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
        displayer.setLayout (new BorderLayout());
        displayer.add (toolbar, BorderLayout.CENTER);
        if (displayer.getModel() != null && displayer.getModel().size() > 0) {
            syncButtonsWithModel();
        }
    }
    
    protected void modelChanged() {
        if (syncButtonsWithModel()) {
            if (displayer.getParent() != null) {
                ((JComponent) displayer.getParent()).revalidate();
            }
        }
    }
    
    protected MouseListener createMouseListener() {
        return null;
    }
    
    private IndexButton findButtonFor (int index) {
        Component[] c = toolbar.getComponents();
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof IndexButton && ((IndexButton) c[i]).getIndex() == index) {
                return (IndexButton) c[i];
            }
        }
        return null;
    }  
    
    public void requestAttention (int tab) {
        //not implemented
    }
    
    public void cancelRequestAttention (int tab) {
        //not implemented
    }
    
    protected ChangeListener createSelectionListener() {
        return new ChangeListener() {
            private int lastKnownSelection = -1;
            public void stateChanged (ChangeEvent ce) {
                int selection = selectionModel.getSelectedIndex();
                if (selection != lastKnownSelection) {
                    if (lastKnownSelection != -1) {
                        IndexButton last = findButtonFor(lastKnownSelection);
                        if (last != null) {
                            last.getModel().setSelected(false);
                        }
                    }
                    if (selection != -1) {
                        IndexButton current = findButtonFor (selection);
                        if (toolbar.getComponentCount() == 0) {
                            syncButtonsWithModel();
                        }
                        if (current != null) {
                            current.getModel().setSelected (true);
                        }
                    }
                }
                lastKnownSelection = selection;
            }
        };
    }
    
    public Polygon getExactTabIndication(int index) {
        JToggleButton jb = findButtonFor (index);
        if (jb != null) {
            return new EqualPolygon (jb.getBounds());
        } else {
            return new EqualPolygon (new Rectangle());
        }
    }
    
    public Polygon getInsertTabIndication(int index) {
        return getExactTabIndication (index);
    }
    
    public Rectangle getTabRect(int index, Rectangle destination) {
        destination.setBounds(findButtonFor(index).getBounds());
        return destination;
    }
    
    public int tabForCoordinate(Point p) {
        Point p1 = SwingUtilities.convertPoint(displayer, p, toolbar);
        Component c = toolbar.getComponentAt(p1);
        if (c instanceof IndexButton) {
            return ((IndexButton) c).getIndex();
        }
        return -1;
    }
    
    public Dimension getPreferredSize(JComponent c) {
        return toolbar.getPreferredSize();
    }

    public Dimension getMinimumSize(JComponent c) {
        return toolbar.getMinimumSize();
    }

    
    private boolean syncButtonsWithModel() {
        assert SwingUtilities.isEventDispatchThread();
        
        int expected = displayer.getModel().size();
        int actual = toolbar.getComponentCount();
        boolean result = actual != expected;
        if (result) {
            if (expected > actual) {
                for (int i = actual; i < expected; i++) {
                    toolbar.add(new IndexButton());
                }
            } else if (expected < actual) {
                for (int i=expected; i < actual; i++) {
                    toolbar.remove(toolbar.getComponentCount() -1);
                }
            }
        }
        int selIdx = selectionModel.getSelectedIndex();
        if (selIdx != -1) {
            findButtonFor(selIdx).setSelected(true);
        }
        if (result) {
            displayer.revalidate();
            displayer.doLayout();
            displayer.repaint();
        }
        return result;
    }    

    public Icon getButtonIcon(int buttonId, int buttonState) {
        return null;
    }
    
    private ButtonGroup bg = new ButtonGroup();
    private static int fontHeight = -1;
    private static int ascent = -1;
    
    /**
     * A button which will get its content from an index in the datamodel 
     * which corresponds to its index in its parent's component hierarchy.
     */
    public final class IndexButton extends JToggleButton implements ActionListener {
        private String lastKnownText = null;

        /** Create a new button representing an index in the model.  The index is immutable for the life of the
         * button.
         */
        public IndexButton () {
            addActionListener(this);
            setFont (displayer.getFont());
            setFocusable(false);
            if( isMac ) {
                putClientProperty("JButton.buttonType", "square");
            } else {
                setBorder (buttonBorder);
                setMargin(new Insets(0, 3, 0, 3));
            }
            setRolloverEnabled( true );

        }

        @Override
        public void addNotify() {
            super.addNotify();
            ToolTipManager.sharedInstance().registerComponent(this);
            bg.add(this);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            ToolTipManager.sharedInstance().unregisterComponent(this);
            bg.remove(this);
        }

        /** Accessor for the UI delegate to determine if the tab displayer is currently active */
        public boolean isActive() {
            return displayer.isActive();
        }

        @Override
        public String getText() {
             //so the font height is included in super.getPreferredSize();
            return " ";
        }

        public String doGetText() {
            int idx = getIndex();
            if (idx == -1) {
                //We're being called in the superclass constructor when the UI is
                //assigned
                return "";
            }
            if (getIndex() < displayer.getModel().size()) {
                lastKnownText = displayer.getModel().getTab(idx).getText();
            } else {
                return "This tab doesn't exist."; //NOI18N
            }
            return lastKnownText;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension result = super.getPreferredSize();
            String s = doGetText();
            int w = DefaultTabLayoutModel.textWidth(s, getFont(), this);
            result.width += w;
            // as we cannot get the button small enough using the margin and border...
            if (Utilities.isMac()) {
                // #67128 the -3 heuristics seems to cripple the buttons on macosx. it looks ok otherwise.
                result.height -= 3;
                result.width -= 5;
            } 
            return result;
        }

        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            
            ColorUtil.setupAntialiasing(g);
            
            String s = doGetText();
            
            Insets ins = getInsets();
            int x = ins.left;
            int w = getWidth() - (ins.left + ins.right);
            int h = getHeight();
            
            int txtW = DefaultTabLayoutModel.textWidth(s, getFont(), this);
            if (txtW < w) {
                x += (w / 2) - (txtW / 2);
            }
            
            if (fontHeight == -1) {
                FontMetrics fm = g.getFontMetrics(getFont());
                fontHeight = fm.getHeight();
                ascent = fm.getMaxAscent();
            }
            int y = ins.top + ascent + (((getHeight() - (ins.top + ins.bottom)) / 2) - (fontHeight / 2));
            
            HtmlRenderer.renderString(s, g, x, y, w, h, getFont(), getForeground(), 
                HtmlRenderer.STYLE_TRUNCATE, true);
        }

        @Override
        public String getToolTipText() {
            return displayer.getModel().getTab(getIndex()).getTooltip();
        }

        /** Implementation of ActionListener - sets the selected index in the selection model */
        public final void actionPerformed(ActionEvent e) {
            selectionModel.setSelectedIndex (getIndex());
        }

        /** Get the index into the data model that this button represents */
        public int getIndex() {
            if (getParent() != null) {
                return Arrays.asList(getParent().getComponents()).indexOf(this);
            }
            return -1;
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        /**
         * Test if the text or icon in the model has changed since the last time <code>getText()</code> or
         * <code>getIcon()</code> was called.  If a change has occured, the button will fire the appropriate
         * property changes, including preferred size, to ensure the tab displayer is re-laid out correctly.
         * This method is called when a change happens in the model over the index this button represents.
         *
         * @return true if something has changed
         */
        final boolean checkChanged() {
            boolean result = false;
            String txt = lastKnownText;
            String nu = doGetText();
            if (nu != txt) { //Equality compare probably not needed
                firePropertyChange ("text", lastKnownText, doGetText()); //NOI18N
                result = true;
            }
            if (result) {
                firePropertyChange ("preferredSize", null, null); //NOI18N
            }
            return result;
        }
    }

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID());//NOI18N
    
    /**
     * Originally in org.netbeans.form.palette.CategorySelectPanel.
     *
     * @author Tomas Pavek
     */
    static class AutoGridLayout implements LayoutManager {

        private int h_margin_left = isAqua ? 0 : 2; // margin on the left
        private int h_margin_right = isAqua ? 0 : 1; // margin on the right
        private int v_margin_top = isAqua ? 0 : 2; // margin at the top
        private int v_margin_bottom = isAqua ? 0 : 3; // margin at the bottom
        private int h_gap = isAqua ? 0 : 1; // horizontal gap between components
        private int v_gap = isAqua ? 0 : 1; // vertical gap between components

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                int containerWidth = parent.getWidth();
                if( containerWidth <= 0 && parent.getParent() != null )
                    containerWidth = parent.getParent().getWidth();
                int count = parent.getComponentCount();

                if (containerWidth <= 0 || count == 0) {
                    // compute cumulated width of all components placed on one row
                    int cumulatedWidth = 0;
                    int height = 0;
                    for (int i=0; i < count; i++) {
                        Dimension size = parent.getComponent(i).getPreferredSize();
                        cumulatedWidth += size.width;
                        if (i + 1 < count)
                            cumulatedWidth += h_gap;
                        if (size.height > height)
                            height = size.height;
                    }
                    cumulatedWidth += h_margin_left + h_margin_right;
                    height += v_margin_top + v_margin_bottom;
                    return new Dimension(cumulatedWidth, height);
                }

                // otherwise the container already has some width set - so we
                // just compute preferred height for it

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = 0;
                for (int i=0; i < count; i++) {
                    Dimension size = parent.getComponent(i).getPreferredSize();
                    if (size.width > columnWidth)
                        columnWidth = size.width;
                    if (size.height > rowHeight)
                        rowHeight = size.height;
                }

                // compute column count
                int columnCount = 0;
                int w = h_margin_left + columnWidth + h_margin_right;
                do {
                    columnCount++;
                    w += h_gap + columnWidth;
                }
                while (w <= containerWidth && columnCount < count);

                // compute row count and preferred height
                int rowCount = count / columnCount +
                               (count % columnCount > 0 ? 1 : 0);
                int prefHeight = v_margin_top + rowCount * rowHeight
                                     + (rowCount - 1) * v_gap + v_margin_bottom;

                Dimension result = new Dimension(containerWidth, prefHeight);
                return result;
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(h_margin_left + h_margin_right,
                                 v_margin_top + v_margin_bottom);
        }

        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                int count = parent.getComponentCount();
                if (count == 0)
                    return;

                // get max. component width and height
                int columnWidth = 0;
                int rowHeight = 0;
                for (int i=0; i < count; i++) {
                    Dimension size = parent.getComponent(i).getPreferredSize();
                    if (size.width > columnWidth)
                        columnWidth = size.width;
                    if (size.height > rowHeight)
                        rowHeight = size.height;
                }

                // compute column count
                int containerWidth = parent.getWidth();
                int columnCount = 0;
                int w = h_margin_left + columnWidth + h_margin_right;
                do {
                    columnCount++;
                    w += h_gap + columnWidth;
                }
                while (w <= containerWidth && columnCount < count);

                // adjust layout matrix - balance number of columns according
                // to last row
                if (count % columnCount > 0) {
                    int roundedRowCount = count / columnCount;
                    int lastRowEmpty = columnCount - count % columnCount;
                    if (lastRowEmpty > roundedRowCount)
                        columnCount -= lastRowEmpty / (roundedRowCount + 1);
                }

                // adjust column width
                if (count > columnCount)
                    columnWidth = (containerWidth - h_margin_left - h_margin_right
                                     - (columnCount - 1) * h_gap) / columnCount;
                if (columnWidth < 0)
                    columnWidth = 0;
                
                // layout the components
                for (int i=0, col=0, row=0; i < count; i++) {
                    parent.getComponent(i).setBounds(
                                       h_margin_left + col * (columnWidth + h_gap),
                                       v_margin_top + row * (rowHeight + v_gap),
                                       columnWidth,
                                       rowHeight);
                    if (++col >= columnCount) {
                        col = 0;
                        row++;
                    }
                }
            }
        }
    }    
    
    static class TabToolbar extends JToolBar {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Color color = g.getColor();

            g.setColor(UIManager.getColor("controlLtHighlight")); // NOI18N
            g.drawLine(0, 0, getWidth(), 0);
            g.drawLine(0, 0, 0, getHeight()-1);
            g.setColor(UIManager.getColor("controlShadow")); // NOI18N
            g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

            g.setColor(color);
        }
    }
}
