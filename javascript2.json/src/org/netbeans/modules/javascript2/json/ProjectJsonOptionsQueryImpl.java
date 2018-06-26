/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.netbeans.modules.javascript2.json.spi.support.JsonPreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * An implementation of the {@link JsonOptionsQueryImplementation} which delegates
 * to the {@link Project} lookup.
 * @author Tomas Zezula
 */
@ServiceProvider(service = JsonOptionsQueryImplementation.class, position = 1_000)
public final class ProjectJsonOptionsQueryImpl implements JsonOptionsQueryImplementation {

    private final Map</*@GuardedBy("normCache")*/Project,Reference<Result>> normCache;

    public ProjectJsonOptionsQueryImpl() {
        normCache = new WeakHashMap<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(@NonNull final FileObject file) {
        final Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        final JsonOptionsQueryImplementation impl = p.getLookup().lookup(JsonOptionsQueryImplementation.class);
        final Result overrideRes = impl == null ?
                null :
                impl.getOptions(file);
        final Result defaultRes = createDefaultResult(p);
        return overrideRes == null ?
                defaultRes :
                new MergedResult(overrideRes, defaultRes);
    }

    private Result createDefaultResult(@NonNull final Project p) {
        synchronized (normCache) {
            final Reference<Result> ref = normCache.get(p);
            Result res = ref == null ? null : ref.get();
            if (res == null) {
                res = new DefaultProjectResult(p);
                normCache.put(p, new WeakReference<>(res));
            }
            return res;
        }
    }

    private static final class DefaultProjectResult implements Result, PropertyChangeListener {
        private final JsonPreferences jsonPrefs;
        private final PropertyChangeSupport listeners;
        private final AtomicBoolean listens = new AtomicBoolean();

        DefaultProjectResult(@NonNull final Project project) {
            this.jsonPrefs = JsonPreferences.forProject(project);
            this.listeners = new PropertyChangeSupport(this);
        }

        @CheckForNull
        @Override
        public Boolean isCommentSupported() {
            return jsonPrefs.isCommentSupported() ?
                    Boolean.TRUE :
                    null;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            if (!listens.get() && listens.compareAndSet(false, true)) {
                jsonPrefs.addPropertyChangeListener(this);
            }
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            listeners.firePropertyChange(PROP_COMMENT_SUPPORTED, null, null);
        }
    }
}
