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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
     * @return true if user pressed OK and libraries were sucessfully modified
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
     * @return true if user pressed OK and libraries were sucessfully modified
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

