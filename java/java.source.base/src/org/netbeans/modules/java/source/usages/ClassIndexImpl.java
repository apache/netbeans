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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.BaseUtilities;

/** Should probably final class with private constructor.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public abstract class ClassIndexImpl {

    public static enum State {
        NEW,
        INITIALIZED,
    }

    public static enum UsageType {
        SUPER_CLASS,
        SUPER_INTERFACE,
        FIELD_REFERENCE,
        METHOD_REFERENCE,
        TYPE_REFERENCE,
        FUNCTIONAL_IMPLEMENTORS;
    }

    /**
     * Type of ClassIndexImpl
     */
    public static enum Type {
        /**
         * Index does not exist yet or
         * it's broken
         */
        EMPTY,

        /**
         * Index for source root
         */
        SOURCE,

        /**
         * Index for binary root
         */
        BINARY;
    }

    public static final ThreadLocal<AtomicBoolean> cancel = new ThreadLocal<AtomicBoolean> ();       
    public static ClassIndexFactory FACTORY;
    private static final Logger LOG = Logger.getLogger(ClassIndexImpl.class.getName());
    
    private State state = State.NEW;
    private final List<WeakReference<ClassIndexImplListener>> listeners = Collections.synchronizedList(new ArrayList<WeakReference<ClassIndexImplListener>> ());

    public abstract <T> void search (
            @NonNull ElementHandle<?> binaryName,
            @NonNull Set<? extends UsageType> usageType,
            @NonNull Set<? extends ClassIndex.SearchScopeType> scope,
            @NonNull Convertor<? super Document, T> convertor,
            @NonNull Set<? super T> result) throws IOException, InterruptedException;

    public abstract <T> void getDeclaredElements (
            @NonNull String name,
            @NonNull ClassIndex.NameKind kind,
            @NonNull Set<? extends ClassIndex.SearchScopeType> scope,
            @NonNull FieldSelector selector,
            @NonNull Convertor<? super Document, T> convertor,
            @NonNull Collection<? super T> result) throws IOException, InterruptedException;

    public abstract <T> void getDeclaredElements (
            String ident,
            ClassIndex.NameKind kind,
            Convertor<? super Document, T> convertor,
            Map<T,Set<String>> result) throws IOException, InterruptedException;
    
    public abstract void getPackageNames (String prefix, boolean directOnly, Set<String> result) throws IOException, InterruptedException;
    
    public abstract void getReferencesFrequences (
            @NonNull final Map<String,Integer> typeFreq,
            @NonNull final Map<String,Integer> pkgFreq) throws IOException, InterruptedException;
    
    public abstract FileObject[] getSourceRoots ();
   
    public abstract FileObject[] getBinaryRoots ();
    
    public abstract BinaryAnalyser getBinaryAnalyser ();
    
    public abstract SourceAnalyzerFactory.StorableAnalyzer getSourceAnalyser ();
    
    public abstract String getSourceName (String binaryName) throws IOException, InterruptedException;
    
    public abstract void setDirty (URL url);
    
    public abstract boolean isValid ();

    public abstract Type getType();

    protected abstract void close () throws IOException;    
    
    public void addClassIndexImplListener (final ClassIndexImplListener listener) {
        assert listener != null;        
        this.listeners.add (new Ref (listener));
    }
    
    public void removeClassIndexImplListener (final ClassIndexImplListener listener) {
        assert listener != null;
        synchronized (this.listeners) {
            for (Iterator<WeakReference<ClassIndexImplListener>> it = this.listeners.iterator(); it.hasNext();) {
                WeakReference<ClassIndexImplListener> lr = it.next();
                ClassIndexImplListener l = lr.get();
                if (listener == l) {
                    it.remove();
                }
            }
        }
    }

    void typesEvent (
            @NonNull final URL root,
            @NonNull final Pair<ElementHandle<ModuleElement>, Collection<? extends ElementHandle<TypeElement>>> added,
            @NonNull final Pair<ElementHandle<ModuleElement>, Collection<? extends ElementHandle<TypeElement>>> removed,
            @NonNull final Pair<ElementHandle<ModuleElement>, Collection<? extends ElementHandle<TypeElement>>> changed) {
        final ClassIndexImplEvent a = added.first() == null && added.second().isEmpty() ? null : new ClassIndexImplEvent(this, root, added.first(), added.second());
        final ClassIndexImplEvent r = removed.first() == null && removed.second().isEmpty() ? null : new ClassIndexImplEvent(this, root, removed.first(), removed.second());
        final ClassIndexImplEvent ch = changed.first() == null && changed.second().isEmpty() ? null : new ClassIndexImplEvent(this, root, changed.first(), changed.second());
        typesEvent(a, r, ch);
    }

    private void typesEvent (
            @NullAllowed final ClassIndexImplEvent added,
            @NullAllowed final ClassIndexImplEvent removed,
            @NullAllowed final ClassIndexImplEvent changed) {
        WeakReference<ClassIndexImplListener>[] _listeners;
        synchronized (this.listeners) {
            _listeners = this.listeners.toArray(new WeakReference[0]);
        }
        for (WeakReference<ClassIndexImplListener> lr : _listeners) {
            ClassIndexImplListener l = lr.get();
            if (l != null) {
                if (added != null) {
                    l.typesAdded(added);
                }
                if (removed != null) {
                    l.typesRemoved(removed);
                }
                if (changed != null) {
                    l.typesChanged(changed);
                }
            }
        }
    }

    public final State getState() {
        return this.state;
    }

    public final void setState(final State state) {
        assert state != null;
        assert this.state != null;
        if (state.ordinal() < this.state.ordinal()) {
            throw new IllegalArgumentException();
        }
        this.state=state;
    }
    
    /**
     * Handles exception. When exception is thrown from the non initialized index,
     * the index has not been checked if it's corrupted. If it's corrupted don't display
     * the error to user just log it. The index will be recovered during the scan.
     * @param ret ret value
     * @param e exception
     * @return ret
     * @throws Exception 
     */
    @CheckForNull
    protected final <R, E extends Exception> R handleException (
            @NullAllowed final R ret,
            @NonNull final E e,
            @NullAllowed final URL root) throws E {
        if (State.NEW == getState()) {
            LOG.log(Level.FINE, "Exception from non initialized index", e); //NOI18N
            return ret;
        } else {
            throw Exceptions.attachMessage(e, "Index state: " + state + ", Root: " + root); //NOI18N
        }
    }
    
    public static interface Writer {
        void clear() throws IOException;
        void deleteAndStore(final List<Pair<Pair<BinaryName,String>, Object[]>> refs, final Set<Pair<String,String>> toDelete) throws IOException;
        /**
         * Different from deleteAndStore in that the data is NOT committed, but just flushed. Make sure, deleteAndStore is called from the
         * indexer's finish!
         * 
         * @param refs
         * @param toDelete
         * @throws IOException 
         */
        void deleteAndFlush(final List<Pair<Pair<BinaryName,String>, Object[]>> refs, final Set<Pair<String,String>> toDelete) throws IOException;
        
        /**
         * Flushes any pending data from deleteAndFlush as if deleteAndStore was called with empty collections
         */
        void commit() throws IOException;
        
        void rollback() throws IOException;
    }
    
    private class Ref extends WeakReference<ClassIndexImplListener> implements Runnable {
        public Ref (ClassIndexImplListener listener) {
            super (listener, BaseUtilities.activeReferenceQueue());
        }

        public void run() {
            listeners.remove(this);
        }
    }
}
