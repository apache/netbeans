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

package org.netbeans.editor;

/**
 * Interface that allows to bridge various implementations
 * of the arrays of objects (especially gap arrays).
 * <p>Once an object implements this interface
 * it's easy to build a list on top of it by using
 * {@link org.netbeans.spi.lexer.util.LexerUtilities#createList(ObjectArray)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface ObjectArray {

    /** Get the item at the given index.
     * @param index &gt;=0 and &lt;{@link #getItemCount()} index at which the item
     *  should be obtained.
     * @return the item at the requested index.
     * @throws IndexOutOfBoundsException if the index is &lt;0
     * or &gt;={@link #getItemCount()}
     */
    public Object getItem(int index);
    
    /**
     * @return &gt;=0 Number of items in the object array.
     */
    public int getItemCount();


    /**
     * Interface allowing more efficient getting of the objects
     * from the object array. If the particular object array
     * does not implement this interface then its items
     * are accessed by {@link ObjectArray.getItem(int)} calls.
     * The {@link ObjectArrayUtilities.copyItems(ObjectArray, int, int, Object[], int)}
     * presents uniform access for obtaining of the items.
     */
    public interface CopyItems {
        
        /**
         * Copy the items in the given index range from the object array into destination array.
         * @param srcStartIndex index of the first item in the object array to get.
         * @param srcEndIndex end index in the object array of the items to get.
         * @param dest destination array of objects. The length of the array
         *  must be at least <CODE>destIndex + (srcEndIndex - srcStartIndex)</CODE>.
         * @param destIndex first destination index at which the items are being stored.
         */
        public void copyItems(int srcStartIndex, int srcEndIndex,
        Object[] dest, int destIndex);

    }


    public interface Modification {

        public ObjectArray getArray();

        public int getIndex();

        public Object[] getAddedItems();

        public int getRemovedItemsCount();
    }

}
