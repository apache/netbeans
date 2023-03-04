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

package org.netbeans.modules.mercurial.kenai;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka, Ondra Vrabec
 */
public class HgKenaiAccessor {

    private static HgKenaiAccessor instance;
    private VCSKenaiAccessor kenaiAccessor = null;
    private Set<String> queriedUrls = new HashSet<String>(5);

    private HgKenaiAccessor() {
        kenaiAccessor = Lookup.getDefault().lookup(VCSKenaiAccessor.class);
    }

    public static HgKenaiAccessor getInstance() {
        if(instance == null) {
            instance = new HgKenaiAccessor();
        }
        return instance;
    }

    public boolean isKenai(String url) {
        return kenaiAccessor != null && kenaiAccessor.isKenai(url);
    }
    
    public boolean isLoggedIntoKenai (String url) {
        return kenaiAccessor != null && kenaiAccessor.isLogged(url);
    }

    public PasswordAuthentication getPasswordAuthentication(String url, boolean forceRelogin) {
        if(kenaiAccessor != null) {
            if(forceRelogin && queriedUrls.contains(url)) {
                // we already queried the authentication for this url, but it didn't
                // seem to be accepted -> force a new login, the current user
                // might not be authorized for the given kenai project (url).
                if(!kenaiAccessor.showLogin()) {
                    return null;
                }
            }
            queriedUrls.add(url);
            return kenaiAccessor.getPasswordAuthentication(url);
        } else {
            return null;
        }
    }

    public boolean isUserOnline(String user) {
        return kenaiAccessor != null ? kenaiAccessor.isUserOnline(user) : false;
    }

    public KenaiUser forName(String user, String url) {
        return kenaiAccessor != null ? kenaiAccessor.forName(user, url) : null;
    }

    public String getRevisionUrl(String repositoryUrl, String revision) {
        return kenaiAccessor == null ? null : kenaiAccessor.getRevisionUrl(repositoryUrl, revision);
    }

    private void removeVCSNoficationListener(PropertyChangeListener l) {
        if(kenaiAccessor != null) {
            kenaiAccessor.removeVCSNoficationListener(l);
        }
    }

    private void addVCSNoficationListener(PropertyChangeListener l) {
        if(kenaiAccessor != null) {
            kenaiAccessor.addVCSNoficationListener(l);
        }
    }

    public void registerVCSNoficationListener() {
        if("true".equals(System.getProperty("kenai.vcs.notifications.ignore"))) {
            return;
        }
        addVCSNoficationListener(new KenaiNotificationListener());
    }

    /**
     * Returns <code>false</code> only when the given repository is a kenai one, current user is logged in and does not have read access rights
     * @param repositoryUrl
     * @return
     */
    public boolean canRead (String repositoryUrl) {
        return isAuthorized(repositoryUrl, VCSKenaiAccessor.RepositoryActivity.READ);
    }

    /**
     * Returns <code>false</code> only when the given repository is a kenai one, current user is logged in and does not have push access rights
     * @param repositoryUrl
     * @return
     */
    public boolean canWrite (String repositoryUrl) {
        return isAuthorized(repositoryUrl, VCSKenaiAccessor.RepositoryActivity.WRITE);
    }

    private boolean isAuthorized (String repositoryUrl, VCSKenaiAccessor.RepositoryActivity permission) {
        return kenaiAccessor == null || !kenaiAccessor.isLogged(repositoryUrl) || kenaiAccessor.isAuthorized(repositoryUrl, permission);
    }

}
