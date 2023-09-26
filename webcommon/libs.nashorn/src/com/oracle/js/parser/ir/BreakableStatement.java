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

import java.util.Collections;
import java.util.List;

// @formatter:off
abstract class BreakableStatement extends LexicalContextStatement implements BreakableNode {

    /** break label. */
    protected final Label breakLabel;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param breakLabel break label
     */
    protected BreakableStatement(final int lineNumber, final long token, final int finish, final Label breakLabel) {
        super(lineNumber, token, finish);
        this.breakLabel = breakLabel;
    }

    /**
     * Copy constructor
     *
     * @param breakableNode source node
     */
    protected BreakableStatement(final BreakableStatement breakableNode) {
        super(breakableNode);
        this.breakLabel = new Label(breakableNode.getBreakLabel());
    }

    /**
     * Check whether this can be broken out from without using a label,
     * e.g. everything but Blocks, basically
     * @return true if breakable without label
     */
    @Override
    public boolean isBreakableWithoutLabel() {
        return true;
    }

    /**
     * Return the break label, i.e. the location to go to on break.
     * @return the break label
     */
    @Override
    public Label getBreakLabel() {
        return breakLabel;
    }

    /**
     * Return the labels associated with this node. Breakable nodes that
     * aren't LoopNodes only have a break label - the location immediately
     * afterwards the node in code
     * @return list of labels representing locations around this node
     */
    @Override
    public List<Label> getLabels() {
        return Collections.unmodifiableList(Collections.singletonList(breakLabel));
    }
}
