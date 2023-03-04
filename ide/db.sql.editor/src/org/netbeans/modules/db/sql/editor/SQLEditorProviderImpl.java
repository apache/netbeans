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

package org.netbeans.modules.db.sql.editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider.class)
public class SQLEditorProviderImpl implements SQLEditorProvider {
    
    // TODO: should ensure that the number of the generated temporary file
    // is greater than any of the numbers of the existing files

    private static final String CMD_FOLDER = "Databases/SQLCommands"; // NOI18N
    
    @Override
    public void openSQLEditor(DatabaseConnection dbconn, String sql, boolean execute) {
        FileObject tmpFo = FileUtil.getConfigFile(CMD_FOLDER);
        if (tmpFo == null) {
            try {
                tmpFo = FileUtil.createFolder(FileUtil.getConfigRoot(), CMD_FOLDER );
            } catch (IOException e) {
                Logger.getLogger(SQLEditorProviderImpl.class.getName()).log(Level.INFO, e.getLocalizedMessage(), e);
            }
        }
        
        FileObject sqlFo = null;
        
        int i = 1;
        for (;;) {
            String nameFmt = NbBundle.getMessage(SQLEditorProviderImpl.class, "LBL_SQLCommandFileName");
            String name = MessageFormat.format(nameFmt, new Object[] { Integer.valueOf(i) });
            try {
                sqlFo = tmpFo.createData(name);
            } catch (IOException e) {
                i++;
                continue;
            }
            break;
        }
        
        try {
            FileLock lock = sqlFo.lock();
            try {
                OutputStream stream = sqlFo.getOutputStream(lock);
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
                    try {
                        writer.write(sql);
                    } finally {
                        writer.close();
                    }
                } finally {
                    stream.close();
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        DataObject sqlDo;
        try {
            sqlDo = DataObject.find(sqlFo);
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(SQLEditorProviderImpl.class.getName()).log(Level.INFO, e.getLocalizedMessage(), e);
            return;
        }
        
        OpenCookie openCookie = sqlDo.getCookie (OpenCookie.class);
        openCookie.open();
        
        SQLExecuteCookie sqlCookie = sqlDo.getCookie (SQLExecuteCookie.class);
        if (sqlCookie != null) {
            sqlCookie.setDatabaseConnection(dbconn);
            if (execute) {
                sqlCookie.execute();
            }
        } else {
            Logger.getLogger(SQLEditorProviderImpl.class.getName()).log(Level.INFO, "No SQLExecuteCookie found for " + sqlDo);
        }
    }
}
