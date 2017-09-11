/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
