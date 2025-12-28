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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Utilities;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;



/**
 * Palette's visual component implementation.
 *
 * @author S. Aubrecht
 */
public class PalettePanel extends JPanel implements Scrollable {

    private static PalettePanel theInstance;
    
    private PaletteController controller;
    private Model model;
    private Settings settings;
    
    private ModelListener modelListener;
    private PropertyChangeListener settingsListener;
    
    private CategoryDescriptor[] descriptors = new CategoryDescriptor[0];
    private Category selectedCategory;
    
    private final Object lock = new Object();
    private MouseListener mouseListener;
    
    private static final boolean isAquaLaF = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private MyScrollPane scrollPane;
    
    private DnDSupport dndSupport;

    private final KeyListener kl;

    private PalettePanel () {
        setLayout( new PaletteLayoutManager() );
        addMouseListener( mouseListener() );
        kl = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    if( removeSearchField() ) {
                        e.consume();
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if( !e.isConsumed() && e.getKeyChar() != KeyEvent.VK_ESCAPE )
                   displaySearchField(e.getKeyChar());
            }
        };

        if (!GraphicsEnvironment.isHeadless()) {
            dndSupport = new DnDSupport(this);
        }

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
            setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
        else
            setBackground( UIManager.getColor ("Panel.background") );
        
        addKeyListener(kl);

        SearchFieldListener searchFieldListener = new SearchFieldListener();
        searchTextField.addKeyListener(searchFieldListener);
        searchTextField.addFocusListener(searchFieldListener);
        searchTextField.getDocument().addDocumentListener(searchFieldListener);
    }
    
    public static synchronized PalettePanel getDefault() {
        if( null == theInstance ) {
            theInstance = new PalettePanel();
        }
        return theInstance;
    }
    
    public JScrollPane getScrollPane() {
        if( null == scrollPane ) {
            scrollPane = new MyScrollPane( this );
        }
        return scrollPane;
    }

    CategoryDescriptor getCategoryDescriptor( Category category ) {
        for( int i=0; i<descriptors.length; i++ ) {
            CategoryDescriptor descriptor = descriptors[i];
            if( descriptor.getCategory () == category )
                return descriptor;
        }
        return null;
    }

    private CategoryDescriptor[] computeDescriptors( Category[] categories ) {
        if( null == categories ) {
            return new CategoryDescriptor[0];
        }
        String searchString= getSearchString();
        categories = getVisibleCategories( categories );
        List<CategoryDescriptor> newDescriptors = new ArrayList<CategoryDescriptor>(categories.length);
        for( int i=0; i<categories.length; i++) {
            Category category = categories[i];
            CategoryDescriptor descriptor = getCategoryDescriptor( category );
            if( descriptor == null ) {
                descriptor = new CategoryDescriptor( this, category );
                descriptor.getList().addKeyListener(kl);
                descriptor.getButton().addKeyListener(kl);
                descriptor.setShowNames( getSettings().getShowItemNames() );
                descriptor.setIconSize( getSettings().getIconSize() );
            } else {
                descriptor.refresh();
            }
            descriptor.setWidth( getWidth() );
            if( descriptor.match( searchString ) )
                newDescriptors.add(descriptor);
        }
        return newDescriptors.toArray(new CategoryDescriptor[0]);
    }

    private String getSearchString() {
        if( null == searchpanel )
            return null;
        return searchTextField.getText().trim().toLowerCase();
    }
    private Category[] getVisibleCategories( Category[] cats ) {
        ArrayList<Category> tmp = new ArrayList<Category>( cats.length );
        for( int i=0; i<cats.length; i++ ) {
            if( settings.isVisible( cats[i] ) ) {
                tmp.add( cats[i] );
            }
        }
        return tmp.toArray(new Category[0]);
    }

    void computeHeights( Category openedCategory ) {
        computeHeights( descriptors, openedCategory );
    }

    private void computeHeights( CategoryDescriptor[] paletteCategoryDescriptors, 
                                 Category openedCategory) {
        if( paletteCategoryDescriptors == null || paletteCategoryDescriptors.length <= 0 ) {
            return;
        }
        revalidate();
    }

    private static boolean arrayContains( Object[] objects, Object object ) {
        if( objects == null || object == null )
            return false;
        for( int i=0; i<objects.length; i++ ) {
            if( objects[i] == object )
                return true;
        }
        return false;
    }

    private void setDescriptors( CategoryDescriptor[] paletteCategoryDescriptors ) {
        for( int i=0; i<descriptors.length; i++ ) {
            CategoryDescriptor descriptor = descriptors[i];
            if( !arrayContains( paletteCategoryDescriptors, descriptor ) ) {
                remove( descriptor.getComponent() );
                if (dndSupport != null) {
                    dndSupport.remove(descriptor);
                }
            }
        }
        for( int i=0; i<paletteCategoryDescriptors.length; i++ ) {
            CategoryDescriptor paletteCategoryDescriptor = paletteCategoryDescriptors[i];
            if( !arrayContains( descriptors, paletteCategoryDescriptor ) ) {
                add( paletteCategoryDescriptor.getComponent() );
                if (dndSupport != null) {
                    dndSupport.add(paletteCategoryDescriptor);
                }
            }
        }
        if( descriptors.length == 0 && paletteCategoryDescriptors.length > 0 ) {
            boolean isAnyCategoryOpened = false;
            for( int i=0; i<paletteCategoryDescriptors.length; i++ ) {
                if( paletteCategoryDescriptors[i].isOpened() ) {
                    isAnyCategoryOpened = true;
                    break;
                }
            }
            if( !isAnyCategoryOpened ) {
                paletteCategoryDescriptors[0].setOpened( true );
            }
        }
        descriptors = paletteCategoryDescriptors;        
        revalidate();
    }
    
    public void doRefresh() {
        if( null != controller )
            controller.refresh();
    }
    
    public void refresh () {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized( lock ) {
                    setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
                    CategoryDescriptor[] paletteCategoryDescriptors = computeDescriptors( null != model ? model.getCategories() : null );
                    setDescriptors (paletteCategoryDescriptors);
                    if( null != settings ) {
                        setIconSize( settings.getIconSize() );
                        setShowItemNames( settings.getShowItemNames() );
                        setItemWidth( settings.getShowItemNames() ? settings.getItemWidth() : -1 );
                    }
                    if( null != model ) {
                        Item item = model.getSelectedItem();
                        Category category = model.getSelectedCategory();
                        setSelectedItemFromModel( category, item );
                    }
                    setCursor( Cursor.getDefaultCursor() );
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater( runnable );
        }
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        refresh ();
    }

    void select( Category category, Item item ) {
        if( category != selectedCategory ) {
            CategoryDescriptor selectedDescriptor = findDescriptorFor( selectedCategory );
            if( selectedDescriptor != null ) {
                selectedDescriptor.setSelectedItem( null );
            }
        }
        selectedCategory = category;
        if( null != model ) {
            if( null == category || null == item )
                model.clearSelection();
            else
                model.setSelectedItem( category.getLookup(), item.getLookup() );
        }
    }
    
    private void setSelectedItemFromModel( Category category, Item item ) {
        if( null != selectedCategory && !selectedCategory.equals( category ) ) {
            CategoryDescriptor selectedDescriptor = findDescriptorFor( selectedCategory );
            if( selectedDescriptor != null ) {
                selectedDescriptor.setSelectedItem( null );
            }
        }
        CategoryDescriptor descriptor = findDescriptorFor( category );
        if( descriptor == null ) {
            return;
        }
        if( item != null ) {
            selectedCategory = category;
         }
        descriptor.setSelectedItem( item );
    }

    private CategoryDescriptor findDescriptorFor( Category category ) {
        if( null != descriptors ) {
            for( int i= 0; i<descriptors.length; i++ ) {
                CategoryDescriptor descriptor = descriptors[i];
                if( descriptor.getCategory().equals( category ) )
                    return descriptor;
            }
        }
        return null;
    }
    
    private void scrollToCategory( final Category category ) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized( lock ) {
                    CategoryDescriptor descriptor = findDescriptorFor( category );
                    if( null != descriptor ) {
                        scrollPane.validate();
                        Point loc = descriptor.getComponent().getLocation();
                        scrollPane.getViewport().setViewPosition( loc );
                    }
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater( runnable );
        }
    }

    /**
     * Set new palette model and settings.
     */
    public void setContent( PaletteController newController, Model newModel, Settings newSettings ) {
        synchronized (lock ) {
            if( newModel == model && newSettings == settings ) {
                return;
            }
            
            Model old = model;
            if( model != null && null != modelListener ) {
                model.removeModelListener( modelListener );
            }
            if( settings != null && null != settingsListener ) {
                settings.removePropertyChangeListener( settingsListener );
            }
            
            model = newModel;
            settings = newSettings;
            controller = newController;
            selectedCategory = null;
            if( model != null ) {
                model.addModelListener( getModelListener() );
            }
            if( null != settings ) {
                settings.addPropertyChangeListener( getSettingsListener() );
            }
            refresh();
        }
    }
    
    private MouseListener mouseListener() {
        if( null == mouseListener ) {
            mouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if( SwingUtilities.isRightMouseButton( event ) && null != model ) {
                        JPopupMenu popup = Utilities.actionsToPopup( model.getActions(), PalettePanel.this );
                        Utils.addCustomizationMenuItems( popup, getController(), getSettings() );
                        popup.show( (Component)event.getSource(), event.getX(), event.getY() );
                    }
                }
            };
        }
        return mouseListener;
    }

    private void setShowItemNames( boolean showNames ) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setShowNames( showNames );
        }
        repaint();
    }

    private void setIconSize(int iconSize) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setIconSize( iconSize );
        }
        repaint();
    }
    
    private void setItemWidth(int itemWidth) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setItemWidth( itemWidth );
        }
        repaint();
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight () {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth () {
        return true;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize () {
        return getPreferredSize ();
    }

    @Override
    public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    @Override
    public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }
    
    public HelpCtx getHelpCtx() {
        HelpCtx ctx = null;
        if( null != getModel() ) {
            Item selItem = getModel().getSelectedItem();
            if( null != selItem ) {
                Node selNode = (Node) selItem.getLookup().lookup( Node.class );
                if( null != selNode )
                    ctx = selNode.getHelpCtx();
            } 
            if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
                //find the selected category
                CategoryDescriptor selCategory = null;
                for( int i=0; i<descriptors.length; i++ ) {
                    if( descriptors[i].isSelected() ) {
                        selCategory = descriptors[i];
                        break;
                    }
                }
                if( null != selCategory ) {
                    Node selNode = (Node) selCategory.getCategory().getLookup().lookup( Node.class );
                    if( null != selNode )
                        ctx = selNode.getHelpCtx();
                }
            }
            if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
                Node selNode = (Node) getModel().getRoot().lookup( Node.class );
                if( null != selNode )
                    ctx = selNode.getHelpCtx();
            }
        }
        if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
            ctx = new HelpCtx("CommonPalette"); // NOI18N
        }
        return ctx;
    }

    private ModelListener getModelListener() {
        if( null == modelListener ) {
            modelListener = new ModelListener() {
                @Override
                public void categoriesAdded( Category[] addedCategories ) {
                    PalettePanel.this.refresh();
                    if( null != addedCategories && addedCategories.length > 0 ) {
                        PalettePanel.this.scrollToCategory(addedCategories[0] );
                    }
                }

                @Override
                public void categoriesRemoved( Category[] removedCategories ) {
                    PalettePanel.this.refresh();
                }

                @Override
                public void categoriesReordered() {
                    PalettePanel.this.refresh();
                }
                
                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    if( ModelListener.PROP_SELECTED_ITEM.equals( evt.getPropertyName() ) ) {
                        Item selectedItem = model.getSelectedItem();
                        Category selectedCategory = model.getSelectedCategory();
                        setSelectedItemFromModel( selectedCategory, selectedItem );
                    }
                }

            };
        } 
        return modelListener;
    }
    
    private PropertyChangeListener getSettingsListener() {
        if( null == settingsListener ) {
            settingsListener = new PropertyChangeListener() {
                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    if( PaletteController.ATTR_IS_VISIBLE.equals( evt.getPropertyName() ) ) {
                        PalettePanel.this.refresh();
                        for( int i=0; null != descriptors && i<descriptors.length; i++ ) {
                            descriptors[i].computeItems();
                        }
                    } else if( PaletteController.ATTR_ICON_SIZE.equals( evt.getPropertyName() ) ) {
                        
                        setIconSize( getSettings().getIconSize() );
                        
                    } else if( PaletteController.ATTR_SHOW_ITEM_NAMES.equals( evt.getPropertyName() ) ) {
                        
                        setShowItemNames( getSettings().getShowItemNames() );
                        setItemWidth( getSettings().getShowItemNames() ? getSettings().getItemWidth() : -1 );
                        
                    }
                }

            };
        } 
        return settingsListener;
    }

    Model getModel() {
        return model;
    }
    
    Settings getSettings() {
        return settings;
    }
    
    PaletteController getController() {
        return controller;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if( null != model )
            model.refresh();
    }

    private JPanel searchpanel = null;
    // searchTextField manages focus because it handles VK_TAB key
    private JTextField searchTextField = new JTextField() {
        @Override
        public boolean isManagingFocus() {
            return true;
        }

        @Override
        public void processKeyEvent(KeyEvent ke) {
            //override the default handling so that
            //the parent will never receive the escape key and
            //close a modal dialog
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                ke.consume();
            } else {
                super.processKeyEvent(ke);
            }
        }
    };
    private int originalScrollMode;

    private static class SearchPanel extends JPanel {
        public SearchPanel() {
            if( isAquaLaF )
                setBorder(BorderFactory.createEmptyBorder(9,6,8,2));
            else
                setBorder(BorderFactory.createEmptyBorder(2,6,2,2));
            setOpaque( true );
        }

        @Override
        protected void paintComponent(Graphics g) {
            if( isAquaLaF && g instanceof Graphics2D ) {
                Graphics2D g2d = (Graphics2D) g;
                Color col1 = UIManager.getColor("NbExplorerView.quicksearch.background.top"); //NOI18N
                Color col2 = UIManager.getColor("NbExplorerView.quicksearch.background.bottom"); //NOI18N
                Color col3 = UIManager.getColor("NbExplorerView.quicksearch.border"); //NOI18N
                if( col1 != null && col2 != null && col3 != null ) {
                    g2d.setPaint( new GradientPaint(0, 0, col1, 0, getHeight(), col2));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor( col3 );
                    g2d.drawLine(0, 0, getWidth(), 0);
                    return;
                }
            }
            super.paintComponent(g);
            g.setColor( UIManager.getColor( "PropSheet.setBackground" ) ); //NOI18N
            g.drawLine(0, 0, getWidth(), 0);
        }
    }


    private void prepareSearchPanel() {
        if( searchpanel == null ) {
            searchpanel = new SearchPanel();

            JLabel lbl = new JLabel(NbBundle.getMessage(PalettePanel.class, "LBL_QUICKSEARCH")); //NOI18N
            searchpanel.setLayout(new GridBagLayout());
            searchpanel.add(lbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,5), 0, 0));
            searchpanel.add(searchTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,5), 0, 0));
            searchpanel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
            lbl.setLabelFor(searchTextField);
            searchTextField.setColumns(10);
            searchTextField.setMaximumSize(searchTextField.getPreferredSize());
            searchTextField.putClientProperty("JTextField.variant", "search"); //NOI18N
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            JButton btnCancel = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/palette/resources/cancel.png", true));
            btnCancel.setBorder(BorderFactory.createEmptyBorder());
            btnCancel.setBorderPainted(false);
            btnCancel.setOpaque(false);
            btnCancel.setContentAreaFilled(false);
            searchpanel.add(btnCancel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,5), 0, 0));
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    removeSearchField();
                }
            });
        }
    }

    /**
     * Adds the search field to the tree.
     */
    private void displaySearchField(char initialChar) {
        if( null != searchpanel )
            return;
        JViewport vp = scrollPane.getViewport();
        originalScrollMode = vp.getScrollMode();
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        searchTextField.setFont(getFont());
        searchTextField.setText(String.valueOf(initialChar));
        prepareSearchPanel();
        scrollPane.add(searchpanel);
        invalidate();
        revalidate();
        repaint();
        searchTextField.requestFocus();
    }

    /**
     * Removes the search field from the tree.
     */
    private boolean removeSearchField() {
        if( null == searchpanel )
            return false;

        scrollPane.remove(searchpanel);
        searchpanel = null;
        scrollPane.getViewport().setScrollMode(originalScrollMode);
        invalidate();
        revalidate();
        repaint();
        refresh();

        focusPalette();
        return true;
    }

    private void focusPalette() {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
                public void run() {
                    if( null == model )
                        return;
                    Item item = model.getSelectedItem();
                    Category category = model.getSelectedCategory();
                    setSelectedItemFromModel( category, item );
                    if( null != category ) {
                        CategoryDescriptor cd = findDescriptorFor(category);
                        cd.getList().requestFocus();
                    } else {
                        descriptors[0].getButton().requestFocus();
                    }
                }
            }
        );
    }

    private class MyScrollPaneLayout extends ScrollPaneLayout {

        @Override
        public void layoutContainer( Container parent ) {
            super.layoutContainer(parent);
            if( null != searchpanel && searchpanel.isVisible() ) {
                Insets innerInsets = scrollPane.getInnerInsets();
                Dimension prefSize = searchpanel.getPreferredSize();
                searchpanel.setBounds(innerInsets.left, parent.getHeight()-innerInsets.bottom-prefSize.height,
                        parent.getWidth()-innerInsets.left-innerInsets.right, prefSize.height);
            }
        }
    }
    
    private class PaletteLayoutManager implements LayoutManager {
        
        @Override
        public void addLayoutComponent( String name, Component comp) {
        }
        
        @Override
        public void layoutContainer( Container parent ) {
            int width = getWidth ();

            int height = 0;
            for( int i=0; i<descriptors.length; i++ ) {
                CategoryDescriptor paletteCategoryDescriptor = descriptors[i];
                paletteCategoryDescriptor.setPositionY( height );
                JComponent comp = paletteCategoryDescriptor.getComponent();
                comp.setSize( width, comp.getPreferredSize().height );
                height += paletteCategoryDescriptor.getComponent().getHeight();
            }
        }
        
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }
        
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int height = 0;
            int width = getWidth();
            for( int i=0; i<descriptors.length; i++ ) {
                CategoryDescriptor descriptor = descriptors[i];
                height += descriptor.getPreferredHeight( width )+1;
            }
            return new Dimension( 10 /* not used - tracks viewports width*/, height );
        }
        
        @Override
        public void removeLayoutComponent(Component comp) {
        }
    }

    private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {

        SearchFieldListener() {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            refresh();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            refresh();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            refresh();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_ESCAPE) {
                removeSearchField();
            } else if(keyCode == KeyEvent.VK_ENTER) {
                focusPalette();
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            // make sure nothing is selected
            searchTextField.select(0, 0);
            searchTextField.setCaretPosition(searchTextField.getText().length());
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    }

    private class MyScrollPane extends JScrollPane {
        public MyScrollPane( JComponent view ) {
            super( view );
            setLayout(new MyScrollPaneLayout());
            setBorder( BorderFactory.createEmptyBorder() );
            addMouseListener( mouseListener() );
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
                getViewport().setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
            else
                getViewport().setBackground( UIManager.getColor ("Panel.background") );
            // GTK L&F paints extra border around viewport, get rid of it
            setViewportBorder(null);
        }

        @Override
        public Insets getInsets() {
            Insets res = getInnerInsets();
            res = new Insets(res.top, res.left, res.bottom, res.right);
            if( null != searchpanel && searchpanel.isVisible() ) {
                res.bottom += searchpanel.getPreferredSize().height;
            }
            return res;
        }

        private Insets getInnerInsets() {
            Insets res = super.getInsets();
            if( null == res ) {
                res = new Insets(0,0,0,0);
            }
            return res;
        }
    }
}
