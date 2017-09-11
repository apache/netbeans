/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.derby.spi.support;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.JDKDerbyHelper;
import org.netbeans.modules.derby.RegisterDerby;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class DerbySupport {

    private static final Logger LOGGER = Logger.getLogger(DerbySupport.class.getName());

    private DerbySupport() {
    }

    /**
     * Sets the location of the Derby installation and registers the Derby drivers.
     * The Derby installation must have a lib subdirectory in which the Derby JAR
     * files are located.
     *
     * @param location the jars locations. This must be an existing directory.
     */
    public static void setLocation(String location) {
        LOGGER.log(Level.FINE, "setLocation called for {0}", location); // NOI18N
        String jdkDerbyLocation = JDKDerbyHelper.forDefault().findDerbyLocation();
        String realLocation = (jdkDerbyLocation != null) ? jdkDerbyLocation : location;
        DerbyOptions.getDefault().trySetLocation(realLocation);
    }

    /**
     * Returns the location of the Derby jars. Never returns null,
     * instead it returns an empty string if the location is unknown.
     */
    public static String getLocation() {
        return DerbyOptions.getDefault().getLocation();
    }

    /**
     * Sets the Derby system home, that is, the directory 
     * where the Derby databases are located.
     */
    public static void setSystemHome(String systemHome) {
        DerbyOptions.getDefault().setSystemHome(systemHome);
    }

    /**
     * Returns the Derby system home. Never returns null,
     * instead it returns an empty string if the location is unknown.
     */
    public static String getSystemHome() {
        return DerbyOptions.getDefault().getSystemHome();
    }

    /**
     * Returns the default Derby system home. It is not guaranteed that
     * the directory returned by this method exists.
     */
    public static String getDefaultSystemHome() {
        // issue 76908
        String propertySystemHome = System.getProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME);
        if (propertySystemHome != null) {
            return propertySystemHome;
        }

        String userHome = System.getProperty("user.home"); // NOI18N
        // dbDir ist the original place where the derby database was located
        String dbDir = ".netbeans-derby"; // NOI18N
        // dbDirWindows is the path relative to the Netbeans subfolder of
        // the profile Folter %AppData%/NetBeans/Derby
        // it is expected that this is a sibling of the cachedir
        String dbDirWindows = "NetBeans/Derby"; // NOI18N

        File dbFile = FileUtil.normalizeFile(new File(userHome, dbDir));

        // Don't change anything for non-windows platforms
        if (System.getProperty("os.name").contains("Windows")) {
            File dbFileWindows;
            // On Windows it is expected that the AppData directory can be read
            // from environment - if that fails the dbDir is create relative to the
            // user home
            String appdataDir = System.getenv("AppData");
            
            // Allow override of environment detection of AppData
            // usecase: allow testing
            appdataDir = System.getProperty("org.netbeans.modules.derby.spi.support.DerbySupport.overrideAppData", appdataDir);
            
            if (appdataDir != null) {
                dbFileWindows = FileUtil
                        .normalizeFile(new File(appdataDir, dbDirWindows));
            } else {
                dbFileWindows = FileUtil
                        .normalizeFile(new File(userHome, dbDirWindows));
            }
            
            // Check first if one of the two possible locations already exist
            // if so - use that directory 
            if(dbFileWindows.exists()) {
                return dbFileWindows.getAbsolutePath();
            } else if (dbFile.exists()) {
                return dbFile.getAbsolutePath();
            } else {
                return dbFileWindows.getAbsolutePath();
            }
        } else {
            return dbFile.getAbsolutePath();
        }
    }

    /**
     * Ensures the Derby database is started, that is, starts it if it is
     * not running and does nothing otherwise.
     *
     * @since 1.5
     */
    public static void ensureStarted() {
        RegisterDerby.getDefault().ensureStarted(false);
    }
}
