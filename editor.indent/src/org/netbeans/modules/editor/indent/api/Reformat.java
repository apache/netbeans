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

package org.netbeans.modules.editor.indent.api;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.IndentImpl;

/**
 * Reformatting of a block of code in a document.
 * <br/>
 * The following pattern should be used:
 * <pre>
 * reformat.lock();
 * try {
 *     doc.atomicLock();
 *     try {
 *         reformat.reformat(...);
 *     } finally {
 *         doc.atomicUnlock();
 *     }
 * } finally {
 *     reformat.unlock();
 * }
 * </pre>
 *
 * @author Miloslav Metelka
 */
public final class Reformat {
    
    /**
     *  Get the reformatting for the given document.
     * 
     * @param doc non-null document.
     * @return non-null reformat object.
     */
    public static Reformat get(Document doc) {
        IndentImpl indentImpl = IndentImpl.get(doc);
        Reformat reformat = indentImpl.getReformat();
        if (reformat == null) {
            reformat = new Reformat(indentImpl);
            indentImpl.setReformat(reformat);
        }
        return reformat;
    }
    
    private final IndentImpl impl;
    
    private Reformat(IndentImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Clients should call this method before acquiring of document's write lock.
     * <br/>
     * The following pattern should be used:
     * <pre>
     * reformat.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         reformat.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     reformat.unlock();
     * }
     * </pre>
     */
    public void lock() {
        impl.reformatLock();
    }
    
    /**
     * Clients should call this method after releasing of document's write lock.
     * <br/>
     * The following pattern should be used:
     * <pre>
     * reformat.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         reformat.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     reformat.unlock();
     * }
     * </pre>
     */
    public void unlock() {
        impl.reformatUnlock();
    }
    
    /**
     * Reformat a given range of code in the given document.
     * <br/>
     * It includes possible fixing of indentation and possible code beautification
     * dependent on the implementation of the reformatter.
     * 
     * <p>
     * If the reformatter implementation is not available the reindentation
     * will be performed (i.e. just the line indentation will be fixed)
     * as a closest match.
     * 
     * @param startOffset start offset of the area to be reformatted.
     * @param endOffset end offset of the area to be reformatted.
     */
    public void reformat(int startOffset, int endOffset) throws BadLocationException {
        impl.reformat(startOffset, endOffset, startOffset);
    }

}
