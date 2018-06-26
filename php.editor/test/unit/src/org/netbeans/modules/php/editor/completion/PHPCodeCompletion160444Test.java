/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPCodeCompletion160444Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion160444Test(String testName) {
        super(testName);
    }

    public void testIssue160444_1() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->^foo();", false);
    }

    public void testIssue160444_2() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj /**/->^foo();", false);
    }

    public void testIssue160444_3() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/ ->^foo();", false);
    }

    public void testIssue160444_4() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj /* aa */ ->^foo();", false);
    }

    public void testIssue160444_5() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /**/^foo();", false);
    }

    public void testIssue160444_6() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /**/ ^foo();", false);
    }

    public void testIssue160444_7() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/**/ ^foo();", false);
    }

    public void testIssue160444_8() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/**/^foo();", false);
    }

    public void testIssue160444_9() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /* aa */^foo();", false);
    }

    public void testIssue160444_10() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/-> /* aa */ ^foo();", false);
    }

    public void testIssue160444_11() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/* aa */^foo();", false);
    }

    public void testIssue160444_12() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->/* aa */ ^foo();", false);
    }

    public void testIssue160444_13() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /* aa */\n ^foo();", false);
    }

    public void testIssue160444_14() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /** aa */\n ^foo();", false);
    }

    public void testIssue160444_15() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n /**/\n ^foo();", false);
    }

    public void testIssue160444_16() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n // aa\n ^foo();", false);
    }

    public void testIssue160444_17() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n // aa\n^foo();", false);
    }

    public void testIssue160444_18() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "$obj/**/->\n// aa\n^foo();", false);
    }

    public void testIssue160444_19() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::^bar();", false);
    }

    public void testIssue160444_20() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment /**/::^bar();", false);
    }

    public void testIssue160444_21() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/ ::^bar();", false);
    }

    public void testIssue160444_22() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment /* aa */ ::^bar();", false);
    }

    public void testIssue160444_23() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /**/^bar();", false);
    }

    public void testIssue160444_24() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /**/ ^bar();", false);
    }

    public void testIssue160444_25() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/**/ ^bar();", false);
    }

    public void testIssue160444_26() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/**/^bar();", false);
    }

    public void testIssue160444_27() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /* aa */^bar();", false);
    }

    public void testIssue160444_28() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/:: /* aa */ ^bar();", false);
    }

    public void testIssue160444_29() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/* aa */^bar();", false);
    }

    public void testIssue160444_30() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::/* aa */ ^bar();", false);
    }

    public void testIssue160444_31() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /* aa */\n ^bar();", false);
    }

    public void testIssue160444_32() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /** aa */\n ^bar();", false);
    }

    public void testIssue160444_33() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n /**/\n ^bar();", false);
    }

    public void testIssue160444_34() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n // aa\n ^bar();", false);
    }

    public void testIssue160444_35() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n // aa\n^bar();", false);
    }

    public void testIssue160444_36() throws Exception {
        checkCompletion("testfiles/completion/lib/tests160444/issue160444.php", "InvocationComment/**/::\n// aa\n^bar();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests160444"))
            })
        );
    }

}
