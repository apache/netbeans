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
