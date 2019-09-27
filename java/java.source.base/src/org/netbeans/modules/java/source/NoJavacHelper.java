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
package org.netbeans.modules.java.source;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openide.modules.OnStart;

/**
 *
 * @author lahvac
 */
public class NoJavacHelper {

    private static final AtomicReference<Boolean> hasWorkingJavac = new AtomicReference<>();
    public static boolean hasWorkingJavac() {
        Boolean res = hasWorkingJavac.get();
        if (res != null) {
            return res;
        }
        try {
            Class.forName("javax.lang.model.element.ModuleElement");
            res = true;
        } catch (ClassNotFoundException ex) {
            //OK
            res = false;
        }
        hasWorkingJavac.compareAndSet(null, res);
        return hasWorkingJavac.get();
    }

    public static boolean hasNbJavac() {
        try {
            Class.forName("com.sun.tools.javac.comp.Repair");
            return true;
        } catch (ClassNotFoundException ex) {
            //OK
            return false;
        }
    }

    @OnStart
    public static class FixClasses implements Runnable {

        @Override
        public void run() {
            if (!hasWorkingJavac()) {
                String JavaVersion = System.getProperty("java.specification.version"); //NOI18N
                boolean isJdkVer8OrBelow = true;
                if (!JavaVersion.startsWith("1.")) {   //NOI18N
                    isJdkVer8OrBelow = false;
                }
                if (isJdkVer8OrBelow) {
                    {
                        ClassWriter w = new ClassWriter(0);
                        w.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC, "com/sun/tools/javac/code/Scope$WriteableScope", null, "com/sun/tools/javac/code/Scope", null);
                        byte[] classData = w.toByteArray();

                        defineClass("com.sun.tools.javac.code.Scope$WriteableScope",
                                    "com.sun.tools.javac.code.Scope",
                                    classData);
                    }
                    {
                        ClassWriter w = new ClassWriter(0);
                        w.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_PUBLIC, "javax/lang/model/element/ModuleElement", null, "java/lang/Object", new String[] {"javax/lang/model/element/Element"});
                        byte[] classData = w.toByteArray();

                        defineClass("javax.lang.model.element.ModuleElement",
                                    "com.sun.tools.javac.code.Scope",
                                    classData);
                    }
                }
            }
        }

        private void defineClass(String fqn, String injectToClass, byte[] classData) {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe"); //NOI18N
                theUnsafe.setAccessible(true);
                Object unsafe = theUnsafe.get(null);

                Class targetClass = Class.forName(injectToClass);  //NOI18N

                Method defineClass = unsafeClass.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);  //NOI18N
                defineClass.invoke(unsafe, fqn, classData, 0, classData.length, targetClass.getClassLoader(), targetClass.getProtectionDomain());  //NOI18N 
            } catch (Throwable t) {
                //ignore...
                Logger.getLogger(NoJavacHelper.class.getName()).log(Level.WARNING, null, t);
            }
        }
    }
}
