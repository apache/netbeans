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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * Abstract deep directory file listener on list of files, existent or not.
 *
 * @author nn136682
 * @author Andrei Badea
 */
public abstract class AbstractFilesListener {
    protected J2eeModuleProvider provider;
    private HashMap fileListeners = new HashMap();
    
    private FileChangeListener listener = new FileListener();
    
    /** Creates a new instance of AbstractFilesListener */
    public AbstractFilesListener(J2eeModuleProvider provider) {
        this.provider = provider;
        startListening();
    }
    
    protected abstract File[] getTargetFiles();
    protected abstract boolean isTarget(FileObject fo);
    protected abstract boolean isTarget(String fileName);
    protected abstract void targetCreated(FileObject fo);
    protected abstract void targetDeleted(FileObject fo);
    protected abstract void targetChanged(FileObject fo);
    
    private synchronized void startListening() {
        File[] targets = getTargetFiles();
        for (int i=0; i<targets.length; i++) {
            startListening(targets[i]);
        }
    }
    public synchronized void stopListening() {
        for (Iterator i = fileListeners.keySet().iterator(); i.hasNext();) {
            FileObject fo = (FileObject) i.next();
            removeFileListenerFrom(fo);
        }
    }
    private void startListening(File target) {
        if (!target.isAbsolute()) {
            // workaround for issue 84872. Should be removed when
            // issue 85132 is addressed.
            return;
        }
        FileObject targetFO = FileUtil.toFileObject(target);
        while (targetFO == null) {
            target = target.getParentFile();
            if (target == null)
                return;
            targetFO = FileUtil.toFileObject(target);
        }
        if (!fileListeners.containsKey(targetFO)) {
            addFileListenerTo(targetFO);
        }
    }
    
    private void addFileListenerTo(FileObject fo) {
        FileChangeListener l = FileUtil.weakFileChangeListener(listener, fo);
        fileListeners.put(fo, l);
        fo.addFileChangeListener(l);
        
    }
    
    private void removeFileListenerFrom(FileObject fo) {
        FileChangeListener l = (FileChangeListener)fileListeners.remove(fo);
        if (l != null) {
            fo.removeFileChangeListener(l);
        }
    }
    
    private final class FileListener implements FileChangeListener {
        
        public void fileFolderCreated(FileEvent e) {
            startListening();
        }
        public void fileDeleted(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    removeFileListenerFrom(fo);
                }
                targetDeleted(fo);
            }
            startListening();
        }
        public void fileDataCreated(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    addFileListenerTo(fo);
                }
                targetCreated(fo);
            }
        }
        public void fileRenamed(FileRenameEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    if (!fileListeners.containsKey(fo)) {
                        addFileListenerTo(fo);
                    }
                }
                targetCreated(fo);
            } else {
                if (isTarget(e.getName() + "." + e.getExt())) {
                    synchronized(fileListeners) {
                        removeFileListenerFrom(fo);
                    }
                    targetDeleted(fo);
                }
            }
            startListening();
        }

        public void fileAttributeChanged(FileAttributeEvent e) {};

        public void fileChanged(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                fo.refresh(true);
                targetChanged(fo);
            }
        }
    }
}
