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
