/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.dlight.sendto.util;

import org.netbeans.modules.dlight.sendto.api.OutputMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * It IO with the same name was requested before and Process that was executed
 * in that previous IO is already finished and it's status == 0, then that tab
 * will be closed before new tab is open.
 *
 */
public final class CachedIOProvider {

    private static final Object lock = new Object();
    private static final Collection<InputOutputData> cache = new ArrayList<InputOutputData>();

    public static InputOutput getIO(final String tabName, final Action[] actions, final AtomicReference<Process> procRef, OutputMode outputMode) {
        synchronized (lock) {
            // Close uneeded tabs...
            Iterator<InputOutputData> it = cache.iterator();

            while (it.hasNext()) {
                InputOutputData cachedData = it.next();

                if (!cachedData.title.equals(tabName)) {
                    continue;
                }

                Process process = cachedData.procRef.get();
                if (process != null && !ProcessUtils.isAlive(process)) {
                    if (process.exitValue() == 0) {
                        cachedData.io.closeInputOutput();
                        it.remove();
                    }
                }
            }

            IOProvider term = null;

            if (OutputMode.INTERNAL_TERMINAL == outputMode) {
                term = IOProvider.get("Terminal"); // NOI18N
            }

            if (term == null) {
                term = IOProvider.getDefault();
            }

            InputOutput io = term.getIO(tabName, actions);
            cache.add(new InputOutputData(tabName, io, procRef));

            return io;
        }
    }

    private static final class InputOutputData {

        private final InputOutput io;
        private final AtomicReference<Process> procRef;
        private final String title;

        private InputOutputData(final String title, final InputOutput io, final AtomicReference<Process> procRef) {
            this.title = title;
            this.io = io;
            this.procRef = procRef;
        }
    }
}
