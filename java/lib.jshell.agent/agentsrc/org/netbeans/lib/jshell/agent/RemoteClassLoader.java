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
package org.netbeans.lib.jshell.agent;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class loader wrapper which caches class files by name until requested.
 * @author Robert Field
 */
class RemoteClassLoader extends URLClassLoader {

    private final Map<String, byte[]> classObjects = new TreeMap<String, byte[]>();

    RemoteClassLoader() {
        super(new URL[0]);
    }

    RemoteClassLoader(URL[] urls) {
        super(urls);
    }

    void delare(String name, byte[] bytes) {
        classObjects.put(name, bytes);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = classObjects.get(name);
        if (b == null) {
            return super.findClass(name);
        }
        return super.defineClass(name, b, 0, b.length, (CodeSource) null);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

}
