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

package org.netbeans.modules.java.source.nbjavac.indexing;

import java.io.File;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class TreeLoaderTest extends NbTestCase {

    public TreeLoaderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    public void test171340() throws Exception {
        prepareTest();

        FileObject src1 = FileUtil.createData(sourceRoot, "test/Test1.java");
        FileObject src2 = FileUtil.createData(sourceRoot, "test/Test2.java");

        TestUtilities.copyStringToFile(src1,
                "package test;\n" +
                "public class Test1 {}");
        TestUtilities.copyStringToFile(src2,
                "package test;\n" +
                "public class Test2 {" +
                "    public void test() {}" +
                "}");
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        JavaSource javaSource = JavaSource.forFileObject(src1);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("test.Test2");
                assertNotNull(typeElement);
                ExecutableElement method = (ExecutableElement) typeElement.getEnclosedElements().get(1);
                assertNotNull(controller.getTrees().getPath(method));
            }
        }, true);
    }

    private FileObject sourceRoot;

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
    }

}
