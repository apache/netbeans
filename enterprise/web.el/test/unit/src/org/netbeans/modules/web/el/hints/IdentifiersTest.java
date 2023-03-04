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
