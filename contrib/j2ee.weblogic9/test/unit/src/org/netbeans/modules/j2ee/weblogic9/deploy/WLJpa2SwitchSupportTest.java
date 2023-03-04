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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class WLJpa2SwitchSupportTest extends NbTestCase {

    public WLJpa2SwitchSupportTest(String name) {
        super(name);
    }

    public void testGetRelativePath() throws Exception {
        File baseDir = getWorkDir();
        File directory = new File(baseDir, "some" + File.separator
                + "directory" + File.separator
                + "structure");
        directory.mkdirs();
        
        File other = new File(baseDir, "other");
        other.mkdirs();
        
        assertEquals("../some/directory/structure",
                WLJpa2SwitchSupport.getRelativePath(other, directory));
        assertEquals("../../../other",
                WLJpa2SwitchSupport.getRelativePath(directory, other));
        assertEquals("some",
                WLJpa2SwitchSupport.getRelativePath(baseDir, new File(baseDir, "some")));        
        assertEquals("",
                WLJpa2SwitchSupport.getRelativePath(baseDir, baseDir));        
    }

}