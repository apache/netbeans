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
