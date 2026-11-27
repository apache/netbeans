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
package org.openide.filesystems;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.Watchable;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

final class NioWatchService implements WatchService {
    private final BlockingQueue<Optional<NioWatchKey>> keys = new LinkedBlockingQueue<>();
    private final Set<NioWatchKey> registeredKeys = new HashSet<>();
    private volatile boolean closed;

    WatchKey register(Path path, WatchEvent.Kind<?>[] events) {
        checkClosed();
        NioWatchKey key = new NioWatchKey(this, path, events);
        synchronized (registeredKeys) {
            if (!closed) {
                if (key.isValid()) {
                    registeredKeys.add(key);
                }
                return key;
            }
        }
        key.cancel();
        throw new ClosedWatchServiceException();
    }

    @Override
    public WatchKey poll() throws ClosedWatchServiceException {
        checkClosed();
        return dequeued(keys.poll());
    }

    @Override
    public WatchKey poll(long timeout, TimeUnit unit)
            throws ClosedWatchServiceException, InterruptedException {
        checkClosed();
        return dequeued(keys.poll(timeout, unit));
    }

    @Override
    public WatchKey take() throws ClosedWatchServiceException, InterruptedException {
        checkClosed();
        return dequeued(keys.take());
    }

    private WatchKey dequeued(Optional<NioWatchKey> entry) {
        if (entry != null && entry.isEmpty()) {
            // Relay the close marker so every blocked consumer wakes up.
            keys.offer(entry);
        }
        checkClosed();
        if (entry != null) {
            NioWatchKey key = entry.orElseThrow();
            key.dequeued();
            return key;
        }
        return null;
    }

    @Override
    public void close() {
        List<NioWatchKey> registered;
        synchronized (registeredKeys) {
            if (closed) {
                return;
            }
            closed = true;
            registered = new ArrayList<>(registeredKeys);
            registeredKeys.clear();
        }
        keys.clear();
        keys.offer(Optional.empty());
        for (NioWatchKey key : registered) {
            key.cancel();
        }
    }

    void checkClosed() {
        if (closed) {
            throw new ClosedWatchServiceException();
        }
    }

    private static final class NioWatchKey implements WatchKey {
        private final NioWatchService service;
        private final Path path;
        private final WatchEvent.Kind<?>[] events;
        private final ConcurrentLinkedQueue<WatchEvent<?>> queuedEvents = new ConcurrentLinkedQueue<>();
        private final FileChangeListener listener;
        private final FileObject watchedObject;
        private volatile boolean valid = true;
        private boolean signalled;
        private boolean queued;

        NioWatchKey(NioWatchService service, Path path, WatchEvent.Kind<?>[] events) {
            this.service = service;
            this.path = path;
            this.events = events.clone();
            this.watchedObject = path instanceof NioPathForFileObject nio ? nio.getFileObject() : null;
            this.listener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent event) {
                    offer(StandardWatchEventKinds.ENTRY_CREATE, event.getFile().getNameExt());
                }

                @Override
                public void fileFolderCreated(FileEvent event) {
                    offer(StandardWatchEventKinds.ENTRY_CREATE, event.getFile().getNameExt());
                }

                @Override
                public void fileDeleted(FileEvent event) {
                    if (event.getFile() == watchedObject) {
                        invalidate(true);
                    } else {
                        offer(StandardWatchEventKinds.ENTRY_DELETE, event.getFile().getNameExt());
                    }
                }

                @Override
                public void fileRenamed(FileRenameEvent event) {
                    String oldName = event.getName();
                    if (event.getExt() != null && !event.getExt().isEmpty()) {
                        oldName += "." + event.getExt();
                    }
                    offer(StandardWatchEventKinds.ENTRY_DELETE, oldName);
                    offer(StandardWatchEventKinds.ENTRY_CREATE, event.getFile().getNameExt());
                }

                @Override
                public void fileChanged(FileEvent event) {
                    offer(StandardWatchEventKinds.ENTRY_MODIFY, event.getFile().getNameExt());
                }
            };
            if (watchedObject != null) {
                watchedObject.addFileChangeListener(listener);
            }
        }

        private synchronized void offer(WatchEvent.Kind<?> kind, String context) {
            if (!isValid()) {
                return;
            }
            for (WatchEvent.Kind<?> watched : events) {
                if (watched == kind) {
                    Path contextPath = path.getFileSystem().getPath(context);
                    queuedEvents.offer(new NioWatchEvent(kind, contextPath));
                    if (!signalled) {
                        signalled = true;
                        enqueue();
                    }
                    break;
                }
            }
        }

        @Override
        public boolean isValid() {
            return valid && !service.closed;
        }

        @Override
        public synchronized List<WatchEvent<?>> pollEvents() {
            List<WatchEvent<?>> result = new ArrayList<>();
            WatchEvent<?> event;
            while ((event = queuedEvents.poll()) != null) {
                result.add(event);
            }
            return result;
        }

        @Override
        public synchronized boolean reset() {
            if (!isValid()) {
                return false;
            }
            if (signalled) {
                if (queuedEvents.isEmpty()) {
                    signalled = false;
                } else {
                    enqueue();
                }
            }
            return true;
        }

        private void enqueue() {
            if (!queued) {
                queued = true;
                service.keys.offer(Optional.of(this));
            }
        }

        private synchronized void dequeued() {
            queued = false;
        }

        @Override
        public void cancel() {
            invalidate(false);
        }

        private void invalidate(boolean signal) {
            synchronized (this) {
                if (!valid) {
                    return;
                }
                valid = false;
                if (signal && !service.closed && !signalled) {
                    signalled = true;
                    enqueue();
                }
            }
            synchronized (service.registeredKeys) {
                service.registeredKeys.remove(this);
            }
            if (watchedObject != null) {
                watchedObject.removeFileChangeListener(listener);
            }
        }

        @Override
        public Watchable watchable() {
            return path;
        }
    }

    private static final class NioWatchEvent implements WatchEvent<Object> {
        private final WatchEvent.Kind<Object> kind;
        private final Object context;

        @SuppressWarnings("unchecked")
        NioWatchEvent(WatchEvent.Kind<?> kind, Object context) {
            this.kind = (WatchEvent.Kind<Object>) kind;
            this.context = context;
        }

        @Override
        public Kind<Object> kind() {
            return kind;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public Object context() {
            return context;
        }
    }
}
