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

package org.netbeans.modules.groovy.editor.api.completion;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;

/**
 *
 * @author Martin Janicek
 */
public class CamelCaseCCTest extends GroovyCCTestBase {

    public CamelCaseCCTest(String testName) {
        super(testName);
        checkLspCompletion = true;
    }

    final boolean deprecatedHolder[] = new boolean[1];

    final HtmlFormatter formatter = new HtmlFormatter() {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void reset() {
            sb.setLength(0);
        }

        @Override
        public void appendHtml(String html) {
            sb.append(html);
        }

        @Override
        public void appendText(String text, int fromInclusive, int toExclusive) {
            sb.append(text, fromInclusive, toExclusive);
        }

        @Override
        public void emphasis(boolean start) {
        }

        @Override
        public void active(boolean start) {
        }

        @Override
        public void name(ElementKind kind, boolean start) {
        }

        @Override
        public void parameters(boolean start) {
        }

        @Override
        public void type(boolean start) {
        }

        @Override
        public void deprecated(boolean start) {
            deprecatedHolder[0] = true;
        }

        @Override
        public String getText() {
            return sb.toString();
        }
    };
    
    @Override
    protected String getTestType() {
        return "camelcase";
    }

    /*
     * All upper case letters used
     */
    public void testCamelCaseCompletion1() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion1.groovy", "    CamCaTGC^", false);
    }

    public void testCamelCaseCompletion2() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion2.groovy", "    CCTestGClass^", false);
    }

    public void testCamelCaseCompletion3() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion3.groovy", "    CamelCTGC^", false);
    }

    /*
     * Some upper case letter might be missing
     */
    public void testCamelCaseCompletion4() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion4.groovy", "    CCT^", false);
    }
    
    public void testCamelCaseCompletion5() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion5.groovy", "    CamCa^", false);
    }

    /*
     * General class type CamelCase completion tests
     */
    public void testCamelCaseCompletion6() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion6.groovy", "    NS^", false);
    }

    public void testCamelCaseCompletion7() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion7.groovy", "    NoSE^", false);
    }

    public void testCamelCaseCompletion8() throws Exception {
        checkCompletion(BASE + "CamelCaseCompletion8.groovy", "    NoSuFie^", false);
    }
}
