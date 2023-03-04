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

package org.netbeans.modules.subversion.kenai;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.PasswordAuthentication;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public class SvnKenaiAccessor {

    private static SvnKenaiAccessor instance;
    private VCSKenaiAccessor kenaiAccessor = null;

    private SvnKenaiAccessor() {
        kenaiAccessor = Lookup.getDefault().lookup(VCSKenaiAccessor.class);
    }

    public static SvnKenaiAccessor getInstance() {
        if(instance == null) {
            instance = new SvnKenaiAccessor();
        }
        return instance;
    }

    public boolean isKenai(String url) {
        return kenaiAccessor != null && kenaiAccessor.isKenai(url);
    }

    public PasswordAuthentication getPasswordAuthentication(String url, boolean forceRelogin) {
        return kenaiAccessor != null ? kenaiAccessor.getPasswordAuthentication(url, forceRelogin) : null;
    }

    /**
     * Shows a login dialog
     * @return true if successfully logged in
     */
    public boolean showLogin () {
        return kenaiAccessor != null ? kenaiAccessor.showLogin() : false;
    }

    public boolean isOnline(String user) {
        return kenaiAccessor != null && kenaiAccessor.isUserOnline(user);
    }

    public KenaiUser forName(String user, String url) {
        return kenaiAccessor != null ? kenaiAccessor.forName(user, url) : null;
    }

    public boolean isLogged (String url) {
        return kenaiAccessor != null && kenaiAccessor.isLogged(url);
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
     * Returns <code>false</code> only when the given repository is a kenai one, current user is logged in and does not have commit access rights
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
