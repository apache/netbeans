/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser.ir;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

public class ExportSpecifierNode extends Node {

    private final IdentNode identifier;

    private final IdentNode exportIdentifier;

    public ExportSpecifierNode(final long token, final int start, final int finish, final IdentNode identifier, final IdentNode exportIdentifier) {
        super(token, start, finish);
        this.identifier = identifier;
        this.exportIdentifier = exportIdentifier;
    }

    private ExportSpecifierNode(final ExportSpecifierNode node, final IdentNode identifier, final IdentNode exportIdentifier) {
        super(node);
        this.identifier = identifier;
        this.exportIdentifier = exportIdentifier;
    }

    public IdentNode getIdentifier() {
        return identifier;
    }

    public IdentNode getExportIdentifier() {
        return exportIdentifier;
    }

    public ExportSpecifierNode setIdentifier(IdentNode identifier) {
        if (this.identifier == identifier) {
            return this;
        }
        return new ExportSpecifierNode(this, identifier, exportIdentifier);
    }

    public ExportSpecifierNode setExportIdentifier(IdentNode exportIdentifier) {
        if (this.exportIdentifier == exportIdentifier) {
            return this;
        }
        return new ExportSpecifierNode(this, identifier, exportIdentifier);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterExportSpecifierNode(this)) {
            IdentNode newExportIdentifier = exportIdentifier == null ? null
                            : (IdentNode) exportIdentifier.accept(visitor);
            return visitor.leaveExportSpecifierNode(
                            setIdentifier((IdentNode) identifier.accept(visitor)).setExportIdentifier(newExportIdentifier));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterExportSpecifierNode(this);
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {
        if (identifier != null) {
            identifier.toString(sb, printType);
            sb.append(" as ");
        }
        exportIdentifier.toString(sb, printType);
    }

}
