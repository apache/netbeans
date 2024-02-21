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

package org.netbeans.modules.form;

import java.awt.EventQueue;
import java.util.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.MenuElement;
import org.netbeans.modules.form.editors.EnumEditor;

import org.openide.nodes.*;

import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.layoutsupport.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Ian Formanek
 */

public class RADVisualComponent extends RADComponent {
    private static final String PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE = "layoutComponentHorizontalSize"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_VERTICAL_SIZE = "layoutComponentVerticalSize"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE = "layoutComponentHorizontalResizable"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE = "layoutComponentVerticalResizable"; // NOI18N
    static final String PROP_LAYER = "JLayeredPane.layer"; // NOI18N

    // -----------------------------------------------------------------------------
    // Private properties

    // [??]
    private Map<String,LayoutConstraints> constraints = new HashMap<String,LayoutConstraints>();
//    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private ConstraintsListenerConvertor constraintsListener;

    enum MenuType { JMenuItem, JCheckBoxMenuItem, JRadioButtonMenuItem,
                    JMenu, JMenuBar, JPopupMenu, JSeparator }

    // -----------------------------------------------------------------------------
    // Initialization

//    public void setParentComponent(RADComponent parentComp) {
//        super.setParentComponent(parentComp);
//        if (parentComp != null)
//            getConstraintsProperties();
//    }

//    void initParent(RADVisualContainer parent) {
//        this.parent = parent;
//    }

/*    protected void setBeanInstance(Object beanInstance) {
        if (beanInstance instanceof java.awt.Component) {
            boolean attached = FakePeerSupport.attachFakePeer(
                                            (java.awt.Component)beanInstance);
            if (attached && beanInstance instanceof java.awt.Container)
                FakePeerSupport.attachFakePeerRecursively(
                                            (java.awt.Container)beanInstance);
        }

        super.setBeanInstance(beanInstance);
    } */

    // -----------------------------------------------------------------------------
    // Public interface

    /** @return The JavaBean visual component represented by this RADVisualComponent */
//    public java.awt.Component getComponent() { // [is it needed ???]
//        return (java.awt.Component) getBeanInstance();
//    }

    public final RADVisualContainer getParentContainer() {
        return (RADVisualContainer) getParentComponent();
    }

    /** @return The index of this component within visual components of its parent */
    public final int getComponentIndex() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getIndexOf(this) : -1;
//        return ((ComponentContainer)getParentComponent()).getIndexOf(this);
    }

    final LayoutSupportManager getParentLayoutSupport() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getLayoutSupport() : null;
    }

    boolean isMenuTypeComponent() {
        return MenuElement.class.isAssignableFrom(getBeanClass());
    }

    /**
     * Returns whether this component is treated specially as a menu component.
     * Not only it must be of particular Swing menu class, but must also be used
     * as a menu, not as normal visual component. Technically it must be either
     * contained in another menu, or be a menu bar of a window.
     * @return whether the component is a menu used in another menu or as menu
     *         bar in a window
     */
    public boolean isMenuComponent() {
        if (isMenuTypeComponent()) {
            RADVisualContainer parent = getParentContainer();
            if ((parent == null && !isInModel())
                || (parent != null
                    && (parent.isMenuTypeComponent() || this == parent.getContainerMenu()))) {
                return true;
            }
        }
        return false;
    }

    static MenuType getMenuType(Class cl) {
        if (MenuElement.class.isAssignableFrom(cl)) {
            if (JMenu.class.isAssignableFrom(cl)) {
                return MenuType.JMenu;
            }
            if (JMenuBar.class.isAssignableFrom(cl)) {
                return MenuType.JMenuBar;
            }
            if (JCheckBoxMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JCheckBoxMenuItem;
            }
            if (JRadioButtonMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JRadioButtonMenuItem;
            }
            if (JMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JMenuItem;
            }
            if (JPopupMenu.class.isAssignableFrom(cl)) {
                return MenuType.JPopupMenu;
            }
        } else if (JSeparator.class.isAssignableFrom(cl)) {
            return MenuType.JSeparator;
        }
        return null;
    }

    // -----------------------------------------------------------------------------
    // Layout constraints management

    /**
     * Sets component's constraints description for given layout-support class. 
     * 
     * @param layoutDelegateClass class of the layout delegate these constraints belong to.
     * @param constr layout constraints.
     */
    public void setLayoutConstraints(Class layoutDelegateClass,
                                     LayoutConstraints constr)
    {
        if (constr != null) {
            constraints.put(layoutDelegateClass.getName(), constr);
        }
    }

    /**
     * Gets component's constraints description for given layout-support class.
     * 
     * @param layoutDelegateClass class of the layout delegate.
     * @return layout constraints for the given layout delegate.
     */
    public LayoutConstraints getLayoutConstraints(Class layoutDelegateClass) {
        return constraints.get(layoutDelegateClass.getName());
    }

    Map<String,LayoutConstraints> getConstraintsMap() {
        return constraints;
    }

    void setConstraintsMap(Map<String,LayoutConstraints> map) {
        constraints.putAll(map);
    }

    // ---------------
    // Properties

    @Override
    protected synchronized void createPropertySets(List<Node.PropertySet> propSets) {
        super.createPropertySets(propSets);
        if (SUPPRESS_PROPERTY_TABS) {
            return;
        }

        if (constraintsProperties == null)
            createConstraintsProperties();

        if (constraintsProperties != null && constraintsProperties.length > 0)
            propSets.add(propSets.size() - 1,
                         new Node.PropertySet("layout", // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTab"), // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTabHint")) // NOI18N
            {
                @Override
                public Node.Property[] getProperties() {
                    Node.Property[] props = getConstraintsProperties();
                    return (props == null) ? NO_PROPERTIES : props;
                }
            });

    }

    /** Called to modify original properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs
     * of subclasses. Here used for adding ButtonGroupProperty.
     */
/*    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {

        super.changePropertiesExplicitly(prefProps, normalProps, expertProps);

        if (getBeanInstance() instanceof java.awt.TextComponent) {
            // hack for AWT text components - "text" property should be first
            for (int i=0, n=normalProps.size(); i < n; i++) {
                RADProperty prop = (RADProperty) normalProps.get(i);
                if ("text".equals(prop.getName())) { // NOI18N
                    normalProps.remove(i);
                    normalProps.add(0, prop);
                    break;
                }
            }
        }

        // hack for buttons - add a fake property for ButtonGroup
//        if (getBeanInstance() instanceof javax.swing.AbstractButton)
//            try {
//                Node.Property prop = new ButtonGroupProperty(this);
//                nameToProperty.put(prop.getName(), prop);
//                if (getBeanInstance() instanceof javax.swing.JToggleButton)
//                    prefProps.add(prop);
//                else
//                    normalProps.add(prop);
//            }
//            catch (IntrospectionException ex) {} // should not happen

//        if (getBeanInstance() instanceof javax.swing.JLabel)
//            try {
//                PropertyDescriptor pd = new PropertyDescriptor("displayedMnemonic",
//                    javax.swing.JLabel.class, "getDisplayedMnemonic", "setDisplayedMnemonic");
//                normalProps.add(createProperty(pd));
//            }
//            catch (IntrospectionException ex) {} // should not happen
    } */

    @Override
    protected synchronized void clearProperties() {
        super.clearProperties();
        constraintsProperties = null;
    }

    // ---------
    // constraints properties

    public synchronized Node.Property[] getConstraintsProperties() {
        if (constraintsProperties == null)
            createConstraintsProperties();
        return constraintsProperties;
    }

    public synchronized void resetConstraintsProperties() {
        if (constraintsProperties != null) {
            for (int i=0; i < constraintsProperties.length; i++)
                nameToProperty.remove(constraintsProperties[i].getName());

            constraintsProperties = null;
            propertySets = null;

            RADComponentNode node = getNodeReference();
            if (node != null)
                node.fireComponentPropertySetsChange();
        }
    }

    private synchronized void createConstraintsProperties() {
        constraintsProperties = null;

        LayoutSupportManager layoutSupport = getParentLayoutSupport();
        if (layoutSupport != null) {
            // Issue 154824 - do not create layout properties for menu-bar
            if (getParentContainer().isLayoutSubcomponent(this)) {
                LayoutConstraints constr = layoutSupport.getConstraints(this);
                if (constr != null) {
                    constraintsProperties = constr.getProperties();
                }
            }
        } else if (getParentContainer() != null && !isMenuComponent()) {
            constraintsProperties = new Node.Property[] {
                new LayoutComponentSizeProperty(LayoutConstants.HORIZONTAL),
                new LayoutComponentSizeProperty(LayoutConstants.VERTICAL),
                new LayoutComponentResizableProperty(LayoutConstants.HORIZONTAL),
                new LayoutComponentResizableProperty(LayoutConstants.VERTICAL)
            };
        }

        if (isInLayeredPane()) {
            List<Node.Property> l;
            if (constraintsProperties != null) {
                l = new ArrayList(constraintsProperties.length+1);
                l.addAll(Arrays.asList(constraintsProperties));
            } else {
                l = new ArrayList(1);
            }

            Node.Property layerProperty = new LayerProperty();
            l.add(layerProperty);
            nameToProperty.put(layerProperty.getName(), layerProperty);

            constraintsProperties = l.toArray(new Node.Property[0]);
        }

        if (constraintsProperties == null) {
            constraintsProperties = NO_PROPERTIES;
            return;
        }

        for (int i=0; i < constraintsProperties.length; i++) {
            if (constraintsProperties[i] instanceof FormProperty) {
                FormProperty prop = (FormProperty)constraintsProperties[i];

                // we suppose the constraint property is not a RADProperty...
                prop.addVetoableChangeListener(getConstraintsListener());
                prop.addPropertyChangeListener(getConstraintsListener());
                prop.addValueConvertor(getConstraintsListener());

                prop.setPropertyContext(new FormPropertyContext.Component(this));

                if (isReadOnly() || !isValid()) {
                    int type = prop.getAccessType() | FormProperty.NO_WRITE;
                    prop.setAccessType(type);
                }
                nameToProperty.put(prop.getName(), prop);
            }
        }
    }

    private ConstraintsListenerConvertor getConstraintsListener() {
        if (constraintsListener == null)
            constraintsListener = new ConstraintsListenerConvertor();
        return constraintsListener;
    }

    private class ConstraintsListenerConvertor implements VetoableChangeListener,
                             PropertyChangeListener, FormProperty.ValueConvertor
    {
        @Override
        public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException
        {
            Object source = ev.getSource();
            String eventName = ev.getPropertyName();
            if (source instanceof FormProperty
                && (FormProperty.PROP_VALUE.equals(eventName)
                    || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)))
            {
                resourcePropertyChanged(ev);

                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                layoutSupport.componentLayoutChanged(index, ev);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints, null, null, null);

                try {
                    layoutSupport.componentLayoutChanged(index, ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }

        @Override
        public Object convert(Object value, FormProperty property) {
            return resourcePropertyConvert(value, property);
        }
    }

    final int getComponentLayer() {
        Object layer = getAuxValue(PROP_LAYER);
        return layer instanceof Integer ? ((Integer)layer).intValue() : JLayeredPane.DEFAULT_LAYER;
    }

    final String getComponentLayerJavaInitCode() {
        if (isInLayeredPane()) {
            int layer = getComponentLayer();
            if (layer != JLayeredPane.DEFAULT_LAYER || RADVisualContainer.isInFreeDesign(this)) {
                // In free design even JLayeredPane.DEFAULT_LAYER makes sense to be
                // generated - defines the order of component which is otherwise random.
                PropertyEditor prEd = new LayerPropertyEditor();
                prEd.setValue(layer);
                return prEd.getJavaInitializationString();
            }
        }
        return null;
    }

    final boolean isInLayeredPane() {
        RADVisualContainer parent = getParentContainer();
        return parent != null && parent.getBeanInstance() instanceof JLayeredPane;
    }

    private abstract class LProperty extends PropertySupport.ReadWrite {
        LProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
            setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }

        abstract Object getDefaultValue();

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        @Override
        public void restoreDefaultValue() {
            try {
                setValue(getDefaultValue());
            } catch (IllegalAccessException ex) { // subclasses don't throw anything
            } catch (InvocationTargetException ex) {
            }
        }

        @Override
        public boolean isDefaultValue() {
            Object value = null;
            try {
                value = getValue();
            } catch (IllegalAccessException ex) { // subclasses don't throw anything
            } catch (InvocationTargetException ex) {
            }
            return value == null || value.equals(getDefaultValue());
        }

        @Override
        public String getHtmlDisplayName() {
            return isDefaultValue() ? null : "<b>" + getDisplayName(); // NOI18N
        }

        @Override
        public boolean canWrite() {
            return !isReadOnly();
        }
    }

    /**
     * Preferred size of the component in the layout.
     */
    private class LayoutComponentSizeProperty extends LProperty {
        private LayoutComponent component;
        private int dimension;
        
        private LayoutComponentSizeProperty(int dimension) {
            super(dimension == LayoutConstants.HORIZONTAL ? PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE
                : PROP_LAYOUT_COMPONENT_VERTICAL_SIZE, Integer.class, null, null);
            boolean horizontal = dimension == LayoutConstants.HORIZONTAL;
            setDisplayName(FormUtils.getBundleString(horizontal ?
                "PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE" : "PROP_LAYOUT_COMPONENT_VERTICAL_SIZE")); // NOI18N
            setShortDescription(FormUtils.getBundleString(horizontal ?
                "HINT_LAYOUT_COMPONENT_HORIZONTAL_SIZE" : "HINT_LAYOUT_COMPONENT_VERTICAL_SIZE")); // NOI18N
            this.dimension = dimension;
        }

        private LayoutComponent getComponent() {
            if (component == null) {
                component = getFormModel().getLayoutModel().getLayoutComponent(getId());
                if (component != null) {                    
                    String sourcePropertyName = (dimension == LayoutConstants.HORIZONTAL) ?
                        LayoutConstants.PROP_HORIZONTAL_PREF_SIZE : LayoutConstants.PROP_VERTICAL_PREF_SIZE;
                    component.addPropertyChangeListener(new PropertySynchronizer(sourcePropertyName, getName()));
                }
            }
            return component;
        }
            
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            LayoutModel layoutModel = getFormModel().getLayoutModel();
            LayoutInterval interval = getComponent().getLayoutInterval(dimension);
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.setUserIntervalSize(interval, dimension, newValue.intValue());
                getNodeReference().firePropertyChangeHelper(
                    getName(), oldValue, newValue);
                autoUndo = false;
            } finally {
                getFormModel().fireContainerLayoutChanged(getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    getFormModel().addUndoableEdit(ue);
                }
                if (autoUndo) {
                    getFormModel().forceUndoOfCompoundEdit();
                }
            }
        }
        
        @Override
        public Object getValue() {
            int size = getComponent().getLayoutInterval(dimension).getPreferredSize();
            return Integer.valueOf(size);
        }

        @Override
        Object getDefaultValue() {
            return Integer.valueOf(LayoutConstants.NOT_EXPLICITLY_DEFINED);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new LayoutSizePropertyEditor();
        }
    }

    private static class LayoutSizePropertyEditor extends EnumEditor {
        LayoutSizePropertyEditor() {
            super(new Object[] {
                FormUtils.getBundleString("VALUE_SizeNotExplicitelyDefined"), // NOI18N
                LayoutConstants.NOT_EXPLICITLY_DEFINED,
                "" // java code string not needed here // NOI18N
            }, false, true);
        }

        @Override
        public void setAsText(String str) {
            if (!setValueFromString(str)) { // something else than Default
                try {
                    int size = Integer.parseInt(str);
                    if (size < 0) {
                        throw new IllegalArgumentException();
                    }
                    setValue(size);
                }  catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private class PropertySynchronizer implements PropertyChangeListener, Runnable {
        private String sourcePropertyName;
        private String targetPropertyName;

        PropertySynchronizer(String sourcePropertyName, String targetPropertyName) {
            this.sourcePropertyName = sourcePropertyName;
            this.targetPropertyName = targetPropertyName;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (sourcePropertyName.equals(propName)) {
                if (!FormLAF.inLAFBlock()) {
                    run();
                } else {
                    EventQueue.invokeLater(this);
                }
            }
        }

        @Override
        public void run() {
            RADComponentNode node = getNodeReference();
            if (node != null) {
                node.firePropertyChangeHelper(targetPropertyName, null, null);
            }
        }
    }

    /**
     * Property that determines whether the component should be resizable.
     */
    private class LayoutComponentResizableProperty extends LProperty {
        private LayoutComponent component;
        private int dimension;
        
        private LayoutComponentResizableProperty(int dimension) {
            super(dimension == LayoutConstants.HORIZONTAL ? PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE
                : PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE, Boolean.class, null, null);
            boolean horizontal = dimension == LayoutConstants.HORIZONTAL;
            setDisplayName(FormUtils.getBundleString(horizontal ?
                "PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE" : "PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE")); // NOI18N
            setShortDescription(FormUtils.getBundleString(horizontal ?
                "HINT_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE" : "HINT_LAYOUT_COMPONENT_VERTICAL_RESIZABLE")); // NOI18N
            this.dimension = dimension;
        }

        private LayoutComponent getComponent() {
            if (component == null) {
                component = getFormModel().getLayoutModel().getLayoutComponent(getId());
                if (component != null) {                    
                    String sourcePropertyName = (dimension == LayoutConstants.HORIZONTAL) ?
                        LayoutConstants.PROP_HORIZONTAL_MAX_SIZE : LayoutConstants.PROP_VERTICAL_MAX_SIZE;
                    component.addPropertyChangeListener(new PropertySynchronizer(sourcePropertyName, getName()));
                }
            }
            return component;
        }

        @Override
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            boolean resizable = newValue.booleanValue();
            LayoutModel layoutModel = getFormModel().getLayoutModel();
            LayoutInterval interval = getComponent().getLayoutInterval(dimension);
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.setUserIntervalSize(interval, dimension, interval.getPreferredSize(), resizable);
                getNodeReference().firePropertyChangeHelper(
                    getName(), oldValue, newValue);                
                autoUndo = false;
            } finally {
                getFormModel().fireContainerLayoutChanged(getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    getFormModel().addUndoableEdit(ue);
                }
                if (autoUndo) {
                    getFormModel().forceUndoOfCompoundEdit();
                }
            }
        }
        
        @Override
        public Object getValue() {
            int pref = getComponent().getLayoutInterval(dimension).getPreferredSize();
            int max = getComponent().getLayoutInterval(dimension).getMaximumSize();
            return Boolean.valueOf((max != pref) && (max != LayoutConstants.USE_PREFERRED_SIZE));
        }

        @Override
        Object getDefaultValue() {
            return Boolean.FALSE;
        }
    }

    /**
     * Property for specifying layer when the component is in JLayeredPane.
     */
    private class LayerProperty extends LProperty {
        LayerProperty() {
            super(PROP_LAYER, Integer.TYPE,
                  NbBundle.getMessage(LayoutSupportManager.class, "PROP_layer"),
                  NbBundle.getMessage(LayoutSupportManager.class, "HINT_layer"));
        }

        @Override
        public Object getValue() {
            Object value = getAuxValue(PROP_LAYER);
            if (!(value instanceof Integer)) {
                value = Integer.valueOf(JLayeredPane.DEFAULT_LAYER);
            }
            return value;
        }

        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException();
            }
            Integer oldValue = (Integer) getValue();
            setAuxValue(PROP_LAYER, value.equals(getDefaultValue()) ? null : value);
            if (!value.equals(oldValue)) {
                getNodeReference().firePropertyChangeHelper(getName(), oldValue, value);
                getFormModel().fireComponentLayoutChanged(RADVisualComponent.this, getName(), oldValue, value);
            }
        }

        @Override
        Object getDefaultValue() {
            return Integer.valueOf(JLayeredPane.DEFAULT_LAYER);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new LayerPropertyEditor();
        }
    }

    private static class LayerPropertyEditor extends EnumEditor {
        LayerPropertyEditor() {
            super(new Object[] {
                "DEFAULT_LAYER", JLayeredPane.DEFAULT_LAYER, "javax.swing.JLayeredPane.DEFAULT_LAYER", // NOI18N
                "PALETTE_LAYER", JLayeredPane.PALETTE_LAYER, "javax.swing.JLayeredPane.PALETTE_LAYER", // NOI18N
                "MODAL_LAYER", JLayeredPane.MODAL_LAYER, "javax.swing.JLayeredPane.MODAL_LAYER", // NOI18N
                "POPUP_LAYER", JLayeredPane.POPUP_LAYER, "javax.swing.JLayeredPane.POPUP_LAYER", // NOI18N
                "DRAG_LAYER", JLayeredPane.DRAG_LAYER, "javax.swing.JLayeredPane.DRAG_LAYER" // NOI18N
            }, false, true);
        }

        @Override
        public void setAsText(String str) {
            if (!setValueFromString(str)) { // not one of the known constants
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } catch (NumberFormatException ex) {
                }
            }
        }
    }
}
