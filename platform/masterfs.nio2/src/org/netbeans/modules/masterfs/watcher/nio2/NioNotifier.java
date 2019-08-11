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
package org.netbeans.modules.masterfs.watcher.nio2;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.netbeans.modules.masterfs.providers.Notifier;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
@ServiceProvider(service=Notifier.class, position=200)
public class NioNotifier extends Notifier<WatchKey> {
    private final WatchService watcher;

    public NioNotifier() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
    }
    
    @Override
    protected WatchKey addWatch(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        try {
            WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            return key;
        } catch (ClosedWatchServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void removeWatch(WatchKey key) throws IOException {
        try {
            key.cancel();
        } catch (ClosedWatchServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected String nextEvent() throws IOException, InterruptedException {
        WatchKey key;
        try {
            key = watcher.take();
        } catch (ClosedWatchServiceException cwse) { // #238261
            @SuppressWarnings({"ThrowableInstanceNotThrown"})
            InterruptedException ie = new InterruptedException();
            throw (InterruptedException) ie.initCause(cwse);
        }
        Path dir = (Path)key.watchable();
               
        String res = dir.toAbsolutePath().toString();
        for (WatchEvent<?> event: key.pollEvents()) {
            if (event.kind() == OVERFLOW) {
                // full rescan
                res = null;
            }
        }
        key.reset();
        return res;
    }

    @Override
    protected void start() throws IOException {
    }

    @Override
    protected void stop() throws IOException {
        watcher.close();
    }
}
