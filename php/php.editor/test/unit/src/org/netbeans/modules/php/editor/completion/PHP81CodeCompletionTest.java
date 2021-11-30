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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP81CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP81CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php81/" + getTestDirName()))
            })
        );
    }

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php81/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    public void testNeverReturnType_Function01() throws Exception {
        checkCompletion("neverReturnType", "function returnType(): ^never { // func");
    }

    public void testNeverReturnType_Function02() throws Exception {
        checkCompletion("neverReturnType", "function returnType(): ne^ver { // func");
    }

    public void testNeverReturnType_Function03() throws Exception {
        checkCompletion("neverReturnType", "function invalidInParameter(ne^ver $never): never { // func");
    }

    public void testNeverReturnType_Class01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never { // class");
    }

    public void testNeverReturnType_Class02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): neve^r { // class");
    }

    public void testNeverReturnType_Class03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never { // class");
    }

    public void testNeverReturnType_Trait01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never { // trait");
    }

    public void testNeverReturnType_Trait02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): neve^r { // trait");
    }

    public void testNeverReturnType_Trait03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never { // trait");
    }

    public void testNeverReturnType_Interface01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never; // interface");
    }

    public void testNeverReturnType_Interface02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ne^ver; // interface");
    }

    public void testNeverReturnType_Interface03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never; // interface");
    }

}
