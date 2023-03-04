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

package org.netbeans.editor;

/**
* Advanced finder that can adjust the start and limit position
* of the search. The finder can be used in the <tt>BaseDocument.find()</tt>
* which calls its adjust-methods automatically.
* The order of the methods called for the search is
* <br>
* 1. <tt>adjustStartPos()</tt> is called<br>
* 2. <tt>adjustStartPos()</tt> is called<br>
* 3. <tt>reset()</tt> is called<br>
* If the search is void i.e. <tt>doc.find(finder, pos, pos)</tt>
* is called, no adjust-methods are called, only the <tt>reset()</tt>
* is called.
* For backward search the start-position is higher than the limit-position.
* The relation <tt>startPos &lt; endPos</tt> defines whether the search
* will be forward or backward. The adjust-methods could in fact
* revert this relation turning the forward search into the backward one
* and vice versa. This is not allowed. If that happens the search
* is considered void.
* The adjust-methods must NOT use the shortcut -1 for the end of document.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface AdjustFinder extends Finder {

    /** Adjust start position of the search to be either the same or lower.
    * This method can be used
    * for example to scan the whole line by the reg-exp finder even 
    * if the original start position is not at the begining of the line.
    * Although it's not specifically checked the finder should NOT in any case
    * return the position that is lower than the original 
    * @param doc document to search on
    * @param startPos start position originally requested in <tt>BaseDocument.find()</tt>.
    * @return possibly modified start position. The returned position must be
    *   the same or lower than the original start position for forward search
    *   and the same or high.
    */
    public int adjustStartPos(BaseDocument doc, int startPos);

    /** Adjust the limit position of the search
    * (it's the position where the search will end) to be either the same or greater.
    * @param doc document to search on
    * @param limitPos limit position originally requested in <tt>BaseDocument.find()</tt>
    * @return possibly modified limit position. The returned position must be
    *   the same or greater than the original limit position.
    */
    public int adjustLimitPos(BaseDocument doc, int limitPos);

}
