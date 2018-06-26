/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author Martin Janicek
 */
public class CamelCaseCCTest extends GroovyCCTestBase {

    public CamelCaseCCTest(String testName) {
        super(testName);
    }

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
