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
package org.netbeans.modules.form;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Very simple class loader that delegates to several class loaders.
 * Note that no class returned by this class loader will claim that
 * it was loaded by this class loader. So, you probably don't want
 * to use this class unless you are sure that this is not a problem
 * in your context.
 *
 * @author Jan Stola
 */
public class MultiClassLoader extends ClassLoader {
    /** Class loader delegates. */
    private ClassLoader[] loaders;
    
    /**
     * Creates new {@code MultiClassLoader}.
     * 
     * @param loaders class loader delegates.
     */
    public MultiClassLoader(ClassLoader... loaders) {
        this.loaders = loaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {}
        }
        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {
        URL url = null;
        for (ClassLoader loader : loaders) {
            url = loader.getResource(name);
            if (url != null) {
                break;
            }
        }
        return url;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Vector<URL> vector = new Vector<URL>();
        for (ClassLoader loader : loaders) {
            Enumeration<URL> enumeration = loader.getResources(name);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                if (!vector.contains(url)) {
                    vector.add(url);
                }
            }
        }
        return vector.elements();
    }
    
}
