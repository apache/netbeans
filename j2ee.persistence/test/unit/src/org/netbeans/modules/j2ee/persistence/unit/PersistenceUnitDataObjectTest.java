/**
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


package org.netbeans.modules.j2ee.persistence.unit;

import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;

/**
 * Tests for the persistence unit data object.
 * @author Erno Mononen
 */
public class PersistenceUnitDataObjectTest extends PersistenceEditorTestBase{
    
    public PersistenceUnitDataObjectTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PersistenceUnitDataObjectTest.class);
        return suite;
    }
    
    public void testAddPersistenceUnit() throws Exception{
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit persistenceUnit = Persistence.VERSION_1_0.equals(version) ?
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit() :
            Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit() :
            new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        persistenceUnit.setName("em3");
        persistenceUnit.setJtaDataSource("jdbc/__default");
        dataObject.addPersistenceUnit(persistenceUnit);
        
        assertTrue(containsUnit(persistenceUnit));
        assertTrue(dataCacheContains("\"em3\""));
        assertTrue(dataCacheContains("<jta-data-source>jdbc/__default"));
    }
    
    public void testRemovePersistenceUnit()throws Exception{
        int originalSize = dataObject.getPersistence().getPersistenceUnit().length;
        PersistenceUnit toBeRemoved = dataObject.getPersistence().getPersistenceUnit(0);
        String name = toBeRemoved.getName();
        dataObject.removePersistenceUnit(toBeRemoved);
        assertFalse(containsUnit(toBeRemoved));
        assertTrue(dataObject.getPersistence().getPersistenceUnit().length == originalSize -1);
        assertFalse(dataCacheContains("name=\"" + name + "\""));
    }
    
    public void testChangeName() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String oldName = persistenceUnit.getName();
        String newName = "new name";
        persistenceUnit.setName(newName);
        dataObject.modelUpdatedFromUI();
        assertTrue(dataCacheContains("\"" + newName + "\""));
        assertFalse(dataCacheContains("\"" + oldName + "\""));
    }

    public void testChangeDatasource() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String newDatasource = "jdbc/new_datasource";
        persistenceUnit.setJtaDataSource(newDatasource);
        dataObject.modelUpdatedFromUI();
        assertEquals(newDatasource, persistenceUnit.getJtaDataSource());
        assertTrue(dataCacheContains(newDatasource));
    }

    public void testAddClass() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz, false);
        assertTrue(dataCacheContains(clazz));
    }
    
    public void testRemoveClass() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz, false);
        assertTrue(dataCacheContains(clazz));
        dataObject.removeClass(persistenceUnit, clazz, false);
        assertFalse(dataCacheContains(clazz));
    }

    public void testAddMultipleClasses() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        String clazz2 = "com.foo.bar.FooClass2";
        String clazz3 = "com.foo.bar.FooClass3";
        dataObject.addClass(persistenceUnit, clazz, false);
        dataObject.addClass(persistenceUnit, clazz2, false);
        dataObject.addClass(persistenceUnit, clazz3, false);
        assertTrue(dataCacheContains(clazz));
        assertTrue(dataCacheContains(clazz2));
        assertTrue(dataCacheContains(clazz3));
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    
    
}
