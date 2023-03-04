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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.*;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo.Scope;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.JavacPackageInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ParsingUtils;
import org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseProfilerTypeUtilsImpl extends ProfilerTypeUtilsProvider {
    private static final Logger LOG = Logger.getLogger(BaseProfilerTypeUtilsImpl.class.getName());
    @Override
    public final Collection<SourcePackageInfo> getPackages(boolean subprojects, final Scope scope) {
        final Collection<SourcePackageInfo> pkgs = new ArrayList<SourcePackageInfo>();

        final ClasspathInfo cpInfo = getClasspathInfo(subprojects, scope == SourcePackageInfo.Scope.SOURCE, scope == SourcePackageInfo.Scope.DEPENDENCIES);
        final ClasspathInfo indexInfo = getClasspathInfo(subprojects, true, true);
        
        // #170201: A misconfigured(?) project can have no source roots defined, returning NULL as its ClasspathInfo
        // ignore such a project
        if (cpInfo != null) {
            ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>(true) {
                @Override
                public void run(CompilationController cc) {
                    for (String pkgName : cpInfo.getClassIndex().getPackageNames("", false, toSearchScope(Collections.singleton(scope)))) { // NOI18N
                        pkgs.add(new JavacPackageInfo(cpInfo, indexInfo, pkgName, pkgName, scope));
                    }
                }
            });
        }        
        return pkgs;
    }

    @Override
    public final SourceClassInfo resolveClass(final String className) {
        final ClasspathInfo cpInfo = getClasspathInfo();
        if (cpInfo != null) {
            ElementHandle<TypeElement> eh = ElementUtilitiesEx.resolveClassByName(className, cpInfo, false);
            return eh != null ? new JavacClassInfo(eh, cpInfo) : null;
        }
        return null;
    }

    @Override
    public Collection<SourceClassInfo> findClasses(final String pattern, final Set<Scope> scope) {
        final Collection<SourceClassInfo> clzs = new ArrayList<SourceClassInfo>();
        final ClasspathInfo cpInfo = getClasspathInfo();
        if (cpInfo != null) {
            ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>(true) {
                @Override
                public void run(CompilationController cc) {
                    for(ElementHandle<TypeElement> eh : cpInfo.getClassIndex().getDeclaredTypes(pattern, NameKind.CASE_INSENSITIVE_REGEXP, toSearchScope(scope))) {
                        clzs.add(new JavacClassInfo(eh, cpInfo));
                    }
                }
            });
        }
        
        return clzs;
    }
    
    protected abstract ClasspathInfo getClasspathInfo();
    protected abstract ClasspathInfo getClasspathInfo(boolean subprojects, boolean source, boolean deps);
    
    private Set<ClassIndex.SearchScope> toSearchScope(Set<Scope> scope) {
        Set<ClassIndex.SearchScope> sScope = EnumSet.noneOf(ClassIndex.SearchScope.class);
        for(Scope s : scope) {
            switch (s) {
                case DEPENDENCIES: {
                    sScope.add(ClassIndex.SearchScope.DEPENDENCIES);
                    break;
                }
                case SOURCE: {
                    sScope.add(ClassIndex.SearchScope.SOURCE);
                    break;
                }
            }
        }
        return sScope;
    }
}
