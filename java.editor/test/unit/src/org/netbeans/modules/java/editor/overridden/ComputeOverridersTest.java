/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.overridden;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil.TestSourceLevelQueryImplementation;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeOverridersTest extends NbTestCase {

    public ComputeOverridersTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        prepareSourceRoot("1");
        prepareSource("1",
                      "test/Test.java",
                      "package test;" +
                      "public abstract class Test {" +
                      "     protected abstract void test();" +
                      "}");
        prepareSourceRoot("2", "1");
        prepareSource("2",
                      "dep/Dep.java",
                      "package dep;" +
                      "public class Dep extends test.Test {" +
                      "     protected void test() {}" +
                      "}");
        prepareSourceRoot("3", "1");
        prepareSource("3",
                      "dep3/Dep3.java",
                      "package dep3;" +
                      "public class Dep3 extends test.Test {" +
                      "     protected void test() {}" +
                      "}");
        prepareSourceRoot("4", "1", "2");
        prepareSource("4",
                      "dep4/Dep4.java",
                      "package dep4;" +
                      "public class Dep4 extends dep.Dep {" +
                      "     protected void test() {}" +
                      "}");

        FileObject file = sourceDirectories.getFileObject("1/test/Test.java");

        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(file), Phase.RESOLVED);
        URL r1 = sourceDirectories.getFileObject("1").getURL();
        URL r2 = sourceDirectories.getFileObject("2").getURL();
        URL r3 = sourceDirectories.getFileObject("3").getURL();
        URL r4 = sourceDirectories.getFileObject("4").getURL();

        ComputeOverriders.reverseSourceRootsInOrderOverride = Arrays.asList(r1, r2, r3, r4);

        ComputeOverriders.dependenciesOverride = new HashMap<URL, List<URL>>();
        ComputeOverriders.dependenciesOverride.put(r1, Collections.<URL>emptyList());
        ComputeOverriders.dependenciesOverride.put(r2, Collections.singletonList(r1));
        ComputeOverriders.dependenciesOverride.put(r3, Collections.singletonList(r1));
        ComputeOverriders.dependenciesOverride.put(r4, Arrays.asList(r1, r2));
        
        Map<ElementHandle<? extends Element>, List<ElementDescription>> output = new ComputeOverriders(new AtomicBoolean()).process(info, null, null, false);
        Map<String, List<String>> outputStrings = new LinkedHashMap<String, List<String>>();

        for (Entry<ElementHandle<? extends Element>, List<ElementDescription>> e : output.entrySet()) {
            List<String> descs = new LinkedList<String>();

            for (ElementDescription ed : e.getValue()) {
                descs.add(ed.getHandle().toString());
            }

            outputStrings.put(e.getKey().toString(), descs);
        }

        Map<String, List<String>> golden = new LinkedHashMap<String, List<String>>();

        golden.put("ElementHandle[kind=CLASS; sigs=test.Test ]", Arrays.asList(
            "ElementHandle[kind=CLASS; sigs=dep.Dep ]",
            "ElementHandle[kind=CLASS; sigs=dep3.Dep3 ]",
            "ElementHandle[kind=CLASS; sigs=dep4.Dep4 ]"
        ));
        golden.put("ElementHandle[kind=METHOD; sigs=test.Test test ()V ]", Arrays.asList(
            "ElementHandle[kind=METHOD; sigs=dep.Dep test ()V ]",
            "ElementHandle[kind=METHOD; sigs=dep3.Dep3 test ()V ]",
            "ElementHandle[kind=METHOD; sigs=dep4.Dep4 test ()V ]"
        ));

        assertEquals(golden, outputStrings);
    }

    public void test234941() throws Exception {
        prepareSourceRoot("1");
        prepareSource("1",
                      "test/Object.java",
                      "package test;" +
                      "import java.lang.Object;" +
                      "public class Object extends Object {" +
                      "}");

        FileObject file = sourceDirectories.getFileObject("1/test/Object.java");

        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(file), Phase.RESOLVED);
        URL r1 = sourceDirectories.getFileObject("1").getURL();

        ComputeOverriders.reverseSourceRootsInOrderOverride = Arrays.asList(r1);

        ComputeOverriders.dependenciesOverride = new HashMap<URL, List<URL>>();
        ComputeOverriders.dependenciesOverride.put(r1, Collections.<URL>emptyList());

        //only checking the computation will end:
        new ComputeOverriders(new AtomicBoolean()).process(info, null, null, false);
    }


    private Map<FileObject, ClassPath> root2ClassPath = new HashMap<FileObject, ClassPath>();
    private Map<FileObject, FileObject> root2BuildRoot = new HashMap<FileObject, FileObject>();
    private Map<FileObject, FileObject> buildRoot2Source = new HashMap<FileObject, FileObject>();

    private FileObject sourceDirectories;
    private FileObject buildDirectories;

    private void prepareSourceRoot(String name, String... dependsOn) throws Exception {
        FileObject src = FileUtil.createFolder(sourceDirectories, name);
        FileObject build = FileUtil.createFolder(buildDirectories, name);

        List<FileObject> dependencies = new LinkedList<FileObject>();

        for(String dep : dependsOn) {
            FileObject depFO = buildDirectories.getFileObject(dep);

            assertNotNull(depFO);
            dependencies.add(depFO);
        }

        root2ClassPath.put(src, ClassPathSupport.createClassPath(dependencies.toArray(new FileObject[0])));
        root2BuildRoot.put(src, build);
        buildRoot2Source.put(build, src);
    }

    private void prepareSource(String sourceRoot, String fileName, String code) throws Exception {
        FileObject root = sourceDirectories.getFileObject(sourceRoot);

        assertNotNull(root);

        FileObject source = FileUtil.createData(root, fileName);

        TestUtilities.copyStringToFile(source, code);

        SourceUtilsTestUtil.compileRecursively(root);
    }
    
    public void setUp() throws Exception {
        SourceUtilsTestUtil.setLookup(new Object[0], ComputeOverridersTest.class.getClassLoader());
        Main.initializeURLFactory();
        
        clearWorkDir();
        File wd = getWorkDir();
        assert wd.isDirectory() && wd.list().length == 0;
        FileObject dir = FileUtil.toFileObject(wd);

        assertNotNull(dir);

        sourceDirectories = FileUtil.createFolder(dir, "src");
        buildDirectories = FileUtil.createFolder(dir, "build");

        FileObject cache = FileUtil.createFolder(dir, "cache");

        List<Object> lookupContent = new LinkedList<Object>();

        lookupContent.add(new TestProxyClassPathProvider());
        lookupContent.add(new TestSourceForBinaryQuery());
        lookupContent.add(new TestSourceLevelQueryImplementation());
        lookupContent.add(JavaDataLoader.findObject(JavaDataLoader.class, true));

        SourceUtilsTestUtil.setLookup(lookupContent.toArray(), SourceUtilsTestUtil.class.getClassLoader());

        IndexUtil.setCacheFolder(FileUtil.toFile(cache));

        RepositoryUpdater.getDefault().start(true);
    }

    private FileObject findRoot(FileObject file) {
        FileObject root = null;
        for (Entry<FileObject, ClassPath> e : root2ClassPath.entrySet()) {
            if (e.getKey() == file || FileUtil.isParentOf(e.getKey(), file)) {
                root = e.getKey();
                break;
            }
        }
        return root;
    }

    private class TestProxyClassPathProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            try {
                FileObject root = findRoot(file);

                if (root == null) {
                    return null;
                }
                
                if (ClassPath.BOOT == type) {
                    return ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
                }

                if (ClassPath.SOURCE == type) {
                    return ClassPathSupport.createClassPath(new FileObject[] {
                        root
                    });
                }

                if (ClassPath.COMPILE == type) {
                    return root2ClassPath.get(root);
                }

                if (ClassPath.EXECUTE == type) {
                    return ClassPathSupport.createProxyClassPath(
                            root2ClassPath.get(root),
                            ClassPathSupport.createClassPath(new FileObject[] {
                                root2BuildRoot.get(root)
                            })
                           );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class TestSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (SourceUtilsTestUtil.getBootClassPath().contains(binaryRoot))
                return null;

            FileObject file = URLMapper.findFileObject(binaryRoot);

            if (file == null) {
                return null;
            }
            
            final FileObject source = buildRoot2Source.get(file);

            if (source == null) {
                return null;
            }

            return new SourceForBinaryQuery.Result() {
                public FileObject[] getRoots() {
                    return new FileObject[] {
                        source,
                    };
                }

                public void addChangeListener(ChangeListener l) {}
                public void removeChangeListener(ChangeListener l) {}
            };
        }

    }
    
}