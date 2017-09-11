/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.subversion.kenai.SvnKenaiAccessor;
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

    private void getKenaiAuthData(SvnKenaiAccessor support) {
        final String urlString = url.toString();
        
        PasswordAuthentication pa = support.getPasswordAuthentication(urlString, true);
        if(pa == null) {
            throw new RuntimeException(new InterruptedException(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ActionCanceledByUser"))); //NOI18N
        }
        String user = pa.getUserName();
        char[] psswd = pa.getPassword();

        username = user != null ? user : "";
        password = psswd;
    }

    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }    
    
    private void getAuthData() {        
        SvnKenaiAccessor support = SvnKenaiAccessor.getInstance();
        if(support != null && support.isKenai(url.toString())) {
            getKenaiAuthData(support);
        } else {
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
