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
