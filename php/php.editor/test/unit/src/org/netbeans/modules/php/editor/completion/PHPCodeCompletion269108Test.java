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

public class PHPCodeCompletion269108Test  extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion269108Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests269108/"))
            })
        );
    }

    // static
    public void testReturnStatic_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnStatic()->^testC()", false);
    }

    public void testReturnStatic_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnStatic()->^staticTestC()", false);
    }

    public void testReturnStatic_03() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnStatic()::^staticTestC()", false);
    }

    public void testReturnStatic_04() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnStatic()->^staticTestC()", false);
    }

    public void testReturnStatic_05() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnStatic()::^staticTestC()", false);
    }

    public void testReturnStatic_06() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnStatic()::^staticTestC()", false);
    }

    // self
    public void testReturnSelf_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnSelf()->^testA()", false);
    }

    public void testReturnSelf_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnSelf()->^staticTestA()", false);
    }

    public void testReturnSelf_03() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnSelf()::^staticTestA()", false);
    }

    public void testReturnSelf_04() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnSelf()->^staticTestA()", false);
    }

    public void testReturnSelf_05() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnSelf()::^staticTestA()", false);
    }

    public void testReturnSelf_06() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnSelf()::^staticTestA()", false);
    }

    // this
    public void testReturnThis_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnThis()->^testC()", false);
    }

    public void testReturnThis_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnThis()::^staticTestC()", false);
    }

}
