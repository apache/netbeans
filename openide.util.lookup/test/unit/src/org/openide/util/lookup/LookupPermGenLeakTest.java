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
