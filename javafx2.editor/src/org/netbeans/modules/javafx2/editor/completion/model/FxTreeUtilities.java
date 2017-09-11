/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.parser.ModelAccessor;
import org.netbeans.modules.javafx2.editor.parser.NodeInfo;

/**
 * Utilities, which work with the model tree at XML and text level.
 * @author sdedic
 */
public final class FxTreeUtilities {
    private FxModel model;
    private ModelAccessor accessor;
    private TokenHierarchy hierarchy;

    public FxTreeUtilities(ModelAccessor accessor, FxModel model, TokenHierarchy hierarchy) {
        if (accessor == null) {
            throw new IllegalArgumentException();
        }
        this.accessor = accessor;
        this.model = model;
        this.hierarchy = hierarchy;
    }
    
    public boolean containsPos(FxNode n, int position, boolean caret) {
        return accessor.i(n).contains(position, caret);
    }
    
    public boolean contentContainsPos(FxNode n, int position, boolean caret) {
        return accessor.i(n).contentContains(position, caret);
    }
    
    public boolean isDefined(int pos) {
        return pos != -1;
    }
    
    public boolean isAccurate(int pos) {
        return pos >= 0;
    }
    
    public int getOffset(int pos) {
        return pos >= 0  ? pos : (-pos) - 1;
    }
    
    public int getStart(FxNode n) {
        return accessor.i(n).getStart();
    }

    /**
     * Finds path of Nodes leading to the position position. If 'ignoreTag' is true,
     * and the position is within element's tag (incl. attributes), that element is
     * excluded.
     * If 'caret' is set, the position is interepreted as caret pos, that is between characters.
     * The caret must be after 1st offset of the element, or 
     * 
     * @param position
     * @param ignoreTag
     * @param caret
     * @return 
     */
    public List<? extends FxNode> findEnclosingElements(final int position, boolean ignoreTag,
            final boolean caret) {
        class T extends FxNodeVisitor.ModelTreeTraversal {
            Deque<FxNode>    nodeStack = new LinkedList<FxNode>();
            
            @Override
            protected void scan(FxNode node) {
                super.scan(node);
            }

            @Override
            public void visitNode(FxNode node) {
                NodeInfo ni = accessor.i(node);
                if (ni.contains(position, caret)) {
                    nodeStack.push(node);
                    super.visitNode(node);
                }
                if (ni.getStart() > position) {
                    throw new Error();
                }
            }
            
        }
        
        T visitor = new T();
        try {
            model.accept(visitor);
        } catch (Error e) {
            // expected
        }
        if (visitor.nodeStack.size() > 1 && ignoreTag) {
            FxNode n = visitor.nodeStack.peekFirst();
            if (!accessor.i(n).contentContains(position, caret)) {
                visitor.nodeStack.removeFirst();
            }
        }
        if (visitor.nodeStack.isEmpty()) {
            // compensate bcs model.contains() does not accept 0th position, but the
            // model by def contains everything in the source.
            visitor.nodeStack.add(model);
        }
        return Collections.unmodifiableList(
                new ArrayList<FxNode>(visitor.nodeStack)
        );
    }
    
    public int[] findAttributePos(FxNode node, String uri, String name, boolean value) {
        NodeInfo ni = accessor.i(node);
        if (!ni.isElement()) {
            throw new IllegalArgumentException();
        }
        TokenSequence<XMLTokenId> seq = hierarchy.tokenSequence();
        seq.move(ni.getStart());
        
        int state = 0;
        
        while (seq.moveNext()) {
            Token<XMLTokenId> t = seq.token();
            if (ni.isDefined(TextPositions.Position.ContentStart) &&
                    seq.offset() >= ni.getContentStart()) {
                return null;
            }
            XMLTokenId id = t.id();
            switch (id) {
                case TAG:
                    if (t.text().charAt(0) == '>' || seq.offset() != ni.getStart()) {
                        // broken tag or something
                        return new int[] { ni.getStart(), ni.getContentStart() };
                    }
                    break;
                    
                case ARGUMENT: {
                    String n = t.text().toString();
                    int pos = n.indexOf(':');
                    // HACK HACK, FIXME - the namespace must be translated into
                    // the prefix, but I don't have prefixes in the model at this moment.
                    if (uri != null && pos == -1) {
                        break;
                    }
                    if (pos != -1) {
                        n = n.substring(pos + 1);
                    }
                    if (name.equals(n)) {
                        if (!value) {
                            return new int[] {
                                seq.offset(),
                                seq.offset() + t.length()
                            };
                        }
                        state = 1;
                    }
                    break;
                }
                    
                case VALUE:
                    if (state != 1) {
                        break;
                    }
                    return new int[] {
                        seq.offset() + 1,
                        seq.offset() + t.length() + 1
                    };
            }
        }
        return null;
    }

    public TextPositions positions(FxNode node) {
        return accessor.i(node);
    }

    public boolean isElement(FxNode node) {
        return accessor.i(node).isElement();
    }
    
    public boolean isAttribute(FxNode node) {
        return accessor.i(node).isAttribute();
    }
}
