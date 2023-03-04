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

package org.netbeans.modules.groovy.editor.occurrences;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 * Tests related to the issue 226026.
 *
 * @author Martin Janicek
 */
public class AnnotationOccurrencesTest extends GroovyTestBase {

    public AnnotationOccurrencesTest(String testName) {
        super(testName);
    }

    public void testAnnotationImport() throws Exception {
        testCaretLine("import java.lang.Depr^ecated");
    }

    public void testAnnotationOnClass() throws Exception {
        testCaretLine("@Dep^recated class AnnotationOccurrencesTester {");
    }

    public void testAnnotationOnField() throws Exception {
        testCaretLine("    @Dep^recated protected String field");
    }

    public void testAnnotationOnProperty() throws Exception {
        testCaretLine("    @Depr^ecated String property");
    }

    public void testAnnotationOnConstructor() throws Exception {
        testCaretLine("    @Depr^ecated AnnotationOccurrencesTester() {}");
    }

    public void testAnnotationOnMethod() throws Exception {
        testCaretLine("    @Depr^ecated public String method() {}");
    }


    private void testCaretLine(String caretLine) throws Exception {
        checkOccurrences("testfiles/AnnotationOccurrencesTester.groovy", caretLine, true);
    }
}
