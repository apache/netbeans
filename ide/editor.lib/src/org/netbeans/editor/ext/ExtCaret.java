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

package org.netbeans.editor.ext;

import org.netbeans.editor.BaseCaret;

/**
* Extended caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtCaret extends BaseCaret {

    static final long serialVersionUID =-4292670043122577690L;

    /** 
     * Update the matching brace of the caret. The document is read-locked
     * while this method is called.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    @Deprecated
    protected void updateMatchBrace() {
    }

    /** 
     * Signal that the next matching brace update
     * will be immediate without waiting for the brace
     * timer to fire the action. This is usually done
     * for the key-typed action.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    @Deprecated
    public void requestMatchBraceUpdateSync() {
    }
    
}
