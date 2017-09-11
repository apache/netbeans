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

package org.netbeans.modules.spring.util.fcs;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import org.openide.filesystems.FileObject;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

// XXX current implementation is not efficient for listening to a large # of files

/**
 * Utility class to notify clients of changes in the existence or timestamp
 * of a named file or directory.
 * Unlike the Filesystems API, permits you to listen to a file which does not
 * yet exist, or continue listening to it after it is deleted and recreated, etc.
 * @author Jesse Glick
 * @see "Blockers: #44213, #44628, #42147, etc."
 * @see "#33162: hierarchical listeners"
 */
public final class FileChangeSupport {
    
    public static final FileChangeSupport DEFAULT = new FileChangeSupport();
    
    private FileChangeSupport() {}
    
    private final Map<FileChangeSupportListener,Map<File,Holder>> holders = new WeakHashMap<FileChangeSupportListener,Map<File,Holder>>();
    
    /**
     * Add a listener to changes in a given path.
     * Can only add a given listener x path pair once.
     * However a listener can listen to any number of paths.
     * Note that listeners are always held weakly - if the listener is collected,
     * it is quietly removed.
     */
    public void addListener(FileChangeSupportListener listener, File path) {
        assert path.equals(FileUtil.normalizeFile(path)) : "Need to normalize " + path + " before passing to FCS!";
        synchronized (holders) {
            Map<File,Holder> f2H = holders.get(listener);
            if (f2H == null) {
                f2H = new HashMap<File,Holder>();
                holders.put(listener, f2H);
            }
            if (f2H.containsKey(path)) {
                throw new IllegalArgumentException("Already listening to " + path); // NOI18N
            }
            f2H.put(path, new Holder(listener, path));
        }
    }
    
    /**
     * Remove a listener to changes in a given path.
     */
    public void removeListener(FileChangeSupportListener listener, File path) {
        assert path.equals(FileUtil.normalizeFile(path)) : "Need to normalize " + path + " before passing to FCS!";
        synchronized (holders) {
            Map<File,Holder> f2H = holders.get(listener);
            if (f2H == null) {
                throw new IllegalArgumentException("Was not listening to " + path); // NOI18N
            }
            if (!f2H.containsKey(path)) {
                throw new IllegalArgumentException(listener + " was not listening to " + path + "; only to " + f2H.keySet()); // NOI18N
            }
            f2H.remove(path);
        }
    }
    
    private static final class Holder extends WeakReference<FileChangeSupportListener> implements FileChangeListener, Runnable {
        
        private final File path;
        private FileObject current;
        private File currentF;
        
        public Holder(FileChangeSupportListener listener, File path) {
            super(listener, Utilities.activeReferenceQueue());
            assert path != null;
            this.path = path;
            locateCurrent();
        }
        
        private void locateCurrent() {
            FileObject oldCurrent = current;
            currentF = path;
            while (true) {
                try {
                    current = FileUtil.toFileObject(currentF);
                } catch (IllegalArgumentException x) {
                    // #73526: was originally normalized, but now is not. E.g. file changed case.
                    currentF = FileUtil.normalizeFile(currentF);
                    current = FileUtil.toFileObject(currentF);
                }
                if (current != null) {
                    break;
                }
                currentF = currentF.getParentFile();
                if (currentF == null) {
                    // #47320: can happen on Windows in case the drive does not exist.
                    // (Inside constructor for Holder.) In that case skip it.
                    return;
                }
            }
            // XXX what happens with UNC paths?
            assert current != null;
            if (current != oldCurrent) {
                if (oldCurrent != null) {
                    oldCurrent.removeFileChangeListener(this);
                }
                current.addFileChangeListener(this);
                current.getChildren();//to get events about children
            }
        }

        private void someChange(FileObject modified) {
            FileChangeSupportListener listener;
            FileObject oldCurrent, nueCurrent;
            File oldCurrentF, nueCurrentF;
            synchronized (this) {
                if (current == null) {
                    return;
                }
                listener = get();
                if (listener == null) {
                    return;
                }
                oldCurrent = current;
                oldCurrentF = currentF;
                locateCurrent();
                nueCurrent = current;
                nueCurrentF = currentF;
            }
            if (modified != null && modified == nueCurrent) {
                FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_MODIFIED, path);
                listener.fileModified(event);
            } else {
                boolean oldWasCorrect = path.equals(oldCurrentF);
                boolean nueIsCorrect = path.equals(nueCurrentF);
                if (oldWasCorrect && !nueIsCorrect) {
                    FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_DELETED, path);
                    listener.fileDeleted(event);
                } else if (nueIsCorrect && !oldWasCorrect) {
                    FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_CREATED, path);
                    listener.fileCreated(event);
                }
            }
        }

        public void fileChanged(FileEvent fe) {
            someChange(fe.getFile());
        }
        
        public void fileDeleted(FileEvent fe) {
            someChange(null);
        }

        public void fileDataCreated(FileEvent fe) {
            someChange(null);
        }

        public void fileFolderCreated(FileEvent fe) {
            someChange(null);
        }

        public void fileRenamed(FileRenameEvent fe) {
            someChange(null);
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
        public synchronized void run() {
            if (current != null) {
                current.removeFileChangeListener(this);
                current = null;
            }
        }

    }
    
}
