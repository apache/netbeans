/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
import java.util.Collections;
import java.util.List;
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

    @Override
    public Class[] getAllLoadedClasses() {
        return new Class[0];
    }

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
    
}
