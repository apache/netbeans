/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Lahoda
 */
public class TokenList {

    private CompilationInfo info;
    private SourcePositions sourcePositions;
    private Document doc;
    private AtomicBoolean cancel;

    private boolean topLevelIsJava;
    private TokenSequence topLevel;
    private TokenSequence ts;
        
    public TokenList(CompilationInfo info, final Document doc, AtomicBoolean cancel) {
        this.info = info;
        this.doc = doc;
        this.cancel = cancel;
        
        this.sourcePositions = info.getTrees().getSourcePositions();
        
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (TokenList.this.cancel.get()) {
                    return ;
                }
                
                topLevel = TokenHierarchy.get(doc).tokenSequence();
                
                topLevelIsJava = topLevel.language() == JavaTokenId.language();
                
                if (topLevelIsJava) {
                    ts = topLevel;
                    ts.moveStart();
                    ts.moveNext(); //XXX: what about empty document
                }
            }
        });
    }
    
    public void moveToOffset(long inputOffset) {
        final int offset = info.getSnapshot().getOriginalOffset((int) inputOffset);

        if (offset < 0) {
            return ;
        }
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (topLevelIsJava) {
                    while (ts.offset() < offset) {
                        if (!ts.moveNext()) {
                            return ;
                        }
                    }
                } else {
                    Iterator<? extends TokenSequence> embeddedSeqs = null;
                    if (ts == null) {
                        List<? extends TokenSequence> seqs = new ArrayList<TokenSequence>(embeddedTokenSequences(TokenHierarchy.get(doc), offset));
                        Collections.reverse(seqs);
                        embeddedSeqs = seqs.iterator();
                        while (embeddedSeqs.hasNext()) {
                            TokenSequence tseq = embeddedSeqs.next();
                            if (tseq.language() == JavaTokenId.language()) {
                                ts = tseq;
                                break;
                            }
                        }
                    }

                    while (ts != null && ts.offset() < offset) {
                        if (!ts.moveNext()) {
                            ts = null;
                            if (embeddedSeqs == null) {
                                List<? extends TokenSequence> seqs = new ArrayList<TokenSequence>(embeddedTokenSequences(TokenHierarchy.get(doc), offset));
                                Collections.reverse(seqs);
                                embeddedSeqs = seqs.iterator();
                            }
                            while (embeddedSeqs.hasNext()) {
                                TokenSequence tseq = embeddedSeqs.next();
                                if (tseq.language() == JavaTokenId.language()) {
                                    ts = tseq;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void moveToEnd(Tree t) {
        if (t == null) {
            return ;
        }

        long end = sourcePositions.getEndPosition(info.getCompilationUnit(), t);

        if (end == (-1))
            return ;

        if (t.getKind() == Kind.ARRAY_TYPE) {
            moveToEnd(((ArrayTypeTree) t).getType());
            return ;
        }
        moveToOffset(end);
    }

    public void moveToEnd(Collection<? extends Tree> trees) {
        if (trees == null) {
            return ;
        }

        for (Tree t : trees) {
            moveToEnd(t);
        }
    }

    public void firstIdentifier(final TreePath tp, final String name, final Map<Tree, List<Token>> tree2Tokens) {
        Token t = firstIdentifier(tp, name);
        if (t != null) {
            tree2Tokens.put(tp.getLeaf(), Collections.singletonList(t));
        }
    }

    public Token firstIdentifier(final TreePath tp, final String name) {
        final Token[] ret = new Token[] {null};
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null) {
                    return ;
                }
                
                boolean next = true;

                while (ts.token().id() != JavaTokenId.IDENTIFIER && (next = ts.moveNext()))
                    ;

                if (next) {
                    if (name.equals(info.getTreeUtilities().decodeIdentifier(ts.token().text()).toString())) {
                        ret[0] = ts.token();
                    } else {
//                            System.err.println("looking for: " + name + ", not found");
                    }
                }
            }
        });
        return ret[0];
    }

    public void identifierHere(final IdentifierTree tree, final Map<Tree, List<Token>> tree2Tokens) {
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null) {
                    return ;
                }
                
                Token t = ts.token();

                if (t.id() == JavaTokenId.IDENTIFIER && tree.getName().toString().equals(info.getTreeUtilities().decodeIdentifier(t.text()).toString())) {
    //                System.err.println("visit ident 1");
                    tree2Tokens.put(tree, Collections.singletonList(ts.token()));
                } else {
    //                System.err.println("visit ident 2");
                }
            }
        });
    }
    
    public void moduleNameHere(final ExpressionTree tree, final Map<Tree, List<Token>> tree2Tokens) {
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null) {
                    return ;
                }
                
                ts.move((int)sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
                int end = (int)sourcePositions.getEndPosition(info.getCompilationUnit(), tree);
                
                List<Token> tokens = null;
                while(ts.moveNext() && ts.offset() <= end) {
                    Token t = ts.token();
                    if (t.id() == JavaTokenId.IDENTIFIER) {
                        if (tokens == null) {
                            tree2Tokens.put(tree, tokens = new ArrayList<>());
                        }
                        tokens.add(t);
                    }
                }
            }
        });
    }
    
    public void moveBefore(final List<? extends Tree> tArgs) {
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null) {
                    return ;
                }
                
                if (!tArgs.isEmpty()) {
                    int offset = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tArgs.get(0));
                    
                    offset = info.getSnapshot().getOriginalOffset(offset);
                    
                    if (offset < 0) {
                        return ;
                    }
                    
                    while (ts.offset() >= offset) {
                        if (!ts.movePrevious()) {
                            return;
                        }
                    }
                }
            }
        });
    }

    public void moveNext() {
        doc.render(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return ;
                }
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null) {
                    return ;
                }
                
                ts.moveNext();
            }
        });
    }
    
    private static List<TokenSequence<?>> embeddedTokenSequences(TokenHierarchy<Document> th, int offset) {
        TokenSequence<?> embedded = th.tokenSequence();
        List<TokenSequence<?>> sequences = new ArrayList<TokenSequence<?>>();

        do {
            TokenSequence<?> seq = embedded;
            embedded = null;

            seq.move(offset);
            if (seq.moveNext()) {
                sequences.add(seq);
                embedded = seq.embedded();
            }
        } while (embedded != null);
        
        return sequences;
    }
    
}
