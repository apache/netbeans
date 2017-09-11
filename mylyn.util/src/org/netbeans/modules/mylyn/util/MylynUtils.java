/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mylyn.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NetworkSettings;

/**
 * 
 * @author Tomas Stupka
 */
public class MylynUtils {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.libs.bugtracking.mylyn");
    
    public static TaskRepository createTaskRepository(String connectorKind, String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        TaskRepository repository = new TaskRepository(connectorKind, url);
        setCredentials(repository, user, password, httpUser, httpPassword);
        return repository;
    }

    public static void setCredentials (TaskRepository repository, String user, char[] password, String httpUser, char[] httpPassword) {
        logCredentials(repository, user, password, "Setting credentials: ");    // NOI18N
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(user != null ? user : "", password != null ? new String(password) : ""); // NOI18N
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);

        if(httpUser != null || httpPassword != null) {
            if(httpUser == null) {
                httpUser = "";      // NOI18N
            }
            if(httpPassword == null) {
                httpPassword = new char[0];  
            }
            logCredentials(repository, httpUser, httpPassword, "Setting http credentials: ");   // NOI18N
            authenticationCredentials = new AuthenticationCredentials(httpUser, new String(httpPassword));
            repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        } else {
            repository.setCredentials(AuthenticationType.HTTP, null, false);
        }

        URI uri = null;
        try {
            uri = new URI(repository.getUrl());
        } catch (URISyntaxException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        
        String proxyHost = NetworkSettings.getProxyHost(uri);

        // check DIRECT connection
        if(proxyHost != null && proxyHost.length() > 0) {
            String proxyPort = NetworkSettings.getProxyPort(uri);
            assert proxyPort != null;
            repository.setDefaultProxyEnabled(false);
            
            LOG.log(Level.FINEST, "Setting proxy: [{0}:{1},{2}]", new Object[]{proxyHost, proxyPort, repository.getUrl()});

            repository.setProperty(TaskRepository.PROXY_HOSTNAME, proxyHost);
            repository.setProperty(TaskRepository.PROXY_PORT, proxyPort);

            String proxyUser = NetworkSettings.getAuthenticationUsername(uri);
            if(proxyUser != null) {
                char[] pwd = NetworkSettings.getAuthenticationPassword(uri);
                String proxyPassword = pwd == null ? "" : new String(pwd); //NOI18N
                logCredentials(repository, proxyUser, proxyPassword, "Setting proxy credentials: ");
                authenticationCredentials = new AuthenticationCredentials(proxyUser, proxyPassword);
                repository.setCredentials(AuthenticationType.PROXY, authenticationCredentials, false);
            }
        } else {
            repository.setProperty(TaskRepository.PROXY_HOSTNAME, null);
            repository.setProperty(TaskRepository.PROXY_PORT, null);
            repository.setCredentials(AuthenticationType.PROXY, null, false);
        }  
    }
    
    public static void logCredentials(TaskRepository repository, String user, char[] psswd, String msg) {
        logCredentials(repository, user, psswd != null ? new String(psswd) : null, msg);
    }
    
    public static void logCredentials(TaskRepository repository, String user, String psswd, String msg) {
        LOG.log(
                Level.FINEST,
                msg + "[{0}, user={1}, password={2}]",                               // NOI18N
                new Object[]{
                    repository.getUrl(),
                    user,
                    getPasswordLog(psswd)
                }
        );
    }

    private static boolean isNonProxyHost (String nonProxyHosts, String host) {
        if(nonProxyHosts.equals("")) {  // NOI18N
            return false;
        }
        // try host name first - might be faster
        return dontUseHostName (nonProxyHosts, host) || dontUseIp (nonProxyHosts, host);
    }

    private static boolean dontUseHostName (String nonProxyHosts, String host) {
        if (host == null) return false;

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, ",", false);           // NOI18N
        while (st.hasMoreTokens () && !dontUseProxy) {
            String token = st.nextToken ().trim();
            int star = token.indexOf ("*");                                             // NOI18N
            if (star == -1) {
                dontUseProxy = token.equals (host);
                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "Host {0} found in nonProxyHosts: {1}", new Object[]{ host, nonProxyHosts}); // NOI18N
                }
            } else {
                String start = token.substring (0, star - 1 < 0 ? 0 : star - 1);
                String end = token.substring (star + 1 > token.length () ? token.length () : star + 1);
                dontUseProxy = host.startsWith(start) && host.endsWith(end);
                if(!dontUseProxy) {
                    if(end.length() > 1 && end.charAt(0) == '.') {                      // NOI18N
                        end = end.substring(1, end.length());
                    }
                    if(start.length() > 1 && start.charAt(start.length() - 1) == '.') { // NOI18N
                        start = start.substring(0, start.length() - 1);
                    }
                    dontUseProxy = host.startsWith(start) && host.endsWith(end);
                }
                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "Host {0} found in nonProxyHosts: {1}", new Object[]{host, nonProxyHosts}); // NOI18N
                }
            }
        }
        return dontUseProxy;
    }

    private static boolean dontUseIp (String nonProxyHosts, String host) {
        if (host == null) return false;

        String ip = null;
        try {
            ip = InetAddress.getByName (host).getHostAddress ();
        } catch (UnknownHostException ex) {
            LOG.log (Level.FINE, ex.getLocalizedMessage (), ex);
        }

        if (ip == null) {
            return false;
        }

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, ",", false);   // NOI18N
        while (st.hasMoreTokens () && !dontUseProxy) {
            String nonProxyHost = st.nextToken ();
            int star = nonProxyHost.indexOf ("*");                              // NOI18N
            if (star == -1) {
                dontUseProxy = nonProxyHost.equals (ip);
                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "Host''s {0} IP {1} found in nonProxyHosts: {2}", new Object[]{host, ip, nonProxyHosts}); // NOI18N
                }
            } else {
                // match with given dotted-quad IP
                try {
                    dontUseProxy = Pattern.matches (nonProxyHost, ip);
                    if (dontUseProxy) {
                        LOG.log (Level.FINEST, "Host''s {0} IP{1} found in nonProxyHosts: {2}", new Object[]{host, ip, nonProxyHosts}); // NOI18N
                    }
                } catch (PatternSyntaxException pse) {
                    // may ignore it here
                }
            }
        }
        return dontUseProxy;
    }
    
    private static String getPasswordLog(String psswd) {
        if(psswd == null) {
            return ""; // NOI18N
        }
        if("true".equals(System.getProperty("org.netbeans.modules.bugtracking.logPasswords", "false"))) { // NOI18N
            return psswd; 
        }
        return "******"; // NOI18N
    }
}
