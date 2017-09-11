/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.base;

import java.io.IOException;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.java.navigation.JavadocTopComponent;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class SelectJavadocTask implements Runnable, Callable<Boolean>, CancellableTask<CompilationController> {

    private final Lookup.Provider lookupProvider;
    //@NotThreadSafe
    private ElementHandle<?> handle;
    //@NotThreadSafe
    private ElementJavadoc doc;
    private volatile boolean cancelled;

    private SelectJavadocTask(@NonNull final Lookup.Provider lookupProvider) {
        Parameters.notNull("lookupProvider", lookupProvider);   //NOI18N
        this.lookupProvider = lookupProvider;
    }

    @Override
    public void run() {
        cancelled = false;
        if (JavadocTopComponent.shouldUpdate()) {
            final Pair<FileObject,ElementJavadoc> documentation = getJavaDoc();
            if (documentation != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final JavadocTopComponent tc = JavadocTopComponent.findInstance();
                        if (tc != null) {
                            tc.open();
                            tc.setJavadoc(documentation.first(),documentation.second());
                        }
                    }
                });
            }
        }
    }

    @Override
    public void run(CompilationController cc) throws Exception {
        if (cancelled) {
            return;
        }
        cc.toPhase(JavaSource.Phase.UP_TO_DATE);
        if (cancelled) {
            return;
        }
        final Element e = handle.resolve(cc);
        if (e != null && !cancelled) {
            doc = ElementJavadoc.create(cc, e, this);
        }
    }

    @Override
    @NonNull
    public Boolean call() throws Exception {
        return cancelled;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @CheckForNull
    private Pair<FileObject,ElementJavadoc> getJavaDoc() {
        final Node node = lookupProvider.getLookup().lookup(Node.class);
        if (node == null) {
            return null;
        }
        final TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph == null) {
            return null;
        }
        final FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return null;
        }
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return null;
        }
        handle = tph.getElementHandle();
        try {
            js.runUserActionTask(this, true);
        } catch (IOException ioE) {
            Exceptions.printStackTrace(ioE);
            return null;
        }
        return Pair.<FileObject,ElementJavadoc>of(fo, doc);
    }

    public static SelectJavadocTask create(@NonNull final Lookup.Provider lookupProvider) {
        return new SelectJavadocTask(lookupProvider);
    }
}
