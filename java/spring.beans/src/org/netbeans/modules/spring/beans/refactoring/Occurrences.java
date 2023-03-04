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
