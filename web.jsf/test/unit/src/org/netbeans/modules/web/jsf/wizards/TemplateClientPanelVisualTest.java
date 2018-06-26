/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
