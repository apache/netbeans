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

package org.netbeans.modules.payara.common.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.PortCollection;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ludo
 * @author vince
 */
public class AddServerLocationPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE;
    private final String PROP_WARNING_MESSAGE = WizardDescriptor.PROP_WARNING_MESSAGE;
    private final String PROP_INFO_MESSAGE = WizardDescriptor.PROP_INFO_MESSAGE;

    private ServerWizardIterator wizardIterator;
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient List<ChangeListener> listeners
            = new CopyOnWriteArrayList<>();
    
    /**
     * 
     * @param instantiatingIterator 
     */
    public AddServerLocationPanel(ServerWizardIterator wizardIterator){
        this.wizardIterator = wizardIterator;
        wizard = null;
    }
    
    /**
     * 
     * @param ev 
     */
    @Override
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel(wizardIterator);
            component.addChangeListener(this);
        }
        return component;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public HelpCtx getHelp() {
        FileObject fo = FileUtil.getConfigFile("Services/JavaHelp/org-netbeans-modules-usersguide-helpset.xml");
        if (null != fo)
            return new HelpCtx("registering_app_server_hk2_location"); //NOI18N
        else
            return null;
    }

    private AtomicBoolean isValidating = new AtomicBoolean();
    
    /**
     * 
     * @return 
     */
    @Override
    public boolean isValid() {
        if (isValidating.compareAndSet(false, true)) {
            try {
                wizardIterator.setHttpPort(-1);
                AddServerLocationVisualPanel panel = (AddServerLocationVisualPanel) getComponent();

                AddServerLocationVisualPanel.DownloadState downloadState = panel.getDownloadState();
                if (downloadState == AddServerLocationVisualPanel.DownloadState.DOWNLOADING) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, panel.getStatusText());
                    return false;
                }

                String locationStr = panel.getHk2HomeLocation();
                locationStr = (locationStr != null) ? locationStr.trim() : null;
                if (locationStr == null || locationStr.length() == 0) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_BlankInstallDir"));
                    return false;
                }

                // !PW Replace some or all of this with a single call to a validate method
                // that throws an exception with a precise reason for validation failure.
                // e.g. domain dir not found, domain.xml corrupt, no ports defined, etc.
                //
                File installDir = new File(locationStr).getAbsoluteFile();
                File payaraDir = getPayaraRoot(installDir);
                File domainDir = getDefaultDomain(payaraDir);
                if (!installDir.exists()) {
                    if (!isLegalFolder(installDir)) {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_InstallDirInvalid", locationStr));
                        return false;
                    } else if (canCreate(installDir)) {
                        if (downloadState == AddServerLocationVisualPanel.DownloadState.AVAILABLE) {
                            panel.updateMessageText(NbBundle.getMessage(AddServerLocationPanel.class,
                                    "LBL_NewInstallDirCanBeUsed", getSanitizedPath(installDir)));  // NOI18N
                            wizard.putProperty(PROP_ERROR_MESSAGE, panel.getStatusText());
                            return false;
                        } else {
                            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                    AddServerLocationPanel.class, "ERR_InstallDirDoesNotExist", getSanitizedPath(installDir)));
                            return false;
                        }
                    } else {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_CannotCreate", getSanitizedPath(installDir)));
                        return false;
                    }
                } else {
                    Object candidate = wizardIterator.isValidInstall(installDir, payaraDir, wizard);
                    if (null == candidate) {
                        String errMsg = NbBundle.getMessage(AddServerLocationPanel.class, "ERR_InstallationInvalid", // NOI18N
                                FileUtil.normalizeFile(installDir).getPath());
                        wizard.putProperty(PROP_ERROR_MESSAGE, errMsg);
                        return false;
                    } else if (!isRegisterableDomain(domainDir)) {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_DefaultDomainInvalid", getSanitizedPath(installDir)));
                    } else {
                        org.netbeans.modules.payara.common.utils.Util.readServerConfiguration(domainDir, wizardIterator);
                        // finish initializing the registration data
                        if (installDir.equals(payaraDir)) {
                            installDir = payaraDir.getParentFile();
                        }
                        wizardIterator.setInstallRoot(installDir.getAbsolutePath());
                        wizardIterator.setPayaraRoot(payaraDir.getAbsolutePath());
                        String uri = wizardIterator.formatUri(PayaraInstance.DEFAULT_HOST_NAME, 
                                wizardIterator.getAdminPort(), wizardIterator.getTargetValue(),
                                domainDir.getParentFile().getAbsolutePath(), domainDir.getName());
                        if (-1 == wizardIterator.getHttpPort()) {
                            wizard.putProperty(PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainDir.getName())); // NOI18N
                            return false;
                        }
                        if (-1 == wizardIterator.getAdminPort()) {
                            wizard.putProperty(PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainDir.getName())); // NOI18N
                            return false;
                        }
                        if (wizardIterator.hasServer(uri)) {
                            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(AddServerLocationPanel.class, "MSG_DefaultDomainExists",
                                    getSanitizedPath(installDir), PayaraInstance.DEFAULT_DOMAIN_NAME));
                            wizardIterator.setHttpPort(-1); // FIXME this is a hack - disables finish button
                        } else {
                            String statusText = panel.getStatusText();
                            if (statusText != null && statusText.length() > 0) {
                                wizard.putProperty(PROP_ERROR_MESSAGE, statusText);
                                return false;
                            } else {
                                wizard.putProperty(PROP_ERROR_MESSAGE, null);
                                wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(
                                        AddServerLocationPanel.class, "MSG_NextForSpecial", candidate)); // NOI18N
                            }
                        }
                    }
                }
                // message has already been set, do not clear it here (see above).

                // finish initializing the registration data
                if (installDir.equals(payaraDir)) {
                    installDir = payaraDir.getParentFile();
                }
                wizardIterator.setInstallRoot(installDir.getAbsolutePath());
                wizardIterator.setPayaraRoot(payaraDir.getAbsolutePath());
                wizardIterator.setDomainLocation(domainDir.getAbsolutePath());

                return true;
            } finally {
                isValidating.set(false);
            }
        }
        return false;
    }

    private static String getSanitizedPath(File dir) {
        return FileUtil.normalizeFile(dir).getPath();
    }

    // These characters ( ? * : | < > " ) are illegal on Windows (NTFS).
    // The first four are detected by getCanonicalFile(), but the last 3 are not
    // so check for them specifically.
    private static Pattern ILLEGAL_WINDOWS_CHARS = Pattern.compile("<|>|\\\"");

    private static boolean isLegalFolder(File installDir) {
        return getCanonicalFile(installDir) != null &&
                (!Utilities.isWindows() || ILLEGAL_WINDOWS_CHARS.matcher(installDir.getPath()).find() == false);
    }

    private static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            return null;
        }
    }

    static boolean canCreate(File dir) {
        if (dir.exists()) {
            return false;
        }
        while(dir != null && !dir.exists()) {
            dir = dir.getParentFile();
        }
        return dir != null ? dir.canRead() && Utils.canWrite(dir) : false;
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param settings 
     */
    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }
    
    /**
     * 
     * @param settings 
     */
    @Override
    public void storeSettings(Object settings) {
    }

    /**
     * Domain attributes should be checked before finishing this wizard.
     * <p/>
     * @return Always returns <code>false</code>.
     */
    @Override
    public boolean isFinishPanel() {
        return false;
    }
    
    /**
     * Validates if <code>domainDir</code> contains valid Payara domain.
     * <p/>
     * @param domainDir      Payara domain directory to be validated.
     * @param portCollection Information from <code>domain.xml</code>
     *                       configuration file is stored here when
     *                       <code>domainDir</code> contains valid Payara
     *                       domain.
     * @return Value of <code>true</code> when <code>domainDir</code> contains
     *         valid Payara domain or <code>false</code> otherwise.
     */
    static boolean isRegisterableDomain(final File domainDir,
            final PortCollection portCollection) {
        File testFile = new File(domainDir, "logs"); // NOI18N
        if (!testFile.exists()) {
            testFile = domainDir;
        }
        return Utils.canWrite(testFile) &&
                org.netbeans.modules.payara.common.utils.Util
                .readServerConfiguration(domainDir, portCollection);
    }

    /**
     * Validates if <code>domainDir</code> contains valid Payara domain.
     * <p/>
     * @param domainDir Payara domain directory to be validated.
     * @return Value of <code>true</code> when <code>domainDir</code> contains
     *         valid Payara domain or <code>false</code> otherwise.
     */
    static boolean isRegisterableDomain(File domainDir) {
        return isRegisterableDomain(domainDir, null);
    }
    
    private File getPayaraRoot(File installDir) {
        File glassfishDir = new File(installDir, "glassfish"); // ${payara}/glassfish
        if(!glassfishDir.exists()) {
            glassfishDir = installDir;
        }
        return glassfishDir;
    }
    
    private File getDefaultDomain(File payaraDir) {
        File retVal = new File(payaraDir, PayaraInstance.DEFAULT_DOMAINS_FOLDER + 
                File.separator + PayaraInstance.DEFAULT_DOMAIN_NAME); // NOI18N
        if (!isRegisterableDomain(retVal)) {
            // see if there is some other domain that will work.
            File domainsDir = new File(payaraDir, PayaraInstance.DEFAULT_DOMAINS_FOLDER); // NOI18N
            File candidates[] = domainsDir.listFiles();
            if (null != candidates && candidates.length > 0) {
                // try to pick a candidate
                for (File c : candidates) {
                    if (isRegisterableDomain(c)) {
                        retVal = c;
                        break;
                    }
                }
            }
        }
        return retVal;
    }
    
}
