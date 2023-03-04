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

package org.netbeans.test.db.derby;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.db.derby.CreateJavaDBDatabaseOperator;
import org.netbeans.jellytools.modules.db.derby.actions.CreateDatabaseAction;
import org.netbeans.jellytools.modules.db.derby.actions.StopServerAction;
import org.netbeans.jellytools.modules.db.nodes.ConnectionNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.StartAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author lg198683
 */
public class CreateDatabaseTest  extends DbJellyTestCase {
    private static String USER="czesiu";
    private static String PASSWORD="czesiu";
    private static String DB="newdatabase";
    private static String URL="jdbc:derby://localhost:1527/newdatabase";
    
    public CreateDatabaseTest(String s) {
        super(s);
    }
    
    @Override
    public void setUp() {
        getDataDir().mkdirs();
        DerbyOptions.getDefault().setSystemHome(getDataDir().getAbsolutePath());
        String derbyLoc = System.getProperty("derby.location");
        if (derbyLoc == null || derbyLoc.length() == 0) {
            derbyLoc = getLocationInJDK(System.getProperty("java.home")).getAbsolutePath();
        }
        DerbyOptions.getDefault().setLocation(derbyLoc);
    }
    
    private static File getLocationInJDK(String javaHome) {
        File dir = new File(javaHome);
        assert dir != null && dir.exists() && dir.isDirectory() : "java.home is directory";
        // path to JavaDB in JDK6
        File loc = new File(dir.getParentFile(), "db"); // NOI18N
        return loc != null && loc.exists() && loc.isDirectory() ? loc : null;
    }

    public void testCreateDatabase(){        
        // <workaround for #112788> - FIXME
        SystemAction.get(StartAction.class).performAction();
        sleep(2000);
        // </workaround>
        
        JTreeOperator rtt = RuntimeTabOperator.invoke().tree();
        Node n = new Node(rtt, "Databases|Java DB");
        new ActionNoBlock(null, "Create Database").performPopup(n);
        
        CreateJavaDBDatabaseOperator operator = new CreateJavaDBDatabaseOperator();
        operator.setDatabaseName(DB);
        operator.setUserName(USER);
        operator.setPassword(PASSWORD);
        operator.ok();
        sleep(3000);
    }
    
    public void testConnect() throws Exception {
        ConnectionNode connection=ConnectionNode.invoke(URL,USER,PASSWORD);   
        connection.connect();
        sleep(2000);
        connection.disconnect();
        sleep(1000);
    }
        
    public void testStopServer(){
        JTreeOperator rtt = RuntimeTabOperator.invoke().tree();
        Node n = new Node(rtt, "Databases|Java DB");
        new ActionNoBlock(null, "Stop Server").performPopup(n);
        //new StopServerAction().perform();
        sleep(5000);
    }    
       
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateDatabaseTest.class)
                .addTest("testCreateDatabase", "testConnect", "testStopServer")
                .enableModules(".*")
                .clusters(".*")
                );
    }
}
