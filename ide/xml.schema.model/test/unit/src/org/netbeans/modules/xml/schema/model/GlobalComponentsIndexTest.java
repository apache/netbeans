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

package org.netbeans.modules.xml.schema.model;

import java.text.DecimalFormat;
import javax.swing.undo.UndoManager;
import org.junit.Test;
import org.junit.After;
import org.netbeans.modules.xml.schema.model.impl.GlobalComponentsIndexSupport;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import static org.junit.Assert.*;

/**
 * The test for class GloablComponentsIndexSupport. 
 *
 * @author Nikita Krjukov
 */
public class GlobalComponentsIndexTest {

    @After
    public void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * Checks the optimization related to issue #169515
     */
    @Test
    public void testPerformance1() throws Exception {
        SchemaModel sm;
        //
        // sm = Util.loadSchemaModel2("resources/performance2/A.xsd"); // NOI18N
        sm = Util.loadSchemaModel2("resources/performance2.zip", "A.xsd"); // NOI18N
        //
        // Wait 1 second till all models are loaded and validated
        Thread.sleep(1000);
        //
        assertTrue(sm.getState() == State.VALID);
        //
        assertTrue(sm instanceof SchemaModelImpl);
        SchemaModelImpl smImpl = SchemaModelImpl.class.cast(sm);
        GlobalComponentsIndexSupport indexSupport = smImpl.getGlobalComponentsIndexSupport();
        assertNotNull(indexSupport != null);
        GlobalComponentsIndexSupport.JUnitTestSupport testSupport =
                indexSupport.getJUnitTestSupport();
        assertNotNull(testSupport != null);
        //
        // Initiate index building
        GlobalElement found = sm.findByNameAndType("A000", GlobalElement.class);
        assertNotNull(found);
        Thread.sleep(500); // Wait the index is build
        //
        DecimalFormat format = new DecimalFormat("000");
        //
        long before = System.nanoTime();
        for (int index = 0; index < 1000; index++) {
            String localName = "A" + format.format(index);
            found = sm.findByNameAndType(localName, GlobalElement.class);
            assertNotNull(found);
        }
        long after = System.nanoTime();
        long delay = (after - before) / 1000000;
        assertTrue("Delay=" + delay, delay < 50L);
        System.out.println("Execution with index = " + delay + " ms"); // NOI18N
        //
        testSupport.setIndexAllowed(false);
        before = System.nanoTime();
        for (int index = 0; index < 1000; index++) {
            String localName = "A" + format.format(index);
            found = sm.findByNameAndType(localName, GlobalElement.class);
            assertNotNull(found);
        }
        //
        after = System.nanoTime();
        delay = (after - before) / 1000000;
        assertTrue("Delay=" + delay, delay < 1000L);
        System.out.println("Execution without index = " + delay + " ms"); // NOI18N
        //
        System.out.println("=============LOG============="); // NOI18N
        String log = testSupport.printLog();
        System.out.print(log);
        System.out.println("============================="); // NOI18N
        //
    }

    /**
     * Checks the index is created after reaching threshold amount of
     * global components and also the index removed if the amound goes down
     * below another threshold.
     */
    @Test
    public void testIndexCreationThreshold() throws Exception {
        SchemaModel sm;
        //
        // sm = Util.loadSchemaModel2("resources/performance2/B.xsd"); // NOI18N
        sm = Util.loadSchemaModel2("resources/performance2.zip", "B.xsd"); // NOI18N
        //
        assertTrue(sm.getState() == State.VALID);
        //
        assertTrue(sm instanceof SchemaModelImpl);
        SchemaModelImpl smImpl = SchemaModelImpl.class.cast(sm);
        GlobalComponentsIndexSupport indexSupport = smImpl.getGlobalComponentsIndexSupport();
        assertNotNull(indexSupport);
        GlobalComponentsIndexSupport.JUnitTestSupport testSupport =
                indexSupport.getJUnitTestSupport();
        assertNotNull(testSupport);
        //
        // Initiate index building
        GlobalElement found = sm.findByNameAndType("B000", GlobalElement.class);
        assertNotNull(found);
        Thread.sleep(500); // Wait the index is build
        //
        assertTrue(testSupport.isSupportIndex());
        int indexSise = testSupport.getIndexSize();
        assertEquals(indexSise, 90);
        //
        UndoManager um = new javax.swing.undo.UndoManager();
        AbstractDocumentModel.class.cast(sm).addUndoableEditListener(um);
        //
        sm.startTransaction();
        try {
            Schema schema = sm.getSchema();
            java.util.List<SchemaComponent> gChildren = schema.getChildren();
            int counter = 0;
            for (SchemaComponent child : gChildren) {
                //
                assertTrue(child instanceof GlobalElement);
                GlobalElement gElem = GlobalElement.class.cast(child);
                schema.removeElement(gElem);
                //
                counter++;
                if (counter >= 50) {
                    break;
                }
            }
        } finally {
            sm.endTransaction();
        }
        //
        int childrenCount = sm.getSchema().getChildren().size();
        assertEquals(childrenCount, 40);
        assertFalse(testSupport.isSupportIndex());
        //
        //
        um.undo();
        //
        // Initiate index building again
        found = sm.findByNameAndType("B000", GlobalElement.class);
        assertNotNull(found);
        Thread.sleep(500); // Wait the index is build
        //
        assertTrue(testSupport.isSupportIndex());
        indexSise = testSupport.getIndexSize();
        assertEquals(indexSise, 90);
        //
        System.out.println("============================="); // NOI18N
        System.out.println(" testIndexCreationThreshold  "); // NOI18N
        System.out.println("=============LOG============="); // NOI18N
        String log = testSupport.printLog();
        System.out.print(log);
        System.out.println("============================="); // NOI18N
        //
    }

    /**
     * A schema can have global components with the same name (but different tipes).
     * It such case the components' index contains not a component but a list of comonent.
     * The test checks how it works.
     */
    @Test
    public void testIndexWithMiltiComponentItem() throws Exception {
        SchemaModel sm;
        //
        // sm = Util.loadSchemaModel2("resources/performance2/C.xsd"); // NOI18N
        sm = Util.loadSchemaModel2("resources/performance2.zip", "C.xsd"); // NOI18N
        //
        assertTrue(sm.getState() == State.VALID);
        //
        assertTrue(sm instanceof SchemaModelImpl);
        SchemaModelImpl smImpl = SchemaModelImpl.class.cast(sm);
        GlobalComponentsIndexSupport indexSupport = smImpl.getGlobalComponentsIndexSupport();
        assertNotNull(indexSupport);
        GlobalComponentsIndexSupport.JUnitTestSupport testSupport =
                indexSupport.getJUnitTestSupport();
        assertNotNull(testSupport);
        //
        // Initiate index building
        GlobalElement gElem = sm.findByNameAndType("C000", GlobalElement.class);
        assertNotNull(gElem);
        Thread.sleep(500); // Wait the index is build
        //
        assertTrue(testSupport.isSupportIndex());
        int indexSise = testSupport.getIndexSize();
        assertEquals(indexSise, 90);
        //
        GlobalComplexType gType = sm.findByNameAndType("C000", GlobalComplexType.class);
        assertNotNull(gType);
        //
        GlobalAttribute gAttr = sm.findByNameAndType("C000", GlobalAttribute.class);
        assertNotNull(gAttr);
        //
        GlobalAttributeGroup gAttrGroup =
                sm.findByNameAndType("C000", GlobalAttributeGroup.class);
        assertNotNull(gAttrGroup);
        //
        GlobalGroup gGroup = sm.findByNameAndType("C000", GlobalGroup.class);
        assertNotNull(gGroup);
        //
        System.out.println("============================="); // NOI18N
        System.out.println(" testIndexWithMiltiComponentItem "); // NOI18N
        System.out.println("=============LOG============="); // NOI18N
        String log = testSupport.printLog();
        System.out.print(log);
        System.out.println("============================="); // NOI18N
        //
    }


}
