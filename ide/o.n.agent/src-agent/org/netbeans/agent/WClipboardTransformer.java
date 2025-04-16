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
package org.netbeans.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;

/**
 * Transform the sun.awt.windows.WClipboard class to fix a copy-n-paste problem
 * observed on windows. It was observed, that sometime copying from NetBeans to
 * other windows applications fails.
 *
 * Discussion can be found here: https://github.com/apache/netbeans/discussions/7051
 *
 * A test was provided to reproduce. The test uses java.awt.Robot to do this:
 *
 * 1. Start notepad.exe and enter "sometext" into notepad window
 * 2. Copy current line to clipboard
 * 3. Input a newline into the notepad window
 * 4. Switch to NetBeans
 * 5. Past text into current NetBeans editor window
 * 6. Append loop index to pasted line
 * 7. Copy the current line to clipboard
 * 8. Input a newline into the NetBeans editor
 * 9. Switch to notepad.exe
 * 10. Paste text into notepad.exe
 * 11. Repeat from step 2
 *
 * The transformer can be debugged:
 *
 * - The system property "org.netbeans.agent.WClipboardTransformer.disable"
 *   takes a boolean to disable the transformer. I.e. running NetBeans as
 *
 *   NBBASEPATH/bin/netbeans64.exe -J-Dorg.netbeans.agent.WClipboardTransformer.disable=true
 *
 *   will run without this.
 *
 * - The system property "org.netbeans.agent.WClipboardTransformer.dumpPath"
 *   takes a string. The string is expected to be a path and the transformed
 *   class data will be dumped at that location. It is expected, that a path
 *   including the filename is specified.
 *
 * - The system property "org.netbeans.agent.WClipboardTransformer.debug"
 *   can be set to true to get info about the transformation process
 */
public class WClipboardTransformer implements ClassFileTransformer {

    public static final String DEBUG_DISABLE_TRANSFORMER = "org.netbeans.agent.WClipboardTransformer.disable";

    private static final Type TYPE_JAVA_AWT_AWT_EVENT = Type.getType("Ljava/awt/AWTEvent;");
    private static final Type TYPE_JAVA_AWT_EVENT_INVOCATION_EVENT = Type.getType("Ljava/awt/event/InvocationEvent;");
    private static final Type TYPE_JAVA_AWT_TOOLKIT = Type.getType("Ljava/awt/Toolkit;");
    private static final Type TYPE_JAVA_LANG_EXCEPTION = Type.getType("Ljava/lang/Exception;");
    private static final Type TYPE_JAVA_LANG_THROWABLE = Type.getType("Ljava/lang/Throwable;");
    private static final Type TYPE_JAVA_LANG_OBJECT = Type.getType("Ljava/lang/Object;");
    private static final Type TYPE_JAVA_LANG_RUNNABLE = Type.getType("Ljava/lang/Runnable;");
    private static final Type TYPE_JAVA_LANG_STRING = Type.getType("Ljava/lang/String;");
    private static final Type TYPE_JAVA_LANG_SYSTEM = Type.getType("Ljava/lang/System;");
    private static final Type TYPE_JAVA_LANG_SYSTEM_LOGGER = Type.getType("Ljava/lang/System$Logger;");
    private static final Type TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL = Type.getType("Ljava/lang/System$Logger$Level;");
    private static final Type TYPE_SUN_AWT_APP_CONTEXT = Type.getType("Lsun/awt/AppContext;");
    private static final Type TYPE_SUN_AWT_SUN_TOOLKIT = Type.getType("Lsun/awt/SunToolkit;");
    private static final Type TYPE_SUN_AWT_WINDOWS_WCLIPBOARD = Type.getType("Lsun/awt/windows/WClipboard;");

    private static final String DEBUG_DUMP_TRANSFORMED_CLASS = System.getProperty("org.netbeans.agent.WClipboardTransformer.dumpPath");
    private static final boolean DEBUG_TRANSFORMER = Boolean.getBoolean("org.netbeans.agent.WClipboardTransformer.debug");
    private static final String WCLIPBOARD_LOGGER_NAME  = "sun.awt.windows.WClipboard";

    private final Instrumentation instrumentation;

    public WClipboardTransformer(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // Only transform WClipboard class - ignore all other classes
        if ("sun/awt/windows/WClipboard".equals(className)) {
            logMsg("%s: Transforming %s", WClipboardTransformer.class.getName(), className);
            try {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        // The original handleContentsChanged method is retained under the name
                        // handleContentsChanged0. Apart from the rename, the method is also
                        // marked as being synchronized.
                        if ("handleContentsChanged".equals(name)) {
                            logMsg("%s: Renaming handleContentsChanged -> handleContentsChanged0 and marking it synchronized", WClipboardTransformer.class.getName());
                            return super.visitMethod(access | Opcodes.ACC_SYNCHRONIZED, "handleContentsChanged0", descriptor, signature, exceptions);
                        } else {
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }

                    @Override
                    public void visitEnd() {
                        logMsg("%s: Creating handleContentsChanged Wrapper", WClipboardTransformer.class.getName());
                        // Implement replacement handleContentsChanged method,
                        // that essentially dispatches the change handling into
                        // the event loop.
                        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PRIVATE, "handleContentsChanged", "()V", null, new String[]{});
                        GeneratorAdapter ga = new GeneratorAdapter(mv, Opcodes.ACC_PRIVATE, "handleContentsChanged", "()V");
                        Label start = ga.mark();
                        Label end = ga.newLabel();
                        generateLoggerInvocation(ga, WCLIPBOARD_LOGGER_NAME, "DEBUG", "handleContentsChanged entered");
                        ga.invokeStatic(TYPE_SUN_AWT_APP_CONTEXT, new Method("getAppContext", TYPE_SUN_AWT_APP_CONTEXT, new Type[0]));
                        ga.storeLocal(1, TYPE_SUN_AWT_APP_CONTEXT);
                        ga.loadLocal(1);
                        Label afterAppContextCheck = ga.newLabel();
                        ga.ifNonNull(afterAppContextCheck);
                        // Fallback path, if no AppContext is available, directly call the original method
                        generateLoggerInvocation(ga, WCLIPBOARD_LOGGER_NAME, "DEBUG", "Entering fallback path (no AppContext found)");
                        ga.loadThis();
                        ga.invokeVirtual(TYPE_SUN_AWT_WINDOWS_WCLIPBOARD, new Method("handleContentsChanged0", Type.VOID_TYPE, new Type[0]));
                        ga.returnValue();
                        ga.mark(afterAppContextCheck);
                        // The new/fix path uses an InvocationEvent to dispatch update handling
                        // into the event loop. The InvocationEvent is created with a lambda, that
                        // essentially calls handleContentsChanged0
                        generateLoggerInvocation(ga, WCLIPBOARD_LOGGER_NAME, "DEBUG", "Dispatching update to event loop");
                        ga.loadLocal(1);
                        ga.newInstance(TYPE_JAVA_AWT_EVENT_INVOCATION_EVENT);
                        ga.dup();
                        ga.invokeStatic(TYPE_JAVA_AWT_TOOLKIT, new Method("getDefaultToolkit", TYPE_JAVA_AWT_TOOLKIT, new Type[0]));
                        ga.loadThis();
                        ga.invokeDynamic(
                                "run",
                                "(Lsun/awt/windows/WClipboard;)Ljava/lang/Runnable;",
                                new Handle(
                                        H_INVOKESTATIC,
                                        "java/lang/invoke/LambdaMetafactory",
                                        "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                                        false
                                ),
                                Type.getMethodType(Type.VOID_TYPE),
                                new Handle(
                                        H_INVOKEVIRTUAL,
                                        "sun/awt/windows/WClipboard",
                                        "handleContentsChanged0",
                                        "()V",
                                        false
                                ),
                                Type.getMethodType(Type.VOID_TYPE));
                        ga.invokeConstructor(TYPE_JAVA_AWT_EVENT_INVOCATION_EVENT, new Method("<init>", Type.VOID_TYPE, new Type[]{TYPE_JAVA_LANG_OBJECT, TYPE_JAVA_LANG_RUNNABLE}));
                        ga.invokeStatic(TYPE_SUN_AWT_SUN_TOOLKIT, new Method("postEvent", Type.VOID_TYPE, new Type[]{TYPE_SUN_AWT_APP_CONTEXT, TYPE_JAVA_AWT_AWT_EVENT}));
                        ga.returnValue();
                        ga.mark(end);
                        // Catch Exceptions and log them
                        ga.catchException(start, end, TYPE_JAVA_LANG_EXCEPTION);
                        ga.push(WCLIPBOARD_LOGGER_NAME );
                        ga.invokeStatic(TYPE_JAVA_LANG_SYSTEM, new Method("getLogger", TYPE_JAVA_LANG_SYSTEM_LOGGER, new Type[]{TYPE_JAVA_LANG_STRING}));
                        ga.swap();
                        ga.getStatic(TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL, "WARNING", TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL);
                        ga.swap();
                        ga.push("Exception in handleContentsChanged invocation");
                        ga.swap();
                        ga.invokeInterface(TYPE_JAVA_LANG_SYSTEM_LOGGER, new Method("log", Type.VOID_TYPE, new Type[]{TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL, TYPE_JAVA_LANG_STRING, TYPE_JAVA_LANG_THROWABLE}));
                        ga.returnValue();
                        ga.endMethod();
                        logMsg("%s: Creating handleContentsChanged Wrapper done", WClipboardTransformer.class.getName());
                        super.visitEnd();
                    }

                    private void generateLoggerInvocation(GeneratorAdapter ga, String loggerName, String levelName, String message) {
                        ga.push(loggerName);
                        ga.invokeStatic(TYPE_JAVA_LANG_SYSTEM, new Method("getLogger", TYPE_JAVA_LANG_SYSTEM_LOGGER, new Type[]{TYPE_JAVA_LANG_STRING}));
                        ga.getStatic(TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL, levelName, TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL);
                        ga.push(message); // STRING
                        ga.invokeInterface(TYPE_JAVA_LANG_SYSTEM_LOGGER, new Method("log", Type.VOID_TYPE, new Type[]{TYPE_JAVA_LANG_SYSTEM_LOGGER_LEVEL, TYPE_JAVA_LANG_STRING}));
                    }

                };
                cr.accept(cv, 0);
                byte[] result = cw.toByteArray();
                if (DEBUG_DUMP_TRANSFORMED_CLASS != null && !DEBUG_DUMP_TRANSFORMED_CLASS.isBlank()) {
                    Files.write(Path.of(DEBUG_DUMP_TRANSFORMED_CLASS), result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                logMsg("%s: Transforming %s done", WClipboardTransformer.class.getName(), className);
                return result;
            } catch (IOException | RuntimeException ex) {
                logErr("%s: Transforming %s failed", WClipboardTransformer.class.getName(), className);
                ex.printStackTrace(System.err);
            } finally {
                instrumentation.removeTransformer(this);
            }
        }
        return null;
    }

    // Don't use JUL as this might interfer with initialization of the logging
    // system in the application/tests
    private void logMsg(String msg, Object... params) {
        if(DEBUG_TRANSFORMER) {
            System.err.printf(msg + "%n", params);
        }
    }

    private void logErr(String msg, Object... params) {
        System.err.printf(msg + "%n", params);
    }
}
