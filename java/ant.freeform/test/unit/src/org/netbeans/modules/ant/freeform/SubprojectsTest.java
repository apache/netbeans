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

package org.netbeans.modules.ant.freeform;

import java.util.Collections;
import java.util.Set;
import org.netbeans.spi.project.SubprojectProvider;

// XXX testChanges

/**
 * Test {@link Subprojects}.
 * @author Jesse Glick
 */
public class SubprojectsTest extends TestBase {

    public SubprojectsTest(String name) {
        super(name);
    }

    private SubprojectProvider simpleSubprojects, extsrcrootSubprojects, simple2Subprojects;

    protected void setUp() throws Exception {
        super.setUp();
        simpleSubprojects = simple.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for simple", simpleSubprojects);
        extsrcrootSubprojects = extsrcroot.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for extsrcroot", extsrcrootSubprojects);
        simple2Subprojects = simple2.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for simple2", simple2Subprojects);
    }
    
    public void testBasicSubprojects() throws Exception {
        Set subprojects = simpleSubprojects.getSubprojects();
        
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        assertEquals("no subprojects for simple", Collections.EMPTY_SET, subprojects);
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        
        subprojects = extsrcrootSubprojects.getSubprojects();
        assertFalse("extsrcroot has simple as a subproject", subprojects.isEmpty());
        assertEquals("extsrcroot has simple as a subproject", Collections.singleton(simple), subprojects);
        assertFalse("extsrcroot has simple as a subproject", subprojects.isEmpty());
        
        subprojects = simple2Subprojects.getSubprojects();
        
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        assertEquals("no subprojects for simple", Collections.EMPTY_SET, subprojects);
        assertTrue("no subprojects for simple", subprojects.isEmpty());
    }
    
}
