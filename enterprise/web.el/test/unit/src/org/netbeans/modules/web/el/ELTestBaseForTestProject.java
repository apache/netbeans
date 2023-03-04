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
package org.netbeans.modules.web.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import static org.netbeans.modules.web.el.spi.ImplicitObjectType.OBJECT_TYPE;
import static org.netbeans.modules.web.el.spi.ImplicitObjectType.RAW;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author marekfukala
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELTestBaseForTestProject extends ELTestBase {

    private FileObject srcFo, webFo, projectFo;

    public ELTestBaseForTestProject(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //disable info exceptions from j2eeserver
        Logger.getLogger("org.netbeans.modules.j2ee.deployment.impl.ServerRegistry").setLevel(Level.SEVERE);

        this.projectFo = getTestFile("projects/testWebProject");
        assertNotNull(projectFo);
        this.srcFo = getTestFile("projects/testWebProject/src");
        assertNotNull(srcFo);
        this.webFo = getTestFile("projects/testWebProject/web");
        assertNotNull(webFo);

        Map<FileObject, ProjectInfo> projects = new HashMap<FileObject, ProjectInfo>();

        //create classpath for web project
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();

        //depend also on the java library
        cps.put(ClassPath.COMPILE,
                ClassPathSupport.createProxyClassPath(
                createServletAPIClassPath()));

        cps.put(ClassPath.EXECUTE, createServletAPIClassPath());
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{srcFo, webFo}));
        cps.put(ClassPath.BOOT, createBootClassPath());
        ClassPathProvider classpathProvider = new TestMultiClassPathProvider(projectFo, cps);
        Sources sources = new TestSources(srcFo, webFo);

        projects.put(projectFo, new ProjectInfo(classpathProvider, sources));

        MockLookup.setInstances(
                new TestMultiProjectFactory(projects),
                new SimpleFileOwnerQueryImplementation(),
                new FakeWebModuleProvider(webFo, srcFo),
                new TestFaceletPlugin(srcFo),
                new TestVariableResolver());

        refreshIndexAndWait();
    }

    protected void refreshIndexAndWait() throws FileStateInvalidException {
        //uff, it looks like we need to refresh the source roots separately since
        //if I use the project's folder here, then the index data are stored to
        //its index folder, but later the QuerySupport uses different cache folders
        //for webFO and srcFO so the index returns nothing.
        IndexingManager.getDefault().refreshIndexAndWait(srcFo.getURL(), null);
        IndexingManager.getDefault().refreshIndexAndWait(webFo.getURL(), null);
    }

    protected FileObject getSourcesFolder() {
        return srcFo;
    }

    protected FileObject getWebFolder() {
        return webFo;
    }

    protected FileObject getProjectFolder() {
        return projectFo;
    }

    private static class ProjectInfo {

        private ClassPathProvider cpp;
        private Sources sources;

        public ProjectInfo(ClassPathProvider cpp, Sources sources) {
            this.cpp = cpp;
            this.sources = sources;
        }

        public ClassPathProvider getCpp() {
            return cpp;
        }

        public Sources getSources() {
            return sources;
        }
    }

    public static class TestFaceletPlugin extends ELPlugin {

        private final FileObject srcFolder;

        public TestFaceletPlugin(FileObject srcFolder) {
            this.srcFolder = srcFolder;
        }

        @Override
        public String getName() {
            return "testPlugin";
        }

        @Override
        public Collection<String> getMimeTypes() {
            return Collections.singletonList("text/xhtml");
        }

        @Override
        public Collection<ImplicitObject> getImplicitObjects(FileObject file) {
            List<ImplicitObject> implObjects = new ArrayList<ImplicitObject>(9);
            implObjects.add(new JsfImplicitObject("request", "javax.servlet.http.HttpServletRequest", OBJECT_TYPE)); //NOI18N
            implObjects.add(new JsfImplicitObject("cc", "javax.faces.component.UIComponent", RAW)); //NOI18N
            return implObjects;
        }

        @Override
        public List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context) {
            Project project = FileOwnerQuery.getOwner(file);
            if (project == null) {
                return Collections.emptyList();
            }
            ResourceBundle rb = new ResourceBundle("java/beans/Messages", "bundle", Collections.singletonList(srcFolder.getFileObject("java/beans/Messages.properties")));
            return Arrays.asList(rb);
        }

        @Override
        public List<Function> getFunctions(FileObject file) {
            return Collections.emptyList();
        }
    }

    public final class TestVariableResolver implements ELVariableResolver {

        @Override
        public String getBeanName(String clazz, FileObject target, ResolverContext context) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FieldInfo getInjectableField(String beanName, FileObject target, ResolverContext context) {
            switch (beanName) {
                case "bean":
                    return new FieldInfo("beans.Bean");
                case "appleBean":
                    return new FieldInfo("issue239883.AppleBean");
                case "test253605":
                    return new FieldInfo("issue253605.Test253605");
            }
            return null;
        }

        @Override
        public List<VariableInfo> getManagedBeans(FileObject target, ResolverContext context) {
            return Collections.emptyList();
        }

        @Override
        public List<VariableInfo> getVariables(Snapshot snapshot, int offset, ResolverContext context) {
            return Collections.emptyList();
        }

        @Override
        public List<VariableInfo> getBeansInScope(String scope, Snapshot snapshot, ResolverContext context) {
            return Collections.emptyList();
        }

        @Override
        public List<VariableInfo> getRawObjectProperties(String name, Snapshot snapshot, ResolverContext context) {
            return Arrays.asList(VariableInfo.createResolvedVariable("muj", "java.lang.String"), VariableInfo.createVariable("jiny"));
        }
    }

    private static class TestMultiProjectFactory implements ProjectFactory {

        private Map<FileObject, ProjectInfo> projects;

        public TestMultiProjectFactory(Map<FileObject, ProjectInfo> projects) {
            this.projects = projects;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            ProjectInfo pi = projects.get(projectDirectory);
            return pi != null ? new TestProject(projectDirectory, state, pi.getCpp(), pi.getSources()) : null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        @Override
        public boolean isProject(FileObject dir) {
            return projects.containsKey(dir);
        }
    }

    private static class TestMultiClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;
        private FileObject root;

        public TestMultiClassPathProvider(FileObject root, Map<String, ClassPath> map) {
            this.map = map;
            this.root = root;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (FileUtil.isParentOf(root, file)) {
                if (map != null) {
                    return map.get(type);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static class JsfImplicitObject implements ImplicitObject {

        private String name, clazz;
        private ImplicitObjectType type;

        public JsfImplicitObject(String name, String clazz, ImplicitObjectType type) {
            this.name = name;
            this.clazz = clazz;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImplicitObjectType getType() {
            return type;
        }

        @Override
        public String getClazz() {
            return clazz;
        }
    }
}
