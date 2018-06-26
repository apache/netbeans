/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
