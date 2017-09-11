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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
