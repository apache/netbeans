/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.ejbcore.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 * @author Martin Adamek
 */
public class TestBase extends NbTestCase {

    protected static final String newline = System.getProperty("line.separator"); // NOI18N
    
    protected static final String EJB_2_1 = "2.1"; // NOI18N
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
     * Creates copy of EJB 2.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb21Module(TestModule... modulesOnClasspath) throws IOException {
        return createTestModule("EJBModule_1_4", EJB_2_1, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.0 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb30Module(TestModule... modulesOnClasspath) throws IOException {
        return createTestModule("EJBModule_5_0", EJB_3_0, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb31Module(TestModule... modulesOnClasspath) throws IOException {
        return createTestModule("EJBModule_6_0", EJB_3_1, modulesOnClasspath);
    }

    /**
     * Creates copy of EJB 3.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb32Module(TestModule... modulesOnClasspath) throws IOException {
        return createTestModule("EJBModule_7_0", EJB_3_2, modulesOnClasspath);
    }

    /**
     * Creates new copy of project in test's working directory instead of using one froo data dir,
     * co it can be called multiple times on 'clean' project (without generated code)
     */
    protected TestModule createTestModule(String projectDirName, String ejbVersion, TestModule... modulesOnClasspath) throws IOException {

        File projectDir = new File(getDataDir(), projectDirName);
        File tempProjectDir = copyFolder(projectDir);

        TestModule testModule = new TestModule(FileUtil.toFileObject(tempProjectDir), ejbVersion);
        activate(testModule, modulesOnClasspath);

        return testModule;
    }

    protected void setUp() throws IOException {
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

    private void activate(TestModule testModule, TestModule... modulesOnClasspath) {
        fileOwnerQuery.setProject(testModule.project);
        ejbJarProvider.setEjbModule(convertEjbVersionToJavaEEVersion(testModule.ejbLevel), testModule.deploymentDescriptor, testModule.sources);
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

    private static Profile convertEjbVersionToJavaEEVersion(String ejbVersion) {
        double version = Double.parseDouble(ejbVersion);
        if (version > 2.1) {
            return Profile.JAVA_EE_5;
        } else {
            return Profile.J2EE_14;
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

    protected static class TestModule {

        private final FileObject projectDir;
        private final String ejbLevel;
        private final FileObject deploymentDescriptor;
        private final FileObject[] sources;
        private final ProjectImpl project;
        private final EnterpriseReferenceContainerImpl erContainer;

        public TestModule(FileObject projectDir, String ejbLevel) {
            this.projectDir = projectDir;
            this.ejbLevel = ejbLevel;
            this.deploymentDescriptor = projectDir.getFileObject("src/conf/ejb-jar.xml");
            this.sources = new FileObject[]{projectDir.getFileObject("src/java")};
            this.erContainer = new EnterpriseReferenceContainerImpl();
            this.project = new ProjectImpl(ejbLevel, erContainer);
            project.setProjectDirectory(projectDir);
        }

        public FileObject getDeploymentDescriptor() {
            return deploymentDescriptor;
        }

        public FileObject[] getSources() {
            return sources;
        }

        public Project getProject() {
            return project;
        }

        public FileObject getConfigFilesFolder() {
            return projectDir.getFileObject("src/conf");
        }
        
        public EjbJar getEjbModule() {
            return EjbJar.getEjbJars(project)[0];
        }
        
        public EnterpriseReferenceContainerImpl getEnterpriseReferenceContainerImpl() {
            return erContainer;
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
    
}
