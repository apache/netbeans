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

package org.netbeans.modules.projectimport.eclipse.core;

import org.netbeans.junit.NbTestCase;

/**
 * Tests equal and hashCode methods.
 *
 * @author mkrauskopf
 */
public class EqualityAndHashCodeTest extends NbTestCase {

    Link link2;
    Link theSameAsLink2;

    Workspace.Variable var2;
    Workspace.Variable theSameAsVar2;

    public EqualityAndHashCodeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        link2 = new Link("link2", true, "/link2");
        
        theSameAsLink2 = new Link("link2", true, "/link2");
        
        var2 = new Workspace.Variable("/var2", "var2");
        
        theSameAsVar2 = new Workspace.Variable("/var2", "var2");
    }
    
    /** tests ClassPathContent.Link.equals() */
    public void testLinksEquality() {
        assertNotSame("link2 and theSameAsLink2 shouldn't be the same " +
                "(link2 == theSameAsLink2)", link2, theSameAsLink2);
        assertEquals("link2 should be equal to theSameAsLink2",
                link2, theSameAsLink2);
    }
    
    /** tests ClassPathContent.Link.hashCode() */
    public void testLinksHashCodes() {
        assertEquals("link2 and theSameAsLink2 should generate the same hashCode",
                link2.hashCode(), theSameAsLink2.hashCode());
    }
    /** tests ClassPathContent.Variable.equals() */
    public void testVariablesEquality() {
        assertNotSame("var2 and theSameAsVar2 shouldn't be the same " +
                "(var2 == theSameAsVar2)", var2, theSameAsVar2);
        assertEquals("var2 should be equal to theSameAsVar2",
                var2, theSameAsVar2);
    }
    
}
