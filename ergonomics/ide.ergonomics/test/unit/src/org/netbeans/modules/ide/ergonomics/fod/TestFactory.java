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
package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.junit.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class, position=29998)
public final class TestFactory extends ProjectOpenedHook
implements ProjectFactory, Project, ProjectInformation, SubprojectProvider, LogicalViewProvider {

    static Set<FileObject> recognize = new HashSet<FileObject>();
    static Set<Project> subprojects = new HashSet<Project>();
    static IOException ex;
    int closed;
    int opened;
    int listenerCount;
    final FileObject dir;

    public TestFactory() {
        dir = null;
    }

    private TestFactory(FileObject dir) {
        this.dir = dir;
    }

    public boolean isProject(FileObject projectDirectory) {
        return recognize.contains(projectDirectory);
    }

    public Project loadProject(FileObject pd, ProjectState state) throws IOException {
        IOException e = ex;
        if (e != null) {
            ex = null;
            throw e;
        }
        return isProject(pd) ? new TestFactory(pd) : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    public FileObject getProjectDirectory() {
        return dir;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public String getName() {
        return "x";
    }

    public String getDisplayName() {
        return "y";
    }

    public Icon getIcon() {
        return null;
    }

    public Project getProject() {
        return this;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerCount++;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerCount--;
    }

    @Override
    protected void projectOpened() {
        opened++;
    }

    @Override
    protected void projectClosed() {
        closed++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestFactory) {
            return super.equals(obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Set<? extends Project> getSubprojects() {
        return subprojects;
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public Node createLogicalView() {
        AbstractNode an = new AbstractNode(new Children.Array());
        an.setName("xyz");
        an.setDisplayName("Name xyz");

        an.getChildren().add(new Node[]{ new AbstractNode(Children.LEAF), new AbstractNode(Children.LEAF) });
        an.getChildren().getNodeAt(0).setName("a");
        an.getChildren().getNodeAt(1).setName("b");
        return an;
    }

    public Node findPath(Node root, Object target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void assertListeners(String msg) {
        if (listenerCount == 0) {
            Assert.fail(msg);
        }
    }


}
