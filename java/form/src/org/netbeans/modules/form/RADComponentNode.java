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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.util.logging.Level;
import javax.swing.Action;

import org.openide.ErrorManager;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.explorer.propertysheet.editors.NodeCustomizer;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.editors.TableCustomizer;
import org.netbeans.modules.form.menu.AddSubItemAction;
import org.netbeans.modules.form.menu.InsertMenuAction;
import org.netbeans.modules.form.menu.MenuEditLayer;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.util.datatransfer.PasteType;

public class RADComponentNode extends FormNode
        implements RADComponentCookie, FormPropertyCookie {
    private static final MessageFormat nodeNameFormat =
            new MessageFormat(
            FormUtils.getBundleString("FMT_ComponentNodeName")); // NOI18N
    private static final MessageFormat nodeNoNameFormat =
            new MessageFormat(
            FormUtils.getBundleString("FMT_UnnamedComponentNodeName")); // NOI18N
    
    private RADComponent component;
    private boolean highlightDisplayName;
    private Map<Integer,Image> img = new HashMap<Integer,Image>();
    
    public RADComponentNode(RADComponent component) {
        this(component instanceof ComponentContainer ?
            new RADChildren((ComponentContainer)component) : Children.LEAF,
            component);
    }
    
    public RADComponentNode(Children children, RADComponent component) {
        super(children, component.getFormModel());
        this.component = component;
        component.setNodeReference(this);
        //        getCookieSet().add(this);
        if (component instanceof ComponentContainer)
            getCookieSet().add(new ComponentsIndex());
        updateName();
    }
    
    void updateName() {
        String compClassName = Utilities.getShortClassName(
                component.getBeanClass());
        if (component == component.getFormModel().getTopRADComponent())
            setDisplayName(nodeNoNameFormat.format(
                    new Object[] { compClassName }));
        else
            setDisplayName(nodeNameFormat.format(
                    new Object[] { getName(), compClassName }));
    }
    
    public void fireComponentPropertiesChange() {
        firePropertyChange(null, null, null);
    }
    
    public void fireComponentPropertySetsChange() {
        firePropertySetsChange(null, null);
    }

    private static boolean iconsInitialized;
    
    @Override
    public Image getIcon(final int iconType) {
        Image icon = img.get(iconType);
        if (icon != null) return icon;
        
        // try to get a special icon
        icon = BeanSupport.getBeanIcon(component.getBeanClass(), iconType);
        if (icon == null) {
            final String className = component.getBeanClass().getName();
            final String classDetails = (String)component.getAuxValue(RADComponent.AUX_VALUE_CLASS_DETAILS);
            if (!iconsInitialized) {
                // getIconForClass invokes getNodes(true) which cannot be called in Mutex
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Image icon = PaletteUtils.getIconForClass(className, classDetails, iconType, true);
			iconsInitialized = true;
                        if (icon != null) {
                            img.put(iconType, icon);
                            fireIconChange();
                        }
                    }
                });
            } else {
                icon = PaletteUtils.getIconForClass(className, classDetails, iconType, false);
            }

            if (icon == null) {
                // get icon from BeanInfo
                java.beans.BeanInfo bi = component.getBeanInfo();
                if (bi != null) {
                    icon = bi.getIcon(iconType);
                }

                if (icon == null) {
                    // use default icon
                    icon = super.getIcon(iconType);
                }
            }
        }
        img.put(iconType, icon);
        return icon;
    }

    @Override
    public Image getOpenedIcon(int iconType) {
        return getIcon(iconType);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.components.editing"); // NOI18N
    }

    @Override
    public Node.PropertySet[] getPropertySets() {
        final Node.PropertySet[][] props = new Node.PropertySet[1][];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FormLAF.executeWithLAFLocks(new Runnable() {
                    @Override
                    public void run() {
                       props[0] = component.getProperties();
                    }
                });
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                // We have made some attempts to keep initialization
                // of properties outside AWT thread, but it always
                // deadlocked with AWT thread for various reasons.
                EventQueue.invokeAndWait(runnable);
            } catch (InterruptedException iex) {
                FormUtils.LOGGER.log(Level.INFO, iex.getMessage(), iex);
            } catch (InvocationTargetException itex) {
                FormUtils.LOGGER.log(Level.INFO, itex.getMessage(), itex);
            }
        }
        return props[0];
    }

    /* List new types that can be created in this node.
     * @return new types
     */
    @Override
    public NewType[] getNewTypes() {
        return component.getNewTypes();
    }

    @Override
    public Action getPreferredAction() {
        FormEditor formEditor = FormEditor.getFormEditor(component.getFormModel());
        if (formEditor == null) {
            return null;
        }

        for (Action action : formEditor.getDefaultComponentActions()) {
            if (action.isEnabled()) {
                return action;
            }
        }

        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) {
            List<Action> actions = new ArrayList<Action>(20);
            RADComponent topComp = component.getFormModel().getTopRADComponent();
            
            if (component.isReadOnly()) {
                if (component == topComp) {
                    actions.add(SystemAction.get(TestAction.class));
                    actions.add(null);
                }
                Event[] events = component.getKnownEvents();
                for (int i=0; i < events.length; i++) {
                    if (events[i].hasEventHandlers()) {
                        actions.add(SystemAction.get(EventsAction.class));
                        actions.add(null);
                        break;
                    }
                }
                
                actions.add(SystemAction.get(CopyAction.class));
            } else {
                if (InPlaceEditLayer.supportsEditingFor(component.getBeanClass(), false)) {
                    actions.add(SystemAction.get(InPlaceEditAction.class));
                }
                if (javax.swing.JTable.class.isAssignableFrom(component.getBeanClass())) {
                    actions.add(SystemAction.get(CustomizeTableAction.class));
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(ChangeVariableNameAction.class));
                } else {
                    actions.add(SystemAction.get(TestAction.class));
                }
                if (FormEditor.getBindingSupport(component.getFormModel()) != null) {
                    actions.add(SystemAction.get(BindAction.class));
                }
                actions.add(SystemAction.get(EventsAction.class));
                actions.add(null);
                
                java.util.List actionProps = component.getActionProperties();
                Iterator iter = actionProps.iterator();
                while (iter.hasNext()) {
                    final RADProperty prop = (RADProperty)iter.next();
                    Action action = PropertyAction.createIfEditable(prop);
                    if (action != null) {
                        actions.add(action);
                    }
                }
                addSeparator(actions);

                if (component instanceof ComponentContainer) {
                    addContainerActions(actions);
                    addLayoutActions(actions);
                } else {
                    addLayoutActions(actions);
                    addContainerActions(actions);
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(MoveUpAction.class));
                    actions.add(SystemAction.get(MoveDownAction.class));
                }
                if (component instanceof ComponentContainer) {
                    actions.add(SystemAction.get(ReorderAction.class));
                }
                addSeparator(actions);
                
                if (component != topComp) {
                    actions.add(SystemAction.get(CutAction.class));
                }
                actions.add(SystemAction.get(CopyAction.class));
                if (component instanceof ComponentContainer) {
                    actions.add(SystemAction.get(PasteAction.class));
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(DuplicateAction.class));
                    actions.add(SystemAction.get(DeleteAction.class));
                }
                
                actions.add(null);
                actions.add(SystemAction.get(CustomCodeAction.class));
            }
            actions.add(null);
            
            javax.swing.Action[] superActions = super.getActions(context);
            for (int i=0; i < superActions.length; i++)
                actions.add(superActions[i]);
            
            this.actions = new Action[actions.size()];
            actions.toArray(this.actions);
        }
        
        return actions;
    }
    
    private void addLayoutActions(List<Action> actions) {
        if (component.getParentComponent() instanceof RADVisualContainer) {
            actions.add(SystemAction.get(AlignAction.class));
            actions.add(SystemAction.get(SetAnchoringAction.class));
            actions.add(SystemAction.get(SetResizabilityAction.class));
            actions.add(SystemAction.get(ChooseSameSizeAction.class));
            actions.add(SystemAction.get(DefaultSizeAction.class));
            actions.add(SystemAction.get(EncloseAction.class));
            actions.add(SystemAction.get(CustomizeEmptySpaceAction.class));
            actions.add(null);
        }
    }

    private void addContainerActions(List<Action> actions) {
        if (component instanceof RADVisualContainer) {
            if (!((RADVisualContainer)component).hasDedicatedLayoutSupport()) {
                actions.add(SystemAction.get(SelectLayoutAction.class));
                actions.add(SystemAction.get(CustomizeLayoutAction.class));
            }
            if(MenuEditLayer.isMenuBarContainer(component)) {
                actions.add(SystemAction.get(InsertMenuAction.class));
            } else if(MenuEditLayer.isMenuRelatedContainer(component)) {
                actions.add(SystemAction.get(AddSubItemAction.class));
            } else { // only use the AddAction for non-menu containers
                actions.add(SystemAction.get(AddAction.class));
            }
        }
        if (getNewTypes().length != 0) {
            actions.add(null);
            actions.add(SystemAction.get(NewAction.class));
        }
        if (EditContainerAction.isEditableComponent(component)) {
            actions.add(SystemAction.get(EditContainerAction.class));
        }
        if (DesignParentAction.isParentEditableComponent(component)) {
            actions.add(SystemAction.get(DesignParentAction.class));
        }
        if (component.getParentComponent() == null) {
            if (component instanceof RADVisualComponent) {
                actions.add(SystemAction.get(DefaultSizeAction.class));
            }
            if (component instanceof RADVisualContainer
                    && !((RADVisualContainer)component).hasDedicatedLayoutSupport()) {
                actions.add(SystemAction.get(CustomizeEmptySpaceAction.class));
            }
        }

        addSeparator(actions);
    }

    private static void addSeparator(List<Action> actions) {
        int n = actions.size();
        if (n > 0 && actions.get(n-1) != null) {
            actions.add(null);
        }
    }

    @Override
    public String getName() {
        return component.getName();
    }
    
    /** Set the system name. Fires a property change event.
     * Also may change the display name according to {@link #displayFormat}.
     *
     * @param s the new name
     */
    @Override
    public void setName(String s) {
        component.rename(s);
    }
    
    /** Can this node be renamed?
     * @return <code>false</code>
     */
    @Override
    public boolean canRename() {
        return !component.isReadOnly()
                && component != component.getFormModel().getTopRADComponent();
    }
    
    /** Can this node be destroyed?
     * @return <CODE>false</CODE>
     */
    @Override
    public boolean canDestroy() {
        return !component.isReadOnly()
                && component != component.getFormModel().getTopRADComponent();
    }
    
    /** Remove the node from its parent and deletes it.
     * The default
     * implementation obtains write access to
     * the {@link Children#MUTEX children's lock}, and removes
     * the node from its parent(if any). Also fires a property change.
     * <P>
     * This may be overridden by subclasses to do any additional
     * cleanup.
     *
     * @exception java.io.IOException if something fails
     */
    @Override
    public void destroy() throws java.io.IOException {
        if (component.getNodeReference() == this) {
            if (MetaComponentCreator.isTransparentLayoutComponent(component.getParentComponent())) {
                component = component.getParentComponent();
            }
            if (EventQueue.isDispatchThread()) {
                component.getFormModel().removeComponent(component, true);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        component.getFormModel().removeComponent(component, true);
                    }
                });
            }
        } // otherwise the component was likely already removed with a parent component
        super.destroy();
    }
    
    /** Test whether there is a customizer for this node. If true,
     * the customizer can be obtained via {@link #getCustomizer}.
     *
     * @return <CODE>true</CODE> if there is a customizer
     */
    @Override
    public boolean hasCustomizer() {
        return !component.isReadOnly()
                && ((component.getBeanInfo().getBeanDescriptor().getCustomizerClass() != null));
    }
    
    /** Creates the customizer component for the node.
     * @return the component, or null if there is no customizer
     */
    @Override
    protected Component createCustomizer() {
        Class customizerClass = component.getBeanInfo().getBeanDescriptor().getCustomizerClass();
        if (customizerClass == null) {
            if (javax.swing.JTable.class.isAssignableFrom(component.getBeanClass())) {
                customizerClass = TableCustomizer.class;
            } else {
                return null;
            }
        }
        
        Object customizerObject;
        try {
            customizerObject = customizerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return null;
        }
        
        if (!(customizerObject instanceof Component)
                || !(customizerObject instanceof Customizer))
            return null;
        
        if (customizerObject instanceof NodeCustomizer)
            ((NodeCustomizer)customizerObject)
                    .attach(component.getNodeReference());

        // Issue 203352 - default values of properties must be initialized
        // before the customizer is shown/used
        component.ensureDefaultPropertyValuesInitialization();
        
        Customizer customizer = (Customizer) customizerObject;
        
        customizer.setObject(component.getBeanInstance());

        if (customizerObject instanceof FormAwareEditor) {
            // Hack - returns some property
            Node.Property prop = component.getProperties()[0].getProperties()[0];
            ((FormAwareEditor)customizerObject).setContext(component.getFormModel(), (FormProperty)prop);
        }

        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                FormProperty[] properties;
                if (evt.getPropertyName() != null) {
                    FormProperty changedProperty =
                            component.getBeanProperty(evt.getPropertyName());
                    if (changedProperty != null)
                        properties = new FormProperty[] { changedProperty };
                    else return; // non-existing property?
                }
                else {
                    properties = component.getAllBeanProperties();
                    evt = null;
                }
                updatePropertiesFromCustomizer(properties, evt);
            }
        });
        // [undo/redo for customizer probably does not work...]
        
        return (Component) customizerObject;
    }
    
    private void updatePropertiesFromCustomizer(
            final FormProperty[] properties,
            final PropertyChangeEvent evt)
    {
        // we run this as privileged to avoid security problems - because
        // the property change is fired from untrusted bean customizer code
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                Object oldValue = evt != null ? evt.getOldValue() : null;
                Object newValue = evt != null ? evt.getNewValue() : null;
                
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = properties[i];
                    try {
                        prop.reinstateProperty();
                        //                        if (prop.isChanged()) // [what if changed to default value?]
                        prop.propertyValueChanged(oldValue, newValue);
                    }
                    catch (Exception ex) { // unlikely to happen
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }
    
    // -----------------------------------------------------------------------------------------
    // Clipboard operations
    
    /** Test whether this node can be copied.
     * The default implementation returns <code>true</code>.
     * @return <code>true</code> if it can
     */
    @Override
    public boolean canCopy() {
        return true;
    }
    
    /** Test whether this node can be cut.
     * The default implementation assumes it can if this node is writeable.
     * @return <code>true</code> if it can
     */
    @Override
    public boolean canCut() {
        return !component.isReadOnly()
                && component != component.getFormModel().getTopRADComponent();
    }
    
    /** Copy this node to the clipboard.
     *
     * @return The transferable for RACComponentNode
     * @throws java.io.IOException if it could not copy
     */
    @Override
    public Transferable clipboardCopy() throws java.io.IOException {
        return new CopySupport.RADTransferable(
                CopySupport.getComponentCopyFlavor(),
                component);
    }
    
    /** Cut this node to the clipboard.
     *
     * @return {@link Transferable} with one flavor, {@link RAD_COMPONENT_COPY_FLAVOR }
     * @throws java.io.IOException if it could not cut
     */
    @Override
    public Transferable clipboardCut() throws java.io.IOException {
        return new CopySupport.RADTransferable(
                CopySupport.getComponentCutFlavor(),
                component);
    }
    
    /** Accumulate the paste types that this node can handle
     * for a given transferable.
     * @param t a transferable containing clipboard data
     * @param s a list of {@link PasteType}s that will have added to it all
     *          types valid for this node
     */
    @Override
    protected void createPasteTypes(Transferable t, java.util.List<PasteType> s) {
        CopySupport.createPasteTypes(t, s, component.getFormModel(), component);
    }

    // -----------------------------------------------------------------------------
    // RADComponentCookie implementation
    
    @Override
    public RADComponent getRADComponent() {
        return component;
    }
    
    // -----------------------------------
    // FormPropertyCookie implementation
    
    @Override
    public FormProperty getProperty(String name) {
        return component.getPropertyByName(name, FormProperty.class, true);
        //        Node.Property prop = component.getPropertyByName(name, true);
        //        return (FormProperty) (prop instanceof FormProperty ? prop : null);
    }
    
    // -----------------------------------------------------------------------------
    // Innerclasses
    
    public static class RADChildren extends FormNodeChildren {
        private ComponentContainer container;
        private Object keyLayout;
        
        public RADChildren(ComponentContainer container) {
            super();
            this.container = container;
            updateKeys();
        }
        
        // FormNodeChildren implementation
        @Override
        protected void updateKeys() {
            RADComponent[] subComps = container.getSubBeans();
            List<Object> keys = new ArrayList<Object>(subComps.length + 2);
            
            if (container instanceof RADVisualContainer) {
                RADVisualContainer visualCont = (RADVisualContainer) container;
                
                RADComponent menuComp = visualCont.getContainerMenu();
                if (menuComp != null)
                    keys.add(menuComp);
                
                if (visualCont.shouldHaveLayoutNode()) {
                    keyLayout = visualCont.getLayoutSupport().getLayoutDelegate(); //new Object(); // [need not be recreated every time]
                    keys.add(keyLayout);
                }
                
                for (int i=0; i < subComps.length; i++)
                    if (subComps[i] != menuComp)
                        keys.add(subComps[i]);
            }
            else {
                for (int i=0; i < subComps.length; i++)
                    keys.add(subComps[i]);
            }
            
            setKeys(keys);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            Node node;
            if (key == keyLayout)
                node = new LayoutNode((RADVisualContainer)container);
            else {
                node = new RADComponentNode((RADComponent)key);
                node.getChildren().getNodes(); // enforce subnodes creation
            }
            return new Node[] { node };
        }
    }
    
    private final class ComponentsIndex extends org.openide.nodes.Index.Support {
        
        @Override
        public Node[] getNodes() {
            RADComponent[] comps;
            if (component instanceof RADVisualContainer)
                comps = ((RADVisualContainer)component).getSubComponents();
            else if (component instanceof ComponentContainer)
                comps = ((ComponentContainer)component).getSubBeans();
            else
                comps = null;
            
            Node[] nodes = new Node[comps != null ? comps.length : 0];
            for (int i = 0; i < nodes.length; i++)
                nodes[i] = comps[i].getNodeReference();
            
            return nodes;
        }
        
        @Override
        public int getNodesCount() {
            return getNodes().length;
        }
        
        @Override
        public void reorder(int[] perm) {
            if (component instanceof ComponentContainer) {
                ComponentContainer cont = (ComponentContainer) component;
                cont.reorderSubComponents(perm);
                component.getFormModel().fireComponentsReordered(cont, perm);
            }
        }
    }
    
    private static final class ChangeVariableNameAction extends RenameAction {
        @Override
        public String getName() {
            return NbBundle.getMessage(ChangeVariableNameAction.class, "ChangeVariableNameAction"); // NOI18N
        }
    }

    @Override
    public String getHtmlDisplayName() {
        if(highlightDisplayName) {
            return  "<html><b>" + getDisplayName() + "</b></html>"; // NOI18N
        } else {
            return "<html>" + getDisplayName() + "</html>"; // NOI18N
        }
    }
    
    void highlightDisplayName(boolean highlight) {
        if(highlight != highlightDisplayName) {
            highlightDisplayName = highlight;
            fireDisplayNameChange(null, getDisplayName());
        }
    }

    private static final class CustomizeTableAction extends CustomizeAction {
            @Override
            public String getName() {
                return FormUtils.getBundleString("NAME_CustomizeTableAction"); // NOI18N
            }
            
            @Override
            protected boolean enable(Node[] activatedNodes) {
                return true;
            }
    }

}
