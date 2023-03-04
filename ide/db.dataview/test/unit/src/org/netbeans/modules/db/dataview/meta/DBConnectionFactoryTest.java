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

package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBConnectionFactoryTest extends NbTestCase {
    
    public DBConnectionFactoryTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBConnectionFactoryTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new DBConnectionProviderImpl().getClass());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    //-------- Test Case ---------

    /**
     * Test of getConnection method, of class DBConnectionFactory.
     */
    public void testGetConnection() {
        try {
            DBConnectionFactory instance = DBConnectionFactory.getInstance();
            Connection result = instance.getConnection(DbUtil.getDBConnection());
            assertNotNull(result);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
