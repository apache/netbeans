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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class ParameterNameProviderImpl {
    public static void register(JavacTask task, ClasspathInfo cpInfo) {
        try {
            Class<?> c = Class.forName("com.sun.source.util.ParameterNameProvider");
            ParameterNameProviderImpl impl = new ParameterNameProviderImpl(cpInfo);
            InvocationHandler h = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("getParameterName")) {
                        return impl.getParameterName((VariableElement) args[0]);
                    }
                    return null;
                }
            };
            Object proxy = Proxy.newProxyInstance(ParameterNameProviderImpl.class.getClassLoader(), new Class[] {c}, h);
            JavacTask.class.getDeclaredMethod("setParameterNameProvider", c).invoke(task, proxy);
        } catch (ClassNotFoundException ex) {
            //ok
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final int MAX_CACHE_SIZE = 100;
    private static final LinkedHashMap<String, Map<String, List<String>>> class2method2Parameters = new LinkedHashMap<>();

    private final ClasspathInfo cpInfo;

    public ParameterNameProviderImpl(ClasspathInfo cpInfo) {
        this.cpInfo = cpInfo;
    }

    public CharSequence getParameterName(VariableElement parameter) {
        Element topLevel = parameter;
        while (topLevel.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            topLevel = topLevel.getEnclosingElement();
        }
        ElementHandle<?> topLevelHandle = ElementHandle.create(topLevel);
        Element method = parameter.getEnclosingElement();
        List<String> names = class2method2Parameters.computeIfAbsent(computeKey(topLevel), d -> {
            Map<String, List<String>> parametersInClass = new HashMap<>();
            FileObject source = SourceUtils.getFile(topLevelHandle, cpInfo);
            JavaSource javaSource = source != null ? JavaSource.forFileObject(source) : null;
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        new TreePathScanner<Void, Void>() {
                            public Void visitMethod(MethodTree mt, Void v) {
                                Element el = cc.getTrees().getElement(getCurrentPath());
                                if (el != null && el.getKind() == ElementKind.METHOD) {
                                    parametersInClass.put(computeKey(el), ((ExecutableElement) el).getParameters().stream().map(p -> p.getSimpleName().toString()).collect(Collectors.toList()));
                                }
                                return super.visitMethod(mt, v);
                            }
                        }.scan(cc.getCompilationUnit(), null);
                    }, true);
                } catch (IOException ex) {
                    //ignore
                }
            }
            return parametersInClass;
        }).getOrDefault(computeKey(method), Collections.emptyList());
        int idx = ((ExecutableElement) method).getParameters().indexOf(parameter);
        return idx != (-1) && idx < names.size() ? names.get(idx) : null;
    }
    
    private String computeKey(Element el) {
        return Arrays.stream(SourceUtils.getJVMSignature(ElementHandle.create(el))).collect(Collectors.joining(":"));
    }
}
