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
public class ParamDeclTypes68Test extends PHPCodeCompletionTestBase {

    public ParamDeclTypes68Test(String testName) {
        super(testName);
    }

    public void testParamDeclTypes() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c,$d) {^", false);
    }

    public void testParamDeclTypes2() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c,$d)^", false);
    }

    public void testParamDeclTypes3() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c,$d^", false);
    }

    public void testParamDeclTypes4() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c,$^", false);
    }

    public void testParamDeclTypes5() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c,^", false);
    }

    public void testParamDeclTypes6() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$c^", false);
    }

    public void testParamDeclTypes7() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,$^", false);
    }

    public void testParamDeclTypes8() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b,^", false);
    }

    public void testParamDeclTypes9() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$b^", false);
    }

    public void testParamDeclTypes10() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,$^", false);
    }

    public void testParamDeclTypes11() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a,^", false);
    }

    public void testParamDeclTypes12() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($a^", false);
    }

    public void testParamDeclTypes13() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc($^", false);
    }

    public void testParamDeclTypes14() throws Exception {
        checkCompletion("testfiles/completion/lib/netbeans68version/paramdecltypes/paramdecltypes.php", "function paramsfnc(^", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
                PhpSourcePath.SOURCE_CP,
                ClassPathSupport.createClassPath(new FileObject[]{
                    FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/netbeans68version/paramdecltypes"))
                }));
    }
}
