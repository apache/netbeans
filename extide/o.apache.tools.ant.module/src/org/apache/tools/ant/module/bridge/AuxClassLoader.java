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

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;

/**
 * Loads classes in the following order:
 * 1. JRE (well, actually app loader, but minus org.apache.tools.** and org.netbeans.**)
 * 2. Ant JARs - whatever is in the "main" class loader.
 * 3. Some NetBeans module class loader.
 * 4. Some other JAR from $nbhome/ant/nblib/*.jar.
 * Used for two cases:
 * A. bridge.jar for #4 and the Ant module for #3.
 * B. ant/nblib/o-n-m-foo.jar for #4 and modules/o-n-m-foo.jar for #3.
 * Lightly inspired by ProxyClassLoader, but much less complex.
 * @author Jesse Glick
 */
final class AuxClassLoader extends AntBridge.AllPermissionURLClassLoader {
    
    private static boolean masked(String name) {
        return name.startsWith("org.apache.tools.") && !name.startsWith("org.apache.tools.ant.module."); // NOI18N
    }
    
    private final ClassLoader nbLoader;
    
    public AuxClassLoader(ClassLoader nbLoader, ClassLoader antLoader, URL extraJar) {
        super(new URL[] {extraJar}, antLoader);
        this.nbLoader = nbLoader;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!masked(name)) {
            try {
                return nbLoader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // OK, didn't find it.
            }
        }
        try {
            return super.findClass(name);
        } catch (UnsupportedClassVersionError e) {
            // May be thrown during unit tests in case there is a JDK mixup.
            Exceptions.attachMessage(e, "loading: " + name);
            throw e;
        }
    }
    
    @Override
    public URL findResource(String name) {
        if (!masked(name)) {
            URL u = nbLoader.getResource(name);
            if (u != null) {
                return u;
            }
        }
        return super.findResource(name);
    }
    
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        // XXX probably wrong now... try to fix somehow
        return Enumerations.removeDuplicates (
            Enumerations.concat (
                nbLoader.getResources(name), 
                super.findResources(name)
            )
        );
    }

    public @Override String toString() {
        return super.toString() + "[nbLoader=" + nbLoader + "]"; // NOI18N
    }
    
    // XXX should maybe do something with packages... but oh well, it is rather hard.
    
}
