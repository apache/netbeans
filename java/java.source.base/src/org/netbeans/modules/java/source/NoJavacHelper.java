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

                String[] javaVersionElements = System.getProperty("java.version").split("\\.");
int major = Integer.parseInt(javaVersionElements[1]);

                // Java versioning is changing and has never really made sense.
                // Look at JEP 322 for the current scheme. 
                if ((Integer.parseInt(javaVersionElements[0]) >= 10) ||
                    ((Integer.parseInt(javaVersionElements[0]) == 1) && (Integer.parseInt(javaVersionElements[1]) >= 8))) {
                    try {
                        Method defineClass = MethodHandles.Lookup.class.getDeclaredMethod("defineClass", Byte[].class); //NOI18N
                        defineClass.invoke(MethodHandles.lookup(), classData);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        Logger.getLogger(NoJavacHelper.class.getName()).log(Level.FINE, null, ex);
                    }
                } else {
                    // Flag an error here?
                }
            }
        }

    }
}
