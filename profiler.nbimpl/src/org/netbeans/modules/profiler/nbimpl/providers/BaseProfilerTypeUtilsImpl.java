/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
abstract public class BaseProfilerTypeUtilsImpl extends ProfilerTypeUtilsProvider {
    private static final Logger LOG = Logger.getLogger(BaseProfilerTypeUtilsImpl.class.getName());
    @Override
    final public Collection<SourcePackageInfo> getPackages(boolean subprojects, final Scope scope) {
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
    final public SourceClassInfo resolveClass(final String className) {
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
    
    abstract protected ClasspathInfo getClasspathInfo();
    abstract protected ClasspathInfo getClasspathInfo(boolean subprojects, boolean source, boolean deps);
    
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
