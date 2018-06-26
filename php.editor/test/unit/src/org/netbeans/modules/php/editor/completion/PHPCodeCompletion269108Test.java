/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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

public class PHPCodeCompletion269108Test  extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion269108Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests269108/"))
            })
        );
    }

    // static
    public void testReturnStatic_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnStatic()->^testC()", false);
    }

    public void testReturnStatic_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnStatic()->^staticTestC()", false);
    }

    public void testReturnStatic_03() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnStatic()::^staticTestC()", false);
    }

    public void testReturnStatic_04() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnStatic()->^staticTestC()", false);
    }

    public void testReturnStatic_05() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnStatic()::^staticTestC()", false);
    }

    public void testReturnStatic_06() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnStatic()::^staticTestC()", false);
    }

    // self
    public void testReturnSelf_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnSelf()->^testA()", false);
    }

    public void testReturnSelf_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnSelf()->^staticTestA()", false);
    }

    public void testReturnSelf_03() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo C::staticReturnSelf()::^staticTestA()", false);
    }

    public void testReturnSelf_04() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnSelf()->^staticTestA()", false);
    }

    public void testReturnSelf_05() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c::staticReturnSelf()::^staticTestA()", false);
    }

    public void testReturnSelf_06() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnSelf()::^staticTestA()", false);
    }

    // this
    public void testReturnThis_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo (new C)->returnThis()->^testC()", false);
    }

    public void testReturnThis_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests269108/issue269108.php", "echo $c->returnThis()::^staticTestC()", false);
    }

}
