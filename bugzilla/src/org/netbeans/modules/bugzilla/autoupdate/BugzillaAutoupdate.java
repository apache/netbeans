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

package org.netbeans.modules.bugzilla.autoupdate;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaAutoupdate {

    public static final BugzillaVersion SUPPORTED_BUGZILLA_VERSION;
    static {
        String version = System.getProperty("netbeans.t9y.bugzilla.supported.version"); // NOI18N
        SUPPORTED_BUGZILLA_VERSION = version != null ? new BugzillaVersion(version) : BugzillaVersion.BUGZILLA_4_0; // NOI18N
    }
    static final String BUGZILLA_MODULE_CODE_NAME = "org.netbeans.modules.bugzilla"; // NOI18N

    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*version ((\\d+?\\.\\d+?\\.\\d+?)|(\\d+?\\.\\d+?)).*$");

    private static BugzillaAutoupdate instance;

    private final Set<BugzillaRepository> repos = new WeakSet<BugzillaRepository>();
    
    private final AutoupdateSupport support = new AutoupdateSupport(new AutoupdateCallback(), BUGZILLA_MODULE_CODE_NAME, NbBundle.getMessage(Bugzilla.class, "LBL_ConnectorName"));
    
    private BugzillaAutoupdate() { }

    public static BugzillaAutoupdate getInstance() {
        if(instance == null) {
            instance = new BugzillaAutoupdate();
        }
        return instance;
    }
    
    /**
     * Checks if the remote Bugzilla repository has a version higher then actually
     * supported and if an update is available on the UC.
     *
     * @param repository the repository to check the version for
     */
    public void checkAndNotify(final BugzillaRepository repository) {
        synchronized(repos) {
            repos.add(repository);
        }
        support.checkAndNotify(repository.getUrl());
    }

    public boolean isSupportedVersion(BugzillaVersion version) {
        return version.compareTo(SUPPORTED_BUGZILLA_VERSION) <= 0;
    }
     
    public BugzillaVersion getVersion(String desc) {
        String[] lines = desc.split("\n");
        for (String l : lines) {
            Matcher m = VERSION_PATTERN.matcher(l);
            if(m.matches()) {
                return new BugzillaVersion(m.group(1)) ;
            }
        }
        return null;        
    }

    public BugzillaVersion getServerVersion(BugzillaRepository repository) {
        BugzillaConfiguration conf = repository.getConfiguration();
        if(!conf.isValid()) {
            return null; // do not force the wrong version notification
        }
        BugzillaVersion version = conf.getInstalledVersion();
        return version;
    }

    public AutoupdateSupport getAutoupdateSupport() {
        return support;
    }
    
    class AutoupdateCallback implements AutoupdateSupport.Callback {
        @Override
        public String getServerVersion(String url) {
            BugzillaRepository repository = null;
            synchronized (repos) {
                for (BugzillaRepository r : repos) {
                    if(r.getUrl().equals(url)) {
                        repository = r;
                    }
                }
            }
            assert repository != null : "no repository found for url " + url;
            if(repository == null) {
                return null;
            }
            BugzillaVersion version = BugzillaAutoupdate.this.getServerVersion(repository);
            return version != null ? version.toString() : null;
        }

        @Override
        public boolean checkIfShouldDownload(String desc) {
            BugzillaVersion version = getVersion(desc);
            return version != null && SUPPORTED_BUGZILLA_VERSION.compareTo(version) < 0;
        }

        @Override
        public boolean isSupportedVersion(String version) {
            return BugzillaAutoupdate.this.isSupportedVersion(new BugzillaVersion(version));
        }
    };
}
