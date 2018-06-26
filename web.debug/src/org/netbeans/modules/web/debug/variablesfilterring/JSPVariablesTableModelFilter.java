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

package org.netbeans.modules.web.debug.variablesfilterring;

import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap;
import org.netbeans.spi.viewmodel.TableModel;

import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesTableModelFilter implements TableModelFilter {

    public JSPVariablesTableModelFilter() {
    }

    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param   original the original table model
     * @param   node a object returned from {@TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getValueAt (...)</code> method call only!
     *
     * @return value of variable representing given position in tree table.
     */
    public Object getValueAt(TableModel original, Object node, String columnID)
    throws UnknownTypeException
    {
        
        Object colValue = "";
        if (node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            colValue = original.getValueAt(((AttributeMap.Attribute)node).getValue(), columnID);
        else if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals)
            colValue = "";
        else
            colValue = original.getValueAt(node, columnID);
        
        return colValue;
    }
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.setValueAt (...)</code> method call only!
     */
    public void setValueAt(TableModel original, Object node, String columnID, Object value)
    throws UnknownTypeException
    {
            original.setValueAt(node, columnID, value);
    }

    /**
     * Filters original isReadOnly value from given table model. You should 
     * not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isReadOnly (...)</code> method call only!
     *
     * @return true if variable on given position is read only
     */
    public boolean isReadOnly(TableModel original, Object node, String columnID)
    throws UnknownTypeException
    {
        boolean ro = true;
        if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals ||
                 node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            ro = true;
        else
            ro = original.isReadOnly(node, columnID);
        
        return ro;
    }

    public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

    public void addModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

}
