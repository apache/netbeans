/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.symfony2.SymfonyPhpFrameworkProvider;
import org.netbeans.modules.php.symfony2.options.SymfonyOptions;

/**
 * Symfony 2/3 preferences for PHP module.
 */
public final class SymfonyPreferences {

    private static final String ENABLED = "enabled"; // NOI18N
    private static final String APP_DIR = "appDir-path"; // NOI18N
    private static final String IGNORE_CACHE_DIRECTORY = "cacheDir-ignored"; // NOI18N

    private static final String DEFAULT_APP_DIR = "app"; // NOI18N


    private SymfonyPreferences() {
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    @CheckForNull
    public static Boolean isEnabled(PhpModule module) {
        String enabled = getPreferences(module).get(ENABLED, null);
        if (enabled == null) {
            return null;
        }
        return Boolean.valueOf(enabled);
    }

    public static void setEnabled(PhpModule module, boolean enabled) {
        getPreferences(module).putBoolean(ENABLED, enabled);
    }

    public static String getAppDir(PhpModule module) {
        return getPreferences(module).get(APP_DIR, DEFAULT_APP_DIR);
    }

    public static void setAppDir(PhpModule module, String appDir) {
        if (appDir.equals(DEFAULT_APP_DIR)) {
            getPreferences(module).remove(APP_DIR);
        } else {
            getPreferences(module).put(APP_DIR, appDir);
        }
    }

    public static boolean isCacheDirIgnored(PhpModule phpModule) {
        String ignored = getPreferences(phpModule).get(IGNORE_CACHE_DIRECTORY, null);
        if (ignored == null) {
            return SymfonyOptions.getInstance().getIgnoreCache();
        }
        return Boolean.valueOf(ignored);
    }

    public static void setCacheDirIgnored(PhpModule phpModule, boolean ignored) {
        getPreferences(phpModule).putBoolean(IGNORE_CACHE_DIRECTORY, ignored);
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(SymfonyPhpFrameworkProvider.class, true);
    }

}
