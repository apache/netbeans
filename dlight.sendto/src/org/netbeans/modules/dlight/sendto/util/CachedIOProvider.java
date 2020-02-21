/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
