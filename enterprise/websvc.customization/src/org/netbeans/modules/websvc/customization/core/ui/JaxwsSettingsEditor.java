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
package org.netbeans.modules.websvc.customization.core.ui;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.customization.jaxwssettings.panel.WsimportOptionsPanel;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 */
public class JaxwsSettingsEditor implements WSEditor {

    private Service service;
    private Client client;
    private WsimportOptionsPanel panel;
    private JaxWsModel jaxWsModel;

    JaxwsSettingsEditor(JaxWsModel jaxWsModel) {
        this.jaxWsModel = jaxWsModel;
    }

    public JComponent createWSEditorComponent(Node node) {
        service = node.getLookup().lookup(Service.class);
        client = node.getLookup().lookup(Client.class);
        WsimportOptions wsimportOptions = null;
        String[] jvmArgs = null;
        String jvmOptions = "";
        if (service != null) {
            wsimportOptions = service.getWsImportOptions();
            jvmArgs = service.getJvmArgs();
        } else if (client != null) {
            wsimportOptions = client.getWsImportOptions();
            jvmArgs = client.getJvmArgs();
        }
        if (jvmArgs.length>0) {
            jvmOptions = strJoin(jvmArgs);
        }
        List<WsimportOption> options = new ArrayList<WsimportOption>();
        List<WsimportOption> jaxbOptions = new ArrayList<WsimportOption>();
        if (wsimportOptions != null) {
            WsimportOption[] wsoptions = wsimportOptions.getWsimportOptions();
            for (int i = 0; i < wsoptions.length; i++) {
                WsimportOption wsimportOption = wsoptions[i];
                if (wsimportOption.getJaxbOption() != null && wsimportOption.getJaxbOption()) {
                    jaxbOptions.add(wsimportOption);
                } else {
                    options.add(wsimportOption);
                }
            }
        }
        panel = new WsimportOptionsPanel(options, jaxbOptions, wsimportOptions, jvmOptions);

        return panel;
    }

    public String getTitle() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_TITLE");
    }

    public void save(Node node) {
        try {           
            WsimportOptions wsimportOptions = null;
            if (service != null) {
                wsimportOptions = service.getWsImportOptions();
                if (wsimportOptions == null) {
                    wsimportOptions = service.newWsimportOptions();
                }
                String jvmOptions = getJvmOptions();
                if (jvmOptions.length()>0) {
                    service.setJvmArgs(jvmOptions.split("\\s+"));
                }
            } else if (client != null) {
                wsimportOptions = client.getWsImportOptions();
                if (wsimportOptions == null) {
                    wsimportOptions = client.newWsimportOptions();
                }
                String jvmOptions = getJvmOptions();
                if (jvmOptions.length()>0) {
                    client.setJvmArgs(jvmOptions.split("\\s+"));
                }
            }

            if (wsimportOptions != null) {
                wsimportOptions.clearWsimportOptions();

                List<WsimportOption> options = getWsimportOptions();
                for (WsimportOption option : options) {
                    wsimportOptions.addWsimportOption(option);
                }

                options = getJaxbOptions();
                for (WsimportOption option : options) {
                    option.setJaxbOption(true);
                    wsimportOptions.addWsimportOption(option);
                }
            }
   
            jaxWsModel.write();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public void cancel(Node node) {
    }

    public String getDescription() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_DESC");
    }

    private List<WsimportOption> getWsimportOptions() {
        return panel.getWsimportOptions();
    }

    private List<WsimportOption> getJaxbOptions() {
        return panel.getJaxbOptions();
    }
    
    private String getJvmOptions() {
        return panel.getJvmOptions();
    }
    
    private String strJoin(String[] aArr) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0; i < aArr.length; i++) {
            if (i > 0)
                sbStr.append(' ');
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }
}
