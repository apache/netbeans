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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.Date;
import javax.swing.SortOrder;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Mutex;

/**
 * Test for InsertRecordTableUI
 */
public class InsertRecordTableUITest extends NbTestCase {

    Connection conn;
    DatabaseConnection dbconn;
    TestCaseContext context;

    public InsertRecordTableUITest(String testName) {
        super(testName);
    }

    @Override
    public boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBTestUtil.suppressSuperfluousLogging();
        MockServices.setServices(new DBConnectionProviderImpl().getClass());
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn = null;
    }

    //--------------------- Test Case ---------------------
    public void testCreate() {
        String sqlString = context.getSqlSelect();
        final DataView dv = DataView.create(dbconn, sqlString, 5);

        Mutex.EVENT.writeAccess(new Mutex.Action<Object>() {
            @Override
            public Void run() {
                dv.createComponents();

                DataViewPageContext pageContext = dv.getPageContext(0);
                DBTable table = pageContext.getTableMetaData().getTable(0);

                InsertRecordDialog ird = new InsertRecordDialog(dv, pageContext, table);

                ResultSetTableModel model = ird.insertRecordTableUI.getModel();
                ird.insertRecordTableUI.appendEmptyRow();
                ird.insertRecordTableUI.appendEmptyRow();

                // Column 5 is the date column => Insert a "real" Date
                // => creates conflict with String inserted by "createNewRow"
                ird.insertRecordTableUI.setValueAt(
                        new Date(new java.util.Date().getTime()), 1, 5);
                try {
                    ird.insertRecordTableUI.setSortOrder(5, SortOrder.ASCENDING);
                } catch (ClassCastException ex) {
                    assert false : "Bug 219011 - should not be reached!";
                }
                return null;
            }
        });
    }
}
