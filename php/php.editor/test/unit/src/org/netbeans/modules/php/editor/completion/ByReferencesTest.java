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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ByReferencesTest extends PHPCodeCompletionTestBase {

    public ByReferencesTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/byReferences/"))
            })
        );
    }

    public void testByReferences01() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort1(^Collection $data) {", false);
    }

    public void testByReferences02() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort1(Colle^ction $data) {", false);
    }

    public void testByReferences03() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort2(^array &$data) {", false);
    }

    public void testByReferences04() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort2(arr^ay &$data) {", false);
    }

    public void testByReferences05() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort3(^MyClass &$data) {", false);
    }

    public void testByReferences06() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort3(MyC^lass &$data) {", false);
    }

    public void testByReferences07() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort4(^$data) {", false);
    }

    public void testByReferences08() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort5(^&$data) {", false);
    }

    public void testByReferences09() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort5(&^$data) {", false);
    }

    public void testByReferences10() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort6(^&$data) {", false);
    }

    public void testByReferences11() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort6(&^$data) {", false);
    }

    public void testByReferences12() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(^array &$data1, Collection &$data2) {", false);
    }

    public void testByReferences13() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(ar^ray &$data1, Collection &$data2) {", false);
    }

    public void testByReferences14() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(array &$data1, ^Collection &$data2) {", false);
    }

    public void testByReferences15() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort7(array &$data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences16() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(^array &$data1, Collection &$data2) {", false);
    }

    public void testByReferences17() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(arr^ay &$data1, Collection &$data2) {", false);
    }

    public void testByReferences18() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(array &$data1, ^Collection &$data2) {", false);
    }

    public void testByReferences19() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort8(array &$data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences20() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(^array $data1, Collection &$data2) {", false);
    }

    public void testByReferences21() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(arr^ay $data1, Collection &$data2) {", false);
    }

    public void testByReferences22() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(array $data1, ^Collection &$data2) {", false);
    }

    public void testByReferences23() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort9(array $data1, Coll^ection &$data2) {", false);
    }

    public void testByReferences24() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(^array &$data1, Collection $data2) {", false);
    }

    public void testByReferences25() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(arr^ay &$data1, Collection $data2) {", false);
    }

    public void testByReferences26() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(array &$data1, ^Collection $data2) {", false);
    }

    public void testByReferences27() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort10(array &$data1, Colle^ction $data2) {", false);
    }

    public void testByReferences28() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(^array $data1, Collection &$data2) {", false);
    }

    public void testByReferences29() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(arr^ay $data1, Collection &$data2) {", false);
    }

    public void testByReferences30() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(array $data1, ^Collection &$data2) {", false);
    }

    public void testByReferences31() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function my_sort11(array $data1, Collec^tion &$data2) {", false);
    }

    public void testByReferences32() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(^array $data1, Collection $data2) {", false);
    }

    public void testByReferences33() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(arr^ay $data1, Collection $data2) {", false);
    }

    public void testByReferences34() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(array $data1, ^Collection $data2) {", false);
    }

    public void testByReferences35() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort12(array $data1, Colle^ction $data2) {", false);
    }

    public void testByReferences36() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort13(array &$data1, ^Collection &...$data2) {", false);
    }

    public void testByReferences37() throws Exception {
        checkCompletion("testfiles/completion/lib/byReferences/byReferences.php", "function &my_sort13(array &$data1, Coll^ection &...$data2) {", false);
    }

}
