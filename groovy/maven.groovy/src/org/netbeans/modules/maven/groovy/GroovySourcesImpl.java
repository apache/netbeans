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

package org.netbeans.modules.maven.groovy;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import static org.netbeans.modules.maven.groovy.Bundle.*;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

@ProjectServiceProvider(service={Sources.class, SourceGroupModifierImplementation.class}, projectType="org-netbeans-modules-maven")
public class GroovySourcesImpl implements Sources, SourceGroupModifierImplementation {

    public static final String TYPE_GROOVY = "groovy";
    public static final String NAME_GROOVYSOURCE = "81GroovySourceRoot";
    public static final String NAME_GROOVYTESTSOURCE = "82GroovyTestSourceRoot";

    private final Project project;

    public GroovySourcesImpl(Project project) {
        this.project = project;
    }

    @Override public SourceGroup[] getSourceGroups(String type) {
        if (TYPE_GROOVY.equals(type)) {
            List<SourceGroup> groups = new ArrayList<SourceGroup>();
            addTestGroup(groups);
            addSourcesGroup(groups);
            return groups.toArray(new SourceGroup[0]);
        } else {
            return new SourceGroup[0];
        }
    }

    @NbBundle.Messages({"SG_Test_GroovySources=Groovy Test Packages"})
    private void addTestGroup(List<SourceGroup> groups) {
        addGroupIfRootExists(groups, "test", NAME_GROOVYTESTSOURCE, SG_Test_GroovySources());
    }

    @NbBundle.Messages({"SG_GroovySources=Groovy Packages"})
    private void addSourcesGroup(List<SourceGroup> groups) {
        addGroupIfRootExists(groups, "main", NAME_GROOVYSOURCE, SG_GroovySources());
    }

    private void addGroupIfRootExists(List<SourceGroup> groups, String rootType, String name, String displayName) {
        FileObject root = project.getProjectDirectory().getFileObject("src/" + rootType + "/groovy");
        if (root != null) {
            groups.add(GenericSources.group(project, root, name, displayName, null, null));
        }
    }

    @Override public void addChangeListener(ChangeListener listener) {
        // XXX listen to creation/deletion of roots
    }

    @Override public void removeChangeListener(ChangeListener listener) {}

    @Override public SourceGroup createSourceGroup(String type, String hint) {
        if (!canCreateSourceGroup(type, hint)) {
            return null;
        }
        List<SourceGroup> groups = new ArrayList<SourceGroup>();

        if (JavaProjectConstants.SOURCES_HINT_TEST.equals(hint)) {
            addTestGroup(groups);
        } else {
            addSourcesGroup(groups);
        }
        return groups.isEmpty() ? null : groups.get(0);
    }

    @Override public boolean canCreateSourceGroup(String type, String hint) {
        return TYPE_GROOVY.equals(type) && (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint) || JavaProjectConstants.SOURCES_HINT_TEST.equals(hint));
    }

}
