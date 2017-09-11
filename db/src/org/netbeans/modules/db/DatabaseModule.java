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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.modules.ModuleInstall;

public class DatabaseModule extends ModuleInstall {

    public static final String IDENTIFIER_MYSQL = "MySQL"; // NOI18N
    public static final String IDENTIFIER_ORACLE = "Oracle"; // NOI18N
    public static final String IDENTIFIER_ORACLE_OCI_DRIVER = "OCI"; // NOI18N
    
    @Override
    public void close () {
        // XXX this method is called in the event thread and could take long
        // to execute

        if (ConnectionList.getDefault(false) != null) {
            DBConnection[] conns = ConnectionList.getDefault().getConnections();
            for (int i = 0; i < conns.length; i++) {
                try {
                    ((DatabaseConnection)conns[i]).disconnect();
                } catch (Exception e) {
                    // cf. issue 64185 exceptions should only be logged
                    Logger.getLogger(DatabaseModule.class.getName()).log(Level.INFO, null, e);
                }
            }
        }

        // stop all running runtimes
        if (DatabaseRuntimeManager.isInstantiated()) {
            DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
            for (int i = 0; i < runtimes.length; i++) {
                if (runtimes[i].isRunning()) {
                    try {
                        runtimes[i].stop();
                    } catch (Exception e) {
                        // cf. issue 64185 exceptions should only be logged
                        Logger.getLogger(DatabaseModule.class.getName()).log(Level.INFO, null, e);
                    }
                }
            }
        }
    }
}
