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
