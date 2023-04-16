/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.editor;

/**
 * Interface that allows to bridge various implementations
 * of the arrays of objects (especially gap arrays).
 * <p>Once an object implements this interface
 * it's easy to build a list on top of it by using
 * {@code org.netbeans.spi.lexer.util.LexerUtilities#createList(ObjectArray)}.
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
     * are accessed by {@link ObjectArray#getItem(int)} calls.
     * The {@link ObjectArrayUtilities#copyItems(ObjectArray, int, int, Object[], int)}
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
