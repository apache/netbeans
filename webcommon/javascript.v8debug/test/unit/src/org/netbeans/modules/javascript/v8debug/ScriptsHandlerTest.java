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

package org.netbeans.modules.javascript.v8debug;

import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.v8debug.V8Script;

/**
 *
 * @author Martin Entlicher
 */
public class ScriptsHandlerTest {
    
    public ScriptsHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of add method, of class ScriptsHandler.
     */
    @Test
    public void testAdd() {
        /* TODO
        System.out.println("add");
        V8Script script = null;
        ScriptsHandler instance = null;
        instance.add(script);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
    }

    /**
     * Test of getLocalPath method, of class ScriptsHandler.
     */
    @Test
    public void testGetLocalPath() throws Exception {
        System.out.println("getLocalPath");
        ScriptsHandler instance = new ScriptsHandler(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
        assertEquals("", instance.getLocalPath(""));
        assertEquals("/a/b/c", instance.getLocalPath("/a/b/c"));
        
        instance = new ScriptsHandler(Collections.singletonList("/home/test"),
                                      Collections.singletonList("/var/server/path/"),
                                      Collections.EMPTY_SET, null);
        try {
            assertEquals("", instance.getLocalPath(""));
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        assertEquals("/home/test", instance.getLocalPath("/var/server/path"));
        assertEquals("/home/test/file", instance.getLocalPath("/var/server/path/file"));
        assertEquals("/home/test/test/My\\File.js", instance.getLocalPath("/var/server/path/test/My\\File.js"));
        try {
            instance.getLocalPath("/var/server/path2");
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        try {
            instance.getLocalPath("/var/server");
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\Users\\Test"),
                                      Collections.singletonList("/var"),
                                      Collections.EMPTY_SET, null);
        assertEquals("C:\\\\Users\\Test", instance.getLocalPath("/var"));
        assertEquals("C:\\\\Users\\Test\\folder\\MyFile.js", instance.getLocalPath("/var/folder/MyFile.js"));
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\"),
                                      Collections.singletonList("/var"),
                                      Collections.EMPTY_SET, null);
        assertEquals("C:\\\\", instance.getLocalPath("/var"));
        assertEquals("C:\\\\File.js", instance.getLocalPath("/var/File.js"));
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\"),
                                      Collections.singletonList("/"),
                                      Collections.EMPTY_SET, null);
        assertEquals("C:\\\\", instance.getLocalPath("/"));
        assertEquals("C:\\\\File.js", instance.getLocalPath("/File.js"));
    }

    /**
     * Test of getServerPath method, of class ScriptsHandler.
     */
    @Test
    public void testGetServerPath() throws Exception {
        System.out.println("getServerPath");
        ScriptsHandler instance = new ScriptsHandler(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_SET, null);
        assertEquals("/a/b/c", instance.getServerPath("/a/b/c"));
        
        instance = new ScriptsHandler(Collections.singletonList("/home/test"),
                                      Collections.singletonList("/var/server/path/"),
                                      Collections.EMPTY_SET, null);
        try {
            assertEquals("", instance.getServerPath(""));
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        assertEquals("/var/server/path", instance.getServerPath("/home/test"));
        assertEquals("/var/server/path/file", instance.getServerPath("/home/test/file"));
        assertEquals("/var/server/path/test/My\\File.js", instance.getServerPath("/home/test/test/My\\File.js"));
        try {
            instance.getServerPath("/home/test2");
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        try {
            instance.getServerPath("/home");
            fail("Did not throw OutOfScope exception.");
        } catch (ScriptsHandler.OutOfScope oos) {
        }
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\Users\\Test"),
                                      Collections.singletonList("/var"),
                                      Collections.EMPTY_SET, null);
        assertEquals("/var", instance.getServerPath("C:\\\\Users\\Test"));
        assertEquals("/var/folder/MyFile.js", instance.getServerPath("C:\\\\Users\\Test\\folder\\MyFile.js"));
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\"),
                                      Collections.singletonList("/var"),
                                      Collections.EMPTY_SET, null);
        assertEquals("/var", instance.getServerPath("C:\\\\"));
        assertEquals("/var/File.js", instance.getServerPath("C:\\\\File.js"));
        
        instance = new ScriptsHandler(Collections.singletonList("C:\\\\"),
                                      Collections.singletonList("/"),
                                      Collections.EMPTY_SET, null);
        assertEquals("/", instance.getServerPath("C:\\\\"));
        assertEquals("/File.js", instance.getServerPath("C:\\\\File.js"));
    }
    
}
