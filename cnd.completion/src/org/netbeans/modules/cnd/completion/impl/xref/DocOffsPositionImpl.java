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
package org.netbeans.modules.cnd.completion.impl.xref;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * (line, col, offset) based CsmOffsetable.Position implementation
 *
 */
public class DocOffsPositionImpl implements CsmOffsetable.Position {

    private int line;
    private int col;
    private final int offset;
    private final BaseDocument doc;

    public DocOffsPositionImpl(BaseDocument doc, int offset) {
        this(-1, -1, offset, doc);
    }

    public DocOffsPositionImpl(CsmOffsetable.Position pos) {
        if (pos != null) {
            this.line = pos.getLine();
            this.col = pos.getColumn();
            this.offset = pos.getOffset();
        } else {
            this.line = -1;
            this.col = -1;
            this.offset = 0;
        }
        this.doc = null;
    }

    public DocOffsPositionImpl(int line, int col, int offset, BaseDocument doc) {
        this.line = line;
        this.col = col;
        this.offset = offset;
        this.doc = doc;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLine() {
        return getLine(true);
    }

    @Override
    public int getColumn() {
        return getColumn(true);
    }

    public int getLine(boolean create) {
        if (create && this.line == -1 && this.doc != null) {
            try {
                this.line = LineDocumentUtils.getLineIndex(this.doc, this.offset) + 1;
            } catch (BadLocationException ex) {
                this.line = -1;
            }
        }
        return this.line;
    }

    public int getColumn(boolean create) {
        if (create && this.col == -1 && this.doc != null) {
            try {
                this.col = Utilities.getVisualColumn(this.doc, this.offset) + 1;
            } catch (BadLocationException ex) {
                this.col = -1;
            }
        }
        return this.col;
    }

    /*package*/
    BaseDocument getDocument() {
        return this.doc;
    }

    @Override
    public String toString() {
        return "" + getLine(true) + ':' + getColumn(true) + '/' + getOffset();
    }
}
