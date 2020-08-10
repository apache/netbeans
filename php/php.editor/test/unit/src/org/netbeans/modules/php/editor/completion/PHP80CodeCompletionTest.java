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
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


public class PHP80CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP80CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.getDefault();
        super.setUp();
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php80/" + getTestDirName()))
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
        return String.format("testfiles/completion/lib/php80/%s/%s.php", getTestDirName(), fileName);
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Ex^ception) { // test1", false);
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Exception^A | ExceptionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (ExceptionA | Excep^tionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_04() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (^) { // test4", false);
    }

    // Allow ::class on Objects
    public void testClassNameLiteralOnObjects_01() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test::^class);", false);
    }

    public void testClassNameLiteralOnObjects_02() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($reference::cl^ass);", false);
    }

    public void testClassNameLiteralOnObjects_03() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump((new Test)::^class);", false);
    }

    public void testClassNameLiteralOnObjects_04() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump(test()::^class);", false);
    }

    public void testClassNameLiteralOnObjects_05() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test->newInstance()::c^lass)", false);
    }

    public void testClassNameLiteralOnObjects_06() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($this::^class);", false);
    }

    public void testClassNameLiteralOnObjects_07() throws Exception {
        // No completion items
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test->noReturnTypes()::^class)", false);
    }

    public void testMatchExpressionSimple01() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple01"), "$result = match^", false);
    }

    public void testMatchExpressionSimple02() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple02"), "$result = match (^)", false);
    }

    public void testMatchExpressionSimple03() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple03"), "    ^", false);
    }

    public void testMatchExpressionSimple04() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple04"), "    \"match test\", ^", false);
    }

    public void testMatchExpressionSimple05_01() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => ^", false);
    }

    public void testMatchExpressionSimple05_02() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => matchT^est(),", false);
    }

    public void testMatchExpressionSimple05_03() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => matchT^", false);
    }

    public void testMatchExpressionSimple06() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple06"), "    MATCH_^", false);
    }

    public void testMatchExpressionInClass_01() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "        return match (^$state) {", false);
    }

    public void testMatchExpressionInClass_02() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            ^MatchExpression::START => self::$start,", false);
    }

    public void testMatchExpressionInClass_03() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::^START => self::$start,", false);
    }

    public void testMatchExpressionInClass_04() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::START => ^self::$start,", false);
    }

    public void testMatchExpressionInClass_05() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::START => self::^$start,", false);
    }

    public void testMatchExpressionInClass_06() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::SUSP^END => $this->suspend,", false);
    }

    public void testMatchExpressionInClass_07() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::SUSPEND => $this->^suspend,", false);
    }

    public void testMatchExpressionInClass_08() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::STOP,^ => $this->stopState(),", false);
    }

    public void testMatchExpressionInClassSimple01() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple01"), "        return match (^)", false);
    }

    public void testMatchExpressionInClassSimple02() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple02"), "            ^", false);
    }

    public void testMatchExpressionInClassSimple03() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple03"), "            $this->suspend => ^", false);
    }
}
