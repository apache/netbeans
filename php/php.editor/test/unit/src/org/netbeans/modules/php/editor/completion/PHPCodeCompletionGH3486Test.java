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

public class PHPCodeCompletionGH3486Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionGH3486Test(String testName) {
        super(testName);
    }

    public void testGH3486_01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh3486/gh3486.php", "        parent::^protectedTraitMethod();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/gh3486"))
            })
        );
    }
}
