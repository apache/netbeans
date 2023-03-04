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
package org.netbeans.modules.javaee.wildfly.nodes.actions;

import org.netbeans.modules.javaee.wildfly.ide.WildflyKiller;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import static org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javaee.wildfly.ide.WildflyOutputSupport;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyManagerNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class KillServerAction extends CookieAction {

    private static final RequestProcessor PROCESSOR = new RequestProcessor ("JBoss kill UI", 1); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(KillServerAction.class.getName());

    private final WildflyKiller killer = new WildflyKiller();

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @NbBundle.Messages("MSG_KillFailed=Kill action failed")
    @Override
    protected void performAction(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            return;
        }

        final WildflyManagerNode managerNode = nodes[0].getCookie(WildflyManagerNode.class);
        if (managerNode == null) {
            return;
        }

        final Future<Boolean> killed = RequestProcessor.getDefault().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                WildflyOutputSupport output = WildflyOutputSupport.getInstance(
                        managerNode.getDeploymentManager().getInstanceProperties(), false);
                if (output != null) {
                    Process p = output.getProcess();
                    if (p != null) {
                        p.destroy();
                        try {
                            boolean ok = p.waitFor(5, TimeUnit.SECONDS);
                            if (!ok) {
                                ok = p.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
                            }
                            if (ok) {
                                LOGGER.log(Level.INFO, "Succesfully killed WildFly");
                                return true;
                            }
                        } catch (InterruptedException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }
                // pretty ugly; kills all processes
                return killer.killServers();
            }
        });
        PROCESSOR.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (killed.get(15, TimeUnit.SECONDS)) {
                        managerNode.getDeploymentManager().getInstanceProperties().refreshServerInstance();
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    LOGGER.log(Level.INFO, "Kill action failed", ex);
                    NotifyDescriptor desc = new NotifyDescriptor.Message(Bundle.MSG_KillFailed(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        });

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(KillServerAction.class, "LBL_KillServerGUIAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[]{};
    }
}
