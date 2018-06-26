/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
