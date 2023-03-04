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

package org.netbeans.modules.gradle.java.api.output;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class LocationOpener {

    public static final Location.Finder GLOBAL_FINDER = (Location loc) -> GlobalPathRegistry.getDefault().findResource(loc.getFileName());
    final Location location;
    final Location.Finder finder;

    public LocationOpener(Location location, Location.Finder finder) {
        this.location = location;
        this.finder = finder;
    }

    public LocationOpener(Location location) {
        this(location, GLOBAL_FINDER);
    }

    public final void open() {
        FileObject fo = finder.findFileObject(location);
        if (fo != null) {
            if (location.isLine()) {
                openAtLine(fo, location.getLineNum());
            } else if (location.isMethod()) {
                int l = getMethodLine(fo, location.getClassNames(), location.getTarget());
                openAtLine(fo, l);
            } else {
                int l = getTargetLine(fo, location.getClassNames());
                openAtLine(fo, l);
            }
        }
    }

   private static String stripMethodParams(String methodNameWithParams) {
        final int paramsIndex = methodNameWithParams.indexOf('(');
        String cleanName = methodNameWithParams;
        if (paramsIndex > 0) {
            cleanName = methodNameWithParams.substring(0, paramsIndex);
        }
        return cleanName;
    }

    private int getMethodLine(final FileObject fo, final String[] classNames, final String methodNameWithParams) {
        String methodName = stripMethodParams(methodNameWithParams);
        final int[] line = new int[1];
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource != null) {
            try {
                javaSource.runUserActionTask((CompilationController compilationController) -> {
                  compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Trees trees = compilationController.getTrees();
                    CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                    List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                    for (Tree tree : typeDecls) {
                        Element element = getClassElement(
                                trees.getElement(trees.getPath(compilationUnitTree, tree)),
                                classNames,0);
                        if (element != null) {
                            List<? extends ExecutableElement> methodElements = ElementFilter.methodsIn(element.getEnclosedElements());
                            System.out.println("Looking for methodName " + methodName);
                            for (Element child : methodElements) {
                                if (child.getSimpleName().contentEquals(methodName)) {
                                    long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(child));
                                    System.out.println("Found method. LINE: " + pos);
                                    line[0] = (int) compilationUnitTree.getLineMap().getLineNumber(pos);
                                    break;
                                }
                            }
                        }
                    }
                }, true);
                return line[0];
            } catch (IOException ioe) {
                //TODO: Do nothing?
            }
        }
        return 1;
    }


    private Element getClassElement(Element element, String[] classNames, int startIndex) {
        if (element != null && element.getKind() == ElementKind.CLASS &&
                element.getSimpleName().contentEquals(classNames[startIndex])) {
            if (startIndex == classNames.length - 1) {
                return element;
            }
            for (Element enclosedElement : element.getEnclosedElements()) {
                Element matchingElement = getClassElement(enclosedElement, classNames, startIndex + 1);
                if (matchingElement != null) {
                    return matchingElement;
                }
            }
        }
        return null;
    }

    private int getTargetLine(final FileObject fo, String[] classNames) {
        final int[] line = new int[]{0};
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource != null) {
            try {
                javaSource.runUserActionTask((CompilationController compilationController) -> {
                    compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Trees trees = compilationController.getTrees();
                    CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                    List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                    for (Tree tree : typeDecls) {
                        Element element = getClassElement(
                                trees.getElement(trees.getPath(compilationUnitTree, tree)),
                                classNames,0);
                        if (element != null) {
                            long pos = trees.getSourcePositions().getStartPosition(
                                    compilationUnitTree,
                                    trees.getTree(element));
                            line[0] = (int) compilationUnitTree.getLineMap().getLineNumber(pos);
                            break;
                        }
                    }
                }, true);
                return line[0];
            } catch (IOException ioe) {
            }
        }
        return 1;
    }

    public static void openAtLine(FileObject file, final int line) {
        openAtLine(file, line, false);
    }

    public static void openAtLine(FileObject file, final int line, final boolean reuse) {
        try {
            DataObject data = DataObject.find(file);
            if (data != null) {
                final LineCookie lines = data.getLookup().lookup(LineCookie.class);
                if (lines != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                lines.getLineSet().getOriginal(line - 1).show(
                                        reuse ? Line.ShowOpenType.REUSE : Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, 0);
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }
                    });

                }

            }
        } catch (DataObjectNotFoundException ex) {
        }
    }

}
