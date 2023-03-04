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
package org.openide.execution;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileSystemCapability;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbClassPathCompatTest extends NbTestCase {
    
    public NbClassPathCompatTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(NbClassPathCompatTest.class).
                gui(false).
                addTest("testRepositoryPath", "testRepositoryPathParam").suite();
    }        
    
    public void testRepositoryPath() throws Exception {
        Method m = NbClassPath.class.getMethod("createRepositoryPath");
        assertTrue("Is static", (m.getModifiers() & Modifier.STATIC) != 0);
        
        assertEquals(NbClassPath.class, m.getReturnType());
    }

    public void testRepositoryPathParam() throws Exception {
        Method m = NbClassPath.class.getMethod("createRepositoryPath", FileSystemCapability.class);
        assertTrue("Is static", (m.getModifiers() & Modifier.STATIC) != 0);
        
        assertEquals(NbClassPath.class, m.getReturnType());
    }
}
