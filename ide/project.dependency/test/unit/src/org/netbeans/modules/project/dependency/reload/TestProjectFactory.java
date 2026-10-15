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
package org.netbeans.modules.project.dependency.reload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ProjectFactory.class)
public class TestProjectFactory implements ProjectFactory2 {
    
    public interface ProjectItem<T> {
        public T create(Project p);
    }
    private static Map<FileObject, Lookup> prjLookups = new HashMap<>();
    private static Map<FileObject, Collection<ProjectItem>> lookupContents = new HashMap<>();
    
    public static void addToProject(FileObject f, ProjectItem<?> item) {
        lookupContents.computeIfAbsent(f, (x) -> new ArrayList<>()).add(item);
    }
    
    public static void registerLookup(FileObject d, Lookup l) {
        prjLookups.put(d, l);
    }
    
    public static void addToProjectLookup(Project p, Object o) {
        
    }
    
    public static void clear() {
        prjLookups.clear();
    }

    @Override
    public ProjectManager.Result isProject2(FileObject projectDirectory) {
        String s = projectDirectory.getNameExt();
        if (!s.endsWith("._test")) {
            return null;
        }
        
        return new ProjectManager.Result(s.substring(0, s.length() - 5), "test-project", null);
    }

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return isProject2(projectDirectory) != null;
    }

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject2(projectDirectory) == null) {
            return null;
        }
        Lookup l = prjLookups.get(projectDirectory);
        
        ProxyLookup.Controller cntr = new ProxyLookup.Controller();
        ProxyLookup pl = new ProxyLookup(cntr);
        
        Prj p = new Prj(projectDirectory, pl);
        Collection objects = new ArrayList<>();
        Collection<ProjectItem> items = lookupContents.get(projectDirectory);
        if (items != null) {
            for (ProjectItem i : items) {
                Object o = i.create(p);
                if (o != null) {
                    objects.add(o);
                }
            }
        }
        Lookup created = Lookups.fixed(objects.toArray(Object[]::new));
        if (l == null) {
            cntr.setLookups(created);
        } else {
            pl = new ProxyLookup(l, created);
        }
        return p;
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
    }
    
    public static class Prj implements Project {
        private final FileObject dir;
        private final Lookup lkp;

        public Prj(FileObject dir, Lookup lkp) {
            this.dir = dir;
            this.lkp = lkp;
        }
        @Override
        public FileObject getProjectDirectory() {
            return dir;
        }

        @Override
        public Lookup getLookup() {
            return lkp;
        }
    }
}
