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
