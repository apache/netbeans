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
 * Subset of functionality of CharSequence present in JDK1.4.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated use {@link org.netbeans.lib.editor.util.swing.DocumentUtilities#getText(javax.swing.text.Document) }
 *  to obtain CharSequence instance from a document instead.
 */

@Deprecated
public interface CharSeq {

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit Unicode characters in the sequence.
     *
     * @return  the number of characters in this sequence
     */
    int length();

    /**
     * Returns the character at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first character of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     *
     * @param   index   the index of the character to be returned
     *
     * @return  the specified character
     *
     * @throws  IndexOutOfBoundsException
     *          if the <tt>index</tt> argument is negative or not less than
     *          <tt>length()</tt>
     */
    char charAt(int index);
    
}
