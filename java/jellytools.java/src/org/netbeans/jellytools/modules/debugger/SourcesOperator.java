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

package org.netbeans.jellytools.modules.debugger;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.modules.debugger.actions.SourcesAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JComponentOperator;

/**
 * Provides access to the Sources top component.
 * <p>
 * Usage:<br>
 * <pre>
 *      SourcesOperator so = SourcesOperator.invoke();
 *      so.useSource("MyProject\\src", true);
 *      so.close();
 * </pre>
 * 
 * @author Jiri.Skrivanek@sun.com
 */
public class SourcesOperator extends TopComponentOperator {

    private static final SourcesAction invokeAction = new SourcesAction();
    
    /** Waits for Sessions top component and creates a new operator for it. */
    public SourcesOperator() {
        super(waitTopComponent(null,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle",
                                        "CTL_Sources_view"),
                0, viewSubchooser));
    }
    
    /**
     * Opens Sessions top component from main menu Window|Debugging|Sessions and
     * returns SourcesOperator.
     * 
     * 
     * @return instance of SourcesOperator
     */
    public static SourcesOperator invoke() {
        invokeAction.perform();
        return new SourcesOperator();
    }
    
    public TreeTableOperator treeTable() {
        return new TreeTableOperator(this);
    }
    
    /********************************** Actions ****************************/

    /** Returns true if source root is used for debugging and false otherwise.
     * @param source source root
     * @return true if source root is used for debugging; false otherwise
     */
    public boolean isUsed(String source) {
        int row = treeTable().findCellRow(source);
        // gets component used to render a value
        TableCellRenderer renderer = treeTable().getCellRenderer(row, 1);
        Component comp = renderer.getTableCellRendererComponent(
                                            (JTable)treeTable().getSource(),
                                            treeTable().getValueAt(row, 1),
                                            false, 
                                            false, 
                                            row, 
                                            1
        );
        String tooltip = new JComponentOperator((JComponent)comp).getToolTipText();
        return "true".equalsIgnoreCase(tooltip);
    }

    /** Sets or unsets source to be used for debugging.
     * @param source source root
     * @param state true to use source, false to not use
     */
    public void useSource(String source, boolean state) {
        if(isUsed(source) != state) {
            treeTable().clickOnCell(treeTable().findCellRow(source), 1);
        }
    }

    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser viewSubchooser = new ComponentChooser() {
        private static final String CLASS_NAME="org.netbeans.modules.debugger.jpda.ui.views.SourcesView";
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith(CLASS_NAME);
        }
        
        public String getDescription() {
            return "component instanceof "+CLASS_NAME;// NOI18N
        }
    };
}
