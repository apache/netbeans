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

import java.util.Set;

/**
 *
 * @author Petr Hejl
 */
public class VariablesCCTest extends GroovyCCTestBase {

    public VariablesCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "variables";
    }

    @Override
    protected Set<String> additionalSourceClassPath() {
        Set<String> sources = super.additionalSourceClassPath();

        // Because we have to have also Variables1.groovy and Variables2.groovy
        // on classpath for variables3 test cases
        if (getName().contains("Variables3")) {
            sources.add(getBasicSourcePath());
        }
        return sources;
    }

    public void testVariables1_1() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "            i^", true);
    }

    public void testVariables1_2() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "        it^", true);
    }

    public void testVariables1_3() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "            a^", true);
    }

    public void testVariables1_4() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "            e^", true);
    }

    public void testVariables1_5() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "        es^", true);
    }

    public void testVariables1_6() throws Exception {
        checkCompletion(BASE + "Variables1.groovy", "        pa^", true);
    }

    public void testVariables2_1() throws Exception {
        checkCompletion(BASE + "Variables2.groovy", "                it^", true);
    }

    public void testVariables2_2() throws Exception {
        checkCompletion(BASE + "Variables2.groovy", "                ind^", true);
    }

    public void testVariables2_3() throws Exception {
        checkCompletion(BASE + "Variables2.groovy", "                par^", true);
    }

    public void testVariables2_4() throws Exception {
        checkCompletion(BASE + "Variables2.groovy", "                inde^", true);
    }

    public void testVariables2_5() throws Exception {
        checkCompletion(BASE + "Variables2.groovy", "            pa^", true);
    }
    
    public void testFields1_1() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "        base.field^", true);
    }
    
    public void testFields1_2() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "        self.kolec^", true);
    }
    
    public void testFields1_3() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "text = \"${kolec^} and ${base.field} and ${self.field} and ${fiel}\"", true);
    }
    
    public void testFields1_4() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "text = \"${kolec} and ${base.field^} and ${self.field} and ${fiel}\"", true);
    }
    
    public void testFields1_5() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "text = \"${kolec} and ${base.field} and ${self.field^} and ${fiel}\"", true);
    }
    
    public void testFields1_6() throws Exception {
        checkCompletion(BASE + "Fields1.groovy", "text = \"${kolec} and ${base.field} and ${self.field} and ${fiel^}\"", true);
    }

    /*
    public void testVariables3_1() throws Exception {
        checkCompletion(BASE + "Variables3.groovy", "println \"Hello $name!\" ^", true);
    }

    public void testVariables3_2() throws Exception {
        checkCompletion(BASE + "Variables3.groovy", "    x ^", true);
    }

    public void testVariables3_3() throws Exception {
        checkCompletion(BASE + "Variables3.groovy", "    def x ^", true);
    }*/
}
