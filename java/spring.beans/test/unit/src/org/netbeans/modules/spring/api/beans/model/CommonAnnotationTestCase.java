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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 *
 */
public class CommonAnnotationTestCase extends JavaSourceTestCase {

    public CommonAnnotationTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(
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

        @Override
        public ExplorerManager createLogicalView() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExplorerManager createPhysicalView() {
            throw new UnsupportedOperationException();
        }

    }
    
    //</editor-fold>
}
