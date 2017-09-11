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
package org.netbeans.modules.spring.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Rohan Ranade
 */
public class FieldNamesCalculatorTest extends TestCase {

    public FieldNamesCalculatorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleCalculation() {
        FieldNamesCalculator instance = new FieldNamesCalculator("BadLocationException", Collections.<String>emptySet());
        List<String> result = instance.calculate();
        assertFieldCalculations(new String[]{"bad", "badLocation", "badLocationException", "location", "locationException", "exception"}, result);
    }

    public void testNoCapsCalculation() {
        FieldNamesCalculator instance = new FieldNamesCalculator("_sampleclass", Collections.<String>emptySet());
        assertFieldCalculations(new String[]{"_sampleclass"}, instance.calculate());
    }

    public void testAllCapsCalculation() {
        FieldNamesCalculator instance = new FieldNamesCalculator("FOOO", Collections.<String>emptySet());
        List<String> result = instance.calculate();
        assertFieldCalculations(new String[]{"f", "fO", "fOO", "fOOO", "o", "oO", "oOO"}, result);
    }

    public void testCalculationWithPrefix() {
        FieldNamesCalculator instance = new FieldNamesCalculator("badValueNamed", Collections.<String>emptySet());
        List<String> result = instance.calculate();
        assertFieldCalculations(new String[]{"bad", "badValue", "badValueNamed", "value", "valueNamed", "named"}, result);
    }

    public void testCalculationWithCollision() {
        Set<String> forbidden = new HashSet<String>(Arrays.<String>asList("bad", "bad1", "value"));
        FieldNamesCalculator instance = new FieldNamesCalculator("BadValue", forbidden);
        List<String> result = instance.calculate();
        assertFieldCalculations(new String[]{"bad2", "badValue", "value1"}, result);
    }

    private void assertFieldCalculations(String[] expected, List<String> result) {
        assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.get(i));
        }
    }
}
