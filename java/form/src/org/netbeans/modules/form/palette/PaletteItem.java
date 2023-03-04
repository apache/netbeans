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

package org.netbeans.modules.form.palette;

import java.beans.*;
import java.awt.Image;

import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.project.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

/**
 * PaletteItem holds important information about one component (item)
 * in the palette.
 *
 * @author Tomas Pavek
 */

public final class PaletteItem implements Node.Cookie {
    public static final String TYPE_CHOOSE_BEAN = "chooseBean"; // NOI18N
    private PaletteItemDataObject itemDataObject;

    // raw data (as read from the item file - to be resolved lazily)
    ClassSource componentClassSource;
//    Boolean isContainer_explicit;
    String componentType_explicit;
    Image icon;
    private FileObject cpRepresentative;
    private String componentInitializerId;
    private ComponentInitializer componentInitializer;

    // resolved data (derived from the raw data)
    private Class componentClass;
    private Throwable lastError; // error occurred when loading component class
//    private Boolean componentIsContainer;
    private int componentType = -1;

    // type of component constants
    private static final int LAYOUT = 1;
    private static final int BORDER = 2;
    private static final int VISUAL = 4; // bit flag
    private static final int MENU = 8; // bit flag
    private static final int TYPE_MASK = 15;

    // -------

    PaletteItem(PaletteItemDataObject dobj) {
        itemDataObject = dobj;
    }

    public PaletteItem(ClassSource componentClassSource, Class componentClass) {
        this.componentClassSource = componentClassSource;
        this.componentClass = componentClass;
    }

    public void setComponentClassSource(ClassSource cs) {
        componentClass = null;
        lastError = null;
        componentType = -1;
        componentClassSource = cs;
    }

    void setComponentExplicitType(String type) {
        componentType_explicit = type;
    }

    void setComponentInitializerId(String initializerId) {
        componentInitializerId = initializerId;
        if (initializerId == null) {
            componentInitializer = null;
        }
    }

    String getComponentInitializerId() {
        return componentInitializerId;
    }

    /**
     * Used by Choose Bean palette item where the user can fill in whatever
     * class from the actual project classpath. Such a palette item will be
     * used in the same project, so ClassSource is not really needed (it is
     * normally used only for project output).
     */
    void setClassFromCurrentProject(String className, FileObject fileInProject) {
        String typeParameters = null;
        if (className != null) {
            int index = className.indexOf('<');
            if (index != -1) {
                typeParameters = className.substring(index);
                className = className.substring(0,index);
            }
        }
        setComponentClassSource(new ClassSource((className == null) ? null : className.trim(), Collections.EMPTY_LIST, typeParameters));
        cpRepresentative = fileInProject;
    }

    // -------

    /** @return a node visually representing this palette item */
    public Node getNode() {
        return ((itemDataObject == null) || !itemDataObject.isValid()) ? null : itemDataObject.getNodeDelegate();
    }

    /** @return a String identifying this palette item */
    public String getId() {
        return getComponentClassName();
    }

    public String getComponentClassName() {
        return componentClassSource.getClassName();
    }

    public ClassSource getComponentClassSource() {
        return componentClassSource;
    }

    /** @return the class of the component represented by this pallete item.
     * May return null - if class loading fails. */
    public Class getComponentClass() {
        if (componentClass == null && lastError == null)
            componentClass = loadComponentClass();
        return componentClass;
    }

    /** @return the exception occurred when trying to resolve the component
     *  class of this pallette item */
    public Throwable getError() {
        return lastError;
    }

    /** @return type of the component as String, e.g. "visual", "menu",
     * "layout", border */
    public String getExplicitComponentType() {
        return componentType_explicit;
    }

    /**
     * Called when the user selects a palette item to add it to the form. The
     * item initializer, if there is any, has a chance here to get data from
     * the user.
     * @param classPathRep FileObject of the target form, representing the classpath
     * @return false if the user canceled adding the item, true otherwise (incl.
     *         the case there is no initializer)
     */
    public boolean prepareComponentInitializer(FileObject classPathRep) {
        componentInitializer = null;
        // PENDING general registration of initializers
        if (componentInitializerId != null) {
            if (componentInitializerId.startsWith("Box.Filler")) { // NOI18N
                componentInitializer = new BoxFillerInitializer();
            } else if (componentInitializerId.equals(PaletteItem.TYPE_CHOOSE_BEAN)) {
                componentInitializer = new ChooseBeanInitializer();
            }
        }
        if (componentInitializer != null) {
            return componentInitializer.prepare(this, classPathRep);
        }
        String className = getComponentClassName();
        if (className != null) {
            checkDefaultPackage(className, classPathRep);
        }
        return true;
    }

    /**
     * Called when a RADComponent instance is created for the item, here it can
     * be initialized with the date obtained in the prepare phase.
     * @param component 
     */
    public void initializeComponent(RADComponent component) {
        if (componentInitializer != null) {
            componentInitializer.initializeComponent(component);
            componentInitializer = null;
        }
    }

    /** @return whether the component of this palette item is a visual component
     * (java.awt.Component subclass) */
    public boolean isVisual() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & VISUAL) != 0;
    }

    /** @return whether the component of this palette item is a menu component */
    public boolean isMenu() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & MENU) != 0;
    }

    /** @return whether the component of this palette item is a layout mamanger
     * (java.awt.LayoutManager implementation) */
    public boolean isLayout() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & TYPE_MASK) == LAYOUT;
    }

    /** @return whether the component of this palette item is a border
     * (javax.swing.border.Border implementation) */
    public boolean isBorder() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & TYPE_MASK) == BORDER;
    }

//    public boolean isContainer() {
//        if (componentIsContainer == null) {
//            if (isContainer_explicit != null)
//                componentIsContainer = isContainer_explicit;
//            else {
//                Class compClass = getComponentClass();
//                if (compClass != null
//                    && java.awt.Container.class.isAssignableFrom(compClass))
//                {
//                    BeanDescriptor bd = getBeanDescriptor();
//                    componentIsContainer =
//                        bd != null && Boolean.FALSE.equals(bd.getValue("isContainer")) ? // NOI18N
//                            Boolean.FALSE : Boolean.TRUE;
//                }
//                else componentIsContainer = Boolean.FALSE;
//            }
//        }
//        return componentIsContainer.booleanValue();
//    }

    @Override
    public String toString() {
        return PaletteUtils.getItemComponentDescription(this);
    }

    String getDisplayName() {
        BeanDescriptor bd = getBeanDescriptor();
        return bd != null ? bd.getDisplayName() : null;
    }

    String getTooltip() {
        BeanDescriptor bd = getBeanDescriptor();
        return bd != null ? bd.getShortDescription() : null;
    }

    public Image getIcon(int type) {
        if (icon != null) return icon;
        BeanInfo bi = getBeanInfo();
        return bi != null ? bi.getIcon(type) : null;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    void reset() {
        componentClass = null;
        lastError = null;
//        componentIsContainer = null; 
        componentType = -1;

        itemDataObject.displayName = null;
        itemDataObject.tooltip = null;
        itemDataObject.icon16 = null;
        itemDataObject.icon32 = null;
    }

    static boolean checkDefaultPackage(String className, FileObject classPathRep) {
        if (className.indexOf('.') == -1) { // Issue 79573
            ClassPath cp = ClassPath.getClassPath(classPathRep,  ClassPath.SOURCE);
            String resName = cp != null ? cp.getResourceName(classPathRep) : null;
            if (resName != null && resName.indexOf('/') > 0) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(FormUtils.getBundleString("MSG_DefaultPackageBean"), // NOI18N
                                                 NotifyDescriptor.WARNING_MESSAGE));
                return false;
            }
        }
        return true;
    }

    // -------

    interface ComponentInitializer {
        boolean prepare(PaletteItem item, FileObject classPathRep);
        void initializeComponent(RADComponent metacomp);
    }

    // -------

    private Class loadComponentClass() {
        try {
            if (cpRepresentative != null) {
                return ClassPathUtils.loadClass(getComponentClassSource().getClassName(), cpRepresentative);
            } else {
                return ClassPathUtils.loadClass(getComponentClassSource());
            }
        }
        catch (Exception ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            lastError = ex;
        }
        catch (LinkageError ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            lastError = ex;
        }
        return null;
    }

    private BeanInfo getBeanInfo() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return FormUtils.getBeanInfo(compClass);
            }
            catch (Exception ex) {} // ignore failure
            //catch (LinkageError ex) {}
            catch (Error er) {} // Issue 74002
        }
        return null;
    }

    private BeanDescriptor getBeanDescriptor() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return FormUtils.getBeanInfo(compClass).getBeanDescriptor();
            }
            catch (Exception ex) {} // ignore failure
            //catch (LinkageError ex) {}
            catch (Error er) {} // Issue 74002
        }
        return null;
    }

    private void resolveComponentType() {
        if (componentType_explicit == null) {
            componentType = 0;

            Class compClass = getComponentClass();
            if (compClass == null)
                return;

            if (java.awt.LayoutManager.class.isAssignableFrom(compClass)) {
                // PENDING LayoutSupportDelegate - should have special entry in pallette item file?
                componentType = LAYOUT;
                return;
            }

            if (javax.swing.border.Border.class.isAssignableFrom(compClass)) {
                componentType = BORDER;
                return;
            }

            if (java.awt.Component.class.isAssignableFrom(compClass))
                componentType |= VISUAL;

            if (java.awt.MenuComponent.class.isAssignableFrom(compClass)
                  || javax.swing.JMenuItem.class.isAssignableFrom(compClass)
                  || javax.swing.JMenuBar.class.isAssignableFrom(compClass)
                  || javax.swing.JPopupMenu.class.isAssignableFrom(compClass))
                componentType |= MENU;
        }
        else if ("visual".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = VISUAL;
        else if ("layout".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = LAYOUT;
        else if ("border".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = BORDER;
        else if ("menu".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = MENU | VISUAL;
        else
            componentType = 0;
    }
}
