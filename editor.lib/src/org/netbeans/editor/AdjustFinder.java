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
