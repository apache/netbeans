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

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class URLEqualityTest extends NbTestCase {
    private File orig;
    
    public URLEqualityTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File odir = new File(getWorkDir(), "orig");
        odir.mkdir();
        orig = new File(odir, "test.js");
        orig.createNewFile();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEqualSymlinks() throws Exception {
        if (!Utilities.isUnix()) {
            return;
        }
        File copy = new File(getWorkDir(), "copy");
        int ret = new ProcessBuilder("ln", "-s", "orig", copy.getPath()).start().waitFor();
        assertEquals("Symlink created", ret, 0);
        assertTrue("Dir exists", copy.exists());
        File f = new File(copy, "test.js");
        assertTrue("File exists", f.exists());
        
        URLEquality oe = new URLEquality(orig.toURI().toURL());
        URLEquality ne = new URLEquality(f.toURI().toURL());

        assertEquals("Same hashCode", oe.hashCode(), ne.hashCode());
        assertEquals("They are similar", oe, ne);
        
    }

    public void testDifferentInSiblinks() throws Exception {
        File copy = new File(getWorkDir(), "copy");
        copy.mkdir();
        File f = new File(copy, "test.js");
        f.createNewFile();
        
        URLEquality oe = new URLEquality(orig.toURI().toURL());
        URLEquality ne = new URLEquality(f.toURI().toURL());
        
        assertEquals("Same hashCode", oe.hashCode(), ne.hashCode());
        assertFalse("Not equals", oe.equals(ne));
    }
    
}
