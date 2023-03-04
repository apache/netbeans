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

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

public class MetaInfCacheTest extends NbTestCase {

    public MetaInfCacheTest(String s) {
        super(s);
    }
    
    public void testNoRehash() {
        Object[] instances = generateProxyTypes(4);
        
        
        MetaInfCache mic = new MetaInfCache(12);
        CharSequence log = Log.enable("org.openide.util.lookup.MetaInfServicesLookup", Level.CONFIG);
        for (Object o : instances) {
            mic.storeInstance(o);
        }
        if (log.toString().indexOf("Rehashing") != -1) {
            fail("No rehash please:\n" + log);
        }
        
        for (Object o : instances) {
            final Class<? extends Object> type = o.getClass();
            final Object cached = mic.findInstance(type);
            assertSame("Are the same", o, cached);
        }
    }
    public void testRehashABit() {
        Object[] instances = generateProxyTypes(13);
        
        
        MetaInfCache mic = new MetaInfCache(10);
        CharSequence log = Log.enable("org.openide.util.lookup.MetaInfServicesLookup", Level.CONFIG);
        for (Object o : instances) {
            mic.storeInstance(o);
        }
        if (log.toString().indexOf("Rehashing") == -1) {
            fail("Should be rehashed:\n" + log);
        }
        
        for (Object o : instances) {
            final Class<? extends Object> type = o.getClass();
            final Object cached = mic.findInstance(type);
            assertSame("Are the same", o, cached);
        }
    }
    public void testRehashFourTimes() {
        Object[] instances = generateProxyTypes(128);
        
        
        MetaInfCache mic = new MetaInfCache(10);
        CharSequence log = Log.enable("org.openide.util.lookup.MetaInfServicesLookup", Level.CONFIG);
        for (Object o : instances) {
            mic.storeInstance(o);
        }
        if (log.toString().indexOf("Rehashing") == -1) {
            fail("Should be rehashed:\n" + log);
        }
        
        for (Object o : instances) {
            final Class<? extends Object> type = o.getClass();
            final Object cached = mic.findInstance(type);
            assertSame("Are the same", o, cached);
        }
    }
    
    public void testGarbageCollection() {
        String s1 = new String("first");
        String s2 = new String("second");
        
        MetaInfCache mic = new MetaInfCache(128);
        mic.storeInstance(s1);
        mic.storeInstance(s2);
        
        assertEquals("First is found", s1, mic.findInstance(String.class));
        
        Reference<String> ref = new WeakReference<String>(s1);
        s1 = null;
        assertGC("Instances are held weakly", ref);
        
        assertEquals("Now the second instance is found", s2, mic.findInstance(String.class));
    }
    
    private static Object[] generateProxyTypes(int cnt) {
        Object[] arr = new Object[cnt];
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (int i = 0; i < cnt; i++) {
            arr[i] = generateProxyType(i + 1);
            types.add(arr[i].getClass());
        }
        assertEquals("Enough different types generated", cnt, types.size());
        return arr;
    }
    private static Object generateProxyType(final int i) {
        class DummyH implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                if (method.getName().equals("toString")) {
                    return "DummyH[" + i + "]";
                }
                return null;
            }
        }
        return Proxy.newProxyInstance(
            MetaInfCache.class.getClassLoader(),
            findTypes(i), 
            new DummyH()
        );
    }
    
    
    private static Class[] findTypes(int perm) {
        List<Class> selected = new ArrayList<Class>();
        for (int i = 0, mask = 1; i < 30; i++, mask *= 2) {
            if ((mask & perm) != 0) {
                selected.add(IFCS[i]);
            }
        }
        assertTrue(selected.size() > 0);
        return selected.toArray(new Class[0]);
    }
    
    private static final Class[] IFCS = {
        Runnable.class,
        Callable.class,
        Serializable.class,
        PropertyChangeListener.class,
        ChangeListener.class,
        VetoableChangeListener.class,
        EventListener.class,
        Externalizable.class
    };
}
