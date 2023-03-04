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

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Rob Englander
 */
public class DriverNode extends BaseNode {
    private static final String PREFERREDICONBASE = "org/netbeans/modules/db/resources/driverPrefered.gif";
    private static final String FOLDER = "Driver"; //NOI18N
    
    private DatabaseDriver databaseDriver;
    
    /** 
     * Create an instance of DriverNode.
     * 
     * @param dataLookup the lookup to use when creating node providers
     * @return the DriverNode instance
     */
    public static DriverNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        DriverNode node = new DriverNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private DriverNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
    }

    @Override
    protected void initialize() {
        StringBuffer sb = new StringBuffer();
        
        JDBCDriver driver = getLookup().lookup(JDBCDriver.class);
        for (int j = 0; j < driver.getURLs().length; j++) {
            if (j != 0)
                sb.append(", "); //NOI18N
            String file = driver.getURLs()[j].getFile();
            if (Utilities.isWindows())
                file = file.substring(1);
            sb.append(file);
        }
        
        databaseDriver = new DatabaseDriver(driver.getDisplayName(), driver.getClassName(), sb.toString(), driver);
    }

    public DatabaseDriver getDatabaseDriver() {
        return databaseDriver;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public void destroy() {
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    try {
                        JDBCDriver driver = databaseDriver.getJDBCDriver();
                        if (driver != null) {
                            JDBCDriverManager.getDefault().removeDriver(driver);
                        }
                    } catch (DatabaseException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        );
    }
    
    public String getName() {
        return databaseDriver.getName();
    }

    @Override
    public String getDisplayName() {
        return databaseDriver.getName();
    }
 
    public String getIconBase() {
        return PREFERREDICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (DriverNode.class, "ND_Driver"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DriverNode.class);
    }
}
