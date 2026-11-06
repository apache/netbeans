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

package org.netbeans.modules.generic.project;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * The legacy {@link SubprojectProvider} view of the projects nested
 * directly in the project directory: every child folder owned by
 * another project is a subproject.
 */
final class GpSubprojectProvider implements SubprojectProvider {

    private final GenericProject prj;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    GpSubprojectProvider(GenericProject prj) {
        this.prj = prj;
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        Set<Project> subprojects = new LinkedHashSet<>();
        FileObject[] children = prj.getProjectDirectory().getChildren();
        if (children != null) {
            for (FileObject child : children) {
                Project owner = FileOwnerQuery.getOwner(child);
                if (owner != null && !owner.equals(prj)) {
                    subprojects.add(owner);
                }
            }
        }
        return Collections.unmodifiableSet(subprojects);
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