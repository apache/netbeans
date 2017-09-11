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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.text.MessageFormat;
import java.util.*;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;
import org.openide.util.Exceptions;

public class CaptureSchema {

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    private final String defaultName = bundle.getString("DefaultSchemaName"); //NOI18N
    private DBSchemaWizardData data;
    
    /** Creates new CaptureSchema */
    public CaptureSchema(DBSchemaWizardData data) {
        this.data = data;
    }

    protected void start() {
        //getting values from package panel
        String target = data.getName();
        DataFolder folder = data.getDestinationPackage();
        
        //getting values from table selection panel
        final LinkedList tables = data.getTables();
        final LinkedList views = data.getViews();
        final boolean allTables = data.isAllTables();
        
        ConnectionProvider cp = data.getConnectionProvider();
        
        try {
            final ConnectionProvider c = cp;
            
            final FileObject fo = folder.getPrimaryFile();
            if (target == null || target.equals("")) //NOI18N
                target = FileUtil.findFreeFileName(fo, defaultName, "dbschema"); //NOI18N
            
            final boolean conned = data.isConnected();
            final boolean ec = data.isExistingConn();
            final DatabaseConnection dbconn = data.getDatabaseConnection();
            final String target1 = target;
            
            //delete values from panels
            data = null;
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    try {
                        StatusDisplayer.getDefault().setStatusText(bundle.getString("CreatingDatabaseSchema")); //NOI18N
                        
                        final ProgressFrame pf = new ProgressFrame();
                        final SchemaElementImpl sei = new SchemaElementImpl(c);
                        
                        PropertyChangeListener listener = new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent event) {
                                String message;
                                
                                if (event.getPropertyName().equals("totalCount")) { //NOI18N
                                    pf.setMaximum(((Integer)event.getNewValue()).intValue());
                                    return;
                                }

                                if (event.getPropertyName().equals("progress")) { //NOI18N
                                    pf.setValue(((Integer)event.getNewValue()).intValue());
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("tableName")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CapturingTable"), new String[] {((String) event.getNewValue()).toUpperCase()}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKt")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CaptureFK"), new String[] {((String) event.getNewValue()).toUpperCase(), bundle.getString("CaptureFKtable")}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKv")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CaptureFK"), new String[] {((String) event.getNewValue()).toUpperCase(), bundle.getString("CaptureFKview")}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("viewName")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CapturingView"), new String[] {((String) event.getNewValue()).toUpperCase()}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("cancel")) { //NOI18N
                                    sei.setStop(true);
                                    StatusDisplayer.getDefault().setStatusText(""); //NOI18N
                                    return;
                                }
                            }
                        };
                        
                        pf.propertySupport.addPropertyChangeListener(listener);
                        pf.setVisible(true);
                        
                        sei.propertySupport.addPropertyChangeListener(listener);
                        final SchemaElement se = new SchemaElement(sei);
                        se.setName(DBIdentifier.create(target1));
                        if (allTables)
                            sei.initTables(c, tables, views, true);
                        else
                            sei.initTables(c, tables, views, false);
                        pf.finishProgress();
                        
                        if (! sei.isStop()) {
                            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                public void run() throws java.io.IOException {
                                    FileObject fo1 = fo.createData(target1, "dbschema"); //NOI18N
                                    FileLock fl = fo1.lock();
                                    java.io.OutputStream out = fo1.getOutputStream(fl);
                                    if (out == null)
                                        throw new java.io.IOException("Unable to open output stream");

                                    pf.setMessage(bundle.getString("SavingDatabaseSchema")); //NOI18N
                                    StatusDisplayer.getDefault().setStatusText(bundle.getString("SavingDatabaseSchema")); //NOI18N

                                    se.save(out);
                                    fl.releaseLock();
                                }
                            });
                            
                            pf.setMessage(bundle.getString("SchemaSaved")); //NOI18N
                            StatusDisplayer.getDefault().setStatusText(bundle.getString("SchemaSaved")); //NOI18N
                            
                            pf.setVisible(false);
                            pf.dispose();                        
                        }
                        
                        if (conned)
                            if (ec)
                                ConnectionManager.getDefault().disconnect(dbconn);
                            else
                                c.closeConnection();
                    } catch (Exception exc) {
                        Exceptions.printStackTrace(exc);
                    }
                }
            }, 0);
        } catch (Exception exc) {
            String message = MessageFormat.format(bundle.getString("UnableToCreateSchema"), new String[] {exc.getMessage()}); //NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            
            try {
                if (cp != null)
                    if (data.isConnected())
                        if (data.isExistingConn())
                            ConnectionManager.getDefault().disconnect(data.getDatabaseConnection());
                        else
                            cp.closeConnection();
            } catch (Exception exc1) {
                //unable to disconnect
            }
        }
    }
}
