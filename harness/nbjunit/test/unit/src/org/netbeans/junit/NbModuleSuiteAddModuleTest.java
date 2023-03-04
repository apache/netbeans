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

package org.netbeans.junit;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteAddModuleTest extends TestCase {
    
    public NbModuleSuiteAddModuleTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of run method, of class NbModuleSuite.
     */
    public void testRun() {
        Test instance = NbModuleSuite.createConfiguration(T.class).clusters("").enableModules("org\\.netbeans.*window.*").gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        String m = System.getProperty("modules");
        assertNotNull("Test called", m);
        assertTrue("Contains the right module", m.contains("org.netbeans.core.windows"));
        
    }
    public static class T extends TestCase {
        public T(String t) {
            super(t);
        }

        public void testOne() {
            try {
                Object o = NbModuleSuite.S.findEnabledModules(Thread.currentThread().getContextClassLoader());
                System.setProperty("modules", o.toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
}
