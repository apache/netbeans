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

package org.netbeans.modules.j2ee.ejbverification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 * @author Martin Adamek
 * @author Martin Fousek
 */
public class TestBase extends NbTestCase {

    protected static final String newline = System.getProperty("line.separator"); // NOI18N
    
    protected static final String EJB_3_0 = "3.0"; // NOI18N
    protected static final String EJB_3_1 = "3.1"; // NOI18N
    protected static final String EJB_3_2 = "3.2"; // NOI18N
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileOwnerQueryImpl fileOwnerQuery;

    protected FileObject dataDir;
    protected FileObject testFO;

    static {
        setLookups();
    }

    public TestBase(String name) {
        super(name);
    }

    /**
     * Creates copy of EJB 3.0 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestEjbModule createEjb30Module(TestEjbModule... modulesOnClasspath) throws IOException {
        return createTestEjbModule("EJBModule_5_0", EJB_3_0, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestEjbModule createEjb31Module(TestEjbModule... modulesOnClasspath) throws IOException {
        return createTestEjbModule("EJBModule_6_0", EJB_3_1, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestWebModule createWeb30Module(TestEjbModule... modulesOnClasspath) throws IOException {
        return createTestWebModule("WebModule_6_0", EJB_3_1, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestWebModule createWeb31Module(TestEjbModule... modulesOnClasspath) throws IOException {
        return createTestWebModule("WebModule_7_0", EJB_3_2, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestEjbModule createEjb32Module(TestEjbModule... modulesOnClasspath) throws IOException {
        return createTestEjbModule("EJBModule_7_0", EJB_3_2, modulesOnClasspath);
    }

    /**
     * Creates new copy of project in test's working directory instead of using one froo data dir,
     * co it can be called multiple times on 'clean' project (without generated code)
     */
    protected TestEjbModule createTestEjbModule(String projectDirName, String ejbVersion, TestEjbModule... modulesOnClasspath) throws IOException {

        File projectDir = new File(getDataDir(), projectDirName);
        File tempProjectDir = copyFolder(projectDir);

        TestEjbModule testModule = new TestEjbModule(FileUtil.toFileObject(tempProjectDir), ejbVersion);
        activate(testModule, true, modulesOnClasspath);

        return testModule;
    }

    /**
     * Creates new copy of project in test's working directory instead of using one froo data dir,
     * co it can be called multiple times on 'clean' project (without generated code)
     */
    protected TestWebModule createTestWebModule(String projectDirName, String webVersion, TestEjbModule... modulesOnClasspath) throws IOException {

        File projectDir = new File(getDataDir(), projectDirName);
        File tempProjectDir = copyFolder(projectDir);

        TestWebModule testModule = new TestWebModule(FileUtil.toFileObject(tempProjectDir), webVersion);
        activate(testModule, false, modulesOnClasspath);

        return testModule;
    }

    protected String errorDescriptionToString(List<? extends ErrorDescription> eds) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ErrorDescription ed : eds) {
            sb.append(ed.getFile().getName()).append("[line ")
                    .append(ed.getRange().getBegin().getLine()).append("]")
                    .append(" (").append(ed.getSeverity()). append(") ").append(ed.getDescription());
        }
        return sb.toString();
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        File file = new File(getWorkDir(), "cache"); //NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        fileOwnerQuery = new FileOwnerQueryImpl();
        setLookups(ejbJarProvider, classPathProvider, fileOwnerQuery, new FakeJavaDataLoaderPool(), new TestSourceLevelQueryImplementation());
        dataDir = FileUtil.toFileObject(getDataDir());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }

    public static void setLookups(Object... instances) {
        Object[] allInstances = new Object[instances.length + 1];
        ClassLoader classLoader = TestBase.class.getClassLoader();
        allInstances[0] = classLoader;
        System.arraycopy(instances, 0, allInstances, 1, instances.length);
        MockLookup.setInstances(allInstances);
    }

    private void activate(TestModule testModule, boolean full, TestEjbModule... modulesOnClasspath) {
        fileOwnerQuery.setProject(testModule.project);
        ejbJarProvider.setEjbModule(convertEjbVersionToJavaEEVersion(testModule.level, full), testModule.sources);
        FileObject[] sources = new FileObject[1 + modulesOnClasspath.length];
        sources[0] = testModule.sources[0];
        for (int i = 0; i < modulesOnClasspath.length; i++) {
            sources[i + 1] = modulesOnClasspath[i].sources[0];
        }
        classPathProvider.setClassPath(sources);
        try {
            for (FileObject fileObject : testModule.sources) {
                IndexingManager.getDefault().refreshIndexAndWait(fileObject.getURL(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Profile convertEjbVersionToJavaEEVersion(String ejbVersion, boolean full) {
        double version = Double.parseDouble(ejbVersion);
        if (full) {
            if (version >= 3.2) {
                return Profile.JAVA_EE_7_FULL;
            } else if (version == 3.1) {
                return Profile.JAVA_EE_6_FULL;
            } else if (version > 2.1) {
                return Profile.JAVA_EE_5;
            } else {
                return Profile.J2EE_14;
            }
        } else {
            if (version >= 3.2) {
                return Profile.JAVA_EE_7_WEB;
            } else {
                return Profile.JAVA_EE_6_WEB;
            }
        }
    }
    
    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.<br>
     * Copy from /ant/freeform/test/unit/src/org/netbeans/modules/ant/freeform/TestBase.java
     */
    private File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = workdir.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }

    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }

    protected static class TestEjbModule extends TestModule {

        public TestEjbModule(FileObject projectDir, String level, EnterpriseReferenceContainerImpl erci, boolean webProject) {
            super(projectDir, level, erci, webProject);
        }

        public TestEjbModule(FileObject projectDir, String ejbLevel) {
            super(projectDir, ejbLevel, new EnterpriseReferenceContainerImpl(), false);
            project.setProjectDirectory(projectDir);
        }

        public EjbJar getEjbModule() {
            return EjbJar.getEjbJars(project)[0];
        }
        
        public EnterpriseReferenceContainerImpl getEnterpriseReferenceContainerImpl() {
            return erci;
        }
    }

    protected static class TestWebModule extends TestEjbModule {

        private final FileObject[] webSources;

        public TestWebModule(FileObject projectDir, String webLevel) {
            super(projectDir, webLevel, new EnterpriseReferenceContainerImpl(), true);
            this.webSources = new FileObject[]{projectDir.getFileObject("web")};
            project.setProjectDirectory(projectDir);
        }

        public WebModule getWebModule() {
            return WebModule.getWebModule(project.getProjectDirectory());
        }
    }

    public static final class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {

        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
    }

    protected static boolean containsMethod(CompilationController controller, MethodModel methodModel, TypeElement typeElement) {
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MethodModelSupport.isSameMethod(controller, executableElement, methodModel)) {
                return true;
            }
        }
        return false;
    }

    protected static Element getMember(TypeElement typeElement, String elementName) {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getSimpleName().contentEquals(elementName)) {
                return element;
            }
        }
        return null;
    }
    
    protected static boolean containsType(CompilationController controller, List<? extends TypeMirror> typeMirrors, String typeFqn) {
        TypeElement typeElement = controller.getElements().getTypeElement(typeFqn);
        TypeMirror searchedTypeMirror = typeElement.asType();
        Types types = controller.getTypes();
        for (TypeMirror typeMirror : typeMirrors) {
            if (types.isSameType(typeMirror, searchedTypeMirror)) {
                return true;
            }
        }
        return false;
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            try {
                FileUtil.copy(is, os);
                return fo;
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }

    protected abstract static class TestModule {

        protected final FileObject projectDir;
        protected final FileObject[] sources;
        protected final ProjectImpl project;
        protected final EnterpriseReferenceContainerImpl erci;
        protected final String level;

        public TestModule(FileObject projectDir, String level, EnterpriseReferenceContainerImpl erci, boolean webProject) {
            this.projectDir = projectDir;
            this.sources = new FileObject[]{projectDir.getFileObject("src/java")};
            if (webProject) {
                this.project = new ProjectImpl(level, J2eeModule.Type.WAR, erci);
            } else {
                this.project = new ProjectImpl(level, J2eeModule.Type.EJB, erci);
            }
            this.erci = erci;
            this.level = level;
        }

        public FileObject[] getSources() {
            return sources;
        }

        public ProjectImpl getProject() {
            return project;
        }
    }
    
}
