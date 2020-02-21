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

package org.netbeans.modules.cnd.makeproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;

/**
 * SourcesHelper does not work with pure FileObjects, it demands that FileUtil.toFile() is not null.
 * So we have to create an implementation of our own
 */
public class FileObjectBasedSources implements Sources, FileChangeListener {

    private final ChangeSupport cs = new ChangeSupport(this);
    private boolean haveAttachedListeners;
    private final Set<CharSequence> rootsListenedTo = new HashSet<>();
    private final Map<String, List<SourceGroup>> groups = new HashMap<>();
    /**
     * The root URLs which were computed last, keyed by group type.
     */
    private final Map<String,List<CharSequence>> lastComputedRoots = new ConcurrentHashMap<>();

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        synchronized (this) {
            List<SourceGroup> l = groups.get(type);
            SourceGroup[] result = (l == null) ? new SourceGroup[0] : l.toArray(new SourceGroup[l.size()]);
            // Remember what we computed here so we know whether to fire changes later.
            List<CharSequence> rootURLs = new ArrayList<>(groups.size());
            for (SourceGroup g : result) {
                rootURLs.add(CndFileUtils.fileObjectToUrl(g.getRootFolder()));
            }
            lastComputedRoots.put(type, rootURLs);
            return result;
        }
    }

    public SourceGroup addGroup(Project project, String type, FileObject fo, String displayName) {
        synchronized (this) {
            if (rootsListenedTo.add(CndFileUtils.fileObjectToUrl(fo)) && haveAttachedListeners) {
                fo.addFileChangeListener(this);
            }
            List<SourceGroup> l = groups.get(type);
            if (l == null) {
                l = new ArrayList<>();
                groups.put(type, l);
            }
            SourceGroup group = GenericSources.group(project, fo, fo.getPath(), displayName, null, null);
            l.add(group);
            return group;
        }
    }

    @Override
    public synchronized void addChangeListener(ChangeListener listener) {
        if (!haveAttachedListeners) {
            haveAttachedListeners = true;
            for (CharSequence url : rootsListenedTo) {
                FileObject fo = CndFileUtils.urlToFileObject(url);
                if (fo != null && fo.isValid()) {
                    fo.addFileChangeListener(this);
                }
            }
        }
        cs.addChangeListener(listener);
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
        if (!cs.hasListeners()) {
            if (haveAttachedListeners) {
                haveAttachedListeners = false;
                for (CharSequence url : rootsListenedTo) {
                    FileObject fo = CndFileUtils.urlToFileObject(url);
                    if (fo != null && fo.isValid()) {
                        fo.removeFileChangeListener(this);
                    }
                }
            }
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        // Root might have been created on disk.
        maybeFireChange();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        maybeFireChange();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        // Root might have been deleted.
        maybeFireChange();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        // ignore; generally should not happen (listening to dirs)
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        maybeFireChange();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // #164930 - ignore
    }

    private void maybeFireChange() {
        boolean change = false;
        // Cannot iterate over entrySet, as the map will be modified by getSourceGroups.
        for (String type : new HashSet<>(lastComputedRoots.keySet())) {
            List<CharSequence> previous = new ArrayList<>(lastComputedRoots.get(type));
            getSourceGroups(type);
            List<CharSequence> nue = lastComputedRoots.get(type);
            if (!nue.equals(previous)) {
                change = true;
                break;
            }
        }
        if (change) {
            cs.fireChange();
        }
    }    
}
