/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
