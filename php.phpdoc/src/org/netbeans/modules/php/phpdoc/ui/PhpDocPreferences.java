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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.phpdoc.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;

public final class PhpDocPreferences {

    private static final String PHPDOC_ENABLED = "enabled"; // NOI18N
    private static final String PHPDOC_TARGET = "target"; // NOI18N
    private static final String PHPDOC_TITLE = "title"; // NOI18N


    private PhpDocPreferences() {
    }

    public static boolean isEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(PHPDOC_ENABLED, false);
    }

    public static void setEnabled(PhpModule phpModule, boolean enabled) {
        getPreferences(phpModule).putBoolean(PHPDOC_ENABLED, enabled);
    }

    public static String getPhpDocTarget(PhpModule phpModule, boolean showPanel) {
        Preferences preferences = getPreferences(phpModule);
        String phpDocTarget = preferences.get(PHPDOC_TARGET, null);
        if ((phpDocTarget == null || phpDocTarget.isEmpty()) && showPanel) {
            phpDocTarget = BrowseFolderPanel.open(phpModule);
            if (phpDocTarget == null) {
                // cancelled
                return null;
            }
            setPhpDocTarget(phpModule, phpDocTarget);
        }
        return phpDocTarget;
    }

    public static void setPhpDocTarget(PhpModule phpModule, String phpDocTarget) {
        getPreferences(phpModule).put(PHPDOC_TARGET, phpDocTarget);
    }

    public static String getPhpDocTitle(PhpModule phpModule) {
        return getPreferences(phpModule).get(PHPDOC_TITLE, getDefaultPhpDocTitle(phpModule));
    }

    public static void setPhpDocTitle(PhpModule phpModule, String phpDocTitle) {
        if (phpDocTitle.equals(getDefaultPhpDocTitle(phpModule))) {
            return;
        }
        getPreferences(phpModule).put(PHPDOC_TITLE, phpDocTitle);
    }

    private static Preferences getPreferences(PhpModule phpModule) {
        return phpModule.getPreferences(PhpDocPreferences.class, false);
    }

    private static String getDefaultPhpDocTitle(PhpModule phpModule) {
        return phpModule.getDisplayName();
    }
}
