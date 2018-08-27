/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.groovy.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;

/** Utility class for opening elements in editor.
 *  XXX Basic code copied from org.netbeans.api.java.source.ui.ElementOpen
 *
 * @author Jan Lahoda
 */
public final class ElementDeclaration {
    private static Logger log = Logger.getLogger(ElementDeclaration.class.getName());

    private ElementDeclaration() {
        super();
    }

    public static DeclarationLocation getDeclarationLocation(final ClasspathInfo cpInfo, final Element el) {
        ElementHandle<Element> handle = ElementHandle.create(el);
        FileObject fo = SourceUtils.getFile(handle, cpInfo);
        if (fo != null) {
            return getDeclarationLocation(fo, handle);
        }
        return DeclarationLocation.NONE;
    }

    private static DeclarationLocation getDeclarationLocation(final FileObject fo,
            final ElementHandle<? extends Element> handle) {

        assert fo != null;

        try {
            int offset = getOffset(fo, handle);
            return new DeclarationLocation(fo, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return DeclarationLocation.NONE;
        }
    }

    private static int getOffset(FileObject fo, final ElementHandle<? extends Element> handle) throws IOException {
        if (IndexingManager.getDefault().isIndexing()) {
            log.info("Skipping location of element offset within file, Scannig in progress");
            return 0; //we are opening @ 0 position. Fix #160478
        }

        final int[]  result = new int[] {-1};

        JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController info) {
                    try {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    Element el = handle.resolve(info);
                    if (el == null) {
                        log.severe("Cannot resolve " + handle + ". " + info.getClasspathInfo());
                        return;
                    }

                    FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);

                    CompilationUnitTree cu = info.getCompilationUnit();

                    v.scan(cu, null);
                    Tree elTree = v.declTree;

                    if (elTree != null)
                        result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
                }
            },true);
        }
        return result[0];
    }

    // Private innerclasses ----------------------------------------------------

    private static class FindDeclarationVisitor extends TreePathScanner<Void, Void> {

        private Element element;
        private Tree declTree;
        private CompilationInfo info;

        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }

	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }

	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }

	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }

        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());

            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }

    }

}
