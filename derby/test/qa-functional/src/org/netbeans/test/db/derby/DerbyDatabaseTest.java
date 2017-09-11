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
import java.sql.Connection;
import junit.framework.Test;
import org.netbeans.modules.derby.StartAction;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.StopAction;
import org.netbeans.test.db.util.DbUtil;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author lg198683
 */
public class DerbyDatabaseTest extends DbJellyTestCase {
      
    public DerbyDatabaseTest(String s) {
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

    public void testStartAction() {
       StartAction start=SystemAction.get(StartAction.class);
       start.performAction();
       // we need a support to detect that the DB started see: #112788
       sleep(15000);
    }
    
    public void testStopAction() throws Exception {
        StopAction stop=SystemAction.get(StopAction.class);
        stop.performAction();
    }
    
    public void testConnect() throws Exception{
        SystemAction.get(StartAction.class).performAction();
        sleep(2000);

        String url="jdbc:derby://localhost:1527/testdbtest;create=true";
        Connection con=DbUtil.createDerbyConnection(url);
        con.close();
   }
    
   public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(DerbyDatabaseTest.class)
                .addTest("testStartAction", "testConnect", "testStopAction")
                .enableModules(".*")
                .clusters(".*")
                );
    }
}
