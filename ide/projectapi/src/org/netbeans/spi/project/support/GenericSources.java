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

package org.netbeans.spi.project.support;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Factories for standard {@link Sources} implementations.
 * @author Jesse Glick
 */
public class GenericSources {
    
    private GenericSources() {}
    
    /**
     * Lists only one source folder group, of {@link Sources#TYPE_GENERIC},
     * containing the project directory, as by {@link #group}.
     * If you think you need this, look at {@link ProjectUtils#getSources} first.
     * @param p a project
     * @return a simple sources implementation
     */
    public static Sources genericOnly(Project p) {
        return new GenericOnlySources(p);
    }
    
    private static final class GenericOnlySources implements Sources {
        
        private final Project p;
        
        GenericOnlySources(Project p) {
            this.p = p;
        }
        
        @Override
        public SourceGroup[] getSourceGroups(String type) {
            if (type.equals(Sources.TYPE_GENERIC)) {
                return new SourceGroup[] {
                    group(p, p.getProjectDirectory(), "generic", // NOI18N
                          ProjectUtils.getInformation(p).getDisplayName(),
                          null, null),
                };
            } else {
                return new SourceGroup[0];
            }
        }
        
        @Override
        public void addChangeListener(ChangeListener listener) {}
        
        @Override
        public void removeChangeListener(ChangeListener listener) {}
        
    }
    
    /**
     * Default kind of source folder group.
     * Contains everything inside the supplied root folder which belongs to the
     * supplied project and is considered sharable by VCS.
     * @param p a project
     * @param rootFolder the root folder to use for sources
     * @param name a code name for the source group
     * @param displayName a display name for the source group
     * @param icon a regular icon to use for the source group, or null
     * @param openedIcon an opened variant icon to use, or null
     * @return a new group object
     */
    public static SourceGroup group(Project p, FileObject rootFolder, String name, String displayName, Icon icon, Icon openedIcon) {
        Parameters.notNull("p", p);
        Parameters.notNull("rootFolder", rootFolder);
        Parameters.notNull("name", name);
        Parameters.notNull("displayName", displayName);
        return new Group(p, rootFolder, name, displayName, icon, openedIcon);
    }
    
    private static final class Group implements SourceGroup {
        
        private final Project p;
        private final FileObject rootFolder;
        private final String name;
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        
        Group(Project p, FileObject rootFolder, String name, String displayName, Icon icon, Icon openedIcon) {
            this.p = p;
            this.rootFolder = rootFolder;
            this.name = name;
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
        }
        
        @Override
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        @Override public boolean contains(FileObject file) {
            if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                return false;
            }
            if (file.isFolder() && file != p.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            if (FileOwnerQuery.getOwner(file) != p) {
                return false;
            }
            // MIXED, UNKNOWN, and SHARABLE -> include it
            return SharabilityQuery.getSharability(file) != SharabilityQuery.Sharability.NOT_SHARABLE;
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }
        
        @Override
        public String toString() {
            return "GenericSources.Group[name=" + name + ",rootFolder=" + rootFolder + "]"; // NOI18N
        }
        
    }
    
}
