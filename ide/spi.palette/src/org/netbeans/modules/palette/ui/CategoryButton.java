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

package org.netbeans.modules.palette.ui;

import java.awt.dnd.Autoscroll;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Utils;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
 * @author David Kaspar, Jan Stola
 */
class CategoryButton extends JCheckBox implements Autoscroll {

    static final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    static final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() );
    static final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );

    private CategoryDescriptor descriptor;
    private Category category;
    
    private AutoscrollSupport support;

    private static Color aquaBackground;
    
    // Workaround for JDK bug in GTK #6527149 - use Metal UI class
    static {
        if (isGTK) {
            UIManager.put("MetalCheckBoxUI_4_GTK", "javax.swing.plaf.metal.MetalCheckBoxUI");
        }
        if( isAqua ) {
            Color defBk = UIManager.getColor("NbExplorerView.background"); //NOI18N
            if( null == defBk )
                defBk = new JPanel().getBackground();
            aquaBackground = new Color( Math.max(0,defBk.getRed()-15)
                    , Math.max(0,defBk.getGreen()-15)
                    , Math.max(0,defBk.getBlue()-15));
        }
    }

    @Override
    public String getUIClassID() {
        String classID = super.getUIClassID();
        if (isGTK) {
            classID = "MetalCheckBoxUI_4_GTK";
        }
        return classID;
    }

    

    CategoryButton( CategoryDescriptor descriptor, Category category ) {
        this.descriptor = descriptor;
        this.category = category;

        //force initialization of PropSheet look'n'feel values 
        UIManager.get( "nb.propertysheet" );
            
        setFont( getFont().deriveFont( Font.BOLD ) );
        setMargin(new Insets(0, 3, 0, 3));
        setFocusPainted( false );

        setSelected( false );

        setHorizontalAlignment( SwingConstants.LEFT );
        setHorizontalTextPosition( SwingConstants.RIGHT );
        setVerticalTextPosition( SwingConstants.CENTER );

        updateProperties();
        
        if( getBorder() instanceof CompoundBorder ) { // from BasicLookAndFeel
            Dimension pref = getPreferredSize();
            pref.height -= 3;
            setPreferredSize( pref );
        }

        addActionListener( new ActionListener () {
            public void actionPerformed( ActionEvent e ) {
                boolean opened = !CategoryButton.this.descriptor.isOpened();
                setExpanded( opened );
            }
        });
        
        addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent e) {
                scrollRectToVisible( getBounds() );
            }
            public void focusLost(FocusEvent e) {
            }
        });

        initActions();
    }
    
    private void initActions() {
        InputMap inputMap = getInputMap( WHEN_FOCUSED );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0, false ), "moveFocusDown" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0, false ), "moveFocusUp" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0, false ), "collapse" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0, false ), "expand" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK, false ), "popup" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( "ctrl V" ), "paste" ); //NOI18N //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( "PASTE" ), "paste" ); //NOI18N //NOI18N
        
        ActionMap actionMap = getActionMap();
        actionMap.put( "moveFocusDown", new MoveFocusAction( true ) ); //NOI18N
        actionMap.put( "moveFocusUp", new MoveFocusAction( false ) ); //NOI18N
        actionMap.put( "collapse", new ExpandAction( false ) ); //NOI18N
        actionMap.put( "expand", new ExpandAction( true ) ); //NOI18N
        actionMap.put( "popup", new PopupAction() ); //NOI18N
        Node categoryNode = (Node)category.getLookup().lookup( Node.class );
        if( null != categoryNode )
            actionMap.put( "paste", new Utils.PasteItemAction( categoryNode ) ); //NOI18N
    }
    
    void updateProperties() {
        setIcon( UIManager.getIcon(isGTK ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon") );
        setSelectedIcon( UIManager.getIcon(isGTK ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon") );
        setRolloverIcon( UIManager.getIcon(isGTK ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon") );
        setRolloverSelectedIcon( UIManager.getIcon(isGTK ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon") );
        Mnemonics.setLocalizedText( this, category.getDisplayName() );
        setToolTipText( category.getShortDescription() );
        getAccessibleContext().setAccessibleName( category.getDisplayName() );
        getAccessibleContext().setAccessibleDescription( category.getShortDescription() );
        if( isAqua ) {
            setContentAreaFilled(true);
            setOpaque(true);
            setBackground( new Color(0,0,0) );
            setForeground( new Color(255,255,255) );
        }
        if( isNimbus ) {
            setOpaque(true);
            setContentAreaFilled(true);
        }
    }
    
    Category getCategory() {
        return category;
    }

    
    /** notify the Component to autoscroll */
    public void autoscroll( Point cursorLoc ) {
        Component dest = getParent().getParent();
        if( null == dest || null == SwingUtilities.getWindowAncestor(dest) )
            return;
        Point p = SwingUtilities.convertPoint( this, cursorLoc, dest );
        getSupport().autoscroll( p );
    }

    /** @return the Insets describing the autoscrolling
     * region or border relative to the geometry of the
     * implementing Component.
     */
    public Insets getAutoscrollInsets() {
        return getSupport().getAutoscrollInsets();
    }
    
    boolean isExpanded() {
        return isSelected();
    }
    
    void setExpanded( boolean expand ) {
        setSelected( expand );
        if( descriptor.isOpened() == expand )
            return;
        descriptor.setOpened( expand );
        descriptor.getPalettePanel().computeHeights( expand ? CategoryButton.this.category : null );
        requestFocus ();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( PalettePanel.getDefault() );
        }

        return support;
    }

    @Override
    public Color getBackground() {
        if( isFocusOwner() ) {
            if( isGTK || isNimbus )
                return UIManager.getColor("Tree.selectionBackground"); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetBackground" ); //NOI18N
        } else {
            if( isAqua ) {
                Color defBk = UIManager.getColor("NbExplorerView.background");
                if( null == defBk )
                    defBk = Color.gray;
                return new Color( defBk.getRed()-10, defBk.getGreen()-10, defBk.getBlue()-10);
            }
            if( isGTK || isNimbus ) {
                if( getModel().isRollover() )
                    return new Color( UIManager.getColor( "Menu.background" ).getRGB() ).darker(); //NOI18N
                return new Color( UIManager.getColor( "Menu.background" ).getRGB() );//NOI18N
            }
            return UIManager.getColor( "PropSheet.setBackground" ); //NOI18N
        }
    }

    @Override
    public Color getForeground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor( "Table.foreground" ); //NOI18N
            else if( isGTK || isNimbus )
                return UIManager.getColor( "Tree.selectionForeground" ); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetForeground" ); //NOI18N
        } else {
            if( isAqua ) {
                Color res = UIManager.getColor("PropSheet.setForeground"); //NOI18N

                if (res == null) {
                    res = UIManager.getColor("Table.foreground"); //NOI18N

                    if (res == null) {
                        res = UIManager.getColor("textText");

                        if (res == null) {
                            res = Color.BLACK;
                        }
                    }
                }
                return res;
            }
            if( isGTK || isNimbus ) {
                return new Color( UIManager.getColor( "Menu.foreground" ).getRGB() ); //NOI18N
            }
            return super.getForeground();
        }
    }
    
    private class MoveFocusAction extends AbstractAction {
        private boolean moveDown;
        
        public MoveFocusAction( boolean moveDown ) {
            this.moveDown = moveDown;
        }
        
        public void actionPerformed(ActionEvent e) {
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Container container = kfm.getCurrentFocusCycleRoot();
            FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
            if( null == policy )
                policy = kfm.getDefaultFocusTraversalPolicy();
            Component next = moveDown ? policy.getComponentAfter( container, CategoryButton.this )
                                      : policy.getComponentBefore( container, CategoryButton.this );
            if(next instanceof CategoryList) {
                if( ((CategoryList)next).getModel().getSize() != 0 ) {
                    ((CategoryList)next).takeFocusFrom( CategoryButton.this );
                    return;
                } else {
                    next = moveDown ? policy.getComponentAfter( container, next )
                                    : policy.getComponentBefore( container, next );
                }
            }
            if(next instanceof CategoryButton) {
                next.requestFocus();
            }
        }
    }
    
    private class ExpandAction extends AbstractAction {
        private boolean expand;
        
        public ExpandAction( boolean expand ) {
            this.expand = expand;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( expand == isExpanded() )
                return;
            setExpanded( expand );
        }
    }

    private class PopupAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Action[] actions = category.getActions();
            JPopupMenu popup = Utilities.actionsToPopup( actions, CategoryButton.this );
            Utils.addCustomizationMenuItems( popup, descriptor.getPalettePanel().getController(), descriptor.getPalettePanel().getSettings() );
            popup.show( getParent(), 0, getHeight() );
        }
    }
}
