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

package org.apache.tools.ant.module.spi;

import java.util.logging.Level;
import java.io.File;
import java.net.URL;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class AutomaticExtraClasspathTest extends NbTestCase {
    private static URL wd;
    
    
    FileObject fo, bad;
    
    public AutomaticExtraClasspathTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        URL u = getClass().getResource("AutomaticExtraClasspathTest.xml");
        FileSystem fs = new XMLFileSystem(u);
        fo = fs.findResource("testAutoProvider");
        assertNotNull("There is the resource", fo);
        bad = fs.findResource("brokenURL");
        assertNotNull("There is the bad", bad);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public static URL getWD() {
        return wd;
    }
    
    public void testReadWorkDir() throws Exception {
        URL u = Utilities.toURI(getWorkDir()).toURL();
        wd = u;
        
        Object value = fo.getAttribute("instanceCreate");
        assertTrue("provider created: " + value, value instanceof AutomaticExtraClasspathProvider);
        
        AutomaticExtraClasspathProvider auto = (AutomaticExtraClasspathProvider)value;
        File[] arr = auto.getClasspathItems();
        assertNotNull(arr);
        assertEquals("One item", 1, arr.length);
        assertEquals("It is our work dir", getWorkDir(), arr[0]);
    }

    public void testBadURL() throws Exception {
        CharSequence log = Log.enable("", Level.INFO);
        Object value = bad.getAttribute("instanceCreate");
        assertNull("no provider created: " + value, value);
        
        if (log.toString().indexOf("IllegalArgumentException") == -1) {
            fail("IllegalArgumentException shall be thrown:\n" + log);
        }
    }

    public void testFailIfTheFileDoesNotExists() throws Exception {
        URL u = Utilities.toURI(new File(getWorkDir(), "does-not-exists.txt")).toURL();
        wd = u;
        
        CharSequence log = Log.enable("", Level.INFO);
        Object value = fo.getAttribute("instanceCreate");
        AutomaticExtraClasspathProvider auto = (AutomaticExtraClasspathProvider)value;
        assertNotNull(auto);
        assertEquals(0, auto.getClasspathItems().length);
        if (log.toString().indexOf("No File found") == -1) {
            fail("should have warned:\n" + log);
        }
    }
    
}
