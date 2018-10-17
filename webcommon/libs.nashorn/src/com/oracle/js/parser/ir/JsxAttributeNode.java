/*
 */

package com.oracle.js.parser.ir;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;
import com.oracle.js.parser.ir.visitor.JsxNodeVisitor;
import com.oracle.js.parser.ir.visitor.JsxTranslatorNodeVisitor;

public class JsxAttributeNode extends Expression {

    private final String name;

    private final Expression value;

    public JsxAttributeNode(String name, Expression value, long token, int finish) {
        super(token, finish);
        this.name = name;
        this.value = value;
    }

    public JsxAttributeNode(JsxAttributeNode node, String name, Expression value) {
        super(node);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    public JsxAttributeNode setValue(Expression value) {
        if (this.value == value) {
            return this;
        }
        return new JsxAttributeNode(this, name, value);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        if (!(visitor instanceof JsxNodeVisitor)) {
            return this;
        }
        JsxNodeVisitor jv = (JsxNodeVisitor)visitor;
        if (jv.enterJsxAttributeNode(this)) {
            Expression newValue = value == null ? null
                            : (Expression) value.accept(visitor);
            return jv.leaveJsxAttributeNode(setValue(newValue));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        if (!(visitor instanceof JsxTranslatorNodeVisitor)) {
            return null;
        }
        return ((JsxTranslatorNodeVisitor<R>)visitor).enterJsxAttributeNode(this);
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {
        sb.append(name);
        if (value != null) {
            sb.append('=');
            value.toString(sb, printType);
        }
    }

}
