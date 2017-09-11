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
package org.netbeans.swing.outline;

/** A model for the rows in an Outline.  This is passed the object in
 * column 0 of an Outline table (the tree column), and provides objects
 * for the other columns - essentially a model for the data in the
 * rows of an Outline.
 * <p>
 * Note that all column indexes passed to this interface are 0-based -
 * that is, column 0 is the first column <strong>after</strong> the
 * tree node column, so the object returned by <code>getValueFor(someObject, 0)</code>
 * is the object that should appear in column <strong>1</strong> of the
 * actual table.
 * <p>
 *
 * @author Tim Boudreau
 */
public interface RowModel {
    /** Get the column count.  Do not include the base (nodes) column
     * of the Outline, only the number of columns in addition to it
     * that should be displayed. 
     * @return the number of columns this model will contribute to the
     *  OutlineModel, not including the tree column */
    public int getColumnCount();
    /** Get the value at a given column.  
     * @param node The node in column 0 of the Outline
     * @param column The index of the column minus the nodes column  
     * @return the value that should be displayed in the specified column,
     *  given the node in the tree column */
    public Object getValueFor (Object node, int column);
    /** Get the object class for the column.  Analogous to 
     * <code>TableModel.getColumnClass(int column)</code> 
     * @param column an index into the columns represented by this model (0
     *  based - does not include the tree column of the OutlineModel)
     * @return the class of object that will be displayed in the specified
     * column */
    public Class getColumnClass (int column);
    /** Determine if the cell in this column is editable for the passed
     * node.
     * @param node the object displayed in the tree column of the Outline
     * @param column the column index into the columns defined by this
     *  RowModel  */
    public boolean isCellEditable (Object node, int column);
    /** Set the value of the object in this column.  Typically this may
     * call a setter on the node object in column 0.  */
    public void setValueFor (Object node, int column, Object value);
    /** Get a localized name of this column that can be displayed in
     * the table header
     * @param column the column a name is requested for
     * @return a localized name for the column  */
    public String getColumnName (int column);
}
