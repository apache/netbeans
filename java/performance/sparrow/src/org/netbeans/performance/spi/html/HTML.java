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
/*
 * HTML.java
 *
 * Created on October 17, 2002, 7:41 PM
 */

package org.netbeans.performance.spi.html;
import java.io.IOException;
/** The basic wrapper for HTML objects.  HTML objects are added to HTMLContainer
 * instances such as HTMLDocument.  HTML objects have a PreferredSize property
 * that indicates how many columns they should take up if placed in a table.
 * There are two special values for this - SINGLE_ROW - indicating that the entity
 * should take up an entire row, or DONT_CARE, in which case the item will take
 * up one column or whatever the container decides.
 * @author  Tim Boudreau
 */
public interface HTML {
    /** A value which, when set as the PreferredWidth of an HTML component, means that
     * it wants to take up an entire row in a table.
     */
    public static final int SINGLE_ROW = -1;
    /** A value which, when set as the value of the PreferredWidth property of an
     * HTML element, indicates that that element does not want to influence how many
     * columns in a table it is displayed across.
     */        
    public static final int DONT_CARE = -2;
    /** Get the HTML text of the object.
     * @return The HTML text.
     */        
    public String toHTML();
    /** Get the HTML text of the object.  Where possible, use this method instead
     * of toHTML() which allocates a new StringBuffer.
     * @param sb A StringBuffer instance to append the text to.
     */        
    public void toHTML (StringBuffer sb);
    /** Returns the preferred width (in terms of table columns) which this component
     * should take up if placed directly in a table.  If the value is SINGLE_ROW
     * the table implementation should try to fit it on a single row.  If the value
     * is DONT_CARE the table implementation should place the element in one column
     * of a table, but it is free to use a number of columns greater than 1.
     * @return The preferred width
     */        
    public int getPreferredWidth();
    /** Write the HTML element to a file.
     * @param filename The fully qualified filename to create.
     * @throws IOException If there are problems during the write.
     */        
    public void writeToFile(String filename) throws IOException;
}

