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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single test suite.
 *
 * @author Erno Mononen
 */
public class TestSuite {
  
    /**
    * The max number of output lines to display in the tooltip.
     */
    static final int MAX_TOOLTIP_LINES = Integer.getInteger("testrunner.max.tooltip.lines", 4); //NOI18N

    public static final String ANONYMOUS_SUITE = new String();

    public static final TestSuite ANONYMOUS_TEST_SUITE = new TestSuite(ANONYMOUS_SUITE);

    /**
     * The name of this suite.
     */
    private final String name;
    /**
     * The test cases that this suite contains.
     */
    private final List<Testcase> testcases = new ArrayList<Testcase>();

    /**
     * Constructs a new TestSuite.
     * 
     * @param name the name for the suite, e.g. WhatEverTest. May be null.
     */
    public TestSuite(String name) {
        this.name = name;
    }

    public void addTestcase(Testcase testcase) {
        testcases.add(testcase);
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    /**
     * @return the name of this suite, may return <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the last test case of this suite or <code>null</code> if 
     * the suite contains no test cases.
     */
    public Testcase getLastTestCase() {
        return testcases.isEmpty() ? null : testcases.get(testcases.size() -1);
    }

}
