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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

/**
 *
 * @author lahvac
 */
public class NBJavacProcessingEnvironment extends JavacProcessingEnvironment {

    public static void preRegister(Context context) {
        context.put(JavacProcessingEnvironment.class, new Context.Factory<JavacProcessingEnvironment>() {
            @Override
            public JavacProcessingEnvironment make(Context c) {
                return new NBJavacProcessingEnvironment(c);
            }
        });
    }

    public NBJavacProcessingEnvironment(Context context) {
        super(context);
        Class<?> c = new ByteBuddy()
                        .subclass(JavacFiler.class)
                        .method(ElementMatchers.named("createSourceFile")).intercept(Advice.to(Interceptor.class))
                        .method(ElementMatchers.named("createClassFile")).intercept(Advice.to(Interceptor.class))
                        .method(ElementMatchers.named("createResourceFile")).intercept(Advice.to(Interceptor.class))
                        .make().load(JavacFiler.class.getClassLoader(), new ClassLoadingStrategy.ForUnsafeInjection()).getLoaded();
        
        try {
            Field filer = JavacProcessingEnvironment.class.getDeclaredField("filer");
            Object newFiler = c.getConstructor(Context.class).newInstance(context);
            filer.setAccessible(true);
            filer.set(this, newFiler);
        } catch (ReflectiveOperationException ex) {
            Logger.getLogger(NBJavacProcessingEnvironment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static final class Interceptor {
        @Advice.OnMethodEnter(inline=true)
        public static void enter(@Advice.This Object ths, @Advice.Origin Executable method, @Advice.AllArguments Object[] args) throws ReflectiveOperationException {
            Field fileManager = ths.getClass().getSuperclass().getDeclaredField("fileManager");
            fileManager.setAccessible(true);
            JavaFileManager fileManagerVal = (JavaFileManager) fileManager.get(ths);
            int idx = method.getName().equals("createResourceFile") ? 4 : 1;
            String key = method.getName().equals("createResourceFile") ? "apt-resource-element" : "apt-source-element";
            Element[] elements = (Element[]) args[idx];
            Set<String> urls = new HashSet<>();
            for (Element el : elements) {
                JavaFileObject classfile = ((Symbol) el).outermostClass().classfile;
                if (classfile != null) {
                    urls.add(classfile.toUri().toString());
                }
            }
            if (!urls.isEmpty()) {
                fileManagerVal.handleOption(key, urls.iterator());
            }
        }

        @Advice.OnMethodExit(onThrowable=Throwable.class,inline=true)
        public static void exit(@Advice.This Object ths, @Advice.Origin Executable method, @Advice.AllArguments Object[] args) throws ReflectiveOperationException {
            Field fileManager = ths.getClass().getSuperclass().getDeclaredField("fileManager");
            fileManager.setAccessible(true);
            JavaFileManager fileManagerVal = (JavaFileManager) fileManager.get(ths);
            int idx = method.getName().equals("createResourceFile") ? 4 : 1;
            String key = method.getName().equals("createResourceFile") ? "apt-resource-element" : "apt-source-element";
            Element[] elements = (Element[]) args[idx];
            Set<String> urls = new HashSet<>();
            for (Element el : elements) {
                JavaFileObject classfile = ((Symbol) el).outermostClass().classfile;
                if (classfile != null) {
                    urls.add(classfile.toUri().toString());
                }
            }
            if (!urls.isEmpty()) {
                fileManagerVal.handleOption(key, Collections.<String>emptyList().iterator());
            }
        }
    }
}
