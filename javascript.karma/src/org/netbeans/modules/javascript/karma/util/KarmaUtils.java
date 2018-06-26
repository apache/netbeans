/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;

@MIMEResolver.Registration(displayName = "karma.conf.js", resource = "../resources/karmaconf-resolver.xml", position = 126)
public final class KarmaUtils {

    private static final Logger LOGGER = Logger.getLogger(KarmaUtils.class.getName());

    private static final FilenameFilter KARMA_CONFIG_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("karma") // NOI18N
                    && name.endsWith(".conf.js"); // NOI18N
        }
    };
    private static final FilenameFilter JS_FILES_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".js"); // NOI18N
        }
    };


    private KarmaUtils() {
    }

    @CheckForNull
    public static String readContent(URL url) {
        assert !EventQueue.isDispatchThread();
        try {
            Path tmpFile = Files.createTempFile("nb-karma-url-", ".html"); // NOI18N
            try {
                NetworkSupport.download(url.toExternalForm(), tmpFile.toFile());
                return new String(Files.readAllBytes(tmpFile), StandardCharsets.UTF_8);
            } finally {
                Files.delete(tmpFile);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public static List<WebBrowser> getDebugBrowsers() {
        List<WebBrowser> browsers = new ArrayList<>();
        for (WebBrowser browser : WebBrowsers.getInstance().getAll(false, false, false, true)) {
            if (browser.isEmbedded()) {
                continue;
            }
            if (browser.getBrowserFamily().isMobile()) {
                continue;
            }
            if (!browser.hasNetBeansIntegration()) {
                continue;
            }
            browsers.add(browser);
        }
        return browsers;
    }

    @CheckForNull
    public static WebBrowser getPreferredDebugBrowser() {
        for (WebBrowser browser : getDebugBrowsers()) {
            return browser;
        }
        return null;
    }

    public static File getKarmaConfigDir(Project project) {
        // prefer directory for current karma config file
        String karmaConfig = KarmaPreferences.getConfig(project);
        if (karmaConfig != null) {
            File karmaConfigFile = new File(karmaConfig);
            if (karmaConfigFile.isFile()) {
                return karmaConfigFile.getParentFile();
            }
        }
        // simply return project directory
        return FileUtil.toFile(project.getProjectDirectory());
    }

    public static List<File> findJsFiles(File dir) {
        assert dir != null;
        // #241556
        if (!dir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] jsFiles = dir.listFiles(JS_FILES_FILTER);
        if (jsFiles == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(jsFiles);
    }

    public static List<File> findKarmaConfigs(File configDir) {
        assert configDir != null;
        // #241556
        if (!configDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] configs = configDir.listFiles(KARMA_CONFIG_FILTER);
        if (configs == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(configs);
    }

    /**
     * Tries to find the "best" Karma config file.
     */
    @CheckForNull
    public static File findKarmaConfig(File configDir) {
        List<File> karmaConfigs = findKarmaConfigs(configDir);
        int indexOf = karmaConfigs.indexOf(new File(configDir, "karma.conf.js")); // NOI18N
        if (indexOf != -1) {
            return karmaConfigs.get(indexOf);
        }
        File firstConfig = null;
        for (File config : karmaConfigs) {
            if (firstConfig == null) {
                firstConfig = config;
            }
            String configName = config.getName().toLowerCase();
            if (configName.contains("share") // NOI18N
                    || configName.contains("common")) { // NOI18N
                continue;
            }
            return config;
        }
        return firstConfig;
    }

}
