/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
