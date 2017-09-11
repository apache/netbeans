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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.refactoring.JavaElementRefFinder.Matcher;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedProperty;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class Occurrences {

    public static List<Occurrence> getPropertyOccurrences(final RenamedProperty renamedProperty, JavaSource js, final SpringScope scope) throws IOException {
        final List<Occurrence> result = new ArrayList<Occurrence>();
        final Set<File> processed = new HashSet<File>();

        js.runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(final CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (SpringConfigModel model : scope.getAllConfigModels()) {
                    model.runDocumentAction(new Action<DocumentAccess>() {
                        public void run(DocumentAccess docAccess) {
                            File file = docAccess.getFile();
                            if (processed.contains(file)) {
                                return;
                            }
                            processed.add(file);
                            try {
                                new PropertyRefFinder(docAccess, cc, renamedProperty).addOccurrences(result);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        
                    });
                }
            }
        }, true);
        
        return result;
    }
    
    public static List<Occurrence> getJavaClassOccurrences(final String className, SpringScope scope) throws IOException {
        final List<Occurrence> result = new ArrayList<Occurrence>();
        final Set<File> processed = new HashSet<File>();
        for (SpringConfigModel model : scope.getAllConfigModels()) {
            model.runDocumentAction(new Action<DocumentAccess>() {
                public void run(DocumentAccess docAccess) {
                    File file = docAccess.getFile();
                    if (processed.contains(file)) {
                        return;
                    }
                    processed.add(file);
                    try {
                        new JavaElementRefFinder(docAccess).addOccurrences(new JavaClassRefMatcher(className), result);
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
        return result;
    }

    public static List<Occurrence> getJavaPackageOccurrences(final String packageName, final boolean subpackages, SpringScope scope) throws IOException {
        final List<Occurrence> result = new ArrayList<Occurrence>();
        final Set<File> processed = new HashSet<File>();
        for (SpringConfigModel model : scope.getAllConfigModels()) {
            model.runDocumentAction(new Action<DocumentAccess>() {
                public void run(DocumentAccess docAccess) {
                    File file = docAccess.getFile();
                    if (processed.contains(file)) {
                        return;
                    }
                    processed.add(file);
                    try {
                        new JavaElementRefFinder(docAccess).addOccurrences(new JavaPackageRefMatcher(packageName, subpackages), result);
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
        return result;
    }

    static final class JavaClassRefMatcher implements Matcher {

        private final String className;

        public JavaClassRefMatcher(String className) {
            this.className = className;
        }

        public String accept(String beanClassName) {
            if (!beanClassName.startsWith(className)) {
                return null;
            }
            if (beanClassName.length() == className.length()) {
                // Exact match.
                return className;
            } else {
                // Then beanClassName.length() > className.length(),
                // so the bean class must be a nested class of the searched class.
                if (beanClassName.charAt(className.length()) == '$') {
                    return className;
                }
            }
            return null;
        }
    }

    static final class JavaPackageRefMatcher implements Matcher {

        private final String packageName;
        private final boolean subpackages;

        public JavaPackageRefMatcher(String packageName, boolean subpackages) {
            this.packageName = packageName;
            this.subpackages = subpackages;
        }

        public String accept(String beanClassName) {
            if (!beanClassName.startsWith(packageName) || beanClassName.length() == packageName.length()) {
                return null;
            }
            if (subpackages) {
                return packageName;
            } else {
                // Not recursive, so beanClassName should be a class in packageName.
                int afterDot = packageName.length() + 1;
                if (afterDot < beanClassName.length() && beanClassName.indexOf('.', afterDot) == -1) {
                    return packageName;
                }
            }
            return null;
        }
    }

    public abstract static class Occurrence {

        private final FileObject fo;
        private final PositionBounds position;

        Occurrence(FileObject fo, PositionBounds position) {
            this.fo = fo;
            this.position = position;
        }

        public FileObject getFileObject() {
            return fo;
        }

        public PositionBounds getPosition() {
            return position;
        }

        public abstract String getDisplayText();
    }
}
