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
package org.netbeans.api.db.explorer.support;

import javax.swing.JComboBox;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;

/**
 *
 * @author Libor Kotouc, Andrei Badea
 */
public class DatabaseExplorerUIsTest extends TestBase {

    private DatabaseConnection dbconn1 = null;
    private DatabaseConnection dbconn2 = null;

    public DatabaseExplorerUIsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }
    
    private void clearConnections() throws Exception {
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (DatabaseConnection dc : connections) {
            ConnectionManager.getDefault().removeConnection(dc);
        }
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
    }
    
    private void initConnections() throws Exception {
        clearConnections();
        JDBCDriver driver = Util.createDummyDriver();
        dbconn1 = DatabaseConnection.create(driver, "db", "dbuser", "dbschema", "dbpassword", true);
        dbconn2 = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn1);
        ConnectionManager.getDefault().addConnection(dbconn2);
        assertEquals(2, ConnectionManager.getDefault().getConnections().length);
    }

    private JComboBox connect() {
        JComboBox combo = new JComboBox();
        DatabaseExplorerUIs.connect(combo, ConnectionManager.getDefault());
        return combo;
    }

    public void testEmptyComboboxContent() throws Exception {
        clearConnections();
        
        JComboBox combo = connect();

        forceFlush();
        
        assertEquals("Wrong number of items in the empty combobox", 1, combo.getItemCount());
    }

    public void testComboboxWithConnections() throws Exception {
        initConnections();
        JComboBox combo = connect();

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 3);

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));
    }

    public void testComboboxChangingConnections() throws Exception {
        initConnections();
        JComboBox combo = connect();

        assertEquals("Wrong number of items in the combobox", 3, combo.getItemCount());

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));

        DatabaseConnection dc = DatabaseConnection.create(Util.createDummyDriver(), "dc1", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dc);

        forceFlush();

        assertEquals("Wrong number of items in the combobox", 4, combo.getItemCount());

        assertSame(dc, combo.getItemAt(2));

        ConnectionManager.getDefault().removeConnection(dc);

        forceFlush();

        assertEquals("Wrong number of items in the combobox", 3, combo.getItemCount());

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));
    }
}
