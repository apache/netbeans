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

package org.netbeans.core.startup;

import java.io.IOException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Template;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;

/**
 * Test functionality of internal URLs.
 * @author Jesse Glick
 */
public class NbURLStreamHandlerFactoryDeadlockTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", MyLkp.class.getName());
    }
    
    public NbURLStreamHandlerFactoryDeadlockTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Main.initializeURLFactory();
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    
    
    public void testNbResourceStreamHandlerAndURLStreamHandlerFactoryMerging() throws Exception {
        URL url = new URL("https://www.netbeans.org");
        
        Class<?> c = getClass();
        assertClass(c);
    }
    
    private static void assertClass(Class<?> c) throws IOException {
        URL u = c.getResource(c.getSimpleName() + ".class");
        assertNotNull("Resource for " + c.getSimpleName() + " found", u);
        byte[] arr = new byte[4096];
        int r = u.openStream().read(arr);
        if (r <= 0) {
            fail("Should read something: " + r);
        }
    }
    
    public static class MyLkp extends AbstractLookup implements Runnable {
        @Override
        protected  void beforeLookup(Template<?> template) {
            RequestProcessor.getDefault().post(this).waitFinished();
        }
        
        public void run() {
            try {
                URL url = new URL("https://www.netbeans.org");
                assertClass(NbURLStreamHandlerFactoryDeadlockTest.class);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
