/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.test.DBTestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.WizardDescriptor;

public class AddConnectionWizardTest extends DBTestBase {

    private static final Logger LOG = Logger.getLogger(AddConnectionWizardTest.class.getName());
    
    public AddConnectionWizardTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Util.clearConnections();
        Util.deleteDriverFiles();
    }

    public void testAddConnectionWizdardReturnsNullOnCancel() throws Exception {
        final CountDownLatch finalLock = new CountDownLatch(1);
        final JDBCDriver driver = Util.createDummyDriver();
        final DatabaseConnection dummyConnection = new DatabaseConnection(driver.getName(), 
                driver.getClassName(), "database", "schema", "user", "password", true);
        
        final DatabaseConnection[] result = new DatabaseConnection[]{dummyConnection};
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AddConnectionWizard wiz = reflectiveConstruct(AddConnectionWizard.class,
                            new Class[] {JDBCDriver.class, String.class, String.class, String.class},
                            new Object[] {driver, "jdbc:mysql://dummy", "dummy", "dummy"});
                    
                    wiz.setDatabaseConnection(dummyConnection);
                    
                    WizardDescriptor wd = reflectiveFieldGet(AddConnectionWizard.class, wiz, "wd");
                    wd.doCancelClick();
                    
                    result[0] = reflectiveCall(AddConnectionWizard.class, wiz, "getResult", new Class[0], new Object[0]);
                } catch (Throwable throwable) {
                    LOG.log(Level.SEVERE, throwable.getLocalizedMessage(), throwable);
                } finally {
                    finalLock.countDown();
                } 
            }
        });
        
        finalLock.await();
        
        assertThat(ConnectionManager.getDefault().getConnections().length, is(0));
        assertNull(result[0]);
    }

    
    public void testAddConnectionWizdardReturnsNonNullOnFinish() throws Exception {
        final CountDownLatch finalLock = new CountDownLatch(1);
        final JDBCDriver driver = Util.createDummyDriver();
        final DatabaseConnection dummyConnection = new DatabaseConnection(driver.getName(), 
                driver.getClassName(), "database", "schema", "user", "password", true);
        
        final DatabaseConnection[] result = new DatabaseConnection[1];
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AddConnectionWizard wiz = reflectiveConstruct(AddConnectionWizard.class,
                            new Class[] {JDBCDriver.class, String.class, String.class, String.class},
                            new Object[] {driver, "jdbc:mysql://dummy", "dummy", "dummy"});
                    
                    wiz.setDatabaseConnection(dummyConnection);
                    
                    WizardDescriptor wd = reflectiveFieldGet(AddConnectionWizard.class, wiz, "wd");
                    wd.doFinishClick();
                    
                    result[0] = reflectiveCall(AddConnectionWizard.class, wiz, "getResult", new Class[0], new Object[0]);
                } catch (Throwable throwable) {
                    LOG.log(Level.SEVERE, throwable.getLocalizedMessage(), throwable);
                } finally {
                    finalLock.countDown();
                }
            }
        });
        
        finalLock.await();
        
        assertThat(ConnectionManager.getDefault().getConnections().length, is(1));
        assertNotNull(result[0]);
    }
    
    private <T> T reflectiveConstruct(Class<T> clazz, Class[] signature, Object[] params) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor(signature);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(params);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private <T> T reflectiveFieldGet(Class clazz, Object instance, String fieldname) {
        try {
            Field f = clazz.getDeclaredField(fieldname);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private <T> T reflectiveCall(Class clazz, Object instance, String name, Class[] signature, Object[] params) {
        try {
            Method m = clazz.getDeclaredMethod(name, signature);
            m.setAccessible(true);
            return (T) m.invoke(instance, params);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
