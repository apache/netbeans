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
package org.netbeans.modules.rust.project;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * Sources for Rust projects.
 *
 * @see
 * <a href="https://bits.netbeans.org/dev/javadoc/org-netbeans-modules-projectapi/org/netbeans/api/project/Sources.html">Sources</a>
 */
public class RustSources implements Sources {

    private final RustProject project;
    private final ChangeSupport changeSupport;

    public RustSources(RustProject project) {
        this.project = project;
        this.changeSupport = new ChangeSupport(this);
        // TODO: What happens if "tests" folder is deleted?
        project.getCargoTOML().addPropertyChangeListener((event)-> {
            if (CargoTOML.PROP_WORKSPACE.equals(event.getPropertyName())) {
                changeSupport.fireChange();
            }
        });
    }

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        if (Sources.TYPE_GENERIC.equals(type)) {
            return new SourceGroup[] { new RustSourceGroup(project, project.getProjectDirectory()) };
            /*
            ArrayList<RustSourceGroup> groups = new ArrayList<>();
            FileObject rootFO = project.getProjectDirectory();
            // Which "SourceGroups" do we have in Rust projects?
            // - The "tests" folder, if any.
            // - The folders for each one of the workspace members.
            // - The "src" folder.
            FileObject src = rootFO.getFileObject("src"); // NOI18N
            if (src != null) {
                groups.add(new RustSourceGroup(project, src));
            }
            FileObject tests = rootFO.getFileObject("tests"); // NOI18N
            if (tests != null) {
                groups.add(new RustSourceGroup(project, tests));
            }
            for (String memberPath: project.getCargoTOML().getWorkspace().keySet()) {
                FileObject memberDirectory = rootFO.getFileObject(memberPath);
                if (memberDirectory != null) {
                    groups.add(new RustSourceGroup(project, memberDirectory));
                }
            }
            return groups.toArray(new RustSourceGroup[0]);
            */
        }
        return new SourceGroup[0];
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

}
