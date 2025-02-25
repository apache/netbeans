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
package org.netbeans.modules.lsp.client.debugger.attach;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger.Type;
import org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.AttachType.Registration;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@Registration(displayName="#DN_DAPAttach")
@Messages({
    "DN_DAPAttach=Debuger Adapter Protocol (DAP) Debugger",
    "DN_Default=Default configuration"
})
public final class DAPAttachType extends AttachType {

    private static final RequestProcessor WORKER = new RequestProcessor(DAPAttachType.class.getName(), 1, false, false);

    private DAPAttachPanel panel;

    @Override
    public JComponent getCustomizer() {
        if (panel == null) {
            panel = new DAPAttachPanel();
            panel.load(getPrivateSettings());
        }

        return panel;
    }

    @Override
    public Controller getController() {
        return new PersistentController() {
            private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

            @Override
            public boolean ok() {
                String hostname = panel.getHostName();
                int port = panel.getPort();
                Type connectionType = panel.getConnectionType();
                String configuration = panel.getJSONConfiguration();
                boolean delay = panel.getDelay();
                WORKER.post(() -> {
                    try {
                        Socket socket = new Socket(hostname, port);
                        DAPConfiguration dapConfig = DAPConfiguration.create(socket.getInputStream(), socket.getOutputStream());
                        if (!configuration.isBlank()) {
                            Map<String, Object> args = new Gson().fromJson(configuration, HashMap.class);
                            dapConfig.addConfiguration(args);
                        }
                        if (delay) {
                            dapConfig.delayLaunch();
                        }
                        switch (connectionType) {
                            case ATTACH -> dapConfig.attach();
                            case LAUNCH -> dapConfig.launch();
                            default -> throw new IllegalStateException("Unknown connection type: " + connectionType);
                        }
                    } catch (IOException | JsonSyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
                return true;
            }

            @Override
            public boolean cancel() {
                return true;
            }

            @Override
            public boolean isValid() {
                return true; //TODO
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener l) {
                pcs.addPropertyChangeListener(l);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener l) {
                pcs.removePropertyChangeListener(l);
            }

            @Override
            public String getDisplayName() {
                return Bundle.DN_Default();
            }

            @Override
            public boolean load(Properties props) {
                panel.load(props);
                return true;
            }

            @Override
            public void save(Properties props) {
                panel.save(props);
                panel.save(getPrivateSettings());
            }
        };
    }

    private static Properties getPrivateSettings() {
        //the debugger does not seem to call "load" on the saved settings, so
        //storing the settings in a private location as well:
        return Properties.getDefault().getProperties("debugger").getProperties("dap_attach_configuration");
    }
}
