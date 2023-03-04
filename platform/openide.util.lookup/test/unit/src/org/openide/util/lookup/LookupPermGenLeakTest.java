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
package org.openide.util.lookup;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LookupPermGenLeakTest extends NbTestCase implements InvocationHandler {
    private int cnt;

    public LookupPermGenLeakTest(String name) {
        super(name);
    }
    
    protected URLClassLoader createClassLoader() {
        URLClassLoader loader;
        URL lookup = Lookup.class.getProtectionDomain().getCodeSource().getLocation();
        URL tests = LookupPermGenLeakTest.class.getProtectionDomain().getCodeSource().getLocation();
        loader = new URLClassLoader(
            new URL[] { lookup, tests },
            Lookup.class.getClassLoader().getParent()
        );
        return loader;
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    private Object createClass() throws Exception {
        URLClassLoader loader;
        Class<?> classLookup;
        Class<?> classLookups;
        Class<?> classLookupListener;
        Class<?> classLookupResult;
        Class<?> classI;
        Method methodMetainfServices;
        Method methodLookupResult;
        Method methodAddLookupListener;
        Method methodAllInstances;
        
        loader = createClassLoader();
        
        classLookup = loader.loadClass(Lookup.class.getName());
        classLookups = loader.loadClass(Lookups.class.getName());
        classLookupListener = loader.loadClass(LookupListener.class.getName());
        classLookupResult = loader.loadClass(Lookup.Result.class.getName());
        classI = loader.loadClass(I.class.getName());
        methodMetainfServices = classLookups.getMethod("metaInfServices", ClassLoader.class);
        Method methodLookupAll = classLookup.getMethod("lookupAll", Class.class);
        methodLookupResult = classLookup.getMethod("lookupResult", Class.class);
        methodAddLookupListener = classLookupResult.getMethod("addLookupListener", classLookupListener);
        methodAllInstances = classLookupResult.getMethod("allInstances");
        Object lkp = methodMetainfServices.invoke(null, loader);
        Object res = methodLookupResult.invoke(lkp, Serializable.class);
        
        Object listener = Proxy.newProxyInstance(loader, new Class[] { classLookupListener }, this);
        methodAddLookupListener.invoke(res, listener);
        
        Collection<?> noneThere = (Collection<?>) methodAllInstances.invoke(res);
        assertEquals("There is nothing yet: " + noneThere, 0, noneThere.size());
        
        methodLookupAll.invoke(lkp, classI);
        
        Collection<?> oneThere = (Collection<?>) methodAllInstances.invoke(res);
        assertEquals("There is one item: " + oneThere, 1, oneThere.size());
        
        assertEquals("One change", 1, waitForOne());
        
        return classLookup;
    }
    
    public void testClassLoaderCanGC() throws Exception {
        Reference<?> ref = new WeakReference<Object>(createClass());
        // assertGC("Can be GCed", ref); TODO: Uncomment after #257013 is implemented.
    }
    
    private synchronized int waitForOne() throws InterruptedException {
        while (cnt != 1) {
            wait();
        }
        return cnt;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("resultChanged")) { // NOI18N
            cnt++;
            notifyAll();
        }
        return null;
    }
    
    
    public static interface I {
    }
    
    @ServiceProvider(service=I.class)
    public static final class Impl implements I, Serializable {
        
    }
}
