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
package org.netbeans.modules.groovy.editor.imports;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test class for fast import feature.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class FastImportTest extends GroovyTestBase {

    private static final CompletionItem.TypeItem aaaType = new CompletionItem.TypeItem("tester.AAA", "AAA", 0, ElementKind.CLASS);
    private static final CompletionItem.TypeItem bbbType = new CompletionItem.TypeItem("tester.BBB", "BBB", 0, ElementKind.CLASS);
    private static final CompletionItem.TypeItem dddType = new CompletionItem.TypeItem("tester.DDD", "DDD", 0, ElementKind.CLASS);

    public FastImportTest(String testName) {
        super(testName);
    }

    @Override
    protected Set<String> additionalSourceClassPath() {
        HashSet<String> sourceClassPath = new HashSet<String>();
        sourceClassPath.add("/testfiles/ccresult");

        return sourceClassPath;
    }
   
    public void testNoImportsYet_spaceAfterPackageStatement() throws Exception {
        checkResult("NoImportsSpaceAfterPackage", "    AAA^", aaaType);
    }

    // #228641
    public void testNoImportsYet_noSpaceAfterPackageStatement() throws Exception {
        checkResult("NoImportsNoSpaceAfterPackage", "    AAA^", aaaType);
    }

    public void testNoImportsYet_severalSpacesAfterPackageStatement() throws Exception {
        checkResult("NoImportsMoreSpacesAfterPackage", "    AAA^", aaaType);
    }

    // #234081
    public void testSeveralImports_firstPosition() throws Exception {
        checkResult("SeveralImportsFirstPosition", "    AAA^", aaaType);
    }

    // #234081
    public void testSeveralImports_middlePosition() throws Exception {
        checkResult("SeveralImportsMiddlePosition", "    BBB^", bbbType);
    }

    // #234081
    public void testSeveralImports_lastPosition() throws Exception {
        checkResult("SeveralImportsLastPosition", "    DDD^", dddType);
    }

    private void checkResult(String fileName, String caretLine, CompletionItem.TypeItem item) throws Exception {
        checkCompletionResult("testfiles/ccresult/" + fileName + ".groovy", caretLine, item);
    }
}
