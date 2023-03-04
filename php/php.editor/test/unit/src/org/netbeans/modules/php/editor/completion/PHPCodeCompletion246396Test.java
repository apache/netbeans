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

public class PHPCodeCompletion246396Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion246396Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests246396/"))
            })
        );
    }

    public void testUseSelf() throws Exception {
        checkCompletion("testfiles/completion/lib/tests246396/issue246396.php", "        self::^publicStaticMethod();", false);
    }

    public void testUseStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests246396/issue246396.php", "        static::^privateStaticMethod();", false);
    }

    public void testUseTraitedSelf() throws Exception {
        checkCompletion("testfiles/completion/lib/tests246396/issue246396.php", "        self::^publicStaticTraitedMethod();", false);
    }

    public void testUseTraitedStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests246396/issue246396.php", "        static::^privateStaticTraitedMethod();", false);
    }

}
