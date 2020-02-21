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
