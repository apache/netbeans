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
package org.netbeans.spi.viewmodel;

/**
 * Use this to separate value and the HTML value.
 * When displaying a property value, then if the value is a String and if it contains
 * a HTML code, it's automatically rendered as HTML. The HTML code is then
 * stripped out to get the raw value.
 * If this is not desired or if it's necessary to provide a value and HTML code
 * that differs from each other, implement this model.
 * 
 * @author Martin Entlicher
 * @since 1.42
 * @see TableHTMLModelFilter
 */
public interface TableHTMLModel extends TableModel {
    
    /**
     * Test if the model has a HTML value.
     * For backward compatibility, if it returns <code>false</code>,
     * HTML value is is taken from the String value, if it contains some.
     * If this is not desired, return true here and null from
     * {@link #getHTMLValueAt(java.lang.Object, java.lang.String)}.
     * @param node an object returned from {@link TreeModel#getChildren(java.lang.Object, int, int) }
     *             for this row
     * @param columnID an id of column defined by {@link ColumnModel#getID()}
     * @return <code>true</code> if there is some HTML value to be returned
     *         from {@link #getHTMLValueAt(java.lang.Object, java.lang.String)},
     *         <code>false</code> otherwise.
     *         When <code>false</code> is returned,
     *         {@link #getHTMLValueAt(java.lang.Object, java.lang.String)} is not called.
     * @throws UnknownTypeException if there is nothing to be provided for the given
     *         parameter type
     */
    boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException;
    
    /**
     * Get the HTML value.
     * 
     * @param node an object returned from {@link TreeModel#getChildren(java.lang.Object, int, int) }
     *             for this row
     * @param columnID an id of column defined by {@link ColumnModel#getID()}
     * @return The HTML value, or <code>null</code> when no HTML value is provided.
     * @throws UnknownTypeException if there is nothing to be provided for the given
     *         parameter type
     * @see #hasHTMLValueAt(java.lang.Object, java.lang.String)
     */
    String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException;

}
