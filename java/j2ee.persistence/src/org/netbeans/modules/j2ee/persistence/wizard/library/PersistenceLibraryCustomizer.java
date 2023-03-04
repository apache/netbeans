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

package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceLibraryCustomizer {
    
    private PersistenceLibraryCustomizer() {
    }
    
    public static boolean showCustomizer() {
        LibraryImplementation libImpl = LibrariesSupport.createLibraryImplementation(PersistenceLibrarySupport.LIBRARY_TYPE, PersistenceLibrarySupport.VOLUME_TYPES);
        PersistenceLibraryPanel customizer = new PersistenceLibraryPanel(libImpl);
        final DialogDescriptor descriptor = new DialogDescriptor(customizer,NbBundle.getMessage(PersistenceLibraryCustomizer.class, "TXT_PersistenceLibrariesManager"));
        customizer.addPropertyChangeListener( (PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals(PersistenceLibraryPanel.IS_VALID)) {
                Object newvalue = evt.getNewValue();
                if (newvalue instanceof Boolean) {
                    descriptor.setValid(((Boolean)newvalue));
                }
            }
        });
        customizer.checkValidity();
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                customizer.apply();
                return true;
            }
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return false;
    }
    
}
