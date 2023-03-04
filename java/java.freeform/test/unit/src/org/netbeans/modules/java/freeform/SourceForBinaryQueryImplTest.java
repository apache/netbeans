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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Check that the correct sources are reported.
 * @author Jesse Glick
 */
public class SourceForBinaryQueryImplTest extends TestBase {

    public SourceForBinaryQueryImplTest(String name) {
        super(name);
    }

    public void testFindSourcesForBinaries() throws Exception {
        FileObject srcroot = simple.getProjectDirectory().getFileObject("src");
        URL binroot = new URL(simple.getProjectDirectory().getURL(), "build/classes/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        binroot = new URL("jar:" + simple.getProjectDirectory().getURL().toString() + "build/simple-app.jar!/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        srcroot = simple.getProjectDirectory().getFileObject("antsrc");
        binroot = new URL(simple.getProjectDirectory().getURL(), "build/antclasses/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        binroot = new URL(simple.getProjectDirectory().getURL(), "build/nonsense/");
        assertEquals("no source root for " + binroot, Collections.EMPTY_LIST, Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
    }
    
}
