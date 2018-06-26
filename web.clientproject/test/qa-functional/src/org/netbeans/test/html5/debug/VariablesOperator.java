/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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