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

import javax.swing.undo.UndoManager;
import org.junit.Test;
import org.junit.After;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import static org.junit.Assert.*;

/**
 * See the issue #169435
 *
 * @author Nikita Krjukov
 */
public class TargetNamespaceCachingTest {

    @After
    public void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
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
        UndoManager um = new javax.swing.undo.UndoManager();
        AbstractDocumentModel.class.cast(sm).addUndoableEditListener(um);
        //
        Schema schema = sm.getSchema();
        String initialTargetNamespace = schema.getTargetNamespace();
        String targetNamspace = initialTargetNamespace;
        assertEquals(targetNamspace, "hl7_performance_test");
        //
        String newTargetNamespace = "newTargetNamespace";
        String newTargetNamespace2 = "newTargetNamespace2";
        sm.startTransaction();
        try {
            schema.setTargetNamespace(newTargetNamespace);
            //
            targetNamspace = schema.getTargetNamespace();
            assertEquals(targetNamspace, newTargetNamespace);
            //
            schema.setTargetNamespace(newTargetNamespace2);
            //
            targetNamspace = schema.getTargetNamespace();
            assertEquals(targetNamspace, newTargetNamespace2);
        } finally {
            sm.endTransaction();
        }
        //
        targetNamspace = schema.getTargetNamespace();
        assertEquals(targetNamspace, newTargetNamespace2);
        //
        um.undo();
        //
        targetNamspace = schema.getTargetNamespace();
        assertEquals(targetNamspace, initialTargetNamespace);
    }

}
