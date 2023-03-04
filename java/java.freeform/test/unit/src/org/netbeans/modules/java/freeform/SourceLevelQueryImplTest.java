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

package org.netbeans.modules.java.freeform;

import org.netbeans.api.java.queries.SourceLevelQuery;

import org.netbeans.modules.ant.freeform.TestBase;

/**
 * Test functionality of source level definitions in FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class SourceLevelQueryImplTest extends TestBase {

    public SourceLevelQueryImplTest(String name) {
        super(name);
    }

    public void testSourceLevel() throws Exception {
        assertEquals("correct source level for MyApp.java", "1.4", SourceLevelQuery.getSourceLevel(myAppJava));
        assertEquals("correct source level for SpecialTask.java", "1.4", SourceLevelQuery.getSourceLevel(specialTaskJava));
        assertEquals("no source level for build.properties", null, SourceLevelQuery.getSourceLevel(buildProperties));
    }
    
}
