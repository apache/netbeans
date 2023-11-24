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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.TestCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

public class CompilerOptionsQueryImplTest extends NbTestCase {

    public CompilerOptionsQueryImplTest(String name) {
        super(name);
    }

    public void testParseCommandLine1() throws Exception {
        File wd = getWorkDir();
        FileObject wdFO = FileUtil.toFileObject(wd);
        FileObject testFO = FileUtil.createData(wdFO, "Test.java");
        File testJar = new File(wd, "test.jar");
        CompilerOptionsQueryImpl query = new CompilerOptionsQueryImpl();
        NbCodeLanguageClient client = new TestCodeLanguageClient() {
        };

        query.setConfiguration(client, "-classpath " + testJar.getAbsolutePath() + " --module-path " + testJar.getAbsolutePath() + " --source 21 --enable-preview");

        AtomicReference<CompilerOptionsQueryImplementation.Result> optionsResult = new AtomicReference<>();
        AtomicInteger optionsResultModificationCount = new AtomicInteger();
        AtomicReference<ClassPath> compileCP = new AtomicReference<>();
        AtomicInteger compileCPModificationCount = new AtomicInteger();
        AtomicReference<ClassPath> compileModuleCP = new AtomicReference<>();
        AtomicInteger compileModuleCPModificationCount = new AtomicInteger();
        AtomicReference<SourceLevelQueryImplementation2.Result> sourceLevelResult = new AtomicReference<>();
        AtomicInteger sourceLevelResultModificationCount = new AtomicInteger();

        Lookups.executeWith(Lookups.fixed(client), () -> {
            optionsResult.set(query.getOptions(testFO));
            compileCP.set(query.findClassPath(testFO, ClassPath.COMPILE));
            compileModuleCP.set(query.findClassPath(testFO, JavaClassPathConstants.MODULE_COMPILE_PATH));
            sourceLevelResult.set(query.getSourceLevel(testFO));
        });

        optionsResult.get().addChangeListener(evt -> optionsResultModificationCount.incrementAndGet());
        assertEquals(Arrays.asList("-classpath", testJar.getAbsolutePath(), "--module-path", testJar.getAbsolutePath(), "--source", "21", "--enable-preview"),
                     optionsResult.get().getArguments());
        assertEquals(0, optionsResultModificationCount.get());

        compileCP.get().addPropertyChangeListener(evt -> compileCPModificationCount.incrementAndGet());
        assertEquals(testJar.getAbsolutePath(),
                     compileCP.get().toString(ClassPath.PathConversionMode.PRINT));
        assertEquals(0, compileCPModificationCount.get());

        compileModuleCP.get().addPropertyChangeListener(evt -> compileModuleCPModificationCount.incrementAndGet());
        assertEquals(testJar.getAbsolutePath(),
                     compileModuleCP.get().toString(ClassPath.PathConversionMode.PRINT));
        assertEquals(0, compileModuleCPModificationCount.get());

        sourceLevelResult.get().addChangeListener(evt -> sourceLevelResultModificationCount.incrementAndGet());
        assertEquals("21",
                     sourceLevelResult.get().getSourceLevel());
        assertEquals(0, sourceLevelResultModificationCount.get());

        query.setConfiguration(client, "-cp " + testJar.getAbsolutePath() + " -p " + testJar.getAbsolutePath() + " --source 17");

        assertEquals(Arrays.asList("-cp", testJar.getAbsolutePath(), "-p", testJar.getAbsolutePath(), "--source", "17"),
                     optionsResult.get().getArguments());
        assertEquals(1, optionsResultModificationCount.get());

        assertEquals(testJar.getAbsolutePath(),
                     compileCP.get().toString(ClassPath.PathConversionMode.PRINT));
        assertEquals(2, compileCPModificationCount.get());

        assertEquals(testJar.getAbsolutePath(),
                     compileModuleCP.get().toString(ClassPath.PathConversionMode.PRINT));
        assertEquals(2, compileModuleCPModificationCount.get());

        assertEquals("17",
                     sourceLevelResult.get().getSourceLevel());
        assertEquals(1, sourceLevelResultModificationCount.get());
    }

}
