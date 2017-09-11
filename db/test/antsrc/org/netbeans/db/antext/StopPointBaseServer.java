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

package org.netbeans.db.antext;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class implementing way how to shutdown PointDase server.
 * Necessary system properties: <tt>pointbase.db.driverclass</tt>, <tt>pointbase.db.url</tt>, <tt>pointbase.db.user</tt>, <tt>pointbase.db.password</tt>.
 * This class is called from <tt>pointbase-server.xml</tt> script.
 *
 * @author Patrik Knakal
 * @version 1.0
 */
public class StopPointBaseServer extends Object {
    private static final String shutdown = "shutdown force"; //NOI18N

    public static void main(String[] atrg) {
        //System.out.println("driverclass ... " + System.getProperty("pointbase.db.driverclass") ); //NOI18N
        //System.out.println("url         ... " + System.getProperty("pointbase.db.url") ); //NOI18N
        //System.out.println("user        ... " + System.getProperty("pointbase.db.user") ); //NOI18N
        //System.out.println("password    ... " + System.getProperty("pointbase.db.password") ); //NOI18N
        try {
            Class.forName(System.getProperty("pointbase.db.driverclass") ); //NOI18N
            Connection connection = DriverManager.getConnection(System.getProperty("pointbase.db.url"), System.getProperty("pointbase.db.user"), System.getProperty("pointbase.db.password") ); //NOI18N
            Statement statement = connection.createStatement();
            statement.executeUpdate(shutdown);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
