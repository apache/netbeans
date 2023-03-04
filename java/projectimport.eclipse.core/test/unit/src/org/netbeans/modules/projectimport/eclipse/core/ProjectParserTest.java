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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectimport.eclipse.core.spi.Facets;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ProjectParserTest extends NbTestCase {
    
    public ProjectParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    
    
    public void testParseJSFLibraryRegistryV2() throws IOException {
        FileObject fo = FileUtil.toFileObject(new File(getDataDir(), "org.eclipse.wst.common.project.facet.core.xml"));
        FileObject dest = FileUtil.createFolder(new File(getWorkDir(), "ep/.settings/"));
        FileUtil.copyFile(fo, dest, fo.getName(), fo.getExt());
        Facets facets = ProjectParser.readProjectFacets(new File(getWorkDir(), "ep/"), 
            Collections.<String>singleton("org.eclipse.wst.common.project.facet.core.nature"));
        assertNotNull(facets);
        assertEquals(3, facets.getInstalled().size());
        assertEquals("jst.java", facets.getInstalled().get(0).getName());
        assertEquals("6.0", facets.getInstalled().get(0).getVersion());
        assertEquals("jst.web", facets.getInstalled().get(1).getName());
        assertEquals("2.4", facets.getInstalled().get(1).getVersion());
        assertEquals("jst.jsf", facets.getInstalled().get(2).getName());
        assertEquals("1.1", facets.getInstalled().get(2).getVersion());
        facets = ProjectParser.readProjectFacets(new File(getWorkDir(), "ep/"), 
            Collections.<String>singleton("org.XXX"));
        assertNull(facets);
    }
}
