/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.twig.editor.format;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigIndenterTest extends TwigIndenterTestBase {

    public TwigIndenterTest(String testName) {
        super(testName);
    }

    public void testIssue230506_01() throws Exception {
        indent("testIssue230506_01");
    }

    public void testIssue230506_02() throws Exception {
        indent("testIssue230506_02");
    }

    public void testIssue230506_03() throws Exception {
        indent("testIssue230506_03");
    }

    public void testIssue230506_04() throws Exception {
        indent("testIssue230506_04");
    }

    public void testIssue230506_05() throws Exception {
        indent("testIssue230506_05");
    }

    public void testIssue230506_06() throws Exception {
        indent("testIssue230506_06");
    }

    public void testIssue230506_07() throws Exception {
        indent("testIssue230506_07");
    }

    public void testIssue230506_08() throws Exception {
        indent("testIssue230506_08");
    }

    public void testIssue230506_09() throws Exception {
        indent("testIssue230506_09");
    }

    public void testIssue230506_10() throws Exception {
        indent("testIssue230506_10");
    }

    public void testIssue230506_11() throws Exception {
        indent("testIssue230506_11");
    }

    public void testIssue230506_12() throws Exception {
        indent("testIssue230506_12");
    }

    public void testIssue243317() throws Exception {
        indent("testIssue243317");
    }

    public void testIssue244434() throws Exception {
        indent("testIssue244434");
    }

    // #243184
    public void testNoBlockContents() throws Exception {
        indent("testNoBlockContents");
    }

    public void testSetBlock() throws Exception {
        indent("testSetBlock");
    }

    public void testTransBlock() throws Exception {
        indent("testTransBlock");
    }

    public void testShortedTransBlock_01() throws Exception {
        indent("testShortedTransBlock_01");
    }

    public void testShortedTransBlock_02() throws Exception {
        indent("testShortedTransBlock_02");
    }

    // #269423
    public void testWhitespaceControl_01() throws Exception {
        indent("testWhitespaceControl_01");
    }

    public void testWhitespaceControl_02() throws Exception {
        indent("testWhitespaceControl_02");
    }

    public void testWhitespaceControl_03() throws Exception {
        indent("testWhitespaceControl_03");
    }

    public void testWhitespaceControl_04() throws Exception {
        indent("testWhitespaceControl_04");
    }

    public void testWhitespaceControl_05() throws Exception {
        indent("testWhitespaceControl_05");
    }

}
