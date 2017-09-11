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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.installations;

import org.netbeans.modules.db.mysql.impl.Installation;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.util.Utilities;

/**
 * Defines the AMP stack distribution called "XAMPP" for Linux
 * See <a href="http://www.apachefriends.org/en/xampp-linux.html">
 * http://www.apachefriends.org/en/xampp-linux.html</a>
 * 
 * @author David Van Couvering
 */
public class LinuxXAMPPInstallation implements Installation {
    private static final String LAMPP = "/opt/lampp/lampp"; // NOI18N
    private static final String ADMIN_URL = "http://localhost/phpmyadmin"; // NOI18N
    private static final String DEFAULT_PORT = "3306"; // NOI18N
    private static final String GKSU = "/usr/bin/gksu"; // NOI18N
        
    private static final LinuxXAMPPInstallation DEFAULT = 
            new LinuxXAMPPInstallation();
    
    public static final LinuxXAMPPInstallation getDefault() {
        return DEFAULT;
    }
    
    protected LinuxXAMPPInstallation() {
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isInstalled() {
        return Utilities.isUnix() && Utils.isValidExecutable(LAMPP)
                && Utils.isValidExecutable(GKSU);
    }

    public String[] getAdminCommand() {
        return new String[] { ADMIN_URL, "" };
    }

    public String[] getStartCommand() {
        return new String[] { GKSU, LAMPP + " startmysql" };
    }

    public String[] getStopCommand() {
        return new String[] { GKSU, LAMPP + " stopmysql" };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public String toString() {
        return "Linux XAMPP Installation - " + LAMPP;                   //NOI18N
    }
}
