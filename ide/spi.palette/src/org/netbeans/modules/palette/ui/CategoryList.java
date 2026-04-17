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


package org.netbeans.modules.palette.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.text.Position;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Utils;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 * Specialized JList for palette items (content of a palette category) - having
 * special UI and renderer providing fine-tuned alignment, rollover effect,
 * showing names and different icon size. Used by CategoryDescriptor.
 *
 * @author Tomas Pavek, S. Aubrecht
 */

public class CategoryList extends JList implements Autoscroll {

    private int rolloverIndex = -1;
    private boolean showNames;

    static final int BASIC_ICONSIZE = BeanInfo.ICON_COLOR_16x16;
    private int iconSize = BASIC_ICONSIZE;
    
    private Category category;
    private PalettePanel palettePanel;

    private static WeakReference<ListCellRenderer> rendererRef;
    
    static final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    static final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() );
    
    private AutoscrollSupport support;

    /**
     * Constructor.
     */
    CategoryList( Category category, PalettePanel palettePanel ) {
        this.category = category;
        this.palettePanel = palettePanel;
        if( isGTK || isNimbus ) {
            setBackground( new Color( UIManager.getColor("Tree.background").getRGB() ) );//NOI18N
            setOpaque(true);
        } else {
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
                setBackground(UIManager.getColor("NbExplorerView.background"));
            else
                setBackground(UIManager.getColor ("Panel.background"));
        }
        setBorder (new EmptyBorder (0, 0, 0, 0));
        setVisibleRowCount (0);
        setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer (getItemRenderer ());
        setLayoutOrientation( HORIZONTAL_WRAP );
        getAccessibleContext().setAccessibleName( category.getDisplayName() );
        getAccessibleContext().setAccessibleDescription( category.getShortDescription() );

        initActions();
    }
    
    private void initActions() {
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0, false ), "defaultAction" );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK, false ), "popup" );

        ActionMap map = getActionMap();
        map.put( "defaultAction", new DefaultAction( this ) );
        map.put( "popup", new PopupAction() );
        map.put( "selectPreviousRow", new MoveFocusAction( map.get( "selectPreviousRow" ), false ) );
        map.put( "selectNextRow", new MoveFocusAction( map.get( "selectNextRow" ), true ) );
        map.put( "selectPreviousColumn", new MoveFocusAction( new ChangeColumnAction( map.get( "selectPreviousColumn" ), false ), false ) );
        map.put( "selectNextColumn", new MoveFocusAction( new ChangeColumnAction( map.get( "selectNextColumn" ), true ), true ) );
        Node categoryNode = category.getLookup().lookup(org.openide.nodes.Node.class);
        if( null != categoryNode )
            map.put( "paste", new Utils.PasteItemAction( categoryNode ) );
        else
            map.remove( "paste" );
        map.put( "copy", new CutCopyAction( true ) );
        map.put( "cut", new CutCopyAction( false ) );
    }

    Item getItemAt( int index ) {
        if( index < 0 || index >= getModel().getSize() )
            return null;
        
        return (Item)getModel().getElementAt( index );
    }

    Category getCategory() {
        return category;
    }

    @Override
    public void updateUI () {
        if( null != rendererRef )
            rendererRef.clear();
        setUI (new CategoryListUI ());
        invalidate ();
    }

    // Workaround for issue 39037. Due to the following method we can
    // use getPreferredSize() in the implementation of the method
    // getPreferredHeight(). Otherwise we would have to copy the content
    // of getPreferredSize() into the layout manager of the enclosing JScrollPane.
    // We cannot change the width directly through setBounds() method
    // because it would force another repaint.
    Integer tempWidth;

    @Override
    public int getWidth () {
        return (tempWidth == null) ? super.getWidth () : tempWidth.intValue ();
    }

    // ---------

    boolean getShowNames () {
        return showNames;
    }

    void setShowNames (boolean show) {
        if (show != showNames) {
            showNames = show;
            firePropertyChange ("cellRenderer", null, null); // NOI18N
        }
    }

    int getIconSize () {
        return iconSize;
    }

    void setIconSize (int size) {
        if (size != iconSize) {
            iconSize = size;
            firePropertyChange ("cellRenderer", null, null); // NOI18N
        }
    }

    // workaround for bug 4832765, which can cause the
    // scroll pane to not let the user easily scroll up to the beginning
    // of the list.  An alternative would be to set the unitIncrement
    // of the JScrollBar to a fixed value. You wouldn't get the nice
    // aligned scrolling, but it should work.
    @Override
    public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction) {
        int row;
        if (orientation == SwingConstants.VERTICAL &&
                direction < 0 && (row = getFirstVisibleIndex ()) != -1) {
            Rectangle r = getCellBounds (row, row);
            if ((r.y == visibleRect.y) && (row != 0)) {
                Point loc = r.getLocation ();
                loc.y--;
                int prevIndex = locationToIndex (loc);
                Rectangle prevR = getCellBounds (prevIndex, prevIndex);

                if (prevR == null || prevR.y >= r.y) {
                    return 0;
                }
                return prevR.height;
            }
        }
        return super.getScrollableUnitIncrement (visibleRect, orientation, direction);
    }

    /**
     * Returns preferred height of the list for the specified <code>width</code>.
     *
     * @return preferred height of the list for the specified <code>width</code>.
     */
    public int getPreferredHeight (int width) {
        return ((CategoryListUI) getUI ()).getPreferredHeight (width);
    }
    
    public void resetRollover() {
        rolloverIndex = -1;
        repaint();
    }
    
    int getColumnCount() {
        if( getModel().getSize() > 0 ) {
            Insets insets = getInsets ();
            int listWidth = getWidth () - (insets.left + insets.right);
            int cellWidth = getCellBounds( 0, 0 ).width;
            if( listWidth >= cellWidth ) {
                return listWidth / cellWidth;
            }
        }
        return 1;
    }

    // --------
    // list item renderer

    private static ListCellRenderer getItemRenderer () {
        ListCellRenderer renderer = rendererRef == null ? null : rendererRef.get ();
        if (renderer == null) {
            renderer = new ItemRenderer ();
            rendererRef = new WeakReference<ListCellRenderer>( renderer );
        }
        return renderer;
    }

    static class ItemRenderer implements ListCellRenderer {

        /** toolbar containing the button, null for GTK L&F */
        private JToolBar toolbar;
        /** toggle button used as renderer component */ 
        private JToggleButton button;

        ItemRenderer () {
            if (button == null) {
                button = new JToggleButton ();
                button.setMargin (new Insets (1, 1, 1, 0));
                
                if (!CategoryButton.isGTK) {
                    toolbar = new JToolBar ();
                    toolbar.setRollover (true);
                    toolbar.setFloatable (false);
                    toolbar.setLayout (new BorderLayout (0, 0));
                    toolbar.setBorder (BorderFactory.createEmptyBorder());
                    toolbar.add (button);
                    if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
                        toolbar.setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
                }
            }
        }

        @Override
        public Component getListCellRendererComponent (JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            CategoryList categoryList = (CategoryList) list;
            boolean showNames = categoryList.getShowNames ();
            int iconSize = categoryList.getIconSize ();
            
            JComponent rendererComponent = toolbar != null ? toolbar : button;

            Item item = (Item) value;
            Image icon = item.getIcon (iconSize);
            if (icon != null) {
                button.setIcon (ImageUtilities.image2Icon(icon));
            }

            button.setText (showNames ? item.getDisplayName () : null);
            rendererComponent.setToolTipText( item.getShortDescription() ); // NOI18N

            button.setSelected (isSelected);
            //if (defaultBorder == null) { // Windows or Metal
                // let the toolbar UI render the button according to "rollover"
                button.getModel ().setRollover (index == categoryList.rolloverIndex && !isSelected);
            /*} else { // Mac OS X and others - set the border explicitly
                button.setBorder (defaultBorder);
            }*/
            button.setBorderPainted ((index == categoryList.rolloverIndex) || isSelected);

            button.setHorizontalAlignment (showNames ? SwingConstants.LEFT : SwingConstants.CENTER);
            button.setHorizontalTextPosition (SwingConstants.RIGHT);
            button.setVerticalTextPosition (SwingConstants.CENTER);
            Color c = new Color(UIManager.getColor("Tree.background").getRGB()); //NOI18N
            if( isNimbus )
                toolbar.setBackground(c);
            if( isGTK )
                button.setBackground(c);

            return rendererComponent;
        }
    }
    
    /** notify the Component to autoscroll */
    @Override
    public void autoscroll( Point cursorLoc ) {
        if( null != getParent() && null != getParent().getParent() ) {
            Point p = SwingUtilities.convertPoint( this, cursorLoc, getParent().getParent() );
            getSupport().autoscroll( p );
        }
    }

    /** @return the Insets describing the autoscrolling
     * region or border relative to the geometry of the
     * implementing Component.
     */
    @Override
    public Insets getAutoscrollInsets() {
        return getSupport().getAutoscrollInsets();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( palettePanel );
        }

        return support;
    }

    /**
     * Take focus from CategoryButton, i.e. select the first or the last item in the list
     */
    void takeFocusFrom( Component c ) {
        int indexToSelect = -1;
        if( c.getParent() != getParent() ) {
            //this is not 'our' CategoryButton so we'll assume it's the one below this category list
            indexToSelect = getModel().getSize()-1;
        } else if( getModel().getSize() > 0 ) {
            indexToSelect = 0;
        }
        requestFocus();
        setSelectedIndex( indexToSelect );
        if( indexToSelect >= 0 ) {
            ensureIndexIsVisible( indexToSelect );
        }
    }
    
    @Override
    public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
        return -1;
    }
    
    // ---------
    // list UI
    
    static class CategoryListUI extends BasicListUI {

        @Override
        protected void updateLayoutState () {
            super.updateLayoutState ();

            if (list.getLayoutOrientation () == JList.HORIZONTAL_WRAP) {
                Insets insets = list.getInsets ();
                int listWidth = list.getWidth () - (insets.left + insets.right);
                if (listWidth >= cellWidth) {
                    int columnCount = listWidth / cellWidth;
                    cellWidth = (columnCount == 0) ? 1 : listWidth / columnCount;
                }
            }
        }

        public int getPreferredHeight (int width) {
            ((CategoryList) list).tempWidth = Integer.valueOf (width);
            int result;
            try {
                result = (int) getPreferredSize (list).getHeight ();
            } finally {
                ((CategoryList) list).tempWidth = null;
            }
            return result;
        }

        @Override
        protected MouseInputListener createMouseInputListener () {
            return new ListMouseInputHandler ();
        }

        private class ListMouseInputHandler extends MouseInputHandler {

                int selIndex = -1;
                
            @Override
            public void mouseClicked(MouseEvent e) {
                if( !list.isEnabled() )
                    return;
                
                if( e.getClickCount() > 1 ) {
                    selIndex = getValidIndex( e.getPoint() );
                    if( selIndex >= 0 ) {
                        list.setSelectedIndex( selIndex );
                        Item item = (Item)list.getModel().getElementAt( selIndex );
                        ActionEvent ae = new ActionEvent( e.getSource(), e.getID(), "doubleclick", e.getWhen(), e.getModifiers() );
                        item.invokePreferredAction( ae );
                        e.consume();
                    }
                }
            }

            @Override
            public void mousePressed( MouseEvent e ) {
                if( getValidIndex( e.getPoint() ) >= 0 ) {
                    selIndex = list.getSelectedIndex ();
                    super.mousePressed (e);
                }
            }

            @Override
            public void mouseDragged( MouseEvent e ) {
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                mouseEntered( e );
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                if( list.isEnabled() )
                    setRolloverIndex( getValidIndex( e.getPoint() ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                if( list.isEnabled() )
                    setRolloverIndex( -1 );
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                if( getValidIndex( e.getPoint() ) >= 0) {
                    super.mouseReleased (e);
                    if( selIndex > -1 && list.getSelectedIndex () == selIndex )
                        list.removeSelectionInterval( selIndex, selIndex );
                }
            }

            private void setRolloverIndex (int index) {
                int oldIndex = ((CategoryList) list).rolloverIndex;
                if (index != oldIndex) {
                    ((CategoryList) list).rolloverIndex = index;
                    if (oldIndex > -1) {
                        Rectangle r = getCellBounds (list, oldIndex, oldIndex);
                        if (r != null)
                            list.repaint (r.x, r.y, r.width, r.height);
                    }
                    if (index > -1) {
                        Rectangle r = getCellBounds (list, index, index);
                        if (r != null)
                            list.repaint (r.x, r.y, r.width, r.height);
                    }
                }
            }
        }

        private int getValidIndex (Point p) {
            int index = locationToIndex (list, p);
            return index >= 0 && getCellBounds (list, index, index).contains (p) ?
                    index : -1;
        }
    }
    
    private static class DefaultAction extends AbstractAction {
        private CategoryList list;
        public DefaultAction( CategoryList list ) {
            this.list = list;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            Item item = list.getItemAt( list.getSelectedIndex() );
            item.invokePreferredAction( e );
        }

        @Override
        public boolean isEnabled() {
            return list.isEnabled() && list.getSelectedIndex() >= 0;
        }
    }
    
    private class PopupAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int posX = 0;
            int posY = 0;
            Item item = getItemAt( getSelectedIndex() );
            if( null != item ) {
                Rectangle rect = getCellBounds( getSelectedIndex(), getSelectedIndex() );
                posX = rect.x;
                posY = rect.y + rect.height;
            }
            Action[] actions = null == item ? category.getActions() : item.getActions();
            JPopupMenu popup = Utilities.actionsToPopup( actions, CategoryList.this );
            Utils.addCustomizationMenuItems( popup, palettePanel.getController(), palettePanel.getSettings() );
            popup.show( getParent(), posX, posY );
        }

        @Override
        public boolean isEnabled() {
            return CategoryList.this.isEnabled();
        }
    }
    
    private class MoveFocusAction extends AbstractAction {
        private Action defaultAction;
        private boolean focusNext;
                
        public MoveFocusAction( Action defaultAction, boolean focusNext ) {
            this.defaultAction = defaultAction;
            this.focusNext = focusNext;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int selIndexBefore = getSelectedIndex();
            defaultAction.actionPerformed( e );
            int selIndexCurrent = getSelectedIndex();
            if( selIndexBefore != selIndexCurrent )
                return;
            
            if( focusNext && 0 == selIndexCurrent && getModel().getSize() > 1 && getModel().getSize() > getColumnCount() )
                return;
            
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Container container = kfm.getCurrentFocusCycleRoot();
            FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
            if( null == policy )
                policy = kfm.getDefaultFocusTraversalPolicy();
            Component next = focusNext ? policy.getComponentAfter( container, CategoryList.this )
                                      : policy.getComponentBefore( container, CategoryList.this );
            if(next instanceof CategoryButton) {
                clearSelection();
                next.requestFocus();
            }
        }
    }
    
    private class ChangeColumnAction extends AbstractAction {
        private Action defaultAction;
        private boolean selectNext;
                
        public ChangeColumnAction( Action defaultAction, boolean selectNext ) {
            this.defaultAction = defaultAction;
            this.selectNext = selectNext;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int selIndexBefore = getSelectedIndex();
            defaultAction.actionPerformed( e );
            int selIndexCurrent = getSelectedIndex();
            if( (selectNext && selIndexBefore < selIndexCurrent)
                ||
                (!selectNext && selIndexBefore > selIndexCurrent) )
                return;
            
            if( selectNext ) {
                if( selIndexCurrent == selIndexBefore+1 )
                    selIndexCurrent++;
                if( selIndexCurrent < getModel().getSize()-1 ) {
                    setSelectedIndex( selIndexCurrent+1 );
                    scrollRectToVisible( getCellBounds( selIndexCurrent+1, selIndexCurrent+1 ) );
                }
            } else {
                if( selIndexCurrent > 0 ) {
                    setSelectedIndex( selIndexCurrent-1 );
                    scrollRectToVisible( getCellBounds( selIndexCurrent-1, selIndexCurrent-1 ) );
                }
            }
        }
    }
    
    private class CutCopyAction extends AbstractAction {
        private boolean doCopy;
        public CutCopyAction( boolean doCopy ) {
            this.doCopy = doCopy;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            Item item = getItemAt( getSelectedIndex() );
            if( null == item )
                return;
            Node itemNode = item.getLookup().lookup(org.openide.nodes.Node.class);
            if( null == itemNode )
                return;
            Action performer;
            if( doCopy )
                performer = new Utils.CopyItemAction( itemNode );
            else
                performer = new Utils.CutItemAction( itemNode );
            if( performer.isEnabled() )
                performer.actionPerformed( e );
        }

        @Override
        public boolean isEnabled() {
            return getSelectedIndex() >= 0;
        }
    }
}
