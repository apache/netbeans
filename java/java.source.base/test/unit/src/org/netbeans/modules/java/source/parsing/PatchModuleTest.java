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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.JavacTool;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;

public class PatchModuleTest extends NbTestCase {

    public PatchModuleTest(String name) {
        super(name);
    }

    private FileObject sourceRoot;
    private FileObject classRoot;
    private FileObject cp;

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {settings, binaryForSource});
        clearWorkDir();
        prepareTest();
    }

    public void testNETBEANS_4044() throws Exception {
        settings.commandLine = "-Xnb-Xmodule:patch";
        binaryForSource.result = new BinaryForSourceQuery.Result() {
            @Override
            public URL[] getRoots() {
                return new URL[] {classRoot.toURL()};
            }
            @Override
            public void addChangeListener(ChangeListener l) {}
            @Override
            public void removeChangeListener(ChangeListener l) {}
        };
        FileObject sourceModule = createFile("module-info.java", "module patch {}"); SourceUtilsTestUtil.setSourceLevel(sourceModule, "11");
        FileObject source1 = createFile("patch/Patch.java", "package patch; class Patch { Dep dep; }"); SourceUtilsTestUtil.setSourceLevel(source1, "11");
        FileObject source2 = createFile("patch/Dep.java", "package patch; class Dep { }"); SourceUtilsTestUtil.setSourceLevel(source2, "11");
        ClasspathInfo cpInfo = ClasspathInfo.create(sourceModule);
        try (JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, "11");
             JavaFileManager output = new OutputFileManager(fm)) {
            List<JavaFileObject> files = new ArrayList<>();
            Enumeration<? extends FileObject> en = sourceRoot.getChildren(true);
            while (en.hasMoreElements()) {
                FileObject f = en.nextElement();
                if (f.isData()) {
                    files.add(FileObjects.fileObjectFileObject(f, sourceRoot, null, null));
                }
            }
            assertTrue(JavacTool.create().getTask(null, output, null, null, null, files).call());
        }
        JavaSource js = JavaSource.forFileObject(source1);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                assertEquals(parameter.getDiagnostics().toString(), 0, parameter.getDiagnostics().size());
            }
        }, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        settings.commandLine = null;
        binaryForSource.result = null;
    }

    private FileObject createFile(String path, String content) throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, path);
        TestUtilities.copyStringToFile(file, content);

        return file;
    }

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");
        classRoot = workFO.createFolder("class");

        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, new FileObject[] {cp});
    }

    private static final CompilerSettingsImpl settings = new CompilerSettingsImpl();

    private static final class CompilerSettingsImpl extends CompilerSettings {
        private String commandLine;
        @Override
        protected String buildCommandLine(FileObject file) {
            return commandLine;
        }

    }

    private static final BinaryForSourceQueryImpl binaryForSource = new BinaryForSourceQueryImpl();

    private static final class BinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {

        private BinaryForSourceQuery.Result result;

        @Override
        public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot) {
            return result;
        }

    }

    private class OutputFileManager extends ForwardingJavaFileManager {

        public OutputFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, javax.tools.FileObject sibling) throws IOException {
            try {
                return new SimpleJavaFileObject(new URI("mem://" + className + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() throws IOException {
                        return FileUtil.createData(classRoot, className.replace(".", "/") + kind.extension).getOutputStream();
                    }
                };
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }
}
