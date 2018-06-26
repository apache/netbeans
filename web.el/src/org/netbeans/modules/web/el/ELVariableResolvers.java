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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
