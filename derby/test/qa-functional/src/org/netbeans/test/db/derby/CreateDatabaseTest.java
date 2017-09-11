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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
