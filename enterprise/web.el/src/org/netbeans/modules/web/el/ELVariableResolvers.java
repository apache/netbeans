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
package org.netbeans.modules.web.el;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ELVariableResolver.FieldInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Convenience methods for dealing with {@link ELVariableResolver}s.
 */
public final class ELVariableResolvers {

    private ELVariableResolvers() {
    }

    /**
     * Gets the FQN class of the bean identified by {@code beanName}.
     * @param beanName
     * @param context
     * @return the FQN of the bean or {@code null}.
     */
    public static String findBeanClass(
            final CompilationContext compilationContext,
            final String beanName,
            final FileObject context) {
        return (String) compilationContext.cache().getOrCache(
                CompilationCache.createKey(beanName, context),
                new CompilationCache.ValueProvider<String>() {

                    @Override
                    public String get() {
                        for (ELVariableResolver resolver : getResolvers()) {
                            FieldInfo beanClass = resolver.getInjectableField(beanName, context, compilationContext.context());
                            if (beanClass != null) {
                                return beanClass.getEnclosingClass();
                            }
                        }
                        return null;

                    }
                });
    }

    /**
     * Gets the name of the bean of the given {@code clazz}.
     * @param clazz the FQN class of the bean.
     * @param context
     * @return the bean name or {@code null}.
     */
    public static String findBeanName(
            final CompilationContext compilationContext,
            final String clazz,
            final FileObject context) {

        return (String) compilationContext.cache().getOrCache(
                CompilationCache.createKey(clazz, context),
                new CompilationCache.ValueProvider<String>() {

                    @Override
                    public String get() {
                        for (ELVariableResolver resolver : getResolvers()) {
                            String beanName = resolver.getBeanName(clazz, context, compilationContext.context());
                            if (beanName != null) {
                                return beanName;
                            }
                        }
                        return null;
                    }
                });
    }

    public static List<ELVariableResolver.VariableInfo> getManagedBeans(
            final CompilationContext compilationContext,
            final FileObject context) {

        return (List<ELVariableResolver.VariableInfo>) compilationContext.cache().getOrCache(
                CompilationCache.createKey(context),
                new CompilationCache.ValueProvider<List<ELVariableResolver.VariableInfo>>() {

                    @Override
                    public List<ELVariableResolver.VariableInfo> get() {
                        List<ELVariableResolver.VariableInfo> result = new ArrayList<>();
                        for (ELVariableResolver resolver : getResolvers()) {
                            result.addAll(resolver.getManagedBeans(context, compilationContext.context()));
                        }
                        return result;
                    }
                });

    }

    public static List<ELVariableResolver.VariableInfo> getVariables(
            final CompilationContext compilationContext,
            final Snapshot snapshot,
            final int offset) {

        return (List<ELVariableResolver.VariableInfo>) compilationContext.cache().getOrCache(
                CompilationCache.createKey(snapshot, offset),
                new CompilationCache.ValueProvider<List<ELVariableResolver.VariableInfo>>() {

                    @Override
                    public List<ELVariableResolver.VariableInfo> get() {
                        List<ELVariableResolver.VariableInfo> result = new ArrayList<>();
                        for (ELVariableResolver resolver : getResolvers()) {
                            result.addAll(resolver.getVariables(snapshot, offset, compilationContext.context()));
                        }
                        return result;
                    }
                });

    }

    public static List<ELVariableResolver.VariableInfo> getBeansInScope(
            final CompilationContext compilationContext,
            final String scope,
            final Snapshot context) {

        return (List<ELVariableResolver.VariableInfo>) compilationContext.cache().getOrCache(
                CompilationCache.createKey(scope, context),
                new CompilationCache.ValueProvider<List<ELVariableResolver.VariableInfo>>() {

                    @Override
                    public List<ELVariableResolver.VariableInfo> get() {
                        List<ELVariableResolver.VariableInfo> result = new ArrayList<>();
                        for (ELVariableResolver resolver : getResolvers()) {
                            result.addAll(resolver.getBeansInScope(scope, context, compilationContext.context()));
                        }
                        return result;
                    }
                });

    }

    public static List<ELVariableResolver.VariableInfo> getRawObjectProperties(
            final CompilationContext compilationContext,
            final String name,
            final Snapshot context) {

        return (List<ELVariableResolver.VariableInfo>) compilationContext.cache().getOrCache(
                CompilationCache.createKey(name, context),
                new CompilationCache.ValueProvider<List<ELVariableResolver.VariableInfo>>() {

                    @Override
                    public List<ELVariableResolver.VariableInfo> get() {
                        List<ELVariableResolver.VariableInfo> result = new ArrayList<>();
                        for (ELVariableResolver resolver : getResolvers()) {
                            result.addAll(resolver.getRawObjectProperties(name, context, compilationContext.context()));
                        }
                        return result;
                    }
                });


    }

    private static Collection<? extends ELVariableResolver> getResolvers() {
        return Lookup.getDefault().lookupAll(ELVariableResolver.class);
    }
}
