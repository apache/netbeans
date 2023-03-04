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

package org.openide.modules;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.SpecificationVersion;

/** Test parsing of specification versions.
 * @author Jesse Glick
 */
public class SpecificationVersionTest extends NbTestCase {

    public SpecificationVersionTest(String name) {
        super(name);
    }

    public void testParseAndCompare() throws Exception {
        SpecificationVersion v = new SpecificationVersion("1.2.3");
        assertEquals("1.2.3", v.toString());
        assertTrue(v.compareTo(new SpecificationVersion("1.2.3")) == 0);
        assertTrue(v.compareTo(new SpecificationVersion("2.4.6")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.4")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.0")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.3")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.3.0")) == 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.2.99")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.3.0")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("2")) < 0);
        v = new SpecificationVersion("10.99.3");
        assertTrue(v.compareTo(new SpecificationVersion("10.9.4")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("10.100")) < 0);
    }
    
    public void testMisparse() throws Exception {
        misparse("");
        misparse("1.");
        misparse(".1");
        misparse("-1");
        misparse("0x13");
        misparse("2..4");
        misparse("2...4");
        misparse("13.8.");
        misparse("1.4.0beta");
        misparse("hello");
    }
    
    private void misparse(String s) throws Exception {
        try {
            new SpecificationVersion(s);
            assertTrue("Should have misparsed: " + s, false);
        } catch (NumberFormatException nfe) {
            // OK, expected.
        }
    }
    
}
