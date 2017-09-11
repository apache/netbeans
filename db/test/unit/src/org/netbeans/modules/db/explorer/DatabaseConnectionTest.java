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

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.db.test.DBTestBase;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseConnectionTest extends DBTestBase {

    public DatabaseConnectionTest(String testName) {
        super(testName);
    }

    public void testPropertyChange() {

        DatabaseConnection dbconn = new DatabaseConnection();

        MyPCL pcl = new MyPCL();
        dbconn.addPropertyChangeListener(pcl);
        
        dbconn.setDriver("driver");
        dbconn.setDatabase("database");
        dbconn.setSchema("schema");
        dbconn.setUser("user");
        
        assertTrue("Not all the property changes were fired", pcl.fired >= 4);
    }
    
    private final class MyPCL implements PropertyChangeListener {
        int fired = 0;
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(DatabaseConnection.PROP_DRIVER)) {
                fired++;
                assertEquals("driver", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_DATABASE)) {
                fired++;
                assertEquals("database", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_SCHEMA)) {
                fired++;
                assertEquals("schema", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_USER)) {
                fired++;
                assertEquals("user", evt.getNewValue());
            }
        }
    }
}
