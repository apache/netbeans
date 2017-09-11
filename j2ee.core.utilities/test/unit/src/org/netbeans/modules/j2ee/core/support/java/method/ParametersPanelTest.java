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
