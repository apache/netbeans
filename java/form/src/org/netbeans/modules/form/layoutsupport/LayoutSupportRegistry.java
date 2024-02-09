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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.util.*;
import java.lang.ref.*;
import java.lang.reflect.Modifier;
import org.netbeans.modules.form.CreationFactory;

import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * Registry and factory class for LayoutSupportDelegate implementations.
 *
 * @author Tomas Pavek
 */

public class LayoutSupportRegistry {

    private static Map<String,String> containerToLayoutDelegate;
    private static Map<String,String> layoutToLayoutDelegate;

    private static boolean needPaletteRescan = true;

    public static final String DEFAULT_SUPPORT = "<default>"; // NOI18N

    private static FileChangeListener paletteListener;

    private static Map<FormModel,LayoutSupportRegistry> instanceMap;

    private Reference<FormModel> formModelRef;

    // -------

    private LayoutSupportRegistry(FormModel formModel) {
        this.formModelRef = new WeakReference<FormModel>(formModel);
    }

    public static LayoutSupportRegistry getRegistry(FormModel formModel) {
        LayoutSupportRegistry reg;
        if (instanceMap == null) {
            instanceMap = new WeakHashMap<FormModel,LayoutSupportRegistry>(); 
            reg = null;
        }
        else reg = instanceMap.get(formModel);

        if (reg == null) {
            reg = new LayoutSupportRegistry(formModel);
            instanceMap.put(formModel, reg);
        }

        return reg;
    }

    // ------------
    // registering methods

    public static void registerSupportForContainer(
                           String containerClassName,
                           String layoutDelegateClassName)
    {
        getContainersMap().put(containerClassName, layoutDelegateClassName);
    }

    public static void registerSupportForLayout(
                           String layoutClassName,
                           String layoutDelegateClassName)
    {
        getLayoutsMap().put(layoutClassName, layoutDelegateClassName);
    }

    // ------------
    // creation methods

    public LayoutSupportDelegate createSupportForContainer(Class containerClass)
        throws ReflectiveOperationException
    {
        String delegateClassName = getContainersMap().get(containerClass.getName());
        if (delegateClassName == null) {
            return createLayoutSupportForSuperClass(getContainersMap(), containerClass);
        } else {
            return (LayoutSupportDelegate) loadClass(delegateClassName).getDeclaredConstructor().newInstance();
        }
    }

    public LayoutSupportDelegate createSupportForLayout(Class layoutClass)
        throws ReflectiveOperationException
    {
        String layoutClassName = layoutClass.getName();
        String delegateClassName = getLayoutsMap().get(layoutClassName);
        if (delegateClassName == null && needPaletteRescan) {
            delegateClassName = scanPalette(layoutClassName);
        }
        if (delegateClassName == null && !isUsableCustomLayoutClass(layoutClass)) {
            return createLayoutSupportForSuperClass(getLayoutsMap(), layoutClass);
        }
        if (DEFAULT_SUPPORT.equals(delegateClassName)) {
            return new DefaultLayoutSupport(layoutClass);
        } else if (delegateClassName != null) {
            return (LayoutSupportDelegate) loadClass(delegateClassName).getDeclaredConstructor().newInstance();
        } else {
            return null;
        }
    }

    public static LayoutSupportDelegate createSupportInstance(
                                            Class layoutDelegateClass)
        throws ReflectiveOperationException
    {
        return (LayoutSupportDelegate) layoutDelegateClass.getDeclaredConstructor().newInstance();
    }

    // -----------
    // private methods

    private static boolean isUsableCustomLayoutClass(Class layoutClass) {
        if ((layoutClass.getModifiers() & Modifier.PUBLIC) == 0) {
            return false;
        }
        try {
            if (layoutClass.getConstructor(new Class[0]) != null) {
                return true; // has a public constructor without parameters
            }
        } catch (NoSuchMethodException ex) {
        }
        return CreationFactory.getDescriptor(layoutClass) != null;
    }

    private LayoutSupportDelegate createLayoutSupportForSuperClass(Map<String,String> map, Class subClass)
            throws ReflectiveOperationException {
        // We don't ask if the loaded registered class is assignable from 'subClass'
        // because it would not work for custom classes when the project classloader changes.
        for (Map.Entry<String,String> en : map.entrySet()) {
            String regName = en.getKey();
            for (Class superClass=subClass.getSuperclass(); superClass != null; superClass=superClass.getSuperclass()) {
                if (superClass.getName().equals(regName)) {
                    String delegateClassName = en.getValue();
                    if (DEFAULT_SUPPORT.equals(delegateClassName)) {
                        return new DefaultLayoutSupport(superClass);
                    } else {
                        return (LayoutSupportDelegate) loadClass(delegateClassName).getDeclaredConstructor().newInstance();
                    }
                }
            }
        }
        return null;
    }

    private static String scanPalette(String wantedClassName) {
        FileObject paletteFolder = PaletteUtils.getPaletteFolder();

        // create palette content listener - only once
        boolean newPaletteListener = paletteListener == null;
        if (newPaletteListener) {
            paletteListener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent fe) {
                    needPaletteRescan = true;
                }
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    needPaletteRescan = true;
                    fe.getFile().addFileChangeListener(this);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    fe.getFile().removeFileChangeListener(this);
                }
            };

            paletteFolder.addFileChangeListener(paletteListener);
        }

        String foundSupportClassName = null;

        FileObject[] paletteCategories = paletteFolder.getChildren();
        for (int i=0; i < paletteCategories.length; i++) {
            FileObject categoryFolder = paletteCategories[i];
            if (!categoryFolder.isFolder())
                continue;
           
            if (newPaletteListener)
                categoryFolder.addFileChangeListener(paletteListener);

            FileObject[] paletteItems = categoryFolder.getChildren();
            for (int j=0; j < paletteItems.length; j++) {
                DataObject itemDO = null;
                try {
                    itemDO = DataObject.find(paletteItems[j]);
                }
                catch (DataObjectNotFoundException ex) {
                    continue;
                }

                PaletteItem item = itemDO.getCookie(PaletteItem.class);
                if (item == null || !item.isLayout())
                    continue;

                Class itemClass = item.getComponentClass();
                if (itemClass == null)
                    continue; // cannot resolve class - ignore

                Class delegateClass = null;
                Class supportedClass = null;

                if (LayoutSupportDelegate.class.isAssignableFrom(itemClass)) {
                    // register LayoutSupportDelegate directly
                    delegateClass = itemClass;
                    try {
                        LayoutSupportDelegate delegate =
                            (LayoutSupportDelegate) delegateClass.getDeclaredConstructor().newInstance();
                        supportedClass = delegate.getSupportedClass();
                    }
                    catch (Exception ex) {
                        org.openide.ErrorManager.getDefault().notify(
                            org.openide.ErrorManager.INFORMATIONAL, ex);
                        continue; // invalid - ignore
                    }
                }
                else if (LayoutManager.class.isAssignableFrom(itemClass)) {
                    // register default support for layout
                    supportedClass = itemClass;
                }

                if (supportedClass != null) {
                    Map<String,String> map;
                    if (Container.class.isAssignableFrom(supportedClass))
                        map = getContainersMap();
                    else if (LayoutManager.class.isAssignableFrom(supportedClass))
                        map = getLayoutsMap();
                    else continue; // invalid - ignore

                    String supportedClassName = supportedClass.getName();
                    if (map.get(supportedClassName) == null) {
                        String delegateClassName = delegateClass != null ?
                                                     delegateClass.getName():
                                                     DEFAULT_SUPPORT;

                        map.put(supportedClassName, delegateClassName);

                        if (supportedClassName.equals(wantedClassName))
                            foundSupportClassName = delegateClassName;
                    }
                }
            }
        }

        needPaletteRescan = false;
        return foundSupportClassName;
    }

    private Class loadClass(String className) throws ClassNotFoundException {
        return FormUtils.loadClass(className, formModelRef.get());
    }

    private static Map<String,String> getContainersMap() {
        if (containerToLayoutDelegate == null) {
            containerToLayoutDelegate = new HashMap<String,String>();
            // fill in default containers
            containerToLayoutDelegate.put(
                "javax.swing.JScrollPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JScrollPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "java.awt.ScrollPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.ScrollPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JSplitPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JSplitPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JTabbedPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JTabbedPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JToolBar", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JToolBarSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JMenuBar", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.MenuFakeSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JMenu", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.MenuFakeSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JPopupMenu", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.MenuFakeSupport"); // NOI18N
        }
        return containerToLayoutDelegate;
    }

    private static Map<String,String> getLayoutsMap() {
        if (layoutToLayoutDelegate == null) {
            layoutToLayoutDelegate = new HashMap<String,String>();
            // fill in default layouts
            layoutToLayoutDelegate.put(
                "java.awt.BorderLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.BorderLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.FlowLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.FlowLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "javax.swing.BoxLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.BoxLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "javax.swing.OverlayLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.OverlayLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.GridBagLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.GridBagLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.GridLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.GridLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.CardLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.CardLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "org.netbeans.lib.awtextra.AbsoluteLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.AbsoluteLayoutSupport"); // NOI18N
            // well known SwingX layouts
            layoutToLayoutDelegate.put("org.jdesktop.swingx.VerticalLayout", DEFAULT_SUPPORT); // NOI18N
            layoutToLayoutDelegate.put("org.jdesktop.swingx.HorizontalLayout", DEFAULT_SUPPORT); // NOI18N
        }
        return layoutToLayoutDelegate;
    }
}
