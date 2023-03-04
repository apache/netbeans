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

package org.netbeans.modules.csl.editor.semantic;

import junit.framework.TestCase;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Tor Norbye
 */
public class SequenceElementTest extends TestCase {
    
    public SequenceElementTest(String testName) {
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

    public void testCompareTo() {
        SequenceElement s1 = new SequenceElement(null, new OffsetRange(2,5),null);
        SequenceElement s2 = new SequenceElement(null, new OffsetRange(5,7),null);
        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);
        s2 = new SequenceElement(null, new OffsetRange(2,5),null);
        assertTrue(s1.compareTo(s2) == 0);
        assertTrue(s2.compareTo(s1) == 0);
        
        s1 = new SequenceElement.ComparisonItem(1);
        s2 = new SequenceElement.ComparisonItem(2);
        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);

        // s1 below s2
        s2 = new SequenceElement(null, new OffsetRange(2,5),null);
        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);

        // s1 inside s2
        s1 = new SequenceElement.ComparisonItem(3);
        assertTrue(s1.compareTo(s2) == 0);
        assertTrue(s2.compareTo(s1) == 0);

        // s1 above s2
        s1 = new SequenceElement.ComparisonItem(6);
        assertTrue(s1.compareTo(s2) > 0);
        assertTrue(s2.compareTo(s1) < 0);
    }
}
