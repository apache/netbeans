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

package org.netbeans.modules.db.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Common ancestor for all test classes.
 *
 * This currently does nothing but keeping it here in case we do want to
 * add common functionality.
 *
 * @author Andrei Badea
 */
public abstract class TestBase extends NbTestCase {
    public TestBase(String name) {
        super(name);
    }

    /**
     * Force flush of config filesystem and EDT.
     *
     * Make sure outstanding writes to the config filesystem and outstanding
     * events on the EDT are flushed
     */
    protected void forceFlush() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                    "forceFlush might only be called off the EDT!");
        }
        try {
            FileUtil.getConfigRoot().getFileSystem()
                    .runAtomicAction(new FileSystem.AtomicAction() {
                        @Override
                        public void run() throws IOException {
                            // NOOP - force a wait
                        }
                    });
            Thread.sleep(1 * 1000);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // NOOP - force a wait
                }
            });
        } catch (IOException | InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
