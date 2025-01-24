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
package org.netbeans.modules.docker.ui.wizard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerSupport;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

@NbBundle.Messages({
    "MSG_ConnectionPassed=Connection successful.",
    "MSG_CannotConnect=Cannot establish connection.",
    "# {0} - detailed error message",
    "MSG_CannotConnectWithDetails=Cannot establish connection, details: {0}",
    "MSG_InaccessibleSocket=Socket is not accessible."
})
public class DockerConnectionPanel implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptor>, ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(DockerConnectionPanel.class.getName());

    private static final Pattern REMOTE_HOST_PATTERN = Pattern.compile("^(tcp://)[^/:](:\\d+)($|/.*)"); // NOI18N

    private static final String CONNECTION_TEST = "connection_test";
    private static final String CONNECTION_EXCEPTION_MSG = "connection_exception_msg";

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final Map<String, Object> values = new HashMap<>();

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DockerConnectionVisual component;

    private WizardDescriptor wizard;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DockerConnectionVisual getComponent() {
        if (component == null) {
            component = new DockerConnectionVisual(this);
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("docker_registering_connection"); // NOI18N
    }

    @NbBundle.Messages({
        "MSG_EmptyDisplayName=Display name must not be empty.",
        "MSG_AlreadyUsedDisplayName=Display name is already used by another instance.",
        "MSG_EmptySocket=Unix socket must not be empty.",
        "MSG_EmptyUrl=URL must not be empty.",
        "MSG_InvalidUrl=URL must be valid http or https URL.",
        "MSG_NonExistingCertificatePath=The certificates path does not exist.",
        "# {0} - missing file",
        "MSG_CertificatePathMissingFile=The certificates path does not contain {0}.",
        "MSG_NoCertificatesForSecure=No certificates configured for secured connection."
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        // process the connection test validation
        Boolean connectioTest = (Boolean) wizard.getProperty(CONNECTION_TEST);
        wizard.putProperty(CONNECTION_TEST, null);
        String connectionExceptionMessage = (String) wizard.getProperty(CONNECTION_EXCEPTION_MSG);
        wizard.putProperty(CONNECTION_EXCEPTION_MSG, null);

        Configuration panel = component.getConfiguration();
        String displayName = panel.getDisplayName();
        if (displayName == null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyDisplayName());
            return false;
        }
        for (DockerInstance instance : DockerSupport.getDefault().getInstances()) {
            if (displayName.equals(instance.getDisplayName())) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_AlreadyUsedDisplayName());
                return false;
            }
        }

        if (panel.isSocketSelected()) {
            File socket = panel.getSocket();
            if (socket == null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptySocket());
                return false;
            }
            if (!socket.exists() || !socket.canRead() || !socket.canWrite()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_InaccessibleSocket());
                return false;
            }
        } else {
            String url = panel.getUrl();
            if (url == null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyUrl());
                return false;
            }

            URL realUrl = null;
            boolean urlWrong = false;
            try {
                realUrl = new URL(url);
                if (!"http".equals(realUrl.getProtocol()) // NOI18N
                        && !"https".equals(realUrl.getProtocol())) { // NOI18N
                    urlWrong = true;
                }
                int port = realUrl.getPort();
                if (port > 65535) {
                    urlWrong = true;
                }
            } catch (MalformedURLException ex) {
                urlWrong = true;
            }
            if (urlWrong) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_InvalidUrl());
                return false;
            }

            String certPath = panel.getCertPath();
            if (certPath != null) {
                File certPathFile = new File(certPath);
                if (!certPathFile.isDirectory()) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonExistingCertificatePath());
                    return false;
                }
                if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_CA_FILE).isFile()) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_CA_FILE));
                    return false;
                }
                if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_CERT_FILE).isFile()) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_CERT_FILE));
                    return false;
                }
                if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_KEY_FILE).isFile()) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_KEY_FILE));
                    return false;
                }
            }

            if (realUrl != null && "https".equals(realUrl.getProtocol()) && certPath == null) { // NOI18N
                wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, Bundle.MSG_NoCertificatesForSecure());
            }
        }

        if (connectioTest != null) {
            if (connectioTest) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, Bundle.MSG_ConnectionPassed());
            } else if (connectionExceptionMessage != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, connectionExceptionMessage);
            } else {
                //fallback to generic message
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_CannotConnect());
            }
            return connectioTest;
        }

        return true;
    }

    @Override
    public void prepareValidation() {
        synchronized (values) {
            values.clear();
            Configuration panel = component.getConfiguration();
            values.put(AddDockerInstanceWizard.DISPLAY_NAME_PROPERTY, panel.getDisplayName());
            values.put(AddDockerInstanceWizard.SOCKET_SELECTED_PROPERTY, panel.isSocketSelected());
            values.put(AddDockerInstanceWizard.SOCKET_PROPERTY, panel.getSocket());
            values.put(AddDockerInstanceWizard.URL_PROPERTY, panel.getUrl());
            values.put(AddDockerInstanceWizard.CERTIFICATE_PATH_PROPERTY, panel.getCertPath());
        }
        component.setWaitingState(true);
    }

    @Override
    public void finishValidation() {
        component.setWaitingState(false);
    }

    @Override
    public void validate() throws WizardValidationException {
        try {
            DockerInstance instance;
            synchronized (values) {
                boolean socketSelected = (Boolean) values.get(AddDockerInstanceWizard.SOCKET_SELECTED_PROPERTY);
                if (socketSelected) {
                    File socket = (File) values.get(AddDockerInstanceWizard.SOCKET_PROPERTY);
                    // this is repeated here as the acessibility might have change since the last check
                    if (!socket.exists() || !socket.canRead() || !socket.canWrite()) {
                        String error = Bundle.MSG_InaccessibleSocket();
                        throw new WizardValidationException(component, error, error);
                    }
                    instance = DockerInstance.getInstance(Utilities.toURI(socket).toURL().toString(),
                            null, null, null, null);
                } else {
                    File caFile = null;
                    File certFile = null;
                    File keyFile = null;

                    String strCertPath = (String) values.get(AddDockerInstanceWizard.CERTIFICATE_PATH_PROPERTY);
                    if (strCertPath != null) {
                        File file = new File(strCertPath);
                        caFile = new File(file, AddDockerInstanceWizard.DEFAULT_CA_FILE);
                        certFile = new File(file, AddDockerInstanceWizard.DEFAULT_CERT_FILE);
                        keyFile = new File(file, AddDockerInstanceWizard.DEFAULT_KEY_FILE);
                    }
                    instance = DockerInstance.getInstance((String) values.get(AddDockerInstanceWizard.URL_PROPERTY),
                            null, caFile, certFile, keyFile);
                }
            }

            DockerAction action = new DockerAction(instance);
            boolean pingResult = false;
            
            try {
                pingResult = action.pingWithExceptions();
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "docker connection ping failed", ex); // NOI18N
                throw new WizardValidationException(component, 
                        Bundle.MSG_CannotConnectWithDetails(ex.getMessage()), 
                        Bundle.MSG_CannotConnectWithDetails(ex.getLocalizedMessage()));
            }
            
            if (!pingResult) {
                String error = Bundle.MSG_CannotConnect();
                throw new WizardValidationException(component, error, error);
            }
        // runtime exception may happen
        } catch (Exception ex) {
            throw new WizardValidationException(component, ex.getMessage(), ex.getLocalizedMessage());
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @NbBundle.Messages({
        "LBL_DefaultDisplayName=Local Docker"
    })
    @Override
    public void readSettings(WizardDescriptor wiz) {
        boolean init = false;
        if (wizard == null) {
            init = true;
            wizard = wiz;
        }

        Configuration panel = component.getConfiguration();
        String displayName = (String) wiz.getProperty(AddDockerInstanceWizard.DISPLAY_NAME_PROPERTY);
        if (displayName == null && init) {
            displayName = Bundle.LBL_DefaultDisplayName();
        }
        panel.setDisplayName(displayName);

        Boolean socketSelected = (Boolean) wiz.getProperty(AddDockerInstanceWizard.SOCKET_SELECTED_PROPERTY);
        if (socketSelected == null) {
            socketSelected = DockerSupport.getDefault().isSocketSupported();
        }
        panel.setSocketSelected(socketSelected);

        File socket = (File) wiz.getProperty(AddDockerInstanceWizard.SOCKET_PROPERTY);
        if (socket == null && init && socketSelected) {
            socket = getDefaultSocket();
        }
        panel.setSocket(socket);

        String url = (String) wiz.getProperty(AddDockerInstanceWizard.URL_PROPERTY);
        if (url == null && init && !socketSelected) {
            url = getDefaultUrl();
        }
        panel.setUrl(url);

        String certPath = (String) wiz.getProperty(AddDockerInstanceWizard.CERTIFICATE_PATH_PROPERTY);
        if (certPath == null && init && !socketSelected) {
            certPath = getDefaultCertificatePath();
        }
        panel.setCertPath(certPath);

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        Configuration panel = component.getConfiguration();
        wiz.putProperty(AddDockerInstanceWizard.DISPLAY_NAME_PROPERTY, panel.getDisplayName());
        wiz.putProperty(AddDockerInstanceWizard.SOCKET_SELECTED_PROPERTY, panel.isSocketSelected());
        wiz.putProperty(AddDockerInstanceWizard.SOCKET_PROPERTY, panel.getSocket());
        wiz.putProperty(AddDockerInstanceWizard.URL_PROPERTY, panel.getUrl());
        wiz.putProperty(AddDockerInstanceWizard.CERTIFICATE_PATH_PROPERTY, panel.getCertPath());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public void testConnection() {
        assert SwingUtilities.isEventDispatchThread();

        prepareValidation();
        final AtomicReference<Exception> ref = new AtomicReference<>();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    validate();
                } catch (WizardValidationException ex) {
                    ref.set(ex);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            finishValidation();
                            Exception ex = ref.get();
                            if (ex != null) {                                                  
                                wizard.putProperty(CONNECTION_EXCEPTION_MSG, ex.getLocalizedMessage());
                                wizard.putProperty(CONNECTION_TEST, false);
                            } else {
                                wizard.putProperty(CONNECTION_TEST, true);
                            }
                            changeSupport.fireChange();
                        }
                    });
                }
            }
        });
    }

    private static File getDefaultSocket() {
        File file = new File("/var/run/docker.sock"); // NOI18N
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private static String getDefaultUrl() {
        String url = null;
        String envUrl = System.getenv("DOCKER_HOST"); // NOI18N
        if (envUrl != null) {
            envUrl = envUrl.trim();
            try {
                return new URL(envUrl).toString();
            } catch (MalformedURLException ex) {
                // try to parse it
            }
            Matcher m = REMOTE_HOST_PATTERN.matcher(envUrl);
            if (m.matches()) {
                boolean https = false;
                String tlsEnv = System.getenv("DOCKER_TLS_VERIFY"); // NOI18N
                if (tlsEnv != null && "1".equals(tlsEnv)) { // NOI18N
                    https = true;
                } else {
                    https = Integer.parseInt(m.group(2)) == 2376;
                }
                return (https ? "https://" : "http://") + envUrl.substring(m.group(1).length()); // NOI18N
            }
        }

        if (url == null) {
            if (Utilities.isMac() || Utilities.isWindows()) {
                if (Utilities.isWindows()) {
                    String appData = System.getenv("APPDATA"); // NOI18N
                    // docker beta detection
                    if (appData != null && new File(appData, "Docker" + File.separatorChar + ".trackid").isFile()) { // NOI18N
                        url = "http://127.0.0.1:2375"; // NOI18N
                    }
                } else if (Utilities.isMac()) {
                    // FIXME beta detection
                }
                if (url == null) {
                    if (new File(System.getProperty("user.home"), ".docker").isDirectory()) { // NOI18N
                        // dockertoolbox
                        url = "https://192.168.99.100:2376"; // NOI18N
                    } else {
                        // obsolete boot2docker
                        url = "https://192.168.59.103:2376"; // NOI18N
                    }
                }
            } else {
                url = "http://127.0.0.1:2375"; // NOI18N
            }
        }
        return url;
    }

    private static String getDefaultCertificatePath() {
        String certPath = null;
        String envPath = System.getenv("DOCKER_CERT_PATH"); // NOI18N
        if (envPath != null) {
            envPath = envPath.trim();
            if (new File(envPath).isDirectory()) {
                return envPath;
            }
        }

        if (Utilities.isMac() || Utilities.isWindows()) {
            // dockertoolbox
            File folder = new File(System.getProperty("user.home"), ".docker" + File.separator // NOI18N
                    + "machine" + File.separator + "machines" + File.separator + "default"); // NOI18N
            if (!folder.isDirectory()) {
                // obsolete boot2docker
                folder = new File(System.getProperty("user.home"), ".boot2docker" + File.separator // NOI18N
                        + "certs" + File.separator + "boot2docker-vm"); // NOI18N
            }
            if (folder.isDirectory()) {
                certPath = folder.getAbsolutePath();
            }
        }
        return certPath;
    }
}
