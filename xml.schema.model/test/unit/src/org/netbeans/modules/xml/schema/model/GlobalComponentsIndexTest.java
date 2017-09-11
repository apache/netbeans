/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
