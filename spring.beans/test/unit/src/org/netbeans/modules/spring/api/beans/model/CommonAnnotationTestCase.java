/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.api.beans.model;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.j2ee.core.Profile;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 *
 */
public class CommonAnnotationTestCase extends JavaSourceTestCase {

    WebModuleProvider webModuleProvider;

    public CommonAnnotationTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webModuleProvider = new FakeWebModuleProvider(srcFO);
        MockLookup.setInstances(
                webModuleProvider,
                new ClassPathProviderImpl(),
                new OpenProject());
        MockServices.setServices(
                OpenProject.class
                );
        
    }

    public MetadataModel<SpringModel> createSpringModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ModelUnit modelUnit = ModelUnit.create(
                bootCP,
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE));
        return SpringModelFactory.createMetaModel(modelUnit);
    }
    
    public List<SpringBean> getAnnotatedBeans(MetadataModel<SpringModel> model) throws IOException {
        return model.runReadAction(new MetadataModelAction<SpringModel, List<SpringBean>>() {

            @Override
            public List<SpringBean> run(SpringModel model) throws Exception {
                List<SpringBean> beans = new LinkedList<SpringBean>();
                beans.addAll(model.getBeans());
                return beans;
            }
        });
    }
    
    /**
     * Creates new instance of {@link ModelUnit} for test project
     * @return ModelUnit
     * @throws MalformedURLException when path to testing project is not valid
     * @throws IOException when boot path property contains non valid path
     */    
    public ModelUnit createNewModelUnitForTestProject() throws MalformedURLException, IOException {
        Map<String, ClassPath> classPath = new HashMap<String, ClassPath>();
        
        
        classPath.put(ClassPath.BOOT, bootCP);
        classPath.put(ClassPath.COMPILE, ClassPathSupport.createClassPath(srcFO));
        classPath.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{srcFO}));
        
        return ModelUnit.create(classPath.get(ClassPath.BOOT), classPath.get(ClassPath.COMPILE), 
                classPath.get(ClassPath.SOURCE));
    }

    //<editor-fold defaultstate="collapsed">
    
    protected static class FakeWebModuleProvider implements WebModuleProvider {

        private FileObject webRoot;

        public FakeWebModuleProvider(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(new FakeWebModuleImplementation2(webRoot));
        }
    }

    private static class FakeWebModuleImplementation2 implements WebModuleImplementation2 {

        private FileObject webRoot;

        public FakeWebModuleImplementation2(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public FileObject getDocumentBase() {
            return webRoot;
        }

        @Override
        public String getContextPath() {
            return "/";
        }

        @Override
        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        @Override
        public FileObject getWebInf() {
            return null;
        }

        @Override
        public FileObject getDeploymentDescriptor() {
            return null;
        }

        @Override
        public FileObject[] getJavaSources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    
    public static class OpenProject implements  OpenProjectsTrampoline {

        public @Override Project[] getOpenProjectsAPI() {
            return new Project[0];
        }

        public @Override void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {

        }

        public @Override void closeAPI(Project[] projects) {

        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            
        }

        public Future<Project[]> openProjectsAPI() {
            return new Future<Project[]>() {

                public boolean cancel(boolean mayInterruptIfRunning) {
                    return true;
                }

                public boolean isCancelled() {
                    return false;
                }

                public boolean isDone() {
                    return true;
                }

                public Project[] get() throws InterruptedException, ExecutionException {
                    return new Project[0];
                }

                public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return new Project[0];
                }
            };
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            
        }

        public @Override Project getMainProject() {
            return null;
        }

        public @Override void setMainProject(Project project) {
            
        }

        @Override
        public ProjectGroup getActiveProjectGroupAPI() {
            return null;
        }

        @Override
        public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

    }
    
    //</editor-fold>
}
