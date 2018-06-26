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
package org.netbeans.modules.javascript.cdnjs;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;

/**
 * Defines an object which listens for changes in CDNJS libraries.
 */
public interface LibraryListener extends EventListener {

    /**
     * This method is called when libraries in a project change.
     * @param project project which libraries has changed
     */
    void librariesChanged(@NonNull Project project);

    //~ Inner classes

    /**
     * Support class for working with {@link LibraryListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<LibraryListener> listeners = new CopyOnWriteArrayList<>();


        /**
         * Adds a {@link LibraryListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param listener the {@link LibraryListener} to be added, can be {@code null}
         */
        public void addLibraryListener(@NullAllowed LibraryListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@link LibraryListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param listener the {@link LibraryListener} to be removed, can be {@code null}
         */
        public void removeLibraryListener(@NullAllowed LibraryListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fires an event in libraries of the given project.
         * @param project project which libraries have changed
         */
        public void fireLibrariesChanged(@NonNull Project project) {
            assert project != null;
            for (LibraryListener listener : listeners) {
                listener.librariesChanged(project);
            }
        }

        /**
         * Checks if there are any listeners registered to this {@code Support}.
         * @return {@code true} if there are one or more listeners, {@code false} otherwise.
         */
        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
