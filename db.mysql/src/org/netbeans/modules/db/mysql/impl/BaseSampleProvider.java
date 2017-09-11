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
                if ("INNODB".equals(rs.getString(1).toUpperCase()) &&
                    ("YES".equals(rs.getString(2).toUpperCase()) || "DEFAULT".equals(rs.getString(2).toUpperCase()))) { // NOI18N
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
