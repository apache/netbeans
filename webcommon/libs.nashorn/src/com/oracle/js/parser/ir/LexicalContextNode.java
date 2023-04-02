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

// @formatter:off
/**
 * Interface for nodes that can be part of the lexical context.
 * @see LexicalContext
 */
public interface LexicalContextNode {
    /**
     * Accept function for the node given a lexical context. It must be prepared
     * to replace itself if present in the lexical context
     *
     * @param lc      lexical context
     * @param visitor node visitor
     *
     * @return new node or same node depending on state change
     */
    Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor);

    <R> R accept(final LexicalContext lc, final TranslatorNodeVisitor<? extends LexicalContext, R> visitor);

    // Would be a default method on Java 8
    /**
     * Helper class for accept for items of this lexical context, delegates to the
     * subclass accept and makes sure that the node is on the context before accepting
     * and gets popped after accepting (and that the stack is consistent in that the
     * node has been replaced with the possible new node resulting in visitation)
     */
    class Acceptor {
        static Node accept(final LexicalContextNode node, final NodeVisitor<? extends LexicalContext> visitor) {
            final LexicalContext lc = visitor.getLexicalContext();
            lc.push(node);
            final LexicalContextNode newNode = (LexicalContextNode)node.accept(lc, visitor);
            return (Node)lc.pop(newNode);
        }

        static <R> R accept(final LexicalContextNode node, final TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
            final LexicalContext lc = visitor.getLexicalContext();
            lc.push(node);
            final R result = node.accept(lc, visitor);
            lc.pop(node);
            return result;
        }
    }
}
