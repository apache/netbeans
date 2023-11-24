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
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

public class StringTemplatesTest extends CompletionTestBase {

    public StringTemplatesTest(String testName) {
        super(testName);
    }

    public void testStringTemplates1() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 922, "\"\\{str.", "stringContent.pass", getLatestSource());
    }

    public void testStringTemplates2() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 999, "str.", "stringContent.pass", getLatestSource());
    }

    public void testStringTemplates3() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 922, "\"\\{\"\\{str.", "stringContent.pass", getLatestSource());
    }

    public void testStringTemplates4() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 922, "\"\\{", "templateStart.pass", getLatestSource());
    }

    public void testStringTemplatesBlock1() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 922, "\"\"\"\n\\{str.", "stringContent.pass", getLatestSource());
    }

    public void testStringTemplatesBlock2() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("StringTemplates", 1101, "str.", "stringContent.pass", getLatestSource());
    }

    private String getLatestSource() {
        return SourceVersion.latest().name().substring(SourceVersion.latest().name().indexOf("_") + 1);
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
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }
    }
}
