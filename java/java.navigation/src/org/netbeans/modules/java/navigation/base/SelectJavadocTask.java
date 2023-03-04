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
