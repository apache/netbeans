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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for element substitution.
 * @author Daniel Bell (dbell@netbeans.org)
 */
public class AbstractElementTest extends AbstractTestCase {

    static final String COMPLETION_DOCUMENT = "resources/AbstractElementCompletion.xml";
    static final String PARENT_SCHEMA = "resources/AbstractElementParent.xsd";
    static final String CHILD_SCHEMA_ONE = "resources/AbstractElementChildOne.xsd";
    static final String CHILD_SCHEMA_TWO = "resources/AbstractElementChildTwo.xsd";

    public AbstractElementTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AbstractElementTest("shouldNotSuggestAbstractElement"));
        suite.addTest(new AbstractElementTest("shouldExpandSubstitutionGroup"));
        return suite;
    }
    
    @Override
    public void setUp() throws Exception {
        setupCompletion(COMPLETION_DOCUMENT);
    }
        
    /**
     * Elements with {@code abstract="true"} should not be suggested
     */
    public void shouldNotSuggestAbstractElement() {
        List<CompletionResultItem> items = query(468);
        assertDoesNotContainSuggestions(items, false, "child");
    }
            
    /**
     * All available elements that can be substituted for elements in the 
     * completion context should be presented as completion options
     */
    public void shouldExpandSubstitutionGroup() {
        List<CompletionResultItem> items = query(468);
        assertContainSuggestions(items, true, "c1:child-one", "c2:child-two");
    }
}
