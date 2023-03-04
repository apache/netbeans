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
package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.security.KeyManagementException;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.FILE;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.HTTP;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.HTTPS;
import static org.netbeans.modules.mercurial.ui.repository.Repository.FLAG_SHOW_HINTS;
import static org.netbeans.modules.mercurial.ui.repository.Repository.FLAG_SHOW_PROXY;
import static org.netbeans.modules.mercurial.ui.repository.Repository.FLAG_URL_ENABLED;

public class CloneRepositoryWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel, ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private JComponent component;
    private Repository repository;
    private boolean valid;
    private String errorMessage;
    private WizardStepProgressSupport support;

    public CloneRepositoryWizardPanel() {
        support = new RepositoryStepProgressSupport();
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {

            repository = new Repository(
                    FLAG_URL_ENABLED | FLAG_SHOW_HINTS | FLAG_SHOW_PROXY,
                    getMessage("CTL_Repository_Location"),
                    false);
            repository.addChangeListener(this);

            support = new RepositoryStepProgressSupport();

            component = new JPanel(new BorderLayout());
            component.add(repository.getPanel(), BorderLayout.CENTER);
            component.add(support.getProgressComponent(), BorderLayout.SOUTH);

            component.setName(getMessage("repositoryPanel.Name"));       //NOI18N

            valid();
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(CloneRepositoryWizardPanel.class);
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(CloneRepositoryWizardPanel.class, msgKey);
    }
    
    //public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
    //    return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    //}
    
    public void stateChanged(ChangeEvent evt) {
        if(repository.isValid()) {
            valid(repository.getMessage());
        } else {
            invalid(repository.getMessage());
        }
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    protected final void valid() {
        setValid(true, null);
    }

    protected final void valid(String extErrorMessage) {
        setValid(true, extErrorMessage);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    private void displayErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            throw new IllegalArgumentException("<null> message");
        }

        if (!errorMessage.equals(this.errorMessage)) {
            this.errorMessage = errorMessage;
            fireChangeEvent();
        }
    }

    private void setValid(boolean valid, String errorMessage) {
        if ((errorMessage != null) && (errorMessage.length() == 0)) {
            errorMessage = null;
        }
        boolean fire = this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        this.valid = valid;
        this.errorMessage = errorMessage;
        if (fire) {
            fireChangeEvent();
        }
    }

    protected void validateBeforeNext() throws WizardValidationException {
        try {
            HgURL url;
            try {
                url = repository.getUrl();
            } catch (URISyntaxException ex) {
                throw new WizardValidationException((JComponent) component,
                                                    ex.getMessage(),
                                                    ex.getLocalizedMessage());
            }

            if (support == null) {
                support = new RepositoryStepProgressSupport();
                component.add(support.getProgressComponent(), BorderLayout.SOUTH);
            }
            support.setRepositoryRoot(url);
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, NbBundle.getMessage(CloneRepositoryWizardPanel.class, "BK2012"));
            task.waitFinished();
        } finally {
            if (support != null) {      //see bug #167172
                /*
                 * We cannot reuse the progress component because
                 * org.netbeans.api.progress.ProgressHandle cannot be reused.
                 */
                component.remove(support.getProgressComponent());
                support = null;
            }
        }

    }

    // comes on next or finish
    public final void validate () throws WizardValidationException {
        try {
        validateBeforeNext();
        if (isValid() == false || errorMessage != null) {
            throw new WizardValidationException (
                (javax.swing.JComponent) component,
                errorMessage,
                errorMessage
            );
        }
        } catch (WizardValidationException ex) {
            EventQueue.invokeLater(new Runnable () {
                public void run() {
                    repository.setEditable(true);
                }
            });
            throw ex;
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {}
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            try {
                ((WizardDescriptor) settings).putProperty("repository", repository.getUrl()); // NOI18N
            } catch (URISyntaxException ex) {
                /*
                 * The panel's data may not be validated yet (bug #163078)
                 * so we cannot assume that the entered URL is valid - so
                 * we must catch the URISyntaxException.
                 */
                Logger.getLogger(getClass().getName()).throwing(
                                                        getClass().getName(),
                                                        "storeSettings",//NOI18N
                                                        ex);
            }
        }
    }

    public void prepareValidation() {
        errorMessage = null;
        
        repository.setEditable(false);
    }

    private void storeHistory() {
        RepositoryConnection rc = getRepositoryConnection();
        if(rc != null) {
            HgModuleConfig.getDefault().insertRecentUrl(rc);
        }
    }

    private RepositoryConnection getRepositoryConnection() {
        try {
            return repository.getRepositoryConnection();
        } catch (Exception ex) {
            displayErrorMessage(ex.getLocalizedMessage());
            return null;
        }
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {

        public RepositoryStepProgressSupport() {
            super();
        }

        public void perform() {
            final RepositoryConnection rc = getRepositoryConnection();
            if (rc == null) {
                return;
            }
            String invalidMsg = null;
            HttpURLConnection con = null;
            try {
                HgURL hgUrl = getRepositoryRoot();

                HgURL.Scheme uriSch = hgUrl.getScheme();
                if (uriSch == FILE) {
                    File f = HgURL.getFile(hgUrl);
                    if(!f.exists() || !f.canRead()){
                        invalidMsg = getMessage("MSG_Progress_Clone_CannotAccess_Err"); //NOI18N
                        return;
                    }
                } else if ((uriSch == HTTP) || (uriSch == HTTPS)) {
                    URL url = hgUrl.toURL();
                    con = (HttpURLConnection) url.openConnection();
                    // Note: valid repository returns con.getContentLength() = -1
                    // so no way to reliably test if this url exists, without using hg
                    if (con != null) {
                        String userInfo = url.getUserInfo();
                        boolean bNoUserAndOrPasswordInURL = userInfo == null;
                        // If username or username:password is in the URL the con.getResponseCode() returns -1 and this check would fail
                        if (uriSch == HTTPS) {
                            setupHttpsConnection(con);
                        }
                        if (bNoUserAndOrPasswordInURL && con.getResponseCode() != HttpURLConnection.HTTP_OK){
                            invalidMsg = getMessage("MSG_Progress_Clone_CannotAccess_Err"); //NOI18N
                            con.disconnect();
                            return;
                        }else if (userInfo != null){
                            Mercurial.LOG.log(Level.FINE, 
                                "RepositoryStepProgressSupport.perform(): UserInfo - {0}", new Object[]{userInfo}); // NOI18N
                        }
                    }
                 }
            } catch (java.lang.IllegalArgumentException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
                 invalidMsg = getMessage("MSG_Progress_Clone_InvalidURL_Err"); //NOI18N
                 return;
            } catch (IOException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
                 invalidMsg = getMessage("MSG_Progress_Clone_CannotAccess_Err"); //NOI18N
                return;
            } catch (RuntimeException re) {
                Throwable t = re.getCause();
                if(t != null) {
                    invalidMsg = t.getLocalizedMessage();
                } else {
                    invalidMsg = re.getLocalizedMessage();
                }
                Mercurial.LOG.log(Level.INFO, invalidMsg, re);
                return;
            } finally {
                if(con != null) {
                    con.disconnect();
                }
                if(isCanceled()) {
                  displayErrorMessage(getMessage("CTL_Repository_Canceled")); //NOI18N
                } else if(invalidMsg == null) {
                  storeHistory();
                } else {
                  displayErrorMessage(invalidMsg);
                }
            }
        }

        public void setEditable(boolean editable) {
            repository.setEditable(editable);
        }

        private void setupHttpsConnection(HttpURLConnection con) {
            X509TrustManager tm = new X509TrustManager() {
                 public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                     // do nothing
                 }
                 public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                     // do nothing
                 }
                 public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                 }
            };
            HostnameVerifier hnv = new HostnameVerifier() {
                 public boolean verify(String hostname, SSLSession session) {
                    return true;
                 }
            };
            try {
                SSLContext context = SSLContext.getInstance("SSLv3");
                TrustManager[] trustManagerArray = { tm };
                context.init(null, trustManagerArray, null);
                HttpsURLConnection c = (HttpsURLConnection) con;
                c.setSSLSocketFactory(context.getSocketFactory());
                c.setHostnameVerifier(hnv);
            } catch (KeyManagementException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
            } catch (NoSuchAlgorithmException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
            }
        }
    };

}

