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
package org.netbeans.modules.projectapi.nb;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

final class FallbackProject implements Project, ProjectInformation, Sources {
    private static final String DASHNAME = "org-netbeans-modules-project-fallback"; // NOI18N
    private static final String ICON = "org/netbeans/modules/projectapi/nb/fallback.svg"; // NOI18N

    private final FileObject dir;
    private final ProjectState state;
    private final Lookup lkp;
    private final SourceGroup genericGroup;

    public FallbackProject(FileObject dir, ProjectState state) {
        this.dir = dir;
        this.state = state;
        this.genericGroup = GenericSources.group(
            this, dir,
            Sources.TYPE_GENERIC, getDisplayName(),
            getIcon(), getIcon()
        );
        Lookup basicLookup = Lookups.fixed(
            this,
                dir,
            LookupProviderSupport.createActionProviderMerger()
        );
        this.lkp = LookupProviderSupport.createCompositeLookup(basicLookup, "Projects/" + DASHNAME + "/Lookup");
    }

    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public String getName() {
        return dir.getNameExt();
    }

    @Override
    @NbBundle.Messages({
        "# {0} - name of the folder",
        "CTL_FallbackProjectDisplayName=Folder {0}"
    })
    public String getDisplayName() {
        return Bundle.CTL_FallbackProjectDisplayName(getName());
    }

    @Override
    public Icon getIcon() {
        Icon icon = ImageUtilities.loadIcon(ICON);
        assert icon != null;
        return icon;
    }

    @Override
    public Project getProject() {
        return this;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        if (Sources.TYPE_GENERIC.equals(type)) {
            return new SourceGroup[] { genericGroup };
        } else {
            return new SourceGroup[0];
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    void notifyDeleted() {
        state.notifyDeleted();
    }
}
