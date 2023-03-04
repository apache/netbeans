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

package org.netbeans.modules.java.hints.declarative;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation.ParameterKind;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.DefaultRuleUtilities;
import org.netbeans.modules.java.hints.declarative.conditionapi.Matcher;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class MethodInvocationContext {

    /*not private for tests*/final List<Class<?>> ruleUtilities;
    
    public MethodInvocationContext() {
        ruleUtilities = new LinkedList<>();
        ruleUtilities.add(DefaultRuleUtilities.class);
    }

    public Method linkMethod(String methodName, Map<? extends String, ? extends ParameterKind> params) {
        Collection<Class<?>> paramTypes = new LinkedList<>();

        for (Entry<? extends String, ? extends ParameterKind> e : params.entrySet()) {
            switch ((ParameterKind) e.getValue()) {
                case VARIABLE:
                    paramTypes.add(Variable.class);
                    break;
                case STRING_LITERAL:
                    paramTypes.add(String.class);
                    break;
                case INT_LITERAL:
                    paramTypes.add(int.class);
                    break;
                case ENUM_CONSTANT:
                    paramTypes.add(loadEnumConstant(e.getKey()).getDeclaringClass());
                    break;
            }
        }

        Method varArgMethod = null;
        
        for (Class<?> clazz : ruleUtilities) {
            OUTER: for (Method m : clazz.getDeclaredMethods()) {
                if (methodName.equals(m.getName())) {
                    Class<?>[] p = m.getParameterTypes();
                    int c = 0;
                    Iterator<Class<?>> it = paramTypes.iterator();

                    for ( ; it.hasNext() && c < p.length; ) {
                        Class<?> paramClass = it.next();
                        Class<?> declaredClass = p[c++];

                        if (declaredClass.equals(paramClass))
                            continue;

                        if (   m.isVarArgs()
                            && declaredClass.isArray()
                            && declaredClass.getComponentType().equals(paramClass)
                            && c == p.length) {
                            while (it.hasNext()) {
                                if (!paramClass.equals(it.next())) {
                                    continue OUTER;
                                }
                            }

                            break;
                        }

                        continue OUTER;
                    }

                    if (!it.hasNext() && c == p.length) {
                        if (!m.isVarArgs()) {
                            return m;
                        }
                        if (varArgMethod == null) {
                            varArgMethod = m;
                        }
                    }
                }
            }
        }

        return varArgMethod;
    }

    public boolean invokeMethod(Context ctx, @NonNull Method method, Map<? extends String, ? extends ParameterKind> params) {
        Collection<Object> paramValues = new LinkedList<>();
        int i = 0;
        Collection<Object> vararg = null;

        for (Entry<? extends String, ? extends ParameterKind> e : params.entrySet()) {
            if (++i == method.getParameterTypes().length && method.isVarArgs()) {
                vararg = new LinkedList<>();
            }
            Object toAdd;
            switch ((ParameterKind) e.getValue()) {
                case VARIABLE:
                    toAdd = new Variable(e.getKey()); //TODO: security/safety
                    break;
                case STRING_LITERAL:
                    toAdd = e.getKey();
                    break;
                case INT_LITERAL:
                    toAdd = Integer.valueOf(e.getKey());
                    break;
                case ENUM_CONSTANT:
                    toAdd = loadEnumConstant(e.getKey());
                    break;
                default:
                    throw new IllegalStateException();
            }

            (vararg != null ? vararg : paramValues).add(toAdd);
        }

        if (method.isVarArgs()) {
            Object[] arr = (Object[]) Array.newInstance(method.getParameterTypes()[method.getParameterTypes().length - 1].getComponentType(), vararg.size());

            vararg.toArray(arr);
            paramValues.add(arr);
        }

        Matcher matcher = new Matcher(ctx);

        Class<?> clazz = method.getDeclaringClass();
        try {
            Constructor<?> c = clazz.getDeclaredConstructor(Context.class, Matcher.class);

            method.setAccessible(true);
            c.setAccessible(true);

            Object instance = c.newInstance(ctx, matcher);

            return (Boolean) method.invoke(instance, paramValues.toArray(new Object[0]));
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static Enum<?> loadEnumConstant(String fqn) {
        int lastDot = fqn.lastIndexOf('.');

        assert lastDot != (-1);

        String className = fqn.substring(0, lastDot);
        String constantName = fqn.substring(lastDot + 1);

        try {
            Class c = (Class) Class.forName(className);

            return Enum.valueOf(c, constantName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final AtomicInteger c = new AtomicInteger();
    void setCode(String imports, Iterable<? extends String> blocks) {
        if (!blocks.iterator().hasNext()) return ;
        String className = "RuleUtilities$" + c.getAndIncrement();
        StringBuilder code = new StringBuilder();

        code.append("package $;\n");

        for (String imp : AUXILIARY_IMPORTS) {
            code.append(imp);
            code.append("\n");
        }

        if (imports != null)
            code.append(imports);

        code.append("class " + className + "{\n");
        code.append("private final Context context;\n");
        code.append("private final Matcher matcher;\n");

        code.append(className + "(Context context, Matcher matcher) {this.context = context; this.matcher = matcher;}");

        for (String b : blocks) {
            code.append(b);
        }

        code.append("}\n");

        try {
            final Map<String, byte[]> classes = Hacks.compile(computeCompileClassPath(), code.toString());

            if (!classes.containsKey("$." + className)) {
                //presumably an error in the custom code, skip
                //TODO: should warn the if happens during an actual hint execution (as opposed to editting the hint in the editor)
                return;
            }
            
            ClassLoader l = new ClassLoader(MethodInvocationContext.class.getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    byte[] bytes = classes.get(name);
                    if (bytes != null) {
                        return defineClass(name, bytes, 0, bytes.length);
                    }
                    
                    return super.findClass(name);
                }
            };

            Class<?> c = l.loadClass("$." + className);

            ruleUtilities.add(c);
        } catch (ClassNotFoundException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static final String[] AUXILIARY_IMPORTS = new String[] {
        "import org.netbeans.modules.java.hints.declarative.conditionapi.Context;",
        "import org.netbeans.modules.java.hints.declarative.conditionapi.Matcher;",
        "import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;"
    };

    static ClassPath computeCompileClassPath() {
        return ClassPathSupport.createClassPath(apiJarURL());
    }

    public static URL apiJarURL() {
        URL jarFile = MethodInvocationContext.class.getProtectionDomain().getCodeSource().getLocation();
        
        return FileUtil.urlForArchiveOrDir(FileUtil.archiveOrDirForURL(jarFile));
    }
}
