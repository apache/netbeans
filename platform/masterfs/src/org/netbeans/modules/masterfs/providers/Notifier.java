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

package org.netbeans.modules.masterfs.providers;

import java.io.IOException;
import org.netbeans.modules.masterfs.watcher.NotifierAccessor;

/**
 * This SPI represents the interface between masterfs and
 * different implementations of filesystem watches on various systems.
 *
 * The SPI is kept very minimal, as the only necessary information is a queue
 * of modified folders, the filesystems code will evaluate the nature
 * of the change itself.
 * The SPI also doesn't distinguish between systems able of hierarchical
 * listening and systems without such a capability.
 * The implementation can report more events than registered, the infrastructure
 * should take care of filtering them.
 *
 * @author Petr Nejedly
 * @since 2.36
 */
public abstract class Notifier<KEY> {
    /**
     * Register a path for notifications. Optionally provide a key useful
     * for unregistering the path. The implementations that need to have every
     * path registered individually shall return a valid key, and shall
     * implement the {@link #removeWatch(java.lang.Object)} properly.
     *
     * @param path the path to register for notifications
     * @return a key useful for unregistering the path.
     * @throws IOException if the path can't be registered. For example if the
     * OS limit on the number of watched folders is reached. The exception
     * should be annotated with localized explanation.
     */
    protected abstract KEY addWatch(String path) throws IOException;
    
    /**
     * Unregister a path. Implementations that listen recursively on the whole
     * filesystem may ignore this request. They shall also return
     * <code>null</code> from the {@link #addWatch(java.lang.String)} call.
     * 
     * @param key the key obtained during registration.
     * @throws IOException
     */
    protected abstract void removeWatch(KEY key) throws IOException;

    /**
     *
     * @return absolute path of the changed folder or null in case
     * of overflow or any other reason to cause a full rescan
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract String nextEvent() throws IOException, InterruptedException;
    
    /** Starts the notifier. If the implementation is not ready to work,
     * it may throw I/O exception to signal it has not been initialized 
     * properly.
     * @exception IOException if the initialization cannot be performed
     */
    protected abstract void start() throws IOException;
    
    /** Get ready for stop. Clean all resources, the system is about to
     * shutdown the VM. By default this is no-op operation.
     */
    protected void stop() throws IOException {
    }
    
    static {
        NotifierAccessor impl = new NotifierAccessor() {
            @Override
            protected <KEY> KEY addWatch(Notifier<KEY> n, String path) throws IOException {
                return n.addWatch(path);
            }
            @Override
            protected <KEY> void removeWatch(Notifier<KEY> n, KEY key) throws IOException {
                n.removeWatch(key);
            }
            @Override
            protected String nextEvent(Notifier<?> n) throws IOException, InterruptedException {
                return n.nextEvent();
            }
            @Override
            protected void start(Notifier<?> n) throws IOException {
                n.start();
            }
            @Override
            protected void stop(Notifier<?> n) throws IOException {
                n.stop();
            }
        };
    }
}
