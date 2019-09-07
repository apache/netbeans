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

package org.netbeans.modules.groovy.editor.completion.provider;

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.MetaMethodItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * FIXME: this should somehow use compilation classpath.
 * 
 * @author Petr Hejl
 * @author Martin Janicek
 */
@ServiceProvider(
    service = CompletionProvider.class,
    position = 500
)
public final class MetaElementsProvider implements CompletionProvider {

    
    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        final Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        final Class clz = loadClass(context);
        
        if (clz != null) {
            final MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (MetaMethod method : metaClz.getMetaMethods()) {
                    if (!method.isStatic()) {
                        populateProposal(clz, method, context.getPrefix(), context.getAnchor(), result, context.isNameOnly());
                    }
                }
            }
            GroovySystem.getMetaClassRegistry().removeMetaClass(clz);
        }
        return result;
    }

    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        final Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        final Class clz = loadClass(context);

        if (clz != null) {
            final MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (MetaMethod method : metaClz.getMetaMethods()) {
                    if (method.isStatic()) {
                        populateProposal(clz, method, context.getPrefix(), context.getAnchor(), result, context.isNameOnly());
                    }
                }
            }
            GroovySystem.getMetaClassRegistry().removeMetaClass(clz);
        }
        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        final Map<FieldSignature, CompletionItem> result = new HashMap<FieldSignature, CompletionItem>();
        final Class<?> clazz = loadClass(context);
        
        if (clazz != null) {
            final MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(clazz);

            if (metaClass != null) {
                
                for (Object field : metaClass.getProperties()) {
                    MetaProperty prop = (MetaProperty) field;
                    if (prop.getName().startsWith(context.getPrefix())) {
                        result.put(new FieldSignature(prop.getName()), new CompletionItem.FieldItem(
                                prop.getType().getSimpleName(),
                                prop.getName(),
                                prop.getModifiers(),
                                context.getAnchor()));
                    }
                }
                GroovySystem.getMetaClassRegistry().removeMetaClass(clazz);
            }
        }
        
        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }
    
    private Class loadClass(CompletionContext context) {
        try {
            return context.getSurroundingClass().getCompileUnit().getClassLoader().loadClass(context.getTypeName());
        } catch (ClassNotFoundException cnfe) {
            return loadClass(context.getTypeName());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private Class loadClass(String className) {
        try {
            // FIXME should be loaded by classpath classloader
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoClassDefFoundError err) {
            return null;
        }
    }

    private void populateProposal(Class clz, MetaMethod method, String prefix, int anchor,
            Map<MethodSignature, CompletionItem> methodList, boolean nameOnly) {

        if (method.getName().startsWith(prefix)) {
            addOrReplaceItem(methodList, new CompletionItem.MetaMethodItem(clz, method, anchor, true, nameOnly));
        }
    }

    private void addOrReplaceItem(Map<MethodSignature, CompletionItem> methodItemList, CompletionItem.MetaMethodItem itemToStore) {

        // if we have a method in-store which has the same name and same signature
        // then replace it if we have a method with a higher distance to the super-class.
        // For example: toString() is defined in java.lang.Object and java.lang.String
        // therefore take the one from String.

        MetaMethod methodToStore = itemToStore.getMethod();

        for (CompletionItem methodItem : methodItemList.values()) {
            if (methodItem instanceof MetaMethodItem) {
                MetaMethod currentMethod = ((MetaMethodItem) methodItem).getMethod();

                if (isSameMethod(currentMethod, methodToStore)) {
                    if (isBetterDistance(currentMethod, methodToStore)) {
                        methodItemList.remove(getSignature(currentMethod));
                        methodItemList.put(getSignature(methodToStore), itemToStore);
                    }
                    return;
                }
            }
        }

        // We don't have method with the same signature yet
        methodItemList.put(getSignature(methodToStore), itemToStore);
    }

    private boolean isSameMethod(MetaMethod currentMethod, MetaMethod methodToStore) {
        if (!currentMethod.getName().equals(methodToStore.getName())) {
            return false;
        }
        
        int mask = java.lang.reflect.Modifier.PRIVATE |
                   java.lang.reflect.Modifier.PROTECTED |
                   java.lang.reflect.Modifier.PUBLIC |
                   java.lang.reflect.Modifier.STATIC;
        if ((currentMethod.getModifiers() & mask) != (methodToStore.getModifiers() & mask)) {
            return false;
        }
        
        if (!isSameParams(currentMethod.getParameterTypes(), methodToStore.getParameterTypes())) {
            return false;
        }
        
        return true;
    }

    private boolean isSameParams(CachedClass[] parameters1, CachedClass[] parameters2) {
        if (parameters1.length == parameters2.length) {
            for (int i = 0, size = parameters1.length; i < size; i++) {
                if (parameters1[i] != parameters2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private MethodSignature getSignature(MetaMethod method) {
        String[] parameters = new String[method.getParameterTypes().length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Utilities.translateClassLoaderTypeName(method.getParameterTypes()[i].getName());
        }

        return new MethodSignature(method.getName(), parameters);
    }
    
    private boolean isBetterDistance(MetaMethod currentMethod, MetaMethod methodToStore) {
        String currentClassName = currentMethod.getDeclaringClass().getName();
        String toStoreClassName = methodToStore.getDeclaringClass().getName();

        // In some cases (e.g. #206610) there is the same distance between java.lang.Object and some
        // other interface java.util.Map and in such cases we always want to prefer the interface over
        // the java.lang.Object
        if ("java.lang.Object".equals(currentClassName)) {
            return true;
        }
        if ("java.lang.Object".equals(toStoreClassName)) {
            return false;
        }

        int currentSuperClassDistance = currentMethod.getDeclaringClass().getSuperClassDistance();
        int toStoreSuperClassDistance = methodToStore.getDeclaringClass().getSuperClassDistance();
        if (currentSuperClassDistance < toStoreSuperClassDistance) {
            return true;
        }

        if (currentSuperClassDistance == toStoreSuperClassDistance) {
            // Always prefer Set methods over the Collection methods
            if ("java.util.Collection".equals(currentClassName) && "java.util.Set".equals(toStoreClassName)) {
                return true;
            }
            if ("java.util.Collection".equals(toStoreClassName) && "java.util.Set".equals(currentClassName)) {
                return false;
            }

            // Always prefer List methods over the Collection methods
            if ("java.util.Collection".equals(currentClassName) && "java.util.List".equals(toStoreClassName)) {
                return true;
            }
            if ("java.util.Collection".equals(toStoreClassName) && "java.util.List".equals(currentClassName)) {
                return false;
            }
        }
        
        //preferMethodsAccording to direct inheritance
        
        return currentMethod.getDeclaringClass().isAssignableFrom(methodToStore.getDeclaringClass().getTheClass());

    }
}
