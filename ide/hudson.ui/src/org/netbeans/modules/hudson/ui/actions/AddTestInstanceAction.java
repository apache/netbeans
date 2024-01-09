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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.ui.api.UI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * #161911: downloads & runs latest hudson.war and configures it for you.
 */
public class AddTestInstanceAction extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(AddTestInstanceAction.class.getName());

    @Messages("AddTestInstanceAction.label=Try Jenkins on &Localhost")
    public AddTestInstanceAction() {
        super(Bundle.AddTestInstanceAction_label());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(this);
    }

    @Messages({
        "# {0} - path to javaws", "AddTestInstanceAction.no_javaws=Could not find {0}. Run: javaws https://hudson.dev.java.net/hudson.jnlp",
        "AddTestInstanceAction.could_not_run=Could not download or run Jenkins. Run: javaws https://hudson.dev.java.net/hudson.jnlp",
        "AddTestInstanceAction.starting=Downloading & running Jenkins...",
        "AddTestInstanceAction.instance_name=Local Test Server"
    })
    @Override
    public void run() {
        // XXX could use JavaPlatformManager.default.defaultPlatform.findTool("javaws") if could depend on java.platform
        File javaHome = new File(System.getProperty("java.home"));
        File bindir = new File(javaHome.getParentFile(), "bin");
        if (!bindir.isDirectory()) { // #171884
            bindir = new File(javaHome, "bin");
        }
        File javaws = new File(bindir, "javaws.exe");
        if (!javaws.isFile()) {
            javaws = new File(bindir, "javaws");
        }
        if (!javaws.isFile()) {
            warning(Bundle.AddTestInstanceAction_no_javaws(javaws));
            return;
        }
        try {
            int exit = new ProcessBuilder(javaws.getAbsolutePath(), "https://hudson.dev.java.net/hudson.jnlp").start().waitFor();
            if (exit != 0) {
                warning(Bundle.AddTestInstanceAction_could_not_run());
                return;
            }
        } catch (Exception x) {
            warning(Bundle.AddTestInstanceAction_could_not_run());
            LOG.log(Level.INFO, null, x);
            return;
        }
        final AtomicBoolean cancelled = new AtomicBoolean();
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                Bundle.AddTestInstanceAction_starting(), new Cancellable() {
            @Override
            public boolean cancel() {
                cancelled.set(true);
                return true;
            }
        });
        progress.start();
        try {
            String localhost = "http://localhost:8080/"; // NOI18N
            while (!cancelled.get()) {
                try {
                    Thread.sleep(1000); // wait a little bit
                } catch (InterruptedException x) {
                    LOG.log(Level.INFO, null, x);
                }
                try {
                    new ConnectionBuilder().url(localhost).connection();
                    // Success!
                    HudsonManager.addInstance(Bundle.AddTestInstanceAction_instance_name(), localhost, 1, true); // NOI18N
                    UI.selectNode(localhost);
                    break;
                } catch (IOException x) {
                    LOG.log(Level.FINER, null, x);
                    // Not up & running yet.
                }
            }
        } finally {
            progress.finish();
        }
    }

    private void warning(String message) throws MissingResourceException {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE));
    }

}
