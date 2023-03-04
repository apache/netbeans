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
package org.netbeans.modules.debugger.jpda.breakpoints;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin
 */
public class ClassBasedBreakpointTest {
    
    @Test
    public void testClassExistsInSources() throws IOException {
        // Non existing class:
        String className = "org.foo.Foo";
        String emptyJarPath = System.getProperty("java.io.tmpdir")+File.separator+"NoSuchJar.jar";
        File emptyJar = new File(emptyJarPath);
        emptyJar.createNewFile();
        emptyJar.deleteOnExit();
        String[] projectSourceRoots = new String[] {
            System.getProperty("user.dir"),
            emptyJarPath,
        };
        boolean exists = ClassBasedBreakpoint.classExistsInSources(className, projectSourceRoots);
        assertFalse(exists);
        
        /*
        String cp = System.getProperty("test.dir.src");
        projectSourceRoots = new String[] {
            System.getProperty("test.dir.src"),
        };
        className = ClassBasedBreakpointTest.class.getName();
        *//*
        projectSourceRoots = new String[] {
            "/tmp/Foo.jar",
        };
        className = "org.test.Foo";
        exists = ClassBasedBreakpoint.classExistsInSources(className, projectSourceRoots);
        assertTrue(exists);
        */
    }
}
