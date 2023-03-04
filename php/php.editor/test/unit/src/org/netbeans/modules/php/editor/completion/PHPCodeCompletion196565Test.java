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
public class PHPCodeCompletion196565Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion196565Test(String testName) {
        super(testName);
    }

    public void testUseCase1_1() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase1.php", "ParentSt::staticFnc()->^;", false);
    }

    public void testUseCase1_2() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase1.php", "ChildSt::staticFnc()->^;", false);
    }

    public void testUseCase2_1() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase2.php", "ParentSe::staticFnc()->^;", false);
    }

    public void testUseCase2_2() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase2.php", "ChildSe::staticFnc()->^;", false);
    }

    public void testUseCase3() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase3.php", "$test->^;", false);
    }

    public void testUseCase4() throws Exception {
        checkCompletion("testfiles/completion/lib/tests196565/useCase4.php", "$c->^;", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests196565"))
            })
        );
    }

}
