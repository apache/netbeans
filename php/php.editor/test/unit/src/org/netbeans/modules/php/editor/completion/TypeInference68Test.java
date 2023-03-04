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
 * @author tomslot
 */
public class TypeInference68Test extends PHPCodeCompletionTestBase {

    public TypeInference68Test(String testName) {
        super(testName);
    }

    public void testTypeInference1() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA1()->^", false);
    }
    public void testTypeInference2() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA2()->^", false);
    }
    public void testTypeInference3() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA3()->^", false);
    }
    public void testTypeInference4() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA4()->^", false);
    }
    public void testTypeInference5() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA5()->^", false);
    }
    public void testTypeInference7() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsA->methodA7()->^", false);
    }
    public void testTypeInference8() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncA()->^", false);
    }
    public void testTypeInference9() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncB()->^", false);
    }
    public void testTypeInference10() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncC()->^", false);
    }
    public void testTypeInference11() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncD()->^", false);
    }
    public void testTypeInference12() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncE()->^", false);
    }
    public void testTypeInference13() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncG()->^", false);
    }
    public void testTypeInference14() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncH()->^", false);
    }
    public void testTypeInference15() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncF()->^", false);
    }
    public void testTypeInference16() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "fncI()->^", false);
    }
        public void testTypeInference17() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsAForFlds->methodA1()->fld->^", false);
    }
    public void testTypeInference18() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/typeinference/typeinference.php", "$clsAForFlds->methodA1()->fld2->^", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/netbeans68version/typeinference"))
            })
        );
    }
}
