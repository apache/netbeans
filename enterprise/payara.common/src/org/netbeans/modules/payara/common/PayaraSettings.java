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
package org.netbeans.modules.payara.common;

import java.util.prefs.Preferences;
import org.netbeans.modules.payara.tooling.PayaraToolsConfig;
import org.openide.util.NbPreferences;

/**
 * Payara module settings.
 * <p/>
 * Handles persistent Payara module settings.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraSettings {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** NetBeans preferences node label. */
    private static final String NB_PREFERENCES_NODE
            = "org/netbeans/modules/payara/common";

    /** Payara settings label: Payara 3.1.2 warning show again. */
    private static final String LBL_GF312_WARNING_SHOW_AGAIN
            = "Gf312WarningAgain";

    /** Payara settings label: Payara 3.1.2 warning show again. */
    private static final String LBL_PF_KILL_SHOW_AGAIN
            = "GfKillWarningAgain";

    /** Payara settings label: Show password text in properties form. */
    private static final String LBL_PF_SHOW_PASSWORD_IN_PROPERTIES_FORM
            = "GfShowPasswordInPropertiesForm";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize and configure Payara Tooling Library.
     */
    static void toolingLibraryconfig() {
        PayaraToolsConfig.noProxyForLoopback();
    }

    /**
     * Get NetBeans preferences node for Payara module settings.
     * <p/>
     * @return NetBeans preferences node for Payara module settings.
     */
    private static Preferences settings() {
        return NbPreferences.root().node(NB_PREFERENCES_NODE);
    }

    /**
     * Get Payara 3.1.2 warning show again property value.
     * <p/>
     * Default value is <code>true</code>.
     * <p/>
     * @return Payara 3.1.2 warning show again property value.
     */
    public static boolean getGf312WarningShowAgain() {
        return settings().getBoolean(LBL_GF312_WARNING_SHOW_AGAIN, true);
    }

    /**
     * Set Payara 3.1.2 warning show again property value.
     * <p/>
     * @param showAgain Payara 3.1.2 warning show again property value
     *                  to be set.
     */
    public static void setGf312WarningShowAgain(final boolean showAgain) {
        settings().putBoolean(LBL_GF312_WARNING_SHOW_AGAIN, showAgain);
    }

    /**
     * Get Payara kill warning show again property value.
     * <p/>
     * Default value is <code>true</code>.
     * <p/>
     * @return Payara kill warning show again property value.
     */
    public static boolean getGfKillWarningShowAgain() {
        return settings().getBoolean(LBL_PF_KILL_SHOW_AGAIN, true);
    }

    /**
     * Set Payara kill warning show again property value.
     * <p/>
     * @param showAgain Payara kill warning show again property value
     *                  to be set.
     */
    public static void setGfKillWarningShowAgain(final boolean showAgain) {
        settings().putBoolean(LBL_PF_KILL_SHOW_AGAIN, showAgain);
    }

    /**
     * Get Payara setting to show password text in properties form.
     * <p/>
     * @return Payara setting to show password text in properties form.
     */
    public static boolean getGfShowPasswordInPropertiesForm() {
        return settings().getBoolean(
                LBL_PF_SHOW_PASSWORD_IN_PROPERTIES_FORM, false);
    }

    /**
     * Get Payara setting to show password text in properties form.
     * <p/>
     * @@param show Payara setting to show password text in properties form.
     */
    public static void setGfShowPasswordInPropertiesForm(final boolean show) {
        settings().putBoolean(LBL_PF_SHOW_PASSWORD_IN_PROPERTIES_FORM, show);
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
