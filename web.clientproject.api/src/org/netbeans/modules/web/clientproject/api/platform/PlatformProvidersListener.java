/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.platform;

import java.beans.PropertyChangeEvent;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.util.Parameters;

/**
 * Defines an object which listens for changes in platform providers.
 * @since 1.68
 */
public interface PlatformProvidersListener extends EventListener {

    /**
     * This method is called when list of platform providers changes.
     */
    void platformProvidersChanged();

    /**
     * This method is called when property of the given platform provider changes.
     * @param project project which properties has changed, can be {@code null} if the property is not project specific
     * @param platformProvider platform provider which properties has changed
     * @param event information about property change
     */
    void propertyChanged(@NullAllowed Project project, @NonNull PlatformProvider platformProvider, @NonNull PropertyChangeEvent event);

    /**
     * Support class for working with {@link PlatformProvidersListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<PlatformProvidersListener> listeners = new CopyOnWriteArrayList<>();

        /**
         * Add a {@code PlatformProvidersListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param listener the {@code PlatformProvidersListener} to be added, can be {@code null}
         */
        public void addPlatformProvidersListener(@NullAllowed PlatformProvidersListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@code PlatformProvidersListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param listener the {@code PlatformProvidersListener} to be removed, can be {@code null}
         */
        public void removePlatformProvidersListener(@NullAllowed PlatformProvidersListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fire an event in {@link PlatformProviders#getPlatformProviders() list of platform providers}.
         */
        public void firePlatformProvidersChanged() {
            for (PlatformProvidersListener listener : listeners) {
                listener.platformProvidersChanged();
            }
        }

        /**
         * Fire an event in property of the given platform provider.
         * @param project project which properties has changed, can be {@code null} if the property is not project specific
         * @param platformProvider platform provider which properties has changed
         * @param event information about property change
         */
        public void firePropertyChanged(@NullAllowed Project project, @NonNull PlatformProvider platformProvider, @NonNull PropertyChangeEvent event) {
            Parameters.notNull("platformProvider", platformProvider); // NOI18N
            Parameters.notNull("event", event); // NOI18N
            for (PlatformProvidersListener listener : listeners) {
                listener.propertyChanged(project, platformProvider, event);
            }
        }

        /**
         * Check if there are any listeners registered to this {@code Support}.
         * @return {@code true} if there are one or more listeners, {@code false} otherwise.
         */
        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
