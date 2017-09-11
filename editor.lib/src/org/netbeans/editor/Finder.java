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
* Finders are used to find some information in document without
* creating copy of the data. They are used as arguments for
* <CODE>DocCache.find()</CODE>. During the find operation
* the <CODE>find()</CODE> method of the finder is called
* with some buffer of character data and some additional information.
* There are two possible search directions and therefore there
* are two finder types: Forward Finders (FwdFinder)
* and Backward Finders (BwdFinder)
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface Finder {

    /** Reset method is used to initialize finder.
    * It is called once at the begining of find. To be most effective
    * <CODE>reset()</CODE> is called after both <CODE>setForward()</CODE>
    * and <CODE>setLimitPos()</CODE> had been called.
    */
    public void reset();

    /** This is the most important function in finder. It can be called several
    * times if the whole search area doesn't fit in the cache buffer.
    * Usual forward search should look like this: <CODE>
    *   int offset = reqPos - bufferStartPos;
    *   while (offset &lt; offset2) {
    *     if (buffer[offset]-meets-condition) {
    *       set-found-flag
    *       return offset + bufferStartPos;
    *     }
    *     offset++;
    *   }
    *   return offset + bufferStartPos;</CODE>
    * Bakward search follows: <CODE>
    *   int offset = reqPos - bufferStartPos
    *   while (offset &gt;= offset1) {
    *     if (buffer[offset]-meets-condition) {
    *       set-found-flag
    *       return offset + bufferStartPos;
    *     }
    *     offset--;
    *   }
    *   return offset + bufferStartPos;</CODE>
    * Caution! Nothing can be written to the data comming in buffer to
    * <CODE>find()</CODE> method because of performance reasons
    * these are primary document data, not a copy.
    * Buffer is always guaranteed to have at least one char - it is
    * char standing at reqPos. However there can be calls to <CODE>find()</CODE>
    * when there will be only that one character, so <CODE>find()</CODE> must
    * must be prepared for this.
    * Unlike calling <CODE>DocCache.find()</CODE> the offset1 &lt; offset2 even
    * for backward searches.
    * @param bufferStartPos begining position of the buffer (not search area).
    * @param buffer buffer with chars to be searched
    * @param offset1 offset of begining of searchable area in buffer.
    *   No searching below this offset can be performed.
    * @param offset2 offset of end of searchable area in buffer.
    *   No searching beyond this offset can be performed.
    * @param reqPos required position. Initially it is the begining
    *   search position requested by caller. In subsequent calls
    *   it is the same value as returned from previous call
    *   to <CODE>find()</CODE> method.
    * @param limitPos is filled with position beyond which search cannot go.
    *   (i.e. forward: pos &lt; limitPos and backward: pos &gt;= limitPos)
    *   Some finders i.e. finder that tries to find some word with
    *   whole-words-only flag turned on can benefit
    *   from this information. If the searched word is at the very end of
    *   the document the finder wouldn't normally find it as it would request
    *   the next buffer even when the whole word was matched because the finder
    *   needs to find white space to know the word ended there. However this
    *   would be beyond the search area so EOT exception would be raised. 
    *   To correctly manage this situation finder must care for limitPos.
    *   When it sees the word and knows this is the last text in document
    *   it signals that it found the word.
    * @return in case the string was found, <CODE>find()</CODE>
    *   method returns the position (not offset) where the string starts
    *   (and must also set some flag resulting to that <CODE>isFound()</CODE>
    *   method will return true).
    *   If the string was not yet found, the function should return
    *   position (not offset) where the next search should continue. If this
    *   position is greater or equal than limit position
    *   (lower than limit position for backward search),
    *   searching will stop resulting in -1 as returned position.
    *   The position returned will be passed as <CODE>reqPos</CODE> in next
    *   call to <CODE>find()</CODE> method.
    */
    public int find(int bufferStartPos, char buffer[], int offset1,
                    int offset2, int reqPos, int limitPos);

    /** Using this function caller determines if finder found
    * desired string. The returned position of <CODE>find</CODE> method 
    * gives the position where the string occurs.
    */
    public boolean isFound();

}
