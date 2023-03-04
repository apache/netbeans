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

/**
 *
 * @author Petr Hejl
 */
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class DuplicatesCCTest extends GroovyCCTestBase {

    String TEST_BASE = "testfiles/completion/duplicates/";

    public DuplicatesCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return ".";
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();
        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/testfiles/completion/duplicates")) }));
        return map;
    }

    public void testDuplicates1() throws Exception {
        checkCompletion(TEST_BASE + "b/B.groovy", "class B extends A^ {", true);
    }

    // TESTFAIL x
    public void testDuplicates2() throws Exception {
        checkCompletion(TEST_BASE + "c/C.groovy", "class C extends a.A^ {", true);
    }

    public void testDuplicates3() throws Exception {
        checkCompletion(TEST_BASE + "d/D.groovy", "class D extends java.util.A^ {", true);
    }
}
