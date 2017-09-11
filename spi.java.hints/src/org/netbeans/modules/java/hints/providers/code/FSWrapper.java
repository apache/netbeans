/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010-2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.providers.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class FSWrapper {

    public static Iterable<? extends ClassWrapper> listClasses() {
        ClassLoader loader = FSWrapper.class.getClassLoader();

        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        List<ClassWrapper> result = new LinkedList<ClassWrapper>();
        FileObject main = FileUtil.getConfigFile("org-netbeans-modules-java-hints/code-hints/");

        if (main != null) {
            for (FileObject c : main.getChildren()) {
                result.add(new ClassWrapper(loader, c));
            }
        }

        return result;
    }

    public static Method resolveMethod(String className, String methodName) throws NoSuchMethodException, ClassNotFoundException {
        Class<?> clazz = CodeHintProviderImpl.findLoader().loadClass(className);

        return clazz.getDeclaredMethod(methodName, HintContext.class);
    }

    public static class AnnotatableWrapper {
        protected final ClassLoader loader;
        protected final FileObject folder;
        protected AnnotatableWrapper(ClassLoader loader, FileObject folder) {
            this.loader = loader;
            this.folder = folder;
        }

        private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();

        public synchronized <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            if (!this.annotations.containsKey(annotationClass)) {
                FileObject f = folder.getFileObject(annotationClass.getName().replace('.', '-') + ".annotation");
                T result = null;

                if (f != null) {
                    try {
                        Annotation a = loadAnnotation(loader, f);

                        result = annotationClass.cast(a);
                    } catch (ClassNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                this.annotations.put(annotationClass, result);
            }

            return annotationClass.cast(this.annotations.get(annotationClass));
        }
    }

    public static class ClassWrapper extends AnnotatableWrapper {
        private final String className;
        public ClassWrapper(ClassLoader loader, FileObject folder) {
            super(loader, folder);
            className = folder.getName().replace('-', '.');
        }

        private Iterable<? extends MethodWrapper> methods;

        public synchronized Iterable<? extends MethodWrapper> getMethods() {
            if (this.methods == null) {
                List<MethodWrapper> methods = new LinkedList<MethodWrapper>();

                for (FileObject c : folder.getChildren()) {
                    if (c.getExt().equals("method")) {
                        methods.add(new MethodWrapper(loader, c, this));
                    }
                }

                this.methods = methods;
            }

            return this.methods;
        }

        private Iterable<? extends FieldWrapper> fields;

        public synchronized Iterable<? extends FieldWrapper> getFields() {
            if (this.fields == null) {
                List<FieldWrapper> fields = new LinkedList<FieldWrapper>();

                for (FileObject c : folder.getChildren()) {
                    if (c.getExt().equals("field")) {
                        fields.add(new FieldWrapper(loader, c, this));
                    }
                }

                this.fields = fields;
            }

            return this.fields;
        }

        public String getName() {
            return className;
        }

        private Class<?> clazz;
        public synchronized Class<?> getDeclaredClass() {
            if (clazz != null) {
                return clazz;
            }

            try {
                return this.clazz = loader.loadClass(className);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null; //XXX
        }
    }

    public static class MethodWrapper extends AnnotatableWrapper {
        private final ClassWrapper clazz;
        public MethodWrapper(ClassLoader loader, FileObject folder, ClassWrapper clazz) {
            super(loader, folder);
            this.clazz = clazz;
        }

        ClassWrapper getClazz() {
            return clazz;
        }
        
        String getName() {
            return folder.getName();
        }
    }

    public static class FieldWrapper extends AnnotatableWrapper {
        private final ClassWrapper clazz;
        public FieldWrapper(ClassLoader loader, FileObject folder, ClassWrapper clazz) {
            super(loader, folder);
            this.clazz = clazz;
        }

        ClassWrapper getClazz() {
            return clazz;
        }

        String getName() {
            return folder.getName();
        }

        String getConstantValue() {
            Object constantValue = folder.getAttribute("constantValue");

            if (constantValue instanceof String) {
                return (String) constantValue;
            }

            return null;
        }
    }

    private static final Object MARKER = new Object();
    
    private static Object computeAttributeValue(ClassLoader loader, FileObject folder, String attributeName, Class<?> returnType, Object defaulValue) throws ClassNotFoundException {
        Object result = folder.getAttribute(attributeName);

        if (result == null) {
            FileObject embedded = folder.getFileObject(attributeName);

            if (embedded == null) {
                result = defaulValue;
            } else {
                if (returnType.isArray()) {
                    List<Object> items = new LinkedList<Object>();
                    int c = 0;

                    while (true) {
                        Object val = computeAttributeValue(loader, embedded, "item" + c, returnType.getComponentType(), MARKER);

                        if (val == MARKER) {
                            break;
                        }

                        items.add(val);
                        c++;
                    }

                    Object res = Array.newInstance(returnType.getComponentType(), items.size());
                    int ci = 0;

                    for (Object i : items) {
                        Array.set(res, ci++, i);
                    }

                    result = res;
                } else if (returnType.isAnnotation()) {
                    result = loadAnnotation(loader, embedded.getChildren()[0]);
                }
            }
        } else {
            if (returnType.isEnum()) {
                String fqn = (String) result;
                int lastDot = fqn.lastIndexOf('.');
                Class<? extends Enum> enumClass = (Class<? extends Enum>) loader.loadClass(fqn.substring(0, lastDot));

                result = Enum.valueOf(enumClass, fqn.substring(lastDot + 1));
            } else if (returnType == Class.class) {
                String fqn = (String) result;

                try {
                    result = loader.loadClass(fqn);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(FSWrapper.class.getName()).log(Level.FINE, null, ex);
                    result = CodeHintProviderImpl.findLoader().loadClass(fqn);
                }
            }
        }

        return result;
    }

    
    private static <T extends Annotation> T loadAnnotation(ClassLoader l, FileObject annotationFolder) throws ClassNotFoundException {
        Class<?> clazz = l.loadClass(annotationFolder.getName().replace('-', '.'));

        return (T) Proxy.newProxyInstance(l, new Class[] {clazz}, new InvocationHandlerImpl(l, annotationFolder));
    }
    
    private static final class InvocationHandlerImpl implements InvocationHandler {

        private final ClassLoader loader;
        private final FileObject folder;
        private final Map<String, Object> attributes = new HashMap<String, Object>();

        public InvocationHandlerImpl(ClassLoader loader, FileObject folder) {
            this.loader = loader;
            this.folder = folder;
        }

        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!attributes.containsKey(method.getName())) {
                Object result = computeAttributeValue(loader, folder, method.getName(), method.getReturnType(), method.getDefaultValue());

                attributes.put(method.getName(), result);
            }

            return attributes.get(method.getName());
        }

    }

}
