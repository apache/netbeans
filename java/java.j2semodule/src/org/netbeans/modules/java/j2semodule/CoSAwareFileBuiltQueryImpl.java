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
package org.netbeans.modules.java.j2semodule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Zezula
 */
final class CoSAwareFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {

    private final FileBuiltQueryImplementation delegate;
    private final J2SEModularProject project;
    private final AtomicBoolean cosEnabled = new AtomicBoolean();
    private final Map<FileObject, Reference<StatusImpl>> file2Status = new WeakHashMap<FileObject, Reference<StatusImpl>>();

    @SuppressWarnings("LeakingThisInConstructor")
    public CoSAwareFileBuiltQueryImpl(FileBuiltQueryImplementation delegate, J2SEModularProject project) {
        this.delegate = delegate;
        this.project = project;
        project.evaluator().addPropertyChangeListener(this);
        setCoSEnabledAndXor();
    }

    private synchronized StatusImpl readFromCache(FileObject file) {
        Reference<StatusImpl> r = file2Status.get(file);
        return r != null ? r.get() : null;
    }

    @Override
    public Status getStatus(FileObject file) {
        StatusImpl result = readFromCache(file);
        if (result != null) {
            return result;
        }
        Status status = delegate.getStatus(file);
        if (status == null) {
            return null;
        }
        synchronized (this) {
            StatusImpl foisted = readFromCache(file);

            if (foisted != null) {
                return foisted;
            }
            file2Status.put(file, new WeakReference<>(result = new StatusImpl(cosEnabled, status)));
        }
        return result;
    }

    boolean setCoSEnabledAndXor() {
        boolean nue = J2SEModularProjectUtil.isCompileOnSaveEnabled(project);
        boolean old = cosEnabled.getAndSet(nue);
        return old != nue;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!setCoSEnabledAndXor()) {
            return;
        }
        Collection<Reference<StatusImpl>> toRefresh;
        synchronized (this) {
            toRefresh = new LinkedList<>(file2Status.values());
        }
        for (Reference<StatusImpl> r : toRefresh) {
            StatusImpl s = r.get();
            if (s != null) {
                s.stateChanged(null);
            }
        }
    }

    private static final class StatusImpl implements Status, ChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final AtomicBoolean cosEnabled;
        private final Status delegate;

        @SuppressWarnings("LeakingThisInConstructor")
        public StatusImpl(AtomicBoolean cosEnabled, Status delegate) {
            this.cosEnabled = cosEnabled;
            this.delegate = delegate;
            this.delegate.addChangeListener(this);
        }

        @Override
        public boolean isBuilt() {
            return cosEnabled.get() || delegate.isBuilt();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }
    }
}
