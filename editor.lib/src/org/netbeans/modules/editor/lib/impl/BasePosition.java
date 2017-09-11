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
