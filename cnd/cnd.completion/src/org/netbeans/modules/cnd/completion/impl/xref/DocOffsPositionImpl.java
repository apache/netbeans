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
