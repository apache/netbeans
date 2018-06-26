/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        final Class clz = loadClass(context.getTypeName());
        
        if (clz != null) {
            final MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (MetaMethod method : metaClz.getMetaMethods()) {
                    populateProposal(clz, method, context.getPrefix(), context.getAnchor(), result, context.isNameOnly());
                }
            }
        }
        return result;
    }

    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        final Map<FieldSignature, CompletionItem> result = new HashMap<FieldSignature, CompletionItem>();
        final Class clazz = loadClass(context.getTypeName());
        
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
            }
        }
        
        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
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
        return false;
    }
}
