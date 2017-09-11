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
package org.netbeans.modules.profiler.nbimpl.javac;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavacPackageInfo extends SourcePackageInfo {
    private static final Logger LOGGER = Logger.getLogger(JavacPackageInfo.class.getName());
    
    private ClasspathInfo cpInfo, indexInfo;
    private Set<ClassIndex.SearchScope> sScope;
    
    public JavacPackageInfo(ClasspathInfo cpInfo, ClasspathInfo indexInfo, String simpleName, String fqn, Scope scope) {
        super(simpleName, fqn, scope);
        this.cpInfo = cpInfo;
        this.indexInfo = indexInfo;
        
        switch (scope) {
            case SOURCE: {
                sScope= EnumSet.of(ClassIndex.SearchScope.SOURCE);
                break;
            }
            case DEPENDENCIES: {
                sScope = EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES);
                break;
            }
            default: {
                sScope = Collections.EMPTY_SET;
            }
        }
        
    }

    @Override
    public Collection<SourceClassInfo> getClasses() {
        final List<SourceClassInfo> clzs = new ArrayList<SourceClassInfo>();
        
        ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController> () {
            @Override
            public void run(CompilationController cc)
                    throws Exception {
                cc.toPhase(JavaSource.Phase.PARSED);

                PackageElement pelem = cc.getElements().getPackageElement(getSimpleName());

                if (pelem != null) {
                    for (TypeElement type : ElementFilter.typesIn(pelem.getEnclosedElements())) {
                        if ((type.getKind() == ElementKind.CLASS) || (type.getKind() == ElementKind.ENUM)) {
                            clzs.add(new JavacClassInfo(ElementHandle.create(type), indexInfo));
                        }
                    }
                } else {
                    LOGGER.log(Level.FINEST, "Package name {0} resulted into a NULL element", getBinaryName()); // NOI18N
                }
            }
        });

        return clzs;
    }

    @Override
    public Collection<SourcePackageInfo> getSubpackages() {
        final ClassIndex index = cpInfo.getClassIndex();
        final List<SourcePackageInfo> pkgs = new ArrayList<SourcePackageInfo>();

        ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>(true) {
            @Override
            public void run(CompilationController cc) {
                for (String pkgName : index.getPackageNames(getBinaryName() + ".", true, sScope)) { // NOI18N
                    pkgs.add(new JavacPackageInfo(cpInfo, indexInfo, pkgName, pkgName, getScope()));
                }
            }
        });

        return pkgs;
    }
}
