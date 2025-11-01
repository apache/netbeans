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

package org.netbeans.modules.java.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

public class JavaCompletionTask125FeaturesTest extends CompletionTestBase {

    private static final String SOURCE_LEVEL = "25"; //NOI18N

    public JavaCompletionTask125FeaturesTest(String testName) {
        super(testName);
    }

    public void testCompactSourceFilesStart() throws Exception {
        performTest("CompactSourceFile", 829, null, "compactSourceFilesInsideClass.pass", SOURCE_LEVEL);
    }

    public void testCompactSourceFilesMiddle() throws Exception {
        performTest("CompactSourceFile", 856, null, "compactSourceFilesInsideClass.pass", SOURCE_LEVEL);
    }

    public void testCompactSourceFilesEnd() throws Exception {
        performTest("CompactSourceFile", 883, null, "compactSourceFilesInsideClass.pass", SOURCE_LEVEL);
    }

    public void testImportModuleKeyword() throws Exception {
        performTest("Import", 823, "import ", "staticAndModuleKeywordAndAllPackages.pass", SOURCE_LEVEL);
    }

    public void testImportModuleNoModuleName() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--limit-modules=java.base,jdk.compiler,jdk.javadoc,jdk.jartool");
        performTest("Import", 823, "import module ", "importModuleListFull.pass", SOURCE_LEVEL);
    }

    public void testImportModulePrefix1() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--limit-modules=java.base,jdk.compiler,jdk.javadoc,jdk.jartool");
        performTest("Import", 823, "import module jd", "importModuleListFullJDKPrefix.pass", SOURCE_LEVEL);
    }

    public void testImportModulePrefix2() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--limit-modules=java.base,jdk.compiler,jdk.javadoc,jdk.jartool");
        performTest("Import", 823, "import module jdk.", "importModuleListFullJDKPrefix.pass", SOURCE_LEVEL);
    }

    public void testImportModulePrefix3() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--limit-modules=java.base,jdk.compiler,jdk.javadoc,jdk.jartool");
        performTest("Import", 823, "import module jdk.j", "importModuleListFullJDKJPrefix.pass", SOURCE_LEVEL);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.clear();
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    @ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
    public static class TestCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

        private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return new CompilerOptionsQueryImplementation.Result() {
                @Override
                public List<? extends String> getArguments() {
                    return EXTRA_OPTIONS;
                }

                @Override
                public void addChangeListener(ChangeListener listener) {}

                @Override
                public void removeChangeListener(ChangeListener listener) {}
            };
        }
    }
}
