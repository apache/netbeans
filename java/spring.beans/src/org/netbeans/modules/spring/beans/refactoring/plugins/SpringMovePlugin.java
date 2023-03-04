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

package org.netbeans.modules.spring.beans.refactoring.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.refactoring.*;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
class SpringMovePlugin implements RefactoringPlugin {

    private final MoveRefactoring refactoring;

    public SpringMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        try {
            URL target = refactoring.getTarget().lookup(URL.class);
            if (target == null) {
                return null;
            }
            String targetPackageName = SpringRefactorings.getPackageName(target);
            if (targetPackageName == null) {
                return null;
            }
            Map<SpringScope, List<String>> scope2PackageNames = new HashMap<SpringScope, List<String>>();
            Map<SpringScope, List<String>> scope2ClassNames = new HashMap<SpringScope, List<String>>();
            for (FileObject fo : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                if (fo.isFolder()) {
                    String oldPackageName = SpringRefactorings.getPackageName(fo);
                    if (oldPackageName == null) {
                        continue;
                    }
                    SpringScope scope = SpringScope.getSpringScope(fo);
                    if (scope == null) {
                        continue;
                    }
                    List<String> packageNames = scope2PackageNames.get(scope);
                    if (packageNames == null) {
                        packageNames = new ArrayList<String>();
                        scope2PackageNames.put(scope, packageNames);
                    }
                    packageNames.add(oldPackageName);
                } else if (SpringRefactorings.isJavaFile(fo)) {
                    SpringScope scope = SpringScope.getSpringScope(fo);
                    if (scope == null) {
                        continue;
                    }
                    List<String> classNames = scope2ClassNames.get(scope);
                    if (classNames == null) {
                        classNames = new ArrayList<String>();
                        scope2ClassNames.put(scope, classNames);
                    }
                    classNames.addAll(SpringRefactorings.getTopLevelClassNames(fo));
                }
            }
            Modifications mods = new Modifications();
            // Packages.
            for (Map.Entry<SpringScope, List<String>> entry : scope2PackageNames.entrySet()) {
                SpringScope scope = entry.getKey();
                for (String packageName : entry.getValue()) {
                    String simplePackageName = SpringRefactorings.getSimpleElementName(packageName);
                    String newPackageName = SpringRefactorings.createQualifiedName(packageName, simplePackageName);
                    if (newPackageName != null) {
                        for (Occurrence occurrence : Occurrences.getJavaPackageOccurrences(packageName, true, scope)) {
                            refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, null, newPackageName));
                        }
                    }
                }
            }
            // Java files.
            for (Map.Entry<SpringScope, List<String>> entry :scope2ClassNames.entrySet()) {
                SpringScope scope = entry.getKey();
                for (String className : entry.getValue()) {
                    String simpleClassName = SpringRefactorings.getSimpleElementName(className);
                    String newClassName = SpringRefactorings.createQualifiedName(targetPackageName, simpleClassName);
                    if (newClassName != null) {
                        for (Occurrence occurrence : Occurrences.getJavaClassOccurrences(className, scope)) {
                            refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, null, newClassName));
                        }
                    }
                }
            }
            refactoringElements.registerTransaction(new RefactoringCommit(Collections.singleton(mods)));
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
}
