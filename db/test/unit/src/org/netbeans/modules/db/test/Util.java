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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.db.test;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.DatabaseConnectionConvertor;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class Util {

    private Util() {
    }

    public static void clearConnections() throws IOException {
        deleteFileObjects(getConnectionsFolder().getChildren());
    }

    public static void deleteDriverFiles() throws IOException {
        deleteFileObjects(getDriversFolder().getChildren());
    }

    public static FileObject getConnectionsFolder() throws IOException {
        return FileUtil.createFolder(FileUtil.getConfigRoot(), DatabaseConnectionConvertor.CONNECTIONS_PATH);
    }

    public static FileObject getDriversFolder() throws IOException {
        return FileUtil.createFolder(FileUtil.getConfigRoot(), JDBCDriverConvertor.DRIVERS_PATH);
    }

    private static void deleteFileObjects(FileObject[] fos) throws IOException {
        for (int i = 0; i < fos.length; i++) {
            fos[i].delete();
        }
    }

    public static JDBCDriver createDummyDriver() throws Exception {
        JDBCDriver[] drivers
                = JDBCDriverManager.getDefault().getDrivers("org.bar.barDriver");
        if (drivers.length > 0) {
            return drivers[0];
        }

        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver",
                "org.bar.BarDriver", new URL[]{new URL("file:///foo/path/foo.jar")});
        JDBCDriverManager.getDefault().addDriver(driver);

        return driver;
    }

    public static JDBCDriver createDummyDriverWithOtherJar() throws Exception {
        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver",
                "org.bar.BarDriver2", new URL[]{new URL("file:///foo2/path/foo2.jar")});
        JDBCDriverManager.getDefault().addDriver(driver);

        return driver;
    }

    /**
     * Disable logging of logging messages from DatabaseUILogger. See #215375.
     *
     * Usefulness of the whole logger seems to be doubtful
     */
    public static void suppressSuperfluousLogging() {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setFilter(new Filter() {
                @Override
                public boolean isLoggable(LogRecord lr) {
                    if (lr.getSourceClassName().equals("org.netbeans.modules.db.explorer.DatabaseUILogger")
                            || lr.getSourceClassName().startsWith("org.netbeans.modules.settings.RecognizeInstanceObjects")) {
                        return false;
                    } else if (lr.getSourceClassName().equals(
                            "org.netbeans.api.db.sql.support.SQLIdentifiers$DatabaseMetaDataQuoter")) {
                        if (lr.getSourceMethodName().equals("getExtraNameChars")
                                && lr.getLevel() == Level.WARNING
                                && lr.getMessage().startsWith(
                                        "DatabaseMetaData.getExtraNameCharacters() failed")) {
                            return false;
                        } else if (lr.getSourceMethodName().equals("needToQuote")
                                && lr.getLevel().intValue()
                                <= Level.INFO.intValue()) {
                            return false;
                        } else {
                            return true;
                        }
                    } else if (lr.getSourceClassName().equals(
                            "org.netbeans.modules.db.explorer.DatabaseConnection") &&
                            lr.getSourceMethodName().equals("doConnect")) {
                        return false;
                    } else {
                        return true;
                    }
                }
            });
        }
    }
}
