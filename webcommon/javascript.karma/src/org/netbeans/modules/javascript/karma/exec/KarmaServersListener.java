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

package org.netbeans.modules.javascript.karma.exec;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;

public interface KarmaServersListener extends EventListener {

    void serverStateChanged(@NonNull Project project);

    //~ Inner classes

    final class Support {

        private final List<KarmaServersListener> listeners = new CopyOnWriteArrayList<>();

        public void addKarmaServersListener(@NullAllowed KarmaServersListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        public void removeKarmaServersListener(@NullAllowed KarmaServersListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        public void fireServerChanged(@NonNull KarmaServer karmaServer) {
            for (KarmaServersListener listener : listeners) {
                listener.serverStateChanged(karmaServer.getProject());
            }
        }

        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
