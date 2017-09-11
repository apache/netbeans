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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.form.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.actions.PropertyAction;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;

/**
 *
 * @author joshua.marinacci@sun.com
 */
public class MenuEditLayer extends JPanel {
    /* === constants for the look of the designer === */
    public static final Border DRAG_MENU_BORDER = BorderFactory.createLineBorder(Color.BLACK,1);
    public static final Border DRAG_SEPARATOR_BORDER = BorderFactory.createLineBorder(Color.RED,1);
    public static final Color SELECTED_MENU_BACKGROUND = new Color(0xA5A6A9);
    public static final Color EMPTY_ICON_COLOR = new Color(0xDDDDDD);
    public static final int EMPTY_ICON_BORDER_WIDTH = 2;
    
    /* === private constants === */
    private static final boolean USE_NEW_ITEM_COLOR_SWITCHING = false;
    
    /* === public and package level fields. these should probably become getters and setters  ===*/
    VisualDesignerPopupFactory hackedPopupFactory = null;
    FormDesigner formDesigner;
    JLayeredPane layers;
    JComponent glassLayer;
    DropTargetLayer dropTargetLayer;
    boolean showMenubarWarning = false;
    
    /* === private fields === */
    private Map<JMenu, PopupMenuUI> menuPopupUIMap;
    
    public enum SelectedPortion { Icon, Text, Accelerator, All, None };
    private SelectedPortion selectedPortion = SelectedPortion.None;
    
    private KeyboardMenuNavigator keyboardMenuNavigator;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    private DragOperation dragop;
    private FormModelListener menuBarFormListener;
    private PropertyChangeListener selectionListener;
    private boolean isAlive = true;
    private static final boolean USE_JSEPARATOR_FIX = true;
    
    /** Creates a new instance of MenuEditLayer */
    public MenuEditLayer(final FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        menuPopupUIMap = new HashMap<JMenu, PopupMenuUI>();
        formModelListeners = new HashMap<RADVisualContainer,FormModelListener>();
        
        layers = new JLayeredPane();
        this.setLayout(new BorderLayout());
        this.add(layers, BorderLayout.CENTER);
        
        dragop = new DragOperation(this);
        
        glassLayer = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
            }
        };

        layers.add(glassLayer, new Integer(500)); // put the glass layer over the drag layer
        glassLayer.setSize(400,400); //josh: do i need this line? probably can delete it.
        
        dropTargetLayer = new DropTargetLayer(this);
        layers.add(dropTargetLayer, new Integer(JLayeredPane.DRAG_LAYER-5)); // put the drop target layer just above the drag layer
        
        // make the extra layers resize to the main component
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                glassLayer.setSize(MenuEditLayer.this.getSize());
                dropTargetLayer.setSize(MenuEditLayer.this.getSize());
            }
        });
        
        MouseInputAdapter mia = new GlassLayerMouseListener();
        glassLayer.addMouseListener(mia);
        glassLayer.addMouseMotionListener(mia);
        glassLayer.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                // #133628: user wants to cancel the action so deselect menu-related component in the palette
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dragop.fastEnd();
                }
            }
        });
        configureSelectionListener();
    }
    
    DragOperation getDragOperation() {
        return dragop;
    }
    
    public static boolean isMenuRelatedRADComponent(RADComponent comp) {
        if(comp == null) return false;
        return isMenuRelatedComponentClass(comp.getBeanClass());
    }
    
    public static boolean isNonMenuJSeparator(RADComponent comp) {
        if(comp == null) return false;
        if(JSeparator.class.isAssignableFrom(comp.getBeanClass())) {
            RADComponent parent = comp.getParentComponent();
            if(parent != null && JMenu.class.isAssignableFrom(parent.getBeanClass())) {
                return false;
            }
            return true;
        }
        return false;
    }
        
    public static boolean isMenuBarContainer(RADComponent comp) {
        if(comp == null) return false;
        Class clas = comp.getBeanClass();
        if(clas == null) return false;
        if(JMenuBar.class.isAssignableFrom(clas)) return true;
        return false;
    }
    
    public static boolean isMenuRelatedContainer(RADComponent comp) {
        if(comp == null) return false;
        Class clas = comp.getBeanClass();
        if(clas == null) return false;
        if(JMenu.class.isAssignableFrom(clas)) return true;
        if(JPopupMenu.class.isAssignableFrom(clas)) return true;
        return false;
    }
    
    public static boolean isMenuRelatedComponentClass(Class clas) {
        if(clas == null) return false;
        if(JMenuItem.class.isAssignableFrom(clas)) return true;
        if(JMenu.class.isAssignableFrom(clas)) return true;
        if(JSeparator.class.isAssignableFrom(clas)) return true;
        if(JMenuBar.class.isAssignableFrom(clas)) return true;
        return false;
    }
    
    public boolean isPossibleNewMenuComponent(PaletteItem item) {
        if(item == null) return false;
        if(item.getComponentClass() == null) return false;
        if(JMenuItem.class.isAssignableFrom(item.getComponentClass())) {
            return true;
        }
        return false;
    }
    
    
    public void startNewMenuComponentPickAndPlop(PaletteItem item, Point pt) {
        this.setVisible(true);
        this.requestFocus();
        dragop = new DragOperation(this);
        dragop.start(item, pt);
    }
    
    public void startNewMenuComponentDragAndDrop(PaletteItem item) {
        this.setVisible(true);
        this.requestFocus();
        configureGlassLayer();
        configureFormListeners();
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        /* Using the assistant now instead of text ontop of the glasspane.
         * josh: I need to delete all of the previous code related to the showMenubarWarning boolean
        if(showMenubarWarning) {
            g2.drawString("You cannot add a menu component to a form without a menubar.", 5, getHeight()-30);
        }*/
        g2.dispose();
    }
    
    
    // the public method for non-menu parts of the form editor to
    // start menu editing
    public void openAndShowMenu(RADComponent metacomp, Component comp) {
        //p("making sure the menu is open: " + metacomp +  " " + metacomp.getName());
        if(hackedPopupFactory == null) {
            this.hackedPopupFactory = new VisualDesignerPopupFactory(this);
        }
        openMenu(metacomp, comp);
        glassLayer.requestFocusInWindow();
    }
    
    void openMenu(RADComponent metacomp, Component comp) {
        getPopupFactory();
        configureGlassLayer();
        registerKeyListeners();
        configureFormListeners();
        configureSelectionListener();
        //reset the layers
        JMenu menu = (JMenu) comp;
        configureMenu(null,menu);
        showMenuPopup(menu);
        if(metacomp instanceof RADVisualContainer) {
            keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)metacomp);
        }
    }
    
    
    public void hideMenuLayer() {
        // tear down each menu and menu item
        unconfigureFormListeners();
        unconfigureSelectionListener();
        for(JMenu m : menuPopupUIMap.keySet()) {
            unconfigureMenu(m);
        }
        menuPopupUIMap.clear();
        if(hackedPopupFactory != null) {
            hackedPopupFactory.containerMap.clear();
            hackedPopupFactory = null;
        }
        if(dragop.isStarted()) {
            dragop.fastEnd();
        }
        // close all popup frames
        this.setVisible(false);
        if(keyboardMenuNavigator != null) {
            glassLayer.removeKeyListener(keyboardMenuNavigator);
            keyboardMenuNavigator.unconfigure();
            keyboardMenuNavigator = null;
        }
        backgroundMap.clear();
        //hackedPopupFactory.containerMap.clear();
        if(formDesigner.getHandleLayer() != null) {
            formDesigner.getHandleLayer().requestFocusInWindow();
        }
    }
    
    //josh: all this key listener stuff should go into a separate class
    private synchronized void registerKeyListeners() {
        if(keyboardMenuNavigator == null) {
            keyboardMenuNavigator = new KeyboardMenuNavigator(this);
            glassLayer.addKeyListener(keyboardMenuNavigator);
        }
    }
    
    private VisualDesignerPopupFactory getPopupFactory() {
        if(hackedPopupFactory == null) {
            hackedPopupFactory = new VisualDesignerPopupFactory(this);
            }
        return hackedPopupFactory;
    }
    
    private void configureGlassLayer() {
        try {
            glassLayer.setDropTarget(new DropTarget());
            glassLayer.getDropTarget().addDropTargetListener(new GlassLayerDropTargetListener());
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
    }
    
    PropertyChangeListener paletteListener = null;
    private void configureFormListeners() {
        
        if(menuBarFormListener == null) {
            menuBarFormListener = new FormModelListener() {
                @Override
                public void formChanged(FormModelEvent[] events) {
                    if(events != null) {
                        for(FormModelEvent evt : events) {
                            // if this is a menubar delete event
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_REMOVED) {
                                if(evt.getComponent() != null && 
                                        JMenuBar.class.isAssignableFrom(evt.getComponent().getBeanClass())) {
                                    hideMenuLayer();
                                }
                            }
                            if(evt.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED) {
                                hideMenuLayer();
                                isAlive = false;
                            }
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_ADDED) {
                                if(evt.getCreatedDeleted()) {
                                    if(USE_NEW_ITEM_COLOR_SWITCHING) {
                                        configureNewComponent(evt.getComponent());
                                    }
                                }
                            }
                            
                        }
                    }
                }                
            };
            formDesigner.getFormModel().addFormModelListener(menuBarFormListener);
        }
        if(paletteListener == null) {
            paletteListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(PaletteUtils.getSelectedItem() == null || 
                            !isMenuRelatedComponentClass(PaletteUtils.getSelectedItem().getComponentClass())) {
                        if(dragop != null && dragop.isStarted()) {
                            dragop.fastEnd();
                        }
                    }     
                }
            };
            paletteContext = formDesigner.getFormEditor().getFormDataObject().getFormFile();
            PaletteUtils.addPaletteListener(paletteListener, paletteContext);
        }
    }    
    
    FileObject paletteContext = null;
    private void unconfigureFormListeners() {
        if(menuBarFormListener != null) {
            if(formDesigner != null && formDesigner.getFormModel() != null) {
                formDesigner.getFormModel().removeFormModelListener(menuBarFormListener);
            }
        }
        if(paletteListener != null) {
            PaletteUtils.removePaletteListener(paletteListener, paletteContext);
            paletteContext = null;
            paletteListener = null;
        }
        menuBarFormListener = null;
    }    

    private void configureSelectionListener() {
        if(selectionListener == null) {
            selectionListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(!isAlive || !"selectedNodes".equals(evt.getPropertyName())) { // NOI18N
                        return;
                    }
                    Node[] newNodes = (Node[])evt.getNewValue();
                    List<RADComponent> selectedNodes = new ArrayList<RADComponent>();

                    for(Node n : newNodes) {
                        if(n instanceof RADComponentNode) {
                            RADComponentNode radn = (RADComponentNode) n;
                            selectedNodes.add(radn.getRADComponent());
                        }
                    }
                    
                    setSelectedRADComponents(selectedNodes);
                    
                }
                
            };
            formDesigner.addPropertyChangeListener(selectionListener);
        }
    }
    
    private void unconfigureSelectionListener() {
        if(selectionListener != null) {
            formDesigner.removePropertyChangeListener(selectionListener);
            selectionListener = null;
        }
    }

    void showMenuPopup(final JMenu menu) {
        getPopupFactory();
        // if already created then just make it visible
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel view = hackedPopupFactory.containerMap.get(menu);
            view.setVisible(true);
        } else {
            if(!isConfigured(menu)) {
                configureMenu(null, menu);
            }
            final JPopupMenu popup = menu.getPopupMenu();
            
            if(!(popup.getUI() instanceof VisualDesignerPopupMenuUI)) {
                popup.setUI(new VisualDesignerPopupMenuUI(this, popup.getUI()));
            }
            if (menu.isShowing()) {
                //force popup view creation
                hackedPopupFactory.getPopup(menu, null, 0, 0);
                
                // do later so that the component will definitely be on screen by then
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            popup.show(menu,0,menu.getHeight());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            //ignore anyexceptions caused by showing the popups
                        }
                        
                    }
                });
            }
        }
        this.validate();
    }
    
    public boolean isMenuLayerComponent(RADComponent metacomp) {
        if(metacomp == null) {
            return false;
        }
        if(metacomp.getBeanClass().equals(JMenuItem.class)) {
            return true;
        }
        if(metacomp.getBeanClass().equals(JMenu.class)) {
            return true;
        }
        return false;
    }
    
    
    
    void configureMenu(final JComponent parent, final JMenu menu) {
        // make sure it will draw it's border so we can have rollovers and selection
        menu.setBorderPainted(true);
        //install the wrapper icon if not a toplevel JMenu
        if(!isTopLevelMenu(menu)) {
            if(!(menu.getIcon() instanceof WrapperIcon)) {
                menu.setIcon(new WrapperIcon(menu.getIcon()));
            }
        }
        
        // configure the maps and popups
        JPopupMenu popup = menu.getPopupMenu();
        menuPopupUIMap.put(menu, popup.getUI());
        popup.setUI(new VisualDesignerPopupMenuUI(this, popup.getUI()));
        
        // get all of the components in this menu
        Component[] subComps = menu.getMenuComponents();
        // if this isn't the first time this menu has been opened then the sub components
        // will have been moved to the popupPanel already, so we will find them there instead.
        JPanel popupPanel = getPopupFactory().containerMap.get(menu);
        if(popupPanel != null) {
            subComps = popupPanel.getComponents();
        }
        
        RADVisualContainer menuRAD = (RADVisualContainer) formDesigner.getMetaComponent(menu);
        registerForm(menuRAD,menu);
        
        // recurse for sub-menus
        for(Component c : subComps) {
            if(c instanceof JMenu) {
                configureMenu(menu, (JMenu)c);
                RADComponent rad = formDesigner.getMetaComponent(c);
                registerForm((RADVisualContainer)rad,(JMenu)c);
            } else {
                configureMenuItem(menu, (JComponent) c);
            }
        }
    }
    
    private void unconfigureMenu(final JMenu menu) {
        if (hackedPopupFactory == null) return; // Issue 145981

        // restore the UI
        menu.getPopupMenu().setUI(menuPopupUIMap.get(menu));
        
        // restore all children
        JPanel popup = hackedPopupFactory.containerMap.get(menu);
        if(popup != null) {
            for(Component c : popup.getComponents()) {
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent) c);
                }
            }
            
            //hide the popup(s) if it's still visible
            if(menu.getPopupMenu() != null) {
                menu.getPopupMenu().setVisible(false);
            }
            popup.setVisible(false);
            //layers.remove(popup);
        }
        VisualDesignerJPanelPopup pop = hackedPopupFactory.getPopup(menu);
        if(pop != null) {
            pop.hide();
        }
        if(popup != null) {
            popup.setVisible(false);
        }
        menu.setPopupMenuVisible(false);
        hackedPopupFactory.containerMap.remove(menu);
    }
    
    private boolean isConfigured(JComponent c) {
        return menuPopupUIMap.containsKey(c);
    }
    
    
    void configureMenuItem(final JMenu parent, final JComponent c) {
        if(c instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) c;
            if(!(item.getIcon() instanceof WrapperIcon)) {
                item.setIcon(new WrapperIcon(item.getIcon()));
            }
            installAcceleratorPreview(item);
            item.setBorderPainted(true);
        }
    }
    
    static final int ACCEL_PREVIEW_WIDTH = 80;
    private static final Border accel_border = new Border() {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.WHITE);
            int offset = 5;
            if(DropTargetLayer.isAqua()) {
                offset = 2;
            }
            int ioffset = 0;
            if(DropTargetLayer.isVista()) {
                ioffset = -2;
            }
            g.fillRect(width-ACCEL_PREVIEW_WIDTH+offset,   1,ACCEL_PREVIEW_WIDTH-0+ioffset, height+ioffset);
            g.setColor(EMPTY_ICON_COLOR);
            g.drawRect(width-ACCEL_PREVIEW_WIDTH+offset,   1,ACCEL_PREVIEW_WIDTH-1+ioffset, height+ioffset);
            g.drawRect(width-ACCEL_PREVIEW_WIDTH+offset+1, 2,ACCEL_PREVIEW_WIDTH-3+ioffset, height-2+ioffset);
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("SansSerif",Font.PLAIN,10)); // NOI18N
            String shortcut = NbBundle.getMessage(MenuEditLayer.class, "MENU_Shortcut"); // NOI18N
            g.drawString(shortcut, width-ACCEL_PREVIEW_WIDTH+15,height-3+ioffset);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0,0,0,ACCEL_PREVIEW_WIDTH);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        
    };
    
    
    //installs a special border to represent the accelerator preview
    //if the menu item already has an accelerator, then it will
    //remove the preview if necessary.
    private static void installAcceleratorPreview(JMenuItem item) {
        if(item instanceof JMenu) return;
        //detect accelerator key
        boolean already_has_accel = false;
        if(item.getAccelerator() != null) already_has_accel = true;
        if(item.getAction() != null && item.getAction().getValue(Action.ACCELERATOR_KEY) != null) already_has_accel = true;

        
        
        boolean already_has_accel_border = false;
        if(item.getBorder() == accel_border) {
            already_has_accel_border = true;
            //uninstall if needed
            if(already_has_accel) {
                item.setBorder(null);
                return;
            }
        }
        
        if(item.getBorder() instanceof CompoundBorder) {
            CompoundBorder comp = (CompoundBorder)item.getBorder();
            if(comp.getInsideBorder() == accel_border) {
                already_has_accel_border = true;
                //uninstall if needed
                if(already_has_accel) {
                    item.setBorder(comp.getOutsideBorder());
                    return;
                }
            }
        }
        
        if(already_has_accel_border) return;
        if(already_has_accel) return;
        
        
        if(item.getBorder() == null) {
            item.setBorder(accel_border);
            return;
        }
        
        item.setBorder(BorderFactory.createCompoundBorder(
                    item.getBorder(),accel_border));
    }
    
    
    void unconfigureMenuItem(JComponent c) {
    }
    
    
    
    //override JComponent.isOpaque to always return false
    @Override
    public boolean isOpaque() {
        return false;
    }
    
    
    // returns true if parent really is an ancestor of target
    boolean isAncestor(JComponent target, JComponent parent) {
        if(!(parent instanceof JMenu)) {
            return false;
        }
        RADComponent targetRad = formDesigner.getMetaComponent(target);
        RADComponent parentRad = targetRad.getParentComponent();
        if(parentRad == null) return false;
        Object possibleParent = formDesigner.getComponent(parentRad);
        RADComponent realParentRad = formDesigner.getMetaComponent(parent);
        if(parentRad == realParentRad) {
            return true;
        }
        if(parent == possibleParent) {
            return true;
        } else {
            // recursively check up the chain to see if this is a further ancestor
            if(possibleParent instanceof JMenu) {
                return isAncestor((JMenu)possibleParent, parent);
            }
        }
        return false;
    }
    
    boolean hasSelectedDescendants(JMenu menu) {
        RADComponent comp =formDesigner.getMetaComponent(menu);
        if(comp instanceof RADVisualContainer) {
            return hasSelectedDescendants((RADVisualContainer)comp);
        }
        return false;
    }
    boolean hasSelectedDescendants(RADVisualContainer comp) {
        if(this.selectedComponents.contains(comp)) {
            return true;
        }
        for(RADComponent c : comp.getSubBeans()) {
            if(this.selectedComponents.contains(c)) return true;
            if(c instanceof RADVisualContainer) {
                boolean sel = hasSelectedDescendants((RADVisualContainer)c);
                if(sel) return true;
            }
        }
        return false;
    }
    
    JComponent getMenuParent(JComponent menu) {
        RADComponent targetRad = formDesigner.getMetaComponent(menu);
        RADComponent parentRad = targetRad.getParentComponent();
        if (parentRad != null) {
            Object possibleParent = formDesigner.getComponent(parentRad);
            if(possibleParent instanceof JComponent) {
                return (JComponent) possibleParent;
            }
        }
        return null;
    }
    
   
    List<RADComponent> getSelectedRADComponents() {
        return Collections.unmodifiableList(selectedComponents);
    }
    
    RADComponent getSingleSelectedComponent() {
        if(selectedComponents.isEmpty()) {
            return null;
        }
        if(selectedComponents.size() > 1) {
            setSelectedRADComponent(selectedComponents.get(0));
        }
        return selectedComponents.get(0);
    }
    
    
    private List<RADComponent> selectedComponents = new ArrayList<RADComponent>();
    
    
    boolean isComponentSelected() {
        return !selectedComponents.isEmpty();
    }
    
    void setSelectedRADComponent(RADComponent comp) {
        List<RADComponent> comps = new ArrayList<RADComponent>();
        comps.add(comp);
        setSelectedRADComponents(comps);
        formDesigner.setSelectedComponent(comp);
    }
    
    void addSelectedRADComponent(RADComponent comp) {
        if (!selectedComponents.contains(comp)) {
            List<RADComponent> comps = new ArrayList<RADComponent>();
            comps.addAll(selectedComponents);
            comps.add(comp);
            setSelectedRADComponents(comps);
            formDesigner.addComponentToSelection(comp);
        }
    }
    
    // #119217: toggle clicked component's selection status
    void toggleSelectedRADComponent(RADComponent comp) {
        if (selectedComponents.contains(comp)) { // component is already selected so remove it from selection
            selectedComponents.remove(comp);
            List<RADComponent> comps = new ArrayList<RADComponent>();
            comps.addAll(selectedComponents);
            setSelectedRADComponents(comps);
            formDesigner.removeComponentFromSelection(comp);
        } else {
            addSelectedRADComponent(comp);
        }
    }
    
    void setSelectedRADComponents(List<RADComponent> comps) {
        try {
            //clear old bgs first
            for(RADComponent rad : selectedComponents) {
                if(isMenuRelatedRADComponent(rad) && !isMenuBarContainer(rad) && !isNonMenuJSeparator(rad)) { // don't mess w/ the menubar's background
                    JComponent c = (JComponent) formDesigner.getComponent(rad);
                    if(c != null) { // could be null if comp was just deleted
                        c.setBackground(getNormalBackground(rad, c));
                    }
                }
            }

            selectedComponents.clear();
            selectedComponents.addAll(comps);

            //check for non-menu comps
            for(RADComponent c : selectedComponents) {
                if (!isMenuRelatedRADComponent(c) || isNonMenuJSeparator(c)) {
                    setVisible(false);
                    return;
                }
            }

            registerKeyListeners();

            for(RADComponent rad : selectedComponents) {
                JComponent c = (JComponent) formDesigner.getComponent(rad);
                if(c != null) {
                    if(!isMenuBarContainer(rad)) { // don't mess w/ the menubar's background
                        c.setBackground(getSelectedBackground(c));
                    }
                    makeSureShowingOnScreen(rad, c);
                    if (c instanceof JMenu) {
                        showMenuPopup((JMenu) c);
                    }
                    }
                }

            repaint();
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getComponentDefaultsPrefix(JComponent c) {
        if(c instanceof JMenuBar) {
            return "MenuBar"; // NOI18N
        }
        if(c instanceof JMenu) {
            return "Menu"; // NOI18N
        }
        if(c instanceof JCheckBoxMenuItem) {
            return "CheckBoxMenuItem"; // NOI18N
        }
        if(c instanceof JRadioButtonMenuItem) {
            return "RadioButtonMenuItem"; // NOI18N
        }
        return "MenuItem"; // NOI18N
    }
    
    private Color getNormalBackground(RADComponent metacomp, JComponent c) {
        RADProperty prop = metacomp.getBeanProperty("background"); // NOI18N
        Color color = null;
        if (prop != null) {
            try {
                Object value = prop.getTargetValue();
                if (value instanceof Color) {
                    color = (Color)value;
                }
            } catch (Exception ex) {}
        }
        if (color == null) {
            // fallback - for example subclass of menu component
            // that hides background property
            color = backgroundMap.get(c);
        }
        return color;
    }

    private Map<JComponent, Color> backgroundMap = new HashMap<JComponent,Color>();
    private Color getSelectedBackground(JComponent c) {
        //don't put into the map twice
        if(!backgroundMap.containsKey(c)) {
            backgroundMap.put(c,c.getBackground());
        }
        return SELECTED_MENU_BACKGROUND;
    }
    
    private Color getNormalForeground(JComponent c) {
        String prefix = getComponentDefaultsPrefix(c);
        Color color = UIManager.getDefaults().getColor(prefix+".foreground"); // NOI18N
        if(color == null) {
            color = Color.BLACK;
        }
        return color;
    }
    
    
    
    private void makeSureShowingOnScreen(RADComponent rad, JComponent comp) {
        if(!this.isVisible()) {
            this.setVisible(true);
            registerKeyListeners();
            if(rad instanceof RADVisualContainer) {
                keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)rad);
            } else {
                keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)rad.getParentComponent());
            }
        }
        
        List<RADComponent> path = new ArrayList<RADComponent>();
        RADComponent temp = rad.getParentComponent();
        while(true) {
            if(temp == null) break;
            path.add(temp);
            temp = temp.getParentComponent();
            if(!isMenuRelatedRADComponent(temp)) {
                break;
            }
        }
        
        // go backwards, top to bottom
        for(int i = path.size()-1; i>=0; i--) {
            RADComponent r = path.get(i);
            JComponent c = (JComponent) formDesigner.getComponent(r);
            if(c instanceof JMenu) {
                showMenuPopup((JMenu)c);
            }
        }
        
    }
    
    

    
    private void showContextMenu(Point popupPos) {
//        ComponentInspector inspector = ComponentInspector.getInstance();
//        Node[] selectedNodes = inspector.getSelectedNodes();
        Node[] selectedNodes = formDesigner.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if(!this.isVisible()) {
            this.setVisible(true);
        }
        if (popup != null) {
            popup.show(this, popupPos.x, popupPos.y);
        }
    }

    
    // returns true if this is a menu container that should be highlighted if the component
    // tcomp is dragged over it.
    public boolean canHighlightContainer(RADVisualContainer targetContainer, RADVisualComponent tcomp) {
        Class beanclass = tcomp.getBeanClass();
        if(targetContainer != null && targetContainer.isMenuComponent() && targetContainer.canAddComponent(beanclass)) {
            return true;
        }
        return false;
    }
    
    // is this rollover code still being used?
    // this turns on and off the rollover highlight as well as auto-opening the menu
    // if it is a menu
    private JComponent prevRollover = null;
    public void rolloverContainer(RADVisualContainer targetContainer) {
        if(targetContainer == null && prevRollover != null) {
            clearRollover();
        }
        if(targetContainer != null) {
            JComponent rollover = (JComponent) formDesigner.getComponent(targetContainer);
            if(rollover != prevRollover){
                clearRollover();
            }
            prevRollover = rollover;
            prevRollover.setBorder(new Border() {

                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(DropTargetLayer.DROP_TARGET_LINE_STROKE);
                    g2.setColor(DropTargetLayer.DROP_TARGET_COLOR);
                    g2.drawRect(x,y,width,height);
                }

                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(2,2,2,2);
                }

                @Override
                public boolean isBorderOpaque() {
                    return false;
                }
                //BorderFactory.createLineBorder(Color.ORANGE,2)
            });
            prevRollover.repaint();
            if(rollover instanceof JMenu) {
                formDesigner.openMenu(targetContainer);
                //openMenu(targetContainer,rollover);
            }
        }
    }
    
    public void clearRollover() {
        if(prevRollover==null) return;
        prevRollover.setBorder(BorderFactory.createEmptyBorder());
        prevRollover.repaint();
        prevRollover = null;
    }
    
    
    void addRadComponentToBefore(JComponent target, MetaComponentCreator creator) {
        addRadComponentTo(target, 0, creator);
    }
    
    void addRadComponentToAfter(JComponent target, MetaComponentCreator creator) {
        addRadComponentTo(target, 1, creator);
    }
    private void addRadComponentTo(JComponent target, int offset, MetaComponentCreator creator) {
        try {
            JComponent targetParent = getMenuParent(target);
            if(target.getParent() instanceof JMenuBar) {
                targetParent = (JComponent) target.getParent();
            }
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            
            assert targetParentRad != null;
            
            int index2 = targetParentRad.getIndexOf(targetRad) + offset;
            creator.addPrecreatedComponent(targetParentRad, new Integer(index2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    boolean addRadComponentToEnd(JComponent targetComponent, MetaComponentCreator creator) {
        RADVisualContainer targetContainer = (RADVisualContainer) formDesigner.getMetaComponent(targetComponent);
        Object constraints = null;
        boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
        return added;
    }
    
    void moveRadComponentInto(JComponent payload, JComponent targetMenu) {
        try {
            
            //check if dragging onto self
            if(payload == targetMenu) {
                return;
            }
            
            //check if dragging to a descendant node
            if(isAncestor(targetMenu, payload)) {
                return;
            }
            
            JComponent payloadParent = getMenuParent(payload);
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            
            // remove the component from it's old location
            // if no payload rad then that probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);
            }
            
            RADVisualContainer targetMenuRad = (RADVisualContainer) formDesigner.getMetaComponent(targetMenu);
            //add inside the target menu
            //add to end of the toplevel menu
            targetMenuRad.add(payloadRad, -1);
            targetMenuRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, null, -1);
            formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    void moveRadComponentToBefore(JComponent payload, JComponent target) {
        moveRadComponentTo(payload, target, 0);
    }
    void moveRadComponentToAfter(JComponent payload, JComponent target) {
        moveRadComponentTo(payload, target, 1);
    }
    private void moveRadComponentTo(JComponent payload, JComponent target, int offset) {
        try {
            if(payload == target) {
                return;
            }
            //check if dragging to a descendant node
            if(isAncestor(target, payload)) {
                return;
            }
            JComponent payloadParent = getMenuParent(payload);
            /*
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }*/
            
            JComponent targetParent = getMenuParent(target);
            
            if(targetParent == null) {
                targetParent = (JComponent) target.getParent();
            }
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);

            //if a toplevel menu dragged next to another toplevel menu
            if(payload instanceof JMenu && payload.getParent() instanceof JMenuBar 
                    && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                //remove from old spot
                targetParent = (JComponent) target.getParent();
                payloadParent = (JComponent) payload.getParent();
                payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
                targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            }
            
            //skip if no payload rad, which probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);
            }
            
            // only Menu component can be added into MenuBar, 
            // reset parent for the other components (issue #143248 fix)
            if (payloadRad != null 
                    && !javax.swing.JMenu.class.isAssignableFrom(payloadRad.getBeanClass()) 
                    && target instanceof JMenu && targetParent instanceof JMenuBar) {
                targetParent = null;
            }
                
            //if dragged component into a toplevel menu
            if(targetParent == null && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                targetParentRad = (RADVisualContainer) targetRad;
                //add to end of the toplevel menu
                targetParentRad.add(payloadRad, -1);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, null, -1);
                formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
                return;
            }
            
            // insert if target exists, else the item was removed by dragging out of the menu
            if(targetParentRad != null) {
                int index2 = targetParentRad.getIndexOf(targetRad) + offset;
                targetParentRad.add(payloadRad, index2);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, 
                        null, index2);
                formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static boolean addComponentToEndOfMenu(RADComponent targetContainer, PaletteItem paletteItem) {
        FormModel model = targetContainer.getFormModel();
        MetaComponentCreator creator = model.getComponentCreator();
        return creator.createComponent(paletteItem, targetContainer, null) != null;
    }
    
    
    // change the look of the component to reflect the newly added state.
    // this mainly means making the foreground color light gray.
    void configureNewComponent(RADComponent item) {
        if(item != null) {
            JComponent c = (JComponent) formDesigner.getComponent(item);
            if(c != null) {
                c.setForeground(Color.LIGHT_GRAY);
            }
        }
    }
    
    
    // change the look of the component to reflect the fully edited state
    private void configureEditedComponent(JComponent c) {
        if(c == null) return;
        if(USE_NEW_ITEM_COLOR_SWITCHING) {
            if(c.getForeground() == Color.LIGHT_GRAY) {
                c.setForeground(getNormalForeground(c));
            }
        }
    }
    
    void configureEditedComponent(RADComponent c) {
        if(c != null) {
            configureEditedComponent((JComponent)formDesigner.getComponent(c));
        }
    }

    //listens to see if this particular menu has been changed
    private void registerForm(final RADVisualContainer metacomp, final JMenu menu) {
        // don't double register
        if(!formModelListeners.containsKey(metacomp)) {
            FormModelListener fml = new FormModelListener() {
                @Override
                public void formChanged(FormModelEvent[] events) {
                    if (events != null) {
                        for(FormModelEvent evt : events) {
                            if(evt.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED) {
                                formModelListeners.remove(metacomp);
                                metacomp.getFormModel().addFormModelListener(this);
                                continue;
                            }
                            
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_PROPERTY_CHANGED) {
                                if("action".equals(evt.getPropertyName())) { // NOI18N
                                    configureEditedComponent(evt.getComponent());
                                }
                            }
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_PROPERTY_CHANGED || evt.getChangeType() == FormModelEvent.BINDING_PROPERTY_CHANGED) {
                                if(evt.getContainer() == metacomp || evt.getComponent() == metacomp) {
                                    rebuildOnScreenMenu(metacomp);
                                }
                                updateIcon(evt.getComponent());
                            }
                            
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_ADDED) {
                                updateIcon(evt.getComponent());
                                //reinstall the accelerator preview when moving items around
                                if(evt.getComponent() != null) {
                                    Component co = (Component) formDesigner.getComponent(evt.getComponent());
                                    if(co instanceof JMenuItem) {
                                        installAcceleratorPreview((JMenuItem)co);
                                    }
                                }
                            }
                            
                            // if this menu was deleted then make sure it's popup is hidden and removed
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_REMOVED) {
                                if(evt.getComponent() == metacomp) {
                                    unconfigureMenu(menu);
                                    continue;
                                }
                            }
                            // if something added to the menu we monitor
                            if(evt.getChangeType() == FormModelEvent.COMPONENT_ADDED ||
                                    evt.getChangeType() == FormModelEvent.COMPONENTS_REORDERED ||
                                    evt.getChangeType() == FormModelEvent.COMPONENT_REMOVED) {
                                if(evt.getContainer() == metacomp) {
                                    // then rebuild the menu
                                    rebuildOnScreenMenu(metacomp);
                                    return;
                                }
                                if(evt.getContainer() == getFormMenuBar()) {
                                    JComponent comp = (JComponent) formDesigner.getComponent(getFormMenuBar());
                                    if (comp != null) { // MenuBar not shown in the designer, see issue 124873
                                        RADVisualContainer rad = (RADVisualContainer) getFormMenuBar();
                                        comp.removeAll();
                                        for(RADVisualComponent c : rad.getSubComponents()) {
                                            if(c!=null) {
                                                comp.add((JComponent)formDesigner.getComponent(c));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };
            formModelListeners.put(metacomp,fml);
            metacomp.getFormModel().addFormModelListener(fml);
        }
    }
    
    private void rebuildOnScreenMenu(RADVisualContainer menuRAD) {
        if(menuRAD == null) return;
        if(hackedPopupFactory == null) return;
        JMenu menu = (JMenu) formDesigner.getComponent(menuRAD);
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel popupContainer = hackedPopupFactory.containerMap.get(menu);
            if(popupContainer == null) return;
            for(Component c : popupContainer.getComponents()) {
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent)c);
                }
            }
            popupContainer.removeAll();
            // rebuild it
            for(RADVisualComponent child : menuRAD.getSubComponents()) {
                if(child != null) {
                    JComponent jchild = (JComponent) formDesigner.getComponent(child);
                    if(!isConfigured(jchild)) {
                        if(jchild instanceof JMenu) {
                            configureMenu(menu, (JMenu)jchild);
                        } else {
                            configureMenuItem(menu,jchild);
                        }
                    }
                    popupContainer.add(jchild);
                }
            }
            
            // repack it
            popupContainer.setSize(popupContainer.getLayout().preferredLayoutSize(popupContainer));
            validate();
            popupContainer.repaint();
        }
    }
    
    private void updateIcon(RADComponent rad) {
        try {
            Component comp = (Component) formDesigner.getComponent(rad);
            if(comp instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) comp;
                RADProperty icon_prop = rad.getBeanProperty("icon");
                Object value = icon_prop.getValue();
                // extract the new value
                Icon icon = null;
                if(value instanceof Icon) {
                    icon = (Icon) value;
                }
                if(value instanceof NbImageIcon) {
                    icon = ((NbImageIcon)value).getIcon();
                }
                if(value instanceof ResourceValue) {
                    ResourceValue rv = (ResourceValue) value;
                    Object designValue = rv.getDesignValue();
                    if(designValue instanceof Icon) {
                        icon = (Icon) designValue;
                    }
                    if(designValue instanceof NbImageIcon) {
                        icon = ((NbImageIcon)designValue).getIcon();
                    }
                }
                // do the actual update
                if(!(item.getIcon() instanceof WrapperIcon) && !isTopLevelMenu(item)) {
                    item.setIcon(new WrapperIcon(item.getIcon()));
                }
                
                if(item.getIcon() instanceof WrapperIcon) {
                    ((WrapperIcon)item.getIcon()).setIcon(icon);
                } else { // we should never get here
                    item.setIcon(icon);
                }
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        
    }
    
    //returns true if this array contains a menu component
    public static boolean containsMenuTypeComponent(RADVisualComponent[] comps) {
        if(comps == null) return false;
        if(comps.length < 1) return false;
        for(RADVisualComponent c : comps) {
            if(JMenuItem.class.isAssignableFrom(c.getBeanClass())) return true;
            if(JMenuBar.class.isAssignableFrom(c.getBeanClass())) return true;
            if(JMenu.class.isAssignableFrom(c.getBeanClass())) return true;
        }
        return false;
    }
    
    public static boolean containsMenuBar(RADVisualComponent[] comps) {
        if(comps == null) return false;
        if(comps.length < 1) return false;
        for(RADVisualComponent c : comps) {
            if(JMenuBar.class.isAssignableFrom(c.getBeanClass())) return true;
        }
        return false;
    }
    
    // returns true if this container is a menubar or menu, else false
    public static boolean isValidMenuContainer(RADVisualContainer cont) {
        if(cont == null) return false;
        if(JMenuBar.class.isAssignableFrom(cont.getBeanClass())) return true;
        if(JMenu.class.isAssignableFrom(cont.getBeanClass())) return true;
        return false;
    }
    
    public static boolean isTopLevelMenu(JComponent comp) {
        if(comp == null) return false;
        if(comp instanceof JMenu) {
            if(comp.getParent() instanceof JMenuBar) return true;
        }
        return false;
    }
    
    public boolean doesFormContainMenuBar() {
        for(RADComponent comp : formDesigner.getFormModel().getAllComponents()) {
            if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
                return true;
            }
        }
        return false;
    }
    
    public RADComponent getFormMenuBar() {
        for(RADComponent comp : formDesigner.getFormModel().getAllComponents()) {
            if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
                return comp;
            }
        }
        return null;
    }


    private class GlassLayerMouseListener extends MouseInputAdapter {
        Point pressPoint = null;
        JComponent pressComp = null;
        private boolean isEditing = false;
        // #116961: Point of last left click (getting point on mouseRelease)
        private Point prevLeftMousePoint;
        
        @Override
        public void mousePressed(MouseEvent e) {
            //if this is a valid menu drop
            if(dragop.isStarted() && dragop.getTargetComponent() != null &&
                    isMenuRelatedComponentClass(dragop.getTargetComponent().getClass())) {
                if(e.isShiftDown()) {
                    dragop.end(e, false);
                    PaletteItem item = PaletteUtils.getSelectedItem();
                    dragop.start(item, e.getPoint());
                } else {
                    dragop.end(e, true);
                }
                return;
            }
            if(shouldRedispatchToHandle()) {
                dragop.fastEnd();
                formDesigner.getHandleLayer().dispatchEvent(e);
                return;
            }
            // drag drag ops
            if(dragop.isStarted()) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // #133628: user wants to cancel the drop so deselect menu-related component in the palette
                    dragop.fastEnd();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    dragop.end(e);
                }
                return;
            }

            // open top level menus when clicking them
            RADComponent rad = formDesigner.getHandleLayer().getMetaComponentAt(e.getPoint(), HandleLayer.COMP_DEEPEST);
            if(rad != null) {
                Object o = formDesigner.getComponent(rad);
                JComponent c = (o instanceof JComponent) ? (JComponent)o : null;
                if(c != null && isTopLevelMenu(c) && rad instanceof RADVisualComponent) {
                    if(e.getClickCount() > 1) {
                        isEditing = true;
                        configureEditedComponent(c);
                        formDesigner.startInPlaceEditing(rad);            
                    } else {
                        openMenu(rad, c);
                        glassLayer.requestFocusInWindow();                        
                        if(DropTargetLayer.isMultiselectPressed(e)) {
                            if(e.isShiftDown()) {// add component to selection
                                addSelectedRADComponent(rad);
                            } else if (e.isControlDown()) {// #119217: toggle component's selection status
                                toggleSelectedRADComponent(rad);
                            }
                        } else {
                            setSelectedRADComponent(rad);
                        }
                        if(e.isPopupTrigger()) {
                            showContextMenu(e.getPoint());
                            return;
                        }
                        // #116961: check if inplace editing should start due to second click on a menu
                        boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();
                        if (prevLeftMousePoint != null
                                && prevLeftMousePoint.distance(e.getPoint()) <= 3
                                && !modifier) {   // second click on the same place in a component
                            isEditing = true;
                            configureEditedComponent(c);
                            formDesigner.startInPlaceEditing(rad);
                        }
                        if(!dragop.isStarted()) {
                            pressPoint = e.getPoint();
                            pressComp = c;
                            return;
                        }
                    }
                    return;
                }
                if(c instanceof JMenuBar) {
                     setSelectedRADComponent(rad);
                     if(e.isPopupTrigger()) {
                         showContextMenu(e.getPoint());
                         return;
                     }
                     return;
                }
            }

            JComponent c = dragop.getDeepestComponentInPopups(e.getPoint());

            
            if(c == null && !isMenuRelatedRADComponent(rad)) {
                PaletteUtils.clearPaletteSelection();
                hideMenuLayer();
                formDesigner.getHandleLayer().mousePressed(e);
                return;
            }

            // start editing
            if(e.getClickCount() > 1) {
                if(c instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) c;
                    Point pt = SwingUtilities.convertPoint(glassLayer, e.getPoint(), item);
                    SelectedPortion portion = DropTargetLayer.calculateSelectedPortion(item, pt);
                    RADComponent radcomp = formDesigner.getMetaComponent(item);
                    configureEditedComponent(c);
                    if(portion == SelectedPortion.Icon) {
                        showIconEditor(radcomp);
                    } else if (portion == SelectedPortion.Accelerator) {
                        showAcceleratorEditor(radcomp);
                    } else {
                        // #116961: check if inplace editing should start or an action listener should be assigned
                        Node node = radcomp.getNodeReference();
                        if (node != null) {
                            Action action = node.getPreferredAction();
                            if (action != null) {// action listener should be assigned (JMenuItem was double-clicked)
                                action.actionPerformed(new ActionEvent(
                                        node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                                prevLeftMousePoint = null; // to prevent inplace editing on mouse release
                            } else {// inplace editing should start (JMenu was double-clicked)
                                isEditing = true;
                                formDesigner.startInPlaceEditing(radcomp);
                            }
                        }
                    }
                }
            } else {
                if (c instanceof JMenu) {// #115152: make all parts of JMenuItem (icon, text, accelerator) appear, when JMenuItem is moved under JMenu
                    openMenu(rad, c);
                    glassLayer.requestFocusInWindow();
                }
                if (c instanceof JMenuItem) {// #116961: check if inplace editing should start due to second click on a menu
                    JMenuItem item = (JMenuItem) c;
                    boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();
                    if (prevLeftMousePoint != null
                            && prevLeftMousePoint.distance(e.getPoint()) <= 3
                            && !modifier) {   // second click on the same place in a component
                        RADComponent metacomp = formDesigner.getMetaComponent(item);
                        if (metacomp != null) {
                            isEditing = true;
                            formDesigner.startInPlaceEditing(metacomp);
                        }
                    }
                }
            }
            
            // show context menu
            if(e.isPopupTrigger()) {
                showContextMenu(e.getPoint());
                return;
            }

            //prep for drag motion for menuitem to menuitem drags
            if(!dragop.isStarted() && c instanceof JMenuItem) {
                pressPoint = e.getPoint();
                pressComp = c;
                return;
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                prevLeftMousePoint = e.getPoint();
            }
            if(e.isPopupTrigger()) {
                showContextMenu(e.getPoint());
                return;
            }
            
            if(dragop.isStarted() && !e.isShiftDown()) {
                dragop.end(e);
            } else {
                if(!isEditing) {
                    JComponent c = dragop.getDeepestComponentInPopups(e.getPoint());
                    if(c != null) { 
                        if(c instanceof JMenuItem) {
                            Point localPt = SwingUtilities.convertPoint(glassLayer, e.getPoint(), c);
                            selectedPortion = DropTargetLayer.calculateSelectedPortion((JMenuItem)c, localPt);
                            dropTargetLayer.repaint();
                        } else {
                            selectedPortion = SelectedPortion.None;
                        }
                        glassLayer.requestFocusInWindow();
                        RADComponent rad = formDesigner.getMetaComponent(c);
                        //add to selection if shift is down, instead of replacing
                        if(DropTargetLayer.isMultiselectPressed(e)) {
                            if(e.isShiftDown()) {// add component to selection
                                addSelectedRADComponent(rad);
                            } else if (e.isControlDown()) {// #119217: toggle component's selection status
                                toggleSelectedRADComponent(rad);
                            }
                        } else {
                            setSelectedRADComponent(rad);
                        }
                    }
                }
                isEditing = false;
            } 
        }
        
        private void showIconEditor(RADComponent comp) {
            try {
                RADProperty prop = comp.getBeanProperty("icon"); // NOI18N
                new PropertyAction(prop).actionPerformed(null);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        private void showAcceleratorEditor(RADComponent comp) {
            try {
                RADProperty prop = comp.getBeanProperty("accelerator"); // NOI18N
                new PropertyAction(prop).actionPerformed(null);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            if(showMenubarWarning) {
                showMenubarWarning = false;
                repaint();
            }
            if(dragop.isStarted()) {
                if(PaletteUtils.getSelectedItem() == null && dragop.isPickAndPlop()) {
                    dragop.fastEnd();
                } else {
                    dragop.setTargetVisible(true);
                }
            }
            if(!dragop.isStarted() || PaletteUtils.getSelectedItem() != dragop.getCurrentItem()) {
                PaletteItem item = PaletteUtils.getSelectedItem();
                
                // if not menu related at all, then jump back to handle layer
                if(item != null && !isMenuRelatedComponentClass(item.getComponentClass())) {
                    hideMenuLayer();
                    return;
                }
                
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_ADD && item != null) {
                    if(JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                        hideMenuLayer();
                        return;
                    }
                    dragop.start(item,e.getPoint());
                }
                
                /*
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT && showMenubarWarning) {
                    //glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    showMenubarWarning = false;
                    repaint();
                }*/
            }
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            if(dragop.isStarted()) {
                 dragop.setTargetVisible(false);
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if(!dragop.isStarted() && pressPoint != null && pressComp instanceof JMenuItem
                    && e.getPoint().distance(pressPoint) > 10) {
                dragop.start((JMenuItem)pressComp, e.getPoint());
                pressPoint = null;
                pressComp = null;
            }
            if(dragop.isStarted()) {
                dragop.move(e.getPoint());
            }
            return;
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            if(shouldRedispatchToHandle()) {
                formDesigner.getHandleLayer().dispatchEvent(e);
                //hideMenuLayer();
                //return;
            }
            if(dragop.isStarted()) {
                if(!doesFormContainMenuBar()) {
                    FormEditor.getAssistantModel(formDesigner.getFormModel()).setContext("missingMenubar"); // NOI18N
                }
                dragop.move(e.getPoint());
            }                
            
        }
        
        private boolean shouldRedispatchToHandle() {
            if(!USE_JSEPARATOR_FIX) return false;
            if(dragop.isStarted() && dragop.isPickAndPlop()) {
                if(dragop.getDragComponent() instanceof JSeparator /*&&
                        dropTargetLayer.getDropTargetComponent() == null*/) {
                    return true;
                }
            }
            return false;
        }
                
    }
    
    private boolean shouldRedispatchDnDToHandle(DropTargetDragEvent dtde) {
        RADComponent rad = formDesigner.getHandleLayer().getMetaComponentAt(dtde.getLocation(), HandleLayer.COMP_DEEPEST);
        if(rad != null && isMenuRelatedComponentClass(rad.getBeanClass())) {
            return false;
        }
        if(!USE_JSEPARATOR_FIX) return false;
        PaletteItem item = PaletteUtils.getSelectedItem();
        if(item != null && JSeparator.class.isAssignableFrom(item.getComponentClass())) {
            return true;
        }
        return false;
    }
    
    private class GlassLayerDropTargetListener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if(shouldRedispatchDnDToHandle(dtde)) {
                dragProxying = true;
                formDesigner.getHandleLayer().getNewComponentDropListener().dragEnter(dtde);
                return;
            }
            if(!dragop.isStarted()) {
                start(dtde);
            }
        }
        
        private void start(DropTargetDragEvent dtde) {
            PaletteItem item = PaletteUtils.getSelectedItem();

            if(item != null && !isMenuRelatedComponentClass(item.getComponentClass())) {
                hideMenuLayer();
                return;
            }

            if(formDesigner.getDesignerMode() == FormDesigner.MODE_ADD && item != null) {
                if(JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                    hideMenuLayer();
                    return;
                }
                dragop.start(item,dtde.getLocation());
            }
        }
        
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // look at the rad component under the cursor first
            if(dragProxying && shouldRedispatchDnDToHandle(dtde)) {
                formDesigner.getHandleLayer().getNewComponentDropListener().dragOver(dtde);
                return;
            }
            dragProxying = false;
            if(dragop.isStarted()) {
                dragop.move(dtde.getLocation());
            } else {
                start(dtde);
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            //if(shouldRedispatchDnDToHandle()) {
            if(dragProxying) {
                formDesigner.getHandleLayer().getNewComponentDropListener().dragExit(dte);
            }
            dragProxying = false;
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            //if(shouldRedispatchDnDToHandle()) {
            if(dragProxying) {
                formDesigner.getHandleLayer().getNewComponentDropListener().drop(dtde);
                dragProxying = false;
                return;
            }
            if(dragop.isStarted()) {
                dragop.end(dtde);
                dragProxying = false;
                return;
            }
        }
        
    }
    
    
    public SelectedPortion getCurrentSelectedPortion() {
        return selectedPortion;
    }
     
    
    private boolean dragProxying = false;
    public boolean isDragProxying() {
        return dragProxying;
    }
    
    
    static class WrapperIcon implements Icon {
        private Icon wrapee;
        public WrapperIcon() {
            this(null);
        }
        public WrapperIcon(Icon icon) {
            wrapee = icon;
        }
        
        public void setIcon(Icon icon) {
            this.wrapee = icon;
        }
        
        @Override
        public void paintIcon(Component arg0, Graphics g, int x,  int y) {
            if(wrapee != null) {
                wrapee.paintIcon(arg0, g, x, y);
            } else {
                Graphics g2 = g.create();
                g2.setColor(Color.WHITE);
                g2.fillRect(x,y,getIconWidth()-1, getIconHeight()-1);
                g2.setColor(MenuEditLayer.EMPTY_ICON_COLOR);
                g2.drawRect(x,y,getIconWidth()-1, getIconHeight()-1);
                g2.drawRect(x+1,y+1,getIconWidth()-3, getIconHeight()-3);
                g2.dispose();
            }
        }
        
        @Override
        public int getIconWidth() {
            if(wrapee != null) {
                return wrapee.getIconWidth();
            }
            return 16;
        }
        
        @Override
        public int getIconHeight() {
            if(wrapee != null) {
                return wrapee.getIconHeight();
            }
            return 16;
        }
        
    }

}
