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
package org.netbeans.modules.docker.ui.wizard;

import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerSupport;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class AddDockerInstanceWizard {

    public static final String DISPLAY_NAME_PROPERTY = "displayName";

    public static final String SOCKET_SELECTED_PROPERTY = "socketSelected";

    public static final String SOCKET_PROPERTY = "socket";

    public static final String URL_PROPERTY = "url";

    public static final String CERTIFICATE_PATH_PROPERTY = "certPath";

    public static final String DEFAULT_CA_FILE = "ca.pem";

    public static final String DEFAULT_CERT_FILE = "cert.pem";

    public static final String DEFAULT_KEY_FILE = "key.pem";

    private static final Logger LOGGER = Logger.getLogger(AddDockerInstanceWizard.class.getName());

    @NbBundle.Messages("LBL_AddDockerInstance=Add Docker Instance")
    public DockerInstance show() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new DockerConnectionPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            c.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.LBL_AddDockerInstance());
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {

            Boolean socketSelected = (Boolean) wiz.getProperty(SOCKET_SELECTED_PROPERTY);
            if (socketSelected) {
                File file = (File) wiz.getProperty(SOCKET_PROPERTY);
                try {
                    DockerInstance instance = DockerInstance.getInstance(
                            Utilities.toURI(file).toURL().toString(),
                            (String) wiz.getProperty(DISPLAY_NAME_PROPERTY),
                            null, null, null);
                    return DockerSupport.getDefault().addInstance(instance);
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            } else {
                File caFile = null;
                File certFile = null;
                File keyFile = null;

                String strCertPath = (String) wiz.getProperty(CERTIFICATE_PATH_PROPERTY);
                if (strCertPath != null) {
                    File file = new File(strCertPath);
                    caFile = new File(file, DEFAULT_CA_FILE);
                    certFile = new File(file, DEFAULT_CERT_FILE);
                    keyFile = new File(file, DEFAULT_KEY_FILE);
                }

                DockerInstance instance = DockerInstance.getInstance(
                        (String) wiz.getProperty(URL_PROPERTY),
                        (String) wiz.getProperty(DISPLAY_NAME_PROPERTY),
                        caFile, certFile, keyFile);
                return DockerSupport.getDefault().addInstance(instance);
            }
        }
        return null;
    }
}
