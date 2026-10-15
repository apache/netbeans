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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class NavigatorPHP84Test extends PhpNavigatorTestBase {

    public NavigatorPHP84Test(String testName) {
        super(testName);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        return new FileObject[]{FileUtil.toFileObject(new File(getDataDir(), getTestDirectoryPath()))};
    }

    private String getTestDirectoryPath() {
        return "/testfiles/structure/php84/" + getTestName();
    }

    private String getTestFilePath() {
        return "structure/php84/" + getTestName() + "/" + getTestName();
    }

    public void testPropertyHooks() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooks02() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooks03() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooks04() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooksInterface01() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooksTrait01() throws Exception {
        performTest(getTestFilePath());
    }

    public void testPropertyHooksAbstract() throws Exception {
        performTest(getTestFilePath());
    }
}
