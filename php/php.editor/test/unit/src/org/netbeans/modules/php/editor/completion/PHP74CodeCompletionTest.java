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


public class PHP74CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP74CodeCompletionTest(String testName) {
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
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php74/" + getTestDirName()))
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
        return String.format("testfiles/completion/lib/php74/%s/%s.php", getTestDirName(), fileName);
    }

    // class
    // method invocations
    public void testTypedProperties20Class_01() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "        $this->myClass->^publicTestMethod();", false);
    }

    public void testTypedProperties20Class_02() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "        $this->myClass2->^publicTestMethod();", false);
    }

    public void testTypedProperties20Class_03() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "        $this->myClass3->^publicTestMethod();", false);
    }

    public void testTypedProperties20Class_04() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "        $this->myClass4->^publicTestMethod();", false);
    }

    public void testTypedProperties20Class_05() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "        $this::$staticMyClass->^publicTestMethod();", false);
    }

    // complete field type
    public void testTypedProperties20Class_06() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public ^MyClass $myClass;", false);
    }

    public void testTypedProperties20Class_06_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Class"), "    public ^MyClass $myClass;", false);
    }

    public void testTypedProperties20Class_07() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public MyClass ^$myClass;", false);
    }

    public void testTypedProperties20Class_08() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public static ^MyClass $staticMyClass;", false);
    }

    public void testTypedProperties20Class_08_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Class"), "    public static ^MyClass $staticMyClass;", false);
    }

    public void testTypedProperties20Class_09() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public int ^$int;", false);
    }

    public void testTypedProperties20Class_10() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public ?^MyClass $myClass2;", false);
    }

    public void testTypedProperties20Class_11() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    private \\Bar\\MyC^lass $myClass3;", false);
    }

    public void testTypedProperties20Class_12() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    private \\Bar\\MyClass^ $myClass3;", false);
    }

    public void testTypedProperties20Class_13() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    protected ?\\^Bar\\MyClass $myClass4;", false);
    }

    public void testTypedProperties20Class_14() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    protected ?\\Bar^\\MyClass $myClass4;", false);
    }

    public void testTypedProperties20Class_15() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    protected ?\\Bar\\^MyClass $myClass4;", false);
    }

    public void testTypedProperties20Class_16() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public ^$test;", false);
    }

    public void testTypedProperties20Class_16_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Class"), "    public ^$test;", false);
    }

    public void testTypedProperties20Class_17() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    var ^string $string;", false);
    }

    public void testTypedProperties20Class_17_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Class"), "    var ^string $string;", false);
    }

    public void testTypedProperties20Class_18() throws Exception {
        checkCompletion(getTestPath("typedProperties20Class"), "    public const ^CONSTANT = \"constant\";", false);
    }

    // trait
    public void testTypedProperties20Trait_01() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "        $this->myClass->^publicTestMethod();", false);
    }

    public void testTypedProperties20Trait_02() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "        $this->myClass2->^publicTestMethod();", false);
    }

    public void testTypedProperties20Trait_03() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "        $this->myClass3->^publicTestMethod();", false);
    }

    public void testTypedProperties20Trait_04() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "        $this->myClass4->^publicTestMethod();", false);
    }

    public void testTypedProperties20Trait_05() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "        $this::$staticMyClass->^publicTestMethod();", false);
    }

    // complete field type
    public void testTypedProperties20Trait_06() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ^MyClass $myClass;", false);
    }

    public void testTypedProperties20Trait_06_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ^MyClass $myClass;", false);
    }

    public void testTypedProperties20Trait_07() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public MyClass ^$myClass;", false);
    }

    public void testTypedProperties20Trait_08() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public static ^MyClass $staticMyClass;", false);
    }

    public void testTypedProperties20Trait_08_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Trait"), "    public static ^MyClass $staticMyClass;", false);
    }

    public void testTypedProperties20Trait_09() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ?^string $string;", false);
    }

    public void testTypedProperties20Trait_10() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ?string ^$string;", false);
    }

    public void testTypedProperties20Trait_11() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ?^MyClass $myClass2;", false);
    }

    public void testTypedProperties20Trait_12() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    private \\Bar\\MyC^lass $myClass3;", false);
    }

    public void testTypedProperties20Trait_13() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    private \\Bar\\MyClass^ $myClass3;", false);
    }

    public void testTypedProperties20Trait_14() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    protected ?\\^Bar\\MyClass $myClass4;", false);
    }

    public void testTypedProperties20Trait_15() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    protected ?\\Bar^\\MyClass $myClass4;", false);
    }

    public void testTypedProperties20Trait_16() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    protected ?\\Bar\\^MyClass $myClass4;", false);
    }

    public void testTypedProperties20Trait_17() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ^$test;", false);
    }

    public void testTypedProperties20Trait_17_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Trait"), "    public ^$test;", false);
    }

    public void testTypedProperties20Trait_18() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    var ^string $string;", false);
    }

    public void testTypedProperties20Trait_18_php73() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.PHP_73;
        checkCompletion(getTestPath("typedProperties20Trait"), "    var ^string $string;", false);
    }

    public void testTypedProperties20Trait_19() throws Exception {
        checkCompletion(getTestPath("typedProperties20Trait"), "    public const ^CONSTANT = \"constant\";", false);
    }

}
