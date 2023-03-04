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

package test.pkg.not.in.junit;

import junit.framework.TestCase;

public class NbModuleSuiteDebugger extends TestCase {
    public NbModuleSuiteDebugger(String t) {
        super(t);
    }

    public void testIfOneCanLoadFromToolsJarOneShallDoThatInTheFrameworkAsWell() throws Exception {
        final String className = "com.sun.jdi.VirtualMachineManager";
        Class<?> own;
        try {
            own = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException ex) {
            //own = null;
            throw ex;
        }
        assertNotNull("We can load class from tools.jar", own);
    }
}
