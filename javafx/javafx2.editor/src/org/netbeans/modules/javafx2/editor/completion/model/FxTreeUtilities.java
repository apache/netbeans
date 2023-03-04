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
