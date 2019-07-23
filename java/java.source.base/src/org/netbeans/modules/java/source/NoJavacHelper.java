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

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ExpressionTree;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openide.modules.OnStart;
import sun.misc.Unsafe;

/**
 *
 * @author lahvac
 */
public class NoJavacHelper {

    public static boolean hasWorkingJavac() {
        try {
            Class.forName("javax.lang.model.element.ModuleElement");
            return true;
        } catch (ClassNotFoundException ex) {
            //OK
            return false;
        }
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
                ClassWriter w = new ClassWriter(0);
                w.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC, "com/sun/tools/javac/code/Scope$WriteableScope", null, "com/sun/tools/javac/code/Scope", null);
                byte[] classData = w.toByteArray();

                String JavaVersion = System.getProperty("java.specification.version"); //NOI18N
                boolean isJdkVer8OrBelow = true;
                if (!JavaVersion.startsWith("1.")) {   //NOI18N
                    isJdkVer8OrBelow = false;
                }
                if (isJdkVer8OrBelow) {
                    try {
                        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe"); //NOI18N
                        theUnsafe.setAccessible(true);
                        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

                        Class scopeClass = Class.forName("com.sun.tools.javac.code.Scope");  //NOI18N

                        Method defineClass = Unsafe.class.getDeclaredMethod("defineClass", String.class, Byte[].class, Integer.class, Integer.class, ClassLoader.class, ProtectionDomain.class);  //NOI18N
                        defineClass.invoke(unsafe, "com.sun.tools.javac.code.Scope$WriteableScope", classData, 0, classData.length, scopeClass.getClassLoader(), scopeClass.getProtectionDomain());  //NOI18N 
                    } catch (Throwable t) {
                        //ignore...
                        Logger.getLogger(NoJavacHelper.class.getName()).log(Level.FINE, null, t);
                    }
                } else {
                    try {
                        Method defineClass = MethodHandles.Lookup.class.getDeclaredMethod("defineClass", Byte[].class); //NOI18N
                        defineClass.invoke(MethodHandles.lookup(), classData);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        Logger.getLogger(NoJavacHelper.class.getName()).log(Level.FINE, null, ex);

                    }
                }

            }
        }

    }
}
