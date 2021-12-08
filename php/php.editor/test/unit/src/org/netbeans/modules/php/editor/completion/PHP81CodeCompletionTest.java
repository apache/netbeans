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

    public void testReadonlyPropertiesTyping01() throws Exception {
        checkCompletion("readonlyPropertiesTyping01", "    ^// test");
    }

    public void testReadonlyPropertiesTyping02() throws Exception {
        checkCompletion("readonlyPropertiesTyping02", "    read^");
    }

    public void testReadonlyPropertiesTyping03() throws Exception {
        checkCompletion("readonlyPropertiesTyping03", "readonly ^");
    }

    public void testReadonlyPropertiesTyping04() throws Exception {
        checkCompletion("readonlyPropertiesTyping04", "    readonly p^");
    }

    public void testReadonlyPropertiesTyping05() throws Exception {
        checkCompletion("readonlyPropertiesTyping05", "readonly public ^");
    }

    public void testReadonlyPropertiesTyping06() throws Exception {
        checkCompletion("readonlyPropertiesTyping06", "    p^");
    }

    public void testReadonlyPropertiesTyping07() throws Exception {
        checkCompletion("readonlyPropertiesTyping07", "    public ^");
    }

    public void testReadonlyPropertiesTyping08() throws Exception {
        checkCompletion("readonlyPropertiesTyping08", "    public read^");
    }

    public void testReadonlyPropertiesTyping09() throws Exception {
        checkCompletion("readonlyPropertiesTyping09", "    public readonly ^");
    }

    public void testReadonlyPropertiesTyping10() throws Exception {
        checkCompletion("readonlyPropertiesTyping10", "    public readonly ?^");
    }

    public void testReadonlyPromotedPropertiesTyping01() throws Exception {
        checkCompletion("readonlyPropertiesTyping01", "        ^// test");
    }

    public void testReadonlyPromotedPropertiesTyping02() throws Exception {
        checkCompletion("readonlyPropertiesTyping02", "        read^");
    }

    public void testReadonlyPromotedPropertiesTyping03() throws Exception {
        checkCompletion("readonlyPropertiesTyping03", "            readonly ^");
    }

    public void testReadonlyPromotedPropertiesTyping04() throws Exception {
        checkCompletion("readonlyPropertiesTyping04", "            readonly p^");
    }

    public void testReadonlyPromotedPropertiesTyping05() throws Exception {
        checkCompletion("readonlyPropertiesTyping05", "            readonly public ^");
    }

    public void testReadonlyPromotedPropertiesTyping06() throws Exception {
        checkCompletion("readonlyPropertiesTyping06", "            ^// test");
    }

    public void testReadonlyPromotedPropertiesTyping07() throws Exception {
        checkCompletion("readonlyPropertiesTyping07", "            read^//test");
    }

    public void testReadonlyPromotedPropertiesTyping08() throws Exception {
        checkCompletion("readonlyPropertiesTyping08", "            readonly public int|^//test");
    }

    public void testReadonlyPromotedPropertiesTyping09() throws Exception {
        checkCompletion("readonlyPropertiesTyping09", "            p^");
    }

    public void testReadonlyPromotedPropertiesTyping10() throws Exception {
        checkCompletion("readonlyPropertiesTyping10", "            private ^");
    }

    public void testReadonlyPromotedPropertiesTyping11() throws Exception {
        checkCompletion("readonlyPropertiesTyping11", "            private readon^");
    }

    public void testReadonlyPromotedPropertiesTyping12() throws Exception {
        checkCompletion("readonlyPropertiesTyping12", "            private readonly ^");
    }

    public void testReadonlyPromotedPropertiesTyping13() throws Exception {
        checkCompletion("readonlyPropertiesTyping13", "            private readonly str^");
    }

    public void testReadonlyProperties_01() throws Exception {
        checkCompletion("readonlyProperties", "    publ^ic readonly int $publicReadonly;");
    }

    public void testReadonlyProperties_02() throws Exception {
        checkCompletion("readonlyProperties", "    public reado^nly int $publicReadonly;");
    }

    public void testReadonlyProperties_03() throws Exception {
        checkCompletion("readonlyProperties", "    public readonly in^t $publicReadonly;");
    }

    public void testReadonlyProperties_04() throws Exception {
        checkCompletion("readonlyProperties", "    private readonly ?strin^g $privateReadonly;");
    }

    public void testReadonlyProperties_05() throws Exception {
        checkCompletion("readonlyProperties", "    protected readonly stri^ng|int $protectedReadonly;");
    }

    public void testReadonlyProperties_06() throws Exception {
        checkCompletion("readonlyProperties", "    protected readonly string|in^t $protectedReadonly;");
    }

    public void testReadonlyProperties_07() throws Exception {
        checkCompletion("readonlyProperties", "    readon^ly public string $readonlyPublic;");
    }

    public void testReadonlyProperties_08() throws Exception {
        checkCompletion("readonlyProperties", "    readonly publi^c string $readonlyPublic;");
    }

    public void testReadonlyProperties_09() throws Exception {
        checkCompletion("readonlyProperties", "    readonly public str^ing $readonlyPublic;");
    }

    public void testReadonlyProperties_10() throws Exception {
        checkCompletion("readonlyProperties", "    readonly private ?stri^ng $readonlyPrivate;");
    }

    public void testReadonlyProperties_11() throws Exception {
        checkCompletion("readonlyProperties", "    readonly protected in^t|string $readonlyProtected;");
    }

    public void testReadonlyProperties_12() throws Exception {
        checkCompletion("readonlyProperties", "    readonly protected int|str^ing $readonlyProtected;");
    }

    public void testReadonlyProperties_13() throws Exception {
        checkCompletion("readonlyProperties", "        publ^ic readonly int|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_14() throws Exception {
        checkCompletion("readonlyProperties", "        public reado^nly int|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_15() throws Exception {
        checkCompletion("readonlyProperties", "        public readonly i^nt|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_16() throws Exception {
        checkCompletion("readonlyProperties", "        public readonly int|str^ing $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_17() throws Exception {
        checkCompletion("readonlyProperties", "        private readonly arr^ay $promotedPrivateReadonly = [],");
    }

    public void testReadonlyProperties_18() throws Exception {
        checkCompletion("readonlyProperties", "        protected readonly ?str^ing $promotedProtectedReadonly = \"test\",");
    }

    public void testReadonlyProperties_19() throws Exception {
        checkCompletion("readonlyProperties", "        readonly public int|st^ring $promotedReadonlyPublic = 0,");
    }

    public void testReadonlyProperties_20() throws Exception {
        checkCompletion("readonlyProperties", "        readonly private arra^y $promotedReadonlyPrivate = [],");
    }

    public void testReadonlyProperties_21() throws Exception {
        checkCompletion("readonlyProperties", "        readonly protected ?stri^ng $promotedReadonlyProtected = \"test\",");
    }

}
