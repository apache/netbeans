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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPCodeCompletion160444Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion160444Test(String testName) {
        super(testName);
    }

    public void testIssue160444_1() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->^foo();", false);
    }

    public void testIssue160444_2() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj /**/->^foo();", false);
    }

    public void testIssue160444_3() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/ ->^foo();", false);
    }

    public void testIssue160444_4() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj /* aa */ ->^foo();", false);
    }

    public void testIssue160444_5() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /**/^foo();", false);
    }

    public void testIssue160444_6() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /**/ ^foo();", false);
    }

    public void testIssue160444_7() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/**/ ^foo();", false);
    }

    public void testIssue160444_8() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/**/^foo();", false);
    }

    public void testIssue160444_9() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /* aa */^foo();", false);
    }

    public void testIssue160444_10() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /* aa */ ^foo();", false);
    }

    public void testIssue160444_11() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/* aa */^foo();", false);
    }

    public void testIssue160444_12() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/* aa */ ^foo();", false);
    }

    public void testIssue160444_13() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /* aa */\n ^foo();", false);
    }

    public void testIssue160444_14() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /** aa */\n ^foo();", false);
    }

    public void testIssue160444_15() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /**/\n ^foo();", false);
    }

    public void testIssue160444_16() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n // aa\n ^foo();", false);
    }

    public void testIssue160444_17() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n // aa\n^foo();", false);
    }

    public void testIssue160444_18() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n// aa\n^foo();", false);
    }

    public void testIssue160444_19() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::^bar();", false);
    }

    public void testIssue160444_20() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment /**/::^bar();", false);
    }

    public void testIssue160444_21() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/ ::^bar();", false);
    }

    public void testIssue160444_22() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment /* aa */ ::^bar();", false);
    }

    public void testIssue160444_23() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /**/^bar();", false);
    }

    public void testIssue160444_24() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /**/ ^bar();", false);
    }

    public void testIssue160444_25() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/**/ ^bar();", false);
    }

    public void testIssue160444_26() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/**/^bar();", false);
    }

    public void testIssue160444_27() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /* aa */^bar();", false);
    }

    public void testIssue160444_28() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /* aa */ ^bar();", false);
    }

    public void testIssue160444_29() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/* aa */^bar();", false);
    }

    public void testIssue160444_30() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/* aa */ ^bar();", false);
    }

    public void testIssue160444_31() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /* aa */\n ^bar();", false);
    }

    public void testIssue160444_32() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /** aa */\n ^bar();", false);
    }

    public void testIssue160444_33() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /**/\n ^bar();", false);
    }

    public void testIssue160444_34() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n // aa\n ^bar();", false);
    }

    public void testIssue160444_35() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n // aa\n^bar();", false);
    }

    public void testIssue160444_36() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n// aa\n^bar();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests160444"))
            })
        );
    }

}
