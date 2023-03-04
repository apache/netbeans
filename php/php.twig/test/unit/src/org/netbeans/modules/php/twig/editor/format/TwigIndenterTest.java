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
