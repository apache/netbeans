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

package org.netbeans.modules.editor.lib.impl;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/**
* Position in document. This is enhanced version of
* Swing <CODE>Position</CODE> interface. It supports
* insert after feature. If Position has
* <CODE>insertAfter</CODE> flag set and text is inserted
* right at the mark's position, the position will NOT move.
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class BasePosition implements Position {

    /** The mark that serves this position */
    private MultiMark mark; // 8-super + 4 = 12 bytes
    
//    public java.util.List<StackTraceElement> allocStack;

    public BasePosition() throws BadLocationException {
    }

    /** Get offset in document for this position */
    public int getOffset() {
        return mark.getOffset();
    }

    void setMark(MultiMark mark) {
        this.mark = mark;
    }
    
    @Override
    public String toString() {
        return super.toString() + " offset=" + getOffset(); // NOI18N
    }

}
