/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
    public JComponent[] getMenuItems(boolean reset) {
        return getMenuItems(reset, null);
    }

    public JComponent[] getMenuItems(boolean reset, Lookup context) {
        assert EventQueue.isDispatchThread();
        if (menuItems == null || reset) {
            List<JComponent> l = createMenuItems(context);
            menuItems = l.toArray(new JComponent[l.size()]);
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
