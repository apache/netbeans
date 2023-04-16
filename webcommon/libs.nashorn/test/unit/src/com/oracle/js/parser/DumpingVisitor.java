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

package com.oracle.js.parser;


import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.JsxElementNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;

class DumpingVisitor extends NodeVisitor {

    private static final int INDENT_PER_LEVEL = 2;
    private int indent = 0;

    public DumpingVisitor(LexicalContext lc) {
        super(lc);
    }

    @Override
    protected Node leaveDefault(Node node) {
        indent -= INDENT_PER_LEVEL;
        return super.leaveDefault(node);
    }

    @Override
    protected boolean enterDefault(Node node) {
        if (node instanceof IdentNode) {
            System.out.printf("%s%s [%d-%d, name=%s]%n", indent(),
                    node.getClass().getName(), node.getStart(),
                    node.getFinish(), ((IdentNode) node).getName());
        } else if (node instanceof LiteralNode) {
            System.out.println(indent() + node.getClass().getName() + " [" + ((LiteralNode) node).getValue() + "]");
        } else if (node instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) node;
            System.out.printf("%s%s [%s-%d, %s, kind=%s, isAsync=%b, isMethod=%b]%n",
                    indent(),
                    node.getClass().getName(),
                    fn.getStart(),
                    fn.getFinish(),
                    fn.getName(),
                    fn.getKind().name(),
                    fn.isAsync(),
                    fn.isMethod());
        } else if (node instanceof ForNode) {
            ForNode fn = (ForNode) node;
            System.out.printf("%s%s [init: %s-%s, modify: %s-%s, test: %s-%s]%n",
                    indent(),
                    node.getClass().getName(),
                    fn.getInit() != null ? fn.getInit().getStart() : "",
                    fn.getInit() != null ? fn.getInit().getFinish() : "",
                    fn.getModify() != null ? fn.getModify().getStart() : "",
                    fn.getModify() != null ? fn.getModify().getFinish() : "",
                    fn.getTest() != null ? fn.getTest().getStart() : "",
                    fn.getTest() != null ? fn.getTest().getFinish() : ""
            );
        } else if (node instanceof BinaryNode) {
            System.out.println(indent() + node.getClass().getName() + " [" + ((BinaryNode) node).tokenType() + "]");
        } else if (node instanceof AccessNode) {
            AccessNode an = (AccessNode) node;
            System.out.println(indent() + node.getClass().getName() + " [property=" + an.getProperty() + ", optional=" + an.isOptional() + "]");
        } else if (node instanceof IndexNode) {
            IndexNode in = (IndexNode) node;
            System.out.println(indent() + node.getClass().getName() + " [" + in.isOptional() + "]");
        } else if (node instanceof CallNode) {
            CallNode cn = (CallNode) node;
            System.out.println(indent() + node.getClass().getName() + " [" + cn.isOptional() + "]");
        } else if (node instanceof PropertyNode) {
            PropertyNode pn = (PropertyNode) node;
            System.out.println(indent() + node.getClass().getName() + " [static=" + pn.isStatic() + "]");
        } else if (node instanceof JsxElementNode) {
            JsxElementNode jen = (JsxElementNode) node;
            System.out.printf("%s%s [%d-%d, name=%s]%n", indent(),
                    node.getClass().getName(), node.getStart(),
                    node.getFinish(), jen.getName());
        } else {
            System.out.printf("%s%s [%d-%d]%n", indent(),
                    node.getClass().getName(), node.getStart(),
                    node.getFinish());
        }
        indent += INDENT_PER_LEVEL;
        return super.enterDefault(node);
    }

    private String indent() {
        StringBuilder sb = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

}
