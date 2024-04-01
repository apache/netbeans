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
package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka 
 */
public abstract class SvnClientCallback implements ISVNPromptUserPassword {
    
    private final SVNUrl url;
    private final int handledExceptions;
    
    private String username = null;
    private char[] password = null;

    private String certFilePath;
    private char[] certPassword;
    private int sshPort = 22;
    
    private static final Logger LOG = Logger.getLogger("versioning.subversion.passwordCallback"); //NOI18N
    protected static final boolean PRINT_PASSWORDS = "true".equals(System.getProperty("versioning.subversion.logpassword", "false")); //NOI18N
    
    /** Creates a new instance of SvnClientCallback */
    public SvnClientCallback(SVNUrl url, int handledExceptions) {
        this.url = url;
        this.handledExceptions = handledExceptions;
    }

    @Override
    public String getUsername() {
        getAuthData();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getUsername: {0}", username); //NOI18N
        }
        return username;
    }

    @Override
    public String getPassword() {
        getAuthData();
        String retval = ""; //NOI18N
        if (password != null) {
            retval = new String(password);
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "getPassword: {0}", retval == null ? "null" : retval.isEmpty() ? "empty" : "non-empty"); //NOI18N
            if (PRINT_PASSWORDS) {
                LOG.log(Level.FINEST, "getPassword: returning {0}", retval); //NOI18N
            }
        }
        return retval;
    }

    @Override
    public int askTrustSSLServer(String certMessage, boolean allowPermanently) {
        
        if((SvnClientExceptionHandler.EX_NO_CERTIFICATE & handledExceptions) != SvnClientExceptionHandler.EX_NO_CERTIFICATE) {
            return -1; // XXX test me
        }
        
        AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
        acceptCertificatePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed"));
        acceptCertificatePanel.certificatePane.setText(certMessage);
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(acceptCertificatePanel, org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "CTL_Error_CertFailed")); // NOI18N        
        
        JButton permanentlyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptPermanently")); // NOI18N
        JButton temporarilyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptTemp")); // NOI18N
        JButton rejectButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_Reject")); // NOI18N
        
        dialogDescriptor.setOptions(new Object[] { permanentlyButton, temporarilyButton, rejectButton }); 
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.subversion.serverCertificateVerification"));

        showDialog(dialogDescriptor);

        if(dialogDescriptor.getValue() == permanentlyButton) {
            return ISVNPromptUserPassword.AcceptPermanently;
        } else if(dialogDescriptor.getValue() == temporarilyButton) {
            return ISVNPromptUserPassword.AcceptTemporary;
        } else {
            return ISVNPromptUserPassword.Reject;
        }
    }

    @Override
    public boolean userAllowedSave() {
        return false;
    }

    @Override
    public String getSSHPrivateKeyPath() {
        getAuthData();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getSSHPrivateKeyPath: {0}", certFilePath); //NOI18N
        }
        return certFilePath;
    }

    @Override
    public String getSSHPrivateKeyPassphrase() {
        String passphrase = getCertPassword();
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "getSSHPrivateKeyPassphrase: {0}", passphrase == null ? "null" : passphrase.isEmpty() ? "empty" : "non-empty"); //NOI18N
            if (PRINT_PASSWORDS) {
                LOG.log(Level.FINEST, "getSSHPrivateKeyPassphrase: returning {0}", passphrase); //NOI18N
            }
        }
        return passphrase;
    }

    @Override
    public int getSSHPort() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getSSHPort: {0}", sshPort); //NOI18N
        }
        return sshPort;
    }

    @Override
    public String getSSLClientCertPassword() {
        String pwd = getCertPassword();
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "getSSLCertPwd: {0}", pwd == null ? "null" : pwd.isEmpty() ? "empty" : "non-empty"); //NOI18N
            if (PRINT_PASSWORDS) {
                LOG.log(Level.FINEST, "getSSLCertPwd: returning {0}", pwd); //NOI18N
            }
        }
        return pwd;
    }

    @Override
    public String getSSLClientCertPath() {
        getAuthData();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getSSLClientCertPath: {0}", certFilePath); //NOI18N
        }
        return certFilePath;
    }

    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }    
    
    private void getAuthData() {        
        RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(url.toString());
        if (rc != null) {
            username = rc.getUsername();
            password = rc.getPassword();
            certFilePath = rc.getCertFile();
            certPassword = rc.getCertPassword();
            sshPort = rc.getSshPortNumber();
            if (sshPort <= 0) {
                sshPort = 22;
            }
        }
    }

    private String getCertPassword() {
        getAuthData();
        String certPwd = ""; //NOI18N
        if (certPassword != null) {
            certPwd = new String(certPassword);
            Arrays.fill(certPassword, '\0');
        }
        return certPwd;
    }

}
