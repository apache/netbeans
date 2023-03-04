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

import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Lahoda
 */
public class FreeformSharabilityQueryTest extends TestBase {

    public FreeformSharabilityQueryTest(String testName) {
	super(testName);
    }

    public void testSharability() throws Exception {
	FreeformProject prj = copyProject(simple);
	FileObject nbproject = prj.getProjectDirectory().getFileObject("nbproject");
	FileObject nbprojectProjectXML = nbproject.getFileObject("project.xml");
	FileObject nbprojectPrivate = nbproject.createFolder("private");
	FileObject nbprojectPrivatePrivateXML = nbprojectPrivate.createData("private.xml");
	FileObject src = prj.getProjectDirectory().getFileObject("src");
	FileObject myAppJava = src.getFileObject("org/foo/myapp/MyApp.java");
	FileObject buildXML = prj.getProjectDirectory().getFileObject("build.xml");
	
	assertNotNull(nbproject);
	assertNotNull(nbprojectProjectXML);
	assertNotNull(nbprojectPrivate);
	assertNotNull(nbprojectPrivatePrivateXML);
	assertNotNull(src);
	assertNotNull(myAppJava);
	assertNotNull(buildXML);
	
	assertEquals(SharabilityQuery.Sharability.MIXED, SharabilityQuery.getSharability(nbproject));
	assertEquals(SharabilityQuery.Sharability.SHARABLE, SharabilityQuery.getSharability(nbprojectProjectXML));
	assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, SharabilityQuery.getSharability(nbprojectPrivate));
	assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, SharabilityQuery.getSharability(nbprojectPrivatePrivateXML));
	assertEquals(SharabilityQuery.Sharability.UNKNOWN, SharabilityQuery.getSharability(src));
	assertEquals(SharabilityQuery.Sharability.UNKNOWN, SharabilityQuery.getSharability(myAppJava));
	assertEquals(SharabilityQuery.Sharability.UNKNOWN, SharabilityQuery.getSharability(buildXML));
    }
    
}
