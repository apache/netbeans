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
package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.CheckForUpdatesProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/** Runnable to install missing modules or activate
 * new ones.
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class InstallOrActivateTask implements Runnable, FileChangeListener {
    private final ModulesInstaller installer;
    private final ModulesActivator activator;
    private final List<FileObject> changed = new ArrayList<FileObject>();

    InstallOrActivateTask(ModulesInstaller installer) {
        this.installer = installer;
        this.activator = null;
    }

    InstallOrActivateTask(ModulesActivator activator) {
        this.activator = activator;
        this.installer = null;
    }

    public void run() {
        FileObject fo = FileUtil.getConfigFile("Modules"); // NOI18N
        try {
            if (fo != null) {
                fo.addFileChangeListener(this);
            }
            if (installer != null) {
                installer.installMissingModules();
            }
            if (activator != null) {
                activator.enableModules();
            }
        } finally {
            if (fo != null) {
                fo.removeFileChangeListener(this);
                FeatureManager.associateFiles(getChangedFiles());
            }
            notifyUpdates();
        }
    }
    
    private void notifyUpdates() {
        assert ! EventQueue.isDispatchThread() : "Don't call it from event dispatch thread.";
        CheckForUpdatesProvider checkForUpdatesProvider = Lookup.getDefault().lookup(CheckForUpdatesProvider.class);
        if (checkForUpdatesProvider == null) {
            Logger.getLogger(InstallOrActivateTask.class.getName()).log(Level.WARNING, "CheckForUpdatesProvider not found in Lookup.getDefault(): " + Lookup.getDefault());
            return;
        }
        boolean anyUpdates = checkForUpdatesProvider.notifyAvailableUpdates(false);
        Logger.getLogger(InstallOrActivateTask.class.getName()).log(Level.FINE, "Any updates? " + anyUpdates);
    }

    public void fileFolderCreated(FileEvent fe) {
    }

    public synchronized void fileDataCreated(FileEvent fe) {
        changed.add(fe.getFile());
    }

    public synchronized void fileChanged(FileEvent fe) {
        changed.add(fe.getFile());
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
    
    private synchronized List<FileObject> getChangedFiles() {
        return new ArrayList<FileObject>(changed);
    }
}
