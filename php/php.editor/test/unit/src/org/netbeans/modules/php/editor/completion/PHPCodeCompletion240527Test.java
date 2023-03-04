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

public class PHPCodeCompletion240527Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion240527Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests240527/"))
            })
        );
    }

    // Instance does not contain class Magic Constant
    public void testStaticAccessFromInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$staticAccessTest::^publicStaticSAMethod(); // test", false);
    }

    public void testStaticAccessFromInstanceArray() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$staticAccessTestArray[0]::^publicStaticSAMethod(); // test", false);
    }

    public void testStaticAccessFromEnclosedInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$enclosedAccess::^privateStaticSAMethod(); // test", false);
    }

    public void testStaticAccessFromExtendedInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$extendedClass::^publicStaticExMethod(); // test", false);
    }

    public void testStaticAccessFromEnclosedExtendedInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$enclosed::^privateStaticExMethod(); // test", false);
    }

    public void testStaticAccessFromThis() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$this::^privateStaticExMethod(); // test", false);
    }

    // ClassName contains class Magic Constant
    public void testStaticAccessFromClassName() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "StaticAccessTest::^publicStaticSAMethod(); // test", false);
    }

    public void testStaticAccessFromExtendedClassName() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "ExClass::^publicStaticExMethod(); // test", false);
    }

    public void testFluentDynamicAccess() throws Exception {
        checkCompletion("testfiles/completion/lib/tests240527/issue240527.php", "$extendedClass::newInstance()->^publicExMethod(); // test", false);
    }

}
