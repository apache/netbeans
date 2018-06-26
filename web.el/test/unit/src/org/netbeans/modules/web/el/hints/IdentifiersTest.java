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
package org.netbeans.modules.web.el.hints;

import org.netbeans.modules.csl.api.Rule;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class IdentifiersTest extends HintTestBase {

    public IdentifiersTest(String testName) {
        super(testName);
    }

    @Override
    protected Rule createRule() {
        return new Identifiers();
    }

    public void testUnknownOperatorForListData() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers01.xhtml", null);
    }

    public void testKnownBeanProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers02.xhtml", null);
    }

    public void testKnownBeanProperty02() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers04.xhtml", null);
    }

    public void testUnknownBeanProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers03.xhtml", null);
    }

    public void testUnknownBeanProperty02() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers05.xhtml", null);
    }

    public void testOperatorForListData01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers06.xhtml", null);
    }

    public void testWrongOperatorForListData01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers07.xhtml", null);
    }

    public void testOperatorForListProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers08.xhtml", null);
    }

    public void testOperatorForStringProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers10.xhtml", null);
    }

    public void testOperatorForArrayProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers09.xhtml", null);
    }

    public void testOperatorForIterableProperty01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers11.xhtml", null);
    }

    public void testPropertyCustomMap01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers12.xhtml", null);
    }

    public void testPropertyImplicitMap01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers13.xhtml", null);
    }

    public void testMethodCalledInBracket01() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers14.xhtml", null);
    }

    public void testMethodCalledInBracket02() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers15.xhtml", null);
    }

    public void testMethodCalledInBracket03() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/identifiers16.xhtml", null);
    }

    public void testIssue232274_0() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue232274_0.xhtml", null);
    }

    public void testIssue232274_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue232274_1.xhtml", null);
    }

    public void testIssue232274_2() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue232274_2.xhtml", null);
    }

    public void testIssue232274_3() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue232274_3.xhtml", null);
    }

    public void testIssue232274_4() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue232274_4.xhtml", null);
    }

    public void testIssue234832_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue234832_1.xhtml", null);
    }

    public void testIssue234832_2() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue234832_2.xhtml", null);
    }

    public void testIssue236450_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue236450_1.xhtml", null);
    }

    public void testIssue239945_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue239945_1.xhtml", null);
    }

    public void testIssue239883_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue239883_1.xhtml", null);
    }

    public void testIssue241973_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue241973_1.xhtml", null);
    }

    public void testIssue253605_1() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/identifiers/issue253605_1.xhtml", null);
    }
}
