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
package org.netbeans.modules.php.editor.csl;

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
public class DeprecatedFunctionsSemanticAnalysisTest extends DeprecatedSemanticAnalysisTestBase {

    public DeprecatedFunctionsSemanticAnalysisTest(String testName) {
        super(testName);
    }

    public void testDeprecatedPrivateClassMethod() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedFunction.php");
    }

    public void testDeprecatedAttributeInGlobal() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInGlobal.php");
    }

    public void testDeprecatedAttributeInNamespace01() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespace01.php");
    }

    public void testDeprecatedAttributeInNamespace02() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespace02.php");
    }

    public void testDeprecatedAttributeInNamespace03() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespace03.php");
    }

    public void testDeprecatedAttributeInNamespaceWithUse01() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespaceWithUse01.php");
    }

    public void testDeprecatedAttributeInNamespaceWithUse02() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespaceWithUse02.php");
    }

    public void testDeprecatedAttributeInNamespaceWithUse03() throws Exception {
        checkSemantic("testfiles/semantic/deprecatedFunctions/deprecatedAttributeInNamespaceWithUse03.php");
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/semantic/deprecatedFunctions"))
            })
        );
    }

}
