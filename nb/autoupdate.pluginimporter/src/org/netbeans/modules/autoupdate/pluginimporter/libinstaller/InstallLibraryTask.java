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
package org.netbeans.modules.autoupdate.pluginimporter.libinstaller;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * #195123: prompt to load JUnit during startup.
 */
//@ServiceProvider(service = Runnable.class, path = "WarmUp")
public class InstallLibraryTask implements Runnable {

    private static final String KEY = "tried.to.download.junit"; // NOI18N
    private static final String JUNIT_APPROVED = "junit_accepted"; // NOI18N
    private static final String JUNIT_DENIED = "junit_denied"; // NOI18N
    private static final Logger LOG = Logger.getLogger(InstallLibraryTask.class.getName());
    public static final RequestProcessor RP = new RequestProcessor(InstallLibraryTask.class.getName(), 1);

    public @Override
    void run() {
        Preferences p = NbPreferences.forModule(InstallLibraryTask.class);
        if (p.getBoolean(KEY, false)) {
            // Only check once (i.e. on first start for a fresh user dir).
            return;
        }
        p.putBoolean(KEY, true);
        // find licenseAcceptedFile
        File licenseAcceptedFile = InstalledFileLocator.getDefault().locate("var/license_accepted", null, false); // NOI18N
        if (licenseAcceptedFile == null) {
            LOG.info("$userdir/var/license_accepted not found => skipping install JUnit.");
            return ;
        }
        try {
            // read content of file
            String content = FileUtil.toFileObject(licenseAcceptedFile).asText();
            LOG.fine("Content of $userdir/var/license_accepted: " + content);
            if (content != null && content.indexOf(JUNIT_APPROVED) != -1) {
                // IDE license accepted, JUnit accpeted => let's install silently
                LOG.fine(" IDE license accepted, JUnit accepted => let's install silently"); 
                JUnitLibraryInstaller.install(true);
            } else if (content != null && content.indexOf(JUNIT_DENIED) != -1) {
                // IDE license accepted but JUnit disapproved => do nothing
                LOG.fine("IDE license accepted but JUnit disapproved => do nothing"); 
            } else {
                // IDE license accepted, JUnit N/A => use prompt & wizard way
                LOG.fine("IDE license accepted, JUnit N/A => use prompt & wizard way"); 
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        JUnitLibraryInstaller.install(false);
                    }
                });
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "while reading " + licenseAcceptedFile, ex);
        }
    }
    
}
