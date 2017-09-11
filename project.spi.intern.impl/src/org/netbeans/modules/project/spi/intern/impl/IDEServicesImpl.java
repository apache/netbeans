/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.spi.intern.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProvider(service=ProjectIDEServicesImplementation.class, position=100)
public class IDEServicesImpl implements ProjectIDEServicesImplementation {
    
    @Override
    public Icon loadIcon(String resource, boolean localized) {
        return ImageUtilities.loadImageIcon(resource, localized);
    }

    @Override
    public FileBuiltQuerySource createFileBuiltQuerySource(FileObject file) {
        try {
            DataObject source = DataObject.find(file);
            return new DataObjectSource(source);
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(IDEServicesImpl.class.getName()).log(Level.FINE, null, e);
            return null;
        }   
    }

    @Override
    public boolean isEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread();
    }

    private static class DataObjectSource implements FileBuiltQuerySource {
        private final DataObject data;
        private final PropertyChangeSupport support;
        private PropertyChangeListener propertyChangeListener;
        public DataObjectSource(DataObject data) {
            this.data = data;
            support = new PropertyChangeSupport(this);
            propertyChangeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                        support.firePropertyChange(PROP_MODIFIED, evt.getOldValue(), evt.getNewValue());
                    }
                }
            };
            data.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, this.data));
        }
        
        @Override
        public boolean isModified() {
            return data.isModified();
        }

        @Override
        public boolean isValid() {
            return data.isValid();
        }

        @Override
        public FileObject getFileObject() {
            return data.getPrimaryFile();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }
        
    }
    
    @Override
    public boolean isUserQuestionException(IOException ioe) {
        return ioe instanceof UserQuestionException;
    }

    @NbBundle.Messages("TITLE_CannotWriteFile=Cannot Write to File")
    @Override
    public void handleUserQuestionException(final IOException ioe, final UserQuestionExceptionCallback callback) {
        if(ioe instanceof UserQuestionException) {
            final UserQuestionException e = (UserQuestionException) ioe;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(
                        e.getLocalizedMessage(),
                        Bundle.TITLE_CannotWriteFile(),
                        NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                        try {
                            e.confirmed();
                            callback.accepted();
                        } catch (IOException x) {
                            callback.error(x);
                        }
                    } else {
                        callback.denied();
                    }
                }
            });
        }
    }

    @Override
    public void notifyWarning(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE));
    }
}
