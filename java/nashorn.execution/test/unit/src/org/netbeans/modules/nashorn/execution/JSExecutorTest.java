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
package org.netbeans.modules.nashorn.execution;

import java.lang.reflect.Method;
import junit.framework.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

public class JSExecutorTest extends NbTestCase {

    public JSExecutorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(JSExecutorTest.class).gui(false).clusters(".*").suite();
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testLoadAllGraalJsLibraries() throws Exception {
        final Method find = JSExecutor.class.getDeclaredMethod("findGraalJsClassPath");
        find.setAccessible(true);
        final ClassPath cp = (ClassPath) find.invoke(null);
        final FileObject[] roots = cp.getRoots();
        assertEquals("Seven roots", 15, roots.length);
        for (FileObject fo : roots) {
            assertTrue("valid: " + fo, fo.isValid());
            assertTrue("folder: " + fo, fo.isFolder());
            assertTrue("has kids: " + fo, fo.getChildren().length > 0);
        }
        Class<?> JSLauncher = cp.getClassLoader(true).loadClass("com.oracle.truffle.js.shell.JSLauncher");
        assertNotNull("Launcher class linked", JSLauncher);
    }

}
