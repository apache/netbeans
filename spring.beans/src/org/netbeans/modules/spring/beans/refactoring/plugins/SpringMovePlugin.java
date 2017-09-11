/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
