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

package org.netbeans.modules.project.ui.groups;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * All projects which can be found beneath some directory, with an optional main project.
 * @author Jesse Glick
 */
public class DirectoryGroup extends Group {

    private static final Logger LOG = Logger.getLogger(DirectoryGroup.class.getName());

    static final String KIND = "directory"; // NOI18N

    /**
     * Create a new group derived from a directory.
     */
    public static DirectoryGroup create(String name, FileObject dir) {
        String path = dir.toURL().toExternalForm();
        String id = sanitizeNameAndUniquifyForId(name);
        LOG.log(Level.FINE, "Creating: {0}", id);
        Preferences p = NODE.node(id);
        p.put(KEY_NAME, name);
        p.put(KEY_KIND, KIND);
        p.put(KEY_PATH, path);
        return new DirectoryGroup(id);
    }

    DirectoryGroup(String id) {
        super(id);
    }

    @Override
    protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        assert !SwingUtilities.isEventDispatchThread(); 
        String dir = prefs().get(KEY_PATH, null);
        FileObject fo = null;
        try {
            fo = URLMapper.findFileObject(new URL(dir));
        } catch (MalformedURLException x) {
            LOG.log(Level.WARNING, "MalformedURLException: {0}", dir);
        }
        if (fo != null && fo.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(fo);
                if (p != null) {
                    projects.add(p);
                    if (h != null) {
                        h.progress(progressMessage(p), Math.min(++start, end));
                    }
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
            Counter counter = new Counter();
            counter.start = start;
            counter.end = end;
            processFolderChildren(fo, projects, h, counter, true);
        }
    }

    public FileObject getDirectory() {
        String p = prefs().get(KEY_PATH, null);
        if (p != null && p.length() > 0) {
            try {
                return URLMapper.findFileObject(new URL(p));
            } catch (MalformedURLException x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
        return null;
    }

    @Override
    public GroupEditPanel createPropertiesPanel() {
        return new DirectoryGroupEditPanel(this);
    }

    private void processFolderChildren(FileObject fo, Set<Project> projects, ProgressHandle h, Counter counter, boolean checkShare) throws IllegalArgumentException {
        Enumeration<? extends FileObject> e = fo.getFolders(false);
        while (e.hasMoreElements()) {
            try {
                FileObject em = e.nextElement();
                SharabilityQuery.Sharability share = SharabilityQuery.Sharability.UNKNOWN;
                if (checkShare) {
                    share = SharabilityQuery.getSharability(fo);
                    if (share == SharabilityQuery.Sharability.NOT_SHARABLE) {
                        continue;
                    }
                }
                Project p = ProjectManager.getDefault().findProject(em);
                if (p != null && !ProjectConvertors.isConvertorProject(p)) {
                    projects.add(p);
                    if (h != null) {
                        h.progress(progressMessage(p), Math.min(++counter.start, counter.end));
                    }
                }
                checkShare = share != SharabilityQuery.Sharability.SHARABLE;
                // don't need to check the sharability if the current folder is marked as recursively sharable
                
                processFolderChildren(em, projects, h, counter, checkShare);
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    private static class Counter {
        int start;
        int end;
    }

}
