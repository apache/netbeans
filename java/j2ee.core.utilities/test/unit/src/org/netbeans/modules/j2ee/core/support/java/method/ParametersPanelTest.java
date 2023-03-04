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

package org.netbeans.modules.j2ee.core.support.java.method;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.support.java.method.ParametersPanel.ParamsTableModel;

/**
 *
 * @author Martin Adamek
 */
public class ParametersPanelTest extends NbTestCase {
    
    public ParametersPanelTest(String testName) {
        super(testName);
    }
    
    public void testParamsTableModel() {
        ParamsTableModel model = new ParamsTableModel(Arrays.asList(new MethodModel.Variable[] {
            MethodModel.Variable.create("java.lang.String", "name", false),
            MethodModel.Variable.create("java.lang.String", "address", true),
        }));
        assertEquals(3, model.getColumnCount());
        // column names
        assertEquals("Name", model.getColumnName(0));
        assertEquals("Type", model.getColumnName(1));
        assertEquals("Final", model.getColumnName(2));
        // everything should be editable
        assertTrue(model.isCellEditable(0, 0));
        assertTrue(model.isCellEditable(0, 1));
        assertTrue(model.isCellEditable(0, 2));
        // 3rd column should be rendered as check box
        assertEquals(Boolean.class, model.getColumnClass(2));
        // check set values
        assertEquals("name", model.getValueAt(0, 0));
        assertEquals("java.lang.String", model.getValueAt(0, 1));
        assertEquals(false, model.getValueAt(0, 2));
        // change values
        model.setValueAt("type", 0, 0);
        model.setValueAt("java.lang.Long", 0, 1);
        model.setValueAt(false, 0, 2);
        assertEquals("type", model.getValueAt(0, 0));
        assertEquals("java.lang.Long", model.getValueAt(0, 1));
        assertEquals(false, model.getValueAt(0, 2));
        // check configured parameters
        List<MethodModel.Variable> parameters = model.getParameters();
        assertEquals(2, parameters.size());
        MethodModel.Variable parameter = parameters.get(0);
        assertEquals("type", parameter.getName());
        assertEquals("java.lang.Long", parameter.getType());
        assertEquals(false, parameter.getFinalModifier());
    }
    
}
