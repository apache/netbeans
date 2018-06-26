/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.util.prefs.Preferences;
import org.netbeans.modules.glassfish.tooling.GlassFishToolsConfig;
import org.openide.util.NbPreferences;

/**
 * GlassFish module settings.
 * <p/>
 * Handles persistent GlassFish module settings.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishSettings {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** NetBeans preferences node label. */
    private static final String NB_PREFERENCES_NODE
            = "org/netbeans/modules/glassfish/common";

    /** GlassFish settings label: GlassFish 3.1.2 warning show again. */
    private static final String LBL_GF312_WARNING_SHOW_AGAIN
            = "Gf312WarningAgain";

    /** GlassFish settings label: GlassFish 3.1.2 warning show again. */
    private static final String LBL_GF_KILL_SHOW_AGAIN
            = "GfKillWarningAgain";

    /** GlassFish settings label: Show password text in properties form. */
    private static final String LBL_GF_SHOW_PASSWORD_IN_PROPERTIES_FORM
            = "GfShowPasswordInPropertiesForm";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize and configure GlassFish Tooling Library.
     */
    static void toolingLibraryconfig() {
        GlassFishToolsConfig.noProxyForLoopback();
    }

    /**
     * Get NetBeans preferences node for GlassFish module settings.
     * <p/>
     * @return NetBeans preferences node for GlassFish module settings.
     */
    private static Preferences settings() {
        return NbPreferences.root().node(NB_PREFERENCES_NODE);
    }

    /**
     * Get GlassFish 3.1.2 warning show again property value.
     * <p/>
     * Default value is <code>true</code>.
     * <p/>
     * @return GlassFish 3.1.2 warning show again property value.
     */
    public static boolean getGf312WarningShowAgain() {
        return settings().getBoolean(LBL_GF312_WARNING_SHOW_AGAIN, true);
    }

    /**
     * Set GlassFish 3.1.2 warning show again property value.
     * <p/>
     * @param showAgain GlassFish 3.1.2 warning show again property value
     *                  to be set.
     */
    public static void setGf312WarningShowAgain(final boolean showAgain) {
        settings().putBoolean(LBL_GF312_WARNING_SHOW_AGAIN, showAgain);
    }

    /**
     * Get GlassFish kill warning show again property value.
     * <p/>
     * Default value is <code>true</code>.
     * <p/>
     * @return GlassFish kill warning show again property value.
     */
    public static boolean getGfKillWarningShowAgain() {
        return settings().getBoolean(LBL_GF_KILL_SHOW_AGAIN, true);
    }

    /**
     * Set GlassFish kill warning show again property value.
     * <p/>
     * @param showAgain GlassFish kill warning show again property value
     *                  to be set.
     */
    public static void setGfKillWarningShowAgain(final boolean showAgain) {
        settings().putBoolean(LBL_GF_KILL_SHOW_AGAIN, showAgain);
    }

    /**
     * Get GlassFish setting to show password text in properties form.
     * <p/>
     * @return GlassFish setting to show password text in properties form.
     */
    public static boolean getGfShowPasswordInPropertiesForm() {
        return settings().getBoolean(
                LBL_GF_SHOW_PASSWORD_IN_PROPERTIES_FORM, false);
    }

    /**
     * Get GlassFish setting to show password text in properties form.
     * <p/>
     * @@param show GlassFish setting to show password text in properties form.
     */
    public static void setGfShowPasswordInPropertiesForm(final boolean show) {
        settings().putBoolean(LBL_GF_SHOW_PASSWORD_IN_PROPERTIES_FORM, show);
    }

    /**
     * Get system property do disable UI in NetBeans.
     * <p/>
     * Default value is <code>false</code>.
     * <p/>
     * @return Value of <code>true</code> when UI is enabled in NetBeans
     *         or <code>false</code> otherwise.
     */
    public static boolean showWindowSystem() {
        String showProperty
                = System.getProperty("org.netbeans.core.WindowSystem.show");
        return showProperty == null
                || !showProperty.toLowerCase().equals("false");
    }

}
