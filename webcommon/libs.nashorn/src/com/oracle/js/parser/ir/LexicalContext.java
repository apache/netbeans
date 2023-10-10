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

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.js.parser.Source;

// @formatter:off
/**
 * A class that tracks the current lexical context of node visitation as a stack
 * of {@link Block} nodes. Has special methods to retrieve useful subsets of the
 * context.
 *
 * This is implemented with a primitive array and a stack pointer, because it
 * really makes a difference performance-wise. None of the collection classes
 * were optimal.
 */
public class LexicalContext {
    private LexicalContextNode[] stack;

    private int[] flags;
    private int sp;

    /**
     * Creates a new empty lexical context.
     */
    public LexicalContext() {
        stack = new LexicalContextNode[16];
        flags = new int[16];
    }

    /**
     * Get the flags for a lexical context node on the stack.
     *
     * @param node node
     *
     * @return the flags for the node
     */
    public int getFlags(final LexicalContextNode node) {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] == node) {
                return flags[i];
            }
        }
        throw new AssertionError("flag node not on context stack");
    }

    /**
     * Get the function body of a function node on the lexical context
     * stack. This will trigger an assertion if node isn't present.
     *
     * @param functionNode function node
     * @return body of function node
     */
    public Block getFunctionBody(final FunctionNode functionNode) {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] == functionNode) {
                return (Block) stack[i + 1];
            }
        }
        throw new AssertionError(functionNode.getName() + " not on context stack");
    }

    /**
     * @return all nodes in the LexicalContext.
     */
    public Iterator<LexicalContextNode> getAllNodes() {
        return new NodeIterator<>(LexicalContextNode.class);
    }

    /**
     * Returns the outermost function in this context. It is either the program,
     * or a lazily compiled function.
     *
     * @return the outermost function in this context.
     */
    public FunctionNode getOutermostFunction() {
        return (FunctionNode) stack[0];
    }

    /**
     * Pushes a new block on top of the context, making it the innermost open
     * block.
     *
     * @param <T> the type of the new node
     * @param node the new node
     *
     * @return the node that was pushed
     */
    public <T extends LexicalContextNode> T push(final T node) {
        assert !contains(node);
        if (sp == stack.length) {
            final LexicalContextNode[] newStack = new LexicalContextNode[sp * 2];
            System.arraycopy(stack, 0, newStack, 0, sp);
            stack = newStack;

            final int[] newFlags = new int[sp * 2];
            System.arraycopy(flags, 0, newFlags, 0, sp);
            flags = newFlags;

        }
        stack[sp] = node;
        flags[sp] = 0;

        sp++;

        return node;
    }

    /**
     * Is the context empty?
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return sp == 0;
    }

    /**
     * @return the depth of the lexical context.
     */
    public int size() {
        return sp;
    }

    /**
     * Pops the innermost block off the context and all nodes that has been
     * contributed since it was put there.
     *
     * @param <T> the type of the node to be popped
     * @param node the node expected to be popped, used to detect unbalanced
     *        pushes/pops
     *
     * @return the node that was popped
     */
    @SuppressWarnings("unchecked")
    public <T extends LexicalContextNode> T pop(final T node) {
        --sp;
        final LexicalContextNode popped = stack[sp];
        stack[sp] = null;
        if (popped instanceof Flags) {
            return (T) ((Flags<?>) popped).setFlag(this, flags[sp]);
        }

        return (T) popped;
    }

    /**
     * Return the top element in the context.
     *
     * @return the node that was pushed last
     */
    public LexicalContextNode peek() {
        return stack[sp - 1];
    }

    /**
     * Check if a node is in the lexical context.
     *
     * @param node node to check for
     *
     * @return {@code true} if in the context
     */
    public boolean contains(final LexicalContextNode node) {
        for (int i = 0; i < sp; i++) {
            if (stack[i] == node) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace a node on the lexical context with a new one. Normally you should try to engineer IR
     * traversals so this isn't needed
     *
     * @param oldNode old node
     * @param newNode new node
     *
     * @return the new node
     */
    public LexicalContextNode replace(final LexicalContextNode oldNode, final LexicalContextNode newNode) {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] == oldNode) {
                assert i == sp - 1 : "violation of contract - we always expect to find the replacement node on top of the lexical context stack: " + newNode + " has " + stack[i + 1].getClass() +
                                " above it";
                stack[i] = newNode;
                break;
            }
        }
        return newNode;
    }

    /**
     * Returns an iterator over all blocks in the context, with the top block
     * (innermost lexical context) first.
     *
     * @return an iterator over all blocks in the context.
     */
    public Iterator<Block> getBlocks() {
        return new NodeIterator<>(Block.class);
    }

    /**
     * Returns an iterator over all functions in the context, with the top
     * (innermost open) function first.
     *
     * @return an iterator over all functions in the context.
     */
    public Iterator<FunctionNode> getFunctions() {
        return new NodeIterator<>(FunctionNode.class);
    }

    /**
     * Get the parent block for the current lexical context block
     *
     * @return parent block
     */
    public Block getParentBlock() {
        final Iterator<Block> iter = new NodeIterator<>(Block.class, getCurrentFunction());
        iter.next();
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * Gets the label node of the current block.
     *
     * @return the label node of the current block, if it is labeled. Otherwise
     *         returns {@code null}.
     */
    public LabelNode getCurrentBlockLabelNode() {
        assert stack[sp - 1] instanceof Block;
        if (sp < 2) {
            return null;
        }
        final LexicalContextNode parent = stack[sp - 2];
        return parent instanceof LabelNode ? (LabelNode) parent : null;
    }

    /**
     * Returns an iterator over all ancestors block of the given block, with its
     * parent block first.
     *
     * @param block the block whose ancestors are returned
     * @return an iterator over all ancestors block of the given block.
     */
    public Iterator<Block> getAncestorBlocks(final Block block) {
        final Iterator<Block> iter = getBlocks();
        while (iter.hasNext()) {
            final Block b = iter.next();
            if (block == b) {
                return iter;
            }
        }
        throw new AssertionError("Block is not on the current lexical context stack");
    }

    /**
     * Returns an iterator over a block and all its ancestors blocks, with the
     * block first.
     *
     * @param block the block that is the starting point of the iteration.
     * @return an iterator over a block and all its ancestors.
     */
    public Iterator<Block> getBlocks(final Block block) {
        final Iterator<Block> iter = getAncestorBlocks(block);
        return new Iterator<Block>() {
            boolean blockReturned = false;

            @Override
            public boolean hasNext() {
                return iter.hasNext() || !blockReturned;
            }

            @Override
            public Block next() {
                if (blockReturned) {
                    return iter.next();
                }
                blockReturned = true;
                return block;
            }
        };
    }

    /**
     * Get the function for this block.
     *
     * @param block block for which to get function
     *
     * @return function for block
     */
    public FunctionNode getFunction(final Block block) {
        final Iterator<LexicalContextNode> iter = new NodeIterator<>(LexicalContextNode.class);
        while (iter.hasNext()) {
            final LexicalContextNode next = iter.next();
            if (next == block) {
                while (iter.hasNext()) {
                    final LexicalContextNode next2 = iter.next();
                    if (next2 instanceof FunctionNode) {
                        return (FunctionNode) next2;
                    }
                }
            }
        }
        assert false;
        return null;
    }

    /**
     * @return the innermost block in the context.
     */
    public Block getCurrentBlock() {
        return getBlocks().next();
    }

    /**
     * @return the innermost function in the context.
     */
    public FunctionNode getCurrentFunction() {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] instanceof FunctionNode) {
                return (FunctionNode) stack[i];
            }
        }
        return null;
    }

    /**
     * Is the topmost lexical context element a function body?
     *
     * @return {@code true} if function body.
     */
    public boolean isFunctionBody() {
        return getParentBlock() == null;
    }

    /**
     * Get the parent function for a function in the lexical context.
     *
     * @param functionNode function for which to get parent
     *
     * @return parent function of functionNode or {@code null} if none (e.g., if
     *         functionNode is the program).
     */
    public FunctionNode getParentFunction(final FunctionNode functionNode) {
        final Iterator<FunctionNode> iter = new NodeIterator<>(FunctionNode.class);
        while (iter.hasNext()) {
            final FunctionNode next = iter.next();
            if (next == functionNode) {
                return iter.hasNext() ? iter.next() : null;
            }
        }
        assert false;
        return null;
    }

    private BreakableNode getBreakable() {
        for (final NodeIterator<BreakableNode> iter = new NodeIterator<>(BreakableNode.class, getCurrentFunction()); iter.hasNext();) {
            final BreakableNode next = iter.next();
            if (next.isBreakableWithoutLabel()) {
                return next;
            }
        }
        return null;
    }

    /**
     * Check whether the lexical context is currently inside a loop.
     *
     * @return {@code true} if inside a loop
     */
    public boolean inLoop() {
        return getCurrentLoop() != null;
    }

    /**
     * @return the loop header of the current loop, or {@code null} if not
     *         inside a loop.
     */
    public LoopNode getCurrentLoop() {
        final Iterator<LoopNode> iter = new NodeIterator<>(LoopNode.class, getCurrentFunction());
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * Find the breakable node corresponding to this label.
     *
     * @param labelName name of the label to search for. If {@code null}, the
     *        closest breakable node will be returned unconditionally, e.g., a
     *        while loop with no label.
     *
     * @return closest breakable node.
     */
    public BreakableNode getBreakable(final String labelName) {
        if (labelName != null) {
            final LabelNode foundLabel = findLabel(labelName);
            if (foundLabel != null) {
                // iterate to the nearest breakable to the foundLabel
                BreakableNode breakable = null;
                for (final NodeIterator<BreakableNode> iter = new NodeIterator<>(BreakableNode.class, foundLabel); iter.hasNext();) {
                    breakable = iter.next();
                }
                return breakable;
            }
            return null;
        }
        return getBreakable();
    }

    private LoopNode getContinueTo() {
        return getCurrentLoop();
    }

    /**
     * Find the continue target node corresponding to this label.
     *
     * @param labelName label name to search for. If {@code null} the closest
     *        loop node will be returned unconditionally, e.g., a while loop
     *        with no label.
     *
     * @return closest continue target node.
     */
    public LoopNode getContinueTo(final String labelName) {
        if (labelName != null) {
            final LabelNode foundLabel = findLabel(labelName);
            if (foundLabel != null) {
                // iterate to the nearest loop to the foundLabel
                LoopNode loop = null;
                for (final NodeIterator<LoopNode> iter = new NodeIterator<>(LoopNode.class, foundLabel); iter.hasNext();) {
                    loop = iter.next();
                }
                return loop;
            }
            return null;
        }
        return getContinueTo();
    }

    /**
     * Check the lexical context for a given label node by name.
     *
     * @param name name of the label.
     *
     * @return LabelNode if found, {@code null} otherwise.
     */
    public LabelNode findLabel(final String name) {
        for (final Iterator<LabelNode> iter = new NodeIterator<>(LabelNode.class, getCurrentFunction()); iter.hasNext();) {
            final LabelNode next = iter.next();
            if (next.getLabelName().equals(name)) {
                return next;
            }
        }
        return null;
    }

    /**
     * Checks whether the current context is inside a switch statement without
     * explicit blocks (curly braces).
     *
     * @return {@code true} if in unprotected switch statement.
     */
    public boolean inUnprotectedSwitchContext() {
        for (int i = sp - 1; i > 0; i--) {
            final LexicalContextNode next = stack[i];
            if (next instanceof Block) {
                break;
            }
            if (next instanceof SwitchNode) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < sp; i++) {
            final Object node = stack[i];
            sb.append(node.getClass().getSimpleName());
            sb.append('@');
            sb.append(id(node));
            sb.append(':');
            if (node instanceof FunctionNode) {
                final FunctionNode fn = (FunctionNode) node;
                final Source source = fn.getSource();
                String src = source.toString();
                if (src.contains(File.pathSeparator)) {
                    src = src.substring(src.lastIndexOf(File.pathSeparator));
                }
                sb.append(src);
                sb.append(' ');
                sb.append(fn.getLineNumber());
            }
            sb.append(' ');
        }
        sb.append(" ==> ]");
        return sb.toString();
    }

    private static String id(Object x) {
        return String.format("0x%08x", System.identityHashCode(x));
    }

    private class NodeIterator<T extends LexicalContextNode> implements Iterator<T> {
        private int index;
        private T next;
        private final Class<T> clazz;
        private LexicalContextNode until;

        NodeIterator(final Class<T> clazz) {
            this(clazz, null);
        }

        NodeIterator(final Class<T> clazz, final LexicalContextNode until) {
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
