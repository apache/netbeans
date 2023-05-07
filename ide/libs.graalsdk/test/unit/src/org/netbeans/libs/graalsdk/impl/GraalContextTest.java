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
package org.netbeans.libs.graalsdk.impl;

import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.TestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Lookup;

public class GraalContextTest {


    public static final junit.framework.Test suite() {
        NbModuleSuite.Configuration cfg = NbModuleSuite.emptyConfiguration().
                honorAutoloadEager(true).
                enableClasspathModules(false).
                gui(false);
        
        return cfg.clusters("platform|webcommon|ide").addTest(S.class).suite();
    }
    
    public static class S extends TestSuite {
        public S() throws Exception {
            ClassLoader parent = Lookup.getDefault().lookup(ClassLoader.class);
            URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
            ClassLoader ldr = new URLClassLoader(new URL[] { u }, parent);
            Class c = ldr.loadClass("org.netbeans.libs.graalsdk.impl.GraalEnginesTest2");
            addTest(new NbTestSuite(c));
        }
    }
}
