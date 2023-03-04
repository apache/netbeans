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

import junit.framework.Test;
import test.pkg.not.in.junit.NbModuleSuiteT;

/** Check behavior of context class loader.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbModuleSuiteContextClassLoaderTest extends NbTestCase {
    
    private static ClassLoader before;
    
    public NbModuleSuiteContextClassLoaderTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbTestSuite ts = new NbTestSuite();
        ts.addTest(new NbModuleSuiteContextClassLoaderTest("testBefore"));
        ts.addTest(NbModuleSuite.allModules(NbModuleSuiteT.class));
        ts.addTest(new NbModuleSuiteContextClassLoaderTest("testAfter"));
        return ts;
    }
    
    public void testBefore() {
        before = Thread.currentThread().getContextClassLoader();
    }
    public void testAfter() {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        assertSame("Class loader remains the same", before, l);
    }
}
