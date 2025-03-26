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
package org.netbeans.modules.payara.common.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * This action will open the server home directory for the selected server instance.
 * 
 * @author Gaurav Gupta
 */
@ActionID(id = "org.netbeans.modules.payara.common.actions.OpenServerHomeDirectoryAction", category = "Payara")
@ActionRegistration(displayName = "#CTL_OpenServerHomeDirectoryAction", lazy = false)
public class OpenServerHomeDirectoryAction extends NodeAction {

    private static final Logger LOGGER
            = PayaraLogger.get(OpenServerHomeDirectoryAction.class);
    
    @Override
    protected void performAction(Node[] nodes) {
        if (nodes != null && nodes.length > 0 && nodes[0] != null) {
            PayaraModule commonSupport = nodes[0].getLookup().lookup(PayaraModule.class);
            if (commonSupport != null && (commonSupport.getInstance() instanceof PayaraInstance)) {
                PayaraInstance server = (PayaraInstance) commonSupport.getInstance();
                String homePath = server.getServerHome();
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    File directory = new File(homePath);

                    if (directory.exists()) {
                        try {
                            desktop.open(directory);
                        } catch (IOException e) {
                            LOGGER.log(Level.INFO, "Error opening server home directory: {0}", e.getMessage());
                        }
                    } else {
                        LOGGER.log(Level.INFO, "Server home directory does not exist: {0}", homePath);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Desktop not supported for opening directory.");
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1 || nodes[0] == null) {
            return false;
        }
        PayaraModule commonSupport = nodes[0].getLookup().lookup(PayaraModule.class);
        if (commonSupport == null || !(commonSupport.getInstance() instanceof PayaraInstance)) {
            return false;
        }
        PayaraInstance server = (PayaraInstance) commonSupport.getInstance();
        String uri = server.getUrl();
        return uri != null && uri.length() > 0
                && !server.isRemote();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenServerHomeDirectoryAction.class, "CTL_OpenServerHomeDirectoryAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
