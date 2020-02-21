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

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class FutureAction {

    private static final int rpLimit = Integer.parseInt(System.getProperty("SendTo.maxParallelTasks", "5")); // NOI18N
    private static final AtomicInteger rpAvail = new AtomicInteger(rpLimit);
    private static final RequestProcessor rp = new RequestProcessor("FutureAction", rpLimit); // NOI18N
    private final String msg;
    private final Callable<Action> callable;
    private final AtomicReference<Action> actionRef = new AtomicReference<Action>();

    public FutureAction(String msg) {
        this.msg = msg;
        this.callable = null;
    }

    public FutureAction(final Callable<Action> callable) {
        this.callable = callable;
        this.msg = null;
    }

    // TODO: hide
    public Action getAction() {
        Action action = actionRef.get();

        if (action != null) {
            return action;
        }

        if (msg != null) {
            action = new MessageAction(msg);
            actionRef.set(action);
            return action;
        }

        try {
            final Action a = callable.call();
            
            if (a == null) {
                return null;
            }
            
            action = new AbstractAction((String) a.getValue(Action.NAME)) {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (rpAvail.getAndDecrement() <= 0) {
                        Message message = new NotifyDescriptor.Message(
                                NbBundle.getMessage(FutureAction.class, "TaskWillBeQueued.message", (String) a.getValue(Action.NAME)), // NOI18N
                                NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(message);
                    }

                    rp.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                a.actionPerformed(e);
                            } finally {
                                rpAvail.incrementAndGet();
                            }
                        }
                    });
                }
            };
            actionRef.set(action);
            return action;
        } catch (Exception ex) {
        }

        return null;
    }
}
