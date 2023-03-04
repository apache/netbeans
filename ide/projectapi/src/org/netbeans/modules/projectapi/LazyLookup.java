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
package org.netbeans.modules.projectapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

final class LazyLookup extends ProxyLookup {
    private final Map<String, Object> attrs;
    private final Lookup lkp;
    private Collection<String> serviceNames;
    final Thread[] LOCK = {null};

    public LazyLookup(Map<String, Object> attrs, Lookup lkp) {
        this.attrs = attrs;
        this.lkp = lkp;
        this.serviceNames = Arrays.asList(((String) attrs.get("service")).split(",")); // NOI18N
    }

    @Override
    protected void beforeLookup(Template<?> template) {
        class NotifyLater implements Executor {
            private List<Runnable> pending = new ArrayList<>();
            @Override
            public void execute(Runnable command) {
                pending.add(command);
            }
            
            public void deliverPending() {
                List<Runnable> notify = pending;
                pending = null;
                for (Runnable r : notify) {
                    r.run();
                }
            }
        }
        LazyLookupProviders.safeToLoad(this.lkp);
        Class<?> service = template.getType();
        NotifyLater later = null;
        synchronized (LOCK) {
            for (;;) {
                if (serviceNames == null || !serviceNames.contains(service.getName())) {
                    return;
                }
                if (LOCK[0] == null) {
                    break;
                }
                if (LOCK[0] == Thread.currentThread()) {
                    return;
                }
                try {
                    LOCK.wait();
                } catch (InterruptedException ex) {
                    LazyLookupProviders.LOG.log(Level.INFO, null, ex);
                }
            }
            LOCK[0] = Thread.currentThread();
            LOCK.notifyAll();
        }
        try {
            Object instance = LazyLookupProviders.loadPSPInstance((String) attrs.get("class"), (String) attrs.get("method"), lkp); // NOI18N
            if (!service.isInstance(instance)) {
                // JRE #6456938: Class.cast currently throws an exception without details.
                throw new ClassCastException("Instance of " + instance.getClass() + " unassignable to " + service);
            }
            setLookups(later = new NotifyLater(), Lookups.singleton(instance));
            synchronized (LOCK) {
                serviceNames = null;
                LOCK.notifyAll();
            }
        } catch (Exception x) {
            Exceptions.attachMessage(x, "while loading from " + attrs);
            Exceptions.printStackTrace(x);
        } finally {
            synchronized (LOCK) {
                LOCK[0] = null;
                LOCK.notifyAll();
            }
            if (later != null) {
                later.deliverPending();
            }
        }
    }

    boolean isInitializing() {
        return LOCK[0] != null;
    }
    
    @Override
    @SuppressWarnings(value = "element-type-mismatch")
    public String toString() {
        return "LazyLookupProviders.LookupProvider[service=" + attrs.get("service") + ", class=" + attrs.get("class") + ", orig=" + attrs.get(FileObject.class) + "]";
    }

}
