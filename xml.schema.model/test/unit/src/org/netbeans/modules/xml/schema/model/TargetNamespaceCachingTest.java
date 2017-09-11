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
