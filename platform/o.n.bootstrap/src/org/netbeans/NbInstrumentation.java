/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
final class NbInstrumentation implements Instrumentation {
    private static final Logger LOG = Logger.getLogger(NbInstrumentation.class.getName());
    private static final Object LOCK = new Object();
    private static volatile Collection<NbInstrumentation> ACTIVE;

    private final List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<ClassFileTransformer>();
    private static final ThreadLocal<Boolean> IN = new ThreadLocal<Boolean>();
    
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
                Collection<NbInstrumentation> clone = new WeakSet<NbInstrumentation>(ACTIVE);
                clone.remove(instr);
                ACTIVE = clone;
            }
        }
    }
    private static NbInstrumentation registerImpl(String agentClassName, ClassLoader l) throws ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        final NbInstrumentation inst = new NbInstrumentation();
        synchronized (LOCK) {
            if (ACTIVE == null) {
                ACTIVE = new WeakSet<NbInstrumentation>();
            } else {
                ACTIVE = new WeakSet<NbInstrumentation>(ACTIVE);
            }
            ACTIVE.add(inst);
        }
        Class<?> agentClass = Class.forName(agentClassName, true, l);
        try {
            Method m = agentClass.getMethod("agentmain", String.class, Instrumentation.class); // NOI18N
            m.invoke(null, "", inst);
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
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        transformers.add(transformer);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return transformers.remove(transformer);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return false;
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        throw new UnmodifiableClassException();
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return false;
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        throw new UnmodifiableClassException();
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getAllLoadedClasses() {
        return new Class[0];
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return new Class[0];
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return 42;
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return false;
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        throw new UnsupportedOperationException();
    }

    public void redefineModule(java.lang.Module module, Set<java.lang.Module> extraReads, Map<String, Set<java.lang.Module>> extraExports, Map<String, Set<java.lang.Module>> extraOpens, Set<Class<?>> extraUses, Map<Class<?>, List<Class<?>>> extraProvides) {
        throw new UnsupportedOperationException();
    }

    public boolean isModifiableModule(java.lang.Module module) {
        return false;
    }
}
