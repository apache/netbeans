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
import junit.framework.*;

/**
 *
 * @author Samaresh
 */
public class CompletionPerfTest extends AbstractTestCase {
    
    static final String COMPLETION_TEST_DOCUMENT = "resources/OTA_TravelItinerary.xsd";
    
    public CompletionPerfTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(CompletionPerfTest.class);
        return suite;
    }

    /**
     * Queries elements on OTA.
     */
    public void testPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        setupCompletion(COMPLETION_TEST_DOCUMENT, null);
        List<CompletionResultItem> items = query(819312);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        String[] expectedResult = {"xs:include", "xs:import", "xs:redefine",
                "xs:annotation", "xs:simpleType", "xs:complexType", "xs:group",
                "xs:attributeGroup", "xs:element", "xs:attribute", "xs:notation", "xs:annotation"};
        assertResult(items, expectedResult);
    }
    
}