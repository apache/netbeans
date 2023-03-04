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

package org.netbeans.modules.groovy.editor.api.completion;

import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class NewVarsCCTest extends GroovyCCTestBase {

    String TEST_BASE = "testfiles/completion/";
    String BASE = TEST_BASE + "newvars/";

    public NewVarsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return ".";
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();
        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/testfiles/completion/newvars")) }));
        return map;
    }

    // test new-var suggestions based on identifiers

    public void testIdentifier1() throws Exception {
        checkCompletion(BASE + "Identifier1.groovy", "String str^", false);
    }

    public void testIdentifier2() throws Exception {
        checkCompletion(BASE + "Identifier1.groovy", "Long lo^", false);
    }

    public void testIdentifier3() throws Exception {
        checkCompletion(BASE + "Identifier2.groovy", "Boolean ^", false);
    }

    public void testIdentifier4() throws Exception {
        checkCompletion(BASE + "Identifier3.groovy", "StringBuffer ^", false);
    }

    // test field suggestions

    public void testCompletionField1_1() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    Identifier4 i^", false);
    }

    public void testCompletionField1_2() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    String ^", false);
    }

    public void testCompletionField1_3() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    private String ^", false);
    }

    // test primitve type suggestions

    public void testPrimitive1() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "boolean ^", false);
    }

    public void testPrimitive2() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "byte ^", false);
    }

    public void testPrimitive3() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "char ^", false);
    }

    public void testPrimitive4() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "double ^", false);
    }

    public void testPrimitive5() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "float ^", false);
    }

    public void testPrimitive6() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "int ^", false);
    }

    public void testPrimitive7() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "long ^", false);
    }

    public void testPrimitive8() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "short ^", false);
    }
}
