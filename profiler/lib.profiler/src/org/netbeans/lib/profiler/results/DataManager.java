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

package org.netbeans.lib.profiler.results;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A common functionality for DataManagers
 *
 * @author Ian Formanek
 */
public abstract class DataManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Set listeners = new HashSet();

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Listeners ---------------------------------------------------------------

    /**
     * Adds new threadData Listener.
     *
     * @param listener threadData listener to add
     */
    public void addDataListener(DataManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes threadData listener.
     *
     * @param listener threadData listener to remove
     */
    public void removeDataListener(DataManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners about the threadData change.
     */
    protected void fireDataChanged() {
        if (listeners.isEmpty()) {
            return;
        }

        Set toNotify;

        synchronized (listeners) {
            toNotify = new HashSet(listeners);
        }

        Iterator iterator = toNotify.iterator();

        while (iterator.hasNext()) {
            final DataManagerListener listener = ((DataManagerListener) iterator.next());
            listener.dataChanged();
        }
    }

    /**
     * Notifies all listeners about the reset of threads data.
     */
    protected void fireDataReset() {
        if (listeners.isEmpty()) {
            return;
        }

        Set toNotify;

        synchronized (listeners) {
            toNotify = new HashSet(listeners);
        }

        Iterator iterator = toNotify.iterator();

        while (iterator.hasNext()) {
            final DataManagerListener listener = ((DataManagerListener) iterator.next());
            listener.dataReset();
        }
    }
}
