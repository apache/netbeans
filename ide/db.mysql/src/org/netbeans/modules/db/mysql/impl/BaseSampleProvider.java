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

package org.netbeans.modules.db.mysql.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecutor;
import org.netbeans.modules.db.mysql.spi.sample.SampleProvider;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * Provides support for our base samples
 * 
 * @author David Van Couvering
 */
public class BaseSampleProvider implements SampleProvider {
    private static final Logger LOGGER = Logger.getLogger(BaseSampleProvider.class.getName());

    private static final BaseSampleProvider DEFAULT = new BaseSampleProvider();

    private static ArrayList<String> SAMPLES;

    private static final String MODULE_JAR_FILE =
            "modules/org-netbeans-modules-db-mysql.jar";
    private static final String RESOURCE_DIR_PATH =
            "org/netbeans/modules/db/mysql/resources";
    
    private String getMessage(String key, String ... values) {
        return NbBundle.getMessage(BaseSampleProvider.class, key, values);
    }
    
    static {
        SAMPLES = new ArrayList<String>();
        SAMPLES.add("sample");
        SAMPLES.add("vir");
        SAMPLES.add("travel");
    }

    public static BaseSampleProvider getDefault() {
        return DEFAULT;
    }
    public void create(String sampleName, DatabaseConnection dbconn) throws DatabaseException {
        if (sampleName == null) {
            throw new NullPointerException();
        }

        if (! SAMPLES.contains(sampleName)) {
                throw new DatabaseException(getMessage("MSG_SampleNotSupported", sampleName));
        }

        if (! checkInnodbSupport(dbconn.getJDBCConnection())) {
            throw new DatabaseException(getMessage("MSG_NoSampleWithoutInnoDB")); // NOI8N
        }

        String sql = getSqlText(sampleName);

        SQLExecutor.execute(dbconn, sql);
    }

    public boolean supportsSample(String name) {
        return SAMPLES.contains(name);
    }
    private boolean checkInnodbSupport(Connection conn) throws DatabaseException {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW STORAGE ENGINES"); // NOI18N

            while (rs.next()) {
                if ("INNODB".equalsIgnoreCase(rs.getString(1)) &&
                    ("YES".equalsIgnoreCase(rs.getString(2)) || "DEFAULT".equalsIgnoreCase(rs.getString(2)))) { // NOI18N
                    return true;
                }
            }
            rs.close();
            stmt.close();

            return false;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public List<String> getSampleNames() {
        return SAMPLES;
    }


    private static String getSqlText(String sampleName) throws DatabaseException {
        FileObject sqlfile = getSampleFile(sampleName);
        StringBuilder builder = new StringBuilder();

        try {
            // Don't need to lock file as it is read-only (in a JAR file)
            BufferedReader reader = new BufferedReader(new InputStreamReader(sqlfile.getInputStream()));

            char[] cbuf = new char[100];
            for ( ; ;) {
                int numread = reader.read(cbuf);
                if (numread < 0) {
                    reader.close();
                    break;
                }
                builder.append(cbuf, 0, numread);
            }
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }

        return builder.toString().trim();
    }

    private static FileObject getSampleFile(String sampleName)
            throws DatabaseException {

        try {
            File jarfile = InstalledFileLocator.getDefault().locate(
                MODULE_JAR_FILE, null, false); // NOI18N

            JarFileSystem jarfs = new JarFileSystem();

            jarfs.setJarFile(jarfile);

            String filename = "/create-" + sampleName + ".sql";
            return jarfs.findResource(RESOURCE_DIR_PATH + filename);
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    Utils.getMessage(
                        "MSG_ErrorLoadingSampleSQL", sampleName,
                        e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        }
    }
}
