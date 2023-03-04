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
