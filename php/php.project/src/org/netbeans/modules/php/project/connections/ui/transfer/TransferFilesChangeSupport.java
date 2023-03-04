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
