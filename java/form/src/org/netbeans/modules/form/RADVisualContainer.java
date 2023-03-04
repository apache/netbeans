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

package org.netbeans.modules.form;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.Map;
import org.netbeans.modules.form.RADVisualComponent.MenuType;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutBuilder;

import org.netbeans.modules.form.layoutsupport.*;
import org.openide.ErrorManager;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList<RADVisualComponent> subComponents = new ArrayList<RADVisualComponent>(10);
    private LayoutSupportManager layoutSupport;
    private LayoutNode layoutNode; // [move to LayoutSupportManager?]
    private LayoutSupportDelegate defaultLayoutDelegate;
    private LayoutManager defaultLayout;

    private RADComponent containerMenu;

    private Method containerDelegateGetter;
    private boolean noContainerDelegate;

    private static Map<MenuType, Class[]> supportedMenus;

    static final String CUSTOM_WINDOW_CONTAINER = "netbeans.form-design-window"; // NOI18N

    @Override
    protected void setBeanInstance(Object beanInstance) {
        containerDelegateGetter = null;
        noContainerDelegate = false;

        super.setBeanInstance(beanInstance);

        rememberDefaultLayout();
        if (layoutSupport != null) { // need new layout support for new container bean
            layoutSupport = new LayoutSupportManager(this, getFormModel().getCodeStructure());
        }
    }

    @Override
    void setInModel(boolean in) {
        boolean alreadyIn = isInModel();
        super.setInModel(in);
        if (in && !alreadyIn && layoutSupport != null) {
            // deferred initialization from pre-creation
            try {
                layoutSupport.initializeLayoutDelegate();
            }
            catch (Exception ex) {
                // [not reported - but very unlikely to happen - only for new container with custom layout]
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                layoutSupport.setUnknownLayoutDelegate(false);
            }
        }
    }

    public void setLayoutSupportDelegate(LayoutSupportDelegate layoutDelegate)
            throws Exception {
        if (layoutDelegate == defaultLayoutDelegate && layoutDelegate.getSupportedClass() != null) {
            layoutSupport.setLayoutDelegate(layoutDelegate, defaultLayout, false);
        } else {
            layoutSupport.setLayoutDelegate(layoutDelegate, null, false);
        }
        setLayoutNodeReference(null);
    }

    public LayoutSupportManager getLayoutSupport() {
        return layoutSupport;
    }

//    public boolean isLayoutSupportSet() {
//        return layoutSupport.getLayoutDelegate() != null;
//    }

    public static boolean isFreeDesignContainer(RADComponent metacomp) {
        return metacomp instanceof RADVisualContainer
               && ((RADVisualContainer)metacomp).getLayoutSupport() == null;
    }

    public static boolean isInFreeDesign(RADComponent metacomp) {
        if (metacomp instanceof RADVisualComponent) {
            RADVisualContainer parent = (RADVisualContainer) metacomp.getParentComponent();
            if (parent != null && parent.getLayoutSupport() == null
                    && metacomp != parent.getContainerMenu()) {
                return true;
            }
        }
        return false;
    }

    void setOldLayoutSupport(boolean old) {
        if (old) {
            if (layoutSupport == null) {
                layoutSupport = new LayoutSupportManager(this, getFormModel().getCodeStructure());
            }
        }
        else {
            if (layoutSupport != null) { // clean the layout delegate and related code structre objects
                try {
                    layoutSupport.setLayoutDelegate(null, null, false);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                refillContainerInstance();
            }
            layoutSupport = null;
            setLayoutNodeReference(null);
        }
    }

    private void refillContainerInstance() {
        Container cont = getContainerDelegate(getBeanInstance());
        cont.removeAll();
        cont.setLayout(null); // Issue 77904
        for (RADVisualComponent sub : subComponents) {
            Component comp = (Component) sub.getBeanInstance();
            FakePeerSupport.attachFakePeer(comp);
            if (comp instanceof Container)
                FakePeerSupport.attachFakePeerRecursively((Container)comp);
            cont.add(comp);
        }
    }

    public boolean hasDedicatedLayoutSupport() {
        return layoutSupport != null && layoutSupport.isDedicated();
    }

    public boolean hasDefaultLayout() {
        if (layoutSupport == null || !layoutSupport.isDedicated()) {
            LayoutManager layout = getContainerDelegate(getBeanInstance()).getLayout();
            return sameLayouts(defaultLayout != null ? defaultLayout.getClass() : null,
                               layout != null ? layout.getClass() : null);
        }
        return false;
    }

    public boolean isLayoutDefaultLayout(Class layoutClass) {
        return sameLayouts(defaultLayout != null ? defaultLayout.getClass() : null, layoutClass);
    }

    public static boolean sameLayouts(Class layoutClass1, Class layoutClass2) {
        if (layoutClass2 == layoutClass1) {
            return true; // incl. both null
        } else if (layoutClass1 != null && layoutClass2 != null) {
            return layoutClass2.getName().equals(layoutClass1.getName())
                   || (layoutClass2.isAssignableFrom(layoutClass1)
                       && (layoutClass1.getModifiers()&Modifier.PUBLIC) == 0) // e.g. JRootPane$1 should be same as BorderLayout
                   || (layoutClass1.isAssignableFrom(layoutClass2)
                       && (layoutClass2.getModifiers()&Modifier.PUBLIC) == 0) // e.g. JRootPane$1 should be same as BorderLayout
                   || (SwingLayoutBuilder.isRelevantLayoutManager(layoutClass1.getName())
                       && SwingLayoutBuilder.isRelevantLayoutManager(layoutClass2.getName()));
        }
        return false;
    }

    public LayoutManager getDefaultLayout() {
        return defaultLayout;
    }

    public boolean isDefaultLayoutDelegate(LayoutSupportDelegate layoutDelegate) {
        return layoutDelegate == defaultLayoutDelegate;
    }

    public LayoutSupportDelegate getDefaultLayoutDelegate(boolean forceNew) throws Exception {
        if (hasDedicatedLayoutSupport()
                || (defaultLayout != null && SwingLayoutBuilder.isRelevantLayoutManager(defaultLayout.getClass().getName()))) {
            return null;
        }
        if (defaultLayoutDelegate == null || forceNew) {
            defaultLayoutDelegate = LayoutSupportManager.getLayoutDelegateForDefaultLayout(getFormModel(), defaultLayout);
        }
        return defaultLayoutDelegate;
    }

    private void rememberDefaultLayout() {
        defaultLayout = getContainerDelegate(getBeanInstance()).getLayout();
    }

    /**
     * @param container container.
     * @return The JavaBean visual container represented by this
     * RADVisualComponent
     */
    public Container getContainerDelegate(Object container) {
        if (container instanceof RootPaneContainer
                && container.getClass().getName().startsWith("javax.swing.")) { // NOI18N
            return ((RootPaneContainer)container).getContentPane();
        }

        if (container.getClass().equals(JRootPane.class)) {
            Object w = ((JRootPane)container).getClientProperty(CUSTOM_WINDOW_CONTAINER); // NOI18N
            if (w instanceof RootPaneContainer) {
                container = w;
            } else {
                return ((JRootPane)container).getContentPane();
            }
        }

        Container containerDelegate = (Container) container;
        // Do not attempt to find container delegate if the classes
        // don't match. This can happen when ViewConverter was used.
        // Happens for JApplet, for example.
        if (getBeanClass().isAssignableFrom(container.getClass())) {
            Method m = getContainerDelegateMethod();
            if (m != null) {
                try {
                    containerDelegate = (Container) m.invoke(container, new Object[0]);
                    if ((containerDelegate == null) && (container instanceof JScrollPane)) {
                        JScrollPane scrollPane = (JScrollPane)container;
                        scrollPane.setViewportView(null); // force recreation of viewport
                        containerDelegate = (Container) m.invoke(container, new Object[0]);
                    }
                }
                catch (Exception ex) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        return containerDelegate;
    }

    public Method getContainerDelegateMethod() {
        if (containerDelegateGetter == null && !noContainerDelegate) {
            String delegateGetterName = getContainerDelegateGetterName();
            if (delegateGetterName == null
                && (RootPaneContainer.class.isAssignableFrom(getBeanClass())
                    || JRootPane.class.isAssignableFrom(getBeanClass())))
                delegateGetterName = "getContentPane"; // NOI18N

            if (delegateGetterName != null) {
                try {
                    containerDelegateGetter =
                        getBeanClass().getMethod(
                            delegateGetterName, new Class[0]);
                }
                catch (NoSuchMethodException ex) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
            else noContainerDelegate = true;
        }
        return containerDelegateGetter;
    }

    String getContainerDelegateGetterName() {
        Object value = getBeanInfo().getBeanDescriptor()
                                        .getValue("containerDelegate"); // NOI18N
        
        if (value instanceof String && !((String)value).isEmpty())
            return (String) value;
        else
            return null;
    }

    public void setLayoutNodeReference(LayoutNode node) {
        this.layoutNode = node;
    }

    public LayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    boolean shouldHaveLayoutNode() {
        return layoutSupport != null && layoutSupport.shouldHaveNode();
    }

    public RADComponent getContainerMenu() {
        return containerMenu;
    }

    public boolean canAddComponent(Class compClass) {
        if (isMenuTypeComponent()) {
            // this is a menu container accepting certain types of menus
            Class[] possibleClasses = getPossibleSubmenus(getMenuType(getBeanClass()));
            if (possibleClasses != null) {
                for (Class<?> cls : possibleClasses) {
                    if (cls.isAssignableFrom(compClass)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (getContainerMenu() == null && canHaveMenu(compClass)) {
            // visual container that can have a menubar
            return true;
        } else if (getMenuType(compClass) != null && !JSeparator.class.isAssignableFrom(compClass)) {
            // otherwise don't accept menu components
            return false;
        } else if (Component.class.isAssignableFrom(compClass)) {
            // visual component can be added to visual container
            // exception: avoid adding components to scroll pane that already contains something
            if (JScrollPane.class.isAssignableFrom(getBeanClass())
                    && (((JScrollPane)getBeanInstance()).getViewport() != null)
                    && (((JScrollPane)getBeanInstance()).getViewport().getView() != null)) {
                return false;
            }
            return true;
        }
        return false;
    }

    boolean canHaveMenu(Class menuClass) {
        return (JMenuBar.class.isAssignableFrom(menuClass)
                  && RootPaneContainer.class.isAssignableFrom(getBeanClass()))
               ||
               (MenuBar.class.isAssignableFrom(menuClass)
                  && Frame.class.isAssignableFrom(getBeanClass())
                  && !JFrame.class.isAssignableFrom(getBeanClass()));
    }

    private static Class[] getPossibleSubmenus(MenuType menuContainerType) {
        if (supportedMenus == null) {
            supportedMenus = new EnumMap<MenuType, Class[]>(MenuType.class);
            supportedMenus.put(MenuType.JMenuBar, new Class[] { JMenu.class });
            supportedMenus.put(MenuType.JMenu,
                               new Class[] { JMenuItem.class,
                                             JCheckBoxMenuItem.class,
                                             JRadioButtonMenuItem.class,
                                             JMenu.class,
                                             JSeparator.class });
            supportedMenus.put(MenuType.JPopupMenu,
                               new Class[] { JMenuItem.class,
                                             JCheckBoxMenuItem.class,
                                             JRadioButtonMenuItem.class,
                                             JMenu.class,
                                             JSeparator.class });
        }
        return supportedMenus.get(menuContainerType);
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    /** @return visual subcomponents (not the menu component) */
    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent[subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public RADVisualComponent getSubComponent(int index) {
        return subComponents.get(index);
    }

    boolean hasVisualSubComponents() {
        return !subComponents.isEmpty();
    }

    // the following methods implement ComponentContainer interface

    /** @return all subcomponents (including the menu component) */
    @Override
    public RADComponent[] getSubBeans() {
        int n = subComponents.size();
        if (containerMenu != null)
            n++;

        RADComponent[] components = new RADComponent[n];
        subComponents.toArray(components);
        if (containerMenu != null)
            components[n-1] = containerMenu;

        return components;
    }

    @Override
    public void initSubComponents(RADComponent[] initComponents) {
        if (subComponents == null)
            subComponents = new ArrayList<RADVisualComponent>(initComponents.length);
        else {
            subComponents.clear();
            subComponents.ensureCapacity(initComponents.length);
        }

        for (int i=0; i < initComponents.length; i++) {
            RADComponent metacomp = initComponents[i];
            if (!isMenuTypeComponent() && canHaveMenu(metacomp.getBeanClass())) {
                containerMenu = metacomp;
            } else {
                subComponents.add((RADVisualComponent)metacomp);
            }
            metacomp.setParentComponent(this);
        }

        if (layoutSupport == null)
            refillContainerInstance();
    }

    @Override
    public void reorderSubComponents(int[] perm) {
        RADVisualComponent[] components = new RADVisualComponent[subComponents.size()];
        LayoutConstraints[] constraints;
        if (layoutSupport != null) {
            layoutSupport.removeAll();
            constraints = new LayoutConstraints[subComponents.size()];
        }
        else constraints = null;

        for (int i=0; i < perm.length; i++) {
            RADVisualComponent metacomp = subComponents.get(i);
            components[perm[i]] = metacomp;
            if (constraints != null)
                constraints[perm[i]] = layoutSupport.getStoredConstraints(metacomp);
        }

        subComponents.clear();
        subComponents.addAll(java.util.Arrays.asList(components));

        if (layoutSupport != null) {
            layoutSupport.addComponents(components, constraints, 0);
        }
        else {
            refillContainerInstance();
        }
    }

    @Override
    public void add(RADComponent comp) {
        add(comp, -1);
    }

    public void add(RADComponent metacomp, int index) {
        RADVisualComponent visual;
        if (index <= 0 && !isMenuTypeComponent() && canHaveMenu(metacomp.getBeanClass())) {
            containerMenu = metacomp;
            visual = null;
        } else {
            visual = (RADVisualComponent) metacomp;
            if (index == -1) {
                subComponents.add(visual);
            } else {
                subComponents.add(index, visual);
            }
            if (layoutSupport == null) {
                Component comp = (Component) visual.getBeanInstance();
                FakePeerSupport.attachFakePeer(comp);
                if (comp instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)comp);
                getContainerDelegate(getBeanInstance()).add(comp, index);
            }
        }

        metacomp.setParentComponent(this);
        if (visual != null) { // force constraints properties creation
            visual.getConstraintsProperties();
        }
    }

    @Override
    public void remove(RADComponent comp) {
        if (comp == containerMenu) {
            containerMenu = null;
            comp.setParentComponent(null);
        } else if (comp instanceof RADVisualComponent) {
            int index = subComponents.indexOf(comp);
            if (layoutSupport != null) {
                layoutSupport.removeComponent((RADVisualComponent) comp, index);
            }
            else {
                getContainerDelegate(getBeanInstance()).remove(index);
            }
            if (subComponents.remove((RADVisualComponent)comp))
                comp.setParentComponent(null);
        }
    }

    @Override
    public int getIndexOf(RADComponent comp) {
        if (comp != null && comp == containerMenu)
            return subComponents.size();

        return subComponents.lastIndexOf(comp);
    }

    boolean isLayoutSubcomponent(RADComponent comp) {
        return (comp != containerMenu);
    }

}
