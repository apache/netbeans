/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
