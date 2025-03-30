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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.util.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;

/** A customizer providing better editing facility for GridBagLayout
 *
 * @author   Petr Hrebejk
 */
public final class GridBagCustomizer extends JPanel implements Customizer
{
    /** bundle to obtain text information from */
//    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(GridBagCustomizer.class);

    // -----------------------------------------------------------------------------
    // private area

    PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    static final int TOP = 1;
    static final int BOTTOM = 2;
    static final int LEFT = 4;
    static final int RIGHT = 8;

    static final int HORIZONTAL = LEFT + RIGHT;
    static final int VERTICAL = TOP + BOTTOM;

    static final int PLUS = 1;
    static final int MINUS = -1;

    private static final Icon REMAINDER_ICON = ImageUtilities.loadIcon(
        "org/netbeans/modules/form/layoutsupport/resources/remainder.gif"); // NOI18N

//    private DesignGridBagLayout designLayout;
    private GridBagLayoutSupport layoutSupport;

    private FormModel formModel;
    private FormModelListener formListener;
    private RADVisualContainer radContainer;
    private RADVisualComponent[] radComponents;
    private GBComponentProxy[] gbcProxies;

    // Customizer components
    private JSplitPane splitPane;
    private JPanel designPanel;

    private GridBagControlCenter controlCenter;
    private GBContainerProxy containerProxy;
    private PropertySheet propertySheet;
    private javax.swing.JScrollPane designScrollPane;
    private JLayeredPane designLayeredPane;

    private GBComponentProxy.DragLabel dragLabel = null;

    /** This is a hack. We need to now whether the GLC is painted first time
     * in order to paint empty cols and rows correctlly. Field is setted in
     * setObject() and Ppaint() methods.
     */
    private boolean firstPaint = false;

    static final long serialVersionUID =-632768048562391785L;

    public GridBagCustomizer() {
        initComponents();
    }

    private void initialize() {

//        initComponents();

        radContainer = ((LayoutSupportManager)
                            layoutSupport.getLayoutSupportHack())
                        .getMetaContainer(); // ugly hack
        formModel = radContainer.getFormModel();
        radComponents = radContainer.getSubComponents();

        gbcProxies = new GBComponentProxy[radComponents.length];
        for (int i = 0; i < radComponents.length; i++) {
            gbcProxies[i] = new GBComponentProxy(radComponents[i], containerProxy);
        }

        containerProxy.removeAll();
        
        FormDesigner designer = FormEditor.getFormDesigner(formModel);
        if (!designer.isInDesigner(radContainer)) {
            designer.setTopDesignComponent(radContainer, true);
            // terrible hack - wait for designer update
            invokeLater(2, new Runnable() {
                @Override
                public void run() {
                    containerProxy.addAllProxies();
                }
            });
        }
        else containerProxy.addAllProxies();

        formListener = new FormListener();
        formModel.addFormModelListener(formListener);
    }

    void customizerClosed() {
        formModel.removeFormModelListener(formListener);
    }

    /** inits the components of the customizer */

    private void initComponents() {

        setBorder(new javax.swing.border.EmptyBorder(4, 0, 8, 0));
        setLayout(new BorderLayout()); // [PENDING]

        propertySheet = new PropertySheet();
        try {
            propertySheet.setSortingMode(PropertySheet.UNSORTED);
        }
        catch (java.beans.PropertyVetoException e) {
//            ErrorManager.getDefault().notifyException(e);
        }
        propertySheet.setPreferredSize(new Dimension(300, 380));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(propertySheet, BorderLayout.CENTER);
        controlCenter = new GridBagControlCenter(this);
        panel.add(controlCenter, BorderLayout.SOUTH);

        designScrollPane = new javax.swing.JScrollPane();
        designPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                //System.out.println(" THE pref size of DesignPanel " + containerProxy.getPreferredSize()); // NOI18N
                return  containerProxy.getPreferredSize();
            }
        };

        designPanel.setLayout(new GridBagLayout());
        Color bgColor = UIManager.getColor(Utilities.isMac() ? "Desktop.background" : "desktop"); // NOI18N
        designPanel.setBackground(bgColor);
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.NONE;
        containerProxy = new GBContainerProxy();

        designPanel.add(containerProxy, con);

        designLayeredPane = new JLayeredPane() {
            @Override
            public Dimension getPreferredSize() {

                Dimension dpd = designPanel.getPreferredSize();
                Dimension spd = designScrollPane.getViewport().getExtentSize();

                int width = Math.max(dpd.width + 40, spd.width);
                int height = Math.max(dpd.height + 40 , spd.height);

                /*
                  Dimension dpd = designPanel.getPreferredSize();
                  Dimension spd = designScrollPane.getViewport().getExtentSize();

                  return  new Dimension(Math.max(dpd.width, spd.width), Math.max(dpd.height, spd.height));
                */
                //System.out.println(" THE GET " + new Dimension(Math.max(400, width), Math.max(300, height))); // NOI18N

                return  new Dimension(Math.max(500, width), Math.max(300, height));
            }
        };

        //designLayeredPane.setLayout(new BorderLayout());

        //designLayeredPane.setLayer(designPanel, JLayeredPane.DEFAULT_LAYER.intValue());
        designLayeredPane.add(designPanel, JLayeredPane.DEFAULT_LAYER);
        designPanel.setBounds(20, 20, designPanel.getPreferredSize().width, designPanel.getPreferredSize().height);
        designLayeredPane.setOpaque(true);
        designLayeredPane.setBackground(bgColor);

        designScrollPane.setViewportView(designLayeredPane);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(panel);
        splitPane.setRightComponent(designScrollPane);
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI());
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);

        getAccessibleContext().setAccessibleDescription(GridBagLayoutSupport.getBundleHack().getString("ACSD_GridBagCustomizer"));
        HelpCtx.setHelpIDString(this, "gui.layouts.gbcustomizer"); // NOI18N
    }

    void setAnchor(int anchor) {
        java.util.List<GBComponentProxy> selected = containerProxy.getSelectedProxies();
        Iterator<GBComponentProxy> it = selected.iterator();
        while (it.hasNext())
//            setProperty((GBComponentProxy)it.next(), DesignGridBagLayout.PROP_ANCHOR, new Integer(anchor));
            setProperty(it.next(), "anchor", new Integer(anchor)); // NOI18N
    }

    void setFill(int fill) {
        java.util.List<GBComponentProxy> selected = containerProxy.getSelectedProxies();
        Iterator<GBComponentProxy> it = selected.iterator();
        while (it.hasNext())
//            setProperty((GBComponentProxy)it.next(), DesignGridBagLayout.PROP_FILL, new Integer(fill));
            setProperty(it.next(), "fill", new Integer(fill)); // NOI18N
    };



    void modifyIPad(int action, int what) {
        java.util.List<GBComponentProxy> selected = containerProxy.getSelectedProxies();
        Iterator<GBComponentProxy> it = selected.iterator();
        while (it.hasNext()) {
            GBComponentProxy p = it.next();
            int value =  what == HORIZONTAL ? p.getRealConstraints().ipadx : p.getRealConstraints().ipady;
            value += action;
            if (value < 0)
                continue;
            setProperty(p,
//                        what == HORIZONTAL ? DesignGridBagLayout.PROP_IPADX : DesignGridBagLayout.PROP_IPADY ,
                        what == HORIZONTAL ? "ipadx" : "ipady" , // NOI18N
                        new Integer(value));
        }
    }

    void modifyInsets(int action, int what) {
        java.util.List<GBComponentProxy> selected = containerProxy.getSelectedProxies();
        Iterator<GBComponentProxy> it = selected.iterator();
        while (it.hasNext()) {
            GBComponentProxy p = it.next();
            Insets old_insets = p.getRealConstraints().insets;
            Insets new_insets =(Insets)old_insets.clone();

            if ((what & TOP) != 0) {
                new_insets.top += action;
                if (new_insets.top < 0)
                    new_insets.top = 0;
            }
            if ((what & BOTTOM) != 0) {
                new_insets.bottom += action;
                if (new_insets.bottom < 0)
                    new_insets.bottom = 0;
            }
            if ((what & LEFT) != 0) {
                new_insets.left += action;
                if (new_insets.left < 0)
                    new_insets.left = 0;
            }
            if ((what & RIGHT) != 0) {
                new_insets.right += action;
                if (new_insets.right < 0)
                    new_insets.right = 0;
            }

//            setProperty(p, DesignGridBagLayout.PROP_INSETS,  new_insets);
            setProperty(p, "insets",  new_insets); // NOI18N
        }
    }


    void modifyGridSize(int action, int what) {
        java.util.List<GBComponentProxy> selected = containerProxy.getSelectedProxies();
        Iterator<GBComponentProxy> it = selected.iterator();

        while (it.hasNext()) {
            GBComponentProxy p = it.next();
            int value =  what == HORIZONTAL ? p.getRealConstraints().gridwidth : p.getRealConstraints().gridheight;

            if (action == 0)
                value = value == 0 ? 1 : 0;
            else {
                value += action;
                if (value < 1) {
                    value = 1;
                }
            }

            setProperty(p,
//                        what == HORIZONTAL ? DesignGridBagLayout.PROP_GRIDWIDTH : DesignGridBagLayout.PROP_GRIDHEIGHT ,
                        what == HORIZONTAL ? "gridwidth" : "gridheight" , // NOI18N
                        new Integer(value));
        }
    }

    private void setProperty(GBComponentProxy p, String name, Object value) {
        Node.Property prop = p.getComponent().getPropertyByName(
                                        "GridBagLayoutConstraints "+name); // NOI18N
        if (prop != null) {
            try {
                prop.setValue(value);
            }
            catch (Exception ex) { // ignore
                ex.printStackTrace();
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Customizer implementation

    /**
     * Set the object to be customized.  This method should be called only
     * once, before the Customizer has been added to any parent AWT container.
     * @param bean  The object to be customized.
     */
    @Override
    public void setObject(Object bean) {
        layoutSupport = (GridBagLayoutSupport) bean;
//                        ((LayoutSupportManager)bean).getLayoutDelegate();
        initialize();
        firstPaint = true;
    }

    /**
     * Register a listener for the PropertyChange event.  The customizer
     * should fire a PropertyChange event whenever it changes the target
     * bean in a way that might require the displayed properties to be
     * refreshed.
     *
     * @param listener  An object to be invoked when a PropertyChange
     *		event is fired.
     */
//    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        propertySupport.addPropertyChangeListener(listener);
//    }

    /**
     * Remove a listener for the PropertyChange event.
     *
     * @param listener  The PropertyChange listener to be removed.
     */
//    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        propertySupport.removePropertyChangeListener(listener);
//    }
    /*
      public void propertyChange(final java.beans.PropertyChangeEvent p0) {
      System.out.println("PCH :" + p0);
      }
    */

    // -----------------------------------------------------------------------------
    // Form listener implementation

    class FormListener implements FormModelListener {
        @Override
        public void formChanged(FormModelEvent[] events) {
            if (events != null && GridBagCustomizer.this.isShowing()) {
                boolean modifying = false;
                for (int i=0; i < events.length; i++)
                    if (events[i].isModifying()) {
                        modifying = true;
                        break;
                    }
                if (!modifying)
                    return;

                // we perform update after designer is updated which takes
                // three dispatch events - this is only temporary patch
                // (very very very ugly :-)
                // (it would be much nicer to have some listener on
                //  FormDesigner directly...)
                invokeLater(3, new Runnable() {
                    @Override
                    public void run() {
                        containerProxy.updateAllProxies();
                    }
                });
            }
//            if (propertyName == DesignGridBagLayout.PROP_ANCHOR || propertyName == DesignGridBagLayout.PROP_FILL ||
//              propertyName == DesignGridBagLayout.PROP_GRIDWIDTH || propertyName == DesignGridBagLayout.PROP_GRIDHEIGHT)
//              controlCenter.newSelection(containerProxy.getSelectedProxies());
        }
    }

    void waitForDesignerUpdate(int count) {
        try {
            while (count > 0) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                count--;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void invokeLater(final int count, final Runnable task) {
        if (count == 0)
            task.run();
        else
            SwingUtilities.invokeLater(new Runnable() {
            @Override
                public void run() {
                    invokeLater(count-1, task);
                }
            });
    }

//      public void layoutChanged(RADVisualContainer container,RADVisualComponent component,
//                                String propertyName,Object oldValue,Object newValue) {
//          //System.out.println("Layout changed" + propertyName + newValue); // NOI18N
//          containerProxy.updateAllProxies();
//          if (propertyName == DesignGridBagLayout.PROP_ANCHOR || propertyName == DesignGridBagLayout.PROP_FILL ||
//              propertyName == DesignGridBagLayout.PROP_GRIDWIDTH || propertyName == DesignGridBagLayout.PROP_GRIDHEIGHT)
//              controlCenter.newSelection(containerProxy.getSelectedProxies());
//      }

//      public void eventAdded(FormEventEvent evt) {}
//      public void eventRemoved(FormEventEvent evt) {}
//      public void eventRenamed(FormEventEvent evt) {}


    void innerLayoutChanged() {

        /*
          System.out.println("1 CP Size       : " + containerProxy.getSize());
          System.out.println("1 CP Preff Size : " + containerProxy.getSize());

          containerProxy.invalidate();
          containerProxy.revalidate();
          containerProxy.widenEmpty();
          containerProxy.revalidate();

          designPanel.invalidate();
          designPanel.validate();
          designPanel.revalidate();
        */
        //designLayeredPane.invalidate();


        containerProxy.widenEmpty();
        designPanel.setBounds(20, 20, designPanel.getPreferredSize().width, designPanel.getPreferredSize().height);

        //containerProxy.widenEmpty();
        //containerProxy.invalidate();
        containerProxy.revalidate();

        //designPanel.invalidate();
        //designPanel.validate();
        //designPanel.revalidate();

        designLayeredPane.revalidate();
        //designLayeredPane.repaint();

        /*
          System.out.println("2 CP Size        : " + containerProxy.getSize());
          System.out.println("2 CP Preff Size  : " + containerProxy.getPreferredSize());
          System.out.println("2 DP Size        : " + designPanel.getSize());
          System.out.println("2 DP Preff Size  : " + designPanel.getPreferredSize());
          System.out.println("2 DLP Size       : " + designLayeredPane.getSize());
          System.out.println("2 DLP Preff Size : " + designLayeredPane.getPreferredSize());
        */

    }

    // -----------------------------------------------------------------------------
    // Innerclasses


    /** Proxy component for one component in the container */
    class GBComponentProxy  extends JPanel  {
        /*
          private String name;

          GBComponent(String name) {
          this.name = name;
          }
        */

        private GBContainerProxy parentProxy;
        private RADVisualComponent component;
        private ComponentProxyNode node;
        private javax.swing.border.CompoundBorder compoundBorder;
        private javax.swing.border.MatteBorder insetsBorder;
        private javax.swing.border.MatteBorder remainderBorder;
        // private JPanel innerPanel;
        private JLabel componentLabel;
        private GridBagLayout layout;
//        private GridBagConstraints componentConstraints;
        private Color INSETS_COLOR = new Color(255, 255, 204);
        private Color CELL_COLOR = new Color(153, 153, 255);

        private boolean isSelected = false;

        static final long serialVersionUID =-6552012922564179923L;

        GBComponentProxy(final RADVisualComponent component, GBContainerProxy parentProxy) {

            this.component = component;
            this.parentProxy = parentProxy;

            //setLayout(new BorderLayout());

            componentLabel = new javax.swing.JLabel()
            {
                @Override
                public void paint(Graphics g) {
                    int borderSize = 5;
                    Color borderColor = Color.blue;

                    super.paint(g);

                    if (GBComponentProxy.this.isSelected()) {

                        Dimension size = getSize();
                        g.setColor(borderColor);
                        g.fillRect(0, 0, borderSize, borderSize); // UpLeft
                        g.fillRect(size.width-borderSize, 0, borderSize, borderSize); // UpRight
                        g.fillRect(0, size.height-borderSize, borderSize, borderSize); // LoLeft
                        g.fillRect(size.width-borderSize, size.height-borderSize, borderSize, borderSize); // LoRight


                        /*
                        //if (resizable) {
                        //g.fillRect(midHor, 0, borderSize, borderSize); // UpMid
                        //g.fillRect(0, midVer, borderSize, borderSize); // LeftMid
                        g.fillRect(size.width-borderSize, midVer, borderSize, borderSize); // RightMid
                        g.fillRect(midHor, size.height-borderSize, borderSize, borderSize); // LoMid
                        //}
                        */
                    }

                }

                @Override
                public Dimension getPreferredSize() {
                    FormDesigner designer = FormEditor.getFormDesigner(formModel);
                    Component comp = (Component)designer.getComponent(component);
                    Dimension size;
                    if (comp == null) {
                        comp = (Component) component.getBeanInstance();
                        size = comp.getPreferredSize();
                    }
                    else {
                        if (comp.isShowing()) {
                            size = comp.getSize();
                            if (size.width > 4096) // [hack for issue 32311]
                                size.width = comp.getPreferredSize().width;
                        }
                        else size = comp.getPreferredSize();
                    }

                    // Use a new instance - avoid modification of
                    // the preferred size of the component (issue 48033)
                    size = new Dimension(size);
                    
                    if (comp instanceof JComponent && !(comp instanceof JPanel)) {
                        Insets thisIns = getInsets();
                        if (comp instanceof JComponent) {
                            javax.swing.border.Border b = ((JComponent)comp).getBorder();
                            if (b != null) {
                                Insets ins = b.getBorderInsets(comp);
                                thisIns.top -= ins.top;
                                thisIns.left -= ins.left;
                                thisIns.bottom -= ins.bottom;
                                thisIns.right -= ins.right;
                            }
                        }
                        if (thisIns.top > 0) size.height += thisIns.top;
                        if (thisIns.bottom > 0) size.height += thisIns.bottom;
                        if (thisIns.left > 0) size.width += thisIns.left;
                        if (thisIns.right > 0) size.width += thisIns.right;
                    }

                    if (size.width < 6) size.width = 6;
                    if (size.height < 6) size.height = 6;

                    return size;
                }

                @Override
                public Dimension getMinimumSize() {
                    Component comp = (Component)
                        FormEditor.getFormDesigner(formModel).getComponent(component);
                    if (comp == null)
                        comp = (Component) component.getBeanInstance();
                    return comp.getMinimumSize();
//                    return component.getComponent().getMinimumSize();
                }

                @Override
                public Dimension getMaximumSize() {
                    Component comp = (Component)
                        FormEditor.getFormDesigner(formModel).getComponent(component);
                    if (comp == null)
                        comp = (Component) component.getBeanInstance();
                    return comp.getMaximumSize();
//                    return component.getComponent().getMaximumSize();
                }
            };
            componentLabel.setOpaque(true);
            componentLabel.setBorder(new javax.swing.border.EtchedBorder());
            componentLabel.setText(component.getName());
            componentLabel.setHorizontalAlignment(0);
            componentLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    GBComponentProxy.this.mouseClicked(evt);
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    GBComponentProxy.this.mousePressed(evt);
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    GBComponentProxy.this.mouseReleased(evt);
                }

            });

            componentLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseDragged(java.awt.event.MouseEvent evt) {
                    GBComponentProxy.this.mouseDragged(evt);
                }
            }
                                                  );


            /*
              innerPanel = new JPanel();
              innerPanel.setLayout(layout = new GridBagLayout());
              innerPanel.setBackground(CELL_COLOR); 
              innerPanel.setOpaque(true);
                    
              innerPanel.add(componentLabel, getInnerComponentConstraints());      
              add(innerPanel);
            */


            setLayout(layout = new GridBagLayout());
            setBackground(CELL_COLOR);
            setOpaque(true);
            updateByComponent();
            add(componentLabel, getInnerComponentConstraints());
            node = new ComponentProxyNode(component.getNodeReference());
        }

        RADVisualComponent getComponent() {
            return component;
        }

//        DesignGridBagLayout.GridBagConstraintsDescription getConstraints() {
//            return(DesignGridBagLayout.GridBagConstraintsDescription) component.getConstraints(DesignGridBagLayout.class);
//        }

        GridBagConstraints getRealConstraints() {
            return (GridBagConstraints)
                   component.getParentContainer().getLayoutSupport()
                       .getConstraints(component).getConstraintsObject();
//            return getConstraints().getGridBagConstraints();
        }

        ComponentProxyNode getNode() {
            return node;
        }

        void updateByComponent() {
            layout.setConstraints(componentLabel, getInnerComponentConstraints());
        }


        /*
          public void paint(Graphics g) {
          int borderSize = 5;
          Color borderColor = Color.blue;
         
          super.paint(g);
          
          if (isSelected()) {
          Dimension size = getSize();
          int midHor =(size.width - borderSize) / 2;
          int midVer =(size.height - borderSize) / 2;
          g.setColor(borderColor);
          g.fillRect(0, 0, borderSize, borderSize); // UpLeft
          g.fillRect(size.width-borderSize, 0, borderSize, borderSize); // UpRight
          g.fillRect(0, size.height-borderSize, borderSize, borderSize); // LoLeft
          g.fillRect(size.width-borderSize, size.height-borderSize, borderSize, borderSize); // LoRight

            
          //if (resizable) {
          g.fillRect(midHor, 0, borderSize, borderSize); // UpMid
          g.fillRect(0, midVer, borderSize, borderSize); // LeftMid
          g.fillRect(size.width-borderSize, midVer, borderSize, borderSize); // RightMid
          g.fillRect(midHor, size.height-borderSize, borderSize, borderSize); // LoMid
          //}
          }
          }
        */

        GridBagConstraints getInnerComponentConstraints() {

            GridBagConstraints con = new GridBagConstraints();
            con.anchor = getRealConstraints().anchor;
            con.fill = getRealConstraints().fill;
            con.gridheight = 1;
            con.gridwidth = 1;
            con.gridx = 0;
            con.gridy = 0;
            // con.insets
            con.ipadx = getRealConstraints().ipadx;
            con.ipady = getRealConstraints().ipady;
            con.weightx = 1.0;
            con.weighty = 1.0;

            return con;
        }


        GridBagConstraints getProxyConstraints() {
            GridBagConstraints con = new GridBagConstraints();

            con.anchor = GridBagConstraints.CENTER;
            con.fill = GridBagConstraints.BOTH;
            con.gridheight = getRealConstraints().gridheight;
            con.gridwidth = getRealConstraints().gridwidth;
            con.gridx = getRealConstraints().gridx;
            con.gridy = getRealConstraints().gridy;
            con.insets = new Insets(3, 3, 3, 3);
            con.ipadx = 0;
            con.ipady = 0;
            //con.weightx = getRealConstraints().weightx;
            //con.weighty = getRealConstraints().weighty;
            con.weightx = 1.0;
            con.weighty = 1.0;
            Insets in  = getRealConstraints().insets;

            insetsBorder = new javax.swing.border.MatteBorder(in.top, in.left, in.bottom, in.right, INSETS_COLOR);
            remainderBorder =  new javax.swing.border.MatteBorder(0, 0, con.gridheight == 0 ? 4 : 0, con.gridwidth == 0 ? 4 : 0, REMAINDER_ICON);
            compoundBorder = new javax.swing.border.CompoundBorder(remainderBorder, insetsBorder);
            setBorder(compoundBorder);

            return con;
        }

        boolean isSelected() {
            return isSelected;
        }

        void setSelected(boolean isSelected) {
            if (this.isSelected == isSelected)
                return;

            this.isSelected = isSelected;
            //innerPanel.invalidate();
            componentLabel.repaint();
            /*
              componentLabel.setBackground(isSelected ?
              (java.awt.Color) javax.swing.UIManager.getDefaults().get("controlLtHighlight") :
              (java.awt.Color) javax.swing.UIManager.getDefaults().get("Button.background"));
              componentLabel.repaint();
            */
        }

        void mouseClicked(java.awt.event.MouseEvent evt) {
            /*
              if (evt.isShiftDown())
              parentProxy.shiftSelect(this);
              else
              parentProxy.select(this);
            */
        }

        void mousePressed(java.awt.event.MouseEvent evt) {
            if (evt.isControlDown())
                parentProxy.shiftSelect(this);
            else
                parentProxy.select(this);
        }

        void mouseReleased(java.awt.event.MouseEvent evt) {
            if (dragLabel != null) {

                if (!dragLabel.getLastIndex().equals(dragLabel.getOriginalIndex())) {
//                    setProperty(this, DesignGridBagLayout.PROP_GRIDX, new Integer(dragLabel.getLastIndex().x));
//                    setProperty(this, DesignGridBagLayout.PROP_GRIDY, new Integer(dragLabel.getLastIndex().y));
                    setProperty(this, "gridx", new Integer(dragLabel.getLastIndex().x)); // NOI18N
                    setProperty(this, "gridy", new Integer(dragLabel.getLastIndex().y)); // NOI18N
                }

                designLayeredPane.remove(dragLabel);
                dragLabel = null;
                componentLabel.setCursor(Cursor.getDefaultCursor());
                designLayeredPane.repaint();
            }
        }

        void mouseDragged(java.awt.event.MouseEvent evt) {

            //System.out.print("Dragged "); // NOI18N


            if (dragLabel == null) {
                if (isSelected()) {

                    //System.out.println(" - first time"); // NOI18N


                    dragLabel = new DragLabel();
                    dragLabel.setHotSpot(evt.getPoint());
                    dragLabel.resolveOrigin();
                    dragLabel.setLastIndex(dragLabel.getIndex(evt.getPoint()));
                    dragLabel.setOriginalIndex(dragLabel.getIndex(evt.getPoint()));

                    designLayeredPane.setLayer(dragLabel, JLayeredPane.DRAG_LAYER.intValue());
                    designLayeredPane.add(dragLabel, BorderLayout.CENTER);
                    dragLabel.setBounds(evt.getPoint().x, evt.getPoint().y);
                    componentLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                else {
                    //System.out.println(" - Undefined"); // NOI18N

                }
            }
            else
            {

                Point newLoc;
                if (!dragLabel.getLastIndex().equals(newLoc = dragLabel.getIndex(evt.getPoint()))) {

                    Point converted = SwingUtilities.convertPoint(dragLabel, evt.getPoint(), designLayeredPane);

                    //System.out.println(" - with layout change"); // NOI18N


                    GridBagConstraints  con = getProxyConstraints();

                    con.gridx = newLoc.x;
                    con.gridy = newLoc.y;

                    parentProxy.layout.setConstraints(this, con);

                    /*
                      invalidate();
                      doLayout();
                    */
                    //System.out.println("Constraints set"); // NOI18N


                    // parentProxy.widenEmpty();

                    //System.out.println(" Widden "); // NOI18N


                    //invalidate();
                    //innerPanel.invalidate();
                    //parentProxy.invalidateAllProxies();

                    //parentProxy.invalidate();
                    //parentProxy.revalidate();

                    //parentProxy.widenEmpty();

                    //innerLayoutChanged();



                    dragLabel.resolveOrigin();

                    SwingUtilities.convertPoint(designLayeredPane, converted, dragLabel);
                    //dragLabel.setBounds(converted.x, converted.y);
                    Rectangle r = dragLabel.getBounds();
                    Point loc = r.getLocation();
                    loc.x += r.width + 20;
                    loc.y += r.height + 20;
                    //designScrollPane.getViewport().setViewPosition(r.getLocation());

                    innerLayoutChanged();
                    dragLabel.repaint();

                    //System.out.println(" layout changed"); // NOI18N


                    dragLabel.setLastIndex(newLoc);

                }
                else
                    //System.out.println(" - simple"); // NOI18N

                    dragLabel.setBounds(evt.getPoint().x, evt.getPoint().y);
            }
        }

        /** Innerclass for the component which is dragged */
        class DragLabel extends JLabel {


            Point origin;
            Point hotSpot;
            Point lastIndex;
            Point originalIndex;

            private Dimension preferredSize;

            static final long serialVersionUID =992490305277357953L;
            DragLabel() {
                setOpaque(false);
                setEnabled(false);
                setBorder(new javax.swing.border.EtchedBorder());
                setText(component.getName());
                setHorizontalAlignment(0);

                preferredSize =(Dimension)componentLabel.getPreferredSize().clone();
            }

            @Override
            public Dimension getPreferredSize() {
                preferredSize.height = componentLabel.getPreferredSize().height + getRealConstraints().ipady;
                preferredSize.width = componentLabel.getPreferredSize().width + getRealConstraints().ipadx;

                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return componentLabel.getMinimumSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return componentLabel.getMaximumSize();
            }

            void setHotSpot(Point hotSpot) {
                this.hotSpot = hotSpot;
            }

            void resolveOrigin() {
                origin = SwingUtilities.convertPoint(componentLabel, 0, 0, designLayeredPane);
            }

            Point getLastIndex() {
                return lastIndex;
            }

            void setLastIndex(Point lastIndex) {
                this.lastIndex = lastIndex;
            }

            Point getOriginalIndex() {
                return originalIndex;
            }

            void setOriginalIndex(Point originalIndex) {
                this.originalIndex = originalIndex;
            }

            Point getIndex(Point p) {
                return parentProxy.getLayoutLocation(SwingUtilities.convertPoint(componentLabel, p, parentProxy));
            }

            public void setBounds(int x, int y) {
                resolveOrigin();
                super.setBounds(origin.x + x - hotSpot.x , origin.y + y - hotSpot.y, getPreferredSize().width, getPreferredSize().height);
            }

        }

    }

    /** Properties of this node are displayed in the layout property sheet */
    static class ComponentProxyNode extends FilterNode {
        
        ComponentProxyNode(Node original) {
            super(original);
        }
        
        @Override
        public Node.PropertySet[] getPropertySets() {
            Node.PropertySet[] sets = super.getPropertySets();
            for (int i=0; i < sets.length; i++)
                if ("layout".equals(sets[i].getName())) // NOI18N
                    return new Node.PropertySet[] { sets[i] };
                    return new Node.PropertySet[0]; // cannot return null...
        }
    }
    

    /** Proxy for the container it's layout is edited */
    class GBContainerProxy extends JPanel {

        private GridBagLayout layout;
        private boolean[][] empties;


        static final long serialVersionUID =5113122235848232590L;

        GBContainerProxy() {
            setLayout(layout = new GridBagLayout());
            setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
            setOpaque(true);
        }

        void addAllProxies() {
            for (int i = 0; i < gbcProxies.length; i++) {
                add(gbcProxies[i], gbcProxies[i].getProxyConstraints());
            }
            invalidate();
            validate();
            innerLayoutChanged();
            widenEmpty();

            if (gbcProxies.length > 0)
                select(gbcProxies[0]);
        }

        void widenEmpty() {

            layout.rowHeights = layout.columnWidths = null;

            layout.layoutContainer(this);
            validate();

            int[][] dims = layout.getLayoutDimensions();

            empties = new boolean[2][];

            int[] widths = new int[ dims[0].length ];
            empties[0] = new boolean[ dims[0].length ];

            for (int i  = 0; i < widths.length; i++) {
                // System.out.println("Col [" + i + "] = " + dims[0][i]); // NOI18N
                widths[i] = 25;
                empties[0][i] = dims[0][i] == 0 ? true : false;
            }
            layout.columnWidths = widths;


            int[] heights = new int[ dims[1].length ];
            empties[1] = new boolean[ dims[1].length ];
            for (int i  = 0; i < heights.length; i++) {
                // System.out.println("Rpw [" + i + "] = " + dims[1][i]); // NOI18N
                heights[i] = 25;
                empties[1][i] = dims[1][i] == 0 ? true : false;
            }
            layout.rowHeights = heights;

            //layout.layoutContainer(this);
        }

        @Override
        public void paint(Graphics g) {

            if (firstPaint) {
                innerLayoutChanged();
                firstPaint = false;
            }

            super.paint(g);

            if (gbcProxies.length > 0) {

                Color emptyColor = new Color(255, 173, 173);

                Point origin = layout.getLayoutOrigin();

                int[][] dims = layout.getLayoutDimensions();

                int width = 0;
                for (int i = 0; i < dims[0].length; i ++) {
                    width += dims[0][i];
                    //System.out.println("W " + i + " : " + dims[0][i]);
                }

                int height = 0;
                for (int i = 0; i < dims[1].length; i ++) {
                    height += dims[1][i];
                    // System.out.println("H " + i + " : " + dims[1][i]);
                }

                //Paint empty rows
                int yCoord = origin.y;
                g.setColor(emptyColor);
                for (int i = 0; i < dims[1].length; i ++) {
                    if (empties[1][i]) {
                        g.setColor(emptyColor);
                        g.fillRect(origin.x, yCoord, width, dims[1][i]);
                    }
                    yCoord += dims[1][i];
                }

                //Paint empty columns
                int xCoord = origin.x;
                for (int i = 0; i < dims[0].length; i ++) {
                    if (empties[0][i]) {
                        g.setColor(emptyColor);
                        g.fillRect(xCoord, origin.y, dims[0][i], height);
                    }
                    xCoord += dims[0][i];
                }


                // Paint horizontal lines
                g.setColor(Color.black);
                yCoord = dims[1][0] + origin.y - 1;
                for (int i = 1; i < dims[1].length; i ++) {
                    g.drawLine(origin.x, yCoord, origin.x + width - 1, yCoord);
                    g.drawLine(origin.x, yCoord + 1, origin.x + width - 1, yCoord + 1);
                    yCoord += dims[1][i];
                }

                // Paint vertical lines
                xCoord = dims[0][0] + origin.x - 1;
                for (int i = 1; i < dims[0].length; i ++) {
                    g.drawLine(xCoord, origin.y, xCoord, origin.y + height - 1);
                    g.drawLine(xCoord + 1, origin.y, xCoord + 1, origin.y + height - 1);
                    xCoord += dims[0][i];
                }
            }

            paintChildren(g);
        }

        Point getLayoutLocation(Point p) {
            return layout.location(p.x, p.y);
        }

        /* Updates all proxies */
        void updateAllProxies() {

            for (int i = 0; i < gbcProxies.length; i++) {
                updateProxy(gbcProxies[i]);
            }

            //invalidate();

            innerLayoutChanged();
            controlCenter.newSelection(getSelectedProxies());

            //designPanel.revalidate();
            //designPanel.repaint();
            //designLayeredPane.revalidate();
            //designPanel.repaint();
        }

        /* Updates the selected proxy */

        void updateProxy(GBComponentProxy p) {
            p.updateByComponent();

            p.getProxyConstraints();
            layout.setConstraints(p, p.getProxyConstraints());

            p.invalidate();
            //p.innerPanel.invalidate();
            p.validate();

        }

        void invalidateAllProxies() {
            for (int i = 0; i < gbcProxies.length; i++) {
                gbcProxies[i].invalidate();
            }
            doLayout();
        }


        java.util.List<GBComponentProxy> getSelectedProxies() {
            java.util.List<GBComponentProxy> selected = new ArrayList<GBComponentProxy>(gbcProxies.length);

            for (int i = 0; i < gbcProxies.length; i++) {
                if (gbcProxies[i].isSelected)
                    selected.add(gbcProxies[i]);
            }

            return selected;
        }

        Node[] getSelectedNodes() {
            java.util.List<GBComponentProxy> selected = getSelectedProxies();

            Node[] result =  new Node[ selected.size() ];

            Iterator<GBComponentProxy> it = selected.iterator();

            for (int i = 0; it.hasNext(); i++) {
                result[i] = it.next().getNode();
            }

            return result;
        }


        void select(GBComponentProxy p) {
            select(p, false);
        }

        void shiftSelect(GBComponentProxy p) {
            select(p, true);
        }

        void select(GBComponentProxy p, boolean shift) {

            java.util.List<GBComponentProxy> selected = getSelectedProxies();

            if (p.isSelected()) {
                if (selected.size() == 1) {
                    return;
                }
                else if (shift) {
                    p.setSelected(false);
                }
                else {
                    Iterator<GBComponentProxy> it = selected.iterator();
                    while (it.hasNext()) {
                        it.next().setSelected(false);
                    }
                    p.setSelected(true);
                }

            }
            else {
                if (!shift) {
                    Iterator<GBComponentProxy> it = selected.iterator();
                    while (it.hasNext())
                        it.next().setSelected(false);
                }
                p.setSelected(true);
            }

            propertySheet.setNodes(getSelectedNodes());
            controlCenter.newSelection(getSelectedProxies());
        }

    }


    public static class Window extends JDialog implements Customizer, ActionListener {
        private final GridBagCustomizer customizerPanel;
        private boolean packCalled;
        public Window() {
            super(org.openide.windows.WindowManager.getDefault().getMainWindow());
            java.util.ResourceBundle bundle = GridBagLayoutSupport.getBundleHack();

            setTitle(bundle.getString("CTL_CustomizerTitle")); // NOI18N

            getContentPane().setLayout(new BorderLayout(0, 0));
            customizerPanel = new GridBagCustomizer();
            getContentPane().add(customizerPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 6));

            JButton closeButton = new JButton();
            closeButton.setText(bundle.getString("CTL_CloseButton")); // NOI18N
            closeButton.setActionCommand("close"); // NOI18N
            closeButton.addActionListener(this);
            buttonPanel.add(closeButton);

            JButton helpButton = new JButton();
            helpButton.setText(bundle.getString("CTL_HelpButton")); // NOi18N
            helpButton.setActionCommand("help"); // NOI18N
            helpButton.addActionListener(this);
            buttonPanel.add(helpButton);

            getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    customizerPanel.customizerClosed();
                }
            });

            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }

        @Override
        public void pack() {
            if (packCalled)
                return;
            packCalled = true;

            super.pack();
            Dimension size = getSize();
            Rectangle screenBounds = Utilities.getUsableScreenBounds();
            if (size.width > screenBounds.width - 80)
                size.width = screenBounds.width * 4 / 5;
            if (size.height > screenBounds.height - 80)
                size.height = screenBounds.height * 4 / 5;
            setBounds(Utilities.findCenterBounds(size));
        }

        @Override
        public void setObject(Object bean) {
            customizerPanel.setObject(bean);
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            if (ev.getActionCommand().equals("close")) { // NOI18N
                dispose();
            } else if (ev.getActionCommand().equals("help")) { // NOI18N
                KeyEvent event = new KeyEvent(this, KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(), 0, KeyEvent.VK_F1, (char)KeyEvent.VK_F1);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
            }
        }
    }
    

}
