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

package org.netbeans.modules.bugzilla.autoupdate;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.NbBundle;

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

    private final Set<BugzillaRepository> repos = Collections.newSetFromMap(new WeakHashMap<>());
    
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
