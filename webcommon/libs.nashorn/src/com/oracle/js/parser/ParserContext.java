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
package com.oracle.js.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.js.parser.ir.Statement;

/**
 * A class that tracks the current lexical context of node visitation as a stack of
 * {@code ParserContextNode} nodes. Has special methods to retrieve useful subsets of the context.
 *
 * This is implemented with a primitive array and a stack pointer, because it really makes a
 * difference performance wise. None of the collection classes were optimal
 */

class ParserContext {

    private ParserContextNode[] stack;
    private int sp;

    private static final int INITIAL_DEPTH = 16;

    /**
     * Constructs a ParserContext, initializes the stack
     */
    ParserContext() {
        this.sp = 0;
        this.stack = new ParserContextNode[INITIAL_DEPTH];
    }

    /**
     * Pushes a new block on top of the context, making it the innermost open block.
     *
     * @param node the new node
     * @return The node that was pushed
     */
    public <T extends ParserContextNode> T push(final T node) {
        assert !contains(node);
        if (sp == stack.length) {
            final ParserContextNode[] newStack = new ParserContextNode[sp * 2];
            System.arraycopy(stack, 0, newStack, 0, sp);
            stack = newStack;
        }
        stack[sp] = node;
        sp++;

        return node;
    }

    /**
     * The topmost node on the stack
     *
     * @return The topmost node on the stack
     */
    public ParserContextNode peek() {
        return stack[sp - 1];
    }

    /**
     * Removes and returns the topmost Node from the stack.
     *
     * @param node The node expected to be popped, used for sanity check
     * @return The removed node
     */
    public <T extends ParserContextNode> T pop(final T node) {
        --sp;
        @SuppressWarnings("unchecked")
        final T popped = (T) stack[sp];
        stack[sp] = null;
        assert node == popped;

        return popped;
    }

    /**
     * Tests if a node is on the stack.
     *
     * @param node The node to test
     * @return true if stack contains node, false otherwise
     */
    public boolean contains(final ParserContextNode node) {
        for (int i = 0; i < sp; i++) {
            if (stack[i] == node) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the topmost {@link ParserContextBreakableNode} on the stack, null if none on stack
     *
     * @return Returns the topmost {@link ParserContextBreakableNode} on the stack, null if none on
     *         stack
     */
    private ParserContextBreakableNode getBreakable() {
        for (final NodeIterator<ParserContextBreakableNode> iter = new NodeIterator<>(ParserContextBreakableNode.class, getCurrentFunction()); iter.hasNext();) {
            final ParserContextBreakableNode next = iter.next();
            if (next.isBreakableWithoutLabel()) {
                return next;
            }
        }
        return null;
    }

    /**
     * Find the breakable node corresponding to this label.
     *
     * @param labelName name of the label to search for. If null, the closest breakable node will be
     *            returned unconditionally, e.g. a while loop with no label
     * @return closest breakable node
     */
    public ParserContextBreakableNode getBreakable(final String labelName) {
        if (labelName != null) {
            final ParserContextLabelNode foundLabel = findLabel(labelName);
            if (foundLabel != null) {
                // iterate to the nearest breakable to the foundLabel
                ParserContextBreakableNode breakable = null;
                for (final NodeIterator<ParserContextBreakableNode> iter = new NodeIterator<>(ParserContextBreakableNode.class, foundLabel); iter.hasNext();) {
                    breakable = iter.next();
                }
                return breakable;
            }
            return null;
        } else {
            return getBreakable();
        }
    }

    /**
     * Returns the loop node of the current loop, or null if not inside a loop
     *
     * @return loop noder
     */
    public ParserContextLoopNode getCurrentLoop() {
        final Iterator<ParserContextLoopNode> iter = new NodeIterator<>(ParserContextLoopNode.class, getCurrentFunction());
        return iter.hasNext() ? iter.next() : null;
    }

    private ParserContextLoopNode getContinueTo() {
        return getCurrentLoop();
    }

    /**
     * Find the continue target node corresponding to this label.
     *
     * @param labelName label name to search for. If null the closest loop node will be returned
     *            unconditionally, e.g. a while loop with no label
     * @return closest continue target node
     */
    public ParserContextLoopNode getContinueTo(final String labelName) {
        if (labelName != null) {
            final ParserContextLabelNode foundLabel = findLabel(labelName);
            if (foundLabel != null) {
                // iterate to the nearest loop to the foundLabel
                ParserContextLoopNode loop = null;
                for (final NodeIterator<ParserContextLoopNode> iter = new NodeIterator<>(ParserContextLoopNode.class, foundLabel); iter.hasNext();) {
                    loop = iter.next();
                }
                return loop;
            }
            return null;
        }
        return getContinueTo();
    }

    /**
     * Get the function body of a function node on the stack. This will trigger an assertion if node
     * isn't present
     *
     * @param functionNode function node
     * @return body of function node
     */
    public ParserContextBaseNode getFunctionBody(final ParserContextFunctionNode functionNode) {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] == functionNode) {
                return (ParserContextBaseNode) stack[i + 1];
            }
        }
        throw new AssertionError(functionNode.getName() + " not on context stack");
    }

    /**
     * Check the stack for a given label node by name
     *
     * @param name name of the label
     * @return LabelNode if found, null otherwise
     */
    public ParserContextLabelNode findLabel(final String name) {
        for (final Iterator<ParserContextLabelNode> iter = new NodeIterator<>(ParserContextLabelNode.class, getCurrentFunction()); iter.hasNext();) {
            final ParserContextLabelNode next = iter.next();
            if (next.getLabelName().equals(name)) {
                return next;
            }
        }
        return null;
    }

    /**
     * Prepends a statement to the current node.
     *
     * @param statement The statement to prepend
     */
    public void prependStatementToCurrentNode(final Statement statement) {
        assert statement != null;
        stack[sp - 1].prependStatement(statement);
    }

    /**
     * Appends a statement to the current Node.
     *
     * @param statement The statement to append
     */
    public void appendStatementToCurrentNode(final Statement statement) {
        assert statement != null;
        stack[sp - 1].appendStatement(statement);
    }

    /**
     * Returns the innermost function in the context.
     *
     * @return the innermost function in the context.
     */
    public ParserContextFunctionNode getCurrentFunction() {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] instanceof ParserContextFunctionNode) {
                return (ParserContextFunctionNode) stack[i];
            }
        }
        return null;
    }

    /**
     * Returns an iterator over all blocks in the context, with the top block (innermost lexical
     * context) first.
     *
     * @return an iterator over all blocks in the context.
     */
    public Iterator<ParserContextBlockNode> getBlocks() {
        return new NodeIterator<>(ParserContextBlockNode.class);
    }

    /**
     * Returns the innermost block in the context.
     *
     * @return the innermost block in the context.
     */
    public ParserContextBlockNode getCurrentBlock() {
        return getBlocks().next();
    }

    /**
     * The last statement added to the context
     *
     * @return The last statement added to the context
     */
    public Statement getLastStatement() {
        if (sp == 0) {
            return null;
        }
        final ParserContextNode top = stack[sp - 1];
        final int s = top.getStatements().size();
        return s == 0 ? null : top.getStatements().get(s - 1);
    }

    /**
     * Returns an iterator over all functions in the context, with the top (innermost open) function
     * first.
     *
     * @return an iterator over all functions in the context.
     */
    public Iterator<ParserContextFunctionNode> getFunctions() {
        return new NodeIterator<>(ParserContextFunctionNode.class);
    }

    public ParserContextModuleNode getCurrentModule() {
        final Iterator<ParserContextModuleNode> iter = new NodeIterator<>(ParserContextModuleNode.class, getCurrentFunction());
        return iter.hasNext() ? iter.next() : null;
    }

    private class NodeIterator<T extends ParserContextNode> implements Iterator<T> {
        private int index;
        private T next;
        private final Class<T> clazz;
        private ParserContextNode until;

        NodeIterator(final Class<T> clazz) {
            this(clazz, null);
        }

        NodeIterator(final Class<T> clazz, final ParserContextNode until) {
            this.index = sp - 1;
            this.clazz = clazz;
            this.until = until;
            this.next = findNext();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            final T lnext = next;
            next = findNext();
            return lnext;
        }

        @SuppressWarnings("unchecked")
        private T findNext() {
            for (int i = index; i >= 0; i--) {
                final Object node = stack[i];
                if (node == until) {
                    return null;
                }
                if (clazz.isAssignableFrom(node.getClass())) {
                    index = i - 1;
                    return (T) node;
                }
            }
            return null;
        }
    }
}
