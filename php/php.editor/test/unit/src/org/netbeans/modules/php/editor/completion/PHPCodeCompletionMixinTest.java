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


public class PHPCodeCompletionMixinTest extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionMixinTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/mixin/"))
            })
        );
    }

    // #241740 for @mixin tag
    public void testMixinTagType() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixin.php", " * @mixin ^C3", false);
    }

    public void testMixin() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixin.php", "$mixin->^publicMethodC1(); // CC", false);
    }

    public void testMixinWithStaticAccess() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixin.php", "Mixin::^publicStaticMethodC1(); // CC", false);
    }

    public void testMixinEnclosing() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixin.php", "        $this->^protectedMethodC1(); // CC", false);
    }

    public void testMixinEnclosingWithStaticAccess() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixin.php", "        Mixin::^protectedStaticMethodC1(); // CC", false);
    }

    public void testMixinFieldAccess_01() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixinFieldAccess.php", "$fieldAccess->C1->^publicMethodC1(); // CC", false);
    }

    public void testMixinFieldAccess_02() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixinFieldAccess.php", "$fieldAccess->Mixin->^publicMethodC1(); // CC", false);
    }

    public void testMixinFieldStaticAccess_01() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixinFieldAccess.php", "$fieldAccess->C1::^publicStaticMethodC1(); // CC", false);
    }

    public void testMixinFieldStaticAccess_02() throws Exception {
        checkCompletion("testfiles/completion/lib/mixin/mixinFieldAccess.php", "$fieldAccess->Mixin::^publicStaticMethodC1(); // CC", false);
    }

}
