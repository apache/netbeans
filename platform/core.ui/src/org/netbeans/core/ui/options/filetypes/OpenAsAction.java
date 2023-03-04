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

package org.netbeans.core.ui.options.filetypes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/** Open the selected file with choosen MIME type and store the choice.
 * 
 * @author Jiri Skrivanek
 */
@ActionID(id = "org.netbeans.core.ui.options.filetypes.OpenAsAction", category = "Edit")
@ActionRegistration(
    displayName = "#OpenAsAction.name",
    iconBase="org/netbeans/core/ui/options/filetypes/openFileAs.png"
)
@ActionReference(path = "Loaders/content/unknown/Actions", position = 150)
public final class OpenAsAction implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(OpenAsAction.class.getName());
    private final DataObject dob;

    public OpenAsAction(DataObject obj) {
        this.dob = obj;
    }
    
    

    /** Opens a dialog with list of available MIME types and when user selects one
     * and clicks OK, it opens the file in editor.
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        OpenAsPanel openAsPanel = new OpenAsPanel();
        FileAssociationsModel model = new FileAssociationsModel();
        openAsPanel.setModel(model);
        FileObject fo = dob.getPrimaryFile();
        String extension = fo.getExt();
        openAsPanel.setExtension(extension);
        
        String openLabel = NbBundle.getMessage(NewExtensionPanel.class, "OpenAsPanel.open"); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(openAsPanel,
                NbBundle.getMessage(OpenAsPanel.class, "OpenAsPanel.title"), //NOI18N
                true, new Object[] {openLabel, DialogDescriptor.CANCEL_OPTION}, openLabel,
                DialogDescriptor.DEFAULT_ALIGN, null, null);

        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (openLabel.equals(dd.getValue())) {
            String mimeType = openAsPanel.getMimeType();
            if (mimeType != null) {
                dob.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(DataObject.PROP_VALID) && !(Boolean)evt.getNewValue()) {
                            LOGGER.log(Level.FINE, "PROP_VALID {0} - {1}", new Object[]{evt.getNewValue(), evt});
                            try {
                                // find a new DataObject and try to open it
                                OpenCookie openCookie = DataObject.find(dob.getPrimaryFile()).getCookie(OpenCookie.class);
                                if(openCookie != null) {
                                    openCookie.open();
                                }
                            } catch (DataObjectNotFoundException ex) {
                                LOGGER.log(Level.INFO, null, ex);
                            } finally {
                                dob.removePropertyChangeListener(this);
                            }
                        } else {
                            dob.removePropertyChangeListener(this);
                        }
                    }
                });
                model.setMimeType(extension, mimeType);
                model.store();
                try {
                    dob.setValid(false);
                } catch (PropertyVetoException ex) {
                    LOGGER.log(Level.INFO, "Can't convert", ex); // NOI18N
                    final Message nd = new Message(
                        NbBundle.getMessage(OpenAsAction.class, "ERR_CantConvert", fo.getPath(), mimeType),
                        NotifyDescriptor.ERROR_MESSAGE
                    );
                    DialogDisplayer.getDefault().notify(nd);
                    return;
                }
                try {
                    // open always - if was choosen MIME type with default loader and propertyChange PROP_VALID is never fired
                    OpenCookie openCookie;
                    openCookie = DataObject.find(fo).getLookup().lookup(OpenCookie.class);
                    if(openCookie != null) {
                        openCookie.open();
                        return;
                    }
                } catch (DataObjectNotFoundException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
                final Message nd = new Message(
                    NbBundle.getMessage(OpenAsAction.class, "ERR_CantOpen", fo.getPath()),
                    NotifyDescriptor.ERROR_MESSAGE
                );
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }
}

