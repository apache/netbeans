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
public class NavigatorDeprecatedTest extends PhpNavigatorTestBase {

    public NavigatorDeprecatedTest(String testName) {
        super(testName);
    }

    public void testDeprecatedDeclarations() throws Exception {
        performTest("structure/deprecatedDeclarations");
    }

    public void testDeprecatedTypes() throws Exception {
        performTest("structure/deprecatedTypes");
    }

    public void testDeprecatedTypesForNullableTypes_01() throws Exception {
        performTest("structure/deprecatedTypesForNullableTypes_01");
    }

    public void testDeprecatedTypesForNullableTypes_02() throws Exception {
        performTest("structure/deprecatedTypesForNullableTypes_02");
    }

    public void testDeprecatedTypedFields() throws Exception {
        // GH-6310
        performTest("structure/deprecatedTypedFields");
    }

    public void testDeprecatedInheritedDeclarations() throws Exception {
        performTest("structure/deprecatedInheritedDeclarations");
    }

    public void testDeprecatedTypesForDNFReturnTypes_01() throws Exception {
        performTest("structure/php82/deprecatedDnfReturnTypes_01");
    }

    public void testDeprecatedTypesForDNFParameterTypes_01() throws Exception {
        performTest("structure/php82/deprecatedDnfParameterTypes_01");
    }

    public void testDeprecatedTypesForDNFFieldTypes_01() throws Exception {
        performTest("structure/php82/deprecatedDnfFieldTypes_01");
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/structure"))
            })
        );
    }

}
