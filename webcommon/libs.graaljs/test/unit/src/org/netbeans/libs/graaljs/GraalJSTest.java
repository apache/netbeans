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
package org.netbeans.libs.graaljs;

import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import junit.framework.TestSuite;
import org.graalvm.polyglot.Context;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Lookup;

public final class GraalJSTest extends NbTestCase {
    public GraalJSTest(String name) {
        super(name);
    }

    public static Test suite() throws Exception {
        TestSuite ts = new TestSuite();
        
        NbModuleSuite.Configuration cfg = NbModuleSuite.emptyConfiguration().
            clusters("platform|webcommon|ide").
            honorAutoloadEager(true).
            gui(false);
        ts.addTest(cfg.addTest(S.class).suite());
        ts.addTestSuite(GraalJSTest.class);
        return ts;
    }
    
    /**
     * Checks direct JS invocation through polyglot API, using classpath
     * @throws Exception 
     */
    public void testDirectEvaluationOfGraalJS() throws Exception {
        try {
            Context ctx = Context.newBuilder("js").build();
            assumeTrue(ctx.getEngine().getLanguages().keySet().contains("js"));
            int fourtyTwo = ctx.eval("js", "6 * 7").asInt();
            assertEquals(42, fourtyTwo);
        } catch (NoClassDefFoundError ex) {
            // this should not be necessary; graal.js and graal.sdk libraries are on test classpath:
            // either those libraries, or the system will win, this classloader delegates to parent first.
            assumeFalse(System.getProperty("java.vm.version").contains("jvmci-"));
            throw ex;
        }
    }
    
    public static class S extends TestSuite {
        public S() throws Exception {
            ClassLoader parent = Lookup.getDefault().lookup(ClassLoader.class);
            Class c = parent.loadClass("org.netbeans.libs.graaljs.GraalJSTest2");
            addTest(new NbTestSuite(c));
        }
    }
}
