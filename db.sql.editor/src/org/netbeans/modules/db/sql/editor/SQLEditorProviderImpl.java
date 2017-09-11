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

package org.netbeans.modules.db.sql.editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
            String name = MessageFormat.format(nameFmt, new Object[] { new Integer(i) });
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
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8")); // NOI18N
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
