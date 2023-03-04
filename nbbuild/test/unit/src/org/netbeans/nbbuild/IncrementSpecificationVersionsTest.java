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

package org.netbeans.nbbuild;

import org.netbeans.junit.NbTestCase;
import static org.netbeans.nbbuild.IncrementSpecificationVersions.*;

/** Test for increments of spec versions.
 *
 * @author Jaroslav Tulach
 */
public class IncrementSpecificationVersionsTest extends NbTestCase {

    public IncrementSpecificationVersionsTest(String testName) {
        super(testName);
    }

    public void testIncrementTrunkManifest() throws Exception {        
        boolean manifest = true;
        int sticky = 1;
        
        assertEquals("1.1", increment("1.0", sticky, manifest));
        assertEquals("1.2", increment("1.1", sticky, manifest));
        assertEquals("2.3", increment("2.2", sticky, manifest));
        assertEquals("3.12", increment("3.11", sticky, manifest));
        assertEquals("203.215", increment("203.214", sticky, manifest));
        
        assertEquals("1.9", increment("1.8.1", sticky, manifest));
        assertEquals("1.10", increment("1.9.1", sticky, manifest));        
        assertEquals("1.1", increment("1.0", sticky, manifest));
        assertEquals("1.7", increment("1.6", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1.5.6", sticky, manifest));
        
        assertEquals("1.1", increment("1", sticky, manifest));
        assertEquals("100.1", increment("100", sticky, manifest));
    }
    
    public void testIncrementTrunkSVB() throws Exception {
        boolean manifest = false;
        int sticky = 1;
                
        assertEquals("1.1.0", increment("1.0.0", sticky, manifest));
        assertEquals("1.2.0", increment("1.1.0", sticky, manifest));
        assertEquals("2.3.0", increment("2.2.0", sticky, manifest));
        assertEquals("3.12.0", increment("3.11.0", sticky, manifest));
        assertEquals("203.215.0", increment("203.214.0", sticky, manifest));

        assertEquals("1.9.0", increment("1.8.1", sticky, manifest));
        assertEquals("1.10.0", increment("1.9.1", sticky, manifest));        
        assertEquals("1.2.0", increment("1.0", sticky, manifest));
        assertEquals("1.8.0", increment("1.6", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("2.1.0", increment("1", sticky, manifest));
        assertEquals("101.1.0", increment("100", sticky, manifest));
    }

    public void testIncrementBranchManifest() throws Exception {
        boolean manifest = true;
        int sticky = 2;
        
        assertEquals("1.0.1", increment("1.0", sticky, manifest));
        assertEquals("1.1.1", increment("1.1", sticky, manifest));
        assertEquals("2.2.1", increment("2.2", sticky, manifest));
        assertEquals("3.11.1", increment("3.11", sticky, manifest));
        assertEquals("203.214.1", increment("203.214", sticky, manifest));
        
        assertEquals("1.0.5", increment("1.0.4", sticky, manifest));
        assertEquals("1.1.7", increment("1.1.6", sticky, manifest));
        assertEquals("2.2.8", increment("2.2.7", sticky, manifest));
        assertEquals("3.11.10", increment("3.11.9", sticky, manifest));
        assertEquals("203.214.1001", increment("203.214.1000", sticky, manifest));
        
        assertEquals("2.3.9", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("1.0.1", increment("1", sticky, manifest));
        assertEquals("100.0.1", increment("100", sticky, manifest));
    }

    public void testIncrementBranchSVB() throws Exception {
        boolean manifest = false;
        int sticky = 2;
        
        assertEquals("1.1.1", increment("1.0", sticky, manifest));
        assertEquals("1.2.1", increment("1.1", sticky, manifest));
        assertEquals("2.3.1", increment("2.2", sticky, manifest));
        assertEquals("3.12.1", increment("3.11", sticky, manifest));
        assertEquals("203.215.1", increment("203.214", sticky, manifest));
                
        assertEquals("1.0.1", increment("1.0.0", sticky, manifest));
        assertEquals("1.1.1", increment("1.1.0", sticky, manifest));
        assertEquals("2.2.1", increment("2.2.0", sticky, manifest));
        assertEquals("3.11.1", increment("3.11.0", sticky, manifest));
        assertEquals("203.214.1", increment("203.214.0", sticky, manifest));
                
        assertEquals("2.3.9", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("2.0.1", increment("1", sticky, manifest));
        assertEquals("101.0.1", increment("100", sticky, manifest));
    }
    
    public void testIncrementLevel4() {
        assertIncrement("1.2.3.4.5.6.7", 4, true, "1.2.3.4.6");
        assertIncrement("1.0", 4, true, "1.0.0.0.1");
        assertIncrement("1.2.3.4.5", 4, true, "1.2.3.4.6");
    }

    private static void assertIncrement(String old, int stickyLevel, boolean manifest, String res) {
        String r = IncrementSpecificationVersions.increment(old, stickyLevel, manifest);
        assertEquals("Old: " + old + " stickyLevel: " + stickyLevel + " manifest: " + manifest, res, r);
    }

}
