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

public class PHPCodeCompletionGH6909Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionGH6909Test(String testName) {
        super(testName);
    }

    public void testGH6909_Instance01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->Type()->^test;", false);
    }

    public void testGH6909_Instance02() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->mtd()->^test;", false);
    }

    public void testGH6909_Instance03() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->fn()->^test;", false);
    }

    public void testGH6909_Instance04() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->fld()->^test;", false);
    }

    public void testGH6909_Instance05() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->var()->^test;", false);
    }

    public void testGH6909_Instance06() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->array()->^test;", false);
    }

    public void testGH6909_Instance07() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->type->^test;", false);
    }

    public void testGH6909_Static01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::type()->^test;", false);
    }

    public void testGH6909_Static02() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::mtd()->^test;", false);
    }

    public void testGH6909_Static03() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::fn()->^test;", false);
    }

    public void testGH6909_Static04() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::fld()->^test;", false);
    }

    public void testGH6909_Static05() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        GH6909Static::var()->^test;", false);
    }

    public void testGH6909_Static06() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        static::array()->^test;", false);
    }

    public void testGH6909_InstanceReturnType01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->Type()->^example;", false);
    }

    public void testGH6909_InstanceReturnType02() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->Type()::^EXAMPLE;", false);
    }

    public void testGH6909_StaticReturnType01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::Type()->^example;", false);
    }

    public void testGH6909_StaticReturnType02() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        self::Type()::^EXAMPLE;", false);
    }

    public void testGH6909_FieldType01() throws Exception {
        checkCompletion("testfiles/completion/lib/gh6909/gh6909.php", "        $this->type->^example;", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/gh6909"))
            })
        );
    }
}
