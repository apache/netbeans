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
package org.netbeans.modules.php.atoum.preferences;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.atoum.AtoumTestingProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Atoum preferences specific for each PHP module.
 */
public final class AtoumPreferences {

    private static final String BOOTSTRAP_ENABLED = "bootstrap.enabled"; // NOI18N
    private static final String BOOTSTRAP_PATH = "bootstrap.path"; // NOI18N
    private static final String CONFIGURATION_ENABLED = "configuration.enabled"; // NOI18N
    private static final String CONFIGURATION_PATH = "configuration.path"; // NOI18N
    private static final String ATOUM_ENABLED = "atoum.enabled"; // NOI18N
    private static final String ATOUM_PATH = "atoum.path"; // NOI18N


    private AtoumPreferences() {
    }

    public static boolean isBootstrapEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(BOOTSTRAP_ENABLED, false);
    }

    public static void setBootstrapEnabled(PhpModule phpModule, boolean bootstrapEnabled) {
        getPreferences(phpModule).putBoolean(BOOTSTRAP_ENABLED, bootstrapEnabled);
    }

    public static String getBootstrapPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(BOOTSTRAP_PATH, null));
    }

    public static void setBootstrapPath(PhpModule phpModule, String bootstrapPath) {
        getPreferences(phpModule).put(BOOTSTRAP_PATH, relativizePath(phpModule, bootstrapPath));
    }

    public static boolean isConfigurationEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(CONFIGURATION_ENABLED, false);
    }

    public static void setConfigurationEnabled(PhpModule phpModule, boolean configurationEnabled) {
        getPreferences(phpModule).putBoolean(CONFIGURATION_ENABLED, configurationEnabled);
    }

    public static String getConfigurationPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(CONFIGURATION_PATH, null));
    }

    public static void setConfigurationPath(PhpModule phpModule, String configurationPath) {
        getPreferences(phpModule).put(CONFIGURATION_PATH, relativizePath(phpModule, configurationPath));
    }

    public static boolean isAtoumEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(ATOUM_ENABLED, false);
    }

    public static void setAtoumEnabled(PhpModule phpModule, boolean atoumEnabled) {
        getPreferences(phpModule).putBoolean(ATOUM_ENABLED, atoumEnabled);
    }

    public static String getAtoumPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(ATOUM_PATH, null));
    }

    public static void setAtoumPath(PhpModule phpModule, String atoumPath) {
        getPreferences(phpModule).put(ATOUM_PATH, relativizePath(phpModule, atoumPath));
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(AtoumTestingProvider.class, true);
    }

    private static String relativizePath(PhpModule phpModule, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return ""; // NOI18N
        }
        File file = new File(filePath);
        String path = PropertyUtils.relativizeFile(FileUtil.toFile(phpModule.getProjectDirectory()), file);
        if (path == null) {
            // sorry, cannot be relativized
            path = file.getAbsolutePath();
        }
        return path;
    }

    private static String resolvePath(PhpModule phpModule, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(phpModule.getProjectDirectory()), filePath).getAbsolutePath();
    }

}
