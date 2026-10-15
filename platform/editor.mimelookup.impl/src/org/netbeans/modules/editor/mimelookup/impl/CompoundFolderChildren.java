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
            mergedLayers = new MultiFileSystem(layers.toArray(new FileSystem[0])).getRoot();
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
