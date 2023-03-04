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

package org.netbeans.api.java.source;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ElementsTest extends NbTestCase {

    public ElementsTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {new FileEncodingQueryImplementation() {
            @Override
            public Charset getEncoding(FileObject file) {
                if (file.equals(testFO))
                    return StandardCharsets.UTF_8;
                else
                    return null;
            }
        }});
    }
    
    private FileObject sourceRoot;
    private FileObject testFO;
        
    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        testFO = sourceRoot.createData("Test.java");
    }
    
    public void testI18N() throws Exception {
        prepareTest();
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "public class Vecernicek {" +
                "}");
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("Vecernicek");
                assertNotNull(typeElement);
            }
        }, true);
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "public class Večerníček {" +
                "}");
        testFO.refresh();
        javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("Večerníček");
                assertNotNull(typeElement);
            }
        }, true);
    }

    public void test175535() throws Exception {
        prepareTest();
        FileObject otherFO = FileUtil.createData(sourceRoot, "test/A.java");
        TestUtilities.copyStringToFile(FileUtil.toFile(otherFO),
                "package test;" +
                "public class A implements Runnable {" +
                "    @Override" +
                "    public void run() {}" +
                "}");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "public class Test {" +
                "}");
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils.forceSource(controller, otherFO);
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("test.A");
                assertNotNull(typeElement);
                Element el = typeElement.getEnclosedElements().get(1);
                assertNotNull(el);
                assertEquals("run", el.getSimpleName().toString());
                TreePath mpath = controller.getTrees().getPath(el);
                MethodTree mtree = (MethodTree) mpath.getLeaf();
                assertNotNull(mtree);
                List<? extends AnnotationTree> annotations = mtree.getModifiers().getAnnotations();
                TypeMirror annotation = controller.getTrees().getTypeMirror(new TreePath(mpath, annotations.get(0)));
                assertNotNull(annotation);
                Element e = controller.getTrees().getElement(new TreePath(mpath, annotations.get(0)));
                assertNotNull(e);
                assertEquals(((DeclaredType)annotation).asElement(), e);
            }
        }, true);
    }
}
