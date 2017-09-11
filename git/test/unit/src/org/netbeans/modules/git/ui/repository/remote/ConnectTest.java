/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.repository.remote;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public class ConnectTest extends AbstractGitTestCase {
    private GitClient client;
    private static final String URL = "http://bugtracking-test.cz.oracle.com/git/repo";
    private static final String RECENT_GURI = "recent_guri";
    private static final String DELIMITER               = "<=~=>";              // NOI18N
    private static final String GURI_PASSWORD           = "guri_password";
    private Preferences prefs;
    
    public ConnectTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(repositoryLocation.getParentFile(), "home").getAbsolutePath());
        client = Git.getInstance().getClient(repositoryLocation, null, false);
        prefs = NbPreferences.forModule(GitModuleConfig.class);
        prefs.clear();
        GitModuleConfig.getDefault().removeConnectionSettings(new GitURI(URL));
    }

    public void testConnectNoCredentials () throws Exception {
        try {
            client.listRemoteBranches(URL, GitUtils.NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            // OK
        }
    }
    
    public void testConnectDoNotStoreCredentials () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        UserPasswordPanel p = getPanel(panel);
        p.userTextField.setText("user");
        p.userPasswordField.setText("heslo");
        p.savePasswordCheckBox.setSelected(false);
        
        assertSettings(Collections.<ConnectionSettings>emptyList());
        repository.store();
        ConnectionSettings conn = new ConnectionSettings(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user"));
        conn.setPassword("heslo".toCharArray());
        conn.setSaveCredentials(false);
        assertSettings(Arrays.asList(conn));
        
        // command passes?
        client.listRemoteBranches(URL, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(conn)), Utils.getStringList(prefs, RECENT_GURI));
        assertNull(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").toString()));
    }
    
    public void testConnectStoreCredentials () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        UserPasswordPanel p = getPanel(panel);
        p.userTextField.setText("user");
        p.userPasswordField.setText("heslo");
        p.savePasswordCheckBox.setSelected(true);
        
        assertSettings(Collections.<ConnectionSettings>emptyList());
        repository.store();
        ConnectionSettings conn = new ConnectionSettings(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user"));
        conn.setSaveCredentials(true);
        conn.setPassword("heslo".toCharArray());
        assertSettings(Arrays.asList(conn));
        
        // command passes?
        client.listRemoteBranches(URL, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(conn)), Utils.getStringList(prefs, RECENT_GURI));
        assertEquals("heslo", new String(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").toString())));
    }
    
    public void testConnectDoNotStoreCredentials_EmptyPassword () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        UserPasswordPanel p = getPanel(panel);
        p.userTextField.setText("user2");
        p.userPasswordField.setText("");
        p.savePasswordCheckBox.setSelected(false);
        
        assertSettings(Collections.<ConnectionSettings>emptyList());
        repository.store();
        ConnectionSettings conn = new ConnectionSettings(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2"));
        conn.setPassword("".toCharArray());
        assertSettings(Arrays.asList(conn));
        
        // command passes?
        client.listRemoteBranches(URL, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(conn)), Utils.getStringList(prefs, RECENT_GURI));
        assertNull(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").toString()));
    }
    
    public void testConnectStoreCredentials_EmptyPassword () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        UserPasswordPanel p = getPanel(panel);
        p.userTextField.setText("user2");
        p.userPasswordField.setText("");
        p.savePasswordCheckBox.setSelected(true);
        
        assertSettings(Collections.<ConnectionSettings>emptyList());
        repository.store();
        ConnectionSettings conn = new ConnectionSettings(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2"));
        conn.setPassword("".toCharArray());
        conn.setSaveCredentials(true);
        assertSettings(Arrays.asList(conn));
        
        // command passes?
        client.listRemoteBranches(URL, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(conn)), Utils.getStringList(prefs, RECENT_GURI));
        assertEquals("", new String(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").toString())));
    }
    
    public void testSupportedProtocols () throws Exception {
        try {
            client.listRemoteBranches("ftp://host.name/resource", GitUtils.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: ftp://host.name/resource", ex.getMessage());
        }
        try {
            client.listRemoteBranches("ftps://host.name/resource", GitUtils.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: ftps://host.name/resource", ex.getMessage());
        }
        try {
            client.listRemoteBranches("rsync://host.name/resource", GitUtils.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: rsync://host.name/resource", ex.getMessage());
        }
    }

    private RemoteRepositoryPanel getPanel (RemoteRepository repository) throws Exception {
        Field f = RemoteRepository.class.getDeclaredField("panel");
        f.setAccessible(true);
        return (RemoteRepositoryPanel) f.get(repository);
    }

    private UserPasswordPanel getPanel (RemoteRepositoryPanel parentPanel) throws Exception {
        return (UserPasswordPanel) parentPanel.connectionSettings.getComponent(0);
    }

    private void assertSettings (List<ConnectionSettings> expectedSettings) {
        List<ConnectionSettings> settings = GitModuleConfig.getDefault().getRecentConnectionSettings();
        assertEquals(expectedSettings.size(), settings.size());
        for (ConnectionSettings expected : expectedSettings) {
            boolean ok = false;
            for (ListIterator<ConnectionSettings> it = settings.listIterator(); it.hasNext(); ) {
                ConnectionSettings sett = it.next();
                String expectedUriString = expected.getUri().setUser(null).setPass(null).toString();
                String uriString = sett.getUri().setUser(null).setPass(null).toString();
                String expectedUser = expected.getUser();
                String user = sett.getUser();
                String expectedPassword = expected.getPassword() == null ? "" : new String(expected.getPassword());
                String expectedPasshrase = expected.getPassphrase() == null ? "" : new String(expected.getPassphrase());
                String password = sett.getPassword() == null ? "" : new String(sett.getPassword());
                String passhrase = sett.getPassphrase() == null ? "" : new String(sett.getPassphrase());
                
                if (expectedUriString.equals(uriString) && user.equals(expectedUser) && password.equals(expectedPassword) && passhrase.equals(expectedPasshrase)
                        && expected.isPrivateKeyAuth() == sett.isPrivateKeyAuth() && expected.isSaveCredentials() == sett.isSaveCredentials()) {
                    it.remove();
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                fail(expected.getUri().toString());
            }
        }
    }

    private void waitForInit (final RemoteRepository repository) throws InterruptedException {
        final boolean[] valid = new boolean[1];
        repository.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        valid[0] = repository.isValid();
                    }
                });
            }
        });
        for (int i = 0; i < 100; ++i) {
            if (valid[0]) {
                break;
            }
            Thread.sleep(100);
        }
    }
    
    private String toPrefsString (ConnectionSettings conn) {
        StringBuilder sb = new StringBuilder();
        sb.append(conn.getUri().setUser(null).toString());
        sb.append(DELIMITER);
        sb.append(conn.getUser());
        sb.append(DELIMITER);
        sb.append(conn.isSaveCredentials() ? "1" : "0"); //NOI18N
        sb.append(DELIMITER);
        sb.append(conn.isPrivateKeyAuth() ? "1" : "0"); //NOI18N
        sb.append(DELIMITER);
        sb.append(conn.getIdentityFile());
        return sb.toString();
    }
}
