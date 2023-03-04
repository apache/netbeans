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

public class PHPCodeCompletionNb792Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionNb792Test(String testName) {
        super(testName);
    }

    public void testNb792_01() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something1($arg):a^ {}", false);
    }

    public void testNb792_02() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something2($arg = ''):a^ {}", false);
    }

    public void testNb792_03() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something3($arg = \"test\"):a^ {}", false);
    }

    public void testNb792_04() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something4($arg = array(\"test\")):a^ {}", false);
    }

    public void testNb792_05() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something5($arg = [\"test\"]):a^ {}", false);
    }

    public void testNb792_06() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something6($arg = 10):a^ {}", false);
    }

    public void testNb792_07() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something7($arg = -10):a^ {}", false);
    }

    public void testNb792_08() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something8($arg = -1.0):a^ {}", false);
    }

    public void testNb792_09() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something9($arg = true):a^ {}", false);
    }

    public void testNb792_10() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something10($arg = false):a^ {}", false);
    }

    public void testNb792_11() throws Exception {
        checkCompletion("testfiles/completion/lib/nb792/nb792.php", "function something11($arg = null):a^ {}", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/nb792"))
            })
        );
    }
}
