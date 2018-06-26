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
package org.netbeans.modules.javascript2.json.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class JsonOptionsQuery {

    private JsonOptionsQuery() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @NonNull
    public static Result getOptions(@NonNull final FileObject file) {
        Parameters.notNull("file", file);
        final Deque<JsonOptionsQueryImplementation.Result> results = new ArrayDeque<>();
        for (JsonOptionsQueryImplementation impl : Lookup.getDefault().lookupAll(JsonOptionsQueryImplementation.class)) {
            final JsonOptionsQueryImplementation.Result res = impl.getOptions(file);
            if (res != null) {
                results.offer(res);
            }
        }
        return new Result(results);
    }

    public static final class Result {

        public static final String PROP_COMMENT_SUPPORTED = "commentSupported"; //NOI18N
        private final Collection<? extends JsonOptionsQueryImplementation.Result> delegates;
        private final PropertyChangeSupport listeners;
        private final PropertyChangeListener pcl;
        private final AtomicBoolean listens = new AtomicBoolean();

        private Result(@NonNull final Collection<? extends JsonOptionsQueryImplementation.Result> delegates) {
            Parameters.notNull("delegates", delegates); //NOI18N
            this.delegates = delegates;
            this.listeners = new PropertyChangeSupport(this);
            this.pcl = (evt) -> listeners.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }

        public boolean isCommentSupported() {
            for (JsonOptionsQueryImplementation.Result delegate : delegates) {
                final Boolean res = delegate.isCommentSupported();
                if (res != null) {
                    return res;
                }
            }
            return false;
        }

        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!listens.get() && listens.compareAndSet(false, true)) {
                this.delegates
                    .forEach((r) -> r.addPropertyChangeListener(WeakListeners.propertyChange(pcl, r)));
            }
            listeners.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.removePropertyChangeListener(listener);
        }
    }
}
