/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.ide.spi;

/**
 * Provides access to various settings related services so that 
 * the expected consumers (bugtracking and team modules) 
 * are able to independently access different IDE settings functionality 
 * and UI (like e.g. NetBeans or JDev). 
 * 
 * @author Tomas Stupka
 */
public interface SettingsServices {
    
    /**
     * The location for all team settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String TEAM_SETTINGS_LOCATION = "Team";
    
    /**
     * Id for Tasks settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String TASKS_SETTINGS_ID = "Tasks";
    
    /**
     * Id for ODCS settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String ODCS_SETTINGS_ID = "Odcs";
    
    /**
     * Represents a particular settings section (page).
     */
    enum Section {
        /**
         * Proxy settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the Proxy settings page.
         */
        PROXY,
        /**
         * Tasks settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the Tasks settings page.
         */
        TASKS,
        /**
         * ODCS settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the ODCS settings page.
         */
        ODCS
    }
    
    /**
     * Determines whether the capability of opening a particular settings UI is available or not.
     * 
     * @param section the particular settings UI
     * @return <code>true</code> if there is a way to open a settings section, otherwise <code>false</code>
     */
    public boolean providesOpenSection(Section section);
    
    /**
     * Opens a particular settings UI - e.g. proxy or tasks settings
     * 
     * @param section a particular settings section to be opened
     */
    public void openSection(Section section);
}
