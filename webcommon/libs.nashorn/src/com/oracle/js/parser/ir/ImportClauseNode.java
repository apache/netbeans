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

public class ImportClauseNode extends Node {

    private final IdentNode defaultBinding;

    private final NameSpaceImportNode nameSpaceImport;

    private final NamedImportsNode namedImports;

    public ImportClauseNode(long token, int start, int finish, final IdentNode defaultBinding) {
        this(token, start, finish, defaultBinding, null, null);
    }

    public ImportClauseNode(long token, int start, int finish, final NameSpaceImportNode nameSpaceImport) {
        this(token, start, finish, null, nameSpaceImport, null);
    }

    public ImportClauseNode(long token, int start, int finish, final NamedImportsNode namedImportsNode) {
        this(token, start, finish, null, null, namedImportsNode);
    }

    public ImportClauseNode(long token, int start, int finish, final IdentNode defaultBinding, final NameSpaceImportNode nameSpaceImport) {
        this(token, start, finish, defaultBinding, nameSpaceImport, null);
    }

    public ImportClauseNode(long token, int start, int finish, final IdentNode defaultBinding, final NamedImportsNode namedImports) {
        this(token, start, finish, defaultBinding, null, namedImports);
    }

    private ImportClauseNode(long token, int start, int finish, final IdentNode defaultBinding, final NameSpaceImportNode nameSpaceImport, final NamedImportsNode namedImports) {
        super(token, start, finish);
        this.defaultBinding = defaultBinding;
        this.nameSpaceImport = nameSpaceImport;
        this.namedImports = namedImports;
    }

    private ImportClauseNode(final ImportClauseNode node, final IdentNode defaultBinding, final NameSpaceImportNode nameSpaceImport, final NamedImportsNode namedImports) {
        super(node);
        this.defaultBinding = defaultBinding;
        this.nameSpaceImport = nameSpaceImport;
        this.namedImports = namedImports;
    }

    public IdentNode getDefaultBinding() {
        return defaultBinding;
    }

    public NameSpaceImportNode getNameSpaceImport() {
        return nameSpaceImport;
    }

    public NamedImportsNode getNamedImports() {
        return namedImports;
    }

    public ImportClauseNode setDefaultBinding(IdentNode defaultBinding) {
        if (this.defaultBinding == defaultBinding) {
            return this;
        }
        return new ImportClauseNode(this, defaultBinding, nameSpaceImport, namedImports);
    }

    public ImportClauseNode setNameSpaceImport(NameSpaceImportNode nameSpaceImport) {
        if (this.nameSpaceImport == nameSpaceImport) {
            return this;
        }
        return new ImportClauseNode(this, defaultBinding, nameSpaceImport, namedImports);
    }

    public ImportClauseNode setNamedImports(NamedImportsNode namedImports) {
        if (this.namedImports == namedImports) {
            return this;
        }
        return new ImportClauseNode(this, defaultBinding, nameSpaceImport, namedImports);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterImportClauseNode(this)) {
            IdentNode newDefaultBinding = defaultBinding == null ? null
                            : (IdentNode) defaultBinding.accept(visitor);
            NameSpaceImportNode newNameSpaceImport = nameSpaceImport == null ? null
                            : (NameSpaceImportNode) nameSpaceImport.accept(visitor);
            NamedImportsNode newNamedImports = namedImports == null ? null
                            : (NamedImportsNode) namedImports.accept(visitor);
            return visitor.leaveImportClauseNode(
                            setDefaultBinding(newDefaultBinding).setNameSpaceImport(newNameSpaceImport).setNamedImports(newNamedImports));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterImportClauseNode(this);
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {
        if (defaultBinding != null) {
            defaultBinding.toString(sb, printType);
            if (nameSpaceImport != null || namedImports != null) {
                sb.append(',');
            }
        }

        if (nameSpaceImport != null) {
            nameSpaceImport.toString(sb, printType);
        } else if (namedImports != null) {
            namedImports.toString(sb, printType);
        }

    }
}
