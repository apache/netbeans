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

package org.netbeans;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.WeakSet;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NbInstrumentation implements InvocationHandler {
    private static final Logger LOG = Logger.getLogger(NbInstrumentation.class.getName());
    private static final Object LOCK = new Object();
    private static volatile Collection<NbInstrumentation> ACTIVE;

    private final List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<Boolean> IN = new ThreadLocal<>();
   
    private final Instrumentation instrumentationProxy;

    public NbInstrumentation() {
        instrumentationProxy = (Instrumentation) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Instrumentation.class }, this);
    }

    static NbInstrumentation registerAgent(ClassLoader l, String agentClassName) {
        try {
            return registerImpl(agentClassName, l);
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Cannot register " + agentClassName, ex);
            return null;
        }
    }

    static void unregisterAgent(NbInstrumentation instr) {
        synchronized (LOCK) {
            if (ACTIVE != null) {
                Collection<NbInstrumentation> clone = new WeakSet<>(ACTIVE);
                clone.remove(instr);
                ACTIVE = clone;
            }
        }
    }

    private static NbInstrumentation registerImpl(String agentClassName, ClassLoader l) throws ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        final NbInstrumentation inst = new NbInstrumentation();
        synchronized (LOCK) {
            if (ACTIVE == null) {
                ACTIVE = new WeakSet<>();
            } else {
                ACTIVE = new WeakSet<>(ACTIVE);
            }
            ACTIVE.add(inst);
        }
        Class<?> agentClass = Class.forName(agentClassName, true, l);
        try {
            Method m = agentClass.getMethod("agentmain", String.class, Instrumentation.class); // NOI18N
            m.invoke(null, "", inst.getInstrumentationProxy());
        } catch (NoSuchMethodException ex) {
            Method m = agentClass.getMethod("agentmain", String.class); // NOI18N
            m.invoke(null, "");
        }
        return inst;
    }
    
    public static byte[] patchByteCode(ClassLoader l, String className, ProtectionDomain pd, byte[] arr) throws IllegalClassFormatException {
        if (ACTIVE == null) {
            return arr;
        }
        if (Boolean.TRUE.equals(IN.get())) {
            return arr;
        }
        try {
            IN.set(Boolean.TRUE);
            for (NbInstrumentation inst : ACTIVE) {
                for (ClassFileTransformer t : inst.transformers) {
                    arr = t.transform(l, className, null, pd, arr);
                }
            }
        } finally {
            IN.set(null);
        }
        return arr;
    }
    
    //
    // Instrumentation methods
    //

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "addTransformer":
                return transformers.add((ClassFileTransformer) args[0]);
            case "removeTransformer":
                return transformers.remove(args[0]);
            case "isRetransformClassesSupported":
            case "isRedefineClassesSupported":
            case "isModifiableClass":
            case "isNativeMethodPrefixSupported":
            case "isModifiableModule":
                return false;
            case "retransformClasses":
            case "redefineClasses":
                throw new UnmodifiableClassException();
            case "getAllLoadedClasses":
            case "getInitiatedClasses":
                return new Class[0];
            case "getObjectSize":
                return 42;
            case "appendToSystemClassLoaderSearch":
            case "setNativeMethodPrefix":
            case "redefineModule":
                throw new UnsupportedOperationException();
            default:
                return null;
        }
    }

    public Instrumentation getInstrumentationProxy() {
        return instrumentationProxy;
    }
}
