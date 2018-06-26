/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.completion;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCompletionTest extends LatteCompletionTestBase {

    public LatteCompletionTest(String testName) {
        super(testName);
    }

    public void testCompletion_01() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_01.latte", "{^}", false);
    }

    public void testCompletion_02() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_02.latte", "{fo^}", false);
    }

    public void testCompletion_03() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_03.latte", "{$item|^}", false);
    }

    public void testCompletion_04() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_04.latte", "{$item|l^}", false);
    }

    public void testCompletion_05() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_05.latte", "{^} <!-- first -->", false);
    }

    public void testCompletion_06() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.latte", "{^}{$bar}{}", false);
    }

    public void testCompletion_07() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.latte", "{}{$bar}{^}", false);
    }

    public void testCompletion_08() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_08.latte", "<!-- second --> {^}", false);
    }

    public void testIterator_01() throws Exception {
        checkCompletion("testfiles/completion/testIterator_01.latte", "{$iterator->^}", false);
    }

    public void testIterator_02() throws Exception {
        checkCompletion("testfiles/completion/testIterator_02.latte", "{$iterator->f^}", false);
    }

    public void testVariable_01() throws Exception {
        checkCompletion("testfiles/completion/testVariable_01.latte", "{$^}", false);
    }

    public void testVariable_02() throws Exception {
        checkCompletion("testfiles/completion/testVariable_02.latte", "{$b^}", false);
    }

    public void testVariable_03() throws Exception {
        checkCompletion("testfiles/completion/testVariable_03.latte", "{$x^}", false);
    }

    public void testVariable_04() throws Exception {
        checkCompletion("testfiles/completion/testVariable_04.latte", "{foreach $b^ as item}", false);
    }

    public void testEndMacro_01() throws Exception {
        checkCompletion("testfiles/completion/testEndMacro_01.latte", "{/^}", false);
    }

    public void testEndMacro_02() throws Exception {
        checkCompletion("testfiles/completion/testEndMacro_02.latte", "{/c^}", false);
    }

}
