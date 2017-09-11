/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
