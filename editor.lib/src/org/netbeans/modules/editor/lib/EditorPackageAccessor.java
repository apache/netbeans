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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.editor.lib;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.Mark;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.lib.impl.MarkVector;
import org.netbeans.modules.editor.lib.impl.MultiMark;


/**
 * Accessor for the package-private functionality in org.netbeans.editor package.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class EditorPackageAccessor {

    private static EditorPackageAccessor ACCESSOR = null;

    public static synchronized void register(EditorPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized EditorPackageAccessor get() {
        // Trying to wake up BaseDocument ...
        try {
            Class clazz = Class.forName(BaseDocument.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected EditorPackageAccessor() {
    }

    public abstract UndoableEdit BaseDocument_markAtomicEditsNonSignificant(BaseDocument doc);
    public abstract void BaseDocument_clearAtomicEdits(BaseDocument doc);
    public abstract MarkVector BaseDocument_getMarksStorage(BaseDocument doc);
    public abstract Mark BaseDocument_getMark(BaseDocument doc, MultiMark multiMark);
    public abstract void Mark_insert(Mark mark, BaseDocument doc, int pos) throws InvalidMarkException, BadLocationException;
    public abstract void ActionFactory_reformat(Reformat formatter, Document doc, int startPos, int endPos, AtomicBoolean canceled) throws BadLocationException;

    public abstract Object BaseDocument_newServices(BaseDocument doc);
    
    public abstract int MarkBlockChain_adjustPos(MarkBlockChain chain, int pos, boolean thisBlock);
}
