/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.php.project.connections.ftp;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.project.connections.api.RemotePreferences;

/**
 * FTP preferences.
 * @see RemotePreferences
 */
public final class FtpPreferences {

    private static final FtpPreferences INSTANCE = new FtpPreferences();
    private static final String WINDOWS_JDK7_WARNING = "windows.jdk7.warning"; // NOI18N


    private FtpPreferences() {
    }

    /**
     * Get instance of FTP preferences.
     * @return instance of FTP preferences
     */
    public static FtpPreferences getInstance() {
        return INSTANCE;
    }

    /**
     * {@code True} if warning about possible firewall issue on Windows should be shown.
     * <p>
     * See issue #202021 for more information.
     * @return {@code true} if warning about possible firewall issue on Windows should be shown
     */
    public boolean getWindowsJdk7Warning() {
        return getPreferences(true).getBoolean(WINDOWS_JDK7_WARNING, true);
    }

    /**
     * Set the state of warning about possible firewall issue on Windows.
     * <p>
     * See issue #202021 for more information.
     * @param shown {@code true} if the warning should be shown, {@code false} otherwise
     */
    public void setWindowsJdk7Warning(boolean shown) {
        getPreferences(true).putBoolean(WINDOWS_JDK7_WARNING, shown);
    }

    private Preferences getPreferences(boolean importEnabled) {
        return RemotePreferences.forType(FtpConnectionProvider.FTP_CONNECTION_TYPE, importEnabled).getPreferences();
    }

}
