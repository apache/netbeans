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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Color;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=TableHTMLModelFilter.class,
                                 position=50)
})
public class VariablesTableHTMLModel implements TableHTMLModelFilter, Constants {

    @Override
    public boolean hasHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (original.hasHTMLValueAt(node, columnID)) {
            return true;
        }
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorValueMsg(((Variable) node));
                if (errorMsg != null) {
                    return true;
                }
            }
        }
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorToStringMsg(((Variable) node));
                if (errorMsg != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (original.hasHTMLValueAt(node, columnID)) {
            return original.getHTMLValueAt(node, columnID);
        }
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorValueMsg(((Variable) node));
                if (errorMsg != null) {
                    return BoldVariablesTableModelFilter.toHTML(">" + errorMsg + "<", false, false, Color.RED);
                }
            }
        }
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorToStringMsg(((Variable) node));
                if (errorMsg != null) {
                    return BoldVariablesTableModelFilter.toHTML(">" + errorMsg + "<", false, false, Color.RED);
                }
            }
        }
        return original.getHTMLValueAt(node, columnID);
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}
