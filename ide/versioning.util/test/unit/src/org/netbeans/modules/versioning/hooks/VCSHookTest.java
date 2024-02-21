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

package org.netbeans.modules.versioning.hooks;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHookTest extends NbTestCase {

    public VCSHookTest(String testName) {
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

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testGetHook() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        Collection<TestVCSHook> c = VCSHooks.getInstance().getHooks(TestVCSHook.class);

        TestVCSHook[] hooks = c.toArray(new TestVCSHook[0]);
        assertEquals(2, hooks.length);

        boolean a = false;
        boolean b = false;
        for (TestVCSHook hook : hooks) {
            if(!a) a = (hook instanceof TestVCSHookFactoryA.TestVCSHookImplA);
            if(!b) b = (hook instanceof TestVCSHookFactoryB.TestVCSHookImplB);
        }
        assertTrue(a);
        assertTrue(b);
    }

    public abstract static class TestVCSHook<TestVCSHookContext> extends VCSHook {

    }

    public abstract static class TestVCSHookContext extends VCSHookContext {
        public TestVCSHookContext(File[] files) {
            super(files);
        }
    }

//    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.hooks.VCSHookFactory.class)
//    public static class TestVCSHookFactoryA extends VCSHookFactory<TestVCSHook>{
//
//        @Override
//        public TestVCSHook createHook() {
//            return new TestVCSHookImplA();
//        }
//
//
//        public class TestVCSHookImplA<TestVCSHookContextImplA> extends TestVCSHook {
//            @Override
//            public JPanel createComponent(VCSHookContext t) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//            @Override
//            public String getDisplayName() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        }
//
//        public class TestVCSHookContextImplA extends VCSHookContext {
//            public TestVCSHookContextImplA(File[] files) {
//                super(files);
//            }
//        }
//    }
//
//    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.hooks.VCSHookFactory.class)
//    public static class TestVCSHookFactoryB extends VCSHookFactory<TestVCSHook>{
//
//        @Override
//        public TestVCSHook createHook() {
//            return new TestVCSHookImplB();
//        }
//
//
//        public class TestVCSHookImplB<TestVCSHookContextImplB> extends TestVCSHook {
//            @Override
//            public JPanel createComponent(VCSHookContext t) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//            @Override
//            public String getDisplayName() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        }
//
//        public class TestVCSHookContextImplB extends VCSHookContext {
//            public TestVCSHookContextImplB(File[] files) {
//                super(files);
//            }
//        }
//    }

}
