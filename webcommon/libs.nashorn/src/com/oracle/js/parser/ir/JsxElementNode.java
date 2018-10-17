/*
 */

package com.oracle.js.parser.ir;

import com.oracle.js.parser.ir.visitor.JsxNodeVisitor;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;
import java.util.Collections;
import java.util.List;
import com.oracle.js.parser.ir.visitor.JsxTranslatorNodeVisitor;

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
        if (!(visitor instanceof JsxNodeVisitor)) {
            return this;
        }
        JsxNodeVisitor jv = (JsxNodeVisitor)visitor;
        if (jv.enterJsxElementNode(this)) {
            return jv.leaveJsxElementNode(
                    setAttributes(Node.accept(visitor, attributes)).
                    setChildren(Node.accept(visitor, children)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        if (!(visitor instanceof JsxTranslatorNodeVisitor)) {
            return null;
        }
        return ((JsxTranslatorNodeVisitor<R>)visitor).enterJsxElementNode(this);
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
