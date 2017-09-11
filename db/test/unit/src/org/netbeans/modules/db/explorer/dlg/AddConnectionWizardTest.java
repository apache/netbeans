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

package org.netbeans.modules.db.explorer.dlg;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.xml.ws.Holder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.Util;
import org.netbeans.modules.db.test.DBTestBase;
import org.openide.WizardDescriptor;

public class AddConnectionWizardTest extends DBTestBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Util.clearConnections();
    }
    
    public AddConnectionWizardTest(String testName) {
        super(testName);
    }

    public void testAddConnectionWizdardReturnsNullOnCancel() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();
        
        final CountDownLatch finalLock = new CountDownLatch(1);
        final JDBCDriver driver = Util.createDummyDriver();
        final org.netbeans.modules.db.explorer.DatabaseConnection dummyConnection 
                = new org.netbeans.modules.db.explorer.DatabaseConnection(
                driver.getName(),
                driver.getClassName(),
                "database", "schema", "user", "password", true);
        
        final Holder<DatabaseConnection> result = new Holder<>();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                    AddConnectionWizard wiz = reflectiveConstruct(AddConnectionWizard.class,
                            new Class[] {JDBCDriver.class, String.class, String.class, String.class},
                            new Object[] {driver, "jdbc:mysql://dummy", "dummy", "dummy"});
                    
                    wiz.setDatabaseConnection(dummyConnection);
                    
                    WizardDescriptor wd = reflectiveFieldGet(AddConnectionWizard.class, wiz, "wd");
                    wd.doCancelClick();
                    
                    result.value = reflectiveCall(AddConnectionWizard.class, wiz, "getResult", new Class[0], new Object[0]);

                    finalLock.countDown();
            }
        });
        
        finalLock.await();
        
        assertThat(ConnectionManager.getDefault().getConnections().length, is(0));
        assertNull(result.value);
    }

    
    public void testAddConnectionWizdardReturnsNonNullOnFinish() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();
        
        final CountDownLatch finalLock = new CountDownLatch(1);
        final JDBCDriver driver = Util.createDummyDriver();
        final org.netbeans.modules.db.explorer.DatabaseConnection dummyConnection 
                = new org.netbeans.modules.db.explorer.DatabaseConnection(
                driver.getName(),
                driver.getClassName(),
                "database", "schema", "user", "password", true);
        
        final Holder<DatabaseConnection> result = new Holder<>();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                    AddConnectionWizard wiz = reflectiveConstruct(AddConnectionWizard.class,
                            new Class[] {JDBCDriver.class, String.class, String.class, String.class},
                            new Object[] {driver, "jdbc:mysql://dummy", "dummy", "dummy"});
                    
                    wiz.setDatabaseConnection(dummyConnection);
                    
                    WizardDescriptor wd = reflectiveFieldGet(AddConnectionWizard.class, wiz, "wd");
                    wd.doFinishClick();
                    
                    result.value = reflectiveCall(AddConnectionWizard.class, wiz, "getResult", new Class[0], new Object[0]);

                    finalLock.countDown();
            }
        });
        
        finalLock.await();
        
        assertThat(ConnectionManager.getDefault().getConnections().length, is(1));
        assertNotNull(result.value);
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
    
    private <T> T reflectiveFieldSet(Class clazz, Object instance, String fieldname) {
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
