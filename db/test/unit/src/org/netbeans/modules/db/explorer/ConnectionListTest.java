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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer;

import java.lang.ref.WeakReference;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionListTest extends TestBase {

    public ConnectionListTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }
    
    /**
     * Tests that ConnectionManager manages the same instance that was
     * added using the {@link ConnectionManager#addConnection} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.clearConnections();
        assertEquals(0, ConnectionList.getDefault().getConnections().length);

        DatabaseConnection dbconn = new DatabaseConnection("org.bar.BarDriver",
                "bar_driver", "jdbc:bar:localhost", "schema", "user", "password", true);
        // We are testing ConnectionList.addConnection(), but that doesn't return a DataObject.
        DataObject dbconnDO = DatabaseConnectionConvertor.create(dbconn);

        WeakReference<DataObject> dbconnDORef = new WeakReference<DataObject>(dbconnDO);
        dbconnDO = null;
        for (int i = 0; i < 50; i++) {
            System.gc();
            if (dbconnDORef.get() == null) {
                break;
            }
        }

        assertEquals(1, ConnectionList.getDefault().getConnections().length);

        // This used to fail as described in issue 75204.
        assertSame(dbconn, ConnectionList.getDefault().getConnections()[0]);

        Util.clearConnections();
        WeakReference<DatabaseConnection> dbconnRef = new WeakReference<DatabaseConnection>(dbconn);
        dbconn = null;
        assertGC("Should be able to GC dbconn", dbconnRef);
    }
}
