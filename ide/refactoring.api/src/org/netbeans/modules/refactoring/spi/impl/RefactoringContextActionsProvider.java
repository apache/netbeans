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

package org.netbeans.modules.refactoring.spi.impl;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.awt.AcceleratorBinding;
import org.openide.awt.Actions;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Translates files registered under {@code Editors/<mime-type>/RefactoringActions/}
 * to array of menu items.
 * <p>Usage: {@code MimeLookup.getLookup(mimepath).lookup(RefactoringContextActionsProvider.class).}
 * </p>
 * @author Jan Pokorsky
 */
@MimeLocation(subfolderName="RefactoringActions", instanceProviderClass=RefactoringContextActionsProvider.class)
public final class RefactoringContextActionsProvider
        implements InstanceProvider<RefactoringContextActionsProvider> {

    private static final Logger LOG = Logger.getLogger(RefactoringContextActionsProvider.class.getName());

    private final List<FileObject> fileObjectList;
    private JComponent[] menuItems;

    public RefactoringContextActionsProvider() {
        fileObjectList = Collections.emptyList();
    }

    public RefactoringContextActionsProvider(List<FileObject> fileObjectList) {
        this.fileObjectList = fileObjectList;
    }

    @Override
    public RefactoringContextActionsProvider createInstance(List<FileObject> fileObjectList) {
        return new RefactoringContextActionsProvider(fileObjectList);
    }

    /**
      * @deprecated use @see #getMenuItems(boolean,Lookup)
      */
    @Deprecated
    public JComponent[] getMenuItems(boolean reset) {
        return getMenuItems(reset, null);
    }

    public JComponent[] getMenuItems(boolean reset, Lookup context) {
        assert EventQueue.isDispatchThread();
        if (menuItems == null || reset) {
            List<JComponent> l = createMenuItems(context);
            menuItems = l.toArray(new JComponent[0]);
        }

        return menuItems;
    }

    private List<JComponent> createMenuItems(Lookup context) {
        if (fileObjectList.isEmpty()) {
            return Collections.emptyList();
        }

        List <JComponent> result = new ArrayList<JComponent>(fileObjectList.size() + 1);
        result.addAll(retrieveMenuItems(fileObjectList, context));

        if (!result.isEmpty()) {
            // add separator at beginning of the context menu
            if (result.get(0) instanceof JSeparator) {
                result.set(0, null);
            } else {
                result.add(0, null);
            }
        }

        return result;
    }

    private static void resolveInstance(Object instance, List<JComponent> result) throws IOException {
        if (instance instanceof Presenter.Popup) {
            JMenuItem temp = ((Presenter.Popup) instance).getPopupPresenter();
            result.add(temp);
        } else if (instance instanceof JSeparator) {
            result.add(null);
        } else if (instance instanceof JComponent) {
            result.add((JComponent) instance);
        } else if (instance instanceof Action) {
            Actions.MenuItem mi = new Actions.MenuItem((Action) instance, true);
            result.add(mi);
        } else {
            throw new IOException(String.format("Unsupported instance: %s, class: %s", instance, instance.getClass())); // NOI18N
        }
     }

    private List<JComponent> retrieveMenuItems(List<FileObject> files, Lookup context) {
        List<JComponent> result = new LinkedList<JComponent>();
        
        for (FileObject fo : files) {
            try {
                if(fo.isFolder()) {
                    DataFolder dobj = DataFolder.findFolder(fo);
                    List<FileObject> children = Arrays.asList(fo.getChildren());
                    children = FileUtil.getOrder(children, false);
                    final String displayName = (String) fo.getAttribute("displayName"); //NOI18N
                    JMenu subMenu = new JMenu(displayName != null? displayName : dobj.getName());
                    for (JComponent jComponent : retrieveMenuItems(children, context)) {
                        subMenu.add(jComponent);
                    }
                    result.add(subMenu);
                } else {
                    DataObject dobj = DataObject.find(fo);
                    InstanceCookie ic = dobj.getLookup().lookup(InstanceCookie.class);
                    if (ic != null) {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof Action) { // #201397
                            AcceleratorBinding.setAccelerator((Action) instance, fo);
                        }
                        if(instance instanceof ContextAwareAction) {
                            instance = ((ContextAwareAction)instance).createContextAwareInstance(context);
                        }
                        resolveInstance(instance, result);
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.WARNING, fo.toString(), ex);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, fo.toString(), ex);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, fo.toString(), ex);
            }
        }
        return result;
    }
}
