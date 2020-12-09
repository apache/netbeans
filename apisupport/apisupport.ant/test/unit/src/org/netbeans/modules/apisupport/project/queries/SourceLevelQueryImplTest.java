/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Test {@link SourceLevelQueryImpl}.
 * @author Jesse Glick
 */
public class SourceLevelQueryImplTest extends TestBase {

    public SourceLevelQueryImplTest(String name) {
        super(name);
    }

    public void testGetSourceLevel() {
        String path = "java/junit/src/org/netbeans/modules/junit/api/JUnitSettings.java";
        FileObject f = nbRoot().getFileObject(path);
        assertNotNull("found " + path, f);
        assertEquals("1.6 used for an average module", "1.6", SourceLevelQuery.getSourceLevel(f));
    }
    
}
