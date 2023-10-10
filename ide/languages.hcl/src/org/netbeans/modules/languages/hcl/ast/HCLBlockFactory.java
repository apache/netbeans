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
package org.netbeans.modules.languages.hcl.ast;

import java.util.ArrayList;
import java.util.function.Consumer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;

/**
 *
 * @author lkishalmi
 */
public class HCLBlockFactory {

    private final Consumer<HCLElement.CreateContext> createAction;
    private final HCLExpressionFactory exprFactory;

    private int group = 0;
    private ParserRuleContext prev = null;

    public HCLBlockFactory(Consumer<HCLElement.CreateContext> createAction) {
        this.createAction = createAction;
        this.exprFactory = new HCLExpressionFactory(createAction);
    }
    
    public HCLBlockFactory() {
        this(null);
    }
    
    public final HCLDocument process(HCLParser.ConfigFileContext ctx) {
        var ret = new HCLDocument();
        if (ctx.body() != null) {
            body(ret, ctx.body());
        }
        return ret;
    }
    
    protected HCLBlock block(HCLContainer parent, HCLParser.BlockContext ctx) {
        HCLBlock ret = created(new HCLBlock(parent), ctx);
        
        
        if (ctx.body() != null) {
            body(ret, ctx.body());
        }
        
        ArrayList<HCLIdentifier> decl = new ArrayList<>(4);
        
        if (ctx.children != null) {
            for (ParseTree pt : ctx.children) {
                if (pt instanceof TerminalNode) {
                    Token token = ((TerminalNode) pt).getSymbol();
                    if (token.getType() == HCLLexer.IDENTIFIER) {
                        HCLIdentifier attrName = created(new HCLIdentifier.SimpleId( token.getText()), token);
                        if (pt instanceof ErrorNode) {
                            // This happens most probably while adding a new attribute to a block
                            if (prev != null) {
                                group += prev.stop.getLine() + 1 < token.getLine() ? 1 : 0;
                            }
                            HCLAttribute attr = created(new HCLAttribute(ret, attrName, null, group), token);
                            ret.add(attr);
                        } else {
                            decl.add(attrName);
                        }
                    }
                }
                if (pt instanceof HCLParser.StringLitContext) {
                    HCLParser.StringLitContext slit = (HCLParser.StringLitContext) pt;
                    String sid = slit.getText();
                    if (sid.length() > 1) { // Do not process the '"' string literal
                        sid = sid.substring(1, sid.length() - (sid.endsWith("\"") ? 1 : 0));
                        HCLIdentifier id = created(new HCLIdentifier.StringId(sid), slit);
                        decl.add(id);
                    }
                }
            }
        }
        
        ret.setDeclaration(decl);
        
        return ret;
    }

    protected void body(HCLContainer c, HCLParser.BodyContext ctx) {
        if (ctx.children != null) {
            for (ParseTree pt : ctx.children) {
                if (pt instanceof HCLParser.AttributeContext) {
                    HCLParser.AttributeContext actx = (HCLParser.AttributeContext) pt;
                    if (prev != null) {
                        group += prev.stop.getLine() + 1 < actx.start.getLine() ? 1 : 0;
                    }
                    HCLIdentifier attrName = created(new HCLIdentifier.SimpleId(actx.IDENTIFIER().getText()), actx.IDENTIFIER().getSymbol());
                    HCLExpression attrValue = exprFactory.process(actx.expression());
                    HCLAttribute attr = created(new HCLAttribute(c, attrName, attrValue, group), actx);
                    c.add(attr);
                    prev = actx;
                    continue;
                }
                if (pt instanceof HCLParser.BlockContext) {
                    c.add(block(c, (HCLParser.BlockContext) pt));
                    continue;
                }
                if (pt instanceof ErrorNode) {
                    Token token = ((ErrorNode) pt).getSymbol();
                    if (token.getType() == HCLLexer.IDENTIFIER) {
                        if (prev != null) {
                            group += prev.stop.getLine() + 1 < token.getLine() ? 1 : 0;
                        }
                        HCLIdentifier attrName = created(new HCLIdentifier.SimpleId(token.getText()), token);
                        HCLAttribute attr = new HCLAttribute(c, attrName, null, group);
                        c.add(attr);
                    }
                }
            }
        }
    }
    
    private <E extends HCLElement> E created(E element, Token token) {
        elementCreated(element, token, token);
        return element;
    }

    private <E extends HCLElement> E created(E element, ParserRuleContext ctx) {
        elementCreated(element, ctx.start, ctx.stop);
        return element;
    }

    private void elementCreated(HCLElement element, Token start, Token stop) {
        if (createAction != null) {
            createAction.accept(new HCLElement.CreateContext(element, start, stop));
        }
    }

}
