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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author sdedic
 */
class NbRemoteLoader extends RemoteClassLoader {
    private final NbRemoteLoader oldDelegate;
    private final Map<String, Long> classIds;
    private final Map<String, Class>  namedClasses;
    private final Map<Long, Class>  definedClasses;
    
    public NbRemoteLoader(ClassLoader parent, ClassLoader oldDelegate, URL[] additionalURLs) {
        super(additionalURLs);
        if (oldDelegate != null && !(oldDelegate instanceof NbRemoteLoader)) {
            throw new IllegalArgumentException("Invalid classloader: " + oldDelegate);
        }
        this.oldDelegate = (NbRemoteLoader)oldDelegate;
        if (oldDelegate == null) {
            classIds = new HashMap<String, Long>();
            definedClasses = new HashMap<Long, Class>();
            namedClasses = new HashMap<String, Class>();
        } else {
            classIds = this.oldDelegate.classIds;
            definedClasses = this.oldDelegate.definedClasses;
            namedClasses = this.oldDelegate.namedClasses;
        }
    }
    
    private Class registerClass(Class c) {
        Long ret = (long)classIds.size() + 1;
        String className = c.getName();
        classIds.put(className, ret);
        definedClasses.put(ret, c);
        namedClasses.put(className, c);
        return c;
    }
    
    Long getClassId(String className) {
        return classIds.get(className);
    }
    
    Class getClassOfId(Long id) {
        if (id == null) {
            return null;
        }
        return definedClasses.get(id);
    }
    
    /**
     * Finds the class.
     * The order should be:
     * - the delegate classloader
     * 
     * @param name
     * @return
     * @throws ClassNotFoundException 
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class c;
        
        try {
            // contact the original classloader for the application; it should not load REPL classes.
            c = getParent().loadClass(name);
            return c;
        } catch (ClassNotFoundException ex) {
        }
        if (oldDelegate != null) {
            try {
                c = oldDelegate.findClass(name);
                return registerClass(c);
            } catch (ClassNotFoundException ex) {
            }
        }
        c = namedClasses.get(name);
        if (c != null) {
            return c;
        }
        c = super.findClass(name);
        return registerClass(c);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> origResources = getParent().getResources(name);
        Enumeration<URL> deleResources = oldDelegate != null ? oldDelegate.findResources(name) : Collections.<URL>emptyEnumeration();
        
        return new CompoundEnumeration<URL>(new Enumeration[] {
                origResources,
                deleResources,
                super.findResources(name)
        });
    }

    @Override
    public URL findResource(String name) {
        try {
            Enumeration<URL> res = findResources(name);
            return res.hasMoreElements() ? res.nextElement() : null;
        } catch (IOException ex) {
            return null;
        }
    }

    
    private static class CompoundEnumeration<T> implements Enumeration<T> {
        private final Enumeration<T>[] enums;
        private final HashSet<T>  enumerated = new HashSet<T>();
        
        private int index;
        private T   nextItem;

        public CompoundEnumeration(Enumeration<T>[] enums) {
            this.enums = enums;
        }
        
        @Override
        public boolean hasMoreElements() {
            if (nextItem != null) {
                return true;
            }
            while (index < enums.length) {
                Enumeration<T> del = enums[index];
                if (del.hasMoreElements()) {
                    T item = del.nextElement();
                    if (!enumerated.add(item)) {
                        continue;
                    }
                    nextItem = item;
                    break;
                } else {
                    index++;
                }
            }
            return nextItem != null;
        }

        @Override
        public T nextElement() {
            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }
            return nextItem;
        }
    
    }
}
