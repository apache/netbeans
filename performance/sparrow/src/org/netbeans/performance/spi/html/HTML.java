/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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

