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
package org.netbeans.api.project.libraries;


import java.awt.Dialog;
import javax.swing.border.EmptyBorder;
import static org.netbeans.api.project.libraries.Bundle.*;
import org.netbeans.modules.project.libraries.ui.NewLibraryPanel;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/** Provides method for opening Libraries customizer
 *
 */
public final class LibrariesCustomizer {

    private LibrariesCustomizer () {
    }

    /**
     * Shows libraries customizer for given library manager.
     * @param activeLibrary if not null the activeLibrary is selected in the opened customizer
     * @return true if user pressed OK and libraries were successfully modified
     */
    @Messages("TXT_LibrariesManager=Ant Library Manager")
    public static boolean showCustomizer (Library activeLibrary, LibraryManager libraryManager) {
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (
                        LibrariesSupport.getLibraryStorageArea(libraryManager));
        customizer.setBorder(new EmptyBorder(12, 12, 0, 12));
        if (activeLibrary != null) {
            customizer.setSelectedLibrary (LibrariesSupport.getLibraryImplementation(activeLibrary));
        }
        DialogDescriptor descriptor = new DialogDescriptor(customizer, TXT_LibrariesManager());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        setAccessibleDescription(dlg, customizer.getAccessibleContext().getAccessibleDescription());
        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return customizer.apply();
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }

    /**
     * Shows libraries customizer for global libraries.
     * @param activeLibrary if not null the activeLibrary is selected in the opened customizer
     * @return true if user pressed OK and libraries were successfully modified
     */
    public static boolean showCustomizer (Library activeLibrary) {
        return showCustomizer(activeLibrary, LibraryManager.getDefault());
    }
    
    /**
     * Show customizer for creating new library in the given library manager.
     * @param manager manager
     * @return created persisted library or null if user cancelled operation
     * @since org.netbeans.modules.project.libraries/1 1.16
     */
    @Messages("LibrariesCustomizer.createLibrary.title=Create New Library")
    public static Library showCreateNewLibraryCustomizer(LibraryManager manager) {                                             
        if (manager == null) {
            manager = LibraryManager.getDefault();
        }
        LibraryStorageArea area = LibrariesSupport.getLibraryStorageArea(manager);
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (area);
        NewLibraryPanel p = new NewLibraryPanel(customizer.getModel(), null, area);
        DialogDescriptor dd = new DialogDescriptor(p, LibrariesCustomizer_createLibrary_title(),
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        p.setDialogDescriptor(dd);
        Dialog dlg = DialogDisplayer.getDefault().createDialog (dd);
        setAccessibleDescription(dlg, customizer.getAccessibleContext().getAccessibleDescription());
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            final String currentLibraryName = p.getLibraryName();
            final String antLibraryName = 
                org.netbeans.modules.project.libraries.ui.LibrariesCustomizer.createFreeAntLibraryName(
                    currentLibraryName,
                    customizer.getModel(),
                    area);
            LibraryImplementation impl;
            if (area != LibraryStorageArea.GLOBAL) {
                impl = customizer.getModel().createArealLibrary(
                        p.getLibraryType(),
                        antLibraryName,
                        LibrariesSupport.getLibraryStorageArea(manager));
            } else {
                LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(p.getLibraryType());
                if (provider == null) {
                    return null;
                }
                impl = provider.createLibrary();
                impl.setName(antLibraryName);
            }
            LibrariesSupport.setDisplayName(impl, currentLibraryName);
            customizer.getModel().addLibrary(impl);
            customizer.forceTreeRecreation();
            if (customizeLibrary(customizer, impl)) {
                return manager.getLibrary(impl.getName());
            }
        }
        return null;
    }

    /**
     * Show library customizer for the given library.
     * @param library library
     * @return true if library was modified or not
     * @since org.netbeans.modules.project.libraries/1 1.16
     */
    public static boolean showSingleLibraryCustomizer(Library library) {
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer (
                        LibrariesSupport.getLibraryStorageArea(library.getManager()));
        return customizeLibrary(customizer, LibrariesSupport.getLibraryImplementation(library));
    }
    
    @Messages("LibrariesCustomizer.customizeLibrary.title=Customize Library")
    private static boolean customizeLibrary(org.netbeans.modules.project.libraries.ui.LibrariesCustomizer customizer,
            LibraryImplementation activeLibrary) {
        customizer.hideLibrariesList();
        customizer.setBorder(new EmptyBorder(12, 8, 0, 10));
        customizer.setSelectedLibrary (activeLibrary);
        DialogDescriptor descriptor = new DialogDescriptor(customizer, LibrariesCustomizer_customizeLibrary_title());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        setAccessibleDescription(dlg, customizer.getAccessibleContext().getAccessibleDescription());
        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                customizer.apply();
                return true;
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }

    private static void setAccessibleDescription(final Dialog dlg, final String description ) {
        if (description != null) {
            dlg.getAccessibleContext().setAccessibleDescription(description);
        }
    }
    
}

