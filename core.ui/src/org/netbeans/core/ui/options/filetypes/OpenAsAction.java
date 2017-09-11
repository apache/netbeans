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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

