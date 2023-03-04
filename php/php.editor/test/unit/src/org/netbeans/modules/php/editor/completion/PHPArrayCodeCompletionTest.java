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
public class PHPArrayCodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHPArrayCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testArrays_01() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "(new ArraysCc)->field[0]->^field;", false);
    }

    public void testArrays_02() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "$a[0]->^field;", false);
    }

    public void testArrays_03() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "arrayFunctionName()[0]->^field;", false);
    }

    public void testArrays_04() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "$b[0]->^field;", false);
    }

    public void testArrays_05() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "arrayFunctionTyped()[0]->^field;", false);
    }

    public void testArrays_06() throws Exception {
        checkCompletion("testfiles/completion/lib/arrays/arrays.php", "$c[0]->^field;", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/arrays/"))
            })
        );
    }

}
