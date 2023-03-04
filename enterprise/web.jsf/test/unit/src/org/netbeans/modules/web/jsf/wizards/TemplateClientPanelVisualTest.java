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
package org.netbeans.modules.web.jsf.wizards;

import org.netbeans.modules.web.jsf.metamodel.CommonTestCase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TemplateClientPanelVisualTest extends CommonTestCase {

    public TemplateClientPanelVisualTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

//    @Override
//    protected Lookup getLookup() {
//        return Lookups.fixed(new TestProjectFactory(projects));
//    }

    public void testGetRelativePathInsideResourceLibrary() throws Exception {
        String fullpath = "/home/NetBeansProjects/WebApplication272/web/template.xhtml";
        assertEquals(fullpath, TemplateClientPanelVisual.getRelativePathInsideResourceLibrary(fullpath));
        fullpath = "/home/NetBeansProjects/WebApplication272/web/contracts/myOne/template.xhtml";
        assertEquals("/template.xhtml", TemplateClientPanelVisual.getRelativePathInsideResourceLibrary(fullpath));
        fullpath = "META-INF/contracts/anotherOne/subdir/template.xml";
        assertEquals("/subdir/template.xml", TemplateClientPanelVisual.getRelativePathInsideResourceLibrary(fullpath));
    }

//    public void testGetProjectDocumentSourceGroups() throws Exception {
//        TemplateClientPanelVisual panel = new TemplateClientPanelVisual(new MyWizardDescriptor(project));
//        for (SourceGroup sourceGroup : panel.getProjectDocumentSourceGroups()) {
//            System.err.println(sourceGroup.getRootFolder().getPath());
//        }
//    }
//
//    private static class MyWizardDescriptor extends WizardDescriptor {
//
//        private final Project project;
//
//        private MyWizardDescriptor(Project project) {
//            this.project = project;
//        }
//
//        @Override
//        public synchronized Object getProperty(String name) {
//            if (ProjectChooserFactory.WIZARD_KEY_PROJECT.equals(name)) {
//                return project;
//            }
//            return super.getProperty(name);
//        }
//    }
//
//    private static class SourcesImpl implements Sources {
//        public Map<String,List<SourceGroup>> grpMap = new HashMap<String,List<SourceGroup>>();
//
//        public SourceGroup[] getSourceGroups(String type) {
////            return grpMap.get(type).toArray(new SourceGroup[0]);
//            return null;
//        }
//
//        public void addChangeListener(ChangeListener listener) {
//        }
//
//        public void removeChangeListener(ChangeListener listener) {
//        }
//
//        public @Override String toString() {
//            return grpMap.toString();
//        }
//    }
//
//    private static class TestProjectFactory implements ProjectFactory {
//
//        private List<FileObject> projects;
//
//        public TestProjectFactory(List<FileObject> projects) {
//            this.projects = projects;
//        }
//
//        @Override
//        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
//            return new TCPVTestProject(projectDirectory, state);
//        }
//
//        @Override
//        public void saveProject(Project project) throws IOException, ClassCastException {
//        }
//
//        @Override
//        public boolean isProject(FileObject dir) {
//            return projects.contains(dir);
//        }
//    }
//
//    private static class TCPVTestProject extends TestProject {
//
//        public TCPVTestProject(FileObject dir, ProjectState state) {
//            super(dir, state);
//        }
//
//        @Override
//        public Lookup getLookup() {
//            return Lookups.fixed(super.getLookup(), Lookups.singleton(new SourcesImpl()));
//        }
//
//    }

}
