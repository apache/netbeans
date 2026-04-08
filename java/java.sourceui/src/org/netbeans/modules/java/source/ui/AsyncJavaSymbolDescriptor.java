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

package org.netbeans.modules.java.source.ui;

import com.sun.tools.javac.api.JavacTaskImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ElementUtils;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.spi.jumpto.support.AsyncDescriptor;
import org.netbeans.spi.jumpto.support.DescriptorChangeEvent;
import org.netbeans.spi.jumpto.support.DescriptorChangeListener;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class AsyncJavaSymbolDescriptor extends JavaSymbolDescriptorBase implements AsyncDescriptor<SymbolDescriptor> {

    private static final RequestProcessor WORKER = new RequestProcessor(AsyncJavaSymbolDescriptor.class);
    private static final String INIT = "<init>"; //NOI18N

    private final String ident;
    private final boolean caseSensitive;
    private final List<DescriptorChangeListener<SymbolDescriptor>> listeners;
    private final AtomicBoolean initialized;

    AsyncJavaSymbolDescriptor (
            @NullAllowed final ProjectInformation projectInformation,
            @NonNull final FileObject root,
            @NonNull final ClassIndexImpl ci,
            @NonNull final ElementHandle<TypeElement> owner,
            @NonNull final String ident,
            final boolean caseSensitive) {
        super(owner, projectInformation, root, ci);
        assert ident != null;
        this.ident = ident;
        this.listeners = new CopyOnWriteArrayList<>();
        this.initialized = new AtomicBoolean();
        this.caseSensitive = caseSensitive;
    }

    @Override
    public Icon getIcon() {
        initialize();
        return null;
    }

    @Override
    public String getSymbolName() {
        initialize();
        return ident;
    }

    @Override
    public String getSimpleName() {
        return ident;
    }

    @Override
    public void open() {
        final Collection<? extends SymbolDescriptor> symbols = resolve();
        /* resolve() falls back to Collections.singleton(this) if it could
           not enrich the descriptor (e.g. javac threw CompletionFailure
           for some transient reason). Detect that case explicitly to
           avoid recursing into our own open(); fall back to opening the
           type by its ElementHandle via ElementOpen, which goes through
           ClassIndex/SourceUtils. */
        if (symbols.size() == 1 && symbols.iterator().next() == this) {
            final FileObject file = getFileObject();
            if (file != null) {
                ElementOpen.open(ClasspathInfo.create(file), getOwner());
            }
            return;
        }
        if (!symbols.isEmpty()) {
            symbols.iterator().next().open();
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = hashCode * 31 + ident.hashCode();
        hashCode = hashCode * 31 + getRoot().hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AsyncJavaSymbolDescriptor)) {
            return false;
        }
        final AsyncJavaSymbolDescriptor other = (AsyncJavaSymbolDescriptor) obj;
        return caseSensitive == other.caseSensitive &&
               ident.equals(other.ident) &&
               getOwner().equals(other.getOwner()) &&
               getRoot().equals(other.getRoot());
    }

    private void initialize() {
        if (initialized.compareAndSet(false, true)) {
            final Runnable action = () -> {
                final Collection<? extends SymbolDescriptor> symbols = resolve();
                fireDescriptorChange(symbols);
            };
            WORKER.execute(action);
        }
    }

    @Override
    public void addDescriptorChangeListener(@NonNull final DescriptorChangeListener<SymbolDescriptor> listener) {
        Parameters.notNull("listener", listener);
        listeners.add(listener);
    }

    @Override
    public void removeDescriptorChangeListener(@NonNull final DescriptorChangeListener<SymbolDescriptor> listener) {
        Parameters.notNull("listener", listener);
        listeners.remove(listener);
    }

    @Override
    public boolean hasCorrectCase() {
        return caseSensitive;
    }

    private void fireDescriptorChange(Collection<? extends SymbolDescriptor> replacement) {
        final DescriptorChangeEvent<SymbolDescriptor> event = new DescriptorChangeEvent<>(
            this,
            replacement);
        for (DescriptorChangeListener<SymbolDescriptor> l : listeners) {
            l.descriptorChanged(event);
        }
    }

    @NonNull
    private Collection<? extends SymbolDescriptor> resolve() {
        final List<SymbolDescriptor> symbols = new ArrayList<>();
        try {
            final String binName = ElementHandleAccessor.getInstance().getJVMSignature(getOwner())[0];
            /* Build the JavacTask via JavacParser.createJavacTask
               (which pre-registers NetBeans' javac context enhancers --
               most importantly NBClassReader, plus NBAttr, NBEnter,
               NBMemberEnter, NBResolve, NBClassFinder, NBJavaCompiler,
               NBClassWriter, etc.) instead of calling
               JavacTool.create().getTask(...) directly. NetBeans .sig
               files (the per-root cached form of indexed Java classes)
               are written by NBClassWriter and use a relaxed/extended
               class-file format that only NBClassReader knows how to
               read; stock javac's ClassReader rejects them with
               BadClassFile, which propagates as CompletionFailure.
               ElementUtils.getTypeElementByBinaryName silently catches
               the CompletionFailure and returns null, and resolve()
               therefore returned an empty collection for every Java
               type whose .sig had any NB-specific encoding -- the
               actual cause of the long-standing Go to Symbol
               disappearance bug. */
            final ClasspathInfo cpInfo = ClasspathInfo.create(getRoot());
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                    cpInfo,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Collections.<JavaFileObject>emptyList());
            //Force JTImpl.prepareCompiler to get JTImpl into Context
            jt.enter();
            final TypeElement te = ElementUtils.getTypeElementByBinaryName(jt, binName);
            if (te != null) {
                if (ident.equals(getSimpleName(te, null, caseSensitive))) {
                    final String simpleName = te.getSimpleName().toString();
                    final String simpleNameSuffix = null;
                    final ElementKind kind = te.getKind();
                    final Set<Modifier> modifiers = te.getModifiers();
                    final ElementHandle<?> me = ElementHandle.create(te);
                    symbols.add(new ResolvedJavaSymbolDescriptor(
                            AsyncJavaSymbolDescriptor.this,
                            simpleName,
                            simpleNameSuffix,
                            te.getQualifiedName().toString(),
                            kind,
                            modifiers,
                            me));
                }
                for (Element ne : te.getEnclosedElements()) {
                    if (ident.equals(getSimpleName(ne, te, caseSensitive))) {
                        final Pair<String,String> name = JavaSymbolProvider.getDisplayName(ne, te);
                        final String simpleName = name.first();
                        final String simpleNameSuffix = name.second();
                        final ElementKind kind = ne.getKind();
                        final Set<Modifier> modifiers = ne.getModifiers();
                        final ElementHandle<?> me = ElementHandle.create(ne);
                        symbols.add(new ResolvedJavaSymbolDescriptor(
                                AsyncJavaSymbolDescriptor.this,
                                simpleName,
                                simpleNameSuffix,
                                te.getQualifiedName().toString(),
                                kind,
                                modifiers,
                                me));
                    }
                }
            }
        } catch (RuntimeException e) {
            /* Swallow so that unexpected javac failures (e.g.
               CompletionFailure, which is a RuntimeException) fall through
               to the "no enriched symbols" path below rather than
               propagating into the WORKER thread and leaving the descriptor
               in a half-initialized state. Previously this was a catch for
               IOException, thrown by the old hand-rolled JavaFileManager
               wiring that has now been deleted. */
            Exceptions.printStackTrace(e);
        }
        /* The async path is meant to *enrich* a descriptor that the
           (Lucene-backed) JavaSymbolProvider already produced -- not to
           delete it. If javac couldn't load the TypeElement (te==null),
           no enclosed element matched ident, or the lookup threw, the
           entry is still a valid match from the index and must remain
           in the list. Returning an empty collection here would cause
           Models.MutableListModelImpl#descriptorChanged to remove the
           source from the live model, producing the long-standing
           "search results appear briefly then disappear" Go to Symbol
           symptom. Fall back to keeping the AsyncJavaSymbolDescriptor
           itself. */
        if (symbols.isEmpty()) {
            return Collections.<SymbolDescriptor>singleton(this);
        }
        return symbols;
    }

    @NonNull
    private static String getSimpleName (
            @NonNull final Element element,
            @NullAllowed final Element enclosingElement,
            final boolean caseSensitive) {
        String result = element.getSimpleName().toString();
        if (enclosingElement != null && INIT.equals(result)) {
            result = enclosingElement.getSimpleName().toString();
        }
        if (!caseSensitive) {
            result = result.toLowerCase();
        }
        return result;
    }

}
