/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.BaseUtilities;

/** Holds FileChangeListener and File pair and handle movement of auxiliary
 * FileChangeListener to the first existing upper folder and firing appropriate events.
 */
final class FileChangeImpl extends WeakReference<FileChangeListener> implements FileChangeListener, Runnable {
    /** Contains mapping of FileChangeListener to File. */
    private static final Map<FileChangeListener,Map<File,FileChangeImpl>> holders = new WeakHashMap<FileChangeListener,Map<File,FileChangeImpl>>();

    private final File path;
    private FileObject current;
    private File currentF;
    /** Whether listener is seeded on target path. */
    private boolean isOnTarget = false;

    public FileChangeImpl(FileChangeListener listener, File path) {
        super(listener, BaseUtilities.activeReferenceQueue());
        assert path != null;
        this.path = path;
    }

    void locateCurrent() {
        FileObject oldCurrent = current;
        currentF = FileUtil.normalizeFile(path);
        while (true) {
            current = FileUtil.toFileObject(currentF);
            if (current != null) {
                isOnTarget = path.equals(currentF);
                break;
            }
            currentF = currentF.getParentFile();
            if (currentF == null) {
                // #47320: can happen on Windows in case the drive does not exist.
                // (Inside constructor for Holder.) In that case skip it.
                return;
            }
        }
        assert current != null;
        if (current != oldCurrent) {
            if (oldCurrent != null) {
                oldCurrent.removeFileChangeListener(this);
            }
            current.addFileChangeListener(this);
            current.getChildren(); //to get events about children
        }
    }

    private void someChange() {
        FileChangeListener listener;
        boolean wasOnTarget;
        FileObject currentNew;
        synchronized (this) {
            if (current == null) {
                return;
            }
            listener = get();
            if (listener == null) {
                return;
            }
            wasOnTarget = isOnTarget;
            locateCurrent();
            currentNew = current;
        }
        if (isOnTarget && !wasOnTarget) {
            // fire events about itself creation (it is difference from FCL
            // on FileOject - it cannot be fired because we attach FCL on already existing FileOject
            if (currentNew.isFolder()) {
                listener.fileFolderCreated(new FileEvent(currentNew));
            } else {
                listener.fileDataCreated(new FileEvent(currentNew));
            }
        }
    }

    public void fileChanged(FileEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener instanceof DeepListener) {
                    ((DeepListener) listener).fileChanged(fe, true);
                } else if (listener != null) {
                    listener.fileChanged(fe);
                }
            } else {
                someChange();
            }
        }
    }

    public void fileDeleted(FileEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener instanceof DeepListener) {
                    ((DeepListener) listener).fileDeleted(fe, true);
                } else if (listener != null) {
                    listener.fileDeleted(fe);
                }
            }
            someChange();
        }
    }

    public void fileDataCreated(FileEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener instanceof DeepListener) {
                    ((DeepListener) listener).fileDataCreated(fe, true);
                } else if (listener != null) {
                    listener.fileDataCreated(fe);
                }
            } else {
                someChange();
            }
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener instanceof DeepListener) {
                    ((DeepListener) listener).fileFolderCreated(fe, true);
                } else if (listener != null) {
                    listener.fileFolderCreated(fe);
                }
            } else {
                someChange();
            }
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener instanceof DeepListener) {
                    ((DeepListener) listener).fileRenamed(fe, true);
                } else if (listener != null) {
                    listener.fileRenamed(fe);
                }
            }
            someChange();
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (fe.getSource() == current) {
            if (isOnTarget) {
                FileChangeListener listener = get();
                if (listener != null) {
                    listener.fileAttributeChanged(fe);
                }
            }
        }
    }

    @Override
    public synchronized void run() {
        if (current != null) {
            current.removeFileChangeListener(this);
            current = null;
        }
    }

    static void addFileChangeListenerImpl(Logger logger, FileChangeListener listener, File path) {
        assert FileUtil.assertNormalized(path);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addFileChangeListener {0} @ {1}", new Object[]{listener, path});
        }
        final FileChangeImpl holder;
        synchronized (holders) {
            Map<File, FileChangeImpl> f2H = holders.get(listener);
            if (f2H == null) {
                f2H = new HashMap<File, FileChangeImpl>();
                holders.put(listener, f2H);
            }
            final FileChangeImpl prev = f2H.get(path);
            if (prev != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Already listening to ").append(path);
                sb.append("\nnew listener   : ").append(listener);
                sb.append("\nholder listener: ").append(prev.get());
                throw new IllegalArgumentException(sb.toString());
            }
            holder = new FileChangeImpl(listener, path);
            f2H.put(path, holder);
        }
        holder.locateCurrent();
    }

    static FileChangeListener removeFileChangeListenerImpl(Logger logger, FileChangeListener listener, File path) {
        assert FileUtil.assertNormalized(path, BaseUtilities.isMac());
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "removeFileChangeListener {0} @ {1}", new Object[]{listener, path});
        }
        synchronized (holders) {
            Map<File, FileChangeImpl> f2H = holders.get(listener);
            if (f2H == null) {
                throw new IllegalArgumentException("Was not listening to " + path);
            }
            if (!f2H.containsKey(path)) {
                throw new IllegalArgumentException(listener + " was not listening to " + path + "; only to " + f2H.keySet());
            }
            FileChangeImpl h = f2H.remove(path);
            if (f2H.isEmpty()) {
                holders.remove(listener);
            }
            h.run();
            return h.get();
        }
    }
    
    static DeepListener addRecursiveListener(FileChangeListener listener, File path, FileFilter recurseInto, Callable<Boolean> stop) {
        final DeepListener deep = new DeepListener(listener, path, recurseInto, stop);
        deep.init();
        FileChangeImpl.addFileChangeListenerImpl(DeepListener.LOG, deep, path);
        return deep;
    }

    static void removeRecursiveListener(FileChangeListener listener, File path) {
        final DeepListener deep = new DeepListener(listener, path, null, null);
        // no need to deep.init()
        DeepListener dl = (DeepListener)FileChangeImpl.removeFileChangeListenerImpl(DeepListener.LOG, deep, path);
        dl.run();
    }
}
