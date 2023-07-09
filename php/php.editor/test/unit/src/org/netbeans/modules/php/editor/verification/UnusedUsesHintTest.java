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
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class UnusedUsesHintTest extends PHPHintsTestBase {

    public UnusedUsesHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "UnusedUsesHint/" + transformTestMethodNameToDirectory();
    }

    public void testUnusedUsesHint() throws Exception {
        checkHints(new UnusedUsesHint(), "testUnusedUsesHint.php");
    }

    public void testUnusedUsesHintWithTypedProperties() throws Exception {
        // PHP 7.4
        checkHints(new UnusedUsesHint(), "testUnusedUsesHintWithTypedProperties.php");
    }

    public void testGH6075() throws Exception {
        checkHints(new UnusedUsesHint(), "testGH6075.php");
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestDirectory()))
            })
        );
    }

    private String transformTestMethodNameToDirectory() {
        return getName().replace('_', '/') + "/";
    }
}
