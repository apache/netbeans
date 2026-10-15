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

package org.netbeans.modules.image;

import java.beans.PropertyVetoException;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * OpenSupport flavored with some <code>CloneableEditorSupport</code> features like
 * listening on changes of image file and renames on dataobject,
 * so it can work appropriate in Editor window.
 *
 * @author Peter Zavadsky
 * @author Marian Petras
 */
public class ImageOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    /** Saves last modified time. */
    private long lastSaveTime;

    /** Listens for changes on file. */
    private FileChangeListener fileChangeL;

    /** Reloading task. */
    private Task reloadTask;


    /** Constructs ImageOpenSupportObject on given MultiDataObject.Entry. */
    public ImageOpenSupport(MultiDataObject.Entry entry) {
        super(entry, new Environment(entry.getDataObject())); // TEMP
    }


    /** Creates the CloenableTOPComponent viewer of image. */
    @Override
    public CloneableTopComponent createCloneableTopComponent () {
        prepareViewer();
        return new ImageViewer((ImageDataObject)entry.getDataObject());
    }

    /** Set listener for changes on image file. */
    void prepareViewer() {
        // listen for changes on the image file
        if(fileChangeL == null) {
            fileChangeL = new FileChangeAdapter() {
                @Override
                public void fileChanged(final FileEvent evt) {
                    if(allEditors.isEmpty()) {
                        return;
                    }

                    if(evt.getFile().isVirtual()) {
                        entry.getFile().removeFileChangeListener(this);
                        // File doesn't exist on disk - simulate env
                        // invalidation.
                        ((Environment)ImageOpenSupport.this.env).fileRemoved();
                        entry.getFile().addFileChangeListener(this);
                        return;
                    }

                    if (evt.getTime() > lastSaveTime) {
                        lastSaveTime = System.currentTimeMillis();

                        // Post in new task.
                        if(reloadTask == null || reloadTask.isFinished()) {

                            reloadTask = RequestProcessor.getDefault().post(() -> {
                                reload();
                            });
                        }
                    }
                }
            };
        }
        entry.getFile().addFileChangeListener(fileChangeL);
        lastSaveTime = System.currentTimeMillis();
    }

    void lastClosed() {
        if (fileChangeL != null) {
            entry.getFile().removeFileChangeListener(fileChangeL);
            fileChangeL = null;
        }
    }

    /** Ask and reload/close image views. */
    private void reload() {
        // ask if reload?
        // XXX the following is a resource path in NB 3.x and a URL after build system
        // merge; better to produce something nicer (e.g. FileUtil.toFile):
        String msg = NbBundle.getMessage(ImageOpenSupport.class, "MSG_ExternalChange", entry.getFile() );
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object ret = DialogDisplayer.getDefault().notify(nd);

        if (NotifyDescriptor.YES_OPTION.equals(ret)) {
            // due to compiler 1.2 bug only
            final ImageDataObject imageObj = (ImageDataObject)entry.getDataObject();
            final CloneableTopComponent.Ref editors = allEditors;

            Enumeration<CloneableTopComponent> e = editors.getComponents();
            while(e.hasMoreElements()) {
                final Object pane = e.nextElement();
                SwingUtilities.invokeLater(() -> {
                    ((ImageViewer)pane).updateView(imageObj);
                });
            }
        }
    }

    /** Environment for image open support. */
    private static class Environment extends OpenSupport.Env {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432254L;

        /** Constructor. */
        public Environment(DataObject dataObject) {
            super(dataObject);
        }


        /** Overrides superclass method. Gets from OpenCookie. */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport)getDataObject().getCookie(OpenCookie.class);
        }

        /** Called from enclosing support.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        private void fileRemoved() {
            try {
                fireVetoableChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
            } catch(PropertyVetoException pve) {
                // Ignore.
            }

            firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    } // End of nested Environment class.
}

