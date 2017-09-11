/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
