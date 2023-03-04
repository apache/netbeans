/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import javax.el.ELException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Represents the parse result of a single EL expression.
 * 
 * @author Erno Mononen
 */
public final class ELElement {

    private final Node node;
    private final OffsetRange embeddedOffset;
    private final ELException error;
    private final ELPreprocessor expression;
    private final Snapshot snapshot;
    private final OffsetRange originalOffset;

    private ELElement(Node node, ELException error, ELPreprocessor expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        assert node == null || error == null;
        this.node = node;
        this.embeddedOffset = embeddedOffset;
        this.snapshot = snapshot;
        this.error = error;
        this.expression = expression;

        int origStart = snapshot.getOriginalOffset(embeddedOffset.getStart());
        int origEnd = snapshot.getOriginalOffset(embeddedOffset.getEnd());
        this.originalOffset = new OffsetRange(origStart, origEnd);
    }

    static ELElement valid(Node node, ELPreprocessor expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        return new ELElement(node, null, expression, embeddedOffset, snapshot);
    }

    static ELElement error(ELException error, ELPreprocessor expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        return new ELElement(null, error, expression, embeddedOffset, snapshot);
    }

    /**
     * Makes a copy of this, but with the given {@code node} and {@code expression}.
     * Can't be invoked on valid elements, and the given {@code node} must represent
     * a valid expression.
     * 
     * @param node 
     * @param expression
     * @return a copy of this but with the given {@code node} and {@code expression}.
     */
    public ELElement makeValidCopy(Node node, ELPreprocessor expression) {
        assert !isValid();
        return valid(node, expression, embeddedOffset, snapshot);
    }

    /**
     * Gets the root node of the expression.
     * 
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * Gets the offset in the embedded source.
     * @see #getOriginalOffset()
     * @return
     */
    public OffsetRange getEmbeddedOffset() {
        return embeddedOffset;
    }

    /**
     * Gets the offset in the original document.
     * @return
     */
    public OffsetRange getOriginalOffset() {
        return originalOffset;
    }

    /**
     * Gets the offset of the given {@code node} in the original document.
     * @param node a node contained by this element.
     * @return
     */
    public OffsetRange getOriginalOffset(Node node) {
        int start = originalOffset.getStart() + expression.getOriginalOffset(node.startOffset());
        int end = originalOffset.getStart() + expression.getOriginalOffset(node.endOffset());
        return new OffsetRange(start, end);
    }

    public boolean isValid() {
        return error == null;
    }

    public ELException getError() {
        return error;
    }

    public ELPreprocessor getExpression() {
        return expression;
    }

    /**
     * Gets the node at the given offset.
     * @param offset an offset in the original document.
     * @return the node at the given {@code offset} or {@code null}.
     */
    public Node findNodeAt(final int offset) {
        assert getOriginalOffset().containsInclusive(offset);
        if (getNode() == null) {
            return null;
        }
        final Node[] result = new Node[1];
        getNode().accept(new NodeVisitor() {
            @Override
            public void visit(Node node) throws ELException {
                int nodeFrom = expression.getOriginalOffset(node.startOffset());
                int nodeTo = expression.getOriginalOffset(node.endOffset());
                if (originalOffset.getStart() + nodeFrom <= offset
                        && originalOffset.getStart() + nodeTo > offset) {
                    result[0] = node;
                }

            }
        });
        return result[0];
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + embeddedOffset + " error=" + error + '}';
    }

}
