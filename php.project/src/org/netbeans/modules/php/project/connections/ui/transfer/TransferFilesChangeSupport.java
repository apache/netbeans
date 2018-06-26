/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ui.transfer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel.TransferFilesChangeListener;
import org.openide.util.Exceptions;

/**
 * Inspired by {@link org.openide.util.ChangeSupport}.
 */
public final class TransferFilesChangeSupport {

    final List<TransferFilesChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final Object source;

    public TransferFilesChangeSupport(Object source) {
        this.source = source;
    }

    public void addChangeListener(TransferFilesChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public void removeChangeListener(TransferFilesChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    public void fireSelectedFilesChange() {
        for (TransferFilesChangeListener listener : listeners) {
            try {
                listener.selectedFilesChanged();
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    public void fireFilterChange() {
        for (TransferFilesChangeListener listener : listeners) {
            try {
                listener.filterChanged();
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
