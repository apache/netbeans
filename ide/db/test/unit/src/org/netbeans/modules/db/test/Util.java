/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
