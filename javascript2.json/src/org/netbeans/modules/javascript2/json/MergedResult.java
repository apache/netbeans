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
import java.util.Arrays;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Merges {@link JsonOptionsQueryImplementation.Result}.
 * @author Tomas Zezula
 */
final class MergedResult implements JsonOptionsQueryImplementation.Result, PropertyChangeListener {

    private final JsonOptionsQueryImplementation.Result[] delegates;
    private final PropertyChangeSupport listeners;

    MergedResult(@NonNull final JsonOptionsQueryImplementation.Result... delegates) {
        Parameters.notNull("delegates", delegates); //NOI18N
        this.delegates = Arrays.copyOf(delegates, delegates.length);
        this.listeners = new PropertyChangeSupport(this);
        Arrays.stream(this.delegates)
                .forEach((r) -> r.addPropertyChangeListener(WeakListeners.propertyChange(this, r)));
    }

    @CheckForNull
    @Override
    public Boolean isCommentSupported() {
        for (JsonOptionsQueryImplementation.Result delegate : delegates) {
            final Boolean res = delegate.isCommentSupported();
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        this.listeners.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}
