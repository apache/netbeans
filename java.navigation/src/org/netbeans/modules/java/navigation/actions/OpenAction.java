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
/*
 * OpenAction.java
 *
 * Created on September 24, 2004, 8:41 PM
 */

package org.netbeans.modules.java.navigation.actions;

import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UsesTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Toolkit;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.QualifiedNameable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.navigation.ElementNode;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * An action that opens editor and jumps to the element given in constructor.
 * Similar to editor's go to declaration action.
 *
 * @author tim, Dafe Simonek
 */
public final class OpenAction extends AbstractAction {

    private final Openable performer;

    private OpenAction(@NonNull final Openable performer) {
        Parameters.notNull("performer", performer); //NOI18N
        this.performer = performer;
        putValue ( Action.NAME, NbBundle.getMessage ( OpenAction.class, "LBL_Goto" ) ); //NOI18N
    }

    @Override
    public void actionPerformed (ActionEvent ev) {
        try {
            performer.open();
        } catch (CannotOpen co) {
            final String msg = co.getLocalizedMessage();
            Toolkit.getDefaultToolkit().beep();
            if(msg != null) {
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        }
    }

    public boolean isEnabled () {
          return true;
    }

    @NonNull
    public static Openable openable(
            @NonNull final ElementHandle<?> handle,
            @NonNull final FileObject fileObject,
            @NonNull final String displayName) {
        return () -> {
            checkFile(fileObject, displayName);
            FileObject file = fileObject;
            if (isClassFile(file)) {
                final FileObject src = findSource(file, handle);
                if (src != null) {
                    file = src;
                }
            }
            if (!ElementOpen.open(file, handle)) {
                noSource(displayName);
            }
        };
    }

    @NonNull
    public static Openable openable(
            @NonNull final TreePathHandle handle,
            @NonNull final FileObject fileObject,
            @NonNull final String displayName) {
        return () -> {
            checkFile(fileObject, displayName);
            if(!ElementOpen.open(fileObject, handle)) {
                noSource(displayName);
            }
        };
    }

    @NonNull
    public static Openable openable(
        @NonNull final ModuleElement module,
        @NonNull final ModuleElement.Directive directive,
        @NonNull final ClasspathInfo cpInfo) {
        final String displayName = module.getQualifiedName().toString();
        final ElementHandle<ModuleElement> moduleHandle = ElementHandle.create(module);
        final Object[] directiveHandle = createDirectiveHandle(directive);
        return () -> {
            final FileObject source = SourceUtils.getFile(moduleHandle, cpInfo);
            if (source == null) {
                noSource(displayName);
            }
            TreePathHandle path = resolveDirectiveHandle(source, directiveHandle);
            if (path == null) {
                noSource(displayName);
            }
            if (!ElementOpen.open(source, path)) {
                noSource(displayName);
            }
        };
    }

    @NonNull
    public static OpenAction create(@NonNull final Openable openable) {
        return new OpenAction(openable);
    }

    private static void checkFile(
            @NullAllowed final FileObject fileObject,
            @NullAllowed final String displayName) {
        if(null == fileObject) {
            noSource(displayName);
        }
    }

    private static void noSource(@NullAllowed final String displayName) {
        throw new CannotOpen(displayName == null ?
                NbBundle.getMessage(OpenAction.class, "MSG_NoSource_Generic"):
                NbBundle.getMessage(OpenAction.class, "MSG_NoSource", displayName));
    }

    private static boolean isClassFile(@NonNull final FileObject file) {
        return "application/x-class-file".equals(file.getMIMEType("application/x-class-file")) || "class".equals(file.getExt());  //NOI18N
    }

    @CheckForNull
    private static FileObject findSource(
            @NonNull final FileObject file,
            @NonNull final ElementHandle<?> elementHandle) {
        FileObject owner = null;
        for (String id : new String[] {
                ClassPath.EXECUTE,
                ClassPath.COMPILE,
                ClassPath.BOOT}) {
            final ClassPath cp = ClassPath.getClassPath(file, id);
            if (cp != null) {
                owner = cp.findOwnerRoot(file);
                if (owner != null) {
                    break;
                }
            }
        }
        return owner == null ?
            owner :
            SourceUtils.getFile(
                elementHandle,
                ClasspathInfo.create(
                    ClassPathSupport.createClassPath(owner),
                    ClassPath.EMPTY,
                    ClassPath.EMPTY));
    }

    @NonNull
    private static Object[] createDirectiveHandle(@NonNull ModuleElement.Directive dir) {
        switch (dir.getKind()) {
            case EXPORTS:
                return new Object[] {dir.getKind(), ((ModuleElement.ExportsDirective)dir).getPackage().getQualifiedName().toString()};
            case OPENS:
                return new Object[] {dir.getKind(), ((ModuleElement.OpensDirective)dir).getPackage().getQualifiedName().toString()};
            case REQUIRES:
                return new Object[] {dir.getKind(), ((ModuleElement.RequiresDirective)dir).getDependency().getQualifiedName().toString()};
            case USES:
                return new Object[] {dir.getKind(), ((ModuleElement.UsesDirective)dir).getService().getQualifiedName().toString()};
            case PROVIDES:
                return new Object[] {dir.getKind(), ((ModuleElement.ProvidesDirective)dir).getService().getQualifiedName().toString()};
            default:
                throw new IllegalArgumentException(String.valueOf(dir));
        }
    }

    private static TreePathHandle resolveDirectiveHandle(
            @NonNull final FileObject file,
            @NonNull final Object[] handle) {
        final JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return null;
        }
        try {
            final TreePathHandle[] res = new TreePathHandle[1];
            js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                @Override
                public void run(final CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    new TreePathScanner<Void, Void>() {
                        @Override
                        public Void visitExports(ExportsTree node, Void p) {
                            if (matches(handle, ModuleElement.DirectiveKind.EXPORTS, node.getPackageName())) {
                                res[0] = TreePathHandle.create(getCurrentPath(), cc);
                            }
                            return super.visitExports(node, p);
                        }

                        @Override
                        public Void visitOpens(OpensTree node, Void p) {
                            if (matches(handle, ModuleElement.DirectiveKind.OPENS, node.getPackageName())) {
                                res[0] = TreePathHandle.create(getCurrentPath(), cc);
                            }
                            return super.visitOpens(node, p);
                        }

                        @Override
                        public Void visitRequires(RequiresTree node, Void p) {
                            if (matches(handle, ModuleElement.DirectiveKind.REQUIRES, node.getModuleName())) {
                                res[0] = TreePathHandle.create(getCurrentPath(), cc);
                            }
                            return super.visitRequires(node, p);
                        }

                        @Override
                        public Void visitUses(UsesTree node, Void p) {
                            if (matches(handle, ModuleElement.DirectiveKind.USES, node.getServiceName())) {
                                res[0] = TreePathHandle.create(getCurrentPath(), cc);
                            }
                            return super.visitUses(node, p);
                        }

                        @Override
                        public Void visitProvides(ProvidesTree node, Void p) {
                            if (matches(handle, ModuleElement.DirectiveKind.PROVIDES, node.getServiceName())) {
                                res[0] = TreePathHandle.create(getCurrentPath(), cc);
                            }
                            return super.visitProvides(node, p);
                        }

                        private boolean matches(
                            final Object[] handle,
                            final ModuleElement.DirectiveKind kind,
                            final Tree selector) {
                            if (handle[0] != kind) {
                                return false;
                            }
                            final TreePath selectorPath = new TreePath(getCurrentPath(), selector);
                            final Element e = cc.getTrees().getElement(selectorPath);
                            return e instanceof QualifiedNameable ?
                                ((QualifiedNameable)e).getQualifiedName().contentEquals((String)handle[1]) :
                                false;
                        }
                    }.scan(cc.getCompilationUnit(), null);
                }
            }, true);
            return res[0];
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }

    private static final class CannotOpen extends RuntimeException {
        CannotOpen(@NullAllowed final String localizedMessage) {
            super(localizedMessage);
        }
    }
}
