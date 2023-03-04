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
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Making sure our framework can replace already registered factories.
 * 
 * @author Jaroslav Tulach
 */
public class NbURLStreamHandlerFactoryWhenSetTest extends NbURLStreamHandlerFactoryTest {
    static {
        // preregister some strange factory:
        java.net.URL.setURLStreamHandlerFactory(new SomeURLStreamHandFact());
    }
    
    
    public NbURLStreamHandlerFactoryWhenSetTest(String s) {
        super(s);
    }
    
    public void testDefaultImpleDelegatesToPreviousURLFactory() throws Exception {
        URL u = new URL("jarda://ClassLoaderCacheContent.properties/");
        
        byte[] arr = new byte[100000];
        int len = u.openStream().read(arr);
        if (len < 50000) {
            fail("Should be able to read at least 50KB: " + len);
        }
    }
    
    private static class SomeURLStreamHandFact implements URLStreamHandlerFactory {
        
        
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("jarda".equals(protocol)) {
                return new H();
            }
            return null;
        }
    }
    
    private static class H extends URLStreamHandler {
        protected URLConnection openConnection(URL u) throws IOException {
            return getClass().getResource(u.getHost()).openConnection();
        }
    }
}
