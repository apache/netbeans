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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author vita, Jesse Glick
 */
public final class CompoundFolderChildren implements FileChangeListener {

    public static final String PROP_CHILDREN = "FolderChildren.PROP_CHILDREN"; //NOI18N

    private static final String HIDDEN_ATTR_NAME = "hidden"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(CompoundFolderChildren.class.getName());
    
    private final String LOCK = new String("CompoundFolderChildren.LOCK"); //NOI18N
    private final List<String> prefixes;
    private final boolean includeSubfolders;
    private List<FileObject> children;
    private FileObject mergedLayers; // just hold a strong ref so listeners remain active
    private final FileChangeListener weakFCL = FileUtil.weakFileChangeListener(this, null);
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public CompoundFolderChildren(String [] paths) {
        this(paths, true);
    }
    
    public CompoundFolderChildren(String [] paths, boolean includeSubfolders) {
        prefixes = new ArrayList<String>();
        for (String path : paths) {
            prefixes.add(path.endsWith("/") ? path : path + "/"); // NOI18N
        }
        this.includeSubfolders = includeSubfolders;
        rebuild();
    }
    
    public List<FileObject> getChildren() {
        synchronized (LOCK) {
            return children;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        synchronized (LOCK) {
            List<FileObject> folders = new ArrayList<FileObject>(prefixes.size());
            List<FileSystem> layers = new ArrayList<FileSystem>(prefixes.size());
            for (final String prefix : prefixes) {
                // use system-wide configuration, ignore execution-local specifics
                FileObject layer = FileUtil.getSystemConfigFile(prefix);
                if (layer != null && layer.isFolder()) {
                    folders.add(layer);
                    try {
                        layers.add(new MultiFileSystem(new FileSystem[]{layer.getFileSystem()}) {

                            @Override
                            protected FileObject findResourceOn(FileSystem fs, String res) {
                                return fs.findResource(prefix + res);
                            }
                        });
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    // Listen to nearest enclosing parent, in case it is created.
                    // XXX would be simpler to use FileChangeSupport but that is in ant/project for now.
                    String parentPath = prefix;
                    while (true) {
                        assert parentPath.length() > 0;
                        parentPath = parentPath.substring(0, Math.max(0, parentPath.lastIndexOf('/')));
                        FileObject parent = FileUtil.getConfigFile(parentPath);
                        if (parent != null) {
                            parent.removeFileChangeListener(weakFCL);
                            parent.addFileChangeListener(weakFCL);
                            break;
                        }
                    }
                }
            }
            mergedLayers = new MultiFileSystem(layers.toArray(new FileSystem[layers.size()])).getRoot();
            mergedLayers.addFileChangeListener(this); // need not be weak since only we hold this FS
            List<FileObject> unsorted = new ArrayList<FileObject>();
            for (FileObject f : mergedLayers.getChildren()) {
                if ((includeSubfolders || f.isData()) && !Boolean.TRUE.equals(f.getAttribute(HIDDEN_ATTR_NAME))) {
                    f.addFileChangeListener(this);
                    unsorted.add(f);
                }
            }
            List<FileObject> sorted = new ArrayList<FileObject>(unsorted.size());
            for (FileObject merged : FileUtil.getOrder(unsorted, true)) {
                String name = merged.getNameExt();
                FileObject original = null;
                for (FileObject folder : folders) {
                    original = folder.getFileObject(name);
                    if (original != null) {
                        break;
                    }
                }
                assert original != null : "Should have equivalent to " + name + " among " + folders;
                sorted.add(original);
            }
            if (children != null && !sorted.equals(children)) {
                event = new PropertyChangeEvent(this, PROP_CHILDREN, children, sorted);
            }
            children = sorted;

            if (LOG.isLoggable(Level.FINE)) {
                rebuildCnt++;
                LOG.log(Level.FINE, "{0} rebuilt {1} times", new Object [] { this, rebuildCnt });
            }
        }
        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        fe.runWhenDeliveryOver(rebuildRunnable);
    }

    public void fileDataCreated(FileEvent fe) {
        fe.runWhenDeliveryOver(rebuildRunnable);
    }

    public void fileChanged(FileEvent fe) {
        fe.runWhenDeliveryOver(propChangeRunnable);
    }

    public void fileDeleted(FileEvent fe) {
        fe.runWhenDeliveryOver(rebuildRunnable);
    }

    public void fileRenamed(FileRenameEvent fe) {
        fe.runWhenDeliveryOver(rebuildRunnable);
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
//        if (FileUtil.affectsOrder(fe)) {
//            if (!filterEvents(fe)) {
//                rebuild();
//            } else {
//                rebuildTask.schedule(DELAY);
//            }
//        }
        fe.runWhenDeliveryOver(rebuildRunnable);
    }

    private final Runnable rebuildRunnable = new Runnable() {
        public void run() {
            rebuild();
        }
    };

    private final Runnable propChangeRunnable = new Runnable() {
        public void run() {
            pcs.firePropertyChange(PROP_CHILDREN, null, null);
        }
    };

//    // ignoring loads of fileAttributeChanged events fired when installing plugins
//    // see issue #161201 and #168536
//    private static final int DELAY = 1000; // milliseconds
//    private final RequestProcessor.Task rebuildTask = RequestProcessor.getDefault().create(new Runnable() {
//        public void run() {
//            synchronized (times) {
//                times.clear();
//            }
//            rebuild();
//        }
//    });
//    private final Map<String, Long> times = new HashMap<String, Long>();
    private long rebuildCnt = 0;
//    private boolean filterEvents(FileEvent event) {
//        // filter out duplicate events
//        synchronized (times) {
//            Long timestamp = times.get(event.getFile().getPath());
//            times.put(event.getFile().getPath(), event.getTime());
//            boolean filterOut = timestamp != null && Math.abs(event.getTime() - timestamp) < DELAY;
//            if (LOG.isLoggable(Level.FINER)) {
//                if (filterOut) {
//                    LOG.fine("Filtering out filesystem event: [" + printEvent(event) + "]"); //NOI18N
//                } else {
//                    LOG.fine("Processing filesystem event: [" + printEvent(event) + "]"); //NOI18N
//                }
//            }
//            return filterOut;
//
//        }
//    }
//
//    private static String printEvent(FileEvent event) {
//        return event.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(event)) //NOI18N
//                + ", ts=" + event.getTime() //NOI18N
//                + ", path=" + event.getFile().getPath(); //NOI18N
//    }
}
