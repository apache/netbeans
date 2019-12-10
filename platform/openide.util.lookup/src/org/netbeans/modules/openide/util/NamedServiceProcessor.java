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
package org.netbeans.modules.openide.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.openide.util.lookup.NamedServiceDefinition;
import org.openide.util.lookup.implspi.AbstractServiceProviderProcessor;

public final class NamedServiceProcessor extends AbstractServiceProviderProcessor {
    private static final String PATH = "META-INF/namedservices.index"; // NOI18N
    private static Pattern reference = Pattern.compile("@([^/]+)\\(\\)"); // NOI18N

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> all = new HashSet<String>();
        all.add(NamedServiceDefinition.class.getName());
        searchAnnotations(all, true);
        return all;
    }
    

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(NamedServiceDefinition.class)) {
            NamedServiceDefinition nsd = e.getAnnotation(NamedServiceDefinition.class);
            if (nsd == null) {
                continue;
            }
            Matcher m = reference.matcher(nsd.path());
            while (m.find()) {
                final ExecutableElement attr = findAttribute(e, m.group(1));
                if (attr == null) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, 
                        "The path attribute contains '" + 
                        m.group(0) + 
                        "' reference, but there is no attribute named '" + 
                        m.group(1) + "'", 
                        e
                    );
                    continue;
                }
                final TypeMirror toCheck = attr.getReturnType();
                TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType(); // NOI18N
                if (processingEnv.getTypeUtils().isAssignable(toCheck, stringType)) {
                    continue;
                }
                ArrayType arrStringType = processingEnv.getTypeUtils().getArrayType(stringType);
                if (processingEnv.getTypeUtils().isAssignable(toCheck, arrStringType)) {
                    continue;
                }
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                        "The path attribute contains '" + m.group(0) + 
                        "' reference, but attribute '" + m.group(1) + 
                        "' does not return String or String[]", 
                        e
                );
            }
            if (!nsd.position().equals("-")) {
                ExecutableElement attr = findAttribute(e, nsd.position());
                if (attr == null) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "The position attribute contains '"
                        + nsd.position() + "' but no such attribute found.",
                        e
                    );
                } else {
                    if (!processingEnv.getTypeUtils().isSameType(
                        processingEnv.getTypeUtils().getPrimitiveType(TypeKind.INT),
                        attr.getReturnType()
                    )) {
                        processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "The position attribute contains '"
                            + nsd.position() + "' but the attribute does not return int.",
                            e
                        );
                    }
                }
            }
            Retention ret = e.getAnnotation(Retention.class);
            if (ret == null || ret.value() != RetentionPolicy.SOURCE) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, 
                    "Please specify @Retention(RetentionPolicy.SOURCE) on this annotation",
                    e
                );
            }
            Target tar = e.getAnnotation(Target.class);
            if (tar == null || tar.value().length != 1 || tar.value()[0] != ElementType.TYPE) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, 
                    "Please specify @Target(ElementType.TYPE) on this annotation",
                    e
                );
            }
            register(e, PATH);
        }
        
        Set<String> index = new HashSet<String>();
        searchAnnotations(index, false);
        for (String className : index) {
            Class<? extends Annotation> c;
            try {
                c = Class.forName(className).asSubclass(Annotation.class);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(c)) {
                Annotation a = e.getAnnotation(c);
                if (a == null) {
                    continue;
                }
                NamedServiceDefinition nsd = c.getAnnotation(NamedServiceDefinition.class);
                int cnt = 0;
                for (Class<?> type : nsd.serviceType()) {
                    TypeMirror typeMirror = processingEnv.getTypeUtils().erasure(asType(type));
                    if (processingEnv.getTypeUtils().isSubtype(e.asType(), typeMirror)) {
                        cnt++;
                        for (String p : findPath(nsd.path(), a)) {
                            register(
                                e, c, typeMirror, p,
                                findPosition(nsd.position(), a)
                            );
                        }
                    }
                }
                if (cnt == 0) {
                    StringBuilder sb = new StringBuilder();
                    String prefix = "The type does not ";
                    for (Class<?> type : nsd.serviceType()) {
                        sb.append(prefix);
                        if (type.isInterface()) {
                            sb.append("implement ").append(type.getCanonicalName());
                        } else {
                            sb.append("subclass ").append(type.getCanonicalName());
                        }
                        prefix = ", neither it does ";
                    }
                    sb.append('.');
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, sb.toString(), e);
                }
            }
        }
        return true;
    }

    private TypeMirror asType(Class<?> type) {
        return processingEnv.getElementUtils().getTypeElement(type.getName()).asType();
    }

    private List<String> findPath(String path, Annotation a) {
        List<String> arr = new ArrayList<String>();
        arr.add(path);
        RESTART: for (;;) {
            for (int i = 0; i < arr.size(); i++) {
                Matcher m = reference.matcher(arr.get(i));
                if (m.find()) {
                    String methodName = m.group(1);
                    Object obj;
                    try {
                        obj = a.getClass().getMethod(methodName).invoke(a);
                    } catch (Exception ex) {
                        throw new IllegalStateException(methodName, ex);
                    }
                    if (obj instanceof String) {
                        arr.set(i, substitute(path, m, (String)obj));
                    } else if (obj instanceof String[]) {
                        String[] subs = (String[])obj;
                        arr.set(i, substitute(path, m, subs[0]));
                        for (int j = 1; j < subs.length; j++) {
                            arr.add(substitute(path, m, subs[j]));
                        }
                    } else {
                        throw new IllegalStateException("Wrong return value " + obj); // NOI18N
                    }
                    continue RESTART;
                }
            }
            break RESTART;
        }
        return arr;
    }
    
    private Integer findPosition(String posDefinition, Annotation a) {
        if (posDefinition.length() == 1 && posDefinition.charAt(0) == '-') {
            try {
                return (Integer)a.getClass().getMethod("position").invoke(a);
            } catch (NoSuchMethodException ex) {
                return Integer.MAX_VALUE;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        try {
            return (Integer)a.getClass().getMethod(posDefinition).invoke(a);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static void searchAnnotations(Set<String> found, boolean canonicalName) {

        try {
            Enumeration<URL> en = NamedServiceProcessor.class.getClassLoader().getResources(PATH);
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                InputStream is = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N

                // XXX consider using ServiceLoaderLine instead
                while (true) {
                    String line = reader.readLine();

                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.startsWith("#")) { // NOI18N
                        continue;
                    }
                    if (canonicalName) {
                        line = line.replace('$', '.');
                    }
                    found.add(line);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String substitute(String path, Matcher m, String obj) {
        return path.substring(0, m.start(0)) + obj + path.substring(m.end(0));
    }

    private static ExecutableElement findAttribute(Element e, String attrName) {
        for (Element attr : e.getEnclosedElements()) {
            if (attr.getKind() == ElementKind.METHOD && attr.getSimpleName().contentEquals(attrName)) {
                return (ExecutableElement)attr;
            }
        }
        return null;
    }
}
