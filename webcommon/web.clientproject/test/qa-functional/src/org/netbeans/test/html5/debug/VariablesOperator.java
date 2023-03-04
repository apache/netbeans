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
package org.netbeans.test.html5.debug;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Operator for Debugging|Variables window
 *
 * @author Vladimir Riha
 */
public class VariablesOperator extends TopComponentOperator {

    public VariablesOperator(String name) {
        super(VariablesOperator.makeVisible(name));
    }

    private static String makeVisible(String name) {
        new Action("Window|Debugging|Variables", null).perform();
        return name;
    }
    private JTableOperator _tblVariables;

    /**
     * Tries to find null OutlineView$OutlineViewOutline in this dialog.
     *
     * @return JTableOperator
     */
    public JTableOperator tblVariables() {
        if (_tblVariables == null) {
            _tblVariables = new JTableOperator(this);
        }
        return _tblVariables;
    }

    /**
     * Returns Map of variables where key is a variable's name and value is
     * instance of {@link Variable}
     *
     * @return variables
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object getVariables() throws Exception {
        return runMapping(new MapAction("getVariables") {
            @Override
            public Object map() throws Exception {
                TableModel tm = tblVariables().getModel();
                org.openide.nodes.Node.Property type, value;
                Map<String, Variable> variables = new HashMap<String, Variable>();
                String name;
                for (int i = 0, max = tm.getRowCount(); i < max; i++) {
                    name = tm.getValueAt(i, 0).toString();
                    if (name.length() > 0) {
                        type = (org.openide.nodes.Node.Property) tm.getValueAt(i, 1);
                        value = (org.openide.nodes.Node.Property) tm.getValueAt(i, 2);
                        variables.put(name, new Variable(name, type.getValue().toString(), value.getValue().toString()));
                    }
                }
                return variables;
            }
        });
    }
}

class Variable {

    public String name;
    public String type;
    public String value;

    public Variable(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name + "|" + this.type + "|" + this.value;
    }
}