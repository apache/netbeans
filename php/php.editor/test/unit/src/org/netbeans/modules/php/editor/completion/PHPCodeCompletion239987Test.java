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

public class PHPCodeCompletion239987Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion239987Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
                PhpSourcePath.SOURCE_CP,
                ClassPathSupport.createClassPath(new FileObject[]{
            FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests239987/"))
        })
        );
    }

    public void testReturnTagAndStatement() throws Exception {
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$base->publicBaseMethod()->^multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod(); // test", false);
    }

    public void testOnlyReturnStatements() throws Exception {
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$base->publicBaseMethod()->multipleReturnStatements()->^publicDelegateMethod()->publicDelegateMethod(); // test", false);
    }

    public void testOnlyReturnTags() throws Exception {
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$base->multipleReturnTags()->^multipleReturnStatements(); // test", false);
    }

    public void testReturnTagsAndStatements() throws Exception {
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$base->multipleReturnTagsAndStatements()->^publicBaseVoidMethod(); // test", false);
    }

    public void testCallerDependentTypes() throws Exception {
        // should be shown all methods of ExClass
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$exClass->publicBaseMethod()->^publicExMethod()->multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod(); // test", false);
    }

    public void testCallerDependentTypes_02() throws Exception {
        // should be shown all public methods of ExClass, BaseClass and Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$exClass->publicBaseMethod()->publicExMethod()->multipleReturnStatements()->^publicDelegateMethod()->publicDelegateMethod(); // test", false);
    }

    public void testCallerDependentTypes_03() throws Exception {
        // should be shown only public methods of Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$exClass->publicBaseMethod()->publicExMethod()->multipleReturnStatements()->publicDelegateMethod()->^publicDelegateMethod(); // test", false);
    }

    public void testCallerDependentTypes_04() throws Exception {
        // should be shown all methods of ExClass, public protected methods of BaseClass and public methods of Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$this->multipleReturnStatements()->^privateExMethod()->multipleReturnStatements(); // test", false);
    }

    public void testCallerDependentTypes_05() throws Exception {
        // should be shown all methods of ExClass, public protected methods of BaseClass and public methods of Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$this->multipleReturnTagsAndStatements()->^publicDelegateMethod()->publicDelegateMethod(); // test", false);
    }

    public void testCallerDependentTypes_06() throws Exception {
        // should be shown only public methods of Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$this->multipleReturnTagsAndStatements()->publicDelegateMethod()->^publicDelegateMethod(); // test", false);
    }

    public void testCallerDependentTypes_07() throws Exception {
        // should be shown all public methods of ExClass, BaseClass and Delegate
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$exClass->multipleReturnTags()->^multipleReturnStatements(); //test", false);
    }

    public void testMethodInvocationReturnType() throws Exception {
        // should be shown all methods of ExClass and public protected methods of BaseClass
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$this->multipleReturnStatements()->privateExMethod()->^multipleReturnStatements(); // test", false);
    }

    public void testReturnTagWithOldThis() throws Exception {
        checkCompletion("testfiles/completion/lib/tests239987/issue239987.php", "$base->returnTagWithOldThis()->^multipleReturnStatements(); // test", false);
    }

}
