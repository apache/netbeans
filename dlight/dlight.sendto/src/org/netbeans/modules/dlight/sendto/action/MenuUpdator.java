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
package org.netbeans.modules.dlight.sendto.action;

import org.netbeans.modules.dlight.sendto.api.Configuration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 *
 */
public class MenuUpdator {

    private static final ExecutorService rp = Executors.newSingleThreadExecutor();
    private static MenuUpdator lastCreated = null;
    private static final Object factoryLock = new Object();
    private final DynamicMenu menu;
    private final ConcurrentLinkedQueue<Future<Void>> tasks = new ConcurrentLinkedQueue<Future<Void>>();

    private MenuUpdator(DynamicMenu menu) {
        this.menu = menu;
    }

    static void start(DynamicMenu menu, Lookup actionContext, List<Configuration> configs) {
        synchronized (factoryLock) {
            if (lastCreated != null) {
                lastCreated.cancel();
            }

            lastCreated = new MenuUpdator(menu);

            for (Configuration cfg : configs) {
                final FutureAction action = cfg.getHandler().createActionFor(actionContext, cfg);
                if (action != null) {
                    lastCreated.add(action);
                }
            }
        }
    }

    private void add(final FutureAction faction) {
        tasks.add(rp.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    menu.addValidatingItem();
                    Action action = faction.getAction();
                    if (action != null) {
                        menu.addDynamicItem(action);
                    }
                } finally {
                    menu.removeValidatingItem();
                }
                return null;
            }
        }));
    }

    private void cancel() {
        for (Future<Void> task : tasks) {
            task.cancel(false);
        }
    }
}
