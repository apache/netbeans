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
