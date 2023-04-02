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
import java.util.Collections;
import java.util.List;

public class JsxElementNode extends Expression {

    private final String name;

    private final List<Expression> attributes;

    private final List<Expression> children;

    public JsxElementNode(String name, List<Expression> attributes, List<Expression> children, long token, int finish) {
        super(token, finish);
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }

    private JsxElementNode(JsxElementNode node, String name, List<Expression> attributes, List<Expression> children) {
        super(node);
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public JsxElementNode setAttributes(List<Expression> attributes) {
        if (this.attributes == attributes) {
            return this;
        }
        return new JsxElementNode(this, name, attributes, children);
    }

    public List<Expression> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public JsxElementNode setChildren(List<Expression> children) {
        if (this.children == children) {
            return this;
        }
        return new JsxElementNode(this, name, attributes, children);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterJsxElementNode(this)) {
            return visitor.leaveJsxElementNode(
                    setAttributes(Node.accept(visitor, attributes)).
                    setChildren(Node.accept(visitor, children)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterJsxElementNode(this);
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {
        sb.append('<').append(name);
        for (Expression attr : attributes) {
            sb.append(' ');
            attr.toString(sb, printType);
        }
        if (children.isEmpty()) {
            sb.append("/>");
        } else {
            sb.append('>');
            for (Expression child : children) {
                child.toString(sb, printType);
            }
            sb.append("</").append(name).append('>');
        }
    }
}
