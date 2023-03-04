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
package org.foo;
// Does not do anything, just needs to be here & loadable.

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Something {
    protected String something() {
        return "hello";
    }
    
    private static final Logger LOG = Logger.getLogger(Something.class.getName());
    public static Class<?> loadClass(String name, ClassLoader ldr) throws ClassNotFoundException {
        LOG.log(Level.INFO, "Trying to load from {0} class named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Class.forName(name);
        }
        return Class.forName(name, true, ldr);
    }
    public static URL loadResource(String name, ClassLoader ldr) {
        LOG.log(Level.INFO, "Trying to load from {0} resource named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Something.class.getResource("/" + name);
        }
        return ldr.getResource(name);
    }
    public static Enumeration loadResources(String name, ClassLoader ldr) throws IOException {
        LOG.log(Level.INFO, "Trying to load from {0} resource named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Something.class.getClassLoader().getResources("/" + name);
        }
        return ldr.getResources(name);
    }
    public static InputStream loadResourceAsStream(String name, ClassLoader ldr) {
        LOG.log(Level.INFO, "Trying to load from {0} resource as stream named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Something.class.getResourceAsStream("/" + name);
        }
        return ldr.getResourceAsStream(name);
    }
    
}
