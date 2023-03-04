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
package org.netbeans.modules.parsing.impl.indexing;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.implspi.NotifyImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
final class Notify {

    private static final Runnable NOP = new Runnable() {
        @Override
        public void run() {
        }
    };

    private Notify() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @NonNull
    static Runnable showStatus(@NonNull final String message) {
        final NotifyImplementation notify = Lookup.getDefault().lookup(NotifyImplementation.class);
        if (notify != null) {
            return notify.showStatus(message);
        } else {
            return NOP;
        }
    }
}
